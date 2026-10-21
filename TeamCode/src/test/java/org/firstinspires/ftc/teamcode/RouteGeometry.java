package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.opmodes.auto.AutoPaths;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.MathUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Geometry for the route tests: an 18 x 18 in robot (rotated square) swept along a route exactly as
 * AutoPaths drives it (constant start heading before the turn piece, linear turn on it, constant end
 * heading after it), and polygon clearances (exact distance when apart, minus the overlap depth when
 * they overlap). Everything is checked in the RED frame: blue poses are rotated 180 deg back first,
 * so a blue route passes only if it is the exact rotation of a good red route.
 */
public final class RouteGeometry {
    private RouteGeometry() {
    }

    public static final double WALL = 70.67;
    public static final double HALF = 9.0;

    /** One swept robot position (red frame, heading in degrees) and the distance along the route. */
    public static final class Sample {
        public final double s, x, y, hDeg;

        Sample(double s, double x, double y, double hDeg) {
            this.s = s;
            this.x = x;
            this.y = y;
            this.hDeg = hDeg;
        }

        public double[][] footprint() {
            return square(x, y, hDeg, HALF);
        }

        @Override
        public String toString() {
            return String.format(java.util.Locale.US, "(%.1f, %.1f, %.0f deg) at s=%.1f in", x, y, hDeg, s);
        }
    }

    public static double[][] square(double cx, double cy, double hDeg, double half) {
        double h = Math.toRadians(hDeg), c = Math.cos(h), s = Math.sin(h);
        double[][] loc = {{half, half}, {-half, half}, {-half, -half}, {half, -half}};
        double[][] out = new double[4][2];
        for (int i = 0; i < 4; i++) {
            out[i][0] = cx + loc[i][0] * c - loc[i][1] * s;
            out[i][1] = cy + loc[i][0] * s + loc[i][1] * c;
        }
        return out;
    }

    public static double[][] robotAt(Pose redPose) {
        return square(redPose.x(), redPose.y(), Math.toDegrees(redPose.heading()), HALF);
    }

    public static double[][] box(double x0, double x1, double y0, double y1) {
        return new double[][]{{x0, y0}, {x1, y0}, {x1, y1}, {x0, y1}};
    }

    /** Segment a-b inflated by r (a rectangle with r of end extension). */
    public static double[][] segment(double ax, double ay, double bx, double by, double r) {
        double dx = bx - ax, dy = by - ay, len = Math.hypot(dx, dy);
        double ux = dx / len, uy = dy / len, nx = -uy, ny = ux;
        double a2x = ax - ux * r, a2y = ay - uy * r, b2x = bx + ux * r, b2y = by + uy * r;
        return new double[][]{{a2x + nx * r, a2y + ny * r}, {b2x + nx * r, b2y + ny * r},
                {b2x - nx * r, b2y - ny * r}, {a2x - nx * r, a2y - ny * r}};
    }

    /** Field obstacles for an 18 in tall robot (R2 section 10), red frame. */
    public static Map<String, double[][]> fieldObstacles() {
        Map<String, double[][]> m = new LinkedHashMap<>();
        m.put("F-far", box(-70.65, -65.63, -26.34, -20.45));
        m.put("F-red", box(20.45, 26.33, -70.67, -65.63));
        m.put("F-aud", box(65.63, 70.65, 20.45, 26.34));
        m.put("F-blue", box(-26.33, -20.45, 65.63, 70.67));
        m.put("HIVE foot +x-y", box(17.2, 19.48, -24.64, -22.77));
        m.put("HIVE foot -x-y", box(-19.48, -17.2, -24.65, -22.77));
        m.put("HIVE foot +x+y", box(17.2, 19.48, 22.79, 24.65));
        m.put("HIVE foot -x+y", box(-19.48, -17.2, 22.79, 24.65));
        m.put("HIVE leg +x-y", segment(18.4, -23.6, 10.7, -19.0, 1.0));
        m.put("HIVE leg -x-y", segment(-18.4, -23.6, -10.7, -19.0, 1.0));
        m.put("HIVE leg +x+y", segment(18.4, 23.6, 10.7, 19.0, 1.0));
        m.put("HIVE leg -x+y", segment(-18.4, 23.6, -10.7, 19.0, 1.0));
        return m;
    }

