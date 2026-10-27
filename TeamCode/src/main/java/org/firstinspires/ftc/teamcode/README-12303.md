# FTC 12303 - BIOBUZZ (2026-27) robot code

Code for our robot: SWYFT Drive V2 mecanum, goBILDA Pinpoint, Limelight 3A, an intake wheel on a floating
arm at the FRONT (DC motor), a tunnel that holds the balls, a carrier wheel above the tunnel that holds them
and feeds them one at a time, and a fixed shooting wheel at the top of the BACK (goBILDA 5203 6000 rpm
motor). The shooter cannot turn: the robot aims by turning its back to the target. Strategy: the "pollen tip
engine" (see the strategy brief).

**Everything you normally change is in ONE file: `config/RobotConfig.java`.** Every number marked
`TODO-MEASURE` is a guess - measure it on our robot before trusting it at a competition.

---

## 1. Getting the code onto the robot

The Control Hub runs the robot app. Android Studio builds that app from this repo and installs it on the
Control Hub. The Expansion Hub needs nothing: it talks to the Control Hub over the RS485 cable.

### Once per laptop

1. **Get access.** The repo is public, so anyone can clone it. To push changes, the coach adds your GitHub
   account as a collaborator (repo Settings > Collaborators).
2. **Install Android Studio** Narwhal 3 Feature Drop or newer (FTC SDK 12 needs it). It brings its own Java.
   On the first open, accept its prompts to install Android SDK parts.
3. **Clone the repo.** `File > New > Project from Version Control`, URL
   `https://github.com/craigmichaelmartin/ftc-12303.git`, then Clone (branch `main`, built on FtcRobotController
   v12.0). Sign in to GitHub in Android Studio if you will push changes. Let Gradle sync finish: the first sync downloads the libraries,
   needs internet and takes several minutes.
4. **Check that it builds.** `Build > Make Project`. Laptop tests (no robot needed):
   `./gradlew :TeamCode:testDebugUnitTest` (section 11).

### Once per robot

5. **Driver Station app 12.0**, the same version as this code. REV Driver Hub: update it with the REV Hardware
   Client (Windows), which also updates hub firmware when it suggests it. Android phone: update "FTC Driver
   Station" from Google Play. The robot side needs no separate update: loading this code installs the matching
   12.0 Robot Controller app.
6. **Names and password** (R705/R711): name the devices `12303-RC` / `12303-DS` and change the Control Hub
   Wi-Fi password (section 9).
7. **Robot Configuration** on the Driver Station: `Configure Robot` > New > scan, then name every port exactly
   as in section 2 (names are case-sensitive), Save and Activate. A missing device is skipped with a `WARN:`
   line in INIT telemetry - read those lines.
8. **Limelight pipeline.** It lives on the camera, not in this code. Plug the Limelight 3A into a laptop by USB,
   wait about 20 s for the green light, and open `http://limelight.local:5801` (or the Limelight Hardware
   Manager). In Settings, set the team number and click "Restart Vision Client". Then set the pipeline as in
   section 5 step 7; changes save on the camera. Back up the pipeline before any LimelightOS update (updates
   erase pipelines). On the robot the 3A goes in the Control Hub's blue USB 3.0 port.

### Every time you load code

9. Plug the laptop into the Control Hub's **USB-C** port with a data cable (charge-only cables don't work). The
   device list at the top of Android Studio shows "REV Robotics Control Hub".
10. Pick the `TeamCode` run configuration and click **Run** (green arrow). It builds and installs the app, and
    the Driver Station reconnects after a few seconds.
11. On the Driver Station, pick an OpMode: the first time, `Check: Sensors` and `Check: Odometry` (section 5
    order); in a match, `12303 Auto` (it queues `12303 TeleOp` next).

### Getting and sharing changes

- `Git > Pull` before you start, then Run again to load the latest code.
- After a change works on the robot, commit and push from Android Studio's Git menu with a message that says
  what changed and why.
- At home only, you can load code over Wi-Fi: join the robot's Wi-Fi network, run
  `adb connect 192.168.43.1:5555`, then Run. **At events: USB only** - no Wi-Fi tools anywhere in the venue
  (R704.D, Q&A 26). Pedro AutoTune stays out of the build (`PEDRO_AUTOTUNE = false`, the default) except at
  home while tuning (section 5 step 8).

### If something goes wrong

- **No Control Hub in Android Studio's device list:** try another USB-C cable (many are charge-only) and another
  laptop port. In a terminal, `adb devices` should list the hub.
- **Gradle sync fails:** check the internet connection, then `File > Sync Project with Gradle Files`.
- **The Driver Station warns about a version mismatch:** update the Driver Station app (step 5).
- **`12303 TeleOp` is missing from the OpMode list:** the install did not finish. Run again and read Android
  Studio's Run window for errors.

### Libraries

In `build.dependencies.gradle`: FTC SDK 12.0.0, **Pedro Pathing 3** `com.pedropathing:revhub:3.0.1` (Maven
Central) and, only when `PEDRO_AUTOTUNE = true`, `com.pedropathing:tuning:1.0.1` (needs the Dairy repo
`https://repo.dairy.foundation/releases/` for its Sloth/Sinister dependencies). JUnit 4.13.2 is a test-only
dependency (never in the robot app).

## 2. Robot Configuration names

