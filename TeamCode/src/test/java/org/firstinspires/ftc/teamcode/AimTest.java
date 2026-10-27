package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoLegs;
import org.firstinspires.ftc.teamcode.subsystems.Aim;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldPoint;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Aiming a fixed shooter that points out of the robot's back: the aim heading, the shot distance from where
 * the ball leaves the aimed robot, the side-offset correction, the aim tolerance computed from the robot-centre
 * distance and the opening's width, the fire gate, the driver's aim-assist turn and the AUTO volley's turn onto
 * the aim heading.
 */
public class AimTest {

    private double savedForward, savedLeft;

    @Before
    public void setUp() {
        savedForward = RobotConfig.SHOOTER_FORWARD_IN;
        savedLeft = RobotConfig.SHOOTER_LEFT_IN;
    }

    @After
    public void tearDown() {
        RobotConfig.SHOOTER_FORWARD_IN = savedForward;
        RobotConfig.SHOOTER_LEFT_IN = savedLeft;
    }

    private static Pose pose(double x, double y, double hDeg) {
        return new Pose(x, y, Math.toRadians(hDeg));
    }

    @Test
    public void theRobotsBackFacesTheTarget() {
        assertEquals("the shooter points out of the back", 180.0, RobotConfig.SHOOTER_FACING_DEG, 0.0);
        FieldPoint target = new FieldPoint(0, 0, 50);
        // target straight ahead of the robot's FRONT: it must turn half a circle
        Aim.Solution s = Aim.solve(target, pose(-40, 0, 0), 0, 0, 0);
        assertEquals(180.0, Math.abs(s.aimHeadingDeg), 1e-9);
        assertEquals(180.0, Math.abs(s.headingErrorDeg), 1e-9);
        assertFalse(s.onTarget());
        // target straight behind: aimed
        s = Aim.solve(target, pose(40, 0, 0), 0, 0, 0);
        assertEquals(0.0, s.headingErrorDeg, 1e-9);
        assertTrue(s.onTarget());
        // robot 10 deg clockwise of the aim: turn 10 deg counter-clockwise (+)
        s = Aim.solve(target, pose(40, 0, -10), 0, 0, 0);
        assertEquals(10.0, s.headingErrorDeg, 1e-9);
    }

    @Test
    public void distanceIsMeasuredFromWhereTheBallLeaves() {
        RobotConfig.SHOOTER_FORWARD_IN = -7.0;
        RobotConfig.SHOOTER_LEFT_IN = 0.0;
        FieldPoint target = new FieldPoint(0, 0, 50);
        Aim.Solution s = Aim.solve(target, pose(40, 0, 0), 0, 0, 0);
        assertEquals("the shooter is 7 in behind the centre, toward the target", 33.0, s.distanceIn, 1e-9);
        // Measured from where the shooter will be once the robot has turned onto its aim (it turns on the
        // spot): the same whichever way the robot faces now, so the shot-table RPM does not swing while it turns.
        for (double h : new double[]{30, 90, 180, -135}) {
            assertEquals("robot facing " + h + " deg", 33.0, Aim.solve(target, pose(40, 0, h), 0, 0, 0).distanceIn, 1e-9);
        }
    }

    @Test
    public void toleranceUsesTheRobotCentreAsTheLeverArm() {
        RobotConfig.SHOOTER_FORWARD_IN = -7.0;
        RobotConfig.SHOOTER_LEFT_IN = 0.0;
        FieldPoint target = new FieldPoint(0, 0, 50);
        Aim.Solution s = Aim.solve(target, pose(99, 0, 0), 0, 0, 0);
        assertEquals("a 92 in shot (99 in from the robot centre)", 92.0, s.distanceIn, 1e-9);
        // A heading error turns the robot - and the shot line - about its CENTRE. Turned by the edge angle for
        // the 99 in centre distance, the ball's centre line crosses the opening's plane 8.6 in (half the 20 in
        // opening minus the ball radius) from the opening's centre: the ball just touches the edge.
        double free = 0.5 * FieldConstants.CELL_OPENING_WIDTH_IN - FieldConstants.POLLEN_RADIUS_IN;
        double edgeDeg = Math.toDegrees(Math.atan2(free, 99.0));
        double[] exit = Aim.shooterOnField(pose(99, 0, edgeDeg));
        double shot = Math.toRadians(edgeDeg + RobotConfig.SHOOTER_FACING_DEG);
        double along = -exit[0] / Math.cos(shot);                 // to the opening's plane (x = 0)
        assertEquals(free, Math.abs(exit[1] + along * Math.sin(shot)), 1e-9);
        // The tolerance is AIM_TOLERANCE_FRACTION of that angle (inside the limits), not of the exit-point angle.
        double expect = MathUtil.clamp(RobotConfig.AIM_TOLERANCE_FRACTION * edgeDeg,
                RobotConfig.AIM_TOLERANCE_MIN_DEG, RobotConfig.AIM_TOLERANCE_MAX_DEG);
        assertEquals(expect, s.toleranceDeg, 1e-9);
        assertEquals(Aim.toleranceDeg(99.0, 0.0), s.toleranceDeg, 1e-9);
    }

