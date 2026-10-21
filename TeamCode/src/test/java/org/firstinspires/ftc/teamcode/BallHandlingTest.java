package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Indexer;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * The REAL Indexer + Intake with fake devices, run loop by loop (10 ms) on a manual clock, in the same
 * order as Robot.update(): indexer (count, colour, feed) then intake (full / refusal).
 */
public class BallHandlingTest {

    private static final long LOOP_MS = 10;

    private FakeHardware.Map2 hw;
    private FakeHardware.CR spinner, roller;
    private FakeHardware.Pos feeder;
    private FakeHardware.Color mouth, slot4;
    private FakeHardware.Beam beam;
    private Indexer idx;
    private Intake intake;
    private boolean shooterReady = true, aimReady = true, still = true, tipPause = false, dip = false;
    private boolean savedAcceptOwn;

    @Before
    public void setUp() {
        Clock.setManualForTests(1_000_000);
        savedAcceptOwn = RobotConfig.ACCEPT_OWN_NECTAR;
        build(false, Alliance.RED);
    }

    @After
    public void tearDown() {
        RobotConfig.ACCEPT_OWN_NECTAR = savedAcceptOwn;
        Clock.useRealTime();
    }

    private void build(boolean withBeam, Alliance alliance) {
        hw = new FakeHardware.Map2();
        spinner = new FakeHardware.CR();
        roller = new FakeHardware.CR();
        feeder = new FakeHardware.Pos();
        mouth = new FakeHardware.Color();
        slot4 = new FakeHardware.Color();
        beam = new FakeHardware.Beam();
        hw.devices.put(RobotConfig.INDEXER, spinner);
        hw.devices.put(RobotConfig.INTAKE, roller);
        hw.devices.put(RobotConfig.FEEDER, feeder);
        hw.devices.put(RobotConfig.MOUTH_COLOR, mouth);
        hw.devices.put(RobotConfig.SLOT4_COLOR, slot4);
        if (withBeam) hw.devices.put(RobotConfig.FEED_BEAM, beam);
        mouth.none();
        slot4.none();
        List<String> w = new ArrayList<>();
        idx = new Indexer(hw, alliance, w);
        intake = new Intake(hw, w);
        idx.arm();
        intake.arm();
        shooterReady = aimReady = still = true;
        tipPause = dip = false;
    }

    private boolean rejectStartedThisLoop;
    private boolean unconfirmedThisLoop;

    /** One robot loop like Robot.update(). */
    private void loop(boolean wantIn, boolean wantOut) {
        intake.request(wantIn, wantOut);
        idx.update(shooterReady, aimReady, still, tipPause, dip, intake.isIntaking(), intake.isReversing());
        dip = false;
        unconfirmedThisLoop = idx.consumeUnconfirmedEvent();
        intake.update(idx.isFull(), idx.refusedBallAtMouth());
        rejectStartedThisLoop = intake.consumeRejectStarted();
        Clock.advanceForTests(LOOP_MS);
    }

    private void run(long ms, boolean in, boolean out) {
        for (long t = 0; t < ms; t += LOOP_MS) loop(in, out);
    }

    private void passBall(boolean in) {
        mouth.pollen();
        run(60, in, false);
        mouth.none();
        run(100, in, false);
    }

    /** One ball's time in the mouth sensor's view, as SensorCheck measures it (MOUTH_ONE_BALL_MAX_MS = this + 30%). */
    private static long oneBallInViewMs() {
        long ms = (long) (RobotConfig.MOUTH_ONE_BALL_MAX_MS / 1.3);
        ms -= ms % LOOP_MS;
        return Math.max(2 * RobotConfig.MOUTH_SENSOR_PERIOD_MS, ms);
    }

    // ---------------------------------------------------------------------------------------------
    @Test
    public void ballsAreCountedOnceTheyHavePassedTheMouth() {
        idx.setCount(0);
        run(50, true, false);
        assertEquals(Intake.State.INTAKING, intake.state());
        assertTrue(roller.power > 0);
        for (int i = 0; i < 2; i++) passBall(true);
        assertEquals(2, idx.count());
        // one ball flickering in and out of view (20 ms gap) counts once (below the last free slot)
        mouth.pollen(); run(40, true, false); mouth.none(); run(20, true, false);
        mouth.pollen(); run(40, true, false); mouth.none(); run(100, true, false);
        assertEquals(3, idx.count());
        passBall(true);
        assertEquals(4, idx.count());
        assertTrue(idx.isFull());
        assertTrue("full event once", idx.consumeFullEvent());
        assertFalse(idx.consumeFullEvent());
    }

