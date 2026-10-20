package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.math.Pose;

/**
 * Converting between OUR field frame and the Pedro Pathing "corner" frame.
 *
 * READ THIS FIRST:
 *  - All 12303 code (FieldConstants, AUTO, aiming) uses the FTC BIOBUZZ frame: origin at the field
 *    centre, +X toward the AUDIENCE wall, +Y toward the BLUE wall, heading 0 = facing the audience,
 *    counter-clockwise positive, inches.
 *  - Our Pedro follower runs DIRECTLY in that frame. Pedro does not care where the origin is: we give
 *    the Pinpoint our start pose with follower.setPose(...) and every path uses our coordinates.
 *    So you do NOT need this class in robot code.
 *  - You only need it when you draw paths on the Pedro visualizer website
 *    (visualizer.pedropathing.com). That site uses a 144 x 144 field with the origin in a corner.
 *    With Pedro's documented conversion (pedropathing.com/docs/pathing/reference/coordinates):
 *
 *        x_pedro = y_ftc + 72        y_pedro = 72 - x_ftc        heading_pedro = heading_ftc - 90 deg
 *
 *    In BIOBUZZ that puts the Pedro origin in the RED / AUDIENCE corner, Pedro +x toward the blue wall
 *    and Pedro +y toward the far wall - the same picture as the manual's top views (audience at the
 *    bottom, red on the left). The real field is 141.3 in between walls (walls at +-70.67), so in Pedro
 *    coordinates the walls are at 1.33 and 142.67, not 0 and 144.
 */
public final class PedroFrame {
    private PedroFrame() {
    }

    /** Our FTC-frame pose -> Pedro visualizer (corner) pose. */
    public static Pose toVisualizer(Pose ftc) {
        return new Pose(ftc.y() + 72.0, 72.0 - ftc.x(), ftc.heading() - Math.PI / 2.0);
    }

    /** Pedro visualizer (corner) pose -> our FTC-frame pose. Inverse of toVisualizer. */
    public static Pose fromVisualizer(Pose pedro) {
        return new Pose(72.0 - pedro.y(), pedro.x() - 72.0, pedro.heading() + Math.PI / 2.0);
    }
}
