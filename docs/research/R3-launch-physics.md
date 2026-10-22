# R3: Launch physics, shooter sizing, HIVE tip and FLOWER stack math

FTC team 12303, BIOBUZZ (2026-27). Prepared 2026-10-20 by research agent R3.

**Primary sources:** Competition Manual TU05 (2026-10-15), cited as "M p.N". BIOBUZZ Event Field Setup Guide V1.0 (2026-09-12), cited as "EFSG p.N", from https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide. Vendor pages and the literature are cited inline.

**Method:** Every number below was computed with the pure-Python scripts in `scratchpad/scripts/`, which are listed in Appendix A. Facts are tagged **[V]** (verified in a primary source), **[D]** (derived from verified data by geometry or arithmetic), **[E]** (estimate or model output) or **[A]** (assumption).

---

## 0. Bottom line

1. **The up-facing CELL can only be scored from one half of the field, and that half flips after every TIP.** [D]
   - The CELL opening is the outer end of a tilted box. It faces the end wall nearest to it and leans back 30°, so its outward normal points 30° above horizontal.
   - The lip (bottom edge of the opening) is 53.5 in above the tiles and 52.7 in from the wall it faces. The peak is 65.6 in high and 7 in farther in.
   - Red starts with the audience-side CELL up, so red shoots from the audience half. Blue starts with the far ("Scoring") side up and shoots from the far half (M p.83, EFSG p.23).
   - After each TIP the other CELL comes up, facing the opposite wall.
2. **Use moderate arcs that arrive near the top of the flight.** [E]
   - Ball exit angle about 60-65°, exit speed about 5.3-6.7 m/s (17-22 ft/s), arriving at only 2-4 m/s.
   - **A fixed hood giving a ~64-65° actual ball exit angle, plus a distance-to-RPM lookup table, covers lip distances of about 27-90 in.** That is the robot's launcher about 46-110 in from the HIVE centre line.
   - Over that band the speed window is ±3-5.5% and the angle window is ±2-10°.
   - Shots from closer than about 27 in to the lip need at least 70°.
   - The fast "line-drive" family (about 12 m/s at 30-50°) has huge speed tolerance but only ±0.2-1.8° of angle tolerance. It is also beyond this motor and wheel (about 8,000 rpm needed on a 72 mm wheel). Do not use it.
3. **Fixed angle plus variable RPM is enough to compete.** [E]
   - With realistic shot-to-shot errors (σ speed 3%, σ angle 1.5°), the clean-entry probability over lip distances of 36-72 in is about 80-82% for a fixed 60-65° hood, against 88% for a fully adjustable hood.
   - Halving those errors raises the fixed hood to 94-96%. **Consistency is worth more than an adjustable hood.**
   - A two-position hood (~62°/~72°) adds the close-in zone directly in front of the CELL. Treat it as a phase-2 upgrade.
4. **POLLEN and NECTAR fly identically.** [D]
   - Their mass per frontal area is 0.628 vs 0.629 g/cm², so drag deceleration is the same.
   - The required exit speed matches to within 0.2%. NECTAR's window is about 10% narrower because the ball is bigger.
   - Any RPM difference comes from the launcher: grip, inertia ratio and compression.
   - **The 0.8 in diameter difference is the real problem. One fixed hood gap cannot launch both.**
5. **Shooter speed ratio k and wheel sizing.** [V/E]
   - For one wheel against a fixed hood, the exit speed is at most 0.5 × wheel surface speed. Teams report 0.2-0.45. **The briefed k range of 0.4-0.6 is too high at the top end. Plan on 0.35-0.45 and measure it.**
   - The required surface speed is 12-19 m/s.
   - The team's 72 mm wheel, driven directly by the 6,000 rpm goBILDA 5203, needs about 3,500-4,400 rpm at k = 0.40. That is 59-74% of free speed at 12 V (61-77% at 11.5 V): adequate if k ≥ 0.4 and shots stay within about 72 in.
   - **A 96-104 mm wheel driven directly needs 2,400-3,300 rpm (40-57% of free speed, near peak power). It has far more margin. Do not gear down.**
6. **Each shot slows the flywheel.** [E]
   - Drop per shot (ideal hooded model): about 19% for POLLEN and 28% for NECTAR on the bare 72 mm wheel. Adding one goBILDA 82 mm steel flywheel (1651 g·cm²) cuts this to 6% and 9%.
   - Full-power recovery takes 0.08-0.2 s at 12 V, about 10-25% longer at 11.5 V.
   - **Fire 4 balls in about 0.5-0.75 s (first to last release) by gating the feeder on RPM error ≤ 1.5% (roughly 0.15-0.25 s between balls).**
7. **HIVE TIP thresholds (official calibration).** [V]
   - An empty up-CELL tips with 8 POLLEN and must not tip with 7, even when the 7th is tossed in.
   - The starting CELL (holding 3 NECTAR) tips on the **3rd** POLLEN. 1 NECTAR is about 1.65 POLLEN.
   - The tip torque is about 0.43-0.49 N·m about the pivot [D].
   - The momentum of a launched ball is worth at most about 0.25 POLLEN, and for most entry angles it pushes the HIVE against its stop. **Plan tips by count, not by impact** [E].
8. **FLOWER capacity and points.** [D/E]
   - The scoring volume is about 16 in tall. A full column holds about 7-8 counted elements.
   - Manual example D shows 7 POLLEN + 1 NECTAR = 8 elements, worth 16 pts plus the 5 pt bottom bonus = 21 pts.
   - Pulling a POLLEN from the bottom drops the stack about 2.3-2.8 in. It can pull a stranded NECTAR back into the volume, which flips ownership.

---

## 1. Verified inputs

### 1.1 Scoring elements

| Quantity | POLLEN | NECTAR | Source |
|---|---|---|---|
| Diameter (nominal) | 2.8 in (7.11 cm) | 3.6 in (9.14 cm) | M p.74; AndyMark BIOBUZZ Scoring Elements page (am-5851_yellow / am-5852): "Diameter: 2.8 in., 3.6 in." [V] |
| Mass | 0.055 lb = 24.9 g | 0.091 lb = 41.3 g | AndyMark page: "Weight: 0.055 lbs., 0.091 lbs" (the page lists the pair without labels; mapped by size) [V] |
| Material | Gopher ResisDent perforated polyethylene | same | M p.74 [V] |
| Size variation | "not perfectly spherical and may vary in size" | same | M p.74 [V] |
| Mass / frontal area | 0.628 g/cm² | 0.629 g/cm² | [D] identical, so the same drag deceleration |
| Mass ratio N/P | 1.655 | | [D] |
| Reynolds number at 6 m/s | 2.8×10⁴ | 3.6×10⁴ | [D] subcritical |

