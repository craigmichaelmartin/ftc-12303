package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TURRET CALIBRATOR - turns servo positions into turret angles and finds the soft limits.
 *
 * Before you start: put two tape marks on the robot/floor: one where the shooter should point when
 * "centred" (usually straight along the robot front or back), and one MARK_DEG (30) degrees to the
 * LEFT (counter-clockwise seen from above) of it. A phone protractor app is fine.
 *
 *   dpad left/right   move the servo -0.01 / +0.01 (coarse)
 *   dpad down/up      move the servo -0.002 / +0.002 (fine)
 *   A                 shooter points at the CENTRE mark   -> TURRET_SERVO_CENTER
 *   X                 shooter points at the 30-deg-LEFT mark -> gear ratio + direction (+ encoder scale)
 *   Y                 at the LEFT hard stop: records it    -> suggests TURRET_MAX_DEG (minus 4 deg margin)
 *   B                 at the RIGHT hard stop: records it   -> suggests TURRET_MIN_DEG (plus 4 deg margin)
 *   RB                slew test: jumps +-20 deg around centre (time it with slow-mo video, or the
 *                     encoder if installed) -> TURRET_SLEW_DEG_PER_SEC
 * Copy the printed lines into RobotConfig section 7. Move SLOWLY near the hard stops: the servo can
 * strip the pinion if you drive it into a stop.
 */
@TeleOp(name = "Tune: Turret Calibrator", group = "12303 Tuning")
public class TurretCalibrator extends LinearOpMode {

    private static final double MARK_DEG = 30.0;
    private static final double LIMIT_MARGIN_DEG = 4.0;
    private static final double SLEW_TEST_DEG = 20.0;

    private double position;
    private double centerPos = Double.NaN, markPos = Double.NaN, leftStopPos = Double.NaN, rightStopPos = Double.NaN;
    private int centerTicks = 0, markTicks = 0;
    private String slewResult = "";

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(50);
        List<String> warnings = new ArrayList<>();
        Turret turret = new Turret(hardwareMap, warnings);
        position = RobotConfig.TURRET_SERVO_CENTER;
        telemetry.addLine("Turret calibrator: press START, then follow the instructions.");
        for (String w : warnings) telemetry.addLine("WARN: " + w);
        telemetry.update();
        waitForStart();
        if (isStopRequested()) return;

        turret.arm();                         // sets the 500-2500 us PWM range
        ServoImplEx servo = turret.servo();
        if (servo == null) {
            telemetry.addLine("No turret servo named '" + RobotConfig.TURRET_SERVO + "' - check the configuration.");
            telemetry.update();
            while (opModeIsActive()) idle();
            return;
        }
        servo.setPosition(position);

