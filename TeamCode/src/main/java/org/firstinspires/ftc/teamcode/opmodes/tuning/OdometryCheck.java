package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.OdometryPods;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ODOMETRY CHECK - which pods are on the robot, what they are plugged into, and which way they count.
 * Nothing moves: you push the robot by hand.
 *
 * 1. Run it. The first line says whether a goBILDA Pinpoint named "pinpoint" is configured.
 * 2. Put the robot against a straight edge (a wall or a long board) with a tape measure beside it.
 *    Set the push distance with dpad up/down (default 48 in; two tile seams on an official field are 47 in).
 * 3. Press A (zeroes the readings), push the robot STRAIGHT FORWARD exactly that far without turning,
 *    and read the X pod line: pod type, counts per inch and direction.
 * 4. Press A again, push the robot STRAIGHT LEFT the same distance, and read the Y pod line.
 * 5. Copy the "SET in RobotConfig section 3" lines into RobotConfig, rebuild, and re-run Check: Sensors
 *    (x must grow pushing forward, y must grow pushing left).
 *
 * If the Pinpoint's counts do not change when you push, the pods are not plugged into it. The list at the
 * bottom shows every configured motor port's encoder: a pod plugged into a REV hub ENCODER port shows up
 * under that port's motor name (the drive motors also change, because the wheels roll when pushed).
 * The code expects a Pinpoint; hub-port pods need a different localizer setup (docs/plan/06-software.md).
 */
@TeleOp(name = "Check: Odometry", group = "12303 Tuning")
public class OdometryCheck extends LinearOpMode {

    private double pushInches = 48.0;
    private int startX = 0, startY = 0;
    private boolean measuring = false;
    private final List<DcMotorEx> motors = new ArrayList<>();
    private final List<String> motorNames = new ArrayList<>();
    private final List<Integer> motorStart = new ArrayList<>();

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        GoBildaPinpointDriver pinpoint = OptionalHardware.getQuiet(hardwareMap, GoBildaPinpointDriver.class, RobotConfig.PINPOINT);
        if (pinpoint != null) {
            // Native counting direction, so the advice below maps straight onto FORWARD / REVERSED.
            pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                    GoBildaPinpointDriver.EncoderDirection.FORWARD);
        }
        for (DcMotorEx m : hardwareMap.getAll(DcMotorEx.class)) {
            motors.add(m);
            motorNames.add(hardwareMap.getNamesOf(m).isEmpty() ? "?" : hardwareMap.getNamesOf(m).iterator().next());
            motorStart.add(m.getCurrentPosition());
        }

        // INIT and START behave the same: this OpMode only reads.
        while (opModeInInit() || opModeIsActive()) {
            if (pinpoint != null) pinpoint.update();
            if (gamepad1.dpadUpWasPressed()) pushInches += 1.0;
            if (gamepad1.dpadDownWasPressed()) pushInches = Math.max(OdometryPods.MIN_PUSH_IN, pushInches - 1.0);
            if (gamepad1.aWasPressed()) {
                if (pinpoint != null) {
                    startX = pinpoint.getEncoderX();
                    startY = pinpoint.getEncoderY();
                }
                for (int i = 0; i < motors.size(); i++) motorStart.set(i, motors.get(i).getCurrentPosition());
                measuring = true;
            }
            show(pinpoint);
        }
    }

    private void show(GoBildaPinpointDriver pinpoint) {
        telemetry.addLine("=== Check: Odometry === nothing moves: push the robot by hand");
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        telemetry.addLine(String.format(Locale.US, "Push distance %.0f in (dpad up/down). A = zero, then push STRAIGHT:",
                pushInches));
        telemetry.addLine("  FORWARD for the X pod line, then A again and LEFT for the Y pod line.");
        if (pinpoint == null) {
            telemetry.addLine("PINPOINT: NOT FOUND (name '" + RobotConfig.PINPOINT + "', an I2C port on bus 1-3, not 0).");
            telemetry.addLine("  Follow the pod cables: a small goBILDA board = Pinpoint (add it to the Robot");
            telemetry.addLine("  Configuration); the hub's ENCODER ports = hub wiring (see the list below).");
        } else {
            telemetry.addLine(String.format(Locale.US, "PINPOINT: found, %s, %.0f Hz | raw X %d, raw Y %d",
                    pinpoint.getDeviceStatus(), pinpoint.getFrequency(), pinpoint.getEncoderX(), pinpoint.getEncoderY()));
            if (measuring) {
                int dx = pinpoint.getEncoderX() - startX;
                int dy = pinpoint.getEncoderY() - startY;
                podLine("X pod (push FORWARD)", dx);
                podLine("Y pod (push LEFT)", dy);
                if (Math.abs(dx) < 200 && Math.abs(dy) < 200) {
                    telemetry.addLine("  Counts barely move? Then the pods are not plugged into the Pinpoint: check the list below.");
                }
            } else {
                telemetry.addLine("  Press A to start a measurement.");
            }
        }
        telemetry.addLine("Motor-port encoders (change since A):");
        for (int i = 0; i < motors.size(); i++) {
            telemetry.addLine(String.format(Locale.US, "  %s: %d", motorNames.get(i),
                    motors.get(i).getCurrentPosition() - motorStart.get(i)));
        }
        telemetry.update();
    }

    private void podLine(String name, int delta) {
        double perInch = OdometryPods.countsPerInch(delta, pushInches);
        if (Math.abs(delta) < 1000) {
            telemetry.addLine(String.format(Locale.US, "%s: %d counts so far", name, delta));
            return;
        }
        OdometryPods.Kind kind = OdometryPods.identify(perInch);
        telemetry.addLine(String.format(Locale.US, "%s: %d counts = %.1f per inch -> %s", name, delta, perInch,
                kind == null ? "not a pod we know" : kind.label + String.format(Locale.US, " (expected %.1f)", kind.countsPerInch)));
        telemetry.addLine("  SET in RobotConfig section 3: " + OdometryPods.configAdvice(perInch));
        telemetry.addLine("  SET direction: " + (name.startsWith("X") ? "PINPOINT_X_POD_DIRECTION" : "PINPOINT_Y_POD_DIRECTION")
                + " = " + OdometryPods.directionAdvice(delta));
    }
}
