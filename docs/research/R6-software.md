# R6 — Software stack & control approaches for FTC 12303 (BIOBUZZ 2026-27)

Prepared 2026-10-20 for the strategy/design agents and the coach. Scope: SDK + IDE, localization and path following, turret auto-aim, flywheel control, intake/indexer logic, code architecture, Java skeletons.

**Evidence tags used below**
- **[V]** verified in a primary source (Competition Manual TU05 page/rule, Team Update, official Q&A, FTC SDK release notes or the SDK 12.0.0 binaries themselves, vendor page, library source on GitHub). The source key is given in brackets, and section 12 lists full URLs.
- **[I]** inference or engineering judgement by me. Treat as a recommendation, not a fact.
- **[E]** a number I estimated by measuring a manual figure (±2–3 in). Measure it on a real field or in the official CAD before relying on it. The manual says the CAD is authoritative at ±1 in [MAN p63].

---

## 0. Executive summary and recommendations

1. **Platform [V/I]:** Use FTC SDK **v12.0**, the official 2026/27 release (GitHub release 12 Sep 2026, build 20260907). Write the code in **Android Studio** (Narwhal 3 Feature Drop or later is required) in Java, with Git and GitHub [SDK-REL, SDK-README]. Blocks and OnBot Java cannot practically host Pedro Pathing, Panels or custom controllers. Use the SDK's new `@Utility` TestHardware and TestGamepad OpModes so students can check wiring [SDK-README v11.2].
2. **Buy odometry hardware now [I]:** The team lists no dead wheels. Pedro Pathing 3 ships no drive-encoder localizer (its revhub module has only Pinpoint, OTOS, OctoQuad, two-wheel, three-wheel and three-wheel+IMU) [PEDRO-SRC]. Mecanum wheel slip ruins encoder-only odometry for field-relative aiming. Recommended options:
   - **goBILDA Pinpoint V2** ($79.99) plus two pods. The goBILDA pods are 2000 CPR; the SWYFT Linear Odometry Module is $64.99 and "works directly with the goBILDA Pinpoint". [GB-PINPOINT, SWYFT-ODO]
   - **SparkFun OTOS** ($84.95). R702 Example 2 explicitly lists it as an allowed device [OTOS, MAN p140-141].
3. **Path following [V/I]:** Use **Pedro Pathing 3.0.2** (released 2026-10-16). Its robot-hosted **AutoTune** pages handle drivetrain, localizer and the "Foresight" algorithm, which makes it the fastest route to reliable autos in 1–2 weeks once the odometry is mounted [PEDRO-REL, PEDRO-QS]. Fall back to **Pedro 2.1.2** (May 2026) if Pedro 3 blocks you. It has mature docs and a full season of DECODE tutorials. Road Runner 1.0.1 still works but has had no core release since Jan 2025, and its quickstart is still on SDK 11.0 [RR].
4. **Vision [V/I]:**
   - **Confirm the camera is a Limelight 3A.** It is the only legal programmable vision coprocessor; the **Limelight 3G is explicitly prohibited** [MAN p140-141 R702, Table 12-9, Example 6].
   - Mount the 3A **on the turret, pitched about 35–40° up**, because the BIOBUZZ tags are on the underside of the cells, about 50 in up [E].
   - Run an AprilTag pipeline with an **ID filter for your own alliance's 8 tags** (red 30–37, blue 38–45) at 640×480, which runs at about 50 fps [LL-3A, LL-TAGS].
5. **Do not use static-field-map localization (MegaTag/botpose) with BIOBUZZ tags [V].** The tags are on HIVE cells that move when the HIVE tips. SDK 12 says verbatim: *"since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization"* [SDK-README v12.0]. Use the tags for **relative aiming at the cell** instead. The SDK's BIOBUZZ library defines each 4-tag **cluster origin at the center of the cell opening** "for easy aiming" [SDK-README v12.0, SDK-VISION-AAR].
6. **Aim [I]:** Use a **fused aim**. Odometry (Pinpoint) gives the field-relative bearing to the up-cell opening. When the Limelight sees the cell, a low-pass "vision bias" corrects it. Use the turret's angle **at image-capture time** to cancel camera latency. Gate the feeder on chassis speed (stop to shoot). The estimated time of flight is 0.3–0.7 s, so at 1 m/s a moving robot would need 12–28 in of lead [I, ballistic table §6.5].
7. **Flywheel [V/I]:**
   - Start with the hub's built-in `setVelocity()`, using REV's PIDF recipe: F = 32767/maxTicksPerSec, P = 0.1·F, I = 0.1·P, D = 0 [REV-PIDF].
   - Move to a custom kS + kV·v + kP·error controller with battery-voltage compensation if recovery between shots is too slow.
   - Declare "ready" only after 3 or more consecutive in-tolerance loops.
   - Use a measured **distance → RPM (+hood)** interpolating table.
   - The goBILDA 6000 rpm 1:1 motor has only **28 counts/rev**, so velocity readings are coarse: about 20 ticks/s ≈ 43 rpm steps [I from GM0-MOTORS, GB-6000].
8. **Indexer [V]:** The Q&A says there is **no MOMENTARY exception for G407 (more than 4 elements) or G408 (opponent NECTAR)**. Any CONTROL is a violation, and routine reliance on "grab then eject" is likely STRATEGIC (MAJOR FOUL plus YELLOW CARD) [QA 3, 4, 23; TU05 G407/G408 boxes]. The software therefore needs to:
   - keep an exact ball count, which starts at **4** because of the preloads;
   - **stop the intake when the count reaches 4**;
   - detect opponent NECTAR **before** it is drawn in, using a front camera or a mouth color sensor, and refuse it.
9. **Architecture [I]:** Use plain-Java **subsystems with explicit enum state machines** and a single `Robot.update()` loop with manual bulk reads. Middle-schoolers can read a `switch(state)`. Optionally adopt Pedro's **Ivy** (1.1.1) or **SolversLib** (0.3.6, built for Pedro 3) once the basics work. Avoid NextFTC v2 for now: the `dev.nextftc.v2` artifacts are at 0.3.0, pre-1.0, and the migration guide is "coming soon" [NEXTFTC, SOLVERS, IVY].
10. **Competition hygiene [V]:**
    - **R704.D bans FTC Dashboard, FTControl Panels and any other third-party streaming.** Q&A 26 says Wi-Fi streaming is prohibited **anywhere in the venue, even the pits and practice field**. Disable both before the event (Dashboard has a persistent "Enable/Disable Dashboard" OpMode and menu item; Panels has a persistent enable/disable preference) and deploy code over USB [MAN p141, QA 26, DASH, PANELS].
    - **TeleOp INIT must not move actuators** during the 8 s transition (G403) [MAN p106-107].
    - Make the AUTO OpModes `@Autonomous(preselectTeleOp=...)` [SDK-RC-AAR; MAN p105 G305].

---

## 1. Rules and match facts that constrain software

| Topic | Rule / source | What the software must do |
|---|---|---|
| Vision hardware | R702 + Table 12-9: only **Limelight 3A (LL_3A)** may be reprogrammed; Example 6 says **Limelight 3G, OpenMV, OAK-1 are prohibited** [MAN p140-141]. R707/R708: USB only for UVC webcams, allowed coprocessors, USB hubs and an Expansion Hub; UVC webcams may use only the UVC stream [MAN p143]. | Confirm the Limelight model. A UVC webcam (Logitech C270/C920, goBILDA USB cam) is also legal and is supported by the SDK `VisionPortal`. |
| Wireless / streaming | R704.C: laptops must be off the RC Wi-Fi during matches. R704.D: only the DS app may stream control/debug/telemetry; *"Additional logging/streaming services, such as those hosted by third party plugins and tools such as FTC Dashboard, FTControl Panels, and others are prohibited. No continuous video stream is allowed."* [MAN p141]. Q&A 26 (HuskyLens 2): *"Wi-Fi streaming is not allowed anywhere in a competition venue (per R704.D) … (even in the pit and practice field)"* [QA]. E301: teams may not run their own Wi-Fi networks [MAN p37]. | Use a competition build or toggle with Dashboard/Panels disabled. Use DS telemetry and on-robot log files only. Retune the Limelight over a **USB cable laptop → LL3A**, not over Wi-Fi [I]. |
| Device config | R705 naming `12303-RC` / `12303-DS`; R711: change the Control Hub Wi-Fi password, turn Bluetooth off, Wi-Fi on [MAN p142-143]. | Checklist item. |
| Actuator budget | R503: **≤ 8 motors and ≤ 8 servos** (servo limit reduced to 8 this year) [MAN p134; TU00]. R501 lists **SWYFT Spike (SR-MOTOR-DC-01)** and goBILDA 520x as legal [MAN p132]. R502 allows servos with a **4th-wire position feedback** output (e.g., Axon) [MAN p133]. | Count: 4 drive + 1 flywheel = 5 motors, leaving 3 free. Servos (turret pinion, hood, intake, spinners) must total 8 or fewer. |
| Start conditions | G304: fully on own side (columns A–C red, D–F blue); **touching the perimeter wall**; not in the LOADING ZONE; not touching a FLOWER; exactly 4 POLLEN preloads; **H. "fully motionless following completion of OpMode initialization"** [MAN p104]. | AUTO INIT may position servos but must finish moving. Set the indexer count to 4. |
| OpMode selection | G305: an OpMode must be INIT'd; AUTO OpModes need the 30 s timer enabled; the blue box recommends a default AUTO with "the auto-loading feature to automatically queue up their TELEOP OpMode" [MAN p105]. | `@Autonomous(name=..., preselectTeleOp="TeleOp")` (annotation member verified in RobotCore 12.0.0) [SDK-RC-AAR]. |
| AUTO | G401: no driver interaction from the countdown to the end of AUTO [MAN p106]. G402: each alliance has priority on its own side (A–C / D–F); crossing is risky [MAN p106]. | All selection happens in INIT. Paths stay on own side. |
| Transition | G403: **no powered movement** in the 8 s AUTO→TELEOP transition. *"If the INIT portion of the OpMode causes the ROBOT to violate this rule (powering actuators such that they move or twitch in any way), then the team should wait until TELEOP begins before pressing INIT."* [MAN p106-107] | TeleOp `init()` must not command servos or motors. Defer the first `setPosition()` until START. |
| End | G404: no powered movement after TELEOP ends [MAN p107]. | Drivers press STOP; the flywheel coasting is OK (inertia is exempt). |
| Holding limit | G407 (≤ 4 CONTROLLED) and G408 (no opponent NECTAR), with TU05 boxes: *"REPEATED instances … are likely to be perceived as STRATEGIC, even if the ROBOT has a 'keep 4 and discard the rest' sorting mechanism"* / *"… even if the ROBOT has a sorting mechanism that automatically discards opponent NECTAR"* [TU05; MAN p108-109]. Q&A 3/4: **no MOMENTARY exception** for either rule; Q&A 23: routine momentary control as a design feature is likely STRATEGIC [QA]. | Intake interlock at count 4; reject opponent NECTAR *before* it is controlled (§7). |
| Catching | G409: may not catch or deflect elements released by a TIPPED HIVE before they touch something else; Q&A 17 says deflection counts [MAN p109; QA 17]. | Optional: inhibit the intake briefly near the HIVE after a detected TIP [I]. |
| NECTAR into FLOWERS | G410: only in the **last 60 s** (primary field timer is the cue) [MAN p109-110]. G426: the human player may enter all remaining NECTAR at ≤ 60 s [MAN p117]. | Robot-side lockout for any "score nectar in flower" macro until TeleOp elapsed ≥ 61 s (+1 s margin) [I]. |
| HIVE | G417: change HIVE motion only by LAUNCHING into an **up-facing** CELL; launching *at* an opponent HIVE to impede its TIP is STRATEGIC [MAN p111-112]. Q&A 10: no rule forbids accidentally launching your own element into the opponent HIVE [QA]. 10.5.1: TIP = moves to the other stable state **and** the damper contacts the frame; *"LAUNCHING at the downward-facing CELL while a HIVE is tipping may disrupt its movement … teams … may need to pause their LAUNCHING"* [MAN p87]. Q&A 12: tip count varies with placement and dynamics; a TIP scores whenever the damper contacts, regardless of count [QA]. | Target filter = **own alliance's cells only**. **Pause feeding about 1 s when a TIP is detected**, then retarget the new up cell. Never hard-code "N balls tips it". |
| LEAVE / PARK | LEAVE = not touching the perimeter wall at **end of AUTO** (3); PARK = at least partially in own LOADING ZONE (5 AUTO, 5 TELEOP) [MAN p90-91]. Q&A 21: touching the wall at the end of AUTO means no LEAVE, even when parked [QA]. SWARM RP needs alliance LEAVE+PARK ≥ 16 (regular events) [MAN p91]. | **AUTO park pose = partly inside the 11-in-deep LOADING ZONE but 2–4 in off the wall** [I]. Needs real localization. |
| Timing / cues | AUTO 0:30, transition 8 s, TELEOP 2:00; audio: chime at 1:00 ("FLOWER Ownership Unlocked"), train whistle at 0:20 [MAN p79, p85; TU04]. | Gamepad rumble/LED cues from a TeleOp timer started at START (§8.5). |

