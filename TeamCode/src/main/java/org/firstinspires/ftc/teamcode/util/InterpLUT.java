package org.firstinspires.ftc.teamcode.util;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Interpolating look-up table: give it measured (x, y) pairs, ask for any x, get a y on the straight
 * line between the two nearest points. Outside the measured range it returns the end value
 * (it does NOT extrapolate - shooting outside the measured range is a guess anyway).
 *
 * We use it for "horizontal distance to the CELL opening (in) -> flywheel RPM". Fill it from the
 * ShotMapLogger OpMode (07 testing plan, Exp 2b: the shot map).
 *
 * No FTC imports: can be unit-tested on a laptop.
 */
public class InterpLUT {
    private final TreeMap<Double, Double> points = new TreeMap<>();

    /** Add one measured point. Adding the same x twice replaces the old y. */
    public InterpLUT add(double x, double y) {
        points.put(x, y);
        return this;
    }

    /** Build a table from rows of {x, y}. */
    public static InterpLUT fromRows(double[][] rows) {
        InterpLUT lut = new InterpLUT();
        for (double[] row : rows) {
            if (row != null && row.length >= 2) lut.add(row[0], row[1]);
        }
        return lut;
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public double minX() {
        return points.isEmpty() ? 0 : points.firstKey();
    }

    public double maxX() {
        return points.isEmpty() ? 0 : points.lastKey();
    }

    /** Interpolated value at x (clamped to the end values). Returns 0 for an empty table. */
    public double get(double x) {
        if (points.isEmpty()) return 0.0;
        Map.Entry<Double, Double> low = points.floorEntry(x);
        Map.Entry<Double, Double> high = points.ceilingEntry(x);
        if (low == null) return high.getValue();
        if (high == null) return low.getValue();
        double x0 = low.getKey();
        double x1 = high.getKey();
        if (x1 == x0) return low.getValue();
        double t = (x - x0) / (x1 - x0);
        return low.getValue() + t * (high.getValue() - low.getValue());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Double, Double> e : points.entrySet()) {
            sb.append(String.format(Locale.US, "{%.0f, %.0f} ", e.getKey(), e.getValue()));
        }
        return sb.toString().trim();
    }
}
