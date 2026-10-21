package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.HiveState;

import java.util.ArrayList;
import java.util.List;

/**
 * INIT-time menu for AUTO, driven with gamepad1 (G401: all choices must be made before the match -
 * no driver input at all once AUTO starts).
 *
 *   dpad up/down     choose a line
 *   dpad left/right  change the value
 *   A / cross        LOCK (or unlock) the selection - it only stops edits; B does nothing
 *
 * Lines: Alliance (RED/BLUE), Start (S1-S7), Routine (A0, A1, A2, A3 role 1, A3 role 2), Park (INNER /
 * CORNER / SOLO), Delay (0-15 s), Preloads (0-4). First run after the Robot Controller app starts:
 * RED / S1 / A1 / INNER / 0 s / 4. After that every line is remembered until the app restarts (a battery
 * swap or reboot puts it back to RED / S1 / A1 / INNER / 0 / 4). The values shown run whether or not
 * they are locked. EVERY MATCH: set ALLIANCE to this match's colour first, then check every line.
 * Changing the Routine also moves Start to that routine's home spot (S1, or S6 for A3 role 2);
 * change Start afterwards if needed.
 */
public class AutoSelector {

    private enum Item { ALLIANCE, START, ROUTINE, PARK, DELAY, PRELOADS }

    private static final int MAX_DELAY_SEC = 15;

    // remembered between AUTO runs (static) until the Robot Controller app restarts
    private static Alliance alliance = Alliance.RED;
    private static StartSpot start = StartSpot.S1;
    private static AutoRoutineType routine = AutoRoutineType.A1_TIP_AND_PARK;
    private static ParkSlot park = ParkSlot.INNER;
    private static int delaySec = 0;
    private static int preloads = 4;

    private int cursor = 0;
    private boolean locked = false;
    private boolean changed = true;

    /** Read gamepad1 once per INIT loop. */
    public void handle(Gamepad pad) {
        // Read every edge exactly once per loop (wasPressed() clears itself when read).
        boolean up = pad.dpadUpWasPressed();
        boolean down = pad.dpadDownWasPressed();
        boolean left = pad.dpadLeftWasPressed();
        boolean right = pad.dpadRightWasPressed();
        boolean a = pad.aWasPressed();

        if (a) locked = !locked;
        if (locked) return;
        int items = Item.values().length;
        if (up) cursor = (cursor + items - 1) % items;
        if (down) cursor = (cursor + 1) % items;
        if (left || right) {
            change(Item.values()[cursor], right ? +1 : -1);
            changed = true;
        }
    }

    private void change(Item item, int dir) {
        switch (item) {
            case ALLIANCE:
                alliance = alliance.opponent();
                break;
            case START:
                start = StartSpot.values()[wrap(start.ordinal() + dir, StartSpot.values().length)];
                break;
            case ROUTINE:
                routine = AutoRoutineType.values()[wrap(routine.ordinal() + dir, AutoRoutineType.values().length)];
                if (routine.homeStart() != null) start = routine.homeStart();   // avoid a stale start spot
                break;
            case PARK:
                park = ParkSlot.values()[wrap(park.ordinal() + dir, ParkSlot.values().length)];
                break;
            case DELAY:
                delaySec = Math.max(0, Math.min(MAX_DELAY_SEC, delaySec + dir));
                break;
            case PRELOADS:
                preloads = Math.max(0, Math.min(4, preloads + dir));
                break;
        }
    }

    private static int wrap(int i, int n) {
        return ((i % n) + n) % n;
    }

    /** True once after any value changed (the OpMode then resets the start pose). */
    public boolean consumeChanged() {
        boolean c = changed;
        changed = false;
        return c;
    }

    public boolean isLocked() {
        return locked;
    }

    public Alliance alliance() {
        return alliance;
    }

    public StartSpot start() {
        return start;
    }

    public AutoRoutineType routine() {
        return routine;
    }

    /** The Park line as selected (A3 roles ignore it: AutoLegs.parkSlot()). */
    public ParkSlot park() {
        return park;
    }

    public int delaySec() {
        return delaySec;
    }

