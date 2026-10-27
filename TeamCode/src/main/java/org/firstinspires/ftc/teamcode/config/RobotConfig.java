package org.firstinspires.ftc.teamcode.config;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * =================================================================================================
 *  FTC 12303 - THE ONE FILE TO EDIT. Every device name, motor/servo direction and tunable number.
 * =================================================================================================
 *
 * How to read the tags:
 *   TODO-MEASURE  = a guess. Measure it on OUR robot (the tuning OpMode or method is named) and
 *                   replace the number. Do not trust any TODO-MEASURE value at a competition.
 *   (rule G407)   = the BIOBUZZ game-manual rule that explains why the code does this.
 *
 * The robot: mecanum drive, an intake wheel at the FRONT (on a floating arm), a tunnel that holds the
 * balls, a CARRIER wheel above the tunnel (it holds the balls and feeds them one at a time) and a fixed
 * SHOOTING wheel at the top of the BACK. The shooter cannot turn: the robot aims by turning its back to
 * the target.
 *
 * Device names must match the Robot Configuration on the Driver Station EXACTLY (case matters).
 * Every device is optional: if one is missing the robot still runs, that feature switches off and a
 * warning appears in INIT telemetry.
 *
 * Other tuning that lives elsewhere ON PURPOSE:
 *   - Pedro path-follower gains (Foresight) -> pedro/Constants.java (paste the AutoTune output there).
 *   - Field geometry (HIVE, zones, start spots) -> config/FieldConstants.java (comes from the CAD).
 *
 * Units: inches, degrees, milliseconds, RPM unless the name says otherwise.
 */
public final class RobotConfig {
    private RobotConfig() {
    }

    // =============================================================================================
    // 1. DEVICE NAMES (Robot Configuration on the Driver Station)
    // =============================================================================================
    // Drive: 4 SWYFT Drive V2 mecanum modules (Spike motor, 12.7:1 in-wheel gearbox).
    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";
    /** Shooting wheel motor at the top of the robot's back (with its encoder cable plugged in!). */
    public static final String FLYWHEEL = "flywheel";
    /** Intake wheel at the front: a DC motor (goBILDA Yellow Jacket). A continuous-rotation servo also works. */
    public static final String INTAKE = "intake";
    /**
     * Carrier wheel above the tunnel: holds the balls while it is stopped and pushes ONE ball into the
     * shooting wheel when it spins. A DC motor or a continuous-rotation servo - configure "carrier" as either.
     */
    public static final String CARRIER = "carrier";
    /** goBILDA Pinpoint odometry computer. Use an I2C bus OTHER than 0 (bus 0 shares the hub IMU). */
    public static final String PINPOINT = "pinpoint";
    /** Limelight 3A (only the 3A is legal - the 3G is prohibited by R702). */
    public static final String LIMELIGHT = "limelight";
    /**
     * OPTIONAL ball sensor at the intake mouth: REV Color Sensor V3, or a Brushland Color Rangefinder in I2C
     * mode (its digital outputs are NOT read by this code). Mount it where every ball passes within
     * MOUTH_BALL_PRESENT_CM before the intake wheel commits it - including garden balls taken with the edge
     * of the wheel. WITHOUT it the robot cannot count balls coming in or refuse NECTAR (INIT warns).
     */
    public static final String MOUTH_COLOR = "mouthColor";
    /** OPTIONAL colour/distance sensor that sees the 4th ball in the tunnel ("we are full"). */
    public static final String SLOT4_COLOR = "slot4";
    /** OPTIONAL IR break-beam (digital channel) where balls enter the shooting wheel. */
    public static final String FEED_BEAM = "feedBeam";

    // =============================================================================================
    // 2. DRIVETRAIN (SWYFT Drive V2 mecanum) + DRIVER AIM-ASSIST
    // =============================================================================================
    // Directions: SWYFT's sample reverses the RIGHT side, but the in-wheel planetary means you must
    // test it (R5 section 2.3). TODO-MEASURE: run "Check: Sensors" (dpad spins each wheel) or the Pedro
    // AutoTune Mecanum Tuner, then copy the 4 directions here.
    public static DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.FORWARD;   // TODO-MEASURE
    public static DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.FORWARD;    // TODO-MEASURE
    public static DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;  // TODO-MEASURE
    public static DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;   // TODO-MEASURE

