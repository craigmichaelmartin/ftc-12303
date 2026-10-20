package org.firstinspires.ftc.teamcode.util;

import java.util.Locale;

/**
 * A 3-D point on the field in the FTC BIOBUZZ frame (inches). Used for aim points and tag positions.
 * Immutable: create a new one instead of changing it.
 */
public final class FieldPoint {
    public final double x;
    public final double y;
    public final double z;

    public FieldPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** This point for the given alliance, if it was written for RED (rotate 180 deg for BLUE). */
    public FieldPoint forAlliance(Alliance alliance) {
        return new FieldPoint(alliance.fromRedX(x), alliance.fromRedY(y), z);
    }

    /** Horizontal (floor) distance from (px, py) to this point, inches. */
    public double horizontalDistanceFrom(double px, double py) {
        return Math.hypot(x - px, y - py);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "(%.1f, %.1f, %.1f)", x, y, z);
    }
}
