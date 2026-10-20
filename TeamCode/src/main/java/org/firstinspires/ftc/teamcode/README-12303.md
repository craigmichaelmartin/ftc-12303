# FTC 12303 - BIOBUZZ (2026-27) robot code

Starter code for our robot as it is built today: SWYFT Drive V2 mecanum, goBILDA Pinpoint, Limelight 3A,
servo turret on a ring gear, 72 mm flywheel on a goBILDA 5203 6000 rpm motor, servo intake, servo
"internal spinners" and a feeder gate. Strategy: the "pollen tip engine" (see the strategy brief).

**Everything you normally change is in ONE file: `config/RobotConfig.java`.** Every number marked
`TODO-MEASURE` is a guess - measure it on our robot before trusting it at a competition.

---

## 1. Setup (once)

1. Android Studio **Narwhal 3 Feature Drop or newer**. Open this repo (FtcRobotController v12.0, branch
   `12303-biobuzz`) and let Gradle sync.
2. Build: `Build > Make Project`. From a terminal: `./gradlew :TeamCode:assembleDebug`.
3. Deploy to the Control Hub with the green Run button (USB-C cable at events - never Wi-Fi tools there).
4. Make a Robot Configuration on the Driver Station with the names in section 2 (names are
   case-sensitive). Any missing device is skipped with a `WARN:` line in INIT telemetry - read those lines.
5. Libraries (in `build.dependencies.gradle`): FTC SDK 12.0.0, **Pedro Pathing 3** `com.pedropathing:revhub:3.0.1`
   (Maven Central) and, only when `PEDRO_AUTOTUNE = true`, `com.pedropathing:tuning:1.0.1` (needs the
   Dairy repo `https://repo.dairy.foundation/releases/` for its Sloth/Sinister dependencies).

## 2. Robot Configuration names

| Name | Device type in the DS configuration | Notes |
|---|---|---|
| `fl` `fr` `bl` `br` | Motor (goBILDA 5202/3/4 type is fine) | SWYFT Spike drive motors. Directions in `RobotConfig` section 2. |
| `flywheel` | Motor | goBILDA 5203 6000 rpm 1:1. Encoder cable MUST be plugged in (28 counts/rev). |
| `turret` | Servo (or "Full Range Servo") | Dual Mode servo in **positional** mode, or the 5-turn servo (`TURRET_SERVO_RANGE_DEG = 1800`). Code sets 500-2500 us. |
| `turretEncoder` | Motor on a free motor port (no motor needed) | OPTIONAL REV Through Bore Encoder in that port's ENCODER socket. Use port **0 or 3** (hardware-decoded). Set `TURRET_USE_ENCODER = true`. |
| `intake` | Continuous Rotation Servo | Intake roller. |
| `indexer` (+ optional `indexer2`) | Continuous Rotation Servo | "Internal spinners". |
| `feeder` | Servo | Gate that releases one ball into the flywheel. |
| `hood` | Servo | OPTIONAL. Held at `HOOD_POSITION`. |
| `pinpoint` | goBILDA Pinpoint Odometry Computer | I2C bus **1, 2 or 3** (bus 0 shares the hub IMU). |
| `limelight` | Limelight 3A (top-level USB device) | Must be a **3A** (the 3G is illegal, R702). USB 3.0 (blue) port. |
| `mouthColor` | REV Color Sensor V3 (Brushland in I2C mode works too) | At the intake mouth. |
| `slot4` | REV Color Sensor V3 | Sees the 4th storage slot ("full"). Different I2C bus from `mouthColor` (same address 0x52). |
| `feedBeam` | Digital Device | OPTIONAL IR break-beam where balls enter the flywheel. Strongly recommended. |

Servo power: hub servo ports are 5 V and share 2 A per pair (0-1, 2-3, 4-5). Spread the servos out, or use
a Servo Hub / power injector at 6 V for the turret and intake.

