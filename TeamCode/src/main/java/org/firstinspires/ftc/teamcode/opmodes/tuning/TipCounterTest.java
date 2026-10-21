package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.CsvLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TIP COUNTER TEST - how many balls does OUR HIVE really need to tip? (strategy brief testing plan,
 * experiment 1: (a) start cell with 3 NECTAR, (b) empty cell; 5 trials each; time the dwell.)
 *
 * Official calibration says: start cell (3 NECTAR inside) tips on the 3rd POLLEN; an empty cell needs
 * 8 POLLEN (7 must not tip) or 3 NECTAR + 3 POLLEN (EFSG section 12). Practice HIVEs differ - measure.
 *
 *   A   shooting mode on/off (flywheel + auto-aim at the current cell)
 *   RB  fire ONE ball (counted when the feed finishes, confirmed or not)
 *   LB  the last launched ball MISSED or BOUNCED OUT (launched - 1): Exp 1 counts only balls that stay in
 *   X   +1 ball placed/tossed by hand (no robot shot)
 *   Y   THE HIVE TIPPED NOW (records the trial: balls in + time since the last ball = "dwell")
 *   B   start a new trial (counters to 0)            dpad up  flip target cell
 * "unconfirmed" on screen = feeds that neither the beam nor a flywheel dip confirmed (maybe no ball left):
 * check them and press LB if the ball did not go in.
 *
 * Results stay on screen and go to /sdcard/FIRST/data/12303_tips_*.csv and logcat (tag 12303).
 */
@TeleOp(name = "Test: Tip Counter", group = "12303 Tuning")
public class TipCounterTest extends LinearOpMode {

    private static class Trial {
        int launched, placed;
        double dwellSec;
    }

    private final List<Trial> trials = new ArrayList<>();
    private int launched = 0, placed = 0;
    private long lastBallMs = 0;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        Robot robot = new Robot(hardwareMap, Alliance.RED);
        robot.drive.setPose(FieldConstants.startPose(Alliance.RED, FieldConstants.StartSpot.S1));
        telemetry.addLine("Tip counter (RED, robot on S1). Load balls, press START.");
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        robot.addWarnings(telemetry);
        telemetry.update();
        waitForStart();
        if (isStopRequested()) return;

        robot.arm();
        CsvLogger log = new CsvLogger("12303_tips", "trial,launched,placed,total,dwell_s");

        while (opModeIsActive()) {
            boolean a = gamepad1.aWasPressed();
            boolean rb = gamepad1.rightBumperWasPressed();
            boolean x = gamepad1.xWasPressed();
            boolean y = gamepad1.yWasPressed();
            boolean b = gamepad1.bWasPressed();
            boolean up = gamepad1.dpadUpWasPressed();
            boolean lb = gamepad1.leftBumperWasPressed();

            if (a) robot.setShootingMode(!robot.spinUp());
            if (rb) {
                robot.setShootingMode(true);
                robot.indexer.requestSingleShot();
            }
            if (x) {
                placed++;
                lastBallMs = System.currentTimeMillis();
            }
            if (up) robot.hive.operatorToggle();
            if (lb) launched = Math.max(0, launched - 1);   // that ball missed / bounced out
            if (b) {
                launched = 0;
                placed = 0;
            }

            robot.update();
            if (robot.shotEventThisLoop()) {
                launched++;
                lastBallMs = System.currentTimeMillis();
            }
            if (y) {
                Trial t = new Trial();
                t.launched = launched;
                t.placed = placed;
                t.dwellSec = lastBallMs == 0 ? 0 : (System.currentTimeMillis() - lastBallMs) / 1000.0;
                trials.add(t);
                log.row(trials.size(), t.launched, t.placed, t.launched + t.placed, t.dwellSec);
                RobotLog.ii("12303", String.format(Locale.US, "TIP trial %d: %d launched + %d placed, dwell %.2f s",
                        trials.size(), t.launched, t.placed, t.dwellSec));
                launched = 0;
                placed = 0;
            }
            show(robot);
        }
        log.close();
    }

    private void show(Robot robot) {
        telemetry.addLine(String.format(Locale.US, "THIS TRIAL: launched %d + placed %d = %d | since last ball %.1f s",
                launched, placed, launched + placed,
                lastBallMs == 0 ? 0.0 : (System.currentTimeMillis() - lastBallMs) / 1000.0));
        telemetry.addLine("A shoot mode | RB fire one | LB last one missed | X +1 by hand | Y TIPPED NOW | B new trial | dpad up flip cell");
        double sum = 0, dwell = 0;
        for (int i = 0; i < trials.size(); i++) {
            Trial t = trials.get(i);
            telemetry.addLine(String.format(Locale.US, "trial %d: %d launched + %d placed = %d, dwell %.2f s",
                    i + 1, t.launched, t.placed, t.launched + t.placed, t.dwellSec));
            sum += t.launched + t.placed;
            dwell += t.dwellSec;
        }
        if (!trials.isEmpty()) {
            telemetry.addLine(String.format(Locale.US, "AVERAGE: %.1f balls to tip, dwell %.2f s", sum / trials.size(), dwell / trials.size()));
        }
        telemetry.addLine(String.format(Locale.US, "flywheel %.0f/%.0f %s | balls in robot %d | target cell %s | feeds confirmed %d, unconfirmed %d",
                robot.shooter.measuredRpm(), robot.shooter.targetRpm(), robot.shooter.state(), robot.indexer.count(),
                robot.targetCell(), robot.indexer.confirmedShots(), robot.indexer.unconfirmedShots()));
        telemetry.update();
    }
}