| Name | Device type in the DS configuration | Notes |
|---|---|---|
| `fl` `fr` `bl` `br` | Motor (goBILDA 5202/3/4 type is fine) | SWYFT Spike drive motors. Directions in `RobotConfig` section 2. |
| `flywheel` | Motor | Shooting wheel at the top of the back: goBILDA 5203 6000 rpm 1:1 (check the motor label). Encoder cable MUST be plugged in (28 counts per motor rev). If the wheel is geared or belted to the motor, multiply `FLYWHEEL_TICKS_PER_REV` by the ratio (measure). |
| `intake` | Motor (a Continuous Rotation Servo also works) | Intake wheel on the floating front arm (goBILDA Yellow Jacket motor). |
| `carrier` | Motor **or** Continuous Rotation Servo | Carrier wheel above the tunnel. Stopped, it holds the balls; spinning, it pushes one ball into the shooting wheel. If a servo drives it, configure that port as a Continuous Rotation Servo named `carrier`. |
| `pinpoint` | goBILDA Pinpoint Odometry Computer | I2C bus **1, 2 or 3** (bus 0 shares the hub IMU). |
| `limelight` | Limelight 3A: shows up as an "Ethernet Device" when you scan; rename it `limelight` | Must be a **3A** (the 3G is illegal, R702). USB 3.0 (blue) port. Mount it looking out of the robot's **back** (the shooter side), pitched up ~35-40 deg (`CAMERA_YAW_OFFSET_DEG = 180`). |
| `mouthColor` | REV Color Sensor V3 (a Brushland Color Rangefinder in **I2C mode** works too; its digital outputs are NOT read) | OPTIONAL ball sensor at the intake mouth, where EVERY ball (garden balls taken with the wheel's edge too) passes within `MOUTH_BALL_PRESENT_CM` before the intake commits it. Without it the robot cannot count balls coming in or refuse NECTAR (section 7). |
| `slot4` | REV Color Sensor V3 | OPTIONAL. Sees the 4th ball in the tunnel ("full"). Different I2C bus from `mouthColor` (same address 0x52). |
| `feedBeam` | Digital Device | OPTIONAL IR break-beam where balls enter the shooting wheel. Confirms every shot. |

With the carrier on a motor port that is 7 motors (R503 allows 8): spread them over both hubs. The ball
sensors are optional: every missing one only switches its feature off.

## 3. Code map

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
  Robot.java                 all subsystems + ONE update() per loop (manual bulk caching on both hubs)
  config/RobotConfig.java    THE file to edit: names, directions, gains, thresholds, AUTO options
  config/FieldConstants.java BIOBUZZ field geometry (CAD): aim points, tags, start spots, park slots, routes
  pedro/Constants.java       Pedro follower (mecanum + Pinpoint + Foresight gains from AutoTune)
  subsystems/  Drivetrain  Shooter  Aim  Vision  HiveTracker  Carrier  Intake  Feedback
  opmodes/teleop/MainTeleOp.java    "12303 TeleOp"
  opmodes/auto/MainAuto.java        "12303 Auto" (+ AutoSelector, AutoRoutine, AutoLegs, AutoPaths, AutoRoutineType)
  opmodes/tuning/  FlywheelTuner  ShotMapLogger  LimelightCheck  SensorCheck  OdometryCheck  TipCounterTest
  util/  Alliance  HiveState  BallColor  FieldPoint  PoseStorage  AllianceMemory  MatchTimer  InterpLUT
         MathUtil  Clock  PedroFrame  CsvLogger  OptionalHardware  CompetitionCheck  OdometryPods
TeamCode/src/test/java/.../        laptop JUnit tests (route sweeps, ball handling, aim, vision geometry, HIVE tracker, shot dips, AUTO timing, odometry pods)
TeamCode/src/autotune/java/.../pedro/   Pedro AutoTune procedures - compiled ONLY when PEDRO_AUTOTUNE = true
```

Every subsystem has an `enum State` (the carrier: `FeedState`) and an `update()`; read the `switch`
statements to see what it does. `Aim` is pure math: the heading that points the robot's back at the target,
the distance for the shot table, and the aim tolerance. `AutoLegs` holds every AUTO drive as pure geometry
(plus the volley's turn onto the aim): the robot drives exactly those legs and the route test sweeps exactly
those legs.

## 4. Field frame and names (used everywhere, including Pedro)

Origin = field centre, **+X toward the AUDIENCE wall, +Y toward the BLUE wall**, heading 0 = facing the
audience, counter-clockwise positive, inches. Walls are at +-70.67 (the real field is 141.3 in, not 144).
Values are written once for RED; blue = red rotated 180 deg: `(x, y, h) -> (-x, -y, h + 180)` (never mirrored).
A robot's heading is the way its FRONT (the intake) faces; the shooter points the other way (`SHOOTER_FACING_DEG
= 180`).

Our Pedro follower runs directly in this frame. Only the Pedro **visualizer website** uses a corner origin:
`x_pedro = y + 72`, `y_pedro = 72 - x`, `heading_pedro = heading - 90 deg` (`util/PedroFrame`).

Alliance-neutral words used in code, telemetry and docs:
- **our start wall** = the end wall behind S1: red AUDIENCE wall, blue FAR wall.
- **start cell** = the cell that is up at match start (red audience cell, tags 34-37; blue far cell, tags
  42-45). **Second cell** = the other one (red tags 30-33, blue 38-41); it comes up after TIP 1.
- **Park slots** in our LOADING ZONE: **CORNER** = the LZ end nearer the field corner, red (-51, -56) /
  blue (51, 56); **INNER** = red (-27, -56) / blue (27, 56); **SOLO** = the middle, red (-35.25, -55) /
  blue (35.25, 55) - SOLO touches both slots, so use it only when the partner does not park in AUTO.
  Rule: the first robot to arrive takes CORNER.

HIVE states: **A** = match start (red AUDIENCE cell up, red shoots from the audience end), **B** = after a
TIP (red far cell up, red shoots from the far end). `HiveTracker` follows it: the operator's **Y**, pressed on
every human-player "TIP!" (the camera can NOT see our own TIP from the zone we fired from - after a TIP all
our tags face the other half), vision (0.5 s of unbroken evidence; it confirms a state only from the zone the
up cell faces), and AUTO. After Y, vision cannot change the state for 5 s (`HIVE_OPERATOR_PRIORITY_MS`); after
that 0.5 s of disagreeing vision can switch it back - if it keeps flipping wrongly, set
`HIVE_VISION_AUTO_SWITCH = false`. A Y press within 3 s of a CAMERA switch confirms that switch instead of
flipping back (`HIVE_Y_CONFIRMS_VISION_MS`).

## 5. Tuning order (do them in this order)

This is the order of the testing plan (`docs/plan/07-testing-and-practice.md`; "Exp" = its experiment
numbers): steps 1-6 are its week 1 (set-up checks, Exp 0, Exp 1, Exp 2a), steps 7-10 its week 2 (Limelight
on the back, Pedro, the aim-assist gains, Exp 2b, 2c and 3), and the AUTO ladder (step 11) starts in week 2
with A0 and A1.

1. **Tunnel capacity** (Exp 0a, no OpMode) - with the carrier stopped, push POLLEN into the tunnel by hand;
   once the intake direction is right (step 2), also let the intake pull them in (Check: Sensors, hold A). If
   a 5th ball fits, add a mechanical stop so the tunnel holds 4 (G407): without a mouth sensor nothing else
   stops a 5th ball.
2. **Check: Sensors** - directions first. Hold dpad (one wheel each): each wheel alone must roll the robot
   FORWARD (toward the intake); flip `*_DIRECTION` in `RobotConfig` if not. A = intake in (balls go in),
   B = carrier toward the shooting wheel, X = carrier backward, Y = flywheel 1500 rpm (a ball must leave out of
   the back): flip `INTAKE_DIRECTION` / `CARRIER_DIRECTION` / `FLYWHEEL_DIRECTION` if not. Try B and Y with no
   balls inside first. With the ball sensors fitted: hold POLLEN / red / blue NECTAR at the mouth sensor and
   set the hue windows, `MIN_SATURATION`, `MOUTH_BALL_PRESENT_CM`, `SLOT4_BALL_PRESENT_CM`. Then press RB, hold
   A and let the intake pull 10 single POLLEN in (none touching another): set `MOUTH_ONE_BALL_MAX_MS` to the
   "longest" time in view + 30% (the line shows the suggestion; redo it whenever the intake, its motor or its
   power changes - see section 9). Pinpoint: the OpMode zeroes the pose at INIT (gamepad1 back zeroes it
   again) - push the robot: x must grow going forward, y going left.
3. **Check: Odometry** - find the X/Y pods' type and wiring (push the robot 48 in forward, then 48 in left):
   it says whether a Pinpoint is found, names the pod from its counts per inch (goBILDA 4-bar ≈ 505, goBILDA
   swing-arm ≈ 337, SWYFT 38 mm ≈ 872) and prints the RobotConfig section 3 lines for pod type and directions.
   If the Pinpoint is not found and the counts appear under a motor port instead, the pods are wired to the
   hub: the code then needs a two-wheel localizer (ask the coach).
4. **Tune: Flywheel** - X = max-speed test on a full battery -> `FLYWHEEL_MAX_TICKS_PER_SEC`. A = spin,
   Y + dpad tunes P and I. B fires one ball (it waits for the flywheel only, and the carrier stops as soon as
   the shot is confirmed): watch "last shot: drop / recovery". If "drop" never updates, the shot dip is not
   detected: lower `FLYWHEEL_SHOT_DIP_FRACTION` or fit the feed beam (the dip or the beam confirms each shot
   and stops the carrier).
   **Carrier feed time (Exp 0b):** RB = a **timed push**: the carrier runs for exactly `CARRIER_FEED_MS` and
   does not stop at the dip - the push it makes when no shot is confirmed. With 2 balls inside, press Y until
   `FEED_MS` is selected, change it with dpad up/down (10 ms steps) and press RB each time: find the shortest
   time that always pushes one ball out and the longest that never pushes the second, then copy the middle
   into `CARRIER_FEED_MS` in RobotConfig (a value changed in the OpMode lasts only until the app restarts).
   Then load 4 and press B four times: exactly one ball per press.
5. **Test: Tip Counter** (Exp 1) - how many balls tip OUR practice HIVE (start cell with 3 NECTAR; empty
   cell), 5 trials each, and the dwell time. RB fires one (counted when the feed finishes, confirmed or not;
   hold RT to aim); **LB = the last launched ball missed / bounced out** (Exp 1 counts only balls that stay
   in); "unconfirmed" shows feeds that may not have launched a ball. The shot table is not measured yet: if
   shots from S1 land short or long, trim the RPM with dpad left/right until they go in, and note that RPM
   for Exp 2a/2b.
6. **Exit point, exit angle and k** (Exp 2a) - tape-measure where the ball leaves the shooting wheel, from the
   robot centre: `SHOOTER_FORWARD_IN` (negative = behind the centre) and `SHOOTER_LEFT_IN`. Every shot
   distance is measured from there. Then film shots at 240 fps (Tune: Flywheel: A at 3,000 / 3,500 / 4,000
   rpm, B one ball) to measure the fixed exit angle and k = ball exit speed / wheel surface speed: set
   `SHOT_TABLE_RPM_SCALE = 0.40 / k_measured` (lower k needs MORE rpm; k = 0.35 -> 1.14).
7. **Check: Limelight** - first, over a **USB cable** from a laptop, set the ONE AprilTag pipeline: **ID filter
   30-45** (both HIVEs - the code keeps only our 8; never filter to one alliance's 8 or the robot is blind on
   the other colour), marker size **82.55 mm** (3.25 in), **Full 3D on** (the "3D" column must show "y"),
   640x480. The camera looks out of the robot's back. Robot on OUR S1 (front against our start wall, back
   toward the HIVE): the up-cell tags read ~49-50 in and "UP", bearing error near 0; adjust
   `CAMERA_PITCH_DEG` / `CAMERA_HEIGHT_IN` / `CAMERA_FORWARD_IN` / `CAMERA_LEFT_IN` / `CAMERA_YAW_OFFSET_DEG`.
   Then at the calibration spot (36, -12.75), back toward the HIVE (blue (-36, 12.75)), press back: the second
   cell's tags read ~35-36 in "dn" and "vision says A". If every height is off by the same FACTOR, fix the
   marker size, not the pitch. The other alliance's tags show as "Ignored": normal.
8. **Pedro AutoTune** (at home only), then the **aim-assist gains**. Set `PEDRO_AUTOTUNE = true` in
   `build.dependencies.gradle` (or build with `-PpedroAutoTune=true`), deploy, join the robot Wi-Fi, open
   `http://192.168.43.1:10158`. Run Mecanum Tuner (copy directions into `RobotConfig` section 2), Pinpoint
   Tuner (copy offsets/directions/pod type into section 3), Foresight Tuner (paste its block into
   `pedro/Constants.java`, set `FORESIGHT_TUNED = true`, remove the `maxPathSpeed` 0.5 cap), then Tests
   (Localization, Line, Curve, Hold: 48 in should read 48 +- 0.5). **Set `PEDRO_AUTOTUNE = false` again and
   re-deploy.** Then hold driver RB in `12303 TeleOp` near the HIVE: raise `AIM_ASSIST_MIN_TURN` if the robot
   stops short of the target, lower `AIM_ASSIST_TURN_KP` if it overshoots.
9. **Tune: Shot Map Logger** (Exp 2b, then 2c) - 5 distances x 0/20/40 deg off-axis x 10 shots (hold RT so
   the robot turns its back onto the cell; a shot waits until it is aimed); score each X made / B bounced out
   / LB missed (RB is ignored until the last shot is scored). back = delete the last row from the screen table
   (the CSV keeps it and adds a `DELETED_PREVIOUS` marker row). Copy the suggested rows into `SHOT_TABLE`.
   Then the bounce-out check (2c) at the table settings: 20 shots into an empty cell, 20 into a cell holding
   3 NECTAR.
10. **Flywheel recovery** (Exp 3) - Tune: Flywheel with 4 balls loaded: LB = a 4-feed volley (each feed waits
    until the flywheel is back at speed, as in a match). Compare "last shot: drop / recovery" and the CSV with
    and without the steel flywheel disc, on a fresh and a used battery.
11. **AUTO ladder** (Exp 7) - A0, then A1, then A2/A3. Promote a rung only after **8 of 10** good runs. A2 and
    A3 need a TUNED follower (step 8); A3 also needs a rehearsal with the partner.

## 6. OpModes

| OpMode | Use |
|---|---|
| **12303 TeleOp** | Match TeleOp (controls below). Takes pose / ball count / HIVE state from AUTO. |
| **12303 Auto** | All AUTO routines; choose in INIT. Queues "12303 TeleOp" automatically. |
| Tune: Flywheel | Max speed, PIDF, shot dip, one-ball feeds (B), timed carrier pushes for `CARRIER_FEED_MS` (RB), 4-feed volleys for the recovery test (LB), CSV log. |
| Tune: Shot Map Logger | Builds the distance -> RPM table (CSV + suggested rows). RT = aim-assist. |
| Check: Limelight | Tags, cell, height (up/down), bearing error, vision bias, for the camera looking out of the back. Nothing moves. |
| Check: Odometry | Pinpoint found?, pod type from a 48 in push (goBILDA 4-bar / swing-arm / SWYFT, or the measured counts per inch), pod directions; motor-port encoders for hub-wired pods. |
| Check: Sensors | One-at-a-time direction tests (wheels, intake, carrier, flywheel), Pinpoint (zeroed), and the ball sensors when fitted: colour classification, slot 4, beam, ball time at the mouth (`MOUTH_ONE_BALL_MAX_MS`). |
| Test: Tip Counter | Balls needed to tip our HIVE, dwell time. RT = aim-assist. |

Every 12303 OpMode shows `!! Pedro AutoTune web server is IN THIS BUILD ...` (or the Dashboard/Panels line) in
INIT if such a library is in the app. CSV files go to `/sdcard/FIRST/data/` on the Control Hub (on-robot files
are fine under R704.D).

## 7. TeleOp

### Controls

| Driver (gamepad1) | | Operator (gamepad2) | |
|---|---|---|---|
| left stick | drive, field-centric ("up" = away from you) | A | shooting mode on/off (flywheel + auto-aim). OFF also **cancels a requested volley** |
| right stick | turn | RB | **fire volley** (all balls, each only when ready; without a mouth sensor it feeds 4 times) |
| LB (hold) | slow | X | fire one ball (works even if the count says 0) |
| RB (hold) | **aim-assist**: the robot turns until its back (the shooter) points at the target cell; the left stick still drives | B | stop firing |
| RT / LT (hold) | intake in (stops at 4 with the mouth or slot-4 sensor; without them, stop at 4 by eye) / out | Y | **HIVE tipped** - press on every "TIP!": flips the target cell; within 3 s of a camera switch it only CONFIRMS that switch (press again to really flip) |
| back/share | heading reset (the robot's front must face straight away from you) | dpad up/down | RPM trim +-50 |
| LB + Y | relocalize: robot parked exactly on OUR S1 (red: front on the audience wall at (61.67, -12.75); blue: front on the far wall at (-61.67, 12.75)) | dpad left/right | aim trim +-0.5 deg (left = the ball goes further left, seen from the shooter looking toward the cell) |
| | | right-stick click | **manual aim** on/off: RB/X then fire as soon as the flywheel is at speed, wherever the robot points (no aim / off-axis / stillness check) |
| | | LB (hold) | eject (intake + carrier backward); without a mouth sensor the count does not drop: press back once per ball ejected |
| | | back / L-stick click | ball count -1 / +1 |

INIT: gamepad1 X = BLUE, B = RED; gamepad2 dpad up/down = ball count. Nothing moves in TeleOp INIT (G403).
Without AUTO data (app restart, reboot, or AUTO not run) TeleOp uses the alliance saved by the last AUTO INIT
(`/sdcard/FIRST/12303_alliance.txt`; RED if there is none), assumes OUR S1 and 4 balls, and shows
`!! NO AUTO DATA - ALLIANCE = ...`: check it and press X/B if needed. With AUTO data, X/B still changes the
alliance and rotates the handed-over pose 180 deg (an AUTO run with the wrong alliance drove the rotated
paths). If AUTO handed over HIVE state B, INIT says so: after an A3 double tip the start cell is up again -
press Y after START.

**Without a mouth sensor** INIT shows `!!!! NO MOUTH SENSOR ...` (TeleOp: at the top; AUTO: right under the
selection menu): the robot cannot count balls coming in or refuse NECTAR. The count starts at the preloads
(4) and drops by one per feed, confirmed or not, so the intake refuses while the count says 4; after a volley
RT never stops by itself (RT stops at 4 only with the mouth sensor or the slot-4 sensor). The drivers stop
intaking at 4 balls by eye and steer around NECTAR; RB feeds 4 times (empty feeds are harmless). An eject
(operator LB) does not lower the count either: after an eject the operator presses back once per ball that
went out - otherwise the intake keeps refusing (`Intake refused: FULL`). The operator fixes the count with
back / L-stick click.

TeleOp does not hold position while firing: release the sticks (motors brake). If the robot is pushed, firing
pauses ("moving") until it is under 6 in/s and 20 deg/s, so re-aim with RB.

### Firing gates

With auto-aim, a ball is fed only when: flywheel at speed, the target cell's opening faces the robot
(off-axis <= `FIRE_MAX_OFF_AXIS_DEG` = 75 deg), the robot's heading is inside the **aim tolerance** (driver RB
aim-assist turns it there), the chassis is still, and no TIP pause (1 s after any target change; a requested
volley is cancelled - press RB again). The aim tolerance is worked out for every shot from the distance
between the robot's centre and the cell (the robot turns about its centre, so a heading error swings the
shot about it) and the 20 in cell opening as seen from the robot: half the heading error at which a POLLEN
would touch the edge of the opening, between 1.5 and 4 deg. Examples: 4 deg (the cap) for every straight-on
shot up to about 54 in and at every AUTO shooting pose; 3.2 deg for a 70 in shot straight on; 2.5 deg for a
92 in shot straight on; 1.8 deg for a 92 in shot 40 deg off-axis. Manual aim fires on flywheel speed only.