## 3. Code map

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
  Robot.java                 all subsystems + ONE update() per loop (manual bulk caching on both hubs)
  config/RobotConfig.java    THE file to edit: names, directions, gains, thresholds, AUTO options
  config/FieldConstants.java BIOBUZZ field geometry (CAD): aim points, tags, start spots, zones, park, routes
  pedro/Constants.java       Pedro follower (mecanum + Pinpoint + Foresight gains from AutoTune)
  subsystems/  Drivetrain  Shooter  Turret  Vision  HiveTracker  Indexer  Intake  Feedback
  opmodes/teleop/MainTeleOp.java    "12303 TeleOp"
  opmodes/auto/MainAuto.java        "12303 Auto" (+ AutoSelector, AutoRoutine, AutoPaths, AutoRoutineType)
  opmodes/tuning/  FlywheelTuner  TurretCalibrator  ShotMapLogger  LimelightCheck  SensorCheck  TipCounterTest
  util/  Alliance  HiveState  BallColor  FieldPoint  PoseStorage  MatchTimer  InterpLUT  MathUtil
         PedroFrame  CsvLogger  OptionalHardware  CompetitionCheck
TeamCode/src/autotune/java/.../pedro/   Pedro AutoTune procedures - compiled ONLY when PEDRO_AUTOTUNE = true
```

Every subsystem has an `enum State` and an `update()`; read the `switch` statements to see what it does.

## 4. Field frame (used everywhere, including Pedro)

Origin = field centre, **+X toward the AUDIENCE wall, +Y toward the BLUE wall**, heading 0 = facing the
audience, counter-clockwise positive, inches. Walls are at +-70.67 (the real field is 141.3 in, not 144).
Values are written once for RED; blue = red rotated 180 deg: `(x, y, h) -> (-x, -y, h + 180)` (never mirrored).

Our Pedro follower runs directly in this frame. Only the Pedro **visualizer website** uses a corner origin:
`x_pedro = y + 72`, `y_pedro = 72 - x`, `heading_pedro = heading - 90 deg` (`util/PedroFrame`).

HIVE states: **A** = match start (red AUDIENCE cell up, red shoots from the audience half), **B** = after a
TIP (red far cell up, red shoots from the far half). `HiveTracker` follows it (operator button, vision, AUTO).

## 5. Tuning order (do them in this order)

1. **Check: Sensors** - directions first. Hold dpad (one wheel each): each wheel alone must roll the robot
   FORWARD; flip `*_DIRECTION` in `RobotConfig` if not. A/B/X/Y test intake, spinners, gate, flywheel.
   Then hold POLLEN / red / blue NECTAR at the mouth sensor and set the hue windows, `MIN_SATURATION`,
   `MOUTH_BALL_PRESENT_CM`, `SLOT4_BALL_PRESENT_CM`. Push the robot: Pinpoint x must grow going forward,
   y going left.
2. **Pedro AutoTune** (at home only): set `PEDRO_AUTOTUNE = true` in `build.dependencies.gradle`, deploy,
   join the robot Wi-Fi, open `http://192.168.43.1:10158`. Run Mecanum Tuner (copy directions into
   `RobotConfig` section 2), Pinpoint Tuner (copy offsets/directions/pod type into section 3), Foresight
   Tuner (paste its block into `pedro/Constants.java`, set `FORESIGHT_TUNED = true`), then Tests
   (Localization, Line, Curve, Hold: 48 in should read 48 +- 0.5). **Set `PEDRO_AUTOTUNE = false` again.**
3. **Tune: Flywheel** - X = max-speed test on a full battery -> `FLYWHEEL_MAX_TICKS_PER_SEC`. A = spin,
   Y + dpad tunes P and I. B fires one ball: watch "drop / recovery". Set `FEEDER_OPEN_MS` so exactly one
   ball passes the gate.
4. **Tune: Turret Calibrator** - centre mark (A), 30-deg-left mark (X), left/right stops (Y/B), slew test
   (RB). Paste the printed lines into `RobotConfig` section 7. Also measure `TURRET_AXIS_*` and
   `TURRET_CENTER_DEG` (0 = shooter faces the robot front, 180 = back).
