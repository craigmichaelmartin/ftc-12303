package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveTracker;
import org.firstinspires.ftc.teamcode.subsystems.Indexer;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
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
 *   3. vision -> HIVE tracker (which cell is up?) -> turret aim + flywheel speed
 *   4. indexer (count, colour, feed) -> intake (obeys "full" and "reject")
 *
 * The OpMode sets what it WANTS (drive sticks, auto-aim on/off, fire, intake) BEFORE calling update().
 *
 * G403: building a Robot never moves anything. Actuators move only after arm().
 */
public class Robot {
    public final Drivetrain drive;
    public final Shooter shooter;
    public final Turret turret;
    public final Vision vision;
    public final HiveTracker hive;
    public final Indexer indexer;
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
    private HiveState aimOverride = null;
    private Turret.AimSolution aim = new Turret.AimSolution();

    private double batteryVolts = 0.0;
    private long lastBatteryReadMs = 0;
    private long lastLoopNs = 0;
    private double loopMs = 0.0;

    // things that happened this loop (each true for exactly one loop)
    private boolean tipEvent, fullEvent, rejectEvent, shotEvent;

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
        turret = new Turret(hw, warnings);
        vision = new Vision(hw, alliance, warnings);
        hive = new HiveTracker(alliance);
        indexer = new Indexer(hw, alliance, warnings);
        intake = new Intake(hw, warnings);
    }

    /** Change alliance (AUTO selector). Updates every subsystem that cares. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
        vision.setAlliance(alliance);
        hive.setAlliance(alliance);
        indexer.setAlliance(alliance);
    }

    public Alliance alliance() {
        return alliance;
    }

    /**
     * Allow actuators to move. TeleOp: call right after START (never in INIT - G403).
     * AUTO: may be called in INIT to set servos, as long as the robot is still before the match starts
     * (G304.H "fully motionless following completion of OpMode initialization").
     */
    public void arm() {
        armed = true;
        drive.arm();
        shooter.arm();
        turret.arm();
        indexer.arm();
        intake.arm();
    }

    public boolean isArmed() {
        return armed;
    }

    // ---------------------------------------------------------------------------------------------
    // What the OpMode wants
    // ---------------------------------------------------------------------------------------------
    /** Turret follows the up cell (fused odometry + vision aim). */
    public void setAutoAim(boolean on) {
        if (autoAim && !on) turret.center();
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

    /** Spin-up + auto-aim together (the operator's main toggle). */
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
     * Aim at this cell no matter what the HIVE tracker says (null = follow the tracker).
     * AUTO A3 role 2 uses it to pre-aim at the far cell while waiting for the partner's TIP.
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

        // Which cell is up, and how far off is our aim? (bias only learned while things are still)
        boolean stable = turret.isSettled() && Math.abs(drive.turnRateDegPerSec()) < 15.0;
        vision.update(pose, turret.currentRobotFrameDeg(), targetCell(), stable);
        hive.update(vision);
        tipEvent = hive.consumeTipEvent();
        if (tipEvent) {
            indexer.pauseForTip();
            vision.resetBias();
        }
        turret.setVisionBias(vision.aimBiasDeg());

        // Aim + flywheel
        FieldPoint target = FieldConstants.aimPoint(alliance, targetCell());
        aim = autoAim ? turret.aimAt(target, pose) : Turret.solve(target, pose, turret.trimDeg(), turret.visionBiasDeg());
        if (manualRpm > 0) {
            shooter.setTargetRpm(manualRpm);
        } else if (spinUp) {
            if (drive.hasLocalizer()) shooter.setTargetForDistance(aim.distanceIn);
            else shooter.setTargetRpm(RobotConfig.FLYWHEEL_DEFAULT_RPM);
        } else {
            shooter.stop();
        }
        turret.update();
        shooter.update();

        // Balls
        boolean aimReady = aimReady();
        indexer.update(shooter.atSpeed(), aimReady, drive.isStill(), hive.tipPauseActive(),
                shooter.consumeShotDip(), intake.isIntaking());
        rejectEvent = indexer.consumeRejectEvent();
        fullEvent = indexer.consumeFullEvent();
        shotEvent = indexer.consumeShotEvent();
        if (shotEvent) hive.onBallLaunched();
        intake.update(indexer.isFull(), rejectEvent);
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
    /** Latest aim solution (distance, turret angle, reachable, chassis turn needed). */
    public Turret.AimSolution aim() {
        return aim;
    }

    /**
     * Aim OK for a shot? Auto-aim: the turret is on a reachable target. Manual: the turret has stopped
     * moving. No turret servo configured: aim cannot be checked, so it never blocks a shot.
     */
    public boolean aimReady() {
        if (!turret.isPresent()) return true;
        return autoAim ? turret.onTarget() : turret.isSettled();
    }

    /** Everything ready for the next ball? (flywheel, aim, chassis still, no TIP pause) */
    public boolean readyToFire() {
        return shooter.atSpeed() && aimReady() && drive.isStill() && !hive.tipPauseActive();
    }

    public boolean tipEventThisLoop() {
        return tipEvent;
    }

    public boolean fullEventThisLoop() {
        return fullEvent;
    }

    public boolean rejectEventThisLoop() {
        return rejectEvent;
    }

    public boolean shotEventThisLoop() {
        return shotEvent;
    }

    public double batteryVolts() {
        return batteryVolts;
    }

    public double loopMs() {
        return loopMs;
    }

    /** AUTO -> TeleOp hand-off (call every AUTO loop). */
    public void saveToPoseStorage() {
        PoseStorage.save(drive.pose(), alliance, indexer.count(), hive.state());
    }

    /** Standard status lines for any OpMode. */
    public void addTelemetry(Telemetry t) {
        Pose p = drive.pose();
        t.addLine(String.format(Locale.US, "BALLS %d/%d%s | mouth %s | slot4 %s | intake %s",
                indexer.count(), RobotConfig.MAX_BALLS, indexer.isFull() ? " FULL" : "",
                indexer.mouthBall(), indexer.slot4Occupied() ? "BALL" : "-", intake.state()));
        t.addLine(String.format(Locale.US, "FLYWHEEL %.0f / %.0f rpm %s (trim %+.0f)",
                shooter.measuredRpm(), shooter.targetRpm(), shooter.state(), shooter.trimRpm()));
        t.addLine(String.format(Locale.US, "AIM cell %s, %s %.1f in | turret %+.1f (cmd %+.1f) %s | bias %+.1f | %s",
                targetCell(), hive.zoneText(), aim.distanceIn, turret.currentOffsetDeg(), turret.commandOffsetDeg(),
                turret.state(), turret.visionBiasDeg(),
                aim.reachable ? "reachable" : String.format(Locale.US, "TURN CHASSIS %+.0f deg", aim.chassisTurnDeg)));
        t.addLine(String.format(Locale.US, "HIVE state %s (by %s) | our balls at this cell %d | vision: %d tags, says %s",
                hive.state(), hive.lastSource(), hive.ballsLaunchedAtCurrentCell(), vision.ownTagsSeen(),
                vision.evidence() == null ? "-" : vision.evidence().toString()));
        t.addLine(String.format(Locale.US, "FEED %s%s | shots confirmed %d, unconfirmed %d",
                indexer.feedState(), indexer.blockedBy().isEmpty() ? "" : " (waiting: " + indexer.blockedBy() + ")",
                indexer.confirmedShots(), indexer.unconfirmedShots()));
        t.addLine(String.format(Locale.US, "POSE (%.1f, %.1f) %.0f deg %s | off-axis %.0f deg | loop %.0f ms | %.1f V",
                p.x(), p.y(), Math.toDegrees(p.heading()), drive.hasLocalizer() ? "" : "(NO ODOMETRY)",
                FieldConstants.offAxisDeg(alliance, targetCell(), p.x(), p.y()), loopMs, batteryVolts));
        if (aim.distanceIn < RobotConfig.SHOT_MIN_DISTANCE_IN || aim.distanceIn > RobotConfig.SHOT_MAX_DISTANCE_IN) {
            t.addLine(String.format(Locale.US, "!! distance %.0f in is outside the shot table range (%.0f-%.0f)",
                    aim.distanceIn, RobotConfig.SHOT_MIN_DISTANCE_IN, RobotConfig.SHOT_MAX_DISTANCE_IN));
        }
    }

    /** Startup warnings, one line each. */
    public void addWarnings(Telemetry t) {
        for (String w : warnings) t.addLine("WARN: " + w);
    }
}
