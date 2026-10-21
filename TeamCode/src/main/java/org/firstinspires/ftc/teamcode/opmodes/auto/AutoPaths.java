package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.api.Paths;
import com.pedropathing.config.ConfigVar;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Turns our FieldConstants poses into Pedro Pathing 3 paths.
 *
 * Pedro 3 path API used here (com.pedropathing.api.Paths): Paths.line(a, b) makes a straight piece,
 * Paths.path(p1, p2, ...) joins pieces, .constant(h) keeps a heading and .linear(h1, h2) turns smoothly
 * (the short way). IMPORTANT: a Pedro 3 path has NO default heading - always set one, or it throws.
 *
 * TURN ONLY IN OPEN SPACE: an 18 in robot turned 45 deg reaches 12.7 in from its centre instead of 9 in,
 * so a robot that turns while it is next to a wall, a FLOWER or a partner can hit it. Every route here
 * therefore:
 *   - drives the FIRST straight piece at its starting heading (it usually starts against a wall),
 *   - does the whole turn on the LONGEST middle piece (normally the open corridor),
 *   - drives the LAST piece already at its final heading.
 * A route with no waypoints gets an extra point 35% of the way along, so it still drives straight off the
 * wall before turning. One exception, routeTurningFirst(), turns on the FIRST piece: A3 role 1 leaves its
 * Zone B spot (in open space) that way. All poses are in our FTC field frame (config/FieldConstants).
 *
 * The JUnit test TeamCode/src/test/.../RouteSweepTest sweeps exactly these plans for every routine.
 */
public final class AutoPaths {
    private AutoPaths() {
    }

    /** The straight pieces of a route and the index of the piece where the robot turns. */
    public static final class RoutePlan {
        /** from, waypoints..., to (headings of the middle points are ignored). */
        public final List<Pose> points;
        /** Index of the piece (points[i] -> points[i+1]) on which the heading changes. */
        public final int turnPiece;

        RoutePlan(List<Pose> points, int turnPiece) {
            this.points = Collections.unmodifiableList(points);
            this.turnPiece = turnPiece;
        }

        public int pieces() {
            return points.size() - 1;
        }

        /** Total length of the straight pieces, inches. */
        public double lengthIn() {
            double total = 0.0;
            for (int i = 0; i < pieces(); i++) total += points.get(i).distance(points.get(i + 1));
            return total;
        }
    }

    /** Work out the pieces of a route (pure math - RouteSweepTest sweeps exactly this). */
    public static RoutePlan plan(Pose from, List<Pose> waypoints, Pose to) {
        return plan(from, waypoints, to, false);
    }

    /** Same, optionally turning on the first piece instead of the longest middle piece. */
    public static RoutePlan plan(Pose from, List<Pose> waypoints, Pose to, boolean turnFirst) {
        List<Pose> pts = new ArrayList<>();
        pts.add(from);
        for (Pose w : waypoints) {
            if (w.distance(pts.get(pts.size() - 1)) > 1.0) pts.add(w);   // skip tiny pieces
        }
        if (pts.size() > 1 && to.distance(pts.get(pts.size() - 1)) <= 1.0) {
            pts.set(pts.size() - 1, to);       // last waypoint ~ end: just end there
        } else {
            pts.add(to);
        }
        if (pts.size() == 2) {
            // Single straight line: add a point 35% along so we leave the wall before turning.
            Pose a = pts.get(0), b = pts.get(1);
            pts.add(1, new Pose(a.x() + 0.35 * (b.x() - a.x()), a.y() + 0.35 * (b.y() - a.y()), 0.0));
        }
        int pieces = pts.size() - 1;
        int turn = 1;
        if (turnFirst) {
            turn = 0;
        } else if (pieces >= 3) {
            double longest = -1.0;
            for (int i = 1; i <= pieces - 2; i++) {
                double length = pts.get(i).distance(pts.get(i + 1));
                if (length > longest) {
                    longest = length;
                    turn = i;
                }
            }
        }
        return new RoutePlan(pts, turn);
    }

    /** Pedro path for a planned route: start heading, turn on plan.turnPiece, end heading. */
    public static Path build(RoutePlan plan, double fromHeading, double toHeading) {
        Path[] pieces = new Path[plan.pieces()];
        for (int i = 0; i < pieces.length; i++) {
            Path line = Paths.line(plan.points.get(i), plan.points.get(i + 1));
            if (i < plan.turnPiece) pieces[i] = line.constant(fromHeading);
            else if (i == plan.turnPiece) pieces[i] = line.linear(fromHeading, toHeading);
            else pieces[i] = line.constant(toHeading);
        }
        return Paths.path(pieces);
    }

    /** Pedro path for a route: start heading, turn on the longest middle piece, end heading. */
    public static Path route(Pose from, List<Pose> waypoints, Pose to) {
        return build(plan(from, waypoints, to), from.heading(), to.heading());
    }

    /** Straight route (no waypoints) - still leaves the wall before turning. */
    public static Path line(Pose from, Pose to) {
        return route(from, new ArrayList<Pose>(), to);
    }

    /** Same path, but limited to this fraction of full speed (e.g. 0.3 while sweeping the garden). */
    public static Path slow(Path path, double speedFraction) {
        return path.with(Constants.foresightConfig.maxPathSpeed.at(speedFraction));
    }

    /**
     * Expected cruise speed (in/s) of a path at this speed fraction (<= 0 = the configured maxPathSpeed):
     * fraction x the slower of Pedro's maxAchievableForward/StrafeVelocity.
     */
    public static double cruiseSpeedInPerSec(double speedFraction) {
        double fraction = speedFraction > 0 ? speedFraction : value(Constants.foresightConfig.maxPathSpeed, 1.0);
        double forward = value(Constants.foresightConfig.maxAchievableForwardVelocity, 40.0);
        double strafe = value(Constants.foresightConfig.maxAchievableStrafeVelocity, 30.0);
        return Math.max(1.0, fraction * Math.min(forward, strafe));
    }

    /** A Pedro config value, or the fallback if it is not set. */
    private static double value(ConfigVar<Double> v, double fallback) {
        try {
            Double d = v.get();
            return d != null ? d : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    /**
     * Path timeout for a leg (D13): length / (AUTO_PATH_TIMEOUT_SPEED_FACTOR x cruise speed) + margin, so the
     * shipped 0.5 speed cap never aborts a normal leg.
     */
    public static double timeoutSec(double lengthIn, double speedFraction) {
        return lengthIn / (RobotConfig.AUTO_PATH_TIMEOUT_SPEED_FACTOR * cruiseSpeedInPerSec(speedFraction))
                + RobotConfig.AUTO_PATH_TIMEOUT_MARGIN_SEC;
    }
}
