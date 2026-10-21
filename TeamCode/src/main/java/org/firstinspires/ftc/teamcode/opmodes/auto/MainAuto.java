package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.AllianceMemory;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.HiveState;

import java.util.Locale;

/**
 * 12303 AUTO - one OpMode, everything chosen in INIT with gamepad1 (see AutoSelector).
 *
 * Match procedure (drive team):
 *  1. Place the robot on OUR alliance's start spot with the jig, touching the wall, 4 POLLEN preloads.
 *  2. Select "12303 Auto", press INIT. Set ALLIANCE to this match's colour FIRST, then check start /
 *     routine / park / delay / preloads; compare the "Place robot at" line with the jig placement (that
 *     line only repeats the selection - nothing on the robot can see a misplaced robot). Press A to lock.
 *     Do not touch the robot after INIT (G304.H): INIT holds the turret centre, gate and hood only; the
 *     intake, spinners, wheels and flywheel stay unpowered until START.
 *  3. The 30 s timer must be enabled on the Driver Station (G305). TeleOp "12303 TeleOp" is
 *     queued automatically (preselectTeleOp, recommended by the G305 box).
 *
 * The robot pose, ball count, alliance and HIVE state are saved EVERY loop for TeleOp (PoseStorage),
 * because the 30 s timer can stop AUTO at any moment. The alliance is also written to a small file
 * (AllianceMemory) so TeleOp starts on the right colour even after an app restart.
 */
@Autonomous(name = "12303 Auto", group = "12303", preselectTeleOp = "12303 TeleOp")
public class MainAuto extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(RobotConfig.TELEMETRY_INTERVAL_MS);
        AutoSelector selector = new AutoSelector();
        Robot robot = new Robot(hardwareMap, selector.alliance());

        // AUTO INIT may hold positional servos (turret centre, gate closed, hood) - they must be done
        // moving before the match starts (G304.H). The CR intake/spinners, wheels and flywheel stay off.
        robot.armForAutoInit();

        while (opModeInInit()) {
            selector.handle(gamepad1);
            if (selector.consumeChanged()) {
                applySelection(robot, selector);
                AllianceMemory.save(selector.alliance());
            }
            robot.update();
            robot.saveToPoseStorage();
            showInitTelemetry(robot, selector);
        }
        if (isStopRequested()) {
            robot.vision.stop();
            return;
        }

        // START: the robot is now exactly on its start spot - tell the Pinpoint (any nudging during
        // INIT is forgotten here), then let every actuator move.
        applySelection(robot, selector);
        AllianceMemory.save(selector.alliance());
        robot.arm();
        AutoRoutine routine = new AutoRoutine(robot, selector.routine(), selector.start(), selector.park(),
                selector.delaySec());
        ElapsedTime clock = new ElapsedTime();
        routine.start(0.0);

        while (opModeIsActive()) {
            routine.update(clock.seconds());
            robot.update();
            robot.saveToPoseStorage();
            showRunTelemetry(robot, routine, clock.seconds());
        }
        robot.saveToPoseStorage();
        robot.vision.stop();
    }

    private static void applySelection(Robot robot, AutoSelector selector) {
        robot.setAlliance(selector.alliance());
        robot.drive.setPose(FieldConstants.startPose(selector.alliance(), selector.start()));
        robot.indexer.setCount(selector.preloads());
        robot.hive.restore(HiveState.A);     // every match starts in state A
    }

    private void showInitTelemetry(Robot robot, AutoSelector selector) {
        selector.addTelemetry(telemetry);
        Pose start = FieldConstants.startPose(selector.alliance(), selector.start());
        double h = Math.toDegrees(start.heading());
        telemetry.addLine(String.format(Locale.US, "Place robot at (%.1f, %.1f), heading %.0f = %s (square to the wall)",
                start.x(), start.y(), h, FieldConstants.frontFacesText(h)));
        telemetry.addLine("  (the POSE line below only repeats this selection - check the placement with the jig)");
        for (String w : selector.sanityWarnings()) telemetry.addLine("CHECK: " + w);
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        if (!Constants.FORESIGHT_TUNED) telemetry.addLine("NOTE: Pedro Foresight not tuned yet (pedro/Constants) - paths capped at half speed and inaccurate");
        if (!robot.drive.hasLocalizer()) telemetry.addLine("!! NO ODOMETRY: paths are skipped (no LEAVE/PARK). From S1, A1 still fires from the start spot");
        robot.addTelemetry(telemetry);
        robot.addWarnings(telemetry);
        telemetry.update();
    }

    private void showRunTelemetry(Robot robot, AutoRoutine routine, double t) {
        telemetry.addLine(String.format(Locale.US, "AUTO %.1f s | %s | park %s", t, routine.step(),
                routine.parkSlot().label(robot.alliance())));
        telemetry.addLine(routine.planText());
        if (!routine.note().isEmpty()) telemetry.addLine("note: " + routine.note());
        robot.addTelemetry(telemetry);
        telemetry.update();
    }
}
