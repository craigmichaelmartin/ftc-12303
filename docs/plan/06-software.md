# 06 · Software plan

How the code supports the strategy, and the competition rules the code must obey. **For build, deploy, configuration names and buttons, the code README is the final word:**
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`

Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

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
  | 3. think:  HiveTracker (which CELL is up) -> aim (odometry bearing + vision bias)           |
  |            -> flywheel target from the distance->RPM LUT -> indexer state machine (count)  |
  | 4. write:  drive | turret | flywheel | intake | feeder gate   (servos only when changed)     |
  | 5. save:   pose + ball count + HIVE state for the AUTO -> TELEOP hand-off; telemetry ~10 Hz |
  +---------------------------------------------------------------------------------------------+
```

| Subsystem | Owns | Key behaviour |
|---|---|---|
| Drivetrain | 4 SWYFT drive motors, Pinpoint (through the Pedro follower) | Field-centric TELEOP drive, "hold pose" while shooting, AUTO path following. Falls back to plain robot-centric drive if Pedro can't start |
| Shooter | Flywheel motor (+ hood servo) | Hub velocity PIDF; "ready" gate; shot detection from speed dips |
| Turret | Turret servo (+ optional Through Bore Encoder) | Converts an angle to a servo position; soft limits; tells the chassis to turn when the target is out of range |
| Intake | Roller servo | Refuses "in" when full; reverses on opponent NECTAR at the mouth |
| Indexer | Spinners, feeder gate, mouth / slot-4 / feed sensors | Ball count, one-ball-at-a-time feeding |
| Vision | Limelight 3A | Own-alliance tags only; vision bias for aim; tag height → HIVE state opinion |
| **HiveTracker** | – | Which of our CELLS is up; the "tip event" |
| Feedback | Gamepad rumble and light bar | Cues for "4 loaded", "at speed", tip, 1:00, 0:20 |

In the code as of 10/20 the folders are `config/` (robot and field constants), `subsystems/`, `util/`, `opmodes/{auto,teleop,tuning}` and `pedro/`. Exact class names are in the README.

---

## 3. HiveTracker (where to aim)

