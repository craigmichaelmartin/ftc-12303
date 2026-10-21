package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every AUTO drive ("leg") as pure geometry: waypoints + end pose (+ slow speed / turn-first flags).
 * AutoRoutine drives exactly these legs, and the JUnit RouteSweepTest sweeps exactly these legs for both
 * alliances, every routine, start and park choice - so a route change here is checked before it reaches
 * the field. No hardware in this class.
 */
public final class AutoLegs {
    private AutoLegs() {
    }

    /** One drive: from wherever the robot is, through the waypoints, to the end pose. */
    public static final class Leg {
        public final List<Pose> waypoints;
        public final Pose end;
        /** Fraction of full path speed for this leg, or 0 for the normal speed. */
        public final double speedFraction;
        /** Turn on the first piece (only used where the start is in open space). */
        public final boolean turnFirst;

        Leg(List<Pose> waypoints, Pose end, double speedFraction, boolean turnFirst) {
            this.waypoints = Collections.unmodifiableList(new ArrayList<>(waypoints));
            this.end = end;
            this.speedFraction = speedFraction;
            this.turnFirst = turnFirst;
        }

        /** The straight pieces the robot drives from this pose. */
        public AutoPaths.RoutePlan plan(Pose from) {
            return AutoPaths.plan(from, waypoints, end, turnFirst);
        }
    }

    private static Leg leg(List<Pose> waypoints, Pose end) {
        return new Leg(waypoints, end, 0.0, false);
    }

    /** Volley-1 drive for shoot-first routines: null = fire from the start spot (S1). */
    public static Leg toShoot1(Alliance alliance, StartSpot start) {
        if (start == StartSpot.S1) return null;
        return leg(FieldConstants.routeToZoneASide(alliance, start), FieldConstants.zoneASideShootPose(alliance));
    }

    /** A2 / A3 role 1: from the volley-1 pose to the garden sweep start. */
    public static Leg toGarden(Alliance alliance) {
        return leg(FieldConstants.gardenApproach(alliance), FieldConstants.gardenSweepStart(alliance));
    }

    /** The slow garden sweep along the wall. */
    public static Leg sweep(Alliance alliance) {
        return new Leg(new ArrayList<Pose>(), FieldConstants.gardenSweepEnd(alliance), RobotConfig.AUTO_SWEEP_SPEED, false);
    }

    /** A2: through the corridor to B2. */
    public static Leg toZoneB(Alliance alliance, Pose here) {
        return leg(FieldConstants.routeToZoneB(alliance, here.x(), here.y()), FieldConstants.zoneBShootPose(alliance));
    }

    /** A3 role 1: through the corridor to the wait point at its end, square heading. */
    public static Leg toRole1Wait(Alliance alliance, Pose here) {
        return leg(FieldConstants.routeToZoneB(alliance, here.x(), here.y()), role1WaitPose(alliance));
    }

    /** The A3 role-1 wait pose: corridor end, heading = the square heading nearest the role-1 shot heading. */
    public static Pose role1WaitPose(Alliance alliance) {
        double square = FieldConstants.squareHeadingRad(FieldConstants.role1ShootPose(alliance).heading());
        return new Pose(alliance.fromRedX(FieldConstants.A3_ROLE1_WAIT_X), alliance.fromRedY(FieldConstants.A3_ROLE1_WAIT_Y), square);
    }

    /** A3 role 1 after the wait: lane, then its Zone B spot. */
    public static Leg toRole1Spot(Alliance alliance) {
        List<Pose> w = new ArrayList<>();
        w.add(FieldConstants.innerLane(alliance));
        return leg(w, FieldConstants.role1ShootPose(alliance));
    }

    /** The slot this routine parks in: A3 roles have their own, everything else uses the INIT choice. */
    public static ParkSlot parkSlot(AutoRoutineType type, ParkSlot choice) {
        ParkSlot fixed = type.fixedParkSlot();
        if (fixed != null) return fixed;
        return choice != null ? choice : ParkSlot.INNER;
    }

    /** True when A3 role 1 is (about) at its Zone B spot and must use its special exit. */
    static boolean atRole1Spot(Alliance alliance, Pose here) {
        return here.distance(FieldConstants.role1ShootPose(alliance)) < 6.0;
    }

    /** Drive into the LOADING ZONE from wherever the robot is. */
    public static Leg toPark(Alliance alliance, AutoRoutineType type, ParkSlot choice, Pose here) {
        ParkSlot slot = parkSlot(type, choice);
        Pose park = FieldConstants.parkPose(alliance, slot, here.heading());
        if (type == AutoRoutineType.A3_ROLE1 && slot == ParkSlot.INNER && atRole1Spot(alliance, here)) {
            // Turn square while dropping straight down, then skim the bottom edge of the TIP-2 landing box.
            return new Leg(FieldConstants.role1ExitToInner(alliance), park, 0.0, true);
        }
        return leg(FieldConstants.routeToLoadingZone(alliance, here.x(), here.y(), slot), park);
    }

    /**
     * Pinned / missing turret: the pose to hold so the CHASSIS points the shooter at the target - the
     * current pose turned by turnDeg, moved off any wall the turned robot would otherwise touch.
     */
    public static Pose chassisAimPose(Pose here, double turnDeg) {
        double h = here.heading() + Math.toRadians(turnDeg);
        double reach = FieldConstants.ROBOT_HALF_WIDTH * (Math.abs(Math.cos(h)) + Math.abs(Math.sin(h)));
        double limit = FieldConstants.WALL - reach - 0.5;
        return new Pose(MathUtil.clamp(here.x(), -limit, limit), MathUtil.clamp(here.y(), -limit, limit), h);
    }
}
