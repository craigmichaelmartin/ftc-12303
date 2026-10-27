package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Aim;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.junit.Test;

/**
 * The Limelight is fixed to the chassis and looks out of the robot's back (like the shooter). Checks the
 * camera pose and the bearing math behind the vision bias, and that the bias corrects the aim.
 */
public class VisionGeometryTest {

    @Test
    public void theCameraLooksOutOfTheBackTowardTheHive() {
        assertEquals(180.0, RobotConfig.CAMERA_YAW_OFFSET_DEG, 0.0);
        Pose s1 = FieldConstants.startPose(Alliance.RED, StartSpot.S1);     // heading 0: back toward the HIVE
        double[] cam = Vision.cameraPose(s1);
        assertEquals("looks toward -X, at the HIVE", 180.0, Math.abs(cam[2]), 1e-9);
        assertEquals(s1.x() + RobotConfig.CAMERA_FORWARD_IN, cam[0], 1e-9);
        assertEquals(s1.y() + RobotConfig.CAMERA_LEFT_IN, cam[1], 1e-9);
        // the start cell's tags are inside a ~+-30 deg view straight out of the back
        for (int id = 34; id <= 37; id++) {
            FieldPoint tag = FieldConstants.tagPosition(Alliance.RED, id, true);
            double bearing = MathUtil.bearingDeg(cam[0], cam[1], tag.x, tag.y);
            assertTrue("tag " + id + " at " + bearing, Math.abs(MathUtil.wrapDeg(bearing - cam[2])) < 30.0);
        }
    }

    @Test
    public void leftInTheImageIsCounterClockwiseOnTheField() {
        Pose robot = new Pose(40, 0, 0);                  // back toward the origin
        double[] cam = Vision.cameraPose(robot);
        FieldPoint ahead = new FieldPoint(0, 0, 50);
        assertEquals("centred tag where odometry says: no error", 0.0, Vision.bearingErrorDeg(cam, 0.0, ahead), 1e-9);
        assertEquals("seen 5 deg to the camera's left = 5 deg CCW of the prediction", 5.0,
                Vision.bearingErrorDeg(cam, 5.0, ahead), 1e-9);
        // a tag that really sits to the camera's left (field -Y when looking toward -X) is predicted there
        FieldPoint left = new FieldPoint(0, -5, 50);
        double azimuth = Math.toDegrees(Math.atan2(5, cam[0]));
        assertEquals(0.0, Vision.bearingErrorDeg(cam, azimuth, left), 1e-9);
    }

    @Test
    public void theVisionBiasUndoesAnOdometryHeadingError() {
        Pose truePose = FieldConstants.startPose(Alliance.RED, StartSpot.S1);                  // aimed as placed
        Pose odometry = new Pose(truePose.x(), truePose.y(), truePose.heading() - Math.toRadians(2.0));  // reads 2 deg CW
        FieldPoint tag = FieldConstants.tagPosition(Alliance.RED, 36, true);
        double[] camTrue = Vision.cameraPose(truePose);
        double azimuth = MathUtil.wrapDeg(MathUtil.bearingDeg(camTrue[0], camTrue[1], tag.x, tag.y) - camTrue[2]);
        double bias = Vision.bearingErrorDeg(Vision.cameraPose(odometry), azimuth, tag);
        assertEquals("the camera sees the 2 deg heading error", -2.0, bias, 0.5);

        FieldPoint target = FieldConstants.aimPoint(Alliance.RED, HiveState.A);
        double off = FieldConstants.offAxisDeg(Alliance.RED, HiveState.A, odometry.x(), odometry.y());
        Aim.Solution withoutBias = Aim.solve(target, odometry, off, 0.0, 0.0);
        Aim.Solution withBias = Aim.solve(target, odometry, off, 0.0, bias);
        assertEquals("odometry alone would turn the (correctly aimed) robot 2 deg", 2.0, withoutBias.headingErrorDeg, 0.1);
        assertEquals("with the bias it stays put", 0.0, withBias.headingErrorDeg, 0.5);
    }
}
