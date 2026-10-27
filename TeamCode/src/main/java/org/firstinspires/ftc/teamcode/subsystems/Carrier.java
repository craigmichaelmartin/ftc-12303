package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.BallColor;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Ball handling inside the robot: the ball COUNT, the optional ball sensors and the CARRIER wheel above
 * the tunnel. The carrier does two jobs: stopped, it holds the balls (a gate); spinning, it pushes the
 * ball under it into the shooting wheel (it feeds). It may be a DC motor or a continuous-rotation servo.
 *
 * WHY THE COUNT MATTERS (possession discipline is a design requirement):
 *  - G407: at most 4 scoring elements CONTROLLED at once. No "momentary" exception (Q&A 3), and a
 *    routine "grab a 5th, then spit it out" design is likely STRATEGIC: MAJOR FOUL + YELLOW CARD.
 *  - G408: never CONTROL the opponent's NECTAR (no momentary exception, Q&A 4/23).
 * So: the count starts at the INIT Preloads value (4 in a match), the intake is not allowed to pull when
 * we are full, and a REFUSED ball at the mouth makes the intake spin backward for as long as it is there.
 * Refused = the opponent's NECTAR, and our own NECTAR too while RobotConfig.ACCEPT_OWN_NECTAR = false
 * (POLLEN only until NECTAR passes its tests). The slot-4 sensor is the safety net: if it sees a ball, we
 * ARE full, whatever the count says.
 *
 * EVERY BALL SENSOR IS OPTIONAL. Without the mouth sensor the robot cannot count balls coming in or refuse
 * NECTAR: the count starts at the Preloads value and drops by one per feed, the operator fixes it with
 * +1 / -1, and a plain volley feeds at least MAX_BALLS times (the code cannot know how many balls came
 * in; empty feeds are harmless). The drivers stop intaking at 4 balls by eye and steer around NECTAR.
 * INIT shows a loud warning (Robot.addBallSensorWarning).
 *
 * COUNTING AT THE MOUTH: a ball is counted when it LEAVES the mouth sensor's view after being pulled in
 * (and has been gone BALL_COUNT_DEBOUNCE_MS) - so the 4th ball is fully drawn in before "full" stops and
 * reverses the intake. A ball that leaves while the intake pushes outward, or a refused ball, is not counted.
 * LAST FREE SLOT (3 counted): a 5th ball right behind ball 4 must not follow it in (G407), and one sensor
 * cannot always see the gap between two touching balls. So with 3 counted, anything back in view before the
 * debounce has ended counts ball 4 at once (the full pulse then pushes the newcomer away), and so does a
 * ball still in view MOUTH_ONE_BALL_MAX_MS after the intake started pulling it (two balls touching). The
 * slot-4 sensor is read every MOUTH_SENSOR_PERIOD_MS while ball 4 comes in.
 *
 * FEED SEQUENCE (one ball):
 *   WAIT_FOR_READY - carrier stopped (holding the balls); wait until the flywheel is at speed, the aim is
 *                    good, the chassis is still, no TIP pause, and MIN_SHOT_INTERVAL_MS since the last ball.
 *   FEEDING        - carrier at CARRIER_FEED_POWER until the shot is confirmed (flywheel dip, or the feed
 *                    beam if fitted) - then it stops at once - or CARRIER_FEED_MS at most, then it stops.
 *                    (A TIMED push - Tune: Flywheel RB - always runs exactly CARRIER_FEED_MS.)
 *   CONFIRMING     - carrier stopped; a push that ended without a confirmation waits up to
 *                    SHOT_CONFIRM_TIMEOUT_MS for a late dip / beam break. None = an "unconfirmed feed"
 *                    (counted as fired when COUNT_UNCONFIRMED_SHOTS, reported once for the operator rumble).
 *   Count - 1. Then the next ball of a volley goes back to WAIT_FOR_READY (so it waits for the flywheel to
 *   be back at speed and the aim to be good), or IDLE. There is no automatic jam retry.
 * While the intake pulls balls in (and nothing is being fired) the carrier runs at CARRIER_INTAKE_POWER
 * (0 = stopped). Eject (operator LB) runs it backward, together with the intake.
 *
 * G403 / G304.H: no carrier power before arm() (TeleOp START; AUTO START).
 */
public class Carrier {

