# FTC 12303 — BIOBUZZ strategy brief (source of truth for all deliverables)

Date: 2026-10-20. Research reports (cite them; they hold the details and sources):
`docs/research/` (in this repo)
R1-rules.md (+.verify.md), R2-field-geometry.md (+R2-field-geometry-data.json, CAD-derived coordinates — PREFER THESE),
R3-launch-physics.md, R4-community-meta.md, R5-hardware.md, R6-software.md, R7-robot-audit.md, R8-robot-v2.md (the robot as built;
R5 and R7 describe the earlier turret version and stay as history).
Where a `.verify.md` corrects a report, the correction wins.

## Team (from the coach, 2026-10-20)
High-school / mixed ages. Java in Android Studio. Driver + operator. 4–8 h robot time per week. First competition 6+ weeks out
(Michigan Metro Detroit league; League Tournament 12/4–5; FiM Round-3 registration opens 10/21 7am — flag to coach).
Has: goBILDA Pinpoint + odometry pods (update 10/21: the X/Y pods are already mounted; pod type and wiring to be confirmed with Check: Odometry), spare DC motors, colour/distance sensors, a HIVE (real or replica), official POLLEN/NECTAR.
NECTAR never tested (POLLEN only so far).
**Design decision: tune the robot as built** (decision record below). Everything below is a refinement; anything bigger is labelled OPTIONAL.

Robot (details and frame-by-frame evidence: `R8-robot-v2.md`): SWYFT Drive V2 mecanum (4 SWYFT Spike motors in-wheel, 12.7:1,
~6.4 ft/s), REV Control Hub + Expansion Hub (on a top rail, in red printed brackets), Limelight 3A (confirmed 3A on 10/21 — the 3G
would be illegal per R702), goBILDA Pinpoint + X/Y pods. Intake wheel at the FRONT: a full-width roller of black compliant star
wheels on a hinged arm that floats (no actuator), driven by a goBILDA Yellow Jacket DC motor through a large side gear/pulley.
Balls go under the front bar, up a clear perforated polycarbonate ramp, past two black fingers into a tunnel between two aluminium
U-channels. Carrier wheel: a black treaded wheel above the tunnel, driven by a DC motor (coach; he is not sure whether the white
servo next to the shooter motor drives it instead). Stopped, it holds the balls (it is the gate); spinning, it pushes one ball into
the shooting wheel (it is the feeder). Shooting wheel: one wheel at the top of the BACK beside a red-sleeved motor (assumed goBILDA
5203 6000 rpm 1:1; check the label), with a fixed exit angle. The shooter cannot turn: the robot aims by turning its back to the up
cell. No ball sensors are fitted. 7 motors (4 drive, shooting wheel, intake, carrier) of the 8 R503 allows; no servo is required.
Unknown: the purpose of the black L-shaped plate on the left side toward the back (ask the coach), the battery location, how many
balls the tunnel holds.

