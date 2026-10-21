package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.BallColor;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Ball handling inside the robot: the ball COUNT, the colour sensors, the "internal spinners"
 * (indexer CR servo) and the feeder GATE that releases one ball at a time into the flywheel.
 *
 * WHY THE COUNT MATTERS (possession discipline is a design requirement):
 *  - G407: at most 4 scoring elements CONTROLLED at once. No "momentary" exception (Q&A 3), and a
 *    routine "grab a 5th, then spit it out" design is likely STRATEGIC: MAJOR FOUL + YELLOW CARD.
 *  - G408: never CONTROL the opponent's NECTAR (no momentary exception, Q&A 4/23).
 * So: the count starts at the INIT Preloads value (4 in a match), the intake is not allowed to pull when
 * we are full, and a REFUSED ball at the mouth makes the intake spin backward for as long as it is there.
 * Refused = the opponent's NECTAR, and our own NECTAR too while RobotConfig.ACCEPT_OWN_NECTAR = false
 * (phase 1: the hood is gapped for 2.8 in POLLEN). The slot-4 sensor is the safety net: if it sees a
 * ball, we ARE full, whatever the count says.
 *
 * COUNTING AT THE MOUTH: a ball is counted when it LEAVES the mouth sensor's view after being pulled in
 * (and has been gone BALL_COUNT_DEBOUNCE_MS) - so the 4th ball is fully drawn in before "full" stops and
 * reverses the roller. A ball that leaves while the roller pushes outward, or a refused ball, is not counted.
 * LAST FREE SLOT (3 counted): a 5th ball right behind ball 4 must not follow it in (G407), and one sensor
 * cannot always see the gap between two touching balls. So with 3 counted, anything back in view before the
 * debounce has ended counts ball 4 at once (the full pulse then pushes the newcomer away), and so does a
 * ball still in view MOUTH_ONE_BALL_MAX_MS after the roller started pulling it (two balls touching). The
 * slot-4 sensor is read every MOUTH_SENSOR_PERIOD_MS while ball 4 comes in.
 *
 * FEED SEQUENCE (one ball):
 *   WAIT_FOR_READY  - stage the next ball against the closed gate; wait until the flywheel is at speed,
 *                     the aim is ready, the chassis is still, and no TIP pause is active.
 *   GATE_OPEN       - open the gate for FEEDER_OPEN_MS while the spinners push one ball through.
 *   WAIT_FOR_SHOT   - gate closed; wait for the break-beam or a flywheel speed dip to confirm the ball
 *                     left (or SHOT_CONFIRM_TIMEOUT_MS: an "unconfirmed feed" - counted as fired when
 *                     COUNT_UNCONFIRMED_SHOTS, reported once for the operator rumble). Count - 1. Then the
 *                     next ball (volley) or IDLE. There is no automatic jam retry.
 */
public class Indexer {

    public enum FeedState {
        /** Gate closed, nothing to fire. */
        IDLE,
        /** A shot is requested; staging the ball and waiting for shooter / aim / chassis. */
        WAIT_FOR_READY,
        /** Gate open: one ball is going into the flywheel. */
        GATE_OPEN,
        /** Gate closed again, waiting to confirm the ball left. */
        WAIT_FOR_SHOT,
        /** The target cell just changed: no launching for a moment while we retarget. */
        TIP_PAUSE,
        /** Operator is emptying the robot backward through the intake. */
        EJECTING
    }

    // hardware (any of these may be null)
    private final CRServo spinner;
    private final CRServo spinner2;
    private final Servo feeder;
    private final NormalizedColorSensor mouthColor;
    private final DistanceSensor mouthDistance;
    private final NormalizedColorSensor slot4Color;
    private final DistanceSensor slot4Distance;
    private final DigitalChannel feedBeam;

    private Alliance alliance;
    private boolean armed = false;
    private boolean gateArmed = false;
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
    /** How long the roller has pulled this presence in so far (sum over mouth reads while intaking). */
    private long presencePulledMs = 0;
    /** How long the last ball was in the mouth sensor's view, and the longest since reset (SensorCheck). */
    private long lastPresenceMs = -1, longestPresenceMs = -1;
    /** The roller was pushing outward when the ball disappeared from view (it went OUT, not in). */
    private boolean leftWhileReversing = false;

    // feeding
    private FeedState feedState = FeedState.IDLE;
    private long stateStartMs = 0;
    private long lastShotMs = -100000;
    private boolean volleyRequested = false;
    private boolean singleRequested = false;
    private boolean ejectRequested = false;
    private boolean shotSeenThisFeed = false;
    private boolean stagingNeeded = true;
    private int volleyMinCycles = 0;
    private int volleyCycles = 0;
    private int consecutiveUnconfirmed = 0;
    private int confirmedShots = 0, unconfirmedShots = 0;
    private String blockedBy = "";

    // events for other subsystems (each is reported once)
    private boolean fullEvent = false;
    private boolean shotEvent = false;
    private boolean unconfirmedEvent = false;
    private boolean wasFull = false;