---

## 2. FTC SDK v12.0 and toolchain

### 2.1 Release facts [V: SDK-REL, SDK-README]

| Version (build) | Highlights relevant to 12303 |
|---|---|
| **12.0 (20260907)** — official 2026/27, GitHub release 12 Sep | **Breaking: AprilTag Clusters.** `getDetections()` now returns `AprilTagSingleDetection` or `AprilTagClusterDetection` (both subclasses of `AprilTagDetection`); old code must `instanceof` and cast. Clusters are co-planar tag groups with a common origin that "may be placed outside the bounds of the tags … to provide a more suitable 'aiming' target"; full 6-DOF pose from even one visible member. **`getCurrentGameTagLibrary()` returns BIOBUZZ tags; "the Origin of each cluster is located in the center of the Cell opening for easy aiming"; "since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization."** Axes drawn on the preview by default; DS gamepad indicator is orange when a pad uses Android's generic driver instead of the DS USB driver; robot-config tree view; OctoQuad MK2 FW 3.1.0; battery voltage now updates on the DS even without telemetry. Min Android Studio: **Narwhal 3 Feature Drop**. |
| 11.2 / 11.2.1 (Jul 2026) | Gradle 9.1 / AGP 8.13.2 (requires Narwhal 3 FD). New **`@Utility` OpMode type**; **TestHardware** (servos, CR servos, motors, color/distance/touch sensors, IMUs, webcams, analog) and **TestGamepad** utilities; `PwmControl.setPulseWidth/getPulseWidth` (µs); UVC "quirks"; DS INIT button turns teal when ready; fix: repeated `setPosition` after direction/scale change now updates. |
| 11.1 (Dec 2025) | Gamepad triggers as booleans with edge detection; **GoBildaPinpointDriver supports Pinpoint v2**; goBILDA USB camera calibration. |
| 11.0 (Aug 2025) | OnBot Java "projects"; AndyMark ToF/IMU/color sensors; color processing (circleFit, erode/dilate order); DS play button turns green when connected + gamepad + timer. |
| 10.3 (Jun 2025) | **Full Range Servo** config type (500–2500 µs vs legacy 600–2400 µs); **gamepad edge detection** (`aWasPressed()` …); **`blackboard`** (persists between OpModes, not across restarts); **goBILDA Pinpoint support** + `SensorGoBildaPinpoint`; `Limelight3A.getLatestResult()` now never null — check `isValid()`. |
| 10.2 (Jan 2025) | Upload Limelight 3A pipelines from code (version-control them). |
| 10.0 (Aug 2024) | **Limelight 3A support**; TensorFlow removed; REV 9-axis IMU; telemetry formatting. |
| 10.1.1 note | OnBot Java supports only **Java 1.8**, so the SDK keeps `sourceCompatibility` 1.8 (Android Studio's bundled JDK 21 warns). |

Other verified SDK 12.0.0 API details:
- **Gamepad** [SDK-RC-AAR]: `rumble(ms)`, `rumble(l,r,ms)`, `rumbleBlips(n)`, `runRumbleEffect`, `setLedColor(r,g,b,ms)`, `runLedEffect`, edge detection `xWasPressed()/xWasReleased()` for every button, `leftTriggerWasPressed()`, `left_trigger_pressed`.
- **OpMode** [SDK-RC-AAR]: static `blackboard` HashMap.
- **@Autonomous** [SDK-RC-AAR]: `name`, `group`, `preselectTeleOp`.
- **DcMotorEx** [SDK-RC-AAR]: `setVelocity`, `setVelocityPIDFCoefficients(p,i,d,f)`, `getCurrent(CurrentUnit)`, `setCurrentAlert`, `isOverCurrent`.
- **LynxModule** [SDK-HW-AAR]: bulk caching, `getCurrent`, `getInputVoltage`.

SDK sample OpModes to start from [SDK-SAMPLES]: `SensorLimelight3A`, `SensorGoBildaPinpoint`, `SensorSparkFunOTOS`, `ConceptAprilTag` (cluster-aware in v12), `ConceptAprilTagLocalization`, `ConceptBlackboard`, `ConceptGamepadRumble`, `ConceptGamepadEdgeDetection`, `ConceptMotorBulkRead`, `RobotTeleopMecanumFieldRelativeDrive`, `RobotAutoDriveToAprilTagOmni`, `ConceptExternalHardwareClass` + `RobotHardware`, `ConceptVisionColorLocator_Circle`, `ConceptVisionColorSensor`, `SensorColor`.

### 2.2 Blocks vs OnBot Java vs Android Studio

| | Blocks | OnBot Java | Android Studio |
|---|---|---|---|
| Install | none (browser on RC Wi-Fi) | none (browser) | Android Studio Narwhal 3 FD+ and the FtcRobotController repo [SDK-README] |
| Language | visual (generates Java) | Java 1.8 only [SDK-README 10.1.1] | Java (or Kotlin) |
| Third-party libs (Pedro, Panels, SolversLib) | effectively no; only libraries annotated `@ExportToBlocks` | can upload .jar/.aar since SDK 7.0, but **.aar files with assets are not supported** and transitive deps must be hand-managed [FTCDOCS-EXTLIB] | Gradle dependencies (one line each) |
| Git / code review / refactoring / unit tests | no / no / no / no | no / weak / no / no | yes / yes / yes / yes (JVM unit tests for pure logic) [I] |
| Debugging | telemetry | telemetry | telemetry, logcat, breakpoints over adb |
| Fit for 12303 | Good for a first "spin a motor" lesson | Stopgap only | **Recommended.** The coach's SE skills pay off; students edit well-scaffolded classes [I]. |

Practical setup [I]:
- One GitHub repo forked from FtcRobotController v12.0, with feature branches per student.
- The coach reviews PRs.
- Deploy over Wi-Fi `adb` at home and over USB-C at events (R704).
- Keep one laptop as the "competition laptop" with the known-good build.

### 2.3 Pinned Gradle dependencies (TeamCode `build.dependencies.gradle`)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://repo.dairy.foundation/releases/' }   // Pedro, Panels, SolversLib, NextFTC
    google()
}
dependencies {
    implementation 'org.firstinspires.ftc:Inspection:12.0.0'
    implementation 'org.firstinspires.ftc:Blocks:12.0.0'
    implementation 'org.firstinspires.ftc:RobotCore:12.0.0'
    implementation 'org.firstinspires.ftc:RobotServer:12.0.0'
    implementation 'org.firstinspires.ftc:OnBotJava:12.0.0'
    implementation 'org.firstinspires.ftc:Hardware:12.0.0'
    implementation 'org.firstinspires.ftc:FtcCommon:12.0.0'
    implementation 'org.firstinspires.ftc:Vision:12.0.0'
    implementation 'androidx.appcompat:appcompat:1.2.0'

    implementation 'com.pedropathing:revhub:3.0.1'     // quickstart pin; library tag v3.0.2 released 2026-10-16 (check Maven for revhub 3.0.2)
    implementation 'com.pedropathing:tuning:1.0.1'     // AutoTune procedures
    // optional, practice only (must be DISABLED at events, R704.D):
    // implementation 'com.bylazar:fullpanels:1.0.19'  // site lists v1.0.19; example repo uses 1.0.16
    // optional frameworks:
    // implementation 'com.pedropathing.ivy:pedro:1.1.1'
    // implementation 'org.solverslib:core:0.3.6'       // 0.3.6+ targets Pedro 3.0.0+
    testImplementation 'junit:junit:4.13.2'             // JVM unit tests for pure math [I]
}
```
Sources: the Pedro quickstart's `build.dependencies.gradle` pins SDK 12.0.0, `revhub:3.0.1` and `tuning:1.0.1` [PEDRO-QS]. Ivy [IVY], SolversLib [SOLVERS] and Panels [PANELS] coordinates come from their docs.

---

## 3. BIOBUZZ AprilTags and the HIVE: what software can and cannot assume

### 3.1 Tag facts
- Tags are 36h11, **3.25 in (8.25 cm)**, arranged as **4-tag clusters on one sticker on the bottom face of each CELL**, "facing downward towards the TILES with its bottom edge oriented towards the center of the FIELD" [MAN p74-75].
- IDs (confirmed visually in the PDF, p76-77) [MAN]:

| CELL | IDs | SDK cluster name | Start state |
|---|---|---|---|
| Red, far side (opposite audience) | 30, 31, 32, 33 | "RED SCORING" | down |
| Red, audience side | 34, 35, 36, 37 | "RED AUDIENCE" | **up** (holds 3 red NECTAR) |
| Blue, audience side | 38, 39, 40, 41 | "BLUE AUDIENCE" | down |
| Blue, far side | 42, 43, 44, 45 | "BLUE SCORING" | **up** (holds 3 blue NECTAR) |

- **What the SDK 12.0.0 library actually contains** [V: bytecode of `AprilTagGameDatabase.getBioBuzzTagLibrary()` / `getBioBuzzCluster(int)` in `org.firstinspires.ftc:Vision:12.0.0`]:
  - It holds four `AprilTagClusterMetadata` objects named exactly as above. Each has **field position (0,0,0) and an identity orientation**, so the library deliberately gives **no field pose**.
  - Members `base+0..base+3` sit at cluster-plane positions **x = −6.5, −2.75, +2.75, +6.5 in; y = +7.1874 in; z = −5.622 in**, with tagsize 3.25 in.
  - So the cluster origin (the opening center) is offset about 7.19 in along the cell depth and about 5.62 in off the tag plane from the tag row center, roughly 9.1 in away. This agrees with Fig 9-15: the tags are 2.75 in from the reference holes, which are 9.938 in from the cell front, and 9.938 − 2.75 = 7.19 [MAN p75].
- **Cluster detection API** [V: SDK-VISION-AAR, FTCDOCS-CLUSTER]:
  - `AprilTagClusterDetection` has `percentClusterFound` (int) and `metadata` (with `name`, `shortName`, `clusterMembers`, `containsTagId()`). It inherits `ftcPose` (x, y, z, yaw, pitch, roll, range, bearing, elevation; inches and degrees) and `frameAcquisitionNanoTime`.
  - ftc-docs gives a targeting recipe: alliance = `metadata.name` starts with "R"/"B"; **scorable if `|ftcPose.roll| < 90`**. Their example: "RED SCORING" roll 159.3° at 50% found, so the unscorable far cell; "RED AUDIENCE" roll −19.7° at 100%, so the target.
- **Limelight 3A does not know about clusters.** It reports each fiducial separately. The FTC wrapper `LLResultTypes.FiducialResult` exposes `getFiducialId()`, `getTargetXDegrees()/getTargetYDegrees()`, `getTargetPoseCameraSpace()`, `getTargetPoseRobotSpace()`, `getCameraPoseTargetSpace()`, `getRobotPoseTargetSpace()`, `getRobotPoseFieldSpace()` and `getTargetArea()`. Poses are built with **DistanceUnit.METER and AngleUnit.DEGREES**, with arrays mapped [x,y,z,roll,pitch,yaw] (JSON keys `t6t_cs`, `t6t_rs`, `t6c_ts`, `t6r_ts`, `t6r_fs`) [V: SDK-HW-AAR bytecode]. Limelight camera space is X right, Y down, Z out of the lens; target space is X right (facing the tag), Y down, Z out of the tag [LL-COORD]. The cluster math has to be done in Java (§9.4).

### 3.2 Field frame and alliance symmetry
- FTC field coordinates: origin at field center on the tile surface, viewed from the Red Wall. **+X runs parallel to the Red Wall to the right, +Y points away from the Red Wall (toward blue), +Z is up.** For a field where "the Red Wall is on the audience's left" (DECODE "inverted"), "+X points toward the audience" [FTCDOCS-FIELD].
- BIOBUZZ also puts red on the audience's left [MAN p68], so **+X is toward the audience and +Y toward blue** [I: same layout as DECODE; confirm when FIRST publishes BIOBUZZ docs]. Heading 0 means facing the audience, CCW positive. Limelight agrees: "0° yaw means the blue alliance is on the robot's left" [LL-FTC].
- **The BIOBUZZ field is 180°-rotationally symmetric, not mirror symmetric** [V by figure; MAN p66, p77, p83]:
  - red LOADING ZONE at the far end of the red wall ↔ blue at the audience end of the blue wall;
  - red GARDEN in the audience corner ↔ blue GARDEN in the far corner;
  - the red audience cell starts up ↔ the blue far cell starts up;
  - FLOWERS map onto each other under a 180° rotation.
- **Blue poses = red poses rotated 180° about the field center:** (x, y, θ) → (−x, −y, θ+π) in FTC coordinates, or (144−x, 144−y, θ+π) in Pedro's corner-origin frame. Pedro's `PoseFactory.rotateAround(cx, cy, angle)` does this [PEDRO-SRC]. **Do not mirror.**
- Tag IDs map the same way: red audience 34–37 ↔ blue far 42–45, and red far 30–33 ↔ blue audience 38–41.
- Measure your practice field's real size and use its true center: tiles are nominally 24 in, and Pedro 2.0.6 changed mirroring to 141.5 in [PEDRO-REL].

### 3.3 Estimated geometry for software constants (FTC frame, inches) [E]
I measured these from Fig 9-10 (side view, scale checked: pivot 43.5 in vs 43.95 in published) and Fig 9-17 (top view on a 12-in grid). **Measure them on a real field before trusting them.**

| Item | Estimate | Basis |
|---|---|---|
| HIVE pivots | x ≈ 0; red HIVE y ≈ −12.75, blue y ≈ +12.75 | "HIVE Center to Center 25.5 in" [MAN p71] |
| Up-cell **opening center** | about 16 in horizontally from the pivot toward that cell's end wall, about **59.5 in** high. Red audience-cell target ≈ (+16, −12.75, 59.5); red far-cell target ≈ (−16, −12.75, 59.5); blue ≈ (±16, +12.75, 59.5) | Fig 9-10: opening spans 53.5–65.6 in [MAN p71] |
| Up-cell opening orientation | faces **outward** (toward the nearer end wall) and about 30° above horizontal; the window is inclined about 60° | Fig 9-10 measurement; consistent with the NECTAR staging note "contacting the back wall" [MAN p84] |
| Up-cell **tag row** | about 50 in high, about 12–13 in from the pivot; tag normal points down and outward | SDK member offsets + Fig 9-10 |
| Down-cell lowest point | 30.6 in | TU01 correction [MAN p71] |
| Tag order along field Y (from above) | audience cells: base+0 → base+3 run −Y → +Y (34→37, 38→41); far cells: reversed (33…30 and 45…42 run −Y → +Y) | Fig 9-17 labels; SDK x offsets [MAN p77; SDK-VISION-AAR]. **Verify the sign.** |
| Red LOADING ZONE | x ∈ [−46.5, −24.4], y ∈ [−72, −61] | Fig 9-17 |
| Blue LOADING ZONE | x ∈ [+24.3, +46.5], y ∈ [+61, +72] | Fig 9-17 |
| Red GARDEN | along the audience wall, x ≈ +70…+72, y ∈ [−72, −48] | Fig 9-17; "23 × 2 in" [MAN p66] |
| Blue GARDEN | along the far wall, x ≈ −72…−70, y ∈ [+48, +72] | Fig 9-17 |
| FLOWERS (wall-mounted) | red half: (+23, −69) on the red wall and (−69, −24) on the far wall; blue half: (−24, +69) on the blue wall and (+68, +23) on the audience wall | Fig 9-17 |
| Tile columns (G402 sides) | A: y∈[−72,−48], B: [−48,−24], C: [−24,0], D: [0,24], E: [24,48], F: [48,72]; row 1 at the audience wall | Fig 9-5 [MAN p67] |

**Implication [I]:** the up-cell window faces outward, so a red robot scores the red audience cell from the red-half band between the HIVE and the audience wall: roughly x ∈ [+28, +66], y ∈ [−60, −3]. Shots are short, about 2–5 ft horizontal to the opening. The red GARDEN pollen sits in that same corner. After a TIP, the up cell is at the **other end** of the HIVE, so the robot must move to the far-side band. Strategy agents should confirm this with the CAD.

### 3.4 Moving tags mean no static-map localization
- The SDK authors say outright that these tags are unsuitable for absolute localization [SDK-README v12.0].
- The Limelight's MegaTag uses one uploaded field map [LL-FTC]. That map is only right for one HIVE state, the HIVE moves during a tip, and each HIVE flips independently.
- **What remains possible:**
  1. **Relative aiming (recommended):** the tag observation gives the target **relative to the robot or turret** directly. No field map is needed.
  2. **State-aware relocalization (advanced, optional):** if the robot knows both HIVE states (vision + bookkeeping), it can compute the current tag field poses from the CAD, fuse a robot-pose measurement into Pedro 3's `FusionLocalizer.addMeasurement(pose, timestamp, variance)`, a timestamped Kalman update with history buffer and NaN-for-unknown axes [PEDRO-SRC], and reject measurements while a HIVE is moving.
     - The `Limelight3A.uploadFieldmap(LLFieldMap, Integer)` API exists [SDK-HW-AAR], so a state-specific map could be uploaded after each tip. That is an HTTP call: do it off the main loop and only when stationary [I].
     - **Not recommended for the first competition.**

### 3.5 Which cell is up? Combine three signals [I]
1. **Height test (most robust):** compute the observed tags' height above the floor from the camera pose. Up-cell tags are about 50 in high [E]; down-cell tags are much lower (the down cell bottoms out at 30.6 in [MAN p71]). Threshold about 44 in; calibrate on a field.
2. **SDK roll test:** `|ftcPose.roll| < 90` [FTCDOCS-CLUSTER]. Caution: I could not confirm it holds from every viewpoint. Viewing a cell from its inner side may flip the apparent roll, because each sticker's bottom edge faces the field center. Use it as a secondary check.
3. **Bookkeeping + driver override:** start state is known (red audience up, blue far up). Toggle on a detected TIP (a cluster's height or roll changes for more than about 0.5 s). Give the driver a "HIVE tipped" toggle and dpad "target audience/far" overrides, because the alliance partner can tip your HIVE too.

### 3.6 Camera choice and mounting

| Option | Pros | Cons |
|---|---|---|
| **Limelight 3A on turret** (recommended if owned) | AprilTag pipeline at **50 fps at 640×480** (20 fps at 1280×960); color pipelines at 90 fps; offloads the Control Hub CPU; FOV 54.5° × 42°; USB-C power and data into a **Control Hub USB 3.0 (blue)** port; boots in about 15–20 s [LL-3A] | Per-tag results only, so you write the cluster math; one pipeline at a time (switching takes a frame or two [TRC]); turret cable wrap |
| UVC webcam + SDK `AprilTagProcessor` | **Native BIOBUZZ clusters** (opening-center pose, `percentClusterFound`, roll); calibrated intrinsics for common cams (C270/C920, goBILDA USB camera since 11.1) [SDK-README] | Runs on the Control Hub CPU; decimation trades range for rate. SDK note: decimation 3 detects a 5" tag at 10 ft at 30 fps; decimation 2 detects a 2" tag at 6 ft at 22 fps [SDK-SAMPLES ConceptAprilTag]. |
| Second, front-facing UVC webcam (optional) | SDK `ColorBlobLocatorProcessor` with built-in `ColorRange.YELLOW/RED/BLUE` and circle fit [SDK-VISION-AAR; SDK-README 11.0] for pollen chasing and **seeing opponent NECTAR before intake** | Extra CPU load |

**Mounting [E/I]:**
- **Pitch the camera about 35–40° up.** The up-cell tags are about 50 in high; from a lens about 16 in high they sit 35–60° above horizontal at 2–4 ft horizontal range.
- The tags face down and outward, so from the shooting side you see them nearly face-on.
- In the pipeline, set marker size **82.55 mm**, enable "Full 3D" and set **ID filter = own 8 IDs** [LL-TAGS, LL-FTC].
- Mount rigidly and re-check exposure at every venue [TRC].
- The TRC BIOBUZZ guide is a useful Limelight walkthrough, but it is wrong about game pieces: it calls red and blue balls "pollen", when they are NECTAR [MAN p74].

---

## 4. Localization and path following

### 4.1 Localization hardware

| Option | Cost | Notes |
|---|---|---|
| **goBILDA Pinpoint V2** (3110-0002-0002) + 2 pods | $79.99 + pods | About 1500 Hz fusion of 2 dead wheels + IMU over I²C; max 256,000 counts/s. The SDK driver supports v2 since 11.1. goBILDA pods are 2000 CPR (SDK constants 13.2629 ticks/mm swingarm 48 mm, 19.8944 ticks/mm 4-bar 32 mm). The original v1 SKU is listed as discontinued. [GB-PINPOINT, SDK-README, SDK-HW-AAR] |
| SWYFT Linear Odometry Module ×2 | $64.99 each | 4096 PPR magnetic encoder, 38 mm omni, JST-PH, "Works directly with the goBILDA Pinpoint" [SWYFT-ODO]. **Check whether 4096 is counts or pulses:** at 16,384 counts/rev the Pinpoint's 256k events/s limit is hit at about 2 m/s [I]. |
| SparkFun OTOS | $84.95 | Optical flow + IMU; no pods; explicitly allowed (R702 Ex. 2) [OTOS, MAN p140-141]; 10 mm working height over tiles. Supported by the SDK, Pedro 3 and RR. |
| Drive encoders + IMU only | $0 | SWYFT Drive V2: 86 mm wheels, 12.7:1, Spike motor 28 PPR, so about 356 counts per wheel rev, or **about 33 counts/in** [I from SWYFT-DRIVE]. Mecanum slip and bumps cause drift. Pedro 3 has **no** drive-encoder localizer class [PEDRO-SRC]; RR 1.0 does (default, IMU heading) [RR]. |

### 4.2 Path-following libraries

| | **Pedro Pathing 3.0.2** | Pedro Pathing 2.1.2 | Road Runner 1.0.1 | Hand-rolled drive-to-pose |
|---|---|---|---|---|
| Status | 3.0.0 on 2026-09-10 ("major update and full rewrite"); 3.0.1 on 09-18; **3.0.2 on 10-16**, fixing controller derivative, integral, piecewise heading, FusionLocalizer, OctoQuad offsets [PEDRO-REL] | last 2.x on 2026-05-07; 2.1.0 added "Predictive Braking" auto-tuned algorithm [PEDRO-REL] | core/actions 1.0.1 (Jan 2025); quickstart on SDK 11.0, last push Oct 2025 [RR] | n/a |
| Tuning | **AutoTune**, "a robot-hosted webpage" with procedures: Mecanum (motor names/directions), localizer (Pinpoint: pod type → push forward → push left → offsets), **Foresight**, Tests (Line, Curve, Interpolation, Hold, Localization, Odometry, Pose) [PEDRO-QS procedures, PEDRO-DOCS] | Web docs + tuning OpModes; predictive braking auto-tuner + manual heading PIDF [PEDRO-REL] | Push tests, ramp loggers, manual FF and feedback tuners, SplineTest; **requires FTC Dashboard** for graphs and config [RR] | Tune 3 PIDs by hand |
| Localizers | Pinpoint, OTOS, OctoQuad, 2-wheel, 3-wheel, 3-wheel+IMU, `FusionLocalizer` for vision [PEDRO-SRC] | same plus older options | drive encoders (default), 2/3 dead wheel, Pinpoint, OTOS [RR] | whatever you write |
| Path API | `line(a,b)`, `curve(...)`, `through(...)`, `path(p1,p2)`; heading `.constant()`, `.linear(a,b)`, `.tangent()`, `.facingPoint(p)`, piecewise; `follower.follow()/isBusy()/hold()/pose()`; `PoseFactory.degrees().of(x,y,deg)` with `rotateAround/mirrorX`; `ManualDrive.fieldCentric(…, offsetHeading)`, `headingLock`, `driveOrHold` [PEDRO-SRC] | PathBuilder / BezierLine / BezierCurve | Actions + trajectory builder (Kotlin core) | n/a |
| Visualizer | visualizer.pedropathing.com (path generator) [PEDRO-REL] | same | MeepMeep | none |
| Team fit | **Recommended**: easiest tuning, active maintenance; only 6 weeks old, so pin versions | Safe fallback with most tutorials | Viable but more tuning; dashboard-centric | Only if no odometry |

### 4.3 Pedro 3 bring-up sequence [V/I]
1. Clone `Pedro-Pathing/Quickstart` (SDK 12.0.0 preconfigured), or copy its `pedro/` package into your repo [PEDRO-DOCS installation, PEDRO-QS].
2. Mount the Pinpoint and pods (or OTOS) rigidly, with springs giving consistent pod down-force. Name it `pinpoint` in the robot config.
3. Run AutoTune **Mecanum Tuner**. It spins each motor and asks direction, then emits a `MecanumConfig` with names and directions [PEDRO-QS MecanumTuner].
4. Run the **Pinpoint Tuner**. You choose the pod type, push forward, push left, then it determines offsets and emits a `PinpointConfig` (`podType`, `xPodOffset`, `yPodOffset`, directions, `DistanceUnit.INCH`) [PEDRO-QS PinpointTuner]. Keep `resetMode` in mind: `RECALIBRATE_IMU` is the default and `NONE` is available, which matters for the TeleOp hand-off [PEDRO-SRC PinpointConfig].
5. Run the **Foresight Tuner**, which produces the `ForesightConfig` (brake coefficients, max achievable forward/strafe velocity, natural deceleration…) [PEDRO-SRC].
6. Run the **Tests**: Line, Curve, Hold, Localization. Verify on tape-measured marks: 48 in forward, 48 in strafe, 10 spins in place.
7. Build `Constants.create(hw)` as `new Follower(new PinpointLocalizer(hw, pinpointCfg), new Mecanum(hw, mecanumCfg), new Foresight(foresightCfg))` [PEDRO-SRC Follower/PinpointLocalizer/Mecanum/Foresight constructors]. Follow the generated code exactly; the quickstart comment shows a different argument order.
8. Write autos with `PoseFactory` and a single 180° rotation for blue.

**Time estimate [I]:** about 1 evening to mount and wire, 1–2 sessions of AutoTune, and 1 session to verify. A reliable preload + collect + park auto is realistic in **1–2 weeks** with the coach debugging. Without odometry hardware, expect weeks of fiddling and still-unreliable LEAVE/PARK precision.

### 4.4 Road Runner 1.0 (alternative)
Tuning order [RR]:
1. Direction debuggers
2. (OTOS scalars)
3. ForwardPushTest
4. LateralPushTest (drive encoders)
5. Forward/Lateral/AngularRampLogger
6. ManualFeedforwardTuner
7. ManualFeedbackTuner
8. SplineTest

Choose RR only if the team insists on drive-encoder localization: it has a supported drive-encoder + IMU localizer. It needs FTC Dashboard during tuning, which is fine at home.

### 4.5 No-odometry fallback [I]
A "drive-to-pose" P-controller: drive-encoder deltas + IMU heading → (x, y, θ); command field-relative velocity = clamp(kP·error); finish when within 1 in and 2° for 150 ms; low max speed. Adequate for "shoot preloads + park" if moves are short and slow. It is **not adequate** for TeleOp field-relative aiming, which needs a vision-only aim in that case.

### 4.6 Relocalization at known features [I]
Cheap and robust ways to correct odometry:
- **Wall bump + distance sensor:** when strafed against a wall, reset x or y.
- **AUTO start pose:** exact via a start-alignment jig, which G302 allows: "It is not a violation … to bring an alignment device to the FIELD to aid pre-MATCH ROBOT set-up" [MAN p103].
- Prefer these over tag-based relocalization (§3.4).

---

## 5. Turret auto-aim

### 5.1 Hardware reality check (from photos) [I]
- The top photo shows a large silver **ring gear driven by a ~60-tooth pinion on a grey servo**, so the turret is servo-driven.
- The orange C-hood carries a single ~72 mm wheel (marked "Ø72mm-50A") driven by a motor, probably the 6000 rpm Yellow Jacket. The coach described that motor as "for turret", meaning **the flywheel on the turret**.
- **Confirm with the coach.** If they intended the 6000 rpm 1:1 motor to *rotate* the turret, it needs a large reduction.

goBILDA Dual Mode Servo facts [GB-SERVO]:
- 300° in standard mode, continuous rotation after reprogramming with the 3102 programmer.
- 25-2 (torque): 40–60 rpm; 25-3 (speed): 90–145 rpm; 25-4 also exists.
- 4 µs deadband; 5 kΩ internal pot; CR PWM range 900–2100 µs (25-2).
- REV hubs supply 5 V to servos, so speed is near the low end of the range [MAN p133].
- **Travel:** turret travel = 300° / (ring teeth ÷ pinion teeth). At a 4:1 ratio that is only about **75° total (±37°)**.
- **Slew:** a 25-3 at about 90 rpm through 4:1 gives about 135°/s. A 25-2 at about 40 rpm gives about 60°/s, which is slow for tracking while driving.
- Configure the port as **"Full Range Servo"** (500–2500 µs, SDK 10.3) or call `ServoImplEx.setPwmRange(new PwmRange(500,2500))` to get the full 300° [SDK-README].

Three ways to proceed [I]:
- **(a) Keep positional servo mode (simplest).**
  - The angle comes from the commanded position; no encoder is needed.
  - Soft limits are a clamp.
  - Add "chassis assist": rotate the robot when the target is outside turret range.
- **(b) CR mode + REV Through Bore Encoder** on the pinion or turret axis, into a free motor-encoder port. Use **port 0 or 3**, which decode in hardware, for high-CPR encoders [GM0-MOTORS]. Run a software PID.
- **(c) A servo with feedback (Axon MAX+/MINI+ 4th wire)**, which R502 explicitly allows [MAN p133], or a geared goBILDA motor with an encoder.

### 5.2 Aiming approaches

| Approach | How | Pros | Cons |
|---|---|---|---|
| **Vision-only** | Turret error = horizontal bearing to the cluster center (Limelight per-tag math, or SDK cluster `ftcPose.bearing`); P/PD on turret angle | No odometry needed; self-corrects field tolerance | Loses lock when tags leave view; latency oscillation if done naively; nothing to aim at when the turret faces away |
| **Field-relative (odometry)** | `turretDeg = atan2(ty−ry, tx−rx) − heading − mountOffset` using the §3.3 target | Works any time, including pre-aiming while driving; smooth | Target constants and odometry drift errors (Pinpoint drift is small over 2.5 min) |
| **Fused (recommended)** | Field-relative command + `visionBias` = low-pass(measured bearing − predicted bearing), with outlier rejection; bias decays slowly when vision is lost | Best of both; robust to brief occlusion and partner robots | Slightly more code (§9.6) |

### 5.3 Latency compensation [I]
- Limelight latency is capture + targeting, plus the `getStaleness()` age [LL-FTC; SDK-HW-AAR `getCaptureLatency/getTargetingLatency/getStaleness`]; SDK detections carry `frameAcquisitionNanoTime`.
- For a turret-mounted camera, turn the measured bearing into an **absolute** setpoint: `setpoint = turretAngleAtCapture + bearingInCameraFrame`. Use a short ring buffer of (time, turret angle) for the servo's estimated angle or the encoder angle. This removes the classic "chase the stale tx" oscillation.
- With a positional servo, estimate the actual angle with a slew-rate model of the command (§9.8).

### 5.4 Target switching after a TIP, and pausing during a TIP [V/I]
- The scorable cell flips to the other end of the HIVE after each TIP. Its window faces outward [E], so the robot must reposition.
- Logic:
  - `targetCell = upCell(alliance)` from §3.5.
  - On a TIP event: **stop feeding for about 1.0 s** (10.5.1 note [MAN p87]), increment `tipCount`, swap `targetCell`, rumble the driver (two blips), and have the dashboard telemetry show "GO FAR SIDE" or "GO AUDIENCE SIDE".
  - In AUTO, branch to the next shooting pose on the other side.
- Target filter: own alliance only. Shooting at the opponent HIVE to impede it is STRATEGIC (G417.D) [MAN p112].

### 5.5 Lead compensation (shoot on the move) [I]
- Iterate 2–3 times: `t = TOF(dist)`, `virtualTarget = target − v_robot·t`, recompute `dist` and the bearing; use `rpm = table(distVirtual)`.
- Add turret feed-forward `−ω_chassis` so the turret counter-rotates while the chassis turns.
- **My ballistic estimate puts TOF at about 0.3–0.7 s** (§6.5). At 0.5 m/s chassis speed that is 6–14 in of lead, comparable to the 20-in-wide window, so compensation is mandatory when moving.
- **Recommendation for this team:** stop to shoot. Gate the feeder on chassis speed < about 5 in/s and turret error < about 2°. Use Pedro `driveOrHold`/`hold` to resist pushing while shooting [PEDRO-SRC].
- Reference discussion of the math: [CD-SOTM].

### 5.6 Soft limits, cable wrap, chassis assist [I]
- Clamp the commanded turret angle to [MIN, MAX], measured with a margin of 3–5° inside the hard stops.
- With continuous rotation and a sensor, choose the equivalent angle (±360°) closest to the current angle **that stays inside limits**. Never cross the cable-wrap seam while tracking; unwind only when no shot is pending.
- If the desired angle is outside range, set `needsChassisTurn` and either rotate the chassis automatically (TeleOp heading-assist via Pedro `headingLock`) or rumble the driver.
- Leave a strain-relieved service loop for the Limelight's USB cable on a turret mount. The TRC guide recommends *not* mounting the camera on a turret, because wobble shows up as jumpy tx [TRC]. If the turret has backlash, consider a chassis-mounted camera plus the field-relative or fused approach.

### 5.7 Turret tuning procedure [I]
1. Calibrate the angle mapping:
   - Servo: command 0.3 and 0.7, measure the turret angles, and solve `POS_AT_0`, `POS_PER_DEG`.
   - Encoder: measure ticks per turret revolution.
2. Measure the slew rate (deg/s): high-speed video or encoder.
3. Find and set the soft limits.
4. Run vision-only with the robot static at 3 distances: the bearing error should be < 1°. Adjust the `TURRET_ZERO_OFFSET` trim.
5. Enable field-relative aim and drive a lap. The vision bias should stay small, under 3°; if it is larger, fix the target constants or the odometry.

---

## 6. Flywheel control

### 6.1 Facts
- goBILDA 5203 Yellow Jacket 1:1 (5203-2402-0001): 6000 rpm rated (5400 rpm tested for the 5202 variant), stall 1.47 kg·cm / 9.2 A, **28 PPR at the output shaft** [GB-6000].
- At 6000 rpm that is 2800 counts/s.
- The REV hub calculates velocity from a 5-value ring buffer updated every 10 ms [GM0-MOTORS]. That implies a ~50 ms window, so a 28-CPR flywheel reads in steps of about 20 ticks/s, roughly 43 rpm or 1–2% at shooting speeds [I].
- **Do not put the flywheel encoder on a port that must also serve a high-CPR dead wheel.** Encoder ports 1 and 2 are software-decoded [GM0-MOTORS].
- PIDF coefficients are per motor port and RunMode, and **do not persist across hub power cycles**: set them in code every init [FTCDOCS-PIDF].

### 6.2 Option A: built-in hub velocity loop (start here)
- Measure `maxTPS`: RUN_WITHOUT_ENCODER at power 1.0 on a full battery, recording the maximum `getVelocity()`.
- Set `F = 32767/maxTPS; P = 0.1F; I = 0.1P; D = 0`, then `motor.setVelocityPIDFCoefficients(P,I,D,F)` and `motor.setVelocity(targetTPS)` [REV-PIDF].
- Pros: runs in hub firmware, independent of loop time. Cons: you cannot add your own feed-forward terms or bang-bang recovery.

### 6.3 Option B: custom feed-forward + feedback with voltage compensation (upgrade path)
- `power = (kS + kV·target)·(12.0/V_batt) + kP·(target − measured)`, clipped to [0, 1]. Never reverse the flywheel.
- Optionally use full power when error is more than 8% below target (bang-bang recovery after a shot).
- Identify kS and kV by stepping power 0.3 → 1.0 and recording steady velocity at about 12.5 V; fit `power = kS + kV·v`.
- Read the battery voltage at most every 250 ms (it is not part of the bulk read) and low-pass filter it.
- Loop rate matters: aim for 10 ms or less with MANUAL bulk caching [SDK-SAMPLES ConceptMotorBulkRead].

### 6.4 At-speed, recovery, shot detection [I]
- `ready = |error| < max(40 tps, 2% of target)` for **3 or more consecutive loops** (about 30–50 ms), plus turret on target, plus chassis slow.
- Wait for `ready` again before the next feed. A typical single-wheel recovery is 150–400 ms; measure it.
- A **shot shows up as a velocity dip** of more than about 4–6% within about 100 ms of a feed. Use it to decrement the ball count and confirm shots (more reliable than timers).

### 6.5 Distance → RPM (+hood) table; ballistic sanity check
Use an **interpolating lookup table** (`TreeMap` with linear interpolation, clamped at the ends; §9.6) keyed on the horizontal distance to the target (or to the tag row, whose offset the table absorbs). Values are `{rpm, hoodPos, tofSec}`. Collect 6–8 points every 6 in across the usable range. If the hood servo is fixed, keep a single angle and vary rpm only.

**Idealized ballistics for picking test ranges [I, my calculation, script `scripts/r6_ballistics.py`]:** launch height 16 in, target 59.5 in, no drag or spin, ~72 mm wheel, ball exit speed ≈ 0.5 × wheel surface speed (rule of thumb).

| Launch angle | Horiz. dist | Exit speed | TOF | Est. wheel rpm |
|---|---|---|---|---|
| 55° | 48 in | 5.9 m/s | 0.36 s | ~3100 |
| 55° | 60 in | 5.7 m/s | 0.47 s | ~3000 |
| 60° | 36 in | 5.9 m/s | 0.31 s | ~3100 |
| 60° | 48 in | 5.4 m/s | 0.45 s | ~2850 |
| 60° | 72 in | 5.6 m/s | 0.65 s | ~3000 |
| 65° | 36 in | 5.2 m/s | 0.42 s | ~2750 |
| 50° | 72 in | 6.1 m/s | 0.47 s | ~3200 |

Takeaways [I]:
- The required wheel speeds of about 2700–3300 rpm leave **lots of headroom** on a 6000 rpm motor for fast recovery.
- Shots under about 3 ft need launch angles ≥ 60°; 45° cannot reach the opening at 24–36 in.
- TOF is 0.3–0.7 s (§5.5).
- Re-derive with real launch height, hood angle and target once known.

### 6.6 Data-collection OpMode [I]
`ShotTableCollector`:
- dpad up/down: ±50 rpm; dpad left/right: ±0.02 hood.
- A: fire one; X/B: log hit or miss with current vision distance, rpm, hood, battery voltage.
- Write a CSV to `/sdcard/FIRST/data/shots.csv` (on-robot file, R704-safe).
- Fit table points as the middle of the hit band at each distance. Re-verify at 12.0 V and 13.5 V battery.

---

## 7. Intake and indexer logic

### 7.1 Rules recap [V]
- At most 4 CONTROLLED SCORING ELEMENTS (G407); never CONTROL opponent NECTAR (G408).
- Both have no MOMENTARY exception (Q&A 3, 4); routine grab-and-eject is likely STRATEGIC (Q&A 23, TU05 boxes).
- Q&A 29 asks exactly about "active mitigation" designs (lifting intake, active expulsion, color rejection) and was **still unanswered** ("ACCEPTED") on 2026-10-20. Re-check it [QA].
- The robot starts with exactly 4 POLLEN (G304.G) [MAN p104], so the **intake must be locked out at the start of AUTO** until the first shot.
- CONTROL definition: the element is fully supported by, or stuck in/on/under, the robot, or the robot intentionally herds it; contact is required [MAN p171].

### 7.2 Sensing options [V/I]

| Need | Sensor | Notes |
|---|---|---|
| Ball present + color in the indexer | **REV Color Sensor V3** (`RevColorSensorV3`, implements a distance sensor) [SDK-HW-AAR] | Read `NormalizedRGBA` → HSV (sample `SensorColor`). Distance channel detects presence within a few cm. Each I²C read costs a few ms, so throttle to 50 Hz. Put sensors on different I²C buses. |
| Size discrimination (NECTAR 3.6 in vs POLLEN 2.8 in) [MAN p74] | Distance sensor or beam break at a known height, or a mechanical gate | A 3.6-in ball reads closer or breaks a beam a 2.8-in ball doesn't [I] |
| **Opponent NECTAR ahead of the intake** | Front UVC webcam with `ColorBlobLocatorProcessor` (RED/BLUE ranges, circularity filter) or a Limelight color pipeline | Must decide *before* contact; inhibit the intake for opponent-colored blobs in the intake zone [I] |
| Count shots | Flywheel velocity dip (§6.4) and/or a sensor at the feeder exit | Redundant counting catches errors |

HSV starting points [I]: yellow pollen hue about 40–70°; red hue < 15° or > 340°; blue about 200–250° (Android `Color.colorToHSV` scale). Tune on real Gopher balls under venue light.

### 7.3 Indexer/intake state machine [I]
States: `IDLE → INTAKING → (BALL_ENTERING → STAGED)×n → FULL(4) → FEEDING → (recover) → … → EMPTY`, plus `REJECTING` and `JAM_CLEAR`.
- `INTAKING`: run intake and indexer. On the entry sensor's rising edge, classify:
  - own NECTAR or POLLEN → `count++`;
  - opponent NECTAR → `REJECTING` (reverse 300–500 ms). Log it; it should be rare if the front-camera inhibit works.
- When `count == 4` → `FULL`: stop the intake, run a short reverse pulse (150 ms) to push away a 5th ball at the mouth, set the gamepad LED red, and ignore the intake button. A driver override exists for recovery only.
- `FEEDING` (shoot sequence): for each ball, wait for `flywheel.ready && turret.onTarget && chassisSlow`, then pulse the feeder until a shot-dip or exit sensor fires (timeout 600 ms), then `count--`.
- Count reconciliation: if the sensors disagree with the count, trust the sensors and flash a warning. A driver "reset count" button exists for emergencies.

### 7.4 Jam detection [V/I]
- **Motor-driven** intake or indexer: `DcMotorEx.getCurrent(CurrentUnit.AMPS)` above threshold for more than 150 ms while commanded forward → jam. Or use `setCurrentAlert(amps)` + `isOverCurrent()` [SDK-RC-AAR]. Response: reverse 250 ms, retry once, then stop and rumble.
- **Servo-driven** (CR goBILDA servos): there is no per-servo current or position API. Use:
  1. a **progress timeout**: a ball seen at the entry sensor but not at the next sensor within T ms → jam;
  2. optionally the hub's total **servo-bus current** via the internal `LynxGetADCCommand.Channel.SERVO_CURRENT`. It exists in the Hardware 12.0.0 binary [SDK-HW-AAR] but is undocumented and internal: advanced, use at your own risk.
  3. Servos with feedback (Axon) give commanded-vs-actual error.

### 7.5 Feeding cadence [I]
Feed only on `ready`. The minimum interval is the measured recovery time, typically 0.25–0.5 s, so 4 balls take about 1–2 s. If the HIVE starts tipping, abort the remaining feeds for 1 s (§5.4).

---

## 8. Architecture, OpMode structure, driver assists

### 8.1 Framework choice

| Option | Version / status | Fit |
|---|---|---|
| **Plain Java subsystems + enum FSMs** | none needed | **Recommended first** [I]: transparent, debuggable, no framework concepts for 7th–8th graders |
| Pedro **Ivy** | 1.1.1, `com.pedropathing.ivy:pedro:1.1.1`; "simple … command-based control flow library for FTC" with Pedro commands [IVY] | Good second step for sequencing autos |
| **SolversLib** (maintained FTCLib fork, WPILib-style commands, controllers) | `org.solverslib:core:0.3.6`; "SolversLib 0.3.6+ is for Pedro Pathing 3.0.0 and higher"; don't install with FTCLib [SOLVERS] | Good if the coach wants WPILib-style command-based |
| NextFTC v1 | 1.1.0 (Feb 2026) [NEXTFTC] | Stable but superseded |
| NextFTC v2 (Kotlin, Java interop) | `dev.nextftc.v2:*` modules; GitHub NextFTCSuite at 0.3.0 (2026-10-17); "migration guide coming soon" [NEXTFTC] | Too new for this season's critical path |

### 8.2 Loop structure and performance [V/I]
- One loop: `clearBulkCache()` on both hubs → read (Pinpoint via `follower.update()`, Limelight `getLatestResult()`, which is non-blocking because a background executor polls at up to `setPollRateHz` [SDK-HW-AAR, LL-FTC], throttled I²C color sensors) → compute (state machines, aim solver, controllers) → write (only changed servo positions) → telemetry (throttled).
- Use `LynxModule.BulkCachingMode.MANUAL` [SDK-SAMPLES ConceptMotorBulkRead].
- Target a loop of 10 ms or less; show loop time in telemetry.
- Send telemetry about 10 Hz (`telemetry.setMsTransmissionInterval(100)`; the Limelight sample uses 11 ms for testing [SDK-SAMPLES]).
- Pure math (aim solver, table, pose transforms, alliance rotation) lives in classes with **no FTC imports**, so it can be **unit-tested on the laptop with JUnit** [I].

### 8.3 Telemetry, logging, live tuning [V/I]
- **At home and practice:** use **Panels** (Configurables for live constants, Graph for flywheel and turret plots, Field view; plugin-based; v1.0.19) [PANELS] *or* FTC Dashboard 0.6.0 (`@Config`, graphs, field overlay) [DASH]. Pick one; Panels pairs naturally with Pedro.
- **At the venue, both must be off (R704.D, Q&A 26):**
  - FTC Dashboard registers an "Enable/Disable Dashboard" OpMode and RC menu items; the setting persists (`PREFS_AUTO_ENABLE_KEY`) [DASH source FtcDashboard.java].
  - Panels has a persistent enable/disable preference and a config `isDisabled` flag [PANELS source Panels.kt].
  - Safest: a `COMPETITION` build flag that omits them. Verify that Wi-Fi web servers are off before loading into the pits.
- **Allowed at events:** DS telemetry, plus **local files on the Control Hub**. They are not streamed, so R704.D is satisfied [I from the rule text]. Pedro 3 has built-in `FollowerLog` loggers [PEDRO-DOCS pedro3]. AdvantageScope can read WPILOG files produced by community FTC loggers such as KoalaLog or FateWeaver [CD-ASCOPE]; this is third-party, so evaluate before adopting. A simple CSV logger (§9.1 `util/CsvLogger`) is enough to start.

### 8.4 OpMode structure [V/I]
**AUTO selector during INIT:**
- `while (opModeInInit())` loop using gamepad edge detection (`dpadUpWasPressed()` …).
- Pick alliance, start position, delay 0–10 s and routine; press A to **lock**. Show a big confirmation line in telemetry.
- Lock defaults to the safest routine if never pressed.
- Selection must finish before the countdown (G401).

**Per-alliance OpModes:** consider separate OpModes per alliance/start, e.g. `Red Audience`, `Blue Far`. They are less error-prone for drive teams under pressure [I].

**OpMode annotations:**
- `@Autonomous(name="…", group="…", preselectTeleOp="TeleOp")` queues TeleOp after AUTO [SDK-RC-AAR; MAN p105].
- Enable the 30-s timer in the DS (G305).

**AUTO→TeleOp hand-off:**
- Write `blackboard.put("pose", follower.pose())`, `"alliance"`, `"ballCount"`, `"hiveState"` **every loop**. The DS timer may stop AUTO at any instant.
- The `blackboard` "is NOT saved across power reboots or downloads or robot resets" [SDK-SAMPLES ConceptBlackboard].
- In TeleOp, construct the localizer with `resetMode = NONE` (or recalibrate the IMU only while stationary) and `setPose(blackboard pose)`. A static field works too.

**G403 / G304.H:**
- AUTO INIT may move servos to start positions, but they must be done before the match starts.
- **TeleOp `init()` must not call `setPosition` or `setPower`.** Arm subsystems in `start()` (an `enabled` flag in each subsystem).
- Servos may go limp between OpModes and jump when first commanded; test this on your robot [I].
- If TeleOp init must move something, press INIT only after TELEOP begins [MAN p107].

**Match timer:**
- TeleOp elapsed starts at START.
- Rumble and LED cues at **59 s** ("1:00 left, FLOWER unlock soon") and **100 s** ("0:20, go park").
- Unlock any NECTAR-to-FLOWER macro at ≥ 61 s (G410).
- Drivers watch the field timer; it is authoritative [MAN p79].

### 8.5 Driver-assist feature list (TeleOp) [I unless noted]

| Feature | Implementation |
|---|---|
| Field-centric drive | Pedro `ManualDrive.fieldCentric(f, l, t, heading, offsetHeading)` [PEDRO-SRC]. Driver "forward" = away from your alliance wall: +Y for red (θ = +90°), −Y for blue (θ = −90°) in FTC frame. Back button = reset heading. |
| Hold position when sticks idle (resist defense while shooting) | `ManualDrive.driveOrHold(follower, f, l, t)` [PEDRO-SRC] |
| Auto-aim toggle (default ON) | Fused aim (§5.2); turret tracks the current up cell whenever pose is known |
| One-button "shoot all" | Spin up → wait ready + aimed + slow → feed up to 4 with recovery → idle rpm |
| Target override | dpad left/right = audience or far cell; Y = "HIVE tipped" toggle |
| Intake with interlocks | RT = intake (auto-stops at 4, refuses opponent NECTAR), LT = reverse |
| Heading-assist when turret is at its limit | `headingLock` toward the target |
| Gamepad LED | PS4/PS5 only: green = ready to fire, yellow = spinning up, red = full (4), blue = no target [SDK-RC-AAR setLedColor] |
| Rumble cues | 1:00 and 0:20; 2 blips on TIP; 1 long rumble when full. Fire on transitions, not every loop, as the SDK sample warns [SDK-SAMPLES ConceptGamepadRumble] |
| Auto-park | Button builds a Pedro path to the LOADING ZONE; cancel on stick input |

### 8.6 Gamepads [V]
- BIOBUZZ removed the allowed-gamepad list [QA 16; TU00], but pads must be wired (R904 bans wireless gamepads [MAN p145]).
- Rumble: "The Xbox & PS4 have a left … and right … rumble motor"; "The Logitech F310 gamepad does not have *any* rumble ability"; the Etpark may respond only to rumble1 [SDK-SAMPLES ConceptGamepadRumble].
- **Recommend wired PS4 or PS5 controllers** for rumble plus the light bar [I].

---

## 9. Java skeletons (outlines: compile-check against your SDK; names marked VERIFY need checking)

### 9.1 Recommended repository structure
```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
  Robot.java                      // owns hubs + subsystems; readAll()/writeAll()
  config/
    RobotConstants.java           // ports, gains, limits (public static, Panels-tunable)
    FieldConstants.java           // FTC-frame targets (§3.3), park/shot poses (red), Cell enum
    ShotTables.java               // InterpolatingTable data
  pedro/                          // copied from Pedro Quickstart: Constants.java, Tuning.java, procedures/*
  subsystems/
    Flywheel.java  Turret.java (interface) TurretServo.java TurretMotor.java
    Indexer.java   Intake.java  CellVision.java  Feedback.java (rumble/LED)
  control/                        // NO FTC imports -> JVM unit-testable
    AimSolver.java  InterpolatingTable.java  PIDF.java  SlewLimiter.java  Debouncer.java  Geometry.java
  util/
    Alliance.java  MatchTimer.java  PoseStorage.java (blackboard keys)  CsvLogger.java  LoopTimer.java
  opmodes/auto/   AutoBase.java  AutoSelector.java  RedAudience.java  BlueFar.java ...
  opmodes/teleop/ MainTeleOp.java
  opmodes/test/   FlywheelTuner.java  TurretCalibrator.java  ShotTableCollector.java  VisionProbe.java  SensorProbe.java
