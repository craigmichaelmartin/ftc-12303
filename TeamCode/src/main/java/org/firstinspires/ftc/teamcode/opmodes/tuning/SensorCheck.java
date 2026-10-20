package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.Locale;

/**
 * SENSOR CHECK - every sensor reading in one place, plus one-at-a-time actuator direction tests.
 *
 * COLOUR CLASSIFICATION (the main job): hold a POLLEN, a red NECTAR and a blue NECTAR in front of the
 * mouth sensor (where balls really pass) and note the HUE / SATURATION / DISTANCE. Then set the hue
 * windows, MIN_SATURATION and MOUTH_BALL_PRESENT_CM in RobotConfig section 10 so each ball shows the
 * right class with a margin. Do it again under the brightest and darkest light you expect.
 *
 * DIRECTION TESTS (after START, hold the button; each must make the robot do the described thing):
 *   dpad up = front-left wheel, dpad right = front-right, dpad left = back-left, dpad down = back-right
 *             -> each wheel alone must roll the robot FORWARD. If not, flip its *_DIRECTION.
 *   A = intake IN (balls go in)        B = spinners FORWARD (balls go up toward the gate)
 *   X = feeder gate OPEN position      Y = flywheel 1500 rpm (ball must go OUT)
 * Nothing moves during INIT.
 */
@TeleOp(name = "Check: Sensors", group = "12303 Tuning")
public class SensorCheck extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        Robot robot = new Robot(hardwareMap, Alliance.RED);
        GoBildaPinpointDriver pinpoint = OptionalHardware.getQuiet(hardwareMap, GoBildaPinpointDriver.class, RobotConfig.PINPOINT);
        DcMotorEx fl = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.FRONT_LEFT);
        DcMotorEx fr = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.FRONT_RIGHT);
        DcMotorEx bl = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.BACK_LEFT);
        DcMotorEx br = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.BACK_RIGHT);
        CRServo intake = OptionalHardware.getQuiet(hardwareMap, CRServo.class, RobotConfig.INTAKE);
        CRServo spinner = OptionalHardware.getQuiet(hardwareMap, CRServo.class, RobotConfig.INDEXER);
        Servo feeder = OptionalHardware.getQuiet(hardwareMap, Servo.class, RobotConfig.FEEDER);
        boolean shooterArmed = false;

        while (opModeInInit()) {
            robot.update();               // nothing is armed: reads only
            show(robot, pinpoint, false);
        }
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            // Direction tests: raw outputs, one device at a time, only while the button is held.
            power(fl, gamepad1.dpad_up ? 0.3 : 0.0);
            power(fr, gamepad1.dpad_right ? 0.3 : 0.0);
            power(bl, gamepad1.dpad_left ? 0.3 : 0.0);
            power(br, gamepad1.dpad_down ? 0.3 : 0.0);
            if (intake != null) intake.setPower(gamepad1.a ? RobotConfig.INTAKE_IN_POWER : 0.0);
            if (spinner != null) spinner.setPower(gamepad1.b ? RobotConfig.INDEXER_STAGE_POWER : 0.0);
            if (feeder != null) {
                feeder.setPosition(gamepad1.x ? RobotConfig.FEEDER_OPEN_POSITION : RobotConfig.FEEDER_CLOSED_POSITION);
            }
            if (gamepad1.y && !shooterArmed) {
                robot.shooter.arm();          // only the flywheel: the other subsystems stay unarmed
                shooterArmed = true;
            }
            robot.setManualRpm(gamepad1.y ? 1500.0 : 0.0);

            robot.update();
            show(robot, pinpoint, true);
        }
    }

    private static void power(DcMotorEx motor, double p) {
        if (motor != null) motor.setPower(p);
    }

    private void show(Robot robot, GoBildaPinpointDriver pinpoint, boolean started) {
        telemetry.addLine(started ? "HOLD: dpad = one wheel (must roll FORWARD), A intake, B spinners, X gate, Y flywheel"
                : "INIT: read-only. Press START for the direction tests.");
        telemetry.addLine(String.format(Locale.US, "MOUTH: %s | hue %.0f sat %.2f | dist %.1f cm (ball if < %.1f)",
                robot.indexer.mouthBall(), robot.indexer.mouthHue(), robot.indexer.mouthSaturation(),
                robot.indexer.mouthDistanceCm(), RobotConfig.MOUTH_BALL_PRESENT_CM));
        telemetry.addLine(String.format(Locale.US, "  windows: POLLEN %.0f-%.0f, RED <%.0f or >%.0f, BLUE %.0f-%.0f, min sat %.2f",
                RobotConfig.POLLEN_HUE_MIN, RobotConfig.POLLEN_HUE_MAX, RobotConfig.RED_HUE_MAX, RobotConfig.RED_HUE_MIN_WRAP,
                RobotConfig.BLUE_HUE_MIN, RobotConfig.BLUE_HUE_MAX, RobotConfig.MIN_SATURATION));
        telemetry.addLine(String.format(Locale.US, "SLOT 4: %s | dist %.1f cm (ball if < %.1f) %s",
                robot.indexer.slot4Occupied() ? "BALL" : "empty", robot.indexer.slot4DistanceCm(),
                RobotConfig.SLOT4_BALL_PRESENT_CM, robot.indexer.hasSlot4Sensor() ? "" : "(NO SENSOR)"));
        telemetry.addLine(String.format(Locale.US, "FEED BEAM: %s", robot.indexer.hasFeedBeam()
                ? (robot.indexer.beamBroken() ? "BROKEN (ball)" : "clear") : "not installed"));
        telemetry.addLine(String.format(Locale.US, "BALL COUNT %d (counts while the intake runs IN in the TeleOp)", robot.indexer.count()));
        if (pinpoint != null) {
            Pose p = robot.drive.pose();
            telemetry.addLine(String.format(Locale.US, "PINPOINT %s | x %.1f y %.1f h %.1f deg | %.0f Hz",
                    pinpoint.getDeviceStatus(), p.x(), p.y(), Math.toDegrees(p.heading()), pinpoint.getFrequency()));
            telemetry.addLine("  push the robot FORWARD: x must grow (heading 0); push LEFT: y must grow");
        } else {
            telemetry.addLine("PINPOINT: not found (name '" + RobotConfig.PINPOINT + "', I2C bus 1-3, not 0)");
        }
        telemetry.addLine(String.format(Locale.US, "FLYWHEEL %.0f rpm (%.0f ticks/s) | TURRET encoder %d ticks %s",
                robot.shooter.measuredRpm(), robot.shooter.measuredTicksPerSec(), robot.turret.encoderTicks(),
                robot.turret.hasEncoder() ? "" : "(no encoder)"));
        telemetry.addLine(String.format(Locale.US, "BATTERY %.2f V | loop %.0f ms", robot.batteryVolts(), robot.loopMs()));
        robot.addWarnings(telemetry);
        telemetry.update();
    }
}