5. **Check: Limelight** - robot on S1: the up-cell tags should read ~49-50 in and "UP"; bearing error near
   0. Adjust `CAMERA_PITCH_DEG` / `CAMERA_HEIGHT_IN` / `CAMERA_YAW_OFFSET_DEG`. Set the Limelight pipeline
   ID filter to our 8 tags (red 30-37, blue 38-45), 640x480, over a **USB cable** from a laptop.
6. **Tune: Shot Map Logger** - first film a few shots to measure k = ball exit speed / wheel surface
   speed, and set `SHOT_TABLE_RPM_SCALE = 0.40 / k_measured` (lower k needs MORE rpm; k = 0.35 -> 1.14).
   Then 5 distances x 0/20/40 deg off-axis x 10 shots; score each X made / B bounced out / LB missed.
   Copy the suggested rows into `SHOT_TABLE`.
7. **Test: Tip Counter** - how many balls tip OUR practice HIVE (start cell with 3 NECTAR; empty cell),
   5 trials each, and the dwell time.
8. **AUTO ladder** - A0, then A1, then A2/A3. Promote a rung only after **8 of 10** good runs.

## 6. OpModes

| OpMode | Use |
|---|---|
| **12303 TeleOp** | Match TeleOp (controls below). Takes pose / ball count / HIVE state from AUTO. |
| **12303 Auto** | All AUTO routines; choose in INIT. Queues "12303 TeleOp" automatically. |
| Tune: Flywheel | Max speed, PIDF, recovery after shots, CSV log. |
| Tune: Turret Calibrator | Servo position <-> turret angle, soft limits, slew rate, encoder scale. |
| Tune: Shot Map Logger | Builds the distance -> RPM table (CSV + suggested rows). |
| Check: Limelight | Tags, cell, height (up/down), bearing error, vision bias. |
| Check: Sensors | Colour classification, slot 4, beam, Pinpoint, one-at-a-time direction tests. |
| Test: Tip Counter | Balls needed to tip our HIVE, dwell time. |

CSV files go to `/sdcard/FIRST/data/` on the Control Hub (on-robot files are fine under R704.D).

## 7. TeleOp controls

| Driver (gamepad1) | | Operator (gamepad2) | |
|---|---|---|---|
| left stick | drive, field-centric ("up" = away from you) | A | shooting mode on/off (flywheel + auto-aim) |
| right stick | turn | RB | **fire volley** (all balls, each only when ready) |
| LB (hold) | slow | X | fire one ball |
| RB (hold) | aim-assist: turn the chassis to centre the turret | B | stop firing |
| RT / LT (hold) | intake in (stops at 4) / out | Y | **HIVE tipped** (flip target cell) |
| back/share | heading reset (robot must face straight away from you) | dpad up/down | RPM trim +-50 |
| LB + Y | "robot is on S1" relocalize | dpad left/right | turret trim |
| | | right stick X | manual turret (auto-aim off) |
| | | LB (hold) | eject (intake + spinners backward) |
| | | back / L-stick click | ball count -1 / +1 |

Rumble: driver 2 blips = 4 balls (full); operator buzz = flywheel at speed; both long buzz = HIVE
switched (go to the other zone); 3 blips = 1:00 left; 1 s = 0:20 left (finish and park).
PS4/PS5 light bars: driver red = full; operator green = ready, yellow = spinning/aiming, purple =
turret at its limit (turn the chassis). Use WIRED pads (R904); Logitech F310 cannot rumble.
Telemetry shows ball count, RPM vs target, turret/aim error, HIVE state, distance, and warnings
(e.g. "IN THE SPILL LANDING BOX").

## 8. AUTO

INIT menu on gamepad1: dpad up/down = line, left/right = value, A = lock. Alliance, start spot
(S1..S7, shown with the exact pose and which wall the robot front faces), routine, delay 0-15 s,
preloads 0-4. Place the robot, then INIT, then do not touch it (G304.H).

