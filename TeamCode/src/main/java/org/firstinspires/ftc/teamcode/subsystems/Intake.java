package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.OptionalHardware;

import java.util.List;

/**
 * Full-width compliant roller intake on a continuous-rotation servo (Dual Mode servo in CR mode).
 *
 * The driver asks for IN or OUT; this class decides what really happens:
 *  - FULL (4 balls, G407): "in" is refused. When we first become full the roller runs backward
 *    briefly to push a 5th ball away from the mouth.
 *  - Opponent NECTAR seen at the mouth (G408): the roller runs backward for INTAKE_REJECT_MS no
 *    matter what the driver asks.
 * Servo power note (R5): hub servo ports are 5 V and share 2 A per port pair; a Servo Power Module /
 * Servo Hub at 6 V gives ~20% more speed and torque.
 *
 * G403: no power until arm() (TeleOp START).
 */
public class Intake {

    public enum State {
        /** Not armed yet, or no servo. */
        DISABLED,
        OFF,
        /** Pulling balls in. */
        INTAKING,
        /** Driver/operator asked to push balls out. */
        EJECTING,
        /** Opponent NECTAR at the mouth: pushing it away (automatic). */
        REJECTING,
        /** Just became full: short reverse pulse to push a 5th ball away (automatic). */
        FULL_PULSE
    }

    private final CRServo roller;
    private State state = State.DISABLED;
    private boolean armed = false;
    private boolean wantIn = false;
    private boolean wantOut = false;
    private long autoUntilMs = 0;
    private double lastPower = Double.NaN;
    private boolean refusedBecauseFull = false;

    public Intake(HardwareMap hw, List<String> warnings) {
        roller = OptionalHardware.get(hw, CRServo.class, RobotConfig.INTAKE, warnings);
        if (roller != null) roller.setDirection(RobotConfig.INTAKE_DIRECTION);
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
     * @param full           Indexer says we hold 4 balls
     * @param opponentAtMouth Indexer saw opponent NECTAR at the mouth this loop
     */
    public void update(boolean full, boolean opponentAtMouth) {
        long now = System.currentTimeMillis();
        if (roller == null || !armed) {
            state = (roller == null) ? State.DISABLED : state;
            return;
        }
        if (opponentAtMouth) {
            state = State.REJECTING;
            autoUntilMs = now + RobotConfig.INTAKE_REJECT_MS;
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

    /** True while the roller pulls balls IN (the Indexer only counts balls then). */
    public boolean isIntaking() {
        return state == State.INTAKING;
    }

    public boolean isEjecting() {
        return state == State.EJECTING;
    }

    /** True if the driver is holding "in" but we refuse because we are full (for telemetry/rumble). */
    public boolean refusedBecauseFull() {
        return refusedBecauseFull;
    }
}