    @Test
    public void ball4IsFullyDrawnInBeforeTheFullPulse() {
        idx.setCount(3);
        run(30, true, false);
        mouth.pollen();
        for (long t = 0; t < oneBallInViewMs(); t += LOOP_MS) {      // ball 4 stays in view while it is pulled in
            loop(true, false);
            assertEquals("count must not reach 4 while ball 4 is still at the mouth", 3, idx.count());
            assertTrue("roller must keep pulling ball 4 in (power " + roller.power + ")", roller.power > 0);
            assertEquals(Intake.State.INTAKING, intake.state());
        }
        mouth.none();
        boolean sawPulse = false;
        long pulseMs = 0;
        for (long t = 0; t < 400; t += LOOP_MS) {
            loop(true, false);
            if (intake.state() == Intake.State.FULL_PULSE) {
                sawPulse = true;
                pulseMs += LOOP_MS;
                assertTrue(roller.power < 0);
            }
        }
        assertEquals(4, idx.count());
        assertTrue("the full pulse comes after ball 4 has passed the mouth", sawPulse);
        assertTrue("pulse lasts about INTAKE_FULL_REVERSE_MS", pulseMs <= RobotConfig.INTAKE_FULL_REVERSE_MS + 2 * LOOP_MS);
        assertEquals(Intake.State.OFF, intake.state());
        assertEquals(0.0, roller.power, 0.0);
        // a 5th ball at the mouth is never pulled in and not counted
        mouth.pollen(); run(200, true, false); mouth.none(); run(100, true, false);
        assertEquals(4, idx.count());
        assertTrue(roller.power <= 0);
    }

    /**
     * G407 (the audit's MergeCheck): 3 counted, ball 4 pulled in, then a 5th ball right behind it - gap 0
     * (touching: the sensor never sees a gap) up to 120 ms; slot-4 sensor quiet. Ball 4 is pulled in for as
     * long as it is in view (D3); the 5th ball is pulled only for a moment (a gap of >= 1 sensor read) or
     * until MOUTH_ONE_BALL_MAX_MS (touching), then the full pulse pushes it back out; the count ends at 4.
     */
    @Test
    public void fifthBallRightBehindBall4IsPushedAwayNotPulledIn() {
        long inView = oneBallInViewMs();
        for (long gapMs : new long[]{0, 10, 20, 40, 60, 80, 120}) {
            build(false, Alliance.RED);
            idx.setCount(3);
            run(30, true, false);
            mouth.pollen();                                   // ball 4
            for (long t = 0; t < inView; t += LOOP_MS) {
                loop(true, false);
                assertTrue("gap " + gapMs + ": ball 4 is pulled in while it is in view (D3)", roller.power > 0);
                assertEquals(3, idx.count());
            }
            mouth.none();
            run(gapMs, true, false);
            mouth.pollen();                                   // the 5th ball
            long pulledMs = 0;
            boolean reversed = false;
            for (long t = 0; t < inView; t += LOOP_MS) {
                loop(true, false);
                if (roller.power < 0) reversed = true;
                if (roller.power > 0 && !reversed) pulledMs += LOOP_MS;
            }
            mouth.none();
            run(300, true, false);
            boolean sensorSawAGap = gapMs >= RobotConfig.MOUTH_SENSOR_PERIOD_MS;   // >= 1 empty read
            long limit = sensorSawAGap ? RobotConfig.MOUTH_SENSOR_PERIOD_MS
                    : RobotConfig.MOUTH_ONE_BALL_MAX_MS - inView + 2 * RobotConfig.MOUTH_SENSOR_PERIOD_MS;
            String what = "gap " + gapMs + " ms: 5th ball pulled in for " + pulledMs + " ms (limit " + limit + ")";
            assertTrue(what, pulledMs <= limit);
            assertTrue(what + ": the full pulse must push it back", reversed || pulledMs == 0);
            assertEquals(what, 4, idx.count());
            assertTrue(idx.isFull());
            assertFalse("never pulls again at 4", intake.isIntaking());
        }
    }

