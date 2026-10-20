package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The AUTO routines as ONE explicit state machine. Each routine is just a different list of steps
 * ("the plan"); each step has an ENTER action (start a path, request a volley...) and a DONE test.
 *
 *   A0 Safe:            DELAY -> PARK
 *   A1 Tip & Park:      DELAY -> SHOOT POSE -> VOLLEY 1 -> PARK
 *   A2 / A3 role 1:     DELAY -> SHOOT POSE -> VOLLEY 1 -> GARDEN -> SWEEP -> ZONE B -> VOLLEY 2 -> PARK
 *   A3 role 2:          DELAY -> WAIT FOR PARTNER'S TIP -> VOLLEY 2 -> PARK
 *
 * Safety nets:
 *  - At AUTO_PARK_DEADLINE_SEC we drop everything and drive to the LOADING ZONE (LEAVE + PARK = 8).
 *  - Every path and volley has a timeout, so a jam or a push never freezes the routine.
 *  - Volley 2 only fires if the HIVE tracker believes the far cell is up (we fired >= 3 balls into
 *    the start cell, and vision did not see the start cell still up). Otherwise we keep the balls.
 *  - After every volley we drive off at once: a TIP spills the cell toward the shooter about 3-6 s
 *    after the threshold ball, and touching those balls before they land is a G409 violation.
 *
 * Coordinates are written for red in FieldConstants and rotated for blue automatically.
 */
public class AutoRoutine {

    public enum Step {
        START_DELAY,
        DRIVE_TO_SHOOT_1,
        VOLLEY_1,
        DRIVE_TO_GARDEN,
        SWEEP_GARDEN,
        DRIVE_TO_ZONE_B,
        VOLLEY_2,
        WAIT_FOR_PARTNER_TIP,
        DRIVE_TO_PARK,
        PARKED
    }

    private final Robot robot;
    private final Alliance alliance;
    private final AutoRoutineType type;
    private final StartSpot start;
    private final double delaySec;
    private final List<Step> plan;

    private int index = 0;
    private Step step = Step.START_DELAY;
    private double nowSec = 0.0;
    private double stepStartSec = 0.0;
    private boolean pathRunning = false;
    private double pathDoneAtSec = -1.0;
    private boolean intakeOn = false;
    private int ballsBeforeVolley = 0;
    private boolean volleyPending = false;
    private double volleyRequestedAtSec = 0.0;
    private double tipSeenSinceSec = -1.0;
    private String note = "";

    public AutoRoutine(Robot robot, AutoRoutineType type, StartSpot start, double delaySec) {
        this.robot = robot;
        this.alliance = robot.alliance();
        this.type = type;
        this.start = start;
        this.delaySec = delaySec;
        this.plan = planFor(type);
    }

