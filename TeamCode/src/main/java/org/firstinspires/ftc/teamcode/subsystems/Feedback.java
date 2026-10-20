package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.MatchTimer;

/**
 * Gamepad rumble + light-bar cues so the drivers never have to read telemetry mid-match.
 *
 *   Cue                         Who        Rumble                 Light bar (PS4/PS5 pads only)
 *   4 balls loaded (full)       driver     2 short blips          driver: RED while full
 *   flywheel reached speed      operator   1 short buzz           operator: GREEN = ready, YELLOW = spinning
 *   HIVE TIP / target switched  both       1 long buzz            -
 *   opponent NECTAR rejected    driver     1 short buzz           -
 *   1:00 left (FLOWER unlock)   both       3 blips                -
 *   0:20 left (go park)         both       1 s continuous         -
 *
 * Cues fire on CHANGES only, never every loop (the SDK rumble sample warns about flooding).
 * Logitech F310 pads cannot rumble at all; R904 requires WIRED gamepads.
 */
public class Feedback {

    public enum Light { OFF, BLUE, YELLOW, GREEN, RED, PURPLE }

    private final Gamepad driver;
    private final Gamepad operator;
    private boolean oneMinuteDone = false;
    private boolean twentySecondsDone = false;
    private boolean wasAtSpeed = false;
    private Light driverLight = null;
    private Light operatorLight = null;

    public Feedback(Gamepad driver, Gamepad operator) {
        this.driver = driver;
        this.operator = operator;
    }

    /** Time cues from the TeleOp match timer. */
    public void updateTimeCues(MatchTimer timer) {
        if (!timer.isStarted()) return;
        double left = timer.remainingSeconds();
        if (!oneMinuteDone && left <= RobotConfig.CUE_ONE_MINUTE_LEFT_SEC) {
            oneMinuteDone = true;
            driver.rumbleBlips(3);
            operator.rumbleBlips(3);
        }
        if (!twentySecondsDone && left <= RobotConfig.CUE_TWENTY_SECONDS_LEFT_SEC) {
            twentySecondsDone = true;
            driver.rumble(1000);
            operator.rumble(1000);
        }
    }

    /** We just reached 4 balls. */
    public void onFull() {
        driver.rumbleBlips(2);
    }

    /** The HIVE state changed (tip detected or operator toggle): go to the other zone. */
    public void onTip() {
        driver.rumble(0.6, 0.6, 600);
        operator.rumble(0.6, 0.6, 600);
    }

    /** Opponent NECTAR was pushed away from the mouth. */
    public void onReject() {
        if (!driver.isRumbling()) driver.rumble(0.0, 1.0, 200);
    }

    /** Call every loop: buzz the operator once each time the flywheel gets to speed. */
    public void updateShooter(boolean spinUpOn, boolean atSpeed) {
        if (spinUpOn && atSpeed && !wasAtSpeed && !operator.isRumbling()) operator.rumble(0.4, 0.4, 150);
        wasAtSpeed = spinUpOn && atSpeed;
    }

    /** Light bars: driver shows ball state, operator shows shooter state. Only sent on change. */
    public void updateLights(boolean full, int balls, boolean spinUpOn, boolean readyToFire, boolean targetReachable) {
        Light d = full ? Light.RED : (balls > 0 ? Light.YELLOW : Light.BLUE);
        Light o;
        if (!spinUpOn) o = Light.BLUE;
        else if (!targetReachable) o = Light.PURPLE;   // turret at its limit: turn the chassis
        else if (readyToFire) o = Light.GREEN;
        else o = Light.YELLOW;
        if (d != driverLight) {
            setLight(driver, d);
            driverLight = d;
        }
        if (o != operatorLight) {
            setLight(operator, o);
            operatorLight = o;
        }
    }

    private static void setLight(Gamepad pad, Light light) {
        double r = 0, g = 0, b = 0;
        switch (light) {
            case BLUE: b = 1; break;
            case YELLOW: r = 1; g = 0.8; break;
            case GREEN: g = 1; break;
            case RED: r = 1; break;
            case PURPLE: r = 0.6; b = 1; break;
            default: break;
        }
        pad.setLedColor(r, g, b, Gamepad.LED_DURATION_CONTINUOUS);
    }
}
