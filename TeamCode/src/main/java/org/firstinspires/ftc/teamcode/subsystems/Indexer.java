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
 * So: the count starts at the number of preloads (4 in AUTO), the intake is not allowed to run when
 * we are full, and a ball of the opponent's NECTAR colour at the mouth makes the intake spin backward.
 * The slot-4 sensor is the safety net: if it sees a ball, we ARE full, whatever the count says.
 *
 * FEED SEQUENCE (one ball):
 *   WAIT_FOR_READY  - stage the next ball against the closed gate; wait until the flywheel is at speed,
 *                     the turret is on target, the chassis is still, and no TIP pause is active.
 *   GATE_OPEN       - open the gate for FEEDER_OPEN_MS while the spinners push one ball through.
 *   WAIT_FOR_SHOT   - gate closed; wait for the break-beam or a flywheel speed dip to confirm the ball
 *                     left (or time out). Count - 1. Then the next ball (volley) or IDLE.
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
        /** A TIP was just detected: no launching for a moment (manual 10.5.1). */
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
    private int count = 0;

    // sensors
    private long lastMouthReadMs = 0, lastSlot4ReadMs = 0;
    private BallColor mouthBall = BallColor.NONE;
    private double mouthHue = 0, mouthSaturation = 0, mouthDistanceCm = Double.NaN;
    private boolean mouthPresenceCounted = false;
    private boolean mouthPresenceRejected = false;
    private long mouthAbsentSinceMs = -1;
    private boolean slot4Occupied = false;
    private double slot4DistanceCm = Double.NaN;
    private boolean beamBroken = false;

    // feeding
    private FeedState feedState = FeedState.IDLE;
    private long stateStartMs = 0;
    private long lastShotMs = 0;
    private boolean volleyRequested = false;
    private boolean singleRequested = false;
    private boolean ejectRequested = false;
    private boolean shotSeenThisFeed = false;
    private boolean stagingNeeded = true;
    private int confirmedShots = 0, unconfirmedShots = 0;
    private String blockedBy = "";

    // events for other subsystems (each is reported once)
    private boolean rejectEvent = false;
    private boolean fullEvent = false;
    private boolean shotEvent = false;
    private boolean wasFull = false;

    // outputs (only written when they change)
    private double lastSpinnerPower = Double.NaN;
    private double lastFeederPosition = Double.NaN;
    private long lastIntakeInMs = 0;

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
        if (mouthColor == null) warnings.add("no mouth colour sensor: intake cannot count balls or reject opponent NECTAR (G407/G408)!");
    }

    private static void setupColorSensor(NormalizedColorSensor sensor) {
        if (sensor == null) return;
        sensor.setGain(RobotConfig.COLOR_SENSOR_GAIN);
        if (sensor instanceof SwitchableLight) ((SwitchableLight) sensor).enableLight(true);
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /** First actuator commands: close the gate (TeleOp START / AUTO INIT - G403). */
    public void arm() {
        armed = true;
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

    // ---------------------------------------------------------------------------------------------
    // Requests
    // ---------------------------------------------------------------------------------------------
    /** Fire every ball we have, one at a time, each only when everything is ready. */
    public void requestVolley() {
        if (feedState == FeedState.EJECTING) return;
        volleyRequested = true;
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
    }

    public boolean isFiring() {
        return volleyRequested || singleRequested || feedState == FeedState.GATE_OPEN
                || feedState == FeedState.WAIT_FOR_SHOT;
    }

    /** Hold to run the spinners backward (empty the robot through the intake). */
    public void setEjecting(boolean eject) {
        ejectRequested = eject;
    }

    /** A TIP happened: stop launching right now (the ball already in the gate still goes). */
    public void pauseForTip() {
        cancelFiring();
        if (feedState == FeedState.WAIT_FOR_READY || feedState == FeedState.IDLE) go(FeedState.TIP_PAUSE);
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    /**
     * @param shooterReady   flywheel at speed
     * @param aimReady       turret on target (or manual aim settled)
     * @param chassisStill   robot not moving / turning (stop to shoot)
     * @param tipPause       HiveTracker says a TIP just happened
     * @param shotDip        flywheel reported a speed dip this loop
     * @param intakeIn       intake is pulling balls in
     */
    public void update(boolean shooterReady, boolean aimReady, boolean chassisStill, boolean tipPause,
                       boolean shotDip, boolean intakeIn) {
        long now = System.currentTimeMillis();
        if (intakeIn) lastIntakeInMs = now;
        readMouth(now, intakeIn);
        readSlot4(now);
        readBeam();
        if (shotDip) shotSeenThisFeed = true;

        boolean full = isFull();
        if (full && !wasFull) fullEvent = true;
        wasFull = full;

        runFeedStateMachine(now, shooterReady, aimReady, chassisStill, tipPause);
        if (armed) writeOutputs(now, intakeIn);
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
                if (singleRequested || (volleyRequested && count > 0)) {
                    stagingNeeded = true;     // first ball: push the stack up against the gate first
                    go(tipPause ? FeedState.TIP_PAUSE : FeedState.WAIT_FOR_READY);
                } else {
                    volleyRequested = false;   // nothing left to fire
                }
                break;

            case WAIT_FOR_READY:
                if (!volleyRequested && !singleRequested) {
                    go(FeedState.IDLE);
                } else if (volleyRequested && !singleRequested && count == 0) {
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
        if (confirmed) confirmedShots++;
        else unconfirmedShots++;
        if (confirmed || RobotConfig.COUNT_UNCONFIRMED_SHOTS) {
            if (count > 0) count--;
            shotEvent = true;
        }
        singleRequested = false;
        go((volleyRequested && count > 0) ? FeedState.WAIT_FOR_READY : FeedState.IDLE);
        if (feedState == FeedState.IDLE) volleyRequested = false;
    }

    private void go(FeedState next) {
        feedState = next;
        stateStartMs = System.currentTimeMillis();
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
            // The ball must be gone for a short gap before the next ball can count (stops one ball
            // that flickers in and out of view from being counted twice).
            if (mouthAbsentSinceMs < 0) mouthAbsentSinceMs = now;
            if (now - mouthAbsentSinceMs >= RobotConfig.BALL_COUNT_DEBOUNCE_MS) {
                mouthPresenceCounted = false;
                mouthPresenceRejected = false;
            }
            return;
        }
        mouthAbsentSinceMs = -1;
        boolean ejecting = feedState == FeedState.EJECTING;

        if (intakeIn) {
            if (alliance.isOpponentColor(seen)) {
                // Opponent NECTAR at the mouth while pulling in: spit it out (G408) and un-count it.
                if (!mouthPresenceRejected) {
                    rejectEvent = true;
                    mouthPresenceRejected = true;
                    if (mouthPresenceCounted && count > 0) count--;
                    mouthPresenceCounted = false;
                }
            } else if (!mouthPresenceCounted && !mouthPresenceRejected) {
                count = Math.min(RobotConfig.MAX_BALLS, count + 1);   // a ball is coming in
                mouthPresenceCounted = true;
            }
        } else if (ejecting && !mouthPresenceCounted) {
            if (count > 0) count--;                                     // a ball is going out
            mouthPresenceCounted = true;                                // (only once per ball)
        }
    }

    private void readSlot4(long now) {
        if (slot4Color == null || now - lastSlot4ReadMs < RobotConfig.SLOT4_SENSOR_PERIOD_MS) return;
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
    /** True once when opponent NECTAR was seen at the mouth (the intake must reverse). */
    public boolean consumeRejectEvent() {
        boolean e = rejectEvent;
        rejectEvent = false;
        return e;
    }

    /** True once when we become full (4 balls). */
    public boolean consumeFullEvent() {
        boolean e = fullEvent;
        fullEvent = false;
        return e;
    }

    /** True once after each ball that left through the flywheel. */
    public boolean consumeShotEvent() {
        boolean e = shotEvent;
        shotEvent = false;
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
