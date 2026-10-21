package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** The real HiveTracker on a manual clock, fed scripted vision evidence (10 ms loops). */
public class HiveTrackerTest {

    private HiveTracker hive;

    @Before
    public void setUp() {
        Clock.setManualForTests(5_000_000);
        hive = new HiveTracker(Alliance.RED);
    }

    @After
    public void tearDown() {
        Clock.useRealTime();
    }

    /** Run loops for ms with this vision evidence; returns how many tip events happened. */
    private int run(long ms, HiveState evidence) {
        int events = 0;
        for (long t = 0; t < ms; t += 10) {
            hive.update(evidence);
            if (hive.consumeTipEvent()) events++;
            Clock.advanceForTests(10);
        }
        return events;
    }

    @Test
    public void visionNeedsUnbrokenEvidence() {
        assertEquals(0, run(400, HiveState.B));
        run(20, null);                                   // one gap resets the clock
        assertEquals(0, run(400, HiveState.B));
        assertEquals(HiveState.A, hive.state());
        assertEquals(1, run(200, HiveState.B));
        assertEquals(HiveState.B, hive.state());
        assertEquals(HiveTracker.Source.VISION, hive.lastSource());
    }

    @Test
    public void yConfirmsARecentCameraSwitchInsteadOfFlippingBack() {
        assertEquals(1, run(RobotConfig.HIVE_VISION_CONFIRM_MS + 50, HiveState.B));   // camera switched
        run(600, null);                                  // human player calls TIP!, operator reacts
        boolean flipped = hive.operatorTipped();
        assertFalse(flipped);
        assertEquals(HiveState.B, hive.state());
        assertEquals(HiveTracker.Source.OPERATOR, hive.lastSource());
        assertTrue("confirm event for the short operator buzz", hive.consumeConfirmEvent());
        assertFalse("no new tip event, no TIP pause", hive.consumeTipEvent());
        assertTrue(hive.visionLockedOut());
        // a second press means "really flip it"
        assertTrue(hive.operatorTipped());
        assertEquals(HiveState.A, hive.state());
        assertTrue(hive.consumeTipEvent());
    }

    @Test
    public void yLongAfterACameraSwitchFlips() {
        run(RobotConfig.HIVE_VISION_CONFIRM_MS + 50, HiveState.B);
        run(RobotConfig.HIVE_Y_CONFIRMS_VISION_MS + 100, null);
        assertTrue(hive.operatorTipped());
        assertEquals(HiveState.A, hive.state());
        assertFalse(hive.consumeConfirmEvent());
    }

    @Test
    public void yFlipsAndKeepsVisionOutForFiveSeconds() {
        assertTrue(hive.operatorTipped());               // our own TIP: the camera cannot see it from here
        assertEquals(HiveState.B, hive.state());
        hive.consumeTipEvent();
        assertTrue(hive.tipPauseActive());
        assertEquals(0, run(RobotConfig.HIVE_OPERATOR_PRIORITY_MS - 100, HiveState.A));
        assertEquals(HiveState.B, hive.state());
        assertEquals(1, run(100 + RobotConfig.HIVE_VISION_CONFIRM_MS + 50, HiveState.A));
        assertEquals(HiveState.A, hive.state());
    }

    @Test
    public void autoAssumptionSurvivesTheDwell() {
        hive.assume(HiveState.B, RobotConfig.AUTO_TIP_ASSUME_PROTECT_MS);
        assertTrue(hive.consumeTipEvent());
        // the start cell is still up (vision says A) during the dwell: must not undo the assumption
        assertEquals(0, run(RobotConfig.AUTO_TIP_ASSUME_PROTECT_MS - 100, HiveState.A));
        assertEquals(HiveState.B, hive.state());
        assertEquals(HiveTracker.Source.AUTO, hive.lastSource());
        // after the protect window a cell that is really still up wins (no TIP happened)
        assertEquals(1, run(100 + RobotConfig.HIVE_VISION_CONFIRM_MS + 50, HiveState.A));
        assertEquals(HiveState.A, hive.state());
    }

    @Test
    public void zoneTextsAreAllianceAware() {
        HiveTracker red = new HiveTracker(Alliance.RED);
        HiveTracker blue = new HiveTracker(Alliance.BLUE);
        assertEquals("AUDIENCE", red.shootingEnd());
        assertEquals("FAR", blue.shootingEnd());
        assertEquals("GO TO OUR AUDIENCE END", red.goText());
        red.setState(HiveState.B, HiveTracker.Source.AUTO);
        blue.setState(HiveState.B, HiveTracker.Source.AUTO);
        assertEquals("FAR", red.shootingEnd());
        assertEquals("AUDIENCE", blue.shootingEnd());
        assertEquals("ZONE B (AUDIENCE half)", blue.zoneText());
    }
}
