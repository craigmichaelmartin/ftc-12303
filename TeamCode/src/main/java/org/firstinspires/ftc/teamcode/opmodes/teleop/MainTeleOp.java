package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Feedback;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.AllianceMemory;
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
 *   left stick   drive (field-centric)         A / cross      SHOOTING MODE on/off (flywheel + auto-aim);
 *   right stick  turn                                         turning it OFF also cancels a requested volley
 *   LB (hold)    slow mode                     RB             FIRE VOLLEY (all balls, each when ready)
 *   RB (hold)    aim-assist: turn the chassis  X / square     fire ONE ball
 *                so the turret is centred      B / circle     stop firing
 *   RT (hold)    intake in (stops at 4)        Y / triangle   HIVE TIPPED - press on every "TIP!": flips the
 *   LT (hold)    intake out                                   target cell A <-> B; within 3 s of a camera
 *   back/share   heading reset: robot must                    switch it CONFIRMS that switch instead
 *                face straight AWAY from you   dpad up/down   flywheel RPM trim +-50
 *   LB + Y       "robot is at OUR S1"          dpad lft/rgt   turret trim (left = CCW)
 *                relocalize (red: back on the  right stick X  manual turret (whenever auto-aim is off)
 *                audience wall; blue: back on  R-stick click  MANUAL AIM on/off: RB/X then fire at the
 *                the far wall)                                manual angle once the flywheel is at speed
 *                                              LB (hold)      EJECT: intake + spinners backward
 *                                              back/share     ball count -1
 *                                              L-stick click  ball count +1
 * INIT: gamepad1 X = BLUE, B = RED (with AUTO data this rotates the hand-over pose to the other side);
 *       gamepad2 dpad up/down = ball count.
 *
 * G403: INIT builds everything but moves NOTHING (motors, servos). Actuators arm only after START.
 * If INIT ever has to move something, press INIT only after TELEOP has begun (manual G403 box).
 */
@TeleOp(name = "12303 TeleOp", group = "12303")
public class MainTeleOp extends LinearOpMode {

    /** AUTO data older than this is ignored (an AUTO + TeleOp match is about 2.6 minutes). */
    private static final long AUTO_DATA_MAX_AGE_MS = 5 * 60 * 1000;
    /** Watch the spill landing box this long after a TIP or our last shot (R2 verify: up to ~6 s). */
    private static final long SPILL_WATCH_MS = 6000;
    /** Show the "target switched" banner this long. */
    private static final long BANNER_MS = 5000;

    private Robot robot;
    private Alliance alliance;
    private boolean fromAuto;
    private boolean allianceFromMemory = false;
    private boolean manualAim = false;
    private long lastLoopNs = 0;
    private long tipAtMs = -100000;
    private long lastShotMs = -100000;
    private long volleyCancelledAtMs = -100000;
    private long unconfirmedFeedAtMs = -100000;
    private boolean wasFiring = false;
    private HiveState stateBeforeTip = HiveState.A;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(RobotConfig.TELEMETRY_INTERVAL_MS);

        // ------------------------------------------------------------------ INIT (nothing moves)
        fromAuto = PoseStorage.isFresh(AUTO_DATA_MAX_AGE_MS);
        if (fromAuto) {
            alliance = PoseStorage.alliance();
        } else {
            Alliance remembered = AllianceMemory.load();   // written by the last AUTO INIT
            allianceFromMemory = remembered != null;
            alliance = remembered != null ? remembered : Alliance.RED;
        }
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
            handleInitSelection();
            robot.update();   // safe: nothing is armed, so this only reads sensors
            showInitTelemetry();
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
            long now = System.currentTimeMillis();

            if (robot.tipEventThisLoop()) {
                tipAtMs = now;
                stateBeforeTip = robot.hive.state().other();
                if (wasFiring) volleyCancelledAtMs = now;   // the target switch cancelled a requested volley
                if (robot.hive.lastSource() == HiveTracker.Source.VISION) feedback.onVisionSwitch();
                else feedback.onTip();
            }
            if (robot.confirmEventThisLoop()) feedback.onConfirm();
            if (robot.shotEventThisLoop()) lastShotMs = now;
            if (robot.fullEventThisLoop()) feedback.onFull();
            if (robot.rejectEventThisLoop()) feedback.onReject();
            if (robot.unconfirmedFeedThisLoop()) {
                unconfirmedFeedAtMs = now;
                feedback.onUnconfirmedFeed();
            }
            wasFiring = robot.indexer.isFiring();
            feedback.updateTimeCues(timer);
            feedback.updateShooter(robot.spinUp(), robot.shooter.atSpeed());
            feedback.updateLights(robot.indexer.isFull(), robot.indexer.count(), robot.spinUp(),
                    robot.readyToFire(), robot.aim().reachable || !robot.autoAim(),
                    robot.autoAim() && robot.targetFacesAway());

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
            // Aim-assist: turret travel is small (~+-28 deg; none when pinned), so the chassis does the
            // coarse aiming.
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
            // Only when the robot is parked exactly on OUR S1 (red: back on the audience wall at
            // (61.67, -12.75); blue: back on the far wall at (-61.67, 12.75)).
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
        boolean rightStickPressed = gamepad2.rightStickButtonWasPressed();

