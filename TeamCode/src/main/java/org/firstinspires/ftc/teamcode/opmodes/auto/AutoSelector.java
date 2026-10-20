package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.ArrayList;
import java.util.List;

/**
 * INIT-time menu for AUTO, driven with gamepad1 (G401: all choices must be made before the match -
 * no driver input at all once AUTO starts).
 *
 *   dpad up/down     choose a line
 *   dpad left/right  change the value
 *   A / cross        LOCK (or unlock) the selection
 *
 * The last selection is remembered until the Robot Controller app restarts, so between matches you
 * usually only check it and press A.
 */
public class AutoSelector {

    private enum Item { ALLIANCE, START, ROUTINE, DELAY, PRELOADS }

    private static final int MAX_DELAY_SEC = 15;

    // remembered between AUTO runs (static)
    private static Alliance alliance = Alliance.RED;
    private static StartSpot start = StartSpot.S1;
    private static AutoRoutineType routine = AutoRoutineType.A1_TIP_AND_PARK;
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

    public int delaySec() {
        return delaySec;
    }

    public int preloads() {
        return preloads;
    }

    /** Combinations that are probably a mistake. */
    public List<String> sanityWarnings() {
        List<String> w = new ArrayList<>();
        if (routine.shootsStartCellFirst() && start != StartSpot.S1 && start != StartSpot.S2) {
            w.add(routine.name() + " is designed for S1 (or S2) - check the start spot!");
        }
        if (routine == AutoRoutineType.A3_ROLE2 && start != StartSpot.S6 && start != StartSpot.S7) {
            w.add("A3 role 2 is designed for S6 (or S7) on the far wall");
        }
        if (routine != AutoRoutineType.A0_SAFE && preloads == 0) {
            w.add("0 preloads: nothing to shoot");
        }
        if (start == StartSpot.S6) {
            w.add("S6 is only ~1.35 in from the F-far FLOWER: do not touch it (G304.D)");
        }
        return w;
    }

    public void addTelemetry(Telemetry t) {
        t.addLine("=== 12303 AUTO SELECT ===  dpad up/down: line, left/right: change, A: "
                + (locked ? "UNLOCK" : "LOCK"));
        line(t, Item.ALLIANCE, "Alliance : " + alliance);
        line(t, Item.START, "Start    : " + start + " - " + start.description);
        line(t, Item.ROUTINE, "Routine  : " + routine.description);
        line(t, Item.DELAY, "Delay    : " + delaySec + " s");
        line(t, Item.PRELOADS, "Preloads : " + preloads + " POLLEN");
        t.addLine(locked ? ">>> LOCKED <<<" : "(not locked - the values shown WILL run)");
    }

    private void line(Telemetry t, Item item, String text) {
        boolean selected = !locked && Item.values()[cursor] == item;
        t.addLine((selected ? "> " : "  ") + text);
    }
}
