package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Feedback;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.MatchTimer;
import org.firstinspires.ftc.teamcode.util.PoseStorage;

import java.util.Locale;

/**
 * 12303 match TeleOp.
 *
 * DRIVER (gamepad1)                          OPERATOR (gamepad2)
 *   left stick   drive (field-centric)         A / cross      SHOOTING MODE on/off (flywheel + auto-aim)
 *   right stick  turn                          RB             FIRE VOLLEY (all balls, each when ready)
 *   LB (hold)    slow mode                     X / square     fire ONE ball
 *   RB (hold)    aim-assist: turn the chassis  B / circle     stop firing
 *                so the turret is centred      Y / triangle   HIVE TIPPED (flip target cell A <-> B)
 *   RT (hold)    intake in (stops at 4)        dpad up/down   flywheel RPM trim +-50
 *   LT (hold)    intake out                    dpad lft/rgt   turret trim (left = CCW)
 *   back/share   heading reset: robot must     right stick X  manual turret (only when auto-aim is OFF)
 *                face straight AWAY from you   LB (hold)      EJECT: intake + spinners backward
 *   LB + Y       "robot is at S1" relocalize   back/share     ball count -1
 *                                              L-stick click  ball count +1
 * INIT (no AUTO data): gamepad1 X = BLUE, B = RED; gamepad2 dpad up/down = ball count.
 *
 * G403: INIT builds everything but moves NOTHING (motors, servos). Actuators arm only after START.
 * If INIT ever has to move something, press INIT only after TELEOP has begun (manual G403 box).
 */
@TeleOp(name = "12303 TeleOp", group = "12303")
public class MainTeleOp extends LinearOpMode {

    /** AUTO data older than this is ignored (an AUTO + TeleOp match is about 2.6 minutes). */
    private static final long AUTO_DATA_MAX_AGE_MS = 5 * 60 * 1000;
    /** Watch the spill landing box this long after a TIP (R2 verify: allow up to ~6 s). */
    private static final long SPILL_WATCH_MS = 6000;

    private Robot robot;
    private Alliance alliance;
    private long lastLoopNs = 0;
    private long tipAtMs = -100000;
    private HiveState stateBeforeTip = HiveState.A;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(RobotConfig.TELEMETRY_INTERVAL_MS);

        // ------------------------------------------------------------------ INIT (nothing moves)
        boolean fromAuto = PoseStorage.isFresh(AUTO_DATA_MAX_AGE_MS);
        alliance = fromAuto ? PoseStorage.alliance() : Alliance.RED;
        robot = new Robot(hardwareMap, alliance);
        Feedback feedback = new Feedback(gamepad1, gamepad2);
        MatchTimer timer = new MatchTimer(RobotConfig.TELEOP_LENGTH_SEC);

        if (fromAuto) {
            robot.drive.setPose(PoseStorage.pose());
            robot.indexer.setCount(PoseStorage.ballCount());
            robot.hive.restore(PoseStorage.hiveState());
        } else {
            setPracticeStart();
        }

        while (opModeInInit()) {
            if (!fromAuto) handleInitSelection();
            robot.update();   // safe: nothing is armed, so this only reads sensors
            showInitTelemetry(fromAuto);
        }
        if (isStopRequested()) {
            robot.vision.stop();
            return;
        }

        // ------------------------------------------------------------------ START
        robot.arm();
        timer.start();
        gamepad1.resetEdgeDetection();
        gamepad2.resetEdgeDetection();

