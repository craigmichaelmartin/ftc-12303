package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.InterpLUT;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Shooting wheel ("flywheel": goBILDA 5203 6000 rpm 1:1 assumed, 28 counts/rev) at the top of the robot's
 * back. It cannot turn and the ball's exit angle is fixed: the robot aims by turning (subsystems/Aim) and
 * the distance is handled by the wheel speed (the shot table).
 *
 * Speed control: the REV hub's built-in velocity PIDF (setVelocity) with REV's starting recipe
 * F = 32767 / maxTicksPerSec, P = 0.1 F, I = 0.1 P (RobotConfig section 4). It runs inside the hub at
 * a fixed rate, independent of our loop time. Upgrade path (R6 6.3): our own kS + kV + kP loop with
 * battery-voltage compensation, if recovery between shots is too slow.
 *
 * "At speed" = within +-max(2.5%, 60 rpm) of the target for 3 loops in a row. The 28 CPR encoder only
 * resolves ~1-1.5%, so a tighter window would just be noise.
 *
 * A shot shows up as a speed dip. The hub's velocity reading is a ~50 ms moving average, so the drop
 * arrives over several loops: ANY excursion below the window while AT_SPEED starts a RECOVERING episode,
 * and the episode reports exactly one shot dip (consumeShotDip) as soon as the speed falls below
 * target x (1 - FLYWHEEL_SHOT_DIP_FRACTION). A target change of more than 2% is not a dip (ignored for
 * FLYWHEEL_DIP_IGNORE_AFTER_TARGET_CHANGE_MS).
 */
public class Shooter {

    public enum State {
        /** Not armed yet, or no flywheel motor. */
        DISABLED,
        /** Target 0: coasting (FLOAT) - never brake a flywheel hard. */
        STOPPED,
        /** Getting up to the target speed. */
        SPINNING_UP,
        /** Inside the speed window for READY_LOOPS loops: OK to feed a ball. */
        AT_SPEED,
        /** Speed dropped below the window (a shot); waiting to get back into the window. */
        RECOVERING
    }

    private final DcMotorEx motor;
    private final InterpLUT rpmTable;

    private State state = State.DISABLED;
    private boolean armed = false;
    private boolean velocityMode = false;
    private double targetRpm = 0.0;
    private double trimRpm = 0.0;
    private double measuredRpm = 0.0;
    private double lastSentTicksPerSec = Double.NaN;
    private int loopsInWindow = 0;
    private boolean shotDipPending = false;
    private boolean dipFlagged = false;
    private long targetChangedMs = -100000;
    private long dipStartNs = 0;
    private double dipLowestRpm = 0.0;
    private double lastRecoveryMs = 0.0;
    private double lastDipRpm = 0.0;

