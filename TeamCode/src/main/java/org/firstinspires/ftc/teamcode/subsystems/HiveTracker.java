package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;

/**
 * Keeps track of which of OUR HIVE's cells is up, and therefore where to aim and which half of the
 * field to shoot from.
 *
 * BIOBUZZ facts this is built on (strategy brief, R2):
 *  - Match start = state A (red: AUDIENCE cell up, shoot from the audience half; blue: far cell up).
 *  - Each TIP flips the HIVE: the cell you just filled dumps its contents toward the wall it faced,
 *    and the OTHER cell comes up facing the OTHER end wall. The shooting zone flips.
 *  - Our alliance partner can tip our HIVE too, so we cannot just count our own balls.
 *
 * Three ways the state changes:
 *  1. Operator presses "HIVE tipped" (always wins; vision cannot undo it for HIVE_OPERATOR_PRIORITY_MS).
 *  2. Vision evidence (a cell's tags seen high = up, low = down) that disagrees with us for
 *     HIVE_VISION_CONFIRM_MS (only if HIVE_VISION_AUTO_SWITCH).
 *  3. AUTO sets it directly (e.g. "we fired 3+ balls into the start cell, assume it tipped").
 *
 * Every change raises a one-shot "tip event": the robot then pauses launching for TIP_PAUSE_MS
 * (manual 10.5.1: launching at a moving HIVE may stop the TIP from counting) and rumbles the drivers.
 */
public class HiveTracker {

    /** Why the state last changed (shown in telemetry). */
    public enum Source { MATCH_START, OPERATOR, VISION, AUTO }

    private Alliance alliance;
    private HiveState state = HiveState.A;
    private Source lastSource = Source.MATCH_START;
    private long operatorPriorityUntilMs = 0;
    private long disagreeSinceMs = -1;
    private long tipPauseUntilMs = 0;
    private boolean tipEventPending = false;
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
        long now = System.currentTimeMillis();
        HiveState seen = (vision == null) ? null : vision.evidence();
        boolean visionAllowed = RobotConfig.HIVE_VISION_AUTO_SWITCH && now >= operatorPriorityUntilMs;
        if (!visionAllowed || seen == null || seen == state) {
            disagreeSinceMs = -1;
            return;
        }
        if (disagreeSinceMs < 0) disagreeSinceMs = now;
        if (now - disagreeSinceMs >= RobotConfig.HIVE_VISION_CONFIRM_MS) {
            change(seen, Source.VISION);
        }
    }

    /** Operator button: "our HIVE just tipped" (flips A <-> B). */
    public void operatorToggle() {
        change(state.other(), Source.OPERATOR);
        operatorPriorityUntilMs = System.currentTimeMillis() + RobotConfig.HIVE_OPERATOR_PRIORITY_MS;
    }

    /** Set the state directly (AUTO, or restoring the AUTO -> TeleOp hand-off). */
    public void setState(HiveState newState, Source source) {
        if (newState != state) change(newState, source);
        else lastSource = source;
    }

    /** Restore without raising a tip event (TeleOp INIT from PoseStorage). */
    public void restore(HiveState saved) {
        state = saved;
        lastSource = Source.AUTO;
    }

    private void change(HiveState newState, Source source) {
        state = newState;
        lastSource = source;
        disagreeSinceMs = -1;
        ballsLaunchedAtCurrentCell = 0;
        tipsSeen++;
        tipEventPending = true;
        tipPauseUntilMs = System.currentTimeMillis() + RobotConfig.TIP_PAUSE_MS;
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
        return System.currentTimeMillis() < tipPauseUntilMs;
    }

    /** True once after each state change. */
    public boolean consumeTipEvent() {
        boolean e = tipEventPending;
        tipEventPending = false;
        return e;
    }

    public int ballsLaunchedAtCurrentCell() {
        return ballsLaunchedAtCurrentCell;
    }

    public int stateChanges() {
        return tipsSeen;
    }

    /** Where to go to shoot, in words, for telemetry. */
    public String zoneText() {
        boolean audienceHalf = (alliance == Alliance.RED) == (state == HiveState.A);
        return "ZONE " + state + (audienceHalf ? " (AUDIENCE half)" : " (FAR half)");
    }
}
