package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.math.Pose;

/**
 * Hand-off from AUTO to TELEOP.
 *
 * AUTO calls save(...) EVERY loop (the 30 s timer can stop AUTO at any instant), and TeleOp reads it
 * in INIT. Static fields live as long as the Robot Controller app is running - like the SDK
 * "blackboard" they are NOT kept across a power cycle, app restart or a new code download, so TeleOp
 * checks isFresh() and falls back to a default pose if the data is old or missing.
 *
 * Pose is in the FTC BIOBUZZ frame (inches, radians) - the same frame our Pedro follower runs in.
 */
public final class PoseStorage {
    private PoseStorage() {
    }

    private static Pose pose = null;
    private static Alliance alliance = Alliance.RED;
    private static int ballCount = 0;
    private static HiveState hiveState = HiveState.A;
    private static long savedAtMs = 0;

    public static synchronized void save(Pose robotPose, Alliance a, int balls, HiveState hive) {
        pose = robotPose;
        alliance = a;
        ballCount = balls;
        hiveState = hive;
        savedAtMs = System.currentTimeMillis();
    }

    /** True if AUTO saved something in the last maxAgeMs milliseconds (a match is ~2.6 min). */
    public static synchronized boolean isFresh(long maxAgeMs) {
        return pose != null && System.currentTimeMillis() - savedAtMs < maxAgeMs;
    }

    public static synchronized Pose pose() {
        return pose;
    }

    public static synchronized Alliance alliance() {
        return alliance;
    }

    public static synchronized int ballCount() {
        return ballCount;
    }

    public static synchronized HiveState hiveState() {
        return hiveState;
    }

    public static synchronized double ageSeconds() {
        return pose == null ? Double.POSITIVE_INFINITY : (System.currentTimeMillis() - savedAtMs) / 1000.0;
    }

    public static synchronized void clear() {
        pose = null;
        savedAtMs = 0;
    }
}