        while (opModeIsActive()) {
            double dt = loopSeconds();
            driverControls();
            operatorControls(dt);
            robot.update();

            if (robot.tipEventThisLoop()) {
                tipAtMs = System.currentTimeMillis();
                stateBeforeTip = robot.hive.state().other();
                feedback.onTip();
            }
            if (robot.fullEventThisLoop()) feedback.onFull();
            if (robot.rejectEventThisLoop()) feedback.onReject();
            feedback.updateTimeCues(timer);
            feedback.updateShooter(robot.spinUp(), robot.shooter.atSpeed());
            feedback.updateLights(robot.indexer.isFull(), robot.indexer.count(), robot.spinUp(),
                    robot.readyToFire(), robot.aim().reachable);

            showMatchTelemetry(timer);
        }
        robot.vision.stop();
    }

    // ---------------------------------------------------------------------------------------------
    // Driver (gamepad1)
    // ---------------------------------------------------------------------------------------------
    private void driverControls() {
        // Read every edge exactly ONCE per loop (wasPressed() clears itself when read).
        boolean backPressed = gamepad1.backWasPressed();
        boolean yPressed = gamepad1.yWasPressed();

        double up = MathUtil.deadband(-gamepad1.left_stick_y, RobotConfig.DRIVE_STICK_DEADBAND);
        double right = MathUtil.deadband(gamepad1.left_stick_x, RobotConfig.DRIVE_STICK_DEADBAND);
        double turnLeft = MathUtil.deadband(-gamepad1.right_stick_x, RobotConfig.DRIVE_STICK_DEADBAND)
                * RobotConfig.DRIVE_TURN_SCALE;
        if (gamepad1.left_bumper) {
            up *= RobotConfig.DRIVE_SLOW_SCALE;
            right *= RobotConfig.DRIVE_SLOW_SCALE;
            turnLeft *= RobotConfig.DRIVE_SLOW_SCALE;
        }
        if (gamepad1.right_bumper && robot.drive.hasLocalizer()) {
            // Aim-assist: turret travel is small (~+-30 deg), so let the chassis do the coarse aiming.
            turnLeft = MathUtil.clamp(RobotConfig.AIM_ASSIST_TURN_KP * robot.aim().chassisTurnDeg,
                    -RobotConfig.AIM_ASSIST_MAX_TURN, RobotConfig.AIM_ASSIST_MAX_TURN);
        }
        robot.drive.driveFieldCentric(up, right, turnLeft, alliance.driverForwardHeadingDeg());

        boolean operatorEject = gamepad2.left_bumper;
        robot.intake.request(gamepad1.right_trigger > 0.3, gamepad1.left_trigger > 0.3 || operatorEject);

        if (backPressed) {
            // Robot must be facing straight away from the driver when this is pressed.
            robot.drive.setHeadingDeg(alliance.driverForwardHeadingDeg());
        }
        if (yPressed && gamepad1.left_bumper && robot.drive.isStill()) {
            robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Operator (gamepad2)
    // ---------------------------------------------------------------------------------------------
    private void operatorControls(double dt) {
        boolean aPressed = gamepad2.aWasPressed();
        boolean rbPressed = gamepad2.rightBumperWasPressed();
        boolean xPressed = gamepad2.xWasPressed();
        boolean bPressed = gamepad2.bWasPressed();
        boolean yPressed = gamepad2.yWasPressed();
        boolean upPressed = gamepad2.dpadUpWasPressed();
        boolean downPressed = gamepad2.dpadDownWasPressed();
        boolean leftPressed = gamepad2.dpadLeftWasPressed();
        boolean rightPressed = gamepad2.dpadRightWasPressed();
        boolean backPressed = gamepad2.backWasPressed();
        boolean stickPressed = gamepad2.leftStickButtonWasPressed();

        if (aPressed) robot.setShootingMode(!robot.spinUp());
        if (rbPressed) {
            robot.setShootingMode(true);          // firing needs the flywheel and the aim anyway
            robot.indexer.requestVolley();
        }
        if (xPressed) {
            robot.setShootingMode(true);
            robot.indexer.requestSingleShot();
        }
        if (bPressed) robot.indexer.cancelFiring();
        if (yPressed) robot.hive.operatorToggle();

        if (upPressed) robot.shooter.adjustTrim(RobotConfig.FLYWHEEL_TRIM_STEP_RPM);
        if (downPressed) robot.shooter.adjustTrim(-RobotConfig.FLYWHEEL_TRIM_STEP_RPM);
        if (leftPressed) robot.turret.addTrim(RobotConfig.TURRET_TRIM_STEP_DEG);
        if (rightPressed) robot.turret.addTrim(-RobotConfig.TURRET_TRIM_STEP_DEG);

        if (!robot.autoAim()) {
            double stick = MathUtil.deadband(-gamepad2.right_stick_x, 0.1);
            if (stick != 0) robot.turret.nudgeManual(stick * RobotConfig.TURRET_MANUAL_DEG_PER_SEC * dt);
        }

        robot.indexer.setEjecting(gamepad2.left_bumper);
        if (backPressed) robot.indexer.adjustCount(-1);
        if (stickPressed) robot.indexer.adjustCount(+1);
    }

    // ---------------------------------------------------------------------------------------------
    // INIT helpers
    // ---------------------------------------------------------------------------------------------
    /**
     * No AUTO data: assume the robot was placed on S1 of the selected alliance. The ball count starts
     * at 4 because a match robot always starts with 4 preloads (G304.G). If AUTO did not run, a count
     * of 0 would let the intake take 4 more = 8 controlled (G407). In practice, lower it with dpad down.
     */
    private void setPracticeStart() {
        robot.drive.setPose(FieldConstants.startPose(alliance, FieldConstants.StartSpot.S1));
        robot.indexer.setCount(RobotConfig.MAX_BALLS);
        robot.hive.restore(HiveState.A);
    }

    private void handleInitSelection() {
        boolean xPressed = gamepad1.xWasPressed();
        boolean bPressed = gamepad1.bWasPressed();
        boolean upPressed = gamepad2.dpadUpWasPressed();
        boolean downPressed = gamepad2.dpadDownWasPressed();
        if (xPressed || bPressed) {
            Alliance chosen = xPressed ? Alliance.BLUE : Alliance.RED;
            if (chosen != alliance) {
                alliance = chosen;
                robot.setAlliance(alliance);
                int balls = robot.indexer.count();
                setPracticeStart();
                robot.indexer.setCount(balls);
            }
        }
        if (upPressed) robot.indexer.adjustCount(+1);
        if (downPressed) robot.indexer.adjustCount(-1);
    }

    private void showInitTelemetry(boolean fromAuto) {
        telemetry.addLine("=== 12303 TeleOp - INIT (nothing moves until START, G403) ===");
        String warning = CompetitionCheck.warning();
        if (warning != null) telemetry.addLine(warning);
        telemetry.addData("Alliance", "%s %s", alliance, fromAuto ? "(from AUTO)" : "(gamepad1 X = BLUE, B = RED)");
        if (fromAuto) {
            telemetry.addLine(String.format(Locale.US, "Pose / balls / HIVE taken from AUTO (%.0f s ago)", PoseStorage.ageSeconds()));
        } else {
            telemetry.addLine("No AUTO data: PRACTICE start - place the robot on S1 ("
                    + FieldConstants.StartSpot.S1.description + ")");
            telemetry.addLine("Ball count starts at 4 (match preloads). Practice with fewer: gamepad2 dpad down");
        }
        robot.addTelemetry(telemetry);
        robot.addWarnings(telemetry);
        telemetry.update();
    }

    private void showMatchTelemetry(MatchTimer timer) {
        Pose p = robot.drive.pose();
        telemetry.addLine(String.format(Locale.US, "TIME %s | %s | BALLS %d/4 | %s",
                timer.remainingText(), alliance, robot.indexer.count(),
                robot.spinUp() ? (robot.readyToFire() ? "READY TO FIRE" : "spinning up / aiming") : "shooting mode OFF"));
        if (System.currentTimeMillis() - tipAtMs < SPILL_WATCH_MS
                && FieldConstants.inSpillBox(alliance, stateBeforeTip, p.x(), p.y(), FieldConstants.ROBOT_HALF_WIDTH)) {
            telemetry.addLine("!! IN THE SPILL LANDING BOX - back off until the balls land (G409)");
        }
        if (robot.intake.refusedBecauseFull()) telemetry.addLine("Intake refused: FULL (4 balls, G407)");
        robot.addTelemetry(telemetry);
        telemetry.update();
    }

    private double loopSeconds() {
        long now = System.nanoTime();
        double dt = (lastLoopNs == 0) ? 0.0 : (now - lastLoopNs) / 1e9;
        lastLoopNs = now;
        return dt;
    }
}
