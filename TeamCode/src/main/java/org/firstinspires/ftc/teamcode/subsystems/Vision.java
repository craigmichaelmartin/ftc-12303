package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Limelight 3A wrapper for BIOBUZZ AprilTags.
 *
 * MOUNT: fixed to the chassis, looking out of the robot's BACK (CAMERA_YAW_OFFSET_DEG = 180), the same way
 * as the shooter, pitched up ~35-40 deg. So while the robot aims (back toward the up cell) the camera sees
 * that cell's tags. Its position on the robot is CAMERA_FORWARD_IN / CAMERA_LEFT_IN from the centre.
 *
 * WHAT WE USE THE TAGS FOR (and what not):
 *  - The tags are on the HIVE cells, which MOVE when the HIVE tips, so they are NOT used to find the
 *    robot's field position (SDK 12.0 README: "not suitable for absolute Field Localization").
 *    The Pinpoint does positioning.
 *  - Aim correction ("vision bias"): for each visible tag of the cell we are aiming at, compare the
 *    bearing the camera MEASURES with the bearing odometry PREDICTS (we know where each tag is from
 *    the CAD). The low-passed difference is added to the aim heading. This fixes small odometry
 *    (heading) and field-tolerance errors. It is only updated while the chassis is neither turning nor
 *    driving, so camera latency does not matter.
 *  - Which cell is up: up-cell tags are ~49.8 in above the floor, down-cell tags ~35.5 in (R2 7.3).
 *    We estimate each tag's height from the camera angle and give HiveTracker an opinion (A or B).
 *    After a TIP all our tags face the OTHER half of the field, so the camera confirms a TIP only from
 *    the zone the new up cell faces (never our own TIP from the zone we fired from).
 *
 * OWN ALLIANCE ONLY: red 30-37, blue 38-45. Opponent tags are ignored here - launching at the opponent's
 * HIVE to stop its TIP is STRATEGIC (G417.D). The code uses ONE Limelight pipeline for both alliances,
 * so set that pipeline's ID filter to 30-45 (never to one alliance's 8 ids: on the other alliance the
 * robot would then see none of its own tags). Also: 640x480, ~50 fps, marker size 82.55 mm (3.25 in),
 * Full 3D ON (heights then come from the 3-D tag position; "3D" shows y in Check: Limelight).
 *
 * SIGN CONVENTIONS (Limelight docs): tx > 0 = target RIGHT of the crosshair, ty > 0 = target ABOVE.
 * Camera space: X right, Y down, Z out of the lens (the SDK reports metres; we convert to inches).
 * Check all of this with "Check: Limelight" before trusting the HIVE auto-switch.
 */
public class Vision {

    public enum State {
        /** No Limelight configured. */
        DISABLED,
        /** No fresh result (not started, stale, or nothing seen). */
        NO_TARGETS,
        /** At least one of OUR tags is visible. */
        SEES_OWN_TAGS
    }

    /** One of our tags seen in the latest frame. */
    public static class TagSighting {
        public int id;
        /** Which of our cells: A = up at match start, B = the other one. */
        public HiveState cell;
        public double txDeg;
        public double tyDeg;
        /** Horizontal angle from the camera's forward direction, deg, + = left (CCW). */
        public double azimuthDeg;
        /** Angle above horizontal, deg. */
        public double elevationDeg;
        /** Estimated height of the tag above the tiles, inches. */
        public double heightIn;
        /** true if the height says its cell is UP. */
        public boolean looksUp;
        /** Measured minus predicted field bearing, deg (only meaningful for the cell we aim at). */
        public double bearingErrorDeg;
        /** true if the 3-D camera-space position was available (pipeline "Full 3D" on). */
        public boolean used3d;
    }

    private final Limelight3A limelight;
    private Alliance alliance;
    private State state;
    private final List<TagSighting> sightings = new ArrayList<>();
    private HiveState evidence = null;
    private double aimBiasDeg = 0.0;
    private double lastLatencyMs = 0.0;
    private long lastUpdateNs = 0;
    private int ownTagsSeen = 0;
    private int outlierRun = 0;
    private double outlierRunSum = 0.0;
    private double outlierRunFirst = 0.0;

