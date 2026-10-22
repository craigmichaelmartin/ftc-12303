package org.firstinspires.ftc.teamcode.util;

import java.util.Locale;

/**
 * Tells which odometry pod is on the robot from one measured push, and what to put in RobotConfig
 * section 3. Pure math, so it is unit-tested on the laptop (OdometryPodsTest).
 *
 * The three pods we expect differ a lot in counts per inch, so a hand push along a tape measure is
 * accurate enough to tell them apart (the windows below do not overlap).
 */
public final class OdometryPods {
    private OdometryPods() {
    }

    public enum Kind {
        /** 2000 counts / (pi x 32 mm) = 19.894 counts/mm (FTC SDK goBILDA_4_BAR_POD). */
        GOBILDA_4_BAR("goBILDA 4-bar pod (32 mm wheel)", 19.894367 * 25.4),
        /** 2000 counts / (pi x 48 mm) = 13.263 counts/mm (FTC SDK goBILDA_SWINGARM_POD). */
        GOBILDA_SWINGARM("goBILDA swing-arm pod (48 mm wheel)", 13.262912 * 25.4),
        /** 4096 counts / (pi x 38 mm) = 34.31 counts/mm (SWYFT's Pinpoint example, docs/research/R5-hardware.md). */
        SWYFT_38MM("SWYFT 38 mm odometry wheel", 34.31 * 25.4);

        public final String label;
        public final double countsPerInch;

        Kind(String label, double countsPerInch) {
            this.label = label;
            this.countsPerInch = countsPerInch;
        }
    }

    /** A measurement matches a pod when it is within this fraction of the pod's counts per inch. */
    public static final double MATCH_TOLERANCE = 0.06;

    /** Pushes shorter than this are too noisy to identify a pod. */
    public static final double MIN_PUSH_IN = 20.0;

    /** Counts per inch from one push. The sign is ignored here (the direction is checked separately). */
    public static double countsPerInch(int deltaCounts, double pushedInches) {
        if (pushedInches <= 0.0) return 0.0;
        return Math.abs(deltaCounts) / pushedInches;
    }

    /** The pod whose counts per inch is within MATCH_TOLERANCE of the measurement, or null if none is. */
    public static Kind identify(double countsPerInch) {
        Kind best = null;
        double bestError = Double.MAX_VALUE;
        for (Kind k : Kind.values()) {
            double error = Math.abs(countsPerInch / k.countsPerInch - 1.0);
            if (error < bestError) {
                bestError = error;
                best = k;
            }
        }
        return bestError <= MATCH_TOLERANCE ? best : null;
    }

    /** What to set in RobotConfig section 3 for this measurement. */
    public static String configAdvice(double countsPerInch) {
        Kind k = identify(countsPerInch);
        if (k == Kind.GOBILDA_4_BAR) {
            return "PINPOINT_POD_TYPE = goBILDA_4_BAR_POD and PINPOINT_CUSTOM_TICKS_PER_INCH = 0";
        }
        if (k == Kind.GOBILDA_SWINGARM) {
            return "PINPOINT_POD_TYPE = goBILDA_SWINGARM_POD and PINPOINT_CUSTOM_TICKS_PER_INCH = 0";
        }
        if (k == Kind.SWYFT_38MM) {
            return String.format(Locale.US, "PINPOINT_CUSTOM_TICKS_PER_INCH = %.1f (SWYFT nominal; measured %.1f)",
                    k.countsPerInch, countsPerInch);
        }
        return String.format(Locale.US, "PINPOINT_CUSTOM_TICKS_PER_INCH = %.1f (not a pod we know: use the measured "
                + "value, and repeat with a longer push to refine it)", countsPerInch);
    }

    /**
     * Pinpoint convention: the X (forward) pod must count UP when the robot is pushed FORWARD, and the Y
     * (strafe) pod must count UP when it is pushed LEFT. A pod that counts down needs REVERSED.
     */
    public static String directionAdvice(int deltaCounts) {
        return deltaCounts >= 0 ? "FORWARD" : "REVERSED";
    }
}