    private static List<Step> planFor(AutoRoutineType type) {
        switch (type) {
            case A1_TIP_AND_PARK:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_SHOOT_1, Step.VOLLEY_1,
                        Step.DRIVE_TO_PARK, Step.PARKED);
            case A2_TIP_RELOAD_VOLLEY:
            case A3_ROLE1:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_SHOOT_1, Step.VOLLEY_1,
                        Step.DRIVE_TO_GARDEN, Step.SWEEP_GARDEN, Step.DRIVE_TO_ZONE_B, Step.VOLLEY_2,
                        Step.DRIVE_TO_PARK, Step.PARKED);
            case A3_ROLE2:
                return Arrays.asList(Step.START_DELAY, Step.WAIT_FOR_PARTNER_TIP, Step.VOLLEY_2,
                        Step.DRIVE_TO_PARK, Step.PARKED);
            case A0_SAFE:
            default:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_PARK, Step.PARKED);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Running
    // ---------------------------------------------------------------------------------------------
    /** Call once right after START. */
    public void start(double timeSec) {
        nowSec = timeSec;
        index = 0;
        enter(plan.get(0));
    }

    /** Call every loop BEFORE robot.update(). timeSec = seconds since START. */
    public void update(double timeSec) {
        nowSec = timeSec;
        boolean parkingAlready = step == Step.DRIVE_TO_PARK || step == Step.PARKED;
        if (!parkingAlready && nowSec >= RobotConfig.AUTO_PARK_DEADLINE_SEC) {
            note = "park deadline reached";
            jumpTo(Step.DRIVE_TO_PARK);
        } else if (isDone()) {
            exit(step);
            next();
        }
        robot.intake.request(intakeOn, false);
    }

    private void next() {
        if (index < plan.size() - 1) {
            index++;
            enter(plan.get(index));
        }
    }

    private void jumpTo(Step target) {
        exit(step);
        int i = plan.indexOf(target);
        index = (i >= 0) ? i : plan.size() - 1;
        enter(plan.get(index));
    }

    private double elapsed() {
        return nowSec - stepStartSec;
    }

    // ---------------------------------------------------------------------------------------------
    // ENTER: what each step starts
    // ---------------------------------------------------------------------------------------------
    private void enter(Step s) {
        step = s;
        stepStartSec = nowSec;
        volleyPending = false;
        pathRunning = false;
        pathDoneAtSec = -1.0;
        switch (s) {
            case START_DELAY:
                // Pre-spin and pre-aim while waiting: no chassis motion, so it costs nothing.
                if (type.shootsStartCellFirst() && robot.indexer.count() > 0) robot.setShootingMode(true);
                break;

            case DRIVE_TO_SHOOT_1: {
                robot.setShootingMode(true);
                boolean fireFromStart = start == StartSpot.S1 && RobotConfig.A1_FIRE_FROM_START;
                if (!fireFromStart) {
                    Pose shoot = FieldConstants.zoneShootPose(alliance, HiveState.A);
                    followIfFar(AutoPaths.line(pose(), shoot), shoot);
                }
                break;
            }

            case VOLLEY_1:
            case VOLLEY_2:
                if (s == Step.VOLLEY_2 && robot.hive.state() != HiveState.B) {
                    note = "volley 2 skipped: HIVE tracker says the far cell is not up - keeping the balls";
                    break;
                }
                robot.setShootingMode(true);
                if (!robot.drive.isBusy()) robot.drive.holdPose(pose());   // resist being pushed
                ballsBeforeVolley = robot.indexer.count();
                volleyPending = true;      // requested in update() once no TIP pause is active
                break;

            case DRIVE_TO_GARDEN: {
                robot.setShootingMode(false);
                Pose sweepStart = FieldConstants.gardenSweepStart(alliance);
                followIfFar(AutoPaths.route(pose(), FieldConstants.gardenApproach(alliance), sweepStart), sweepStart);
                break;
            }

            case SWEEP_GARDEN: {
                intakeOn = true;   // stops by itself at 4 balls (G407)
                Pose end = FieldConstants.gardenSweepEnd(alliance);
                followIfFar(AutoPaths.slow(AutoPaths.line(pose(), end), RobotConfig.AUTO_SWEEP_SPEED), end);
                break;
            }

            case DRIVE_TO_ZONE_B: {
                intakeOn = false;
                robot.setShootingMode(true);   // spin up and pre-aim on the way
                Pose shoot = zoneBShootPose();
                Pose here = pose();
                List<Pose> waypoints = FieldConstants.routeToZoneB(alliance, here.x(), here.y());
                if (type == AutoRoutineType.A3_ROLE1) waypoints.add(FieldConstants.role1Lane(alliance));
                followIfFar(AutoPaths.route(here, waypoints, shoot), shoot);
                break;
            }

            case WAIT_FOR_PARTNER_TIP:
                // Pre-aim at the FAR cell (still down) and spin up; fire when vision sees it come up.
                robot.setAimOverride(HiveState.B);
                robot.setShootingMode(true);
                robot.drive.holdPose(pose());
                tipSeenSinceSec = -1.0;
                break;

            case DRIVE_TO_PARK: {
                robot.indexer.cancelFiring();
                robot.setShootingMode(false);
                robot.setAimOverride(null);
                intakeOn = false;
                Pose park = parkPose();
                Pose here = pose();
                List<Pose> waypoints = FieldConstants.routeToLoadingZone(alliance, here.x(), here.y());
                if (type == AutoRoutineType.A3_ROLE1 && alliance.fromRedX(here.x()) < -33.0) {
                    // from the role-1 Zone B spot: go east of the role-2 robot's park slot first
                    waypoints.add(0, FieldConstants.role1Lane(alliance));
                }
                followIfFar(AutoPaths.route(here, waypoints, park), park);
                break;
            }

            case PARKED:
                robot.setShootingMode(false);
                // Pedro keeps holding the end pose of the last path: stays in the LZ, off the wall.
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // DONE tests
    // ---------------------------------------------------------------------------------------------
    private boolean isDone() {
        switch (step) {
            case START_DELAY:
                return elapsed() >= delaySec;

            case DRIVE_TO_SHOOT_1:
            case DRIVE_TO_GARDEN:
            case DRIVE_TO_ZONE_B:
            case DRIVE_TO_PARK:
                return pathFinished();

            case SWEEP_GARDEN:
                if (robot.indexer.isFull()) return true;
                if (!pathFinished()) return false;
                if (pathDoneAtSec < 0) pathDoneAtSec = nowSec;
                return nowSec - pathDoneAtSec >= RobotConfig.AUTO_SWEEP_DWELL_SEC;

            case VOLLEY_1:
            case VOLLEY_2:
                if (volleyPending) {
                    // A HIVE state change (e.g. "partner TIP seen" just before VOLLEY_2) starts a short
                    // TIP pause that cancels firing - so ask for the volley only after the pause.
                    if (!robot.hive.tipPauseActive()) {
                        robot.indexer.requestVolley();
                        volleyPending = false;
                        volleyRequestedAtSec = nowSec;
                    }
                    return elapsed() > RobotConfig.AUTO_VOLLEY_TIMEOUT_SEC;
                }
                if (!robot.indexer.isFiring() && nowSec - volleyRequestedAtSec > 0.2) return true;   // emptied, or cancelled
                return elapsed() > RobotConfig.AUTO_VOLLEY_TIMEOUT_SEC;

            case WAIT_FOR_PARTNER_TIP:
                return partnerTipSeen() || elapsed() > RobotConfig.A3_ROLE2_TIP_WAIT_TIMEOUT_SEC;

            case PARKED:
            default:
                return false;
        }
    }

    private boolean pathFinished() {
        if (!pathRunning) return true;
        if (elapsed() > RobotConfig.AUTO_PATH_TIMEOUT_SEC) {
            note = step + " timed out";
            return true;
        }
        return elapsed() > 0.05 && !robot.drive.isBusy();
    }

    /** Vision must see the far cell up (or its own tracker switched to B) for HIVE_VISION_CONFIRM_MS. */
    private boolean partnerTipSeen() {
        boolean seesB = robot.vision.evidence() == HiveState.B || robot.hive.state() == HiveState.B;
        if (!seesB) {
            tipSeenSinceSec = -1.0;
            return false;
        }
        if (tipSeenSinceSec < 0) tipSeenSinceSec = nowSec;
        return (nowSec - tipSeenSinceSec) * 1000.0 >= RobotConfig.HIVE_VISION_CONFIRM_MS;
    }

    // ---------------------------------------------------------------------------------------------
    // EXIT: clean-up after a step
    // ---------------------------------------------------------------------------------------------
    private void exit(Step s) {
        switch (s) {
            case VOLLEY_1: {
                int fired = ballsBeforeVolley - robot.indexer.count();
                robot.indexer.cancelFiring();
                if (fired >= RobotConfig.AUTO_ASSUME_TIP_AFTER_BALLS) {
                    // 3 own NECTAR start in this cell, so ~3 POLLEN tip it (EFSG 12): assume it tipped.
                    robot.hive.setState(HiveState.B, HiveTracker.Source.AUTO);
                }
                note = "volley 1 fired " + fired;
                break;
            }
            case VOLLEY_2:
                robot.indexer.cancelFiring();
                break;
            case WAIT_FOR_PARTNER_TIP:
                if (partnerTipSeen() || robot.hive.state() == HiveState.B) {
                    robot.hive.setState(HiveState.B, HiveTracker.Source.AUTO);
                    note = "partner TIP seen";
                } else if (RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM) {
                    robot.hive.setState(HiveState.B, HiveTracker.Source.AUTO);
                    note = "no TIP seen - firing anyway (A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM)";
                } else {
                    note = "no TIP seen - keeping the preloads";
                }
                robot.setAimOverride(null);
                break;
            case SWEEP_GARDEN:
                intakeOn = false;
                break;
            default:
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------
    private Pose pose() {
        return robot.drive.pose();
    }

    /** Start the path unless we are already there (Pedro does not like zero-length paths). */
    private void followIfFar(Path path, Pose end) {
        if (!robot.drive.hasLocalizer()) {
            note = "no odometry: paths skipped";
            return;
        }
        double headingErrorDeg = Math.toDegrees(MathUtil.wrapRad(pose().heading() - end.heading()));
        if (pose().distance(end) < 1.0 && Math.abs(headingErrorDeg) < 3.0) return;
        robot.drive.followPath(path);
        pathRunning = true;
    }

    private Pose zoneBShootPose() {
        if (type == AutoRoutineType.A3_ROLE1) {
            return FieldConstants.shootPose(alliance, FieldConstants.ZONE_B_SHOOT_ROLE1_X,
                    FieldConstants.ZONE_B_SHOOT_ROLE1_Y, HiveState.B);
        }
        return FieldConstants.zoneShootPose(alliance, HiveState.B);
    }

    private Pose parkPose() {
        double heading = pose().heading();
        if (type == AutoRoutineType.A3_ROLE1) {
            return FieldConstants.parkPose(alliance, FieldConstants.PARK_ROLE1_X, FieldConstants.PARK_ROLE1_Y, heading);
        }
        if (type == AutoRoutineType.A3_ROLE2) {
            return FieldConstants.parkPose(alliance, FieldConstants.PARK_ROLE2_X, FieldConstants.PARK_ROLE2_Y, heading);
        }
        return FieldConstants.parkPose(alliance, FieldConstants.PARK_X, FieldConstants.PARK_Y, heading);
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    public Step step() {
        return step;
    }

    public String note() {
        return note;
    }

    /** The plan as text, e.g. "START_DELAY > VOLLEY_1 > ...". */
    public String planText() {
        List<String> names = new ArrayList<>();
        for (Step s : plan) names.add(s == step ? "[" + s + "]" : s.toString());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(" > ");
            sb.append(names.get(i));
        }
        return sb.toString();
    }
}
