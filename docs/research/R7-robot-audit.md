# R7: Audit of the Current Robot Against BIOBUZZ Needs (FTC 12303)

Prepared 2026-10-20 for the 12303 coach and the strategy agents that follow.

**What this covers.** What the three photos show, the probable ball path from floor to launch, a check against each BIOBUZZ requirement, likely failure points, and a ranked list of fixes. Each fix is tagged **quick-win**, **medium** or **big-change**. The report ends with questions only the team can answer.

**Inputs.**
- Three photos taken 2026-10-20 (listed in §1).
- Competition Manual TU05 (2026-10-15), cited as "M p.N" with rule numbers.
- Event Field Setup Guide V1.0.
- The official Q&A board.
- Manufacturer pages for every part identified.
- Sibling research files R1 (rules), R4 (community data) and R5 (hardware).

**Evidence labels**
- **[V] VERIFIED**: stated directly by a cited primary source (manual, FIRST guide or Q&A, manufacturer page).
- **[P] PHOTO**: seen in a photo. Confidence is high (H), medium (M) or low (L), and the crop file is cited.
- **[D] DERIVED**: my arithmetic. The script is `scratch/scripts/r7_calcs.py`.
- **[I] INFERENCE**: engineering judgment. Treat it as a hypothesis to test on the robot.
- **[C] COMMUNITY**: forum or third-party data, mostly via R4. Not official.

**Orientation used throughout.**
- FRONT is the intake side; photo …002335726 looks at the front.
- LEFT and RIGHT are the robot's own left and right when facing forward. In the front photo, the robot's left is the right side of the image.

---

## 0. Bottom line

### 0.1 What the robot is
A **turret-mounted single-flywheel shooter on a mecanum chassis with a full-width roller intake**. This is the "HIVE bot" design that most early BIOBUZZ contenders use [C: R4 §3.5]. The launcher is essentially the goBILDA BIOBUZZ StarterBot launcher (1:1 Yellow Jacket plus a Hogback wheel) placed on a turret [V: GB12 assembly steps 44 and 57; R4 §7]. The design direction is sound for the main scoring path: each HIVE TIP is worth 20 points, and TIPs drive the POLLINATOR RPs (M p.91 Table 10-2/10-3).

### 0.2 The robot is unfinished. Missing or not visible:
- the internal transfer/indexer ("internal spinners"),
- the hood surface that sets the launch angle,
- any ball-count or colour sensors,
- the Limelight,
- turret position feedback,
- a main power switch,
- robot signs,
- cable management.

### 0.3 The five biggest risks, in priority order
1. **Intake actuator.** A goBILDA Dual Mode Servo produces about 2–3 W, against about 22.6 W for a goBILDA Yellow Jacket motor [D]. At the hub's 5 V, a servo-driven roller reaches only about **0.6–2.9 ft/s** of surface speed (≈10–45% of the drive's 6.4 ft/s top speed) with a 3.5 in roller and no gearing [D]. Expect slow, stall-prone pickup, especially with a 3.6 in NECTAR wedged under the roller.
2. **Turret range and feedback.** The turret is driven by a grey goBILDA Dual Mode Servo through a Compact ServoBlock, with a 60-tooth pinion on a large ring gear [P-M]. At the estimated 4.5–5.5:1 ring ratio, a standard 300° servo gives only **≈55–67° of turret travel**. There is no encoder, home switch or hard stop visible [D/P].
3. **Shooter consistency with two ball sizes.**
   - A single 72 mm, 55 g Hogback wheel stores little energy. Each shot costs roughly **18% (POLLEN) and 33% (NECTAR) of wheel speed** [D, estimate].
   - A fixed hood gap cannot grip both a 2.8 in POLLEN and a 3.6 in NECTAR (§5.2).
   - This single-wheel, backspin layout is the type that showed **44% bounce-outs** in one team's test [C: R4 §6].
   - The hood surface is not built yet.
4. **No G407/G408 protection yet.**
   - G407: a robot may never CONTROL more than 4 elements.
   - G408: it may never CONTROL opponent NECTAR.
   - The Q&A says **neither rule has a "MOMENTARY" exception** (QA3, QA4, QA23).
   - A full-width roller will grab anything it touches. It needs sensors and an automatic stop/reject from day one.
5. **Packaging and robustness.**
   - Both REV hubs stand upright on the front-top bridge, directly above the intake path. They are exposed to hits, falling balls and ESD.
   - The battery hangs outside the frame on one Velcro strap.
   - Power and servo wires are loose near the mecanum wheels and the turret ring gear.
   - The flywheel motor is held in a printed clamp shimmed with cloth tape.
   - The flywheel may be driven through a right-angle gear at 6000 rpm (unconfirmed).

### 0.4 Two inspection blockers to clear now
- **R702**: the only programmable vision coprocessor allowed is the **Limelight 3A**. The **Limelight 3G is explicitly prohibited** (M p.140–141). Confirm the model.
- **R603**: a legal main power switch is required, and none is visible.

### 0.5 Good news
- **SWYFT Spike drive motors** are on the R501 legal motor list (M p.132).
- **The battery is legal.** The label reads "12V NiMH Nested Battery … 3100-0012-002_", which is legal per R601 Table 12-4 (M p.135).
- **The actuator budget has room.** As built: 5 of 8 motors and about 3–4 of 8 servos (R503).
- **Odometry hardware appears to be on the robot.** The pod looks like a goBILDA Swingarm Odometry Pod [P-M].
- **The turret already has a hollow centre**, so balls can be fed up through it.
- **The intake hangs on hinges.** A floating intake can help with two ball sizes.

### 0.6 Top-10 actions (details in §7)

**Timing.** Michigan Metro-Detroit league meets are on 10/24, 10/30, 11/7, 11/11 and 11/19, and the League Tournament is 12/4–5 [V: FTC Events via R4 §8.1]. "Quick-win" means doable before the next meet.

| # | Action | Tag |
|---|---|---|
| 1 | Confirm the Limelight is a **3A**. Fit a legal main power switch, robot signs and a grounding strap. | quick-win |
| 2 | **Lock the turret** (pin it at 0°) for the first events and aim by rotating the mecanum chassis with Limelight `tx`. Keep the servo turret as a later upgrade. | quick-win |
| 3 | Velocity-control the flywheel (`setVelocity`, PIDF). **Feed only when the wheel is back to speed** (goBILDA StarterBot pattern). Use a distance-to-RPM table. Shoot POLLEN only at first. | quick-win (software) |
| 4 | Add **sensors for 4-ball counting and opponent-NECTAR rejection**: colour/distance at the intake mouth and at slot 4, plus a break-beam at the shooter feed. Auto-stop the intake at 4. | quick-win → medium |
| 5 | Wire management, robot and electrical: re-route main power off the wheel faces; strain-relieve inline XT30s; give the turret a service loop with no cables near the ring/pinion mesh; nest the battery in a U-channel cradle with two straps. | quick-win |
| 6 | Put a **guard or lid over the turret's open centre and over the flywheel**. This covers G409 (no catching or deflecting), G407 and finger safety (R202). | quick-win |
| 7 | **Move the intake to a DC motor**, e.g. a goBILDA 5203 at 312/435 rpm for 96 mm Boot-Wheels, or 1150 rpm for 72 mm wheels. Keep servos for the indexer and gate. | medium |
| 8 | **Add flywheel inertia**: a steel/brass disc or a second wheel. Make the hood **compliant or adjustable** for 2.8 and 3.6 in. Test bounce-outs early, and add a counter-roller if needed. | medium |
| 9 | Re-drive the turret with feedback: a **312 rpm Yellow Jacket + home switch**, a **5-turn servo**, or a CR servo plus a **REV Through Bore Encoder**. Add hard stops and plan cable travel. | medium → big |
| 10 | Copy the goBILDA StarterBot's **corner Gecko-wheel flower extractor**, which pulls POLLEN from FLOWER bottoms. Decide on NECTAR (§5.4) only after POLLEN cycles are reliable. | medium |

---

## 1. Evidence: photos and crops

The originals were copied to `scratch/downloads/photos/`. Crops were made with `scripts/r7_crop.py` (python3 -I) and saved under `…/scratchpad/downloads/photos/crops/`.

| Photo | View | Key crops |
|---|---|---|
| PXL_20261020_002925221.jpg (4032×2268) | From above, at the robot's front-left corner looking toward the rear. Hubs on the front bridge; turret at the rear. | `t_turret.jpg`, `t_wheel.jpg` (rim text "Ø72mm – 50A"), `t_motor.jpg`, `t_motorend.jpg`, `t_miter.jpg`, `t_hoodservo.jpg`, `t_pinion.jpg`, `t_pinion_teeth.jpg`, `t_ring1.jpg`, `t_ringinner.jpg`, `t_hole.jpg`, `t_hubs.jpg`, `t_under.jpg`, `t_left.jpg`, `t_battery.jpg`, `t_batlabel.jpg`, `t_frontchan.jpg` |
| PXL_20261020_002335726.jpg (2268×4032) | Front of the robot, from above | `f_intake.jpg`, `f_intakeleft.jpg`, `f_intakecenter.jpg`, `f_geartrain.jpg`, `f_lowside.jpg`, `f_arch.jpg`, `f_frame.jpg`, `f_blackbridge.jpg`, `f_turret.jpg`, `f_wheelgear.jpg`, `f_miter.jpg`, `f_motor.jpg`, `f_hole.jpg`, `f_foam.jpg`, `f_pinion.jpg`, `f_pinion_teeth.jpg`, `f_right.jpg`, `f_rightwheel.jpg`, `f_odo1.jpg`, `f_odo2.jpg`, `f_swyftlabel.jpg` |
| PXL_20261020_003126527.jpg (2268×4032) | Close-up of one drive module | `w_module.jpg`, `w_top.jpg`, `w_led.jpg` |