TeamCode/src/test/java/...        // JUnit tests for control/ and FieldConstants rotations
```

### 9.2 Robot container and loop
```java
public class Robot {
    public final Follower follower;          // Pedro 3
    public final Flywheel flywheel;
    public final Turret turret;              // TurretServo or TurretMotor
    public final Indexer indexer;
    public final CellVision vision;
    public final AimSolver aim = new AimSolver(ShotTables.MAIN);
    private final List<LynxModule> hubs;

    public Robot(HardwareMap hw, Alliance alliance) {
        hubs = hw.getAll(LynxModule.class);
        for (LynxModule h : hubs) h.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        follower = Constants.create(hw);     // quickstart pattern: new Follower(localizer, drivetrain, foresight)
        flywheel = new Flywheel(hw);
        turret   = new TurretServo(hw);
        indexer  = new Indexer(hw, alliance);
        vision   = new CellVision(hw, alliance);
    }
    /** Arm actuators only after START in TeleOp (G403). */
    public void enableActuators() { turret.enable(); flywheel.enable(); indexer.enable(); }

    public void update() {
        for (LynxModule h : hubs) h.clearBulkCache();
        follower.update();                                  // reads localizer + drives
        vision.poll(follower.pose(), turret.estimatedDeg());
        // aim, flywheel and indexer logic use follower.pose()/velocity() + vision observation
        flywheel.update();
        turret.update();
        indexer.update(flywheel);
    }
}
```

### 9.3 Cell enum (field constants)
```java
public enum Cell {
    RED_FAR(30, Alliance.RED, false), RED_AUDIENCE(34, Alliance.RED, true),
    BLUE_AUDIENCE(38, Alliance.BLUE, true), BLUE_FAR(42, Alliance.BLUE, false);

