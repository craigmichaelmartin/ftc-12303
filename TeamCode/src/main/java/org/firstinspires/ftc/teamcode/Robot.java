package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Aim;
import org.firstinspires.ftc.teamcode.subsystems.Carrier;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.PoseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The whole robot: every subsystem plus ONE update() that runs them in the right order each loop.
 *
 * LOOP ORDER (read -> think -> write):
 *   1. clear the bulk cache on both hubs (MANUAL bulk caching: every motor encoder / digital input
 *      is read from the hubs in ONE message per hub per loop - SDK sample ConceptMotorBulkRead)
 *   2. drivetrain (reads the Pinpoint over I2C, drives the wheels)
 *   3. vision -> HIVE tracker (which cell is up?) -> aim (heading + distance) -> flywheel speed
 *   4. carrier (count, colour, feed) -> intake (obeys "full" and refuses NECTAR at the mouth)
 *
 * The OpMode sets what it WANTS (drive sticks, auto-aim on/off, fire, intake) BEFORE calling update().
 *
 * AIMING: the shooter is fixed and shoots out of the robot's BACK, so the robot aims by turning
 * (subsystems/Aim). TeleOp: the driver holds RB (aim-assist). AUTO: the shooting poses face the target
 * and the robot turns itself onto the aim heading during a volley.
 *
 * FIRING GATES (the carrier feeds a ball only when all are OK):
 *   flywheel at speed, and - with AUTO-AIM on - the target cell's opening faces the robot (off-axis <=
 *   FIRE_MAX_OFF_AXIS_DEG), the robot's heading is inside the aim tolerance (computed from the distance
 *   and the opening's width), the chassis is still, and no TIP pause.
 *   MANUAL aim (auto-aim off) is a deliberate operator override: only the flywheel speed gates it.
 *
 * G403 / G304.H: building a Robot never moves anything, and nothing moves before arm() (at START).
 */
public class Robot {
    public final Drivetrain drive;
    public final Shooter shooter;
    public final Vision vision;
    public final HiveTracker hive;
    public final Carrier carrier;
    public final Intake intake;
    /** Problems found while starting up (missing devices, ...). Show them in INIT telemetry. */
    public final List<String> warnings = new ArrayList<>();

    private final List<LynxModule> hubs;
    private final VoltageSensor battery;
    private Alliance alliance;
    private boolean armed = false;
    private boolean autoAim = false;
    private boolean spinUp = false;
    private double manualRpm = 0.0;
    private double aimTrimDeg = 0.0;
    private HiveState aimOverride = null;
    private Aim.Solution aim = new Aim.Solution();

    private double batteryVolts = 0.0;
    private long lastBatteryReadMs = 0;
    private long lastLoopNs = 0;
    private double loopMs = 0.0;

    // things that happened this loop (each true for exactly one loop)
    private boolean tipEvent, confirmEvent, fullEvent, rejectEvent, shotEvent, unconfirmedEvent;

    public Robot(HardwareMap hw, Alliance alliance) {
        this.alliance = alliance;
        hubs = hw.getAll(LynxModule.class);
        for (LynxModule hub : hubs) hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        VoltageSensor vs = null;
        for (VoltageSensor s : hw.voltageSensor) {
            vs = s;
            break;
        }
        battery = vs;

        drive = new Drivetrain(hw, warnings);
        shooter = new Shooter(hw, warnings);
        vision = new Vision(hw, alliance, warnings);
        hive = new HiveTracker(alliance);
        carrier = new Carrier(hw, alliance, warnings);
        intake = new Intake(hw, warnings);
    }

    /** Change alliance (AUTO selector / TeleOp INIT). Updates every subsystem that cares. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
        vision.setAlliance(alliance);
        hive.setAlliance(alliance);
        carrier.setAlliance(alliance);
    }

    public Alliance alliance() {
        return alliance;
    }

    /**
     * Allow every actuator to move. TeleOp: call right after START (never in INIT - G403).
     * AUTO: call at START (AUTO INIT moves nothing, so the robot is motionless after INIT - G304.H).
     */
    public void arm() {
        armed = true;
        drive.arm();
        shooter.arm();
        carrier.arm();
        intake.arm();
    }

