package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Turret: a POSITIONAL servo turns a 60T pinion that drives a large ring gear. This class only ever
 * sends positions - never put the turret servo in continuous mode (a continuous-rotation turret would
 * need new closed-loop code; with this code it would spin into its stops).
 *
 * ANGLES: "offset" = turret angle in degrees from its centre position, + = counter-clockwise (left,
 * seen from above). At offset 0 the shooter points TURRET_CENTER_DEG from the robot front.
 *
 * SERVO MAPPING: servo position 0 -> 1 turns the turret TURRET_SERVO_RANGE_DEG / TURRET_GEAR_RATIO
 * degrees (300 / 5 = only 60 deg with the standard servo; 1800 / 5 = 360 deg with the 5-turn servo).
 *   servo = TURRET_SERVO_CENTER + TURRET_SERVO_DIRECTION * offset / degreesPerServoUnit
 *
 * TRAVEL IS SMALL, so aimAt() tells you when the target is outside the soft limits and how far the
 * CHASSIS must turn to bring it back to the turret centre. TeleOp uses that for the driver's
 * aim-assist; AUTO picks shooting poses that face the target so the turret only fine-tunes.
 *
 * WHERE IS IT NOW? A servo gives no feedback, so without an encoder we estimate the angle by moving
 * toward the command at TURRET_SLEW_DEG_PER_SEC. With the optional REV Through Bore Encoder (FEEDBACK
 * only, on an idler gear meshing with the ring) we measure it: the encoder is zeroed automatically the
 * first time the turret settles after arm(), and re-zeroed (with a warning) if it disagrees with a
 * steady command for TURRET_ENCODER_REZERO_AFTER_MS - skipped teeth or a wrong ticks/deg scale must not
 * block every shot for the rest of the match.
 *
 * PINNED (RobotConfig.TURRET_PINNED): the servo is held at its centre and aimAt() only computes the
 * solution; Robot then aims with the chassis (TeleOp: driver RB aim-assist; AUTO turns the robot).
 *
 * G403: arm() is the first time the servo is commanded (TeleOp: at START; AUTO: in INIT, holding the
 * centre - a positional servo may hold the starting configuration, R103).
 */
public class Turret {

    public enum State {
        /** Not armed yet, or no servo. */
        DISABLED,
        /** Holding the centre position. */
        CENTERED,
        /** Operator is steering it by hand (auto-aim off). */
        MANUAL,
        /** Following aimAt() and the target is inside the soft limits. */
        TRACKING,
        /** Following aimAt() but the target is outside the soft limits: turn the chassis! */
        AT_LIMIT,
        /** TURRET_PINNED: held at the centre, the chassis aims. */
        PINNED
    }

    /** Result of an aim calculation. All angles in degrees. */
    public static class AimSolution {
        /** Horizontal distance from the turret axis to the aim point (inches) - the shot-table input. */
        public double distanceIn;
        /** Field bearing from the turret axis to the aim point (0 = +X, CCW +). */
        public double fieldBearingDeg;
        /** Turret offset needed to point at the target (may be outside the limits). */
        public double desiredOffsetDeg;
        /** Turret offset we can actually command (clamped to the soft limits). */
        public double commandOffsetDeg;
        /** True if the target is inside the turret's soft limits. */
        public boolean reachable;
        /**
         * Rotate the CHASSIS counter-clockwise by this many degrees to put the target at the turret
         * CENTRE (negative = clockwise). Used for aim-assist and to choose shooting headings.
         */
        public double chassisTurnDeg;
    }

    private final ServoImplEx servo;
    private final DcMotorEx encoder;
    private State state = State.DISABLED;
    private boolean armed = false;

    private double commandOffsetDeg = 0.0;
    private double estimatedOffsetDeg = 0.0;
    private double trimDeg = 0.0;
    private double visionBiasDeg = 0.0;
    private double lastServoPosition = Double.NaN;
    private long lastUpdateNs = 0;
    private AimSolution lastAim = null;