## Decision record
- **2026-10-26 — robot v2.** The team rebuilt the robot (the coach's video `VID_20261026_071756.mp4` and his answers;
  `R8-robot-v2.md`). It replaces the turret robot that R5 and R7 describe; that code stays in the git history up to commit
  `ffd62eb`. Decided:
  1. Fixed shooter out of the robot's BACK (`RobotConfig.SHOOTER_FACING_DEG = 180`). Every start spot puts the FRONT (intake)
     against its wall and every shooting pose puts the back toward the up cell (red S1: heading 0, front on the audience wall,
     back toward the HIVE). The robot aims by turning: driver RB aim-assist in TELEOP; in AUTO the robot turns itself during each
     volley. A ball is fed only with the heading inside an aim tolerance worked out from the robot-centre distance and the
     opening's width (1.5–4°); the 75° off-axis gate stays. Operator d-pad left/right = aim trim ±0.5° (left = the ball goes
     further left, seen from the shooter looking toward the cell). Shot distances (the AIM line, the shot table, the 30–92 in
     band) are measured from where the ball leaves the robot once it is aimed, so they do not change while the robot turns.
  2. The carrier wheel is the gate and the feeder: one device `carrier` (DC motor or continuous-rotation servo).
  3. The intake is a DC motor (`intake` configured as a Motor; a CR servo still works).
  4. No ball sensors fitted: all three stay OPTIONAL in the code and a MUST refinement on the robot. Without a mouth sensor the
     count starts at the preloads and drops by one per feed (confirmed or not), INIT shows a loud warning, and the drivers stop
     at 4 by eye and steer around NECTAR: RT stops at 4 only with the mouth (or slot-4) sensor, and an eject does not lower the
     count (the operator presses back once per ball ejected). New first experiment: tunnel capacity (if a 5th ball fits, add a
     mechanical stop so the tunnel holds 4).
  5. The Limelight 3A looks out of the back, pitched up about 35–40° (`CAMERA_YAW_OFFSET_DEG = 180`).
  6. The strategy stays: TIPS win, the AUTO ladder A0–A3, the two-zone TELEOP loop, the last-minute script, POLLEN only until
     NECTAR passes its tests. With the shooter at the back the loop is one turn per cycle (TELEOP item 8).
  7. The robot refinements are re-listed for this robot (Robot refinements, below).
  8. B2 (A2's second volley) stays at (−48, −12.75): with the placeholder exit point a ≈ 24.5 in shot, the one AUTO shooting pose
     below the 30–92 in band, until the shot map (plan 07 Exp 2b) decides it. If the short shot misses, B2 moves back toward
     (−55, −12.75) (≈ 31.5 in), which is only ≈ 1.6 in from the F-far FLOWER on an approach that drives toward it.
  9. A3 role 2 starts on S6 only (from S7 it would have to turn ~31° with its front against the far wall).
  10. `Tune: Flywheel` has a timed carrier push (RB, exactly `CARRIER_FEED_MS`) for the feed-time experiment. Experiment numbers
     and the week order follow plan 07 (`docs/plan/07-testing-and-practice.md`).

## The game in one paragraph
TIPS decide matches (20 pts each; early-season data: alliance with more tips won 98% of matches where tip counts differed (102 of 104);
2 tips ≈78% win, 3 ≈90%).
Official field calibration: the starting up-CELL already holds 3 own NECTAR, so ~3 launched POLLEN tip it; every later (empty) cell
needs ~8 POLLEN (7 must not tip) or 3 NECTAR + 3 POLLEN. The up-CELL opening faces OUTWARD toward its own end wall, tilted 30° up;
it can only be scored from the half of the field it faces (within ~40° of head-on) — so the shooting zone FLIPS after every tip.
A tipped cell's contents spill toward the wall it faced, landing ~3 s after the threshold ball (up to ~6 s), roughly X ±24..42 in from
centre (the zone you were just shooting from). G409: do not touch/catch spilled pieces before they hit something else — stay out of the
landing box until the balls have landed (usually ~3 s, up to ~6 s). Flowers are almost unused so far; a flower still holding its staged
POLLEN is worth ≈13 pts for one own NECTAR dropped in after 1:00 (15 if 4 POLLEN count, e.g. after we add one) — but only for robots
that can place NECTAR into a 4.0 in opening at 21.4 in.

## Field frame (use everywhere)
FTC frame for BIOBUZZ (R2 §1): origin field centre, **+X toward the AUDIENCE wall, +Y toward BLUE**, heading 0 = facing audience, CCW+.
A robot's heading is the way its FRONT (the intake) faces; the shooter points the other way.
Field is 141.3 in wall-to-wall (walls at ±70.67). Blue = red rotated 180°: (x, y, h) → (−x, −y, h+180). Not a mirror.
Red key points (inches): HIVE pivot (0, −12.75, 43.95). Red AUDIENCE cell up at start, aim point (+16.50, −12.74, 58.27), faces +X.
Red SCORING (far) cell aim point when up (−16.48, −12.74, 58.31), faces −X. Tags: red far 30–33, red audience 34–37,
blue audience 38–41, blue far 42–45. Red LOADING ZONE X[−46.60,−23.91] Y[−70.67,−59.10]. Red GARDEN X[68.10,70.67] Y[−70.10,−47.41]
(4 POLLEN at (69.3, −69.3/−66.4/−63.5/−60.6)). Red-side FLOWERS: F-red (+23.39, −68.04) on red wall; F-far (−68.04, −23.39) on far wall.
Start S1 (61.67, −12.75) heading 0 (front against the audience wall, back toward the HIVE) = straight in front of the red
audience opening: a ≈ 38 in shot, measured from where the ball leaves the shooter (about 7 in behind the robot centre:
`SHOOTER_FORWARD_IN`, to be measured; 45 in from the robot centre). S6 (−61.67, −10.1) heading 180 = mirror spot for the far
cell (A3 role 2's only start; ≈ 38 in shot). Park+LEAVE: two-robot slots CORNER (−51, −56) (the LZ end nearer the field
corner; the first robot to arrive takes it) and INNER (−27, −56); SOLO pose (−35.25, −55) only when the partner does not park in AUTO (all in the LZ, 5.7–6.7 in off the
wall — touching a wall at end of AUTO loses LEAVE, QA21). Every start spot is square to its wall, front against it.
Shooting zones: Zone A (state A, red audience cell up): around A2 (48, −12.75), cone ±40° about the +X axis through the cell.
Zone B (red far cell up): around B2 (−48, −12.75), 44 in from the LZ solo pose. Corridor between zones: y ≈ −40 via (30, −42) →
(−30, −40). From where the ball leaves, A2 and B2 are only ≈ 24.5 in shots, below the model's 30–92 in band: the head spots
(±55, −12.75), ≈ 31.5 in, are the default TELEOP spots. B2 stays A2's second-volley pose (the route test checks it against a
24 in floor) until the shot map decides it; if the short shot misses, move B2 back toward (−55, −12.75), only ≈ 1.6 in from the
F-far FLOWER on an approach that drives toward it (an overshoot touches it).
Under-HIVE lane (TELEOP only, centre |Y| ≤ 9.5, G409/G417 risk) — avoid in AUTO.