Reference photos downloaded for comparison:
- the goBILDA Dual Mode Servo product image (`downloads/r7-gobilda-servo/servo.jpg`): its case is grey, matching the grey servos on the robot;
- the goBILDA odometry pod image (`downloads/r7-gobilda-odopod/pod.jpg`).

---

## 2. As-built inventory

| # | Subsystem | What the photos show | Best identification | Conf. | Evidence |
|---|---|---|---|---|---|
| 1 | **Drive modules** | Round black housings printed "SWYFT … SWYFTROBOTICS.COM DRIVE V2". A 12-sided rim of nylock nuts holds the roller axles. Bolted to black steel side plates with M4 screws. Black motors behind the modules inside the frame. | **SWYFT Drive V2, SR-DRIVE-02.** Mecanum, 86 mm, 40A rollers. Each module contains a **SWYFT Spike** motor (SR-MOTOR-DC-01: 12 V, 6000 rpm, 9.2 A max, 1.47 kg·cm) and a **12.7:1** all-metal planetary (≈475 rpm wheel). 575 g per module; 6.4 ft/s advertised; designed for robots up to 40 lb. [V: SW1] | H | photo 3, `w_module.jpg`, `t_under.jpg`, `f_rightwheel.jpg` |
| 2 | **Chassis** | Black perforated aluminium channels and black steel plates form the base. Silver goBILDA-pattern channels are used for the upper structure. | Probably the **SWYFT frame kit**: "steel plates and aluminum channel extrusions" [V: SW1]. Silver parts are goBILDA **1120 U-Channel** and **1121 Low-Side U-Channel** (8 mm grid, 14 mm holes on a 24 mm pitch). | M | `t_left.jpg`, `f_frame.jpg`, `f_lowside.jpg` |
| 3 | **Front arch ("bridge")** | Two vertical silver U-channel legs. A black U-channel across the top (about 13 large holes ≈ 336 mm ≈ 13.2 in between silver end plates). Two short silver U-channel segments hang under the bridge. | goBILDA-pattern structure; overall front width ≈ 15–16 in (estimated from hole counts). | M | `f_arch.jpg`, `f_blackbridge.jpg` |
| 4 | **Control electronics** | **REV Control Hub** and **REV Expansion Hub**, each standing on edge in a red 3D-printed holder on top of the front bridge, faces toward the robot's sides. Labels and LEDs visible. Expansion Hub has a yellow XT30 and white JST-VH motor plugs on its side edge. Hand-labelled tape flags on cables. | Control Hub REV-31-1595 (209 g, 143×103×29.5 mm [V: REV1]); Expansion Hub REV-31-1153. Legal per R701 (M p.140). | H | `t_hubs.jpg`, `f_hubtop.jpg` |
| 5 | **Battery** | Black shrink-wrapped pack with a goBILDA label reading "12V NiMH Nested Battery (3000mAh, MH-FC XT30 Connector) … -0012-002_" (last digit hidden by Velcro). Strapped with **one Velcro strap** to the outside of the left-rear structure. Shrink-wrap is scuffed. | **goBILDA 3100-0012-0020**: 597 g, Sub-C NiMH, 20 A ATM fuse, 16 AWG, "nested" shape designed to fit **inside 1120 U-Channel** for protection [V: GB10]. Legal per R601 Table 12-4 (M p.135). | H (pack) / M (SKU digit) | `t_battery.jpg`, `t_batlabel.jpg` |
| 6 | **Main power path** | Red/black 16–18 AWG silicone leads loop around the frame. **Inline XT30 junctions**: one on the front-left leg (orange zip tie), one running across the outside of a side plate **directly above a mecanum wheel**. No power switch or grounding strap seen. | Wire gauge looks compliant with R609 (≥18 AWG for main/motor power, M p.138). **R603 main switch: not visible.** | M | `t_left.jpg`, `w_module.jpg`, `f_right.jpg` |
| 7 | **Intake** | One shaft across the full front width, carrying many staggered black silicone "boot" paddle wheels ("toe/heel" profile), about 3.3–3.9 in diameter. Mounted under a silver low-side U-channel crossbar (about 10–11 large holes ≈ 9.4–11.3 in). The crossbar hangs on **two goBILDA-style hinges** from the hanging segments in row 3. On the robot's left, two large perforated aluminium MOD 0.8 hub-mount gears (≈100T and ≈120T, estimated) mesh. The **lower gear is on the roller shaft**; the upper gear's hub has the 4-screw 16 mm pattern of a ServoBlock hub-shaft. | Wheels are almost certainly **goBILDA 3615 Series Boot-Wheels** (8 mm REX, 30A silicone on acetal; 72 mm = 14 g, 96 mm = 37–38 g). Turning the "toes" toward the robot lifts items as they are taken in [V: GB9]. The size in the photo suggests **96 mm** [P-M]. Per the coach, a **goBILDA Dual Mode Servo (continuous mode)** drives it, probably via a Compact ServoBlock to the upper gear (≈1.2:1 speed-up), but the servo is not visible [I]. | M | `f_intake.jpg`, `f_intakeleft.jpg`, `f_intakecenter.jpg`, `f_geartrain.jpg`, `f_arch.jpg` |
| 8 | **Internal transfer / indexer ("internal spinners")** | Not visible. The space behind the intake, under the hub bridge, is partly taken by the two hanging segments that carry the intake hinges. | Coach: goBILDA Dual Mode Servos. **Not built yet.** | — | `f_arch.jpg` |
| 9 | **Turret bearing / ring** | Large aluminium ring gear with fine **external** teeth on a large-bore bearing, supported by goBILDA-pattern aluminium brackets in a polygon. The inside of the ring is a smooth cylinder with a slot and a small bracket, so the **centre is hollow**. | Vendor unknown; not found among goBILDA/ServoCity products [V: searches]. Pitch probably MOD 0.8 (it meshes with a goBILDA MOD 0.8 pinion). At the estimated 4.5–5.5:1 ratio over a 48 mm-PD pinion, the pitch diameter is ≈ 8.5–10.4 in (≈ 270–330T). This is a rough photo-scale estimate [D]; count the teeth. A COTS turntable is a legal single-DoF part (R303-E, M p.127). | M (layout) / L (size) | `t_ring1.jpg`, `t_ringinner.jpg`, `t_hole.jpg` |
| 10 | **Turret drive** | A **grey servo** under a silver block with mounting lugs. On top, a clear-anodised **≈60-tooth** aluminium gear on a 16 mm-pattern hub meshes with the ring. No encoder, limit switch or hard stop visible. | Servo: **goBILDA 2000-series Dual Mode Servo** (grey case matches [V: GB2 image]); variant unknown. Block: **goBILDA Compact ServoBlock, 43 mm** (3217-0001-2501; isolates radial load from the servo; 16 mm-pattern hub-shaft) [V: GB7]. Gear: **goBILDA 2302-series 60T MOD 0.8 hub-mount gear** (14 mm bore, PD 48 mm) [V: GB8; count ≈ 56–60 from photo]. Estimated ring:pinion ≈ **4.5–5.5:1**. | M | `t_pinion.jpg`, `t_pinion_teeth.jpg`, `f_pinion.jpg` |
| 11 | **Flywheel** | One rubber-tyred wheel with a black spoked rim, yellow pin-stripe and printed **"Ø72mm – 50A"**. Hex shaft supported on **both sides** by printed arms (flanged bearing/bushing visible on the right). | **goBILDA Hogback Traction Wheel, 72 mm, 50A** (3626-0014-0072; 55 g; plastic core with bonded silicone tread) [V: GB5]. The goBILDA StarterBot uses the 96 mm version on its launcher [V: GB12]. | H | `t_wheel.jpg`, `f_wheelgear.jpg`, `f_motor.jpg` |
| 12 | **Flywheel motor** | Silver can-type gearmotor lying along the back of the hood, its output end toward the wheel. Held in a printed clamp; the can is wrapped in **red cloth tape**, apparently as a shim. Gearbox face shows Phillips screws and partial "52…" lettering. | **goBILDA Yellow Jacket at 1:1 / 6000 rpm**, either the native 5203-2402-0001 or a 19.2:1 converted with goBILDA's 11T Conversion Coupler (as in the StarterBot). Native specs: 6000 rpm free, 9.2 A stall, 1.47 kg·cm, 28 counts/rev, 391 g, 3.5 mm bullet power leads, 4-pin JST-XH encoder [V: GB1]. Legal per R501 (M p.132). | H (type) / M (variant) | `t_motor.jpg`, `t_motorend.jpg` |
| 13 | **Motor-to-wheel drive** | Next to the wheel, on the far end of its shaft, a small silver part with **radial conical teeth** sits beside a printed bracket. In the top view the motor axis appears **perpendicular** to the wheel axle. | Probably a **right-angle (miter/bevel) gear pair**, roughly 1:1 [I, L–M]. If so, at 6000 rpm a printed bracket must hold the mesh precisely. | L–M | `f_miter.jpg`, `f_wheelgear.jpg`, `t_miter.jpg` |
| 14 | **Hood body** | Orange 3D-printed **horseshoe ("C")** lying flat on the turret ring; its opening sits over the ring's hollow centre. The flywheel is at one tip. At the other tip: a **dense black foam block** in a pocket and a **grey goBILDA servo** bolted to the end face. **No curved hood surface over the wheel yet.** | Custom part, material unknown (PLA/PETG?). The grey servo probably adjusts a hood or pad, or acts as a gate [I]. | M | `t_turret.jpg`, `f_turret.jpg`, `f_foam.jpg`, `t_hoodservo.jpg`, `f_hole.jpg` |
| 15 | **Odometry** | A gold/yellow-faced pod with a dark logo window and a small black omni wheel near the left chassis rail. A second similar small wheel is visible on the rail. | Looks like **goBILDA Swingarm Odometry Pod(s)** (3110-0001-0001: 48 mm wheel, 2000 counts/rev, 43 mm wide, 121 g) [V: GB11; P-L/M]. Unknown whether a goBILDA Pinpoint is fitted. | L–M | `f_odo1.jpg`, `f_odo2.jpg`, `f_rightwheel.jpg` |
| 16 | **Vision** | No Limelight visible. | Coach: "Limelight camera" (model unconfirmed). | — | — |
| 17 | **Other sensors** | 4-wire JST-PH cables (encoders). No colour, distance or beam-break sensors visible. A blue LED glow near the drive module is unidentified. | — | L | `w_led.jpg` |
| 18 | **Cabling** | Main power loops hang free across the front and top. Servo leads from the hood servo hang down beside the **ring gear**. Some zip ties, many unsecured runs. | See §6 F7. | H | `t_left.jpg`, `t_hubs.jpg`, `t_hoodservo.jpg`, `f_right.jpg` |

