package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.Locale;

/**
 * SENSOR CHECK - every sensor reading in one place, plus one-at-a-time actuator direction tests.
 *
 * DIRECTION TESTS (after START, hold the button; each must make the robot do the described thing):
 *   dpad up = front-left wheel, dpad right = front-right, dpad left = back-left, dpad down = back-right
 *             -> each wheel alone must roll the robot FORWARD (toward the intake). If not, flip its *_DIRECTION.
 *   A = intake IN: the front wheel pulls balls in        -> else flip INTAKE_DIRECTION
 *   B = carrier FORWARD: balls move toward the shooting wheel at the back -> else flip CARRIER_DIRECTION
 *   X = carrier BACKWARD (the eject direction): balls move toward the intake
 *   Y = flywheel 1500 rpm: a ball pushed in must go OUT of the robot's back -> else flip FLYWHEEL_DIRECTION
 *   Run B and Y with NO balls in the robot first (B with balls and the flywheel stopped can jam a ball).
 * Nothing moves during INIT.
 *
 * BALL SENSORS (all optional - the lines say "no sensor" when one is not configured):
 * COLOUR CLASSIFICATION: hold a POLLEN, a red NECTAR and a blue NECTAR in front of the mouth sensor
 * (where balls really pass) and note the HUE / SATURATION / DISTANCE. Then set the hue windows,
 * MIN_SATURATION and MOUTH_BALL_PRESENT_CM in RobotConfig section 9 so each ball shows the right class
 * with a margin. Do it again under the brightest and darkest light you expect.
 * BALL TIME AT THE MOUTH (G407 last-ball rule): after START press RB (resets "longest"), then hold A and
 * let the intake pull 10 single POLLEN in, one at a time, none touching another (drive into them the way
 * the drivers do). Set MOUTH_ONE_BALL_MAX_MS to "longest" + 30% (the line shows the suggestion).
 *
 * PINPOINT CHECK: the pose is zeroed to (0, 0, heading 0) at INIT (gamepad1 back zeroes it again), so
 * "push the robot FORWARD: x grows; push it LEFT: y grows" is valid. (The Pinpoint otherwise keeps the
 * pose of the last OpMode - after AUTO the robot may face any way, and then x does not grow forward.)
 */
@TeleOp(name = "Check: Sensors", group = "12303 Tuning")
public class SensorCheck extends LinearOpMode {

