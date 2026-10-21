package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Full-width compliant roller intake on a continuous-rotation servo (Dual Mode servo in CR mode) - or a
 * DC motor: configure "intake" as either one (both are a DcMotorSimple to the SDK).
 *
 * The driver asks for IN or OUT; this class decides what really happens:
 *  - FULL (4 balls, G407): "in" is refused. When we first become full the roller runs backward
 *    briefly to push a 5th ball away from the mouth. Ball 4 is counted once it has passed the mouth
 *    sensor - or at once when a 5th ball shows up right behind it (Indexer, MOUTH_ONE_BALL_MAX_MS) - so
 *    the pulse hits the 5th ball, not ball 4.
 *  - Refused NECTAR at the mouth (G408: always the opponent's; ours too while ACCEPT_OWN_NECTAR = false):
 *    the roller runs backward for as long as that ball is still at the mouth, plus INTAKE_REJECT_MS,
 *    whenever the drivers (or AUTO) want the intake to pull. It never pulls a refused ball in.
 * Servo power note (R5): hub servo ports are 5 V and share 2 A per port pair; a Servo Power Module /
 * Servo Hub at 6 V gives ~20% more speed and torque.
 *
 * G403 / G304.H: no power until arm() (TeleOp START; AUTO arms it at START, not in INIT).
 */
public class Intake {

    public enum State {
        /** Not armed yet, or no roller configured. */
        DISABLED,
        OFF,
        /** Pulling balls in. */
        INTAKING,
        /** Driver/operator asked to push balls out. */
        EJECTING,
        /** Refused NECTAR at the mouth: pushing it away (automatic). */
        REJECTING,
        /** Just became full: short reverse pulse to push a 5th ball away (automatic). */
        FULL_PULSE
    }

    private final DcMotorSimple roller;
    private State state = State.DISABLED;
    private boolean armed = false;
    private boolean wantIn = false;
    private boolean wantOut = false;
    private long autoUntilMs = 0;
    private double lastPower = Double.NaN;
    private boolean refusedBecauseFull = false;
    private boolean rejectStarted = false;

    public Intake(HardwareMap hw, List<String> warnings) {
        roller = OptionalHardware.get(hw, DcMotorSimple.class, RobotConfig.INTAKE, warnings);
        if (roller != null) {
            roller.setDirection(RobotConfig.INTAKE_DIRECTION);
            if (roller instanceof DcMotor) {
                // A DC-motor intake: plain power control (no encoder needed). Nothing moves here (G403).
                ((DcMotor) roller).setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                ((DcMotor) roller).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            }
        }
    }

    public void arm() {
        armed = true;
        if (state == State.DISABLED) state = State.OFF;
    }

    /** What the drivers want this loop. If both are pressed, OUT wins. */
    public void request(boolean in, boolean out) {
        wantIn = in;
        wantOut = out;
    }

    /**
     * @param full               Indexer says we hold 4 balls
     * @param refusedBallAtMouth Indexer sees a refused NECTAR at the mouth right now (level, not an edge)
     */
    public void update(boolean full, boolean refusedBallAtMouth) {
        long now = Clock.nowMs();
        if (roller == null || !armed) {
            state = (roller == null) ? State.DISABLED : state;
            return;
        }
        boolean wouldPull = (wantIn && !wantOut) || state == State.INTAKING;
        if (refusedBallAtMouth && (wouldPull || state == State.REJECTING)) {
            if (state != State.REJECTING) rejectStarted = true;
            state = State.REJECTING;
            autoUntilMs = now + RobotConfig.INTAKE_REJECT_MS;     // keeps extending while the ball is there
        } else if (full && state == State.INTAKING) {
            state = State.FULL_PULSE;
            autoUntilMs = now + RobotConfig.INTAKE_FULL_REVERSE_MS;
        }

        boolean automaticActive = (state == State.REJECTING || state == State.FULL_PULSE) && now < autoUntilMs;
        refusedBecauseFull = wantIn && !wantOut && full;
        if (!automaticActive) {
            if (wantOut) state = State.EJECTING;
            else if (wantIn && !full) state = State.INTAKING;
            else state = State.OFF;
        }
        write(powerFor(state));
    }

    private static double powerFor(State s) {
        switch (s) {
            case INTAKING:
                return RobotConfig.INTAKE_IN_POWER;
            case EJECTING:
            case REJECTING:
            case FULL_PULSE:
                return RobotConfig.INTAKE_OUT_POWER;
            default:
                return 0.0;
        }
    }

    private void write(double power) {
        if (power == lastPower) return;
        roller.setPower(power);
        lastPower = power;
    }

    public State state() {
        return state;
    }

    /** True while the roller pulls balls IN (the Indexer only counts balls pulled in). */
    public boolean isIntaking() {
        return state == State.INTAKING;
    }

    /** True while the roller pushes balls OUT (eject, NECTAR refusal or the full pulse). */
    public boolean isReversing() {
        return state == State.EJECTING || state == State.REJECTING || state == State.FULL_PULSE;
    }

    public boolean isEjecting() {
        return state == State.EJECTING;
    }

    /** True if the driver is holding "in" but we refuse because we are full (for telemetry/rumble). */
    public boolean refusedBecauseFull() {
        return refusedBecauseFull;
    }

    /** True once each time the roller starts reversing to refuse NECTAR (for the driver rumble). */
    public boolean consumeRejectStarted() {
        boolean e = rejectStarted;
        rejectStarted = false;
        return e;
    }

    public boolean isPresent() {
        return roller != null;
    }
}