    /** Hold-to-slow multiplier for precise moves (driver left bumper). */
    public static double DRIVE_SLOW_SCALE = 0.35;
    /** Multiplier on the turn stick so turning is not twitchy. */
    public static double DRIVE_TURN_SCALE = 0.8;
    /** Stick values smaller than this are treated as zero. */
    public static double DRIVE_STICK_DEADBAND = 0.05;
    /** gm0 mecanum fudge: strafing is slower than driving forward, so scale strafe up (TeleOp only). */
    public static double DRIVE_STRAFE_CORRECTION = 1.1;
    /** BRAKE when the sticks are released in TeleOp (SWYFT default; good for precise stops). */
    public static boolean DRIVE_BRAKE_IN_TELEOP = true;
    /**
     * Driver aim-assist (hold RB): turns the robot so its back (the shooter) points at the target cell.
     * Turn power = AIM_ASSIST_MIN_TURN + AIM_ASSIST_TURN_KP x heading error (deg), at most
     * AIM_ASSIST_MAX_TURN; it stops turning inside half of the aim tolerance (section 6).
     * TODO-MEASURE: raise MIN_TURN if the robot stops short of the target, lower KP if it overshoots.
     */
    public static double AIM_ASSIST_TURN_KP = 0.015;                                             // TODO-MEASURE
    /** Smallest turn power that still turns the robot on the tiles (beats wheel friction). */
    public static double AIM_ASSIST_MIN_TURN = 0.08;                                              // TODO-MEASURE
    /** Driver aim-assist: maximum turn power. */
    public static double AIM_ASSIST_MAX_TURN = 0.45;

    // =============================================================================================
    // 3. PINPOINT ODOMETRY (copy these from the Pedro AutoTune "Pinpoint Tuner" Java output)
    // =============================================================================================
    /** goBILDA pods: 4-bar (32 mm) or swing-arm (48 mm). Ignored when PINPOINT_CUSTOM_TICKS_PER_INCH > 0. */
    public static GoBildaPinpointDriver.GoBildaOdometryPods PINPOINT_POD_TYPE =
            GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;                            // TODO-MEASURE: run Check: Odometry
    /**
     * For NON-goBILDA pods only (e.g. SWYFT Linear Odometry: 4096 counts / (pi x 38 mm) = 34.31 ticks/mm
     * = 871.5 ticks/in - but check whether SWYFT's "4096 PPR" means counts or pulses: R5 2.4). Check: Odometry
     * measures it: for any pod it does not recognise it prints the measured value to put here.
     * Leave at 0 to use PINPOINT_POD_TYPE.
     */
    public static double PINPOINT_CUSTOM_TICKS_PER_INCH = 0.0;
    /** Sideways distance of the FORWARD (X) pod from the robot centre, inches, left = +. */
    public static double PINPOINT_X_POD_OFFSET_IN = 0.0;                                           // TODO-MEASURE
    /** Forward distance of the STRAFE (Y) pod from the robot centre, inches, forward = +. */
    public static double PINPOINT_Y_POD_OFFSET_IN = 0.0;                                           // TODO-MEASURE
    public static GoBildaPinpointDriver.EncoderDirection PINPOINT_X_POD_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.FORWARD;                                        // TODO-MEASURE
    public static GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_POD_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.FORWARD;                                        // TODO-MEASURE

