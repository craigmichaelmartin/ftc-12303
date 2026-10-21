package org.firstinspires.ftc.teamcode.opmodes.tuning;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CompetitionCheck;
import org.firstinspires.ftc.teamcode.util.CsvLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * SHOT MAP LOGGER - builds the distance -> RPM table (strategy brief testing plan, experiment 2:
 * 5 distances x 0/20/40 deg off-axis, 10 shots each, record made / bounce-out / miss).
 *
 * Setup: robot on RED, placed at a known spot (start it on S1, or drive there - the Pinpoint tracks it).
 * Auto-aim is ON: the turret points at the current target cell (Y flips between the two cells).
 *
 *   A           flywheel on/off at the chosen RPM
 *   dpad up/dn  RPM +-50                  dpad left/right  distance override -+3 in (only without odometry)
 *   RB          fire ONE ball (ignored until the previous shot is scored)
 *   then score that ball:  X = MADE,  B = BOUNCED OUT,  LB = MISSED
 *   back        delete the last row from the screen table (the CSV keeps it and gets a
 *               DELETED_PREVIOUS marker row - skip marked rows when you analyse the file)
 *   Y           flip target cell (HIVE state)
 *   sticks      drive slowly (field-centric, red driver view) to the next test spot
 *
 * The table on screen groups rows by distance (3 in buckets) and RPM. The suggested SHOT_TABLE row per
 * distance is the RPM with the best make rate (min 5 shots). Every row is also saved to
 * /sdcard/FIRST/data/12303_shotmap_*.csv and printed to logcat (tag 12303).
 */
@TeleOp(name = "Tune: Shot Map Logger", group = "12303 Tuning")
public class ShotMapLogger extends LinearOpMode {

    private enum Result { MADE, BOUNCED_OUT, MISSED }

    private static class Shot {
        double distanceIn, offAxisDeg, targetRpm, measuredRpm, volts;
        Result result;
    }

    private Robot robot;
    private final List<Shot> shots = new ArrayList<>();
    private double rpm = 3550.0;
    private boolean spinning = false;
    private double manualDistanceIn = 45.0;
    private Shot pending = null;
    private CsvLogger log;

    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(100);
        robot = new Robot(hardwareMap, Alliance.RED);
        robot.drive.setPose(FieldConstants.startPose(Alliance.RED, FieldConstants.StartSpot.S1));
        telemetry.addLine("Shot map logger (RED). Robot assumed on S1 at START - or drive it to a spot.");
        String competition = CompetitionCheck.warning();
        if (competition != null) telemetry.addLine(competition);
        robot.addWarnings(telemetry);
        telemetry.update();
        waitForStart();
        if (isStopRequested()) return;

        robot.arm();
        robot.setAutoAim(true);
        log = new CsvLogger("12303_shotmap", "distance_in,off_axis_deg,target_rpm,measured_rpm,battery_v,result");

