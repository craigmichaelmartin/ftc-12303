package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.HiveState;

import java.util.Locale;

/**
 * 12303 AUTO - one OpMode, everything chosen in INIT with gamepad1 (see AutoSelector).
 *
 * Match procedure (drive team):
 *  1. Place the robot on the start spot shown in telemetry, touching the wall, 4 POLLEN preloads.
 *  2. Select "12303 Auto", press INIT, choose alliance / start / routine / delay / preloads, press A
 *     to lock. Do not touch the robot after INIT finishes moving the servos (G304.H).
 *  3. The 30 s timer must be enabled on the Driver Station (G305). TeleOp "12303 TeleOp" is
 *     queued automatically (preselectTeleOp, recommended by the G305 box).
 *
 * The robot pose, ball count, alliance and HIVE state are saved EVERY loop for TeleOp (PoseStorage),
 * because the 30 s timer can stop AUTO at any moment.
 */
@Autonomous(name = "12303 Auto", group = "12303", preselectTeleOp = "12303 TeleOp")
public class MainAuto extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(RobotConfig.TELEMETRY_INTERVAL_MS);
        AutoSelector selector = new AutoSelector();
        Robot robot = new Robot(hardwareMap, selector.alliance());

        // AUTO INIT may position servos (turret centre, gate closed, hood) - they must be done moving
        // before the match starts (G304.H). The wheels and flywheel stay at zero power.
        robot.arm();

        while (opModeInInit()) {
            selector.handle(gamepad1);
            if (selector.consumeChanged()) applySelection(robot, selector);
            robot.update();
            robot.saveToPoseStorage();
            showInitTelemetry(robot, selector);
        }
        if (isStopRequested()) {
            robot.vision.stop();
            return;
        }

        // START: the robot is now exactly on its start spot - tell the Pinpoint (any nudging during
        // INIT is forgotten here).
        applySelection(robot, selector);
        AutoRoutine routine = new AutoRoutine(robot, selector.routine(), selector.start(), selector.delaySec());
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
        telemetry.addLine(String.format(Locale.US, "Place robot at (%.1f, %.1f), heading %.0f = %s",
                start.x(), start.y(), h, FieldConstants.frontFacesText(h)));
        for (String w : selector.sanityWarnings()) telemetry.addLine("CHECK: " + w);
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        if (!Constants.FORESIGHT_TUNED) telemetry.addLine("NOTE: Pedro Foresight not tuned yet (pedro/Constants) - paths slow and inaccurate");
        if (!robot.drive.hasLocalizer()) telemetry.addLine("!! NO ODOMETRY: the robot cannot drive paths (no LEAVE/PARK)");
        robot.addTelemetry(telemetry);
        robot.addWarnings(telemetry);
        telemetry.update();
    }

    private void showRunTelemetry(Robot robot, AutoRoutine routine, double t) {
        telemetry.addLine(String.format(Locale.US, "AUTO %.1f s | %s", t, routine.step()));
        telemetry.addLine(routine.planText());
        if (!routine.note().isEmpty()) telemetry.addLine("note: " + routine.note());
        robot.addTelemetry(telemetry);
        telemetry.update();
    }
}
