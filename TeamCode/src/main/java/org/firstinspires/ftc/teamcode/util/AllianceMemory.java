package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Remembers the alliance chosen in AUTO INIT in a tiny file on the Control Hub
 * (/sdcard/FIRST/12303_alliance.txt), so it survives an app restart or a Control Hub reboot.
 *
 * Why: the AUTO -> TeleOp hand-off (PoseStorage) lives in memory only. If the Robot Controller app
 * restarts between AUTO and TELEOP (brownout, ESD, battery swap), or TeleOp is started without AUTO,
 * TeleOp has no AUTO data. It then starts on the alliance saved here (RED only if the file is missing)
 * and shows a loud INIT line so the drivers check it.
 *
 * Never throws: a file problem just means "no remembered alliance".
 */
public final class AllianceMemory {
    private AllianceMemory() {
    }

    private static final String FILE_NAME = "12303_alliance.txt";

    private static File file() {
        return new File(AppUtil.FIRST_FOLDER, FILE_NAME);
    }

    /** Save the alliance (call from AUTO INIT whenever it changes, and at START). */
    public static void save(Alliance alliance) {
        if (alliance == null) return;
        try {
            File f = file();
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
            FileWriter w = new FileWriter(f, false);
            w.write(alliance.name());
            w.close();
        } catch (IOException | RuntimeException e) {
            RobotLog.ww("12303", "AllianceMemory could not save: " + e.getMessage());
        }
    }

    /** The last alliance saved by AUTO INIT, or null if there is none (or it cannot be read). */
    public static Alliance load() {
        try {
            File f = file();
            if (!f.exists()) return null;
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line = r.readLine();
            r.close();
            if (line == null) return null;
            return Alliance.valueOf(line.trim());
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