    /** Gentle carrier power for the direction test (directions are set on the device by the Carrier). */
    private static final double CARRIER_TEST_POWER = 0.4;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        Robot robot = new Robot(hardwareMap, Alliance.RED);
        GoBildaPinpointDriver pinpoint = OptionalHardware.getQuiet(hardwareMap, GoBildaPinpointDriver.class, RobotConfig.PINPOINT);
        DcMotorEx fl = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.FRONT_LEFT);
        DcMotorEx fr = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.FRONT_RIGHT);
        DcMotorEx bl = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.BACK_LEFT);
        DcMotorEx br = OptionalHardware.getQuiet(hardwareMap, DcMotorEx.class, RobotConfig.BACK_RIGHT);
        DcMotorSimple intake = OptionalHardware.getQuiet(hardwareMap, DcMotorSimple.class, RobotConfig.INTAKE);     // motor or CR servo
        DcMotorSimple carrier = OptionalHardware.getQuiet(hardwareMap, DcMotorSimple.class, RobotConfig.CARRIER);   // motor or CR servo
        boolean shooterArmed = false;
        robot.drive.setPose(new Pose(0, 0, 0));   // direction check needs heading 0 (moves nothing)

        while (opModeInInit()) {
            if (gamepad1.backWasPressed()) robot.drive.setPose(new Pose(0, 0, 0));
            if (gamepad1.rightBumperWasPressed()) robot.carrier.resetLongestMouthPresence();
            robot.update();               // nothing is armed: reads only
            show(robot, pinpoint, false);
        }
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            if (gamepad1.backWasPressed()) robot.drive.setPose(new Pose(0, 0, 0));
            if (gamepad1.rightBumperWasPressed()) robot.carrier.resetLongestMouthPresence();
            // Direction tests: raw outputs, one device at a time, only while the button is held. The
            // subsystems are not armed, so these raw commands are the only ones sent.
            power(fl, gamepad1.dpad_up ? 0.3 : 0.0);
            power(fr, gamepad1.dpad_right ? 0.3 : 0.0);
            power(bl, gamepad1.dpad_left ? 0.3 : 0.0);
            power(br, gamepad1.dpad_down ? 0.3 : 0.0);
            if (intake != null) intake.setPower(gamepad1.a ? RobotConfig.INTAKE_IN_POWER : 0.0);
            if (carrier != null) {
                carrier.setPower(gamepad1.b ? CARRIER_TEST_POWER : (gamepad1.x ? -CARRIER_TEST_POWER : 0.0));
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
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        telemetry.addLine(started ? "HOLD: dpad = one wheel (must roll FORWARD), A intake IN, B carrier toward shooter, X carrier back, Y flywheel | RB = reset 'longest'"
                : "INIT: read-only. Press START for the direction tests.");
        telemetry.addLine(String.format(Locale.US, "MOUTH: %s | hue %.0f sat %.2f | dist %.1f cm (ball if < %.1f) %s",
                robot.carrier.mouthBall(), robot.carrier.mouthHue(), robot.carrier.mouthSaturation(),
                robot.carrier.mouthDistanceCm(), RobotConfig.MOUTH_BALL_PRESENT_CM,
                robot.carrier.hasMouthSensor() ? "" : "(NO SENSOR)"));
        long last = robot.carrier.lastMouthPresenceMs();
        long longest = robot.carrier.longestMouthPresenceMs();
        telemetry.addLine(String.format(Locale.US, "  ball in view: last %s, longest %s -> MOUTH_ONE_BALL_MAX_MS ~ %s (now %d)",
                last < 0 ? "-" : last + " ms", longest < 0 ? "-" : longest + " ms",
                longest < 0 ? "-" : String.valueOf(Math.round(longest * 1.3)), RobotConfig.MOUTH_ONE_BALL_MAX_MS));
        telemetry.addLine(String.format(Locale.US, "  windows: POLLEN %.0f-%.0f, RED <%.0f or >%.0f, BLUE %.0f-%.0f, min sat %.2f",
                RobotConfig.POLLEN_HUE_MIN, RobotConfig.POLLEN_HUE_MAX, RobotConfig.RED_HUE_MAX, RobotConfig.RED_HUE_MIN_WRAP,
                RobotConfig.BLUE_HUE_MIN, RobotConfig.BLUE_HUE_MAX, RobotConfig.MIN_SATURATION));
        telemetry.addLine(String.format(Locale.US, "SLOT 4: %s | dist %.1f cm (ball if < %.1f) %s",
                robot.carrier.slot4Occupied() ? "BALL" : "empty", robot.carrier.slot4DistanceCm(),
                RobotConfig.SLOT4_BALL_PRESENT_CM, robot.carrier.hasSlot4Sensor() ? "" : "(NO SENSOR)"));
        telemetry.addLine(String.format(Locale.US, "FEED BEAM: %s", robot.carrier.hasFeedBeam()
                ? (robot.carrier.beamBroken() ? "BROKEN (ball)" : "clear") : "not installed"));
        telemetry.addLine(String.format(Locale.US, "BALL COUNT %d (counts at the mouth sensor while the intake runs IN in the TeleOp)",
                robot.carrier.count()));
        if (pinpoint != null) {
            Pose p = robot.drive.pose();
            telemetry.addLine(String.format(Locale.US, "PINPOINT %s | x %.1f y %.1f h %.1f deg | %.0f Hz",
                    pinpoint.getDeviceStatus(), p.x(), p.y(), Math.toDegrees(p.heading()), pinpoint.getFrequency()));
            telemetry.addLine("  pose zeroed at INIT (back = zero again): push the robot FORWARD: x must grow; push LEFT: y must grow");
        } else {
            telemetry.addLine("PINPOINT: not found (name '" + RobotConfig.PINPOINT + "', I2C bus 1-3, not 0)");
        }
        telemetry.addLine(String.format(Locale.US, "FLYWHEEL %.0f rpm (%.0f ticks/s) | intake %s | carrier %s",
                robot.shooter.measuredRpm(), robot.shooter.measuredTicksPerSec(),
                robot.intake.isPresent() ? "found" : "NOT FOUND", robot.carrier.isPresent() ? "found" : "NOT FOUND"));
        telemetry.addLine(String.format(Locale.US, "BATTERY %.2f V | loop %.0f ms", robot.batteryVolts(), robot.loopMs()));
        robot.addWarnings(telemetry);
        telemetry.update();
    }
}