## Strategy: "Pollen tip engine" (phase 1), nectar/flowers as phase-3 options
### AUTO (red described; blue = rotate 180°). Ladder — build in order, never skip the lower rung's reliability bar (≥8/10 runs):
- **A0 Safe** (any start): drive off wall into our LZ park slot (INIT Park item). LEAVE 3 + PARK 5 = 8.
- **A1 Tip & Park** (primary, start S1): INIT holds 4 preloads; at START spin the flywheel and fire from S1 (from S2–S4 the robot first
  drives to the Zone A side spot (55, −37)); auto-aim (the robot's back on the cell: odometry bearing + Limelight tag bias), fire all 4
  (3 tip it; the 4th is bounce-out insurance and may shorten the dwell — weak evidence). Hold at the firing pose until the spill has
  landed (start-cell tags seen up then gone 1 s, or 6 s after the last shot), then drive (52,−40)→(30,−42)→(−30,−40)→LZ park slot.
  20 + 3 + 5 = 28.
  Alliance with a partner that LEAVEs+PARKs = 36 + SWARM RP (US AUTO max so far is 36; only ~7% of alliance-matches reach it).
- **A2 Tip, Reload, Volley** (stretch, tuned follower only): A1's volley (no hold), then sweep the 4 red GARDEN POLLEN (line along the
  audience wall, red corner), drive to Zone B (B2), fire into the (now up, empty) second cell, park. Alone that is 4 of 8 for tip 2.