### 2.1 Parts family
The parts strongly overlap the **goBILDA FTC Starter Kit 2026-27 (3200-4008-2627)**:
- Dual Mode Servos 2000-0025-0002 ×1 and -0003 ×2;
- 3× Compact ServoBlocks;
- 5× 96 mm Hogback wheels;
- 2× 72 mm GripForce Gecko wheels;
- 3× 312 rpm and 1× 117 rpm Yellow Jackets plus an **11T 1:1 conversion coupler**;
- a battery mount with strap [V: GB12 kit list].

If the team owns that kit, **spare 312 rpm and 117 rpm motors, 96 mm Hogbacks and Gecko wheels may already be on the shelf**. Several fixes below use them.

---

## 3. Probable architecture and ball path (floor to launch)

```
FLOOR ──> [1] Full-width boot-wheel roller (front, hinged crossbar, servo→ServoBlock→~1.2:1 spur gears)
              │   balls pass under the hub bridge (two hanging channel segments are in this zone today)
              ▼
          [2] Transfer / indexer "internal spinners" (CR servos)  ← NOT BUILT; must rise ~8-12 in and
              │   travel ~6-10 in rearward; must hold ≤4 (G407) incl. the 4 preloads
              ▼
          [3] Up through the hollow centre of the turret ring (rotating joint) into the orange horseshoe
              ▼
          [4] Single 72 mm Hogback flywheel at one tip of the horseshoe (6000 rpm YJ, probable miter drive);
              ball squeezed against a hood/pad (black foam block / grey servo at far tip; hood surface not yet built)
              ▼
          LAUNCH toward the up-facing CELL (opening 53.5–65.6 in high, faces outward and 30° up)
```

Turret rotation: grey Dual Mode Servo → Compact ServoBlock → 60T pinion → ring gear (≈4.5–5.5:1).

**Why the design makes sense [I].** Feeding up through the turret axis lets the shooter rotate while the intake and indexer stay fixed to the chassis. This is the standard turret pattern; Mona Shores and CyberCoyotes are building similar machines [C: R4 §3.5].

**What must still exist for the path to work [I]:**
- **Lift.** A riser from the intake to the ring bore: a vertical belt or roller tower, or a ramp plus a "windmill" feeder like the StarterBot's.
- **Capacity.** At least four balls stored in single file. Four POLLEN need about 11.2 in of path and four NECTAR about 14.4 in [D]. Any mix must fit inside the 18 in starting cube with the four preloads (R102, M p.122; preloads may stick out of the cube per the R102 box).
- **Feed gate.** A gate or kicker that holds the next ball until the flywheel is at speed.
- **Bore clearance.** The ring's inner bore must pass a 3.6 in NECTAR "not perfectly spherical" (M p.74) with margin. Visually the bore looks large enough (≈5–6 in, estimated).

---

## 4. Actuator, port and power budget

### 4.1 Legality of the identified actuators

| Actuator | Rule | Status |
|---|---|---|
| SWYFT Spike SR-MOTOR-DC-01 (×4, inside the drive modules) | R501 Table 12-1 lists "SWYFT Robotics SWYFT Spike Motor SR-MOTOR-DC-01" (M p.132) | **Legal** [V] |
| goBILDA Yellow Jacket 1:1 (flywheel) | R501 lists goBILDA 5201/5202/5203/5204. A legal gearmotor "may be used with or without the provided gearbox" (M p.132) | **Legal** [V] |
| goBILDA Dual Mode Servos | R502: ≤ 8 W mechanical and ≤ 4 A stall at 6 V (M p.133, checked visually). The 25-2 Torque is 21.6 kg·cm at 50 rpm at 6 V, so 0.25 × 2.118 N·m × 5.24 rad/s ≈ **2.8 W**; stall 2.5 A [V: GB2; D]. 25-3 and 25-4 come out about the same (≈2.75 W). **2000-0025-0003 is named in R502's examples.** | **Legal.** Bring spec sheets for -0002, -0004 and the 5-turn versions [V/D] |
| Switching to continuous rotation with the goBILDA 3102 programmer | R504-C allows "modification for continuous rotation" when the manufacturer specifies it (M p.134) | Legal [V] |
| goBILDA **Servo Travel Tuner** (if anyone suggests it) | R612 prohibits it by name (M p.139) | **Not legal.** Use the SDK's 500–2500 µs PWM range instead [V] |

### 4.2 Count against R503 (8 motors, 8 servos; M p.134)

| | As built / planned by coach | After the recommended changes (example) |
|---|---|---|
| Motors | 4 drive (Spike) + 1 flywheel = **5/8** | 4 drive + flywheel + **intake motor** + **turret motor** (or a 2nd flywheel) = 7/8 |
| Servos | intake (CR) + turret + hood + 1–2 indexer = **4–5/8** | indexer (CR) + gate + hood + flower-extractor ×1–2 (+ Limelight tilt, optional) = 5–6/8 |

### 4.3 Power regulation (R505, M p.134)
- **Ports.** Each REV hub motor port takes up to 2 motors and each servo port up to 2 servos.
- **Servo power on the hub ports is limited** [V: REV1]:
  - 5 V supply;
  - **2 A total per port pair** (0-1, 2-3, 4-5);
  - **5 A total** per hub, shared with the +5 V ports.
- **Stall currents.** One goBILDA Dual Mode Servo stalls at 2.0 A (4.8 V) to 2.5 A (6 V) [V: GB2/GB3]. So **a stalled intake or turret servo can brown out the other servo on its pair** [D/I].
- **Fix.** A **REV Servo Hub (REV-11-1855)** or **goBILDA 6V Servo Power Injector (3125-0001-0001)** gives 6 V and more current. Both are legal regulators in R505 Table 12-3. 6 V also makes each servo about 20% faster (§5.2).

---

## 5. Evaluation against BIOBUZZ requirements

### 5.0 Summary table

| Requirement (source) | Current robot | Verdict |
|---|---|---|
| Launch POLLEN (2.8 in) and NECTAR (3.6 in) into a CELL opening 53.5–65.6 in high, 20 in wide × 14 in tall, ≈12 in deep, facing outward and 30° up (M p.70–72, Fig 9-9 to 9-11) | 6000 rpm, 72 mm flywheel on a turret; hood not built | **Feasible for POLLEN.** Speed headroom is fine. Consistency (inertia, spin, two sizes) is the gap. |
| One intake/indexer for both sizes; balls "not perfectly spherical and may vary in size" (M p.74) | Floating (hinged) boot-wheel roller; indexer not built | **Partially.** The hinge helps; the indexer, hood and gates must take 2.8–3.6 in. |
| Never CONTROL > 4 (G407, M p.108); never CONTROL opponent NECTAR (G408, M p.108–109); no MOMENTARY exception (QA3, QA4, QA23) | No sensing; full-width roller | **Gap (high).** |
| NECTAR into a FLOWER only in the last 60 s (G410, M p.109): 4.0 in opening at 21.5 in with a 1.25 in backstop (M p.72–73) | No mechanism; the turret could try a lob | **Gap.** Decide whether to pursue it (§5.4). |
| Remove POLLEN only from the FLOWER bottom (G418, M p.112): Retrieval Opening 3.55 in tall × 3.57 in deep; bottom ring ID 2.79 in, 0.43 in thick (M p.72–73) | Front roller might reach; untested | **Unknown / probable gap.** Add a dedicated extractor (§5.5). |
| Start inside 18 × 18 × 18 in (R102, M p.122); stay within 18 × 24 × 29 in, physically limited (R105/G416, M p.123–125, 111) | Size not measurable from photos; turret may overhang | **Unknown.** Measure at every turret angle (§5.6). |
| ≤ 8 motors and ≤ 8 servos (R503, M p.134) | 5 motors, ~4–5 servos | **OK**, with room to spare. |
| Don't catch or deflect spill from a TIP (G409, M p.109; QA17) | Open hood centre and open top | **Gap.** Add a guard. |
| Don't touch the HIVE except by launching into the up CELL (G417, M p.111–112) | Tall robot near the HIVE frame | **Driver/AUTO discipline.** |
| Vision: Limelight **3A only** (R702 Table 12-9; 3G prohibited, M p.140–141) | Model unknown, not mounted | **Verify.** |
| Battery (R601), switch (R603), frame isolation/grounding (R605), inspectable electronics (R606), wiring (R609/R610), signs (R401–R403) | Legal battery; no switch, signs or strap visible | **To do before inspection.** |
| AUTO: 30 s; 4 preloads; TIP 1 needs only 3 POLLEN in the starting up CELL [V: EFSG §12.3]; LEAVE 3 + PARK 5 | Shooter, indexer and code not finished | **Achievable once the indexer and shooter work.** Highest-value AUTO target. |

