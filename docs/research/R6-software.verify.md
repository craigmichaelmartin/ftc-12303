# R6 Software report: adversarial fact-check

Checked 2026-10-20 against primary sources I fetched myself. Downloads are in `scratchpad/downloads/r6v-*`. My own scripts are `scratchpad/scripts/r6v_jdis.py` (a static class-file disassembler that executes nothing), `r6v_qa_parse.py` and `r6v_ballistics.py`.

**Result:** 34 facts confirmed, 1 corrected (F18), 0 refuted, 0 unverifiable. Several facts are confirmed but carry a nuance or a source fix, noted below.

## Sources re-fetched independently

**FIRST sources**
- Competition Manual TU05 PDF, visually checked:
  - p65-77: zones, tile grid, HIVE, CELL, AprilTag figures
  - p79, p85-91, p103-112, p122-123, p132-134, p140-145
- Team Updates 00-05 (combined PDF from first-resources.pdx-prod.ftclive.org)
- Live Q&A board, game-qa.firstinspires.org/boards/2027/FTC/QA. I parsed the JSON embedded in the page: 38 questions, 26 answered.

**SDK and libraries**
- FtcRobotController README (raw GitHub) and the GitHub releases API
- SDK sample OpModes from master
- Maven Central SDK artifacts (Vision, Hardware, RobotCore 12.0.0), disassembled with my own parser
- Pedro Pathing: GitHub releases, repo tree and FusionLocalizer/PoseFactory source; Quickstart build files; Maven Central metadata; AutoTune (`com.pedropathing:tuning:1.0.1`) sources jar
- Road Runner: releases, quickstart build files, MecanumDrive and TuningOpModes source, rr.brott.dev tuning docs
- Framework sources: FtcDashboard.java, Panels source (Panels.kt, PanelsConfig.kt, PreferencesHandler.kt), NextFTC and NextFTCSuite releases, Ivy releases, SolversLib docs

**Docs and vendors**
- ftc-docs: apriltag-clusters, field coordinate system, PIDF coefficients, external libraries, hub tech tips
- gm0 SDK motors page
- Limelight docs: 3A page, coordinate systems, FTC programming
- Vendor pages: goBILDA Pinpoint V1 and V2, goBILDA Dual Mode Servo 25-2 and 25-3, ServoCity 5203-2402-0001, SWYFT Drive V2, SWYFT odometry module, SparkFun OTOS

## Verdicts