**Shot distances** (the AIM line, the shot table, the 30-92 in band) are measured from where the ball leaves
the robot once it is aimed, so they do not change while the robot turns; with the placeholder
`SHOOTER_FORWARD_IN` = -7 they are 7 in shorter than the distance from the robot centre.

The carrier holds the balls while it is stopped. For each ball it spins until the shot is confirmed (a
flywheel speed dip, or the feed beam) and then stops at once, or stops after `CARRIER_FEED_MS` = 0.4 s; the
next ball waits at least 0.15 s, and until the flywheel is back at speed and the aim is good. A feed with no
beam break and no flywheel dip by 0.25 s after the carrier stops is "unconfirmed": it still counts the ball as fired
(`COUNT_UNCONFIRMED_SHOTS`); there is no automatic jam retry - clear jams with operator LB and fix the count
with back / L-stick click. While the intake pulls balls in, the carrier stays stopped (`CARRIER_INTAKE_POWER`
= 0). With a mouth sensor, NECTAR at the mouth is refused (the intake reverses for as long as it is there):
always the opponent's; ours too while `ACCEPT_OWN_NECTAR = false` (POLLEN only until NECTAR passes its tests,
the default). The mouth sensor reacts on contact, so drivers still steer around NECTAR.

### Rumble and lights

