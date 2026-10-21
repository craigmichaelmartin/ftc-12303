package org.firstinspires.ftc.teamcode.util;

/**
 * The time source for the 12303 subsystems (milliseconds).
 *
 * On the robot this is simply System.currentTimeMillis(). The JUnit tests (TeamCode/src/test) switch it
 * to a manual clock so ball-handling and HIVE-tracker timing can be checked exactly, without sleeping.
 * Robot code never calls the *ForTests methods.
 */
public final class Clock {
    private Clock() {
    }

    /** Negative = use the real clock. */
    private static volatile long manualMs = -1;

    /** Current time in milliseconds. */
    public static long nowMs() {
        long m = manualMs;
        return m >= 0 ? m : System.currentTimeMillis();
    }

    /** Tests only: freeze the clock at this time (ms, must be >= 0). */
    public static void setManualForTests(long ms) {
        manualMs = Math.max(0, ms);
    }

    /** Tests only: move the frozen clock forward. */
    public static void advanceForTests(long ms) {
        if (manualMs >= 0) manualMs += Math.max(0, ms);
    }

    /** Tests only: go back to the real clock. */
    public static void useRealTime() {
        manualMs = -1;
    }
}