        while (opModeIsActive()) {
            boolean left = gamepad1.dpadLeftWasPressed();
            boolean right = gamepad1.dpadRightWasPressed();
            boolean down = gamepad1.dpadDownWasPressed();
            boolean up = gamepad1.dpadUpWasPressed();
            boolean a = gamepad1.aWasPressed();
            boolean x = gamepad1.xWasPressed();
            boolean y = gamepad1.yWasPressed();
            boolean b = gamepad1.bWasPressed();
            boolean rb = gamepad1.rightBumperWasPressed();

            if (left) position -= 0.01;
            if (right) position += 0.01;
            if (down) position -= 0.002;
            if (up) position += 0.002;
            position = Math.max(0.0, Math.min(1.0, position));
            servo.setPosition(position);       // raw servo control here: we do NOT call turret.update()
            int ticks = turret.encoderTicks();

            if (a) {
                centerPos = position;
                centerTicks = ticks;
            }
            if (x) {
                markPos = position;
                markTicks = ticks;
            }
            if (y) leftStopPos = position;
            if (b) rightStopPos = position;
            if (rb && !Double.isNaN(centerPos) && !Double.isNaN(markPos)) slewTest(servo, turret);

            show(ticks);
        }
    }

    /** Turret degrees per servo unit from the two marks (+ = servo increase turns CCW). */
    private double degPerUnit() {
        return MARK_DEG / (markPos - centerPos);
    }

    private double offsetAt(double pos) {
        return (pos - centerPos) * degPerUnit();
    }

    private void slewTest(ServoImplEx servo, Turret turret) {
        double plus = centerPos + SLEW_TEST_DEG / degPerUnit();
        double minus = centerPos - SLEW_TEST_DEG / degPerUnit();
        servo.setPosition(minus);
        sleep(1500);
        ElapsedTime t = new ElapsedTime();
        servo.setPosition(plus);
        double settledAt = -1;
        int lastTicks = turret.encoderTicks();
        double stillSince = -1;
        while (opModeIsActive() && t.seconds() < 2.0) {
            int ticks = turret.encoderTicks();
            if (ticks != lastTicks) stillSince = -1;
            else if (stillSince < 0) stillSince = t.seconds();
            if (turret.hasEncoder() && stillSince > 0 && t.seconds() - stillSince > 0.1 && settledAt < 0 && t.seconds() > 0.1) {
                settledAt = stillSince;
            }
            lastTicks = ticks;
        }
        if (turret.hasEncoder() && settledAt > 0) {
            slewResult = String.format(Locale.US, "slew: %.0f deg in %.2f s -> TURRET_SLEW_DEG_PER_SEC = %.0f;",
                    2 * SLEW_TEST_DEG, settledAt, 2 * SLEW_TEST_DEG / settledAt);
        } else {
            slewResult = "slew: no encoder - film the RB jump in slow motion: 40 deg / time = deg per s";
        }
        servo.setPosition(position);
    }

    private void show(int ticks) {
        telemetry.addLine("dpad L/R coarse, D/U fine | A centre, X 30-deg-LEFT mark, Y left stop, B right stop, RB slew test");
        telemetry.addLine(String.format(Locale.US, "servo position %.3f | encoder ticks %d %s",
                position, ticks, RobotConfig.TURRET_USE_ENCODER ? "" : "(encoder off in RobotConfig)"));
        telemetry.addLine(String.format(Locale.US, "with CURRENT RobotConfig this is %+.1f deg from centre",
                Turret.offsetForServoPosition(position)));
        telemetry.addLine("---- paste into RobotConfig section 7 ----");
        if (!Double.isNaN(centerPos)) {
            telemetry.addLine(String.format(Locale.US, "TURRET_SERVO_CENTER = %.3f;", centerPos));
        } else {
            telemetry.addLine("(press A when the shooter points at the centre mark)");
        }
        if (!Double.isNaN(centerPos) && !Double.isNaN(markPos) && Math.abs(markPos - centerPos) > 0.005) {
            double dpu = degPerUnit();
            double ratio = RobotConfig.TURRET_SERVO_RANGE_DEG / Math.abs(dpu);
            telemetry.addLine(String.format(Locale.US, "TURRET_SERVO_DIRECTION = %.0f;   TURRET_GEAR_RATIO = %.3f;   (%.1f turret deg per servo unit)",
                    Math.signum(dpu), ratio, Math.abs(dpu)));
            if (RobotConfig.TURRET_USE_ENCODER && markTicks != centerTicks) {
                double tpd = (markTicks - centerTicks) / MARK_DEG;
                telemetry.addLine(String.format(Locale.US, "TURRET_ENCODER_TICKS_PER_DEG = %.3f;   TURRET_ENCODER_DIRECTION = %.0f;",
                        Math.abs(tpd), Math.signum(tpd)));
            }
            telemetry.addLine(String.format(Locale.US, "now: %+.1f deg from centre (new calibration)", offsetAt(position)));
            if (!Double.isNaN(leftStopPos)) {
                telemetry.addLine(String.format(Locale.US, "TURRET_MAX_DEG = %.1f;   (left stop at %+.1f)",
                        offsetAt(leftStopPos) - Math.signum(offsetAt(leftStopPos)) * LIMIT_MARGIN_DEG, offsetAt(leftStopPos)));
            }
            if (!Double.isNaN(rightStopPos)) {
                telemetry.addLine(String.format(Locale.US, "TURRET_MIN_DEG = %.1f;   (right stop at %+.1f)",
                        offsetAt(rightStopPos) - Math.signum(offsetAt(rightStopPos)) * LIMIT_MARGIN_DEG, offsetAt(rightStopPos)));
            }
        } else {
            telemetry.addLine("(then press X when it points at the mark 30 deg to the LEFT)");
        }
        if (!slewResult.isEmpty()) telemetry.addLine(slewResult);
        telemetry.update();
    }
}
