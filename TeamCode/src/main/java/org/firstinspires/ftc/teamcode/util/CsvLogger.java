package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Writes rows of numbers to a .csv file on the Control Hub (/sdcard/FIRST/data/...).
 *
 * Files on the robot are NOT streamed over Wi-Fi, so this is allowed at events (R704.D only bans
 * streaming services like FTC Dashboard / Panels). Copy files off with Android Studio's Device
 * Explorer or "adb pull /sdcard/FIRST/data/".
 *
 * Never throws: if the file cannot be opened, logging is silently disabled (robot code keeps running).
 */
public class CsvLogger {
    private FileWriter writer;
    private final String path;

    /** Creates /sdcard/FIRST/data/<prefix>_<date-time>.csv and writes the header line. */
    public CsvLogger(String prefix, String header) {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir = new File(AppUtil.FIRST_FOLDER, "data");
        File file = new File(dir, prefix + "_" + stamp + ".csv");
        path = file.getAbsolutePath();
        try {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            writer = new FileWriter(file, false);
            writer.write(header + "\n");
        } catch (IOException e) {
            RobotLog.ee("12303", e, "CsvLogger could not open " + path);
            writer = null;
        }
    }

    public boolean isOpen() {
        return writer != null;
    }

    public String path() {
        return path;
    }

    /** Write one row. Values are joined with commas. */
    public void row(Object... values) {
        if (writer == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            Object v = values[i];
            if (v instanceof Double || v instanceof Float) {
                sb.append(String.format(Locale.US, "%.3f", ((Number) v).doubleValue()));
            } else {
                sb.append(v);
            }
        }
        sb.append('\n');
        try {
            writer.write(sb.toString());
        } catch (IOException e) {
            close();
        }
    }

    public void close() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
            // nothing useful to do
        }
        writer = null;
    }
}