    // =============================================================================================
    // 4. SHOOTING WHEEL ("flywheel": goBILDA 5203 6000 rpm 1:1 assumed - check the motor label)
    // =============================================================================================
    /** Must spin so the ball leaves out of the robot's back. TODO-MEASURE (FlywheelTuner: spin at 1000 rpm and look). */
    public static DcMotorSimple.Direction FLYWHEEL_DIRECTION = DcMotorSimple.Direction.FORWARD;    // TODO-MEASURE
    /**
     * Encoder counts per WHEEL revolution = 28 counts per motor revolution (goBILDA 5203 1:1) x motor turns
     * per wheel turn (1 if the wheel sits on the motor shaft; multiply by the gear/belt ratio otherwise).
     * TODO-MEASURE (mark the wheel, count motor turns per wheel turn). Every "rpm" the code shows is wheel
     * rpm only when this is right.
     */
    public static double FLYWHEEL_TICKS_PER_REV = 28.0;                                           // TODO-MEASURE (ratio)
    /**
     * Free speed in encoder ticks/s at full power on a FULL battery (6000 rpm = 2800 t/s on paper).
     * TODO-MEASURE: FlywheelTuner "X = max speed test". The PIDF below is computed from this.
     */
    public static double FLYWHEEL_MAX_TICKS_PER_SEC = 2700.0;                                     // TODO-MEASURE
    // REV hub velocity PIDF recipe (REV FIRST Global guide): F = 32767 / maxTicksPerSec, P = 0.1 F,
    // I = 0.1 P, D = 0. Coefficients are NOT saved in the hub - we re-send them every OpMode.
    public static double FLYWHEEL_P_OVER_F = 0.1;    // P = 0.1 x F      (tune in FlywheelTuner)
    public static double FLYWHEEL_I_OVER_F = 0.01;   // I = 0.1 x P = 0.01 x F
    public static double FLYWHEEL_D = 0.0;
    /** Optional override: set > 0 to use this F instead of 32767 / MAX_TICKS_PER_SEC. */
    public static double FLYWHEEL_F_OVERRIDE = 0.0;
    /**
     * "At speed" window. The 28-count encoder reads in steps of ~20-25 ticks/s (about 1-1.5% at
     * shooting speed, gm0 / R3 verify F16), so a tighter gate than ~2-3% is noise. TODO-MEASURE.
     */
    public static double FLYWHEEL_READY_TOLERANCE_FRACTION = 0.025;
    /** Never use a window smaller than this many RPM. */
    public static double FLYWHEEL_READY_TOLERANCE_MIN_RPM = 60.0;
    /** Must be inside the window this many loops in a row before we call it "ready" (debounce). */
    public static int FLYWHEEL_READY_LOOPS = 3;
    /**
     * A speed drop below target x (1 - this) during one excursion out of the at-speed window = "a ball went
     * through" (one shot dip per excursion). It CONFIRMS shots and stops the carrier, so tune it early.
     * TODO-MEASURE with Tune: Flywheel B (watch "last shot: drop"; 07 Exp 0b).
     */
    public static double FLYWHEEL_SHOT_DIP_FRACTION = 0.05;                                       // TODO-MEASURE
    /** Ignore speed drops this soon after a target change of more than 2% (it is a new target, not a ball). */
    public static long FLYWHEEL_DIP_IGNORE_AFTER_TARGET_CHANGE_MS = 100;
    /**
     * Hard safety cap (~83% of the 6000 rpm free speed). R3 6.3: above ~4,300 rpm (75% of free speed at
     * 11.5 V) recovery between shots suffers - if the tuned table needs more, use a bigger wheel.
     */
    public static double FLYWHEEL_MAX_RPM = 5000.0;
    /** Operator dpad up/down changes the RPM trim by this much. */
    public static double FLYWHEEL_TRIM_STEP_RPM = 50.0;
    /** Fixed RPM used when the distance to the target is unknown (no odometry). TODO-MEASURE */
    public static double FLYWHEEL_DEFAULT_RPM = 3550.0;                                           // TODO-MEASURE

    // =============================================================================================
    // 5. SHOT TABLE: horizontal distance (where the ball leaves the robot -> CELL opening centre, inches)
    //    -> flywheel RPM. The distance is the one the AIM telemetry line shows (subsystems/Aim).
    // =============================================================================================
    // TODO-MEASURE: placeholder rows from the R3 physics MODEL (72 mm wheel, ~65 deg ball exit, exit
    // speed = 0.40 x wheel surface speed), converted from "lip distance" to "opening-centre distance"
    // (+2.8 in). Our shooting wheel's size and its fixed exit angle are not measured yet, so these rows
    // only get the first test shots near the cell. Below 33 in the table gives the 33 in value. Use the
    // Shot Map Logger (07 Exp 2b: 5 distances x 10 shots) and replace them with the middle of the RPM band
    // that scores.
    public static double[][] SHOT_TABLE = {
            // {distance in, rpm}
            {33, 3600},
            {39, 3525},
            {45, 3550},
            {51, 3625},
            {57, 3725},
            {63, 3825},
            {75, 4050},
            {87, 4300},
    };
    /**
     * Multiplies every RPM in the table. After the FIRST test, set it to 0.40 / k_measured, where
     * k = ball exit speed / wheel surface speed (the model assumed k = 0.40). A LOWER k needs MORE rpm:
     * k = 0.35 -> scale 1.14. TODO-MEASURE (slow-motion video, 07 Exp 2a).
     */
    public static double SHOT_TABLE_RPM_SCALE = 1.0;                                              // TODO-MEASURE
    /**
     * Reliable shot band in OPENING-CENTRE distance, measured from where the ball leaves the aimed robot
     * (what aim.distanceIn and the AIM telemetry line show): R3's ~27-90 in LIP distance + ~2.8 in = ~30-92 in
     * (same band as the playbook cone). Telemetry warns outside it. Re-check it once the fixed exit angle is
     * measured. B2 (A2 volley 2, ~24.5 in with SHOOTER_FORWARD_IN = -7) is the one AUTO pose below it until
     * the shot map decides B2 (FieldConstants.ZONE_B_SHOOT_X).
     */
    public static double SHOT_MIN_DISTANCE_IN = 30.0;
    public static double SHOT_MAX_DISTANCE_IN = 92.0;

