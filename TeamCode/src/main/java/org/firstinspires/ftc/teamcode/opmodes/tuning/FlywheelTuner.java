package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CsvLogger;

import java.util.Locale;

/**
 * FLYWHEEL TUNER (strategy brief testing plan, experiment 3: recovery during a 4-ball volley).
 *
 * Order of work:
 *  1. X = MAX SPEED TEST (full battery!): full power for 3 s, records the top ticks/s. Copy the result
 *     into RobotConfig.FLYWHEEL_MAX_TICKS_PER_SEC - the velocity PIDF F term is computed from it.
 *  2. A = spin on/off at the target RPM. Watch "measured" settle; check the wheel spins the right way.
 *  3. Y cycles what dpad up/down edits: RPM, P/F, I/F. Raise P until recovery is quick without
 *     oscillation; small I removes a steady offset. Copy the final numbers into RobotConfig.
 *  4. B = feed ONE ball (needs balls loaded and the gate tuned): look at "last shot: drop / recovery".
 *     Repeat with 4 balls to time a volley. Compare with and without the steel flywheel mass.
 * dpad left/right = RPM +-25 (fine). Every loop while spinning is logged to /sdcard/FIRST/data.
 */
@TeleOp(name = "Tune: Flywheel", group = "12303 Tuning")
public class FlywheelTuner extends LinearOpMode {

    private enum Edit { RPM, P_OVER_F, I_OVER_F }

    private Robot robot;
    private double targetRpm = 3500.0;
    private boolean spinning = false;
    private Edit edit = Edit.RPM;
    private double maxTestResult = 0.0;
    private CsvLogger log;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(50);
        robot = new Robot(hardwareMap, Alliance.RED);
        telemetry.addLine("Flywheel tuner ready. X = max speed test, A = spin, B = feed one ball.");
        robot.addWarnings(telemetry);
        telemetry.update();
        waitForStart();
        if (isStopRequested()) return;

        robot.arm();
        log = new CsvLogger("12303_flywheel", "t_s,target_rpm,measured_rpm,target_tps,measured_tps,at_speed,feed_state,battery_v");
        ElapsedTime clock = new ElapsedTime();

        while (opModeIsActive()) {
            boolean aPressed = gamepad1.aWasPressed();
            boolean bPressed = gamepad1.bWasPressed();
            boolean xPressed = gamepad1.xWasPressed();
            boolean yPressed = gamepad1.yWasPressed();
            boolean up = gamepad1.dpadUpWasPressed();
            boolean down = gamepad1.dpadDownWasPressed();
            boolean left = gamepad1.dpadLeftWasPressed();
            boolean right = gamepad1.dpadRightWasPressed();

            if (xPressed) runMaxSpeedTest();
            if (aPressed) spinning = !spinning;
            if (yPressed) edit = Edit.values()[(edit.ordinal() + 1) % Edit.values().length];
            if (up || down) changeSelected(up ? +1 : -1);
            if (left) targetRpm -= 25;
            if (right) targetRpm += 25;
            if (bPressed) robot.indexer.requestSingleShot();

            robot.setManualRpm(spinning ? targetRpm : 0.0);
            robot.update();

            if (spinning) {
                log.row(clock.seconds(), robot.shooter.targetRpm(), robot.shooter.measuredRpm(),
                        robot.shooter.targetRpm() * RobotConfig.FLYWHEEL_TICKS_PER_REV / 60.0,
                        robot.shooter.measuredTicksPerSec(), robot.shooter.atSpeed() ? 1 : 0,
                        robot.indexer.feedState(), robot.batteryVolts());
            }
            showTelemetry();
        }
        log.close();
    }

    private void changeSelected(int dir) {
        switch (edit) {
            case RPM:
                targetRpm += 100 * dir;
                break;
            case P_OVER_F:
                RobotConfig.FLYWHEEL_P_OVER_F = Math.max(0.0, RobotConfig.FLYWHEEL_P_OVER_F + 0.01 * dir);
                robot.shooter.applyPidf();
                break;
            case I_OVER_F:
                RobotConfig.FLYWHEEL_I_OVER_F = Math.max(0.0, RobotConfig.FLYWHEEL_I_OVER_F + 0.002 * dir);
                robot.shooter.applyPidf();
                break;
        }
        targetRpm = Math.max(0.0, Math.min(RobotConfig.FLYWHEEL_MAX_RPM, targetRpm));
    }

    /** Full power, no velocity control, for 3 s; remembers the highest ticks/s. */
    private void runMaxSpeedTest() {
        DcMotorEx motor = robot.shooter.motor();
        if (motor == null) return;
        robot.setManualRpm(0.0);
        spinning = false;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setPower(1.0);
        ElapsedTime t = new ElapsedTime();
        double max = 0.0;
        while (opModeIsActive() && t.seconds() < 3.0) {
            robot.clearBulkCache();
            max = Math.max(max, motor.getVelocity());
            telemetry.addLine(String.format(Locale.US, "MAX SPEED TEST... %.1f s  top so far %.0f ticks/s", t.seconds(), max));
            telemetry.update();
        }
        motor.setPower(0.0);
        robot.shooter.resetCommandCache();
        maxTestResult = max;
    }

    private void showTelemetry() {
        telemetry.addLine(String.format(Locale.US, "SPIN %s | target %.0f rpm | measured %.0f rpm (%.0f ticks/s) | %s",
                spinning ? "ON" : "off", targetRpm, robot.shooter.measuredRpm(), robot.shooter.measuredTicksPerSec(),
                robot.shooter.state()));
        telemetry.addLine("dpad up/down edits: " + edit + " (Y = next) | dpad left/right: RPM +-25");
        telemetry.addLine(String.format(Locale.US, "PIDF in use: P %.3f  I %.4f  D %.3f  F %.3f   (P/F %.3f, I/F %.4f)",
                RobotConfig.flywheelP(), RobotConfig.flywheelI(), RobotConfig.FLYWHEEL_D, RobotConfig.flywheelF(),
                RobotConfig.FLYWHEEL_P_OVER_F, RobotConfig.FLYWHEEL_I_OVER_F));
        telemetry.addLine(String.format(Locale.US, "last shot: drop %.0f rpm, recovery %.0f ms | balls %d | feed %s %s",
                robot.shooter.lastDipRpm(), robot.shooter.lastRecoveryMs(), robot.indexer.count(),
                robot.indexer.feedState(), robot.indexer.blockedBy()));
        if (maxTestResult > 0) {
            double f = 32767.0 / maxTestResult;
            telemetry.addLine(String.format(Locale.US,
                    "MAX TEST: %.0f ticks/s (= %.0f rpm). Paste: FLYWHEEL_MAX_TICKS_PER_SEC = %.0f;  -> F %.2f, P %.2f, I %.3f",
                    maxTestResult, maxTestResult * 60.0 / RobotConfig.FLYWHEEL_TICKS_PER_REV, maxTestResult, f, 0.1 * f, 0.01 * f));
        }
        telemetry.addLine(String.format(Locale.US, "battery %.2f V | loop %.0f ms | log: %s",
                robot.batteryVolts(), robot.loopMs(), log != null && log.isOpen() ? log.path() : "off"));
        telemetry.update();
    }
}