    public enum FeedState {
        /** Carrier stopped, nothing to fire. */
        IDLE,
        /** A shot is requested; carrier stopped, waiting for shooter / aim / chassis. */
        WAIT_FOR_READY,
        /** Carrier spinning: one ball is going into the shooting wheel. */
        FEEDING,
        /** Carrier stopped again, waiting (briefly) to confirm the ball left. */
        CONFIRMING,
        /** The target cell just changed: no launching for a moment while we retarget. */
        TIP_PAUSE,
        /** Operator is emptying the robot backward through the intake. */
        EJECTING
    }

    // hardware (any of these may be null)
    private final DcMotorSimple carrier;
    private final NormalizedColorSensor mouthColor;
    private final DistanceSensor mouthDistance;
    private final NormalizedColorSensor slot4Color;
    private final DistanceSensor slot4Distance;
    private final DigitalChannel feedBeam;

    private Alliance alliance;
    private boolean armed = false;
    private int count = 0;

    // sensors
    private long lastMouthReadMs = -1000, lastSlot4ReadMs = -1000;
    private BallColor mouthBall = BallColor.NONE;
    private double mouthHue = 0, mouthSaturation = 0, mouthDistanceCm = Double.NaN;
    private long mouthAbsentSinceMs = -1;
    private boolean slot4Occupied = false;
    private double slot4DistanceCm = Double.NaN;
    private boolean beamBroken = false;
    private long lastIntakeOutMs = -100000;

    // the ball currently at the mouth ("presence"), from first sighting until it is gone
    private boolean presenceActive = false;
    private boolean presenceRefused = false;
    private boolean presencePulledIn = false;
    private boolean presenceOutgoing = false;
    /** Already counted while still in view (last free slot: two balls touching). */
    private boolean presenceCounted = false;
    private long presenceStartMs = 0, presenceLastSeenMs = 0;
    /** How long the intake has pulled this presence in so far (sum over mouth reads while intaking). */
    private long presencePulledMs = 0;
    /** How long the last ball was in the mouth sensor's view, and the longest since reset (SensorCheck). */
    private long lastPresenceMs = -1, longestPresenceMs = -1;
    /** The intake was pushing outward when the ball disappeared from view (it went OUT, not in). */
    private boolean leftWhileReversing = false;

    // feeding
    private FeedState feedState = FeedState.IDLE;
    private long stateStartMs = 0;
    private long lastShotMs = -100000;
    private boolean volleyRequested = false;
    private boolean singleRequested = false;
    private boolean ejectRequested = false;
    /** The requested single ball is a TIMED push (Tune: Flywheel RB): it ignores the shot confirmation. */
    private boolean timedPushRequested = false;
    /** The push running now ignores the shot confirmation and lasts exactly CARRIER_FEED_MS. */
    private boolean timedFeed = false;
    private boolean shotSeenThisFeed = false;
    private int volleyMinFeeds = 0;
    private int volleyFeeds = 0;
    private int consecutiveUnconfirmed = 0;
    private int confirmedShots = 0, unconfirmedShots = 0;
    private String blockedBy = "";

    // events for other subsystems (each is reported once)
    private boolean fullEvent = false;
    private boolean shotEvent = false;
    private boolean unconfirmedEvent = false;
    private boolean wasFull = false;

    // output (only written when it changes)
    private double lastCarrierPower = Double.NaN;

    public Carrier(HardwareMap hw, Alliance alliance, List<String> warnings) {
        this.alliance = alliance;
        carrier = OptionalHardware.get(hw, DcMotorSimple.class, RobotConfig.CARRIER, warnings);
        mouthColor = OptionalHardware.getQuiet(hw, NormalizedColorSensor.class, RobotConfig.MOUTH_COLOR);
        mouthDistance = OptionalHardware.getQuiet(hw, DistanceSensor.class, RobotConfig.MOUTH_COLOR);
        slot4Color = OptionalHardware.getQuiet(hw, NormalizedColorSensor.class, RobotConfig.SLOT4_COLOR);
        slot4Distance = OptionalHardware.getQuiet(hw, DistanceSensor.class, RobotConfig.SLOT4_COLOR);
        feedBeam = OptionalHardware.getQuiet(hw, DigitalChannel.class, RobotConfig.FEED_BEAM);

        if (carrier != null) {
            carrier.setDirection(RobotConfig.CARRIER_DIRECTION);
            if (carrier instanceof DcMotor) {
                // A DC-motor carrier: plain power control, and BRAKE so a stopped carrier holds the balls.
                // Nothing moves here (G403).
                ((DcMotor) carrier).setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                ((DcMotor) carrier).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            }
        }
        if (feedBeam != null) feedBeam.setMode(DigitalChannel.Mode.INPUT);
        setupColorSensor(mouthColor);
        setupColorSensor(slot4Color);
        if (mouthColor == null) {
            warnings.add("no mouth sensor '" + RobotConfig.MOUTH_COLOR
                    + "': balls coming in are not counted and NECTAR is not refused (G407/G408)");
        }
    }