    @Test
    public void withThreeCountedABallInViewTooLongIsTakenAsTwoTouchingBalls() {
        // G407 first: one sensor cannot tell "ball 4 slow at the mouth" from "ball 4 + a 5th touching it".
        idx.setCount(3);
        mouth.pollen();
        run(RobotConfig.MOUTH_ONE_BALL_MAX_MS + 200, false, false);   // intake off: not pulled in, not timed
        assertEquals(3, idx.count());
        long reversedAfter = -1;
        for (long t = 0; t < RobotConfig.MOUTH_ONE_BALL_MAX_MS + 200; t += LOOP_MS) {
            loop(true, false);
            if (reversedAfter < 0 && roller.power < 0) reversedAfter = t;
        }
        assertEquals(4, idx.count());
        assertTrue("reversed after " + reversedAfter + " ms", reversedAfter >= RobotConfig.MOUTH_ONE_BALL_MAX_MS
                && reversedAfter <= RobotConfig.MOUTH_ONE_BALL_MAX_MS + 3 * RobotConfig.MOUTH_SENSOR_PERIOD_MS);
        mouth.none();
        run(300, true, false);
        assertEquals("counted once", 4, idx.count());
        assertEquals(Intake.State.OFF, intake.state());
    }

    @Test
    public void featheringTheTriggerOnBall4DoesNotCountItEarly() {
        // Only the time the roller really pulls counts toward MOUTH_ONE_BALL_MAX_MS.
        idx.setCount(3);
        mouth.pollen();
        long part = RobotConfig.MOUTH_ONE_BALL_MAX_MS * 4 / 10;
        part -= part % LOOP_MS;
        run(part, true, false);
        run(2 * RobotConfig.MOUTH_ONE_BALL_MAX_MS, false, false);   // the driver lets go, ball 4 stays at the mouth
        run(part, true, false);
        assertEquals("ball 4 still at the mouth: not counted yet", 3, idx.count());
        assertTrue("still pulling ball 4 in", roller.power > 0);
        mouth.none();
        run(200, true, false);
        assertEquals(4, idx.count());
    }

    @Test
    public void ball4EjectedAndBouncingBackIsNeitherCountedNorSubtracted() {
        idx.setCount(3);
        run(30, true, false);
        mouth.pollen();
        run(100, true, false);                 // ball 4 at the mouth, being pulled in
        idx.setEjecting(true);
        run(40, false, true);                  // operator LB: roller and spinners backward
        mouth.none();
        run(20, false, true);                  // pushed out of view...
        mouth.pollen();
        run(40, false, true);                  // ...bounces back (not a 5th ball behind ball 4)
        mouth.none();
        run(200, false, true);
        idx.setEjecting(false);
        run(50, false, false);
        assertEquals("ball 4 never got in: still 3", 3, idx.count());
    }

    @Test
    public void slot4IsWatchedAtTheMouthRateWhileBall4ComesIn() {
        for (long phase = 0; phase < RobotConfig.SLOT4_SENSOR_PERIOD_MS; phase += LOOP_MS) {
            build(false, Alliance.RED);
            idx.setCount(3);
            run(200 + phase, true, false);
            slot4.cm = 1.0;                                    // ball 4 has reached slot 4
            run(RobotConfig.MOUTH_SENSOR_PERIOD_MS + LOOP_MS, true, false);
            assertTrue("phase " + phase + ": full within one mouth-sensor period", idx.isFull());
            assertEquals(Intake.State.FULL_PULSE, intake.state());
        }
    }

