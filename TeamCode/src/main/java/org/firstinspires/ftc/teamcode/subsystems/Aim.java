package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.MathUtil;

/**
 * Aiming with a FIXED shooter. The shooting wheel points RobotConfig.SHOOTER_FACING_DEG from the robot
 * front (180 = straight out of the back), so the robot aims by TURNING until its back points at the up
 * cell. Pure math, no hardware: AimTest checks it on the laptop.
 *
 *   aim heading   = bearing (robot -> aim point) - SHOOTER_FACING_DEG
 *                   - a small correction when the shooter sits beside the robot's centre line
 *                   + operator trim + vision bias
 *   heading error = aim heading - robot heading (wrapped to +-180): turn counter-clockwise by this much
 *   distance      = from where the ball leaves the robot once it is AIMED (the shooter position at the aim
 *                   heading) to the aim point: the shot-table input. It does not change while the robot turns.
 *   tolerance     = from the robot-CENTRE distance and the opening's width (RobotConfig section 6): a heading
 *                   error turns the shot line about the robot centre.
 *
 * Who turns the robot: in TeleOp the driver, holding RB for aim-assist (assistTurn); in AUTO the shooting
 * poses already face the target and, during a volley, the robot turns itself onto the aim heading.
 * FIRE GATE with auto-aim (readyToFire): the target opening faces the robot (off-axis <=
 * FIRE_MAX_OFF_AXIS_DEG) and the heading error is inside the tolerance.
 */
public final class Aim {
    private Aim() {
    }

    /** One aim calculation. All angles in degrees. */
    public static class Solution {
        /**
         * Horizontal distance (inches) from where the ball leaves the shooter, with the robot turned to the aim
         * heading, to the aim point: the shot-table input. The same whichever way the robot faces right now.
         */
        public double distanceIn;
        /** Field heading the robot needs so the shooter points at the aim point (trim and vision bias included). */
        public double aimHeadingDeg;
        /** Turn the robot counter-clockwise by this many degrees to aim (negative = clockwise). */
        public double headingErrorDeg;
        /** Largest heading error a shot may still be fired with, from here. */
        public double toleranceDeg;

        /** True when the robot's heading is inside the aim tolerance. */
        public boolean onTarget() {
            return Math.abs(headingErrorDeg) <= toleranceDeg;
        }
    }

    /** Where the ball leaves the shooter on the field, {x, y}, for this robot pose (inches). */
    public static double[] shooterOnField(Pose robot) {
        double h = robot.heading();
        double fwd = RobotConfig.SHOOTER_FORWARD_IN;
        double left = RobotConfig.SHOOTER_LEFT_IN;
        return new double[]{
                robot.x() + fwd * Math.cos(h) - left * Math.sin(h),
                robot.y() + fwd * Math.sin(h) + left * Math.cos(h)};
    }

    /**
     * How to point the shooter at a field point from this robot pose.
     * @param offAxisDeg    how far off the target opening's axis the robot is (FieldConstants.offAxisDeg)
     * @param trimDeg       operator aim trim (+ = counter-clockwise)
     * @param visionBiasDeg Limelight correction (+ = the target is further counter-clockwise than odometry thinks)
     */
    public static Solution solve(FieldPoint target, Pose robot, double offAxisDeg, double trimDeg, double visionBiasDeg) {
        Solution s = new Solution();
        double bearing = MathUtil.bearingDeg(robot.x(), robot.y(), target.x, target.y);
        double centreDistance = target.horizontalDistanceFrom(robot.x(), robot.y());
        s.aimHeadingDeg = MathUtil.wrapDeg(bearing - RobotConfig.SHOOTER_FACING_DEG
                - sideCorrectionDeg(centreDistance) + trimDeg + visionBiasDeg);
        s.headingErrorDeg = MathUtil.wrapDeg(s.aimHeadingDeg - Math.toDegrees(robot.heading()));
        // Measured from where the shooter WILL be once the robot has turned onto the aim (it turns on the
        // spot), so the shot-table RPM does not swing while the robot turns.
        double[] shooter = shooterOnField(new Pose(robot.x(), robot.y(), Math.toRadians(s.aimHeadingDeg)));
        s.distanceIn = target.horizontalDistanceFrom(shooter[0], shooter[1]);
        // A heading error turns the shot line about the robot CENTRE: that distance is the lever arm.
        s.toleranceDeg = toleranceDeg(centreDistance, offAxisDeg);
        return s;
    }