| Routine | Plan |
|---|---|
| A0 Safe | drive to the LOADING ZONE (LEAVE 3 + PARK 5) |
| A1 Tip & Park | from S1: spin up, auto-aim, fire 4 preloads into the start cell (3 tip it), park via the y = -40 corridor |
| A2 | A1, sweep the 4 GARDEN POLLEN, fire them into the far cell from Zone B, park |
| A3 role 1 | A2 but shoots from (-52, -30) (clear of the partner at S6) and parks at (-27, -56) |
| A3 role 2 | from S6: wait for the partner's TIP (vision), fire 4 preloads into the far cell, park at (-51, -56) |

Safety nets: at `AUTO_PARK_DEADLINE_SEC` (23 s) it drops everything and parks; every path and volley
has a timeout; volley 2 is skipped if the far cell is not believed to be up; the robot drives off right
after each volley (the TIP spill lands ~3-6 s after the threshold ball, G409). Routes drive straight off
walls and only turn in open space (a turning 18 in robot reaches 12.7 in from its centre). A3 needs
rehearsal with the partner: role 2 must have parked before role 1 reaches Zone B.

## 9. Competition reminders

- **R704.D + Q&A 26: no Wi-Fi streaming/debug services anywhere in the venue (pits and practice field
  too).** Build with `PEDRO_AUTOTUNE = false` (INIT telemetry warns if AutoTune is inside the app).
  Do not add FTC Dashboard / Panels, or remove/disable them before the event. Tune the Limelight over USB.
- **G403**: TeleOp INIT moves nothing; actuators start at START. Never INIT an OpMode that moves
  things during the 8 s AUTO -> TeleOp transition.
- **G304.H / G305**: AUTO INIT sets the turret, gate and hood once, then the robot is still. Enable the
  30 s timer; "12303 TeleOp" is pre-selected after AUTO.
- **G401**: no gamepad input during AUTO (all choices in INIT).
- **G407 / G408**: max 4 balls (intake stops at 4; slot-4 sensor forces "full"); opponent NECTAR at the
  mouth is pushed out. There is NO momentary exception - fix the sensors before relying on them.
- **G409**: after a TIP keep out of the spill landing box until the balls land.
- **G410**: NECTAR into FLOWERS only in the last 60 s (chime at 1:00).
- **R702**: Limelight must be a 3A. **R705/R711**: name the devices `12303-RC` / `12303-DS`, change the
  Control Hub Wi-Fi password. **R105.B**: size limits must be mechanical, not turret soft limits.
- Pose / ball count / HIVE state pass from AUTO to TeleOp in memory (`PoseStorage`): do not restart the
  robot between AUTO and TeleOp. Without AUTO data TeleOp assumes the robot starts on S1.

## 10. TODO-MEASURE list (all in `config/RobotConfig.java` unless noted)

Drive directions (4); Pinpoint pod type, X/Y pod offsets and directions; `AIM_ASSIST_TURN_KP`;
`FLYWHEEL_DIRECTION`, `FLYWHEEL_MAX_TICKS_PER_SEC`, ready window, `FLYWHEEL_SHOT_DIP_FRACTION`,
`FLYWHEEL_DEFAULT_RPM`; `SHOT_TABLE` (physics model only) and `SHOT_TABLE_RPM_SCALE`; `HOOD_POSITION`;
turret `GEAR_RATIO`, `SERVO_CENTER`, `SERVO_DIRECTION`, `CENTER_DEG`, `MIN/MAX_DEG`, `AXIS_FORWARD/LEFT_IN`,
`SLEW_DEG_PER_SEC`, encoder ticks/deg and direction; feeder open/closed positions and `FEEDER_OPEN_MS`;
`INDEXER_DIRECTION`, `INDEXER_STAGE_POWER`, `INDEXER_STAGE_MS`; `INTAKE_DIRECTION`, `INTAKE_FACING_DEG`;
colour gain, presence distances, hue windows, `MIN_SATURATION`, `BALL_COUNT_DEBOUNCE_MS`; camera height,
pitch, forward offset, yaw offset; `AUTO_PARK_DEADLINE_SEC`, `AUTO_SWEEP_SPEED`,
`A3_ROLE2_TIP_WAIT_TIMEOUT_SEC`; `FieldConstants.GARDEN_SWEEP_X` / `GARDEN_SWEEP_END_Y`;
`pedro/Constants.java` Foresight block (AutoTune).
