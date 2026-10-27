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
| Tape marks on the floor | S1, S6, A-head (55, -12.75), B-head (-55, -12.75), A2 (48, -12.75) and B2 (-48, -12.75), the Zone A side spot (55, -37), the corner / inner / solo park slots ([02](02-autonomous.md#poses-red)), the shot-map spots below | | |
| Directions (**Check: Sensors**; try the carrier and flywheel with no balls first) | Each wheel alone rolls the robot forward (toward the intake); A = intake in (balls go in); B = carrier toward the shooting wheel, X = backward; Y = flywheel at 1500 rpm (a ball leaves out of the back). Flip `*_DIRECTION`, `INTAKE_DIRECTION`, `CARRIER_DIRECTION`, `FLYWHEEL_DIRECTION` in `RobotConfig` if not | | |
| Limelight tag heights (Check: Limelight, pipeline ID filter 30–45, 82.55 mm, Full 3D on; camera looking out of the back) | On our S1 (front against our start wall, back toward the HIVE) the up-CELL tags read ~49–50 in "UP"; from the calibration spot (36, -12.75) (blue (-36, 12.75)), back toward the HIVE, press back: the second CELL's tags read ~35–36 in "dn" and "vision says A". All heights off by the same factor → fix the marker size, not the pitch | | |
| Ball time at the mouth (once the mouth sensor is fitted; Check: Sensors: after START press RB, then hold A and pull 10 single POLLEN in) | `MOUTH_ONE_BALL_MAX_MS` = longest single-ball time + 30% (the screen shows the suggestion). Redo after any intake, motor or power change. Bench: with 3 counted, feed two touching POLLEN: the 2nd must come back out (BALLS 4, 4 inside) | | |
| **Measure on robot:** what drives the carrier wheel | A DC motor (configure `carrier` as a Motor) or the white servo by the shooter (configure that port as a Continuous Rotation Servo named `carrier`) | | |
| **Measure on robot:** shooting-wheel motor and drive | Motor label (5203 6000 rpm 1:1 assumed); on the motor shaft, geared or belted: count motor turns per wheel turn; `FLYWHEEL_TICKS_PER_REV` = 28 × that ratio. Wheel size and type | | |
| **Measure on robot:** where the ball leaves the shooter | Forward and left of the robot centre (negative forward = behind): `SHOOTER_FORWARD_IN`, `SHOOTER_LEFT_IN` (placeholders -7 / 0). What the ball is squeezed against, and the gap | | |
| **Measure on robot:** intake | Yellow Jacket label (rpm); teeth on the side gear/pulley pair; star-wheel diameter; how far the floating arm swings and whether it drops back onto the tiles | | |
| **Measure on robot:** Limelight mount | Looking out of the back, pitched up 35–40°: `CAMERA_FORWARD_IN`, `CAMERA_LEFT_IN`, `CAMERA_HEIGHT_IN`, `CAMERA_PITCH_DEG`, `CAMERA_YAW_OFFSET_DEG` (180) | | |
| **Measure on robot:** steel flywheel disc fit | Axial room beside a shooting-wheel face > disc thickness + screw heads ([05 §3.2](05-robot-tuning.md#32-add-a-steel-flywheel-disc-if-it-fits)) | | |
| **Measure on robot:** size through the intake arm's swing | Power off, arm swung by hand from its lowest to its highest point: inside 18 × 18 × 18 in at the start (R102) and 18 × 24 × 29 in at every point (R105, Fig 12-6); the black L-shaped plate counts too | | |
| **Ask the coach:** the black L-shaped plate | What it is for (left side, toward the back) | | |

### HIVE calibration (EFSG §12.3, FAC 2.3–2.6)

Place balls **gently against the back wall** of the up CELL (EFSG §12.2). Add or remove ballast washers on the screw at the CELL's opening end. Calibrate each CELL separately; changing one affects the other (EFSG §12).

| Test | Must happen | Result | Washers changed |
|---|---|---|---|
| 3 NECTAR + 1 POLLEN, **toss** in the 2nd POLLEN | **No tip** | | |
| 3 NECTAR + 2 POLLEN, **toss** in the 3rd POLLEN | **Tip** | | |
| 6 POLLEN, **toss** in the 7th | **No tip** | | |
| 7 POLLEN, **toss** in the 8th | **Tip** | | |

---

## Exp 0 — Tunnel capacity and carrier feed (do these first)

### 0a. Tunnel capacity

**Question:** how many POLLEN fit in the tunnel before the intake can't take another? It must be **no more than 4** (G407). Without a mouth sensor nothing else stops a 5th ball, so this comes before every other experiment.

**Procedure**
1. Carrier stopped (power off, or the robot in INIT). Push POLLEN in through the intake by hand, one at a time, until no more will go in. Count them. Repeat 3 times.
2. With power (**Check: Sensors**, hold A = intake in), pull POLLEN in with the intake until it can't take another. The carrier stays stopped. Count them. Repeat 3 times.
3. Watch the two black fingers at the tunnel entrance: does a ball roll back out? Does a 5th ball sit under the intake wheel or on the ramp? Can a ball rest anywhere outside the tunnel (on top of the robot)?
4. Mark where ball 4 sits in the tunnel: that is the place for the slot-4 sensor ([05 §2](05-robot-tuning.md#2-must-rules-and-inspection)).

| Trial | Method | Balls inside | A 5th ball stuck under the wheel or on the ramp? | A ball rolled back out? | Notes |
|---|---|---|---|---|---|
| 1–3 | by hand | | | | |
| 4–6 | intake (Check: Sensors A) | | | | |

**Decision:**
- Exactly 4 fit → done; note it for inspection and the drive team.
- 5 or more fit → **add a mechanical stop** so the tunnel holds 4, then repeat the test.
- Fewer than 4 fit → the robot can't carry a full load (A1 needs 4): fix the tunnel before anything else.

### 0b. Carrier feed timing

**Question:** how long must the carrier spin to push exactly **one** ball into the shooting wheel (`CARRIER_FEED_MS`, 400 ms until measured)?

**Procedure** (**Tune: Flywheel**)
1. Directions first (Check: Sensors): B pushes balls toward the shooting wheel, X backward; Y spins the flywheel so a ball leaves out of the back. Flip the directions in `RobotConfig` if not.
2. **Shot dip.** In Tune: Flywheel, A = spin at about 3,500 rpm. Load **2 POLLEN** and press **B** (one carrier feed: it stops as soon as the shot is confirmed). Exactly one ball must leave. Watch "last shot: drop / recovery": "drop" must update on every shot. If it never updates, the shot dip is not detected: lower `FLYWHEEL_SHOT_DIP_FRACTION` (or fit the feed beam).
3. **Feed time.** Press **Y** until the edit line shows `FEED_MS`; dpad up/down then changes `CARRIER_FEED_MS` in 10 ms steps. With 2 POLLEN inside, press **RB** for a **timed push**: the carrier runs for exactly `CARRIER_FEED_MS` and does not stop at the dip. That is the push the robot makes whenever no shot is confirmed. Find the shortest time that always pushes one ball out and the longest that never pushes the second, and copy the middle into `RobotConfig.CARRIER_FEED_MS` (a value changed in the OpMode lasts only until the app restarts).
4. Load 4 and press B four times: exactly one ball leaves per press.

| `CARRIER_FEED_MS` | RB timed pushes (2 balls each) | One ball left | Two balls left | None left | Dip seen (confirmed) / not | Notes |
|---|---|---|---|---|---|---|
| | 10 | | | | | |

**Decision:** set `CARRIER_FEED_MS` (and `CARRIER_FEED_POWER` if the push is too hard or too weak). Target: exactly one ball per press, 20/20. An unconfirmed feed then takes `CARRIER_FEED_MS` + 0.25 s + 0.15 s (0.8 s at 400 ms).

---

## Exp 1 — Tip threshold on our HIVE

**Question:** how many **launched** balls tip our calibrated HIVE, how long does the tip take, and where does the spill land?

**Procedure**
1. Calibrate (table above).
2. **(a) Start CELL:** 3 NECTAR against the back wall. The robot at S1 (front against the wall, back toward the HIVE) launches POLLEN **one at a time**, 4 s apart (hold RT so the robot keeps its back on the CELL). Count only balls that stay in: on **Test: Tip Counter** press **LB** for every ball that misses or bounces out (its "unconfirmed" line shows feeds that may not have launched a ball). The shot table is not measured yet: if shots land short or long, trim the RPM with dpad left/right until they go in, and note that RPM for Exp 2a/2b. Repeat 5 times.
3. **(b) Empty CELL:** launch **4-ball volleys** until it tips (on Test: Tip Counter, press RB again as soon as each ball has left: a press while a shot is still running joins that shot). Repeat 5 times.
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

## Exp 2 — Shot map and exit angle

### 2a. Exit angle, exit point and speed ratio k (do this first)
1. **Exit point:** tape-measure where the ball leaves the shooting wheel, from the robot centre: forward (negative = behind the centre) → `SHOOTER_FORWARD_IN`; left → `SHOOTER_LEFT_IN` (placeholders -7 and 0). Every shot distance the code uses is measured from this point, with the robot turned onto its aim. Also measure the shooting wheel's diameter D.
2. Film perpendicular to the shot at **240 fps**, in front of a backdrop with a 1 in grid. Log the flywheel RPM.
3. 3 wheel speeds (e.g. 3,000 / 3,500 / 4,000 rpm) × 10 shots each, in **Tune: Flywheel**: A spins the wheel at the target RPM (dpad left/right ±25 rpm; with Y on RPM, dpad up/down ±100), B feeds one ball.
4. From the first 10–20 frames after the ball leaves, measure the **exit angle** and the **exit speed v**.
5. Compute **k = v ÷ (π × D × RPM ÷ 60)**, with D in metres (0.072 for a 72 mm wheel): the ball's exit speed divided by the wheel's surface speed.
6. The exit angle is **fixed**: compare it with the 64–65° that the shot-table seeds and the 30–92 in band assume.

| RPM | Shot | Exit angle (°) | Exit speed v (m/s) | k | Notes |
|---|---|---|---|---|---|
| 3,000 | 1–10 | | | | |
| 3,500 | 1–10 | | | | |
| 4,000 | 1–10 | | | | |
| **Spread** | | σ angle target ≤ 1–1.5° | σ speed target ≤ 2–3% | average k = | |

(Targets and method come from our ball-flight model, est. For one wheel against a fixed hood, k can't exceed 0.5. A Chief Delphi poster guessed "most teams are between 20-45%" (FRC). Our model plans on 0.35–0.45.)

**Decisions:**
- Put the measured exit point into `SHOOTER_FORWARD_IN` / `SHOOTER_LEFT_IN`, set `SHOT_TABLE_RPM_SCALE = 0.40 ÷ k_measured`, and run the laptop tests: the route test re-checks every AUTO shot distance from the new exit point. A failure means a shooting pose has left the band. B2 is the first to go: once the ball leaves more than 7.5 in behind the robot centre, it drops under the test's 24 in floor. Re-decide that pose (B2: in 2b) instead of lowering the floor.
- Exit angle within a few degrees of 64–65°: keep the seed rows as the starting point for 2b. Far from it: the seeds and the 30–92 in band don't apply; build the table from 2b alone. Changing the angle (a hood adjustment) is OPTIONAL ([05 §4](05-robot-tuning.md#4-optional-bigger-changes-only-after-23-work)).
- Recompute the 2b spots with the measured exit point (it replaces the 7 in below).

### 2b. Shot map (builds the distance → RPM table)

The robot's **back** points at the CELL, and the shot distance d is measured from where the ball leaves the aimed robot (the distance on the AIM telemetry line). Red, Zone A, aiming at the audience CELL (16.50, -12.74): the robot **centre** goes at (16.5 + (d + 7)·cos a, -12.74 − (d + 7)·sin a), heading −a, where 7 in is the placeholder exit point behind the centre (replace it with the measured `SHOOTER_FORWARD_IN`). "–" means the turned robot would hit the audience wall: **head-on shots longer than about 38 in are impossible in Zone A** (CAD; the robot's front then touches the wall at S1). The Shot Map Logger records the real distance from odometry, so a spot that is an inch off still gives a good row.

| Shot distance d (shooter → aim point) | 0° off-axis (heading 0°) | 20° off-axis (heading -20°) | 40° off-axis (heading -40°) |
|---|---|---|---|
| 26 in | (49.5, -12.7) | (47.5, -24.0) | (41.8, -34.0) |
| 30 in | (53.5, -12.7) | (51.3, -25.4) | (44.8, -36.5) |
| 34 in | (57.5, -12.7) | (55.0, -26.8) | (47.9, -39.1) |
| 38 in | (61.5, -12.7) ≈ S1 | (58.8, -28.1), 0.35 in off the wall | (51.0, -41.7) |
| 44 in | – | – | (55.6, -45.5) |

**Procedure**
1. **Tune: Shot Map Logger**: drive the robot to each spot and hold **RT** (aim-assist) so it turns its back onto the CELL; a shot waits until it is aimed.
2. At each spot, start from the seed RPM below (A = flywheel on, dpad up/down ±50 rpm). Step the speed in 1–2% steps.
3. Shoot **10** at each step: **RB** fires one ball, then score it: **X** made, **B** bounced out, **LB** missed (RB waits until the last shot is scored).
4. Find the band where **≥ 8/10 go in cleanly**. The table value is the **middle of the band** (method from our ball-flight research).
5. Record makes, bounce-outs, and misses (short / long / left / right), plus the battery voltage.
6. Spot-check the other side (Y = -12.74 + (d + 7)·sin a, heading +a) with 5 shots at 20° and 40°.

**Seed RPMs** (est.: the code's starting table, a model of a 72 mm wheel with a 64–65° exit and k = 0.40; below 33 in it uses the 33 in value). Our wheel and exit angle are not measured, so they are only a place to start. **After 2a, multiply every RPM by 0.40 ÷ k_measured** (a lower k needs more RPM).

| Distance (in) | 26 | 30 | 34 | 38 | 44 |
|---|---|---|---|---|---|
| Seed RPM | 3,600 | 3,600 | 3,590 | 3,540 | 3,545 |
| Seed ticks/s (28 per rev) | 1,680 | 1,680 | 1,675 | 1,652 | 1,654 |

Notes: the model's speed window is only about ±3–5% (est.). Between 30 and 60 in the required speed hardly changes, so distance errors matter little there. The shot table covers 33–87 in from the model; replace its rows with the distances we really shoot from (about 24–41 in from where the ball leaves, with the placeholder exit point: the [03 shooting spots](03-teleop.md#shooting-spots) and the AUTO poses).

**Data sheet (one row per spot and RPM step)**

| d (in) | Angle | RPM | Made | Bounce-out | Short | Long | Left | Right | Battery V | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| | | | /10 | | | | | | | |

**Result: the table that goes into the code**

| d (in) | Band low RPM | Band high RPM | **Table RPM (middle)** | Made at table RPM |
|---|---|---|---|---|
| 26 | | | | /10 |
| 30 | | | | /10 |
| 34 | | | | /10 |
| 38 | | | | /10 |
| 44 | | | | /10 |

**Decision: B2.** A2's second volley fires from B2 (-48, -12.75), heading 180: with the placeholder exit point a 24.5 in shot, below the 30–92 in band (the one AUTO shooting pose there). The same shot on the Zone A side is A2 (48, -12.75), heading 0, so map A2 like the other spots (seed 3,600 rpm).
- A band with ≥ 8/10 clean: B2 stays, and its row goes into the table.
- No such band: move B2 back toward (-55, -12.75) (B-head, a 31.5 in shot). That spot passes the route test but is only about 1.6 in from the F-far FLOWER, and the approach drives toward it, so an overshoot touches it. Change `FieldConstants.ZONE_B_SHOOT_X`, run the laptop tests, and update [02 poses](02-autonomous.md#poses-red).

### 2c. Bounce-out check
At the table settings, shoot **20 into an empty CELL** and **20 into a CELL holding 3 NECTAR**.

| CELL | Shots | Went in | Bounced out | Bounce-out % |
|---|---|---|---|---|
| Empty | 20 | | | |
| 3 NECTAR inside | 20 | | | |

**Decision:** more than 5% bounce-outs → lower the entry speed, soften the lining where the ball is squeezed, or (OPTIONAL) change the exit angle a few degrees (our ball-flight research). Then retest. One team measured 44% with a solid hood (CD-14343).

---

## Exp 3 — Flywheel recovery

**Question:** how much speed does each shot cost, how fast does the wheel recover, and does a steel flywheel disc help (if one fits)?

**Procedure** (**Tune: Flywheel**, 4 POLLEN loaded): A = spin, then **LB** = a 4-feed volley; each feed waits until the flywheel is back at speed, as in a match. Tune: Flywheel logs time, target and measured RPM, battery voltage and the feed state every loop (≥ 50 Hz) to a CSV on the Control Hub while it spins. Fire 3 volleys of 4 in each case.

| Case | Battery V | Spin-up 0 → ready (s) | Drop per shot (%) | Recovery (s) | First → last ball (s) | Made /4 (×3) |
|---|---|---|---|---|---|---|
| Bare wheel, fresh battery (~12.8 V) | | | | | | |
| Bare wheel, used battery (~12.0 V) | | | | | | |
| + steel flywheel disc, fresh | | | | | | |
| + steel flywheel disc, used | | | | | | |

**What to expect (model, for a 72 mm wheel with the 60 mm disc):**
- Drop per POLLEN shot about **19% bare** and **11% with the steel flywheel**.
- Spin-up about 0.3–0.5 s bare, about 0.5 s with the flywheel.
- Full-power recovery 0.08–0.2 s; a real controller is 1.5–3× slower (our flywheel model; est.).

**Decision:** keep the steel flywheel if made shots go up and spin-up stays under 1 s. Each ball takes the carrier push (until the shot is confirmed); the next push waits at least 0.15 s (`MIN_SHOT_INTERVAL_MS`) and until the flywheel is back at speed, whichever takes longer; an unconfirmed feed takes 0.8 s. If "last shot: drop" never updates in Tune: Flywheel, the shot dip is not detected: lower `FLYWHEEL_SHOT_DIP_FRACTION` or fit the feed beam.

---

## Exp 4 — Load-cycle timing

**Setup:**
- Scatter 8 POLLEN where a Zone A spill lands: X 30–60, Y -30 to -5, mostly against the audience wall.
- The robot starts at A-head (55, -12.75), heading 0 (front toward the audience wall, back toward the HIVE), as after a Zone A volley.
- The HIVE is set so the far CELL is up.

**Run:** "GO" → turn around once → sweep 4 intake-first → cross by the corridor (y ≈ -40) → stop at B-head (-55, -12.75), back toward the far CELL → RB for the last few degrees → volley. Stop the watch when the 4th ball leaves. Count the big turns: the target is **one per cycle** ([03](03-teleop.md#the-two-zone-loop)).

| Trial | Sweep (s) | Cross (s) | Aim + volley (s) | **Total (s)** | Big turns | Made /4 | Notes |
|---|---|---|---|---|---|---|---|
| 1 | | | | | | | |
| 2 | | | | | | | |
| 3 | | | | | | | |
| 4 | | | | | | | |
| 5 | | | | | | | |

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
| 2. Tunnel and carrier | 10 NECTAR up the ramp, past the two fingers, into the tunnel, held by the stopped carrier and pushed to the shooting wheel: any jams? | | |
| 3. Shooting wheel | 10 launches from about 32–38 in (shot distance, from where the ball leaves), starting at the POLLEN RPM + 10% (NECTAR probably needs +5–20%, est.). Made, jams, motor stall, **ball damage** (gouges or tears are a G406/R201 problem) | | |
| 4. Mixed | 2 NECTAR + 2 POLLEN volley: does one setting handle both? | | |
| 5. FLOWER placing (only for FLOWER points) | 10 placements into a 4.0 in ring at 21.5 in (dropper or lob) | | |

- **GO for the tip accelerant** (phase 3a): steps 1–2 ≥ 9/10 with no stall over 1 s, no ball damage, step 3 ≥ 7/10 clean.
- **GO for FLOWERS** (phase 3b): step 5 ≥ 8/10.
- **NO-GO:** stay POLLEN-only: keep `ACCEPT_OWN_NECTAR = false` (with a mouth sensor the intake refuses all NECTAR at the mouth) and drivers keep steering around NECTAR.
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

**Question:** is the intake (a motor-driven star-wheel roller on a floating arm) fast enough, and which GARDEN sweep works ([02 poses](02-autonomous.md#poses-red))? Set the intake direction first (Exp 0a, Check: Sensors A).

| Test | 10 tries each | Picked | Missed | Stalled | Notes |
|---|---|---|---|---|---|
| Drive into a single POLLEN at 1 ft/s | | | | | |
| … at 2 ft/s | | | | | |
| … at 4 ft/s | | | | | |
| GARDEN sweep option 1 (face the wall, 3 s; not in the code) | balls in 3 s | | | | |
| GARDEN sweep option 2 (the code's sweep: along the wall, (60.5, -46) → (60.5, -60) at 0.3 speed) | balls in 3 s | | | | |

**Decisions:**
- Below 8/10 at 2 ft/s → check `INTAKE_IN_POWER`, the motor's speed and gearing, the star wheels' squeeze and whether the floating arm drops back onto the tiles ([05 §3.6](05-robot-tuning.md#36-intake-motor-direction-and-speed)).
- A2/A3 only if one sweep option gets **4 balls in ≤ 3 s, 8/10 times**. If the wheel's edge can't reach the GARDEN balls 1.4 in from the wall, adjust `GARDEN_SWEEP_X` and re-run the route test.
- Note how many balls were counted: without a mouth sensor nothing counts them, so AUTO's volley 2 feeds 4 times anyway.

---

## Driver drills

Driver = gamepad 1, operator = gamepad 2, plus the coach and the human player. Use the real call-outs from [03](03-teleop.md#alliance-call-outs).

| Drill | Set-up | Target | From week |
|---|---|---|---|
| **DR1 Load cycle** | Exp 4 set-up | **≤ 20 s per load** (stretch 15 s), one big turn per cycle | W3 |
| **DR2 Precision volleys** | 3 spots per zone: head, left, right ([03](03-teleop.md#shooting-spots)); the driver aims with RB | **≥ 8/10 clean** from each spot; RB settles on the target in < 1 s without hunting | W3 |
| **DR3 "TIP!" switch** | Coach calls "TIP!" at random | Stop firing within 0.5 s; **operator presses Y within 1 s of "TIP!"**; wait outside the landing box until the balls land; at the other zone's spot within 6 s (est.); on arrival the operator light is not RED and the AIM line names the new zone | W3 |
| **DR4 Possession** | Pile of 6 POLLEN + 2 opponent-colour NECTAR + 1 own NECTAR; run it without a mouth sensor, and again once one is fitted | 20 pickups: **never 5 held, never an opponent NECTAR controlled**; no own NECTAR taken (phase 1). Without the sensor the driver stops at 4 by eye | W3 |
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
| W1 | 50% (inspection blockers, tunnel stop if needed, ball sensors) | 30% (set-up checks, Exp 0, Exp 1, 2a) | 20% | – |
| W2 | 30% (Limelight on the back, carrier tweaks) | 20% (Exp 2b, 2c, 3) | 40% (Pedro, aim-assist gains, A0/A1) | 10% |
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
- **Robot as built** (tunnel, carrier, fixed rear shooter, unknowns to measure): [R8-robot-v2.md](../research/R8-robot-v2.md); tuning OpModes: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md` §5–6
- est. values (seed RPMs, speed windows, flywheel drops) come from our research team's ball-flight and flywheel models (2026-10-20).