    public final int baseId; public final Alliance alliance; public final boolean audienceSide;
    Cell(int b, Alliance a, boolean aud) { baseId = b; alliance = a; audienceSide = aud; }

    public boolean contains(int id) { return id >= baseId && id < baseId + 4; }
    public static Cell ofTag(int id) { for (Cell c : values()) if (c.contains(id)) return c; return null; }
    public Cell partner() { switch (this) { case RED_FAR: return RED_AUDIENCE; case RED_AUDIENCE: return RED_FAR;
                                            case BLUE_FAR: return BLUE_AUDIENCE; default: return BLUE_FAR; } }

    /** Tag's offset from cluster center along FIELD +Y (in). SDK v12 layout {-6.5,-2.75,2.75,6.5};
     *  order from Fig 9-17 (audience cells increase toward +Y, far cells reversed). VERIFY SIGN ON FIELD. */
    public double fieldYOffsetIn(int id) {
        final double[] k = {-6.5, -2.75, 2.75, 6.5};
        double off = k[id - baseId];
        return audienceSide ? off : -off;
    }
    /** Estimated opening center when this cell is UP, FTC frame (in). [E] measure on a real field. */
    public double[] openingCenterIn() {
        double x = audienceSide ? 16.0 : -16.0;
        double y = (alliance == Alliance.RED) ? -12.75 : 12.75;
        return new double[]{x, y, 59.5};
    }
}
```

### 9.4 CellVision: Limelight 3A, per-tag to cluster center, up/down test
```java
public class CellVision {
    // ---- measure on YOUR robot (camera on turret) ----
    public static double CAM_PITCH_DEG = 37;     // camera tilted UP
    public static double CAM_HEIGHT_IN = 15.0;   // lens above tiles
    public static double CAM_FWD_IN    = 3.0;    // lens ahead of turret axis
    public static double UP_TAG_MIN_HEIGHT_IN = 44; // up-cell tags ~50 in [E]; calibrate
    public static long   MAX_STALENESS_MS = 120;
    public static int    PIPE_TAGS = 0;          // AprilTag pipeline, 640x480, Full 3D, marker 82.55 mm,
                                                 // ID filter = own alliance (red 30-37 / blue 38-45)
    private final Limelight3A ll;
    private final Alliance alliance;
    private Observation latest = null;