    // =============================================================================================
    // 6. AIM (the shooter is FIXED: the robot aims by turning its back to the target)
    // =============================================================================================
    /**
     * Where the shooter points, relative to the robot FRONT (the intake side), counter-clockwise +:
     * 180 = straight out of the robot's back. Every start pose, shooting pose and aim uses it, so the
     * robot's BACK faces the up cell when it shoots.
     */
    public static final double SHOOTER_FACING_DEG = 180.0;
    /**
     * Where the ball leaves the shooting wheel, from the robot centre: forward + (negative = behind the
     * centre) and left +, inches. The shot table distances are measured from this point (with the robot
     * turned onto its aim), so measure it BEFORE the Shot Map Logger. A sideways offset also changes the aim
     * heading a little (the code allows for it). TODO-MEASURE (07 Exp 2a: tape measure; the wheel is at the
     * top of the back).
     */
    public static double SHOOTER_FORWARD_IN = -7.0;                                               // TODO-MEASURE
    public static double SHOOTER_LEFT_IN = 0.0;                                                   // TODO-MEASURE
    /**
     * Fire gate: the robot's heading must be within the AIM TOLERANCE of the aim heading. The tolerance is
     * computed for every shot from the distance between the robot CENTRE and the aim point (the robot turns
     * about its centre, so a heading error swings the shot about it) and the cell opening's width as seen
     * from the robot (FieldConstants.CELL_OPENING_WIDTH_IN x cos(off-axis), minus the ball radius):
     *   tolerance = AIM_TOLERANCE_FRACTION x atan(free half-width / robot-centre distance),
     * kept between AIM_TOLERANCE_MIN_DEG and AIM_TOLERANCE_MAX_DEG. Examples (shot distances from where the
     * ball leaves, SHOOTER_FORWARD_IN = -7): 4.0 deg (the cap) for every straight-on shot up to ~54 in (61 in
     * from the robot centre) and at every AUTO shooting pose; 3.2 deg for a 70 in shot straight on; 2.5 deg
     * for a 92 in shot straight on; 1.8 deg for a 92 in shot at 40 deg off-axis. TODO-MEASURE: lower the
     * fraction if shots fired at the edge of the window miss (Shot Map Logger).
     */
    public static double AIM_TOLERANCE_FRACTION = 0.5;                                            // TODO-MEASURE
    public static double AIM_TOLERANCE_MIN_DEG = 1.5;
    public static double AIM_TOLERANCE_MAX_DEG = 4.0;
    /**
     * Operator dpad left/right: aim heading trim step (left = the ball goes further left, seen from the
     * shooter looking toward the cell; + = the robot turns counter-clockwise).
     */
    public static double AIM_TRIM_STEP_DEG = 0.5;

    // =============================================================================================
    // 7. CARRIER (holds the balls, feeds them one at a time) + BALL COUNT
    // =============================================================================================
    /** G407: a robot may CONTROL at most 4 scoring elements. Do not change. */
    public static final int MAX_BALLS = 4;
    /** FORWARD power must push balls toward the shooting wheel. TODO-MEASURE ("Check: Sensors" B). */
    public static DcMotorSimple.Direction CARRIER_DIRECTION = DcMotorSimple.Direction.FORWARD;     // TODO-MEASURE
    /** Carrier power while it pushes one ball into the shooting wheel. TODO-MEASURE */
    public static double CARRIER_FEED_POWER = 1.0;                                                // TODO-MEASURE
    /**
     * The carrier stops as soon as the shot is confirmed (flywheel dip, or the feed beam if fitted) - or
     * after this long at the latest. Long enough to push ONE ball into the shooting wheel, too short for
     * two. TODO-MEASURE (07 Exp 0b: Tune: Flywheel, 2 balls inside, RB = a timed push of exactly this long
     * that ignores the dip; Y selects FEED_MS, dpad up/down changes it: exactly one ball must leave per push).
     */
    public static long CARRIER_FEED_MS = 400;                                                     // TODO-MEASURE
    /**
     * After the carrier stops without a confirmation, wait this long for a late flywheel dip / beam break.
     * No confirmation = an "unconfirmed feed": telemetry counts it and the operator pad gives 4 blips.
     */
    public static long SHOT_CONFIRM_TIMEOUT_MS = 250;
    /**
     * Minimum gap from one ball's confirmation (or confirm timeout) to the next carrier push. The next ball
     * also waits until the flywheel is back at speed and the aim is good.
     */
    public static long MIN_SHOT_INTERVAL_MS = 150;
    /**
     * If neither the beam-break nor the flywheel dip confirms a shot, still count the ball as gone?
     * true  = every feed lowers the count, confirmed or not (the count may go too LOW after a jam; the
     *         slot-4 sensor, if fitted, forces it back to 4 - G407 safe). Without a mouth sensor this is how
     *         the count drops: one per feed.
     * false = the count only drops on a confirmed shot (needs feedBeam or a tuned dip); a volley then stops
     *         after 2 unconfirmed feeds in a row.
     * There is NO automatic jam reverse/retry: clear jams with operator LB (eject), fix the count with
     * operator back (-1) / left-stick click (+1). Without a mouth sensor an eject never lowers the count:
     * press back once for each ball that went out.
     */
    public static boolean COUNT_UNCONFIRMED_SHOTS = true;
    /**
     * Carrier power while the intake pulls balls in. 0 = stopped: the carrier holds the balls and none can
     * reach the shooting wheel. Raise it a little ONLY if balls stop short of the carrier. TODO-MEASURE
     */
    public static double CARRIER_INTAKE_POWER = 0.0;                                              // TODO-MEASURE
    /** Carrier power while the operator ejects (LB): balls go back out through the intake. */
    public static double CARRIER_EJECT_POWER = -1.0;