### 5.1 Launching into the HIVE

#### Geometry [V: M p.70–72]
- Pivot at 43.95 in.
- Up-cell opening from **53.5 in to 65.6 in** above the tiles.
- Opening **20 in wide × 14 in tall**, a pentagon with the shoulder at 7.61 in.
- Cell about **12.04 in deep**; the two cells are 18.84 in apart.
- The HIVE rests **30°** from level.
- The up-cell opening faces outward and upward, so shots come from the outer side of the up cell (geometry worked out in R5 §3.4).

#### Required exit speed [D] (no drag; target = opening centre ≈ 59.6 in; ball descending at entry)

| Horizontal distance | Launch at 14 in | Launch at 18 in | 72 mm wheel rpm (ideal ½-surface-speed exit) | Same at ~75% efficiency |
|---|---|---|---|---|
| 3 ft | 5.1 m/s @ 71° | 4.9 m/s @ 70° | ≈2,600–2,700 | ≈3,500–3,600 |
| 6 ft | 5.7 m/s @ 61° | 5.6 m/s @ 60° | ≈3,000 | ≈3,950–4,050 |
| 10 ft | 6.6 m/s @ 55.5° | 6.5 m/s @ 54.5° | ≈3,450–3,500 | ≈4,600–4,650 |

- **A fixed 65° launch angle** needs only 5.0–6.9 m/s over 3–10 ft (launch at 14–18 in), so **a fixed hood near 60–65° with speed control is enough** [D]. An adjustable hood is optional. R5's table with a drag estimate agrees: 4.9–7.7 m/s for 2–12 ft.
- **Speed headroom.** The 6000 rpm free speed (lower under load and battery sag) covers about 3,500–4,700 rpm. That is fine to about 10 ft, but **closed-loop velocity control is mandatory**. The goBILDA StarterBot uses `setVelocity` with PIDF (40, 0, 0, 12.5) and feeds only when above 1200 of a 1250 ticks/s target [V: GB12 example code `BioBuzzStarterBotTeleop.java`].

#### Energy per shot [D, estimate]
- **Inputs.**
  - Hogback 72 mm mass: 55 g [V: GB5].
  - Wheel plus shaft, hub and gears: I ≈ 6×10⁻⁵ kg·m².
  - Ball masses: 0.055 and 0.091 lb.
  - Ball model: thin shell rolling off the hood, with 1.5× losses.
- **Result.** Each shot takes **≈18% (POLLEN) and ≈33% (NECTAR) of wheel speed** during contact. The motor cannot react within the 10–20 ms the ball is in contact. So exit speed (and range) depends on ball type and on how fully the wheel recovered.

| Flywheel option (≈7.5 m/s ideal exit) | I (kg·m²) | rpm needed | POLLEN drop | NECTAR drop |
|---|---|---|---|---|
| 1× 72 mm Hogback (as built) | 6.1×10⁻⁵ | ~3,980 | 18% | 33% |
| 2× 72 mm Hogback | 1.1×10⁻⁴ | ~3,980 | 10% | 17% |
| 1× 96 mm Hogback (82 g [V: GB6]) | 1.4×10⁻⁴ | ~2,980 | 14% | 25% |
| 2× 96 mm Hogback | 2.6×10⁻⁴ | ~2,980 | 7% | 12% |
| 72 mm + 250 g steel disc (r = 40 mm) | 2.6×10⁻⁴ | ~3,980 | 4% | 7% |

Conclusion [D/I]:
- Aim for **I ≳ 2.5–3.5×10⁻⁴ kg·m²**. About 3.4×10⁻⁴ keeps a NECTAR shot to ≤5% at 4,500 rpm. R5 §4.2 reaches the same conclusion (2–4×10⁻⁴).
- Recovery after a shot is quick, about 0.1–0.3 s at ~20 W. So **gate feeding on measured velocity** and fire the 4 balls about 0.3 s apart.
- Heavier flywheels take about 1–2 s to spin up from rest, so keep the wheel spinning while lined up.

#### Spin and bounce-outs [C: R4 §6]
- FTC 14343 measured **24 of 43 kept (44% bounce-out)** with a fixed solid hood that puts backspin on the ball.
- A single wheel against a stationary hood always adds backspin.
- Remedies seen in the community: a powered **hood roller / counter-roller at about 2× flywheel rpm** (≈ zero spin, CyberCoyotes), a compliant hood, or topspin designs.
- **Test bounce-outs into a real or replica CELL in week 1**, before adding RPM.

#### Turret aiming tolerance [D, R5 §4.3]
The opening allows about ±8.6 in sideways for POLLEN, which is **±4° at 10 ft and ±8° at 5 ft**. The servo, ServoBlock and gear backlash (≈1–2° is typical, not measured) is acceptable. **Elevation and range consistency matter more than azimuth.**

### 5.2 Two ball sizes through one intake, indexer and shooter
- **Sizes** [V: M p.74]: POLLEN ≈ 2.8 in (71 mm), NECTAR ≈ 3.6 in (91 mm), "not perfectly spherical and may vary in size". The 0.8 in difference is about 22% of the NECTAR diameter.
- **Fixed hood gap** [D]:

| Gap | POLLEN compression | NECTAR compression | Effect |
|---|---|---|---|
| 2.4 in | 14% | 33% | NECTAR over-squeezed or jammed |
| 2.6 in | 7% | 28% | POLLEN barely gripped; NECTAR heavy |
| 3.0 in | none | 17% | POLLEN not gripped |

  So **one fixed gap cannot serve both** sizes. 14343 abandoned a passive dual-element hood for an active switch, and removed a transfer stage because of "extreme compression" [C: R4 §3.5, §6].
- **Options**:
  - (a) A **spring- or rubber-loaded hood** that deflects about 0.8 in. The black foam block may be the start of this.
  - (b) A **servo-set gap** with two presets, chosen from a colour sensor. The grey hood servo could do this.
  - (c) **Shoot POLLEN only** and handle NECTAR another way (FLOWER or human-player strategy).
- **Intake.** The **hinged crossbar** lets the roller ride up over a NECTAR. Add a **hard stop** setting the minimum gap for POLLEN (≈2.3–2.4 in, ~15% squeeze, to be tuned) and a light spring or gravity preload. The 30A boot wheels add about 0.2–0.3 in of compliance [I; GB9 notes toe/heel orientation changes how the ball is lifted].
- **Indexer.** Channels sized for 3.6 in **plus clearance** (≈3.8–4.0 in) will let POLLEN rattle. Use compliant side rollers or a single-file path with belts or flex wheels on one side that grip both sizes.

### 5.3 Possession limits: G407 and G408
- **Rules.**
  - G407: no more than 4 SCORING ELEMENTS CONTROLLED at a time.
  - G408: no CONTROL of opponent NECTAR.
  - Penalties: G407 is a VERBAL WARNING, escalating to MAJOR + YELLOW CARD per match if STRATEGIC. G408 is a VERBAL WARNING, escalating to a YELLOW CARD if STRATEGIC (M p.108–109, text checked visually by R1).
  - The TU05 boxes say **repeated** violations are likely STRATEGIC **"even if the ROBOT has a 'keep and discard' sorting mechanism"** or "a sorting mechanism that automatically discards opponent NECTAR" (M p.108–109).
- **Q&A rulings** [V: QA board; answers read from the board data saved at `downloads/qa/qa-all.txt`]:
  - **QA3.** "CONTROL of a fifth game object is a violation, and there is no MOMENTARY exception." A quick reverse is "likely not STRATEGIC but would still be in violation… VERBAL WARNING."
  - **QA4.** "There is no MOMENTARY exception for G408." Immediate ejection with no advantage is "typically not … STRATEGIC".
  - **QA23.** "a ROBOT that routinely MOMENTARILY CONTROLS more than 4 … or opponent NECTAR as part of the ROBOT design and/or strategy is likely to be perceived as … STRATEGIC".
  - **QA34 (pending).** Asks whether balls trapped under a **wide intake bar** count toward the 4. This applies directly to 12303's full-width roller.
