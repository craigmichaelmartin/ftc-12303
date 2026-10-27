package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The AUTO routines as ONE explicit state machine. Each routine is just a different list of steps
 * ("the plan"); each step has an ENTER action (start a path, request a volley...) and a DONE test.
 * Every drive is a leg from AutoLegs (pure geometry, swept by the JUnit RouteSweepTest).
 *
 *   A0 Safe:        DELAY -> PARK
 *   A1 Tip & Park:  DELAY -> SHOOT POSE -> VOLLEY 1 -> SPILL HOLD -> PARK
 *   A2:             DELAY -> SHOOT POSE -> VOLLEY 1 -> GARDEN -> SWEEP -> ZONE B (B2) -> VOLLEY 2 -> PARK
 *   A3 role 1:      DELAY -> SHOOT POSE -> VOLLEY 1 -> GARDEN -> SWEEP -> CORRIDOR END -> WAIT UNTIL 17 s
 *                   -> ZONE B (role-1 spot) -> VOLLEY 2 -> PARK (INNER, turning first, along the TIP-2 box edge)
 *   A3 role 2:      DELAY -> WAIT FOR PARTNER'S TIP (fire only if seen by 9 s) -> VOLLEY 2 -> PARK (CORNER;
 *                   whatever step it is in, it leaves for the park slot at 13 s, so it is parked before 17 s)
 *   Invalid start:  DELAY -> PARKED (the robot does not move; INIT shows "!! INVALID START")
 *   SHOOT POSE: S1 fires from the start spot; S2-S4 drive to the Zone A side spot (55, -37) first.
 *
 * Safety nets:
 *  - At AUTO_PARK_DEADLINE_SEC (23 s after START) we drop everything and drive to the LOADING ZONE.
 *  - A3 time gates (from START): role 2 fires only if TIP 1 is seen by 9 s and leaves by 13 s
 *    (A3_ROLE2_LEAVE_BY_SEC); role 1 waits at the corridor end until 17 s.
 *  - Every path has a timeout computed from its length and the configured path speed (the 0.5 speed cap
 *    gives longer timeouts); a timed-out path stops and the robot holds where it is. Every volley has a
 *    timeout that starts once the robot has stopped.
 *  - Volley 1 assumes the TIP after >= AUTO_ASSUME_TIP_AFTER_BALLS balls (only CONFIRMED shots count when
 *    a feed beam is fitted) and locks vision out for AUTO_TIP_ASSUME_PROTECT_MS (the start cell stays up
 *    during the 0.25-3.4 s dwell). Volley 2 fires only if the tracker says the second cell is up; if not,
 *    it waits up to AUTO_VOLLEY2_WAIT_FOR_B_SEC for vision, then keeps the balls.
 *  - A1 holds at its firing pose (outside the tip-1 landing box) until the spill has landed: start-cell
 *    tags seen up and then gone A1_SPILL_TAGS_GONE_SEC, or A1_SPILL_HOLD_MAX_SEC after the last shot.
 *    A2 / A3 role 1 leave at once, on legs that keep the turning robot out of the tip-1 landing box (G409).
 *  - A volley never starts when the target cell faces away from the robot (wrong start / zone).
 *  - Aiming: the shooter is fixed (out of the robot's back). Every shooting pose already faces the target;
 *    during a volley the robot also turns itself onto the exact aim heading (operator trim and vision bias
 *    included) whenever it is more than half the aim tolerance off, stepping off a wall first if it is
 *    touching one (AutoLegs.volleyAimHold / chassisAimPose).
 *  - A3 role 2 starts on S6 only (any other start is invalid: the robot does not move).
 *
 * Coordinates are written for red in FieldConstants and rotated for blue automatically.
 */
public class AutoRoutine {

    public enum Step {
        START_DELAY,
        DRIVE_TO_SHOOT_1,
        VOLLEY_1,
        SPILL_HOLD,
        DRIVE_TO_GARDEN,
        SWEEP_GARDEN,
        DRIVE_TO_CORRIDOR_END,
        WAIT_FOR_PARTNER_PARK,
        DRIVE_TO_ZONE_B,
        WAIT_FOR_PARTNER_TIP,
        VOLLEY_2,
        DRIVE_TO_PARK,
        PARKED
    }

    private final Robot robot;
    private final Alliance alliance;
    private final AutoRoutineType type;
    private final StartSpot start;
    private final ParkSlot parkChoice;
    private final double delaySec;
    private final boolean valid;
    private final List<Step> plan;

    private int index = 0;
    private Step step = Step.START_DELAY;
    private double nowSec = 0.0;
    private double stepStartSec = 0.0;
    private boolean pathRunning = false;
    private boolean lastPathTimedOut = false;
    private double pathTimeoutSec = 0.0;
    private double pathDoneAtSec = -1.0;
    private boolean intakeOn = false;
    private int ballsBeforeVolley = 0;
    private int confirmedBeforeVolley = 0;
    private int volley1Launched = 0;
    private String volleySkipNote = null;
    private boolean volleyPending = false;
    private boolean waitingForB = false;
    private boolean skipVolley = false;
    private boolean skipVolley2 = false;
    private double volleyRequestedAtSec = -1.0;
    private double volleyClockSec = -1.0;
    private double lastAimHoldSec = -10.0;
    private double tipSeenSinceSec = -1.0;
    private double lastShotSec = -1.0;
    private boolean sawStartCellUp = false;
    private double lastStartCellUpSec = -1.0;
    private String note = "";

    public AutoRoutine(Robot robot, AutoRoutineType type, StartSpot start, ParkSlot parkChoice, double delaySec) {
        this.robot = robot;
        this.alliance = robot.alliance();
        this.type = type;
        this.start = start;
        this.parkChoice = parkChoice;
        this.delaySec = delaySec;
        this.valid = type.canStartFrom(start);
        this.plan = valid ? planFor(type) : Arrays.asList(Step.START_DELAY, Step.PARKED);
        if (!valid) note = invalidText(type, start);
    }

    /** "!! INVALID START S6 FOR A1_TIP_AND_PARK: ROBOT WILL NOT MOVE" */
    public static String invalidText(AutoRoutineType type, StartSpot start) {
        return "!! INVALID START " + start + " FOR " + type.name() + ": ROBOT WILL NOT MOVE";
    }

    private static List<Step> planFor(AutoRoutineType type) {
        switch (type) {
            case A1_TIP_AND_PARK:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_SHOOT_1, Step.VOLLEY_1, Step.SPILL_HOLD,
                        Step.DRIVE_TO_PARK, Step.PARKED);
            case A2_TIP_RELOAD_VOLLEY:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_SHOOT_1, Step.VOLLEY_1,
                        Step.DRIVE_TO_GARDEN, Step.SWEEP_GARDEN, Step.DRIVE_TO_ZONE_B, Step.VOLLEY_2,
                        Step.DRIVE_TO_PARK, Step.PARKED);
            case A3_ROLE1:
                return Arrays.asList(Step.START_DELAY, Step.DRIVE_TO_SHOOT_1, Step.VOLLEY_1,
                        Step.DRIVE_TO_GARDEN, Step.SWEEP_GARDEN, Step.DRIVE_TO_CORRIDOR_END,
                        Step.WAIT_FOR_PARTNER_PARK, Step.DRIVE_TO_ZONE_B, Step.VOLLEY_2,
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
        watchShotsAndStartCell();
        boolean parkingAlready = step == Step.DRIVE_TO_PARK || step == Step.PARKED;
        if (valid && !parkingAlready && nowSec >= RobotConfig.AUTO_PARK_DEADLINE_SEC) {
            note = "park deadline reached (" + RobotConfig.AUTO_PARK_DEADLINE_SEC + " s)";
            jumpTo(Step.DRIVE_TO_PARK);
        } else if (valid && !parkingAlready && type == AutoRoutineType.A3_ROLE2 && nowSec >= RobotConfig.A3_ROLE2_LEAVE_BY_SEC) {
            // A3 gate (D5): role 2 must be parked before role 1 leaves the corridor end at
            // A3_ROLE1_ZONE_B_NOT_BEFORE_SEC - even when its volley stalls (the volley timeout alone would end
            // it 5 s after it began, 14 s after a TIP at 9 s) or a start delay was set.
            note = "A3 role 2 leaves at " + RobotConfig.A3_ROLE2_LEAVE_BY_SEC + " s (parked before role 1 moves)";
            jumpTo(Step.DRIVE_TO_PARK);
        } else if (isDone()) {
            exit(step);
            next();
        } else {
            during(step);
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

    /** Remember the last shot and whether the start cell's tags were seen up (A1 spill hold). */
    private void watchShotsAndStartCell() {
        if (robot.shotEventThisLoop()) lastShotSec = nowSec;
        if (step == Step.VOLLEY_1 || step == Step.SPILL_HOLD) {
            if (robot.vision.seesCellUp(HiveState.A)) {
                sawStartCellUp = true;
                lastStartCellUpSec = nowSec;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ENTER: what each step starts
    // ---------------------------------------------------------------------------------------------
    private void enter(Step s) {
        boolean previousTimedOut = lastPathTimedOut;
        step = s;
        stepStartSec = nowSec;
        volleyPending = false;
        waitingForB = false;
        skipVolley = false;
        volleySkipNote = null;
        pathRunning = false;
        lastPathTimedOut = false;
        pathDoneAtSec = -1.0;
        switch (s) {
            case START_DELAY:
                // Pre-spin while waiting: no chassis motion, so it costs nothing.
                if (valid && type.shootsStartCellFirst() && robot.carrier.count() > 0) robot.setShootingMode(true);
                break;

            case DRIVE_TO_SHOOT_1: {
                robot.setShootingMode(true);
                AutoLegs.Leg leg = AutoLegs.toShoot1(alliance, start);
                if (leg != null) follow(leg);
                break;
            }

            case VOLLEY_1:
            case VOLLEY_2:
                if (s == Step.VOLLEY_2 && skipVolley2) {
                    skipVolley = true;          // the note was set when the reason was found
                    break;
                }
                if (previousTimedOut) robot.drive.holdPose(pose());   // stop chasing a timed-out path
                robot.setShootingMode(true);
                ballsBeforeVolley = robot.carrier.count();
                confirmedBeforeVolley = robot.carrier.confirmedShots();
                volleyClockSec = -1.0;
                volleyRequestedAtSec = -1.0;
                lastAimHoldSec = -10.0;
                waitingForB = s == Step.VOLLEY_2 && robot.targetCell() != HiveState.B;
                if (waitingForB) note = "volley 2: waiting up to " + RobotConfig.AUTO_VOLLEY2_WAIT_FOR_B_SEC
                        + " s for vision to see the second cell up";
                volleyPending = !waitingForB;   // requested in isDone() once no TIP pause is active
                break;

            case SPILL_HOLD:
                // The robot holds its firing pose, so the rear camera keeps facing the start cell and sees it tip.
                robot.setShootingMode(false);
                intakeOn = false;
                if (!robot.drive.isBusy()) robot.drive.holdPose(pose());
                break;

            case DRIVE_TO_GARDEN:
                robot.setShootingMode(false);
                follow(AutoLegs.toGarden(alliance));
                break;

            case SWEEP_GARDEN:
                intakeOn = true;   // with a mouth sensor it stops by itself at 4 balls (G407)
                follow(AutoLegs.sweep(alliance));
                break;

            case DRIVE_TO_CORRIDOR_END:
                intakeOn = false;
                robot.setShootingMode(false);
                follow(AutoLegs.toRole1Wait(alliance, pose()));
                break;

            case WAIT_FOR_PARTNER_PARK:
                // A3 role 1 waits (square heading, out of role 2's way) until role 2 has parked.
                if (!robot.drive.isBusy()) robot.drive.holdPose(pose());
                note = String.format(Locale.US, "waiting at the corridor end until %.0f s (role 2 parks first)",
                        RobotConfig.A3_ROLE1_ZONE_B_NOT_BEFORE_SEC);
                break;

            case DRIVE_TO_ZONE_B:
                intakeOn = false;
                robot.setShootingMode(true);   // spin up on the way
                follow(type == AutoRoutineType.A3_ROLE1 ? AutoLegs.toRole1Spot(alliance) : AutoLegs.toZoneB(alliance, pose()));
                break;

            case WAIT_FOR_PARTNER_TIP:
                // Aim at the second cell (still down; S6 puts the robot's back toward it) and spin up; fire
                // when vision sees it come up.
                robot.setAimOverride(HiveState.B);
                robot.setShootingMode(true);
                robot.drive.holdPose(pose());
                tipSeenSinceSec = -1.0;
                break;

            case DRIVE_TO_PARK:
                robot.carrier.cancelFiring();
                robot.setShootingMode(false);
                robot.setAimOverride(null);
                intakeOn = false;
                follow(AutoLegs.toPark(alliance, type, parkChoice, pose()));
                break;

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
            case DRIVE_TO_CORRIDOR_END:
            case DRIVE_TO_ZONE_B:
            case DRIVE_TO_PARK:
                return pathFinished();

            case SWEEP_GARDEN:
                if (robot.carrier.isFull()) return true;
                if (!pathFinished()) return false;
                if (pathDoneAtSec < 0) pathDoneAtSec = nowSec;
                return nowSec - pathDoneAtSec >= RobotConfig.AUTO_SWEEP_DWELL_SEC;

            case VOLLEY_1:
            case VOLLEY_2:
                return volleyDone();

            case SPILL_HOLD: {
                if (volley1Launched <= 0 || lastShotSec < 0) return true;   // nothing left the robot: nothing spills
                if (nowSec - lastShotSec >= RobotConfig.A1_SPILL_HOLD_MAX_SEC) return true;
                return sawStartCellUp && nowSec - lastStartCellUpSec >= RobotConfig.A1_SPILL_TAGS_GONE_SEC;
            }

            case WAIT_FOR_PARTNER_PARK:
                return nowSec >= RobotConfig.A3_ROLE1_ZONE_B_NOT_BEFORE_SEC;

            case WAIT_FOR_PARTNER_TIP:
                if (partnerTipSeen()) return true;
                if (RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM && nowSec >= RobotConfig.A3_ROLE2_LATEST_FIRE_SEC) return true;
                return nowSec >= RobotConfig.A3_ROLE2_TIP_WAIT_TIMEOUT_SEC;

            case PARKED:
            default:
                return false;
        }
    }

    private boolean volleyDone() {
        if (skipVolley) return true;
        // The volley clock starts once the robot has stopped (or AUTO_VOLLEY_MAX_SETTLE_SEC after entry).
        if (volleyClockSec < 0 && (!robot.drive.isBusy() || elapsed() >= RobotConfig.AUTO_VOLLEY_MAX_SETTLE_SEC)) {
            volleyClockSec = nowSec;
            robot.drive.holdPose(pose());   // resist being pushed (during() turns the robot onto the aim)
        }
        boolean clockExpired = volleyClockSec >= 0 && nowSec - volleyClockSec > RobotConfig.AUTO_VOLLEY_TIMEOUT_SEC;
        if (waitingForB) {
            if (robot.targetCell() == HiveState.B) {
                waitingForB = false;          // vision switched it (a TIP pause follows, then we fire)
                volleyPending = true;
                note = "volley 2: vision sees the second cell up";
            } else if (elapsed() > RobotConfig.AUTO_VOLLEY2_WAIT_FOR_B_SEC) {
                volleySkipNote = "volley 2 skipped: the HIVE tracker does not see the second cell up - keeping the balls";
                note = volleySkipNote;
                return true;
            } else {
                return false;
            }
        }
        if (volleyPending) {
            if (volleyClockSec >= 0 && robot.targetFacesAway()) {
                volleySkipNote = (step == Step.VOLLEY_1 ? "volley 1" : "volley 2")
                        + " skipped: the target cell faces away from the robot - keeping the balls";
                note = volleySkipNote;
                return true;
            }
            // A HIVE state change (e.g. "partner TIP seen" just before VOLLEY_2) starts a short TIP pause
            // that cancels firing - so ask for the volley only after the pause.
            if (!robot.hive.tipPauseActive()) {
                int minFeeds = (step == Step.VOLLEY_2 && plan.contains(Step.SWEEP_GARDEN))
                        ? RobotConfig.AUTO_VOLLEY2_MIN_FEEDS : 0;
                robot.carrier.requestVolley(minFeeds);
                volleyPending = false;
                volleyRequestedAtSec = nowSec;
            }
            return clockExpired;
        }
        if (!robot.carrier.isFiring() && nowSec - volleyRequestedAtSec > 0.2) return true;   // emptied, or cancelled
        return clockExpired;
    }

    private boolean pathFinished() {
        if (!pathRunning) return true;
        if (elapsed() > pathTimeoutSec) {
            note = String.format(Locale.US, "%s timed out after %.1f s - holding here", step, pathTimeoutSec);
            lastPathTimedOut = true;
            pathRunning = false;
            robot.drive.holdPose(pose());
            return true;
        }
        return elapsed() > 0.05 && !robot.drive.isBusy();
    }

    /** Vision must see the second cell up (or its own tracker switched to B) for HIVE_VISION_CONFIRM_MS. */
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
    // DURING: work done every loop while a step is running
    // ---------------------------------------------------------------------------------------------
    private void during(Step s) {
        if ((s == Step.VOLLEY_1 || s == Step.VOLLEY_2) && !skipVolley && volleyClockSec >= 0
                && robot.drive.hasLocalizer()) {
            // Fixed shooter: turn the robot until its back points at the target (off the wall if needed).
            Pose hold = AutoLegs.volleyAimHold(pose(), robot.aim(), nowSec - lastAimHoldSec);
            if (hold != null) {
                robot.drive.holdPose(hold);
                lastAimHoldSec = nowSec;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // EXIT: clean-up after a step
    // ---------------------------------------------------------------------------------------------
    private void exit(Step s) {
        switch (s) {
            case VOLLEY_1: {
                // With a feed beam, only CONFIRMED shots count toward the TIP (an unconfirmed feed may be a jam).
                int confirmed = robot.carrier.confirmedShots() - confirmedBeforeVolley;
                int countDrop = ballsBeforeVolley - robot.carrier.count();
                int fired = robot.carrier.hasFeedBeam() ? confirmed : countDrop;
                volley1Launched = Math.max(0, Math.max(confirmed, countDrop));
                robot.carrier.cancelFiring();
                if (fired >= RobotConfig.AUTO_ASSUME_TIP_AFTER_BALLS) {
                    // 3 own NECTAR start in this cell, so ~3 POLLEN tip it (EFSG 12): assume it tipped, and
                    // keep vision out while the start cell is still up during the dwell.
                    robot.hive.assume(HiveState.B, RobotConfig.AUTO_TIP_ASSUME_PROTECT_MS);
                }
                note = (volleySkipNote != null ? volleySkipNote + " | " : "") + "volley 1 fired " + Math.max(0, fired);
                break;
            }
            case SPILL_HOLD:
                if (sawStartCellUp && robot.vision.seesCellUp(HiveState.A)) {
                    // Still up after the whole hold: no TIP. TeleOp must start in state A.
                    robot.hive.setState(HiveState.A, HiveTracker.Source.AUTO);
                    note = "start cell still up after the hold: no TIP (HIVE state A)";
                } else {
                    note = "spill hold done (" + String.format(Locale.US, "%.1f", elapsed()) + " s)";
                }
                break;
            case VOLLEY_2:
                robot.carrier.cancelFiring();
                break;
            case WAIT_FOR_PARTNER_TIP: {
                boolean seen = partnerTipSeen() || robot.hive.state() == HiveState.B;
                if (seen) {
                    robot.hive.setState(HiveState.B, HiveTracker.Source.AUTO);
                    if (nowSec > RobotConfig.A3_ROLE2_LATEST_FIRE_SEC) {
                        skipVolley2 = true;
                        note = String.format(Locale.US, "partner TIP seen at %.1f s, after %.0f s: keeping the preloads",
                                nowSec, RobotConfig.A3_ROLE2_LATEST_FIRE_SEC);
                    } else {
                        note = String.format(Locale.US, "partner TIP seen at %.1f s", nowSec);
                    }
                } else if (RobotConfig.A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM && nowSec <= RobotConfig.A3_ROLE2_LATEST_FIRE_SEC + 0.5) {
                    robot.hive.setState(HiveState.B, HiveTracker.Source.AUTO);
                    note = "no TIP seen - firing anyway (A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM)";
                } else {
                    skipVolley2 = true;
                    note = "no TIP seen - keeping the preloads";
                }
                robot.setAimOverride(null);
                break;
            }
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

    /** Start the leg unless we are already there (Pedro does not like zero-length paths). */
    private void follow(AutoLegs.Leg leg) {
        if (!robot.drive.hasLocalizer()) {
            note = "no odometry: paths skipped";
            return;
        }
        Pose here = pose();
        double headingErrorDeg = Math.toDegrees(MathUtil.wrapRad(here.heading() - leg.end.heading()));
        if (here.distance(leg.end) < 1.0 && Math.abs(headingErrorDeg) < 3.0) return;
        AutoPaths.RoutePlan routePlan = leg.plan(here);
        Path path = AutoPaths.build(routePlan, here.heading(), leg.end.heading());
        if (leg.speedFraction > 0) path = AutoPaths.slow(path, leg.speedFraction);
        robot.drive.followPath(path);
        pathRunning = true;
        pathTimeoutSec = AutoPaths.timeoutSec(routePlan.lengthIn(), leg.speedFraction);
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

    public boolean isValid() {
        return valid;
    }

    /** The slot this run parks in. */
    public ParkSlot parkSlot() {
        return AutoLegs.parkSlot(type, parkChoice);
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