    // =============================================================================================
    // 8. INTAKE (full-width roller of compliant star wheels on a floating front arm, DC motor)
    // =============================================================================================
    public static DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;      // TODO-MEASURE
    /** Which side of the robot the intake mouth is on (relative to the robot front, CCW +): 0 = front. */
    public static double INTAKE_FACING_DEG = 0.0;
    public static double INTAKE_IN_POWER = 1.0;                                                   // TODO-MEASURE
    public static double INTAKE_OUT_POWER = -1.0;
    /**
     * When we reach 4 balls, spin the intake wheel backward this long to push a 5th ball away (G407). Ball 4
     * is counted once it has passed the mouth sensor - or at once when a 5th ball shows up right behind it
     * (MOUTH_ONE_BALL_MAX_MS) - so the pulse hits the 5th ball, not ball 4.
     */
    public static long INTAKE_FULL_REVERSE_MS = 150;
    /**
     * Refused NECTAR at the mouth (G408): the intake wheel runs backward for as long as the refused ball is
     * still at the mouth, and this much longer after it is gone. Needs the mouth sensor.
     */
    public static long INTAKE_REJECT_MS = 400;
    /**
     * POLLEN only until the NECTAR tests pass: false = the mouth refuses ALL NECTAR (ours too), because a
     * 3.6 in ball through the tunnel, carrier and shooting wheel is untested. Set true only after the NECTAR
     * test (07 Exp 6) is GO; then only the opponent's NECTAR is refused (G408). Drivers still steer around
     * NECTAR: the sensor reacts on contact (and without a mouth sensor nothing is refused).
     */
    public static boolean ACCEPT_OWN_NECTAR = false;

    // =============================================================================================
    // 9. BALL SENSORS (all OPTIONAL): mouth colour/distance, slot 4 (tunnel full), feed break-beam
    // =============================================================================================
    /** Colour sensor gain (REV V3 reads dark values; higher gain = brighter). TODO-MEASURE (SensorCheck) */
    public static float COLOR_SENSOR_GAIN = 2.0f;                                                 // TODO-MEASURE
    /** A ball is "at the mouth" when the distance reading is below this (cm). TODO-MEASURE */
    public static double MOUTH_BALL_PRESENT_CM = 4.0;                                             // TODO-MEASURE
    /** A ball is "in slot 4" (the 4th tunnel place) when the slot sensor reads below this (cm). TODO-MEASURE */
    public static double SLOT4_BALL_PRESENT_CM = 3.0;                                             // TODO-MEASURE
    // Hue windows (0-360). Starting points from R5/R6: yellow ~40-70, red <15 or >340, blue ~200-250.
    // The REV V3 often reads yellow greener than a camera does. TODO-MEASURE all of them with
    // "Check: Sensors" using real POLLEN / NECTAR under event-like light.
    public static double POLLEN_HUE_MIN = 40.0;                                                   // TODO-MEASURE
    public static double POLLEN_HUE_MAX = 95.0;                                                   // TODO-MEASURE
    public static double RED_HUE_MAX = 20.0;                                                      // TODO-MEASURE
    public static double RED_HUE_MIN_WRAP = 330.0;                                                // TODO-MEASURE
    public static double BLUE_HUE_MIN = 190.0;                                                    // TODO-MEASURE
    public static double BLUE_HUE_MAX = 250.0;                                                    // TODO-MEASURE
    /** Below this saturation (0-1) the colour is "UNKNOWN" (still counted as a ball). */
    public static double MIN_SATURATION = 0.25;                                                   // TODO-MEASURE
    /** I2C reads cost ~4 ms each, so we do not read every sensor every loop. */
    public static long MOUTH_SENSOR_PERIOD_MS = 20;
    public static long SLOT4_SENSOR_PERIOD_MS = 100;
    /**
     * A ball is counted when it LEAVES the mouth sensor's view inward (it has then passed the mouth) and has
     * been out of view this long. Stops one ball that flickers in and out of view from counting twice.
     * TODO-MEASURE (SensorCheck): shorter than the gap between two balls taken back to back.
     */
    public static long BALL_COUNT_DEBOUNCE_MS = 60;                                               // TODO-MEASURE
    /**
     * The LAST free slot (3 counted, G407): ball 4 must be drawn in, but a 5th ball right behind it must not
     * follow. One mouth sensor cannot always tell two touching balls apart, so with 3 counted:
     *  - anything back in view after ball 4 left it (even for one read, before BALL_COUNT_DEBOUNCE_MS) is a
     *    5th ball: ball 4 is counted at once and the full pulse pushes the newcomer away;
     *  - a ball still in view MOUTH_ONE_BALL_MAX_MS after the intake started pulling it is TWO balls touching
     *    (no gap the sensor can see): ball 4 is counted then, and the full pulse pushes the 5th away.
     * Set it to the LONGEST time one ball stays in the mouth sensor's view while the intake pulls it in, + 30%.
     * Too short: a slow ball 4 is counted early and pushed back out (BALLS 4 with 3 inside - operator back = -1).
     * Too long: a touching 5th ball gets in. TODO-MEASURE ("Check: Sensors": hold A, pull 10 single POLLEN
     * through, read "longest"). The slot-4 sensor is read every MOUTH_SENSOR_PERIOD_MS while ball 4 comes in.
     */
    public static long MOUTH_ONE_BALL_MAX_MS = 300;                                               // TODO-MEASURE
    /** IR break-beam receivers usually pull the line LOW when the beam is broken. */
    public static boolean FEED_BEAM_BROKEN_IS_LOW = true;