    @Test
    public void autoVolleyTurnsOntoTheAimHeadingAndSettles() {
        FieldPoint cellB = FieldConstants.aimPoint(Alliance.RED, HiveState.B);
        Pose b2 = FieldConstants.zoneBShootPose(Alliance.RED);
        double off = FieldConstants.offAxisDeg(Alliance.RED, HiveState.B, b2.x(), b2.y());
        // Pushed 10 deg clockwise during the volley: hold the aim heading on the same spot (B2 is clear of walls).
        Pose pushed = new Pose(b2.x(), b2.y(), b2.heading() - Math.toRadians(10));
        Aim.Solution aim = Aim.solve(cellB, pushed, off, 0, 0);
        Pose hold = AutoLegs.volleyAimHold(pushed, aim, 1.0);
        assertNotNull(hold);
        assertEquals("holds the aim heading", 0.0, MathUtil.wrapDeg(Math.toDegrees(hold.heading()) - aim.aimHeadingDeg), 1e-9);
        assertEquals(pushed.x(), hold.x(), 1e-9);
        assertEquals(pushed.y(), hold.y(), 1e-9);
        assertNull("no new aim pose until Pedro has had time to turn",
                AutoLegs.volleyAimHold(pushed, aim, AutoLegs.VOLLEY_REAIM_PERIOD_SEC));
        // Inside half the tolerance nothing is re-sent (the robot settles instead of hunting); outside, it turns.
        double tol = aim.toleranceDeg;
        Pose nearly = new Pose(b2.x(), b2.y(), Math.toRadians(aim.aimHeadingDeg - 0.4 * tol));
        assertNull(AutoLegs.volleyAimHold(nearly, Aim.solve(cellB, nearly, off, 0, 0), 1.0));
        Pose outside = new Pose(b2.x(), b2.y(), Math.toRadians(aim.aimHeadingDeg - 0.6 * tol));
        assertNotNull(AutoLegs.volleyAimHold(outside, Aim.solve(cellB, outside, off, 0, 0), 1.0));
        // A3 role 2 on S6 (front against the far wall, ~3.3 deg off its aim): it turns, moved off the wall first.
        Pose s6 = FieldConstants.startPose(Alliance.RED, StartSpot.S6);
        double offS6 = FieldConstants.offAxisDeg(Alliance.RED, HiveState.B, s6.x(), s6.y());
        Aim.Solution aimS6 = Aim.solve(cellB, s6, offS6, 0, 0);
        assertTrue("S6 is more than half the tolerance off", Math.abs(aimS6.headingErrorDeg) > aimS6.toleranceDeg / 2);
        Pose held = AutoLegs.volleyAimHold(s6, aimS6, 1.0);
        assertNotNull(held);
        assertEquals(0.0, MathUtil.wrapDeg(Math.toDegrees(held.heading()) - aimS6.aimHeadingDeg), 1e-9);
        double reach = FieldConstants.ROBOT_HALF_WIDTH * (Math.abs(Math.cos(held.heading())) + Math.abs(Math.sin(held.heading())));
        assertTrue("the turned robot is 0.5 in off the far wall: x " + held.x(),
                held.x() - reach >= -FieldConstants.WALL + 0.5 - 1e-9);
        assertTrue("moved straight off the wall", held.x() > s6.x() && Math.abs(held.y() - s6.y()) < 1e-9);
    }

    @Test
    public void aShooterBesideTheCentreLineStillHitsTheTarget() {
        RobotConfig.SHOOTER_FORWARD_IN = -7.0;
        RobotConfig.SHOOTER_LEFT_IN = 3.0;
        FieldPoint target = new FieldPoint(-50, 10, 50);
        Pose robot = pose(0, 0, 0);
        Aim.Solution s = Aim.solve(target, robot, 0, 0, 0);
        // Turn the robot to the aim heading: the shot line (from the shooter, out of the back) must pass
        // through the target.
        Pose aimed = new Pose(0, 0, Math.toRadians(s.aimHeadingDeg));
        double[] shooter = Aim.shooterOnField(aimed);
        double shot = Math.toRadians(s.aimHeadingDeg + RobotConfig.SHOOTER_FACING_DEG);
        double dx = target.x - shooter[0], dy = target.y - shooter[1];
        double sideways = -dx * Math.sin(shot) + dy * Math.cos(shot);
        double along = dx * Math.cos(shot) + dy * Math.sin(shot);
        assertEquals("the shot line passes through the target", 0.0, sideways, 1e-9);
        assertTrue("the target is in front of the shooter", along > 0);
        assertEquals(0.0, Aim.solve(target, aimed, 0, 0, 0).headingErrorDeg, 1e-9);
    }

    @Test
    public void trimAndVisionBiasTurnTheAimCounterClockwise() {
        FieldPoint target = new FieldPoint(0, 0, 50);
        Aim.Solution s = Aim.solve(target, pose(40, 0, 0), 0, 1.5, 0.5);
        assertEquals(2.0, s.aimHeadingDeg, 1e-9);
        assertEquals(2.0, s.headingErrorDeg, 1e-9);
    }

