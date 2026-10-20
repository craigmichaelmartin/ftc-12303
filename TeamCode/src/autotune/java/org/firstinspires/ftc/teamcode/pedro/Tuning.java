package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;

/**
 * Pedro Pathing AutoTune procedures for 12303 (SWYFT mecanum + goBILDA Pinpoint).
 *
 * THIS FILE IS ONLY COMPILED WHEN PEDRO_AUTOTUNE = true (build.dependencies.gradle), because the
 * AutoTune library runs a Wi-Fi web server that is NOT allowed at events (R704.D, Q&A 26).
 *
 * Use: deploy with PEDRO_AUTOTUNE = true, connect a laptop to the robot's Wi-Fi, open
 * http://192.168.43.1:10158 and run, in order:
 *   1. Mecanum Tuner   - enter fl / fr / bl / br, watch each wheel, then copy the 4 DIRECTIONS into
 *                        RobotConfig section 2 (do not paste the generated block into Constants).
 *   2. Pinpoint Tuner  - name "pinpoint", pod type, push forward, push left, spin 180 deg CCW; copy the
 *                        offsets / directions / pod type into RobotConfig section 3.
 *   3. Foresight Tuner - needs ~4 x 4 ft of clear floor; PASTE the generated ForesightConfig block over
 *                        the placeholder in pedro/Constants.java and set FORESIGHT_TUNED = true.
 *   4. Tests           - Localization, Line, Curve, Hold. 48 in pushes should read 48 +- 0.5 in.
 * The @Tuner methods are found automatically by the AutoTune library (no OpMode list entry).
 */
public class Tuning {

    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig));
    }

    @Tuner
    public static Procedure tests() {
        return new Tests(
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig),
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                () -> new Foresight(Constants.foresightConfig));
    }
}
