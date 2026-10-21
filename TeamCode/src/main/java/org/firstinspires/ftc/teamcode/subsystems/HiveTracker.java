package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;

/**
 * Keeps track of which of OUR HIVE's cells is up, and therefore where to aim and which end of the
 * field to shoot from.
 *
 * BIOBUZZ facts this is built on (strategy brief, R2):
 *  - Match start = state A (red: AUDIENCE cell up, shoot from the audience end; blue: far cell up).
 *  - Each TIP flips the HIVE: the cell you just filled dumps its contents toward the wall it faced,
 *    and the OTHER cell comes up facing the OTHER end wall. The shooting zone flips.
 *  - Our alliance partner can tip our HIVE too, so we cannot just count our own balls.
 *  - After a TIP all 8 of our tags face the OTHER half of the field: the camera can NOT see our own TIP
 *    from the zone we fired from. It confirms the new state only once the robot faces the HIVE from the
 *    zone the new up cell faces. So in TELEOP the operator's Y on the human player's "TIP!" is the main
 *    signal; vision is the backup.
 *
 * Ways the state changes:
 *  1. Operator Y ("HIVE tipped", operatorTipped()): flips A <-> B and keeps vision out for
 *     HIVE_OPERATOR_PRIORITY_MS (5 s). If the CAMERA switched the state less than HIVE_Y_CONFIRMS_VISION_MS
 *     (3 s) ago, Y CONFIRMS that switch instead of flipping it back (confirm event, one short operator buzz).
 *  2. Vision evidence (a cell's tags seen high = up, low = down) that disagrees with us for
 *     HIVE_VISION_CONFIRM_MS without a break (only if HIVE_VISION_AUTO_SWITCH, and not while locked out).
 *  3. AUTO: assume(state, protectMs) after a volley (vision locked out for protectMs), or setState().
 *
 * Every change raises a one-shot "tip event": the robot then pauses launching for TIP_PAUSE_MS while it
 * retargets (any requested volley is cancelled) and rumbles the drivers. The change usually comes after
 * the swing, so this pause does NOT protect a tipping HIVE.
 */
public class HiveTracker {

    /** Why the state last changed (shown in telemetry). */
    public enum Source { MATCH_START, OPERATOR, VISION, AUTO }

    private Alliance alliance;
    private HiveState state = HiveState.A;
    private Source lastSource = Source.MATCH_START;
    private long visionLockedUntilMs = 0;
    private long disagreeSinceMs = -1;
    private long tipPauseUntilMs = 0;
    private long lastChangeMs = -100000;
    private boolean tipEventPending = false;
    private boolean confirmEventPending = false;
    private int ballsLaunchedAtCurrentCell = 0;
    private int tipsSeen = 0;

