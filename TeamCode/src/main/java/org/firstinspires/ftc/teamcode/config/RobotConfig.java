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
    /** goBILDA 5203 6000 rpm 1:1 flywheel motor (with its encoder cable plugged in!). */
    public static final String FLYWHEEL = "flywheel";
    /** Turret pinion servo (positional goBILDA Dual Mode servo, or the 5-turn version). */
    public static final String TURRET_SERVO = "turret";
    /**
     * OPTIONAL REV Through Bore Encoder on the turret. It plugs into the ENCODER port of a motor port,
     * so configure that motor port as any DC motor with this name (no motor needs to be attached).
     * Use motor port 0 or 3: those encoder inputs are decoded in hardware (ports 1/2 are software
     * decoded and can miss counts from an 8192 CPR encoder - ftc-docs hub tech tips).
     */
    public static final String TURRET_ENCODER = "turretEncoder";
    /** Intake roller: continuous-rotation servo. */
    public static final String INTAKE = "intake";
    /** "Internal spinners" that carry balls up through the turret centre: continuous-rotation servo. */
    public static final String INDEXER = "indexer";
    /** OPTIONAL second spinner servo (runs exactly like the first one). */
    public static final String INDEXER_2 = "indexer2";
    /** Gate servo that releases ONE ball into the flywheel. */
    public static final String FEEDER = "feeder";
    /** OPTIONAL hood servo. */
    public static final String HOOD = "hood";
    /** goBILDA Pinpoint odometry computer. Use an I2C bus OTHER than 0 (bus 0 shares the hub IMU). */
    public static final String PINPOINT = "pinpoint";
    /** Limelight 3A (only the 3A is legal - the 3G is prohibited by R702). */
    public static final String LIMELIGHT = "limelight";
    /** REV Color Sensor V3 / Brushland Color Rangefinder (I2C mode) at the intake mouth. */
    public static final String MOUTH_COLOR = "mouthColor";
    /** Colour/distance sensor that sees the 4th storage slot ("we are full"). */
    public static final String SLOT4_COLOR = "slot4";
    /** OPTIONAL IR break-beam (digital channel) where balls enter the flywheel. */
    public static final String FEED_BEAM = "feedBeam";

    // =============================================================================================
    // 2. DRIVETRAIN (SWYFT Drive V2 mecanum)
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
    /** Driver aim-assist (hold RB): turn power per degree of heading error. TODO-MEASURE (start small). */
    public static double AIM_ASSIST_TURN_KP = 0.015;
    /** Driver aim-assist: maximum turn power. */
    public static double AIM_ASSIST_MAX_TURN = 0.45;

    // =============================================================================================
    // 3. PINPOINT ODOMETRY (copy these from the Pedro AutoTune "Pinpoint Tuner" Java output)
    // =============================================================================================
    /** goBILDA pods: 4-bar (32 mm) or swing-arm (48 mm). Ignored when PINPOINT_CUSTOM_TICKS_PER_INCH > 0. */
    public static GoBildaPinpointDriver.GoBildaOdometryPods PINPOINT_POD_TYPE =
            GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;                            // TODO-MEASURE (which pods?)
    /**
     * For NON-goBILDA pods only (e.g. SWYFT Linear Odometry: 4096 counts / (pi x 38 mm) = 34.31 ticks/mm
     * = 871.5 ticks/in - but check whether SWYFT's "4096 PPR" means counts or pulses: R5 2.4).
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
    // 4. FLYWHEEL (goBILDA 5203 6000 rpm 1:1, 72 mm Hogback, 3D-printed C hood)
    // =============================================================================================
    /** Must spin so the ball leaves the robot. TODO-MEASURE (FlywheelTuner: spin at 1000 rpm and look). */
    public static DcMotorSimple.Direction FLYWHEEL_DIRECTION = DcMotorSimple.Direction.FORWARD;    // TODO-MEASURE
    /** 28 encoder counts per output revolution (goBILDA 5203 1:1 spec). Not a guess. */
    public static double FLYWHEEL_TICKS_PER_REV = 28.0;
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
    /** A sudden drop bigger than this fraction of the target means "a ball just went through". TODO-MEASURE */
    public static double FLYWHEEL_SHOT_DIP_FRACTION = 0.05;
    /** Hard cap: keep ~75% of free speed at 11.5 V for recovery torque (R3 6.3). */
    public static double FLYWHEEL_MAX_RPM = 5000.0;
    /** Operator dpad up/down changes the RPM trim by this much. */
    public static double FLYWHEEL_TRIM_STEP_RPM = 50.0;
    /** Fixed RPM used when the distance to the target is unknown (no odometry). TODO-MEASURE */
    public static double FLYWHEEL_DEFAULT_RPM = 3550.0;

    // =============================================================================================
    // 5. SHOT TABLE: horizontal distance (turret axis -> CELL opening centre, inches) -> flywheel RPM
    // =============================================================================================
    // TODO-MEASURE: these rows are the R3 physics MODEL (72 mm wheel, ~65 deg ball exit, exit speed =
    // 0.40 x wheel surface speed), converted from "lip distance" to "opening-centre distance"
    // (+2.8 in). They are a starting point only. Use ShotMapLogger (5 distances x 10 shots) and
    // replace them with the middle of the RPM band that scores.
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
     * k = 0.35 -> scale 1.14. TODO-MEASURE (slow-motion video, R3 section 11 experiment 1).
     */
    public static double SHOT_TABLE_RPM_SCALE = 1.0;
    /** Outside this distance band shots are unreliable (R3 recommendation: ~27-90 in). Telemetry warns. */
    public static double SHOT_MIN_DISTANCE_IN = 27.0;
    public static double SHOT_MAX_DISTANCE_IN = 90.0;

    // =============================================================================================
    // 6. HOOD (optional servo)
    // =============================================================================================
    /** Fixed hood position giving ~64-65 deg actual ball exit angle (R3 section 10). TODO-MEASURE (video) */
    public static double HOOD_POSITION = 0.50;                                                     // TODO-MEASURE

    // =============================================================================================
    // 7. TURRET (servo + 60T pinion driving a large ring gear)
    // =============================================================================================
    /**
     * Servo travel in degrees for servo position 0 -> 1. goBILDA Dual Mode servo: 300 (needs the 500-2500 us
     * PWM range below - the SDK default 600-2400 us only gives 270). goBILDA 5-TURN servo: 1800.
     */
    public static double TURRET_SERVO_RANGE_DEG = 300.0;
    public static double TURRET_PWM_MIN_US = 500.0;
    public static double TURRET_PWM_MAX_US = 2500.0;
    /** Ring gear teeth / pinion teeth (~4.5-5.5 from the photos). TODO-MEASURE: count the teeth. */
    public static double TURRET_GEAR_RATIO = 5.0;                                                  // TODO-MEASURE
    /** Servo position when the shooter points along TURRET_CENTER_DEG. TODO-MEASURE (TurretCalibrator A). */
    public static double TURRET_SERVO_CENTER = 0.50;                                               // TODO-MEASURE
    /** +1 if a BIGGER servo position turns the turret counter-clockwise (left, seen from above), else -1. */
    public static double TURRET_SERVO_DIRECTION = 1.0;                                             // TODO-MEASURE
    /**
     * Where the shooter points (relative to the ROBOT FRONT, CCW +) when the servo is at
     * TURRET_SERVO_CENTER: 0 = shooter faces the robot front, 180 = faces the back. TODO-MEASURE
     */
    public static double TURRET_CENTER_DEG = 0.0;                                                  // TODO-MEASURE
    /**
     * Soft limits, degrees from centre (+ = left/CCW). Keep 3-5 deg inside the hard stops.
     * 300 deg servo / 5:1 ring = only 60 deg total, so +-28. With a 5-turn servo you can go to +-150.
     * NOTE (R105.B): size limits must be enforced mechanically, never by these software limits.
     */
    public static double TURRET_MIN_DEG = -28.0;                                                   // TODO-MEASURE
    public static double TURRET_MAX_DEG = 28.0;                                                    // TODO-MEASURE
    /** Turret rotation axis position relative to the robot centre (inches; forward +, left +). */
    public static double TURRET_AXIS_FORWARD_IN = 0.0;                                             // TODO-MEASURE
    public static double TURRET_AXIS_LEFT_IN = 0.0;                                                // TODO-MEASURE
    /** How fast the turret really turns (deg/s), used to estimate where it is without an encoder. */
    public static double TURRET_SLEW_DEG_PER_SEC = 120.0;                                          // TODO-MEASURE (slow-mo video)
    /** Fire only when the turret is within this many degrees of the aim (R3: +-2-3 deg is enough). */
    public static double TURRET_AIM_TOLERANCE_DEG = 2.0;
    /** Operator dpad left/right turret trim step. */
    public static double TURRET_TRIM_STEP_DEG = 0.5;
    /** Operator right stick manual turret speed (deg/s at full stick) when auto-aim is OFF. */
    public static double TURRET_MANUAL_DEG_PER_SEC = 60.0;
    /** Set true when the REV Through Bore Encoder is installed and configured as TURRET_ENCODER. */
    public static boolean TURRET_USE_ENCODER = false;
    /**
     * Encoder ticks per TURRET degree. Through Bore = 8192 counts/rev. On the ring axis: 8192/360 = 22.76.
     * On the pinion/servo axis: 8192/360 x gear ratio. TODO-MEASURE (TurretCalibrator shows it).
     */
    public static double TURRET_ENCODER_TICKS_PER_DEG = 8192.0 / 360.0;                            // TODO-MEASURE
    /** +1 if encoder ticks increase when the turret turns CCW, else -1. */
    public static double TURRET_ENCODER_DIRECTION = 1.0;                                           // TODO-MEASURE
    /** Below this speed (deg/s) the encoder says the turret is not moving. */
    public static double TURRET_SETTLED_DEG_PER_SEC = 8.0;

    // =============================================================================================
    // 8. FEEDER GATE + INDEXER ("internal spinners") + BALL COUNT
    // =============================================================================================
    /** G407: a robot may CONTROL at most 4 scoring elements. Do not change. */
    public static final int MAX_BALLS = 4;
    public static double FEEDER_CLOSED_POSITION = 0.20;                                           // TODO-MEASURE
    public static double FEEDER_OPEN_POSITION = 0.55;                                             // TODO-MEASURE
    /** How long the gate stays open: long enough for ONE ball, too short for two. TODO-MEASURE */
    public static long FEEDER_OPEN_MS = 180;
    /** Wait this long for the beam-break / flywheel dip to confirm the ball left. */
    public static long SHOT_CONFIRM_TIMEOUT_MS = 400;
    /** Minimum time between two balls (R3: 0.15-0.25 s; the flywheel must also be back at speed). */
    public static long MIN_SHOT_INTERVAL_MS = 150;
    /**
     * If neither the beam-break nor the flywheel dip confirms a shot, still count the ball as gone?
     * true  = the count may go too LOW after a jam, but the slot-4 sensor forces it back to 4 (G407 safe).
     * false = the count only drops on a confirmed shot (needs feedBeam or a tuned dip).
     */
    public static boolean COUNT_UNCONFIRMED_SHOTS = true;
    public static DcMotorSimple.Direction INDEXER_DIRECTION = DcMotorSimple.Direction.FORWARD;     // TODO-MEASURE
    /** Indexer power while a ball is pushed through the open gate. */
    public static double INDEXER_FEED_POWER = 1.0;
    /** Indexer power while packing balls up toward the closed gate (intaking / between shots). */
    public static double INDEXER_STAGE_POWER = 0.5;                                               // TODO-MEASURE
    /** How long to run the indexer to stage the next ball against the gate. */
    public static long INDEXER_STAGE_MS = 300;                                                    // TODO-MEASURE
    /** Keep packing this long after the intake stops. */
    public static long INDEXER_RUN_AFTER_INTAKE_MS = 500;
    public static double INDEXER_EJECT_POWER = -1.0;

    // =============================================================================================
    // 9. INTAKE (full-width compliant roller on a continuous-rotation servo)
    // =============================================================================================
    public static DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;      // TODO-MEASURE
    /** Which side of the robot the intake mouth is on (relative to the robot front, CCW +): 0 = front, 180 = back. */
    public static double INTAKE_FACING_DEG = 0.0;                                                 // TODO-MEASURE
    public static double INTAKE_IN_POWER = 1.0;
    public static double INTAKE_OUT_POWER = -1.0;
    /** When we reach 4 balls, spin the roller backward this long to push a 5th ball away (G407). */
    public static long INTAKE_FULL_REVERSE_MS = 150;
    /** Opponent NECTAR seen at the mouth: run the roller backward this long (G408). */
    public static long INTAKE_REJECT_MS = 400;

    // =============================================================================================
    // 10. COLOUR SENSORS (mouth + slot 4) and BREAK-BEAM
    // =============================================================================================
    /** Colour sensor gain (REV V3 reads dark values; higher gain = brighter). TODO-MEASURE (SensorCheck) */
    public static float COLOR_SENSOR_GAIN = 2.0f;
    /** A ball is "at the mouth" when the distance reading is below this (cm). TODO-MEASURE */
    public static double MOUTH_BALL_PRESENT_CM = 4.0;
    /** A ball is "in slot 4" when the slot sensor reads below this (cm). TODO-MEASURE */
    public static double SLOT4_BALL_PRESENT_CM = 3.0;
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
     * A ball must be out of the mouth sensor's view this long before the NEXT ball can be counted.
     * Stops one ball that flickers in and out of view from counting twice. TODO-MEASURE (SensorCheck).
     */
    public static long BALL_COUNT_DEBOUNCE_MS = 60;                                               // TODO-MEASURE
    /** IR break-beam receivers usually pull the line LOW when the beam is broken. */
    public static boolean FEED_BEAM_BROKEN_IS_LOW = true;

    // =============================================================================================
    // 11. LIMELIGHT 3A (on the turret, pitched up ~35-40 deg; AprilTag pipeline, own 8 tags, 640x480)
    // =============================================================================================
    public static int LIMELIGHT_APRILTAG_PIPELINE = 0;
    public static int LIMELIGHT_POLL_HZ = 100;
    /** Ignore Limelight results older than this. */
    public static long VISION_MAX_STALENESS_MS = 100;
    /** true = camera turns with the turret (brief). false = fixed to the chassis. */
    public static boolean CAMERA_ON_TURRET = true;
    /** Lens height above the tiles. TODO-MEASURE */
    public static double CAMERA_HEIGHT_IN = 14.0;                                                 // TODO-MEASURE
    /** How far the camera is tilted UP from horizontal. TODO-MEASURE (brief: 35-40) */
    public static double CAMERA_PITCH_DEG = 37.0;                                                 // TODO-MEASURE
    /** Lens distance in front of the turret axis (or robot centre if not on the turret). TODO-MEASURE */
    public static double CAMERA_FORWARD_IN = 3.0;                                                 // TODO-MEASURE
    /** Camera yaw relative to the shooter (or robot front if not on the turret), CCW +. TODO-MEASURE */
    public static double CAMERA_YAW_OFFSET_DEG = 0.0;                                             // TODO-MEASURE
    /** Up-cell tags are ~49.8 in high, down-cell tags ~35.5 in (R2 7.3, CAD); split the difference. */
    public static double TAG_UP_HEIGHT_THRESHOLD_IN = 42.7;
    /** Use vision to correct the odometry aim (low-pass "bias"). */
    public static boolean VISION_BIAS_ENABLED = true;
    /** Fraction of each new measurement mixed into the bias (0.15 = smooth, slow). */
    public static double VISION_BIAS_GAIN = 0.15;
    /** The bias may never exceed this (protects against a bad frame / wrong tag). */
    public static double VISION_BIAS_MAX_DEG = 8.0;
    /** Ignore a frame whose correction differs from the current bias by more than this. */
    public static double VISION_BIAS_OUTLIER_DEG = 6.0;
    /** How fast the bias fades back to 0 when no tags are seen (fraction per second). */
    public static double VISION_BIAS_DECAY_PER_SEC = 0.1;

    // =============================================================================================
    // 12. HIVE TRACKING
    // =============================================================================================
    /** Let vision switch the target cell by itself (needs a calibrated camera - check in LimelightCheck). */
    public static boolean HIVE_VISION_AUTO_SWITCH = true;
    /** Vision must disagree with the current state this long before we switch. */
    public static long HIVE_VISION_CONFIRM_MS = 500;
    /** At least this many own tags must be seen for vision evidence to count. */
    public static int HIVE_VISION_MIN_TAGS = 1;
    /** After the operator presses "HIVE tipped", vision may not override for this long. */
    public static long HIVE_OPERATOR_PRIORITY_MS = 5000;
    /**
     * Pause launching after a TIP is detected. Manual 10.5.1 note: launching at a HIVE while it is
     * tipping "may disrupt its movement" and the TIP might not count.
     */
    public static long TIP_PAUSE_MS = 1000;
    /** AUTO: the start cell already holds 3 own NECTAR, so ~3 POLLEN tip it (EFSG section 12). */
    public static int AUTO_ASSUME_TIP_AFTER_BALLS = 3;

    // =============================================================================================
    // 13. FIRING CONDITIONS (stop to shoot - R6 5.5, R3 recommendation)
    // =============================================================================================
    public static double FIRE_MAX_CHASSIS_SPEED_IN_PER_SEC = 6.0;
    public static double FIRE_MAX_CHASSIS_TURN_DEG_PER_SEC = 20.0;

    // =============================================================================================
    // 14. MATCH CUES
    // =============================================================================================
    public static double TELEOP_LENGTH_SEC = 120.0;
    /** 1:00 left = FLOWER ownership unlocked chime; human player enters remaining NECTAR (G426). */
    public static double CUE_ONE_MINUTE_LEFT_SEC = 60.0;
    /** 0:20 left = train whistle; finish the last cycle and head for the LOADING ZONE. */
    public static double CUE_TWENTY_SECONDS_LEFT_SEC = 20.0;

    // =============================================================================================
    // 15. AUTO
    // =============================================================================================
    /** After this many seconds, whatever we are doing, drive to the LOADING ZONE (LEAVE 3 + PARK 5). */
    public static double AUTO_PARK_DEADLINE_SEC = 23.0;                                           // TODO-MEASURE
    /** Give up on a volley after this long (e.g. a jam) and move on. */
    public static double AUTO_VOLLEY_TIMEOUT_SEC = 5.0;
    /** Give up on any single path after this long (robot blocked / pushed). */
    public static double AUTO_PATH_TIMEOUT_SEC = 6.0;
    /** A1 from S1: shoot from the start spot (true) or drive to the Zone A shoot spot first (false). */
    public static boolean A1_FIRE_FROM_START = true;
    /** Garden sweep speed as a fraction of full path speed (slow so the servo intake keeps up). */
    public static double AUTO_SWEEP_SPEED = 0.30;                                                 // TODO-MEASURE
    /** Keep the intake running this long after the sweep path ends. */
    public static double AUTO_SWEEP_DWELL_SEC = 0.6;
    /**
     * A3 role 2: wait at most this long for the partner's TIP before giving up and parking. Keep it short
     * enough that role 2 has left the far half before role 1 arrives in Zone B (~12 s). TODO-MEASURE
     * (rehearse both robots together).
     */
    public static double A3_ROLE2_TIP_WAIT_TIMEOUT_SEC = 10.0;
    /** A3 role 2: fire anyway at the timeout even if vision never saw the far cell come up. */
    public static boolean A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = false;

    // =============================================================================================
    // 16. TELEMETRY
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

    /** Turret degrees moved when the servo goes from position 0 to 1. */
    public static double turretDegreesPerServoUnit() {
        return TURRET_SERVO_RANGE_DEG / Math.max(0.01, TURRET_GEAR_RATIO);
    }
}
