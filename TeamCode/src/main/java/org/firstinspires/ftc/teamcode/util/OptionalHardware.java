package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.List;

/**
 * Looks up a device by its Robot Configuration name WITHOUT crashing if it is missing.
 *
 * hardwareMap.get(...) throws an exception when a name is wrong or a device is not configured, which
 * stops the whole OpMode. We would rather drive with a broken sensor than not drive at all, so every
 * 12303 subsystem asks for its devices through here: a missing device returns null, the subsystem
 * turns itself off, and a warning shows in telemetry during INIT.
 */
public final class OptionalHardware {
    private OptionalHardware() {
    }

    /**
     * @param warnings list to add a message to when the device is missing (may be null)
     * @return the device, or null if it is not in the active Robot Configuration
     */
    public static <T> T get(HardwareMap hw, Class<T> type, String name, List<String> warnings) {
        T device = null;
        try {
            device = hw.tryGet(type, name);
        } catch (Exception e) {
            device = null;
        }
        if (device == null && warnings != null) {
            warnings.add("missing device '" + name + "' (" + type.getSimpleName() + ")");
        }
        return device;
    }

    /** Same as get(...) but does not add a warning (for devices that are truly optional). */
    public static <T> T getQuiet(HardwareMap hw, Class<T> type, String name) {
        return get(hw, type, name, null);
    }
}