    @Test
    public void toleranceComesFromTheDistanceAndTheOpeningWidth() {
        // Robot-centre distances. The free half-width: 10 in x cos(off-axis) minus the 1.4 in POLLEN radius.
        double near = Aim.toleranceDeg(45, 0);
        double far = Aim.toleranceDeg(92, 0);
        double farSide = Aim.toleranceDeg(92, 40);
        assertEquals("capped close in", RobotConfig.AIM_TOLERANCE_MAX_DEG, near, 1e-9);
        double expectFar = RobotConfig.AIM_TOLERANCE_FRACTION * Math.toDegrees(Math.atan2(10.0 - 1.4, 92));
        assertEquals(expectFar, far, 1e-9);
        double expectSide = RobotConfig.AIM_TOLERANCE_FRACTION
                * Math.toDegrees(Math.atan2(10.0 * Math.cos(Math.toRadians(40)) - 1.4, 92));
        assertEquals(expectSide, farSide, 1e-9);
        assertTrue("the opening looks narrower from the side", farSide < far);
        assertTrue("further = tighter", far < near);
        assertEquals("never tighter than the minimum", RobotConfig.AIM_TOLERANCE_MIN_DEG, Aim.toleranceDeg(500, 70), 1e-9);
    }

    @Test
    public void fireGateNeedsTheHeadingInsideTheToleranceAndTheOpeningFacingUs() {
        FieldPoint target = FieldConstants.aimPoint(Alliance.RED, HiveState.A);
        Pose s1 = FieldConstants.startPose(Alliance.RED, StartSpot.S1);
        double off = FieldConstants.offAxisDeg(Alliance.RED, HiveState.A, s1.x(), s1.y());
        Aim.Solution aimed = Aim.solve(target, s1, off, 0, 0);
        assertTrue("S1 is aimed as placed", Aim.readyToFire(aimed, off));

        double tol = aimed.toleranceDeg;
        Pose inside = new Pose(s1.x(), s1.y(), s1.heading() + Math.toRadians(0.9 * tol));
        Pose outside = new Pose(s1.x(), s1.y(), s1.heading() + Math.toRadians(1.1 * tol));
        assertTrue(Aim.readyToFire(Aim.solve(target, inside, off, 0, 0), off));
        assertFalse("heading outside the tolerance", Aim.readyToFire(Aim.solve(target, outside, off, 0, 0), off));
        assertFalse("opening faces away", Aim.readyToFire(aimed, RobotConfig.FIRE_MAX_OFF_AXIS_DEG + 1));
    }

    @Test
    public void aimAssistTurnsTowardTheTargetAndSettles() {
        Aim.Solution s = new Aim.Solution();
        s.toleranceDeg = 3.0;
        s.headingErrorDeg = 1.0;                       // inside half the tolerance: stop (no hunting)
        assertEquals(0.0, Aim.assistTurn(s), 0.0);
        s.headingErrorDeg = 2.0;                       // close: at least the minimum turn power
        assertEquals(RobotConfig.AIM_ASSIST_MIN_TURN + RobotConfig.AIM_ASSIST_TURN_KP * 2.0, Aim.assistTurn(s), 1e-9);
        s.headingErrorDeg = -2.0;                      // clockwise
        assertTrue(Aim.assistTurn(s) < 0);
        s.headingErrorDeg = 170.0;                     // far: capped
        assertEquals(RobotConfig.AIM_ASSIST_MAX_TURN, Aim.assistTurn(s), 1e-9);
        assertEquals(-RobotConfig.AIM_ASSIST_MAX_TURN, Aim.assistTurn(negate(s)), 1e-9);
    }

    private static Aim.Solution negate(Aim.Solution s) {
        Aim.Solution n = new Aim.Solution();
        n.toleranceDeg = s.toleranceDeg;
        n.headingErrorDeg = -s.headingErrorDeg;
        return n;
    }

    @Test
    public void blueIsRedRotated() {
        for (StartSpot spot : new StartSpot[]{StartSpot.S1, StartSpot.S6}) {
            HiveState cell = spot == StartSpot.S1 ? HiveState.A : HiveState.B;
            Pose red = FieldConstants.startPose(Alliance.RED, spot);
            Pose blue = FieldConstants.startPose(Alliance.BLUE, spot);
            Aim.Solution r = Aim.solve(FieldConstants.aimPoint(Alliance.RED, cell), red, 0, 0, 0);
            Aim.Solution b = Aim.solve(FieldConstants.aimPoint(Alliance.BLUE, cell), blue, 0, 0, 0);
            assertEquals(r.headingErrorDeg, b.headingErrorDeg, 1e-9);
            assertEquals(r.distanceIn, b.distanceIn, 1e-9);
            assertEquals(0.0, MathUtil.wrapDeg(b.aimHeadingDeg - r.aimHeadingDeg - 180.0), 1e-9);
        }
    }
}