- **What the robot needs** [I]:
  1. **Counting.** A sensor at the 4th storage slot (or a count at the intake mouth) that **stops the roller at 4**. The 4 preloads mean the robot starts full.
  2. **Colour.** A colour sensor at the intake mouth that **reverses immediately** on opponent NECTAR. Drivers must still avoid opponent NECTAR, because repeated rejections look STRATEGIC.
  3. A **narrower or gated mouth**, or a roller that only "grabs" while commanded, to reduce the "extra ball under the bar" risk while QA34 is pending.
  4. **Driver feedback** (LED or gamepad rumble) for "4 loaded".
- R5 §8 lists candidate parts, e.g. Brushland Color Rangefinder ~$25 or REV Color Sensor V3 REV-31-1557, and an IR break-beam.

### 5.4 NECTAR into FLOWERS (last 60 s)
- **Rules and geometry** [V]:
  - **Timing (G410).** NECTAR may enter the FLOWER scoring volume only in the last 60 s; MAJOR foul per NECTAR (M p.109).
  - **Entry point (G418).** Enter only through the top; remove only POLLEN, only from the bottom (M p.112).
  - **Top ring.** 4.0 in diameter at 21.5 in, with a 1.25 in backstop on the wall side (M p.72–73).
  - **Clearance.** NECTAR is 3.6 in, leaving **≈0.2 in** of radial clearance.
- **Value** [C/I: R4]. About 40% of FLOWERS still hold their staged POLLEN at match end. One own-colour NECTAR in such a FLOWER makes it owned: 2 × (3–4 POLLEN + 1) + 5 bottom-NECTAR bonus = **13–15 pts**.
- **Current robot.** No mechanism. Options [I]:
  - **(a) Low-speed lob from the turret** from 1.5–2 ft [D, `r7_calcs.py` §9]:
    - At a 75° launch it needs about 3.1–3.6 m/s and arrives steeply (≈72–73° descent).
    - Sensitivity: **+1% exit speed lands ≈0.4–0.5 in long**. Landing within about ±1 in (the backstop catches slightly long shots) needs about **±2% speed consistency** and about ±1 in robot positioning.
    - It is cheap but uncertain. Fire the first, fully spun-up shot only, and test against a replica top ring.
  - **(b) A simple "dropper"**: a servo-flipped cup or chute on the robot that reaches **≥ 22.75 in** (21.5 + 1.25 backstop) and about 2.4 in past the ring's front edge. The 29 in expansion limit allows it (R105). This is more reliable but needs a mechanism and 1–2 servos.
  - **(c) Skip FLOWER NECTAR** until HIVE cycles are reliable. R4 reports most teams treat NECTAR as a trap early in the season.

### 5.5 Pulling POLLEN from the FLOWER bottom
- **Geometry** [V: M p.72–73 Fig 9-12]:
  - Retrieval Opening **3.55 in tall × 3.57 in deep** between the bottom ring and the middle ring.
  - The bottom ring is **0.43 in thick** with a **2.79 in** hole.
  - So a 2.8 in POLLEN sits in the hole on the tile and must be **lifted over a 0.43 in lip** and pulled out under a ring about 3.98 in above the tiles.
- **Current robot** [I].
  - The front roller (boot wheels ≈3.5–3.8 in) might reach the ball if it sticks out ≥1–2 in ahead of all other front structure and nothing above it is higher than about 3.9 in at the FLOWER.
  - Today the crossbar sits just above the roller, so it may hit the middle ring first. **Test against a FLOWER replica.**
- **Proven alternative** [V: GB12].
  - The goBILDA BIOBUZZ StarterBot "pull[s] POLLEN out of the FLOWER and directly into the intake".
  - It uses **two 72 mm GripForce Gecko wheels on 'Speed' Dual Mode Servos in Compact ServoBlocks**, mounted at the front corners and run as CR servos with the intake.
  - The parts are in the same kit family the robot already uses.
- **Value** [V: M p.83]. 16 POLLEN start in the FLOWERS (4 each). Two FLOWERS are on each alliance's side (shared context). R4 reports teams **"starved of POLLEN"** without a FLOWER intake.

