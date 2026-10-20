package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.config.RobotConfig;

import java.util.OptionalDouble;

/**
 * Pedro Pathing 3 setup: SWYFT mecanum drive + goBILDA Pinpoint localizer + the Foresight follower.
 *
 * The follower runs in OUR field frame (config/FieldConstants: origin at field centre, +X toward the
 * audience, +Y toward blue, inches, radians). Pedro does not care where the origin is.
 *
 * HOW TO TUNE (README-12303.md, "Tuning order"): set PEDRO_AUTOTUNE = true in
 * build.dependencies.gradle, deploy, open http://192.168.43.1:10158 while connected to the robot Wi-Fi,
 * and run the procedures in TeamCode/src/autotune/java/.../pedro/Tuning.java in this order:
 *   1. Mecanum Tuner  -> copy the 4 DIRECTIONS into RobotConfig (names already match).
 *   2. Pinpoint Tuner -> copy pod offsets/directions/pod type into RobotConfig section 3.
 *   3. Foresight Tuner -> PASTE its whole Java block over foresightConfig below, set FORESIGHT_TUNED = true.
 *   4. Tests (Line, Curve, Hold, Localization) to verify. Then set PEDRO_AUTOTUNE = false again (R704.D).
 */
public class Constants {

    /** Change to true after you paste the AutoTune Foresight output below. AUTO warns while false. */
    public static final boolean FORESIGHT_TUNED = false;

    /** Drive motors: names + directions come from RobotConfig (one file to edit). */
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(RobotConfig.FRONT_LEFT);
        c.backLeftName.set(RobotConfig.BACK_LEFT);
        c.frontRightName.set(RobotConfig.FRONT_RIGHT);
        c.backRightName.set(RobotConfig.BACK_RIGHT);
        c.frontLeftDirection.set(RobotConfig.FRONT_LEFT_DIRECTION);
        c.backLeftDirection.set(RobotConfig.BACK_LEFT_DIRECTION);
        c.frontRightDirection.set(RobotConfig.FRONT_RIGHT_DIRECTION);
        c.backRightDirection.set(RobotConfig.BACK_RIGHT_DIRECTION);
        c.manualBrakeMode.set(RobotConfig.DRIVE_BRAKE_IN_TELEOP);
    });

    /** Pinpoint: name, pods, offsets and directions come from RobotConfig section 3. */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(RobotConfig.PINPOINT);
        if (RobotConfig.PINPOINT_CUSTOM_TICKS_PER_INCH > 0) {
            c.ticksPerUnit.set(OptionalDouble.of(RobotConfig.PINPOINT_CUSTOM_TICKS_PER_INCH));
            c.encoderResolutionUnit.set(DistanceUnit.INCH);
        } else {
            c.podType.set(RobotConfig.PINPOINT_POD_TYPE);
        }
        c.xPodOffset.set(RobotConfig.PINPOINT_X_POD_OFFSET_IN);
        c.yPodOffset.set(RobotConfig.PINPOINT_Y_POD_OFFSET_IN);
        c.xPodDirection.set(RobotConfig.PINPOINT_X_POD_DIRECTION);
        c.yPodDirection.set(RobotConfig.PINPOINT_Y_POD_DIRECTION);
        c.offsetUnits.set(DistanceUnit.INCH);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        // Recalibrates the Pinpoint IMU when the OpMode is INITed (robot must be still). Position is
        // then set by our code (AUTO start pose, or the AUTO -> TeleOp hand-off in PoseStorage).
        c.resetMode.set(PinpointLocalizer.ResetMode.RECALIBRATE_IMU);
    });

    /**
     * Foresight path-following gains.
     * ===== PASTE the AutoTune "Foresight Tuner" Java output over everything inside this block. =====
     * TODO-TUNE: the numbers below are the EXAMPLE robot from the Pedro docs, NOT ours. They only exist so
     * the code compiles and paths run slowly and safely. Expect poor path accuracy until tuned.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.3);
        Controller secondaryTranslationalForward = Controller.proportional(0.1);
        Controller primaryTranslationalLateral = Controller.proportional(0.3);
        Controller secondaryTranslationalLateral = Controller.proportional(0.1);
        c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
        c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));
        c.coast.set(Controller.proportionalFeedforward(0.010978350889324107));
        c.brake.set(Controller.proportionalFeedforward(0.008731598255925491));
        c.headingFeedback.set(Controller.proportional(5.258721785960744));
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05642143125655298, 0.0063829525363003695));
        c.linearBrakeCoefficients.set(Matrix.diag(0.10605894992901523, 0.08719146175596092));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0014663966976606565, 0.0013837064502458813));
        c.maxAchievableForwardVelocity.set(72.72923108818539);
        c.maxAchievableStrafeVelocity.set(52.34323936525474);
        c.naturalForwardDeceleration.set(85.01144677379789);
        c.naturalStrafeDeceleration.set(104.49787535782846);
        // Safety cap while untuned: paths run at half of the (guessed) max speed. Remove after tuning.
        c.maxPathSpeed.set(0.5);
    });

    /** Builds the follower. Throws if the Pinpoint or a drive motor is missing (Drivetrain catches it). */
    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                new Foresight(foresightConfig));
    }
}
