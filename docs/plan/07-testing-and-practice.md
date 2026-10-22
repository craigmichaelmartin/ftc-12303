# 07 · Testing and practice

Experiments turn our estimates into real numbers for **our** robot, **our** HIVE and **our** field. Fill in the tables (copy them into a spreadsheet if easier). Every experiment ends with a **decision**.

Robot time is 4–8 hours a week, so run the experiments in the order shown and don't skip the set-up checks. Coordinates are red ([01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame)). Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

---

## Set-up checks (week 1, before any experiment)

| Check | Target | Our result | OK? |
|---|---|---|---|
| Practice field, wall to wall, both directions | Official CAD field is **141.3 in** (CAD). Tiles may make ours different | | |
| Our HIVE pivot position from two walls | Centre of the field; pivot 43.95 in high (M p.69) | | |
| Up-CELL lip height | 53.5 ± 0.5 in (FAC 1.13) | | |
| Odometry pods: **Check: Odometry**, push 48 in forward, then 48 in left | Pinpoint found; each pod line names the pod (goBILDA 4-bar ≈ 505 counts/in, swing-arm ≈ 337, SWYFT 38 mm ≈ 872, or a measured value) and a direction; copy the SET lines into RobotConfig §3 and rebuild | | |
| Odometry: push the robot 48 in straight | Reads 48 ± 0.5 in | | |
| Odometry: spin the robot 10 full turns | Reads 3600 ± 2° | | |
| Limelight distance vs tape measure, 5 spots | Within ±2 in (≈ 1% speed error, est.) | | |
| Limelight bearing vs a taped line | Within 1° | | |
| Battery voltage right off the charger / after a match | Write both down; fresh packs read above 13 V | | |
| Tape marks on the floor | S1, S6, A-head (55, -12.75), B-head (-55, -12.75), the Zone A side spot (55, -37), the corner / inner / solo park slots ([02](02-autonomous.md#poses-red)), the shot-map spots below | | |
| Limelight tag heights (Check: Limelight, pipeline ID filter 30–45, 82.55 mm, Full 3D on) | On our S1 the up-CELL tags read ~49–50 in "UP"; from the calibration spot (36, -12.75) (blue (-36, 12.75)), facing the HIVE, the second CELL's tags read ~35–36 in "dn" and "vision says A". All heights off by the same factor → fix the marker size, not the pitch | | |
| Ball time at the mouth (Check: Sensors: after START press RB, then hold A and pull 10 single POLLEN in) | `MOUTH_ONE_BALL_MAX_MS` = longest single-ball time + 30% (the screen shows the suggestion). Redo after any intake, servo/motor or power change. Bench: with 3 counted, feed two touching POLLEN: the 2nd must come back out (BALLS 4, 4 inside) | | |
| **Measure on robot:** turret ring and pinion teeth (or the ring's tooth-tip diameter D in mm: teeth ≈ D ÷ 0.8 − 2) | Ratio 4–6:1 expected (photo estimate 4.5–5.5:1; pinion ≈ 60T). Set `TURRET_GEAR_RATIO` and soft limits ±(150 ÷ ratio − 3), then run Tune: Turret Calibrator | | |
| **Measure on robot:** flywheel drive type and ratio | Photos show a right-angle bevel/miter pair. Count motor turns per wheel turn; `FLYWHEEL_TICKS_PER_REV` = 28 × that ratio | | |
| **Measure on robot:** hood contact surface | What the ball is squeezed against (printed C, black foam block or grey servo pad), the gap, and whether the exit angle can change without reprinting | | |
| **Measure on robot:** intake roller layout | One shaft or two; boot-wheel size (72 or 96 mm); teeth on both intake gears and which one the servo drives; the servo label (2000-0025-0002/-0003/-0004) and mode | | |
| **Measure on robot:** 60 mm steel flywheel fit | Axial room between a wheel face and its printed arm > disc thickness + screw heads ([05 §3.2](05-robot-tuning.md#32-add-the-60-mm-steel-flywheel)) | | |
| **Measure on robot:** size through the full turret travel | Power off, turret turned by hand every ~15°: inside 18 × 24 × 29 in at every angle (R105, Fig 12-6) | | |

### HIVE calibration (EFSG §12.3, FAC 2.3–2.6)

Place balls **gently against the back wall** of the up CELL (EFSG §12.2). Add or remove ballast washers on the screw at the CELL's opening end. Calibrate each CELL separately; changing one affects the other (EFSG §12).

| Test | Must happen | Result | Washers changed |
|---|---|---|---|
| 3 NECTAR + 1 POLLEN, **toss** in the 2nd POLLEN | **No tip** | | |
| 3 NECTAR + 2 POLLEN, **toss** in the 3rd POLLEN | **Tip** | | |
| 6 POLLEN, **toss** in the 7th | **No tip** | | |
| 7 POLLEN, **toss** in the 8th | **Tip** | | |

---

## Exp 1 — Tip threshold on our HIVE

**Question:** how many **launched** balls tip our calibrated HIVE, how long does the tip take, and where does the spill land?

**Procedure**
1. Calibrate (table above).
2. **(a) Start CELL:** 3 NECTAR against the back wall. The robot at S1 launches POLLEN **one at a time**, 4 s apart. Count only balls that stay in: on **Test: Tip Counter** press **LB** for every ball that misses or bounces out (its "unconfirmed" line shows feeds that may not have launched a ball). Repeat 5 times.
3. **(b) Empty CELL:** launch **4-ball volleys** until it tips. Repeat 5 times.
4. **(c) Volley vs one-at-a-time:** repeat (a) with a 4-ball volley. Is the dwell shorter? Saline video suggested it is (MonaShores; weak evidence).
5. Film every tip (phone at 60–240 fps). Time from the threshold ball landing → HIVE starts to move (**dwell**) → damper hits the frame (**swing**).
6. Tape-measure where the first spilled ball touches the floor (distance from the end wall it faced) and where the balls stop.

**Data sheet**

| Trial | Start | Method | Tipped on ball # | Dwell (s) | Swing (s) | First touch from end wall (in) | Balls ended up where | Notes |
|---|---|---|---|---|---|---|---|---|
| a1 | 3 NECTAR | one at a time | | | | | | |
| a2 | 3 NECTAR | one at a time | | | | | | |
| a3 | 3 NECTAR | one at a time | | | | | | |
| a4 | 3 NECTAR | one at a time | | | | | | |
| a5 | 3 NECTAR | one at a time | | | | | | |
| b1 | empty | 4-ball volleys | | | | | | |
| b2 | empty | 4-ball volleys | | | | | | |
| b3 | empty | 4-ball volleys | | | | | | |
| b4 | empty | 4-ball volleys | | | | | | |
| b5 | empty | 4-ball volleys | | | | | | |
| c1–c3 | 3 NECTAR | 4-ball volley | | | | | | |

**What to expect:**
- (a) tips on the 3rd ball; (b) on the 8th (EFSG §12.3).
- Dwell 0.25–3.4 s, swing 0.65–0.9 s, spill on the tiles about 3 s after the threshold ball (MonaShores).
- First touch about 35–42 in from the end wall (MonaShores, 2 clean spills).

**Decisions:**
- Confirm "3 tip the start CELL" and "8 tip an empty CELL" for the coach's count.
- Set the AUTO spill hold (code: the start-CELL tags seen up and then gone for 1.0 s, `A1_SPILL_TAGS_GONE_SEC`, or 6 s after the last shot, `A1_SPILL_HOLD_MAX_SEC`).
- From (b), note where the **TIP-2** first touch lands: A3 role 1 leaves its spot right after the threshold ball and skims the bottom edge of the nominal TIP-2 box ([02](02-autonomous.md#a3-role-1-tipper-timeline)). Measure this before promoting A3.
- Confirm the landing boxes in [01 §8](01-game-and-scoring.md#8-spill-where-the-balls-go-after-a-tip).

---

## Exp 2 — Shot map and hood angle

### 2a. Hood exit angle and speed ratio k (do this first)
1. Film perpendicular to the shot at **240 fps**, in front of a backdrop with a 1 in grid. Log the flywheel RPM.
2. 3 wheel speeds (e.g. 3,000 / 3,500 / 4,000 rpm) × 10 shots each.
3. From the first 10–20 frames after the ball leaves, measure the **exit angle** and the **exit speed v**.
4. Compute **k = v ÷ (π × 0.072 m × RPM ÷ 60)**: the ball's exit speed divided by the wheel's surface speed.
5. Adjust the hood until the **actual exit angle is 64–65°**.

| RPM | Shot | Exit angle (°) | Exit speed v (m/s) | k | Notes |
|---|---|---|---|---|---|
| 3,000 | 1–10 | | | | |
| 3,500 | 1–10 | | | | |
| 4,000 | 1–10 | | | | |
| **Spread** | | σ angle target ≤ 1–1.5° | σ speed target ≤ 2–3% | average k = | |

(Targets and method come from our ball-flight model, est. For one wheel against a fixed hood, k can't exceed 0.5. A Chief Delphi poster guessed "most teams are between 20-45%" (FRC). Our model plans on 0.35–0.45.)

### 2b. Shot map (builds the distance → RPM table)

Put the **turret axis** on each spot. Red, Zone A, aiming at the audience CELL (16.50, -12.74): spot = (16.5 + d·cos a, -12.74 − d·sin a). "–" means the robot would have to be outside the audience wall: **head-on shots longer than about 45 in are impossible in Zone A** (CAD).

| Distance d (turret axis → aim point) | 0° off-axis | 20° off-axis | 40° off-axis |
|---|---|---|---|
| 33 in | (49.5, -12.7) | (47.5, -24.0) | (41.8, -33.9) |
| 39 in | (55.5, -12.7) | (53.2, -26.1) | (46.4, -37.8) |
| 45 in | (61.5, -12.7) ≈ S1 | (58.8, -28.1) | (51.0, -41.7) |
| 51 in | – | – | (55.6, -45.5) |
| 57 in | – | – | (60.2, -49.4) |

**Procedure**
1. At each spot, start from the seed RPM below. Step the speed in 1–2% steps.
2. Shoot **10** at each step.
3. Find the band where **≥ 8/10 go in cleanly**. The table value is the **middle of the band** (method from our ball-flight research).
4. Record makes, bounce-outs, and misses (short / long / left / right), plus the battery voltage.
5. Spot-check the other side (Y = -12.74 + d·sin a) with 5 shots at 20° and 40°.

**Seed RPMs** (est.: model with a 72 mm wheel, 64–65° exit and k = 0.40; same as the code's starting table). **After 2a, multiply every RPM by 0.40 ÷ k_measured** (a lower k needs more RPM).

| Distance (in) | 33 | 39 | 45 | 51 | 57 |
|---|---|---|---|---|---|
| Seed RPM | 3,600 | 3,525 | 3,550 | 3,625 | 3,725 |
| Seed ticks/s (28 per rev) | 1,680 | 1,645 | 1,657 | 1,692 | 1,738 |

Notes: the model's speed window is only about ±3–5% (est.). Between 30 and 60 in the required speed hardly changes, so distance errors matter little there.

**Data sheet (one row per spot and RPM step)**

| d (in) | Angle | RPM | Made | Bounce-out | Short | Long | Left | Right | Battery V | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| | | | /10 | | | | | | | |

**Result: the table that goes into the code**

| d (in) | Band low RPM | Band high RPM | **Table RPM (middle)** | Made at table RPM |
|---|---|---|---|---|
| 33 | | | | /10 |
| 39 | | | | /10 |
| 45 | | | | /10 |
| 51 | | | | /10 |
| 57 | | | | /10 |

### 2c. Bounce-out check
At the table settings, shoot **20 into an empty CELL** and **20 into a CELL holding 3 NECTAR**.

| CELL | Shots | Went in | Bounced out | Bounce-out % |
|---|---|---|---|---|
| Empty | 20 | | | |
| 3 NECTAR inside | 20 | | | |

**Decision:** more than 5% bounce-outs → raise the hood a few degrees, lower the entry speed, or soften the hood lining (our ball-flight research). Then retest. One team measured 44% with a solid hood (CD-14343).

---

## Exp 3 — Flywheel recovery

**Question:** how much speed does each shot cost, how fast does the wheel recover, and does the 60 mm steel flywheel help?

**Procedure:** log time, target RPM, measured RPM, battery voltage and feed events at **≥ 50 Hz** to a CSV on the Control Hub. Fire 3 volleys of 4 in each case.

| Case | Battery V | Spin-up 0 → ready (s) | Drop per shot (%) | Recovery (s) | First → last ball (s) | Made /4 (×3) |
|---|---|---|---|---|---|---|
| Bare wheel, fresh battery (~12.8 V) | | | | | | |
| Bare wheel, used battery (~12.0 V) | | | | | | |
| + 60 mm steel flywheel, fresh | | | | | | |
| + 60 mm steel flywheel, used | | | | | | |

**What to expect (model):**
- Drop per POLLEN shot about **19% bare** and **11% with the steel flywheel**.
- Spin-up about 0.3–0.5 s bare, about 0.5 s with the flywheel.
- Full-power recovery 0.08–0.2 s; a real controller is 1.5–3× slower (our flywheel model; est.).

**Decision:** keep the steel flywheel if made shots go up and spin-up stays under 1 s. Set the feed spacing to the measured recovery time (the code can't feed faster than one ball per 0.33 s: 180 ms gate + 150 ms interval). If "last shot: drop" never updates in Tune: Flywheel, the shot dip is not detected: lower `FLYWHEEL_SHOT_DIP_FRACTION` or fit the feed beam.

---

## Exp 4 — Load-cycle timing

**Setup:**
- Scatter 8 POLLEN where a Zone A spill lands: X 30–60, Y -30 to -5, mostly against the audience wall.
- The robot starts at A-head (55, -12.75).
- The HIVE is set so the far CELL is up.

**Run:** "GO" → sweep 4 → cross by the corridor (y ≈ -40) → volley from B-head (-55, -12.75). Stop the watch when the 4th ball leaves.

| Trial | Sweep (s) | Cross (s) | Aim + volley (s) | **Total (s)** | Made /4 | Notes |
|---|---|---|---|---|---|---|
| 1 | | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |
| 4 | | | | | | |
| 5 | | | | | | |

**Target: ≤ 20 s per load** (strategy brief); stretch 15 s. For comparison, a strong team's benchmark is "a tip every 30 seconds" (CD-14343).

---

## Exp 5 — FLOWER-bottom extraction

**Question:** can our front roller pull POLLEN out of a FLOWER's bottom opening? 16 of the 24 non-preload POLLEN start in FLOWERS (M p.83).

**Set-up:** a FLOWER, or a replica of its bottom (M p.72–73):
- bottom ring 0.43 in thick with a 2.79 in hole;
- **retrieval opening 3.55 in tall × 3.57 in deep**;
- middle ring underside about 3.98 in above the tiles.

Stack 4 POLLEN from the tile up. A static POLLEN can't fall out by itself; removal takes a sideways push (FAC 2.8–2.9).

**Run:** 10 tries. Drive in slowly with the intake on.

| Try | POLLEN came out? | Time (s) | Touched or moved the FLOWER? | Pulled through the **side**? (illegal, G418 box D) | Notes |
|---|---|---|---|---|---|
| 1–10 | | | | | |

**Pass (team target):** ≥ 8/10, each in ≤ 3 s, never through the side.
- **Pass:** FLOWER bottoms become ammo source #4 ([03](03-teleop.md#ammo-sources-in-this-order)). In AUTO, use our own-side FLOWERS only (G402).
- **Fail:** consider the OPTIONAL corner Gecko-wheel extractor ([05 §4](05-robot-tuning.md#4-optional-bigger-changes-only-after-23-work)).

---

## Exp 6 — NECTAR go / no-go

Run this only once POLLEN cycles are reliable (week 4).

| Step | Test | Result | Pass? |
|---|---|---|---|
| 1. Intake | 10 NECTAR at 1 ft/s, 10 at 2 ft/s: picked cleanly? any stall over 1 s? | | |
| 2. Indexer | 10 NECTAR to the feed position: any jams? | | |
| 3. Hood | 10 launches from 39 or 45 in, starting at the POLLEN RPM + 10% (NECTAR probably needs +5–20%, est.). Made, jams, motor stall, **ball damage** (gouges or tears are a G406/R201 problem) | | |
| 4. Mixed | 2 NECTAR + 2 POLLEN volley: does one setting handle both? | | |
| 5. FLOWER placing (only for FLOWER points) | 10 placements into a 4.0 in ring at 21.5 in (dropper or lob) | | |

- **GO for the tip accelerant** (phase 3a): steps 1–2 ≥ 9/10 with no stall over 1 s, no ball damage, step 3 ≥ 7/10 clean.
- **GO for FLOWERS** (phase 3b): step 5 ≥ 8/10.
- **NO-GO:** stay POLLEN-only: keep `ACCEPT_OWN_NECTAR = false` (the intake refuses all NECTAR at the mouth) and drivers keep steering around NECTAR.
- **GO** for either option: set `ACCEPT_OWN_NECTAR = true` (then only the opponent's NECTAR is refused).

---

## Exp 7 — AUTO reliability

10 runs in a row per rung, same jig, HIVE calibrated. **Promote only at ≥ 8/10** ([02 promotion rule](02-autonomous.md#promotion-rule)). A2 and A3 only with a tuned follower; note the Park slot used and any `CHECK:` line.

| Run | Rung | Battery V | Tip? (time s) | LEAVE (off all walls) | PARK | Stayed on our side | No spill hit | Parked at (s) | Success? | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | | | | | | | | | | |
| 2 | | | | | | | | | | |
| 3 | | | | | | | | | | |
| 4 | | | | | | | | | | |
| 5 | | | | | | | | | | |
| 6 | | | | | | | | | | |
| 7 | | | | | | | | | | |
| 8 | | | | | | | | | | |
| 9 | | | | | | | | | | |
| 10 | | | | | | | | | | |
| **Total** | | | | | | | | | **/10** | |

---

## Exp 8 — Intake pickup speed

**Question:** is the servo intake fast enough, and which GARDEN sweep works ([02 poses](02-autonomous.md#poses-red))?

| Test | 10 tries each | Picked | Missed | Stalled | Notes |
|---|---|---|---|---|---|
| Drive into a single POLLEN at 1 ft/s | | | | | |
| … at 2 ft/s | | | | | |
| … at 4 ft/s | | | | | |
| GARDEN sweep option 1 (face the wall, 3 s; not in the code) | balls in 3 s | | | | |
| GARDEN sweep option 2 (the code's sweep: along the wall, (60.5, -46) → (60.5, -60) at 0.3 speed) | balls in 3 s | | | | |

**Decisions:**
- Below 8/10 at 2 ft/s → consider the OPTIONAL DC-motor intake ([05 §4](05-robot-tuning.md#4-optional-bigger-changes-only-after-23-work)).
- A2/A3 only if one sweep option gets **4 balls in ≤ 3 s, 8/10 times**.
- Try the 6 V servo power first ([05 §3.6](05-robot-tuning.md#36-intake-power-keep-the-servo-give-it-6-v)).

---

## Driver drills

Driver = gamepad 1, operator = gamepad 2, plus the coach and the human player. Use the real call-outs from [03](03-teleop.md#alliance-call-outs).

| Drill | Set-up | Target | From week |
|---|---|---|---|
| **DR1 Load cycle** | Exp 4 set-up | **≤ 20 s per load** (stretch 15 s) | W3 |
| **DR2 Precision volleys** | 3 spots per zone: head, left, right ([03](03-teleop.md#shooting-spots)) | **≥ 8/10 clean** from each spot | W3 |
| **DR3 "TIP!" switch** | Coach calls "TIP!" at random | Stop firing within 0.5 s; **operator presses Y within 1 s of "TIP!"**; wait outside the landing box until the balls land; at the other zone's spot within 6 s (est.); on arrival the operator light is not RED and the AIM line names the new zone | W3 |
| **DR4 Possession** | Pile of 6 POLLEN + 2 opponent-colour NECTAR + 1 own NECTAR | 20 pickups: **never 5 held, never an opponent NECTAR controlled**; no own NECTAR taken (phase 1) | W3 |
| **DR5 Spill discipline** | Threshold ball launched or dropped in | 10/10 with no falling ball touching the robot | W3 |
| **DR6 Endgame** | Timer starts at 0:25, robot in Zone B (then Zone A) | Final volley by 0:12 (Zone A: 0:15), then park at once: **in the LZ by 0:05**, 10/10 | W4 |
| **DR7 Under defence** | A second robot or a cart screens the shooting cone | Clean volley within 5 s of arriving; never pin (count "1-2", back off 2 ft) | W4 |
| **DR8 Human player** | 20 NECTAR entries with the coach's tip count | **20/20 land on the LZ tile first**; none early | W2 |
| **DR9 Transition** | 10 AUTO → TELEOP hand-offs, including blue set-ups and one A3 double-tip hand-off | No motion before the bells; TeleOp INIT Alliance line = our colour; pose and HIVE state correct (after a double tip the operator presses Y at START) | W2 |
| **DR10 Full match sims** | 2:30 on a timer, scored with the [01](01-game-and-scoring.md#2-points) table | 10 sims in W5: track tips, fouls, PARK, cycle times | W5 |

**Drill log**

| Date | Drill | Driver / operator | Trial | Time or result | Target met? | Notes |
|---|---|---|---|---|---|---|
| | | | | | | |

---

## Suggested use of robot time (4–8 h per week, est.)

| Week | Build and fix | Experiments | Software on the robot | Driving |
|---|---|---|---|---|
| W1 | 50% (inspection blockers, sensors, Pinpoint) | 30% (set-up checks, Exp 1, 2a) | 20% | – |
| W2 | 30% (turret, Limelight mount) | 20% (Exp 2b, 2c, 3) | 40% (Pedro, A0/A1, aim) | 10% |
| W3 | 20% (bounce-out fixes) | 10% | 30% (TELEOP automation) | 40% (DR1–DR5) |
| W4 | 20% | 30% (Exp 5, 6, 8; A2/A3 runs) | 20% | 30% (DR6–DR7) |
| W5 | 10% | 10% | 20% | 60% (DR10 match sims) |
| W6 | 10% (fixes only) | – | 10% (bug fixes only) | 80% |

---

## Sources

- **Manual TU05** (p.69, p.72–73, p.83; G402, G406, G418, R201): https://ftc-resources.firstinspires.org/ftc/game/manual
- **EFSG §12**: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **FAC 1.13, 2.3–2.9**: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
- **CAD** (field size, aim points, shot-map spots): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **MonaShores** (tip timing, spill distances; community): https://github.com/Mona-Shores-FTC-Robotics/biobuzz
- **CD-14343** (bounce-out test, tip pace; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
- **Chief Delphi flywheel thread** (k ≤ 0.5; "most teams 20–45%"): https://www.chiefdelphi.com/t/determine-flywheel-velocity-for-ball-exit-velocity/394940
- est. values (seed RPMs, speed windows, flywheel drops) come from our research team's ball-flight and flywheel models (2026-10-20).