**Analog ball:** a USA Pickleball-approved ball is 2.87-2.97 in and 22.1-26.5 g, with 26-40 holes. It must bounce 30-34 in (to the top of the ball) when dropped from 78 in onto granite. That implies a coefficient of restitution of about 0.6-0.66 on a rigid surface (USA Pickleball Equipment Standards Manual 2021, §2.D.3-2.D.5, extracted from https://www.pickleball.ph/uploads/1/2/5/9/125919777/-_official_equipment_standards_manual.pdf) [V]. The POLLEN is close in size and mass.

### 1.2 Field and CELL geometry

All items are [V] unless marked.

- **Field:** about 144 × 144 in inside the walls; 24 in tiles (M p.64). Red ALLIANCE AREA is on the left as seen from the audience (M p.68).
- **HIVE position:** the HIVE structure sits at field centre on tiles C3-D4 (M p.65, 67, 69).
- **Frame:** 49.46 in wide × 38.95 in deep (M p.69-70).
- **Pivot:** 43.95 in above the tiles (M p.69-70).
- **Arm tilt:** 30° from horizontal in either stable state (Fig 9-10, M p.71).
- **CELLs:** 12.04 in deep, inner faces 18.84 in apart, 42.91 in overall. The two HIVEs are 25.5 in apart centre to centre (Figs 9-9 and 9-10, M p.71).
- **Opening:** a pentagon about 20 in wide (outer) × 14 in tall at the peak, 7.61 in tall at the sides (Fig 9-11, M p.72).
- **Heights:** bottom of opening 53.5 in, top of opening 65.6 in, bottom of HIVE 30.6 in (Fig 9-10, M p.71).
- **CELL contents:** NECTAR rests against the "Hive Back Skin", the closed inner end (EFSG p.23; M p.84 says "contacting the back wall of the CELL").
- **Ballast:** each CELL has a ballast-washer screw "at the end of each Cell nearest its opening" (EFSG p.26).
- **AprilTag cluster:** four 36h11 tags, 3.25 in square, "on the bottom of a CELL facing downward towards the TILES with its bottom edge oriented towards the center of the FIELD" (M p.74-75).
  - Tag centre line: 9.938 − 2.75 ≈ 7.2 in inward from the CELL front (Fig 9-15).
  - IDs: red far CELL 30-33, red audience CELL 34-37, blue audience CELL 38-41, blue far CELL 42-45 (M p.76, read from the page image).

### 1.3 FLOWER geometry (M p.72-73, Fig 9-12) [V]

- **Top opening:** 4.0 in diameter, 21.5 in above the tiles, with a 1.25 in backstop.
- **Bottom ring:** 0.43 in thick, 2.79 in ID.
- **Retrieval Opening:** 3.55 in tall and 3.57 in deep. Only POLLEN may be removed from the bottom (G418, M p.112).
- **Scoring volume:** "between the top ring and the middle ring". An element scores if it is "at least partially within" (M p.88).
- **Measured from the 400 dpi render [D]:** the middle-ring plate's underside is at 3.98 in (0.43 + 3.55); the plate top is about 4.3 in; the pipe-socket tops are about 5.3 in; the top ring is about 1.2 in thick.

### 1.4 Hardware

| Item | Data | Source |
|---|---|---|
| goBILDA 5203 1:1 motor | 12 V; 6,000 rpm free; 0.25 A free; 9.2 A stall; stall torque 1.47 kg·cm (20.45 oz-in = 0.144 N·m); 391 g; encoder 28 PPR at the output shaft | https://www.gobilda.com/5203-series-yellow-jacket-motor-1-1-ratio-24mm-length-8mm-rex-shaft-6000-rpm-3-3-5v-encoder/ [V] |
| Derived motor limits | Peak mechanical power 22.6 W at 12 V; 24.6 W at 12.5 V; 20.8 W at 11.5 V. Free speed 6,250 / 6,000 / 5,750 rpm at 12.5 / 12 / 11.5 V | [D], linear DC motor model |
| Team flywheel | The rim in photo PXL_20261020_002925221 reads "Ø72mm - 50A". That matches the **goBILDA Hogback Traction Wheel, 72 mm, 50A, 55 g** | https://www.gobilda.com/hogback-traction-wheel-72mm-diameter-50a-durometer/ [V specs; ID from photo = E] |
| Hogback 96 mm 50A | 82 g | https://www.gobilda.com/hogback-traction-wheel-96mm-diameter-50a-durometer/ [V] |
| goBILDA steel flywheel | 82 mm, 152 g, MOI 1651 g·cm², "Built for our 96mm Diameter Hogback Traction Wheel" | https://www.gobilda.com/steel-flywheel-82mm-diameter-152g-1651-g-cm/ [V] |
| Robot size | 18 in cube to start (R102); after the start, up to 18 × 24 × 29 in tall, physically constrained (R105, M p.122-123) | [V] |

---

## 2. CELL geometry: which way the opening faces and what can enter

### 2.1 Derivation

- **What the CELL is.** In the side view (Fig 9-10, looking along the pivot axis), each CELL is a box laid along its arm. Its two red pentagon frames are seen edge-on and lie perpendicular to the arm. The CELL icon (Fig 9-11) and the 3-D render (Fig 9-9 left) show the arm running along one flat 20 in face, the "base", which carries the AprilTags. **So a CELL is a pentagonal prism whose axis lies along the arm, open at one end** [D].
- **Which end is open.** NECTAR is staged "adjacent to the Hive Back Skin" at the pivot end (EFSG p.23), and the ballast screw is "at the end nearest its opening" on the outside (EFSG p.26). So the opening is at the **outer** end [V/D].
- **How it holds and spills.** In the up position the arm rises 30° outward, so the floor slopes down toward the back skin and balls collect there. In the down position the mouth points 30° below horizontal and everything spills out. This matches "the contents spill onto the field" [D].
- **Coordinates.** Put the pivot at the origin. Let *s* run along the arm and *t* run perpendicular to it, toward the pentagon peak. The arm (floor line) sits a distance *d* below the pivot.
  - Lip height = 43.95 + 21.46·sin30 − d·cos30 = 53.5, so **d ≈ 1.36 in**.
  - Check 1: predicted top of opening = 53.5 + 14·cos30 = **65.62 in**, against 65.6 in given.
  - Check 2: lowest point of the down CELL, including the ~1.6 in frame tab in Fig 9-11, = 43.95 − 10.73 − 1.18 − 1.39 = **30.65 in**, against 30.6 in given [D].
- **Mouth orientation.** The opening plane is perpendicular to the arm, so it **leans back 30° from vertical**. Its outward normal points toward the nearer end wall, **30° above horizontal**. The 14 in edge from lip to peak rises at 60°: 7.0 in horizontal and 12.12 in vertical [D].

**Horizontal positions** (measured from the HIVE pivot line, which is the field centre line between the two end walls) [D]:

| Point | Height (in) | Toward the wall it faces (in) | From that wall (in) |
|---|---|---|---|
| Lip (bottom of opening) | 53.5 | 19.3 | 52.7 |
| Peak (top of opening) | 65.6 | 12.3 | 59.7 |
| AprilTag cluster centre (underside of CELL) | ≈ 49.9 | ≈ 13.1 (6.2 in behind the lip) | ≈ 58.9 |
| Back skin (inner end), floor corner | ≈ 47.5 | ≈ 9 | |

### 2.2 Where you can shoot from

At match start (M p.83, EFSG p.23):

- **Red:** the audience-side CELL is up and its mouth faces the **audience wall**. The red CELL spans X ≈ 49-69 in measured from the red wall (tile column C). The lip is 52.7 in from the audience wall.
- **Blue:** mirror image. The far-side CELL is up, facing the far wall (column D).
- **After each TIP:** the other CELL is up and faces the opposite wall. Red shoots from the audience half for TIPs 1, 3, 5 and 7, and from the far half for TIPs 2, 4 and 6. Blue is the reverse.
- **Shots must come from the mouth side.** A ball approaching from behind meets the closed back skin or roof. Deliberately launching into the outside of a CELL is a G417 example C violation; honest misses are excused under G417 example H (M p.111-112).
- **Maximum head-on distance.** Lip distance D is limited to about **44 in** (robot touching the wall, launcher at robot centre, 52.7 − 9 = 43.7 in), or about 50 in if the launcher sits at the back of an expanded robot. Longer shots must come in at an angle.
- **Oblique shots.** The speed window barely changes with azimuth (see §4.5), but the usable mouth width shrinks with cos(azimuth).

### 2.3 Entry criteria and projected aperture [D]

**Entry condition.** A ball enters if its centre crosses the opening plane moving inward, meaning v·n_out < 0. In practice that means any descending path, or a rising path angle below 60°.

**Clean entry.** The ball must also never come within (r_ball + r_edge) of the lip or peak edge while still outside. The rounded rim edge is taken as r_edge = 0.375 in [A].

**Projected aperture.** For a straight path descending at angle γ:

  a(γ) = 14·sin(60° + γ) − 2·(r_ball + 0.375)  (inches)

| Path at the mouth | γ | POLLEN aperture | NECTAR aperture |
|---|---|---|---|
| Descending perpendicular to the mouth (best) | +30° | 10.45 in | 9.65 in |
| Horizontal | 0° | 8.6 in | 7.8 in |
| Descending steeply | +60° | 8.6 in | 7.8 in |
| Rising | −30° | 3.5 in | 2.7 in |
| Vertical drop | +90° | 3.5 in (7 in horizontal run) | 2.7 in |

**Off-centre shots.** The pentagon is only 7.61 in tall at its sides, so the vertical window shrinks if the ball is off-centre. At a 65° hood and D = 48 in, the speed window falls from ±4.5% at the centre to ±3.9%, ±3.3% and ±2.7% at lateral offsets of 2, 4 and 6 in (§4.5). **Aim at the centre. Lateral error within ±3-4 in costs little.** That corresponds to a turret/heading accuracy of about ±4° at 48 in and ±2.5-3° at 84 in.

### 2.4 Bounce-out risk [E]

**Model.** A 2-D simulation inside the CELL's centre section (`bounce.py`):

- Walls: the floor slopes down 30° to the back skin; the back skin is parallel to the mouth; the roof ridge runs from the peak inward.
- Each wall impact uses normal restitution e_n and tangential retention e_t.
- **Trapped** means the ball's total energy can no longer lift it over the lip (z_lip + r). **Escaped** means its centre re-crosses the mouth plane outward.

**Key insight: the floor/back-skin corner is 90°, so it acts as a retro-reflector.** A ball entering roughly perpendicular to the mouth comes straight back out if it is fast. Two kinds of entry are kept more reliably:

- entries that hit the **roof** first (rising 20° or more);
- **steep descents** of 45° or more, which hit the floor first.

**Maximum POLLEN entry speed (m/s) that is still retained, entering at the mouth centre.** "12" means retained at every speed tested.

| e_n / e_t | γ = −40 | −30 | −20 | −10 | 0 | +10 | +20 | +30 | +45 | +60 |
|---|---|---|---|---|---|---|---|---|---|---|
| 0.40 / 0.60 | 12 | 12 | 12 | 9.9 | 7.8 | 3.8 | 3.8 | 4.2 | 5.7 | 7.6 |
| 0.40 / 0.80 | 12 | 11.1 | 8.2 | 6.5 | 5.5 | 3.8 | 3.7 | 3.9 | 4.6 | 5.4 |
| 0.55 / 0.60 | 12 | 12 | 9.9 | 7.0 | 2.7 | 2.6 | 2.7 | 2.9 | 3.8 | 5.3 |
| 0.55 / 0.80 | 12 | 7.8 | 5.7 | 4.5 | 2.7 | 2.6 | 2.6 | 2.7 | 3.1 | 3.7 |
| 0.70 / 0.80 | 12 | 6.0 | 4.1 | 2.1 | 1.9 | 1.8 | 1.9 | 1.9 | 2.2 | 2.6 |

**Interpretation.**

- e_n for this hollow polyethylene ball on a **thin, flexible polycarbonate skin** is probably 0.4-0.55. It is about 0.6-0.66 on granite for the pickleball analog in §1.1.
- The real CELL is 3-D: sloped roof panels and side walls add dissipation, so this 2-D model is probably pessimistic.
- Balls already inside help. The starting CELL holds 3 NECTAR at the back skin, and they absorb impacts.
- **Design target: arrive at 2-3.5 m/s near the top of the arc, or enter steeply.** The recommended 62-65° shots enter at 2.1-3.9 m/s, descending 0-53°. They are retained at e_n = 0.55 out to D ≈ 84-90 in.
- The probability tables in §4 flag, at each shot's nominal entry, whether this model predicts a bounce-out.
- **Verify bounce-out on a mock-up (§11).**

---

## 3. Ball-flight model

**Equations** (2-D, in the vertical plane of the shot):

  dv/dt = −g ẑ − k_d |v| v + k_l |v| (−v_z, v_x),  where k_d = ρ C_D A / (2m) and k_l = ρ C_L A / (2m)

**Parameters.**

| Parameter | Value |
|---|---|
| Air density ρ | 1.19 kg/m³ (SE Michigan, ~200 m elevation, ~20 °C) [A] |
| g | 9.80665 m/s² |
| k_d at C_D = 0.50 | 0.0474 m⁻¹ for both balls |
| Terminal speed | 14.4 m/s |
| Integrator | RK4, dt = 2 ms. Exact against the analytic vacuum solution; converged to 10⁻⁴ in at dt = 0.5-4 ms |
| Lift | Magnus lift (backspin) optional; C_L = 0.15 sensitivity case |

**Choice of drag coefficient: nominal C_D = 0.50, sensitivity 0.35-0.65** [E]. Literature for perforated balls:

- **Pickleballs** (closest analog: 74 mm, ~26 g, 26-40 holes), from free-flight trajectory fits at 5-13 m/s, 300 fps video, RK4 (Lindsey 2025, Tennis Warehouse University, https://twu.tennis-warehouse.com/learning_center/pickleball/pickleball_aerodynamics.php) [V]:
  - indoor 26-hole: **C_D ≈ 0.45** (SD 0.09);
  - outdoor 40-hole: **C_D ≈ 0.33**;
  - Steyn et al. (2025), as cited there: C_D ≈ 0.30 ± 0.02;
  - Emond, Sun and Swartz (2024, arXiv:2409.19000) assumed C_D = 0.6.
  - Backspin lift: C_L ≈ 0.17-0.20, with a flat baseline (Steyn: C_L ≈ 0.195·S).
- **Floorball CFD** (perforated plastic ball, Re ~ 10⁵): C_D = 0.39 / 0.51 / 0.58 / 0.72 for 6 / 8 / 10 / 13 mm holes. Bigger holes mean more drag (Tandjono et al., AIP Conf. Proc. 2227, 2020, https://scholar.ui.ac.id/en/publications/on-the-aerodynamics-drag-of-floorball/) [V].
- **Wiffle ball wind tunnel** (Rossmann & Rau, *Am. J. Phys.* 75(12):1099-1105, 2007, doi:10.1119/1.2787013, citation confirmed via Crossref). Drag and lift depend on hole orientation, and flow inside the ball matters. A secondary summary quotes C_D falling from about 1 at the lowest speeds to about 0.4-0.6. **[unverified: paywalled]**
- **Smooth sphere, subcritical:** about 0.47.
- The ResisDent balls have fairly large holes (M p.74 photos), so 0.50 is a reasonable central value.

**Spin.**

- A hooded single-wheel launcher rolls the ball on the hood, giving **backspin** with spin parameter S = ωr/v ≈ 1 when there is no slip [D].
- For the recommended 60-65° arcs, Magnus lift with C_L = 0.15 changes the required speed by **≤ ±1%**: it lifts on the way up and pushes forward on the way down, and the two nearly cancel.
- At low angles (line drives) it changes the required speed by up to −10%.

**Search ranges and outputs.**

- Exit angle 20-82° in 0.5° steps; exit speed 3-15 m/s in 0.02 m/s steps.
- Launch heights 12, 16, 20 and 24 in.
- Lip distances D = 12-120 in, where D is the horizontal distance from the ball's release point to the lip.
- Conversions: distance to HIVE centre line = D + 19.3 in; distance to the AprilTag cluster = D + 6.2 in.
- Outputs:
  - **Speed window** [v_lo, v_hi] for a clean entry at each (angle, D). Speed tolerance = (v_hi − v_lo)/(v_hi + v_lo).
  - **Angle window** at the window's mid speed.
  - **Success probability** with Gaussian shot-to-shot errors:

  P(θ₀, v₀) = Σ_θ w(θ − θ₀) · Σ_windows [Φ((v_hi − v₀)/σ_v) − Φ((v_lo − v₀)/σ_v)],  with exit speed capped at 9 m/s.

---

## 4. Results: launch windows

Baseline: POLLEN, C_D = 0.5, no lift, launch height 16 in, head-on.

### 4.1 Best launch angle per distance (requested distances)

**(a) Ranked by speed tolerance alone.** For D ≥ 33 in the winner is always a fast, nearly straight line drive that enters while still rising, so this column only shows why speed tolerance alone is the wrong criterion.

| D lip (in) | D pivot (in) | Best angle | Exit speed window (m/s) | ±% | Angle window at mid speed | Entry state |
|---|---|---|---|---|---|---|
| 24 | 43 | 75.5° | 4.77-5.33 | 5.5 | 68.8-79.2° (±5.2°) | descending 39°, 1.5 m/s |
| 36 | 55 | 50.5° | 10.69-13.87 | 12.9 | 49.8-50.8° (**±0.5°**) | rising 46°, 10.4 m/s |
| 48 | 67 | 43.5° | 9.87-15.0+ | 20.6 | 42.2-44.2° (±1.0°) | rising 38°, 10.5 m/s |
| 60 | 79 | 38.5° | 9.71-14.63 | 20.2 | 36.8-39.8° (±1.5°) | rising 31°, 10.1 m/s |
| 72 | 91 | 34.5° | 10.05-14.79 | 19.1 | ±1.8° | rising 26°, 10.2 m/s |
| 84 | 103 | 31.5° | 10.39-14.71 | 17.2 | ±1.8° | rising 22°, 10.2 m/s |
| 96 | 115 | 29.0° | 10.85-14.85 | 15.6 | ±1.8° | rising 18°, 10.4 m/s |
| 108 | 127 | 27.0° | 11.31-15.0+ | 14.0 | ±1.5° | rising 15°, 10.5 m/s |
| 120 | 139 | 25.5° | 11.69-15.0+ | 12.4 | ±1.5° | rising 13°, 10.5 m/s |

**(b) The practical arc family, angle ≥ 55.5°.** Exit speeds are 5-7 m/s.

| D lip (in) | Best angle | Exit speed window (m/s) | ±% | Angle window at mid | Entry | Flight time | Apex |
|---|---|---|---|---|---|---|---|
| 24 | 75.5° | 4.77-5.33 | 5.5 | 68.8-79.2° | +39°, 1.5 m/s | 0.58 s | 61 in |
| 36 | 55.5° | 6.45-7.45 | 7.2 | 54.2-56.8° | −37°, 4.6 m/s | 0.27 s | 76 in |
| 48 | 55.5° | 5.57-6.31 | 6.2 | 52.8-58.8° | −12°, 3.2 m/s | 0.41 s | 61 in |
| 60 | 55.5° | 5.59-6.19 | 5.1 | 50.8-70.8° (±10°) | +8°, 3.1 m/s | 0.51 s | 60 in |
| 72 | 55.5° | 5.79-6.31 | 4.3 | 49.8-67.2° | +20°, 3.3 m/s | 0.60 s | 62 in |
| 84 | 55.5° | 6.05-6.51 | 3.7 | 48.2-64.8° | +28°, 3.5 m/s | 0.67 s | 66 in |
| 96 | 55.5° | 6.33-6.75 | 3.2 | 47.2-63.2° | +34°, 3.9 m/s | 0.74 s | 69 in |
| 108 | 56.0° | 6.61-7.01 | 2.9 | 45.8-61.8° | +39°, 4.2 m/s | 0.81 s | 74 in |
| 120 | 55.5° | 6.91-7.27 | 2.5 | 44.8-60.8° | +41°, 4.5 m/s | 0.86 s | 78 in |

**Why the arcs are better in practice.**

- **Line drives (table a):**
  - They need ±0.5-1.8° of angle control.
  - They need 10-15 m/s, which means 25-33 m/s surface speed: about 8,000 rpm on a 72 mm wheel. **Not reachable with this motor and wheel.**
  - They enter at about 10 m/s, which risks bounce-out and damage.
  - They are sensitive to Magnus lift (−3% to −10%).
- **Arcs (table b):**
  - The ball arrives near its apex, so the **angle window is ±3-10°**, the speed window ±3-7%, and the ball arrives slowly.
- **Distance limits:**
  - Below about 27 in, only steep lobs of 70° or more work.
  - Above about 96 in, the windows shrink to ±3% or less. Those distances are also mostly unreachable on this field (§2.2).

### 4.2 Fixed hood angle: exit speed and window

POLLEN, launch height 16 in. Cells show mid exit speed in m/s and the ±% window. "!" means the 2-D model predicts bounce-out at e_n = 0.55 (it is retained at e_n = 0.40 in every "!" case at 55° and above).

| Hood (ball exit angle) | D=24 | 36 | 48 | 60 | 72 | 84 | 96 | 108 | 120 |
|---|---|---|---|---|---|---|---|---|---|
| 45° | — | — | 9.95 ±14.7 | 7.44 ±8.5 | 6.98 ±6.4 | 6.91 ±5.2 ! | 7.00 ±4.4 ! | 7.15 ±3.9 ! | 7.35 ±3.5 ! |
| 50° | — | 13.45 ±11.6 | 7.02 ±8.4 | 6.41 ±6.2 | 6.36 ±5.2 ! | 6.47 ±4.3 ! | 6.66 ±3.8 ! | 6.88 ±3.3 ! | 7.12 ±2.9 ! |
| 55° | — | 7.16 ±7.4 | 6.00 ±6.2 | 5.92 ±5.2 ! | 6.07 ±4.3 ! | 6.28 ±3.7 ! | 6.54 ±3.2 ! | 6.81 ±2.9 ! | 7.08 ±2.7 ! |
| 60° | — | 5.80 ±6.0 | 5.58 ±5.2 | 5.74 ±4.4 ! | 5.99 ±3.7 ! | 6.29 ±3.2 ! | 6.59 ±2.7 ! | 6.91 ±2.6 ! | 7.22 ±2.4 ! |
| **65°** | 6.25 ±1.9 | **5.32 ±5.5** | **5.46 ±4.6** | **5.77 ±3.8** | **6.12 ±3.1** | **6.48 ±2.9** | 6.84 ±2.5 ! | 7.21 ±2.2 ! | 7.57 ±2.1 ! |
| 70° | 5.18 ±4.8 | 5.24 ±4.8 | 5.61 ±3.9 | 6.04 ±3.5 | 6.48 ±2.9 | 6.92 ±2.5 | 7.36 ±2.3 ! | 7.79 ±2.1 ! | 8.22 ±1.8 ! |
| 75° | 5.04 ±5.4 | 5.51 ±4.4 | 6.08 ±3.5 | 6.65 ±3.0 | 7.22 ±2.6 | 7.77 ±2.3 ! | 8.32 ±2.0 ! | 8.86 ±1.9 ! | 9.40 ±1.8 ! |

**Shot states at the recommended hood angles** (`targeted.py`): entry angle and speed, flight time and apex.

| D lip (in) | 60°: entry | 60°: t / apex | 65°: entry | 65°: t / apex | 70°: entry | 70°: t / apex |
|---|---|---|---|---|---|---|
| 30 | −41°, 4.1 m/s | 0.28 s / 75 in | −23°, 2.3 m/s | 0.39 s / 62 in | +12°, 1.7 m/s | 0.51 s / 60 in |
| 36 | −24°, 3.0 m/s | 0.36 s / 63 in | −1°, 2.1 m/s | 0.47 s / 60 in | +32°, 2.0 m/s | 0.59 s / 62 in |
| 48 | +5°, 2.6 m/s | 0.49 s / 60 in | +28°, 2.4 m/s | 0.60 s / 62 in | +50°, 2.7 m/s | 0.72 s / 68 in |
| 60 | +23°, 2.8 m/s | 0.60 s / 62 in | +41°, 2.9 m/s | 0.70 s / 67 in | +57°, 3.3 m/s | 0.84 s / 76 in |
| 72 | +34°, 3.2 m/s | 0.68 s / 66 in | +48°, 3.4 m/s | 0.80 s / 73 in | +61°, 3.9 m/s | 0.94 s / 84 in |
| 84 | +40°, 3.6 m/s | 0.76 s / 71 in | +53°, 3.9 m/s | 0.88 s / 79 in | +63°, 4.4 m/s | 1.04 s / 92 in |

Positive entry angles are descending. With a 60-65° hood the required speed is nearly flat for D = 30-60 in (5.3-5.8 m/s), so **distance errors barely matter there**: about 0.5% of speed per inch at 60-72 in, and less closer in.

### 4.3 Success probability: fixed hood vs adjustable

P(clean entry), with exit speed limited to ≤ 9 m/s.

**Realistic errors: σ_v = 3%, σ_θ = 1.5°**

| Hood | D=24 | 36 | 48 | 60 | 72 | 84 | 96 | Mean 24-84 | Mean 36-72 |
|---|---|---|---|---|---|---|---|---|---|
| 55° | — | 55% | 85% | 87% | 83% | 77% | 72% | 65% | 78% |
| 60° | 0% | 77% | 89% | 85% | 77% | 70% | 64% | 66% | **82%** |
| 62.5° | 4% | 85% | 88% | 81% | 74% | 66% | 59% | 66% | **82%** |
| 65° | 22% | 89% | 86% | 78% | 68% | 60% | 54% | 67% | **80%** |
| 70° | 75% | 88% | 76% | 64% | 54% | 47% | 42% | 68% | 71% |
| Adjustable (best per D) | 91% (74°) | 91% (67°) | 89% (60°) | 87% (54°) | 85% (51°) | 82% (46°) | 79% (41°) | 87% | 88% |

**Tighter errors: σ_v = 2%, σ_θ = 0.75°.** Fixed 60-62.5°: 96% (mean over 36-72 in). Fixed 65°: 94%. Adjustable: 99%.

**Conclusions** [E]:

- A **fixed hood at 60-65° plus variable RPM is enough.** Over the 36-72 in band it gives up only 6-8 points to an adjustable hood, and less if shot consistency is good.
- The adjustable hood's real value is **close range (D < 30 in) and long range (D > 80 in)**.
- **The cheapest improvement is consistency** (σ_v and σ_θ), not hood adjustability.
- NECTAR results are 3-5 points lower everywhere because its window is narrower.

### 4.4 Shooting-zone map

**Setup:** red's starting up-CELL, the same map for every up-CELL by symmetry. σ_v = 3%, σ_θ = 1.5°, turret/heading σ = 1.5°, and 0.5 in of position error. Values are % chance of a clean entry for a launcher at that point. X is measured from the red wall and Y from the audience wall; the CELL lip is at Y = 52.7, X = 59.2 ± 9.25 (`zones.py`).

**Fixed 65° hood:**

- **First tile row (Y = 9-21 in), X ≈ 33-93 in: 83-89%.**
- **Head-on, tile C1:** Y = 9 in gives 89% (D = 43.7 in); Y = 15 in gives 89%; Y = 21 in gives 82%.
- **Too close:** C2 directly in front of the CELL (Y = 27 in, D = 25.7 in) gives only 41%.
- **Diagonals:**

| Spot | D (in) | Azimuth | P |
|---|---|---|---|
| B1 | 46.9 | 30° | 87% |
| D1 | 47.7 | 31° | 87% |
| A1 corner | 62.4 | 49° | 72% |
| E1 | 63.5 | 50° | 71% |
| F1 far corner | 83.4 | 61° | 41% |
| B2 | 32.5 | 46° | 84% |
| D2 | 33.6 | 47° | 87% |

**Two-position hood (62.5° and 72°):** at least 85% almost everywhere within about 50 in of the lip, including directly in front of the CELL (C2: 90%). Only the far corners and the area under the CELL are poor.

**Practical "shooting spots" for an up-CELL facing a wall:**

- head-on, about 1 robot-length from the wall: tile C1 for red's starting CELL (D6 for blue's);
- at about 30° off-axis: tiles B1 and D1 for red's starting CELL (C6 and E6 for blue's).

All have P ≈ 85-89% with a fixed ~63-65° hood.

**A red robot can start against the audience wall in tile C1 and fire its preloads head-on at D ≈ 44 in without driving** [D]. This start is legal under G304 (M p.104): fully inside columns A-C, touching the perimeter wall, not in the LOADING ZONE, and clear of FLOWERS (the audience-wall FLOWER is on blue's side at about X = 96 in). The mirror for blue is the far wall in tile D6.

### 4.5 Sensitivities

Change in the required mid exit speed, POLLEN, 60-65° hood, D = 36-84 in.

| Change | Effect | Comment |
|---|---|---|
| No drag (vacuum) vs C_D = 0.5 | vacuum needs **3-6% less** | Drag matters as much as the whole window. Do not tune from a vacuum calculator. |
| C_D 0.35 vs 0.65 | 1.7-3.8% spread | Absorbed by empirical tuning |
| Magnus C_L = 0.15 (backspin) | −0.9% to +0.7% | Negligible for arcs. Up to −10% for line drives. |
| Launch height 12 / 16 / 20 in | Mean P over 36-72 in ≈ 79% / 80% / 81% at 65° | A higher release helps short range: at 65°, D = 24, P = 2% / 22% / 57% |
| Lateral offset 0 / 2 / 4 / 6 in (65°, D = 48) | ±4.5 / 3.9 / 3.3 / 2.7% | Pentagon gets shorter off-centre |
| Azimuth 0 / 30 / 45 / 60° (60°, D = 48) | Window ±5.2 / 5.2 / 5.3 / 5.6% | Usable width 14.9 / 12.5 / 9.5 / 5.7 in |
| Air density ±4% | ±0.2% | Negligible |
| Lip height ±1 in (field tolerance, M p.63) | ≈ ±1-2% shift | Re-check on the practice field |
| Robot moving 1 ft/s during a 0.4-0.9 s flight | 5-11 in miss | **Stop to shoot**, or lead the target in software |

---

## 5. POLLEN vs NECTAR through the same shooter

- **Flight.**
  - Mass per area: 0.628 vs 0.629 g/cm². The mass ratio, 1.655, equals the area ratio, (3.6/2.8)² = 1.653.
  - Same C_D gives **the same drag deceleration**.
  - The required exit speed matches within ±0.2% at every hood angle and distance computed. [D]
  - NECTAR's windows are about 8-12% narrower in relative terms, because the larger ball needs more rim clearance (aperture 9.65 vs 10.45 in at best).
  - Clean-entry probability for NECTAR is 3-5 points lower (§4.3).
- **Launcher: this is where the RPM offset comes from.**
  - In the ideal hooded model (§6.4), the heavier ball gives a larger inertia ratio x, so k is lower.

| Wheel setup | k (POLLEN) | k (NECTAR) | Extra RPM for NECTAR |
|---|---|---|---|
| Bare 72 mm | 0.406 | 0.362 | +12% |
| 72 mm + 1 steel flywheel | 0.472 | 0.455 | +4% |
| Bare 96 mm | 0.423 | 0.384 | +10% |

  - Compression differences (§7) will dominate in practice. **Expect a separate NECTAR RPM table that runs roughly 5-20% higher. Measure it.**
- **Can one shooter serve both?**
  - **Ballistically, yes.**
  - Mechanically, only if the hood can grip a 2.8 in ball and pass a 3.6 in ball. Options are in §7.
  - Strategically, NECTAR has better uses: FLOWER ownership in the last 60 s (G410, M p.109). For TIPs, 1 NECTAR = 1.65 POLLEN of mass.
  - A POLLEN-only shooter, with NECTAR handled by a simple chute or dropper for FLOWERS, is a defensible scope cut for a middle-school team.

---

## 6. Shooter sizing (single wheel, hooded, goBILDA 5203 1:1)

### 6.1 Exit-speed ratio k = v_exit / wheel surface speed

**Theory.** The ball rolls on a stationary hood, so its centre moves at half the wheel's surface speed, and **k ≤ 0.5** is a hard limit.

**Evidence** [V]:

- Chief Delphi, "Determine flywheel velocity for ball exit velocity" (2021), https://www.chiefdelphi.com/t/determine-flywheel-velocity-for-ball-exit-velocity/394940:
  - "A wheel working against a fixed hood will have roughly 1/2 the surface speed of the wheel imparted on the ball" (zog);
  - "1/2 the surface speed of wheel is going to be a maximum … most teams are between 20-45%" (AdamHeard);
  - "30% shot efficiency was pretty standard" (csj1232);
  - two opposed wheels give roughly k ≈ 1 (zog).
- Brettle, "New Flywheel Shooter Analysis" (2023), https://www.chiefdelphi.com/t/new-flywheel-shooter-analysis/439111: exit at no slip gives ω_wf = 2·v_p/r_w; mechanical efficiency "less than 50%"; speed loss per shot is "half of what is predicted by previous work".

**Ideal model with finite flywheel inertia** [D], with ball inertia I_b = β·m·r² and β = 2/3 for a thin shell:

- Impulse balance: (m + I_b/r²)·v = 2J_w and I_w·(ω₀ − ω_f) = r_w·J_w, with v = ω_f·r_w/2.
- Define the inertia ratio **x = m(1+β)·r_w² / (4·I_w)**. Then:
  - **k_ideal = 0.5/(1 + x)**
  - **drop per shot = x/(1 + x) = 2·x·k**
  - **efficiency = 1/(2 + x)**

**Planning value: k = 0.35-0.45, nominal 0.40.** The briefed range of k ≈ 0.4-0.6 is not achievable above 0.5 with a single wheel and a fixed hood.

### 6.2 Required wheel speed

RPM = 60·v / (π·d·k). Over D = 30-90 in at a 65° hood, v = 5.3-6.7 m/s, so the surface speed needed is v/k = **12-19 m/s**.

| Wheel | Factor (RPM per m/s at k = 1) | v = 5.3 m/s (D ≈ 36 in): k = 0.35 / 0.40 / 0.45 | v = 6.5 m/s (D ≈ 84 in): k = 0.35 / 0.40 / 0.45 |
|---|---|---|---|
| 72 mm (team's wheel) | 265.3 | 4,017 / 3,515 / 3,124 | 4,926 / 4,311 / 3,832 |
| 96 mm | 198.9 | 3,013 / 2,636 / 2,343 | 3,695 / 3,233 / 2,874 |
| 104 mm | 183.6 | 2,781 / 2,433 / 2,163 | 3,411 / 2,984 / 2,653 |
| 4 in (101.6 mm) | 188.0 | 2,847 / 2,491 / 2,214 | 3,491 / 3,055 / 2,715 |

### 6.3 Is the 6,000 rpm motor driving the wheel directly adequate?

**Guideline.** Keep the operating point at or below about **75% of the free speed at the lowest expected terminal voltage**: about 4,300 rpm at 11.5 V. Peak power, and the fastest recovery, is near 50% of free speed. Above about 80%, the recovery torque collapses. [E]

**72 mm wheel, direct drive:**

- At k = 0.40: 3,520-4,410 rpm for D = 30-90 in. That is 59-74% of 6,000 and 61-77% of 5,750.
- **OK up to about D = 72 in.**
- If the team measures k ≈ 0.35, the long shots need 4,900+ rpm, which is 85% of free speed at 11.5 V. **Not adequate.**

**96, 104 mm or 4 in wheel, direct drive:**

- 2,400-3,700 rpm, which is 40-64% of free speed: **comfortable, and right at peak power.**
- **Recommended**, especially if the measured k is below 0.4.

**Gearing:**

- **Gearing down is wrong.** It would cap the top speed.
- An overdrive (for example 1:1.5 on the 72 mm wheel) works but cuts torque by a third. A larger wheel is simpler.

**Closed-loop control.**

- **Use closed-loop velocity control, not open-loop power.** With setPower, the flywheel speed tracks battery voltage: 12.5 V vs 11.5 V is a ±4% speed change, as large as the whole window.
- The 28-count encoder on the 1:1 motor gives about 1,600-2,100 counts/s at these speeds. That is adequate for velocity control if velocity is averaged over 20-50 ms.

### 6.4 Energy per shot and RPM drop per shot

**Energy.** For a thin shell with spin parameter 1, ball kinetic energy = ½·m·v²·(1 + β) = (5/6)·m·v².

- POLLEN at 6 / 7 m/s: **0.75 / 1.02 J**, which costs the wheel about **1.6 / 2.1 J** at under 50% efficiency.
- NECTAR needs 1.65× as much.

**Inertia and per-shot drop.** Wheel inertias are estimates from a tread-plus-core model. Rotor (50 g·cm²) and shaft/hubs (100 g·cm²) are included. ✓ marks the team's current set-up.

| Configuration | I_total (g·cm²) | x (POLLEN) | k_ideal (P / N) | Drop per shot (P / N) | Spin-up 0→target at 12 V (v = 6 / 7.5 m/s, k = 0.4) |
|---|---|---|---|---|---|
| 72 mm Hogback ✓ | ~585 | 0.230 | 0.406 / 0.362 | **18.7% / 27.6%** | 0.28 / 0.45 s |
| 72 mm + 1 steel flywheel | ~2,236 | 0.060 | 0.472 / 0.455 | 5.7% / 9.1% | 1.06 / 1.72 s |
| 72 mm + 2 steel flywheels | ~3,887 | 0.035 | 0.483 / 0.473 | 3.3% / 5.4% | 1.84 / 2.99 s |
| 96 mm Hogback | ~1,317 | 0.182 | 0.423 / 0.384 | 15.4% / 23.1% | 0.39 / 0.56 s |
| 96 mm + 1 steel flywheel (designed pairing) | ~2,968 | 0.081 | 0.463 / 0.441 | 7.5% / 11.8% | 0.89 / 1.26 s |
| 104 mm (~120 g, assumed) | ~2,194 | 0.128 | 0.443 / 0.413 | 11.4% / 17.5% | 0.59 / 0.82 s |
| 4 in (~100 g, assumed) | ~1,766 | 0.152 | 0.434 / 0.400 | 13.2% / 20.1% | 0.49 / 0.68 s |

### 6.5 Recovery time after one POLLEN shot

Full-power controller from the post-shot speed back to the target, k = 0.40 (`shooter.py`). The time constant is τ = I·ω_f/T_s = I × 4,358 s/(kg·m²); recovery time t = τ·ln((ω_f,V − ω₁)/(ω_f,V − ω₀)).

| Configuration | Target | 12.5 V | 12.0 V | 11.5 V | 11.0 V |
|---|---|---|---|---|---|
| 72 mm bare | 3,979 rpm (v = 6) | 0.071 s | 0.079 s | 0.088 s | 0.100 s |
| 72 mm bare | 4,974 rpm (v = 7.5, 83% free) | 0.138 s | 0.163 s | **0.199 s** | **0.257 s** |
| 72 mm + 1 flywheel | 4,974 rpm | 0.168 s | 0.205 s | 0.262 s | 0.366 s |
| 96 mm bare | 3,730 rpm (v = 7.5) | 0.112 s | 0.123 s | 0.137 s | 0.154 s |
| 96 mm + 1 flywheel | 3,730 rpm | 0.118 s | 0.130 s | 0.146 s | 0.165 s |

**Observations:**

- Recovery time is set by motor power (about 20 W), not by inertia. Added mass shrinks the drop and lengthens the recovery, and the two roughly cancel.
- **Battery sag hurts most when running near free speed:** +60% recovery time at 11 V vs 12.5 V when at 83% of free speed.
- A real PID loop is slower than a saturated controller. Budget 1.5-3× these times.

### 6.6 Firing four balls quickly

Simulated speed of balls 2-4 relative to ball 1 (`shooter.py`). Ideal controller, POLLEN, v₁ = 7 m/s.

| Configuration | V | Feed every 0.08 s | 0.12 s | ≥ 0.16 s |
|---|---|---|---|---|
| 72 mm bare | 12.0 | −6 / −9 / −11% | −1 / −1 / −1% | 0% |
| 72 mm bare | 11.5 | −7 / −11 / −14% | −3 / −4 / −4% | 0% |
| 72 mm + 1 flywheel | 11.5 | −3 / −5 / −7% | −1 / −3 / −4% | ≈ 0% |
| 96 mm bare | 11.5 | −4 / −7 / −10% | 0% | 0% |
| 96 mm + 1 flywheel | 11.5 | −2 / −4 / −6% | 0% | 0% |

**Implication.** The windows are only ±3-5%, so **feed one ball at a time when |ω − ω_target| ≤ 1-1.5%, typically every 0.15-0.25 s.** A 4-ball volley then takes about 0.5-0.75 s from first to last release (three intervals), plus 0.4-0.8 s of flight for the last ball.

**Starting the flywheel.** Spin up early: 0.3-0.5 s for the bare wheel, 1-2 s with added mass. Keep it idling at the expected RPM while driving to the shooting spot.

**Recommended flywheel set-up.** The 96 mm Hogback + 1 steel flywheel pairing is goBILDA's designed combination (product page). It gives a 6.5-7.5% drop, 0.12-0.15 s recovery and about 1-1.3 s spin-up. It is a good default if the team changes the wheel.

---

## 7. Compression and hood gap (POLLEN 2.8 in vs NECTAR 3.6 in)

- **Prior art (same ball family).** The 2017 FRC FUEL was a 5 in Gopher ResisDent ball. Team 254's 2017 binder (https://media.team254.com/2017/09/964207d8-technicalBinder2017.pdf) [V] records:
  - "Fixed hood at 14º"; "0.5" foam provides optimal compression";
  - four 4 in Fairlane wheels at 3,500 rpm;
  - an "Interpolating Tree Map determines the correct flywheel RPM for the current distance";
  - "once RPM has stabilized … start" (feed).
  
  0.5 in on a 5 in ball suggests compression of about 10% of diameter, partly taken up by compliant hood foam.
- **Pinch force needed** [E]. POLLEN accelerates to about 6 m/s over about 0.1 m of wrap (a ≈ 180 m/s²). The wheel friction force needed is about m(1+β)/2·a ≈ 3-4 N. With μ ≈ 0.8, that is a normal force of about 5 N (about 1 lbf). Hollow polyethylene balls are stiff, so **a few millimetres of compression (0.15-0.35 in, 5-12%) is enough**. The bigger problem is **diameter variation between balls**: M p.74 warns that sizes vary. That is why a **compliant** hood lining (foam, or a flexible polycarbonate hood) is the norm.
- **Fixed gap set for POLLEN** (gap about 2.45-2.6 in): NECTAR would be squeezed 1.0-1.15 in (28-32%). Expect jams, motor stall spikes (up to 9.2 A) or crushed balls.
- **Fixed gap set for NECTAR** (about 3.2-3.3 in): POLLEN (2.8 in) is never gripped and no launch occurs.
- **Options, in order of simplicity:**
  1. **Do not shoot NECTAR.** Use the shooter for POLLEN only and place NECTAR in FLOWERS with a simple chute or dropper. There is only 0.2 in of radial clearance in the 4.0 in FLOWER opening, so placing is better than launching anyway.
  2. **Spring-loaded hinged hood** with at least 0.9-1.0 in of travel. Preload it so POLLEN gets about 0.25 in of compression at rest and NECTAR pushes it open.
     - Exit angle and k will differ for each ball, so build **two calibration tables**.
     - Keep the spring rate low so the NECTAR pinch force stays at about 2-3× the POLLEN force.
  3. **Servo-adjustable gap** with two positions, set by the ball type the intake detects. Each position gets its own RPM table. There appears to be a grey servo on the hood side in the photos; its function is unconfirmed.
  4. **Separate paths:** two hoods or two wheels. Most complex. Not recommended for this team.
- **Exit angle depends on the hood.** The ball leaves tangent to the hood/wheel path at the release point, and compression and spin shift it by a few degrees. **Measure the actual exit angle on video. Do not assume the hood's CAD angle.**

---

## 8. HIVE tipping physics

### 8.1 Official calibration (EFSG §12, p.26-28) [V]

> "Each Hive should be calibrated to tip when [2] combinations of scoring elements are placed in an upward facing Cell: [8] Pollen + [0] Nectar, and [3] Pollen + [3] Nectar."

| Hive state | Test action | Required result |
|---|---|---|
| 3 N + 1 P | 2nd POLLEN "tossed in" | **No tip** (necessary) |
| 3 N + 2 P | 3rd POLLEN gently placed / tossed | Tip (preferred) / **Tip** (necessary) |
| 0 N + 6 P | 7th POLLEN tossed in | **No tip** (necessary) |
| 0 N + 7 P | 8th POLLEN gently placed / tossed | Tip (preferred) / **Tip** (necessary) |

**How calibration is done:** with **ballast washers** on a screw at each CELL's opening end, plus "tweaking" the CELL attachment. Balls are placed "in the back of the Cell contacting the Hive Back Skin". **Teams "should expect some amount of variation"** (EFSG p.26).

### 8.2 Torque model [D/E]

**Where the balls sit.** They settle against the back skin, which is about 9.9 in from the pivot along the arm.

| Ball position | Horizontal moment arm |
|---|---|
| POLLEN, back row | 9.58 in |
| POLLEN, second row | 11.89 in |
| NECTAR, back row | 9.73 in |

About 6 POLLEN or 5 NECTAR fit across the roughly 18.5 in back row (`tip.py`).

**Torques for the calibration cases and other mixes.** Pollen-equivalent (PE) is by mass.

| Contents | Mass (lb) | PE | Torque (lb·in) | Torque (N·m) | Calibration |
|---|---|---|---|---|---|
| 3 N (start) | 0.273 | 4.96 | 2.66 | 0.30 | — |
| 3 N + 2 P | 0.383 | 6.96 | 3.71 | 0.42 | no tip |
| 0 N + 7 P | 0.385 | 7.00 | 3.82 | 0.43 | no tip |
| 3 N + 3 P | 0.438 | 7.96 | 4.36 | 0.49 | **tip** |
| 0 N + 8 P | 0.440 | 8.00 | 4.47 | 0.51 | **tip** |
| 5 N | 0.455 | 8.27 | 4.43 | 0.50 | ≈ tip |
| 2 N + 5 P | 0.457 | 8.31 | 4.53 | 0.51 | tip |
| 4 N + 1 P | 0.419 | 7.62 | 4.07 | 0.46 | borderline |
| 1 N + 6 P | 0.421 | 7.65 | 4.17 | 0.47 | borderline |
| 2 N + 4 P | 0.402 | 7.31 | 3.88 | 0.44 | probably not |
| 1 N + 7 P | 0.476 | 8.65 | 4.83 | 0.55 | tip |

**Threshold:** between 3.82 and 4.36 lb·in, that is **0.43-0.49 N·m, or 7.0-8.0 PE.** The EFSG itself notes that 3P+3N "has less mass than 8 Pollen" (7.96 vs 8.00 PE).

**Practical rules for a robot:**

- Starting CELL: **3 POLLEN, or 2 NECTAR**.
- An empty up-CELL: **8 PE**, such as 8 POLLEN, 5 NECTAR or 2 NECTAR + 5 POLLEN.
- A robot may control at most 4 elements (G407). **Every TIP after the first needs at least two loads.**
- Balls that stop near the mouth instead of at the back skin carry about 25% more moment arm, so **the 8th, occasionally the 7th, will tip it**.

### 8.3 Momentum of launched balls [E]

**Angular impulse about the pivot** for a ball crossing the mouth at (y, z) relative to the pivot, moving inward and descending at γ:

  **L = m·v·(z·cos γ − y·sin γ)**

At the mouth centre (y ≈ 15.8 in, z ≈ 15.6 in), L < 0, the **tipping** direction, only when **γ > ~45°**. Flatter or rising entries push the HIVE **into its stop** (no effect).

**Size.** For POLLEN at 2.5-4 m/s, |L| ≤ 0.01-0.04 N·m·s.

**Energy comparison.** The HIVE's moment of inertia is assumed to be 0.4-1.2 kg·m² (unpublished).

- A 0.012 N·m·s kick gives the HIVE 0.6-1.8×10⁻⁴ J.
- An over-centre bistable model (structure CG above the pivot; a hypothesis) puts the energy barrier at 1.0×10⁻⁴ / 4.2×10⁻⁴ / 1.7×10⁻³ J for deficits of 0.25 / 0.5 / 1.0 PE.
- **So one launched ball's momentum is worth at most about 0.25 PE**, and only for steep entries.
- This matches the calibration rule that a tossed 7th POLLEN must not tip.

**Rate-dependent damping.** The HIVE dampers (Fig 9-9, Fig 10-3) absorb fast motion, which further reduces impulse effects.

### 8.4 Tip timing

**TIP is scored when** the HIVE reaches the other stable state and the damper contacts the frame (M p.87). Contents of the formerly-up CELL spill. **G409: robots may not catch spilled elements before they touch something else** (M p.109).

**Do not fire extra balls at a CELL that is already tipping.** The manual warns that launching at the down-facing CELL while a HIVE is tipping "may disrupt its movement and may result in the TIP not being achieved" (M p.87). In practice:

- Fire exactly 3 POLLEN into the starting CELL.
- Hold the 4th for the next CELL, which faces the other wall.

**Tip duration is not published.** Measure it on video if a replica HIVE is available.

---

## 9. FLOWER stack math

### 9.1 Geometry and stacking [D/E]

**Scoring volume** (M p.88). It runs from the top of the middle ring, about 5.3 in (range about 4.3-5.7 in depending on where the purple CAD volume starts; Fig 9-12 and Fig 10-5 were scaled), to the top surface of the top ring at 21.5 in. That is **about 16-16.5 in tall**. It counts anything at least partially inside, including a ball sitting proud of the top ring (Fig 10-5 H).

**What can pass where:**

- POLLEN passes the middle-ring plate; NECTAR cannot (G418 commentary, M p.112).
- The bottom POLLEN sits on the tile inside the bottom ring (EFSG p.24).
- The first two POLLEN stack single file through the retrieval column and the plate hole.
- Above the plate, balls zig-zag ("expected", EFSG p.24).
- From the zig-zag pitch visible in Fig 10-5, the clear column between the four HIPS pipes is about 4.2-4.4 in (the top opening is 4.0 in).

**Vertical pitch**, for clear column width D_c = 4.0 / 4.2 / 4.4 in:

| Pair | 4.0 in | 4.2 in | 4.4 in |
|---|---|---|---|
| POLLEN on POLLEN | 2.53 in | 2.42 in | 2.30 in |
| POLLEN on NECTAR | 3.10 in | 3.04 in | 2.97 in |
| NECTAR on NECTAR | 3.58 in | 3.55 in | 3.51 in |

### 9.2 Validation against Fig 10-5 (M p.89)

The model counts elements in the volume:

| Manual example | Manual count | Model count (z_m = 5.3 / 5.7 in) |
|---|---|---|
| A | 6 | 6 / 6 |
| B | 5 | 5 / 5 |
| C | 6 | 6 / 6 |
| D | 8 | 8 / 7 |
| E | 4 | 4 / 4 |
| G | 6 | 7 / 6 |
| H | 7 | 7 / 6 |

That is within ±1 everywhere. The ±1 comes from the 2nd POLLEN from the bottom, which sits within about 0.3 in of the volume's floor. **The manual's figures treat it as NOT counted.**

### 9.3 Capacity and points

An owned FLOWER scores 2 pts per element in the volume; the bottom NECTAR bonus is +5 (Table 10-2, M p.91).

| End state | Counted elements | Owner points | Notes |
|---|---|---|---|
| Starting 4 POLLEN, no NECTAR | 2-3 | 0 | No owner |
| 4 starting POLLEN + 1 own NECTAR | 3-4 | 6-8 (+5 if that is the only NECTAR) | Cheapest ownership: **11-13 pts** for one NECTAR |
| Top up with about 4 POLLEN, then cap with 1 NECTAR | 7-8 | 14-16 (+5) | **19-21 pts**, as in Fig 10-5 D |
| 4 starting POLLEN + NECTAR until full | 3-4 NECTAR; 5-7 total | 10-14 | NECTAR is scarce: 8 per alliance |
| Alternating contested stack (Fig 10-5 H) | 7 | Owner 14 | The other alliance's bottom NECTAR earns 5 |
| Column of NECTAR only (all POLLEN pulled) | 6 | 12 | Theoretical |

**Theoretical ceiling:** about 16 + 5 = 21 pts per FLOWER, so about 84 pts for all four. In practice the top-most NECTAR decides ownership, so the **last legal NECTAR placed in the volume wins**.

### 9.4 Pulling POLLEN from the bottom

**Mechanics.**

- Each POLLEN pulled through the retrieval opening (3.55 in tall × 3.57 in deep) lowers everything above it by about one pitch (2.3-2.8 in).
- The ball removed is usually an *uncounted* ball from the retrieval column, so **the count usually does not drop**.
- Pulling stops working once a NECTAR rests on the middle-ring plate.

**Uses:**

1. **Make room** for one more element on top.
2. **Rescue a stranded NECTAR.** A NECTAR resting fully above the top ring (Fig 10-5 G, red) does not count. In the model, pulling 2 POLLEN drops it about 4.8 in, back into the volume, and makes it the top-most: **ownership flips**.
3. **Harvest POLLEN** for the HIVE. There are 16 POLLEN in FLOWERS at the start (M p.83).

**Launching into a FLOWER is impractical** [D]. The 4.0 in opening leaves 0.6 in of radial clearance for POLLEN and 0.2 in for NECTAR. Placing from above, using the 29 in expansion height (R105) and the 1.25 in backstop, is far more reliable.

---

## 10. Recommendations

1. **Shooting band.**
   - Lip distance D = 30-60 in. That puts the launcher 49-79 in from the HIVE centre line, and 36-66 in from the CELL's AprilTag cluster measured horizontally.
   - Head-on from the first tile row, or at up to about 30-45° off-axis.
   - Avoid D < 27 in (directly under the front of the CELL) and the far corners (D > 80 in at more than 50° off-axis).
2. **Hood.** Fix the **actual** ball exit angle at about **64° (range 62-65°)**, measured on video.
   - Optional phase-2 upgrade: a 2-position hood (about 62° normal, about 72° for D < 30 in).
   - A fully adjustable hood is worth it only if the team will shoot from very close or very far.
3. **Starting RPM table** (to tune from). Assumptions: hood 65°, POLLEN, launch height 16 in, k = 0.40. Tag distance = lip D + 6.2 in; pivot distance = lip D + 19.3 in.

| Lip D (in) | Tag D (in) | Exit speed (m/s) [window] | ±% | 72 mm rpm | Ticks/s | 96 mm rpm | Ticks/s |
|---|---|---|---|---|---|---|---|
| 24 | 30 | 6.25 [6.13-6.37] | 1.9 | 4,145 | 1,934 | 3,108 | 1,451 |
| 27 | 33 | 5.68 [5.42-5.94] | 4.6 | 3,767 | 1,758 | 2,825 | 1,318 |
| 30 | 36 | 5.44 [5.15-5.73] | 5.3 | 3,608 | 1,684 | 2,706 | 1,263 |
| 36 | 42 | 5.31 [5.03-5.60] | 5.4 | 3,525 | 1,645 | 2,643 | 1,234 |
| 42 | 48 | 5.35 [5.09-5.62] | 4.9 | 3,551 | 1,657 | 2,663 | 1,243 |
| 48 | 54 | 5.46 [5.22-5.71] | 4.5 | 3,624 | 1,691 | 2,718 | 1,268 |
| 54 | 60 | 5.60 [5.38-5.83] | 4.0 | 3,717 | 1,735 | 2,788 | 1,301 |
| 60 | 66 | 5.77 [5.56-5.98] | 3.6 | 3,826 | 1,786 | 2,870 | 1,339 |
| 66 | 72 | 5.94 [5.74-6.14] | 3.4 | 3,939 | 1,838 | 2,954 | 1,379 |
| 72 | 78 | 6.12 [5.93-6.31] | 3.1 | 4,058 | 1,894 | 3,044 | 1,420 |
| 78 | 84 | 6.29 [6.11-6.48] | 2.9 | 4,175 | 1,948 | 3,131 | 1,461 |
| 84 | 90 | 6.48 [6.30-6.66] | 2.8 | 4,297 | 2,005 | 3,223 | 1,504 |

   - For a 60° hood, the speeds are 5.58-6.57 m/s over D = 30-84 in; see `r3out/rpmtable.md`.
   - **Scale the whole table by k_measured/0.40 after the first test.** Then refine each row empirically.
   - Ticks/s assume the 28 counts per revolution of the 1:1 goBILDA motor.
4. **Control.**
   - Closed-loop velocity (PIDF with voltage-compensated feedforward).
   - Feed only when RPM is within ±1-1.5%.
   - Keep 0.15-0.25 s between balls.
   - Pre-spin while driving.
   - **Stop the robot before shooting.**
   - Aim the turret at the centre of the mouth; target turret error ≤ ±2-3°.
5. **Wheel.** If the measured k < 0.4, or long shots are wanted, **switch to a 96 mm Hogback + steel flywheel driven directly**. Otherwise keep the 72 mm wheel and consider one steel flywheel; it mounts to the 96 mm Hogback, so check how it would attach to the 72 mm wheel.
6. **NECTAR.** Flight is identical, but it needs its own RPM table and hood compliance. Simplest is not to launch NECTAR at all (§7).
7. **AUTO.** Spin up during the drive. A red robot starting against the audience wall in tile C1 (blue: far wall, D6) can fire 3 POLLEN head-on at D ≈ 44 in for a first TIP within a few seconds. After the TIP, the next up-CELL faces the **far** wall.
8. **TIP accounting.** First TIP = 3 POLLEN into the starting CELL. Each later TIP = 8 PE into the CELL facing the opposite wall. Do not fire into a CELL that is already tipping.

---

## 11. Calibration and experiment plan

**Equipment.**

- Tape measure, phone with 240 fps slow motion, a contrasting backdrop with 1 in grid tape, and a log from the robot (encoder velocity at ≥ 50 Hz, battery voltage).
- A CELL-mouth mock-up made of foam-board, cardboard or PVC and polycarbonate:
  - lip at **53.5 in**;
  - opening leaning back 30°, so the peak is 7 in farther away horizontally at **65.6 in**;
  - pentagon 20 in wide, 7.61 in sides, 14 in centre;
  - a 12 in deep box behind it, sloping down 30° to a closed back skin;
  - AprilTag cluster printout on the underside, 7.2 in in from the mouth (printable tags: M p.75, Playing Field Resources page).
  - Field elements carry ±1 in tolerance (M p.63).

**Experiments:**

1. **Exit speed and exit angle at 3 RPMs** (10 shots each).
   - Film perpendicular to the shot. v comes from the ball's displacement over the first 10-20 frames.
   - Cross-check v with the apex height: Δh = (v·sin θ)²/2g, about 3% low because of drag.
   - θ comes from the first frames.
   - **Gives k = v/(π·d·RPM/60), the shot-to-shot σ_v and σ_θ.**
   - Targets: σ_v ≤ 2-3%, σ_θ ≤ 1-1.5°.
2. **RPM drop and recovery.**
   - Log ω(t) during single shots and 4-ball volleys at about 12.8 V and about 12.0 V.
   - **Gives the drop % (compare with §6.4), recovery time, and the feed interval to use.**
3. **RPM table.**
   - At D = 30, 36, 42, 48, 54, 60 and 72 in (and 84 if needed), sweep RPM in 1% steps.
   - At each step, shoot 10 and record the lowest and highest RPM where at least 8/10 enter cleanly.
   - **The table value is the midpoint. Expect windows of ±3-5%.** Narrower windows mean σ_v or σ_θ is too large.
4. **Bounce-out.**
   - At the table settings, shoot 20 each into an empty CELL and into a CELL holding 3 NECTAR.
   - **If more than 5% bounce out, raise the hood angle a few degrees or lower the entry speed.**
5. **Off-axis.** Repeat 3 spot checks at 30° and 45° azimuth and at the corner tiles. Measure the lateral spread; the target is σ ≤ 1.5-2 in at D = 48 in.
6. **Battery.** Repeat a short table check at 12.0 V. With closed-loop velocity control the table should hold. If it does not, add voltage compensation.
7. **NECTAR** (only if NECTAR will be launched). Repeat steps 1 and 3 with NECTAR and the chosen hood-compliance scheme.
8. **Vision.** Compare the Limelight tag-based distance with the tape measure at 5 spots. **±2 in is sufficient** (≤ 1% speed error).
9. **HIVE.** If a HIVE replica is available, calibrate it per EFSG §12.3 and test with *launched* balls:
   - does a launched 7th POLLEN ever tip it;
   - how long the tip takes;
   - where spills land, for G409-compliant pickup.

---

## 12. Assumptions, limitations, open questions

**Assumptions:**

- The opening is a plane lip-to-peak, with rounded edges of r = 0.375 in.
- Interior depth is 11 in.
- The model is 2-D: lateral and oblique effects are handled separately.
- C_D = 0.5 (0.35-0.65); C_L = 0 (0.15 sensitivity).
- Launch height 16 in; ρ = 1.19 kg/m³.
- Wheel inertias are modelled, not measured.
- The 5203 rotor is about 50 g·cm².
- The ideal hooded impulse model (no slip at exit).

**Not modelled:**

- Ball-to-ball collisions inside the launcher.
- Hood friction slip.
- Real PID dynamics.
- Spin decay.
- 3-D bounce in the CELL.

**Open questions:**

- **Actual k and exit angle** of the team's C-shaped hood with the 72 mm Hogback. These drive everything in §6.
- **Where the photographed robot's ball exits:** height and offset from the turret axis.
- **Which motor drives what.** The 6,000 rpm motor was described as "for turret", but the photos suggest it drives the flywheel, with a servo and pinion turning the ring gear.
- **COR** of ResisDent balls on the polycarbonate CELL skin, and the real bounce-out rate.
- **HIVE moment of inertia and tip duration;** how much event-to-event variation there is in the calibration.
- **FLOWER volume floor:** the exact height (middle-ring top) in the official CAD (Onshape link via M p.88), which settles whether the 2nd POLLEN counts.
- **Whether a team-buildable CELL drawing exists** (check FIRST's Playing Field Resources page).

---

## Appendix A: Scripts and outputs

All files are under `scratch/`. Run with `python3 -I`; standard library only.

| Script (`scripts/`) | Output (`r3out/`) | What it does |
|---|---|---|
| `ballistics.py` | — | RK4 drag and Magnus integrator; CELL geometry; clean-entry classifier |
| `run_windows.py` | `windows/*.json` | Angle × speed scans for 10 configurations |
| `summarize.py` | `summary.md` | Best-angle tables, fixed-hood tables, pollen vs nectar, vacuum, C_D and Magnus comparisons |
| `prob.py` | `prob.md` | Success probabilities |
| `bounce.py` | — | Bounce-out model |
| `targeted.py` | `targeted.txt` | Lateral, oblique and shot-state tables |
| `zones.py` | `zones_*.txt` | Field shooting-zone maps |
| `rpmtable.py` | `rpmtable.md` | Starting RPM table |
| `shooter.py` | `shooter.txt` | Wheel, motor, inertia, recovery and volley analysis |
| `tip.py` | `tip.txt` | HIVE torque and impulse estimates |
| `flower.py` | `flower.txt` | FLOWER stack model |

Downloaded primary documents are in `downloads/fieldguide/eventfieldguide.pdf`, `downloads/team254/binder2017.pdf` and `downloads/pickleball/usap_esm.pdf`. Manual renders are in `r3img/`.
