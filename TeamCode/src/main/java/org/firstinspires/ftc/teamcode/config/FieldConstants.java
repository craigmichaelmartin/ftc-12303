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
 *   Example: red start S1 (61.67, -12.75, 0) -> blue S1 (-61.67, 12.75, 180).
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
     * are high. AUTO paths below use the corridor (30, -42) -> (-30, -40) instead.
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
    /** The up cell's opening is a pentagon about 20 in wide x 14 in tall (M p.71-72). The aim tolerance uses it. */
    public static final double CELL_OPENING_WIDTH_IN = 20.0;
    /** POLLEN is a 2.8 in ball. */
    public static final double POLLEN_RADIUS_IN = 1.4;

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
    // SHOOTER points, away from the wall; the robot heading is facing - SHOOTER_FACING_DEG, so at every
    // spot the FRONT (intake) is against the wall and the back (shooter) faces into the field. Every spot
    // is SQUARE to its wall (red S2 heading 0, S7 heading 180): the 151 / 31 deg values in older notes are
    // aim bearings, not placement headings - an 18 in robot turned ~30 deg cannot touch the wall there.
    // "Our start wall" = the end wall behind S1: red = AUDIENCE wall, blue = FAR wall.
    // =============================================================================================
    public enum StartSpot {
        S1("Audience wall on the red HIVE axis (col C): front (intake) against the wall, back toward the HIVE - shoots straight at the up cell",
                "Far wall on the blue HIVE axis (col D): front (intake) against the wall, back toward the HIVE - shoots straight at the up cell",
                61.67, -12.75, 180.0),
        S2("Audience wall, col B: front (intake) against the wall - shoot-first routines drive to the Zone A side spot to fire",
                "Far wall, col E: front (intake) against the wall - shoot-first routines drive to the Zone A side spot to fire",
                61.67, -37.50, 180.0),
        S3("Red wall, audience side: front (intake) against the wall - shoot-first routines drive to the Zone A side spot",
                "Blue wall, far side: front (intake) against the wall - shoot-first routines drive to the Zone A side spot",
                45.00, -61.67, 90.0),
        S4("Red wall centre, beside the HIVE: front (intake) against the wall - shoot-first routines drive to the Zone A side spot",
                "Blue wall centre, beside the HIVE: front (intake) against the wall - shoot-first routines drive to the Zone A side spot",
                0.00, -61.67, 90.0),
        S5("Far/red corner, next to the LOADING ZONE: front (intake) against the far wall - A0 only",
                "Audience/blue corner, next to the LOADING ZONE: front (intake) against the audience wall - A0 only",
                -61.67, -61.67, 0.0),
        S6("Far wall on the red HIVE axis: front (intake) against the wall, back toward the HIVE - A3 role 2 (only 1.35 in from the F-far FLOWER!)",
                "Audience wall on the blue HIVE axis: front (intake) against the wall, back toward the HIVE - A3 role 2 (only 1.35 in from the F-aud FLOWER!)",
                -61.67, -10.10, 0.0),
        S7("Far wall, col B: front (intake) against the wall - A0 only (A3 role 2 starts on S6)",
                "Audience wall, col E: front (intake) against the wall - A0 only (A3 role 2 starts on S6)",
                -61.67, -40.00, 0.0);

        private final String redText;
        private final String blueText;
        public final double redX;
        public final double redY;
        public final double redShooterFacingDeg;

        StartSpot(String redText, String blueText, double redX, double redY, double redShooterFacingDeg) {
            this.redText = redText;
            this.blueText = blueText;
            this.redX = redX;
            this.redY = redY;
            this.redShooterFacingDeg = redShooterFacingDeg;
        }

        /** Where the spot is, in words, for THIS alliance (walls, columns and flowers differ on blue). */
        public String description(Alliance alliance) {
            return alliance == Alliance.BLUE ? blueText : redText;
        }
    }

    /** Robot start pose (FTC frame) for this alliance and start spot. */
    public static Pose startPose(Alliance alliance, StartSpot spot) {
        double robotHeadingRed = spot.redShooterFacingDeg - RobotConfig.SHOOTER_FACING_DEG;
        return pose(alliance, spot.redX, spot.redY, robotHeadingRed);
    }

    // =============================================================================================
    // Shooting spots (RED). The robot heading is chosen so the shooter (the robot's BACK) points at the
    // aim point; during the volley the robot turns itself onto the exact aim heading (subsystems/Aim).
    // Shot distances are measured like the AIM telemetry: from where the ball leaves the aimed robot to the
    // opening centre, with the placeholder RobotConfig.SHOOTER_FORWARD_IN = -7 (7 in shorter than from the
    // robot centre). (Rotated-footprint clearances, 18 in robot.)
    // =============================================================================================
    /**
     * AUTO Zone A "side spot" for shoot-first routines that do not start on S1 (S2-S4): a 38.5 in shot
     * (45.5 in from the robot centre) at the audience-cell opening, 32 deg off-axis, heading ~-32 deg (back
     * toward the cell). Its footprint is 6.8 in outside the tip-1 landing box (x 24..42, y -28..+2) and 2.9 in
     * clear of a partner still standing on S1, 3.3 in off the audience wall. (The TeleOp spot A2 (48, -12.75)
     * is 3 in INSIDE the landing box: not used in AUTO.)
     */
    public static final double ZONE_A_SIDE_SHOOT_X = 55.0, ZONE_A_SIDE_SHOOT_Y = -37.0;
    /** Shoot-first starts on our alliance wall (S3, S4) first drive straight off it to this y. */
    public static final double LEAVE_ALLIANCE_WALL_Y = -48.0;
    /**
     * Zone B ("B2", A2 volley 2): straight in front of the red far-cell opening, heading 180 (back toward the
     * cell). A 24.5 in shot (31.5 in from the robot centre): BELOW the 30-92 in shot band, the one documented
     * exception (RouteSweepTest checks B2 against a 24 in floor) until the shot map (07 Exp 2b) decides B2.
     * If the short shot misses, move B2 back toward (-55, -12.75) (a 31.5 in shot): that spot passes the route
     * test but is only ~1.6 in from the F-far FLOWER, and the approach drives toward it, so an overshoot
     * touches it. The robot's edge nearest the HIVE is ~3 in inside the TIP-2 landing box - fine for A2 (4
     * balls cannot tip the empty far cell), but a robot that fires a TIP-2 threshold ball here must move off
     * after the volley.
     */
    public static final double ZONE_B_SHOOT_X = -48.0, ZONE_B_SHOOT_Y = -12.75;
    /**
     * Zone B spot for A3 role 1: a 32.5 in shot (39.5 in from the robot centre) at the far-cell opening,
     * heading ~-154.1 deg (back toward the aim point), ~26 deg off-axis. Turned 25.9 deg from square, its
     * corners reach 12.0 in: 0.87 in outside the TIP-2 landing box (x -42..-24, y -28..+2; +-10 in), 1.6 in
     * from the F-far FLOWER, 1.1 in from a partner still at S6 and 5.0 in from the robot in the corner slot.
     * A partner on S7 would overlap this spot by ~8 in - another reason role 2 starts on S6 (the only start
     * it accepts) and is parked before 17 s.
     */
    public static final double ZONE_B_SHOOT_ROLE1_X = -52.0, ZONE_B_SHOOT_ROLE1_Y = -30.0;
    /**
     * A3 role 1 waits here (square heading) until A3_ROLE1_ZONE_B_NOT_BEFORE_SEC: the end of the corridor,
     * 2.3 in clear of the role-2 robot's park drive and 3 in from a robot in the corner slot.
     */
    public static final double A3_ROLE1_WAIT_X = -30.0, A3_ROLE1_WAIT_Y = -40.0;
    /**
     * Lane point used by every drive from the far half into the INNER park slot, and by A3 role 1 on its way
     * to its Zone B spot: keeps >= 2.4 in from a robot in the corner slot and 2.3-3.1 in from the HIVE foot.
     * Straight lines from the far half would clip the corner-slot robot.
     */
    public static final double INNER_LANE_X = -31.0, INNER_LANE_Y = -35.5;
    /**
     * A3 role 1 exit after its volley (it fired the TIP-2 threshold ball): it turns square while dropping
     * straight down, then runs east along the bottom edge of the TIP-2 landing box (the footprint skims the
     * nominal box by 0.5 in instead of crossing it 7.7 in deep) and stays 1.5 in above the corner-slot robot.
     */
    public static final double ROLE1_EXIT_1_X = -51.5, ROLE1_EXIT_1_Y = -36.5;
    public static final double ROLE1_EXIT_2_X = -31.0, ROLE1_EXIT_2_Y = -36.5;

    /** AUTO Zone A side spot (shoot-first routines from S2 / S3 / S4). */
    public static Pose zoneASideShootPose(Alliance alliance) {
        return shootPose(alliance, ZONE_A_SIDE_SHOOT_X, ZONE_A_SIDE_SHOOT_Y, HiveState.A);
    }

    /** B2: A2's volley-2 pose. */
    public static Pose zoneBShootPose(Alliance alliance) {
        return shootPose(alliance, ZONE_B_SHOOT_X, ZONE_B_SHOOT_Y, HiveState.B);
    }

    /** A3 role 1's volley-2 pose. */
    public static Pose role1ShootPose(Alliance alliance) {
        return shootPose(alliance, ZONE_B_SHOOT_ROLE1_X, ZONE_B_SHOOT_ROLE1_Y, HiveState.B);
    }

    /** A pose at the RED spot (redX, redY) turned so the shooter (the robot's back) points at the target cell. */
    public static Pose shootPose(Alliance alliance, double redX, double redY, HiveState target) {
        FieldPoint redAim = (target == HiveState.A) ? RED_AIM_STATE_A : RED_AIM_STATE_B;
        double redHeading = MathUtil.bearingDeg(redX, redY, redAim.x, redAim.y) - RobotConfig.SHOOTER_FACING_DEG;
        return pose(alliance, redX, redY, redHeading);
    }

    // =============================================================================================
    // Corridor between the zones (RED). Stays out of the under-HIVE lane and the spill box.
    // =============================================================================================
    /** First waypoint after a Zone A volley: away from the spill landing box, toward the corridor. */
    public static final double SPILL_ESCAPE_X = 52.0, SPILL_ESCAPE_Y = -40.0;
    /**
     * Corridor between the zones. Point A is 2 in lower than B so robots that arrive still turned from S1 or
     * the garden keep >= 2 in from the tip-1 landing box while they turn. From the Zone A side spot the margin
     * is smaller (about 1.6 in while turning, 0.2 in on the leg before), which is why A1 holds for the spill first.
     */
    public static final double CORRIDOR_A_X = 30.0, CORRIDOR_A_Y = -42.0;
    public static final double CORRIDOR_B_X = -30.0, CORRIDOR_B_Y = -40.0;
    /**
     * Leaving the far wall from S6: first drive STRAIGHT along +X to this x at the same y, then turn toward
     * the park slot. A diagonal start from S6 (-61.67, -10.1) clips the F-far FLOWER, whose footprint is
     * x -70.65..-65.63, y -26.34..-20.45 (the robot may only drop 1.35 in before its edge reaches the
     * flower's y band, while it is still beside it). S7 (y = -40) never needs it.
     */
    public static final double FAR_WALL_ESCAPE_X = -50.0;
    /** Only robots this close to the far wall (S6, x = -61.67) use the far-wall escape. */
    public static final double FAR_WALL_ESCAPE_BEYOND_X = -56.0;

    // =============================================================================================
    // LOADING ZONE and PARK (RED)
    // =============================================================================================
    /** Red LOADING ZONE tape box (CAD). Event fields: only 11.0 in deep (FAC 3.4) -> y >= -59.67. */
    public static final double RED_LZ_MIN_X = -46.60, RED_LZ_MAX_X = -23.91;
    public static final double RED_LZ_MIN_Y = -70.67, RED_LZ_MAX_Y = -59.10;
    /**
     * Where to park in the LOADING ZONE (AUTO INIT "Park" item; A3 role 1 always INNER, role 2 always CORNER).
     * Two robots share the 22.7 in wide LZ: CORNER and INNER are 6 in apart, both 5.67 in off our alliance
     * wall, overlapping the LZ by 4.6 x 5.9 in (CORNER) and 12.1 x 5.9 in (INNER); 5.3 in deep on an event
     * field. SOLO is the middle (overlaps the LZ ~4.9 in, 6.7 in off the wall) and touches BOTH slots, so
     * use it only when the partner does not park in AUTO. Rule: the first robot to arrive takes CORNER.
     * Q&A 21: touching ANY wall at the end of AUTO means no LEAVE, even when parked.
     */
    public enum ParkSlot {
        /** LZ end away from the field corner: red (-27, -56), blue (27, 56). */
        INNER(-27.0, -56.0),
        /** LZ end nearer the field corner (red far-wall end, blue audience end): red (-51, -56), blue (51, 56). */
        CORNER(-51.0, -56.0),
        /** One-robot pose in the middle: red (-35.25, -55), blue (35.25, 55). */
        SOLO(-35.25, -55.0);

        public final double redX;
        public final double redY;

        ParkSlot(double redX, double redY) {
            this.redX = redX;
            this.redY = redY;
        }

        /** "INNER slot (-27, -56)" with this alliance's coordinates. */
        public String label(Alliance alliance) {
            return name() + (this == SOLO ? " pose (" : " slot (") + num(alliance.fromRedX(redX)) + ", "
                    + num(alliance.fromRedY(redY)) + ")";
        }

        private static String num(double v) {
            return (v == Math.rint(v)) ? String.valueOf((long) v) : String.valueOf(v);
        }
    }

    /**
     * Park pose in a slot. The heading is the robot's current heading rounded to the nearest multiple of
     * 90 deg: a square robot has a predictable footprint (LZ overlap, wall gap, room for the partner), and
     * it barely needs to turn on the way in. A robot turned ~45 deg reaches 12.7 in from its centre
     * instead of 9 in.
     */
    public static Pose parkPose(Alliance alliance, ParkSlot slot, double currentHeadingRad) {
        return new Pose(alliance.fromRedX(slot.redX), alliance.fromRedY(slot.redY), squareHeadingRad(currentHeadingRad));
    }

    /** The multiple of 90 deg nearest to this heading (radians in, radians out). */
    public static double squareHeadingRad(double headingRad) {
        return Math.toRadians(Math.round(Math.toDegrees(headingRad) / 90.0) * 90.0);
    }

    // =============================================================================================
    // GARDEN (RED): audience wall, red corner. 4 POLLEN at (69.3, -69.3 / -66.4 / -63.5 / -60.6).
    // The balls sit 1.4 in from the wall, so only the outer edge of a full-width intake reaches them.
    // =============================================================================================
    public static final double RED_GARDEN_MIN_X = 68.10, RED_GARDEN_MAX_X = 70.67;
    public static final double RED_GARDEN_MIN_Y = -70.10, RED_GARDEN_MAX_Y = -47.41;
    /** Sweep along the wall toward the red corner. TODO-MEASURE (x: intake edge must reach x ~ 69). */
    public static final double GARDEN_SWEEP_X = 60.5;                                              // TODO-MEASURE
    public static final double GARDEN_SWEEP_START_Y = -46.0;
    public static final double GARDEN_SWEEP_END_Y = -60.0;                                         // TODO-MEASURE
    /** Red sweep travels toward the red wall (-Y). */
    public static final double GARDEN_SWEEP_TRAVEL_DEG = -90.0;

    /**
     * Two waypoints between Zone A and the sweep start: the robot turns to face the sweep direction
     * between them, away from the audience wall (a turning 18 in robot reaches 12.7 in from its centre,
     * so it must not turn while hugging the wall). They sit low enough that the turning robot stays
     * ~2 in outside the tip-1 landing box: A2 / A3 role 1 leave right after volley 1 while the spill
     * may still be falling (G409). From S1 the leg is ~49 in.
     */
    public static final double GARDEN_APPROACH_1_X = 52.0, GARDEN_APPROACH_1_Y = -40.0;
    public static final double GARDEN_APPROACH_2_X = 51.0, GARDEN_APPROACH_2_Y = -50.0;

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
    /**
     * Waypoints from (x, y) to a LOADING ZONE slot that avoid the HIVE, the spill box, the F-far flower
     * and (for the INNER slot) a robot already in the CORNER slot.
     */
    public static List<Pose> routeToLoadingZone(Alliance alliance, double x, double y, ParkSlot target) {
        double rx = alliance.fromRedX(x);
        double ry = alliance.fromRedY(y);
        List<Pose> points = new ArrayList<>();
        if (rx > 20.0) {
            if (rx > 44.0 && ry > -38.0) points.add(point(alliance, SPILL_ESCAPE_X, SPILL_ESCAPE_Y));
            points.add(point(alliance, CORRIDOR_A_X, CORRIDOR_A_Y));
            points.add(point(alliance, CORRIDOR_B_X, CORRIDOR_B_Y));
        } else if (rx < FAR_WALL_ESCAPE_BEYOND_X && ry > -36.0) {
            points.add(point(alliance, FAR_WALL_ESCAPE_X, ry));   // S6: straight out along +X first (F-far flower)
        } else if (rx > -50.0 && ry < -58.0) {
            points.add(point(alliance, rx, LEAVE_ALLIANCE_WALL_Y)); // S4: straight off our alliance wall first
        }
        // From the far half into the INNER slot: the lane goes AFTER any far-wall escape point (never
        // before it, or the route doubles back west through the corner slot).
        if (target == ParkSlot.INNER && rx < -33.0) points.add(innerLane(alliance));
        return points;
    }

    /** Lane point into the INNER slot from the far half (clear of a robot in the CORNER slot). */
    public static Pose innerLane(Alliance alliance) {
        return point(alliance, INNER_LANE_X, INNER_LANE_Y);
    }

    /** A3 role 1's exit waypoints from its Zone B spot to the INNER slot (along the TIP-2 box's bottom edge). */
    public static List<Pose> role1ExitToInner(Alliance alliance) {
        List<Pose> points = new ArrayList<>();
        points.add(point(alliance, ROLE1_EXIT_1_X, ROLE1_EXIT_1_Y));
        points.add(point(alliance, ROLE1_EXIT_2_X, ROLE1_EXIT_2_Y));
        return points;
    }

    /** Waypoints from the start to the Zone A side spot (S3/S4: straight off our alliance wall first). */
    public static List<Pose> routeToZoneASide(Alliance alliance, StartSpot start) {
        List<Pose> points = new ArrayList<>();
        if (start == StartSpot.S3 || start == StartSpot.S4) {
            points.add(point(alliance, start.redX, LEAVE_ALLIANCE_WALL_Y));
        }
        return points;
    }

    /** Waypoints from (x, y) in the audience half to the far half, through the corridor. */
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