    public boolean isArmed() {
        return armed;
    }

    // ---------------------------------------------------------------------------------------------
    // What the OpMode wants
    // ---------------------------------------------------------------------------------------------
    /** Auto-aim on: shots wait until the robot's back points at the up cell. Off = manual aim. */
    public void setAutoAim(boolean on) {
        autoAim = on;
    }

    public boolean autoAim() {
        return autoAim;
    }

    /** Flywheel spins to the shot-table RPM for the current distance. */
    public void setSpinUp(boolean on) {
        spinUp = on;
    }

    public boolean spinUp() {
        return spinUp;
    }

    /** Spin-up + auto-aim together (the operator's main toggle when manual aim is off). */
    public void setShootingMode(boolean on) {
        setAutoAim(on);
        setSpinUp(on);
    }

    /** Tuning OpModes: hold this exact RPM instead of the shot table (0 = back to normal). */
    public void setManualRpm(double rpm) {
        manualRpm = Math.max(0.0, rpm);
    }

    public double manualRpm() {
        return manualRpm;
    }

    /**
     * Operator aim trim (deg, + = the robot turns counter-clockwise = the ball goes further left, seen from
     * the shooter looking toward the cell).
     */
    public void addAimTrim(double deltaDeg) {
        aimTrimDeg += deltaDeg;
    }

    public double aimTrimDeg() {
        return aimTrimDeg;
    }

    /**
     * Aim at this cell no matter what the HIVE tracker says (null = follow the tracker).
     * AUTO A3 role 2 uses it to pre-aim at the second cell while waiting for the partner's TIP.
     */
    public void setAimOverride(HiveState cell) {
        aimOverride = cell;
    }

    /** The cell we are aiming at right now. */
    public HiveState targetCell() {
        return aimOverride != null ? aimOverride : hive.state();
    }

    // ---------------------------------------------------------------------------------------------
    // The loop
    // ---------------------------------------------------------------------------------------------
    public void update() {
        for (LynxModule hub : hubs) hub.clearBulkCache();
        timeLoop();
        readBattery();

        drive.update();
        Pose pose = drive.pose();

        // Which cell is up, and how far off is our aim? (bias only learned while the chassis is still)
        boolean stable = Math.abs(drive.turnRateDegPerSec()) < 15.0
                && drive.speedInPerSec() < RobotConfig.FIRE_MAX_CHASSIS_SPEED_IN_PER_SEC;
        vision.update(pose, targetCell(), stable);
        hive.update(vision);
        tipEvent = hive.consumeTipEvent();
        confirmEvent = hive.consumeConfirmEvent();
        if (tipEvent) {
            carrier.pauseForTip();
            vision.resetBias();
        }

        // Aim (heading the robot needs + distance) and flywheel speed
        FieldPoint target = FieldConstants.aimPoint(alliance, targetCell());
        aim = Aim.solve(target, pose, offAxisDeg(), aimTrimDeg, vision.aimBiasDeg());
        if (manualRpm > 0) {
            shooter.setTargetRpm(manualRpm);
        } else if (spinUp) {
            if (drive.hasLocalizer()) shooter.setTargetForDistance(aim.distanceIn);
            else shooter.setTargetRpm(RobotConfig.FLYWHEEL_DEFAULT_RPM);
        } else {
            shooter.stop();
        }
        shooter.update();

        // Balls
        carrier.update(shooter.atSpeed(), aimReady(), stillEnoughToFire(), hive.tipPauseActive(),
                shooter.consumeShotDip(), intake.isIntaking(), intake.isReversing());
        fullEvent = carrier.consumeFullEvent();
        shotEvent = carrier.consumeShotEvent();
        unconfirmedEvent = carrier.consumeUnconfirmedEvent();
        if (shotEvent) hive.onBallLaunched();
        // Level-triggered refusal (G408): the intake keeps reversing while a refused ball is at the mouth.
        intake.update(carrier.isFull(), carrier.refusedBallAtMouth());
        rejectEvent = intake.consumeRejectStarted();
    }

    /** Fresh sensor reads without running the subsystems (FlywheelTuner's raw max-speed test only). */
    public void clearBulkCache() {
        for (LynxModule hub : hubs) hub.clearBulkCache();
    }

