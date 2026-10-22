package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.OdometryPods;
import org.firstinspires.ftc.teamcode.util.OdometryPods.Kind;
import org.junit.Test;

/**
 * Check: Odometry names the pod from one hand push (2026-10-21: the coach confirmed X/Y pods are on the
 * robot but not which kind). The pods must be told apart even with a few percent of push error, and a
 * reading that matches nothing must fall back to the measured value.
 */
public class OdometryPodsTest {

    @Test
    public void identifiesEachPodWithPushError() {
        for (Kind k : Kind.values()) {
            assertEquals(k, OdometryPods.identify(k.countsPerInch));
            assertEquals(k, OdometryPods.identify(k.countsPerInch * 1.04));   // pushed ~2 in short over 48 in
            assertEquals(k, OdometryPods.identify(k.countsPerInch * 0.96));
        }
    }

    @Test
    public void podWindowsDoNotOverlap() {
        Kind[] kinds = Kind.values();
        for (int i = 0; i < kinds.length; i++) {
            for (int j = i + 1; j < kinds.length; j++) {
                double lo = Math.min(kinds[i].countsPerInch, kinds[j].countsPerInch);
                double hi = Math.max(kinds[i].countsPerInch, kinds[j].countsPerInch);
                assertTrue(lo * (1 + OdometryPods.MATCH_TOLERANCE) < hi * (1 - OdometryPods.MATCH_TOLERANCE));
            }
        }
    }

    @Test
    public void unknownReadingsAreNotForcedOntoAPod() {
        assertNull(OdometryPods.identify(420.0));
        assertNull(OdometryPods.identify(700.0));
        assertTrue(OdometryPods.configAdvice(700.0).startsWith("PINPOINT_CUSTOM_TICKS_PER_INCH = 700.0"));
    }

    @Test
    public void adviceUsesSdkPodTypesForGoBildaAndCountsForOthers() {
        assertTrue(OdometryPods.configAdvice(Kind.GOBILDA_SWINGARM.countsPerInch).contains("goBILDA_SWINGARM_POD"));
        assertTrue(OdometryPods.configAdvice(Kind.GOBILDA_4_BAR.countsPerInch).contains("goBILDA_4_BAR_POD"));
        assertTrue(OdometryPods.configAdvice(Kind.SWYFT_38MM.countsPerInch * 0.99).startsWith("PINPOINT_CUSTOM_TICKS_PER_INCH = 871.5"));
    }

    @Test
    public void countsPerInchIgnoresSignAndDirectionUsesIt() {
        assertEquals(336.9, OdometryPods.countsPerInch(-16171, 48.0), 0.1);
        assertEquals("REVERSED", OdometryPods.directionAdvice(-16171));
        assertEquals("FORWARD", OdometryPods.directionAdvice(16171));
    }
}