    // encoder bookkeeping
    private boolean encoderZeroed = false;
    private int encoderZeroTicks = 0;
    private double measuredOffsetDeg = 0.0;
    private double measuredSpeedDegPerSec = 0.0;
    private long armedAtNs = 0;
    private long stillSinceNs = 0;
    private long commandStableSinceNs = 0;
    private double lastCommandForStability = Double.NaN;
    private int encoderRezeroCount = 0;
    private double lastRezeroErrorDeg = 0.0;

    public Turret(HardwareMap hw, List<String> warnings) {
        Servo s = OptionalHardware.get(hw, Servo.class, RobotConfig.TURRET_SERVO, warnings);
        servo = (s instanceof ServoImplEx) ? (ServoImplEx) s : null;
        if (s != null && servo == null) warnings.add("turret servo is not a ServoImplEx - cannot set the PWM range");
        if (RobotConfig.TURRET_USE_ENCODER) {
            encoder = OptionalHardware.get(hw, DcMotorEx.class, RobotConfig.TURRET_ENCODER, warnings);
            if (encoder != null) {
                // Reading an encoder never moves anything. Do NOT reset it: the count survives AUTO -> TeleOp.
                encoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        } else {
            encoder = null;
        }
    }

    /** First servo command: set the full PWM range and hold the centre (TeleOp START / AUTO INIT). */
    public void arm() {
        if (servo == null || armed) return;     // arming twice (AUTO INIT, then START) changes nothing
        servo.setPwmRange(new PwmControl.PwmRange(RobotConfig.TURRET_PWM_MIN_US, RobotConfig.TURRET_PWM_MAX_US));
        armed = true;
        armedAtNs = System.nanoTime();
        encoderZeroed = false;
        if (state == State.DISABLED) state = RobotConfig.TURRET_PINNED ? State.PINNED : State.CENTERED;
    }

    /** True when the turret cannot aim by itself (pinned, or no servo): the chassis must aim. */
    public boolean chassisMustAim() {
        return RobotConfig.TURRET_PINNED || servo == null;
    }

    public boolean isPresent() {
        return servo != null;
    }

    public boolean hasEncoder() {
        return encoder != null;
    }

    // ---------------------------------------------------------------------------------------------
    // Pure math (no hardware) - easy to test
    // ---------------------------------------------------------------------------------------------
    /** Turret rotation axis position on the field for a robot pose (inches). */
    public static double[] turretAxisOnField(Pose robot) {
        double h = robot.heading();
        double fwd = RobotConfig.TURRET_AXIS_FORWARD_IN;
        double left = RobotConfig.TURRET_AXIS_LEFT_IN;
        return new double[]{
                robot.x() + fwd * Math.cos(h) - left * Math.sin(h),
                robot.y() + fwd * Math.sin(h) + left * Math.cos(h)};
    }

    /**
     * Work out how to point at a field point.
     * desired offset = (bearing to target) - (robot heading) - TURRET_CENTER_DEG + trim + vision bias
     */
    public static AimSolution solve(FieldPoint target, Pose robot, double trimDeg, double visionBiasDeg) {
        double[] axis = turretAxisOnField(robot);
        AimSolution s = new AimSolution();
        s.distanceIn = target.horizontalDistanceFrom(axis[0], axis[1]);
        s.fieldBearingDeg = MathUtil.bearingDeg(axis[0], axis[1], target.x, target.y);
        double robotFrameDeg = s.fieldBearingDeg - Math.toDegrees(robot.heading());
        s.desiredOffsetDeg = MathUtil.wrapDeg(robotFrameDeg - RobotConfig.TURRET_CENTER_DEG) + trimDeg + visionBiasDeg;
        s.commandOffsetDeg = MathUtil.clamp(s.desiredOffsetDeg, RobotConfig.TURRET_MIN_DEG, RobotConfig.TURRET_MAX_DEG);
        s.reachable = Math.abs(s.commandOffsetDeg - s.desiredOffsetDeg) < 1e-6;
        s.chassisTurnDeg = s.desiredOffsetDeg;
        return s;
    }

    /** Servo position (0..1) for a turret offset in degrees. */
    public static double servoPositionFor(double offsetDeg) {
        double pos = RobotConfig.TURRET_SERVO_CENTER
                + RobotConfig.TURRET_SERVO_DIRECTION * offsetDeg / RobotConfig.turretDegreesPerServoUnit();
        return MathUtil.clamp(pos, 0.0, 1.0);
    }

    /** Turret offset in degrees for a servo position (inverse of servoPositionFor). */
    public static double offsetForServoPosition(double position) {
        return (position - RobotConfig.TURRET_SERVO_CENTER) * RobotConfig.turretDegreesPerServoUnit()
                * RobotConfig.TURRET_SERVO_DIRECTION;
    }

    // ---------------------------------------------------------------------------------------------
    // Commands
    // ---------------------------------------------------------------------------------------------
    /**
     * Point at a field point from this robot pose (uses the current trim and vision bias).
     * @return the solution; check .reachable and .chassisTurnDeg
     */
    public AimSolution aimAt(FieldPoint target, Pose robotPose) {
        AimSolution s = solve(target, robotPose, trimDeg, visionBiasDeg);
        lastAim = s;
        if (RobotConfig.TURRET_PINNED) {
            commandOffsetDeg = 0.0;           // held at the centre: s.chassisTurnDeg tells the chassis
            state = State.PINNED;
            return s;
        }
        commandOffsetDeg = s.commandOffsetDeg;
        state = s.reachable ? State.TRACKING : State.AT_LIMIT;
        return s;
    }

    /** Hold the centre position. */
    public void center() {
        commandOffsetDeg = 0.0;
        state = RobotConfig.TURRET_PINNED ? State.PINNED : State.CENTERED;
    }

    /** Manual control: go to this offset (clamped to the soft limits). Ignored while pinned. */
    public void setManualOffset(double offsetDeg) {
        if (RobotConfig.TURRET_PINNED) {
            center();
            return;
        }
        commandOffsetDeg = MathUtil.clamp(offsetDeg, RobotConfig.TURRET_MIN_DEG, RobotConfig.TURRET_MAX_DEG);
        state = State.MANUAL;
    }

    /** Nudge the manual command (deg). */
    public void nudgeManual(double deltaDeg) {
        setManualOffset(commandOffsetDeg + deltaDeg);
    }

    public void addTrim(double deltaDeg) {
        trimDeg += deltaDeg;
    }

    public double trimDeg() {
        return trimDeg;
    }

    /** Vision correction from the Limelight (deg, + = target is further CCW than odometry thinks). */
    public void setVisionBias(double biasDeg) {
        visionBiasDeg = biasDeg;
    }

    public double visionBiasDeg() {
        return visionBiasDeg;
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    public void update() {
        long now = System.nanoTime();
        double dt = (lastUpdateNs == 0) ? 0.0 : (now - lastUpdateNs) / 1e9;
        lastUpdateNs = now;

        readEncoder(now, dt);
        if (!armed || servo == null) {
            if (servo == null) state = State.DISABLED;
            return;
        }
        double step = RobotConfig.TURRET_SLEW_DEG_PER_SEC * dt;
        estimatedOffsetDeg += MathUtil.clamp(commandOffsetDeg - estimatedOffsetDeg, -step, step);

        double pos = servoPositionFor(commandOffsetDeg);
        if (Double.isNaN(lastServoPosition) || Math.abs(pos - lastServoPosition) > 0.0005) {
            servo.setPosition(pos);           // only write when it changes
            lastServoPosition = pos;
        }
    }

    private void readEncoder(long now, double dt) {
        if (encoder == null) return;
        int ticks = encoder.getCurrentPosition();                  // bulk-cached read
        double raw = RobotConfig.TURRET_ENCODER_DIRECTION * ticks / RobotConfig.TURRET_ENCODER_TICKS_PER_DEG;
        double previous = measuredOffsetDeg;
        if (encoderZeroed) {
            measuredOffsetDeg = RobotConfig.TURRET_ENCODER_DIRECTION * (ticks - encoderZeroTicks)
                    / RobotConfig.TURRET_ENCODER_TICKS_PER_DEG;
        } else {
            measuredOffsetDeg = raw;
        }
        if (dt > 0) measuredSpeedDegPerSec = (measuredOffsetDeg - previous) / dt;

        boolean still = Math.abs(measuredSpeedDegPerSec) < RobotConfig.TURRET_SETTLED_DEG_PER_SEC;
        if (!still) stillSinceNs = now;
        if (Double.isNaN(lastCommandForStability) || Math.abs(commandOffsetDeg - lastCommandForStability) > 0.2) {
            commandStableSinceNs = now;
            lastCommandForStability = commandOffsetDeg;
        }
        // Auto-zero: the first time the armed turret has been still for 0.3 s (and >= 0.5 s after arm),
        // assume the servo reached its command (a positional servo is absolute) and line the encoder up.
        if (armed && !encoderZeroed && now - armedAtNs > 500_000_000L && now - stillSinceNs > 300_000_000L) {
            zeroEncoderToCommand(ticks);
            return;
        }
        // Skipped teeth / wrong scale: encoder and a steady command disagree while nothing moves. Trust the
        // (absolute) positional servo, re-zero and warn instead of blocking every shot.
        long rezeroNs = RobotConfig.TURRET_ENCODER_REZERO_AFTER_MS * 1_000_000L;
        double disagreement = commandOffsetDeg - measuredOffsetDeg;
        if (armed && encoderZeroed && Math.abs(disagreement) > RobotConfig.TURRET_AIM_TOLERANCE_DEG
                && now - stillSinceNs > rezeroNs && now - commandStableSinceNs > rezeroNs) {
            lastRezeroErrorDeg = disagreement;
            encoderRezeroCount++;
            zeroEncoderToCommand(ticks);
        }
    }

    private void zeroEncoderToCommand(int ticks) {
        encoderZeroTicks = ticks - (int) Math.round(commandOffsetDeg * RobotConfig.TURRET_ENCODER_TICKS_PER_DEG
                * RobotConfig.TURRET_ENCODER_DIRECTION);
        encoderZeroed = true;
        measuredOffsetDeg = commandOffsetDeg;
    }

    /** How many times the encoder was re-zeroed because it disagreed with the command (0 = healthy). */
    public int encoderRezeroCount() {
        return encoderRezeroCount;
    }

    /** Size of the last disagreement that caused a re-zero (deg). */
    public double lastRezeroErrorDeg() {
        return lastRezeroErrorDeg;
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    public State state() {
        return state;
    }

    /** Best guess of the turret offset right now (encoder if zeroed, otherwise the slew model). */
    public double currentOffsetDeg() {
        if (encoder != null && encoderZeroed) return measuredOffsetDeg;
        return estimatedOffsetDeg;
    }

    /** Where the shooter points relative to the robot front (deg, CCW +). */
    public double currentRobotFrameDeg() {
        return RobotConfig.TURRET_CENTER_DEG + currentOffsetDeg();
    }

    public double commandOffsetDeg() {
        return commandOffsetDeg;
    }

    /** Degrees between where we want to be and where we are. */
    public double errorDeg() {
        return commandOffsetDeg - currentOffsetDeg();
    }

    /** True when the turret has reached its command (and, with an encoder, stopped moving). */
    public boolean isSettled() {
        boolean close = Math.abs(errorDeg()) < RobotConfig.TURRET_AIM_TOLERANCE_DEG;
        if (encoder != null && encoderZeroed) {
            close = close && Math.abs(measuredSpeedDegPerSec) < RobotConfig.TURRET_SETTLED_DEG_PER_SEC;
        }
        return armed && servo != null && close;
    }

    /** True when tracking a reachable target and settled on it: OK to fire (turret aiming only). */
    public boolean onTarget() {
        return state == State.TRACKING && isSettled();
    }

    public AimSolution lastAim() {
        return lastAim;
    }

    public double servoPosition() {
        return Double.isNaN(lastServoPosition) ? servoPositionFor(commandOffsetDeg) : lastServoPosition;
    }

    /** Raw access for the TurretCalibrator only. */
    public ServoImplEx servo() {
        return servo;
    }

    /** Raw encoder ticks (0 if no encoder). */
    public int encoderTicks() {
        return encoder == null ? 0 : encoder.getCurrentPosition();
    }

    public double measuredSpeedDegPerSec() {
        return measuredSpeedDegPerSec;
    }
}
