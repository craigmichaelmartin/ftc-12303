package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.RouteGeometry.Sample;
import org.firstinspires.ftc.teamcode.config.FieldConstants;
import org.firstinspires.ftc.teamcode.config.FieldConstants.ParkSlot;
import org.firstinspires.ftc.teamcode.config.FieldConstants.StartSpot;
import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoLegs;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoPaths;
import org.firstinspires.ftc.teamcode.opmodes.auto.AutoRoutineType;
import org.firstinspires.ftc.teamcode.subsystems.Aim;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.HiveState;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sweeps EVERY AUTO route the code can drive - both alliances, every routine, every valid start and every
 * park choice - with an 18 in robot that turns exactly as AutoPaths turns it, and checks:
 *   walls (>= 0.4 in once off the start wall), our own side (y <= 0, red frame), the FLOWERS (>= 0.5 in),
 *   the HIVE feet and legs (>= 1 in), the other robot (A3 partner, or a partner in the CORNER slot when we
 *   park INNER: >= 1 in), and the ACTIVE spill landing box (tip-1 box after volley 1: clear; tip-2 box on
 *   A3 role 1's exit: at most a 0.6 in skim of its bottom edge - no route can clear both that box and the
 *   corner-slot robot, the gap between them is 19 in for an 18 in robot).
 * Legs are chained with perfect following (each leg starts at the previous leg's end pose). The shooter is
 * fixed at the robot's back, so every start pose has the front (intake) against its wall and every shooting
 * pose has the back toward its cell; the legs that turn between "back toward a cell" and "intake leading"
 * (the garden approach, the corridor to Zone B) are swept with exactly those headings.
 */
public class RouteSweepTest {

    private static final Map<String, double[][]> FIELD = RouteGeometry.fieldObstacles();
    private static final double[][] CORNER_ROBOT = RouteGeometry.square(-51, -56, 0, 9);
    private static final double ROLE1_EXIT_MAX_TIP2_SKIM_IN = 0.6;
    /**
     * B2 (A2 volley 2) is the ONE shooting pose allowed below the 30 in shot-band floor: with the placeholder
     * SHOOTER_FORWARD_IN = -7 it is a ~24.5 in shot. The route-clear alternative (-55, -12.75) gives 31.5 in
     * but is only ~1.6 in from the F-far FLOWER on an approach that drives toward it, so B2 stays at
     * (-48, -12.75) until the shot map (07 Exp 2b) decides it. If this floor fails once SHOOTER_FORWARD_IN is
     * measured, re-decide B2 (02 poses) - do not just lower the floor.
     */
    private static final double B2_MIN_SHOT_DISTANCE_IN = 24.0;

    /** One leg to check. */
    private static final class LegRun {
        final String name;
        final Alliance alliance;
        final Pose from;
        final AutoLegs.Leg leg;
        final boolean startsAtWall;
        final double[][] activeSpill;
        final double maxSpillPenetration;
        final Map<String, double[][]> robots = new LinkedHashMap<>();
        final List<List<double[][]>> movingRobots = new ArrayList<>();

        LegRun(String name, Alliance alliance, Pose from, AutoLegs.Leg leg, boolean startsAtWall,
               double[][] activeSpill, double maxSpillPenetration) {
            this.name = name;
            this.alliance = alliance;
            this.from = from;
            this.leg = leg;
            this.startsAtWall = startsAtWall;
            this.activeSpill = activeSpill;
            this.maxSpillPenetration = maxSpillPenetration;
        }

        AutoPaths.RoutePlan plan() {
            return leg.plan(from);
        }

        List<Sample> sweep() {
            return RouteGeometry.sweep(alliance, plan(), from.heading(), leg.end.heading());
        }
    }

    /** The legs of one routine, exactly as AutoRoutine drives them (no motion during holds/volleys). */
    private static List<LegRun> legsFor(AutoRoutineType type, Alliance a, StartSpot start, ParkSlot parkChoice) {
        List<LegRun> runs = new ArrayList<>();
        Pose pose = FieldConstants.startPose(a, start);
        String tag = a + " " + type + " " + start + " park " + parkChoice + ": ";
        boolean atWall = true;
        if (type.shootsStartCellFirst()) {
            AutoLegs.Leg shoot = AutoLegs.toShoot1(a, start);
            if (shoot != null) {
                runs.add(new LegRun(tag + "to shoot pose", a, pose, shoot, true, null, 0));
                pose = shoot.end;
                atWall = false;
            }
        }
        ParkSlot slot = AutoLegs.parkSlot(type, parkChoice);
        switch (type) {
            case A0_SAFE:
                runs.add(parkRun(tag, a, type, parkChoice, pose, atWall, null));
                break;
            case A1_TIP_AND_PARK:
                // holds at the firing pose until the spill has landed; still checked against the tip-1 box
                runs.add(parkRun(tag, a, type, parkChoice, pose, atWall, RouteGeometry.TIP1_BOX));
                break;
            case A2_TIP_RELOAD_VOLLEY: {
                AutoLegs.Leg garden = AutoLegs.toGarden(a);
                runs.add(new LegRun(tag + "garden approach", a, pose, garden, atWall, RouteGeometry.TIP1_BOX, 0));
                AutoLegs.Leg sweep = AutoLegs.sweep(a);
                runs.add(new LegRun(tag + "garden sweep", a, garden.end, sweep, false, RouteGeometry.TIP1_BOX, 0));
                AutoLegs.Leg zoneB = AutoLegs.toZoneB(a, sweep.end);
                runs.add(new LegRun(tag + "corridor to B2", a, sweep.end, zoneB, false, RouteGeometry.TIP1_BOX, 0));
                runs.add(parkRun(tag, a, type, parkChoice, zoneB.end, false, null));
                break;
            }
            case A3_ROLE1: {
                List<double[][]> role2Route = role2RouteFootprints(a);
                AutoLegs.Leg garden = AutoLegs.toGarden(a);
                runs.add(new LegRun(tag + "garden approach", a, pose, garden, atWall, RouteGeometry.TIP1_BOX, 0));
                AutoLegs.Leg sweep = AutoLegs.sweep(a);
                runs.add(new LegRun(tag + "garden sweep", a, garden.end, sweep, false, RouteGeometry.TIP1_BOX, 0));
                AutoLegs.Leg wait = AutoLegs.toRole1Wait(a, sweep.end);
                LegRun corridor = new LegRun(tag + "corridor to the wait point", a, sweep.end, wait, false, RouteGeometry.TIP1_BOX, 0);
                corridor.movingRobots.add(role2Route);          // role 2 may still be anywhere on its route
                runs.add(corridor);
                AutoLegs.Leg spot = AutoLegs.toRole1Spot(a);
                LegRun approach = new LegRun(tag + "lane to the role-1 spot (after 17 s)", a, wait.end, spot, false, null, 0);
                approach.robots.put("role 2 in the CORNER slot", CORNER_ROBOT);
                runs.add(approach);
                LegRun exit = parkRun(tag, a, type, parkChoice, spot.end, false, RouteGeometry.TIP2_BOX);
                runs.add(exit);
                break;
            }
            case A3_ROLE2: {
                LegRun park = parkRun(tag, a, type, parkChoice, pose, true, null);
                park.robots.put("role 1 waiting at the corridor end",
                        RouteGeometry.robotAt(RouteGeometry.toRed(a, AutoLegs.role1WaitPose(a))));
                runs.add(park);
                // The volley turns the robot onto the exact aim heading first (off the wall): park from there too.
                Pose aimed = role2AimedPose(a, start);
                LegRun fromAimed = parkRun(tag + "after aiming: ", a, type, parkChoice, aimed, false, null);
                fromAimed.robots.put("role 1 waiting at the corridor end",
                        RouteGeometry.robotAt(RouteGeometry.toRed(a, AutoLegs.role1WaitPose(a))));
                runs.add(fromAimed);
                break;
            }
        }
        // A partner already in the CORNER slot when we park INNER (S5/S7 + INNER is a CHECK warning instead).
        LegRun last = runs.get(runs.size() - 1);
        if (slot == ParkSlot.INNER && type.fixedParkSlot() == null && start != StartSpot.S5 && start != StartSpot.S7) {
            last.robots.put("partner in the CORNER slot", CORNER_ROBOT);
        }
        if (type == AutoRoutineType.A3_ROLE1) last.robots.put("role 2 in the CORNER slot", CORNER_ROBOT);
        return runs;
    }

    /** Where A3 role 2 stands while it fires: its start pose turned onto the second cell, off the wall. */
    private static Pose role2AimedPose(Alliance a, StartSpot start) {
        Pose s = FieldConstants.startPose(a, start);
        double off = FieldConstants.offAxisDeg(a, HiveState.B, s.x(), s.y());
        Aim.Solution aim = Aim.solve(FieldConstants.aimPoint(a, HiveState.B), s, off, 0.0, 0.0);
        return AutoLegs.chassisAimPose(s, aim.headingErrorDeg);
    }

    private static LegRun parkRun(String tag, Alliance a, AutoRoutineType type, ParkSlot choice, Pose from,
                                  boolean atWall, double[][] spill) {
        AutoLegs.Leg leg = AutoLegs.toPark(a, type, choice, from);
        double pen = (type == AutoRoutineType.A3_ROLE1) ? ROLE1_EXIT_MAX_TIP2_SKIM_IN : 0;
        return new LegRun(tag + "park " + AutoLegs.parkSlot(type, choice), a, from, leg, atWall, spill, pen);
    }

    /** Role 2's whole park drive (S6 -> escape -> CORNER), red frame footprints. */
    private static List<double[][]> role2RouteFootprints(Alliance a) {
        Pose s6 = FieldConstants.startPose(a, StartSpot.S6);
        AutoLegs.Leg leg = AutoLegs.toPark(a, AutoRoutineType.A3_ROLE2, ParkSlot.CORNER, s6);
        List<double[][]> out = new ArrayList<>();
        for (Sample s : RouteGeometry.sweep(a, leg.plan(s6), s6.heading(), leg.end.heading())) out.add(s.footprint());
        return out;
    }

    private static List<LegRun> allLegs() {
        List<LegRun> all = new ArrayList<>();
        for (Alliance a : Alliance.values()) {
            for (AutoRoutineType type : AutoRoutineType.values()) {
                for (StartSpot start : StartSpot.values()) {
                    if (!type.canStartFrom(start)) continue;              // invalid pairs do not move (A3 role 2: S6 only)
                    for (ParkSlot park : ParkSlot.values()) {
                        if (type.fixedParkSlot() != null && park != ParkSlot.INNER) continue;  // A3: choice ignored
                        all.addAll(legsFor(type, a, start, park));
                    }
                }
            }
        }
        return all;
    }

    // ---------------------------------------------------------------------------------------------
    @Test
    public void everyAutoRouteKeepsTheRobotClear() {
        List<String> failures = new ArrayList<>();
        int legs = 0;
        for (LegRun run : allLegs()) {
            legs++;
            double minWall = 1e9, maxY = -1e9, spill = 1e9;
            Map<String, Double> worst = new LinkedHashMap<>();
            String worstSpillAt = "";
            for (Sample s : run.sweep()) {
                double[][] r = s.footprint();
                double wall = RouteGeometry.wallGap(r);
                if (!(run.startsAtWall && s.s < 2.0)) minWall = Math.min(minWall, wall);
                if (wall < -0.01) failures.add(run.name + ": through a wall at " + s);
                maxY = Math.max(maxY, RouteGeometry.maxY(r));
                for (Map.Entry<String, double[][]> e : FIELD.entrySet()) {
                    worst.merge(e.getKey(), RouteGeometry.clearance(r, e.getValue()), Math::min);
                }
                for (Map.Entry<String, double[][]> e : run.robots.entrySet()) {
                    worst.merge(e.getKey(), RouteGeometry.clearance(r, e.getValue()), Math::min);
                }
                for (List<double[][]> moving : run.movingRobots) {
                    for (double[][] other : moving) worst.merge("A3 partner on its route", RouteGeometry.clearance(r, other), Math::min);
                }
                if (run.activeSpill != null) {
                    double c = RouteGeometry.clearance(r, run.activeSpill);
                    if (c < spill) {
                        spill = c;
                        worstSpillAt = s.toString();
                    }
                }
            }
            if (minWall < 0.4) failures.add(String.format("%s: %.2f in from a wall", run.name, minWall));
            if (maxY > 0.01) failures.add(String.format("%s: crosses the centre line (y %.2f)", run.name, maxY));
            for (Map.Entry<String, Double> e : worst.entrySet()) {
                String k = e.getKey();
                double need = k.startsWith("F-") ? 0.5 : 1.0;
                if (e.getValue() < need) failures.add(String.format("%s: %.2f in from %s", run.name, e.getValue(), k));
            }
            if (run.activeSpill != null && spill < -run.maxSpillPenetration) {
                failures.add(String.format("%s: %.2f in inside the active spill box at %s", run.name, -spill, worstSpillAt));
            }
        }
        assertTrue("no legs swept", legs > 100);
        assertTrue(failures.size() + " problems:\n" + String.join("\n", failures), failures.isEmpty());
    }

    @Test
    public void role1ParkRouteNeverRevisitsTheFarWallEscape() {
        // GEOMETRY-1 / CODE-5: the robot settles a hair west of x = -52 at its Zone B spot.
        for (Alliance a : Alliance.values()) {
            Pose spot = FieldConstants.role1ShootPose(a);
            for (double dxRed : new double[]{0.0, -0.01, -0.05, -0.5, -1.0, -2.0, 1.0}) {
                for (double dyRed : new double[]{0.0, -1.0, 1.0}) {
                    Pose here = new Pose(spot.x() + a.fromRedX(dxRed), spot.y() + a.fromRedY(dyRed), spot.heading());
                    AutoLegs.Leg leg = AutoLegs.toPark(a, AutoRoutineType.A3_ROLE1, ParkSlot.SOLO, here);
                    AutoPaths.RoutePlan plan = leg.plan(here);
                    double lastX = -1e9;
                    for (int i = 0; i < plan.points.size(); i++) {
                        Pose p = RouteGeometry.toRed(a, plan.points.get(i));
                        assertFalse("far-wall escape point in the role-1 exit " + a + " dx " + dxRed,
                                i > 0 && Math.abs(p.x() - FieldConstants.FAR_WALL_ESCAPE_X) < 0.5
                                        && Math.abs(p.y() - (FieldConstants.ZONE_B_SHOOT_ROLE1_Y + dyRed)) < 0.5);
                        assertTrue("the role-1 exit doubles back west " + a + " dx " + dxRed + " at point " + i,
                                p.x() >= lastX - 0.6);
                        lastX = Math.max(lastX, p.x());
                    }
                    assertEquals("the role-1 exit turns on its first piece", 0, plan.turnPiece);
                    Pose end = RouteGeometry.toRed(a, leg.end);
                    assertEquals(-27.0, end.x(), 1e-6);
                    assertEquals(-56.0, end.y(), 1e-6);
                    // and it stays clear of the corner-slot robot even from the jittered start
                    for (Sample s : RouteGeometry.sweep(a, plan, here.heading(), leg.end.heading())) {
                        assertTrue("role-1 exit hits the corner-slot robot at " + s,
                                RouteGeometry.clearance(s.footprint(), CORNER_ROBOT) >= 1.0);
                    }
                }
            }
        }
    }

    @Test
    public void innerSlotRoutesFromTheFarHalfUseTheLaneAfterTheEscape() {
        for (Alliance a : Alliance.values()) {
            Pose s6 = FieldConstants.startPose(a, StartSpot.S6);
            List<Pose> w = FieldConstants.routeToLoadingZone(a, s6.x(), s6.y(), ParkSlot.INNER);
            assertEquals(2, w.size());
            Pose esc = RouteGeometry.toRed(a, w.get(0)), lane = RouteGeometry.toRed(a, w.get(1));
            assertEquals(FieldConstants.FAR_WALL_ESCAPE_X, esc.x(), 1e-6);
            assertEquals(FieldConstants.INNER_LANE_X, lane.x(), 1e-6);
            // S7 (y = -40) and the role-1 spot (x = -52) never get the far-wall escape
            Pose s7 = FieldConstants.startPose(a, StartSpot.S7);
            for (Pose p : FieldConstants.routeToLoadingZone(a, s7.x(), s7.y(), ParkSlot.CORNER)) {
                assertTrue(Math.abs(RouteGeometry.toRed(a, p).x() - FieldConstants.FAR_WALL_ESCAPE_X) > 0.5);
            }
            Pose spot = FieldConstants.role1ShootPose(a);
            for (Pose p : FieldConstants.routeToLoadingZone(a, spot.x() + a.fromRedX(-1.0), spot.y(), ParkSlot.CORNER)) {
                assertTrue(Math.abs(RouteGeometry.toRed(a, p).x() - FieldConstants.FAR_WALL_ESCAPE_X) > 0.5);
            }
        }
    }

    @Test
    public void startSpotsAreSquareLegalAndBlueIsRedRotated() {
        for (StartSpot s : StartSpot.values()) {
            Pose red = FieldConstants.startPose(Alliance.RED, s);
            Pose blue = FieldConstants.startPose(Alliance.BLUE, s);
            double h = Math.toDegrees(red.heading());
            assertEquals(s + " is square to its wall", 0.0, MathUtil.wrapDeg(h - Math.round(h / 90.0) * 90.0), 1e-6);
            double[][] r = RouteGeometry.robotAt(red);
            assertEquals(s + " touches a wall", 0.0, RouteGeometry.wallGap(r), 0.02);
            assertTrue(s + " on our side", RouteGeometry.maxY(r) <= 0.01);
            for (Map.Entry<String, double[][]> e : FIELD.entrySet()) {
                assertTrue(s + " clear of " + e.getKey(), RouteGeometry.clearance(r, e.getValue()) > 0.5);
            }
            Pose back = RouteGeometry.toRed(Alliance.BLUE, blue);
            assertEquals(red.x(), back.x(), 1e-9);
            assertEquals(red.y(), back.y(), 1e-9);
            assertEquals(0.0, MathUtil.wrapDeg(Math.toDegrees(back.heading() - red.heading())), 1e-6);
            assertFalse("alliance-aware description", s.description(Alliance.RED).equals(s.description(Alliance.BLUE)));
            // Fixed rear shooter: the FRONT (intake) is against the wall, the back (shooter) faces into the field.
            for (Alliance a : Alliance.values()) {
                Pose p = RouteGeometry.toRed(a, FieldConstants.startPose(a, s));
                double frontX = p.x() + FieldConstants.ROBOT_HALF_WIDTH * Math.cos(p.heading());
                double frontY = p.y() + FieldConstants.ROBOT_HALF_WIDTH * Math.sin(p.heading());
                assertEquals(a + " " + s + ": the front edge touches the wall",
                        FieldConstants.WALL, Math.max(Math.abs(frontX), Math.abs(frontY)), 0.02);
                assertTrue(a + " " + s + " text says so", s.description(a).contains("front (intake) against the"));
            }
        }
        assertEquals("red S1: front on the audience wall, heading 0", 0.0,
                Math.toDegrees(FieldConstants.startPose(Alliance.RED, StartSpot.S1).heading()), 1e-9);
        assertEquals("blue S1: front on the far wall, heading 180", 180.0,
                Math.abs(Math.toDegrees(FieldConstants.startPose(Alliance.BLUE, StartSpot.S1).heading())), 1e-9);
    }

    @Test
    public void zoneASideSpotIsClearOfTheLandingBoxAndAnS1Partner() {
        Pose spot = FieldConstants.zoneASideShootPose(Alliance.RED);
        double[][] r = RouteGeometry.robotAt(spot);
        assertTrue(RouteGeometry.clearance(r, RouteGeometry.TIP1_BOX) > 5.0);
        assertTrue(RouteGeometry.clearance(r, RouteGeometry.robotAt(FieldConstants.startPose(Alliance.RED, StartSpot.S1))) > 2.0);
        assertTrue(RouteGeometry.wallGap(r) > 2.0);
        double offAxis = FieldConstants.offAxisDeg(Alliance.RED, HiveState.A, spot.x(), spot.y());
        assertTrue("side spot off-axis " + offAxis, offAxis < 35.0 && offAxis < RobotConfig.FIRE_MAX_OFF_AXIS_DEG);
        assertNull(AutoLegs.toShoot1(Alliance.RED, StartSpot.S1));
        assertNotNull(AutoLegs.toShoot1(Alliance.RED, StartSpot.S2));
    }

    @Test
    public void everyShootingPoseFacesItsCellAndIsInsideTheShotBand() {
        double floor = RobotConfig.SHOT_MIN_DISTANCE_IN;
        for (Alliance a : Alliance.values()) {
            check(a, "S1", FieldConstants.startPose(a, StartSpot.S1), HiveState.A, floor);
            check(a, "Zone A side spot", FieldConstants.zoneASideShootPose(a), HiveState.A, floor);
            check(a, "B2", FieldConstants.zoneBShootPose(a), HiveState.B, B2_MIN_SHOT_DISTANCE_IN);   // the one exception
            check(a, "role-1 spot", FieldConstants.role1ShootPose(a), HiveState.B, floor);
            check(a, "S6", FieldConstants.startPose(a, StartSpot.S6), HiveState.B, floor);
        }
    }

    /**
     * The shot band is checked on the distance the shot table and the AIM telemetry use: from where the ball
     * leaves the aimed robot (Aim.solve), not from the robot centre.
     */
    private static void check(Alliance a, String name, Pose p, HiveState cell, double minShotIn) {
        double off = FieldConstants.offAxisDeg(a, cell, p.x(), p.y());
        assertTrue(a + " " + name + " off-axis " + off, off <= 35.0);
        Aim.Solution aim = Aim.solve(FieldConstants.aimPoint(a, cell), p, off, 0.0, 0.0);
        assertTrue(a + " " + name + " shot distance " + aim.distanceIn,
                aim.distanceIn >= minShotIn && aim.distanceIn <= RobotConfig.SHOT_MAX_DISTANCE_IN);
        // The robot's BACK (the fixed shooter) points at the cell: inside the aim tolerance with no trim/bias.
        assertTrue(a + " " + name + " heading error " + aim.headingErrorDeg + " > tolerance " + aim.toleranceDeg, aim.onTarget());
        assertTrue(a + " " + name + " fire gate open", Aim.readyToFire(aim, off));
    }

    @Test
    public void shootingHeadingsPutTheBackOnTheTarget() {
        // red headings: the robot's back (heading + 180) points at the up cell
        assertEquals(0.0, deg(FieldConstants.startPose(Alliance.RED, StartSpot.S1)), 0.1);
        assertEquals(-32.2, deg(FieldConstants.zoneASideShootPose(Alliance.RED)), 0.1);
        assertEquals(180.0, Math.abs(deg(FieldConstants.zoneBShootPose(Alliance.RED))), 0.1);
        assertEquals(-154.1, deg(FieldConstants.role1ShootPose(Alliance.RED)), 0.1);
        assertEquals(180.0, Math.abs(deg(FieldConstants.startPose(Alliance.RED, StartSpot.S6))), 1e-9);
        assertEquals("the role-1 wait pose is the square heading nearest its shot", 180.0,
                Math.abs(deg(AutoLegs.role1WaitPose(Alliance.RED))), 1e-9);
        for (Alliance a : Alliance.values()) {
            for (Pose p : new Pose[]{FieldConstants.startPose(a, StartSpot.S1), FieldConstants.zoneASideShootPose(a)}) {
                double backDeg = deg(p) + RobotConfig.SHOOTER_FACING_DEG;
                double toCell = Math.toDegrees(Math.atan2(FieldConstants.aimPoint(a, HiveState.A).y - p.y(),
                        FieldConstants.aimPoint(a, HiveState.A).x - p.x()));
                assertEquals("S1 (square to its wall) is 0.01 in off the HIVE axis", 0.0, MathUtil.wrapDeg(backDeg - toCell), 0.05);
            }
        }
        // The garden sweep is driven intake-first (the intake is at the front).
        assertEquals(-90.0, deg(FieldConstants.gardenSweepStart(Alliance.RED)), 1e-9);
    }

    private static double deg(Pose p) {
        return MathUtil.wrapDeg(Math.toDegrees(p.heading()));
    }

    @Test
    public void pathTimeoutsLeaveRoomForTheSpeedCap() {
        for (LegRun run : allLegs()) {
            double len = run.plan().lengthIn();
            double cruise = AutoPaths.cruiseSpeedInPerSec(run.leg.speedFraction);
            double timeout = AutoPaths.timeoutSec(len, run.leg.speedFraction);
            assertTrue(run.name + ": timeout " + timeout + " for " + len + " in", timeout >= 1.5 * len / cruise);
        }
    }

    @Test
    public void a3TimeBudgetParksBothRobotsInTime() {
        double cruise = AutoPaths.cruiseSpeedInPerSec(0);     // shipped config (0.5 cap until tuned)
        // One unconfirmed feed (the slowest): carrier push, wait for a late confirmation, minimum interval.
        double feedSec = (RobotConfig.CARRIER_FEED_MS + RobotConfig.SHOT_CONFIRM_TIMEOUT_MS + RobotConfig.MIN_SHOT_INTERVAL_MS) / 1000.0;
        for (Alliance a : Alliance.values()) {
            Pose s6 = FieldConstants.startPose(a, StartSpot.S6);
            double parkLen = AutoLegs.toPark(a, AutoRoutineType.A3_ROLE2, ParkSlot.CORNER, s6).plan(s6).lengthIn();
            double parkDrive = 1.3 * parkLen / cruise;          // x1.3 for acceleration
            // Role 2, normal case: TIP confirmed at the latest fire time, TIP pause, then 4 feeds that are never
            // confirmed (the slowest volley): the 4th carrier push has ended before role 2 leaves.
            double fourthBallFed = RobotConfig.A3_ROLE2_LATEST_FIRE_SEC + RobotConfig.TIP_PAUSE_MS / 1000.0
                    + 3 * feedSec + RobotConfig.CARRIER_FEED_MS / 1000.0;
            assertTrue("role 2's 4th ball fed by " + fourthBallFed, fourthBallFed <= RobotConfig.A3_ROLE2_LEAVE_BY_SEC);
            // Role 2, ANY case - a volley that stalls until its own timeout (entry at 9 s + 5 s = 14 s), a start
            // delay, a late TIP: the park drive starts at A3_ROLE2_LEAVE_BY_SEC at the latest and must end
            // (with 0.5 s to spare) before role 1 may leave the corridor end.
            double role2Parked = RobotConfig.A3_ROLE2_LEAVE_BY_SEC + parkDrive;
            assertTrue("role 2 parked by " + role2Parked, role2Parked + 0.5 <= RobotConfig.A3_ROLE1_ZONE_B_NOT_BEFORE_SEC);
            // Role 1: the park deadline cuts everything at 23 s; its exit must still end well before 30 s.
            Pose spot = FieldConstants.role1ShootPose(a);
            double exitLen = AutoLegs.toPark(a, AutoRoutineType.A3_ROLE1, ParkSlot.INNER, spot).plan(spot).lengthIn();
            double role1Parked = RobotConfig.AUTO_PARK_DEADLINE_SEC + 1.3 * exitLen / cruise + 1.0;
            assertTrue("role 1 parked by " + role1Parked, role1Parked < 29.0);
        }
    }
}
