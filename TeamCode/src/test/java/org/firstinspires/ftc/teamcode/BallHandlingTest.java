package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Carrier;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * The REAL Carrier + Intake with fake devices, run loop by loop (10 ms) on a manual clock, in the same
 * order as Robot.update(): carrier (count, colour, feed) then intake (full / refusal).
 */
public class BallHandlingTest {

    private static final long LOOP_MS = 10;

    private FakeHardware.Map2 hw;
    private FakeHardware.CR carrierWheel, roller;
    private FakeHardware.Color mouth, slot4;
    private FakeHardware.Beam beam;
    private Carrier carrier;
    private Intake intake;
    private List<String> warnings;
    private boolean shooterReady = true, aimReady = true, still = true, tipPause = false, dip = false;
    private boolean savedAcceptOwn, savedCountUnconfirmed;
    private double savedIntakePower;

    @Before
    public void setUp() {
        Clock.setManualForTests(1_000_000);
        savedAcceptOwn = RobotConfig.ACCEPT_OWN_NECTAR;
        savedCountUnconfirmed = RobotConfig.COUNT_UNCONFIRMED_SHOTS;
        savedIntakePower = RobotConfig.CARRIER_INTAKE_POWER;
        build(false, Alliance.RED);
    }

    @After
    public void tearDown() {
        RobotConfig.ACCEPT_OWN_NECTAR = savedAcceptOwn;
        RobotConfig.COUNT_UNCONFIRMED_SHOTS = savedCountUnconfirmed;
        RobotConfig.CARRIER_INTAKE_POWER = savedIntakePower;
        Clock.useRealTime();
    }

    private void build(boolean withBeam, Alliance alliance) {
        build(withBeam, alliance, true, true);
    }

    private void build(boolean withBeam, Alliance alliance, boolean withMouthSensor, boolean arm) {
        hw = new FakeHardware.Map2();
        carrierWheel = new FakeHardware.CR();
        roller = new FakeHardware.CR();
        mouth = new FakeHardware.Color();
        slot4 = new FakeHardware.Color();
        beam = new FakeHardware.Beam();
        hw.devices.put(RobotConfig.CARRIER, carrierWheel);
        hw.devices.put(RobotConfig.INTAKE, roller);
        if (withMouthSensor) hw.devices.put(RobotConfig.MOUTH_COLOR, mouth);
        hw.devices.put(RobotConfig.SLOT4_COLOR, slot4);
        if (withBeam) hw.devices.put(RobotConfig.FEED_BEAM, beam);
        mouth.none();
        slot4.none();
        warnings = new ArrayList<>();
        carrier = new Carrier(hw, alliance, warnings);
        intake = new Intake(hw, warnings);
        if (arm) {
            carrier.arm();
            intake.arm();
        }
        shooterReady = aimReady = still = true;
        tipPause = dip = false;
    }

    private boolean rejectStartedThisLoop;
    private boolean unconfirmedThisLoop;

