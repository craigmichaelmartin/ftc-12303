package org.firstinspires.ftc.teamcode.util;

import java.util.Locale;

/**
 * A simple match clock started when the OpMode starts.
 *
 * TELEOP is 2:00. Audio cues on the field (manual Table 9-1): chime at 1:00 left ("FLOWER Ownership
 * Unlocked", NECTAR may go into FLOWERS from now on, G410) and train whistle at 0:20 left.
 * The FIELD timer is the authority - this clock only drives gamepad reminders.
 */
public class MatchTimer {
    private final double lengthSeconds;
    private long startNs = 0;
    private boolean started = false;

    public MatchTimer(double lengthSeconds) {
        this.lengthSeconds = lengthSeconds;
    }

    public void start() {
        startNs = System.nanoTime();
        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public double elapsedSeconds() {
        return started ? (System.nanoTime() - startNs) / 1e9 : 0.0;
    }

    public double remainingSeconds() {
        return Math.max(0.0, lengthSeconds - elapsedSeconds());
    }

    /** "1:23" style text for telemetry. */
    public String remainingText() {
        int total = (int) Math.ceil(remainingSeconds());
        return String.format(Locale.US, "%d:%02d", total / 60, total % 60);
    }
}