| Cue | Who | Rumble |
|---|---|---|
| 4 balls loaded (full) | driver | 2 short blips |
| NECTAR refused at the mouth (mouth sensor) | driver | 1 short buzz |
| flywheel reached speed | operator | 1 light short buzz (again after every shot) |
| operator Y switched the target cell | both | 1 long buzz |
| the CAMERA switched the target cell | both | 2 long buzzes |
| Y confirmed the camera's switch (no change) | operator | 1 strong short buzz |
| a feed was not confirmed (jam? check the count) | operator | 4 blips (on every shot = dip detection not tuned and no feed beam) |
| 1:00 left (FLOWER unlock; NECTAR goes in at 1:00 on the field timer) | both | 3 blips |
| 0:20 left | both | 1 s continuous |

After any target switch shoot from the end the up cell faces (look at the HIVE); if you are already there,
stay. PS4/PS5 light bars: driver RED = full, YELLOW = 1-3 balls, BLUE = empty; operator BLUE = shooting mode
off, **RED = target cell faces away (wrong zone, or the HIVE tipped: press Y)**, PURPLE = not aimed (driver:
hold RB), GREEN = ready to fire, YELLOW = aimed, waiting (flywheel spinning up, robot moving, TIP pause). Use
WIRED pads (R904); Logitech F310 cannot rumble. The 1:00 / 0:20 cues run on our own TeleOp clock: the FIELD
timer and the coach are the authority.