    // =============================================================================================
    // 10. LIMELIGHT 3A (fixed to the chassis, looking out of the BACK like the shooter, pitched up ~35-40
    //     deg). ONE AprilTag pipeline for both alliances: ID filter 30-45 (the code keeps only our 8),
    //     marker size 82.55 mm, Full 3D ON, 640x480.
    // =============================================================================================
    public static int LIMELIGHT_APRILTAG_PIPELINE = 0;
    public static int LIMELIGHT_POLL_HZ = 100;
    /** Ignore Limelight results older than this. */
    public static long VISION_MAX_STALENESS_MS = 100;
    /** Lens height above the tiles. TODO-MEASURE */
    public static double CAMERA_HEIGHT_IN = 14.0;                                                 // TODO-MEASURE
    /** How far the camera is tilted UP from horizontal. TODO-MEASURE (brief: 35-40) */
    public static double CAMERA_PITCH_DEG = 37.0;                                                 // TODO-MEASURE
    /** Lens position from the robot centre: forward + (negative = behind the centre) and left +, inches. */
    public static double CAMERA_FORWARD_IN = -7.0;                                                // TODO-MEASURE
    public static double CAMERA_LEFT_IN = 0.0;                                                    // TODO-MEASURE
    /**
     * Which way the camera looks, relative to the robot FRONT, CCW +: 180 = out of the back, the same way
     * as the shooter, so it sees the target cell's tags while the robot aims. TODO-MEASURE (Check: Limelight).
     */
    public static double CAMERA_YAW_OFFSET_DEG = 180.0;                                           // TODO-MEASURE
    /** Up-cell tags are ~49.8 in high, down-cell tags ~35.5 in (R2 7.3, CAD); split the difference. */
    public static double TAG_UP_HEIGHT_THRESHOLD_IN = 42.7;
    /** Use vision to correct the odometry aim (low-pass "bias"). */
    public static boolean VISION_BIAS_ENABLED = true;
    /** Fraction of each new measurement mixed into the bias (0.15 = smooth, slow). */
    public static double VISION_BIAS_GAIN = 0.15;
    /** The bias may never exceed this (protects against a bad frame / wrong tag). */
    public static double VISION_BIAS_MAX_DEG = 8.0;
    /** Ignore a frame whose correction differs from the current bias by more than this... */
    public static double VISION_BIAS_OUTLIER_DEG = 6.0;
    /** ...unless this many outlier frames in a row agree within VISION_BIAS_OUTLIER_AGREE_DEG: then accept them. */
    public static int VISION_BIAS_OUTLIER_ACCEPT_FRAMES = 5;
    public static double VISION_BIAS_OUTLIER_AGREE_DEG = 2.0;
    /** How fast the bias fades back to 0 when no tags are seen (fraction per second). */
    public static double VISION_BIAS_DECAY_PER_SEC = 0.1;

