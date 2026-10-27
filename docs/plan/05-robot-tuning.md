# 05 · Robot refinements (tune the robot as built)

**Decision (final): we refine the robot as built** ([R8](../research/R8-robot-v2.md)). The robot: a SWYFT mecanum drive; an **intake wheel** (a full-width roller of compliant star wheels) on a floating front arm, driven by a goBILDA Yellow Jacket DC motor; a ramp and a **tunnel** that hold the balls; a **carrier wheel** above the tunnel that holds them while it is stopped and pushes one into the shooting wheel when it spins; and a fixed **shooting wheel** at the top of the back. The shooter cannot turn, so the robot aims by turning its back to the up CELL. Everything below is a refinement. Bigger changes are marked **OPTIONAL**: do them only after the MUST and TUNE items work.

Prices and part numbers come from vendor pages checked on 2026-10-20 (links at the bottom). "Check vendor" means our research has no price. Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate. R5 (hardware) and R7 (robot audit) cover a different design; their notes on the parts this robot shares with it (drive, flywheel control, sensors, Limelight, inspection) apply here too, and where [R7-robot-audit.verify.md](../research/R7-robot-audit.verify.md) corrects R7, this page follows the fact-check.

---

## 1. Inspection blockers: fix these in week 1

The robot can't play until these pass (G303, R-rules; self-check with the Inspection Checklist, IC). Row 4 is recommended, not required.

| # | Item | Rule | What to do | Part / price |
|---|---|---|---|---|
| 1 | **Limelight must be a 3A**: **confirmed 3A** (coach, 10/21) | R702, Table 12-9: the 3A is the only programmable vision coprocessor allowed. **The 3G is named as prohibited** (Example 6; M p.140–141) | Look at it: the 3A is USB-C only, with **no Ethernet jack**. A 3G or 4 cannot be used; fall back to a UVC webcam with the SDK AprilTag processor (legal under R707/R708) | LL3A $189 (ServoCity 3122-0002-0001, sold out 10/20). Webcam: goBILDA Global Shutter USB Camera 3122-0004-0001, $74.99 |
| 2 | **Main power switch** (not seen in the video) | R603, Table 12-5 (M p.136): exactly one approved switch, accessible to the team (it need not be reachable by field staff, IC), away from fast-moving parts and pinch points | Mount it where the team can reach it, away from the shooting wheel, the intake arm and its gear, and other pinch points, shielded from robot contact | REV Switch Cable and Bracket REV-31-1387, $13 (checked 10/20), or goBILDA Floodgate 3103-0005-0001 (check vendor) |
| 3 | **Robot signs** (not seen) | R401–R403 (M p.129–130): at least 2 signs on surfaces ≥ 90° apart, each ≥ 6.5 × 2.5 in, solid red or blue, white numerals about 2.25 in tall, not stacked, not lit | Make one red set and one blue set; bring both to inspection | DIY |
| 4 | Grounding strap (**recommended, not an inspection item**) | R605, Table 12-6 (M p.136–137): grounding the electronics to the frame is only *permitted*, with listed straps; the frame can't carry current. The IC asks how you ground the electronics "if at all" | Recommended for static (ESD) protection, e.g. for the Limelight: spare XT30 on a hub → frame | REV-31-1269, $5.50 (470 Ω) or SWYFT SR-Ground-01 |
| 5 | **Size through the intake arm's swing** | R102 (18 in cube at start), R105 (18 × 24 × 29 in after the start, **enforced by physical stops**: software limits don't count; an extension must fit "at any point in its travel", Fig 12-6; M p.122–125) | Power off, swing the floating intake arm by hand from its lowest to its highest point and measure the robot's height and footprint at each end (the black L-shaped plate counts too). Add a mechanical stop if any position breaks the box | |
| 6 | **Actuator count** | R503 (M p.134): at most 8 motors and 8 servos, counted across **every** configuration | 7 motors (4 drive + shooting wheel + intake + carrier); no servo is needed. If the white servo by the shooter drives the carrier, it is 6 motors + 1 servo. See §5 | |
| 7 | Servo and battery paperwork | R502; IQR p.4 pre-verifies all three Dual Mode variants (-0002, -0003, -0004). R601: our battery 3100-0012-0020 is legal | Bring the spec sheet for any servo on the robot (the white servo, if it stays) | |
| 8 | Secure parts and wiring | R201-E (secured parts), R609/R610 (≥ 18 AWG main and motor power, ≥ 22 AWG servo; red/black colours), R202 (no entanglement hazards) | Battery cradle with two straps (not an inspection item per the IC, but a loose battery can get the robot DISABLED, G412.B); tidy wiring (§3.9) | |
| 9 | Control-system settings | R705: name the devices `12303-RC` and `12303-DS`. R711: change the Control Hub Wi-Fi password, Bluetooth off (M p.142–143) | | |
| 10 | Banned parts | R612 / IQR p.9: no goBILDA Servo Travel Tuner | Don't use one on any servo | |