    private static void setupColorSensor(NormalizedColorSensor sensor) {
        if (sensor == null) return;
        sensor.setGain(RobotConfig.COLOR_SENSOR_GAIN);
        if (sensor instanceof SwitchableLight) ((SwitchableLight) sensor).enableLight(true);
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /** Allow the carrier to move (TeleOp START / AUTO START - G403, G304.H). */
    public void arm() {
        armed = true;
    }

    // ---------------------------------------------------------------------------------------------
    // Ball count
    // ---------------------------------------------------------------------------------------------
    public int count() {
        return count;
    }

    /** Set the count (AUTO: number of preloads; TeleOp: value handed over from AUTO). */
    public void setCount(int balls) {
        count = Math.max(0, Math.min(RobotConfig.MAX_BALLS, balls));
        wasFull = isFull();
    }

    /** Operator correction (+1 / -1). */
    public void adjustCount(int delta) {
        setCount(count + delta);
    }

    /** Full = 4 counted, or the slot-4 sensor sees a ball (the sensor wins - G407 safety). */
    public boolean isFull() {
        return count >= RobotConfig.MAX_BALLS || slot4Occupied;
    }

    /** True if this ball colour must not be taken in (G408; own NECTAR too until NECTAR is accepted). */
    public boolean isRefusedColor(BallColor c) {
        return alliance.isOpponentColor(c) || (!RobotConfig.ACCEPT_OWN_NECTAR && alliance.isOwnColor(c));
    }

    /** True while a refused ball is at the mouth (until it has been gone BALL_COUNT_DEBOUNCE_MS). */
    public boolean refusedBallAtMouth() {
        return presenceActive && presenceRefused;
    }

    // ---------------------------------------------------------------------------------------------
    // Requests
    // ---------------------------------------------------------------------------------------------
    /**
     * Fire every ball we have, one at a time, each only when everything is ready. Without a mouth sensor
     * (balls coming in are not counted) it feeds at least MAX_BALLS times.
     */
    public void requestVolley() {
        requestVolley(hasMouthSensor() ? 0 : RobotConfig.MAX_BALLS);
    }

    /**
     * Fire every ball we have, and feed at least minFeeds times even if the count says fewer (AUTO after
     * the garden sweep, where a ball may have missed the mouth sensor). Empty feeds are harmless; with a
     * feed beam fitted the volley stops after 2 unconfirmed (empty) feeds in a row.
     */
    public void requestVolley(int minFeeds) {
        if (feedState == FeedState.EJECTING) return;
        volleyRequested = true;
        volleyMinFeeds = Math.max(0, minFeeds);
        volleyFeeds = 0;
        consecutiveUnconfirmed = 0;
    }

    /** Fire one ball (works even if the count says 0, in case the count is wrong). */
    public void requestSingleShot() {
        if (feedState == FeedState.EJECTING) return;
        singleRequested = true;
        timedPushRequested = false;
    }

    /**
     * Tune: Flywheel only (07 Exp 0b): one TIMED push. Like requestSingleShot() (it waits for the same
     * gates), but the carrier runs for exactly CARRIER_FEED_MS even when a flywheel dip or the beam confirms
     * the shot early - the push the carrier makes when nothing confirms a shot - so CARRIER_FEED_MS can be
     * tuned: long enough for one ball, too short for two. A dip or beam break during the push still counts
     * the ball as confirmed.
     */
    public void requestTimedPush() {
        if (feedState == FeedState.EJECTING) return;
        singleRequested = true;
        timedPushRequested = true;
    }

    /** Stop firing after the current ball. */
    public void cancelFiring() {
        volleyRequested = false;
        singleRequested = false;
        timedPushRequested = false;
        volleyMinFeeds = 0;
    }

    public boolean isFiring() {
        return volleyRequested || singleRequested || feedState == FeedState.FEEDING
                || feedState == FeedState.CONFIRMING;
    }

    /** Hold to run the carrier backward (empty the robot through the intake). */
    public void setEjecting(boolean eject) {
        ejectRequested = eject;
    }

    /** The target cell changed: stop launching right now (a ball already being pushed still goes). */
    public void pauseForTip() {
        cancelFiring();
        if (feedState == FeedState.WAIT_FOR_READY || feedState == FeedState.IDLE) go(FeedState.TIP_PAUSE);
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    /**
     * @param shooterReady   flywheel at speed
     * @param aimReady       aim OK (heading inside the aim tolerance; always true with manual aim)
     * @param chassisStill   robot not moving / turning (stop to shoot; always true with manual aim)
     * @param tipPause       HiveTracker says the target cell just changed
     * @param shotDip        flywheel reported a speed dip this loop
     * @param intakeIn       intake wheel is pulling balls in
     * @param intakeOut      intake wheel is pushing outward (eject, refusal, full pulse)
     */
    public void update(boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause,
                       boolean shotDip, boolean intakeIn, boolean intakeOut) {
        long now = Clock.nowMs();
        if (intakeOut) lastIntakeOutMs = now;
        readMouth(now, intakeIn);
        readSlot4(now, intakeIn);
        readBeam();
        if (shotDip) shotSeenThisFeed = true;

        boolean full = isFull();
        if (full && !wasFull) fullEvent = true;
        wasFull = full;

        runFeedStateMachine(now, shooterReady, aimReady, chassisStill, tipPause);
        if (armed) writeOutput(intakeIn);
    }

    private boolean moreToFire() {
        boolean unconfirmedRun = consecutiveUnconfirmed >= 2;
        // With COUNT_UNCONFIRMED_SHOTS = false the count only drops on confirmed shots: stop after 2
        // unconfirmed feeds in a row instead of feeding an empty (or jammed) robot forever.
        boolean countLeft = count > 0 && (RobotConfig.COUNT_UNCONFIRMED_SHOTS || !unconfirmedRun);
        boolean minFeedsLeft = volleyFeeds < volleyMinFeeds && !(feedBeam != null && unconfirmedRun);
        return countLeft || minFeedsLeft;
    }

    private void runFeedStateMachine(long now, boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause) {
        long inState = now - stateStartMs;
        if (ejectRequested && feedState != FeedState.FEEDING && feedState != FeedState.EJECTING) {
            cancelFiring();
            go(FeedState.EJECTING);
            return;
        }
        switch (feedState) {
            case IDLE:
                if (singleRequested || (volleyRequested && moreToFire())) {
                    go(tipPause ? FeedState.TIP_PAUSE : FeedState.WAIT_FOR_READY);
                } else {
                    volleyRequested = false;   // nothing left to fire
                }
                break;

            case WAIT_FOR_READY:
                if (!volleyRequested && !singleRequested) {
                    go(FeedState.IDLE);
                } else if (volleyRequested && !singleRequested && !moreToFire()) {
                    volleyRequested = false;   // volley finished
                    go(FeedState.IDLE);
                } else if (tipPause) {
                    cancelFiring();
                    go(FeedState.TIP_PAUSE);
                } else {
                    blockedBy = whatBlocks(now, shooterReady, aimReady, chassisStill);
                    if (blockedBy.isEmpty()) {
                        shotSeenThisFeed = false;
                        timedFeed = timedPushRequested;
                        timedPushRequested = false;
                        go(FeedState.FEEDING);
                    }
                }
                break;

            case FEEDING:
                if (shotSeenThisFeed && !timedFeed) {
                    ballLeft(true, now);                 // confirmed: the carrier stops at once
                } else if (inState >= RobotConfig.CARRIER_FEED_MS) {
                    go(FeedState.CONFIRMING);            // pushed long enough for one ball: stop
                }
                break;

            case CONFIRMING:
                if (shotSeenThisFeed) {
                    ballLeft(true, now);
                } else if (inState >= RobotConfig.SHOT_CONFIRM_TIMEOUT_MS) {
                    ballLeft(false, now);
                }
                break;

            case TIP_PAUSE:
                if (!tipPause) go(FeedState.IDLE);
                break;

            case EJECTING:
                if (!ejectRequested) go(FeedState.IDLE);
                break;
        }
    }

    /** Empty string = nothing blocks the shot. */
    private String whatBlocks(long now, boolean shooterReady, boolean aimReady, boolean chassisStill) {
        if (now - lastShotMs < RobotConfig.MIN_SHOT_INTERVAL_MS) return "interval";
        if (!shooterReady) return "flywheel";
        if (!aimReady) return "aim";
        if (!chassisStill) return "moving";
        return "";
    }

    private void ballLeft(boolean confirmed, long now) {
        lastShotMs = now;
        volleyFeeds++;
        if (confirmed) {
            confirmedShots++;
            consecutiveUnconfirmed = 0;
        } else {
            unconfirmedShots++;
            consecutiveUnconfirmed++;
            unconfirmedEvent = true;
        }
        if (confirmed || RobotConfig.COUNT_UNCONFIRMED_SHOTS) {
            if (count > 0) count--;
            shotEvent = true;
        }
        singleRequested = false;
        go((volleyRequested && moreToFire()) ? FeedState.WAIT_FOR_READY : FeedState.IDLE);
        if (feedState == FeedState.IDLE) {
            volleyRequested = false;
            volleyMinFeeds = 0;
        }
    }

    private void go(FeedState next) {
        feedState = next;
        stateStartMs = Clock.nowMs();
        if (next != FeedState.WAIT_FOR_READY) blockedBy = "";
    }

    // ---------------------------------------------------------------------------------------------
    // Sensors
    // ---------------------------------------------------------------------------------------------
    private void readMouth(long now, boolean intakeIn) {
        if (mouthColor == null || now - lastMouthReadMs < RobotConfig.MOUTH_SENSOR_PERIOD_MS) return;
        lastMouthReadMs = now;
        mouthDistanceCm = (mouthDistance != null) ? mouthDistance.getDistance(DistanceUnit.CM) : Double.NaN;
        NormalizedRGBA rgba = mouthColor.getNormalizedColors();
        double[] hsv = toHsv(rgba.red, rgba.green, rgba.blue);
        mouthHue = hsv[0];
        mouthSaturation = hsv[1];
        BallColor seen = classify(hsv, mouthDistanceCm, RobotConfig.MOUTH_BALL_PRESENT_CM);
        mouthBall = seen;

        if (!seen.isBall()) {
            // Gone for a short gap = this ball has passed (or left). Stops one ball that flickers in and
            // out of view from counting twice.
            if (mouthAbsentSinceMs < 0) {
                mouthAbsentSinceMs = now;
                leftWhileReversing = now - lastIntakeOutMs <= 2 * RobotConfig.MOUTH_SENSOR_PERIOD_MS;
            }
            if (presenceActive && now - mouthAbsentSinceMs >= RobotConfig.BALL_COUNT_DEBOUNCE_MS) endPresence();
            return;
        }
        boolean backInView = presenceActive && mouthAbsentSinceMs >= 0;   // after >= 1 empty read
        mouthAbsentSinceMs = -1;
        if (backInView && !leftWhileReversing && isLastFreeSlotBall()) {
            // G407, last free slot: ball 4 has left the sensor's view inward and something is there again
            // before the debounce ended. Treat it as a 5th ball right behind ball 4 (never as ball 4
            // flickering): count ball 4 now, so the intake stops and its full pulse pushes the newcomer away.
            endPresence();
        }
        if (!presenceActive) {
            // A new ball at the mouth.
            presenceActive = true;
            presenceRefused = false;
            presencePulledIn = false;
            presenceOutgoing = false;
            presenceCounted = false;
            presenceStartMs = now;
            presenceLastSeenMs = now;
            presencePulledMs = 0;
            if (feedState == FeedState.EJECTING) {
                if (count > 0) count--;                 // a ball from inside is going out
                presenceOutgoing = true;
            }
        }
        long sincePreviousRead = now - presenceLastSeenMs;
        presenceLastSeenMs = now;
        if (presenceOutgoing) return;
        if (isRefusedColor(seen)) presenceRefused = true;   // sticky for this ball (G408 / POLLEN only)
        if (intakeIn && !presenceRefused) {
            if (presencePulledIn) presencePulledMs += sincePreviousRead;   // only time spent pulling counts
            presencePulledIn = true;
        }
        if (isLastFreeSlotBall() && presencePulledMs > RobotConfig.MOUTH_ONE_BALL_MAX_MS) {
            // G407: still in view longer than one ball ever is = two balls touching. Ball 4 has passed:
            // count it now; the full pulse pushes the 5th away.
            count = Math.min(RobotConfig.MAX_BALLS, count + 1);
            presenceCounted = true;
        }
    }

    /** The ball at the mouth is being pulled in as ball 4 (3 counted) and is not counted yet. */
    private boolean isLastFreeSlotBall() {
        return count == RobotConfig.MAX_BALLS - 1 && presencePulledIn && !presenceRefused
                && !presenceOutgoing && !presenceCounted;
    }

    /** The ball at the mouth is gone: count it if it went IN. */
    private void endPresence() {
        // The intake pushing outward at the moment the ball left view = it went OUT, not in.
        if (presencePulledIn && !presenceRefused && !presenceOutgoing && !presenceCounted && !leftWhileReversing) {
            count = Math.min(RobotConfig.MAX_BALLS, count + 1);
        }
        if (!presenceRefused && !presenceOutgoing) {
            lastPresenceMs = presenceLastSeenMs - presenceStartMs;
            longestPresenceMs = Math.max(longestPresenceMs, lastPresenceMs);
        }
        presenceActive = false;
        presenceRefused = false;
        presencePulledIn = false;
        presenceOutgoing = false;
        presenceCounted = false;
    }

    private void readSlot4(long now, boolean intakeIn) {
        // While ball 4 comes in (3 counted, intake pulling) slot 4 is read at the mouth rate: "full" as soon
        // as ball 4 reaches its place, before a 5th ball right behind it is drawn in (G407).
        long period = (intakeIn && count == RobotConfig.MAX_BALLS - 1)
                ? RobotConfig.MOUTH_SENSOR_PERIOD_MS : RobotConfig.SLOT4_SENSOR_PERIOD_MS;
        if (slot4Color == null || now - lastSlot4ReadMs < period) return;
        lastSlot4ReadMs = now;
        slot4DistanceCm = (slot4Distance != null) ? slot4Distance.getDistance(DistanceUnit.CM) : Double.NaN;
        if (slot4Distance != null) {
            slot4Occupied = slot4DistanceCm < RobotConfig.SLOT4_BALL_PRESENT_CM;
        } else {
            NormalizedRGBA rgba = slot4Color.getNormalizedColors();
            slot4Occupied = toHsv(rgba.red, rgba.green, rgba.blue)[1] > RobotConfig.MIN_SATURATION;
        }
        if (slot4Occupied && count < RobotConfig.MAX_BALLS) count = RobotConfig.MAX_BALLS;  // trust the sensor
    }

    private void readBeam() {
        if (feedBeam == null) return;
        boolean raw = feedBeam.getState();
        boolean broken = RobotConfig.FEED_BEAM_BROKEN_IS_LOW ? !raw : raw;
        if (broken && !beamBroken && (feedState == FeedState.FEEDING || feedState == FeedState.CONFIRMING)) {
            shotSeenThisFeed = true;       // a ball crossed the beam on its way into the shooting wheel
        }
        beamBroken = broken;
    }

    /** RGB (any scale) -> {hue 0-360, saturation 0-1, value}. Plain Java so it is easy to test. */
    public static double[] toHsv(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;
        double hue = 0.0;
        if (delta > 1e-9) {
            if (max == r) hue = 60.0 * (((g - b) / delta) % 6.0);
            else if (max == g) hue = 60.0 * ((b - r) / delta + 2.0);
            else hue = 60.0 * ((r - g) / delta + 4.0);
        }
        if (hue < 0) hue += 360.0;
        double sat = (max <= 1e-9) ? 0.0 : delta / max;
        return new double[]{hue, sat, max};
    }

    /** Decide what is in front of a sensor from its hue/saturation and distance. */
    public static BallColor classify(double[] hsv, double distanceCm, double presentBelowCm) {
        boolean present = Double.isNaN(distanceCm) ? hsv[1] > RobotConfig.MIN_SATURATION : distanceCm < presentBelowCm;
        if (!present) return BallColor.NONE;
        if (hsv[1] < RobotConfig.MIN_SATURATION) return BallColor.UNKNOWN;
        double hue = hsv[0];
        if (hue >= RobotConfig.POLLEN_HUE_MIN && hue <= RobotConfig.POLLEN_HUE_MAX) return BallColor.POLLEN;
        if (hue <= RobotConfig.RED_HUE_MAX || hue >= RobotConfig.RED_HUE_MIN_WRAP) return BallColor.RED;
        if (hue >= RobotConfig.BLUE_HUE_MIN && hue <= RobotConfig.BLUE_HUE_MAX) return BallColor.BLUE;
        return BallColor.UNKNOWN;
    }

    // ---------------------------------------------------------------------------------------------
    // Output
    // ---------------------------------------------------------------------------------------------
    /** Carrier power for the current state (+ = toward the shooting wheel). Stopped = holding the balls. */
    private double powerFor(boolean intakeIn) {
        switch (feedState) {
            case FEEDING:
                return RobotConfig.CARRIER_FEED_POWER;
            case EJECTING:
                return RobotConfig.CARRIER_EJECT_POWER;
            case IDLE:
                // Nothing is being fired: optionally help the balls along while the intake pulls them in.
                return intakeIn ? RobotConfig.CARRIER_INTAKE_POWER : 0.0;
            default:
                // WAIT_FOR_READY, CONFIRMING, TIP_PAUSE: stopped, so no ball reaches the shooting wheel.
                return 0.0;
        }
    }

    private void writeOutput(boolean intakeIn) {
        double power = powerFor(intakeIn);
        if (carrier == null || power == lastCarrierPower) return;
        carrier.setPower(power);
        lastCarrierPower = power;
    }

    // ---------------------------------------------------------------------------------------------
    // Events and status
    // ---------------------------------------------------------------------------------------------
    /** True once when we become full (4 balls). */
    public boolean consumeFullEvent() {
        boolean e = fullEvent;
        fullEvent = false;
        return e;
    }

    /** True once after each ball that left through the shooting wheel (confirmed, or counted unconfirmed). */
    public boolean consumeShotEvent() {
        boolean e = shotEvent;
        shotEvent = false;
        return e;
    }

    /** True once after each feed that neither the beam nor a flywheel dip confirmed (possible jam). */
    public boolean consumeUnconfirmedEvent() {
        boolean e = unconfirmedEvent;
        unconfirmedEvent = false;
        return e;
    }

    public FeedState feedState() {
        return feedState;
    }

    /** Why we are not firing yet ("flywheel", "aim", "moving", ...), empty if not waiting. */
    public String blockedBy() {
        return blockedBy;
    }

    public BallColor mouthBall() {
        return mouthBall;
    }

    public double mouthHue() {
        return mouthHue;
    }

    public double mouthSaturation() {
        return mouthSaturation;
    }

    public double mouthDistanceCm() {
        return mouthDistanceCm;
    }

    /** How long the last ball (not refused, not going out) stayed in the mouth sensor's view, ms (-1 = none yet). */
    public long lastMouthPresenceMs() {
        return lastPresenceMs;
    }

    /** The longest such time since resetLongestMouthPresence(): sets MOUTH_ONE_BALL_MAX_MS (SensorCheck). */
    public long longestMouthPresenceMs() {
        return longestPresenceMs;
    }

    public void resetLongestMouthPresence() {
        longestPresenceMs = -1;
    }

    public boolean slot4Occupied() {
        return slot4Occupied;
    }

    public double slot4DistanceCm() {
        return slot4DistanceCm;
    }

    public boolean hasFeedBeam() {
        return feedBeam != null;
    }

    public boolean beamBroken() {
        return beamBroken;
    }

    public int confirmedShots() {
        return confirmedShots;
    }

    public int unconfirmedShots() {
        return unconfirmedShots;
    }

    public boolean hasMouthSensor() {
        return mouthColor != null;
    }

    public boolean hasSlot4Sensor() {
        return slot4Color != null;
    }

    public boolean isPresent() {
        return carrier != null;
    }

    /** The last power sent to the carrier (0 before START). */
    public double carrierPower() {
        return Double.isNaN(lastCarrierPower) ? 0.0 : lastCarrierPower;
    }
}
