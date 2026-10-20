package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * SWYFT Drive V2 mecanum drivetrain + goBILDA Pinpoint position tracking, through Pedro Pathing 3.
 *
 * Pedro's Follower owns the 4 drive motors and the Pinpoint. We use it for:
 *   - TeleOp: field-centric driving (heading from the Pinpoint), with an optional aim-assist turn.
 *   - AUTO: following paths and holding a pose (resists being pushed).
 * Every pose is in the FTC BIOBUZZ field frame (see config/FieldConstants), inches + radians.
 *
 * If Pedro cannot start (Pinpoint unplugged / misnamed), we fall back to plain robot-centric mecanum
 * with the raw motors so the robot can still drive in TeleOp. AUTO paths are impossible then.
 *
 * G403: nothing here moves until arm() is called (TeleOp calls it at START).
 */
public class Drivetrain {

    /** What the drivetrain is doing right now. */
    public enum Mode {
        /** Not armed yet (TeleOp INIT) or no motors: only reads the Pinpoint. */
        DISABLED,
        /** Armed and stopped. */
        IDLE,
        /** TeleOp: driver sticks, field-centric (needs the Pinpoint heading). */
        FIELD_CENTRIC,
        /** TeleOp fallback without the Pinpoint. */
        ROBOT_CENTRIC,
        /** AUTO: following a Pedro path. */
        FOLLOWING_PATH,
        /** Holding a pose (end of a path, parked, or waiting). */
        HOLDING
    }

    private final Follower follower;          // null if Pedro could not start
    private final DcMotorEx[] fallbackMotors; // FL, FR, BL, BR - only used when follower == null
    private Mode mode = Mode.DISABLED;
    private boolean armed = false;
    private Pose fallbackPose = new Pose(0, 0, 0);
    private DrivePowers manualPowers = DrivePowers.zero();
    private String startupError = null;

    public Drivetrain(HardwareMap hw, List<String> warnings) {
        Follower f = null;
        try {
            f = Constants.create(hw);
        } catch (Throwable t) {
            startupError = t.getClass().getSimpleName() + ": " + t.getMessage();
            warnings.add("Pedro/Pinpoint did not start (" + startupError + ") -> robot-centric drive only, no AUTO paths");
        }
        follower = f;
        if (follower == null) {
            fallbackMotors = new DcMotorEx[]{
                    fallbackMotor(hw, RobotConfig.FRONT_LEFT, RobotConfig.FRONT_LEFT_DIRECTION, warnings),
                    fallbackMotor(hw, RobotConfig.FRONT_RIGHT, RobotConfig.FRONT_RIGHT_DIRECTION, warnings),
                    fallbackMotor(hw, RobotConfig.BACK_LEFT, RobotConfig.BACK_LEFT_DIRECTION, warnings),
                    fallbackMotor(hw, RobotConfig.BACK_RIGHT, RobotConfig.BACK_RIGHT_DIRECTION, warnings)};
        } else {
            fallbackMotors = null;
        }
    }

