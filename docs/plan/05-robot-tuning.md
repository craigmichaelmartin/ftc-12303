# 05 · Robot refinements (keep the design, tune it)

**Decision (final): we keep the current robot** (SWYFT mecanum drive, turret with a single 72 mm flywheel, full-width roller intake, servo indexer up through the turret) **and refine it.** Everything below is a refinement. Bigger changes are marked **OPTIONAL**: do them only after the MUST and TUNE items work.

Prices and part numbers come from vendor pages checked on 2026-10-20 (links at the bottom). "Check vendor" means our research has no price. Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

---

## 1. Inspection blockers: fix these in week 1

The robot can't play until these pass (G303, R-rules; self-check with the Inspection Checklist, IC).

| # | Item | Rule | What to do | Part / price |
|---|---|---|---|---|
| 1 | **Limelight must be a 3A** | R702, Table 12-9: the 3A is the only programmable vision coprocessor allowed. **The 3G is named as prohibited** (Example 6; M p.140–141) | Look at it: the 3A is USB-C only, with **no Ethernet jack**. A 3G or 4 cannot be used; fall back to a UVC webcam with the SDK AprilTag processor (legal under R707/R708) | LL3A $189 (ServoCity 3122-0002-0001, sold out 10/20). Webcam: goBILDA Global Shutter USB Camera 3122-0004-0001, $74.99 |
| 2 | **Main power switch** (none seen in photos) | R603, Table 12-5 (M p.136): exactly one approved switch, reachable by the team, away from fast-moving parts and pinch points | Mount it where staff can reach it but other robots can't hit it | REV Switch Cable and Bracket REV-31-1387, $13 (checked 10/20), or goBILDA Floodgate 3103-0005-0001 (check vendor) |
| 3 | **Robot signs** (none seen) | R401–R403 (M p.129–130): at least 2 signs on surfaces ≥ 90° apart, each ≥ 6.5 × 2.5 in, solid red or blue, white numerals about 2.25 in tall, not stacked, not lit | Make one red set and one blue set; bring both to inspection | DIY |
| 4 | **Grounding strap** | R605, Table 12-6 (M p.136–137): only listed straps; the frame can't carry current | Spare XT30 on a hub → frame. Also protects against static (ESD) | REV-31-1269, $5.50 (470 Ω) or SWYFT SR-Ground-01 |
| 5 | **Size at every turret angle** | R102 (18 in cube at start), R105 (18 × 24 × 29 in after the start, **enforced by physical stops**: software limits don't count; M p.122–125) | Measure height and footprint with the turret at both ends of its travel. Add mechanical hard stops if any angle breaks the box | |
| 6 | **Actuator count** | R503 (M p.134): at most 8 motors and 8 servos, counted across **every** configuration | Today: 5 motors (4 drive + flywheel), about 4–5 servos | |
| 7 | Servo and battery paperwork | R502; IQR p.4 pre-verifies all three Dual Mode variants (-0002, -0003, -0004). R601: our battery 3100-0012-0020 is legal | Bring the 5-turn servo spec sheet if we add one | |
| 8 | Secure parts and wiring | R201-E (secured parts), R609/R610 (≥ 18 AWG main and motor power, ≥ 22 AWG servo; red/black colours), R202 (no entanglement hazards) | Battery cradle with two straps; tidy wiring (§3) | |
| 9 | Control-system settings | R705: name the devices `12303-RC` and `12303-DS`. R711: change the Control Hub Wi-Fi password, Bluetooth off (M p.142–143) | | |
| 10 | Banned parts | R612 / IQR p.9: no goBILDA Servo Travel Tuner | Use the SDK's 500–2500 µs servo range instead | |

---

## 2. MUST (rules and inspection)

Items 1–4 above, plus **possession sensors and an auto-stop at 4 balls** (G407/G408). This is a design requirement, not a nice-to-have:
- There is **no "momentary" exception** for a 5th ball or an opponent NECTAR (QA3, QA4, QA23).
- Repeated violations count as STRATEGIC **even with a sorter that throws the extras back out** (TU05).
- A full-width roller grabs anything it touches.
- QA34 (balls trapped under a wide intake bar) is still unanswered.

| Sensor | Where | Job | Part / price |
|---|---|---|---|
| Colour + distance #1 | **Intake mouth** | See the ball **before** it is pulled in. POLLEN (yellow) → take it. Opponent NECTAR → don't take it. Phase 1: own NECTAR → don't take it either, because the hood can't launch it yet | Brushland Color Rangefinder, $25: digital outputs, read in the fast bulk read; **out of stock 10/20**. Or REV Color Sensor V3 REV-31-1557, $20.75: I2C, about 4 ms per read, fixed address 0x52, so each one needs its own I2C bus |
| Presence #2 | **Storage slot 4** | "Full" → stop the intake | Same choices |
| IR break-beam | **Shooter feed** | Count shots, detect jams | Adafruit #2167 (3 mm), $2.95 |
| Software | – | Count starts at the preloads (4); intake auto-stops at 4; refuse at the mouth; "4 loaded" rumble/LED | free ([06](06-software.md)) |

Also make the intake mouth narrower or gated if a 5th ball can get under the bar. That lowers the QA34 risk while we wait for the answer.

---

## 3. TUNE (performance and reliability)

### 3.1 Flywheel control (software first)
- Use the hub's built-in velocity control (`setVelocity`, PIDF). Starting gains: measure the top speed with full power on a fresh battery, then **F = 32767 ÷ max ticks/s, P = 0.1 F, I = 0.1 P, D = 0** (REV PIDF guide).
- Re-apply the gains in **every OpMode**; they don't survive a power cycle (ftc-docs PIDF page).
- **Feed only when the wheel is back to speed: within ±2–3% for 3 or more loops.** The motor's encoder has only 28 counts per turn, and the hub averages speed over a 5-sample buffer refreshed every 10 ms, so readings move in steps of about 1–1.5% (gm0). A tighter gate can't be measured.
- The goBILDA StarterBot does the same: PIDF (40, 0, 0, 12.5), and it feeds only above 1200 of a 1250 ticks/s target (goBILDA example code).

### 3.2 Add the 60 mm steel flywheel
- **goBILDA Steel Flywheel 60 mm, 3628-0014-0060**: 115 g, 551 g·cm², "built for compact attachment to the 72 mm Hogback" (our wheel). **$12.99** (goBILDA page, checked 10/20).
- Model (est.): speed lost per shot drops from about **19% → 11% for POLLEN** (28% → 16% for NECTAR). Spin-up from rest becomes about 0.5 s.
- Prove it with [Exp 3](07-testing-and-practice.md#exp-3--flywheel-recovery): keep it if made-shots go up and spin-up stays under 1 s.

### 3.3 Hood angle and bounce-outs
- Set the hood so the ball's **actual exit angle is about 64–65°**. **Measure it on 240 fps video**; don't trust the CAD angle (compression and spin shift it by a few degrees) ([Exp 2a](07-testing-and-practice.md#exp-2--shot-map-and-hood-angle)).
- A fixed hood plus a distance → RPM table is enough. Model (est.): about 80–82% clean entries with realistic shot-to-shot scatter, and 94–96% if the scatter is halved. **Consistency is worth more than an adjustable hood.**
- **Bounce-outs** are a common shooting failure: 44% in one team's fixed solid-hood test (CD-14343), and Saline commentators kept calling them (MonaShores). Fixes, in order:
  1. A **compliant hood** lining (foam or flexible polycarbonate). It squeezes every ball about the same amount, a few millimetres (≈ 5–12% of the diameter, est.), even though ball sizes vary (M p.74).
  2. A consistent ball path and squeeze.
  3. **Test spin and arrival speed.** Community teams report fewer bounce-outs with zero-spin or topspin shots (community), and our model says slow arrivals near the top of the arc stay in better (est.).
  4. More than 5% bounce-outs in the test: raise the hood a few degrees or lower the speed.
  5. Still bad: **OPTIONAL** counter-roller (§4).

### 3.4 Flywheel mount
- The photos suggest the flywheel motor sits in a printed clamp shimmed with cloth tape, possibly driving through a right-angle gear at 6000 rpm (unconfirmed). **Bolt it** with a goBILDA motor mount and check the gear mesh.
- Reprint load-bearing printed parts (hood, flywheel arms, clamps, hub holders) in PETG, ABS, ASA or nylon, with ≥ 4 perimeters and 40–60% infill, and use real flanged bearings at the flywheel (est.).

### 3.5 Turret: range and a known zero
- As built, a 300° servo through a ~4–6:1 ring gives only about **50–75° of turret travel** (45–68° unless the servo is set to its full 500–2500 µs pulse range; the SDK default is 600–2400 µs). There is no feedback.
- **Count the ring and pinion teeth first**; every option depends on the ratio.

| Option | What | Turret travel | Part / price |
|---|---|---|---|
| **A (preferred, same footprint)** | goBILDA **5-turn** Dual Mode Servo | ≈ 330–450°, depending on the ratio | 2000-0025-0502 (Torque) / -0503 (Speed), $49.99. Needs the 500–2500 µs range |
| B | Keep a servo in continuous mode + **REV Through Bore Encoder V2** on the turret axis | Unlimited (cable-limited) | REV-11-3174, $48 + servo programmer 3102-0001-0001, $12.99. Plug the encoder into motor encoder port 0 or 3: those are counted in hardware (ftc-docs hub tips) |

Either way:
- add a **home/zero reference**: REV Magnetic Limit Switch REV-31-1462, $17.50;
- add **mechanical hard stops** at the cable limits (R105 needs physical limits);
- leave a **service loop** for the cables.

**Until this is done:** the chassis heading does the coarse aim, the turret does the fine aim. If the turret misbehaves, pin it at 0° and aim with the chassis only. That is the fallback.

### 3.6 Intake power (keep the servo, give it 6 V)
- Hub servo ports supply **5 V**, with **2 A per port pair** and 5 A per hub. A stalled goBILDA servo draws 2.0–2.5 A, so a stalled intake servo can starve the other servo on its pair (REV Control Hub specs; goBILDA).
- Power the intake and turret servos at **6 V** from a **REV Servo Hub** (REV-11-1855, $90, 15 A total) or a **goBILDA 6V Servo Power Injector** (3125-0001-0001, $69.99, 24 A). Both are legal (R505). Servos run about 20% faster at 6 V (est.).
- Spread the remaining hub servos across different port pairs.
- **OPTIONAL** bigger fix: a DC-motor intake (§4).

### 3.7 Odometry: Pinpoint + 2 pods (the team has them)
- Mount the pods rigidly with consistent spring down-force.
- Wire the Pinpoint to an **I2C bus other than 0** (bus 0 shares the hub's IMU; ftc-docs hub tips).
- Tuned, expect about 0.5 in error after driving around and returning (goBILDA Pinpoint guide).
- If buying: Pinpoint V2 3110-0002-0002, $79.99 (V1 is discontinued). Pods: goBILDA 4-Bar 3110-0001-0002, $99.99 each, or SWYFT Linear SR-ODOMETRY-LINEAR-01, $64.99 each.

### 3.8 Limelight 3A on the turret
- Pitch it **about 35–40° up**, lens around 12 in high. At 35°, the up-CELL tags stay in view from about 26 to 120 in (est., geometry model).
- AprilTag pipeline: 640 × 480 (50 fps), **ID filter = our 8 tags** (red 30–37, blue 38–45), marker size 3.25 in (82.55 mm). FOV is 54.5° × 42° (Limelight docs).
- Plug a **USB-C → USB-A** cable into the Control Hub's **USB 3.0 (blue)** port; C-to-C cables don't work (ftc-docs hub tips). Add a ferrite, strain relief, and a service loop through the turret's travel.
- **Export the pipelines** before any LimelightOS update (updates erase them). It takes 15–20 s to boot (Limelight docs).
- While the turret is pinned, a fixed chassis mount works just as well.

### 3.9 Protection
- **Guard the turret's open centre and the flywheel.** A lid keeps falling balls out (G409's box encourages guards; G407) and keeps fingers away from a 6000 rpm wheel (R202).
- **Shield the hubs.** They stand on top, above the intake path.
- **Battery:** low, in a 1120 U-Channel cradle with the goBILDA Battery Mount 1209-0001-0001 (the pack is designed to nest in that channel) and two straps.
- **Wiring:**
  - route it inside the frame and tie it every ~3 in;
  - clip or sleeve the XT30 junctions;
  - keep it at least 1 in from the wheels and the ring/pinion mesh.
- **Static (ESD):**
  - ferrites on USB and sensor cables;
  - electronics on non-conductive mounts with at least 3/8 in of air gap;
  - the grounding strap (ftc-docs ESD page).

### 3.10 NECTAR (test only after POLLEN cycles are reliable)
- The hood gap is set for 2.8 in POLLEN. A 3.6 in NECTAR would be squeezed about 28–32%: expect jams, motor stalls (up to 9.2 A) or crushed balls (est.).
- Run the [NECTAR go/no-go](07-testing-and-practice.md#exp-6--nectar-go--no-go) before any NECTAR plan.

---

## 4. OPTIONAL (bigger changes; only after §2–3 work)

| Change | Why | Parts | Cost |
|---|---|---|---|
| **Intake on a spare DC motor** (same roller) | A servo makes ~2–3 W; a 5203 motor ~22.6 W. A servo roller reaches about 1.4–1.8 ft/s at the surface (3.5 in roller), while the drive does 6.4 ft/s. A motor also reports current, which helps detect jams (est.; gm0 recommends a dedicated intake motor) | goBILDA 5203 **1150 rpm** (5203-2402-0005). 312/435 rpm for larger or geared rollers; the 312 (5203-2402-0019) may already be in the team's starter kit but is sold out online (10/20) | ≈ $54.99 |
| Counter-roller / hood wheel | Near-zero spin → fewer bounce-outs (community approach) | Spare motor or servo | – |
| Compliant NECTAR hood: spring-loaded with ≥ 0.9–1.0 in travel, or a 2-position servo gap | Launch both ball sizes; needs a separate NECTAR RPM table | Existing hood servo | – |
| 96 mm Hogback + 82 mm steel flywheel (1651 g·cm²) | More speed headroom if the measured speed ratio is below 0.4 | 3626-0014-0096 + 82 mm flywheel; needs a reprinted hood | check vendor |
| FLOWER POLLEN extractor | Ammo supply (16 POLLEN start in FLOWERS) | Copy the goBILDA StarterBot: 2× 72 mm GripForce Gecko wheels (3613-0014-0072) on Speed Dual Mode servos in Compact ServoBlocks (3217-0001-2501) | – |
| Direct coupler or 1:1 belt instead of the right-angle gear | Reliability at 6000 rpm | – | – |
| Move the hubs lower | Robustness, room for the indexer | – | – |
| NECTAR dropper for FLOWERS | Phase-3 FLOWER points ([04](04-endgame.md#b-flowers-last-60-seconds-only)) | 1–2 servos | – |
| Second (floor) camera | Ball chasing | goBILDA Global Shutter USB Camera 3122-0004-0001 | $74.99 |

---

## 5. Actuator budget (R503: 8 motors, 8 servos)

| | Now | After MUST + TUNE | If the OPTIONAL intake motor is added |
|---|---|---|---|
| Motors | 4 SWYFT Spike drive + 1 flywheel = **5/8** | 5/8 | 6/8 |
| Servos | Intake, turret, hood/gate, indexer ×1–2 = **4–5/8** | Same (the 5-turn servo replaces the turret servo) | One fewer servo |

Wiring notes for the code team:
- Limelight → USB 3.0.
- Pinpoint → I2C bus 1 or higher.
- REV colour sensors → separate I2C buses (or Brushland on digital ports).
- Break-beam and limit switch → digital ports.
- Through Bore Encoder → encoder port 0 or 3.
- Servos → spread across port pairs or onto the 6 V module.

---

## 6. Shopping list

| Item | Part # | Price | Tier |
|---|---|---|---|
| Colour/distance sensors ×2 | REV-31-1557 (or Brushland ×2 at $25) | 2 × $20.75 | MUST |
| IR break-beam | Adafruit 2167 | $2.95 | MUST |
| Grounding strap | REV-31-1269 | $5.50 | MUST |
| Main power switch (if none) | REV-31-1387 (or goBILDA 3103-0005-0001) | $13 | MUST |
| 60 mm steel flywheel | 3628-0014-0060 | $12.99 | TUNE |
| 5-turn Dual Mode Servo (or TBE V2 + programmer) | 2000-0025-0502 / -0503 (REV-11-3174 + 3102-0001-0001) | $49.99 ($48 + $12.99) | TUNE |
| Magnetic limit switch (turret home) | REV-31-1462 | $17.50 | TUNE |
| 6 V servo power | 3125-0001-0001 or REV-11-1855 | $69.99 / $90 | TUNE |
| Spare batteries ×2 (3–4 total for events) | 3100-0012-0020 / REV-31-1302 | $64.99 / $60 each | TUNE |
| Ferrites, cable clips | – | ≈ $10 | TUNE |
| Intake motor | 5203-2402-0005 | ≈ $54.99 | OPTIONAL |

**Rough total (our arithmetic): MUST ≈ $63; TUNE ≈ $280–320**, depending on the choices in each row.

**Supply warnings (checked 10/20):** out of stock: Brushland Color Rangefinder. Sold out: Limelight 3A (ServoCity), goBILDA 5203 312 rpm, AndyMark practice POLLEN. Order alternatives early.

---

## 7. Questions only the robot can answer (measure in week 1)

1. Limelight model (3A?).
2. Ring and pinion tooth counts; servo variants and which are in continuous mode; which hub ports (5 V) or 6 V module.
3. How the flywheel is driven (miter gear? ratio?); what the grey hood servo and black foam block are for.
4. Intake wheel size (72 or 96 mm boot wheels?), gear ratio, and whether the hinged crossbar floats.
5. The planned indexer path from the intake up through the turret, and how many balls it holds (must be **4**).
6. Robot height and footprint at each end of the turret's travel; robot weight.
7. Battery count and age.
8. Spare starter-kit parts on the shelf (312/117 rpm motors, 96 mm Hogbacks, Gecko wheels, battery mount).

---

## Sources

- **Manual TU05** (G303, G407–G409, R102, R105, R201–R202, R401–R403, R502–R505, R601, R603, R605, R609–R612, R702, R705, R707–R711): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A** QA3, QA4, QA23, QA34 (pending): https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N · **TU05**: https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **Inspection Checklist (IC)**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check · **Inspection Quick Reference (IQR)**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-reference
- **goBILDA**:
  - Steel Flywheel 60 mm: https://www.gobilda.com/steel-flywheel-60mm-diameter-115g-551-g-cm/
  - 5203 6000 rpm motor: https://gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder
  - 5-turn servos: https://gobilda.com/2000-series-5-turn-dual-mode-servo-25-2-torque
  - 6V Servo Power Injector: https://www.gobilda.com/6v-servo-power-injector-6-channel-8-15v-input/
  - Pinpoint V2: https://www.gobilda.com/pinpoint-v2-odometry-computer-imu-sensor-fusion-for-2-wheel-odometry/
  - Odometry pods: https://www.gobilda.com/odometry/
  - Battery: https://www.gobilda.com/12v-nimh-nested-battery-3000mah-mh-fc-xt30-connector/
  - StarterBot guide and code: https://www.gobilda.com/ftc-starter-bot-resource-guide-2026-2027-season/
  - Global Shutter camera: https://www.gobilda.com/global-shutter-usb-camera-with-gobilda-case-100-fps/
- **REV**:
  - Control Hub specs: https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics
  - Servo Hub: https://www.revrobotics.com/rev-11-1855/
  - Switch Cable and Bracket: https://www.revrobotics.com/rev-31-1387/
  - Through Bore Encoder V2: https://www.revrobotics.com/rev-11-3174/
  - Magnetic Limit Switch: https://www.revrobotics.com/rev-31-1462/
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