    public HiveTracker(Alliance alliance) {
        this.alliance = alliance;
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /** Call every loop with the latest vision opinion. */
    public void update(Vision vision) {
        update(vision == null ? null : vision.evidence());
    }

    /** Same, with the vision opinion directly (null = no opinion this frame). */
    public void update(HiveState seen) {
        long now = Clock.nowMs();
        boolean visionAllowed = RobotConfig.HIVE_VISION_AUTO_SWITCH && now >= visionLockedUntilMs;
        if (!visionAllowed || seen == null || seen == state) {
            disagreeSinceMs = -1;
            return;
        }
        if (disagreeSinceMs < 0) disagreeSinceMs = now;
        if (now - disagreeSinceMs >= RobotConfig.HIVE_VISION_CONFIRM_MS) {
            change(seen, Source.VISION);
        }
    }

    /**
     * Operator Y: "our HIVE just tipped" (press on every "TIP!"). Flips the target cell, unless the camera
     * switched it less than HIVE_Y_CONFIRMS_VISION_MS ago - then the press only confirms that switch.
     * Either way vision is locked out for HIVE_OPERATOR_PRIORITY_MS.
     * @return true if the press flipped the state, false if it confirmed a camera switch
     */
    public boolean operatorTipped() {
        long now = Clock.nowMs();
        boolean flipped;
        if (lastSource == Source.VISION && now - lastChangeMs < RobotConfig.HIVE_Y_CONFIRMS_VISION_MS) {
            lastSource = Source.OPERATOR;
            confirmEventPending = true;
            flipped = false;
        } else {
            change(state.other(), Source.OPERATOR);
            flipped = true;
        }
        visionLockedUntilMs = now + RobotConfig.HIVE_OPERATOR_PRIORITY_MS;
        return flipped;
    }

    /** Old name kept for the tuning OpModes: a plain flip with the operator's vision lock-out. */
    public void operatorToggle() {
        change(state.other(), Source.OPERATOR);
        visionLockedUntilMs = Clock.nowMs() + RobotConfig.HIVE_OPERATOR_PRIORITY_MS;
    }

    /** Set the state directly (AUTO). Vision may change it again right away. */
    public void setState(HiveState newState, Source source) {
        if (newState != state) change(newState, source);
        else lastSource = source;
    }

    /** AUTO: "assume the HIVE is now in this state" and keep vision out for protectMs (dwell + swing). */
    public void assume(HiveState newState, long protectMs) {
        setState(newState, Source.AUTO);
        visionLockedUntilMs = Math.max(visionLockedUntilMs, Clock.nowMs() + protectMs);
    }

    /** Restore without raising a tip event (TeleOp INIT from PoseStorage). */
    public void restore(HiveState saved) {
        state = saved;
        lastSource = Source.AUTO;
        disagreeSinceMs = -1;
    }

    private void change(HiveState newState, Source source) {
        long now = Clock.nowMs();
        state = newState;
        lastSource = source;
        lastChangeMs = now;
        disagreeSinceMs = -1;
        ballsLaunchedAtCurrentCell = 0;
        tipsSeen++;
        tipEventPending = true;
        tipPauseUntilMs = now + RobotConfig.TIP_PAUSE_MS;
    }

    /** Count one of OUR balls launched at the current cell (for the "4 in... 8, it's going" call-out). */
    public void onBallLaunched() {
        ballsLaunchedAtCurrentCell++;
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    public HiveState state() {
        return state;
    }

    public Source lastSource() {
        return lastSource;
    }

    /** Centre of the up cell's opening: where to aim. */
    public FieldPoint aimPoint() {
        return FieldConstants.aimPoint(alliance, state);
    }

    /** True for TIP_PAUSE_MS after any state change: do not launch. */
    public boolean tipPauseActive() {
        return Clock.nowMs() < tipPauseUntilMs;
    }

    /** True while vision may not change the state (operator Y or AUTO assumption). */
    public boolean visionLockedOut() {
        return Clock.nowMs() < visionLockedUntilMs;
    }

    /** True once after each state change. */
    public boolean consumeTipEvent() {
        boolean e = tipEventPending;
        tipEventPending = false;
        return e;
    }

    /** True once after a Y press confirmed a recent camera switch (no state change). */
    public boolean consumeConfirmEvent() {
        boolean e = confirmEventPending;
        confirmEventPending = false;
        return e;
    }

    public int ballsLaunchedAtCurrentCell() {
        return ballsLaunchedAtCurrentCell;
    }

    public int stateChanges() {
        return tipsSeen;
    }

    /** "AUDIENCE" or "FAR": the end of the field we shoot from in the current state (alliance-aware). */
    public String shootingEnd() {
        boolean audienceEnd = (alliance == Alliance.RED) == (state == HiveState.A);
        return audienceEnd ? "AUDIENCE" : "FAR";
    }

    /** Where to go to shoot, in words, for telemetry: "ZONE B (FAR half)". */
    public String zoneText() {
        return "ZONE " + state + " (" + shootingEnd() + " half)";
    }

    /** Banner after a target switch: "GO TO OUR FAR END". */
    public String goText() {
        return "GO TO OUR " + shootingEnd() + " END";
    }
}