- **A3 Alliance double-tip** (stretch, needs partner with a shooter; tuned robots only, needs a rehearsal): Robot 1 runs "A3 role 1"
  from S1: TIP 1, garden sweep, waits at the corridor end until 17 s, fires from (−52, −30). Robot 2 runs "A3 role 2" from S6 only
  (the end wall opposite our start), waits for tip 1, fires its 4 preloads into the second cell only if the tip is confirmed by 9 s, and is
  parked in the corner slot by ~16 s → 8 → TIP 2 in AUTO (40 pts of tips + LEAVE/PARK). No US alliance has done 2 AUTO tips yet. 12303
  can play either role (both are routines in the 12303 Auto INIT menu).
- Human player: a TIP in AUTO unlocks 1 NECTAR and the human may enter it during AUTO (QA11) — only matters once NECTAR is usable.
- AUTO selector in INIT (gamepad): alliance, start (S1–S7), routine (A0, A1, A2, A3 role 1, A3 role 2), park slot (INNER/CORNER/SOLO),
  start delay (0–15 s), preloads.

### TELEOP (2:00 → 1:00): the two-zone loop
1. Shoot from the zone the up cell faces; the robot turns its back onto the cell (the driver holds RB = aim-assist).
2. Each empty-cell tip needs 8 POLLEN = 2 robot loads (G407 caps a robot at 4). Alliance plan: both robots feed the same cell;
   "two loads = one tip". Call loads out loud ("4 in… 8, it's going").
3. After the threshold ball: stop firing (launching at a moving HIVE can disrupt the tip; dwell is 0.25–3.4 s); the operator presses
   "HIVE tipped" (Y) on the human player's "TIP!" (the camera cannot see our own tip from the zone we fired from); keep clear of the
   spill box until the balls have landed (usually ~3 s, up to ~6 s), then sweep the spill (it lands in your zone) up to 4 balls, then
   cross to the other zone and fire.
4. Ammo sources, in order: own spill (predictable, lands in your zone) → loose balls in your zone → garden POLLEN (either garden in
   TELEOP) → flower-bottom POLLEN (16 of 24 non-preload POLLEN start in flowers; only if the intake can pull through the 3.55 in
   retrieval opening — test it) → opponent spill.
5. Target pace: one 4-ball load every 15–20 s per robot ⇒ a tip every ~30–40 s per robot alone, ~20 s with a partner.
   Benchmarks: Saline (MI) winners ≈3.6–3.8 tips/alliance-match; "5 tips is a good benchmark for consistent early-season wins".
   12303 goal by League Tournament: 1 AUTO tip + 2–3 TELEOP tips contributed per match.
6. Defence: blocking shots is legal (QA2); keep moving, shoot from different spots in the cone, never pin > 3 s.
7. Possession discipline is a design requirement: count starts at 4 (preloads), intake auto-stops at 4, reject NECTAR at the mouth
   (always the opponent's; ours too in phase 1, ACCEPT_OWN_NECTAR = false) (no MOMENTARY exception, QA3/QA4/QA23; G407 MAJOR+YC if
   strategic; G408 per manual = VW, YC if strategic). Counting balls in and refusing NECTAR need the mouth sensor; without it the
   tunnel must hold no more than 4, the count drops by one per feed, RT does not stop at 4 by itself (only the mouth or slot-4
   sensor stops it), the drivers stop at 4 by eye and steer around NECTAR, and after an eject the operator presses back once per
   ball ejected.
8. One turn per cycle (shooter at the back, intake at the front): after a volley the spill lands behind the robot, between it and
   the HIVE. Once it has landed, turn around once, sweep it intake-first, keep driving to the other zone and arrive with the back
   already facing the new up cell (RB for the last few degrees).

### ENDGAME (1:00 → 0:00)
- 1:00 on the field timer (the chime is only a courtesy): human player enters all remaining NECTAR into the LZ (drop it, must hit the
  LZ tile first; may roll it toward a robot).