    private void timeLoop() {
        long now = System.nanoTime();
        if (lastLoopNs != 0) {
            double ms = (now - lastLoopNs) / 1e6;
            loopMs = (loopMs == 0.0) ? ms : 0.9 * loopMs + 0.1 * ms;
        }
        lastLoopNs = now;
    }

    private void readBattery() {
        long now = System.currentTimeMillis();
        if (battery == null || now - lastBatteryReadMs < 500) return;   // not in the bulk read: ~2 ms each
        lastBatteryReadMs = now;
        batteryVolts = battery.getVoltage();
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    /** Latest aim solution (distance, aim heading, heading error, tolerance). */
    public Aim.Solution aim() {
        return aim;
    }

    /** Driver aim-assist turn power (+ = counter-clockwise) for the latest aim; 0 without odometry. */
    public double aimAssistTurn() {
        return drive.hasLocalizer() ? Aim.assistTurn(aim) : 0.0;
    }

    /**
     * True when the target cell's opening faces away from the robot (off-axis > FIRE_MAX_OFF_AXIS_DEG):
     * wrong zone, or the HIVE tipped without us noticing. Needs odometry.
     */
    public boolean targetFacesAway() {
        if (!drive.hasLocalizer()) return false;
        return offAxisDeg() > RobotConfig.FIRE_MAX_OFF_AXIS_DEG;
    }

    /** How far off the target opening's axis the robot is (deg; 0 = straight in front). */
    public double offAxisDeg() {
        Pose p = drive.pose();
        return FieldConstants.offAxisDeg(alliance, targetCell(), p.x(), p.y());
    }

    /**
     * Aim OK for a shot? Manual aim (auto-aim off): always - a deliberate override. Auto-aim: the target
     * must face us and the robot's heading must be inside the aim tolerance (Aim.readyToFire). Without
     * odometry the aim cannot be checked: only the flywheel gates the shot.
     */
    public boolean aimReady() {
        if (!autoAim || !drive.hasLocalizer()) return true;
        return Aim.readyToFire(aim, offAxisDeg());
    }

    /** Auto-aim shots need a still chassis (stop to shoot); manual aim is gated on flywheel speed only. */
    private boolean stillEnoughToFire() {
        return !autoAim || drive.isStill();
    }

    /** Everything ready for the next ball? (flywheel, aim, chassis still, no TIP pause) */
    public boolean readyToFire() {
        return shooter.atSpeed() && aimReady() && stillEnoughToFire() && !hive.tipPauseActive();
    }

    public boolean tipEventThisLoop() {
        return tipEvent;
    }

    /** A Y press confirmed a recent camera switch this loop (no state change). */
    public boolean confirmEventThisLoop() {
        return confirmEvent;
    }

    public boolean fullEventThisLoop() {
        return fullEvent;
    }

    /** The intake started reversing to refuse NECTAR this loop. */
    public boolean rejectEventThisLoop() {
        return rejectEvent;
    }

    public boolean shotEventThisLoop() {
        return shotEvent;
    }

    /** A feed finished without beam or dip confirmation this loop (possible jam). */
    public boolean unconfirmedFeedThisLoop() {
        return unconfirmedEvent;
    }

    public double batteryVolts() {
        return batteryVolts;
    }

    public double loopMs() {
        return loopMs;
    }

    /** AUTO -> TeleOp hand-off (call every AUTO loop). */
    public void saveToPoseStorage() {
        PoseStorage.save(drive.pose(), alliance, carrier.count(), hive.state());
    }

    /** Standard status lines for any OpMode. */
    public void addTelemetry(Telemetry t) {
        Pose p = drive.pose();
        t.addLine(String.format(Locale.US, "BALLS %d/%d%s | mouth %s | slot4 %s | intake %s",
                carrier.count(), RobotConfig.MAX_BALLS, carrier.isFull() ? " FULL" : "",
                carrier.hasMouthSensor() ? carrier.mouthBall().toString() : "NO SENSOR (count drops per feed)",
                carrier.hasSlot4Sensor() ? (carrier.slot4Occupied() ? "BALL" : "-") : "no sensor", intake.state()));
        t.addLine(String.format(Locale.US, "FLYWHEEL %.0f / %.0f rpm %s (trim %+.0f)",
                shooter.measuredRpm(), shooter.targetRpm(), shooter.state(), shooter.trimRpm()));
        String aimMode = autoAim ? "auto" : "MANUAL AIM";
        t.addLine(String.format(Locale.US, "AIM %s: cell %s, %s %.1f in | heading err %+.1f (tol %.1f) %s | trim %+.1f | bias %+.1f",
                aimMode, targetCell(), hive.zoneText(), aim.distanceIn, aim.headingErrorDeg, aim.toleranceDeg,
                aim.onTarget() ? "ON TARGET" : String.format(Locale.US, "TURN %+.0f deg (driver RB)", aim.headingErrorDeg),
                aimTrimDeg, vision.aimBiasDeg()));
        t.addLine(String.format(Locale.US, "HIVE state %s (by %s) | our balls at this cell %d | vision: %d tags, says %s",
                hive.state(), hive.lastSource(), hive.ballsLaunchedAtCurrentCell(), vision.ownTagsSeen(),
                vision.evidence() == null ? "-" : vision.evidence().toString()));
        t.addLine(String.format(Locale.US, "FEED %s%s | carrier %+.2f | shots confirmed %d, unconfirmed %d",
                carrier.feedState(), carrier.blockedBy().isEmpty() ? "" : " (waiting: " + carrier.blockedBy() + ")",
                carrier.carrierPower(), carrier.confirmedShots(), carrier.unconfirmedShots()));
        t.addLine(String.format(Locale.US, "POSE (%.1f, %.1f) %.0f deg %s | off-axis %.0f deg | loop %.0f ms | %.1f V",
                p.x(), p.y(), Math.toDegrees(p.heading()), drive.hasLocalizer() ? "" : "(NO ODOMETRY)",
                offAxisDeg(), loopMs, batteryVolts));
        if (autoAim && targetFacesAway()) {
            t.addLine(String.format(Locale.US, "!! TARGET CELL FACES AWAY - wrong zone, or the HIVE tipped: press Y (off-axis %.0f deg)",
                    offAxisDeg()));
        } else if (drive.hasLocalizer() && offAxisDeg() > FieldConstants.GOOD_SHOT_MAX_OFF_AXIS_DEG) {
            t.addLine(String.format(Locale.US, "! off-axis %.0f deg (> %.0f): shots start to hit the cell's side wall - move toward the cell's front",
                    offAxisDeg(), FieldConstants.GOOD_SHOT_MAX_OFF_AXIS_DEG));
        }
        if (aim.distanceIn < RobotConfig.SHOT_MIN_DISTANCE_IN || aim.distanceIn > RobotConfig.SHOT_MAX_DISTANCE_IN) {
            t.addLine(String.format(Locale.US, "!! distance %.0f in is outside the reliable shot band (%.0f-%.0f in)",
                    aim.distanceIn, RobotConfig.SHOT_MIN_DISTANCE_IN, RobotConfig.SHOT_MAX_DISTANCE_IN));
        }
    }

    /**
     * Loud INIT lines when there is no mouth sensor (nothing when it is fitted): the robot then cannot
     * count balls coming in or refuse NECTAR, so the drivers must (G407 / G408). TeleOp shows them at the
     * top; AUTO right under its selection menu (the alliance banner stays the top line).
     */
    public void addBallSensorWarning(Telemetry t) {
        if (carrier.hasMouthSensor()) return;
        t.addLine("!!!! NO MOUTH SENSOR ('" + RobotConfig.MOUTH_COLOR + "'): balls coming in are NOT counted and NECTAR is NOT refused (G407/G408) !!!!");
        t.addLine("!! The count starts at the preloads and drops by one per feed; an RB volley feeds " + RobotConfig.MAX_BALLS + " times");
        t.addLine("!! Drivers: stop intaking at 4 balls BY EYE and steer around NECTAR. Operator back / L-stick = count -1 / +1;"
                + " an eject (LB) does not lower the count: press back once per ball ejected");
    }

    /** Startup warnings, one line each. */
    public void addWarnings(Telemetry t) {
        for (String w : warnings) t.addLine("WARN: " + w);
    }
}
