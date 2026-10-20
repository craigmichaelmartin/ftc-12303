# FTC 12303 — BIOBUZZ strategy brief (source of truth for all deliverables)

Date: 2026-10-20. Research reports (cite them; they hold the details and sources):
`docs/research/` (in this repo)
R1-rules.md (+.verify.md), R2-field-geometry.md (+R2-field-geometry-data.json, CAD-derived coordinates — PREFER THESE),
R3-launch-physics.md, R4-community-meta.md, R5-hardware.md, R6-software.md, R7-robot-audit.md.
Where a `.verify.md` corrects a report, the correction wins.

## Team (from the coach, 2026-10-20)
High-school / mixed ages. Java in Android Studio. Driver + operator. 4–8 h robot time per week. First competition 6+ weeks out
(Michigan Metro Detroit league; League Tournament 12/4–5; FiM Round-3 registration opens 10/21 7am — flag to coach).
Has: goBILDA Pinpoint + odometry pods, spare DC motors, colour/distance sensors, a HIVE (real or replica), official POLLEN/NECTAR.
Robot works today: drives, shooter launches POLLEN. NECTAR never tested (pollen only so far).
**Design decision: KEEP the current design and tune it.** Everything below is a refinement; anything bigger is labelled OPTIONAL.

Robot: SWYFT Drive V2 mecanum (4 SWYFT Spike motors in-wheel, 12.7:1, ~6.4 ft/s), REV Control Hub + Expansion Hub, Limelight
(must be a 3A — 3G is illegal per R702), turret on a large ring gear turned by a goBILDA Dual Mode Servo + 60T pinion (~55–75° travel
as built, no feedback), single 72 mm Hogback flywheel direct-driven by goBILDA 5203 6000 rpm 1:1 under a 3D-printed C hood,
full-width star/compliant-wheel roller intake driven by a Dual Mode Servo through gears, servo "internal spinners" (indexer) feeding
up through the hollow turret centre.

## The game in one paragraph
TIPS decide matches (20 pts each; early-season data: alliance with more tips won 98% of matches; 2 tips ≈78% win, 3 ≈90%).
Official field calibration: the starting up-CELL already holds 3 own NECTAR, so ~3 launched POLLEN tip it; every later (empty) cell
needs ~8 POLLEN (7 must not tip) or 3 NECTAR + 3 POLLEN. The up-CELL opening faces OUTWARD toward its own end wall, tilted 30° up;
it can only be scored from the half of the field it faces (within ~40° of head-on) — so the shooting zone FLIPS after every tip.
A tipped cell's contents spill toward the wall it faced, landing ~3 s after the threshold ball, roughly X ±24..42 in from centre
(the zone you were just shooting from). G409: do not touch/catch spilled pieces before they hit something else — stay out of the
landing box for ~3 s. Flowers are almost unused so far; a flower still holding staged POLLEN is worth 13–15 pts for one own NECTAR
dropped in after 1:00 — but only for robots that can place NECTAR into a 4.0 in opening at 21.4 in.

## Field frame (use everywhere)
FTC frame for BIOBUZZ (R2 §1): origin field centre, **+X toward the AUDIENCE wall, +Y toward BLUE**, heading 0 = facing audience, CCW+.
Field is 141.3 in wall-to-wall (walls at ±70.67). Blue = red rotated 180°: (x, y, h) → (−x, −y, h+180). Not a mirror.
Red key points (inches): HIVE pivot (0, −12.75, 43.95). Red AUDIENCE cell up at start, aim point (+16.50, −12.74, 58.27), faces +X.
Red SCORING (far) cell aim point when up (−16.48, −12.74, 58.31), faces −X. Tags: red far 30–33, red audience 34–37,
blue audience 38–41, blue far 42–45. Red LOADING ZONE X[−46.60,−23.91] Y[−70.67,−59.10]. Red GARDEN X[68.10,70.67] Y[−70.10,−47.41]
(4 POLLEN at (69.3, −69.3/−66.4/−63.5/−60.6)). Red-side FLOWERS: F-red (+23.39, −68.04) on red wall; F-far (−68.04, −23.39) on far wall.
Start S1 (61.67, −12.75) heading 180 = 45 in straight in front of the red audience opening. S6 (−61.67, −10.1) heading 0 = mirror
spot for the far cell. Park+LEAVE pose (−35.25, −55) (in LZ, 6.7 in off the wall — touching a wall at end of AUTO loses LEAVE, QA21).
Shooting zones: Zone A (state A, red audience cell up): around A2 (48, −12.75), cone ±40° about the +X axis through the cell.
Zone B (red far cell up): around B2 (−48, −12.75), 44 in from the LZ park pose. Corridor between zones: y ≈ −40 via (±30, −40).
Under-HIVE lane (TELEOP only, centre |Y| ≤ 9.5, G409/G417 risk) — avoid in AUTO.