### Telemetry lines to know

`TIME | alliance | BALLS n/4 | READY TO FIRE / spinning up / aiming / shooting mode OFF | MANUAL AIM`;
the AIM line names the zone (e.g. `ZONE B (FAR half)`) and the shot distance, and shows `heading err` and
`tol`, then `ON TARGET` or `TURN +12 deg (driver RB)`; the BALLS line shows `mouth NO SENSOR (count drops per
feed)` without a mouth sensor;
`>> TARGET SWITCHED: GO TO OUR FAR END (stay if you are already there) <<` (5 s after a switch);
`Volley cancelled by the target switch - press RB again`; `!! FEED NOT CONFIRMED (no beam break / flywheel dip):
jam? ...` (3 s after an unconfirmed feed); `!! TARGET CELL FACES AWAY - wrong zone, or the HIVE
tipped: press Y`; `!! IN THE SPILL LANDING BOX - ... back off until the balls land (G409)` (for 6 s after our
last shot while inside the up cell's landing box, and for 6 s after a target switch inside the old cell's box);
`!! distance N in is outside the reliable shot band (30-92 in)`; `! off-axis N deg (> 40): shots start to hit
the cell's side wall`; `Intake refused: FULL (4 balls, G407)` (without a mouth sensor it adds `- no mouth
sensor: after an eject, operator back = -1 per ball ejected`).

