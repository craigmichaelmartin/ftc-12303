package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoRoutine;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoRoutineType;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The REAL AutoRoutine + Robot on an empty fake hardware map, on a manual clock, loop by loop (10 ms) like
 * MainAuto. No Pinpoint: paths are skipped, so only the step TIMING runs. No flywheel: a volley never fires,
 * which is the worst case for the A3 time budget (a stalled volley).
 */
public class AutoTimingTest {

    private boolean savedFireWithoutTip;

    @Before
    public void setUp() {
        Clock.setManualForTests(1_000_000);
        savedFireWithoutTip = RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM;
    }

    @After
    public void tearDown() {
        RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = savedFireWithoutTip;
        Clock.useRealTime();
    }

    /** Runs 30 s of AUTO; returns {s after START when VOLLEY_2 began (-1 = never), s when the park drive began}. */
    private static double[] runAuto(AutoRoutineType type, StartSpot start, double delaySec) {
        Robot robot = new Robot(new FakeHardware.Map2(), Alliance.RED);
        robot.drive.setPose(FieldConstants.startPose(Alliance.RED, start));
        robot.indexer.setCount(4);
        robot.arm();
        AutoRoutine routine = new AutoRoutine(robot, type, start, ParkSlot.CORNER, delaySec);
        routine.start(0.0);
        double volley2 = -1, park = -1;
        for (int i = 0; i <= 3000; i++) {
            double t = i / 100.0;
            routine.update(t);
            robot.update();
            if (volley2 < 0 && routine.step() == AutoRoutine.Step.VOLLEY_2) volley2 = t;
            if (park < 0 && (routine.step() == AutoRoutine.Step.DRIVE_TO_PARK || routine.step() == AutoRoutine.Step.PARKED)) {
                park = t;
            }
            Clock.advanceForTests(10);
        }
        return new double[]{volley2, park};
    }

    @Test
    public void a3Role2LeavesAtItsGateWhenItsVolleyStalls() {
        // Fire at 9 s without a confirmed TIP: the volley then starts as late as it ever can. It stalls (no
        // flywheel); its own 5 s timeout would end it at 14 s - too late for role 1's 17 s gate.
        RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = true;
        double[] t = runAuto(AutoRoutineType.A3_ROLE2, StartSpot.S6, 0);
        assertEquals("volley 2 starts at the latest fire time", RobotConfig.A3_ROLE2_LATEST_FIRE_SEC, t[0], 0.02);
        assertEquals("role 2 leaves at its A3 gate, not at the volley timeout", RobotConfig.A3_ROLE2_LEAVE_BY_SEC, t[1], 0.02);
    }

    @Test
    public void a3Role2WithALongStartDelayStillLeavesAtItsGate() {
        double[] t = runAuto(AutoRoutineType.A3_ROLE2, StartSpot.S6, 15);
        assertEquals("no volley", -1, t[0], 0.0);
        assertEquals(RobotConfig.A3_ROLE2_LEAVE_BY_SEC, t[1], 0.02);
    }

    @Test
    public void a3Role2WithoutATipParksWhenItStopsWaiting() {
        double[] t = runAuto(AutoRoutineType.A3_ROLE2, StartSpot.S6, 0);
        assertEquals("no TIP seen: volley 2 is skipped at once (keeps the preloads)", t[0], t[1], 0.02);
        assertEquals(RobotConfig.A3_ROLE2_TIP_WAIT_TIMEOUT_SEC, t[1], 0.03);
    }

    @Test
    public void otherRoutinesKeepTheNormalParkDeadline() {
        double[] t = runAuto(AutoRoutineType.A1_TIP_AND_PARK, StartSpot.S1, 25);
        assertEquals(RobotConfig.AUTO_PARK_DEADLINE_SEC, t[1], 0.02);
    }
}