        while (opModeIsActive()) {
            boolean a = gamepad1.aWasPressed();
            boolean up = gamepad1.dpadUpWasPressed();
            boolean down = gamepad1.dpadDownWasPressed();
            boolean left = gamepad1.dpadLeftWasPressed();
            boolean right = gamepad1.dpadRightWasPressed();
            boolean rb = gamepad1.rightBumperWasPressed();
            boolean x = gamepad1.xWasPressed();
            boolean b = gamepad1.bWasPressed();
            boolean lb = gamepad1.leftBumperWasPressed();
            boolean back = gamepad1.backWasPressed();
            boolean y = gamepad1.yWasPressed();

            if (a) spinning = !spinning;
            if (up) rpm += 50;
            if (down) rpm -= 50;
            if (left) manualDistanceIn -= 3;
            if (right) manualDistanceIn += 3;
            if (y) robot.hive.operatorToggle();
            if (rb && pending == null) {      // score the last shot first
                spinning = true;
                robot.indexer.requestSingleShot();
                pending = snapshot();
            }
            if (pending != null && (x || b || lb)) {
                pending.result = x ? Result.MADE : (b ? Result.BOUNCED_OUT : Result.MISSED);
                record(pending);
                pending = null;
            }
            if (back && !shots.isEmpty()) {
                Shot removed = shots.remove(shots.size() - 1);
                log.row(removed.distanceIn, removed.offAxisDeg, removed.targetRpm, removed.measuredRpm, removed.volts,
                        "DELETED_PREVIOUS");
            }

            rpm = Math.max(0, Math.min(RobotConfig.FLYWHEEL_MAX_RPM, rpm));
            robot.setManualRpm(spinning ? rpm : 0.0);
            robot.drive.driveFieldCentric(-gamepad1.left_stick_y * 0.4, gamepad1.left_stick_x * 0.4,
                    -gamepad1.right_stick_x * 0.3, Alliance.RED.driverForwardHeadingDeg());
            robot.update();
            show();
        }
        log.close();
    }

    private double distanceIn() {
        return robot.drive.hasLocalizer() ? robot.aim().distanceIn : manualDistanceIn;
    }

    private Shot snapshot() {
        Shot s = new Shot();
        Pose p = robot.drive.pose();
        s.distanceIn = distanceIn();
        s.offAxisDeg = FieldConstants.offAxisDeg(Alliance.RED, robot.targetCell(), p.x(), p.y());
        s.targetRpm = rpm;
        s.measuredRpm = robot.shooter.measuredRpm();
        s.volts = robot.batteryVolts();
        return s;
    }

    private void record(Shot s) {
        shots.add(s);
        log.row(s.distanceIn, s.offAxisDeg, s.targetRpm, s.measuredRpm, s.volts, s.result);
        RobotLog.ii("12303", String.format(Locale.US, "SHOT dist %.1f off %.0f rpm %.0f meas %.0f %.2fV %s",
                s.distanceIn, s.offAxisDeg, s.targetRpm, s.measuredRpm, s.volts, s.result));
    }

    private void show() {
        telemetry.addLine(String.format(Locale.US, "RPM %.0f (flywheel %s, %.0f) | distance %.1f in %s | off-axis %.0f deg",
                rpm, spinning ? "ON" : "off", robot.shooter.measuredRpm(), distanceIn(),
                robot.drive.hasLocalizer() ? "(odometry)" : "(MANUAL - dpad L/R)",
                FieldConstants.offAxisDeg(Alliance.RED, robot.targetCell(), robot.drive.pose().x(), robot.drive.pose().y())));
        telemetry.addLine(pending != null ? ">>> score the last shot: X made, B bounced out, LB missed (RB waits for this)"
                : "RB = fire one | A = flywheel | back = delete last row (CSV marks it) | Y = flip target cell");
        telemetry.addLine(String.format(Locale.US, "feed %s %s | balls %d | target cell %s | turret %+.1f %s",
                robot.indexer.feedState(), robot.indexer.blockedBy(), robot.indexer.count(), robot.targetCell(),
                robot.turret.currentOffsetDeg(), robot.aim().reachable ? "" : "(OUT OF RANGE: rotate robot)"));
        addSummary();
        telemetry.update();
    }

    /** Groups shots by 3-inch distance bucket and RPM; suggests the best RPM per bucket. */
    private void addSummary() {
        Map<Long, Map<Long, int[]>> table = new TreeMap<>();   // bucket -> rpm -> {made, total}
        for (Shot s : shots) {
            long bucket = Math.round(s.distanceIn / 3.0) * 3;
            long r = Math.round(s.targetRpm);
            Map<Long, int[]> row = table.get(bucket);
            if (row == null) {
                row = new TreeMap<>();
                table.put(bucket, row);
            }
            int[] c = row.get(r);
            if (c == null) {
                c = new int[2];
                row.put(r, c);
            }
            if (s.result == Result.MADE) c[0]++;
            c[1]++;
        }
        telemetry.addLine("---- " + shots.size() + " shots: distance | rpm made/total ----");
        StringBuilder suggestion = new StringBuilder();
        for (Map.Entry<Long, Map<Long, int[]>> e : table.entrySet()) {
            StringBuilder line = new StringBuilder(String.format(Locale.US, "%3d in |", e.getKey()));
            long bestRpm = -1;
            double bestRate = -1;
            for (Map.Entry<Long, int[]> r : e.getValue().entrySet()) {
                int[] c = r.getValue();
                line.append(String.format(Locale.US, " %d:%d/%d", r.getKey(), c[0], c[1]));
                double rate = c[1] >= 5 ? (double) c[0] / c[1] : -1;
                if (rate > bestRate) {
                    bestRate = rate;
                    bestRpm = r.getKey();
                }
            }
            telemetry.addLine(line.toString());
            if (bestRate >= 0) suggestion.append(String.format(Locale.US, "{%d, %d}, ", e.getKey(), bestRpm));
        }
        if (suggestion.length() > 0) telemetry.addLine("SHOT_TABLE suggestion: " + suggestion);
    }
}