## 8. AUTO

### INIT menu (gamepad1)

dpad up/down = line, left/right = value, **A = lock/unlock** (it only stops edits; B does nothing). The
values shown run whether or not they are locked.

| Line | Values | First run after the app starts |
|---|---|---|
| Alliance | RED, BLUE | RED |
| Start | S1-S7 | S1 |
| Routine | A0, A1, A2, A3 role 1, A3 role 2 | A1 |
| Park | INNER, CORNER, SOLO (A3 role 1 always INNER, role 2 always CORNER: the line says so and is ignored) | INNER |
| Delay | 0-15 s (1 s steps) | 0 |
| Preloads | 0-4 | 4 |

Every line is remembered until the Robot Controller app restarts (a battery swap or reboot resets it).
**Every match: set Alliance to THIS match's colour first** (the top line shows `##### RED ALLIANCE #####`),
then check every line. Changing the Routine also moves Start to that routine's home spot (S1; S6 for A3 role
2). The `Place robot at (x, y), heading h = front faces ... wall` line only repeats the selected start - nothing
on the robot can see a misplaced robot: place it with the jig. AUTO INIT moves nothing: the intake, carrier,
wheels and flywheel stay unpowered until START (G304.H).
`CHECK:` lines: invalid start (the robot will not move; the line names the starts to use, e.g.
`!! INVALID START S7 FOR A3_ROLE2: ROBOT WILL NOT MOVE (use S6)`), Park INNER from S5/S7 (crosses the CORNER
slot), Park SOLO, Preloads not 4, 0 preloads, S6 next to the F-far (blue: F-aud) FLOWER, A2/A3 without a
tuned follower. INIT also warns `!! NO ODOMETRY` (paths skipped; from S1, A1 still fires from the start
spot), `!!!! NO MOUTH SENSOR` (right under the selection menu; section 7) and when Foresight is untuned.

### Start spots (red; blue = rotate 180 deg)

Every spot is square to its wall with the robot's **front (intake) against the wall** and its back (shooter)
facing into the field. S1 (61.67, -12.75, heading 0) on our start wall on our HIVE axis, back toward the up
cell; S2 (61.67, -37.5, 0); S3 (45, -61.67, -90); S4 (0, -61.67, -90); S5 (-61.67, -61.67, 180); S6
(-61.67, -10.1, 180) - 1.35 in from the F-far FLOWER, back toward the second cell; S7 (-61.67, -40, 180).
Valid starts: A0 any; A1 / A2 / A3 role 1 S1-S4; A3 role 2 S6 only (from S7 it would have to turn ~31 deg
with its front against the far wall). Any other combination runs a **no-motion** plan.

### Routines (red coordinates; at every shooting pose the robot's back points at the target)

| Routine | What it does |
|---|---|
| A0 Safe | drive to the Park slot (LEAVE 3 + PARK 5) |
| A1 Tip & Park | S1: fire the preloads from the start spot (a 38.2 in shot, on-axis). S2-S4: first drive to the Zone A side spot (55, -37), heading ~-32 deg (a 38.5 in shot, 32 deg off-axis; S3/S4 leave the wall via (x, -48)). Then **hold** at the firing pose until the spill has landed (start-cell tags seen up and then gone 1 s, or 6 s after the last shot), then (52, -40) -> (30, -42) -> (-30, -40) -> Park slot |
| A2 | A1's volley, then at once (no hold) (52, -40) -> (51, -50), turning to drive intake-first -> sweep (60.5, -46) -> (60.5, -60) at 0.3 speed, heading -90, intake on; with a mouth sensor it ends at 4 balls, otherwise 0.6 s after the sweep path -> (30, -42) -> (-30, -40) -> B2 (-48, -12.75, heading 180; a 24.5 in shot, below the 30-92 in band: see below) -> volley 2 -> Park slot |
| A3 role 1 | A2 up to the sweep, then (30, -42) -> corridor end (-30, -40) heading 180, **waits until 17 s** (role 2 parks first), -> lane (-31, -35.5) -> role-1 spot (-52, -30), heading -154.1 deg (a 32.5 in shot) -> volley 2 (the TIP-2 threshold ball) -> leaves at once, turning square while dropping to (-51.5, -36.5), then (-31, -36.5) along the bottom edge of the TIP-2 landing box -> INNER slot |
| A3 role 2 | S6 only: its back already points at the second cell (a 38.3 in shot; ~3.3 deg off its aim, so the volley turns it, 1 in off the wall first); it waits for the partner's TIP 1 (vision: second cell's tags seen up for 0.5 s). Fires its 4 preloads only if the TIP is confirmed by **9 s** after START; waits at most until 10 s; then S6 -> (-50, -10.1) -> CORNER slot. Whatever it is doing, it leaves for the CORNER slot by **13 s** (`A3_ROLE2_LEAVE_BY_SEC`, an A3 gate): a stalled volley (or a start delay) is cut there and the balls left stay inside |