    public static final class Observation {
        public Cell cell; public boolean up; public int tagsSeen;
        public double bearingDegTurret;  // + = target to the LEFT of the camera axis (CCW)
        public double horizDistIn;       // turret axis -> cluster center, horizontal
        public double tagHeightIn;
        public long   captureNanos;      // for latency compensation
    }

    public CellVision(HardwareMap hw, Alliance a) {
        alliance = a;
        ll = hw.get(Limelight3A.class, "limelight");
        ll.setPollRateHz(100);
        ll.pipelineSwitch(PIPE_TAGS);
        ll.start();
    }

    /** heading = robot heading (rad, FTC frame); turretDeg = turret angle at capture (deg, CCW+). */
    public void poll(Pose robotPose, double turretDegAtCapture) {
        LLResult r = ll.getLatestResult();                 // never null since SDK 10.3; check isValid()
        if (!r.isValid() || r.getStaleness() > MAX_STALENESS_MS) { latest = null; return; }
        double psi = robotPose.heading() + Math.toRadians(turretDegAtCapture);  // turret forward in field
        double a = Math.toRadians(CAM_PITCH_DEG);
        Map<Cell, double[]> acc = new EnumMap<>(Cell.class); // F, L, U sums + count
        for (LLResultTypes.FiducialResult f : r.getFiducialResults()) {
            Cell c = Cell.ofTag(f.getFiducialId());
            if (c == null || c.alliance != alliance) continue;           // own alliance only (G417)
            Position p = f.getTargetPoseCameraSpace().getPosition().toUnit(DistanceUnit.INCH);
            // Limelight camera space: x right, y down, z forward (LL docs). Rotate into level turret frame:
            double F = p.y * Math.sin(a) + p.z * Math.cos(a) + CAM_FWD_IN;
            double L = -p.x;
            double U = -p.y * Math.cos(a) + p.z * Math.sin(a) + CAM_HEIGHT_IN;
            // shift from this tag to the cluster (row) center along FIELD Y, expressed in turret frame
            double dy = -c.fieldYOffsetIn(f.getFiducialId());
            F += dy * Math.sin(psi);
            L += dy * Math.cos(psi);
            double[] s = acc.computeIfAbsent(c, k -> new double[4]);
            s[0] += F; s[1] += L; s[2] += U; s[3] += 1;
        }
        Observation best = null;
        for (Map.Entry<Cell, double[]> e : acc.entrySet()) {
            double[] s = e.getValue(); double n = s[3];
            Observation o = new Observation();
            o.cell = e.getKey(); o.tagsSeen = (int) n;
            double F = s[0] / n, L = s[1] / n;
            o.tagHeightIn = s[2] / n;
            o.up = o.tagHeightIn > UP_TAG_MIN_HEIGHT_IN;
            o.bearingDegTurret = Math.toDegrees(Math.atan2(L, F));
            o.horizDistIn = Math.hypot(F, L);
            o.captureNanos = System.nanoTime()
                    - (long) ((r.getCaptureLatency() + r.getTargetingLatency()) * 1e6); // ms -> ns; VERIFY units
            if (o.up && (best == null || o.tagsSeen > best.tagsSeen)) best = o;
        }
        latest = best;
    }
    public Observation latest() { return latest; }
}
```
Notes:
- Using the 3D tag positions instead of `getTargetXDegrees()` makes the result independent of which tag is "primary".
- The shift along field Y uses the robot's odometry heading, so one visible tag is enough.
- If there is no odometry, average the visible tag pairs symmetric about the center (34+37, 35+36).

### 9.5 Alternative: SDK cluster detection with a UVC webcam
```java
AprilTagProcessor tags = new AprilTagProcessor.Builder()
        .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())   // == getCurrentGameTagLibrary() in 12.0
        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
        .build();
