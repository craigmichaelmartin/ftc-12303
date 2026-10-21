package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

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
        private Direction dir = Direction.FORWARD;
        public ServoController getController() { return null; }
        public int getPortNumber() { return 0; }
        public void setDirection(Direction d) { dir = d; }
        public Direction getDirection() { return dir; }
        public void setPower(double p) { power = p; }
        public double getPower() { return power; }
    }

    public static class Pos extends Dev implements Servo {
        public double pos = -1;
        private Direction dir = Direction.FORWARD;
        public ServoController getController() { return null; }
        public int getPortNumber() { return 0; }
        public void setDirection(Direction d) { dir = d; }
        public Direction getDirection() { return dir; }
        public void setPosition(double p) { pos = p; }
        public double getPosition() { return pos; }
        public void scaleRange(double a, double b) { }
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
