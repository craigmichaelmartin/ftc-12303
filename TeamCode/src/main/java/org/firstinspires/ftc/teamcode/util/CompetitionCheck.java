package org.firstinspires.ftc.teamcode.util;

/**
 * Event-readiness checks shown in INIT telemetry.
 *
 * R704.D + official Q&A 26 (2026-10-19): Wi-Fi streaming / debug services (FTC Dashboard, FTControl
 * Panels, "and others") are not allowed ANYWHERE in an event venue, pits and practice field included.
 * Pedro's AutoTune starts such a web server whenever its library is in the app, so it must be
 * left out of competition builds (set PEDRO_AUTOTUNE = false in build.dependencies.gradle).
 */
public final class CompetitionCheck {
    private CompetitionCheck() {
    }

    /** True if the Pedro AutoTune library (and its Wi-Fi web server) is inside this app build. */
    public static boolean autoTuneInstalled() {
        return classExists("com.pedropathing.tuning.autotune.Tuner");
    }

    /** True if FTC Dashboard or FTControl Panels was added to the build. */
    public static boolean dashboardInstalled() {
        return classExists("com.acmerobotics.dashboard.FtcDashboard") || classExists("com.bylazar.panels.Panels");
    }

    /** One warning line, or null when the build is event-legal. */
    public static String warning() {
        if (autoTuneInstalled()) {
            return "!! Pedro AutoTune web server is IN THIS BUILD - not legal at events (R704.D). "
                    + "Set PEDRO_AUTOTUNE = false in build.dependencies.gradle and re-deploy.";
        }
        if (dashboardInstalled()) {
            return "!! Dashboard/Panels library in this build - disable/remove it before events (R704.D).";
        }
        return null;
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, CompetitionCheck.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