| ID | Verdict | Key evidence |
|---|---|---|
| F1 | confirmed | README: "Version 12.0 (20260907-090034)"; GitHub release v12.0 published 2026-09-12T18:03Z. Requirements: "Android Studio Narwhal 3 Feature Drop or later". v11.2: Gradle 9.1 / AGP 8.13.2. NOTICE: "public FTC SDK for the BIOBUZZ (2026-2027) competition season". |
| F2 | confirmed | README v12.0 says verbatim: getCurrentGameTagLibrary() returns BIOBUZZ tags; "Origin of each cluster is located in the center of the Cell opening for easy aiming"; "since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization." The breaking change requires `instanceof` checks and casts. |
| F3 | confirmed | My disassembly of `AprilTagGameDatabase` in Vision 12.0.0 shows four clusters: "RED SCORING" (30), "RED AUDIENCE" (34), "BLUE AUDIENCE" (38), "BLUE SCORING" (42). Each has VectorF(0,0,0) INCH and an identity quaternion. Members are at x = −6.5, −2.75, 2.75, 6.5; y = 7.1874; z = −5.622; size 3.25. Extra detail: getCurrentGameTagLibrary() = sample tags + BIOBUZZ. |
| F4 | confirmed (as a description of ftc-docs) | The page uses the name prefix R/B and treats \|roll\| < 90 as scorable. Its examples: RED SCORING roll 159.3° (50% found) and RED AUDIENCE −19.7° (100%). My geometry analysis (see "Missed" below) shows the heuristic depends on which side the camera views from, so the report's caveat is justified. |
| F5 | confirmed | p140-141: R702 and Table 12-9 list only "Limelight Vision Limelight 3A LL_3A"; Example 6 prohibits OpenMV, OAK-1 and the Limelight 3G. p143: R707 allows only webcams and optical vision sensors per R708, a USB hub or switch, and a REV Expansion Hub on USB. R708 allows single-sensor vision natively supported by the RC app (UVC webcams and R702 coprocessors); stereo cameras are banned, and UVC cameras may use only the UVC stream. |
| F6 | confirmed | R704.D text matches (it names FTC Dashboard and FTControl Panels; "No continuous video stream is allowed"). Live Q26 (answered 2026-10-19): "Wi-Fi streaming is not allowed anywhere in a competition venue (per R704.D) … (even in the pit and practice field)". |
| F7 | confirmed | Limelight 3A page: AprilTag "20fps @ 1280x960 (2x downscale), 50fps @ 640x480 (2x downscale)"; 90 fps color; FOV H 54.5° × V 42°; "No built-in LED illumination"; USB-C into the Control Hub's blue USB 3.0 port; green light after "about 15-20 seconds". |
| F8 | confirmed | Bytecode: `FiducialResult` parses t6t_cs, t6t_rs, t6c_ts, t6r_ts and t6r_fs. `createPose3DRobot` builds Position in METER and YawPitchRoll in DEGREES with yaw = [5], pitch = [4], roll = [3]. `getLatestResult()` lazily creates an empty LLResult and never returns null (README 10.3). `uploadFieldmap(LLFieldMap, Integer)` and `uploadPipeline(String, Integer)` exist. Limelight docs: camera space X right, Y down, Z out of the lens. |
| F9 | confirmed | GitHub: v3.0.0 2026-09-10, v3.0.1 2026-09-18, v3.0.2 2026-10-16. The release body lists Foresight, Pose Factory, the Path API and AutoTune ("a robot-hosted webpage"). pedropathing.com/docs/pathing/pedro3 says "major update and full rewrite". Quickstart `build.dependencies.gradle` pins SDK 12.0.0, revhub 3.0.1 and tuning 1.0.1. Extra detail: Pedro artifacts are on **Maven Central** (not Dairy). revhub 3.0.2, tuning 1.0.2 and tuning 1.0.3-beta.1 are already published there. |
| F10 | confirmed | `revhub/.../localizers` contains OTOS, OctoQuad, Pinpoint, TwoWheel, ThreeWheel and ThreeWheelIMU, plus helper classes. There is no drive-encoder localizer anywhere in the repo or in the Quickstart tuners. `FusionLocalizer` is a Kalman filter with a history buffer; its `addMeasurement(pose, timestamp[, variance])` takes NaN for unknown axes. |
| F11 | confirmed | road-runner v1.0.1 published 2025-01-05. The quickstart pins SDK 11.0.0, roadrunner core/actions 1.0.1, ftc 0.1.25 and dashboard 0.5.1. `MecanumDrive.DriveLocalizer` uses drive encoders plus the IMU. RR docs: "Many steps require using FTC Dashboard". |
| F12 | confirmed (source fix) | The cited URL is the **discontinued V1** page (3110-0002-0001). The V2 page (gobilda.com/pinpoint-v2-odometry-computer-imu-sensor-fusion-for-2-wheel-odometry/, SKU 3110-0002-0002, in stock) independently confirms $79.99, "~1500hz" and "256000 countable events per second". README 11.1 adds V2 support. Driver constants: 13.2629 ticks/mm × π × 48 mm = 2000 and 19.8944 × π × 32 = 2000. SWYFT module: $64.99, "4096 PPR magnetic quadrature" (whether that is counts or pulses is unstated), "Works directly with goBILDA Pinpoint". |
| F13 | confirmed | SparkFun SEN-24904: $84.95, in stock, "Must be exactly 10mm for FTC field tiles". R702 Example 2 allows it. Support exists in all three stacks: SDK `SparkFunOTOS` class (Hardware 12.0.0), Pedro `OTOSLocalizer`, and the RR quickstart `OTOSLocalizer`. |
| F14 | confirmed | SWYFT page: SR-DRIVE-02 is the 86 mm pack of 4, $389.99; 12.7:1 (~475 rpm at the wheel); Spike SR-MOTOR-DC-01 at 6000 rpm; encoder "28 PPR"; 6.4 ft/s (86 mm). The vendor's example code is a mecanum teleop, so the drive is mecanum. R501 lists the Spike. 28 × 12.7 = 355.6 counts per wheel revolution and 33.4 counts/in, **if** the hub reads 28 counts per motor revolution (goBILDA-style "PPR"). Check empirically. |
| F15 | confirmed | ServoCity 5203-2402-0001: "28 PPR at Output Shaft", 6000 rpm, 9.2 A stall. gm0: "5 value ring buffer … new value added … every 10 ms". The ~20 ticks/s (≈43 rpm) step is the report's inference, and the arithmetic holds. ftc-docs hub tips: "Encoder ports 0 and 3 are hardware-counted, but ports 1 and 2 are software-counted"; high-CPR encoders such as the Through Bore belong on 0 or 3. |
| F16 | confirmed | REV FGC page (FIRST Global, same Control Hub): F = 32767/maxV, P = 0.1F, I = 0.1P, D = 0, and "only remembered for the duration of the OpMode". ftc-docs: "specific to each channel (motor port) and to each RunMode"; changes "do not persist if you power cycle". |
| F17 | confirmed | goBILDA 25-2: 300°; CR mode via the 3102 programmer; 40/50/60 rpm at 4.8/6/7.4 V; PWM 500-2500 µs (CR 900-2100); 4 µs deadband; 5 kΩ pot. 25-3: 90/115/145 rpm (CR 1000-2000 µs). README 10.3 adds Full Range Servo (500-2500 vs 600-2400). The photo shows a ~60T spur pinion on a servo hub, driven by a grey 3-wire servo and meshing with the ring gear. |
| F18 | **corrected** | Q3/Q4: no MOMENTARY exception (confirmed live). Q23 (2026-10-13) and the TU05 boxes say routine design-based control is likely STRATEGIC (confirmed). Q29 is still ACCEPTED and unanswered (confirmed). **But** the manual text checked visually on p108 gives G408 as "VERBAL WARNING. YELLOW CARD per MATCH, if STRATEGIC." with **no MAJOR FOUL**. Only G407 carries "MAJOR FOUL and YELLOW CARD". Q23 restates both as MAJOR FOUL + YELLOW CARD, but §1.7.4 says "responses in the Q&A do not supersede the text in the manual". |
| F19 | confirmed | p104 G304 A, C, E, G (TU03 wording) and H: "fully motionless following completion of OpMode initialization". Also D (no FLOWER contact) and F (STARTING CONFIGURATION). Preloads may be on tiles touching the robot (10.3.4). |
| F20 | confirmed | p106-107 G403 plus the box: "(powering actuators such that they move or twitch in any way), then the team should wait until TELEOP begins before pressing INIT." |
| F21 | confirmed | p109 G410 (MAJOR FOUL per NECTAR). p110: "primary FIELD timer display is the cue". p79 Table 9-1: 1:00 "Chime" (FLOWER Ownership Unlocked) and 0:20 "Train Whistle"; "visual FIELD timers are the authority". TU04 added the chime. |
| F22 | confirmed | p87 10.5.1 A/B, plus the box about pausing launches at the downward-facing CELL. Q12 (live): tipping varies with "placement … and dynamics"; it is scored when the damper contacts "regardless of how many". |
| F23 | confirmed | p111-112 G417 and example D: "impeding the TIP of an opponent's HIVE by LAUNCHING … at it". Q10: "no rule that specifically disallows LAUNCHING your own NECTAR (or POLLEN) into an opponent HIVE". |
| F24 | confirmed | p90 10.5.4, p86 10.5 F, p91 Tables 10-2 and 10-3 (SWARM 16 at "All Other Events"; TBA for championships; Premier events may set their own). Q21: "if the ROBOT is touching a wall at the end of AUTO the LEAVE achievement is not awarded." LOADING ZONE is 23 × 11 in (p65-66). |
| F25 | confirmed | Fig 9-2, 9-5 and 9-17 (viewed): red LZ on the red wall at the far end ↔ blue LZ on the blue wall at the audience end; red GARDEN in the audience corner ↔ blue GARDEN in the far corner; FLOWERS at (red wall, 48 in from audience), (far wall, 48 in from red), (blue wall, 96 in), (audience wall, 96 in), so they are 180° symmetric; up cells are red audience and blue far. The tag order maps base+i ↔ base+i (34↔42, 30↔38). |
| F26 | confirmed (inference) | ftc-docs: an inverted square field with the Red Wall on the audience's left (DECODE) has "X axis points towards the audience"; +Y points toward blue; CCW is positive. Manual p68: red ALLIANCE AREA is on the audience's left. ftc-docs does not mention BIOBUZZ. |
| F27 | confirmed (estimate) | My own geometry from published dimensions (42.91 in overall, 12.04 in deep, 30°, 53.5/65.6 in) gives a 1.37 in bottom-face offset below the pivot. Opening centroid: ≈16.5 in horizontally from the pivot, ≈58.3 in high (vertical midpoint 59.55). Up-cell tag row: ≈13.0 in from the pivot, ≈49.9 in high. Down-cell tag row: ≈35.6 in high. The opening faces outward because Fig 9-15 places the "Front of Cell" opposite the sticker's bottom edge, and p75 says the bottom edge points to field center. Its normal is 30° above horizontal. 25.5 in center-to-center confirmed. |
| F28 | confirmed (reproduced) | My script reproduces the table exactly in vacuum (e.g., 55°/48 in: 5.90 m/s, 0.36 s, 3131 rpm). Minimum angles with zero margin: 50.4° at 36 in, 55.4° at 30 in, 61.1° at 24 in. A drag model for perforated balls (Cd 0.5) adds about 3-5% to the required speed. The 50% transfer factor is an unvalidated assumption. |
| F29 | confirmed | RobotCore 12.0.0 Gamepad has rumble(int), rumble(d,d,int), rumbleBlips, runRumbleEffect, setLedColor, runLedEffect, xWasPressed/Released for every button, leftTriggerWasPressed and left_trigger_pressed. ConceptGamepadRumble: Xbox/PS4 have rumble1/rumble2; "Logitech F310 … does not have *any* rumble ability". R904 example: "wireless gamepad communication" is prohibited. Q16: no device list, but no Wi-Fi or Bluetooth. |
| F30 | confirmed | The `OpMode.blackboard` static HashMap and `@Autonomous.preselectTeleOp()` exist. ConceptBlackboard: "NOT saved across power reboots or downloads or robot resets". G305's box recommends auto-loading, specifically for teams creating a default AUTO. |
| F31 | confirmed | DcMotorEx has getCurrent, setCurrentAlert and isOverCurrent. LynxModule exposes only total, GPIO-bus and I2C-bus current plus voltages. SERVO_CURRENT exists only as an internal `LynxGetADCCommand.Channel`. The servo API has PWM range and pulse width only; `getPosition()` returns the commanded value. R502 allows a 4th-wire feedback output. |
| F32 | confirmed | p134 R503 sets 8 motors and 8 servos. TU00: "maximum number of allowed servos is reduced to 8". |
| F33 | confirmed | NextFTC v1.1.0 (2026-02-23); NextFTCSuite v0.3.0 (2026-10-17); nextftc.dev uses `dev.nextftc.v2:*` and says "migration guide (coming soon)"; SolversLib docs list `org.solverslib:core:0.3.6` and "0.3.6+ is for Pedro Pathing 3.0.0 and higher"; Ivy v1.1.1 (2026-09-10) is on Maven Central. |
| F34 | confirmed | FtcDashboard.java registers the "Enable/Disable Dashboard" OpMode and "Enable Dashboard"/"Disable Dashboard" menu items, persisted with the SharedPreferences "autoEnable" key (v0.6.0 released 2026-05-16). Panels: `PanelsConfig.isDisabled`, a persistent "autoEnable" preference, and HTTP 8001 / WS 8002 started only if enabled. The site lists v1.0.19. |
| F35 | confirmed | README 10.1.1: "OnBotJava only supports Java 1.8". README 7.0 and ftc-docs: ".aar files with assets are not supported"; transitive dependencies must be uploaded by hand. "Impractical for Pedro/Panels" is an inference, and a sound one: Panels and AutoTune ship web assets. |