---

## 2. MUST (rules and inspection)

Items 1–3 above, plus **possession: a tunnel that holds no more than 4, ball sensors and an auto-stop at 4 balls** (G407/G408). This is a design requirement, not a nice-to-have:
- There is **no "momentary" exception** for a 5th ball or an opponent NECTAR (QA3, QA4, QA23).
- Repeated violations count as STRATEGIC **even with a sorter that throws the extras back out** (TU05).
- A full-width roller grabs anything it touches.
- QA34 (balls trapped under a wide intake bar) is still unanswered.

**First, the tunnel capacity** ([07 Exp 0a](07-testing-and-practice.md#exp-0--tunnel-capacity-and-carrier-feed-do-these-first)). With the carrier stopped, push POLLEN in until no more fit. It must be **4**: if a 5th ball fits, add a mechanical stop so the tunnel holds 4. Without a mouth sensor nothing else stops a 5th ball once the count is wrong.

**Then the ball sensors.** The robot has none; the code treats each one as optional and switches its feature off when it is missing.

| Sensor | Where | Job | Part / price |
|---|---|---|---|
| Colour + distance #1 (`mouthColor`) | **Intake mouth** (the front bar or the sides of the ramp), placed so that **every** ball (garden balls taken with the wheel's outer edge too) passes within 4 cm (`MOUTH_BALL_PRESENT_CM`) before the intake wheel commits it. Bench-check that each garden ball is counted | See the ball **before** it is drawn in. POLLEN → take it (counted once it has passed the sensor). NECTAR → refuse it: always the opponent's; ours too while `RobotConfig.ACCEPT_OWN_NECTAR = false` (phase 1: NECTAR through the tunnel, carrier and shooting wheel is untested). The sensor reacts on contact, so drivers still steer around NECTAR | REV Color Sensor V3 REV-31-1557, $20.75: I2C, about 4 ms per read, fixed address 0x52, so each one needs its own I2C bus. Or the Brushland Color Rangefinder ($25, **out of stock 10/20**) **in I2C mode**: the code reads it like a REV V3; its digital outputs are not supported |
| Presence #2 (`slot4`) | **The 4th ball's place in the tunnel** (find it in Exp 0a) | "Full" → stop the intake, whatever the count says | Same choices (I2C, on a different bus from `mouthColor`) |
| IR break-beam (`feedBeam`) | **Across the tunnel where balls enter the shooting wheel** | Confirm every shot: the carrier stops at once on the beam break. A feed with no beam break and no flywheel dip is reported as unconfirmed (a likely jam) | Adafruit #2167 (3 mm), $2.95 |
| Software | – | Count starts at the Preloads value (4). With the mouth sensor: a ball counts once it has passed the mouth; the intake auto-stops at 4 (a short reverse pulse pushes a 5th away; with 3 counted a ball right behind ball 4 counts ball 4 at once; `MOUTH_ONE_BALL_MAX_MS` measured in Check: Sensors); NECTAR is refused at the mouth for as long as it stays there; "4 loaded" rumble/LED. Without it: INIT warns; the count only drops, by one per feed, confirmed or not; the intake refuses only while the count says 4, so after a volley RT never stops by itself (RT stops at 4 only with the mouth or slot-4 sensor) and the drivers stop at 4 by eye; a TELEOP RB volley feeds 4 times; an eject (operator LB) does not lower the count, so the operator presses back once per ball that went out | free ([06](06-software.md)) |

Also make the intake mouth narrower or gated if a 5th ball can get under the bar. That lowers the QA34 risk while we wait for the answer.

---

## 3. TUNE (performance and reliability)

### 3.1 Flywheel control (software first)
- Use the hub's built-in velocity control (`setVelocity`, PIDF). Starting gains: measure the top speed with full power on a fresh battery, then **F = 32767 ÷ max ticks/s, P = 0.1 F, I = 0.1 P, D = 0** (REV PIDF guide).
- Re-apply the gains in **every OpMode**; they don't survive a power cycle (ftc-docs PIDF page).
- **Feed only when the wheel is back to speed: within ±2–3% for 3 or more loops.** The motor's encoder has only 28 counts per turn, and the hub averages speed over a 5-sample buffer refreshed every 10 ms, so readings move in steps of about 1–1.5% (gm0). A tighter gate can't be measured.
- The goBILDA StarterBot does the same: PIDF (40, 0, 0, 12.5), and it feeds only above 1200 of a 1250 ticks/s target (goBILDA example code).

### 3.2 Add a steel flywheel disc (if it fits)
- **goBILDA Steel Flywheel 60 mm, 3628-0014-0060**: 115 g, 551 g·cm², "built for compact attachment to the 72 mm Hogback". It bolts to the side of the wheel with 12 mm M4 thread-forming screws. **$12.99** (goBILDA page, checked 10/20).
- **First identify our shooting wheel** (size and type, [§7](#7-questions-only-the-robot-can-answer-measure-in-week-1)) and check that a disc fits: one wheel face needs axial room for the disc and its screw heads ([07 set-up checks](07-testing-and-practice.md#set-up-checks-week-1-before-any-experiment)). If our wheel is not a 72 mm Hogback, find a disc made for it.
- Model (est., for a 72 mm wheel): speed lost per shot drops from about **19% → 11% for POLLEN** (28% → 16% for NECTAR). Spin-up from rest becomes about 0.5 s.
- Prove it with [Exp 3](07-testing-and-practice.md#exp-3--flywheel-recovery): keep it if made-shots go up and spin-up stays under 1 s.

### 3.3 Exit angle and bounce-outs
- The exit angle is **fixed**: nothing on the robot adjusts it. **Measure it on 240 fps video**, together with where the ball leaves the robot (`SHOOTER_FORWARD_IN`, `SHOOTER_LEFT_IN`) and the speed ratio k ([Exp 2a](07-testing-and-practice.md#2a-exit-angle-exit-point-and-speed-ratio-k-do-this-first)). The shot-table seeds and the 30–92 in shot band assume about 64–65° and k = 0.40 (R3 model, 72 mm wheel).
- Also find out what the ball is squeezed against as it passes the shooting wheel, and the gap.
- A fixed exit angle plus a distance → RPM table is enough. Model (est.): about 80–82% clean entries with realistic shot-to-shot scatter, and 94–96% if the scatter is halved. **Consistency is worth more than an adjustable hood.** If the measured angle is far from 64–65°, build the table at the angle we have first; changing the angle is OPTIONAL (§4).
- **Bounce-outs** are a common shooting failure: 44% in one team's fixed solid-hood test (CD-14343), and Saline commentators kept calling them (MonaShores). Fixes, in order:
  1. A **compliant lining** (foam or flexible polycarbonate) where the ball is squeezed. It squeezes every ball about the same amount, a few millimetres (≈ 5–12% of the diameter, est.), even though ball sizes vary (M p.74).
  2. A consistent ball path and squeeze.
  3. **Test spin and arrival speed.** Community teams report fewer bounce-outs with zero-spin or topspin shots (community), and our model says slow arrivals near the top of the arc stay in better (est.).
  4. More than 5% bounce-outs in the test: lower the speed, or (OPTIONAL) change the exit angle a few degrees.
  5. Still bad: **OPTIONAL** counter-roller (§4).

### 3.4 Shooting-wheel mount and drive
- **Read the motor label.** The plan and the code assume a goBILDA 5203 6000 rpm 1:1 (`flywheel`, 28 encoder counts per motor turn). The encoder cable must be plugged in: speed control, the ready gate and shot detection all use it.
- **Count the motor turns per wheel turn** (on the motor shaft = 1; geared or belted = the ratio) and set `FLYWHEEL_TICKS_PER_REV` = 28 × that number in `RobotConfig`, or every rpm the code shows is motor-side, not wheel-side. (The shot table's `SHOT_TABLE_RPM_SCALE` calibration absorbs the ratio either way.)
- Bolt the motor firmly and check that nothing rubs at full speed. If a load-bearing printed part (motor or wheel mounts, the red hub brackets) is PLA or shows cracks, creep or loose bearings, reprint it **with the same geometry** in PETG/ABS/ASA/nylon (≥ 4 perimeters, 40–60% infill) and use real flanged bearings at the wheel; re-run Exp 2a after any change that moves the wheel. Otherwise leave it (est.).

### 3.5 Carrier feed timing
- The carrier wheel is the **gate and the feeder**: stopped, it holds the balls; spinning, it pushes the ball under it into the shooting wheel. Configure `carrier` as a Motor (the coach says a DC motor drives it), or as a Continuous Rotation Servo if the white servo by the shooter turns out to drive it.
- Directions first: in **Check: Sensors**, B must push balls toward the shooting wheel and X backward; flip `CARRIER_DIRECTION` if not. Try it with no balls inside first.
- **`CARRIER_FEED_MS`** (0.4 s default) is how long the carrier pushes when no shot is confirmed: long enough to push one ball into the shooting wheel, too short to push two. Set it with the **timed push** in **Tune: Flywheel**: RB runs the carrier for exactly `CARRIER_FEED_MS` and does not stop at the dip; Y selects `FEED_MS` and dpad up/down changes it in 10 ms steps ([Exp 0b](07-testing-and-practice.md#0b-carrier-feed-timing)). `CARRIER_FEED_POWER` starts at 1.0.
- The carrier stops as soon as the shot is confirmed (a flywheel speed dip, or the feed beam), so tune the dip detection early: in **Tune: Flywheel**, B feeds one ball and "last shot: drop" must update on every shot; if it doesn't, lower `FLYWHEEL_SHOT_DIP_FRACTION` or fit the feed beam. The next ball waits at least 0.15 s (`MIN_SHOT_INTERVAL_MS`) and until the flywheel is back at speed and the robot is aimed, whichever takes longer.
- While the intake pulls balls in, the carrier stays stopped (`CARRIER_INTAKE_POWER = 0`) and holds them. Raise it a little only if balls stop short of the carrier.

### 3.6 Intake motor direction and speed
- `intake` is configured as a **Motor** (the Yellow Jacket on the floating front arm; the code also accepts a CR servo). In Check: Sensors, A = intake in: balls must go in; flip `INTAKE_DIRECTION` if not. Start at `INTAKE_IN_POWER = 1.0` and lower it only if balls bounce off the star wheels.
- Read the motor label (rpm) and count the side gear/pulley teeth: surface speed (ft/s) = π × D (in) × motor rpm × (driver teeth ÷ driven teeth) ÷ 720. Aim for ≈ 10–13 ft/s; the drive does 6.4 ft/s (gm0 recommends an intake faster than the drive).
- Check that the floating arm drops back onto the tiles after riding over a ball, and that the star wheels reach a GARDEN ball 1.4 in from the wall (`GARDEN_SWEEP_X`, [Exp 8](07-testing-and-practice.md#exp-8--intake-pickup-speed)).
- A motor reports its current, which could detect jams; the code doesn't read it.

### 3.7 Odometry: Pinpoint + 2 pods (the team has them)
- **The X/Y pods are already on the robot** (coach, 10/21); which pods, and whether they plug into the Pinpoint, are not known yet. Run **Check: Odometry** (push the robot 48 in forward, then 48 in left): it says whether a Pinpoint is found, names the pod from its counts per inch (goBILDA 4-bar ≈ 505, goBILDA swing-arm ≈ 337, SWYFT 38 mm ≈ 872) and prints the RobotConfig section 3 lines for pod type and directions. If the Pinpoint is not found and the counts show up under a motor port, the pods are wired to the REV hub instead: the code then needs Pedro's two-wheel localizer (a short code change; ask).
- Mount the pods rigidly with consistent spring down-force.
- Wire the Pinpoint to an **I2C bus other than 0** (bus 0 shares the hub's IMU; ftc-docs hub tips).
- Tuned, expect about 0.5 in error after driving around and returning (goBILDA Pinpoint guide).
- If buying: Pinpoint V2 3110-0002-0002, $79.99 (V1 is discontinued). Pods: goBILDA 4-Bar 3110-0001-0002, $99.99 each, or SWYFT Linear SR-ODOMETRY-LINEAR-01, $64.99 each.

### 3.8 Limelight 3A on the back
- Mount it **fixed to the chassis, looking out of the robot's back** (the shooter side), pitched **about 35–40° up**, lens around 12–14 in high. It then sees the target CELL's tags while the robot aims, and on S1 (back toward the HIVE) it sees the start CELL. At 35°, the up-CELL tags stay in view from about 26 to 120 in (est., geometry model).
- Tape-measure and set `CAMERA_YAW_OFFSET_DEG` (180 = out of the back), `CAMERA_FORWARD_IN` (negative = behind the robot centre), `CAMERA_LEFT_IN`, `CAMERA_HEIGHT_IN` and `CAMERA_PITCH_DEG`, then fine-tune them with Check: Limelight.
- **One AprilTag pipeline (index 0) for both alliances**: 640 × 480 (50 fps), **ID filter = tags 30–45** (both HIVEs; the code keeps only ours: red 30–37 / blue 38–45. Never filter to one alliance's 8 tags, or the robot is blind on the other colour), marker size 3.25 in (82.55 mm), **Full 3D on**. FOV is 54.5° × 42° (Limelight docs). Set it over a **USB cable**, never Wi-Fi at an event.
- Calibrate with **Check: Limelight** (nothing moves): on our S1 (front against our start wall, back toward the HIVE) the up-CELL tags read about 49–50 in and "UP"; from the calibration spot (36, -12.75) (blue (-36, 12.75)), back toward the HIVE, the second CELL's down tags read about 35–36 in and "dn". If every height is off by the same factor, fix the marker size, not the pitch. The other alliance's tags show as "Ignored": that is normal.
- Plug a **USB-C → USB-A** cable into the Control Hub's **USB 3.0 (blue)** port; C-to-C cables don't work (ftc-docs hub tips). Add a ferrite and strain relief.
- **Export the pipeline** before any LimelightOS update (updates erase them). It takes 15–20 s to boot (Limelight docs).

### 3.9 Protection
- **Guard the shooting wheel.** A cover keeps fingers away from a 6000 rpm wheel (R202) and falling balls off the top of the robot (G409's box encourages guards; G407). It does not prevent deflection calls (QA17): only staying out of the landing box does.
- **Shield the hubs.** They stand on a top rail, in red printed brackets.
- **Battery:** low, in a 1120 U-Channel cradle with the goBILDA Battery Mount 1209-0001-0001 (the pack is designed to nest in that channel) and two straps. Not an inspection item, but a loose battery can get the robot DISABLED (G412.B).
- **Wiring:**
  - route it inside the frame and tie it every ~3 in;
  - clip or sleeve the XT30 junctions;
  - keep it at least 1 in from the wheels, the intake arm and its gear, and the carrier and shooting wheels.
- **Static (ESD):**
  - ferrites on USB and sensor cables;
  - electronics on non-conductive mounts with at least 3/8 in of air gap;
  - a listed grounding strap (§1 row 4: recommended, not required; ftc-docs ESD page).

### 3.10 NECTAR (test only after POLLEN cycles are reliable)
- NECTAR (3.6 in) through this ball path is untested: the ramp, the two fingers, the tunnel, the carrier wheel's gap and the shooting wheel are all set up for 2.8 in POLLEN. Expect jams, motor stalls or crushed balls until the test says otherwise (est.).
- Until the [NECTAR go/no-go](07-testing-and-practice.md#exp-6--nectar-go--no-go) passes, keep `RobotConfig.ACCEPT_OWN_NECTAR = false`: with a mouth sensor, the intake then refuses **all** NECTAR at the mouth.

---

## 4. OPTIONAL (bigger changes; only after §2–3 work)

| Change | Why | Parts | Cost |
|---|---|---|---|
| **NECTAR path** | Phase-3 tip accelerant: 3 NECTAR + 3 POLLEN tip an empty CELL (6 balls instead of 8) | Compliance where NECTAR is squeezed (fingers, carrier gap, shooting-wheel gap) and a separate NECTAR RPM table; only after Exp 6 | – |
| **Hood adjustment** | Move the fixed exit angle toward ~64–65° if Exp 2a finds it far off, or give NECTAR its own angle | Shims or a reprinted guide; a servo-driven hood adds an actuator (R503) | – |
| Counter-roller / hood wheel | Near-zero spin → fewer bounce-outs (community approach) | The 8th motor, or a servo | – |
| Bigger shooting wheel + steel flywheel (e.g. 96 mm Hogback + 82 mm steel flywheel, 1651 g·cm²) | More speed headroom if the measured speed ratio is below 0.4 or the table needs more than ~4,300 rpm | 3626-0014-0096 + 82 mm flywheel; needs a new mount | check vendor |
| FLOWER POLLEN extractor | Ammo supply (16 POLLEN start in FLOWERS) | Copy the goBILDA StarterBot: 2× 72 mm GripForce Gecko wheels (3613-0014-0072) on Speed Dual Mode servos in Compact ServoBlocks (3217-0001-2501) | – |
| NECTAR dropper for FLOWERS | Phase-3 FLOWER points ([04](04-endgame.md#b-flowers-last-60-seconds-only)) | 1–2 servos | – |
| Second (floor) camera | Ball chasing | goBILDA Global Shutter USB Camera 3122-0004-0001 | $74.99 |

---

## 5. Actuator budget (R503: 8 motors, 8 servos)

| | As built | If the white servo drives the carrier | With OPTIONAL extras |
|---|---|---|---|
| Motors | 4 SWYFT Spike drive + shooting wheel + intake + carrier = **7/8** | 6/8 | A counter-roller motor would make 8/8 |
| Servos | None needed = **0/8** (the white servo counts if it stays configured) | 1/8 | FLOWER extractor +2, NECTAR dropper +1–2, counter-roller servo +1 |

R503 allows at most 8 motors and 8 servos **across all configurations** we bring to inspection.

Wiring notes for the code team (names are the Robot Configuration names in `README-12303.md` §2):
- Limelight → USB 3.0 (blue) port.
- Pinpoint → I2C bus 1 or higher.
- REV colour sensors (`mouthColor`, `slot4`) → separate I2C buses (a Brushland sensor only in I2C mode).
- Break-beam (`feedBeam`) → digital port.
- `flywheel` → a motor port, with its encoder cable. `intake` and `carrier` → motor ports (or `carrier` on a servo port, configured as a Continuous Rotation Servo). Spread the motors over both hubs.

---

## 6. Shopping list

| Item | Part # | Price | Tier |
|---|---|---|---|
| Colour/distance sensors ×2 | REV-31-1557 (or Brushland ×2 at $25, used in I2C mode) | 2 × $20.75 | MUST |
| IR break-beam | Adafruit 2167 | $2.95 | MUST |
| Main power switch (if none) | REV-31-1387 (or goBILDA 3103-0005-0001) | $13 | MUST |
| Tunnel stop (only if a 5th ball fits, Exp 0a) | spare channel, standoffs or a printed stop | – | MUST |
| Grounding strap (recommended, ESD) | REV-31-1269 | $5.50 | TUNE |
| Steel flywheel disc (identify our wheel and check the fit first) | 3628-0014-0060 (made for the 72 mm Hogback) | $12.99 | TUNE |
| Spare batteries ×2 (3–4 total for events) | 3100-0012-0020 / REV-31-1302 | $64.99 / $60 each | TUNE |
| Ferrites, cable clips | – | ≈ $10 | TUNE |
| 96 mm Hogback + 82 mm steel flywheel (only if speed headroom is short) | 3626-0014-0096 + 82 mm flywheel | check vendor | OPTIONAL |

**Rough total (our arithmetic): MUST ≈ $57; TUNE ≈ $148–158** (includes the recommended $5.50 grounding strap).

**Supply warnings (checked 10/20):** out of stock: Brushland Color Rangefinder. Sold out: Limelight 3A (ServoCity), AndyMark practice POLLEN. Order alternatives early.

---

## 7. Questions only the robot can answer (measure in week 1)

These are also the "measure on robot" rows in the [07 set-up checks](07-testing-and-practice.md#set-up-checks-week-1-before-any-experiment).

1. ~~Limelight model~~: confirmed 3A (coach, 10/21). Odometry pods: on the robot, type and wiring found with Check: Odometry.
2. How many POLLEN the tunnel holds with the carrier stopped (must be 4; Exp 0a), and what the two black fingers at the tunnel entrance do.
3. What drives the carrier wheel: a DC motor (a yellow goBILDA motor can sits near the shooter) or the white servo next to the shooter motor. That decides whether `carrier` is configured as a Motor or a Continuous Rotation Servo.
4. The shooting wheel: the motor's label (5203 6000 rpm 1:1?), how it drives the wheel (motor turns per wheel turn), the wheel's size and type, where the ball leaves it (forward and left of the robot centre: `SHOOTER_FORWARD_IN`, `SHOOTER_LEFT_IN`), what the ball is squeezed against and the gap, and whether a steel flywheel disc fits beside it.
5. Intake: the Yellow Jacket's rpm (label), the teeth on the side gear/pulley pair, the star-wheel diameter, and how far the floating arm swings.
6. What the black L-shaped plate on the left side toward the back is for (ask the coach).
7. Robot height and footprint with the intake arm at both ends of its swing; robot weight.
8. Where the battery sits and how it is held; where the main power switch will go; battery count and age.
9. The Limelight's mount: it should look out of the back, pitched up 35–40°; measure its position, height and pitch.
10. Spare parts on the shelf (motors, Hogbacks, Gecko wheels, battery mount, sensors).
11. Print material and settings of the load-bearing printed parts (the red hub brackets, any wheel or motor mounts), and whether real bearings sit in printed parts.

---

## Sources

- **Manual TU05** (G303, G407–G409, G412, R102, R105, R201–R202, R401–R403, R502–R505, R601, R603, R605, R609–R612, R702, R705, R707–R711): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A** QA3, QA4, QA17, QA23, QA34 (pending): https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N · **TU05**: https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **Inspection Checklist (IC)**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check · **Inspection Quick Reference (IQR)**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-reference
- **Robot as built** (the coach's video and answers): [R8-robot-v2.md](../research/R8-robot-v2.md). For the parts this robot shares with the design R5 and R7 audited (drive, flywheel control, sensors, Limelight): [R7-robot-audit.verify.md](../research/R7-robot-audit.verify.md), [R5-hardware.verify.md](../research/R5-hardware.verify.md)
- **Code** (device names, carrier, intake, NECTAR flag): `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md` §2, `config/RobotConfig.java`
- **goBILDA**:
  - Steel Flywheel 60 mm: https://www.gobilda.com/steel-flywheel-60mm-diameter-115g-551-g-cm/
  - 5203 6000 rpm motor: https://gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder
  - Pinpoint V2: https://www.gobilda.com/pinpoint-v2-odometry-computer-imu-sensor-fusion-for-2-wheel-odometry/
  - Odometry pods: https://www.gobilda.com/odometry/
  - Battery: https://www.gobilda.com/12v-nimh-nested-battery-3000mah-mh-fc-xt30-connector/
  - StarterBot guide and code: https://www.gobilda.com/ftc-starter-bot-resource-guide-2026-2027-season/
  - Global Shutter camera: https://www.gobilda.com/global-shutter-usb-camera-with-gobilda-case-100-fps/
- **REV**:
  - Switch Cable and Bracket: https://www.revrobotics.com/rev-31-1387/
  - Grounding strap: https://www.revrobotics.com/rev-31-1269/
  - Color Sensor V3: https://www.revrobotics.com/rev-31-1557/
  - PIDF guide: https://docs.revrobotics.com/first-global/fgc-pid
- **Brushland Color Rangefinder**: https://docs.brushlandlabs.com/sensors/color-rangefinder · **Adafruit break-beam**: https://www.adafruit.com/product/2167
- **SWYFT** (drive, odometry module): https://swyftrobotics.com/kits/swyft-drive-v2 · https://swyftrobotics.com/kits/swyft-linear-odometry-module
- **Limelight 3A docs**: https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a · **ServoCity LL3A**: https://www.servocity.com/limelight-3a-smart-camera
- **ftc-docs**:
  - Hub tips: https://ftc-docs.firstinspires.org/tech_tips/tech-tips/tech-tip-hub-tips/tech-tip-hub-tips.html
  - ESD: https://ftc-docs.firstinspires.org/hardware_and_software_configuration/configuring/managing_esd/managing-esd.html
  - PIDF: https://ftc-docs.firstinspires.org/programming_resources/shared/pidf_coefficients/pidf-coefficients.html
- **gm0**:
  - Motor velocity: https://gm0.org/en/latest/docs/software/adv-control-system/sdk-motors.html
  - Intakes: https://gm0.org/en/latest/docs/common-mechanisms/active-intake/index.html
- **CD-14343** (bounce-out test; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
- est. values come from our research team's ball-flight and flywheel models (2026-10-20). Confirm them with the experiments in [07](07-testing-and-practice.md).