tags.setDecimation(2);
VisionPortal portal = new VisionPortal.Builder()
        .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
        .setCameraResolution(new Size(640, 480))
        .addProcessor(tags).build();
// loop:
for (AprilTagDetection d : tags.getDetections()) {
    if (!(d instanceof AprilTagClusterDetection)) continue;
    AprilTagClusterDetection c = (AprilTagClusterDetection) d;
    if (!c.metadata.name.startsWith(alliance == Alliance.RED ? "R" : "B")) continue;
    boolean uprightSdkHeuristic = Math.abs(d.ftcPose.roll) < 90;        // ftc-docs recipe
    double heightIn = CAM_HEIGHT_IN + d.ftcPose.range
            * Math.sin(Math.toRadians(d.ftcPose.elevation + CAM_PITCH_DEG)); // approx; origin = opening center
    double bearingDeg = d.ftcPose.bearing;                                 // + = CCW (left)
    double rangeIn = d.ftcPose.range;  int pct = c.percentClusterFound;
    long captureNs = d.frameAcquisitionNanoTime;
}
```

### 9.6 AimSolver and InterpolatingTable (pure Java, unit-testable)
```java
public final class InterpolatingTable {
    private final TreeMap<Double, double[]> m = new TreeMap<>();
    public InterpolatingTable put(double key, double... vals) { m.put(key, vals.clone()); return this; }
    public double[] get(double key) {
        Map.Entry<Double, double[]> lo = m.floorEntry(key), hi = m.ceilingEntry(key);
        if (lo == null) return hi.getValue().clone();
        if (hi == null || lo.getKey().equals(hi.getKey())) return lo.getValue().clone();
        double t = (key - lo.getKey()) / (hi.getKey() - lo.getKey());
        double[] a = lo.getValue(), b = hi.getValue(), out = new double[a.length];
        for (int i = 0; i < a.length; i++) out[i] = a[i] + t * (b[i] - a[i]);
        return out;
    }
}
// ShotTables.MAIN = new InterpolatingTable()  // key: horizontal dist (in) -> {rpm, hoodPos, tofSec}
//     .put(30, 3000, 0.40, 0.35).put(36, 2950, 0.42, 0.38) ...  // fill from ShotTableCollector

