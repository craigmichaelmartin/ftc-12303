package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.ServoController;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Tiny fake devices for the laptop tests (no Control Hub needed). */
public final class FakeHardware {
    private FakeHardware() {
    }

    public abstract static class Dev implements HardwareDevice {
        public Manufacturer getManufacturer() { return Manufacturer.Other; }
        public String getDeviceName() { return "fake"; }
        public String getConnectionInfo() { return ""; }
        public int getVersion() { return 1; }
        public void resetDeviceConfigurationForOpMode() { }
        public void close() { }
    }

    public static class CR extends Dev implements CRServo {
        public double power;
        /** How many times setPower was called (0 = never commanded). */
        public int writes;
        private Direction dir = Direction.FORWARD;
        public ServoController getController() { return null; }
        public int getPortNumber() { return 0; }
        public void setDirection(Direction d) { dir = d; }
        public Direction getDirection() { return dir; }
        public void setPower(double p) {
            power = p;
            writes++;
        }
        public double getPower() { return power; }
    }

    /** What a fake DC motor was told. */
    public static final class MotorState {
        public double power;
        public int writes;
        public DcMotorSimple.Direction direction = DcMotorSimple.Direction.FORWARD;
        public DcMotor.RunMode mode;
        public DcMotor.ZeroPowerBehavior zeroPower;
    }

    /** A fake DC motor (a DcMotor proxy): records power, direction, run mode and zero-power behaviour. */
    public static DcMotor motor(MotorState st) {
        return (DcMotor) Proxy.newProxyInstance(DcMotor.class.getClassLoader(), new Class<?>[]{DcMotor.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setPower": st.power = (Double) args[0]; st.writes++; return null;
                        case "getPower": return st.power;
                        case "setDirection": st.direction = (DcMotorSimple.Direction) args[0]; return null;
                        case "getDirection": return st.direction;
                        case "setMode": st.mode = (DcMotor.RunMode) args[0]; return null;
                        case "getMode": return st.mode;
                        case "setZeroPowerBehavior": st.zeroPower = (DcMotor.ZeroPowerBehavior) args[0]; return null;
                        case "getZeroPowerBehavior": return st.zeroPower;
                        case "equals": return proxy == args[0];
                        case "hashCode": return System.identityHashCode(proxy);
                        case "toString": return "FakeMotor";
                        default: break;
                    }
                    Class<?> r = method.getReturnType();
                    if (r == boolean.class) return false;
                    if (r == int.class) return 0;
                    if (r == double.class) return 0.0;
                    if (r == float.class) return 0f;
                    if (r == long.class) return 0L;
                    return null;
                });
    }

    /** Colour + distance sensor: call none() / pollen() / red() / blue(). */
    public static class Color extends Dev implements NormalizedColorSensor, DistanceSensor {
        float r, g, b;
        public double cm = 10;
        private float gain = 1;
        public NormalizedRGBA getNormalizedColors() {
            NormalizedRGBA c = new NormalizedRGBA();
            c.red = r;
            c.green = g;
            c.blue = b;
            c.alpha = 1;
            return c;
        }
        public float getGain() { return gain; }
        public void setGain(float v) { gain = v; }
        public double getDistance(DistanceUnit u) { return u.fromCm(cm); }
        public void none() { cm = 10; r = g = b = 0.02f; }
        public void pollen() { cm = 2; r = 0.08f; g = 0.07f; b = 0.01f; }
        public void blue() { cm = 2; r = 0.005f; g = 0.02f; b = 0.06f; }
        public void red() { cm = 2; r = 0.06f; g = 0.005f; b = 0.008f; }
    }

    public static class Beam extends Dev implements DigitalChannel {
        public boolean state = true;   // pulled high = beam intact
        public Mode getMode() { return Mode.INPUT; }
        public void setMode(Mode m) { }
        public boolean getState() { return state; }
        public void setState(boolean s) { state = s; }
        @SuppressWarnings("deprecation")
        public void setMode(DigitalChannelController.Mode m) { }
    }

    /** A HardwareMap that returns the fake devices put into it. */
    public static class Map2 extends HardwareMap {
        public final Map<String, Object> devices = new HashMap<>();

        public Map2() {
            super(null, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T tryGet(Class<? extends T> cls, String name) {
            Object d = devices.get(name);
            return (d != null && cls.isInstance(d)) ? (T) d : null;
        }
    }
}