    // outputs (only written when they change)
    private double lastSpinnerPower = Double.NaN;
    private double lastFeederPosition = Double.NaN;
    private long lastIntakeInMs = -100000;

    public Indexer(HardwareMap hw, Alliance alliance, List<String> warnings) {
        this.alliance = alliance;
        spinner = OptionalHardware.get(hw, CRServo.class, RobotConfig.INDEXER, warnings);
        spinner2 = OptionalHardware.getQuiet(hw, CRServo.class, RobotConfig.INDEXER_2);
        feeder = OptionalHardware.get(hw, Servo.class, RobotConfig.FEEDER, warnings);
        mouthColor = OptionalHardware.get(hw, NormalizedColorSensor.class, RobotConfig.MOUTH_COLOR, warnings);
        mouthDistance = OptionalHardware.getQuiet(hw, DistanceSensor.class, RobotConfig.MOUTH_COLOR);
        slot4Color = OptionalHardware.get(hw, NormalizedColorSensor.class, RobotConfig.SLOT4_COLOR, warnings);
        slot4Distance = OptionalHardware.getQuiet(hw, DistanceSensor.class, RobotConfig.SLOT4_COLOR);
        feedBeam = OptionalHardware.getQuiet(hw, DigitalChannel.class, RobotConfig.FEED_BEAM);

        if (spinner != null) spinner.setDirection(RobotConfig.INDEXER_DIRECTION);
        if (spinner2 != null) spinner2.setDirection(RobotConfig.INDEXER_DIRECTION);
        if (feedBeam != null) feedBeam.setMode(DigitalChannel.Mode.INPUT);
        setupColorSensor(mouthColor);
        setupColorSensor(slot4Color);
        if (mouthColor == null) warnings.add("no mouth colour sensor: intake cannot count balls or refuse NECTAR (G407/G408)!");
    }