## Strategy: "Pollen tip engine" (phase 1), nectar/flowers as phase-3 options
### AUTO (red described; blue = rotate 180°). Ladder — build in order, never skip the lower rung's reliability bar (≥8/10 runs):
- **A0 Safe** (any start): drive off wall into LZ park pose. LEAVE 3 + PARK 5 = 8.
- **A1 Tip & Park** (primary, start S1): INIT holds 4 preloads; at START spin flywheel during a short move to A2-ish or fire from S1;
  auto-aim (odometry bearing + Limelight tag bias), fire all 4 (3 tip it, the 4th shortens the dwell). Wait ~3 s clear of the spill
  box, then drive the corridor (30,−40)→(−30,−40)→LZ park. 20 + 3 + 5 = 28. Alliance with a partner that LEAVEs+PARKs = 36 + SWARM RP
  (US AUTO max so far is 36; only ~7% of alliances reach it).
- **A2 Tip, Reload, Volley** (stretch): A1, then sweep the 4 red GARDEN POLLEN (line along the audience wall, red corner) — and/or settled
  spill POLLEN — drive to Zone B, fire into the (now up, empty) far cell, park. Alone that is 4 of 8 for tip 2.
- **A3 Alliance double-tip** (stretch, needs partner with a shooter): Robot 1 runs A1 from S1. Robot 2 starts at S6 (far wall),
  waits for tip 1, fires its 4 preloads into the far cell. Robot 1 brings the 4 garden POLLEN to Zone B and fires → 8 → TIP 2 in AUTO
  (40 pts of tips + LEAVE/PARK). No US alliance has done 2 AUTO tips yet. 12303 should be able to play either role (OpMode per role).
- Human player: a TIP in AUTO unlocks 1 NECTAR and the human may enter it during AUTO (QA11) — only matters once NECTAR is usable.
- AUTO selector in INIT (gamepad): alliance, start (S1/S2/S6/other), routine (A0–A3), start delay.

### TELEOP (2:00 → 1:00): the two-zone loop
1. Shoot from the zone the up cell faces (turret aims; chassis gives coarse heading because turret travel is limited).
2. Each empty-cell tip needs 8 POLLEN = 2 robot loads (G407 caps a robot at 4). Alliance plan: both robots feed the same cell;
   "two loads = one tip". Call loads out loud ("4 in… 8, it's going").
3. After the threshold ball: stop firing (launching at a moving HIVE can disrupt the tip; dwell is 0.25–3.4 s), keep clear of the spill
   box for ~3 s, then sweep the spill (it lands in your zone) up to 4 balls, then cross to the other zone and fire.
4. Ammo sources, in order: own spill (predictable, lands in your zone) → loose balls in your zone → garden POLLEN (either garden in
   TELEOP) → flower-bottom POLLEN (16 of 24 non-preload POLLEN start in flowers; only if the intake can pull through the 3.55 in
   retrieval opening — test it) → opponent spill.
5. Target pace: one 4-ball load every 15–20 s per robot ⇒ a tip every ~30–40 s per robot alone, ~20 s with a partner.
   Benchmarks: Saline (MI) winners ≈3.6–3.8 tips/alliance-match; "5 tips is a good benchmark for consistent early-season wins".
   12303 goal by League Tournament: 1 AUTO tip + 2–3 TELEOP tips contributed per match.
6. Defence: blocking shots is legal (QA2); keep moving, use the turret to shoot from different spots in the cone, never pin > 3 s.
7. Possession discipline is a design requirement: count starts at 4 (preloads), intake auto-stops at 4, reject opponent NECTAR at the
   mouth (no MOMENTARY exception, QA3/QA4/QA23; G407 MAJOR+YC if strategic; G408 per manual = VW, YC if strategic).