    private static DcMotorEx fallbackMotor(HardwareMap hw, String name, DcMotorSimple.Direction dir, List<String> warnings) {
        DcMotorEx m = OptionalHardware.get(hw, DcMotorEx.class, name, warnings);
        if (m != null) {
            m.setDirection(dir);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m.setZeroPowerBehavior(RobotConfig.DRIVE_BRAKE_IN_TELEOP
                    ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT);
        }
        return m;
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    /** True when the Pinpoint/Pedro is running, so poses, field-centric drive and paths work. */
    public boolean hasLocalizer() {
        return follower != null;
    }

    public Mode mode() {
        return mode;
    }

    public String startupError() {
        return startupError;
    }

    /** Pedro follower, for advanced use (null without the Pinpoint). */
    public Follower follower() {
        return follower;
    }

    // ---------------------------------------------------------------------------------------------
    // Pose
    // ---------------------------------------------------------------------------------------------
    /** Robot pose in the field frame (inches, radians). Without a localizer: the last pose we were told. */
    public Pose pose() {
        if (follower == null) return fallbackPose;
        Pose p = follower.pose();
        return p == null ? fallbackPose : p;
    }

    public double headingDeg() {
        return Math.toDegrees(pose().heading());
    }

    /** Tell the Pinpoint where the robot is (start of AUTO, AUTO -> TeleOp hand-off, relocalize). */
    public void setPose(Pose pose) {
        fallbackPose = pose;
        if (follower != null) follower.setPose(pose);
    }

    /** Heading-only reset (driver "back" button): the robot must be facing exactly this way. */
    public void setHeadingDeg(double headingDeg) {
        Pose p = pose();
        setPose(new Pose(p.x(), p.y(), Math.toRadians(headingDeg)));
    }

    /** Field-frame velocity (in/s, rad/s). Zero without a localizer. */
    public Velocity velocity() {
        if (follower == null) return Velocity.zero();
        Velocity v = follower.velocity();
        return v == null ? Velocity.zero() : v;
    }

    public double speedInPerSec() {
        Velocity v = velocity();
        return Math.hypot(v.vx, v.vy);
    }

    public double turnRateDegPerSec() {
        return Math.toDegrees(velocity().omega);
    }

    /** "Stopped enough to shoot" (R6 5.5: stop to shoot; the ball flies 0.3-0.9 s). */
    public boolean isStill() {
        if (follower == null) {
            // No odometry: trust the sticks.
            return Math.abs(manualPowers.forward()) < 0.05 && Math.abs(manualPowers.strafe()) < 0.05
                    && Math.abs(manualPowers.turn()) < 0.05;
        }
        return speedInPerSec() < RobotConfig.FIRE_MAX_CHASSIS_SPEED_IN_PER_SEC
                && Math.abs(turnRateDegPerSec()) < RobotConfig.FIRE_MAX_CHASSIS_TURN_DEG_PER_SEC;
    }

    // ---------------------------------------------------------------------------------------------
    // Commands
    // ---------------------------------------------------------------------------------------------
    /** Allow the motors to move (TeleOp: at START, never in INIT - G403). */
    public void arm() {
        armed = true;
        if (mode == Mode.DISABLED) mode = Mode.IDLE;
    }

    /**
     * Field-centric TeleOp drive. "Stick up" always drives AWAY from the driver, whatever way the robot
     * faces, because we rotate the stick vector by (driver forward heading - robot heading).
     *
     * @param stickUp          driver left stick forward (+1 = pushed away from you)
     * @param stickRight       driver left stick right (+1 = pushed right)
     * @param turnLeft         rotation command, + = counter-clockwise
     * @param driverForwardDeg Alliance.driverForwardHeadingDeg() (red +90, blue -90)
     */
    public void driveFieldCentric(double stickUp, double stickRight, double turnLeft, double driverForwardDeg) {
        if (follower == null) {
            driveRobotCentric(stickUp, -stickRight, turnLeft);
            return;
        }
        double[] robotFrame = fieldCentricToRobot(stickUp, stickRight, driverForwardDeg, pose().heading());
        setManual(robotFrame[0], robotFrame[1] * RobotConfig.DRIVE_STRAFE_CORRECTION, turnLeft, Mode.FIELD_CENTRIC);
    }

    /**
     * The field-centric math on its own (no hardware, easy to test).
     * The stick vector is in the DRIVER's frame (up = away from the driver, which is field heading
     * driverForwardDeg). Rotating it by (driverForwardDeg - robotHeading) gives the robot frame.
     * @return {forward, left} for the robot, each roughly -1..1
     */
    public static double[] fieldCentricToRobot(double stickUp, double stickRight, double driverForwardDeg, double robotHeadingRad) {
        double rot = Math.toRadians(driverForwardDeg) - robotHeadingRad;
        double leftDriver = -stickRight;
        double forwardRobot = stickUp * Math.cos(rot) - leftDriver * Math.sin(rot);
        double leftRobot = stickUp * Math.sin(rot) + leftDriver * Math.cos(rot);
        return new double[]{forwardRobot, leftRobot};
    }

    /** Robot-centric drive: forward, left, turnLeft (CCW) in -1..1. */
    public void driveRobotCentric(double forward, double left, double turnLeft) {
        setManual(forward, left * RobotConfig.DRIVE_STRAFE_CORRECTION, turnLeft, Mode.ROBOT_CENTRIC);
    }

    private void setManual(double forward, double left, double turn, Mode newMode) {
        manualPowers = new DrivePowers(forward, left, turn);
        mode = newMode;
        if (follower != null && armed) follower.manual(manualPowers);
    }

    /** Stop and release the motors (they BRAKE or FLOAT per RobotConfig). */
    public void stop() {
        manualPowers = DrivePowers.zero();
        mode = armed ? Mode.IDLE : Mode.DISABLED;
        if (follower != null) follower.stop();
    }

    /** AUTO: start following a path. */
    public void followPath(Path path) {
        if (follower == null || !armed) return;
        follower.follow(path);
        mode = Mode.FOLLOWING_PATH;
    }

    /** AUTO: actively hold this pose (pushes back if another robot shoves us). */
    public void holdPose(Pose pose) {
        if (follower == null || !armed) return;
        follower.hold(pose);
        mode = Mode.HOLDING;
    }

    /** True while a path is still running (or the robot is still settling at its end). */
    public boolean isBusy() {
        if (follower == null) return false;
        return follower.isBusy();
    }

    /** Distance (in) from the robot to the end of the current path. */
    public double distanceToPathEnd() {
        return follower == null ? 0.0 : follower.distanceToEndpoint();
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    /** Call once per loop (Robot.update does this). Reads the Pinpoint and drives the motors. */
    public void update() {
        if (follower != null) {
            if (!armed) {
                // INIT: read the Pinpoint only - never command the motors before START (G403).
                follower.localizer.update();
                return;
            }
            follower.update();
            if (mode == Mode.FOLLOWING_PATH && follower.holding()) mode = Mode.HOLDING;
            return;
        }
        if (!armed || fallbackMotors == null) return;
        applyFallbackPowers(manualPowers);
    }

    /** Same mixing as Pedro's Mecanum: + strafe = left, + turn = counter-clockwise. */
    private void applyFallbackPowers(DrivePowers p) {
        double f = p.forward(), s = p.strafe(), t = p.turn();
        double fl = f - s - t;
        double fr = f + s + t;
        double bl = f + s - t;
        double br = f - s + t;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br))));
        setPower(fallbackMotors[0], fl / max);
        setPower(fallbackMotors[1], fr / max);
        setPower(fallbackMotors[2], bl / max);
        setPower(fallbackMotors[3], br / max);
    }

    private static void setPower(DcMotorEx motor, double power) {
        if (motor != null) motor.setPower(MathUtil.clamp(power, -1, 1));
    }
}
