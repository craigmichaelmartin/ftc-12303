package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.util.MatchTimer;

/**
 * Gamepad rumble + light-bar cues so the drivers never have to read telemetry mid-match.
 *
 *   Cue                                     Who        Rumble
 *   4 balls loaded (full)                   driver     2 short blips
 *   NECTAR refused at the mouth             driver     1 short buzz (opponent's NECTAR always; ours
 *                                                      too while ACCEPT_OWN_NECTAR = false)
 *   flywheel reached speed                  operator   1 light short buzz (again after every shot)
 *   operator Y switched the target cell     both       1 long buzz
 *   CAMERA switched the target cell         both       2 long buzzes
 *   Y confirmed the camera's switch         operator   1 strong short buzz (no change)
 *   a feed was not confirmed (jam? count?)  operator   4 blips (every shot = dip detection untuned
 *                                                      and no feed beam)
 *   1:00 left (FLOWER unlock, NECTAR in)    both       3 blips
 *   0:20 left                               both       1 s continuous
 *
 *   Light bar (PS4/PS5 pads only):
 *   driver:   RED = full (4), YELLOW = 1-3 balls, BLUE = empty
 *   operator: BLUE = shooting mode off, RED = target cell faces away (wrong zone, or the HIVE tipped:
 *             press Y), PURPLE = not aimed: the robot's back is not on the target (driver: hold RB),
 *             GREEN = ready to fire, YELLOW = aimed, waiting (flywheel spinning up, robot moving, TIP pause)
 *
 * After a target switch shoot from the end the up cell faces (look at the HIVE, or the ">> ... <<"
 * telemetry banner); if you are already there, stay.
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

    /** Time cues from the TeleOp match timer (reminders: the FIELD timer and the coach are the authority). */
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

    /** The operator's Y switched the target cell: shoot from the end the up cell faces. */
    public void onTip() {
        driver.rumble(0.6, 0.6, 600);
        operator.rumble(0.6, 0.6, 600);
    }

    /** The CAMERA switched the target cell (it saw the other cell up). */
    public void onVisionSwitch() {
        Gamepad.RumbleEffect two = new Gamepad.RumbleEffect.Builder()
                .addStep(0.8, 0.8, 400).addStep(0.0, 0.0, 200).addStep(0.8, 0.8, 400).build();
        driver.runRumbleEffect(two);
        operator.runRumbleEffect(two);
    }

    /** Y confirmed a camera switch (the target cell did not change). */
    public void onConfirm() {
        operator.rumble(1.0, 0.0, 300);
    }

    /** NECTAR was refused at the mouth (the roller reverses while it is there). */
    public void onReject() {
        if (!driver.isRumbling()) driver.rumble(0.0, 1.0, 200);
    }

    /** A feed was not confirmed by the beam or a flywheel dip (jam? check the balls and the count). */
    public void onUnconfirmedFeed() {
        if (!operator.isRumbling()) operator.rumbleBlips(4);
    }

    /** Call every loop: buzz the operator once each time the flywheel gets to speed. */
    public void updateShooter(boolean spinUpOn, boolean atSpeed) {
        if (spinUpOn && atSpeed && !wasAtSpeed && !operator.isRumbling()) operator.rumble(0.4, 0.4, 150);
        wasAtSpeed = spinUpOn && atSpeed;
    }

    /** Light bars: driver shows ball state, operator shows shooter state. Only sent on change. */
    public void updateLights(boolean full, int balls, boolean spinUpOn, boolean readyToFire, boolean aimed,
                             boolean targetFacesAway) {
        Light d = full ? Light.RED : (balls > 0 ? Light.YELLOW : Light.BLUE);
        Light o;
        if (!spinUpOn) o = Light.BLUE;
        else if (targetFacesAway) o = Light.RED;          // wrong zone, or the HIVE tipped: press Y
        else if (!aimed) o = Light.PURPLE;                // not aimed: driver holds RB (aim-assist)
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