Shot distances are measured from where the ball leaves the robot (what the AIM line shows), with the
placeholder `SHOOTER_FORWARD_IN` = -7: S1 38.2 in, Zone A side spot 38.5 in, B2 24.5 in, role-1 spot 32.5 in,
S6 38.3 in (from the robot centre each is 7 in more). **B2 is the one shooting pose below the 30-92 in band**
(the route test checks it against a 24 in floor). It stays at (-48, -12.75) until the shot map (Exp 2b)
decides it: if the short shot misses, move B2 back toward (-55, -12.75) (a 31.5 in shot) - that spot is only
~1.6 in from the F-far FLOWER and the approach drives toward it, so an overshoot touches it.

Volleys: during every volley the robot turns itself onto the exact aim heading (operator trim and vision bias
included) whenever it is more than half the aim tolerance off (a new aim pose at most every 0.4 s), stepping
off a wall first if it touches one; each ball waits until the heading is inside the aim tolerance. The
carrier makes its first push as soon as the flywheel is at speed and the aim is good. Each push stops when
the shot is confirmed (flywheel dip or feed beam); the next push waits at least 0.15 s, and until the
flywheel is back at speed and the robot is aimed. An unconfirmed feed takes 0.8 s (a 0.4 s push, a 0.25 s
wait, then 0.15 s). A 4-ball volley takes ~3.05 s when every feed is unconfirmed, much less when every shot
is confirmed (feed beam or a tuned dip). Volley 1
assumes TIP 1 after >= 3 balls (only beam-confirmed shots count when a feed beam is fitted) and keeps vision
from undoing that for 5 s. Volley 2 fires only if the tracker says the second cell is up; if not, it waits up
to 1 s for vision, then keeps the balls. After the garden sweep, volley 2 feeds at least 4 times
(`AUTO_VOLLEY2_MIN_FEEDS`: without a mouth sensor nothing is counted in; with one, a garden ball may have
missed it). A volley never starts when the target cell faces away from the robot.

Safety nets: at `AUTO_PARK_DEADLINE_SEC` (23 s after START) it drops everything and parks (the one park deadline;
A3 role 2 also leaves by its 13 s A3 gate, `A3_ROLE2_LEAVE_BY_SEC`). Path timeouts come
from the leg length and the configured speed (length / (0.6 x cruise) + 1.5 s; cruise = 26 in/s at the shipped
0.5 cap, 52 in/s tuned) - a timed-out path stops and the robot holds where it is. A volley gives up 5 s after
the robot has stopped (at most 1.5 s after the step starts). Drive times in the plan assume a tuned follower;
at the 0.5 cap expect ~2x (S1 -> park ~5-6 s, garden -> Zone B ~6.5-8 s). Routes drive straight off walls and
only turn in open space (a turning 18 in robot reaches 12.7 in from its centre); the route test checks every
leg. A3 needs a tuned follower and a rehearsal: role 2 starts its park drive by 13 s and is parked by ~16 s (even
at the 0.5 cap), role 1 leaves the corridor end at 17 s.

## 9. Competition reminders

- **R704.D + Q&A 26: no Wi-Fi streaming/debug services anywhere in the venue (pits and practice field
  too).** Build with `PEDRO_AUTOTUNE = false` (every 12303 OpMode warns in INIT if AutoTune is inside the
  app). Do not add FTC Dashboard / Panels, or remove/disable them before the event. Tune the Limelight over USB.
- **G403**: TeleOp INIT moves nothing; actuators start at START. Never INIT an OpMode that moves
  things during the 8 s AUTO -> TeleOp transition.
- **G304.H / G305**: AUTO INIT moves nothing, so the robot is still from INIT to START. Enable the 30 s
  timer; "12303 TeleOp" is pre-selected after AUTO.
