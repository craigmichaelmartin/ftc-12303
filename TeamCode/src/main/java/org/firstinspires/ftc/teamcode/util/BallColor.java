package org.firstinspires.ftc.teamcode.util;

/**
 * What a colour sensor thinks is in front of it.
 *
 * BIOBUZZ scoring elements: POLLEN = 2.8 in yellow ball (anyone may use it);
 * NECTAR = 3.6 in red or blue ball (alliance-owned). We must never CONTROL the opponent's NECTAR
 * (G408, no momentary exception per Q&A 3/4/23).
 */
public enum BallColor {
    /** Nothing close enough to the sensor. */
    NONE,
    /** Yellow ball. */
    POLLEN,
    /** Red NECTAR. */
    RED,
    /** Blue NECTAR. */
    BLUE,
    /** Something is there but the colour is not clear (dirty ball, odd lighting). Counted as a ball. */
    UNKNOWN;

    public boolean isBall() {
        return this != NONE;
    }
}