- **Starts in state A**: red's audience CELL (tags 34–37) is up; blue's far CELL (42–45) is up ([01 §7](01-game-and-scoring.md#7-which-way-the-cells-face-read-this-twice)).
- **What flips it:**
  1. The operator's **"HIVE tipped"** button. This always wins.
  2. **Vision**: a CELL's tags seen high (about 49.8 in above the floor = up) or low (about 35.5 in = down) (CAD). Threshold about 44 in; vision must disagree with the tracker for a confirm time before it flips.
  3. AUTO logic.
- **Every flip is a "tip event":**
  - pause launching about 1 s or more, because launching at a moving HIVE can stop the tip (M p.87, QA12);
  - rumble both gamepads;
  - switch to the other aim point;
  - show "GO FAR SIDE" or "GO AUDIENCE SIDE".
- Don't rely on the SDK's "|roll| < 90 = scorable" test alone. It really tells you which side you are looking from, not which CELL is up (our research team's check of the SDK). Use tag height.
- The partner can tip our HIVE too, so our own ball count can't be the only signal.

## 4. Fused aim

- **Odometry bearing:** turret angle = bearing from the robot to the active aim point − robot heading − turret-centre offset. Aim points (CAD; SDK cluster origin):
  - red audience CELL (16.50, -12.74, 58.27);
  - red far CELL (-16.48, -12.74, 58.31);
  - blue = rotated 180°.
- **Vision bias:** compare the bearing the camera measures to each visible own tag with the bearing odometry predicts (tag positions from CAD). Low-pass the difference (gain about 0.15). Reject jumps over about 8° (partner robot, wrong CELL). Let it decay slowly when no tags are seen. Update it **only while the turret and chassis are still**, so camera latency doesn't matter.
- **Out of turret range** (as built, about ±28° from centre, est.): the driver gets an aim-assist turn, and AUTO picks poses that face the target.
- **Feed only when** the flywheel is ready **and** the turret is within about 2° **and** the chassis is nearly still. The opening allows about ±8.6 in sideways for POLLEN: about ±4° at 10 ft and ±8° at 5 ft (est.), so 1–2° of aim error is fine.

## 5. Flywheel control and the distance → RPM table

- **Hub velocity PIDF first:** F = 32767 ÷ max ticks/s, P = 0.1 F, I = 0.1 P, D = 0 (REV PIDF guide). Set them in every OpMode; they don't survive a power cycle (ftc-docs).
- **"Ready" = within ±max(2.5%, 60 rpm) of target for 3 loops.** The motor has 28 counts per turn, and the hub's speed reading is a 5-sample average refreshed every 10 ms, so readings move in steps of about 1–1.5% (gm0). A tighter gate is just noise.
- **Table key** = horizontal distance from the turret axis to the aim point, in inches. Seed rows come from the ball-flight model ([07 Exp 2](07-testing-and-practice.md#exp-2--shot-map-and-hood-angle)). **Replace every seed with shot-map results.**
- **Rescaling the model seeds after measuring the speed ratio k** (ball exit speed ÷ wheel surface speed; the model assumed 0.40): **multiply every RPM by 0.40 ÷ k_measured.** A lower k needs **more** RPM.
- **Shot detection:** a speed dip of about 5% within ~100 ms of a feed, or the feed break-beam. The count goes down by one.
- **Flywheel never brakes or reverses** (FLOAT). It coasts through the 8 s transition (G403).
- **Upgrade path, if recovery between shots is too slow:** our own loop, power = (kS + kV·target) × 12 V ÷ battery V + kP·error (our software research).

## 6. Indexer: counting to 4 (possession discipline)

- The count starts at the number of balls **actually loaded**: 4 in AUTO, confirmed by sensors. TELEOP gets the count from the AUTO hand-off.
- The intake **refuses "in" at 4**, with a short reverse pulse when it first becomes full. The **slot-4 sensor is the safety net**: if it sees a ball, we are full, whatever the count says.
- **Mouth colour sensor:** opponent NECTAR → the roller reverses **before** the ball is drawn in. Phase 1 option flag: refuse our own NECTAR too, because the hood can't launch it yet.
- **Feed sequence for one ball:**
  1. Wait until ready (flywheel, turret, still chassis, no tip pause).
  2. Open the gate briefly.
  3. Confirm the shot (beam or speed dip, about 0.4 s timeout).
  4. Count − 1.
- **Jam:** no shot confirmed → reverse briefly and retry once → stop and rumble.
- Rules behind this: G407/G408, no momentary exception (QA3, QA4, QA23), TU05 sorter boxes. QA29 (active rejection designs) is pending ([03](03-teleop.md#possession-discipline-design-requirement-and-driving-rule)).

## 7. AUTO and TELEOP structure

**AUTO**
- Selector in INIT: alliance, start, routine (A0, A0-blind, A1, A2, A3-R1, A3-R2), delay; lock with A; **defaults to A0** ([02](02-autonomous.md#init-selector-what-the-drive-team-sets)).
- `@Autonomous(preselectTeleOp = …)` queues TELEOP automatically (G305 box; SDK RobotCore).
- Poses are written once for red and rotated 180° for blue. **Never mirror.**
- Each routine is a state machine with deadlines (park drive by 24 s or 26 s).
- Save pose, ball count and HIVE state **every loop**: the DS can stop AUTO at any moment. The SDK `blackboard` is not saved across power cycles (SDK sample `ConceptBlackboard`).
- Coordinates: the 12303 code runs Pedro in the FTC field frame. Pedro's own website uses a corner origin; convert with x_pedro = Y + 72, y_pedro = 72 − X, heading_pedro = heading − 90° (Pedro coordinates docs). Only needed for drawing paths on the visualizer.

**TELEOP**
- INIT: **nothing moves** (G403). Read the hand-off; show the competition-build warning line.
- START: arm every subsystem (first actuator commands happen here).
- Loop: field-centric drive, intake requests, volley, aim, overrides, cues. The match timer only drives reminders; **the field timer is the authority** (M p.79).
- **Fallbacks:**
  - Pedro fails → robot-centric drive;
  - no Limelight → odometry-only aim;
  - turret misbehaving → pin it, aim with the chassis.

---

## 8. Competition rules for software (checklist)

| Rule | What it means for us |
|---|---|
| **R704.D + QA26** | No Wi-Fi streaming **anywhere in the venue**, pits and practice field included: no FTC Dashboard, no Panels, no Limelight web UI over Wi-Fi. **Pedro AutoTune starts a web server (ports 10158 and 12649) whenever it is in the app, with no off switch.** Before every event: set `PEDRO_AUTOTUNE = false` in `build.dependencies.gradle`, rebuild, deploy over USB. The OpModes print a warning in INIT if AutoTune or a dashboard is still inside |
| R704.C | Laptops off the robot network during matches |
| R705 / R711 | Device names `12303-RC` / `12303-DS`; change the Control Hub Wi-Fi password; Bluetooth off |
| E301 | No personal hotspots at the venue |
| G304.H, R102 | Motionless after INIT; no flywheel pre-spin |
| G305 | Select and INIT an OpMode; AUTO needs the 30 s timer enabled |
| G401 | No driver input during AUTO: every choice is made in INIT |
| G402 | AUTO paths stay on our side (red Y ≤ 0, blue Y ≥ 0) |
| **G403** | TELEOP INIT moves nothing; actuators wake at START; **press START only when TELEOP begins** |
| G404 | Nothing powered after 0:00; press stop at the buzzer |
| G407 / G408 | Count interlock at 4; refuse opponent NECTAR at the mouth |
| G409 | Optional: block the intake briefly near our HIVE right after a tip event |
| G410 | Any "NECTAR into FLOWER" macro stays locked until TELEOP has run ≥ 61 s (1:00 left + 1 s margin) |
| G417.D | Aim at our own tags only (red 30–37, blue 38–45). Never target the opponent HIVE |
| R105 | Size limits must be **mechanical**. Software soft limits on the turret don't count for inspection |
| T405 (M p.151) | In the optional field-calibration window robots may run OpModes but may not drive or launch. Tune the Limelight over a **USB cable** to a laptop, not Wi-Fi |

**Limelight specifics:**
- USB-C → USB-A cable into the Control Hub **USB 3.0** port (ftc-docs hub tips).
- AprilTag pipeline at 640 × 480 (50 fps), ID filter = our 8 tags, marker 3.25 in (82.55 mm) (Limelight docs).
- `getLatestResult()` is never null in SDK 12; check `isValid()` and staleness.
- Export pipelines before LimelightOS updates (updates erase them).

**SDK specifics:**
- There is no SWYFT motor type, so use the ticks-per-second velocity calls, not the angle-unit ones (our check of the SDK 12.0 motor types).
- Set goBILDA servos to the full 500–2500 µs range for their full travel (gm0 SDK servos).

---

## 9. Tests, tuning and logging

- **Tuning OpModes** (names in the README): Pedro AutoTune (home only), flywheel tuner, turret calibration, **shot-map logger** (CSV), vision probe, sensor probe. They feed the experiments in [07](07-testing-and-practice.md).
- **Logging:** CSV files on the Control Hub (`/sdcard/FIRST/...`) and DS telemetry. Both are allowed because nothing is streamed (R704.D).
- **Unit tests:** pure math (aim solver, LUT, red→blue rotation) has no FTC imports and runs as JUnit tests on the laptop.
- **Software gates:**

| Gate | Target | Week |
|---|---|---|
| Odometry | 48 in push reads 48 ± 0.5 in; 10 spins read 3600 ± 2° | W1–W2 |
| Paths | End error < 1 in | W2 |
| Flywheel | Ready < 1.0 s from idle | W1–W2 |
| Vision | Bearing within 1° of a tape-measured line; distance within ±2 in | W2 |
| Loop time | < 20 ms with every sensor on | W3 |

## 10. Software week by week (matches [00](00-master-plan.md#six-week-timeline))

| Week | Deliverable |
|---|---|
| W1 | Build and deploy; TestHardware passes; Pinpoint configured; flywheel PIDF + ready gate; shot-map logger; count-to-4 interlock |
| W2 | Pedro tuning at home (AutoTune, then switch it off); turret calibration; Limelight pipeline + vision bias; **A0 and A1 reliable** |
| W3 | TELEOP automation: auto-aim, one-button volley, auto-stop at 4, cues, HiveTracker overrides |
| W4 | A2 / A3-R1 / A3-R2 OpModes; HiveTracker vision auto-switch tuned on our HIVE |
| W5 | Log review after match sims; competition build checklist |
| W6 | **Code freeze**: bug fixes only, every change re-tested on the robot |

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
  - servos: https://gm0.org/en/latest/docs/software/adv-control-system/sdk-servos.html
  - bulk reads: https://gm0.org/en/latest/docs/software/tutorials/bulk-reads.html
- **Limelight**:
  - 3A: https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a
  - FTC programming: https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming
- **ftc-docs**:
  - hub tips: https://ftc-docs.firstinspires.org/tech_tips/tech-tips/tech-tip-hub-tips/tech-tip-hub-tips.html
  - AprilTag clusters: https://ftc-docs.firstinspires.org/apriltag-clusters
- **CAD** (aim points, tag heights): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- Our research team's fact-check of the SDK 12.0 binaries and the Pedro AutoTune source (2026-10-20).