    /** One robot loop like Robot.update(). */
    private void loop(boolean wantIn, boolean wantOut) {
        intake.request(wantIn, wantOut);
        carrier.update(shooterReady, aimReady, still, tipPause, dip, intake.isIntaking(), intake.isReversing());
        dip = false;
        unconfirmedThisLoop = carrier.consumeUnconfirmedEvent();
        intake.update(carrier.isFull(), carrier.refusedBallAtMouth());
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

    /** Carrier pushes seen while a volley/shot runs: start times and how long each push lasted (ms). */
    private static final class Pushes {
        final List<Long> starts = new ArrayList<>();
        final List<Long> lengths = new ArrayList<>();
        int unconfirmedEvents = 0;
    }

    /** Run loops until nothing is left to fire (or maxLoops), recording every carrier push. */
    private Pushes fireAndRecord(int maxLoops, Long beamAfterMs) {
        Pushes p = new Pushes();
        long t0 = Clock.nowMs();
        long pushStart = -1;
        for (int i = 0; i < maxLoops && (carrier.count() > 0 || carrier.isFiring()); i++) {
            long now = Clock.nowMs();
            if (beamAfterMs != null) {
                // the ball breaks the feed beam beamAfterMs into a push (state false = broken: LOW)
                beam.state = !(pushStart >= 0 && now - pushStart >= beamAfterMs && now - pushStart < beamAfterMs + 40);
            }
            loop(false, false);
            if (unconfirmedThisLoop) p.unconfirmedEvents++;
            boolean pushing = carrierWheel.power > 0;
            if (pushing && pushStart < 0) {
                pushStart = now;
                p.starts.add(now - t0);
            } else if (!pushing && pushStart >= 0) {
                p.lengths.add(now - pushStart);
                pushStart = -1;
            }
        }
        return p;
    }

    // ---------------------------------------------------------------------------------------------
    // Counting at the mouth (mouth sensor fitted)
    // ---------------------------------------------------------------------------------------------
    @Test
    public void ballsAreCountedOnceTheyHavePassedTheMouth() {
        carrier.setCount(0);
        run(50, true, false);
        assertEquals(Intake.State.INTAKING, intake.state());
        assertTrue(roller.power > 0);
        for (int i = 0; i < 2; i++) passBall(true);
        assertEquals(2, carrier.count());
        // one ball flickering in and out of view (20 ms gap) counts once (below the last free slot)
        mouth.pollen(); run(40, true, false); mouth.none(); run(20, true, false);
        mouth.pollen(); run(40, true, false); mouth.none(); run(100, true, false);
        assertEquals(3, carrier.count());
        passBall(true);
        assertEquals(4, carrier.count());
        assertTrue(carrier.isFull());
        assertTrue("full event once", carrier.consumeFullEvent());
        assertFalse(carrier.consumeFullEvent());
    }

    @Test
    public void ball4IsFullyDrawnInBeforeTheFullPulse() {
        carrier.setCount(3);
        run(30, true, false);
        mouth.pollen();
        for (long t = 0; t < oneBallInViewMs(); t += LOOP_MS) {      // ball 4 stays in view while it is pulled in
            loop(true, false);
            assertEquals("count must not reach 4 while ball 4 is still at the mouth", 3, carrier.count());
            assertTrue("intake must keep pulling ball 4 in (power " + roller.power + ")", roller.power > 0);
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
        assertEquals(4, carrier.count());
        assertTrue("the full pulse comes after ball 4 has passed the mouth", sawPulse);
        assertTrue("pulse lasts about INTAKE_FULL_REVERSE_MS", pulseMs <= RobotConfig.INTAKE_FULL_REVERSE_MS + 2 * LOOP_MS);
        assertEquals(Intake.State.OFF, intake.state());
        assertEquals(0.0, roller.power, 0.0);
        // a 5th ball at the mouth is never pulled in and not counted
        mouth.pollen(); run(200, true, false); mouth.none(); run(100, true, false);
        assertEquals(4, carrier.count());
        assertTrue(roller.power <= 0);
    }

    /**
     * G407: 3 counted, ball 4 pulled in, then a 5th ball right behind it - gap 0 (touching: the sensor never
     * sees a gap) up to 120 ms; slot-4 sensor quiet. Ball 4 is pulled in for as long as it is in view; the
     * 5th ball is pulled only for a moment (a gap of >= 1 sensor read) or until MOUTH_ONE_BALL_MAX_MS
     * (touching), then the full pulse pushes it back out; the count ends at 4.
     */
    @Test
    public void fifthBallRightBehindBall4IsPushedAwayNotPulledIn() {
        long inView = oneBallInViewMs();
        for (long gapMs : new long[]{0, 10, 20, 40, 60, 80, 120}) {
            build(false, Alliance.RED);
            carrier.setCount(3);
            run(30, true, false);
            mouth.pollen();                                   // ball 4
            for (long t = 0; t < inView; t += LOOP_MS) {
                loop(true, false);
                assertTrue("gap " + gapMs + ": ball 4 is pulled in while it is in view", roller.power > 0);
                assertEquals(3, carrier.count());
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
            assertEquals(what, 4, carrier.count());
            assertTrue(carrier.isFull());
            assertFalse("never pulls again at 4", intake.isIntaking());
        }
    }

    @Test
    public void withThreeCountedABallInViewTooLongIsTakenAsTwoTouchingBalls() {
        // G407 first: one sensor cannot tell "ball 4 slow at the mouth" from "ball 4 + a 5th touching it".
        carrier.setCount(3);
        mouth.pollen();
        run(RobotConfig.MOUTH_ONE_BALL_MAX_MS + 200, false, false);   // intake off: not pulled in, not timed
        assertEquals(3, carrier.count());
        long reversedAfter = -1;
        for (long t = 0; t < RobotConfig.MOUTH_ONE_BALL_MAX_MS + 200; t += LOOP_MS) {
            loop(true, false);
            if (reversedAfter < 0 && roller.power < 0) reversedAfter = t;
        }
        assertEquals(4, carrier.count());
        assertTrue("reversed after " + reversedAfter + " ms", reversedAfter >= RobotConfig.MOUTH_ONE_BALL_MAX_MS
                && reversedAfter <= RobotConfig.MOUTH_ONE_BALL_MAX_MS + 3 * RobotConfig.MOUTH_SENSOR_PERIOD_MS);
        mouth.none();
        run(300, true, false);
        assertEquals("counted once", 4, carrier.count());
        assertEquals(Intake.State.OFF, intake.state());
    }

    @Test
    public void featheringTheTriggerOnBall4DoesNotCountItEarly() {
        // Only the time the intake really pulls counts toward MOUTH_ONE_BALL_MAX_MS.
        carrier.setCount(3);
        mouth.pollen();
        long part = RobotConfig.MOUTH_ONE_BALL_MAX_MS * 4 / 10;
        part -= part % LOOP_MS;
        run(part, true, false);
        run(2 * RobotConfig.MOUTH_ONE_BALL_MAX_MS, false, false);   // the driver lets go, ball 4 stays at the mouth
        run(part, true, false);
        assertEquals("ball 4 still at the mouth: not counted yet", 3, carrier.count());
        assertTrue("still pulling ball 4 in", roller.power > 0);
        mouth.none();
        run(200, true, false);
        assertEquals(4, carrier.count());
    }

    @Test
    public void ball4EjectedAndBouncingBackIsNeitherCountedNorSubtracted() {
        carrier.setCount(3);
        run(30, true, false);
        mouth.pollen();
        run(100, true, false);                 // ball 4 at the mouth, being pulled in
        carrier.setEjecting(true);
        run(40, false, true);                  // operator LB: intake and carrier backward
        mouth.none();
        run(20, false, true);                  // pushed out of view...
        mouth.pollen();
        run(40, false, true);                  // ...bounces back (not a 5th ball behind ball 4)
        mouth.none();
        run(200, false, true);
        carrier.setEjecting(false);
        run(50, false, false);
        assertEquals("ball 4 never got in: still 3", 3, carrier.count());
    }

    @Test
    public void slot4IsWatchedAtTheMouthRateWhileBall4ComesIn() {
        for (long phase = 0; phase < RobotConfig.SLOT4_SENSOR_PERIOD_MS; phase += LOOP_MS) {
            build(false, Alliance.RED);
            carrier.setCount(3);
            run(200 + phase, true, false);
            slot4.cm = 1.0;                                    // ball 4 has reached slot 4
            run(RobotConfig.MOUTH_SENSOR_PERIOD_MS + LOOP_MS, true, false);
            assertTrue("phase " + phase + ": full within one mouth-sensor period", carrier.isFull());
            assertEquals(Intake.State.FULL_PULSE, intake.state());
        }
    }

    @Test
    public void opponentNectarIsRefusedForAsLongAsItStaysAtTheMouth() {
        carrier.setCount(1);
        run(30, true, false);
        mouth.blue();                                   // RED alliance: blue NECTAR is the opponent's
        loop(true, false);
        int rejectStarts = rejectStartedThisLoop ? 1 : 0;
        for (long t = 0; t < 1000; t += LOOP_MS) {      // the ball is wedged at the mouth for 1 s
            loop(true, false);
            if (rejectStartedThisLoop) rejectStarts++;
            assertEquals("still refusing at " + t + " ms", Intake.State.REJECTING, intake.state());
            assertTrue("intake never pulls a refused ball in", roller.power < 0);
        }
        assertEquals("one rumble per refused ball", 1, rejectStarts);
        mouth.none();
        run(RobotConfig.BALL_COUNT_DEBOUNCE_MS + RobotConfig.INTAKE_REJECT_MS - 3 * LOOP_MS, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        run(200, true, false);
        assertEquals("intake resumes after the refusal", Intake.State.INTAKING, intake.state());
        assertEquals("refused ball is never counted", 1, carrier.count());
    }

    @Test
    public void ownNectarIsRefusedUntilAcceptedAndCountedWhenAccepted() {
        RobotConfig.ACCEPT_OWN_NECTAR = false;
        carrier.setCount(0);
        run(30, true, false);
        mouth.red();                                    // RED alliance: our own NECTAR
        run(500, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        assertTrue(roller.power < 0);
        mouth.none();
        run(600, true, false);
        assertEquals(0, carrier.count());

        RobotConfig.ACCEPT_OWN_NECTAR = true;
        build(false, Alliance.RED);
        carrier.setCount(0);
        run(30, true, false);
        mouth.red(); run(60, true, false); mouth.none(); run(100, true, false);
        assertEquals("own NECTAR accepted once ACCEPT_OWN_NECTAR = true", 1, carrier.count());
        // ...but the opponent's is always refused
        mouth.blue(); run(60, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
    }

    @Test
    public void blueAllianceRefusesRedNectar() {
        build(false, Alliance.BLUE);
        RobotConfig.ACCEPT_OWN_NECTAR = true;
        carrier.setCount(0);
        run(30, true, false);
        mouth.red(); run(300, true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        mouth.none(); run(600, true, false);
        mouth.blue(); run(60, true, false); mouth.none(); run(100, true, false);
        assertEquals(1, carrier.count());
    }

    @Test
    public void refusedBallIsLeftAloneWhenNobodyPullsIn() {
        carrier.setCount(1);
        mouth.blue();
        run(300, false, false);
        assertEquals(Intake.State.OFF, intake.state());
        assertEquals(0.0, roller.power, 0.0);
        // the moment the driver pulls in, the intake reverses instead
        loop(true, false);
        loop(true, false);
        assertEquals(Intake.State.REJECTING, intake.state());
        assertTrue(roller.power < 0);
    }

    @Test
    public void ballAtTheMouthCountsOnlyIfItIsPulledIn() {
        carrier.setCount(2);
        mouth.pollen(); run(100, false, false); mouth.none(); run(100, false, false);
        assertEquals("touching the mouth with the intake off", 2, carrier.count());
        mouth.pollen(); run(50, false, false); run(80, true, false); mouth.none(); run(100, true, false);
        assertEquals("pulled in while it was there", 3, carrier.count());
    }

    @Test
    public void slot4SensorForcesFull() {
        carrier.setCount(2);
        slot4.cm = 1.0;
        run(150, false, false);
        assertEquals(4, carrier.count());
        assertTrue(carrier.isFull());
    }

    // ---------------------------------------------------------------------------------------------
    // The carrier: holds the balls, feeds them one at a time
    // ---------------------------------------------------------------------------------------------
    @Test
    public void nothingMovesBeforeArm() {
        build(false, Alliance.RED, true, false);       // TeleOp / AUTO INIT: built, not armed (G403, G304.H)
        carrier.setCount(4);
        carrier.requestVolley();
        mouth.pollen();
        run(1000, true, false);
        assertEquals("carrier never commanded before START", 0, carrierWheel.writes);
        assertEquals("intake never commanded before START", 0, roller.writes);
    }

    @Test
    public void carrierHoldsTheBallsWhileIntakingUnlessAskedToHelp() {
        carrier.setCount(1);
        run(300, true, false);
        assertEquals(Intake.State.INTAKING, intake.state());
        assertEquals("CARRIER_INTAKE_POWER = 0: the carrier holds the balls", 0.0, carrierWheel.power, 0.0);

        RobotConfig.CARRIER_INTAKE_POWER = 0.3;
        run(50, true, false);
        assertEquals(0.3, carrierWheel.power, 1e-9);
        run(50, false, false);
        assertEquals("intake off: carrier stopped", 0.0, carrierWheel.power, 0.0);
        // while a shot waits for the flywheel the carrier stays stopped, even with the intake running
        shooterReady = false;
        carrier.requestSingleShot();
        run(300, true, false);
        assertEquals(Carrier.FeedState.WAIT_FOR_READY, carrier.feedState());
        assertEquals("no ball may reach a flywheel that is not ready", 0.0, carrierWheel.power, 0.0);
    }

    @Test
    public void volleyWithoutConfirmationSensors() {
        carrier.setCount(4);
        carrier.requestVolley();
        Pushes p = fireAndRecord(500, null);
        assertEquals(4, p.starts.size());
        assertEquals(0, carrier.count());
        assertEquals(4, carrier.unconfirmedShots());
        assertEquals("one operator cue per unconfirmed feed", 4, p.unconfirmedEvents);
        for (long len : p.lengths) {
            assertTrue("each push lasts CARRIER_FEED_MS: " + len,
                    len >= RobotConfig.CARRIER_FEED_MS && len <= RobotConfig.CARRIER_FEED_MS + LOOP_MS);
        }
        long minGap = RobotConfig.CARRIER_FEED_MS + RobotConfig.SHOT_CONFIRM_TIMEOUT_MS + RobotConfig.MIN_SHOT_INTERVAL_MS;
        for (int i = 1; i < p.starts.size(); i++) {
            long gap = p.starts.get(i) - p.starts.get(i - 1);
            assertTrue("gap " + gap, gap >= minGap && gap < minGap + 3 * LOOP_MS);
        }
        assertEquals(0.0, carrierWheel.power, 0.0);
        assertEquals(Carrier.FeedState.IDLE, carrier.feedState());
    }

    @Test
    public void volleyWithBeamConfirmsEveryShotAndStopsTheCarrierAtOnce() {
        build(true, Alliance.RED);
        carrier.setCount(4);
        carrier.requestVolley();
        long beamAfterMs = 100;
        Pushes p = fireAndRecord(500, beamAfterMs);
        assertEquals(4, p.starts.size());
        assertEquals(4, carrier.confirmedShots());
        assertEquals(0, carrier.unconfirmedShots());
        assertEquals(0, carrier.count());
        for (long len : p.lengths) {
            assertTrue("push stops when the beam confirms the shot: " + len, len <= beamAfterMs + 2 * LOOP_MS);
        }
        for (int i = 1; i < p.starts.size(); i++) {
            long gap = p.starts.get(i) - p.starts.get(i - 1);
            long min = beamAfterMs + RobotConfig.MIN_SHOT_INTERVAL_MS;
            assertTrue("gap " + gap, gap >= min && gap < min + 50);
        }
    }

    @Test
    public void aFlywheelDipConfirmsTheShotAndStopsTheCarrier() {
        carrier.setCount(3);
        carrier.requestSingleShot();
        long pushedMs = 0;
        for (int i = 0; i < 100 && carrierWheel.power <= 0; i++) loop(false, false);
        assertTrue("the push started", carrierWheel.power > 0);
        while (pushedMs < 120) {
            loop(false, false);
            pushedMs += LOOP_MS;
        }
        assertTrue("still pushing before the dip", carrierWheel.power > 0);
        dip = true;                                     // the ball hits the shooting wheel
        loop(false, false);
        assertEquals("the carrier stops in the same loop", 0.0, carrierWheel.power, 0.0);
        assertFalse("confirmed: no unconfirmed cue", unconfirmedThisLoop);
        assertEquals(1, carrier.confirmedShots());
        assertEquals(2, carrier.count());
        run(500, false, false);
        assertEquals("one ball per single shot", 0.0, carrierWheel.power, 0.0);
        assertEquals(Carrier.FeedState.IDLE, carrier.feedState());
    }

    @Test
    public void timedPushRunsTheWholeFeedTimeEvenWhenTheDipConfirmsEarly() {
        carrier.setCount(2);                            // Tune: Flywheel RB, 2 balls inside
        carrier.requestTimedPush();
        long[] push = pushWithDipAfter(100);
        long len = push[1] - push[0];
        assertTrue("a timed push lasts exactly CARRIER_FEED_MS whatever the dip says: " + len,
                len >= RobotConfig.CARRIER_FEED_MS && len <= RobotConfig.CARRIER_FEED_MS + LOOP_MS);
        run(RobotConfig.SHOT_CONFIRM_TIMEOUT_MS + 100, false, false);
        assertEquals("the dip during the push still confirms the shot", 1, carrier.confirmedShots());
        assertEquals(0, carrier.unconfirmedShots());
        assertEquals(1, carrier.count());
        assertEquals(Carrier.FeedState.IDLE, carrier.feedState());
        // The next normal feed (B) stops at the dip again: the timed push does not stick.
        carrier.requestSingleShot();
        push = pushWithDipAfter(100);
        assertTrue("a normal feed stops at the dip: " + (push[1] - push[0]), push[1] - push[0] <= 100 + 2 * LOOP_MS);
        assertEquals(2, carrier.confirmedShots());
    }

    /** Runs until one carrier push has ended; the flywheel dips dipAfterMs into it. Returns {start, end} ms. */
    private long[] pushWithDipAfter(long dipAfterMs) {
        long start = -1, end = -1;
        for (int i = 0; i < 300 && end < 0; i++) {
            long now = Clock.nowMs();
            if (start >= 0 && now - start == dipAfterMs) dip = true;     // the ball hits the shooting wheel
            loop(false, false);
            boolean pushing = carrierWheel.power > 0;
            if (pushing && start < 0) start = now;
            if (!pushing && start >= 0) end = now;
        }
        assertTrue("a push started and ended", start >= 0 && end > start);
        return new long[]{start, end};
    }

    @Test
    public void nextBallWaitsUntilTheFlywheelIsBackAtSpeedAndTheAimIsGood() {
        carrier.setCount(2);
        carrier.requestVolley();
        for (int i = 0; i < 100 && carrierWheel.power <= 0; i++) loop(false, false);
        run(50, false, false);
        dip = true;                                     // ball 1 confirmed...
        shooterReady = false;                           // ...and the flywheel has slowed down
        loop(false, false);
        run(400, false, false);
        assertEquals(0.0, carrierWheel.power, 0.0);
        assertEquals("flywheel", carrier.blockedBy());
        shooterReady = true;
        aimReady = false;                               // the shot knocked the robot off its aim
        run(200, false, false);
        assertEquals(0.0, carrierWheel.power, 0.0);
        assertEquals("aim", carrier.blockedBy());
        aimReady = true;
        loop(false, false);
        assertTrue("ball 2 is fed once flywheel and aim are ready", carrierWheel.power > 0);
    }

    @Test
    public void minimumFeedsFireBallsTheMouthSensorMissed() {
        carrier.setCount(2);                   // e.g. 4 garden balls inside, the mouth sensor saw 2
        carrier.requestVolley(4);
        Pushes p = fireAndRecord(600, null);
        assertEquals(4, p.starts.size());
        assertEquals(0, carrier.count());
        // with a beam: stop after 2 empty (unconfirmed) feeds once the count is used up
        build(true, Alliance.RED);
        carrier.setCount(0);
        carrier.requestVolley(4);
        p = fireAndRecord(600, null);
        assertEquals(2, p.starts.size());
    }

    @Test
    public void unconfirmedFeedsThatDoNotCountStopAVolleyAfterTwo() {
        RobotConfig.COUNT_UNCONFIRMED_SHOTS = false;
        carrier.setCount(3);
        carrier.requestVolley();
        int pushes = 0;
        boolean was = false;
        for (int i = 0; i < 1000 && carrier.isFiring(); i++) {
            loop(false, false);
            boolean pushing = carrierWheel.power > 0;
            if (pushing && !was) pushes++;
            was = pushing;
        }
        assertFalse("the volley ends", carrier.isFiring());
        assertEquals(2, pushes);
        assertEquals("only confirmed shots lower the count", 3, carrier.count());
    }

    @Test
    public void firingWaitsForFlywheelAimAndStillness() {
        carrier.setCount(2);
        shooterReady = false;
        carrier.requestVolley();
        run(600, false, false);
        assertEquals(0.0, carrierWheel.power, 0.0);
        assertEquals("flywheel", carrier.blockedBy());
        shooterReady = true;
        aimReady = false;
        run(100, false, false);
        assertEquals("aim", carrier.blockedBy());
        aimReady = true;
        still = false;
        run(100, false, false);
        assertEquals("moving", carrier.blockedBy());
        assertEquals("stopped = holding the balls", 0.0, carrierWheel.power, 0.0);
        still = true;
        tipPause = true;
        carrier.pauseForTip();
        run(100, false, false);
        assertEquals(Carrier.FeedState.TIP_PAUSE, carrier.feedState());
        assertFalse(carrier.isFiring());
        tipPause = false;
        run(50, false, false);
        assertEquals(Carrier.FeedState.IDLE, carrier.feedState());
        assertEquals(2, carrier.count());
        assertEquals(0.0, carrierWheel.power, 0.0);
    }

    @Test
    public void cancelFiringStopsARequestedVolley() {
        carrier.setCount(3);
        shooterReady = false;                 // e.g. the operator turns shooting mode OFF
        carrier.requestVolley();
        run(200, false, false);
        carrier.cancelFiring();
        shooterReady = true;
        run(1000, false, false);
        assertEquals("no ball fired without a new RB/X", 3, carrier.count());
        assertEquals(0.0, carrierWheel.power, 0.0);
    }

    @Test
    public void singleShotWorksWithCountZeroAndEjectRunsCarrierAndIntakeBackward() {
        carrier.setCount(0);
        carrier.requestSingleShot();
        run(1200, false, false);
        assertEquals(1, carrier.unconfirmedShots());
        assertEquals(0, carrier.count());

        build(false, Alliance.RED);
        carrier.setCount(3);
        carrier.setEjecting(true);
        run(50, false, true);
        assertTrue("eject: carrier and intake backward", carrierWheel.power < 0 && roller.power < 0);
        assertEquals(RobotConfig.CARRIER_EJECT_POWER, carrierWheel.power, 1e-9);
        assertEquals(Carrier.FeedState.EJECTING, carrier.feedState());
        mouth.pollen(); run(60, false, true); mouth.none(); run(100, false, true);
        assertEquals("a ball leaving through the mouth while ejecting", 2, carrier.count());
        carrier.setEjecting(false);
        run(50, false, false);
        assertEquals(Carrier.FeedState.IDLE, carrier.feedState());
        assertEquals(0.0, carrierWheel.power, 0.0);
    }

    // ---------------------------------------------------------------------------------------------
    // No mouth sensor (the robot as built: no ball sensors yet)
    // ---------------------------------------------------------------------------------------------
    @Test
    public void withoutAMouthSensorTheCountStartsAtThePreloadsAndOnlyDrops() {
        build(false, Alliance.RED, false, true);
        assertFalse(carrier.hasMouthSensor());
        boolean warned = false;
        for (String w : warnings) warned |= w.contains("no mouth sensor");
        assertTrue("INIT warning without a mouth sensor: " + warnings, warned);

        carrier.setCount(4);                            // 4 preloads
        run(100, true, false);
        assertTrue("4 preloads = full: the intake refuses", intake.refusedBecauseFull());
        assertEquals(Intake.State.OFF, intake.state());

        carrier.requestVolley();
        Pushes p = fireAndRecord(600, null);
        assertEquals(4, p.starts.size());
        assertEquals(0, carrier.count());

        run(500, true, false);                          // balls come in: nothing can count them
        assertEquals(Intake.State.INTAKING, intake.state());
        assertEquals(0, carrier.count());
        carrier.requestVolley();                        // so a volley still feeds 4 times
        int pushes = 0;
        boolean was = false;
        for (int i = 0; i < 600 && carrier.isFiring(); i++) {
            loop(false, false);
            boolean pushing = carrierWheel.power > 0;
            if (pushing && !was) pushes++;
            was = pushing;
        }
        assertEquals(RobotConfig.MAX_BALLS, pushes);
        carrier.adjustCount(+1);                        // operator correction
        assertEquals(1, carrier.count());
    }

    @Test
    public void carrierCanBeADcMotor() {
        hw = new FakeHardware.Map2();
        FakeHardware.MotorState motor = new FakeHardware.MotorState();
        hw.devices.put(RobotConfig.CARRIER, FakeHardware.motor(motor));
        carrier = new Carrier(hw, Alliance.RED, new ArrayList<String>());
        assertTrue(carrier.isPresent());
        assertEquals(RobotConfig.CARRIER_DIRECTION, motor.direction);
        assertEquals(DcMotor.RunMode.RUN_WITHOUT_ENCODER, motor.mode);
        assertEquals("BRAKE: a stopped carrier holds the balls", DcMotor.ZeroPowerBehavior.BRAKE, motor.zeroPower);
        assertEquals("nothing moves when it is set up", 0, motor.writes);
        carrier.arm();
        carrier.setCount(1);
        carrier.requestSingleShot();
        boolean pushed = false;
        for (int i = 0; i < 200 && (carrier.isFiring() || i < 5); i++) {
            carrier.update(true, true, true, false, false, false, false);
            if (motor.power == RobotConfig.CARRIER_FEED_POWER) pushed = true;
            Clock.advanceForTests(LOOP_MS);
        }
        assertTrue("the motor pushed the ball", pushed);
        assertEquals(0.0, motor.power, 0.0);
        assertEquals(0, carrier.count());
    }

    @Test
    public void intakeCanBeADcMotor() {
        hw = new FakeHardware.Map2();
        FakeHardware.MotorState motor = new FakeHardware.MotorState();
        hw.devices.put(RobotConfig.INTAKE, FakeHardware.motor(motor));   // the Yellow Jacket on the front arm
        intake = new Intake(hw, new ArrayList<String>());
        assertTrue(intake.isPresent());
        assertEquals(RobotConfig.INTAKE_DIRECTION, motor.direction);
        assertEquals(DcMotor.RunMode.RUN_WITHOUT_ENCODER, motor.mode);
        assertEquals("FLOAT when stopped (the carrier holds the balls, not the intake)",
                DcMotor.ZeroPowerBehavior.FLOAT, motor.zeroPower);
        intake.request(true, false);
        intake.update(false, false);
        assertEquals("nothing moves before START (G403)", 0, motor.writes);
        assertEquals(Intake.State.DISABLED, intake.state());
        intake.arm();
        intake.request(true, false);
        intake.update(false, false);
        assertEquals(Intake.State.INTAKING, intake.state());
        assertEquals(RobotConfig.INTAKE_IN_POWER, motor.power, 1e-9);
        intake.request(false, true);
        intake.update(false, false);
        assertEquals(RobotConfig.INTAKE_OUT_POWER, motor.power, 1e-9);
        intake.request(true, false);
        intake.update(true, false);                     // full (4 counted): "in" is refused
        assertEquals(Intake.State.OFF, intake.state());
        assertTrue(intake.refusedBecauseFull());
        assertEquals(0.0, motor.power, 0.0);
    }
}