    @Test
    public void opponentNectarIsRefusedForAsLongAsItStaysAtTheMouth() {
        idx.setCount(1);
        run(30, true, false);
        mouth.blue();                                   // RED alliance: blue NECTAR is the opponent's
        loop(true, false);
        int rejectStarts = rejectStartedThisLoop ? 1 : 0;
        for (long t = 0; t < 1000; t += LOOP_MS) {      // the ball is wedged at the mouth for 1 s
            loop(true, false);
            if (rejectStartedThisLoop) rejectStarts++;
            assertEquals("still refusing at " + t + " ms", Intake.State.REJECTING, intake.state());
            assertTrue("roller never pulls a refused ball in", roller.power < 0);
        }
        assertEquals("one rumble per refused ball", 1, rejectStarts);
        mouth.none();
        run(RobotConfig.BALL_COUNT_DEBOUNCE_MS + RobotConfig.INTAKE_REJECT_MS - 3 * LOOP_MS, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        run(200, true, false);
        assertEquals("intake resumes after the refusal", Intake.State.INTAKING, intake.state());
        assertEquals("refused ball is never counted", 1, idx.count());
    }

    @Test
    public void ownNectarIsRefusedInPhase1AndCountedWhenAccepted() {
        RobotConfig.ACCEPT_OWN_NECTAR = false;
        idx.setCount(0);
        run(30, true, false);
        mouth.red();                                    // RED alliance: our own NECTAR
        run(500, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        assertTrue(roller.power < 0);
        mouth.none();
        run(600, true, false);
        assertEquals(0, idx.count());

        RobotConfig.ACCEPT_OWN_NECTAR = true;
        build(false, Alliance.RED);
        idx.setCount(0);
        run(30, true, false);
        mouth.red(); run(60, true, false); mouth.none(); run(100, true, false);
        assertEquals("own NECTAR accepted once ACCEPT_OWN_NECTAR = true", 1, idx.count());
        // ...but the opponent's is always refused
        mouth.blue(); run(60, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
    }

    @Test
    public void blueAllianceRefusesRedNectar() {
        build(false, Alliance.BLUE);
        RobotConfig.ACCEPT_OWN_NECTAR = true;
        idx.setCount(0);
        run(30, true, false);
        mouth.red(); run(300, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        mouth.none(); run(600, true, false);
        mouth.blue(); run(60, true, false); mouth.none(); run(100, true, false);
        assertEquals(1, idx.count());
    }

    @Test
    public void refusedBallIsLeftAloneWhenNobodyPullsIn() {
        idx.setCount(1);
        mouth.blue();
        run(300, false, false);
        assertEquals(Intake.State.OFF, intake.state());
        assertEquals(0.0, roller.power, 0.0);
        // the moment the driver pulls in, the roller reverses instead
        loop(true, false);
        loop(true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        assertTrue(roller.power < 0);
    }

    @Test
    public void ballAtTheMouthCountsOnlyIfItIsPulledIn() {
        idx.setCount(2);
        mouth.pollen(); run(100, false, false); mouth.none(); run(100, false, false);
        assertEquals("touching the mouth with the intake off", 2, idx.count());
        mouth.pollen(); run(50, false, false); run(80, true, false); mouth.none(); run(100, true, false);
        assertEquals("pulled in while it was there", 3, idx.count());
    }

    @Test
    public void slot4SensorForcesFull() {
        idx.setCount(2);
        slot4.cm = 1.0;
        run(150, false, false);
        assertEquals(4, idx.count());
        assertTrue(idx.isFull());
    }

    @Test
    public void volleyWithoutConfirmationSensors() {
        idx.setCount(4);
        idx.requestVolley();
        List<Long> gateOpens = new ArrayList<>();
        boolean wasOpen = false;
        int unconfirmedEvents = 0;
        long t0 = Clock.nowMs();
        for (int i = 0; i < 500 && (idx.count() > 0 || idx.isFiring()); i++) {
            loop(false, false);
            if (unconfirmedThisLoop) unconfirmedEvents++;
            boolean open = feeder.pos == RobotConfig.FEEDER_OPEN_POSITION;
            if (open && !wasOpen) gateOpens.add(Clock.nowMs() - t0);
            wasOpen = open;
        }
        assertEquals(4, gateOpens.size());
        assertEquals(0, idx.count());
        assertEquals(4, idx.unconfirmedShots());
        assertEquals("one operator cue per unconfirmed feed", 4, unconfirmedEvents);
        assertTrue("first gate after staging: " + gateOpens.get(0), gateOpens.get(0) >= RobotConfig.INDEXER_STAGE_MS);
        long minGap = RobotConfig.FEEDER_OPEN_MS + RobotConfig.SHOT_CONFIRM_TIMEOUT_MS + RobotConfig.MIN_SHOT_INTERVAL_MS;
        for (int i = 1; i < gateOpens.size(); i++) {
            assertTrue("gap " + (gateOpens.get(i) - gateOpens.get(i - 1)), gateOpens.get(i) - gateOpens.get(i - 1) >= minGap);
        }
        assertEquals(RobotConfig.FEEDER_CLOSED_POSITION, feeder.pos, 0.0);
        assertEquals(Indexer.FeedState.IDLE, idx.feedState());
    }

    @Test
    public void volleyWithBeamConfirmsEveryShot() {
        build(true, Alliance.RED);
        idx.setCount(4);
        idx.requestVolley();
        List<Long> gateOpens = new ArrayList<>();
        boolean wasOpen = false;
        long t0 = Clock.nowMs();
        for (int i = 0; i < 500 && (idx.count() > 0 || idx.isFiring()); i++) {
            beam.state = feeder.pos != RobotConfig.FEEDER_OPEN_POSITION;   // broken while the gate is open
            loop(false, false);
            boolean open = feeder.pos == RobotConfig.FEEDER_OPEN_POSITION;
            if (open && !wasOpen) gateOpens.add(Clock.nowMs() - t0);
            wasOpen = open;
        }
        assertEquals(4, gateOpens.size());
        assertEquals(4, idx.confirmedShots());
        assertEquals(0, idx.count());
        long minGap = RobotConfig.FEEDER_OPEN_MS + RobotConfig.MIN_SHOT_INTERVAL_MS;
        for (int i = 1; i < gateOpens.size(); i++) {
            long gap = gateOpens.get(i) - gateOpens.get(i - 1);
            assertTrue("gap " + gap, gap >= minGap && gap < minGap + 100);
        }
    }

    @Test
    public void minimumGateCyclesFireBallsTheMouthSensorMissed() {
        idx.setCount(2);                       // e.g. 4 garden balls inside, the mouth sensor saw 2
        idx.requestVolley(4);
        int opens = 0;
        boolean wasOpen = false;
        for (int i = 0; i < 600 && idx.isFiring(); i++) {
            loop(false, false);
            boolean open = feeder.pos == RobotConfig.FEEDER_OPEN_POSITION;
            if (open && !wasOpen) opens++;
            wasOpen = open;
        }
        assertEquals(4, opens);
        assertEquals(0, idx.count());
        // with a beam: stop after 2 empty (unconfirmed) cycles once the count is used up
        build(true, Alliance.RED);
        idx.setCount(0);
        idx.requestVolley(4);
        opens = 0;
        wasOpen = false;
        for (int i = 0; i < 600 && idx.isFiring(); i++) {
            loop(false, false);
            boolean open = feeder.pos == RobotConfig.FEEDER_OPEN_POSITION;
            if (open && !wasOpen) opens++;
            wasOpen = open;
        }
        assertEquals(2, opens);
    }

    @Test
    public void firingWaitsForFlywheelAimAndStillness() {
        idx.setCount(2);
        shooterReady = false;
        idx.requestVolley();
        run(600, false, false);
        assertEquals(RobotConfig.FEEDER_CLOSED_POSITION, feeder.pos, 0.0);
        assertEquals("flywheel", idx.blockedBy());
        shooterReady = true;
        aimReady = false;
        run(100, false, false);
        assertEquals("aim", idx.blockedBy());
        aimReady = true;
        still = false;
        run(100, false, false);
        assertEquals("moving", idx.blockedBy());
        still = true;
        tipPause = true;
        idx.pauseForTip();
        run(100, false, false);
        assertEquals(Indexer.FeedState.TIP_PAUSE, idx.feedState());
        assertFalse(idx.isFiring());
        tipPause = false;
        run(50, false, false);
        assertEquals(Indexer.FeedState.IDLE, idx.feedState());
        assertEquals(2, idx.count());
    }

    @Test
    public void cancelFiringStopsARequestedVolley() {
        idx.setCount(3);
        shooterReady = false;                 // e.g. the operator turns shooting mode OFF
        idx.requestVolley();
        run(200, false, false);
        idx.cancelFiring();
        shooterReady = true;
        run(1000, false, false);
        assertEquals("no ball fired without a new RB/X", 3, idx.count());
        assertEquals(RobotConfig.FEEDER_CLOSED_POSITION, feeder.pos, 0.0);
    }

    @Test
    public void singleShotWorksWithCountZeroAndEjectCountsBallsLeaving() {
        idx.setCount(0);
        idx.requestSingleShot();
        run(1200, false, false);
        assertEquals(1, idx.unconfirmedShots());
        assertEquals(0, idx.count());

        build(false, Alliance.RED);
        idx.setCount(3);
        idx.setEjecting(true);
        run(50, false, true);
        assertTrue(spinner.power < 0 && roller.power < 0);
        assertEquals(Indexer.FeedState.EJECTING, idx.feedState());
        mouth.pollen(); run(60, false, true); mouth.none(); run(100, false, true);
        assertEquals("a ball leaving through the mouth while ejecting", 2, idx.count());
        idx.setEjecting(false);
        run(50, false, false);
        assertEquals(Indexer.FeedState.IDLE, idx.feedState());
    }
}