    public Shooter(HardwareMap hw, List<String> warnings) {
        motor = OptionalHardware.get(hw, DcMotorEx.class, RobotConfig.FLYWHEEL, warnings);
        rpmTable = InterpLUT.fromRows(RobotConfig.SHOT_TABLE);
        if (motor != null) {
            // None of this moves the motor, so it is fine in TeleOp INIT (G403).
            motor.setDirection(RobotConfig.FLYWHEEL_DIRECTION);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            applyPidf();
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    /** (Re-)send the velocity PIDF from RobotConfig. The hub forgets it between OpModes. */
    public void applyPidf() {
        if (motor == null) return;
        motor.setVelocityPIDFCoefficients(RobotConfig.flywheelP(), RobotConfig.flywheelI(),
                RobotConfig.FLYWHEEL_D, RobotConfig.flywheelF());
    }

    /** Allow the flywheel to spin (TeleOp and AUTO: at START - G403, G304.H). */
    public void arm() {
        armed = true;
        state = State.STOPPED;
    }

    public boolean isPresent() {
        return motor != null;
    }

    // ---------------------------------------------------------------------------------------------
    // Targets
    // ---------------------------------------------------------------------------------------------
    /** Spin to this RPM (0 = coast to a stop). The operator trim is added. */
    public void setTargetRpm(double rpm) {
        double next = (rpm <= 0) ? 0.0 : MathUtil.clamp(rpm + trimRpm, 0.0, RobotConfig.FLYWHEEL_MAX_RPM);
        if (Math.abs(next - targetRpm) > 0.02 * Math.max(targetRpm, 1.0)) targetChangedMs = Clock.nowMs();
        targetRpm = next;
    }

    /** Spin to the shot-table RPM for this horizontal distance to the CELL opening (inches). */
    public void setTargetForDistance(double distanceIn) {
        setTargetRpm(rpmForDistance(distanceIn));
    }

    /** Table RPM (without trim) for a distance. Falls back to FLYWHEEL_DEFAULT_RPM if the table is empty. */
    public double rpmForDistance(double distanceIn) {
        if (rpmTable.isEmpty()) return RobotConfig.FLYWHEEL_DEFAULT_RPM;
        return rpmTable.get(distanceIn) * RobotConfig.SHOT_TABLE_RPM_SCALE;
    }

    public void stop() {
        setTargetRpm(0.0);
    }

    public void adjustTrim(double deltaRpm) {
        trimRpm += deltaRpm;
    }

    public void setTrim(double rpm) {
        trimRpm = rpm;
    }

    public double trimRpm() {
        return trimRpm;
    }

    // ---------------------------------------------------------------------------------------------
    // Loop
    // ---------------------------------------------------------------------------------------------
    public void update() {
        if (motor == null) {
            state = State.DISABLED;
            return;
        }
        measuredRpm = motor.getVelocity() * 60.0 / RobotConfig.FLYWHEEL_TICKS_PER_REV;  // bulk-cached read
        if (!armed) {
            state = State.DISABLED;
            return;
        }
        sendCommand();
        updateState();
    }

    private void sendCommand() {
        if (targetRpm <= 0) {
            if (velocityMode || Double.isNaN(lastSentTicksPerSec) || lastSentTicksPerSec != 0.0) {
                // Coast: RUN_WITHOUT_ENCODER + power 0 + FLOAT, so the hub PID does not brake the wheel.
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                motor.setPower(0.0);
                velocityMode = false;
                lastSentTicksPerSec = 0.0;
            }
            return;
        }
        if (!velocityMode) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            applyPidf();
            velocityMode = true;
            lastSentTicksPerSec = Double.NaN;
        }
        double ticksPerSec = targetRpm * RobotConfig.FLYWHEEL_TICKS_PER_REV / 60.0;
        if (Double.isNaN(lastSentTicksPerSec) || Math.abs(ticksPerSec - lastSentTicksPerSec) > 0.5) {
            motor.setVelocity(ticksPerSec);   // only write when it changes (each write costs loop time)
            lastSentTicksPerSec = ticksPerSec;
        }
    }

    private void updateState() {
        if (targetRpm <= 0) {
            state = State.STOPPED;
            loopsInWindow = 0;
            return;
        }
        double window = Math.max(RobotConfig.FLYWHEEL_READY_TOLERANCE_MIN_RPM,
                RobotConfig.FLYWHEEL_READY_TOLERANCE_FRACTION * targetRpm);
        boolean inWindow = Math.abs(measuredRpm - targetRpm) <= window;
        loopsInWindow = inWindow ? loopsInWindow + 1 : 0;
        boolean ready = loopsInWindow >= RobotConfig.FLYWHEEL_READY_LOOPS;
        boolean dipped = measuredRpm < targetRpm * (1.0 - RobotConfig.FLYWHEEL_SHOT_DIP_FRACTION);
        boolean targetJustChanged = Clock.nowMs() - targetChangedMs < RobotConfig.FLYWHEEL_DIP_IGNORE_AFTER_TARGET_CHANGE_MS;

        switch (state) {
            case AT_SPEED:
                if (!inWindow) {
                    if (measuredRpm < targetRpm && !targetJustChanged) {
                        // Below the window: probably a ball going through (the averaged reading falls over
                        // several loops). One RECOVERING episode = at most one shot dip.
                        state = State.RECOVERING;
                        dipStartNs = System.nanoTime();
                        dipLowestRpm = measuredRpm;
                        dipFlagged = false;
                        flagDipIfDeep(dipped);
                    } else {
                        state = State.SPINNING_UP;   // new target, or reading high
                    }
                }
                break;
            case RECOVERING:
                dipLowestRpm = Math.min(dipLowestRpm, measuredRpm);
                if (targetJustChanged) {
                    state = State.SPINNING_UP;       // the target moved: this is no longer a shot episode
                } else {
                    flagDipIfDeep(dipped);
                    if (ready) {
                        if (dipFlagged) {
                            lastRecoveryMs = (System.nanoTime() - dipStartNs) / 1e6;
                            lastDipRpm = targetRpm - dipLowestRpm;
                        }
                        state = State.AT_SPEED;
                    }
                }
                break;
            default:
                state = ready ? State.AT_SPEED : State.SPINNING_UP;
                break;
        }
    }

    private void flagDipIfDeep(boolean dipped) {
        if (dipped && !dipFlagged) {
            shotDipPending = true;       // a ball (probably) just went through the wheel
            dipFlagged = true;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------
    public State state() {
        return state;
    }

    /** True when it is OK to feed a ball. */
    public boolean atSpeed() {
        return state == State.AT_SPEED;
    }

    /** True once after each detected speed dip (a ball went through). */
    public boolean consumeShotDip() {
        boolean d = shotDipPending;
        shotDipPending = false;
        return d;
    }

    public double targetRpm() {
        return targetRpm;
    }

    public double measuredRpm() {
        return measuredRpm;
    }

    public double measuredTicksPerSec() {
        return measuredRpm * RobotConfig.FLYWHEEL_TICKS_PER_REV / 60.0;
    }

    /** How long the last recovery after a dip took (ms) and how far the speed fell (rpm). */
    public double lastRecoveryMs() {
        return lastRecoveryMs;
    }

    public double lastDipRpm() {
        return lastDipRpm;
    }

    /** Direct access for the FlywheelTuner's max-speed test only. */
    public DcMotorEx motor() {
        return motor;
    }

    /** Forget the internal command cache (FlywheelTuner uses this after raw power tests). */
    public void resetCommandCache() {
        velocityMode = false;
        lastSentTicksPerSec = Double.NaN;
    }
}