## Missed facts and risks that matter

1. **Pedro AutoTune starts web servers unconditionally.** The `com.pedropathing:tuning` sources (1.0.1) use a Sinister `OnCreateEventLoop` hook that starts a NanoHTTPD server on port **10158** and a WebSocket server on port **12649** whenever the RC event loop is created. Unlike Dashboard and Panels, it has no enable/disable switch.
   - The report's Gradle block includes `tuning:1.0.1` as a normal dependency, and its competition-build advice covers only Dashboard and Panels.
   - Given R704.D and Q26 (no third-party streaming services anywhere in the venue), exclude `tuning`, Panels and Dashboard from the competition build, using a build flavor or a commented dependency. Finish AutoTune at home.
2. **The G408 strategic penalty is a YELLOW CARD only** (manual p108); G407's is a MAJOR FOUL plus a YELLOW CARD. Q&A answers do not supersede the manual (§1.7.4), and referees are the final authority.
3. **The ftc-docs |roll| < 90 test depends on viewing side, not HIVE state.**
   - Stickers face down with their bottom edge toward field center (p75). Each tag's top edge therefore points outward in **both** HIVE states; the ±30° tilt does not flip it.
   - A camera outboard of a cell (on the opening side) sees that cell's tags upright; a camera inboard sees them inverted. The test equals "up" only when the robot is on the shooting side of the up cell.
   - After a TIP, from the old side, the newly-up far cell's tags face away and the newly-down near cell's tags face down and inward, so vision may lose lock until the robot crosses over.
   - Use the height test (≈50 in up vs ≈36 in down) or the tag-normal tilt direction.
