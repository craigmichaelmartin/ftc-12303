package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.api.Paths;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

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
 * wall before turning. All poses are in our FTC field frame (config/FieldConstants).
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
    }

    /** Work out the pieces of a route (pure math - the RouteCheck test sweeps exactly this). */
    public static RoutePlan plan(Pose from, List<Pose> waypoints, Pose to) {
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
        if (pieces >= 3) {
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

    /** Pedro path for a route: start heading, turn on the longest middle piece, end heading. */
    public static Path route(Pose from, List<Pose> waypoints, Pose to) {
        RoutePlan plan = plan(from, waypoints, to);
        Path[] pieces = new Path[plan.pieces()];
        for (int i = 0; i < pieces.length; i++) {
            Path line = Paths.line(plan.points.get(i), plan.points.get(i + 1));
            if (i < plan.turnPiece) pieces[i] = line.constant(from.heading());
            else if (i == plan.turnPiece) pieces[i] = line.linear(from.heading(), to.heading());
            else pieces[i] = line.constant(to.heading());
        }
        return Paths.path(pieces);
    }

    /** Straight route (no waypoints) - still leaves the wall before turning. */
    public static Path line(Pose from, Pose to) {
        return route(from, new ArrayList<Pose>(), to);
    }

    /** Same path, but limited to this fraction of full speed (e.g. 0.3 while sweeping the garden). */
    public static Path slow(Path path, double speedFraction) {
        return path.with(Constants.foresightConfig.maxPathSpeed.at(speedFraction));
    }
}