    // =============================================================================================
    // 11. HIVE TRACKING
    // =============================================================================================
    /** Let vision switch the target cell by itself (needs a calibrated camera - check in LimelightCheck). */
    public static boolean HIVE_VISION_AUTO_SWITCH = true;
    /** Vision must disagree with the current state this long before we switch. */
    public static long HIVE_VISION_CONFIRM_MS = 500;
    /** At least this many own tags must be seen for vision evidence to count. */
    public static int HIVE_VISION_MIN_TAGS = 1;
    /**
     * After the operator presses Y ("HIVE tipped"), vision may not override for this long. After that,
     * 0.5 s of disagreeing vision can switch it back (set HIVE_VISION_AUTO_SWITCH = false if it keeps
     * flipping wrongly).
     */
    public static long HIVE_OPERATOR_PRIORITY_MS = 5000;
    /** A Y press this soon after a CAMERA switch confirms that switch instead of flipping back. */
    public static long HIVE_Y_CONFIRMS_VISION_MS = 3000;
    /**
     * Pause launching this long after the target cell changes (operator Y or vision) while the robot turns
     * and the flywheel retargets; any requested volley is cancelled (press RB again). It starts after the
     * HIVE has moved (or late in its swing), so it is NOT the manual 10.5.1 "don't launch at a tipping
     * HIVE" protection - the drive team handles that (03).
     */
    public static long TIP_PAUSE_MS = 1000;
    /**
     * AUTO: the start cell already holds 3 own NECTAR, so ~3 POLLEN tip it (EFSG section 12). With a feed
     * beam fitted only CONFIRMED shots count toward this.
     */
    public static int AUTO_ASSUME_TIP_AFTER_BALLS = 3;
    /** AUTO assumes the tip after volley 1; vision may not undo that for this long (dwell + swing). */
    public static long AUTO_TIP_ASSUME_PROTECT_MS = 5000;

    // =============================================================================================
    // 12. FIRING CONDITIONS (stop to shoot - R6 5.5, R3 recommendation)
    // =============================================================================================
    public static double FIRE_MAX_CHASSIS_SPEED_IN_PER_SEC = 6.0;
    public static double FIRE_MAX_CHASSIS_TURN_DEG_PER_SEC = 20.0;
    /**
     * Auto-aim never feeds a ball when the target cell's opening faces more than this far away from the
     * robot (off-axis angle; R2 verify F23: ~47-59 in^2 of opening at 75 deg, ~0-31 from behind). That
     * happens in the wrong zone or after an unnoticed TIP: telemetry says "TARGET CELL FACES AWAY" and the
     * operator light turns RED. Manual aim is not gated.
     */
    public static double FIRE_MAX_OFF_AXIS_DEG = 75.0;

    // =============================================================================================
    // 13. MATCH CUES
    // =============================================================================================
    public static double TELEOP_LENGTH_SEC = 120.0;
    /**
     * 1:00 left: FLOWER ownership unlocked; the human player enters the remaining NECTAR at 1:00 on the
     * FIELD TIMER (G426; the chime is a courtesy). This gamepad cue runs on our own TeleOp clock: a reminder only.
     */
    public static double CUE_ONE_MINUTE_LEFT_SEC = 60.0;
    /** 0:20 left = train whistle. Reminder only - the COACH calls the clock ("0:25", "0:12 last volley", "park"). */
    public static double CUE_TWENTY_SECONDS_LEFT_SEC = 20.0;