### ENDGAME (1:00 → 0:00)
- 1:00 chime: human player enters all remaining NECTAR into the LZ (drop it, must hit the LZ tile first; may roll it toward a robot).
- Phase-1 (pollen-only robot): keep tipping. At ~0:12 fire everything on board into the up cell (2 pts each if it doesn't tip),
  then park (Zone B → LZ is 44 in ≈ 3 s; plan the last cycle so you finish in Zone B on red). Both robots partially in LZ = 10.
- Phase-3 options if NECTAR tests pass: (a) NECTAR as tip accelerant: 3 NECTAR + 3 POLLEN tips an empty cell (6 balls instead of 8);
  (b) flowers: first NECTAR in a flower = +5; top-most NECTAR owns it (2 pts per element; staged-POLLEN flowers ≈13–15 pts).
  Only pursue if the robot can place NECTAR into the 4.0 in opening at 21.4 in reliably.
- GARDEN: ignore except free pushes on the way to park (1 pt each).

## Robot refinements (keep the design) — priority order
Must (rules/inspection): confirm Limelight 3A; legal main power switch (R603); robot signs; grounding strap; G407/G408 sensors
(colour/distance at intake mouth + "slot 4 full" + break-beam at shooter feed) and software auto-stop at 4.
Tune: flywheel velocity PIDF + feed gating on measured speed (gate ±2–3% with a short dwell — 28 CPR encoder is coarse);
add goBILDA 60 mm steel flywheel (551 g·cm², made for the 72 mm Hogback; cuts per-shot speed drop a lot); set hood for ~64–65° actual
ball exit angle; fight bounce-outs (44% in one team's fixed-hood test): compliant/foam hood, consistent compression, test spin.
Turret: swap the turret servo for a goBILDA 5-turn Dual Mode Servo (same footprint, 360°+ turret travel) OR add a REV Through Bore
Encoder; add a home/zero reference and hard stops; until then aim coarse with chassis heading + fine with turret.
Intake: power servos at 6 V via REV Servo Hub / goBILDA Servo Power Injector (hub ports are 5 V and share 2 A per pair);
OPTIONAL: move the intake to a spare DC motor (5203 1150 rpm) — same roller, much more power.
Mount Pinpoint + 2 pods; Limelight on the turret pitched ~35–40° up, AprilTag pipeline filtered to own 8 tags, 640x480.
Protect hubs/wiring, cradle the battery, guard the turret's open centre and the flywheel (G409, safety).
NECTAR: test through intake/hood once POLLEN cycles are reliable (hood gap is fixed for 2.8 in; 3.6 in needs compliance).

## Software plan (Android Studio, SDK 12.0)
Pedro Pathing 3 for AUTO paths (`com.pedropathing:revhub:3.0.1`, `com.pedropathing:tuning:1.0.1`, repo
`https://repo.dairy.foundation/releases/`); Pinpoint localizer. Plain-Java subsystems + enum state machines, single `Robot.update()`
with manual bulk caching. Fused turret aim: odometry bearing to the active cell's aim point + low-pass Limelight tx bias; HiveTracker
(starts state A for red / far-up for blue, toggles on detected or operator-confirmed tip, operator override). Flywheel: hub
setVelocity PIDF first (F = 32767/maxTicksPerSec, P = 0.1F, I = 0.1P), distance→RPM interpolating table, ready = in tolerance for
3+ loops. Indexer: ball count (starts 4), auto-stop, colour reject. AUTO selector in INIT; pose handoff AUTO→TELEOP (static).
TeleOp INIT must not move actuators (G403). Panels/FTC Dashboard must be disabled at events (R704.D, QA26).
Controls: DRIVER (gamepad1) = field-centric drive, intake in/out, slow mode, heading reset. OPERATOR (gamepad2) = spin-up/auto-aim toggle,
fire volley, turret trim, RPM trim, "hive tipped" toggle / target-cell override, eject. Rumble/LED cues: 4 loaded, at speed, 1:00, 0:20.

## Testing plan (first experiments, use the team's HIVE)
1. Tip threshold on THEIR hive: count placed vs launched POLLEN to tip from (a) 3 NECTAR start, (b) empty; repeat 5×. Time the dwell.
2. Shot map: 5 distances × ±0/20/40° off-axis, 10 shots each, record made / bounce-out / miss and RPM → build the distance→RPM table.
3. Flywheel recovery: RPM trace for a 4-ball volley, with and without the steel flywheel.
4. Cycle timing drills: spill sweep → cross → volley; target ≤ 20 s per load.
5. Intake into a flower retrieval opening (3.55 in tall): can the front roller pull POLLEN out? Yes → flower ammo becomes available.
6. NECTAR go/no-go through intake, indexer, hood.
7. AUTO reliability: 10 runs each rung; promote only at ≥ 8/10.

## Six-week plan (≈30–45 robot hours)
W1: inspection blockers, sensors, flywheel control + gating, tip-threshold + shot-map tests, Pinpoint mounted, starter code on robot.
W2: turret feedback/range fix + calibration, Limelight mount + aiming, A0/A1 reliable, Pedro tuning.
W3: TELEOP automation (auto-aim, one-button volley, auto-stop at 4), driver drills, bounce-out fixes.
W4: A2/A3, NECTAR experiments (go/no-go), flower-extraction test.
W5: full match sims with timing, scouting sheet, reliability, portfolio.
W6: freeze; practice; checklists.