    public Vision(HardwareMap hw, Alliance alliance, List<String> warnings) {
        this.alliance = alliance;
        Limelight3A ll = OptionalHardware.get(hw, Limelight3A.class, RobotConfig.LIMELIGHT, warnings);
        if (ll != null) {
            try {
                // Starting the camera is not an actuator, so it is fine during TeleOp INIT.
                ll.setPollRateHz(RobotConfig.LIMELIGHT_POLL_HZ);
                ll.pipelineSwitch(RobotConfig.LIMELIGHT_APRILTAG_PIPELINE);
                ll.start();
            } catch (RuntimeException e) {
                warnings.add("Limelight did not start (" + e.getMessage() + ") - vision off");
                ll = null;
            }
        }
        limelight = ll;
        state = (limelight != null) ? State.NO_TARGETS : State.DISABLED;
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    public boolean isPresent() {
        return limelight != null;
    }

    /** Stop polling (call when the OpMode ends). */
    public void stop() {
        if (limelight != null) limelight.stop();
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    /**
     * @param robotPose        robot pose from the Pinpoint (field frame)
     * @param targetCell       the cell we are aiming at (its tags feed the aim bias)
     * @param stableForBias    true when the chassis is not turning or driving
     */
    public void update(Pose robotPose, HiveState targetCell, boolean stableForBias) {
        long now = System.nanoTime();
        double dt = (lastUpdateNs == 0) ? 0.0 : (now - lastUpdateNs) / 1e9;
        lastUpdateNs = now;
        sightings.clear();
        evidence = null;
        ownTagsSeen = 0;
        if (limelight == null) {
            state = State.DISABLED;
            return;
        }

        LLResult result = limelight.getLatestResult();   // never null since SDK 10.3; check isValid()
        if (result == null || !result.isValid() || result.getStaleness() > RobotConfig.VISION_MAX_STALENESS_MS) {
            state = State.NO_TARGETS;
            decayBias(dt);
            return;
        }
        lastLatencyMs = result.getCaptureLatency() + result.getTargetingLatency();

        double[] cam = cameraPose(robotPose);   // {x, y, yawDeg}
        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null) fiducials = Collections.emptyList();
        for (LLResultTypes.FiducialResult f : fiducials) {
            TagSighting s = makeSighting(f, cam, targetCell);
            if (s != null) sightings.add(s);
        }
        ownTagsSeen = sightings.size();
        state = ownTagsSeen > 0 ? State.SEES_OWN_TAGS : State.NO_TARGETS;
        evidence = computeEvidence();
        updateBias(targetCell, stableForBias, dt);
    }

    /**
     * Camera space (X right, Y down, Z out of the lens) -> a LEVEL frame that turns with the camera:
     * {forward, left, up}. The camera is tilted UP by pitchDeg. Pure math, easy to test.
     */
    public static double[] cameraToLevel(double camX, double camY, double camZ, double pitchDeg) {
        double pitch = Math.toRadians(pitchDeg);
        double fwd = camY * Math.sin(pitch) + camZ * Math.cos(pitch);
        double left = -camX;
        double up = -camY * Math.cos(pitch) + camZ * Math.sin(pitch);
        return new double[]{fwd, left, up};
    }

    /**
     * Camera position and yaw on the field: {x, y, yawDeg}. The camera is fixed to the chassis at
     * (CAMERA_FORWARD_IN, CAMERA_LEFT_IN) from the robot centre, looking CAMERA_YAW_OFFSET_DEG from the
     * robot front (180 = out of the back).
     */
    public static double[] cameraPose(Pose robot) {
        double h = robot.heading();
        double fwd = RobotConfig.CAMERA_FORWARD_IN, left = RobotConfig.CAMERA_LEFT_IN;
        return new double[]{
                robot.x() + fwd * Math.cos(h) - left * Math.sin(h),
                robot.y() + fwd * Math.sin(h) + left * Math.cos(h),
                MathUtil.wrapDeg(Math.toDegrees(h) + RobotConfig.CAMERA_YAW_OFFSET_DEG)};
    }

    /**
     * Measured minus predicted field bearing to a tag (deg, + = the tag is further counter-clockwise than
     * odometry thinks). measured = camera yaw + the tag's azimuth in the camera (+ = left); predicted =
     * bearing from the camera position to the tag's CAD position.
     */
    public static double bearingErrorDeg(double[] cam, double azimuthDeg, FieldPoint tag) {
        double measured = cam[2] + azimuthDeg;
        double predicted = MathUtil.bearingDeg(cam[0], cam[1], tag.x, tag.y);
        return MathUtil.wrapDeg(measured - predicted);
    }

    private TagSighting makeSighting(LLResultTypes.FiducialResult f, double[] cam, HiveState targetCell) {
        int id = f.getFiducialId();
        if (!alliance.ownsTag(id)) return null;                 // own alliance only (G417.D)
        HiveState cell = FieldConstants.cellOfTag(alliance, id);
        if (cell == null) return null;

        TagSighting s = new TagSighting();
        s.id = id;
        s.cell = cell;
        s.txDeg = f.getTargetXDegrees();
        s.tyDeg = f.getTargetYDegrees();

        // Direction to the tag in a LEVEL frame attached to the camera: forward, left, up.
        double camX, camY, camZ;               // camera space: X right, Y down, Z out of the lens (inches)
        Position p = positionInInches(f.getTargetPoseCameraSpace());
        if (p != null) {
            camX = p.x;
            camY = p.y;
            camZ = p.z;
            s.used3d = true;
        } else {
            camX = Math.tan(Math.toRadians(s.txDeg));
            camY = -Math.tan(Math.toRadians(s.tyDeg));
            camZ = 1.0;
            s.used3d = false;
        }
        double[] level = cameraToLevel(camX, camY, camZ, RobotConfig.CAMERA_PITCH_DEG);
        double fwd = level[0], left = level[1], up = level[2];
        s.azimuthDeg = Math.toDegrees(Math.atan2(left, fwd));
        s.elevationDeg = Math.toDegrees(Math.atan2(up, Math.hypot(fwd, left)));

        // Height: from the 3-D position if we have it, else from the angle + odometry range.
        FieldPoint tagUp = FieldConstants.tagPosition(alliance, id, true);
        if (s.used3d) {
            s.heightIn = RobotConfig.CAMERA_HEIGHT_IN + up;             // up is in inches here
        } else {
            double range = tagUp.horizontalDistanceFrom(cam[0], cam[1]);
            s.heightIn = RobotConfig.CAMERA_HEIGHT_IN + range * Math.tan(Math.toRadians(s.elevationDeg));
        }
        s.looksUp = s.heightIn > RobotConfig.TAG_UP_HEIGHT_THRESHOLD_IN;

        // Bearing check (only used for the cell we aim at, which should be up).
        s.bearingErrorDeg = bearingErrorDeg(cam, s.azimuthDeg, tagUp);
        return s;
    }

    /** Camera-space position in INCHES, or null if the pipeline did not give one (all zeros). */
    private static Position positionInInches(Pose3D pose) {
        if (pose == null || pose.getPosition() == null) return null;
        Position p = pose.getPosition();
        if (Math.abs(p.x) + Math.abs(p.y) + Math.abs(p.z) < 1e-6) return null;
        if (p.unit == null) p = new Position(DistanceUnit.METER, p.x, p.y, p.z, 0);   // SDK builds these in metres
        return p.toUnit(DistanceUnit.INCH);
    }

    /**
     * Which HIVE state do the tags say? A cell seen HIGH is up; a cell seen LOW is down.
     * Returns null if we see too few tags or the tags disagree.
     */
    private HiveState computeEvidence() {
        if (ownTagsSeen < RobotConfig.HIVE_VISION_MIN_TAGS) return null;
        int votesA = 0, votesB = 0;
        for (TagSighting s : sightings) {
            boolean saysA = (s.cell == HiveState.A) == s.looksUp;  // cell A up, or cell B down
            if (saysA) votesA++;
            else votesB++;
        }
        if (votesA > 0 && votesB == 0) return HiveState.A;
        if (votesB > 0 && votesA == 0) return HiveState.B;
        return null;
    }

    private void updateBias(HiveState targetCell, boolean stableForBias, double dt) {
        double sum = 0.0;
        int n = 0;
        for (TagSighting s : sightings) {
            if (s.cell == targetCell && s.looksUp) {
                sum += s.bearingErrorDeg;
                n++;
            }
        }
        if (n == 0) {
            decayBias(dt);
            return;
        }
        if (!RobotConfig.VISION_BIAS_ENABLED || !stableForBias) return;
        double measuredBias = sum / n;
        if (Math.abs(measuredBias - aimBiasDeg) > RobotConfig.VISION_BIAS_OUTLIER_DEG
                && Math.abs(aimBiasDeg) > 0.5) {
            // Wild jump: wrong tag, partner robot in the way, motion blur... ignore this frame - unless the
            // "outliers" keep agreeing with each other: then the error is real (e.g. odometry pods slipped
            // after contact) and we jump to it.
            if (outlierRun > 0 && Math.abs(measuredBias - outlierRunFirst) > RobotConfig.VISION_BIAS_OUTLIER_AGREE_DEG) {
                outlierRun = 0;
                outlierRunSum = 0.0;
            }
            if (outlierRun == 0) outlierRunFirst = measuredBias;
            outlierRun++;
            outlierRunSum += measuredBias;
            if (outlierRun >= RobotConfig.VISION_BIAS_OUTLIER_ACCEPT_FRAMES) {
                aimBiasDeg = MathUtil.clamp(outlierRunSum / outlierRun,
                        -RobotConfig.VISION_BIAS_MAX_DEG, RobotConfig.VISION_BIAS_MAX_DEG);
                outlierRun = 0;
                outlierRunSum = 0.0;
            }
            return;
        }
        outlierRun = 0;
        outlierRunSum = 0.0;
        aimBiasDeg += RobotConfig.VISION_BIAS_GAIN * (measuredBias - aimBiasDeg);
        aimBiasDeg = MathUtil.clamp(aimBiasDeg, -RobotConfig.VISION_BIAS_MAX_DEG, RobotConfig.VISION_BIAS_MAX_DEG);
    }

    private void decayBias(double dt) {
        aimBiasDeg *= Math.max(0.0, 1.0 - RobotConfig.VISION_BIAS_DECAY_PER_SEC * dt);
    }

    /** Forget the aim correction (call after a TIP: we now aim at a different cell). */
    public void resetBias() {
        aimBiasDeg = 0.0;
        outlierRun = 0;
        outlierRunSum = 0.0;
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    public State state() {
        return state;
    }

    public List<TagSighting> sightings() {
        return sightings;
    }

    /** What the tags say about the HIVE right now (null = no opinion). */
    public HiveState evidence() {
        return evidence;
    }

    /** Low-passed aim correction to add to the aim heading, degrees (+ = counter-clockwise). */
    public double aimBiasDeg() {
        return RobotConfig.VISION_BIAS_ENABLED ? aimBiasDeg : 0.0;
    }

    public int ownTagsSeen() {
        return ownTagsSeen;
    }

    /** True if any tag of this cell is visible in the latest frame. */
    public boolean seesCell(HiveState cell) {
        for (TagSighting s : sightings) if (s.cell == cell) return true;
        return false;
    }

    /** True if a tag of this cell is visible AND looks up. */
    public boolean seesCellUp(HiveState cell) {
        for (TagSighting s : sightings) if (s.cell == cell && s.looksUp) return true;
        return false;
    }

    public double latencyMs() {
        return lastLatencyMs;
    }

    /** Raw device for LimelightCheck (status, pipeline). May be null. */
    public Limelight3A limelight() {
        return limelight;
    }
}