- Phase-1 (pollen-only robot): keep tipping. At ~0:12 fire everything on board into the up cell (2 pts each if it doesn't tip),
  then park; after a tipping volley, wait until the spill has landed or leave along red y ≤ -38 (blue y ≥ +38) first, because a straight drive from B2 to the LZ crosses the landing box (G409) (Zone B → LZ is 44 in ≈ 3 s; plan the last cycle so you finish in Zone B, the half next to our LZ: red far half, blue
  audience half). Both robots partially in LZ = 10.
- Phase-3 options if NECTAR tests pass: (a) NECTAR as tip accelerant: 3 NECTAR + 3 POLLEN tips an empty cell (6 balls instead of 8);
  (b) flowers: first NECTAR in a flower = +5; top-most NECTAR owns it (2 pts per element; staged-POLLEN flowers ≈13 pts; 15 if 4 POLLEN
  count, e.g. after adding one).
  Only pursue if the robot can place NECTAR into the 4.0 in opening at 21.4 in reliably.
- GARDEN: ignore except free pushes on the way to park (1 pt each).

## Robot refinements (tune the robot as built) — priority order
Must (rules/inspection): Limelight 3A (confirmed 10/21); legal main power switch (R603); robot signs; G407/G408 ball sensors
(colour/distance at the intake mouth + "4 in the tunnel" sensor + break-beam where balls enter the shooting wheel) and the software
auto-stop at 4; confirm the tunnel holds no more than 4 (add a mechanical stop if a 5th ball fits). Recommended (not an inspection
item, R605 only permits it): a listed grounding strap for ESD.
Tune: flywheel velocity PIDF + feed gating on measured speed (gate ±2–3% with a short dwell — 28 CPR encoder is coarse); a goBILDA
steel flywheel disc if one fits beside the shooting wheel (cuts the per-shot speed drop a lot; check our wheel's size and type
first); measure the fixed exit angle (240 fps video) and where the ball leaves the robot, then build the distance → RPM table;
carrier feed timing (`CARRIER_FEED_MS`: exactly one ball per push); Limelight on the back pitched ~35–40° up, one AprilTag pipeline
for both alliances with ID filter 30–45 (the code keeps our own 8), marker 82.55 mm, Full 3D, 640x480; intake motor direction and
speed; fight bounce-outs (44% in one team's fixed-hood test): a compliant lining where the ball is squeezed, consistent compression,
test spin.
Optional: a NECTAR path (the tunnel, carrier and shooting wheel must pass a 3.6 in ball); a hood adjustment for the exit angle.
Protect hubs/wiring, cradle the battery, guard the shooting wheel (G409, safety).
NECTAR: test through the intake, tunnel, carrier and shooting wheel once POLLEN cycles are reliable.

## Software plan (Android Studio, SDK 12.0)
Pedro Pathing 3 for AUTO paths (`com.pedropathing:revhub:3.0.1`, `com.pedropathing:tuning:1.0.1`, repo
`https://repo.dairy.foundation/releases/`); Pinpoint localizer. Plain-Java subsystems + enum state machines, single `Robot.update()`
with manual bulk caching. Fused aim: the robot turns its back to the active cell's aim point (odometry bearing − 180° + a low-pass
Limelight bias; the camera looks out of the back), and a ball is fed only with the heading inside the aim tolerance; HiveTracker
(starts in state A: red audience cell / blue far cell up; the operator's "HIVE tipped" (Y), pressed on every "TIP!", is the main
TELEOP signal; vision confirms a state only from the zone the up cell faces; Y within 3 s of a camera switch confirms it). Flywheel: hub
setVelocity PIDF first (F = 32767/maxTicksPerSec, P = 0.1F, I = 0.1P), distance→RPM interpolating table, ready = in tolerance for
3+ loops. Carrier: holds the balls and feeds one per push (it stops when a flywheel dip or the feed beam confirms the shot, or after
`CARRIER_FEED_MS`; the next push waits at least 0.15 s and until the flywheel is back at speed, whichever is longer); ball count
(starts at the preloads), auto-stop at 4, colour reject at the mouth (the sensors are optional; without a mouth sensor the count
drops by one per feed and is fixed by hand after an eject). AUTO selector in INIT; pose handoff AUTO→TELEOP (static).
TeleOp INIT must not move actuators (G403). Panels/FTC Dashboard and Pedro AutoTune must be out of the build at events (R704.D, QA26).
Controls: DRIVER (gamepad1) = field-centric drive, intake in/out, slow mode, aim-assist (hold RB: the robot turns until its back
points at the target), heading reset. OPERATOR (gamepad2) = shooting mode (spin-up + auto-aim) toggle, fire volley, aim trim
(d-pad left/right ±0.5°; left = the ball goes further left, seen from the shooter looking toward the cell), RPM trim, "HIVE
tipped" (Y; confirms a recent camera switch), manual aim, eject, count ±1. Rumble/LED cues: 4 loaded, at speed, target switched,
NECTAR refused, unconfirmed feed, 1:00, 0:20.

## Testing plan (experiment numbers as in plan 07; use the team's HIVE)
- **0a** Tunnel capacity, first: how many POLLEN fit with the carrier stopped (must be 4; add a stop if 5 fit).
- **0b** Carrier feed time: exactly one ball per push (`CARRIER_FEED_MS`, set with the `Tune: Flywheel` timed push).
- **1** Tip threshold on THEIR hive: count placed vs launched POLLEN to tip from (a) 3 NECTAR start, (b) empty; repeat 5×. Time
  the dwell.
- **2a** Where the ball leaves the shooter, the fixed exit angle and the speed ratio k (tape measure + 240 fps video).
- **2b** Shot map: 5 distances × 0/20/40° off-axis, 10 shots each, record made / bounce-out / miss and RPM → build the
  distance→RPM table; it also decides B2.
- **2c** Bounce-out check: 20 shots into an empty cell, 20 into one holding 3 NECTAR.
- **3** Flywheel recovery: RPM trace for a 4-ball volley, with and without a steel flywheel disc (if one fits).
- **4** Load-cycle timing: spill sweep → cross → volley (one turn per cycle); target ≤ 20 s per load.
- **5** FLOWER-bottom extraction (3.55 in tall retrieval opening): can the front roller pull POLLEN out? Yes → flower ammo
  becomes available.
- **6** NECTAR go/no-go through the intake, tunnel, carrier and shooting wheel.
- **7** AUTO reliability: 10 runs each rung; promote only at ≥ 8/10.
- **8** Intake pickup speed and the GARDEN sweep (before A2/A3).

## Six-week plan (≈30–45 robot hours)
- W1: inspection blockers, tunnel capacity (Exp 0a; a stop if a 5th ball fits), ball sensors, set-up checks (odometry pods with
  Check: Odometry, the flywheel motor and its ratio, where the ball leaves the wheel), flywheel control + gating, carrier feed
  timing (Exp 0b), tip threshold (Exp 1), exit point, exit angle and k (Exp 2a), starter code on robot.
- W2: Limelight on the back + aiming, Pedro tuning, then the aim-assist gains; shot map and bounce-out check (Exp 2b, 2c);
  flywheel recovery (Exp 3); A0/A1 reliable.
- W3: TELEOP automation (auto-aim, one-button volley, auto-stop at 4), driver drills, bounce-out fixes.
- W4: A2/A3 (tuned follower only), FLOWER extraction (Exp 5), NECTAR go/no-go (Exp 6), intake pickup speed (Exp 8).
- W5: full match sims with timing, scouting sheet, reliability, portfolio.
- W6: freeze; practice; checklists.
