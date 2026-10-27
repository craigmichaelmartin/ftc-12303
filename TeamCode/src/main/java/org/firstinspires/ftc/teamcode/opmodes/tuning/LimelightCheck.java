package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;

import java.util.Locale;

/**
 * LIMELIGHT CHECK - is the camera calibrated well enough to trust?
 *
 * Shows every AprilTag the Limelight reports, which of OUR cells it belongs to, its estimated height
 * (UP cells ~49.8 in, DOWN cells ~35.5 in) and how far the measured bearing is from what odometry
 * predicts. Use it to set CAMERA_HEIGHT_IN / CAMERA_PITCH_DEG / CAMERA_FORWARD_IN / CAMERA_LEFT_IN /
 * CAMERA_YAW_OFFSET_DEG in RobotConfig. The camera looks out of the robot's BACK (yaw offset 180), like
 * the shooter, so "facing the HIVE" below means the robot's BACK toward the HIVE:
 *  - FIRST, over a USB cable from a laptop (never Wi-Fi streaming at events - R704.D, Q&A 26), set the
 *    ONE AprilTag pipeline: ID filter 30-45 (both HIVEs: the code keeps only our 8 - never filter to one
 *    alliance's 8 or the robot is blind on the other colour), marker size 82.55 mm (3.25 in), Full 3D ON
 *    (the "3D" column must show "y"), 640x480.
 *  - Put the robot on OUR S1 (front against our start wall, back toward the HIVE): the up-cell tags
 *    should read ~49-50 in and "UP". If they read too high/low, adjust CAMERA_PITCH_DEG (biggest effect)
 *    and CAMERA_HEIGHT_IN.
 *  - Then move it to the calibration spot (36, -12.75), back toward the HIVE, heading 0 (red; blue
 *    (-36, 12.75), heading 180), and press back so the pose matches: the other cell's tags (red 30-33 /
 *    blue 38-41, ~45 in away) must read ~35-36 in and "dn", and "vision says A".
 *    If EVERY height is off by the same FACTOR, fix the marker size, not the pitch.
 *  - "bearing err" should be within ~1-2 deg when the robot pose is right. A constant offset usually
 *    means CAMERA_YAW_OFFSET_DEG (or the camera position) is off.
 *  - The other alliance's tags are listed as "Ignored": that is normal (the filter must allow 30-45).
 *
 *   after START: X = BLUE, B = RED      back = robot is at the calibration spot      Y = flip HIVE state
 *   Nothing moves in this OpMode.
 */
@TeleOp(name = "Check: Limelight", group = "12303 Tuning")
public class LimelightCheck extends LinearOpMode {

    /** Calibration spot (red): 36 in from the HIVE axis, in front of the second cell's tags. */
    private static final double CALIBRATION_X = 36.0, CALIBRATION_Y = -12.75;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        Robot robot = new Robot(hardwareMap, Alliance.RED);
        Alliance alliance = Alliance.RED;
        robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
        long lastStatusMs = 0;
        String statusLine = "";

        telemetry.addLine("Limelight check. RED, robot assumed on red S1 (back toward the HIVE). After START: X = BLUE, B = RED, back = calibration spot.");
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        robot.addWarnings(telemetry);
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            boolean x = gamepad1.xWasPressed();
            boolean b = gamepad1.bWasPressed();
            boolean back = gamepad1.backWasPressed();
            boolean y = gamepad1.yWasPressed();
            if (x || b) {
                alliance = x ? Alliance.BLUE : Alliance.RED;
                robot.setAlliance(alliance);
                robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
            }
            if (back) {
                // Calibration spot, back (camera) toward the HIVE: red (36, -12.75) heading 0.
                robot.drive.setPose(FieldConstants.pose(alliance, CALIBRATION_X, CALIBRATION_Y, 0.0));
            }
            if (y) robot.hive.operatorToggle();

            robot.update();

            Limelight3A ll = robot.vision.limelight();
            if (ll == null) {
                telemetry.addLine("No Limelight named '" + RobotConfig.LIMELIGHT + "' in the configuration.");
                telemetry.update();
                continue;
            }
            long now = System.currentTimeMillis();
            if (now - lastStatusMs > 1000) {          // status is a separate request: not every loop
                lastStatusMs = now;
                LLStatus st = ll.getStatus();
                statusLine = String.format(Locale.US, "%s | temp %.0f C | cpu %.0f%% | fps %.0f | pipeline %d (%s)",
                        st.getName(), st.getTemp(), st.getCpu(), st.getFps(), st.getPipelineIndex(), st.getPipelineType());
            }
            telemetry.addLine("LL " + statusLine + (ll.isConnected() ? "" : " | NOT CONNECTED"));
            LLResult r = ll.getLatestResult();
            telemetry.addLine(String.format(Locale.US, "result valid %s | staleness %d ms | latency %.0f ms | alliance %s",
                    r != null && r.isValid(), r == null ? -1 : r.getStaleness(), robot.vision.latencyMs(), alliance));

            StringBuilder others = new StringBuilder();
            if (r != null && r.isValid() && r.getFiducialResults() != null) {
                for (LLResultTypes.FiducialResult f : r.getFiducialResults()) {
                    if (!alliance.ownsTag(f.getFiducialId())) others.append(f.getFiducialId()).append(' ');
                }
            }
            if (others.length() > 0) {
                telemetry.addLine("Ignored (other alliance, normal): " + others
                        + "- if this is YOUR practice HIVE, press X = BLUE / B = RED");
            }

            telemetry.addLine("id cell  tx    ty   az    el   height  UP?  3D  bearing err");
            for (Vision.TagSighting s : robot.vision.sightings()) {
                telemetry.addLine(String.format(Locale.US, "%d  %s  %+5.1f %+5.1f %+5.1f %5.1f  %5.1f  %s  %s  %+5.1f",
                        s.id, s.cell, s.txDeg, s.tyDeg, s.azimuthDeg, s.elevationDeg, s.heightIn,
                        s.looksUp ? "UP " : "dn ", s.used3d ? "y" : "n", s.bearingErrorDeg));
            }
            telemetry.addLine(String.format(Locale.US, "vision says HIVE %s | tracker state %s | aim bias %+.2f deg",
                    robot.vision.evidence() == null ? "-" : robot.vision.evidence().toString(), robot.hive.state(),
                    robot.vision.aimBiasDeg()));
            robot.addTelemetry(telemetry);
            telemetry.update();
        }
        robot.vision.stop();
    }
}
