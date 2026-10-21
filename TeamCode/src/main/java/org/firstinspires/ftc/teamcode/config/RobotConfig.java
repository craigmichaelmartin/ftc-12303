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
    /**
     * Turret pinion servo: a POSITIONAL goBILDA Dual Mode servo, or the 5-turn version. Never put it in
     * continuous mode: Turret.java only sends positions, so a continuous-rotation turret would keep
     * spinning into its stops (a continuous turret needs new closed-loop code).
     */
    public static final String TURRET_SERVO = "turret";
    /**
     * OPTIONAL REV Through Bore Encoder for turret FEEDBACK (the servo stays positional). The ring centre
     * is the ball path, so the encoder sits on an idler gear meshing with the ring (or on the pinion shaft).
     * It plugs into the ENCODER port of a motor port, so configure that motor port as any DC motor with
     * this name (no motor needs to be attached). Use motor port 0 or 3: those encoder inputs are decoded
     * in hardware (ports 1/2 are software decoded and can miss counts from an 8192 CPR encoder).
     */
    public static final String TURRET_ENCODER = "turretEncoder";
    /** Intake roller: a continuous-rotation servo OR a DC motor - configure "intake" as either one. */
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
    /**
     * REV Color Sensor V3, or a Brushland Color Rangefinder in I2C mode (its digital outputs are NOT read
     * by this code), at the intake mouth. Mount it where every ball passes within MOUTH_BALL_PRESENT_CM
     * before the roller commits it - including garden balls taken with the edge of the roller.
     */
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
    /**
     * Encoder counts per FLYWHEEL revolution = 28 counts per motor revolution (goBILDA 5203 1:1) x motor
     * turns per wheel turn. The photos suggest the motor drives the wheel through a right-angle bevel/miter
     * pair: if that pair is not 1:1, multiply 28 by the ratio. TODO-MEASURE (mark the wheel, count motor
     * turns per wheel turn). Every "rpm" the code shows is wheel rpm only when this is right.
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
     * through" (one shot dip per excursion). TODO-MEASURE with FlywheelTuner B (watch "last shot: drop").
     */
    public static double FLYWHEEL_SHOT_DIP_FRACTION = 0.05;
    /** Ignore speed drops this soon after a target change of more than 2% (it is a new target, not a ball). */
    public static long FLYWHEEL_DIP_IGNORE_AFTER_TARGET_CHANGE_MS = 100;
    /**
     * Hard safety cap (~83% of the 6000 rpm free speed). R3 6.3: above ~4,300 rpm (75% of free speed at
     * 11.5 V) recovery between shots suffers - if the tuned table needs more, consider the 96 mm wheel.
     */
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
    /**
     * Reliable shot band in OPENING-CENTRE distance (what aim.distanceIn measures): R3's ~27-90 in LIP
     * distance + ~2.8 in = ~30-92 in (same band as the playbook cone). Telemetry warns outside it.
     */
    public static double SHOT_MIN_DISTANCE_IN = 30.0;
    public static double SHOT_MAX_DISTANCE_IN = 92.0;

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
    /** Ring gear teeth / pinion teeth (~4-6 from the photos, rough). TODO-MEASURE: count the teeth. */
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
     * Standard 300 deg servo: travel = 300 / ratio, so limits = +-(150 / ratio - 3): +-28 at 5:1 (only valid
     * up to a 5.36 ratio). 5-turn servo (1800 deg): +-(900 / ratio - 5). TurretCalibrator measures them.
     * With +-28, S7 cannot shoot the second cell (31.1 deg off the turret centre): A3 role 2 needs S6.
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
    /**
     * Set true when the REV Through Bore Encoder is installed and configured as TURRET_ENCODER - but only
     * AFTER TurretCalibrator has measured TURRET_ENCODER_TICKS_PER_DEG (a wrong scale makes the turret look
     * unsettled; the code then re-zeroes the encoder and warns, see Turret).
     */
    public static boolean TURRET_USE_ENCODER = false;
    /**
     * Encoder ticks per TURRET degree. The encoder sits on an idler gear meshing with the ring (the ring
     * centre is the ball path), so ticks/deg = 8192 / 360 x (ring teeth / idler teeth): about 114 for a
     * 60T idler on a ~300T ring (the default below assumes ratio 5). Placeholder only: run
     * TurretCalibrator (X mark) with TURRET_USE_ENCODER = true and paste its value BEFORE a match.
     */
    public static double TURRET_ENCODER_TICKS_PER_DEG = 8192.0 / 360.0 * 5.0;                      // TODO-MEASURE
    /** +1 if encoder ticks increase when the turret turns CCW, else -1. */
    public static double TURRET_ENCODER_DIRECTION = 1.0;                                           // TODO-MEASURE
    /** Below this speed (deg/s) the encoder says the turret is not moving. */
    public static double TURRET_SETTLED_DEG_PER_SEC = 8.0;
    /**
     * Encoder and command disagree by more than TURRET_AIM_TOLERANCE_DEG while both have been still this
     * long: treat it as skipped teeth / a wrong encoder scale, re-zero the encoder to the command and warn
     * (instead of blocking every shot for the rest of the OpMode).
     */
    public static long TURRET_ENCODER_REZERO_AFTER_MS = 500;
    /**
     * FALLBACK when the turret misbehaves: pin it mechanically at its centre and set this true. The servo
     * is then held at TURRET_SERVO_CENTER, auto-aim checks the CHASSIS heading instead (|aim error| <=
     * CHASSIS_AIM_TOLERANCE_DEG) - the driver's RB aim-assist turns the chassis in TeleOp, AUTO turns the
     * robot itself before a volley. Same behaviour when no "turret" servo is configured.
     * Never use TURRET_MIN_DEG = TURRET_MAX_DEG = 0 for this: nothing would ever fire.
     */
    public static boolean TURRET_PINNED = false;
    /** Chassis aim tolerance used when the turret is pinned or missing. */
    public static double CHASSIS_AIM_TOLERANCE_DEG = 3.0;

    // =============================================================================================
    // 8. FEEDER GATE + INDEXER ("internal spinners") + BALL COUNT
    // =============================================================================================
    /** G407: a robot may CONTROL at most 4 scoring elements. Do not change. */
    public static final int MAX_BALLS = 4;
    public static double FEEDER_CLOSED_POSITION = 0.20;                                           // TODO-MEASURE
    public static double FEEDER_OPEN_POSITION = 0.55;                                             // TODO-MEASURE
    /** How long the gate stays open: long enough for ONE ball, too short for two. TODO-MEASURE */
    public static long FEEDER_OPEN_MS = 180;
    /**
     * Wait this long after the gate closes for the beam-break / flywheel dip to confirm the ball left.
     * No confirmation = an "unconfirmed feed": telemetry counts it and the operator pad gives 4 blips.
     */
    public static long SHOT_CONFIRM_TIMEOUT_MS = 400;
    /**
     * Minimum gap from one ball's confirmation (or confirm timeout) to the next gate opening. The real
     * floor between balls is FEEDER_OPEN_MS + this (0.33 s), plus the flywheel getting back into its
     * window; an unconfirmed feed adds SHOT_CONFIRM_TIMEOUT_MS (0.73 s between gate openings).
     */
    public static long MIN_SHOT_INTERVAL_MS = 150;
    /**
     * If neither the beam-break nor the flywheel dip confirms a shot, still count the ball as gone?
     * true  = the count may go too LOW after a jam, but the slot-4 sensor forces it back to 4 (G407 safe).
     * false = the count only drops on a confirmed shot (needs feedBeam or a tuned dip).
     * There is NO automatic jam reverse/retry: clear jams with operator LB (eject), fix the count with
     * operator back (-1) / left-stick click (+1).
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
    /**
     * When we reach 4 balls, spin the roller backward this long to push a 5th ball away (G407). Ball 4 is
     * counted once it has passed the mouth sensor - or at once when a 5th ball shows up right behind it
     * (MOUTH_ONE_BALL_MAX_MS) - so the pulse hits the 5th ball, not ball 4.
     */
    public static long INTAKE_FULL_REVERSE_MS = 150;
    /**
     * Refused NECTAR at the mouth (G408): the roller runs backward for as long as the refused ball is still
     * at the mouth, and this much longer after it is gone.
     */
    public static long INTAKE_REJECT_MS = 400;
    /**
     * Phase 1 (hood gapped for 2.8 in POLLEN): false = the mouth refuses ALL NECTAR (ours too), because a
     * 3.6 in ball would jam the hood. Set true only after the NECTAR shot test (07 Exp 6) is GO; then only
     * the opponent's NECTAR is refused (G408). Drivers still steer around NECTAR: the sensor reacts on contact.
     */
    public static boolean ACCEPT_OWN_NECTAR = false;

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
     *  - a ball still in view MOUTH_ONE_BALL_MAX_MS after the roller started pulling it is TWO balls touching
     *    (no gap the sensor can see): ball 4 is counted then, and the full pulse pushes the 5th away.
     * Set it to the LONGEST time one ball stays in the mouth sensor's view while the roller pulls it in, + 30%.
     * Too short: a slow ball 4 is counted early and pushed back out (BALLS 4 with 3 inside - operator back = -1).
     * Too long: a touching 5th ball gets in. TODO-MEASURE ("Check: Sensors": hold A, pull 10 single POLLEN
     * through, read "longest"). The slot-4 sensor is read every MOUTH_SENSOR_PERIOD_MS while ball 4 comes in.
     */
    public static long MOUTH_ONE_BALL_MAX_MS = 300;                                               // TODO-MEASURE
    /** IR break-beam receivers usually pull the line LOW when the beam is broken. */
    public static boolean FEED_BEAM_BROKEN_IS_LOW = true;

    // =============================================================================================
    // 11. LIMELIGHT 3A (on the turret, pitched up ~35-40 deg). ONE AprilTag pipeline for both alliances:
    //     ID filter 30-45 (the code keeps only our 8), marker size 82.55 mm, Full 3D ON, 640x480.
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
    /** Ignore a frame whose correction differs from the current bias by more than this... */
    public static double VISION_BIAS_OUTLIER_DEG = 6.0;
    /** ...unless this many outlier frames in a row agree within VISION_BIAS_OUTLIER_AGREE_DEG: then accept them. */
    public static int VISION_BIAS_OUTLIER_ACCEPT_FRAMES = 5;
    public static double VISION_BIAS_OUTLIER_AGREE_DEG = 2.0;
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
    /**
     * After the operator presses Y ("HIVE tipped"), vision may not override for this long. After that,
     * 0.5 s of disagreeing vision can switch it back (set HIVE_VISION_AUTO_SWITCH = false if it keeps
     * flipping wrongly).
     */
    public static long HIVE_OPERATOR_PRIORITY_MS = 5000;
    /** A Y press this soon after a CAMERA switch confirms that switch instead of flipping back. */
    public static long HIVE_Y_CONFIRMS_VISION_MS = 3000;
    /**
     * Pause launching this long after the target cell changes (operator Y or vision) while the turret and
     * flywheel retarget; any requested volley is cancelled (press RB again). It starts after the HIVE has
     * moved (or late in its swing), so it is NOT the manual 10.5.1 "don't launch at a tipping HIVE"
     * protection - the drive team handles that (03).
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
    // 13. FIRING CONDITIONS (stop to shoot - R6 5.5, R3 recommendation)
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
    // 14. MATCH CUES
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
    // 15. AUTO
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
    /** Garden sweep speed as a fraction of full path speed (slow so the servo intake keeps up). */
    public static double AUTO_SWEEP_SPEED = 0.30;                                                 // TODO-MEASURE
    /** Keep the intake running this long after the sweep path ends. */
    public static double AUTO_SWEEP_DWELL_SEC = 0.6;
    /**
     * After the garden sweep, VOLLEY 2 cycles the gate at least this many times even if the ball count is
     * lower (garden balls taken with the roller edge can miss the mouth sensor). Empty cycles are harmless;
     * with a feed beam fitted it stops after 2 unconfirmed (empty) cycles in a row. 0 = off.
     */
    public static int AUTO_VOLLEY2_MIN_GATE_CYCLES = 4;
    /**
     * A1 only: after volley 1, hold at the firing pose (outside the tip-1 landing box) until the spill has
     * landed: leave once the start-cell tags were seen up and have then been gone A1_SPILL_TAGS_GONE_SEC,
     * or A1_SPILL_HOLD_MAX_SEC after the last shot, whichever comes first (G409). TODO-MEASURE (07 Exp 1).
     */
    public static double A1_SPILL_HOLD_MAX_SEC = 6.0;
    public static double A1_SPILL_TAGS_GONE_SEC = 1.0;
    /**
     * VOLLEY 2 (A2 / A3 role 1): if the HIVE tracker does not say the second cell is up when the robot
     * arrives, wait up to this long for vision to see it before skipping the volley.
     */
    public static double AUTO_VOLLEY2_WAIT_FOR_B_SEC = 1.0;
    /**
     * A3 timing gates, measured from START (a start delay does not move them). Budget (RouteSweepTest and
     * AutoTimingTest check it): role 2 fires only if the TIP is confirmed by A3_ROLE2_LATEST_FIRE_SEC = 9.0 s;
     * + 1.0 s TIP_PAUSE_MS + 0.3 s staging + 4 feeds (the 4th gate opens by ~12.5 s even when no shot is
     * confirmed). Whatever happens (stalled volley, start delay), role 2 leaves for the CORNER slot at
     * A3_ROLE2_LEAVE_BY_SEC = 13.0 s at the latest - balls not fired by then stay inside. The park drive
     * (57.6 in) takes <= ~2.9 s at the shipped 0.5 speed cap (~1.4 s tuned): role 2 is parked by ~16 s, before
     * role 1 may leave the corridor end at A3_ROLE1_ZONE_B_NOT_BEFORE_SEC = 17.0 s. TODO-MEASURE (rehearse
     * both robots). The one park deadline for every routine stays AUTO_PARK_DEADLINE_SEC.
     */
    public static double A3_ROLE2_LATEST_FIRE_SEC = 9.0;
    public static double A3_ROLE2_LEAVE_BY_SEC = 13.0;
    public static double A3_ROLE1_ZONE_B_NOT_BEFORE_SEC = 17.0;
    /**
     * A3 role 2: stop waiting for the partner's TIP this long after START. A TIP confirmed after
     * A3_ROLE2_LATEST_FIRE_SEC is recorded for TeleOp (HIVE state B) but role 2 keeps its preloads.
     */
    public static double A3_ROLE2_TIP_WAIT_TIMEOUT_SEC = 10.0;
    /** A3 role 2: fire anyway at A3_ROLE2_LATEST_FIRE_SEC even if vision never saw the second cell come up. */
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
