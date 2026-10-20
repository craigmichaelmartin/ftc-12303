package org.firstinspires.ftc.teamcode.util;

/**
 * Which alliance we are playing on this match.
 *
 * BIOBUZZ field frame (used everywhere in this code, see config/FieldConstants):
 *   origin = field centre, +X toward the AUDIENCE wall, +Y toward the BLUE wall, heading 0 = facing
 *   the audience, counter-clockwise positive.
 *
 * The BIOBUZZ field is 180-degree ROTATIONALLY symmetric, NOT mirrored (R2 section 8): the blue
 * loading zone, garden, flowers, HIVE start state and tag layout are the red ones rotated about the
 * field centre. So we write each field constant ONCE for red and convert it for blue with:
 *
 *     (x, y, heading)  ->  (-x, -y, heading + 180)
 *
 * Never mirror (flip one axis) - that puts the robot on the wrong side of the HIVE.
 */
public enum Alliance {
    RED, BLUE;

    public Alliance opponent() {
        return this == RED ? BLUE : RED;
    }

    /** Convert an X coordinate written for RED into this alliance's X. */
    public double fromRedX(double redX) {
        return this == RED ? redX : -redX;
    }

    /** Convert a Y coordinate written for RED into this alliance's Y. */
    public double fromRedY(double redY) {
        return this == RED ? redY : -redY;
    }

    /** Convert a heading (degrees) written for RED into this alliance's heading. */
    public double fromRedHeadingDeg(double redHeadingDeg) {
        return this == RED ? redHeadingDeg : MathUtil.wrapDeg(redHeadingDeg + 180.0);
    }

    /**
     * The field heading (degrees) our drivers look along from their ALLIANCE AREA.
     * RED drivers stand behind the red wall (y = -71) and look toward +Y  ->  +90.
     * BLUE drivers stand behind the blue wall (y = +71) and look toward -Y ->  -90.
     * Field-centric driving uses this so "stick up" always means "away from me".
     */
    public double driverForwardHeadingDeg() {
        return this == RED ? 90.0 : -90.0;
    }

    /** True if this AprilTag id is on one of OUR two HIVE CELLS (red 30-37, blue 38-45). */
    public boolean ownsTag(int id) {
        return this == RED ? (id >= 30 && id <= 37) : (id >= 38 && id <= 45);
    }

    /** True if this is the colour of NECTAR we must never control (G408). */
    public boolean isOpponentColor(BallColor color) {
        return (this == RED && color == BallColor.BLUE) || (this == BLUE && color == BallColor.RED);
    }

    /** True if this is our own NECTAR colour (legal to hold and launch). */
    public boolean isOwnColor(BallColor color) {
        return (this == RED && color == BallColor.RED) || (this == BLUE && color == BallColor.BLUE);
    }
}