        if (rightStickPressed) {
            manualAim = !manualAim;
            double keep = robot.turret.commandOffsetDeg();
            robot.setAutoAim(robot.spinUp() && !manualAim);
            if (manualAim) robot.turret.setManualOffset(keep);   // start manual aim from the current angle
        }
        if (aPressed) {
            boolean on = !robot.spinUp();
            robot.setSpinUp(on);
            robot.setAutoAim(on && !manualAim);
            if (!on) robot.indexer.cancelFiring();   // shooting mode OFF also cancels a requested volley
        }
        if (rbPressed) {
            robot.setSpinUp(true);                   // firing needs the flywheel (and the aim) anyway
            robot.setAutoAim(!manualAim);
            robot.indexer.requestVolley();
        }
        if (xPressed) {
            robot.setSpinUp(true);
            robot.setAutoAim(!manualAim);
            robot.indexer.requestSingleShot();
        }
        if (bPressed) robot.indexer.cancelFiring();
        if (yPressed) robot.hive.operatorTipped();

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
     * No AUTO data: assume the robot was placed on OUR S1. The ball count starts at 4 because a match
     * robot always starts with 4 preloads (G304.G). If AUTO did not run, a count of 0 would let the intake
     * take 4 more = 8 controlled (G407). In practice, lower it with gamepad2 dpad down.
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
                allianceFromMemory = false;
                robot.setAlliance(alliance);
                if (fromAuto) {
                    // AUTO ran with the wrong alliance: its paths were the exact 180 deg rotation of ours,
                    // so the real pose is the stored one rotated 180 deg. Count and HIVE state stay.
                    Pose p = robot.drive.pose();
                    robot.drive.setPose(new Pose(-p.x(), -p.y(), MathUtil.wrapRad(p.heading() + Math.PI)));
                } else {
                    int balls = robot.indexer.count();
                    setPracticeStart();
                    robot.indexer.setCount(balls);
                }
            }
        }
        if (upPressed) robot.indexer.adjustCount(+1);
        if (downPressed) robot.indexer.adjustCount(-1);
    }

    private void showInitTelemetry() {
        telemetry.addLine("=== 12303 TeleOp - INIT (nothing moves until START, G403) ===");
        String warning = CompetitionCheck.warning();
        if (warning != null) telemetry.addLine(warning);
        if (fromAuto) {
            telemetry.addLine(String.format(Locale.US, "Alliance %s (from AUTO, %.0f s ago) - wrong? gamepad1 X = BLUE / B = RED",
                    alliance, PoseStorage.ageSeconds()));
            telemetry.addLine("Pose / balls / HIVE state taken from AUTO");
            if (robot.hive.state() == HiveState.B) {
                telemetry.addLine("AUTO says HIVE state B: target = the cell facing " + robot.hive.goText().replace("GO TO ", "")
                        + ". If the OTHER cell is up (e.g. an A3 double tip), press Y after START.");
            }
        } else {
            telemetry.addLine("!! NO AUTO DATA - ALLIANCE = " + alliance
                    + (allianceFromMemory ? " (from the last AUTO INIT)" : " (default)")
                    + ". Press gamepad1 X = BLUE / B = RED to change");
            telemetry.addLine("Robot assumed on OUR S1 (" + FieldConstants.StartSpot.S1.description(alliance)
                    + "). Not there? After START: point it straight away from the drivers and press back; "
                    + "relocalize with LB+Y when it is on S1.");
            telemetry.addLine("Ball count starts at 4 (match preloads). Fewer: gamepad2 dpad down");
        }
        robot.addTelemetry(telemetry);
        robot.addWarnings(telemetry);
        telemetry.update();
    }

    private void showMatchTelemetry(MatchTimer timer) {
        Pose p = robot.drive.pose();
        long now = System.currentTimeMillis();
        String mode = !robot.spinUp() ? "shooting mode OFF"
                : (robot.readyToFire() ? "READY TO FIRE" : "spinning up / aiming");
        telemetry.addLine(String.format(Locale.US, "TIME %s | %s | BALLS %d/4 | %s%s",
                timer.remainingText(), alliance, robot.indexer.count(), mode, manualAim ? " | MANUAL AIM" : ""));
        if (now - tipAtMs < BANNER_MS) {
            telemetry.addLine(">> TARGET SWITCHED: " + robot.hive.goText() + " (stay if you are already there) <<");
        }
        if (now - volleyCancelledAtMs < 3000) {
            telemetry.addLine("Volley cancelled by the target switch - press RB again when ready");
        }
        if (now - unconfirmedFeedAtMs < 3000) {
            telemetry.addLine("!! FEED NOT CONFIRMED (no beam break / flywheel dip): jam? Check the balls; "
                    + "LB = eject, back / L-stick = count -1 / +1");
        }
        boolean afterTip = now - tipAtMs < SPILL_WATCH_MS
                && FieldConstants.inSpillBox(alliance, stateBeforeTip, p.x(), p.y(), FieldConstants.ROBOT_HALF_WIDTH);
        boolean afterOurVolley = now - lastShotMs < SPILL_WATCH_MS
                && FieldConstants.inSpillBox(alliance, robot.hive.state(), p.x(), p.y(), FieldConstants.ROBOT_HALF_WIDTH);
        if (afterTip || afterOurVolley) {
            telemetry.addLine("!! IN THE SPILL LANDING BOX - a TIP may be spilling: back off until the balls land (G409)");
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