4. **Hub wiring details from ftc-docs tech tips.**
   - Servo port pairs (0,1), (2,3) and (4,5) share one supply. Put high-current servos on 0/2/4 or 1/3/5, or add a Servo Power Module. goBILDA Dual Mode servos stall at 2-2.5 A, and the team plans several servos (turret, intake, spinners, hood).
   - **I2C port 0 shares the bus with the IMU.** Keep the Pinpoint, OTOS and color sensors off port 0.
5. **HIVE clearance risk.** R105 allows a 29 in robot height. The down-cell bottom is 30.6 in (TU01), with a ±1 in field tolerance (p63). G417.E lists "contacting a HIVE directly or transitively through a CONTROLLED SCORING ELEMENT" as likely STRATEGIC (MAJOR FOUL plus YELLOW CARD). Paths and auto-park routines should avoid driving under down cells if the robot is tall. G409/Q17 also penalize deflecting spilled elements under a tipped HIVE.
6. **Size limits must be physical.** R105.B requires expansion limits to be enforced "without the use of software". Turret or hood soft limits cannot be what keeps the robot inside 18 × 24 × 29 in.
7. **Preloads need not be held.** Preloads may sit on tiles touching the robot (10.3.4, TU03 G304.G), so the indexer's starting count should match what is actually loaded, not a hard-coded 4.
8. **LEAVE works even if a partner moves the robot.** Q28: LEAVE is awarded if a partner pushes or pulls the robot off the wall, regardless of how it ends up off the wall.
9. **Library versions and sources.**
   - Pedro artifacts come from Maven Central. revhub 3.0.2 (with FusionLocalizer, controller and piecewise-heading fixes) and tuning 1.0.2 / 1.0.3-beta.1 are available; the Quickstart still pins 3.0.1 / 1.0.1.
   - The goBILDA Pinpoint V1 SKU is discontinued; buy V2 (3110-0002-0002).
10. **The Limelight 3A uses HTTP over USB** (port 5807, default 10 ms poll) and its AprilTag frame rates assume 2x downscale. Limelight docs tell FTC users to configure it by plugging the 3A into a laptop over USB, which matches the report's advice not to tune it over Wi-Fi.
