# 06 · Software plan

How the code supports the strategy, and the competition rules the code must obey. **For build, deploy, configuration names and buttons, the code README is the final word:**
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`

Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate. Values in `code font` are `config/RobotConfig.java` settings (the defaults in the code).

The robot this code drives ([R8](../research/R8-robot-v2.md)): an intake wheel at the front (DC motor), a tunnel and a carrier wheel that holds the balls and feeds them one at a time, and a fixed shooting wheel at the top of the back. The shooter cannot turn, so **the robot aims by turning its back to the target**.

---

## 1. Stack

| Piece | Choice | Why / source |
|---|---|---|
| IDE + language | Android Studio (Narwhal 3 Feature Drop or newer), **Java** | Required by SDK 12.0 (SDK README) |
| SDK | **FtcRobotController v12.0** (released 2026-09-12) | The official BIOBUZZ SDK. Adds AprilTag "clusters" whose origin is the centre of a CELL opening (SDK README) |
| Path following | **Pedro Pathing 3**: `com.pedropathing:revhub:3.0.1`. AutoTune `com.pedropathing:tuning:1.0.1` is **for home only**. Extra repo: `https://repo.dairy.foundation/releases/` | Fastest route to reliable AUTO paths. Pedro's own artifacts are on Maven Central. 3.0.2 (2026-10-16) fixes controller and heading bugs: upgrade on purpose, then re-run the path tests (Pedro releases) |
| Position (localizer) | **goBILDA Pinpoint + 2 odometry pods** through Pedro's Pinpoint localizer | Mecanum wheels slip, so wheel encoders drift, and Pedro 3 has no drive-encoder localizer anyway (Pedro source) |
| Vision | **Limelight 3A**, AprilTag pipeline, used for **aim correction and HIVE state only** | The BIOBUZZ tags move with the HIVE: "not suitable for absolute Field Localization" (SDK 12.0 README) |
| Structure | Plain Java **subsystems** + **enum state machines**, one `Robot.update()` loop, manual bulk caching | Students can read a `switch(state)` |
| Tests | **JUnit 4.13.2**, test-only, on the laptop | Route, ball-handling, aim, vision-geometry, AUTO-timing, HIVE-tracker, shot-dip and odometry-pod tests ([§9](#9-tests-tuning-and-logging)) |
| Not used | FTC Dashboard / Panels at events; NextFTC v2 | R704.D; NextFTC v2 is still pre-1.0 |

**Jargon, once:**
- **Bulk caching** = reading all of a hub's sensors in one 3–4 ms request per loop instead of one request per sensor (gm0; Brushland docs).
- **LUT** (lookup table) = measured points with straight lines between them.
- **PIDF** = a speed controller with Proportional, Integral, Derivative and Feed-forward terms.
- **State machine** = a variable that says which step a mechanism is on, and rules for moving to the next step.

---

## 2. Architecture

```
  Gamepads (TELEOP)  or  AUTO routine (state machine with deadlines)
                 |  requests: drive, intake, fire, overrides
                 v
  +-------------------------- Robot.update()  (one loop, target 10-20 ms) ---------------------+
  | 1. clear bulk cache on both hubs                                                            |
  | 2. read:   Pinpoint pose (via Pedro follower) | Limelight result | colour/beam sensors      |
  | 3. think:  HiveTracker (which CELL is up) -> aim (heading for the robot's back + distance   |
  |            from the shooter, vision bias) -> flywheel target from the distance->RPM LUT     |
  |            -> carrier state machine (count, feed one ball at a time)                        |
  | 4. write:  drive | flywheel | carrier | intake   (only when a value changes)                |
  | 5. save:   pose + ball count + HIVE state for the AUTO -> TELEOP hand-off; telemetry ~10 Hz |
  +---------------------------------------------------------------------------------------------+
```

| Subsystem | Owns | Key behaviour |
|---|---|---|
| Drivetrain | 4 SWYFT drive motors, Pinpoint (through the Pedro follower) | Field-centric TELEOP drive (no position hold in TELEOP: the motors only brake), driver aim-assist turn, "hold pose" while shooting in AUTO, AUTO path following. Falls back to plain robot-centric drive if Pedro can't start |
| Shooter | Shooting-wheel motor (`flywheel`) | Hub velocity PIDF; "ready" gate; shot detection from speed dips |
| Aim | – (pure math) | The heading that points the robot's back at the target CELL, the shot distance for the shot table (from where the ball leaves the aimed robot), the aim tolerance, the fire gate, the driver aim-assist turn |
| Intake | Intake wheel: DC motor (a continuous-rotation servo also works) | Refuses "in" when full; reverses while a refused NECTAR is at the mouth |
| Carrier | Carrier wheel (DC motor **or** continuous-rotation servo); optional mouth / slot-4 / feed sensors | Ball count, NECTAR refusal decision (needs the mouth sensor), one-ball-at-a-time feeding, unconfirmed-feed events, eject |
| Vision | Limelight 3A, fixed to the chassis, looking out of the back | Own-alliance tags only (the pipeline passes 30–45; the code drops the opponent's); vision bias for the aim heading; tag height → HIVE state opinion |
| **HiveTracker** | – | Which of our CELLS is up; the "tip event" |
| Feedback | Gamepad rumble and light bar | Cues for "4 loaded", "at speed", target switched (operator Y / camera / confirm), NECTAR refused, unconfirmed feed, 1:00, 0:20 |

The folders are `config/` (robot and field constants), `subsystems/`, `util/`, `opmodes/{auto,teleop,tuning}` and `pedro/`, plus the laptop tests in `TeamCode/src/test/`. Exact class names are in the README.

---

## 3. HiveTracker (where to aim)

- **Starts in state A**: red's audience CELL (tags 34–37) is up; blue's far CELL (42–45) is up ([01 §7](01-game-and-scoring.md#7-which-way-the-cells-face-read-this-twice)).
- **What flips it:**
  1. The operator's **Y ("HIVE tipped")**, pressed on every human-player "TIP!". After Y, vision cannot change the state for 5 s (`HIVE_OPERATOR_PRIORITY_MS`); after that, 0.5 s of disagreeing vision can switch it back. If vision keeps flipping it wrongly, set `HIVE_VISION_AUTO_SWITCH = false` and re-deploy. A Y pressed within 3 s of a **camera** switch only confirms that switch instead of flipping back (`HIVE_Y_CONFIRMS_VISION_MS`); press again to really flip.
  2. **Vision**: a CELL's tags seen high (about 49.8 in above the floor = up) or low (about 35.5 in = down) (CAD). Threshold 42.7 in, midway (`TAG_UP_HEIGHT_THRESHOLD_IN`). Vision must disagree with the tracker for 0.5 s of unbroken evidence (`HIVE_VISION_CONFIRM_MS`; one tag is enough, `HIVE_VISION_MIN_TAGS = 1`) before it flips.
  3. **AUTO logic**: after volley 1 (≥ 3 balls) AUTO assumes TIP 1 and keeps vision out for 5 s (`AUTO_TIP_ASSUME_PROTECT_MS`), because the start CELL stays up during the dwell. A1's spill hold records "no TIP" (state A) if the start CELL is still seen up at its end. AUTO does not record TIP 2 of an A3 double tip.
- **In TELEOP the operator's Y on the "TIP!" call is the main signal.** After a tip all 8 of our tags face the other half (01 §7), so vision cannot see our own tip from the zone we fired from. It confirms a new state only once the robot's back (the camera) faces the HIVE from the zone the new up CELL faces.
- **Every flip is a "tip event":**
  - pause launching for 1 s (`TIP_PAUSE_MS`) and cancel a requested volley while the robot turns and the flywheel retargets (press RB again). The flip usually comes after the swing (operator Y on "TIP!"), so this pause does **not** protect a tipping HIVE: the drivers do that by firing each load as one volley and starting no new volley after "8, it's going" ([03](03-teleop.md#the-two-zone-loop), M p.87);
  - rumble both gamepads: 1 long buzz for operator Y, 2 long buzzes for a camera switch (a Y that only confirms gives the operator 1 strong short buzz);
  - switch to the other aim point;
  - show `>> TARGET SWITCHED: GO TO OUR AUDIENCE END` (or `FAR END`) `(stay if you are already there) <<` for 5 s, correct for our colour. The AIM telemetry line always names the zone, e.g. "ZONE B (FAR half)".
- Don't rely on the SDK's "|roll| < 90 = scorable" test alone. It really tells you which side you are looking from, not which CELL is up (our research team's check of the SDK). Use tag height.
- The partner can tip our HIVE too, so our own ball count can't be the only signal.

## 4. Fused aim

The shooter is fixed and points out of the robot's back (`SHOOTER_FACING_DEG = 180`), so **aiming means turning the robot** (`subsystems/Aim`, pure math, checked by `AimTest`).
- **Aim heading** = bearing from the robot to the active aim point − 180° − a small correction when the shooter sits beside the centre line (asin(sideways offset ÷ distance); 0 while `SHOOTER_LEFT_IN = 0`) + operator trim + vision bias. **Heading error** = aim heading − robot heading (+ = turn counter-clockwise). Aim points (CAD; SDK cluster origin):
  - red audience CELL (16.50, -12.74, 58.27);
  - red far CELL (-16.48, -12.74, 58.31);
  - blue = rotated 180°.
- **Who turns the robot:** in TELEOP the driver holds RB (aim-assist): turn power = `AIM_ASSIST_MIN_TURN` (0.08) + `AIM_ASSIST_TURN_KP` (0.015) × error, at most `AIM_ASSIST_MAX_TURN` (0.45), and nothing inside half of the aim tolerance, so the robot settles instead of hunting. In AUTO the shooting poses already face the target, and during a volley the robot turns itself onto the aim heading whenever the error is more than half the tolerance (a new hold pose at most every 0.4 s, `AutoLegs.volleyAimHold`; stepping off a wall first). Operator d-pad left/right = aim trim ±0.5° (`AIM_TRIM_STEP_DEG`; left = the ball goes further left, seen from the shooter looking toward the cell: the robot turns counter-clockwise).
- **Shot distance** (the shot-table key, the 30–92 in band and the number on the AIM telemetry line) is measured from where the ball leaves the robot **once it is aimed** (the shooter position at the aim heading), not from the robot centre, so it does not change while the robot turns. `SHOOTER_FORWARD_IN` = -7 and `SHOOTER_LEFT_IN` = 0 are placeholders: measure them before the shot map ([07 Exp 2a](07-testing-and-practice.md#2a-exit-angle-exit-point-and-speed-ratio-k-do-this-first)). With the placeholder, every shot distance is 7 in shorter than the distance from the robot centre.
- **Vision bias:** the camera is fixed to the chassis and looks out of the back (`CAMERA_YAW_OFFSET_DEG = 180`), so it sees the target CELL's tags while the robot aims. Compare the bearing the camera measures to each visible own tag with the bearing odometry predicts (tag positions from CAD). Low-pass the difference (gain 0.15, at most ±8°) and add it to the aim heading: it also corrects odometry heading error. Treat a jump over 6° as an outlier (partner robot, wrong CELL) unless 5 such frames agree within 2°. Let it decay slowly when no tags are seen. Update it **only while the chassis turns < 15°/s and moves < 6 in/s**, so camera latency doesn't matter.
- **Aim tolerance** (worked out for every shot) = `AIM_TOLERANCE_FRACTION` (0.5) × atan((10 in × cos(off-axis) − 1.4 in) ÷ distance from the **robot centre**), kept between 1.5° and 4° (`AIM_TOLERANCE_MIN_DEG`, `AIM_TOLERANCE_MAX_DEG`). The 10 in is half the 20 in opening (`FieldConstants.CELL_OPENING_WIDTH_IN`) and 1.4 in is the POLLEN radius, so the atan is the heading error at which a ball would just touch the edge of the opening. The robot turns about its centre, so a heading error swings the shot about the centre: the centre distance is the lever arm. Half of the edge angle leaves room for odometry and vision error. Examples (shot distances from where the ball leaves; the robot centre is 7 in further away): 4.0° (the cap) for every straight-on shot up to about 54 in and at every AUTO shooting pose; 3.2° for a 70 in shot straight on; 2.5° for a 92 in shot straight on; 1.8° for a 92 in shot 40° off-axis.
- **Feed only when** (auto-aim) the flywheel is ready **and** the target opening faces the robot within 75° (`FIRE_MAX_OFF_AXIS_DEG`) **and** the heading error is inside the aim tolerance **and** the chassis is still (< 6 in/s, < 20°/s) **and** no 1 s TIP pause is running. Manual aim fires on flywheel speed only. Without odometry the aim can't be checked: the flywheel (at `FLYWHEEL_DEFAULT_RPM`) and released drive sticks gate the shot.
- **Hints, not gates:** telemetry warns above 40° off-axis and outside the 30–92 in shot band (the shot distance to the opening centre: R3's 27–90 in lip band + 2.8 in, a model of a 72 mm wheel at ~65°: re-check it once our exit angle is measured). The shot table covers 33–87 in; below 33 in it gives its 33 in value.
- **AUTO shooting poses** (shot distances with the placeholder exit point): S1 38.2 in, the Zone A side spot 38.5 in, the A3 role-1 spot 32.5 in, S6 38.3 in. **B2** (A2's second volley, (-48, -12.75)) is a 24.5 in shot: the one AUTO shooting pose below the band (the route test checks it against a 24 in floor) until the shot map decides it ([07 Exp 2b](07-testing-and-practice.md#2b-shot-map-builds-the-distance--rpm-table)). **A3 role 2 starts on S6 only**: there it is 3.3° off its aim, inside the 4° tolerance but more than half of it, so its volley turns it onto the aim, 1 in off the far wall first. From S7 it would have to turn about 31° with its front against the far wall, so S7 is an invalid start for it: no-motion plan, and INIT shows `CHECK: !! INVALID START S7 FOR A3_ROLE2: ROBOT WILL NOT MOVE (use S6)`.

## 5. Flywheel control and the distance → RPM table

- **Hub velocity PIDF first:** F = 32767 ÷ max ticks/s, P = 0.1 F, I = 0.1 P, D = 0 (REV PIDF guide). Set them in every OpMode; they don't survive a power cycle (ftc-docs).
- **"Ready" = within ±max(2.5%, 60 rpm) of target for 3 loops.** The motor has 28 counts per turn, and the hub's speed reading is a 5-sample average refreshed every 10 ms, so readings move in steps of about 1–1.5% (gm0). A tighter gate is just noise.
- **Encoder scale:** `FLYWHEEL_TICKS_PER_REV` = 28 counts per motor turn × motor turns per wheel turn (1 if the wheel sits on the motor shaft; the gear or belt ratio otherwise: measure it). `FLYWHEEL_MAX_RPM` 5000 is about 83% of the 6000 rpm free speed; R3's recovery guideline is about 4300 rpm.
- **Table key** = the shot distance from [§4](#4-fused-aim): horizontal distance from where the ball leaves the aimed robot to the aim point, in inches. Seed rows come from the ball-flight model for a 72 mm wheel at ~65° ([07 Exp 2](07-testing-and-practice.md#exp-2--shot-map-and-exit-angle)); our wheel and its fixed exit angle are not measured yet. **Replace every seed with shot-map results.**
- **Rescaling the model seeds after measuring the speed ratio k** (ball exit speed ÷ wheel surface speed; the model assumed 0.40): **multiply every RPM by 0.40 ÷ k_measured** (`SHOT_TABLE_RPM_SCALE`). A lower k needs **more** RPM.
- **Shot detection:** any drop below the target by more than about 5% (`FLYWHEEL_SHOT_DIP_FRACTION`) flags exactly one shot per dip (ignored for 100 ms after a target change), or the feed break-beam. It stops the carrier at once, and the count goes down by one.
- **Flywheel never brakes or reverses** (FLOAT). It coasts through the 8 s transition (G403).
- **Upgrade path, if recovery between shots is too slow:** our own loop, power = (kS + kV·target) × 12 V ÷ battery V + kP·error (our software research).

## 6. Carrier: counting to 4 and feeding (possession discipline)

`subsystems/Carrier` owns the ball count, the optional ball sensors and the carrier wheel. Every ball sensor is optional: a missing one only switches its feature off.

- The count starts at the INIT **Preloads** value (4 in every match); the slot-4 sensor (when fitted) can only raise it to 4. TELEOP gets the count from the AUTO hand-off (4 without AUTO data).
- **With the mouth sensor:** a ball is **counted once it has passed the mouth sensor** inward (`BALL_COUNT_DEBOUNCE_MS` = 60). The intake **refuses "in" at 4**. The short "full" reverse pulse (`INTAKE_FULL_REVERSE_MS` = 150) pushes away a 5th ball at the mouth. With 3 counted, a ball right behind ball 4 counts ball 4 at once (back in view after any gap the sensor sees, or still in view after `MOUTH_ONE_BALL_MAX_MS` = two balls touching), so the pulse hits the 5th ball; slot 4 is read every 20 ms while ball 4 comes in. One sensor cannot always split two touching balls, so `MOUTH_ONE_BALL_MAX_MS` must be measured (Check: Sensors: longest single-ball time in view + 30%; default 300 ms). The **slot-4 sensor is the safety net**: if it sees a ball, we are full, whatever the count says.
- **Mouth colour sensor:** a refused NECTAR makes the intake wheel reverse **before** the ball is drawn in, for as long as the ball stays at the mouth and then 400 ms more (`INTAKE_REJECT_MS`), and only while the drivers or AUTO want the intake to pull. `ACCEPT_OWN_NECTAR = false` (the phase-1 default: NECTAR through the tunnel, carrier and shooting wheel is untested) refuses **all** NECTAR; `true` refuses only the opponent's. The sensor reacts on contact, so drivers still steer around NECTAR.
- **Without the mouth sensor** the robot cannot count balls coming in or refuse NECTAR. INIT shows `!!!! NO MOUTH SENSOR ('mouthColor'): balls coming in are NOT counted and NECTAR is NOT refused (G407/G408) !!!!` and two `!!` lines on what that means: in TeleOp at the top, in AUTO right under the selection menu (the `##### RED ALLIANCE #####` banner stays the top line). A `WARN: no mouth sensor …` line also appears with the other WARN lines at the bottom. The count starts at the preloads and drops by one per feed, confirmed or not (`COUNT_UNCONFIRMED_SHOTS = true`). The intake refuses only while the count says 4, so after a volley RT never stops by itself: RT stops at 4 only with the mouth or slot-4 sensor. A plain volley (TELEOP RB) feeds at least 4 times, because the code cannot know how many balls came in; empty feeds are harmless. The drivers stop intaking at 4 by eye and steer around NECTAR; the operator fixes the count with back (−1) / left-stick click (+1). An eject (operator LB) does not lower the count: after one, the operator presses back once per ball that went out, or the intake keeps refusing (TeleOp shows `Intake refused: FULL (4 balls, G407) - no mouth sensor: after an eject, operator back = -1 per ball ejected`). The tunnel must hold no more than 4. The BALLS telemetry line says `mouth NO SENSOR (count drops per feed)`.
- **The carrier holds the balls** while it is stopped. While the intake pulls balls in, it stays stopped (`CARRIER_INTAKE_POWER` = 0).
- **Feed sequence for one ball:**
  1. Wait until ready ([§4](#4-fused-aim)): flywheel at speed, aim good, chassis still, no TIP pause, and at least `MIN_SHOT_INTERVAL_MS` (150 ms) since the last ball.
  2. Spin the carrier at `CARRIER_FEED_POWER` until the shot is confirmed (a flywheel speed dip, or the feed beam): then it stops at once. Otherwise it stops after `CARRIER_FEED_MS` (400 ms: long enough for one ball, too short for two).
  3. A push that ended without a confirmation waits up to `SHOT_CONFIRM_TIMEOUT_MS` (250 ms) for a late one.
  4. Count − 1.
  An unconfirmed feed takes 0.8 s (0.4 + 0.25 + 0.15); a 4-ball volley with no confirmations ≈ 3.05 s. A confirmed shot is faster: the push stops at the dip, and the next push waits at least 0.15 s and until the flywheel is back at speed and the aim is good (the longer of the two, not their sum). The first push starts as soon as the flywheel is at speed and the aim is good. **Tune: Flywheel** also has a **timed push** (RB) that always runs exactly `CARRIER_FEED_MS`, for setting it ([07 Exp 0b](07-testing-and-practice.md#0b-carrier-feed-timing)).
- **Jam:** if neither the beam nor a speed dip confirms a shot, the ball is counted as fired anyway (`COUNT_UNCONFIRMED_SHOTS = true`) and feeding continues. **No reverse, no retry.** The operator gets 4 blips and TELEOP shows `!! FEED NOT CONFIRMED …` for 3 s; the FEED telemetry line shows the carrier power and counts confirmed and unconfirmed shots. Clear jams with operator LB (eject: carrier and intake backward, `CARRIER_EJECT_POWER` = -1) and fix the count with back (−1) / left-stick click (+1). Blips on every shot mean the dip detection is untuned and there is no feed beam. With `COUNT_UNCONFIRMED_SHOTS = false` the count only drops on confirmed shots, and a volley stops after 2 unconfirmed feeds in a row.
- Rules behind this: G407/G408, no momentary exception (QA3, QA4, QA23), TU05 sorter boxes. QA29 (active rejection designs) is pending ([03](03-teleop.md#possession-discipline-design-requirement-and-driving-rule)).

## 7. AUTO and TELEOP structure

**AUTO**
- Selector in INIT: alliance, start (S1–S7), routine (A0, A1, A2, A3 role 1, A3 role 2), park (INNER / CORNER / SOLO), delay 0–15 s, preloads 0–4; A toggles the lock; the first run after an app start is RED · S1 · A1 · INNER · 0 s · 4, then the last choice is remembered until the app restarts ([02](02-autonomous.md#init-selector-what-the-drive-team-sets)).
- `@Autonomous(preselectTeleOp = …)` queues TELEOP automatically (G305 box; SDK RobotCore).
- Poses are written once for red and rotated 180° for blue. **Never mirror.**
- Each routine is a state machine with **one park deadline** (23 s after START, `AUTO_PARK_DEADLINE_SEC`; A3 role 2 also leaves by its 13 s gate, `A3_ROLE2_LEAVE_BY_SEC`), per-leg path timeouts (length ÷ (0.6 × cruise) + 1.5 s) and a volley timeout (5 s after the robot has stopped). Every drive is a leg in `AutoLegs`, which the route test sweeps.
- Save pose, ball count, alliance and HIVE state **every loop**: the DS can stop AUTO at any moment. The SDK `blackboard` is not saved across power cycles (SDK sample `ConceptBlackboard`). AUTO INIT also writes the alliance to `/sdcard/FIRST/12303_alliance.txt`, so TELEOP starts on the right colour after an app restart.
- AUTO INIT moves nothing: the intake, carrier, wheels and flywheel stay unpowered until START (G304.H).
- Start poses put the robot's front against the wall and shooting poses put its back toward the CELL. During every volley, whenever the heading is more than half the aim tolerance off, the robot gets a new hold pose on the exact aim heading (trim and vision bias included; at most every 0.4 s), stepped off a wall first. After the garden sweep, volley 2 feeds at least 4 times (`AUTO_VOLLEY2_MIN_FEEDS`).
- Valid starts: A0 any; A1, A2 and A3 role 1 S1–S4; A3 role 2 S6 only. Any other pair runs a no-motion plan, and INIT shows a `CHECK: !! INVALID START …` line that names the starts to use (`(use S1-S4)` or `(use S6)`).
- Coordinates: the 12303 code runs Pedro in the FTC field frame. Pedro's own website uses a corner origin; convert with x_pedro = Y + 72, y_pedro = 72 − X, heading_pedro = heading − 90° (Pedro coordinates docs). Only needed for drawing paths on the visualizer.

**TELEOP**
- INIT: **nothing moves** (G403). Read the hand-off (or, without AUTO data, the saved alliance, OUR S1 and 4 balls); show the competition-build warning line, the `!!!! NO MOUTH SENSOR` lines at the top when there is no mouth sensor, and the Alliance line.
- START: arm every subsystem (first actuator commands happen here).
- Loop: field-centric drive, intake requests, volley, aim, overrides, cues. The match timer only drives reminders (1:00 and 0:20 cues); **the field timer is the authority** (M p.79).
- **Fallbacks:**
  - Pedro fails → robot-centric drive;
  - no Limelight → odometry-only aim;
  - no odometry → the aim can't be checked: the flywheel (at `FLYWHEEL_DEFAULT_RPM`) and released drive sticks gate the shot;
  - no mouth sensor → the count runs from the preloads ([§6](#6-carrier-counting-to-4-and-feeding-possession-discipline));
  - aim wrong (odometry or vision off) → operator right-stick click = manual aim: the driver points the robot's back at the CELL by eye; RB / X fire on flywheel speed only.

---

## 8. Competition rules for software (checklist)

| Rule | What it means for us |
|---|---|
| **R704.D + QA26** | No Wi-Fi streaming **anywhere in the venue**, pits and practice field included: no FTC Dashboard, no Panels, no Limelight web UI over Wi-Fi. **Pedro AutoTune starts a web server (ports 10158 and 12649) whenever it is in the app, with no off switch.** Before every event: set `PEDRO_AUTOTUNE = false` in `build.dependencies.gradle` (and don't build with `-PpedroAutoTune=true`), rebuild, deploy over USB. **Every 12303 OpMode** (12303 Auto, 12303 TeleOp and the six Tune / Check / Test OpModes) prints a warning in INIT if AutoTune, Dashboard or Panels is still inside |
| R704.C | Laptops off the robot network during matches |
| R705 / R711 | Device names `12303-RC` / `12303-DS`; change the Control Hub Wi-Fi password; Bluetooth off |
| E301 | No personal hotspots at the venue |
| G304.H, R102 | Motionless after INIT; no flywheel pre-spin; AUTO INIT moves nothing |
| G305 | Select and INIT an OpMode; AUTO needs the 30 s timer enabled |
| G401 | No driver input during AUTO: every choice is made in INIT |
| G402 | AUTO paths stay on our side (red Y ≤ 0, blue Y ≥ 0); the route test checks every leg |
| **G403** | TELEOP INIT moves nothing; actuators wake at START; **press START only when TELEOP begins** |
| G404 | Nothing powered after 0:00; press stop at the buzzer |
| G407 / G408 | Count interlock at 4; refuse NECTAR at the mouth (the opponent's always; ours while `ACCEPT_OWN_NECTAR = false`). Both need the mouth sensor: without it INIT warns loudly, the tunnel holds no more than 4, and the drivers stop at 4 by eye and steer around NECTAR |
| G409 | TELEOP warns `!! IN THE SPILL LANDING BOX …` for 6 s after our last shot inside the up CELL's landing box, and for 6 s after a target switch inside the old CELL's box. Optional idea: block the intake briefly then |
| G410 | Any "NECTAR into FLOWER" macro stays locked until TELEOP has run ≥ 61 s (1:00 left + 1 s margin) |
| G417.D | Aim at our own tags only (red 30–37, blue 38–45). Never target the opponent HIVE |
| R105 | Size limits must be **mechanical**: software limits don't count for inspection. Check the floating intake arm through its whole swing |
| T405 (M p.151) | In the optional field-calibration window robots may run OpModes but may not drive or launch. Tune the Limelight over a **USB cable** to a laptop, not Wi-Fi |

**Limelight specifics:**
- USB-C → USB-A cable into the Control Hub **USB 3.0** port (ftc-docs hub tips).
- **One** AprilTag pipeline (index 0) for both alliances: 640 × 480 (50 fps), **ID filter = 30–45** (both HIVEs; the code keeps only our 8), marker 3.25 in (82.55 mm), **Full 3D on** (Limelight docs). Check: Limelight lists the other alliance's tags as "Ignored": that is normal.
- `getLatestResult()` is never null in SDK 12; check `isValid()` and staleness.
- Export the pipeline before LimelightOS updates (updates erase them).

**SDK specifics:**
- There is no SWYFT motor type, so use the ticks-per-second velocity calls, not the angle-unit ones (our check of the SDK 12.0 motor types).
- `carrier` and `intake` are read as `DcMotorSimple`, so each can be configured as a Motor or a Continuous Rotation Servo without a code change. As a motor, the carrier brakes when stopped (so it holds the balls) and the intake floats.

---

## 9. Tests, tuning and logging

- **Tuning OpModes** (details in the README): Pedro AutoTune (home only), **Tune: Flywheel** (max speed, PIDF and the shot dip; B = one carrier feed that stops at the dip; RB = a **timed push** of exactly `CARRIER_FEED_MS` that ignores the dip, for Exp 0b; LB = a 4-feed volley, for Exp 3; Y picks what dpad up/down changes: RPM, P/F, I/F or `FEED_MS`), **Tune: Shot Map Logger** (CSV; RT = aim-assist), **Check: Limelight** (nothing moves), **Check: Odometry** (pod type, wiring and directions from a 48 in push), **Check: Sensors** (directions of the wheels, intake, carrier and flywheel; the ball sensors when fitted), **Test: Tip Counter** (RT = aim-assist). They feed the experiments in [07](07-testing-and-practice.md).
- **Tuning order** (`README-12303.md` §5, in the order of the [07](07-testing-and-practice.md) experiments): 1. tunnel capacity (Exp 0a: by hand, carrier stopped, then with the intake; a stop if a 5th ball fits); 2. Check: Sensors (directions of the wheels, intake, carrier and flywheel; the ball sensors when fitted); 3. Check: Odometry (pods); 4. Tune: Flywheel (max speed, PIDF, shot dip; `CARRIER_FEED_MS` with the RB timed push = Exp 0b); 5. Test: Tip Counter (Exp 1); 6. exit point, exit angle and k (Exp 2a: tape measure, then 240 fps video with Tune: Flywheel); 7. Check: Limelight (camera on the back); 8. Pedro AutoTune at home, then the aim-assist gains (`AIM_ASSIST_MIN_TURN`, `AIM_ASSIST_TURN_KP`); 9. Tune: Shot Map Logger (Exp 2b, then the 2c bounce-out check); 10. flywheel recovery (Exp 3: Tune: Flywheel LB volley); 11. the AUTO ladder (Exp 7). Steps 1–6 are week 1 and steps 7–10 week 2; A0 and A1 start in week 2.
- **Logging:** CSV files on the Control Hub (`/sdcard/FIRST/data/`) and DS telemetry. Both are allowed because nothing is streamed (R704.D).
- **Laptop tests:** JUnit 4.13.2 (test-only) under `TeamCode/src/test/`, run with `./gradlew :TeamCode:testDebugUnitTest` (69 tests in 8 classes, fake devices, a manual clock `util/Clock`):
  - **RouteSweepTest** (9): every AUTO leg for both alliances, every routine, valid start and park choice, swept with a turning 18 in robot (walls, our side, FLOWERS, HIVE feet and legs, the A3 partner, a corner-slot robot, the active spill box); every start pose has the front against its wall; every shooting pose has the robot's back on its CELL inside the aim tolerance, with its shot distance (from where the ball leaves) inside the 30–92 in band (B2 is the one exception, checked against a 24 in floor); A3 role 2's park drive also from its aimed pose; path timeouts and the A3 time budget (role 2's 4th push ends by 9 + 1 + 3 × 0.8 + 0.4 = 12.8 s, before its 13 s gate);
  - **BallHandlingTest** (28): the real Carrier + Intake on fake sensors (counting, ball 4 drawn in before the full pulse, a 5th ball right behind ball 4 (gap 0-120 ms) pushed away, slot 4 read every 20 ms while ball 4 comes in, NECTAR refusal, the carrier holding the balls while intaking, one push per ball stopped by the dip or the beam (or after `CARRIER_FEED_MS`), the timed push for Tune: Flywheel (always exactly `CARRIER_FEED_MS`), the next ball waiting for the flywheel and the aim, unconfirmed feeds, minimum feeds, eject, nothing moving before START, the no-mouth-sensor mode, the carrier and the intake as DC motors);
  - **AimTest** (10): the robot's back faces the target, the shot distance is measured from where the ball leaves once the robot is aimed (the same at any heading), a shooter beside the centre line, trim and vision bias, the aim tolerance (from the robot-centre distance and the opening's width), the fire gate, the aim-assist turn, the AUTO volley's turn onto the aim heading (straight off the far wall at S6);
  - **VisionGeometryTest** (3): the camera looking out of the back, left in the image = counter-clockwise on the field, the vision bias undoing an odometry heading error;
  - **AutoTimingTest** (5): the real AutoRoutine on fake hardware: A3 role 2 leaves by 13 s even when its volley stalls or a start delay is set; other routines keep the 23 s deadline; A3 role 2 starts on S6 only (from S7 nothing moves, and INIT says `(use S6)`);
  - **HiveTrackerTest** (6): Y confirms a recent camera switch, AUTO's assumption survives the dwell, zone texts;
  - **ShooterDipTest** (3): every real shot dip is reported once; a target change is not a shot;
  - **OdometryPodsTest** (5): which pod a measured counts-per-inch belongs to, and the RobotConfig advice.
  Run them after any change to `FieldConstants`, `AutoLegs`, `AutoPaths`, `AutoRoutine`, `Aim`, `Carrier`, `Intake`, `Vision`, `HiveTracker` or `Shooter`, and after changing a number in `RobotConfig` (several tests read it). The pure math (`MathUtil`, `InterpLUT`, `AutoLegs`, `AutoPaths`, `Aim`) is easy to test the same way.
- **Software gates:**

| Gate | Target | Week |
|---|---|---|
| Odometry | 48 in push reads 48 ± 0.5 in; 10 spins read 3600 ± 2° | W1–W2 |
| Paths | End error < 1 in | W2 |
| Flywheel | Ready < 1.0 s from idle | W1–W2 |
| Vision | Bearing within 1° of a tape-measured line; distance within ±2 in | W2 |
| Aim-assist | Driver RB turns the robot's back onto the CELL and settles inside the aim tolerance in < 1 s, without stopping short or hunting | W2 |
| Carrier | Exactly one ball per push, 20/20 (Exp 0b) | W1 |
| Loop time | < 20 ms with every sensor on | W3 |
| Laptop tests | All pass before every deploy that changes AUTO or ball handling | every week |

## 10. Software week by week (matches [00](00-master-plan.md#six-week-timeline))

| Week | Deliverable |
|---|---|
| W1 | Build and deploy; Check: Odometry names the pods; Check: Sensors passes; Pinpoint configured; flywheel PIDF + ready gate + shot-dip detection; carrier feed timing (`CARRIER_FEED_MS`, Exp 0b with the Tune: Flywheel timed push); count-to-4 interlock (needs the mouth sensor) |
| W2 | Pedro tuning at home (AutoTune, then compile it out again); aim-assist gains (`AIM_ASSIST_MIN_TURN`, `AIM_ASSIST_TURN_KP`); Limelight pipeline + vision bias (camera on the back); the shot table from the Shot Map Logger (Exp 2b); **A0 and A1 reliable** |
| W3 | TELEOP automation: auto-aim, one-button volley, auto-stop at 4, cues, HiveTracker overrides |
| W4 | A2 / A3 role 1 / A3 role 2 routines (12303 Auto), only with a tuned follower; HiveTracker vision auto-switch tuned on our HIVE |
| W5 | Log review after match sims; competition build checklist |
| W6 | **Code freeze**: bug fixes only, every change re-tested on the robot and with the laptop tests |

---

## Sources

- **SDK v12.0 README**: https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md · samples: https://github.com/FIRST-Tech-Challenge/FtcRobotController
- **Pedro Pathing**:
  - releases: https://github.com/Pedro-Pathing/PedroPathing/releases
  - quickstart: https://github.com/Pedro-Pathing/Quickstart
  - coordinates: https://pedropathing.com/docs/pathing/reference/coordinates
- **Manual TU05** (G304–G305, G401–G410, G417, R102, R105, R704–R711, T405, p.79, p.87): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA3, QA4, QA12, QA23, QA26; pending QA29. https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **REV PIDF guide**: https://docs.revrobotics.com/first-global/fgc-pid · **ftc-docs PIDF**: https://ftc-docs.firstinspires.org/programming_resources/shared/pidf_coefficients/pidf-coefficients.html
- **gm0**:
  - motors and velocity: https://gm0.org/en/latest/docs/software/adv-control-system/sdk-motors.html
  - bulk reads: https://gm0.org/en/latest/docs/software/tutorials/bulk-reads.html
- **Limelight**:
  - 3A: https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a
  - FTC programming: https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming
- **ftc-docs**:
  - hub tips: https://ftc-docs.firstinspires.org/tech_tips/tech-tips/tech-tip-hub-tips/tech-tip-hub-tips.html
  - AprilTag clusters: https://ftc-docs.firstinspires.org/apriltag-clusters
- **CAD** (aim points, tag heights): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **Code**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` and `TeamCode/src/test/java/org/firstinspires/ftc/teamcode/`; summary in `README-12303.md`
- **Robot as built**: [R8-robot-v2.md](../research/R8-robot-v2.md)
- Our research team's fact-check of the SDK 12.0 binaries and the Pedro AutoTune source (2026-10-20).
