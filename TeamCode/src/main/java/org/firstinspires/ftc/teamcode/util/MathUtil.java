package org.firstinspires.ftc.teamcode.util;

/**
 * Small math helpers. No FTC imports, so this can be unit-tested on a laptop.
 */
public final class MathUtil {
    private MathUtil() {
    }

    /** Wrap an angle in degrees into the range (-180, 180]. */
    public static double wrapDeg(double deg) {
        double a = deg % 360.0;
        if (a <= -180.0) a += 360.0;
        if (a > 180.0) a -= 360.0;
        return a;
    }

    /** Wrap an angle in radians into the range (-pi, pi]. */
    public static double wrapRad(double rad) {
        return Math.toRadians(wrapDeg(Math.toDegrees(rad)));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Field bearing in degrees from point (fromX, fromY) to point (toX, toY). 0 = +X, CCW positive. */
    public static double bearingDeg(double fromX, double fromY, double toX, double toY) {
        return Math.toDegrees(Math.atan2(toY - fromY, toX - fromX));
    }

    /** Returns 0 when |value| is below the deadband, so tiny stick drift does not move the robot. */
    public static double deadband(double value, double deadband) {
        return Math.abs(value) < deadband ? 0.0 : value;
    }
}