    /**
     * A shooter beside the robot's centre line shoots along a line beside the robot centre, so the robot
     * turns a little more to put that line through the target: asin(sideways offset / distance).
     * 0 when the shooter is on the centre line (SHOOTER_LEFT_IN = 0).
     */
    public static double sideCorrectionDeg(double centreDistanceIn) {
        double facing = Math.toRadians(RobotConfig.SHOOTER_FACING_DEG);
        // Offset of the shot line from the robot centre, + = to the LEFT of the shot direction.
        double side = -RobotConfig.SHOOTER_FORWARD_IN * Math.sin(facing) + RobotConfig.SHOOTER_LEFT_IN * Math.cos(facing);
        double ratio = MathUtil.clamp(side / Math.max(1.0, centreDistanceIn), -1.0, 1.0);
        return Math.toDegrees(Math.asin(ratio));
    }

    /**
     * Aim tolerance (deg) for a robot whose CENTRE is this far from the aim point, at this off-axis angle:
     * AIM_TOLERANCE_FRACTION of the heading error at which the ball would just touch the side of the opening
     * (the opening looks narrower from off its axis), kept between AIM_TOLERANCE_MIN_DEG and
     * AIM_TOLERANCE_MAX_DEG. The robot turns about its centre, so the centre distance is the lever arm.
     */
    public static double toleranceDeg(double centreDistanceIn, double offAxisDeg) {
        double off = Math.toRadians(MathUtil.clamp(offAxisDeg, 0.0, 89.0));
        double freeHalfWidth = 0.5 * FieldConstants.CELL_OPENING_WIDTH_IN * Math.cos(off) - FieldConstants.POLLEN_RADIUS_IN;
        if (freeHalfWidth <= 0.0) return RobotConfig.AIM_TOLERANCE_MIN_DEG;
        double edgeDeg = Math.toDegrees(Math.atan2(freeHalfWidth, Math.max(1.0, centreDistanceIn)));
        return MathUtil.clamp(RobotConfig.AIM_TOLERANCE_FRACTION * edgeDeg,
                RobotConfig.AIM_TOLERANCE_MIN_DEG, RobotConfig.AIM_TOLERANCE_MAX_DEG);
    }

    /** The auto-aim fire gate: the target opening faces the robot and the heading is inside the tolerance. */
    public static boolean readyToFire(Solution s, double offAxisDeg) {
        return offAxisDeg <= RobotConfig.FIRE_MAX_OFF_AXIS_DEG && s.onTarget();
    }

    /**
     * Driver aim-assist turn power (+ = counter-clockwise) for this aim: 0 inside half of the tolerance (so
     * the robot settles instead of hunting), else AIM_ASSIST_MIN_TURN + AIM_ASSIST_TURN_KP x error, at most
     * AIM_ASSIST_MAX_TURN.
     */
    public static double assistTurn(Solution s) {
        double error = s.headingErrorDeg;
        if (Math.abs(error) <= 0.5 * s.toleranceDeg) return 0.0;
        double turn = Math.signum(error) * RobotConfig.AIM_ASSIST_MIN_TURN + RobotConfig.AIM_ASSIST_TURN_KP * error;
        return MathUtil.clamp(turn, -RobotConfig.AIM_ASSIST_MAX_TURN, RobotConfig.AIM_ASSIST_MAX_TURN);
    }
}
