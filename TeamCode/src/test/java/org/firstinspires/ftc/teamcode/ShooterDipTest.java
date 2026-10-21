package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.config.RobotConfig;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Shot-dip detection (CODE-8): the hub reports a 5-sample moving average refreshed every 10 ms, so a
 * shot's drop arrives over ~50 ms and the first sample out of the window is usually still above the dip
 * threshold. Every real drop below the threshold must be reported exactly once; a target change is not
 * a shot.
 */
public class ShooterDipTest {

    private double trueRpm;
    private final ArrayDeque<Double> hubSamples = new ArrayDeque<>();
    private Shooter shooter;

    @Before
    public void setUp() {
        Clock.setManualForTests(2_000_000);
        FakeHardware.Map2 hw = new FakeHardware.Map2();
        DcMotorEx motor = (DcMotorEx) Proxy.newProxyInstance(DcMotorEx.class.getClassLoader(),
                new Class<?>[]{DcMotorEx.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getVelocity") && (args == null || args.length == 0)) {
                        double sum = 0;
                        for (double v : hubSamples) sum += v;
                        double avgRpm = hubSamples.isEmpty() ? 0 : sum / hubSamples.size();
                        return avgRpm * RobotConfig.FLYWHEEL_TICKS_PER_REV / 60.0;
                    }
                    Class<?> r = method.getReturnType();
                    if (r == boolean.class) return false;
                    if (r == int.class) return 0;
                    if (r == double.class) return 0.0;
                    if (r == float.class) return 0f;
                    if (r == long.class) return 0L;
                    return null;
                });
        hw.devices.put(RobotConfig.FLYWHEEL, motor);
        shooter = new Shooter(hw, new ArrayList<String>());
        shooter.arm();
    }

    @After
    public void tearDown() {
        Clock.useRealTime();
    }

    /** One 10 ms step: the true speed moves, the hub averages the last 5 values. */
    private int step(double rpm) {
        trueRpm = rpm;
        hubSamples.addLast(trueRpm);
        while (hubSamples.size() > 5) hubSamples.removeFirst();
        shooter.update();
        Clock.advanceForTests(10);
        return shooter.consumeShotDip() ? 1 : 0;
    }

    private void settle(double target) {
        shooter.setTargetRpm(target);
        for (int i = 0; i < 30; i++) step(target);
        assertTrue(shooter.atSpeed());
    }

    /** A ball takes `drop` (fraction) out of the wheel at once, then it recovers over ~200 ms. */
    private int shot(double target, double drop) {
        int dips = 0;
        double low = target * (1 - drop);
        dips += step(low);
        for (int i = 1; i <= 20; i++) dips += step(low + (target - low) * i / 20.0);
        for (int i = 0; i < 10; i++) dips += step(target);
        return dips;
    }

    @Test
    public void everyRealShotIsReportedExactlyOnce() {
        double target = 3600;
        settle(target);
        for (double drop : new double[]{0.07, 0.11, 0.19}) {
            assertEquals("drop " + drop, 1, shot(target, drop));
            assertTrue(shooter.atSpeed());
            assertTrue(shooter.lastDipRpm() > 0.05 * target);
        }
    }

    @Test
    public void aSmallWobbleIsNotAShot() {
        double target = 3600;
        settle(target);
        assertEquals(0, shot(target, 0.035));   // leaves the 2.5 % window, never below the 5 % dip line
        assertTrue(shooter.atSpeed());
    }

    @Test
    public void aTargetChangeIsNotAShot() {
        settle(3600);
        shooter.setTargetRpm(3600 * 1.10);      // new distance: 10 % more
        int dips = 0;
        for (int i = 0; i < 40; i++) dips += step(3600 + 360.0 * Math.min(1.0, i / 25.0));
        assertEquals(0, dips);
    }
}