    public int preloads() {
        return preloads;
    }

    /** True if the routine is built for this start (otherwise MainAuto runs a no-motion plan). */
    public boolean validCombination() {
        return routine.canStartFrom(start);
    }

    /** Combinations that are probably a mistake (shown as "CHECK:" lines in INIT). */
    public List<String> sanityWarnings() {
        List<String> w = new ArrayList<>();
        boolean red = alliance == Alliance.RED;
        if (!validCombination()) {
            w.add(AutoRoutine.invalidText(routine, start)
                    + (routine.shootsStartCellFirst() ? " (use S1-S4)" : routine == AutoRoutineType.A3_ROLE2 ? " (use S6)" : ""));
        }
        if (routine == AutoRoutineType.A3_ROLE2 && validCombination() && !RobotConfig.TURRET_PINNED) {
            Turret.AimSolution s = Turret.solve(FieldConstants.aimPoint(alliance, HiveState.B),
                    FieldConstants.startPose(alliance, start), 0.0, 0.0);
            if (!s.reachable) {
                w.add(String.format(java.util.Locale.US,
                        "A3 role 2 cannot shoot from %s: the second cell is %.0f deg off the turret centre (limits %+.0f/%+.0f). Use S6, or skip A3",
                        start, s.desiredOffsetDeg, RobotConfig.TURRET_MIN_DEG, RobotConfig.TURRET_MAX_DEG));
            }
        }
        if (routine.fixedParkSlot() == null && park == ParkSlot.INNER && (start == StartSpot.S5 || start == StartSpot.S7)) {
            w.add("Park INNER from " + start + " crosses the CORNER slot: use CORNER unless the partner parks after us");
        }
        if (routine.fixedParkSlot() == null && park == ParkSlot.SOLO) {
            w.add("Park SOLO touches both slots: only if the partner does not park in AUTO");
        }
        if (routine != AutoRoutineType.A0_SAFE && preloads == 0) {
            w.add("0 preloads: nothing to shoot");
        }
        if (preloads != 4) {
            w.add("Preloads " + preloads + ": a match starts with exactly 4 (G304.G)");
        }
        if (start == StartSpot.S6) {
            w.add("S6 is only ~1.35 in from the " + (red ? "F-far (far-wall)" : "F-aud (audience-wall)")
                    + " FLOWER: do not touch it (G304.D)");
        }
        if ((routine == AutoRoutineType.A2_TIP_RELOAD_VOLLEY || routine == AutoRoutineType.A3_ROLE1
                || routine == AutoRoutineType.A3_ROLE2) && !Constants.FORESIGHT_TUNED) {
            w.add(routine.name() + " needs a TUNED path follower (pedro/Constants) and, for A3, a rehearsal with the partner");
        }
        return w;
    }

    public void addTelemetry(Telemetry t) {
        t.addLine("##### " + alliance + " ALLIANCE #####  must be THIS match's colour (RED after an app restart)");
        t.addLine("=== 12303 AUTO SELECT ===  dpad up/down: line, left/right: change, A: "
                + (locked ? "UNLOCK" : "LOCK"));
        line(t, Item.ALLIANCE, "Alliance : " + alliance);
        line(t, Item.START, "Start    : " + start + " - " + start.description(alliance));
        line(t, Item.ROUTINE, "Routine  : " + routine.description);
        ParkSlot fixed = routine.fixedParkSlot();
        line(t, Item.PARK, "Park     : " + (fixed == null ? park.label(alliance)
                : (routine == AutoRoutineType.A3_ROLE1 ? "A3 role 1" : "A3 role 2") + " always parks " + fixed.label(alliance)
                + " (line ignored)"));
        line(t, Item.DELAY, "Delay    : " + delaySec + " s");
        line(t, Item.PRELOADS, "Preloads : " + preloads + " POLLEN");
        t.addLine(locked ? ">>> LOCKED <<<" : "(not locked - the values shown WILL run)");
    }

    private void line(Telemetry t, Item item, String text) {
        boolean selected = !locked && Item.values()[cursor] == item;
        t.addLine((selected ? "> " : "  ") + text);
    }
}
