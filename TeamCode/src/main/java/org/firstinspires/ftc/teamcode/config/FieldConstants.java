package org.firstinspires.ftc.teamcode.config;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * =================================================================================================
 *  BIOBUZZ field geometry for FTC 12303.
 * =================================================================================================
 *
 * FRAME (used by ALL 12303 code, including the Pedro follower):
 *   origin = centre of the field on top of the tiles; +X toward the AUDIENCE wall; +Y toward the BLUE
 *   wall; +Z up; heading 0 = facing the audience wall, counter-clockwise positive; INCHES.
 *   (ftc-docs field frame, "inverted" square layout - red alliance area on the audience's left, M p68.)
 *   Heading 90 = facing the blue wall, 180 = facing the far (scoring-table) wall, -90 = facing red.
 *
 *   If you want to draw a path on the Pedro visualizer website, convert with util/PedroFrame.
 *
 * WHERE THE NUMBERS COME FROM: the official field STEP CAD (research R2 + R2 verify, 2026-10-20),
 * which the manual says is authoritative (+-1 in). The real field is 141.3 in between walls, NOT 144.
 * Event fields vary by about +-1 in - re-measure the important ones on a practice field.
 *
 * ALLIANCES: every value below is written ONCE for RED. Blue = red rotated 180 deg about the centre:
 *   (x, y, heading) -> (-x, -y, heading + 180)   (Alliance.fromRed...). Never mirror.
 *   Example: red start S1 (61.67, -12.75, 180) -> blue S1 (-61.67, 12.75, 0).
 *
 * HIVE STATES (util/HiveState): A = match start (red AUDIENCE cell up, faces +X -> red shoots from the
 * audience half), B = after a TIP (red far/SCORING cell up, faces -X -> red shoots from the far half).
 */
public final class FieldConstants {
    private FieldConstants() {
    }

    // =============================================================================================
    // Field
    // =============================================================================================
    /** Inner face of the perimeter walls is at x, y = +-70.67 (CAD). */
    public static final double WALL = 70.67;
    /** Half of an 18 in robot: a robot touching a wall has its centre 61.67 from the field centre. */
    public static final double ROBOT_HALF_WIDTH = 9.0;
    /** AUTO side rule (G402, G304.A): red stays at y <= 0, blue at y >= 0. */
    public static final double CENTER_LINE_Y = 0.0;
    /**
     * Under-HIVE lane (robot <= 18 in tall): centre |y| <= ~9.5. TELEOP ONLY - a red robot in AUTO would
     * have to stay at y <= 0 with 0.25 in margin, and G409 (spill deflection) / G417 (HIVE contact) risks
     * are high. AUTO paths below use the corridor at y = -40 instead.
     */
    public static final double UNDER_HIVE_LANE_HALF_WIDTH = 9.5;

    // =============================================================================================
    // HIVE and aim points (red)
    // =============================================================================================
    /** Red HIVE pivot (x, y, z). Blue pivot = (0, +12.76, 43.95) in CAD; rotation gives +12.74. */
    public static final FieldPoint RED_HIVE_PIVOT = new FieldPoint(0.0, -12.74, 43.95);
    /**
     * Aim point in state A: centre of the red AUDIENCE cell opening (= SDK 12.0 AprilTag cluster origin
     * "RED AUDIENCE"). The opening faces +X (audience wall) and is tilted 30 deg up.
     */
    public static final FieldPoint RED_AIM_STATE_A = new FieldPoint(16.50, -12.74, 58.27);
    /** Aim point in state B: centre of the red SCORING (far) cell opening; faces -X, tilted 30 deg up. */
    public static final FieldPoint RED_AIM_STATE_B = new FieldPoint(-16.48, -12.74, 58.31);
    /** Shots more than ~40 deg off the opening's axis start to hit the cell side wall (R2 9, R2 verify F23). */
    public static final double GOOD_SHOT_MAX_OFF_AXIS_DEG = 40.0;

    /** Where to aim for our alliance in this HIVE state. */
    public static FieldPoint aimPoint(Alliance alliance, HiveState state) {
        return (state == HiveState.A ? RED_AIM_STATE_A : RED_AIM_STATE_B).forAlliance(alliance);
    }

    public static FieldPoint hivePivot(Alliance alliance) {
        return RED_HIVE_PIVOT.forAlliance(alliance);
    }

    /** Field heading (deg) the UP opening faces: red A = 0 (toward audience), red B = 180. */
    public static double openingFacingDeg(Alliance alliance, HiveState state) {
        double red = (state == HiveState.A) ? 0.0 : 180.0;
        return alliance.fromRedHeadingDeg(red);
    }

    /**
     * How far off the opening's axis a shot from (x, y) is, degrees (0 = straight in front).
     * More than ~40 = poor; more than 90 = behind the cell (cannot score).
     */
    public static double offAxisDeg(Alliance alliance, HiveState state, double x, double y) {
        FieldPoint aim = aimPoint(alliance, state);
        double fromOpeningToRobot = MathUtil.bearingDeg(aim.x, aim.y, x, y);
        return Math.abs(MathUtil.wrapDeg(fromOpeningToRobot - openingFacingDeg(alliance, state)));
    }

    // =============================================================================================
    // AprilTags (36h11, 3.25 in, 4 per cell on the cell's UNDERSIDE, facing down)
    // =============================================================================================
    // Red AUDIENCE cell (up in state A): ids 34-37. Red SCORING cell (up in state B): ids 30-33.
    // Blue: AUDIENCE 38-41 (up in state B), SCORING 42-45 (up in state A). Blue id = red id + 8 and its
    // position is the red one rotated 180 deg. CAD tag centres (R2 table 7.3):
    public static final int RED_CELL_A_FIRST_TAG = 34;
    public static final int RED_CELL_B_FIRST_TAG = 30;
    private static final double[] RED_CELL_A_TAG_Y = {-19.24, -15.49, -9.99, -6.24};   // ids 34, 35, 36, 37
    private static final double[] RED_CELL_B_TAG_Y = {-6.24, -9.99, -15.49, -19.24};   // ids 30, 31, 32, 33
    private static final double CELL_A_TAG_X_UP = 13.10, CELL_A_TAG_Z_UP = 49.80;
    private static final double CELL_A_TAG_X_DOWN = 11.58, CELL_A_TAG_Z_DOWN = 35.48;
    private static final double CELL_B_TAG_X_UP = -13.08, CELL_B_TAG_Z_UP = 49.84;
    private static final double CELL_B_TAG_X_DOWN = -11.61, CELL_B_TAG_Z_DOWN = 35.52;

    /**
     * Which of OUR cells a tag is on: HiveState.A = the cell that is up at the start, B = the other.
     * Returns null for tags that are not on our HIVE (opponent tags must be ignored: G417.D).
     */
    public static HiveState cellOfTag(Alliance alliance, int id) {
        int redId = (alliance == Alliance.RED) ? id : id - 8;
        if (redId >= RED_CELL_A_FIRST_TAG && redId < RED_CELL_A_FIRST_TAG + 4) return HiveState.A;
        if (redId >= RED_CELL_B_FIRST_TAG && redId < RED_CELL_B_FIRST_TAG + 4) return HiveState.B;
        return null;
    }

    /** Field position of one of our tags, with its cell up or down. Null if the tag is not ours. */
    public static FieldPoint tagPosition(Alliance alliance, int id, boolean cellUp) {
        HiveState cell = cellOfTag(alliance, id);
        if (cell == null) return null;
        int redId = (alliance == Alliance.RED) ? id : id - 8;
        FieldPoint red;
        if (cell == HiveState.A) {
            double y = RED_CELL_A_TAG_Y[redId - RED_CELL_A_FIRST_TAG];
            red = cellUp ? new FieldPoint(CELL_A_TAG_X_UP, y, CELL_A_TAG_Z_UP)
                    : new FieldPoint(CELL_A_TAG_X_DOWN, y, CELL_A_TAG_Z_DOWN);
        } else {
            double y = RED_CELL_B_TAG_Y[redId - RED_CELL_B_FIRST_TAG];
            red = cellUp ? new FieldPoint(CELL_B_TAG_X_UP, y, CELL_B_TAG_Z_UP)
                    : new FieldPoint(CELL_B_TAG_X_DOWN, y, CELL_B_TAG_Z_DOWN);
        }
        return red.forAlliance(alliance);
    }

    // =============================================================================================
    // Start spots (G304: fully on our side, touching a wall, not in the LOADING ZONE, not touching a
    // FLOWER, 4 POLLEN preloads). Centres of an 18 x 18 robot, RED. "facing" = the direction the
    // SHOOTER (turret centre) points; the robot heading is facing - TURRET_CENTER_DEG.
    // =============================================================================================
    public enum StartSpot {
        S1("Audience wall on red HIVE axis (col C) - shoots straight at the up cell", 61.67, -12.75, 180.0),
        S2("Audience wall, col B - 2nd shooter, 29 deg off-axis", 61.67, -37.50, 180.0),
        S3("Red wall, audience side (60 deg off-axis: hard shot)", 45.00, -61.67, 90.0),
        S4("Red wall centre, beside the HIVE (cannot shoot)", 0.00, -61.67, 90.0),
        S5("Far/red corner, next to the LOADING ZONE", -61.67, -61.67, 0.0),
        S6("Far wall on red HIVE axis - A3 role 2 (only 1.35 in from F-far flower!)", -61.67, -10.10, 0.0),
        S7("Far wall, col B", -61.67, -40.00, 0.0);

        public final String description;
        public final double redX;
        public final double redY;
        public final double redShooterFacingDeg;

        StartSpot(String description, double redX, double redY, double redShooterFacingDeg) {
            this.description = description;
            this.redX = redX;
            this.redY = redY;
            this.redShooterFacingDeg = redShooterFacingDeg;
        }
    }

    /** Robot start pose (FTC frame) for this alliance and start spot. */
    public static Pose startPose(Alliance alliance, StartSpot spot) {
        double robotHeadingRed = spot.redShooterFacingDeg - RobotConfig.TURRET_CENTER_DEG;
        return pose(alliance, spot.redX, spot.redY, robotHeadingRed);
    }

    // =============================================================================================
    // Shooting spots (RED). Robot heading is chosen so the turret CENTRE points at the aim point,
    // which leaves the most turret travel for correction.
    // =============================================================================================
    /** Zone A ("A2"): 31.5 in straight in front of the red audience-cell opening. */
    public static final double ZONE_A_SHOOT_X = 48.0, ZONE_A_SHOOT_Y = -12.75;
    /** Zone B ("B2"): straight in front of the red far-cell opening; 44 in from the LZ park pose. */
    public static final double ZONE_B_SHOOT_X = -48.0, ZONE_B_SHOOT_Y = -12.75;
    /**
     * Zone B spot for A3 role 1: 39.5 in from the far-cell opening, heading ~26 deg (turret centre on the
     * aim point), ~26 deg off-axis. Clear of the partner at S6 (its shot line is y ~ -10) and of the
     * TIP-2 spill landing box (x -42..-24, y -28..+2; +-10 in) - but still leave right after the
     * threshold ball (G409). Agree on it with your partner.
     */
    public static final double ZONE_B_SHOOT_ROLE1_X = -52.0, ZONE_B_SHOOT_ROLE1_Y = -30.0;
    /**
     * A3 role 1 lane point between the corridor and the role-1 spot / role-1 park slot. Keeps role 1 at
     * least 2 in clear of the role-2 robot parked at (-51, -56) (straight lines would clip its corner).
     */
    public static final double ROLE1_LANE_X = -29.0, ROLE1_LANE_Y = -35.0;

    /** Default shooting pose for a HIVE state (Zone A for state A, Zone B for state B). */
    public static Pose zoneShootPose(Alliance alliance, HiveState state) {
        if (state == HiveState.A) return shootPose(alliance, ZONE_A_SHOOT_X, ZONE_A_SHOOT_Y, HiveState.A);
        return shootPose(alliance, ZONE_B_SHOOT_X, ZONE_B_SHOOT_Y, HiveState.B);
    }

    /** A pose at the RED spot (redX, redY) facing so the turret centre points at the target cell. */
    public static Pose shootPose(Alliance alliance, double redX, double redY, HiveState target) {
        FieldPoint redAim = (target == HiveState.A) ? RED_AIM_STATE_A : RED_AIM_STATE_B;
        double redHeading = MathUtil.bearingDeg(redX, redY, redAim.x, redAim.y) - RobotConfig.TURRET_CENTER_DEG;
        return pose(alliance, redX, redY, redHeading);
    }

    // =============================================================================================
    // Corridor between the zones (RED). Stays out of the under-HIVE lane and the spill box.
    // =============================================================================================
    /** First waypoint after a Zone A volley: away from the spill landing box, toward the corridor. */
    public static final double SPILL_ESCAPE_X = 52.0, SPILL_ESCAPE_Y = -40.0;
    public static final double CORRIDOR_A_X = 30.0, CORRIDOR_A_Y = -40.0;
    public static final double CORRIDOR_B_X = -30.0, CORRIDOR_B_Y = -40.0;
    /**
     * Leaving the far wall (S6 / S7 area): first drive STRAIGHT along +X to this x at the same y, then
     * turn toward the park slot. A diagonal start from S6 (-61.67, -10.1) clips the F-far FLOWER, whose
     * footprint is x -70.65..-65.63, y -26.34..-20.45 (the robot may only drop 1.35 in before its edge
     * reaches the flower's y band, while it is still beside it).
     */
    public static final double FAR_WALL_ESCAPE_X = -50.0;

    // =============================================================================================
    // LOADING ZONE and PARK (RED)
    // =============================================================================================
    /** Red LOADING ZONE tape box (CAD). Event fields: only 11.0 in deep (FAC 3.4) -> y >= -59.67. */
    public static final double RED_LZ_MIN_X = -46.60, RED_LZ_MAX_X = -23.91;
    public static final double RED_LZ_MIN_Y = -70.67, RED_LZ_MAX_Y = -59.10;
    /**
     * Single-robot park: overlaps the LZ by ~4.9 in (PARK) while staying 6.7 in off the red wall.
     * Q&A 21: touching ANY wall at the end of AUTO means no LEAVE, even when parked.
     */
    public static final double PARK_X = -35.25, PARK_Y = -55.0;
    /** A3: two robots share the 22.7 in wide LZ. Each overlaps it ~4.6-12 in, 6 in apart, 5.7 in off the wall. */
    public static final double PARK_ROLE1_X = -27.0, PARK_ROLE1_Y = -56.0;
    public static final double PARK_ROLE2_X = -51.0, PARK_ROLE2_Y = -56.0;

    /**
     * Park pose at a RED spot. The heading is the robot's current heading rounded to the nearest
     * multiple of 90 deg: a square robot has a predictable footprint (LZ overlap, wall gap, room for the
     * partner), and it barely needs to turn on the way in. A robot turned ~45 deg reaches 12.7 in from its
     * centre instead of 9 in.
     */
    public static Pose parkPose(Alliance alliance, double redX, double redY, double currentHeadingRad) {
        double square = Math.round(Math.toDegrees(currentHeadingRad) / 90.0) * 90.0;
        return new Pose(alliance.fromRedX(redX), alliance.fromRedY(redY), Math.toRadians(square));
    }

    // =============================================================================================
    // GARDEN (RED): audience wall, red corner. 4 POLLEN at (69.3, -69.3 / -66.4 / -63.5 / -60.6).
    // The balls sit 1.4 in from the wall, so only the outer edge of a full-width intake reaches them.
    // =============================================================================================
    public static final double RED_GARDEN_MIN_X = 68.10, RED_GARDEN_MAX_X = 70.67;
    public static final double RED_GARDEN_MIN_Y = -70.10, RED_GARDEN_MAX_Y = -47.41;
    /** Sweep along the wall toward the red corner. TODO-MEASURE (x: intake edge must reach x ~ 69). */
    public static final double GARDEN_SWEEP_X = 60.5;                                              // TODO-MEASURE
    public static final double GARDEN_SWEEP_START_Y = -42.0;
    public static final double GARDEN_SWEEP_END_Y = -60.0;                                         // TODO-MEASURE
    /** Red sweep travels toward the red wall (-Y). */
    public static final double GARDEN_SWEEP_TRAVEL_DEG = -90.0;

    /**
     * Two waypoints between Zone A and the sweep start: the robot turns to face the sweep direction
     * between them, away from the audience wall (a turning 18 in robot reaches 12.7 in from its centre,
     * so it must not turn while hugging the wall).
     */
    public static final double GARDEN_APPROACH_1_X = 52.0, GARDEN_APPROACH_1_Y = -24.0;
    public static final double GARDEN_APPROACH_2_X = 50.0, GARDEN_APPROACH_2_Y = -38.0;

    public static List<Pose> gardenApproach(Alliance alliance) {
        List<Pose> points = new ArrayList<>();
        points.add(point(alliance, GARDEN_APPROACH_1_X, GARDEN_APPROACH_1_Y));
        points.add(point(alliance, GARDEN_APPROACH_2_X, GARDEN_APPROACH_2_Y));
        return points;
    }

    public static Pose gardenSweepStart(Alliance alliance) {
        return pose(alliance, GARDEN_SWEEP_X, GARDEN_SWEEP_START_Y, intakeLeadingHeadingRed());
    }

    public static Pose gardenSweepEnd(Alliance alliance) {
        return pose(alliance, GARDEN_SWEEP_X, GARDEN_SWEEP_END_Y, intakeLeadingHeadingRed());
    }

    private static double intakeLeadingHeadingRed() {
        return GARDEN_SWEEP_TRAVEL_DEG - RobotConfig.INTAKE_FACING_DEG;
    }

    // =============================================================================================
    // FLOWERS on the red half (opening centres, top ring at z = 21.40)
    // =============================================================================================
    public static final double F_RED_WALL_X = 23.39, F_RED_WALL_Y = -68.04;
    public static final double F_FAR_WALL_X = -68.04, F_FAR_WALL_Y = -23.39;

    // =============================================================================================
    // Spill landing boxes (RED, first floor contact, +-10 in). A TIP dumps the full cell out of its
    // opening toward the end wall it faced, ~3-6 s after the threshold ball. G409: do not catch or
    // deflect those balls before they touch something else - stay out of this box until they land.
    // =============================================================================================
    /** True if (x, y) is inside the landing box of a TIP that happened while the HIVE was in fromState. */
    public static boolean inSpillBox(Alliance alliance, HiveState fromState, double x, double y, double margin) {
        double rx = alliance.fromRedX(x);
        double ry = alliance.fromRedY(y);
        double minX = (fromState == HiveState.A) ? 24.0 : -42.0;
        double maxX = (fromState == HiveState.A) ? 42.0 : -24.0;
        return rx > minX - margin && rx < maxX + margin && ry > -28.0 - margin && ry < 2.0 + margin;
    }

    // =============================================================================================
    // Routes (waypoints in OUR alliance frame) used by the AUTO paths
    // =============================================================================================
    /** Waypoints from (x, y) to the LOADING ZONE that avoid the HIVE, the spill box and the F-far flower. */
    public static List<Pose> routeToLoadingZone(Alliance alliance, double x, double y) {
        double rx = alliance.fromRedX(x);
        double ry = alliance.fromRedY(y);
        List<Pose> points = new ArrayList<>();
        if (rx > 20.0) {
            if (rx > 44.0 && ry > -38.0) points.add(point(alliance, SPILL_ESCAPE_X, SPILL_ESCAPE_Y));
            points.add(point(alliance, CORRIDOR_A_X, CORRIDOR_A_Y));
            points.add(point(alliance, CORRIDOR_B_X, CORRIDOR_B_Y));
        } else if (rx < -52.0 && ry > -36.0) {
            points.add(point(alliance, FAR_WALL_ESCAPE_X, ry));   // straight out along +X first (F-far flower)
        }
        return points;
    }

    /** A3 role 1: lane point that keeps us clear of the role-2 partner parked at (-51, -56). */
    public static Pose role1Lane(Alliance alliance) {
        return point(alliance, ROLE1_LANE_X, ROLE1_LANE_Y);
    }

    /** Waypoints from (x, y) in the audience half to a Zone B spot, through the corridor. */
    public static List<Pose> routeToZoneB(Alliance alliance, double x, double y) {
        double rx = alliance.fromRedX(x);
        double ry = alliance.fromRedY(y);
        List<Pose> points = new ArrayList<>();
        if (rx > 20.0) {
            if (rx > 44.0 && ry > -38.0) points.add(point(alliance, SPILL_ESCAPE_X, SPILL_ESCAPE_Y));
            points.add(point(alliance, CORRIDOR_A_X, CORRIDOR_A_Y));
            points.add(point(alliance, CORRIDOR_B_X, CORRIDOR_B_Y));
        } else if (rx > -20.0) {
            points.add(point(alliance, CORRIDOR_B_X, CORRIDOR_B_Y));
        }
        return points;
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================
    /** Pose for this alliance from RED values (x, y in inches, heading in DEGREES). */
    public static Pose pose(Alliance alliance, double redX, double redY, double redHeadingDeg) {
        return new Pose(alliance.fromRedX(redX), alliance.fromRedY(redY),
                Math.toRadians(alliance.fromRedHeadingDeg(redHeadingDeg)));
    }

    /** Waypoint (heading unused) for this alliance from RED values. */
    public static Pose point(Alliance alliance, double redX, double redY) {
        return new Pose(alliance.fromRedX(redX), alliance.fromRedY(redY), 0.0);
    }

    /** Human-readable heading: which wall the robot FRONT faces. */
    public static String frontFacesText(double headingDeg) {
        double h = MathUtil.wrapDeg(headingDeg);
        if (Math.abs(h) <= 20) return "front faces AUDIENCE wall";
        if (Math.abs(h - 90) <= 20) return "front faces BLUE wall";
        if (Math.abs(Math.abs(h) - 180) <= 20) return "front faces FAR wall";
        if (Math.abs(h + 90) <= 20) return "front faces RED wall";
        return String.format(java.util.Locale.US, "heading %.0f deg", h);
    }
}