public final class AimSolver {
    public static double TURRET_TRIM_DEG = 0;          // mechanical zero trim
    public static double BIAS_GAIN = 0.15, BIAS_MAX_STEP_DEG = 8, BIAS_DECAY = 0.002;
    private final InterpolatingTable table; private double visionBiasDeg = 0;
    public AimSolver(InterpolatingTable t) { table = t; }

    public static final class Solution { public double turretDeg, distIn, rpm, hood, tof; }

    /** rx,ry (in), heading (rad), vx,vy field velocity (in/s), tx,ty target (in). */
    public Solution solve(double rx, double ry, double heading, double vx, double vy, double tx, double ty) {
        double gx = tx, gy = ty; double[] shot = null; double dist = 0;
        for (int i = 0; i < 3; i++) {                        // lead compensation iteration
            dist = Math.hypot(gx - rx, gy - ry);
            shot = table.get(dist);
            double tof = shot[2];
            gx = tx - vx * tof; gy = ty - vy * tof;
        }
        Solution s = new Solution();
        double fieldBearing = Math.atan2(gy - ry, gx - rx);
        s.turretDeg = Math.toDegrees(normalize(fieldBearing - heading)) + TURRET_TRIM_DEG + visionBiasDeg;
        s.distIn = dist; s.rpm = shot[0]; s.hood = shot[1]; s.tof = shot[2];
        return s;
    }
    /** measured & predicted target bearings in the SAME turret frame at capture time (deg). */
    public void addVision(double measuredDeg, double predictedDeg) {
        double e = Math.toDegrees(normalize(Math.toRadians(measuredDeg - predictedDeg)));
        if (Math.abs(e - visionBiasDeg) > BIAS_MAX_STEP_DEG) return;   // outlier (wrong cell, partner robot)
        visionBiasDeg += BIAS_GAIN * (e - visionBiasDeg);
    }
    public void decayBias() { visionBiasDeg *= (1 - BIAS_DECAY); }
    static double normalize(double a) { while (a > Math.PI) a -= 2*Math.PI; while (a < -Math.PI) a += 2*Math.PI; return a; }
}
```

### 9.7 Flywheel (custom FF + P with voltage compensation; built-in alternative in comments)
```java
public class Flywheel {
    public static double TICKS_PER_REV = 28;              // goBILDA 6000 rpm 1:1 [GB-6000]
    public static double kS = 0.04, kV = 1.0 / 2650.0, kP = 0.0006;   // identify! kV: power per (ticks/s) at 12 V
    public static double READY_ABS_TPS = 40, READY_FRAC = 0.02;
    private final DcMotorEx m; private final VoltageSensor batt;
    private double targetTps = 0, vBatt = 12.5; private int okLoops = 0; private boolean enabled = false;
    private final ElapsedTime vTimer = new ElapsedTime();

    public Flywheel(HardwareMap hw) {
        m = hw.get(DcMotorEx.class, "flywheel");
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);    // we close the loop
        batt = hw.voltageSensor.iterator().next();
        // --- Built-in alternative (Option A) ---
        // m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // double F = 32767.0 / MAX_TPS; m.setVelocityPIDFCoefficients(0.1*F, 0.01*F, 0, F);  // [REV-PIDF]
        // then m.setVelocity(targetTps) each time the target changes
    }
    public void enable() { enabled = true; }
    public void setRpm(double rpm) { targetTps = rpm * TICKS_PER_REV / 60.0; }
    public double rpm() { return m.getVelocity() * 60.0 / TICKS_PER_REV; }

    public void update() {
        if (vTimer.milliseconds() > 250) { vBatt = 0.8 * vBatt + 0.2 * batt.getVoltage(); vTimer.reset(); }
        double v = m.getVelocity();                        // bulk-cached ticks/s
        double err = targetTps - v;
        double p = 0;
        if (enabled && targetTps > 0) {
            p = (kS + kV * targetTps) * (12.0 / vBatt) + kP * err;
            if (err > 0.08 * targetTps) p = 1.0;          // bang-bang recovery after a shot
        }
        m.setPower(Range.clip(p, 0, 1));                   // never drive backwards
        boolean ok = targetTps > 0 && Math.abs(err) < Math.max(READY_ABS_TPS, READY_FRAC * targetTps);
        okLoops = ok ? okLoops + 1 : 0;
    }
    public boolean ready() { return okLoops >= 3; }
}
```

### 9.8 Turret: positional-servo variant and encoder/PID variant
```java
public interface Turret { void enable(); void setTargetDeg(double d); void update();
                          double estimatedDeg(); boolean onTarget(double tolDeg); boolean atLimit(); }

public class TurretServo implements Turret {
    public static double MIN_DEG = -35, MAX_DEG = 35;     // measure (300 deg / gear ratio, minus margin)
    public static double POS_AT_0 = 0.50, POS_PER_DEG = 0.0;   // calibrate from two measured points
    public static double SLEW_DEG_PER_S = 120;             // measured, used to estimate actual angle
    private final ServoImplEx s; private double cmd = 0, est = 0, lastPos = -1; private boolean enabled = false;
    private long lastNs = System.nanoTime(); private boolean clamped = false;
    public TurretServo(HardwareMap hw) {
        s = hw.get(ServoImplEx.class, "turret");
        s.setPwmRange(new PwmControl.PwmRange(500, 2500)); // goBILDA full 300 deg (or "Full Range Servo" config)
    }
    public void enable() { enabled = true; }
    public void setTargetDeg(double d) { cmd = Range.clip(d, MIN_DEG, MAX_DEG); clamped = cmd != d; }
    public void update() {
        long now = System.nanoTime(); double dt = (now - lastNs) / 1e9; lastNs = now;
        double step = SLEW_DEG_PER_S * dt; est += Range.clip(cmd - est, -step, step);
        if (!enabled) return;                              // G403: no motion before START in TeleOp
        double pos = POS_AT_0 + POS_PER_DEG * cmd;
        if (Math.abs(pos - lastPos) > 0.001) { s.setPosition(pos); lastPos = pos; }
    }
    public double estimatedDeg() { return est; }
    public boolean onTarget(double tol) { return Math.abs(cmd - est) < tol; }
    public boolean atLimit() { return clamped; }           // -> chassis assist / rumble
}

public class TurretMotor implements Turret {               // CR servo or motor + encoder (REV Through Bore on port 0/3)
    public static double TICKS_PER_DEG = 0;                // measure
    public static double MIN_DEG = -170, MAX_DEG = 170, kP = 0.03, kD = 0.0015, kS = 0.04, kFFomega = 0.0;
    private final DcMotorEx enc; private final CRServo drive;  // or DcMotorEx drive
    private double target = 0; private boolean enabled = false; private double chassisOmegaDegS = 0;
    public TurretMotor(HardwareMap hw) { enc = hw.get(DcMotorEx.class, "turretEnc"); drive = hw.get(CRServo.class, "turret"); }
    public void enable() { enabled = true; }
    public void setChassisOmega(double degPerS) { chassisOmegaDegS = degPerS; }
    public void setTargetDeg(double d) {
        // pick the equivalent angle (d, d±360) nearest the current angle that is inside limits (no cable-wrap crossing)
        double cur = estimatedDeg(), best = Double.NaN;
        for (double c : new double[]{d, d - 360, d + 360})
            if (c >= MIN_DEG && c <= MAX_DEG && (Double.isNaN(best) || Math.abs(c - cur) < Math.abs(best - cur))) best = c;
        target = Double.isNaN(best) ? Range.clip(d, MIN_DEG, MAX_DEG) : best;
    }
    public void update() {
        double pos = estimatedDeg(), vel = enc.getVelocity() / TICKS_PER_DEG, err = target - pos;
        double out = kP * err - kD * vel - kFFomega * chassisOmegaDegS + (Math.abs(err) > 0.5 ? Math.signum(err) * kS : 0);
        if ((pos >= MAX_DEG && out > 0) || (pos <= MIN_DEG && out < 0)) out = 0;   // soft limit
        if (enabled) drive.setPower(Range.clip(out, -1, 1));
    }
    public double estimatedDeg() { return enc.getCurrentPosition() / TICKS_PER_DEG; }
    public boolean onTarget(double tol) { return Math.abs(target - estimatedDeg()) < tol; }
    public boolean atLimit() { return target <= MIN_DEG || target >= MAX_DEG; }
}
```

### 9.9 Indexer (FSM core)
```java
public class Indexer {
    enum State { IDLE, INTAKING, FULL, REJECTING, FEEDING, RECOVER, JAM_CLEAR }
    public static int MAX_BALLS = 4;                       // G407
    public static long REJECT_MS = 400, FEED_TIMEOUT_MS = 600, JAM_MS = 900;
    private State st = State.IDLE; private int count; private final Alliance alliance;
    private final ElapsedTime t = new ElapsedTime(); private boolean intakeRequested, shootRequested;
    // hardware: CRServo intake, spinner, feeder; NormalizedColorSensor mouth, staged; ...

    public Indexer(HardwareMap hw, Alliance a) { alliance = a; /* map hardware */ }
    public void setCount(int c) { count = c; }             // AUTO: 4 preloads; TeleOp: from blackboard
    public void requestIntake(boolean on) { intakeRequested = on; }
    public void requestShootAll() { shootRequested = true; }

    public void update(Flywheel fw /*, Turret turret, boolean chassisSlow, boolean opponentNectarAhead */) {
        switch (st) {
            case IDLE:
                if (shootRequested && count > 0) go(State.FEEDING);
                else if (intakeRequested && count < MAX_BALLS) go(State.INTAKING);
                break;
            case INTAKING:
                // if (opponentNectarAhead) { stopIntake(); break; }          // pre-contact inhibit (G408)
                // on mouth-sensor rising edge: classify -> own: count++ ; opponent NECTAR: go(REJECTING)
                // if (no progress for JAM_MS while a ball is present) go(JAM_CLEAR);
                if (count >= MAX_BALLS) go(State.FULL);
                if (!intakeRequested) go(State.IDLE);
                break;
            case FULL:      /* reverse 150 ms then stop; LED red; ignore intake */ if (t.milliseconds() > 150) go(State.IDLE); break;
            case REJECTING: /* reverse intake */ if (t.milliseconds() > REJECT_MS) go(State.INTAKING); break;
            case FEEDING:
                // if (fw.ready() && turret.onTarget(2) && chassisSlow) pulse feeder
                // if (shot detected via fw dip or exit sensor) { count--; go(count > 0 ? State.RECOVER : State.IDLE); }
                // if (t.milliseconds() > FEED_TIMEOUT_MS) go(State.JAM_CLEAR);
                break;
            case RECOVER:   if (fw.ready()) go(State.FEEDING); break;
            case JAM_CLEAR: /* reverse 250 ms */ if (t.milliseconds() > 250) go(State.IDLE); break;
        }
    }
    private void go(State s) { st = s; t.reset(); if (s == State.IDLE) shootRequested = false; }
    public int count() { return count; }
}
```

### 9.10 AUTO: selector + state machine (example routine for red; strategy agents choose the real one)
```java
@Autonomous(name = "Auto (select in INIT)", group = "Comp", preselectTeleOp = "TeleOp")
public class MainAuto extends LinearOpMode {
    enum S { WAIT_DELAY, TO_SHOT1, SHOOT1, TO_COLLECT, COLLECT, TO_SHOT2, SHOOT2, TO_PARK, DONE }
    public static double PARK_DEADLINE_S = 25.0;           // leave time to reach park pose (measure)

