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

import java.util.Locale;

/**
 * LIMELIGHT CHECK - is the camera calibrated well enough to trust?
 *
 * Shows every AprilTag the Limelight reports, which of OUR cells it belongs to, its estimated height
 * (UP cells ~49.8 in, DOWN cells ~35.5 in) and how far the measured bearing is from what odometry
 * predicts. Use it to set CAMERA_HEIGHT_IN / CAMERA_PITCH_DEG / CAMERA_FORWARD_IN in RobotConfig:
 *  - Put the robot on S1 (red) facing the HIVE: cell A tags should read ~49-50 in and "UP".
 *    If they read too high/low, adjust CAMERA_PITCH_DEG (biggest effect) and CAMERA_HEIGHT_IN.
 *  - "bearing err" should be within ~1-2 deg when the robot pose is right. A constant offset usually
 *    means CAMERA_YAW_OFFSET_DEG or TURRET_CENTER_DEG is off.
 *  - Opponent tags should NOT appear: set the pipeline ID filter to our 8 ids (red 30-37, blue 38-45).
 *  - Tune pipelines over a USB cable from a laptop - never Wi-Fi streaming at events (R704.D, Q&A 26).
 *
 *   X = BLUE, B = RED      A = auto-aim on/off (turret moves!)      Y = flip HIVE state
 */
@TeleOp(name = "Check: Limelight", group = "12303 Tuning")
public class LimelightCheck extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        Robot robot = new Robot(hardwareMap, Alliance.RED);
        Alliance alliance = Alliance.RED;
        robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
        boolean aiming = false;
        long lastStatusMs = 0;
        String statusLine = "";

        telemetry.addLine("Limelight check. Robot assumed on S1 (red). Press START.");
        robot.addWarnings(telemetry);
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            boolean x = gamepad1.xWasPressed();
            boolean b = gamepad1.bWasPressed();
            boolean a = gamepad1.aWasPressed();
            boolean y = gamepad1.yWasPressed();
            if (x || b) {
                alliance = x ? Alliance.BLUE : Alliance.RED;
                robot.setAlliance(alliance);
                robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
            }
            if (a) {
                aiming = !aiming;
                if (aiming && !robot.isArmed()) robot.arm();
                robot.setAutoAim(aiming);
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
            telemetry.addLine(String.format(Locale.US, "result valid %s | staleness %d ms | latency %.0f ms | alliance %s | auto-aim %s",
                    r != null && r.isValid(), r == null ? -1 : r.getStaleness(), robot.vision.latencyMs(), alliance, aiming ? "ON" : "off"));

            StringBuilder others = new StringBuilder();
            if (r != null && r.isValid() && r.getFiducialResults() != null) {
                for (LLResultTypes.FiducialResult f : r.getFiducialResults()) {
                    if (!alliance.ownsTag(f.getFiducialId())) others.append(f.getFiducialId()).append(' ');
                }
            }
            if (others.length() > 0) telemetry.addLine("Ignored (not ours): " + others + " <- set the pipeline ID filter");

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