### 5.6 Size limits with a turret
- **Rules** [V: R102, M p.122; R105/G416, M p.123–125, 111]:
  - At the start, 18 × 18 × 18 in. Preloads may stick out.
  - During the match, the robot must stay inside an **18 × 24 × 29 in** volume at all times, **physically** constrained (software limits don't count).
  - A rotating extension is fine if the overall footprint fits at every point of its travel (Fig 12-6 shows the box re-oriented with the extension).
- **What the photos suggest** [P-L]. Front width is about 15–16 in. The turret sits at the rear on top, and the hood, flywheel and servo may overhang the ring.
- **To do.** Measure the overall height (servo on the hood, hubs on the bridge) and the footprint at every turret angle. Add **mechanical turret hard stops** if any angle breaks 18 × 24 or 18 in at the start.

### 5.7 HIVE spill (G409) and HIVE contact (G417)
- **G409** (M p.109): a robot may not catch or deflect elements released by a TIPPED HIVE until they touch something else.
  - **QA17**: "A ROBOT that drives under a HIVE and has SCORING ELEMENTS bounce and deflect off their ROBOT is in violation of G409" (VERBAL WARNING unless STRATEGIC).
  - The G409 box encourages **guards** so elements cannot get stuck on top of the robot.
  - 12303's **open horseshoe centre over a hollow turret bore is a funnel** for falling POLLEN. It risks G409 and an accidental 5th ball (G407). **Add a lid or guard** [I].
- **G417** (M p.111–112): no ramming the HIVE frame and no contact with the HIVE, even through a CONTROLLED element. Shots that miss into the outside of a CELL while trying to score are not STRATEGIC.
  - A tall robot (turret) driving near the frame base should approach slowly.
  - In AUTO, keep clear of the spill landing zone for about 3 s after the threshold ball [C: R4 §6].

### 5.8 Robustness under defense
- **What's allowed** [V].
  - Blocking or defending shots is "not specifically disallowed" (**QA2**).
  - Contact with a shot-blocking extension is allowed, but **damage or functional impairment** is not (**QA8**, G419).
  - Expect blockers and pushing.
- **Mecanum** has weak pushing traction (gm0, via R5 §2.5). The robot will be moved off its spot, so quick re-aiming (turret or chassis plus vision) matters.
- **Exposed items** [P]:
  - hubs on the top front;
  - the printed hood at the top;
  - a spinning flywheel with no guard;
  - loose wiring;
  - the battery strapped outside.
- These are the parts most likely to be hit, snagged or unplugged. See §6.

### 5.9 Vision and localization
- **Legality** [V: M p.140–141]:
  - **R702 Table 12-9**: Limelight **3A (LL_3A)** is the only supported programmable vision coprocessor. **Example 6** names the **Limelight 3G** as prohibited.
  - **R704-D**: no continuous video streaming during matches.
  - The Control Hub supports only one LL3A (AndyMark listing).
- **Specs** [V: LL1]: 93 g; USB-C; 640×480 at 90 fps; REV/goBILDA threaded mounting.
- **Tags** [V: M p.74–77]. Four 3.25 in 36h11 tags sit on the **bottom face of each CELL**, facing the floor.
- **Geometry from R5 §3.4** (derived from the manual figures):
  - The up-cell cluster centre is at about 49.7 in.
  - All tags on a HIVE face down and 30° toward the up-cell (launch) side.
  - **After a TIP they all face the other half of the field.**
  - Recommended mount: about **12 in lens height, pitched up about 35°**. The up-cell tags then stay in view from about 2 to 10 ft.
- **Where to mount it on this robot** [I]. For the first events (turret locked), mount the Limelight **on the chassis**: front-top, beside the hubs, facing forward, ≈35° up. Aim by rotating the chassis to drive `tx` to zero.
  - Simplest code; no cable through the turret.
  - Third-party guidance quoted in R5: "Bolt the Limelight to the frame, not to a moving arm or turret."
  - Make sure the hood and hubs don't block the upward view.
  - Once the turret has feedback, compute the turret angle from the camera or odometry pose and the **fixed HIVE coordinates**.
- **Odometry pods.** If they are goBILDA Swingarm pods, pair them with a **goBILDA Pinpoint** for field pose. The HIVE is fixed, so a field-relative turret aim can work even without tags in view [I]. FTC SDK 12.0 notes say the BIOBUZZ tags "move" and are not suitable for absolute field localization (via R5 §3.5).

### 5.10 Inspection and electrical

| Rule | Status | Action |
|---|---|---|
| R601: one legal 12 V NiMH main battery (M p.135) | goBILDA 3100-0012-0020 appears legal [V/P-M] | Keep the original fuse (R604). Label the batteries. |
| R603: exactly one approved main power switch, accessible, away from pinch points (M p.136) | **Not visible** | Fit a goBILDA Floodgate (3103-0005-0001) or REV switch (REV-31-1387) where staff can reach it, protected from robot contact. |
| R605: frame not a current path; only listed grounding straps (M p.136–137) | Hubs sit in plastic holders (good isolation); no strap seen | Add a REV-31-1269 or SWYFT SR-Ground-01 strap from a spare XT30 to the frame (ESD protection). |
| R606: electronics inspectable, LEDs visible (M p.137) | Hub LEDs and labels visible | Keep them visible after adding guards. |
| R609/R610: ≥18 AWG main/motor, ≥22 AWG servo; red/black colour coding (M p.138–139) | Silicone red/black leads look OK | Keep labels. Strain-relieve XT30 junctions. |
| R401–R403: 2 robot signs, ≥6.5 × 2.5 in, ≥90° apart, alliance colour plus 2.25 in white numerals (M p.129–130) | Not visible | Plan two mounting faces now. The turret top and front are crowded. |
| R202-I: no unnecessary entanglement risk (M p.126) | Loose wire loops | Tidy the wiring (§6 F7). |
| R201-E: components must be secured (M p.126) | Battery held by one Velcro strap | Use a proper cradle and two straps. |

### 5.11 Readiness for AUTO
- **Field setup** [V: EFSG §12, p.26–27; M p.84]. Every CELL is calibrated to TIP at **8 POLLEN** or **3 NECTAR + 3 POLLEN**, and to NOT tip at 7 POLLEN or 3 NECTAR + 2 POLLEN. Each alliance's starting up-cell holds 3 own-colour NECTAR.
- **So TIP 1 needs only 3 POLLEN.** One robot's 4 preloads can do it in AUTO for 20 points, plus LEAVE 3 and PARK 5 [V: M p.91].
- **Requirements for 12303 to do this** [I]:
  - an indexer that holds and feeds 4 preloads;
  - a flywheel at speed before feeding;
  - an aim (fixed start pose plus a locked turret is enough);
  - a drive to the LOADING ZONE that ends **not touching the wall**. QA21: "if the ROBOT is touching a wall at the end of AUTO the LEAVE achievement is not awarded". QA28: a partner may push or pull the robot off the wall.

---

## 6. Likely failure points (ranked)

| ID | Failure point | Evidence | Mechanism | Likelihood / impact | Mitigation (see §7) |
|---|---|---|---|---|---|
| F1 | **Servo-driven intake is slow and stalls** | Coach: Dual Mode Servo for the intake; servo → gear pair → full-width roller [P-M] | Surface speed, 3.5 in roller [D]: Torque 25-2 at 5 V gives 0.6–0.8 ft/s; Speed 25-3 1.4–1.7 ft/s; Super Speed 25-4 2.9–3.5 ft/s. The drive does 6.4 ft/s. Mechanical power is about 2–3 W against about 22.6 W for a 5203 motor [D]. A full-width roller squeezing a 3.6 in NECTAR needs torque. Stalls also brown out the paired servo port (2 A per pair, 5 A per hub [V: REV1]). For comparison, goBILDA's own StarterBot intake is also slow (a 50.9:1, 117 rpm Yellow Jacket [V: GB12]), but it is motor-powered, with several times a servo's torque and about 10× its power [D, from the R502 formula vs motor specs]. | High / High | R7: DC motor intake. Interim: 25-4 or 25-3 at 6 V on a Servo Hub or Injector, and creep into balls. |
| F2 | **Turret: limited range, no feedback, backlash, cable wrap** | Grey servo + ServoBlock + 60T pinion on a large ring [P-M]; no encoder or switch visible | A standard 300° servo through ≈4.5–5.5:1 gives **≈55–67°** [D]. Hits on the hood back-drive the ring, which loads the servo gears and pulls stall current. With no absolute reference, the turret angle is unknown after a hit. Hood servo and flywheel cables hang next to the ring/pinion mesh [P]. | High / Med–High | R2 (lock now). R9: motor + home switch, 5-turn servo, or CR servo + TBE. Hard stops; service loop; cable guard over the mesh. |
| F3 | **Shot-to-shot speed droop and NECTAR/POLLEN mismatch** | One 72 mm, 55 g wheel [V: GB5] | 18% / 33% speed drop per shot [D]. The 4th shot drifts off; community reports "4th-shot droop" and "first shot OK then misses" [C: R4 §6] | High / High | R3 velocity gating; R8 add inertia. |
| F4 | **Bounce-outs from backspin** | Single wheel + stationary hood | 44% bounce-out measured by one team with a fixed hood [C: R4 §6] | Med–High / High | R8: test early; counter-roller or compliant hood. |
| F5 | **Hood can't handle two sizes; hood not built** | Horseshoe + foam pad + servo; no hood surface over the wheel [P-M] | See the compression table in §5.2 | High (if shooting NECTAR) / Med | R8: compliant or servo-preset hood; or POLLEN-only shooter. |
| F6 | **Flywheel drivetrain at 6000 rpm on printed parts** | Motor in a printed clamp with a cloth-tape shim; probable miter drive; printed bearing arms [P-M/L] | A printed clamp lets the motor creep and vibrate. A right-angle gear mesh at 6000 rpm needs rigid alignment; flex causes noise, wear and tooth skipping. Printed bearing seats can loosen. | Med / High (shooter down) | R12: bolt the motor through its face holes with a goBILDA motor mount. Prefer **direct drive or a 1:1 belt** to the wheel shaft. Print structural parts in PETG/ABS/nylon with ≥4 perimeters and 40–60% infill. |
| F7 | **Wiring exposure** | Main power loops; an inline XT30 on the side plate **just above a mecanum wheel**; servo leads hanging by the ring gear [P-H] | Snagged by own wheels or turret mesh, or by opponents (R202-I). XT30s can work loose and brown out the robot. | High / High (robot dead) | R5: route inside the frame, tie every ~3 in, clip or retain XT30 junctions, service loop for the turret, keep ≥1 in from wheels. |
| F8 | **Hub placement** | Both hubs stand on the top-front bridge above the intake path [P-H] | Collisions, falling balls (G409 zone), static from balls passing nearby (ESD guidance via R5), and long wires to the rear turret. Raises the CG slightly. | Med / High | R13: add guards or a polycarbonate shield and a grounding strap. Later, move the hubs lower (side walls or under the turret). |
| F9 | **Battery retention and placement** | One Velcro strap, outside the frame [P-H] | Can come off in a hit (R201-E). Exposed XT30. Somewhat high. The pack is designed to nest inside 1120 U-Channel [V: GB10]. | Med / High | R5: goBILDA Battery Mount (1209-0001-0001, with strap) in a low 1120 U-Channel cradle. Two straps; easy swap access. |
| F10 | **CG with a tall turret** | Turret, hood, motor and servo sit high at the rear | Estimated robot ≈ 8.5 kg (18.8 lb), CG ≈ 6 in. A 6–7 in half-track gives ≈44–48° static tip angle and about 1 g lateral to tip [D, rough]. Fine on flat tiles, but **watch for climbing the HIVE frame's base rails** and turret slew while braking. Moving the battery low lowers the CG by only ≈0.3 in. | Low–Med / Med | Keep the heavy flywheel mass low on the turret. Avoid riding up on frame rails. Measure the real weight. |
| F11 | **Servo power brownouts** | Several servos on 5 V hub ports | 2 A per pair, 5 A per hub [V: REV1]; servo stall 2.0–2.5 A [V: GB2] | Med / Med | Servo Hub or 6 V Injector; spread loads across pairs. |
| F12 | **G407/G408 violations from a wide intake** | Full-width roller, no sensors | QA3/4/23: no momentary exception; QA34 pending | High / Med (fouls, cards) | R4 sensors and auto-stop; narrower mouth; driver discipline. |
| F13 | **G409 catches** | Open horseshoe centre over a hollow bore; open top | QA17: deflection counts | Med / Med | R6 lid or guard. |
| F14 | **Limelight illegal or poorly placed** | Model unknown | Only the 3A is legal (R702). Tags face down and away after a TIP. | Unknown / High (inspection) | R1 verify; mount per §5.9. |

---

## 7. Prioritized recommendations

Tag key:
- **quick-win**: a few hours, no new parts or ≤$30, before the next meet.
- **medium**: 1–3 build sessions or a modest purchase.
- **big-change**: a subsystem redesign; target the League Tournament on 12/4–5.

### 7.1 Before the next meet

**R1. Inspection blockers.** [quick-win]
- Confirm the camera is a **Limelight 3A** (USB-C only, no Ethernet jack; R5 §3.1). If it is a 3G or 4, it cannot be used (R702).
- Fit the **main power switch** (goBILDA Floodgate 3103-0005-0001 or REV-31-1387; R603).
- Add a **grounding strap** (REV-31-1269 ≈ $5.50, or SWYFT SR-Ground-01; R605).
- Mount **two robot signs** on faces ≥90° apart (R401–R403).

**R2. Lock the turret at 0°** with a bolt or pin through the ring and frame. [quick-win]
- Aim with the mecanum chassis: rotate until Limelight `tx` ≈ 0, or use pre-measured start poses in AUTO.
- This removes F2 (range, feedback, backlash, cables) for the first events. CyberCoyotes keeps a "turret locked, 2–3 preset shots, no vision" fallback TeleOp [C: R4 §3.5].

**R3. Shooter software.** [quick-win, software]
- Run the flywheel with `DcMotorEx.setVelocity()` in RUN_USING_ENCODER and PIDF.
  - Starting point: goBILDA StarterBot (40, 0, 0, 12.5).
  - Or REV's method: F = 32767 / maxV.
- **Feed only when velocity is within a band** of the target. The StarterBot uses MIN 1200 for a 1250 ticks/s target [V: GB12 code].
  - 28 counts/rev at the motor [V: GB1], so 1250 ticks/s ≈ 2,680 rpm.
  - The 72 mm wheel needs about 3,500–4,700 rpm (≈1,650–2,200 ticks/s) for 3–10 ft [D].
- Build a **distance → velocity table** and feed the 4 balls about 0.3 s apart.
- Set servos to the full 500–2500 µs range (`ServoImplEx.setPwmRange`); see R5 §5.3.
- **Shoot POLLEN only** until the hood handles NECTAR.

**R4. Ball count and colour.** [quick-win → medium]
- Mount colour/distance sensors **at the intake mouth** (opponent-NECTAR → reverse immediately) and **at storage slot 4** (full → stop the intake and signal the driver).
- Optionally add an **IR break-beam at the shooter feed** to count shots and detect jams.
- Candidates and wiring are in R5 §8 and §9.3: Brushland Color Rangefinder ~$25 each, REV Color Sensor V3 REV-31-1557 ~$20.75, Adafruit 2167 ~$2.95.
- Use **gamepad rumble or LEDs** for "4 loaded" and "flywheel ready".

**R5. Wiring and battery.** [quick-win]
- Re-route the main power leads **inside** the frame, away from the wheel faces (photo 3).
- Tie down every ~3 in. Retain XT30 junctions with a clip, tape or a printed sleeve.
- Give the turret a **service loop**, and keep all wires ≥1 in from the ring/pinion mesh and the mecanum rollers.
- Nest the battery low in a **1120 U-Channel cradle** with the goBILDA Battery Mount 1209-0001-0001 (in the 2026-27 Starter Kit) plus a second strap. Keep it reachable for swaps.

**R6. Guards.** [quick-win]
- Put a light lid (polycarbonate or printed) over the **horseshoe opening and flywheel**. Leave only the ball exit open.
- Add top-side deflectors so POLLEN cannot rest on the robot (G409 box, QA17).
- This also guards fingers from a 6000 rpm wheel (R202).

### 7.2 Next 2–3 weeks

**R7. Intake on a DC motor.** [medium]
- Use a **goBILDA 5203 Yellow Jacket**, e.g. **312 rpm (5203-2402-0019)**, which is in the Starter Kit.
  - With the existing ≈1.2:1 gear step-up and 96 mm Boot-Wheels: **≈6.2 ft/s**, about the drive's top speed [D].
  - **435 rpm** (≈7.2 ft/s direct) or **1150 rpm** with 72 mm wheels (≈14 ft/s) if the team wants faster pickup.
- R5 §5.4 suggests 1150 rpm for 2.5–3.5 in rollers.
- gm0: "dedicate at least one motor to [the] intake" (via R5).
- A motor also gives `getCurrent()` for jam detection.
- Reuse the servo for the indexer or gate. Budget: motor 6 of 8.

**R8. Make the shooter consistent.** [medium]
- **(a) Inertia.** Reach I ≈ 2.5–3.5×10⁻⁴ kg·m²:
  - a 200–300 g steel or brass disc on the flywheel shaft; or
  - two 96 mm Hogbacks (3626-0014-0096, 82 g each; 5 in the Starter Kit), which also lowers the rpm needed by 25%. This needs a reprinted hood.
- **(b) Hood.** Build the **hood surface** for a ~60–65° exit. Make it **compliant** (sprung or foam-backed, ~0.8 in travel) or **two-position** using the grey servo with presets chosen by the colour sensor.
- **(c) Bounce-outs.** Test them on a CELL replica. If they are high (≥20%), add a **hood roller or counter-roller** for low spin.
- **(d) Drive.** Replace the cloth-tape clamp with a **bolted goBILDA motor mount**. Prefer **direct drive** (coupler) or a **1:1 timing belt** over a right-angle gear at 6000 rpm.

**R9. Turret with feedback.** [medium → big-change] Pick one:
- **(i) goBILDA 5203 312 rpm** on the ring through a new pinion. At 5:1: 0.13° per count and ~60 rpm turret [V/D: R5 §4.3]. Add a **REV Magnetic Limit Switch (REV-31-1462)** to home it. Motor 7 of 8.
- **(ii) goBILDA 5-turn Dual Mode Servo** (2000-0025-0502/-0503, 1800°), giving ≈330–400° of turret travel at 4.5–5.5:1 [D]. Needs the full 500–2500 µs range.
- **(iii) CR servo** plus a **REV Through Bore Encoder V2** (REV-11-3174, 8192 counts/rev) on the turret axis.

Whichever is chosen:
- add **mechanical hard stops** at the limits of the cable travel (R105 is a physical limit too);
- add a cable track or service loop;
- move the Limelight onto the turret only if range is ≥ ±90° and a home switch exists.

**R10. FLOWER POLLEN extractor.** [medium]
- Copy the StarterBot: **two 72 mm GripForce Gecko wheels** (3613-0014-0072) on **Speed Dual Mode Servos** in **Compact ServoBlocks** at the front corners, run as CR servos with the intake [V: GB12 step 37].
- Check that the extractor or roller reaches under the 3.98 in middle ring and lifts the ball over the 0.43 in lip (M p.72–73).

**R11. Servo power.** [medium] Use a **REV Servo Hub (REV-11-1855, 6 V, ~$90)** or a **goBILDA 6V Servo Power Injector (3125-0001-0001, ~$70)** for intake, turret and indexer servos (R505 legal; R5 §5.3).

**R12. Printed parts.** [medium]
- Reprint load-bearing parts (hood, flywheel arms, motor clamp, hub holders) in **PETG/ABS/ASA/nylon**, ≥4 perimeters, 40–60% infill.
- Use **captured or press-fit flanged bearings** at the flywheel.
- Check bolt torque after every event.

### 7.3 Before the League Tournament (12/4–5)

**R13. Repackage the electronics.** [big-change]
- Move the hubs lower (side walls or under the turret), with short motor leads and shielded USB to the Limelight.
- Free the space behind the intake for the **indexer tower**.
- Shield the electronics from the ball path (ESD; R5 §8 suggests ferrites and a grounding strap).

**R14. Indexer to the turret centre.** [big-change]
- Build a compact single-file path from the intake to the ring bore that **holds exactly 4** (11.2–14.4 in [D]) and handles 2.8–3.6 in balls:
  - compliant belts or flex wheels on one side;
  - a feed gate under the flywheel;
  - sensors at the mouth, slot 4 and the feed.
- The coach's "internal spinners" (CR servos) are fine here: low power, short distances [I; R5 §5.4].

**R15. NECTAR plan.** [big-change, optional]
- After POLLEN tips are reliable, choose between:
  - (a) a turret lob into FLOWERS (test the ±1 in landing accuracy at about 2 ft); or
  - (b) a servo **dropper** reaching ≥22.75 in and about 2.4 in beyond the ring's front edge, for 13–15 pt FLOWERS in the last 60 s.
- Shooting NECTAR into the HIVE only makes sense with a hood that handles 3.6 in (R8b).

---

## 8. What the photos can't tell us: questions for the team

**Identity and legality**
1. Which **Limelight** model is it (3A, 3G or 4)? Only the 3A is legal (R702).
2. Is the 6000 rpm motor driving the **flywheel** (as the photos show) or meant to **rotate the turret** (the coach's words)? Is it a native 5203-2402-0001 or a 19.2:1 converted with the 11T coupler?
3. Which **Dual Mode Servo variants** are used where (25-2 Torque, 25-3 Speed, 25-4 Super Speed, 5-turn)? Which are in continuous mode? Are servos on hub ports (5 V) or a Servo Hub/Injector (6 V), and which port pairs?
4. Where will the **main power switch** go? Is there a grounding strap?

**Turret**

5. **Ring and pinion details**:
   - ring tooth count and module, vendor and part number;
   - bearing type and inner bore diameter;
   - pinion tooth count;
   - any hard stops, encoder or home switch;
   - intended rotation range;
   - cable plan for the flywheel motor, encoder, hood servo and camera.

**Flywheel and hood**

6. How is the flywheel driven: miter gears, belt or direct? Ratio? Is the small toothed part beside the wheel a miter gear?
7. What is the intended **ball-contact surface and launch angle**? What do the **grey servo** and **black foam block** at the far end of the horseshoe do?
8. What **hood gap** is planned, and for which ball size? Do they plan to shoot NECTAR?
9. What material and print settings were used for the hood and hub holders? Are there real bearings in the printed arms? What is the red cloth tape on the motor for?

**Intake and indexer**

10. Boot-Wheel size (72 or 96 mm), roller-to-floor height, and the tooth counts of the two intake gears. Is the hinged crossbar meant to **float** or to **deploy**, and is there a hard stop?
11. What **intake speed** is measured (drive into a ball at 1, 2 and 4 ft/s: does it pick up)? Does it stall on NECTAR?
12. What is the planned **"internal spinners"** path from the front intake up into the turret centre? How many servos, and what capacity (it must be 4)?
13. Any plan for **ball counting and colour sensing** (G407/G408)?

**Sensors and software**

14. Are the small gold or yellow wheels **goBILDA Swingarm odometry pods**? How many? Is there a **goBILDA Pinpoint**?
15. Java or Blocks? Experience with velocity PIDF, Limelight, Pinpoint and path following?

**Size, weight, power**

16. Current overall **L × W × H** in the starting configuration, and the footprint with the turret at its extremes. Current **weight**.
17. How many batteries, and how old? Battery mount plan?

**Logistics**

18. Do they own the **goBILDA FTC Starter Kit 2026-27**? If so: spare 312 and 117 rpm motors, 96 mm Hogbacks, Gecko wheels, ServoBlocks, battery mount.
19. Which **meet dates** are they entering first? This decides which fixes are realistic.
20. Is there CAD (Onshape)? Even rough CAD would settle questions 5–12.

---

## 9. Implications for strategy (for downstream agents)

1. **Design-to-strategy fit.**
   - The robot is a **HIVE bot**: turret shooter plus floor intake.
   - The highest-value, lowest-risk plan is **POLLEN-only HIVE TIPs**.
   - AUTO: 4 preloads, TIP 1 needs 3 POLLEN, then LEAVE and PARK, for 28 robot points in AUTO [V: EFSG §12.3; M p.91].
   - TeleOp: about one 4-ball load every ~15 s toward 8-POLLEN TIPs, plus PARK at the end (R4 §2.2 and §12).
2. **The AUTO scoring play depends on three unbuilt or weak items**: the indexer (holds and feeds 4), a consistent first volley (velocity-gated feeding), and aiming. A **locked turret plus a fixed start pose** is enough for TIP 1.
3. **Expected cycle limits today.** A servo intake forces slow pickup (creeping into balls). Plan TeleOp routes around **gardens, the spill area just after it settles, and FLOWER bottoms** (with an extractor) rather than chasing scattered balls. Revisit once the intake has a motor.
4. **Turret value comes later.** With ≈55–67° of servo travel and no feedback, the turret adds risk now. Lock it, aim with the chassis, and unlock after R9. The up CELL switches ends after every TIP, so the robot must relocate anyway (R4 §12.5).
5. **NECTAR.** Treat FLOWER NECTAR (13–15 pt FLOWERS in the last 60 s) as a **stretch goal**, gated on R15. Shooting NECTAR into the HIVE needs R8b. Meanwhile the human player still enters NECTAR after own-HIVE TIPs, and freely in the last 60 s (G426/G427).
6. **Fouls to design out**: G407/G408 (sensors and stop; QA34 pending on wide intakes), G409 (lid; leave the spill zone), G417 (no HIVE contact), G410 (no NECTAR in FLOWERS before 1:00). Each MAJOR gives the opponent 20 pts (M p.91 context; Table 10-2).
7. **Defense.** Expect blockers (legal per QA2/QA8). A steep (60–65°) shot from 5–8 ft clears most blockers. Mecanum loses pushing fights. Protect the hubs and wiring.

---

## 10. Sources

**FIRST primary**
- [M] FIRST Tech Challenge Competition Manual **TU05 (2026-10-15)**, local `…/scratchpad/manual/manual.pdf`.
  - HIVE/CELL p.69–72 (Fig 9-8 to 9-11); FLOWER p.72–73 (Fig 9-12); SCORING ELEMENTS p.74; AprilTags p.74–77; staging p.83–84; scoring p.86–91 (Table 10-2/10-3).
  - G403–G418 p.106–112; R101–R105 p.121–125 (Fig 12-1 to 12-6); R201–R202 p.125–126; R301–R303 p.127–128; R401–R403 p.129–131.
  - R501–R505 p.132–134 (R502 limits ≤8 W and ≤4 A @6 V, verified visually p.133); R601–R613 p.135–140; R701–R704 p.140–141; Glossary p.171–175 (CONTROL, LAUNCH, MOMENTARY).
- [EFSG] 2026-27 Event Field Setup Guide **V1.0**, §12 "Hive Calibration", §12.3 table (p.26–27): https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide. Local text: `…/scratchpad/work/eventfieldguide.txt`.
- [QA#] Official Game Q&A board, BIOBUZZ: https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N (QA2, QA3, QA4, QA8, QA10, QA17, QA23 answered; QA29, QA30, QA34, QA35, QA37 pending as of 2026-10-20). Answer text from the saved board data `…/scratchpad/downloads/qa/qa-all.txt`.

**Manufacturers**
- [SW1] SWYFT Drive V2 (SR-DRIVE-02 / SR-DRIVE-FRAME-02; Spike motor SR-MOTOR-DC-01; 12.7:1; 86 mm 40A; 575 g; 6.4 ft/s; robots up to 40 lb; steel-plate and aluminium-channel frame kit): https://swyftrobotics.com/kits/swyft-drive-v2
- [SW2] SWYFT Intake Wheels: https://swyftrobotics.com/motion/swyft-intake-wheels
- [SW3] SWYFT Servos: https://swyftrobotics.com/electrical/swyft-servos
- [SW4] SWYFT Linear Odometry Module: https://swyftrobotics.com/kits/swyft-linear-odometry-module
- [GB1] goBILDA 5203-2402-0001 Yellow Jacket 1:1, 6000 rpm (9.2 A stall, 1.47 kg·cm, 28 PPR, 391 g): https://gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder
- [GB2] goBILDA 2000-0025-0002 Dual Mode Servo (Torque): https://gobilda.com/2000-series-dual-mode-servo-25-2 (product image showing the grey case: downloaded to `…/downloads/r7-gobilda-servo/servo.jpg`)
- [GB3] goBILDA 2000-0025-0003 (Speed: 90/115/145 rpm, 7.9/9.3/10.8 kg·cm at 4.8/6/7.4 V; CR 1000–2000 µs): https://www.gobilda.com/2000-series-dual-mode-servo-25-3-speed/
- [GB4] goBILDA 2000-0025-0004 (Super Speed: 180/230/290 rpm): https://www.gobilda.com/2000-series-dual-mode-servo-25-4-super-speed/
- [GB5] goBILDA Hogback Traction Wheel 72 mm 50A (3626-0014-0072, 55 g): https://www.gobilda.com/hogback-traction-wheel-72mm-diameter-50a-durometer/
- [GB6] goBILDA Hogback 96 mm 50A (3626-0014-0096, 82 g): https://core-electronics.com.au/gobilda-hogback-traction-wheel-96mm-50a-durometer.html
- [GB7] goBILDA Compact ServoBlock 43 mm (3217-0001-2501, 44 g): https://www.gobilda.com/compact-servoblock-43mm-width-for-standard-size-h25t-spline-servo-hub-shaft/
- [GB8] goBILDA 2302 Series 60T MOD 0.8 hub-mount gear (14 mm bore): https://www.gobilda.com/2302-series-aluminum-mod-0-8-hub-mount-gear-14mm-bore-60-tooth
- [GB9] goBILDA 3615 Series Boot-Wheel (72 mm 14 g; 96 mm 37–38 g; 30A; toe/heel orientation): https://www.gobilda.com/3615-series-boot-wheel-8mm-rex-bore-72mm-diameter-30a-durometer , https://www.gobilda.com/3615-series-boot-wheel-8mm-rex-bore-96mm-diameter-30a-durometer/ ; Intake Wheel Starter Pack: https://www.gobilda.com/intake-wheel-starter-pack-8mm-rex-bore/
- [GB10] goBILDA 12V NiMH Nested Battery 3000 mAh (3100-0012-0020; 597 g; nests in 1120 U-Channel; 20 A fuse): https://www.gobilda.com/12v-nimh-nested-battery-3000mah-mh-fc-xt30-connector/ ; manual https://www.gobilda.com/content/user_manuals/3100-0012-0020_user-manual.pdf
- [GB11] goBILDA Swingarm Odometry Pod 48 mm (3110-0001-0001, 2000 CPR, 121 g): https://www.gobilda.com/swingarm-odometry-pod-48mm-wheel/
- [GB12] goBILDA FTC StarterBot for BIOBUZZ: guide https://www.gobilda.com/ftc-starter-bot-resource-guide-2026-2027-season/ ; assembly instructions 3200-2627-0003 (local `…/downloads/gobilda-starterbot/assembly.txt`: kit list p.2–3, STEP 37 Gecko servos, STEP 44 1:1 conversion, STEP 57 Hogback, STEP 58 windmill servo) https://www.gobilda.com/content/user_manuals/3200-2627-0003_assembly-instructions.min.pdf ; example code `BioBuzzStarterBotTeleop.java` (LAUNCHER_TARGET_VELOCITY 1250, MIN 1200, PIDF 40/0/0/12.5)
- [REV1] REV Control Hub specifications (servo 5 V; 2 A per port pair; 5 A total; 209 g; 143×103×29.5 mm): https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics
- [LL1] Limelight 3A: https://www.servocity.com/limelight-3a-smart-camera (93 g, USB-C, 640×480 @ 90 fps, REV/goBILDA threaded mounting); https://ftc.andymark.com/products/limelight-3a ("The REV Control Hub can only support a single LL3A at this time")

**Sibling research (secondary; their primary sources are listed inside them)**
- R1 rules: `…/scratchpad/research/R1-rules.md`
- R4 community and meta: `…/scratchpad/research/R4-community-meta.md` (tip calibration, bounce-out data, StarterBot table, Michigan schedule)
- R5 hardware: `…/scratchpad/research/R5-hardware.md` (Limelight geometry, servo and motor tables, port map, recommended parts with prices)

**Local tools**
- Crop script: `scratch/scripts/r7_crop.py`
- Calculations: `scratch/scripts/r7_calcs.py`
  - §1 intake speeds; §2 turret travel; §3 flywheel energy; §4 exit speeds; §5 compression; §6 CG; §7 storage length; §8 flywheel options; §9 FLOWER lob sensitivity.
- Crops: `scratch/downloads/photos/crops/`
