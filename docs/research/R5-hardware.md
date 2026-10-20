# R5 — Team 12303 Hardware Deep-Dive and Recommended Additions (BIOBUZZ 2026-27)

Prepared 2026-10-20 for the 12303 coach. Scope: SWYFT Drive V2, Limelight 3A, goBILDA 6000 rpm Yellow Jacket, goBILDA Dual Mode Servos, REV Control/Expansion Hubs and batteries, plus recommended additions, an R503 actuator budget and a port map.

**Labels used in this report**
- **VERIFIED**: stated directly by the cited source (manual page, manufacturer page or SDK release notes).
- **DERIVED**: my arithmetic from cited inputs. The script is at `/private/tmp/claude-501/-Users-craigmartin-projects-ftc-12303/1db60cdd-00ce-4f71-b01b-0f877dc4c285/scratchpad/scripts/hw_calcs.py` and `launch2.py`.
- **ESTIMATE / INFERENCE**: engineering judgment. Treat these as hypotheses to test on the robot.
- **COMMUNITY**: forum or third-party claims that I could not confirm.

Manual citations ("M p.N", rule numbers) refer to Competition Manual TU05 (2026-10-15). Source IDs [S#] are listed in Section 12.

---

## 0. Executive summary (most decision-relevant)

1. **The drivetrain is a mecanum drive with motors inside the wheels** (VERIFIED [S1]). The coach's SR-DRIVE-02 is the 86 mm, 40A-roller pack of 4. Each module holds a SWYFT Spike 550-class motor (6000 rpm, 28 counts per motor revolution) and a 12.7:1 all-metal planetary gearbox. SWYFT advertises 6.4 ft/s; the theoretical free speed is 6.98 ft/s (DERIVED). The modules are rated for robots up to 40 lb and weigh 575 g each. Program it with standard gm0 mecanum code: reverse one side, BRAKE, strafe ×1.1 (VERIFIED [S1][S30]). Only one gear ratio is offered.
2. **The flywheel motor is a good choice (goBILDA 5203-2402-0001, 6000 rpm, 1:1).** Specs: 1.47 kg·cm stall torque, 9.2 A stall current, 28 counts/rev, about 22.6 W peak mechanical power (VERIFIED [S6], DERIVED). The ball must leave the shooter at roughly **5–7.7 m/s** to enter the up-facing CELL opening from 2–12 ft (DERIVED, drag estimated). With the roughly 72 mm wheel seen in the photo, that needs about 3,000–5,000 rpm, which leaves modest headroom at 11–12 V. A larger wheel (about 96 mm) or more flywheel inertia would help. **This motor is a poor turret-rotation motor**: it has no reduction, it would give about 140 counts per turret revolution through a 5:1 ring, and it is back-drivable.
3. **The turret currently appears to be driven by a servo pinion on a large ring gear (photo).** A standard 300° servo through a roughly 4–5:1 ring gives only about **60–75° of turret travel**. Fix this with one of: (a) a goBILDA 5-turn Dual Mode Servo (1800° range, so 360–450° of turret travel at 4–5:1); (b) a CR servo plus a REV Through Bore Encoder V2 on the turret axis; or (c) a geared DC motor (5203 312/435 rpm) on the ring, which gives 0.13–0.19° per count.
4. **The servo intake is under-powered.** By R502's own power formula, a goBILDA Dual Mode Servo produces about 2.8 W at 6 V and about 2 W at the 5 V hub ports. A 5203 motor produces about 22.6 W. On a 3.5–4.5 in roller the servo gives only about 1.4–2.3 ft/s of surface speed, against the rule of thumb of about 2× robot speed (≈13 ft/s). gm0 strongly recommends dedicating a motor to the intake [S33]. **Recommendation: move the intake to a 5203 1150 rpm (or 435 rpm) motor and keep servos for the gate, feeder and hood.**
5. **Limelight 3A is the only legal programmable vision coprocessor** (R702 Table 12-9, M p.140). The Limelight 3G is explicitly prohibited (R702 Example 6), and the Limelight 4 is not on the approved list, so **confirm the team's unit is a 3A**. Specs: OV5647 colour rolling-shutter sensor, 640×480 at 90 fps, FOV 54.5°×42°. AprilTag pipelines run at 50 fps (640×480) or 20 fps (1280×960). Neural pipelines run on the CPU only. It connects to the Control Hub **USB 3.0 port** with an **A-to-C cable** [S8][S20].
6. **Geometry of the BIOBUZZ tags (DERIVED from M p.70-77 figures).**
   - Up-cell tag cluster centre: about **49.7 in** high. Down-cell tag cluster centre: about **34 in** high. The up-cell opening centre: about 59.6 in high.
   - **All 8 tags of a HIVE face down and 30° toward the side where that HIVE's up cell is**, which is the side you launch from. From that side you see the up-cell tags upright and the down-cell tags upside-down. From the other side you see none of them.
   - **When a HIVE tips, all 8 of its tags flip to face the other half of the field.**
   - Mount the camera pitched **up about 35–40°** at about 12 in lens height. At 35° the up-cell tags stay in view from about 2 to 10 ft. Reject inverted (down-cell) tags in software by roll.
7. **FTC SDK v12.0 (2026-09-07) adds AprilTag *clusters* for BIOBUZZ** (VERIFIED [S13][S14]).
   - The cluster origin is the **centre of the CELL opening**.
   - One visible tag gives a full 6-DoF pose.
   - **|roll| < 90° means that cell is right-side-up and scorable.**
   - The SDK states that BIOBUZZ tags "move" and are **not suitable for absolute field localization**.
   - So use dead-wheel odometry (Pinpoint) for position, and use tags for aiming and HIVE-state detection.
8. **One Limelight cannot both aim at the HIVE and see pollen on the floor.** Pipeline switching takes milliseconds ("a frame or two"), but the physical pitch conflicts: about +35–40° up for tags versus about 20–30° down for floor balls. Options: a second camera (UVC webcam with the SDK colour locator), a servo tilt mount, or skip floor vision.
9. **The REV hubs share servo power in pairs.** Each hub gives 2 A per servo-port pair and 5 A total at 5 V (VERIFIED [S17]). One stalled goBILDA servo draws 2.0–2.5 A. Put high-load servos (turret, any servo intake) on a **REV Servo Hub** (6 V, 15 A, RS485, $90) or a **goBILDA 6 V Servo Power Injector** (24 A, $69.99).
10. **Highest-value additions:**
    - goBILDA **Pinpoint** with 2 pods. Either the goBILDA pack at $279.99, or Pinpoint V2 $79.99 plus 2 SWYFT linear pods at $64.99 each.
    - 2–3 **Brushland Color Rangefinders** at $25 each. They emulate the REV Color Sensor V3 but also output fast digital or analog signals. Use them for ball count/colour, the G407 4-ball limit and G408 opponent-NECTAR rejection.
    - A beam-break at the shooter feed (Adafruit, $2.95).
    - A turret encoder or home switch.
    - LEDs plus **gamepad rumble/LED** (free in the SDK) for driver feedback.
    - A resistive grounding strap ($5.50).

---

## 1. What the photos show (as-built inventory)

| Item | What is visible | Confidence |
|---|---|---|
| Drive module (PXL_..._003126527.jpg) | Round black housing printed "SWYFT" and "SWYFTROBOTICS.COM DRIVE V2". A ring of nylock nuts around the rim (mecanum roller axles). Bolted to a black perforated steel plate (consistent with the SWYFT frame kit [S1]). A red/black power lead with a yellow **XT30** passes the module. This is probably main power, because the Spike motor itself uses JST-VH power and JST-XH encoder connectors [S1]. | High |
| Control electronics (…002925221.jpg) | REV Control Hub and REV Expansion Hub mounted vertically in red 3D-printed holders, labels and LEDs visible (helps R606). A **goBILDA 12V NiMH Nested Battery** (label reads 3100-0012-00…, a legal pack per Table 12-4, M p.135) strapped with Velcro. | High |
| Turret | Large aluminium ring gear with fine external teeth on a large-diameter bearing. Driven by an aluminium pinion (≈60T, goBILDA-style hub-mount gear) mounted on a **grey servo** under a goBILDA bracket. The ring-to-pinion ratio is roughly 4–6:1 (ESTIMATE from the photo; count the teeth). | Medium |
| Shooter | Orange 3D-printed C-shaped hood wrapping a **single rubber-tyred wheel** (about 72–75 mm; the printed size is illegible) on a hex shaft. A cylindrical motor with a red sleeve is mounted on the hood, with a small gear near the wheel. This is consistent with the **6000 rpm Yellow Jacket driving the flywheel ON the turret**. A grey servo on the hood side is probably a hood-angle or gate actuator. | Medium |
| Intake (…002335726.jpg) | Full-width double roller of black compliant star/spiked wheels on two shafts, coupled by two large aluminium spur gears so they counter-rotate (pinch intake). The coach says a goBILDA Dual Mode Servo drives it. | Medium |
| Wiring | Several 4-wire (red/black/white/blue) JST-PH sensor cables are visible, but the sensors are not identified. A blue LED is visible above the drive module (unidentified). | Low |

**Interpretation of "6000 rpm motor for turret".** The photos fit "the flywheel motor that rides on the turret", with turret rotation done by the servo and pinion. Section 4 covers both readings.

---

## 2. SWYFT Drive V2 (SR-DRIVE-02)

### 2.1 What it is (VERIFIED unless noted, [S1][S2])

| Spec | Value |
|---|---|
| Type | Self-contained **mecanum** wheel module with the gearbox inside the wheel. SWYFT's example is "robot-centric mecanum teleop". The wheel has 11 rollers. |
| SKU (coach's) | **SR-DRIVE-02** = 86 mm, 40A rollers, **module pack of 4, $389.99**. Frame kit SR-DRIVE-FRAME-02 is $74.99 (86 mm only). The 110 mm variant is SR-DRIVE-110mm-02 ($384.99, 30A rollers with "traction spikes"); its frame kit takes either wheel. |
| Motor | **SWYFT Spike, SR-MOTOR-DC-01** (550 motor). 12 V, 6000 rpm free, 0.4 A max no-load, 9.2 A max current, 1.47 kg·cm max torque. Legal: listed in R501 Table 12-1, M p.132. |
| Encoder | 2-channel quadrature, "28 PPR", 3.3 or 5 V. The connector is **JST-XH 4-pin**. REV hub encoder ports are JST-PH 4-pin, so you need an XH→PH cable. SWYFT's "PPR" means counts after ×4 decoding; its odometry example uses 4096 "PPR" = 34.31 ticks/mm on a 38 mm wheel [S3]. |
| Motor power connector | JST-VH 2-pin. Plugs straight into a REV hub motor port. |
| Gear ratio | **12.7:1** only (all-metal; 4140 steel pinion and planets, 6061 ring gear). |
| Wheel speed | "approximately 475 RPM" (6000/12.7 = 472 rpm, DERIVED). |
| Advertised top speed | **6.4 ft/s** (86 mm) / 8.2 ft/s (110 mm). |
| Module mass | 575 g (86 mm) / 640 g (110 mm) including motor and gearbox. Four modules weigh 2.3 kg (5.1 lb). |
| Design load | Robots "up to 40 lbs". |
| Bearings | Dual 40×50×6 mm. |
| Mounting | M4 tapped holes on the front face, M3 on the back face. The frame kit uses steel plates and aluminium channel. |
| Docs/CAD | CAD Google Drive folder, Onshape configurator, AMB drivetrain simulator, 86 mm assembly video (youtu.be/Wwqk6szAvVU), spare-parts page [S1]. |
| Pushing force | **Not published.** |

### 2.2 Derived numbers (DERIVED)

- **Theoretical free speed (86 mm):** π × 0.086 m × 472/60 = 2.13 m/s = **6.98 ft/s**. SWYFT's 6.4 ft/s is about 92% of that. Under load, plan on about 5.5–6 ft/s cruise.
- **Encoder resolution at the wheel:** 28 × 12.7 = **355.6 counts per wheel revolution**, or 33.4 counts/in at 86 mm. At free speed that is about 2,800 counts/s.
- **SDK motor type.** There is probably no "SWYFT" motor type in the SDK. **"goBILDA 5202/3/4 series 435 rpm" has almost the same maximum ticks/s**: 435 × 384.5/60 = 2,788 versus SWYFT's 2,800. If you use RUN_USING_ENCODER, choose that type. FTC Docs stress choosing the correct motor type because the encoder modes depend on it [S29]. This choice is INFERENCE; verify it.
- **Thrust (rough).** Wheel stall torque ≈ 0.144 N·m × 12.7 × 0.85 efficiency ≈ 1.56 N·m. That is about 36 N (8 lbf) per wheel and about 33 lbf for four wheels, limited by the motors at stall. Real pushing is lower: mecanum rollers have a lower friction coefficient than traction wheels (gm0 [S30]). Four stalled motors would draw about 37 A against the battery's **20 A fuse** (Section 6.4).

### 2.3 Programming notes

- **Kinematics.** Standard gm0 robot-centric formula (VERIFIED [S31]):
  - `den = max(|y|+|x|+|rx|, 1)`
  - FL = (y+x+rx)/den, BL = (y−x+rx)/den, FR = (y−x−rx)/den, BR = (y+x−rx)/den
  - x is multiplied by **1.1** to correct imperfect strafing.
  - For field-centric driving, rotate (x, y) by −heading first. The heading can come from the Pinpoint.
- **Directions.** SWYFT's sample reverses the **right** side, uses hardware names `frontLeft/backLeft/frontRight/backRight`, **BRAKE** on all four motors and RUN_WITHOUT_ENCODER [S1]. gm0 notes that the side to reverse depends on gearing: "If your robot moves backwards when commanded to go forwards, reverse the left side instead" [S31]. The in-wheel planetary means you cannot assume either convention, so test it.
- **Wheel orientation.** "When viewed from above, the rollers of each wheel should point towards the center of the robot, forming an X shape" [S30]. Check this before blaming the code for bad strafing.
- **Zero-power behaviour.** BRAKE is SWYFT's default and good for precise stops. Consider FLOAT plus a deceleration slew limit when carrying balls if hard stops spill them (ESTIMATE).
- **Strafing** is inherently slower than driving forward on mecanum [S30].

### 2.4 Odometry options

The drive encoders slip on mecanum, so use dead wheels (gm0 recommends a separate odometry mechanism with mecanum [S30]).
- **goBILDA Pinpoint plus 2 pods.** Recommended; details in §8.1. It uses **no hub encoder ports**, which matters because four drive motors, the flywheel and the turret already use 6 of the 8 ports.
- **SWYFT Linear Odometry Module** (SR-ODOMETRY-LINEAR-01, $64.99).
  - 38 mm omni wheel with 80A PU rollers.
  - 4096 counts/rev magnetic encoder, so 34.31 ticks/mm.
  - 22 mm spring travel with 10 mm recommended preload; 4 linear bearings; 4 springs (use all 4 for maximum downforce, or 2 for less drag).
  - 110 g, 43×43×80 mm, JST-PH 4-pin cable included.
  - Use 2 with a Pinpoint, or 3 if wiring to hub encoder ports [S3]. SWYFT's Pinpoint example uses `setEncoderResolution(34.31, MM)` [S3].
  - Its ticks/mm is under the Pinpoint's 256,000 events/s limit up to about 24 ft/s (DERIVED).

### 2.5 Known issues / reviews

- **No independent reviews or failure reports found** for SWYFT Drive V2. Searches of Reddit, Chief Delphi and the SWYFT docs turned up nothing; the SWYFT docs site only covers the original "SWYFT Drive" with a NeveRest motor [S2].
- General mecanum caveats apply: lower traction, poor pushing power, no redundancy if one module fails [S30].
- Product-specific risks (INFERENCE):
  - The 550 motors heat up under sustained pushing.
  - The 12.7:1 ratio is fixed.
  - The motor sits inside the wheel envelope, so check motor-lead strain relief.
  - The four modules together can exceed the 20 A fuse when stalled.
- The 40A rollers on the 86 mm version should be gentle on the foam tiles (R201).

---

## 3. Limelight 3A

### 3.1 Legality

- **R702 Table 12-9** lists the **Limelight 3A (LL_3A)** as the *only* supported programmable vision coprocessor. R702 Example 6 explicitly names the **Limelight 3G as prohibited** (M p.140-141). The Limelight 4 is also not on the list.
- R707/R708 (M p.143): USB is for vision devices, single-image-sensor devices natively supported by the RC app, plus USB hubs and an Expansion Hub.
- R704 D (M p.141): no continuous video streaming over the RC Wi-Fi during matches, and third-party streaming tools are prohibited. Do not have the Limelight web UI or Pedro/FTC Dashboard panels open during a match.
- **Action: confirm the camera is a 3A.** The 3A is USB-C only with no Ethernet jack; the 3G and 4 have Ethernet [S8].

### 3.2 Specifications (VERIFIED [S8][S9][S10])

| Spec | Value |
|---|---|
| Sensor | OV5647 colour **rolling shutter**, 640×480 at 90 fps (up to 60 fps at 1280×960 per vendors) |
| FOV | **H 54.5°, V 42°** |
| Pipelines | 10 hot-swappable: AprilTag/localization, colour blob (90 fps), **neural detector and classifier on CPU only (no Coral)**, barcode, Python (OpenCV 4.10) |
| AprilTag rate | **50 fps at 640×480** (2× downscale); **20 fps at 1280×960** (2× downscale) |
| Power | 4.1–5.75 V over USB, **4 W max**. The enclosure is a heatsink and runs hot. |
| Connection | USB-C. FTC: USB-C→USB-A cable into the **Control Hub USB 3.0 (blue)** port |
| Size/mass | 72.11 × 48.11 × 16.80 mm; 0.20 lb (≈93 g with cable per ServoCity) |
| Mounting | 4× #10 through-holes, 4× M3, 6× M4 threaded. Recommended: ≥2 M3/M4 screws into REV/goBILDA channel. |
| Boot | Green status light active about 15–20 s after power-up |
| Price | $189 (ServoCity 3122-0002-0001) |
| LEDs | None |

### 3.3 FTC SDK integration (VERIFIED from the SDK 12.0 Javadoc [S11][S12] and Limelight's FTC guide [S9])

- **Setup.**
  - In the Limelight web UI, set the team number and restart the vision client.
  - In the Driver Station, use Configure Robot → Scan and rename "Ethernet Device" to `limelight`.
  - In code: `Limelight3A ll = hardwareMap.get(Limelight3A.class, "limelight"); ll.setPollRateHz(100); ll.pipelineSwitch(n); ll.start();`. The poll rate is clamped to 1–250 Hz and should be set before `start()`.
- **Limelight3A methods.** `getLatestResult()`, `start/pause/stop`, `isConnected()`, `getStatus()`, `getTimeSinceLastUpdate()`, `pipelineSwitch(int)` (fire-and-forget), `reloadPipeline()`, `updateRobotOrientation(yaw)` (for MegaTag2), `uploadFieldmap(LLFieldMap, Integer)`, `uploadPipeline(json, idx)`, `uploadPython`, `updatePythonInputs`, `captureSnapshot`, `deleteSnapshots`, calibration getters, `shutdown()`.
  - `getLatestResult()` reads the latest result from a background poll, so it should cost little loop time (INFERENCE from the API design: "start() starts or resumes periodic polling").
- **LLResult methods.**
  - `isValid()`, `getTx/Ty` (degrees), `getTxNC/TyNC`, `getTa`.
  - Pose: `getBotpose()` (MegaTag1), `getBotpose_MT2()`, `getBotposeTagCount/Span/AvgDist/AvgArea`, `getStddevMt1/Mt2`.
  - Lists: `getFiducialResults()`, `getColorResults()`, `getDetectorResults()`, `getClassifierResults()`, `getBarcodeResults()`.
  - Timing: `getCaptureLatency()` (ms), `getTargetingLatency()` (ms), `getParseLatency()`, `getStaleness()`, `getTimestamp()`, `getControlHubTimeStamp()`.
  - Other: `getPipelineIndex()`, `getPythonOutput()`.
- **FiducialResult methods.** `getFiducialId()`, `getRobotPoseTargetSpace()` (Limelight calls it "most useful"), `getCameraPoseTargetSpace()`, `getRobotPoseFieldSpace()`, `getTargetPoseCameraSpace()`, `getTargetPoseRobotSpace()`, `getTargetXDegrees/YDegrees()`.
- **Freshness check.** Limelight's sample treats staleness under 100 ms as good [S9].
- **Latency.** Frame period is 20 ms at 50 fps for AprilTags or 11 ms at 90 fps for colour. Measure the real end-to-end delay as `getCaptureLatency()+getTargetingLatency()` (ESTIMATE: a few tens of ms).
- **MegaTag2.** It needs robot yaw every frame (`updateRobotOrientation`), a field map, and the camera's robot-space pose. It is designed to give a good pose from a single tag and removes MT1's ambiguity [S16]. **But the field map assumes the tags do not move.** See §3.5.

### 3.4 BIOBUZZ tag geometry and camera mounting (DERIVED from M Fig. 9-8 to 9-17, p.70-77)

**Inputs from the manual.**
- Pivot at 43.95 in.
- Up-cell opening bottom at 53.5 in and top at 65.6 in.
- Down-cell lowest point at 30.6 in.
- The HIVE rests 30° from level in each stable state.
- CELL opening 20 in wide × 14 in tall, about 12 in deep.
- Tag cluster: four 3.25 in 36h11 tags on the **bottom face** of each CELL, facing the tiles, with the bottom edge toward field centre (p.75). The reference-hole centreline is 9.938 in from the front of the cell; tag centres are 2.75 in toward the front from that line (Fig. 9-15).
- IDs: red far-side cell 30-33, red audience-side cell 34-37, blue audience-side cell 38-41, blue far-side cell 42-45 (p.76, confirmed visually).

**Model** (reproduces the manual's 65.6 in opening top to within 0.02 in):

| Feature | Height above tiles | Horizontal position (from HIVE pivot line, + toward the up cell) |
|---|---|---|
| Up-cell opening centre | ≈ **59.6 in** | ≈ +17.4 in |
| Up-cell tag-cluster centre | ≈ **49.7 in** | ≈ +14.8 in |
| Down-cell tag-cluster centre | ≈ **34.0 in** | ≈ −12.4 in |
| Normal of every tag on that HIVE | points **down, and 30° toward the up-cell side** | — |

**Key consequences**

1. The up cell's opening faces outward (toward the nearer audience or far wall) and 30° upward, so robots launch from **the outer side of the up cell**. **Both cells' tags on that HIVE face that same side.**
   - From there the camera sees the up-cell tags upright and higher in the image.
   - It also sees the down-cell tags **upside-down**, lower in the image and about 80–130 in away.
   - This matches a Chief Delphi report that the tags "appear upside down" and that the camera should be "rotated upward enough" to see only 4 tags [S18].
   - It also matches the SDK rule "|roll| < 90 ⇒ right-side-up / scorable" [S14].
   - Manual Fig. 9-10 (front view) shows red tags but no blue tags from the audience side, consistent with this model.
2. **After a TIP, all 8 tags of that HIVE face the other half of the field.** A robot that just tipped its HIVE from (say) the audience side **loses sight of its own alliance's tags** and must drive to the far half to aim again.
   - The newly lowered cell's opening now points down toward the side the robot is on. Its contents spill there, which brings G409 into play (no catching).
   - At match start the red audience cell is up (red launches from the audience half) and the blue far cell is up (blue launches from the far half). So at the start each alliance mostly sees only its own HIVE's tags.
3. **View table**: lens height 12 in; D = horizontal distance from the camera to the opening centre; tag pixel width at 640 px; obliquity = angle between the tag normal and the camera ray.

| D (in) | Up-tag elevation | Up obliquity | Up range (in) | Up tag px (640) | Down-tag elevation | Down obliquity | Down px |
|---|---|---|---|---|---|---|---|
| 24 | 54.8° | 5° | 46 | 47 | 22.2° | 38° | 38 |
| 36 | 44.3° | 16° | 54 | 41 | 18.5° | 42° | 32 |
| 48 | 36.7° | 23° | 63 | 35 | 15.8° | 44° | 27 |
| 60 | 31.1° | 29° | 73 | 30 | 13.8° | 46° | 24 |
| 72 | 26.8° | 33° | 84 | 26 | 12.2° | 48° | 21 |
| 96 | 20.9° | 39° | 106 | 21 | 9.9° | 50° | 17 |
| 120 | 17.1° | 43° | 128 | 17 | 8.3° | 52° | 14 |
| 144 | 14.4° | 46° | 151 | 14 | 7.2° | 53° | 12 |

   - An 8 in lens height raises elevations by about 2–3°; a 16 in height lowers them by about 3°. The full tables are in the script output.

4. **Recommended mount (ESTIMATE): pitch up about 35° at about 12 in lens height.**
   - The visible elevations are then about 14°–56°. Keeping about 3° of margin, the up-cell tags stay in frame from D ≈ 26 in to ≈ 120 in (about 2–10 ft).
   - Down-cell tags will also appear when D < about 5 ft, so reject them in code by roll/orientation or by ID plus a known HIVE state.
   - Pitching up 40° removes the down tags beyond about 3 ft but limits range to about 7.5 ft.
   - Limelight's own guidance is to mount above or below tag height, angled so the target is centred, and to avoid head-on views that cause tag flipping [S15]. Our geometry already gives favourable obliquity (≤45° out to 10 ft).
5. **Pixels and range.**
   - At 640×480 the tag is about 21 px wide at 8 ft and about 14 px at 12 ft.
   - Higher capture resolution improves 3D accuracy and range at lower fps, and detector downscale trades range for fps without hurting 3D accuracy [S15].
   - Suggestion: start at 640×480 for 50 fps aiming. Use 1280×960 (20 fps) only if long shots are needed (ESTIMATE).
   - Exposure: as low as possible while still detecting tags, to fight motion blur from the rolling shutter [S15]. Use **ID filters** (for example `30-37` for red) to drop the opponent's tags [S15].
6. **Chassis versus turret mount.**
   - **Turret-mounted.** The camera always looks along the shot, and `tx` approximates the turret error, which is simple. Costs:
     - The USB cable must flex through turret travel.
     - The camera's robot-space pose changes with the turret, so Limelight botpose/MegaTag, which assume a fixed camera pose, are wrong unless you do your own transform.
     - It is exposed to turret vibration (TRC guide: "Bolt the Limelight to the frame, not to a moving arm or turret" [S19]).
   - **Chassis-mounted (fixed).** Requires turret-angle math using the turret encoder, but gives clean poses. Pick one deliberately; both are workable.
7. **Clearances.** The robot's own hood or turret must not block the upward view. Also note R105's 18 × 24 × 29 in expansion limit (M p.123).

### 3.5 Localization and the moving HIVE

- **SDK v12.0 release notes:** "Unfortunately, since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization." The new cluster origin is "the center of the Cell opening for easy aiming" [S13]. FTC Docs add the following [S14]:
  - "only a minimum of one of the tags needs to be detected in order to detect the whole cluster";
  - check `percentClusterFound` and `metadata`, where the name starts with "R" or "B";
  - roll with |roll| < 90 means "right side up and the CELL is scorable".
- **Limelight field maps (COMMUNITY [S18]).**
  - "Limelight only has one map slot and all 8 pipelines share it." Teams kept two maps per alliance and swapped them by hand.
  - There is no Limelight-side filter for upside-down tags.
  - The SDK does expose `uploadFieldmap(LLFieldMap, Integer)` [S11], so code *could* upload a per-HIVE-state map at runtime after a tip. This is **untested**: the upload time and behaviour mid-match are unknown.
- **Community practice (COMMUNITY [S17b]).**
  - Most teams use **Pinpoint for position** and tags only for aiming and HIVE-state detection. One team reported Pinpoint "within 1 inch of starting after driving around randomly", and another skipped the Limelight entirely at Worlds in the prior season.
  - Team 19725 solves the HIVE angle from MegaTag1 plus a "robot z≈0" constraint. They call it noisy and fuse it with Pinpoint through an EKF (repo `BluePowerRobotics/Team19725_BIOBUZZ`).
  - A poster at kickoff testing said the TIP motion is slow.
- **Recommended architecture (INFERENCE).**
  - Pinpoint pose drives navigation.
  - While the HIVE is stable (tag pose stationary across N frames), use the up-cell cluster's relative bearing and range to (a) fine-aim the turret and (b) optionally correct Pinpoint drift. Your code already knows which state is which.
  - Ignore vision while a TIP is in progress (tag pose changing) and during fast turret slews (rolling shutter).

### 3.6 Pollen on the floor: colour pipeline, neural detector, and "one camera for both?"

- **Colour pipeline** (Limelight "Color/Retroreflective", up to 90 fps). TRC's BIOBUZZ guide [S19] starts with:
  - yellow hue **18–35** (Limelight 0–180 scale), saturation 110–255, value 100–255;
  - erode/dilate 1/2, area 0.05–40%, fullness 50–100%, aspect 0.6–1.6, sort Largest;
  - lower exposure to reduce blur and glare.
  - Distance can be estimated from `ty` and the mount angle: `(h_cam − r_ball)/tan(mount − ty)`.
  - Caveat: the guide wrongly calls red and blue balls "pollen"; in BIOBUZZ they are NECTAR.
  - Re-tune at every venue. Yellow field graphics (FLOWER parts, HIVE logo panel) could cause false positives (INFERENCE).
- **Neural detector.** The 3A runs neural models **on the CPU only**. A community BIOBUZZ pollen model ("RepLiteDet-S0" by FTC 8565, Apache-2.0) reports **52.8 ms per frame (≈19 fps) on a 3A**, compared with 86.5 ms for SSD-MobileNetV2, and mAP@0.5 of 0.94 on its own eval set. It is single-class (pollen) and the dataset is unpublished (COMMUNITY [S21]).
- **Pipeline switching** is fast. Limelight: "change its pipeline in a matter of milliseconds, but your code will not wait" [S9]; TRC: "only a frame or two". Gate on `result.getPipelineIndex() == wanted` [S19].
- **Can one camera do both? Physically, not well.** The tag view needs about 35–40° *up*. A floor view needs about 20–30° *down* to see roughly 6 in to 4 ft ahead of the intake (TRC [S19]). Options:
  - (a) **Second camera.** For example the goBILDA Global Shutter USB Camera (3122-0004-0001, $74.99, OV9782, UVC, about 70° FOV [S22]). The SDK 11.1 added goBILDA USB-camera calibrations [S13]. Run the SDK `ColorBlobLocatorProcessor` on the Control Hub.
    - USB caveat: the CH's USB 2.0 port shares a bus with the Wi-Fi radio, and ESD there can drop Wi-Fi [S20][S23]. Use a hub on the 3.0 port or protect the 2.0 port with ferrites.
  - (b) **Servo tilt mount** for the Limelight (costs 1 servo; recalibrate the pitch for each pose).
  - (c) **Skip floor vision.** Pollen is mostly at known staging spots (FLOWERS, GARDENS, pre-loads), and drivers can see spills. **Recommended priority: HIVE aiming first, floor vision later.**

### 3.7 Known pitfalls and checklist

1. Plug into the **USB 3.0** port with an **A-to-C cable**. "USB C-to-C cables do not work properly with the Control Hub" [S20].
2. **ESD** (FTC Docs [S23]):
   - add ferrite chokes to USB and signal cables;
   - mount the electronics on non-conductive material, keeping ≥3/8 in of air gap or tape from the frame;
   - use a **FIRST-approved resistive grounding strap** (REV-31-1269, $5.50, or SWYFT SR-Ground-01; R605 Table 12-6, M p.137).
   - One forum thread links Limelight freezes to heat and ESD (COMMUNITY [S24]).
3. Strain-relieve the USB-C plug. Avoid routing the cable through turret rotation without a service loop.
4. **Updating LimelightOS erases pipelines.** Export the pipelines to Git first [S8].
5. Boot takes 15–20 s. `getLatestResult()` can be null or invalid early, so always null-check [S8][S9].
6. Keep the heatsink enclosure ventilated [S8].
7. Rolling shutter: limit exposure; distrust frames during fast robot or turret rotation. MegaTag2 sample code rejects updates above 360°/s [S16].
8. Anecdote: one team lost web-stream access at :5801 after a Control Hub update (COMMUNITY [S18]).

---

## 4. goBILDA 5203-2402-0001 Yellow Jacket, 6000 rpm (1:1)

### 4.1 Specifications (VERIFIED [S6])

| Spec | Value |
|---|---|
| Ratio | 1:1 (planetary housing, no reduction) |
| Voltage | 12 V nominal |
| No-load speed | **6,000 rpm** |
| No-load current | 0.25 A |
| **Stall current** | **9.2 A** |
| **Stall torque** | **1.47 kg·cm (0.144 N·m, 20.45 oz-in)** |
| Encoder | **28 counts/rev at the output shaft** (magnetic Hall, 3.3–5 V) |
| Connectors | Power: 3.5 mm FH-MC bullets (needs a bullet→JST-VH adapter for REV hubs). Encoder: 4-pos JST-XH (needs XH→PH). |
| Shaft | 8 mm REX, 24 mm long |
| Mass | 391 g |
| Price | $54.99 |
| Legality | goBILDA 5201–5204 are in R501 Table 12-1 (M p.132). R501 also allows "with or without the provided gearbox, and/or with any other compatible gearbox". |

**DERIVED**
- Peak mechanical power = 0.25 × τ_stall × ω_free = 0.25 × 0.144 × 628 = **22.6 W at 12 V**.
- Speed scales with voltage: about 5,500 rpm free at 11 V.
- Mechanical time constant τ_m = I·ω_free/τ_stall.

### 4.2 As a flywheel motor: suitable, with caveats

**Required ball exit speed** (DERIVED; [S-calc]). Target: pass through the up-cell opening centre (≈59.6 in high) while descending at ≥10°. The opening faces outward and 30° up, so descending entries of about 20–40° are ideal (INFERENCE). Drag uses an assumed Cd = 0.5 for both balls; the masses are 0.055 lb / 0.091 lb and diameters 2.8 / 3.6 in [S27]. Both balls have about the same area-to-mass ratio, so they fly alike *if* they leave the shooter at the same speed.

| D from opening (ft) | Min speed, no drag (launch at 12–18 in) | With drag estimate | Best launch angle |
|---|---|---|---|
| 2 | 4.7–5.0 m/s | 4.9–5.2 m/s | 75–77° |
| 4 | 5.1–5.4 | 5.3–5.6 | 65–67° |
| 6 | 5.6–5.8 | 5.9–6.1 | 59–62° |
| 8 | 6.0–6.2 | 6.4–6.6 | 56–58° |
| 10 | 6.5–6.6 | 7.0–7.1 | 54–56° |
| 12 | 6.9–7.0 | 7.5–7.7 | 51–54° |

**Wheel speed needed.**
- A single wheel with a fixed hood ideally launches the ball at about **½ the wheel surface speed**, because the ball rolls on the hood. Real shooters lose more to slip and compression (ESTIMATE: 70–90% of ideal).
- With a **72 mm wheel**, 5–7.7 m/s ideally needs about 2,650–4,100 rpm, or **roughly 3,000–5,000 rpm** after losses. That is up to about 85% of free speed, which is thin headroom when the battery sags to about 11 V.
- A **96 mm wheel** needs about 2,000–3,100 rpm ideally (about 2,300–3,800 rpm real). That gives more headroom and more inertia.

| Wheel | 3,000 rpm | 4,000 rpm | 4,500 rpm | 5,000 rpm | 6,000 rpm |
|---|---|---|---|---|---|
| 72 mm surface / ideal exit | 11.3 / 5.7 m/s | 15.1 / 7.5 | 17.0 / 8.5 | 18.8 / 9.4 | 22.6 / 11.3 |
| 96 mm surface / ideal exit | 15.1 / 7.5 | 20.1 / 10.1 | 22.6 / 11.3 | 25.1 / 12.6 | 30.2 / 15.1 |

**Spin-up time and speed drop per shot** (DERIVED; I = flywheel-assembly inertia, no friction; each shot takes about (5/24)·m·v_s² for translation plus spin, ×1.5 for losses):

| Inertia | Time 0→70% speed | Drop per pollen shot at 4,500 rpm (72 mm) | Drop per NECTAR shot |
|---|---|---|---|
| 1×10⁻⁴ kg·m² (light wheel) | 0.52 s | ≈11% | ≈18% |
| 2×10⁻⁴ | 1.05 s | ≈5% | ≈9% |
| 4×10⁻⁴ | 2.1 s | ≈3% | ≈4% |

- Recovery from a 5–10% dip takes about 0.2–0.4 s at 12 V (ESTIMATE).
- **Recommendation:** add inertia, for example a steel or brass flywheel mass or a heavier wheel, to land near 2–4×10⁻⁴ kg·m². A second 6000 rpm motor on the same shaft (allowed by R503 if the budget permits) speeds up recovery and spin-up.

**Two ball sizes (critical design issue, INFERENCE).**
- POLLEN is 2.8 in and NECTAR is 3.6 in (M p.74), and they "are not perfectly spherical and may vary in size".
- A fixed hood gap cannot compress both well. Either the pollen slips or the NECTAR jams.
- Options: a **sprung or compliant hood**; a servo-adjusted hood gap; or design the shooter for POLLEN only (40 per match) and score NECTAR (FLOWERS, last 60 s per G410) another way.

**Velocity control on REV hubs.**
- Use `DcMotorEx` in RUN_USING_ENCODER and `setVelocity(ticksPerSec)`. The hub firmware closes the loop on the motor port, so Control Hub versus Expansion Hub placement does not change loop latency (INFERENCE).
- Starting gains from REV's FIRST Global guide [S25]:
  - measure maxV with power = 1 on a full battery, then **F = 32767/maxV**, **P = 0.1·F**, **I = 0.1·P**, **D = 0**;
  - for example, maxV ≈ 2,600–2,800 ticks/s gives F ≈ 12;
  - re-apply the gains in every OpMode.
- REV's DECODE starter-bot guide used a custom loop, `power = F·target + P·(target − v)` with F and P starting at 0.0004. It fed only when the velocity was within −50…+100 ticks/s of target [S26].
- **Voltage sag.** Open-loop flywheel speed scales with battery voltage, so always run closed loop. Choose targets you can still reach at about 11 V. For a custom loop, scale the feed-forward by 12 V / V_batt using `hardwareMap.voltageSensor`, read at a few Hz (INFERENCE; voltage-compensation discussion [S28]).
- **Encoder resolution.**
  - 28 counts/rev gives only about 2,100 counts/s at 4,500 rpm, so short-window velocity estimates are coarse. One count in a 10 ms window is about ±5%. Average or filter in software (DERIVED).
  - **Do not fit a high-resolution encoder to the flywheel.** The Expansion Hub reports velocity as a signed 16-bit counts/s value. An 8192-count encoder overflows at about **4 rev/s (240 rpm)** [S32]. 28 counts at 6000 rpm is only 2,800 counts/s, well inside the limit.
- **ZeroPowerBehavior FLOAT** on the flywheel lets it coast instead of dumping energy into a hard brake (INFERENCE).
- **Heat.** A flywheel runs near no-load most of the time (0.25 A no-load), so heat is low. The risk is long stalls from a jammed ball at 9.2 A, so add a jam-detect timeout (INFERENCE).

### 4.3 Why it is a poor motor to rotate a turret directly, and what to use instead

- **No reduction.** Even a 4–6:1 ring gear leaves the turret able to spin at more than 1,000 rpm. Control would be twitchy and the motor would sit at near-stall currents while holding.
- **Resolution.** 28 counts × 5:1 = 140 counts per turret revolution = **2.6° per count**. Aiming needs better than about 1° (see below).
- **Back-drivable.** Impacts move the turret, and holding position needs continuous current.
- **Aiming tolerance (DERIVED).** The opening is 20 in wide; minus the ball radius (1.4 in pollen), that leaves about ±8.6 in. That is **±4.1° at 10 ft and ±8° at 5 ft**, so about 1° of turret precision is plenty.

**Turret-drive options (DERIVED; ring reduction R is the ring teeth ÷ pinion teeth):**

| Option | Turret speed | Resolution | Notes |
|---|---|---|---|
| 5203 **312 rpm** (19.2:1, 537.7 counts/rev [S7b]) at R = 5 | 62 rpm (374°/s) | 2,688 counts/rev, **0.13°/count** | Recommended DC option. 24.3 kg·cm stall. Needs a home switch. |
| 5203 **435 rpm** (13.7:1, 384.5 counts/rev, 18.7 kg·cm [S7]) at R = 5 | 87 rpm (522°/s) | 0.19°/count | Faster; slightly less holding torque |
| 5203 223 rpm (≈751.8 counts/rev) at R = 5 | 45 rpm | 0.10°/count | Slower, more precise |
| **goBILDA 5-turn Dual Mode Servo** (1800°, 0.90°/µs) at R = 4–5 | ≈10–12 rpm turret at 6 V (servo 50 rpm Torque version) | 360–450° range; 0.18–0.23° of turret per µs; 4 µs deadband (≈0.8°) | Needs the **full 500–2500 µs range** (`setPwmRange`); "average radio" 1000–2000 µs gives only 2.25 turns. The Speed variant (2000-0025-0503, $49.99) is faster. Absolute position feedback is internal; there is no external encoder. |
| CR servo (goBILDA 25-3/25-4 in CR mode, or Axon MAX CR) + **REV Through Bore Encoder V2** on the turret axis | Servo speed ÷ R | 8,192 counts/rev = **0.044°/count** | Needs a closed loop in code. Max encoder speed 10,000 rpm. FTC hubs read only its incremental (quadrature) output, not the absolute PWM [S34]. |
| Standard 300° servo (what it looks like now) at R = 4–5 | — | **only 60–75° of turret range** | Too little travel for a turret. |

- Any relative encoder needs a **known start angle**: a hard stop in the starting configuration plus a REV Magnetic Limit Switch (REV-31-1462, $17.50, active-low digital, about 10 mm detection [S35]) for re-homing.
- **Cable management.** The flywheel motor and encoder, hood servo and possibly the camera ride on the turret. Limit travel to under about ±180–270° with a service loop, or use a properly rated slip ring (≥10 A for the motor; INFERENCE).

---

## 5. goBILDA Dual Mode Servos (2000 Series)

### 5.1 Specifications (VERIFIED [S36][S37][S38][S39])

| Model | 4.8 V | 6.0 V | 7.4 V | Stall current (4.8/6.0/7.4 V) | Gear ratio | Range | Price |
|---|---|---|---|---|---|---|---|
| **2000-0025-0002 "Torque" (25-2)** | 40 rpm, 17.2 kg·cm | **50 rpm, 21.6 kg·cm** | 60 rpm, 25.2 kg·cm | 2.0 / 2.5 / 3.0 A | 300:1 | 300° (0.15°/µs over 500–2500 µs) | $36.99 |
| **2000-0025-0003 "Speed" (25-3)** | 90 rpm, 7.9 kg·cm | **115 rpm, 9.3 kg·cm** | 145 rpm, 10.8 kg·cm | 2.0 / 2.5 / 3.0 A | 135:1 | 300° | $36.99 |
| 2000-0025-0004 "Super Speed" (25-4) | 180 rpm, 4.0 kg·cm | **230 rpm, 4.7 kg·cm** | 290 rpm, 5.4 kg·cm | — | 67:1 | 300° | — |
| 2000-0025-0502 5-Turn Torque | 40 rpm, 17.2 | 50 rpm, 21.6 | 60 rpm, 25.2 | 2.0 / 2.5 / 3.0 A | — | **1800° (0.90°/µs)** | $49.99 |
| 2000-0025-0503 5-Turn Speed | — | — | — | — | — | 1800° | $49.99 |

Common traits:
- 4.8–7.4 V; 25-tooth spline; steel gears; 5 kΩ potentiometer feedback; 4 µs deadband.
- PWM 500–2500 µs in standard mode; 900–2100 µs (25-2) or 1000–2000 µs (25-3) in continuous mode.
- Weight 58–60 g; TJC8 3-pin connector.
- **Continuous mode is toggled with the 3102 Dual Mode Servo Programmer** (3102-0001-0001, $12.99).

### 5.2 Legality (R502, M p.133)

- Limits: **≤ 8 W** (0.25 × stall N·m × no-load rad/s at 6 V) and **≤ 4 A stall at 6 V**.
- DERIVED power: 25-2 = **2.77 W**, 25-3 = 2.75 W, 25-4 = 2.78 W, 5-turn 25-2 = 2.77 W. Stall is 2.5 A. **All legal.** The 2000-0025-0003 is named in R502's example list. Bring spec sheets for the others to inspection.
- Switching to continuous rotation "as specified by the manufacturer" is allowed by R504-C (M p.134).
- Inline devices that modify the signal, such as the **goBILDA Servo Travel Tuner, are prohibited** (R612, M p.139). Use the hub's extended PWM range instead.

### 5.3 5 V (hub ports) versus 6 V (servo power module)

- REV hub servo ports supply **5 V**. The goBILDA Power Injector, REV Servo Power Module, Studica block and **REV Servo Hub supply 6 V** (R502 blue box, M p.133).
- At 5 V, interpolated (DERIVED): 25-3 gives about 94 rpm and 8.1 kg·cm; 25-2 gives about 42 rpm and 17.9 kg·cm. That is about 18–20% slower than at 6 V.
- **Hub servo power limits** (VERIFIED [S17]): 2 A per port *pair* (0-1, 2-3, 4-5) and **5 A total for all servo plus 5 V aux ports on a hub**. One goBILDA servo can stall at 2.0 A at 4.8 V, so **two loaded servos on one pair can brown out**. Spread servos across pairs and move high-load servos to a 6 V module.
- **SDK PWM range.** The SDK defaults to 600–2400 µs. The hubs can output 500–2500 µs if you call `((ServoImplEx) s).setPwmRange(new PwmControl.PwmRange(500, 2500))`. That is needed for the full 300° or 1800° travel of goBILDA servos (gm0 [S40]). SDK 11.2 also added `setPulseWidth()` [S13].

### 5.4 Adequacy for the intake (2.8–3.6 in balls), feeder and indexer

- **Mechanical power** (R502 formula, DERIVED): a goBILDA servo gives **≈2.8 W at 6 V, ≈2 W at 5 V**. Any 5203 Yellow Jacket gives **≈22.6 W**, about 10× more.
- **Intake surface speed** (DERIVED):

| Actuator | 2.5 in roller | 3.5 in roller | 4.5 in roller |
|---|---|---|---|
| 25-3 CR at 5 V (≈94 rpm) | 1.0 ft/s | 1.4 | 1.8 |
| 25-3 CR at 6 V (115 rpm) | 1.3 | 1.8 | 2.3 |
| 25-4 CR at 6 V (230 rpm) | 2.5 | 3.5 | 4.5 |
| 5203 435 rpm | 4.7 | 6.6 | 8.5 |
| **5203 1150 rpm** | **12.5** | **17.6** | **22.6** |
| 5203 1620 rpm | 17.7 | 24.7 | 31.8 |

- **Rule of thumb:** intake surface speed ≈ **2× robot top speed**, so roughly 13 ft/s for a 6.4 ft/s drive (FRCDesign heuristic, via CD [S33b]).
- **gm0:** "it is highly recommended that teams dedicate at least one motor to their intake"; top FTC teams used 3.7:1 (≈1600 rpm) or 5.2:1 motors; "Touch it, own it" [S33].
- **Verdict: the servo intake will only collect while the robot creeps.** Recommend a **5203 1150 rpm** motor for 2.5–3.5 in rollers, or 435 rpm for larger rollers or a geared-down layout.
- A DC motor also gives you `getCurrent()` for **jam detection**. That is a separate hub read, so call it at a lower rate (INFERENCE).
- **Feeder / indexer ("internal spinners"):** **servos are fine.** Moving 25–41 g balls a few inches at 1–4 ft/s needs little power. Use CR mode with a 25-4 Super Speed or 25-3 at 6 V on 2–3 in wheels. A fast standard-mode servo (25-3 or an Axon MINI) suits a **gate** that holds or releases balls into the flywheel.
- **G407 (M p.108):** the robot may CONTROL no more than 4 elements. Design storage for exactly 4 and **stop or reverse the intake when 4 are sensed**.
- **G408:** do not control opponent NECTAR. Colour-sense at the intake mouth and auto-eject.

### 5.5 Alternatives

- **Axon MAX MK2** (2004-0025-0002, $99.99 at goBILDA): 34 kg·cm and 83 rpm at 6 V, 4.0 A stall, 350° range, CR via the Axon programmer ($59.99), and a **4th-wire analog position feedback** [S41]. R502: 7.25 W and 4.0 A, which is legal but at the current limit; "Axon MAX+" is in R502's example list.
- **Axon MINI MK2** (2004-0025-0001, $99.99): 25 kg·cm and 111 rpm at 6 V, 3.8 A, 7.12 W; analog feedback [S41].
- The analog-feedback wire goes to a hub analog port (0–5 V input; R502 allows "4th wire position feedback"). This makes a CR Axon a reasonable turret actuator.

---

## 6. REV Control Hub (REV-31-1595) + Expansion Hub (REV-31-1153)

### 6.1 Ports and limits (VERIFIED [S17][S17c]); the Expansion Hub has the same I/O specs as the Control Hub

| Port | Per hub | Two hubs | Key limits |
|---|---|---|---|
| Motor (JST-VH) | 4 | **8** (= R503 motor cap) | 10 A continuous, 20 A absolute (fuse-limited) |
| Encoder (JST-PH 4-pin, 3.3 V) | 4 | **8** | 500 mA total supply. Quadrature only; TBE absolute PWM not supported [S34]. Velocity reported as 16-bit counts/s [S32]. |
| Servo (0.1 in header, 5 V, 500–2500 µs) | 6 | **12** (R503 caps servos at 8) | **2 A per port pair; 5 A total including 5 V aux** |
| +5 V aux | 2 | 4 | 2 A per pair, shares the 5 A total |
| Digital (JST-PH, 2 channels per connector, 3.3 V, pull-ups) | 8 channels | 16 | 1 A total. Inputs have **pull-up resistors** (default high) [S42] |
| Analog | 4 channels | 8 | 0–5 V input; 3.3 V supply on the connector |
| I2C (4 separate buses, 100/400 kHz, 3.3 V) | 4 | 8 | **CH bus 0 hosts the internal IMU.** Expansion Hubs bought after Dec 2021 have no IMU. Same-address devices (Color Sensor V3 at 0x52) need separate buses [S42b]. |
| RS485 (JST-PH 3-pin) | 2 | — | Hub-to-hub link. You can run 2 cables for redundancy [S20]. |
| USB (CH) | USB-C (PC), USB 2.0-A, **USB 3.0-A** | — | Cameras on **USB 3.0**. The 2.0 port shares a bus with the Wi-Fi radio [S20][S23]. |
| Input power | XT30 ×2 | — | 8–15 V operating |

- **CH computer:** RK3328 quad-core A53 with 1 GB RAM, plus a TI Cortex-M4 that runs the I/O, and Wi-Fi 2.4/5 GHz.
- **Rules:**
  - R701: one Control Hub plus at most one Expansion Hub (M p.140).
  - R505: motor ports drive at most 2 motors per port; servo ports at most 2 servos per port.
  - R608: power hubs only through their XT30 ports from the main battery.
  - R613: no cross-powering of ports.

### 6.2 RS485 versus USB to the Expansion Hub

- With a Control Hub, connect the Expansion Hub over **RS485** (3-pin JST-PH), plus the XT30 power jumper. This is REV's documented method [S17c]. In the configuration it appears as "Expansion Hub 2" under the Control Hub Portal.
- USB to an Expansion Hub is mainly for phone-based controllers (R703).
- **Add a second RS485 cable for redundancy** [S20]. Strain-relieve the JST-PH plugs.

### 6.3 Bulk caching and loop time

- **Bulk reads** fetch "all sensor values (except I2C) on a hub at once". In AUTO mode a repeated read triggers a new bulk read. In MANUAL mode you must call `hub.clearBulkCache()` once per loop [S43].

```java
List<LynxModule> hubs = hardwareMap.getAll(LynxModule.class);
for (LynxModule h : hubs) h.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
// each loop:
for (LynxModule h : hubs) h.clearBulkCache();
```

- **Costs:**
  - Bulk-reading every non-I2C port on a hub takes about **3–4 ms** (Brushland [S44]).
  - "A standard I2C read on the control hub will take upwards of 4 milliseconds" (Brushland [S44]).
  - Older community estimates: about **3 ms per motor `setPower` write** and about 7 ms per IMU read (COMMUNITY [S45]).
- **Expectation (ESTIMATE):**
  - A well-written loop with 2 bulk reads, 1 Pinpoint I2C read, about 8 motor and servo writes that are only sent when changed, and Limelight results from its background thread should run at **about 8–20 ms (50–120 Hz)**.
  - Each extra I2C colour sensor adds ≥4 ms, which is why **digital-output colour sensors are preferred**.
  - Measure with a loop timer averaged over 250–1,000 iterations [S45].
  - Skip the hub IMU if the Pinpoint supplies heading.

### 6.4 Batteries and voltage sag

| | goBILDA 12V NiMH Nested (3100-0012-0020), current battery | REV 12V Slim (REV-31-1302) |
|---|---|---|
| Legal (R601 Table 12-4, M p.135) | Yes | Yes |
| Capacity / cells | 3000 mAh, Sub-C NiMH | 3000 mAh, 10 cells |
| Connector / fuse | XT30 / **20 A mini-blade (ATM)** | XT30 / **20 A ATM** |
| Mass | 597 g | 567 g (113.5 × 90.5 × 23 mm, flat) |
| Discharge | Max 30 A, limited to 20 A by the fuse; recommended charge 1 A; storage 10 V [S46] | 10C (30 A), limited to 20 A by the fuse; **min no-load 9.0 V; brief dips below 9 V under load are OK**; charge 1.5–3 A (1.8–2 A recommended) [S47] |
| Price | $64.99 | $60.00 |

- **Electrically equivalent.** The REV Slim is flatter and easier to mount low. **Buy 3–4 batteries** for an event rotation and label them.
- R601-A/B: you may replace the fuse with a COTS 20 A ATM fuse and change connectors to an equivalent type. R604: never up-rate the fuse.
- **Sag effects on the flywheel (INFERENCE):**
  - Peak voltage right off the charger is about 13–14 V. Under drivetrain acceleration it drops to about 10–11 V. Open-loop flywheel speed follows the voltage, so shots go short late in the match.
  - Mitigations: closed-loop velocity; targets ≤ about 80–85% of the speed measured at about 11.5 V; voltage-scaled feed-forward; wait for "at speed" before feeding; log V_batt.
  - The **20 A fuse** is shared by the four drive motors (36.8 A combined at stall), flywheel and intake. Sustained pushing can blow it or cause brownouts, so slew-limit drive power and avoid stalls.

---

## 7. Recommended architecture changes (summary)

1. **Intake → DC motor** (5203 1150 rpm, about $55). Free the servo for a gate or feeder.
2. **Turret → geared DC motor** (5203 312 rpm) on the existing ring, with a home switch. Alternatively, a 5-turn servo or a CR servo plus a TBE V2.
3. **Flywheel:** keep the 6000 rpm motor. Add inertia, consider a 96 mm wheel, and use closed loop with voltage-aware targets. Make the hood **compliant** if it will shoot both 2.8 in and 3.6 in balls.
4. **Odometry:** Pinpoint plus 2 pods. Use Limelight only for aiming and HIVE state.
5. **Sensing for G407/G408:** colour and presence sensors at the intake mouth and the shooter feed.
6. **Servo power:** Servo Hub or 6 V injector for high-load servos.
7. **Driver feedback:** LEDs plus gamepad rumble/LED for states such as "4 loaded", "flywheel ready", "target locked" and "our cell is up".
8. **ESD hardening:** grounding strap, ferrites, insulated electronics. Add a **top guard** so spilled HIVE balls cannot land in the robot (G409 blue box encourages guards; also G407).

---

## 8. Recommended additions: part numbers, prices, purpose

| Priority | Item | Part # | Price (USD) | Why / notes | Source |
|---|---|---|---|---|---|
| **1** | goBILDA **Pinpoint V2** Odometry Computer | 3110-0002-0002 | $79.99 | I2C 0x31, 1,500 Hz fusion, 256,000 counts/s per encoder, internal IMU (these figures are from the V1 guide; V2 is assumed similar). V2 adds a CRC8 checksum on I2C and pitch/roll [S48][S49]. Built into the SDK as `GoBildaPinpointDriver` (Pinpoint v2 supported since SDK 11.1 [S13]). After tuning, expect a return-to-start error of about 10 mm (0.5 in) [S49]. Plug into an I2C bus other than 0 [S50]. | [S48][S49] |
| 1 | Odometry pods ×2: **goBILDA 4-Bar** (32 mm, 2000 counts/rev → 19.89 ticks/mm) or **SWYFT Linear** (38 mm, 4096 counts → 34.31 ticks/mm) | 3110-0001-0002 / SR-ODOMETRY-LINEAR-01 | $99.99 ea / $64.99 ea | The goBILDA **pack** (2 pods + Pinpoint) 3203-3110-0002 is $279.99. SWYFT pods are cheaper and match the drive vendor. R303-J allows COTS dead-wheel kits (M p.128). | [S48][S51][S3] |
| **1** | **Brushland Labs Color Rangefinder** ×2–3 | — | $25 ea | Emulates the REV Color Sensor V3 over I2C, but can be **configured to output digital threshold pins** (HSV hue range plus a max distance in mm) or analog hue/distance. These are read through **bulk reads** instead of ≥4 ms I2C calls. 940 nm Class 1 VCSEL with 100 mm max range, which satisfies R710 (invisible, Class 1, part of a sensor). Use one at the intake mouth (pin0 = yellow POLLEN, pin1 = opponent-colour NECTAR, for auto-reject) and one at the 4th storage slot to stop intake at 4. | [S44] |
| 2 | REV **Color Sensor V3** (alternative) | REV-31-1557 | $20.75 | APDS-9151, I2C 0x52, RGB + proximity (1–10 cm). Each one needs its own I2C bus. Slower (I2C). | [S52] |
| 2 | **IR break-beam**, 3 mm (Adafruit #2167) | 2167 | $2.95 | Open-collector output, <2 ms response, about 25 cm range, 3.3–5.5 V. Works with REV digital pull-ups. Put one at the shooter feed for "ball fired" counting. | [S53] |
| **1** (if a servo turret) | REV **Through Bore Encoder V2** | REV-11-3174 | $48.00 | 8,192 counts/rev, ±0.5°, 10,000 rpm max. Quadrature only on FTC hubs. Fine for turret speeds (<240 rpm velocity limit [S32]). | [S34][S54] |
| 2 | REV **Magnetic Limit Switch** (turret home) | REV-31-1462 | $17.50 | Digital active-low, about 10 mm detection | [S35] |
| **1** | goBILDA 5203 motor for the **intake** (1150 rpm) | 5203-2402-0005 | ≈$54.99 | 7.9 kg·cm, 145.1 counts/rev; see §5.4 | [S7c] |
| 2 | goBILDA 5203 motor for the **turret** (312 rpm) | 5203-2402-0019 | ≈$54.99 | 24.3 kg·cm, 537.7 counts/rev | [S7b] |
| 2 | goBILDA **5-Turn Dual Mode Servo** (Speed or Torque), if keeping a servo turret | 2000-0025-0503 / -0502 | $49.99 | 1800° with the 500–2500 µs range | [S39] |
| 3 | **Axon MAX MK2 / MINI MK2** | 2004-0025-0002 / -0001 | $99.99 | Strong, with analog position feedback; at the R502 current limit (MAX) | [S41] |
| 2 | **REV Servo Hub** (or goBILDA 6V Servo Power Injector) | REV-11-1855 (3125-0001-0001) | $90.00 ($69.99) | 6 channels at 6 V (5–7.4 V configurable later). 15 A total, 3 A per channel. Connects to the CH over **RS485**; needs RC app 10.0+; set its ID 1–10. Per-channel current is not available in the FTC SDK. The goBILDA injector gives 6 V at 24 A for 6 channels, passes the signal through, and stays off until it sees a PWM signal. | [S55][S56] |
| 2 | goBILDA **RGB Indicator Light** | 3118-0808-0002 | $24.99 | PWM colour on a servo port (5–15 V recommended input). Not an actuator, so it does not count under R503, but it uses a servo port. R202-J: flashing above 5 Hz invites scrutiny. | [S57] |
| 3 | REV **Digital LED Indicator** (4-pack) | REV-31-2010-PK4 | $14.75 | Red/green/amber; 2 digital channels each | [S58] |
| **0 cost** | **Gamepad rumble/LED** | SDK `Gamepad.rumble()`, `rumbleBlips()`, `runRumbleEffect()`, `setLedColor()` | $0 | The fastest driver feedback channel. LED support depends on the controller (PS4-style pads). | [S59] |
| **1** | **Resistive grounding strap** | REV-31-1269 (or SWYFT SR-Ground-01) | $5.50 | 470 Ω, XT30 to frame. Legal per R605 Table 12-6. | [S60] |
| 2 | Ferrite chokes (USB/sensor), USB cable retention | — | ~$10 | ESD guidance | [S23] |
| 3 | 2nd camera: goBILDA **Global Shutter USB Camera** | 3122-0004-0001 | $74.99 | For floor pollen through VisionPortal, or as a backup tag camera using the SDK cluster processor | [S22] |
| 2 | Spare batteries ×2 | 3100-0012-0020 / REV-31-1302 | $64.99 / $60 | Battery rotation | [S46][S47] |
| as needed | goBILDA Dual Mode Servo Programmer | 3102-0001-0001 | $12.99 | Switch servos between CR and standard modes | [S36] |
| as needed | Cables: JST-XH→JST-PH encoder adapters, goBILDA bullet→JST-VH motor adapters | — | — | Spike and 5203 motors use XH encoder connectors; 5203 power uses bullets | [S1][S6] |

**Rough budgets (DERIVED):**
- **Core package** ≈ **$330–$390**: Pinpoint V2 + 2 SWYFT pods $210, 2 Color Rangefinders $50, break-beam $3, intake motor $55, ground strap $6, ferrites about $10, and optionally the limit switch $17.50 and LED $25.
- **Full package** ≈ **$650–$750**: the core plus a turret motor or TBE, Servo Hub, spare battery and 2nd camera.

---

## 9. Sample R503 actuator budget and port map

**R503 (M p.134):** at most **8 motors and 8 servos** from the R501/R502 lists, summed across all configurations used at an event. Non-actuator PWM devices (LED indicator) and integral sensor motors do not count.

### 9.1 Recommended configuration ("B")

| # | Motor (8 max) | Hub / port | Encoder |
|---|---|---|---|
| 1–4 | SWYFT Spike drive (FL, BL, FR, BR) | CH M0–M3 | CH E0–E3 (optional for teleop; useful for velocity drive) |
| 5 | Flywheel: goBILDA 6000 rpm (existing) | EH M0 | EH E0 (needed for setVelocity) |
| 6 | Intake: 5203 1150 rpm (new) | EH M1 | EH E1 (optional) |
| 7 | Turret: 5203 312 rpm (new) on the ring gear | EH M2 | EH E2 |
| 8 | Spare: a 2nd flywheel motor or a transfer roller | EH M3 | EH E3 (spare) |

| # | Servo (8 max) | Port / power |
|---|---|---|
| 1 | Hood angle/gap: goBILDA 25-2 Torque (existing) | CH S0 (pair 0-1) |
| 2 | Feeder/indexer ("internal spinner"): 25-4 or 25-3 in CR | CH S2 (pair 2-3) |
| 3 | Gate (hold/release to the flywheel): 25-3 standard or Axon MINI | CH S4 (pair 4-5) |
| 4 | Optional second indexer, or a Limelight tilt servo | EH S0 |
| 5–8 | Spare | — |
| (n/a) | goBILDA RGB indicator: does not count, uses a port | EH S5 |

### 9.2 Minimal-change configuration ("A": keep servo intake and servo turret)

| # | Servos | Notes |
|---|---|---|
| 1 | Intake (existing), **on a Servo Hub / 6 V** | Expect slow pickup; drive slowly into balls |
| 2 | Turret: **5-turn** goBILDA, or CR servo + TBE V2 (encoder on EH E2) | Range and feedback fixed |
| 3 | Hood | |
| 4–5 | Feeder/indexer ×2 | |
| 6 | Gate | |
| Motors used | 4 drive + 1 flywheel = 5 of 8 | 3 motor ports idle (wasted capability) |

### 9.3 Sensors and comms map (suggested)

| Device | Connection |
|---|---|
| Pinpoint (+2 pods) | **CH I2C bus 1** (not bus 0, which has the internal IMU) |
| Color Rangefinder #1 (intake mouth): pin0 = POLLEN-yellow, pin1 = opponent-NECTAR | CH digital 0-1 connector |
| Color Rangefinder #2 (storage slot 4 / "full"): pin0 = any-ball presence | CH digital 2-3 |
| Break-beam at the shooter feed | EH digital 0 |
| Turret home magnetic switch | EH digital 1 |
| REV Digital LED indicator(s) | EH digital 2-3 |
| Axon analog feedback (if used) | CH/EH analog 0 |
| Limelight 3A | **CH USB 3.0** (A-to-C cable, ferrite) |
| Optional webcam | USB hub on 3.0, or the 2.0 port with care (ESD/Wi-Fi) |
| Expansion Hub | RS485 ×2 (redundant) plus XT30 jumper |
| Grounding strap | Spare XT30 to the frame |

---

## 10. Programming quick reference (snippets)

```java
// Flywheel (hub velocity PID)
DcMotorEx fly = hardwareMap.get(DcMotorEx.class, "flywheel");
fly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
fly.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
double maxV = 2700;                     // measure: power=1, full battery, RUN_WITHOUT_ENCODER
double F = 32767.0 / maxV, P = 0.1 * F, I = 0.1 * P;   // REV/FGC starting point [S25]
fly.setVelocityPIDFCoefficients(P, I, 0, F);           // re-apply in every OpMode
fly.setVelocity(targetTicksPerSec);                    // 28 ticks/rev; 4500 rpm = 2100 t/s
boolean ready = Math.abs(fly.getVelocity() - targetTicksPerSec) < 50;  // feed window; tune

// Servos: full goBILDA travel
((ServoImplEx) hood).setPwmRange(new PwmControl.PwmRange(500, 2500));

// Limelight: own-alliance up-cell tags only
LLResult r = ll.getLatestResult();
if (r != null && r.isValid() && r.getPipelineIndex() == TAG_PIPE && r.getStaleness() < 100) {
  for (LLResultTypes.FiducialResult f : r.getFiducialResults()) {
    int id = f.getFiducialId();                       // red 30-37, blue 38-45
    Pose3D t = f.getTargetPoseCameraSpace();          // check orientation to reject inverted (down-cell) tags;
    // verify empirically which angle flips ~180 deg between up/down cells
  }
}

// Pinpoint
GoBildaPinpointDriver odo = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
odo.setOffsets(xMm, yMm, DistanceUnit.MM);
odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD); // or (34.31, MM) for SWYFT pods
odo.resetPosAndIMU();          // robot must be still
// loop: odo.update(); Pose2D p = odo.getPosition();

// Color Rangefinder in digital mode (after one-time I2C config op-mode)
DigitalChannel yellowAtMouth = hardwareMap.digitalChannel.get("crf0_pin0");
```

**Notes**
- The Brushland sample config (written for DECODE colours) sets `setPin0Digital(HSV, lo/360*255, hi/360*255)` plus `setPin0DigitalMaxDistance(HSV, 10 mm)` [S44]. For BIOBUZZ, tune yellow (≈40–70° hue), red (≈0–15° and 345–360°) and blue (≈200–240°) on real balls. These hue ranges are ESTIMATES.
- The SDK v12 VisionPortal AprilTag processor returns `AprilTagClusterDetection` objects for BIOBUZZ, with `ftcPose` referenced to the CELL opening [S13][S14].
- R704 forbids FTC Dashboard, Panels and Pedro's web tuner during matches. They are fine in practice.

---

## 11. Open questions and things to verify on the robot

1. **Which Limelight model** is it? Only the 3A is legal (R702).
2. **Ring-gear and pinion tooth counts.** What actually drives the turret (servo model and mode)? Is there any turret position feedback?
3. **Flywheel wheel diameter, motor-to-wheel ratio** (the photo shows a small gear) and **flywheel inertia**. Measure maxV and the exit speed (slow-motion video).
4. Does the hood gap grip **both** 2.8 in POLLEN and 3.6 in NECTAR? Will the team shoot NECTAR at all?
5. **Intake roller diameter** and the servo's gear ratio to the rollers. Measure the pickup rate at drive speeds of 1, 2 and 4 ft/s.
6. **How many elements tip a HIVE?** This is not published. One community post says the first tip needs about 4 balls because of the pre-staged NECTAR (COMMUNITY [S17b]). How long does the TIP take? This affects vision gating.
7. **Real `ftcPose` / Limelight orientation conventions:** which angle flips between up-cell and down-cell tags? Test on a practice CELL.
8. Does `uploadFieldmap()` at runtime work fast enough to switch maps after a tip? Untested.
9. **Loop time** with your final sensor set (target under 20 ms).
10. **Fuse and brownouts** during pushing: log battery voltage in practice matches.
11. **Drive motor direction and the SDK motor type** for SWYFT modules (the 435 rpm type is suggested as the equivalent).
12. Battery inventory and age. NiMH packs that no longer hold charge cause late-match shot drop.
13. Camera mount height and pitch. Confirm the up-cell tags stay in view across your intended shooting spots, and that robot parts do not block the view at the turret's extreme angles.

---

## 12. Sources

- [M] FIRST Tech Challenge Competition Manual TU05 (2026-10-15), local PDF. Pages cited: HIVE p.69-72; FLOWER p.72; scoring elements p.74; AprilTags p.74-77; G407-G410 p.108-109; G417-G418 p.111-112; R102/R105 p.122-123; R201-R202 p.125-126; R301-R303 p.127-128; R501 p.132; **R502 p.133** (≤8 W, ≤4 A @6 V); R503-R505 p.134; R601-R602 p.135; R603-R605 p.136-137; R608-R609 p.137-138; R612-R613 p.139; R701-R702 p.140-141; R704 p.141; R707-R710 p.143; glossary p.171-175.
- [S1] SWYFT Drive V2 product page: https://swyftrobotics.com/kits/swyft-drive-v2 (specs, SKUs, example code summary, CAD and assembly links)
- [S2] SWYFT docs index / SWYFT Drive (V1): https://docs.swyftrobotics.com/llms.txt, https://docs.swyftrobotics.com/hardware/ftc-kits/swyft-drive
- [S3] SWYFT Linear Odometry Module: https://swyftrobotics.com/kits/swyft-linear-odometry-module and its Pinpoint example code (34.31 ticks/mm)
- [S4] SWYFT Spike gearmotors: https://swyftrobotics.com/products/swyft-spike-ftc-planetary-gearmotors
- [S6] goBILDA 5203-2402-0001 (6000 rpm): https://gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder
- [S7] 5203 435 rpm specs (RobotShop/Core Electronics mirrors): https://www.robotshop.com/products/5203-series-yellow-jacket-planetary-gear-motor-1371-435-rpm-33-5v-encoder ; [S7b] 312 rpm spec sheet: https://www.gobilda.com/content/spec_sheets/5202-2402-0019_spec_sheet.pdf and https://servocity.com/5203-series-yellow-jacket-planetary-gear-motor-19-2-1-ratio-24mm-length-8mm-rex-shaft-312-rpm-3-3-5v-encoder ; [S7c] 1150 rpm: https://www.robotshop.com/products/5203-series-yellow-jacket-planetary-gear-motor-521-1150-rpm-33-5v-encoder
- [S8] Limelight 3A quick-start/specs: https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a
- [S9] Limelight FTC programming guide: https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming
- [S10] ServoCity Limelight 3A listing: https://www.servocity.com/limelight-3a-smart-camera
- [S11] SDK 12.0 Javadoc, Limelight3A: https://javadoc.io/static/org.firstinspires.ftc/Hardware/12.0.0/com/qualcomm/hardware/limelightvision/Limelight3A.html (also LLFieldMap)
- [S12] SDK 12.0 Javadoc, LLResult: https://javadoc.io/static/org.firstinspires.ftc/Hardware/12.0.0/com/qualcomm/hardware/limelightvision/LLResult.html
- [S13] FtcRobotController README release notes, v12.0 (20260907), v11.2, v11.1: https://github.com/FIRST-Tech-Challenge/FtcRobotController (README "Release Information")
- [S14] FTC Docs, AprilTag Clusters: https://ftc-docs.firstinspires.org/apriltag-clusters
- [S15] Limelight AprilTag pipeline tips: https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltags
- [S16] Limelight MegaTag2: https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-robot-localization-megatag2
- [S17] REV Control Hub specifications: https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics ; [S17c] Expansion Hub specifications and "Adding an Expansion Hub": https://docs.revrobotics.com/duo-control/control-system-overview/expansion-hub-basics , https://docs.revrobotics.com/duo-control/menu/adding-more-motors/adding-an-expansion-hub
- [S17b] Chief Delphi, "Idea about FTC Biobuzz localization?": https://www.chiefdelphi.com/t/idea-about-ftc-biobuzz-localization/524104 (COMMUNITY)
- [S18] Chief Delphi, "FTC BioBuzz Limelight Settings/FMAP": https://www.chiefdelphi.com/t/ftc-biobuzz-limelight-settings-fmap/524110 (COMMUNITY)
- [S19] Tennessee Robotics Center, Limelight 3A BIOBUZZ guide (2026-09-25): https://irp.cdn-website.com/3a1d1852/files/uploaded/TRC_Limelight_Guide_BIOBUZZ.pdf (third-party)
- [S20] FTC Docs, Control and Expansion Hub Tips: https://ftc-docs.firstinspires.org/tech_tips/tech-tips/tech-tip-hub-tips/tech-tip-hub-tips.html
- [S21] RepLiteDet-S0 BIOBUZZ pollen model card (FTC 8565): https://huggingface.co/ftc8565/replitedet-s0 (COMMUNITY)
- [S22] goBILDA Global Shutter USB Camera: https://www.gobilda.com/global-shutter-usb-camera-with-gobilda-case-100-fps/
- [S23] FTC Docs, Managing ESD: https://ftc-docs.firstinspires.org/hardware_and_software_configuration/configuring/managing_esd/managing-esd.html
- [S24] Chief Delphi, "Limelight Freezing Issue": https://www.chiefdelphi.com/t/limelight-freezing-issue/516389 (COMMUNITY)
- [S25] REV FIRST Global PIDF guide: https://docs.revrobotics.com/first-global/fgc-pid
- [S26] REV DECODE programming tips & tricks: https://docs.revrobotics.com/ftc-kickoff-concepts/decode-2025-26/programming-tips-and-tricks
- [S27] AndyMark BIOBUZZ scoring elements (2.8/3.6 in; 0.055/0.091 lb): https://andymark.com/am-5851_yellow , https://andymark.com/am-5852_red
- [S28] Chief Delphi, "Voltage compensation in FTC": https://www.chiefdelphi.com/t/voltage-compensation-in-ftc/467928 (COMMUNITY)
- [S29] FTC Docs, Motor Modes and Encoders: https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-motor-modes/tech-tip-motor-modes.html
- [S30] gm0, Holonomic drivetrains: https://gm0.org/en/latest/docs/common-mechanisms/drivetrains/holonomic.html
- [S31] gm0, Mecanum drive tutorial: https://gm0.org/en/latest/docs/software/tutorials/mecanum-drive.html
- [S32] SkyStone SDK issue #241, 16-bit velocity overflow: https://github.com/FIRST-Tech-Challenge/SkyStone/issues/241
- [S33] gm0, Active Intake: https://gm0.org/en/latest/docs/common-mechanisms/active-intake/index.html ; [S33b] Chief Delphi "Intake" (FRCDesign 2× surface-speed rule): https://www.chiefdelphi.com/t/intake/517373 (COMMUNITY)
- [S34] REV Through Bore Encoder V2 docs/specs: https://docs.revrobotics.com/rev-crossover-products/sensors/tbe , https://docs.revrobotics.com/rev-crossover-products/sensors/tbe/specs , https://docs.revrobotics.com/duo-control/sensors/encoders/through-bore-encoder
- [S35] REV Magnetic Limit Switch: https://www.revrobotics.com/rev-31-1462/
- [S36] goBILDA 2000-0025-0002: https://gobilda.com/2000-series-dual-mode-servo-25-2
- [S37] goBILDA 2000-0025-0003: https://gobilda.com/2000-series-dual-mode-servo-25-3-speed
- [S38] goBILDA 2000-0025-0004 (Core Electronics / RobotShop mirrors): https://core-electronics.com.au/2000-series-dual-mode-servo-25-4-super-speed.html
- [S39] goBILDA 5-Turn servos: https://gobilda.com/2000-series-5-turn-dual-mode-servo-25-2-torque , https://gobilda.com/2000-series-5-turn-dual-mode-servo-25-3-speed
- [S40] gm0, SDK Servos (600–2400 µs default, 500–2500 via setPwmRange): https://gm0.org/en/latest/docs/software/adv-control-system/sdk-servos.html
- [S41] Axon MAX MK2 / MINI MK2 at goBILDA: https://www.gobilda.com/axon-max-servo-mk2/ , https://www.gobilda.com/axon-mini-servo-mk2/
- [S42] REV Digital sensors (pull-ups): https://docs.revrobotics.com/duo-control/sensors/digital ; [S42b] REV I2C: https://docs.revrobotics.com/duo-control/sensors/i2c
- [S43] gm0, Bulk reads: https://gm0.org/en/latest/docs/software/tutorials/bulk-reads.html
- [S44] Brushland Labs Color Rangefinder docs (getting started, configuration, reading data, specs): https://docs.brushlandlabs.com/sensors/color-rangefinder ; store: https://www.brushlandlabs.com/
- [S45] openftc/revextensions2 issue #9 (loop-time anecdotes): https://github.com/openftc/revextensions2/issues/9 (COMMUNITY)
- [S46] goBILDA 12V NiMH Nested Battery: https://www.gobilda.com/nimh-battery-12v-3000mah-xt30-connector-mh-fc-20a-fuse-12-20/ and user manual https://www.gobilda.com/content/user_manuals/3100-0012-0020_user-manual.pdf
- [S47] REV 12V Slim Battery: https://www.revrobotics.com/rev-31-1302/ ; REV battery best practices: https://docs.revrobotics.com/duo-control/troubleshooting-the-control-system/12v-battery-best-practices-and-troubleshooting
- [S48] goBILDA odometry category (Pinpoint V2, pods, packs, prices): https://www.gobilda.com/odometry/ ; Pinpoint V2 page: https://www.gobilda.com/pinpoint-v2-odometry-computer-imu-sensor-fusion-for-2-wheel-odometry/
- [S49] goBILDA Pinpoint user guide (V1 specs, tuning): https://www.gobilda.com/content/user_manuals/3110-0002-0001%20User%20Guide.pdf
- [S50] Pedro Pathing Pinpoint localizer docs: https://pedropathing.com/docs/pathing/tuning/localization/pinpoint
- [S51] goBILDA 4-Bar Odometry Pod: https://www.gobilda.com/4-bar-odometry-pod-32mm-wheel/
- [S52] REV Color Sensor V3: https://www.revrobotics.com/rev-31-1557/ , https://docs.revrobotics.com/rev-crossover-products/sensors/color-sensor/specs
- [S53] Adafruit IR break-beam (3 mm): https://www.adafruit.com/product/2167
- [S54] REV Through Bore Encoder V2 price/SKU (REV-11-3174): https://www.revrobotics.com/rev-11-3174/
- [S55] REV Servo Hub: https://www.revrobotics.com/rev-11-1855/ , https://docs.revrobotics.com/rev-crossover-products/servo/servo-hub , configuring with a Control Hub: https://docs.revrobotics.com/rev-crossover-products/servo/servo-hub/configuring-the-servo-hub-with-a-control-hub
- [S56] goBILDA 6V Servo Power Injector: https://www.gobilda.com/6v-servo-power-injector-6-channel-8-15v-input/
- [S57] goBILDA RGB Indicator Light: https://www.gobilda.com/rgb-indicator-light-pwm-controlled
- [S58] REV Digital LED Indicator: https://www.revrobotics.com/rev-31-2010/ , https://docs.revrobotics.com/rev-crossover-products/indicators/digital-led/specs
- [S59] SDK 12.0 Javadoc, Gamepad (rumble/LED): https://javadoc.io/static/org.firstinspires.ftc/RobotCore/12.0.0/com/qualcomm/robotcore/hardware/Gamepad.html
- [S60] REV Resistive Grounding Strap: https://www.revrobotics.com/rev-31-1269/
- [S-calc] Local calculation scripts: `/private/tmp/claude-501/-Users-craigmartin-projects-ftc-12303/1db60cdd-00ce-4f71-b01b-0f877dc4c285/scratchpad/scripts/hw_calcs.py`, `/private/tmp/claude-501/-Users-craigmartin-projects-ftc-12303/1db60cdd-00ce-4f71-b01b-0f877dc4c285/scratchpad/scripts/launch2.py`