    // =============================================================================================
    // 14. AUTO
    // =============================================================================================
    /**
     * At this time after START (whatever we are doing) drive to the LOADING ZONE (LEAVE 3 + PARK 5). One
     * deadline for every routine. The slowest leg from here (Zone B / LZ side) is ~3-4 s at the 0.5 speed cap.
     */
    public static double AUTO_PARK_DEADLINE_SEC = 23.0;                                           // TODO-MEASURE
    /**
     * Give up on a volley after this long (flywheel never ready, jam, aim blocked) and move on with the
     * balls that are left. The clock starts once the robot has stopped (at most AUTO_VOLLEY_MAX_SETTLE_SEC
     * after the volley step begins), so a slow drive does not eat the volley time.
     */
    public static double AUTO_VOLLEY_TIMEOUT_SEC = 5.0;
    public static double AUTO_VOLLEY_MAX_SETTLE_SEC = 1.5;
    /**
     * Path timeout = leg length / (this x expected cruise speed) + AUTO_PATH_TIMEOUT_MARGIN_SEC. The cruise
     * speed comes from pedro/Constants (maxPathSpeed x the slower of maxAchievableForward/StrafeVelocity),
     * so the shipped 0.5 speed cap gives proportionally longer timeouts (a 125 in leg: ~9.5 s at the cap,
     * ~5.5 s tuned at full speed). A timed-out path stops: the robot holds where it is.
     */
    public static double AUTO_PATH_TIMEOUT_SPEED_FACTOR = 0.6;
    public static double AUTO_PATH_TIMEOUT_MARGIN_SEC = 1.5;
    /** Garden sweep speed as a fraction of full path speed (slow so the intake keeps up). */
    public static double AUTO_SWEEP_SPEED = 0.30;                                                 // TODO-MEASURE
    /** Keep the intake running this long after the sweep path ends. */
    public static double AUTO_SWEEP_DWELL_SEC = 0.6;
    /**
     * After the garden sweep, VOLLEY 2 feeds at least this many times even if the ball count is lower
     * (without a mouth sensor nothing is counted in, and garden balls taken with the wheel's edge can miss
     * it). Empty feeds are harmless; with a feed beam fitted it stops after 2 unconfirmed (empty) feeds in a
     * row. 0 = off.
     */
    public static int AUTO_VOLLEY2_MIN_FEEDS = 4;
    /**
     * A1 only: after volley 1, hold at the firing pose (outside the tip-1 landing box) until the spill has
     * landed: leave once the start-cell tags were seen up and have then been gone A1_SPILL_TAGS_GONE_SEC,
     * or A1_SPILL_HOLD_MAX_SEC after the last shot, whichever comes first (G409). TODO-MEASURE (07 Exp 1).
     */
    public static double A1_SPILL_HOLD_MAX_SEC = 6.0;                                             // TODO-MEASURE
    public static double A1_SPILL_TAGS_GONE_SEC = 1.0;                                            // TODO-MEASURE
    /**
     * VOLLEY 2 (A2 / A3 role 1): if the HIVE tracker does not say the second cell is up when the robot
     * arrives, wait up to this long for vision to see it before skipping the volley.
     */
    public static double AUTO_VOLLEY2_WAIT_FOR_B_SEC = 1.0;
    /**
     * A3 timing gates, measured from START (a start delay does not move them). Budget (RouteSweepTest and
     * AutoTimingTest check it): role 2 fires only if the TIP is confirmed by A3_ROLE2_LATEST_FIRE_SEC = 9.0 s;
     * + 1.0 s TIP_PAUSE_MS + 4 feeds (the 4th carrier push has ended by ~12.8 s even when no shot is
     * confirmed). Whatever happens (stalled volley, start delay), role 2 leaves for the CORNER slot at
     * A3_ROLE2_LEAVE_BY_SEC = 13.0 s at the latest - balls not fired by then stay inside. The park drive
     * (57.6 in) takes <= ~2.9 s at the shipped 0.5 speed cap (~1.4 s tuned): role 2 is parked by ~16 s, before
     * role 1 may leave the corridor end at A3_ROLE1_ZONE_B_NOT_BEFORE_SEC = 17.0 s. TODO-MEASURE (rehearse
     * both robots). The one park deadline for every routine stays AUTO_PARK_DEADLINE_SEC.
     */
    public static double A3_ROLE2_LATEST_FIRE_SEC = 9.0;                                          // TODO-MEASURE
    public static double A3_ROLE2_LEAVE_BY_SEC = 13.0;                                            // TODO-MEASURE
    public static double A3_ROLE1_ZONE_B_NOT_BEFORE_SEC = 17.0;                                   // TODO-MEASURE
    /**
     * A3 role 2: stop waiting for the partner's TIP this long after START. A TIP confirmed after
     * A3_ROLE2_LATEST_FIRE_SEC is recorded for TeleOp (HIVE state B) but role 2 keeps its preloads.
     */
    public static double A3_ROLE2_TIP_WAIT_TIMEOUT_SEC = 10.0;                                     // TODO-MEASURE
    /** A3 role 2: fire anyway at A3_ROLE2_LATEST_FIRE_SEC even if vision never saw the second cell come up. */
    public static boolean A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = false;

    // =============================================================================================
    // 15. TELEMETRY
    // =============================================================================================
    public static int TELEMETRY_INTERVAL_MS = 100;

    // ---------------------------------------------------------------------------------------------
    // Computed helpers (do not edit)
    // ---------------------------------------------------------------------------------------------
    /** Flywheel feed-forward gain for the hub velocity PIDF. */
    public static double flywheelF() {
        if (FLYWHEEL_F_OVERRIDE > 0) return FLYWHEEL_F_OVERRIDE;
        return 32767.0 / Math.max(1.0, FLYWHEEL_MAX_TICKS_PER_SEC);
    }

    public static double flywheelP() {
        return FLYWHEEL_P_OVER_F * flywheelF();
    }

    public static double flywheelI() {
        return FLYWHEEL_I_OVER_F * flywheelF();
    }
}