- **G401**: no gamepad input during AUTO (all choices in INIT).
- **G407 / G408**: max 4 balls. **With the mouth sensor**: a ball is counted once it has passed the mouth
  sensor; at 4 the intake stops and a short reverse pulse pushes away a 5th ball at the mouth; the slot-4
  sensor forces "full". With 3 counted, a ball right behind ball 4 counts ball 4 at once (back in view after
  any gap the sensor sees, or still in view after `MOUTH_ONE_BALL_MAX_MS` = two balls touching), so the pulse
  pushes the 5th away, and slot 4 is read every 20 ms. One sensor cannot always split two touching balls:
  `MOUTH_ONE_BALL_MAX_MS` must be MEASURED (section 5 step 2) - too long and a touching 5th ball gets in. If
  BALLS says 4 but slot 4 shows `-` with 3 inside, the limit is too short: operator back = -1, then measure
  again. Refused NECTAR at the mouth (always the opponent's, ours too while `ACCEPT_OWN_NECTAR = false`) is
  pushed out for as long as it is there. **Without the mouth sensor** nothing counts balls coming in or
  refuses NECTAR: the tunnel must hold no more than 4 (section 5 step 1), the drivers stop intaking at 4 by
  eye and steer around NECTAR. There is NO momentary exception - if a 5th ball gets in, eject at once
  (operator LB; without the mouth sensor, then press back once per ball that went out).
- **G409**: after a TIP keep out of the spill landing box until the balls land.
- **G410**: NECTAR into FLOWERS only in the last 60 s.
- **R702**: Limelight must be a 3A. **R705/R711**: name the devices `12303-RC` / `12303-DS`, change the
  Control Hub Wi-Fi password. **R105**: size limits (18 x 24 x 29 in expanded) must be held mechanically.
- Pose / ball count / HIVE state pass from AUTO to TeleOp in memory (`PoseStorage`): do not restart the robot
  between AUTO and TeleOp. Without AUTO data TeleOp assumes the alliance from the last AUTO INIT (RED if none),
  OUR S1 and 4 balls: read the INIT Alliance line and press gamepad1 X/B if it is wrong.

## 10. TODO-MEASURE list (all in `config/RobotConfig.java` unless noted)

Drive directions (4); Pinpoint pod type (Check: Odometry), X/Y pod offsets and directions; `AIM_ASSIST_TURN_KP`,
`AIM_ASSIST_MIN_TURN`; `FLYWHEEL_DIRECTION`, `FLYWHEEL_TICKS_PER_REV` (gear/belt ratio), `FLYWHEEL_MAX_TICKS_PER_SEC`,
ready window, `FLYWHEEL_SHOT_DIP_FRACTION`, `FLYWHEEL_DEFAULT_RPM`; `SHOT_TABLE` (placeholder model rows) and
`SHOT_TABLE_RPM_SCALE`; `SHOOTER_FORWARD_IN`, `SHOOTER_LEFT_IN`, `AIM_TOLERANCE_FRACTION`; `CARRIER_DIRECTION`,
`CARRIER_FEED_POWER`, `CARRIER_FEED_MS`, `CARRIER_INTAKE_POWER`; `INTAKE_DIRECTION`, `INTAKE_IN_POWER`; ball
sensors (when fitted): colour gain, presence distances, hue windows, `MIN_SATURATION`, `BALL_COUNT_DEBOUNCE_MS`,
`MOUTH_ONE_BALL_MAX_MS`; camera height, pitch, forward/left position, yaw offset (180 = looking out of the
back); `AUTO_PARK_DEADLINE_SEC`, `AUTO_SWEEP_SPEED`, `A1_SPILL_HOLD_MAX_SEC` / `A1_SPILL_TAGS_GONE_SEC`,
`A3_ROLE2_LATEST_FIRE_SEC`, `A3_ROLE2_LEAVE_BY_SEC`, `A3_ROLE1_ZONE_B_NOT_BEFORE_SEC`,
`A3_ROLE2_TIP_WAIT_TIMEOUT_SEC`; `FieldConstants.GARDEN_SWEEP_X` / `GARDEN_SWEEP_END_Y`; `pedro/Constants.java`
Foresight block (AutoTune).

## 11. Laptop tests

`./gradlew :TeamCode:testDebugUnitTest` runs, with no robot:
- **RouteSweepTest** - every AUTO leg (both alliances, every routine, valid start and park choice) swept with a
  turning 18 in robot: walls, our side of the field, FLOWERS, HIVE feet and legs, the A3 partner / a partner in
  the CORNER slot, the active spill box; A3 role 2's park drive also from its aimed pose (turned, off the wall);
  the A3 role-1 exit never doubles back through the far-wall escape; every start pose has the front (intake)
  against its wall; every shooting pose has the robot's back on its cell inside the aim tolerance and a shot
  distance (from where the ball leaves) inside the 30-92 in band - B2 is the one exception, checked against
  a 24 in floor; path timeouts and the A3 time budget (role 2 leaves by 13 s and is parked before role 1's
  17 s gate).
- **BallHandlingTest** - the real Carrier + Intake on fake sensors: counting at the mouth, ball 4 drawn in before
  the full pulse, a 5th ball right behind ball 4 (gap 0-120 ms) pushed away instead of pulled in, slot 4 read
  every 20 ms while ball 4 comes in, NECTAR refused for as long as it stays at the mouth (own NECTAR until it is
  accepted); the carrier holding the balls while intaking, one push per ball that stops when the flywheel dip
  or the beam confirms the shot (or after `CARRIER_FEED_MS`), the timed push for Tune: Flywheel (always
  exactly `CARRIER_FEED_MS`), the next ball waiting for the flywheel and the aim, unconfirmed feeds, minimum
  feeds, eject (carrier + intake backward), nothing moving before START, the no-mouth-sensor mode (count from
  the preloads, 4 feeds per volley), and the carrier and the intake as DC motors.
- **AimTest** - the robot's back faces the target, the distance is measured from where the ball leaves once
  the robot is aimed (it does not change while the robot turns), a shooter beside the centre line still hits,
  trim and vision bias, the aim tolerance from the robot-centre distance and the opening's width, the fire
  gate, the aim-assist turn, the AUTO volley's turn onto the aim heading (and off the wall at S6).
- **VisionGeometryTest** - the camera looking out of the back, left in the image = counter-clockwise on the
  field, the vision bias undoing an odometry heading error.
- **AutoTimingTest** - the real AutoRoutine + Robot on an empty fake hardware map: A3 role 2 starts its park drive
  at 13 s even when its volley stalls or a start delay is set; other routines keep the 23 s deadline; A3 role 2
  starts on S6 only (from S7 it does not move, and INIT says `(use S6)`).
- **HiveTrackerTest** - Y confirms a recent camera switch, AUTO's assumption survives the dwell, zone texts.
- **ShooterDipTest** - every real shot dip is reported once; a target change is not a shot.
- **OdometryPodsTest** - which pod a measured counts-per-inch belongs to, and the RobotConfig advice.
Run them after any change to `FieldConstants`, `AutoLegs`, `AutoPaths`, `AutoRoutine`, `Aim`, `Carrier`,
`Intake`, `Vision`, `HiveTracker` or `Shooter`, and after changing a number in `RobotConfig` (several tests
read it).