    @Override public void runOpMode() {
        AutoConfig cfg = AutoSelector.run(this);            // INIT menu with edge detection + lock
        Robot robot = new Robot(hardwareMap, cfg.alliance);
        FieldPoses P = FieldPoses.forAlliance(cfg.alliance); // red constants; blue = rotate 180 deg about center
        robot.follower.setPose(P.start(cfg.start));
        robot.indexer.setCount(4);                          // G304.G preloads
        Cell target = (cfg.alliance == Alliance.RED) ? Cell.RED_AUDIENCE : Cell.BLUE_FAR; // start-up cells
        Path toShot1 = line(P.start(cfg.start), P.shot(target)).linear(P.start(cfg.start), P.shot(target));
        while (opModeInInit()) { telemetry.addLine(cfg.summary()); telemetry.update(); }   // G304.H: no motion

        robot.enableActuators();
        ElapsedTime match = new ElapsedTime(), st = new ElapsedTime();
        S s = S.WAIT_DELAY;
        while (opModeIsActive()) {
            robot.update();
            PoseStorage.save(blackboard, robot.follower.pose(), cfg.alliance, robot.indexer.count()); // every loop
            if (match.seconds() > PARK_DEADLINE_S && s.ordinal() < S.TO_PARK.ordinal()) {
                robot.follower.follow(line(robot.follower.pose(), P.autoPark()).constant(P.autoPark().heading()));
                s = S.TO_PARK; st.reset();
            }
            switch (s) {
                case WAIT_DELAY:
                    if (match.seconds() >= cfg.delaySec) { robot.follower.follow(toShot1); s = S.TO_SHOT1; st.reset(); }
                    break;
                case TO_SHOT1:   // spin up and pre-aim while driving
                    robot.aimAt(target);                    // sets turret + flywheel from AimSolver
                    if (!robot.follower.isBusy() || st.seconds() > 4) { robot.indexer.requestShootAll(); s = S.SHOOT1; st.reset(); }
                    break;
                case SHOOT1:
                    robot.aimAt(target);
                    if (robot.vision.tipDetected()) { target = target.partner(); }   // pause handled in Indexer
                    if (robot.indexer.count() == 0 || st.seconds() > 4) { /* follow toCollect */ s = S.TO_COLLECT; st.reset(); }
                    break;
                // TO_COLLECT -> COLLECT (intake on; until count==4 or timeout) -> TO_SHOT2 -> SHOOT2 ...
                case TO_PARK:   // must end partly inside LOADING ZONE but NOT touching the wall (LEAVE+PARK)
                    if (!robot.follower.isBusy()) s = S.DONE;
                    break;
                case DONE: break;
            }
            telemetry.addData("state", s); telemetry.addData("t", "%.1f", match.seconds()); telemetry.update();
        }
    }
}
```
`AutoSelector.run(op)` outline:
- Loop `while (op.opModeInInit() && !locked)`.
- `dpadUp/Down` moves the cursor; `dpadLeft/Right` changes the value (alliance, start, delay 0–10 s, routine); `a` locks; `b` unlocks.
- Show `>>> RED | AUDIENCE START | DELAY 3s | PRELOAD+GARDEN+PARK <<<` in large, clear telemetry.
- Store the result in `blackboard` for TeleOp.

### 9.11 TeleOp outline
```java
@TeleOp(name = "TeleOp", group = "Comp")
public class MainTeleOp extends LinearOpMode {
    @Override public void runOpMode() {
        Alliance a = PoseStorage.alliance(blackboard, Alliance.RED);
        Robot robot = new Robot(hardwareMap, a);            // constructors must NOT move actuators (G403)
        robot.follower.setPose(PoseStorage.pose(blackboard)); // localizer resetMode NONE or recal while still
        robot.indexer.setCount(PoseStorage.count(blackboard, 0));
        MatchCues cues = new MatchCues();                     // rumble/LED at 59 s & 100 s; unlock flags
        waitForStart();
        robot.enableActuators();                              // first actuator commands happen here
        ElapsedTime tele = new ElapsedTime();
        double driverFwd = (a == Alliance.RED) ? Math.PI / 2 : -Math.PI / 2;  // away from own wall [I]
        while (opModeIsActive()) {
            robot.update();
            double f = -gamepad1.left_stick_y, l = -gamepad1.left_stick_x, t = -gamepad1.right_stick_x;
            DrivePowers p = ManualDrive.fieldCentric(f, l, t, robot.follower.pose().heading(), driverFwd); // VERIFY sign/offset
            ManualDrive.driveOrHold(robot.follower, p);       // hold when idle (resist pushing)
            robot.indexer.requestIntake(gamepad1.right_trigger > 0.3);
            if (gamepad1.aWasPressed()) robot.indexer.requestShootAll();
            if (gamepad1.yWasPressed()) robot.toggleHiveTipped();
            if (gamepad1.dpadLeftWasPressed()) robot.forceTarget(/*audience*/);
            if (gamepad1.dpadRightWasPressed()) robot.forceTarget(/*far*/);
            robot.autoAim(true);                              // fused aim each loop
            cues.update(tele.seconds(), gamepad1, gamepad2, robot);
            telemetry.addData("balls", robot.indexer.count()).addData("rpm", "%.0f", robot.flywheel.rpm());
            telemetry.update();
        }
    }
}
```

---

## 10. Suggested 4-week software plan [I]

| Week | Software deliverables | Gate to pass |
|---|---|---|
| 1 | Repo on SDK 12.0 + Pedro 3 quickstart; config names; TestHardware passes; Pinpoint/OTOS mounted; AutoTune: Mecanum + localizer; field-centric TeleOp drive | 48-in pushes read 48 ± 0.5 in; 10 spins read 3600 ± 2° |
| 2 | Foresight tune + tests; flywheel Option A + `FlywheelTuner` (step responses, recovery time); turret calibration; Limelight pipeline + `VisionProbe` (bearing, distance, height, up/down) | Path end error < 1 in; flywheel ready < 1.0 s from idle; vision bearing matches tape within 1° |
| 3 | `ShotTableCollector` → LUT; fused aim; indexer FSM with count interlock + opponent-NECTAR inhibit; one-button shoot; preload-shoot + park AUTO for each start | 8/10 hits at 3 distances from a static robot; AUTO LEAVE+PARK 10/10 off the wall |
| 4 | Collect cycle in AUTO; TIP detection + target swap + pause; rumble/LED cues; blackboard hand-off; competition build with Dashboard/Panels disabled; CSV logging; drive practice | Full mock matches; event checklist complete |

---

## 11. Open questions / things to verify

See the structured output. Key items:
- Limelight model (3A vs 3G).
- Turret drive and travel; what the hood servo does.
- Whether any odometry hardware is on hand or budgeted.
- Exact up-cell opening and tag positions (CAD).
- Q&A 29 ruling on "active mitigation" designs.
- The real tip threshold.
- SWYFT pod CPR.
- Controller models (rumble and LED support).

---

## 12. Sources

Local primary sources:
- **[MAN]** 2026-2027 FTC Competition Manual BIOBUZZ, Version TU05 (2026-10-15), local PDF `scratchpad/manual/manual.pdf`. Pages cited inline (e.g., p63-80 Arena, p86-91 Scoring, p99-118 Game rules, p119-146 Robot rules).
- **[TU]** Team Updates 00–05 combined (TU05 = 2026-10-15; TU04 = 10-08; TU03 = 10-01; TU02 = 09-24; TU01 = 09-17; TU00 = 09-12): https://first-resources.pdx-prod.ftclive.org/file/ftc/game/tu-combined (copy: `scratchpad/downloads/r6-tu/`).
- **[QA]** Official FTC Q&A 2026-27: https://game-qa.firstinspires.org/boards/2027/FTC/QA (Q1–Q31 parsed 2026-10-20; copy `scratchpad/downloads/r6-qa/qa_dump.txt`).

FTC SDK:
- **[SDK-REL]** https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases
- **[SDK-README]** https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/master/README.md (release notes 12.0 → 10.0)
- **[SDK-VISION-AAR]** `org.firstinspires.ftc:Vision:12.0.0` from Maven Central (https://repo1.maven.org/maven2/org/firstinspires/ftc/Vision/12.0.0/). I inspected the bytecode of `AprilTagGameDatabase.getBioBuzzTagLibrary()`, `getBioBuzzCluster(int)` and the cluster classes with a static parser (`scratchpad/scripts/r6_classdump.py`, no code executed).
- **[SDK-HW-AAR]** `org.firstinspires.ftc:Hardware:12.0.0`: `Limelight3A`, `LLResult`, `LLResultTypes$FiducialResult`, `LLFieldMap`, `GoBildaPinpointDriver` (+ pod constants), `LynxModule`, `LynxGetADCCommand$Channel`.
- **[SDK-RC-AAR]** `org.firstinspires.ftc:RobotCore:12.0.0`: `Gamepad`, `OpMode.blackboard`, `DcMotorEx`, `@Autonomous.preselectTeleOp`, `Position`, `Pose3D`, `YawPitchRollAngles`.
- **[SDK-SAMPLES]** https://github.com/FIRST-Tech-Challenge/FtcRobotController/tree/master/FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples (`ConceptAprilTag`, `SensorLimelight3A`, `ConceptGamepadRumble`, `ConceptBlackboard`, `ConceptMotorBulkRead`, `SensorGoBildaPinpoint`, …).

ftc-docs and REV:
- **[FTCDOCS-CLUSTER]** https://ftc-docs.firstinspires.org/apriltag-clusters
- **[FTCDOCS-FTCPOSE]** https://ftc-docs.firstinspires.org/en/latest/apriltag/understanding_apriltag_detection_values/understanding-apriltag-detection-values.html
- **[FTCDOCS-FIELD]** https://ftc-docs.firstinspires.org/en/latest/game_specific_resources/field_coordinate_system/field-coordinate-system.html
- **[FTCDOCS-PIDF]** https://ftc-docs.firstinspires.org/programming_resources/shared/pidf_coefficients/pidf-coefficients.html
- **[FTCDOCS-EXTLIB]** https://ftc-docs.firstinspires.org/programming_resources/shared/external_libraries_blocks/external-libraries-blocks.html
- **[REV-PIDF]** https://docs.revrobotics.com/first-global/fgc-pid
- **[GM0-MOTORS]** https://gm0.org/en/latest/docs/software/adv-control-system/sdk-motors.html

Limelight:
- **[LL-FTC]** https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming
- **[LL-3A]** https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a
- **[LL-TAGS]** https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltags
- **[LL-COORD]** https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-coordinate-systems

Pedro Pathing and Road Runner:
- **[PEDRO-REL]** https://github.com/Pedro-Pathing/PedroPathing/releases
- **[PEDRO-QS]** https://github.com/Pedro-Pathing/Quickstart (`build.dependencies.gradle`; `TeamCode/.../pedro/procedures/*`)
- **[PEDRO-SRC]** https://github.com/Pedro-Pathing/PedroPathing (core: `Follower`, `Paths`, `PoseFactory`, `FusionLocalizer`, `ManualDrive`, `Foresight(Config)`; revhub: `Mecanum(Config)`, `PinpointLocalizer/Config`, localizer list)
- **[PEDRO-DOCS]** https://pedropathing.com/docs/pathing/pedro3 , /docs/pathing/installation , /docs/pathing/tuning , /docs/pathing/tuning/localization
- **[IVY]** https://pedropathing.com/docs/ivy
- **[RR]** https://rr.brott.dev/docs/v1-0/tuning/ ; https://github.com/acmerobotics/road-runner/releases ; https://github.com/acmerobotics/road-runner-quickstart (`TeamCode/build.gradle`, `build.dependencies.gradle`)

Dashboards and frameworks:
- **[DASH]** https://github.com/acmerobotics/ftc-dashboard (releases; `FtcDashboard/src/main/java/com/acmerobotics/dashboard/FtcDashboard.java` enable/disable menu and OpMode)
- **[PANELS]** https://panels.bylazar.com/ ; https://github.com/ftcontrol/ftcontrol-panels (`library/Panels/src/main/java/com/bylazar/panels/Panels.kt`)
- **[NEXTFTC]** https://nextftc.dev/ ; https://nextftc.dev/introduction/installation/ ; https://github.com/NextFTC/NextFTC/releases ; https://github.com/NextFTC/NextFTCSuite/releases
- **[SOLVERS]** https://docs.seattlesolvers.com/installation.md

Vendors:
- **[SWYFT-DRIVE]** https://swyftrobotics.com/kits/swyft-drive-v2
- **[SWYFT-ODO]** https://swyftrobotics.com/kits/swyft-linear-odometry-module
- **[GB-PINPOINT]** https://www.gobilda.com/pinpoint-odometry-computer-imu-sensor-fusion-for-2-wheel-odometry/
- **[GB-SERVO]** https://gobilda.com/2000-series-dual-mode-servo-25-2 (and 25-3 listings)
- **[GB-6000]** https://gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder
- **[OTOS]** https://www.sparkfun.com/sparkfun-optical-tracking-odometry-sensor-paa5160e1-qwiic.html

Third-party / community (lower confidence):
- **[TRC]** Tennessee Robotics Center, "Limelight 3A: Tracking Pollen and AprilTags (BIOBUZZ)", 2026-09-25, https://irp.cdn-website.com/3a1d1852/files/uploaded/TRC_Limelight_Guide_BIOBUZZ.pdf. Contains game-piece errors.
- **[CD-SOTM]** https://www.chiefdelphi.com/t/shoot-on-the-move-from-the-code-perspective/511815
- **[CD-ASCOPE]** https://www.chiefdelphi.com/t/advantage-scope-for-ftc/522762