    public static final double[][] TIP1_BOX = box(24, 42, -28, 2);
    public static final double[][] TIP2_BOX = box(-42, -24, -28, 2);

    /** Pose in the red frame (blue = rotate 180 deg back). */
    public static Pose toRed(Alliance a, Pose p) {
        if (a == Alliance.RED) return p;
        return new Pose(-p.x(), -p.y(), MathUtil.wrapRad(p.heading() + Math.PI));
    }

    /** Sweep a planned route (poses in the alliance's frame) and return the samples in the RED frame. */
    public static List<Sample> sweep(Alliance a, AutoPaths.RoutePlan plan, double fromHeading, double toHeading) {
        List<Sample> out = new ArrayList<>();
        double h0 = Math.toDegrees(fromHeading);
        double dh = MathUtil.wrapDeg(Math.toDegrees(toHeading) - h0);
        double s0 = 0.0;
        for (int i = 0; i < plan.pieces(); i++) {
            Pose pa = plan.points.get(i), pb = plan.points.get(i + 1);
            double len = pa.distance(pb);
            int n = Math.max(1, (int) Math.ceil(len / 0.25));
            for (int k = 0; k <= n; k++) {
                double f = (double) k / n;
                double x = pa.x() + (pb.x() - pa.x()) * f, y = pa.y() + (pb.y() - pa.y()) * f;
                double h = i < plan.turnPiece ? h0 : (i == plan.turnPiece ? h0 + dh * f : h0 + dh);
                Pose red = toRed(a, new Pose(x, y, Math.toRadians(h)));
                out.add(new Sample(s0 + len * f, red.x(), red.y(), Math.toDegrees(red.heading())));
            }
            s0 += len;
        }
        return out;
    }

    // ---------------------------------------------------------------------------------------------
    // Polygon clearance (separating axis + exact distance)
    // ---------------------------------------------------------------------------------------------
    /** Exact distance between convex polygons, or minus the overlap depth when they overlap. */
    public static double clearance(double[][] a, double[][] b) {
        double best = -1e9;
        for (double[] axis : axes(a, b)) {
            double[] pa = project(a, axis), pb = project(b, axis);
            best = Math.max(best, Math.max(pb[0] - pa[1], pa[0] - pb[1]));
        }
        if (best <= 0) return best;
        double d = 1e9;
        for (double[] p : a) for (int i = 0; i < b.length; i++) d = Math.min(d, segDist(p, b[i], b[(i + 1) % b.length]));
        for (double[] p : b) for (int i = 0; i < a.length; i++) d = Math.min(d, segDist(p, a[i], a[(i + 1) % a.length]));
        return d;
    }

    private static List<double[]> axes(double[][] a, double[][] b) {
        List<double[]> out = new ArrayList<>();
        for (double[][] poly : new double[][][]{a, b}) {
            for (int i = 0; i < poly.length; i++) {
                double ex = poly[(i + 1) % poly.length][0] - poly[i][0];
                double ey = poly[(i + 1) % poly.length][1] - poly[i][1];
                double len = Math.hypot(ex, ey);
                out.add(new double[]{-ey / len, ex / len});
            }
        }
        return out;
    }

    private static double[] project(double[][] poly, double[] axis) {
        double min = 1e18, max = -1e18;
        for (double[] p : poly) {
            double v = p[0] * axis[0] + p[1] * axis[1];
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return new double[]{min, max};
    }

    private static double segDist(double[] p, double[] a, double[] b) {
        double dx = b[0] - a[0], dy = b[1] - a[1], len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / len2));
        return Math.hypot(p[0] - a[0] - t * dx, p[1] - a[1] - t * dy);
    }

    /** Smallest gap between the footprint and the perimeter walls (negative = through a wall). */
    public static double wallGap(double[][] r) {
        double g = 1e9;
        for (double[] p : r) g = Math.min(g, Math.min(WALL - Math.abs(p[0]), WALL - Math.abs(p[1])));
        return g;
    }

    /** Highest y of the footprint (red must stay at y <= 0 in AUTO). */
    public static double maxY(double[][] r) {
        double m = -1e9;
        for (double[] p : r) m = Math.max(m, p[1]);
        return m;
    }
}