    private static void setupColorSensor(NormalizedColorSensor sensor) {
        if (sensor == null) return;
        sensor.setGain(RobotConfig.COLOR_SENSOR_GAIN);
        if (sensor instanceof SwitchableLight) ((SwitchableLight) sensor).enableLight(true);
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /** First actuator commands: close the gate and allow the spinners (TeleOp START / AUTO START - G403). */
    public void arm() {
        armed = true;
        armGateOnly();
    }

    /**
     * AUTO INIT: hold the feeder gate closed (a positional servo may hold the starting configuration,
     * R103) but leave the continuous-rotation spinners unpowered until START (G304.H).
     */
    public void armGateOnly() {
        gateArmed = true;
        writeFeeder(RobotConfig.FEEDER_CLOSED_POSITION);
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

    /** True if this ball colour must not be taken in (G408; own NECTAR too in phase 1). */
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
    /** Fire every ball we have, one at a time, each only when everything is ready. */
    public void requestVolley() {
        requestVolley(0);
    }

    /**
     * Fire every ball we have, and cycle the gate at least minGateCycles times even if the count says
     * fewer (AUTO after the garden sweep, where a ball may have missed the mouth sensor). Empty cycles are
     * harmless; with a feed beam fitted the volley stops after 2 unconfirmed (empty) cycles in a row.
     */
    public void requestVolley(int minGateCycles) {
        if (feedState == FeedState.EJECTING) return;
        volleyRequested = true;
        volleyMinCycles = Math.max(0, minGateCycles);
        volleyCycles = 0;
        consecutiveUnconfirmed = 0;
    }

    /** Fire one ball (works even if the count says 0, in case the count is wrong). */
    public void requestSingleShot() {
        if (feedState == FeedState.EJECTING) return;
        singleRequested = true;
    }

    /** Stop firing after the current ball. */
    public void cancelFiring() {
        volleyRequested = false;
        singleRequested = false;
        volleyMinCycles = 0;
    }

    public boolean isFiring() {
        return volleyRequested || singleRequested || feedState == FeedState.GATE_OPEN
                || feedState == FeedState.WAIT_FOR_SHOT;
    }

    /** Hold to run the spinners backward (empty the robot through the intake). */
    public void setEjecting(boolean eject) {
        ejectRequested = eject;
    }

    /** The target cell changed: stop launching right now (the ball already in the gate still goes). */
    public void pauseForTip() {
        cancelFiring();
        if (feedState == FeedState.WAIT_FOR_READY || feedState == FeedState.IDLE) go(FeedState.TIP_PAUSE);
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    /**
     * @param shooterReady   flywheel at speed
     * @param aimReady       aim OK (turret on target / chassis aimed; always true with manual aim)
     * @param chassisStill   robot not moving / turning (stop to shoot; always true with manual aim)
     * @param tipPause       HiveTracker says the target cell just changed
     * @param shotDip        flywheel reported a speed dip this loop
     * @param intakeIn       intake roller is pulling balls in
     * @param intakeOut      intake roller is pushing outward (eject, refusal, full pulse)
     */
    public void update(boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause,
                       boolean shotDip, boolean intakeIn, boolean intakeOut) {
        long now = Clock.nowMs();
        if (intakeIn) lastIntakeInMs = now;
        if (intakeOut) lastIntakeOutMs = now;
        readMouth(now, intakeIn);
        readSlot4(now, intakeIn);
        readBeam();
        if (shotDip) shotSeenThisFeed = true;

        boolean full = isFull();
        if (full && !wasFull) fullEvent = true;
        wasFull = full;

        runFeedStateMachine(now, shooterReady, aimReady, chassisStill, tipPause);
        if (armed) writeOutputs(now, intakeIn);
        else if (gateArmed) writeFeeder(RobotConfig.FEEDER_CLOSED_POSITION);
    }

    /** Older signature (no outward-roller information). */
    public void update(boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause,
                       boolean shotDip, boolean intakeIn) {
        update(shooterReady, aimReady, chassisStill, tipPause, shotDip, intakeIn, false);
    }

    private boolean moreToFire() {
        return count > 0 || (volleyCycles < volleyMinCycles
                && !(feedBeam != null && consecutiveUnconfirmed >= 2));
    }

    private void runFeedStateMachine(long now, boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause) {
        long inState = now - stateStartMs;
        if (ejectRequested && feedState != FeedState.GATE_OPEN && feedState != FeedState.EJECTING) {
            cancelFiring();
            go(FeedState.EJECTING);
            return;
        }
        switch (feedState) {
            case IDLE:
                if (singleRequested || (volleyRequested && moreToFire())) {
                    stagingNeeded = true;     // first ball: push the stack up against the gate first
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
                        stagingNeeded = false;
                        go(FeedState.GATE_OPEN);
                    }
                }
                break;

            case GATE_OPEN:
                if (inState >= RobotConfig.FEEDER_OPEN_MS) go(FeedState.WAIT_FOR_SHOT);
                break;

            case WAIT_FOR_SHOT:
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
        if (stagingNeeded && now - stateStartMs < RobotConfig.INDEXER_STAGE_MS) return "staging";
        if (now - lastShotMs < RobotConfig.MIN_SHOT_INTERVAL_MS) return "interval";
        if (!shooterReady) return "flywheel";
        if (!aimReady) return "aim";
        if (!chassisStill) return "moving";
        return "";
    }

    private void ballLeft(boolean confirmed, long now) {
        lastShotMs = now;
        volleyCycles++;
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
            volleyMinCycles = 0;
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
        if (isRefusedColor(seen)) presenceRefused = true;   // sticky for this ball (G408 / phase 1)
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
        // The roller pushing outward at the moment the ball left view = it went OUT, not in.
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
        // While ball 4 comes in (3 counted, roller pulling) slot 4 is read at the mouth rate: "full" as soon
        // as ball 4 reaches its slot, before a 5th ball right behind it is drawn in (G407).
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
        if (broken && !beamBroken && (feedState == FeedState.GATE_OPEN || feedState == FeedState.WAIT_FOR_SHOT)) {
            shotSeenThisFeed = true;       // a ball crossed the beam on its way to the flywheel
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
    // Outputs
    // ---------------------------------------------------------------------------------------------
    private void writeOutputs(long now, boolean intakeIn) {
        double power = 0.0;
        switch (feedState) {
            case GATE_OPEN:
                power = RobotConfig.INDEXER_FEED_POWER;
                break;
            case WAIT_FOR_READY:
                // first ball: push the stack up against the closed gate for INDEXER_STAGE_MS
                power = (stagingNeeded && now - stateStartMs < RobotConfig.INDEXER_STAGE_MS) ? RobotConfig.INDEXER_STAGE_POWER : 0.0;
                break;
            case WAIT_FOR_SHOT:
                // gate just closed: bring the next ball up against it while we confirm the shot
                power = RobotConfig.INDEXER_STAGE_POWER;
                break;
            case EJECTING:
                power = RobotConfig.INDEXER_EJECT_POWER;
                break;
            default:
                // pack balls toward the gate while (and shortly after) the intake runs
                if (intakeIn || now - lastIntakeInMs < RobotConfig.INDEXER_RUN_AFTER_INTAKE_MS) {
                    power = RobotConfig.INDEXER_STAGE_POWER;
                }
                break;
        }
        writeSpinners(power);
        writeFeeder(feedState == FeedState.GATE_OPEN ? RobotConfig.FEEDER_OPEN_POSITION : RobotConfig.FEEDER_CLOSED_POSITION);
    }

    private void writeSpinners(double power) {
        if (power == lastSpinnerPower) return;
        if (spinner != null) spinner.setPower(power);
        if (spinner2 != null) spinner2.setPower(power);
        lastSpinnerPower = power;
    }

    private void writeFeeder(double position) {
        if (feeder == null || position == lastFeederPosition) return;
        feeder.setPosition(position);
        lastFeederPosition = position;
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

    /** True once after each ball that left through the flywheel (confirmed, or counted unconfirmed). */
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
}
