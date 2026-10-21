# 02 · Autonomous (AUTO, the first 30 seconds)

AUTO is a ladder of four routines. **Build them in order, and never use a rung in a match until it passes ≥ 8 of 10 practice runs** (see [promotion rule](#promotion-rule)). All poses are given for **red**. **Blue = rotate 180°** (see [blue poses](#blue-poses)).

Coordinates are inches in the field frame from [01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame): +X toward the audience, +Y toward blue, heading 0° = facing the audience, counter-clockwise positive. Source tags are explained in the [source key](00-master-plan.md#source-key). **est.** = our estimate; time it on the practice field.

**This page describes what the code does** (as of 2026-10-20: `config/FieldConstants.java`, `opmodes/auto/AutoLegs.java`, `AutoRoutine.java`, `AutoSelector.java`, `config/RobotConfig.java`; summary in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md` §8). If this page and the code disagree, the code is what the robot will do: fix the page, or change the code on purpose and re-run the laptop route test (`RouteSweepTest`, [06 §9](06-software.md#9-tests-tuning-and-logging)).

**Words that work for both alliances** (use them in the queue and on the field):
- **Our start wall** = the end wall behind S1 (red: audience wall; blue: far wall). **The end wall opposite our start** = red: far wall; blue: audience wall.
- **Start CELL** = our CELL that is up at the start (red: audience CELL, tags 34–37; blue: far CELL, tags 42–45). **Second CELL** = our other CELL, which comes up after TIP 1 (red: far, tags 30–33; blue: audience, tags 38–41).
- **Corner slot / inner slot / solo**: the park spots in our LOADING ZONE ([poses](#poses-red)). The corner slot is the LZ end nearer the field corner (red: far-wall end; blue: audience end).

---

## Rules that shape AUTO

- **Hands off.** From the countdown to the end of AUTO, nobody touches the robot or Driver Station except to press start, stop, or for safety (G401, M p.106). Everything is chosen in INIT.
- **Legal start** (G304, M p.104):
  - fully on our side (red: Y ≤ 0, columns A–C; blue: Y ≥ 0, columns D–F), not hanging over the wall;
  - touching the perimeter wall;
  - not touching a FLOWER and not in the LOADING ZONE (LZ);
  - inside the 18 in cube (R102), though preloads may stick out;
  - touching exactly 4 preload POLLEN;
  - **motionless after INIT**. Our AUTO INIT holds only positional servos (turret at centre, feeder gate closed, hood). The intake, spinners, wheels and flywheel stay unpowered until START.
- **No flywheel pre-spin.** All parts must be still at the start (R102, G304.H). Spin-up happens after START.
- **Stay on our side.** Disrupting the opponent's AUTO is a MAJOR foul per match, even when accidental, and crossing the centre line is "a risky gameplay strategy" (G402, M p.106). Each alliance has priority over the balls and FLOWERS on its side.
- **LEAVE** = not touching **any** wall when AUTO ends (QA21). **PARK** = at least partly in our LZ. A partner pushing you off the wall still counts (QA28).
- A tip that finishes during the 8 s transition still counts as an AUTO tip (M p.86).
- **G409 applies in AUTO**: don't let spilling balls hit the robot before they touch the floor (QA17). Still unanswered: QA35 asks whether a robot may wait with its intake running just outside the spill area.
- The human player may enter 1 NECTAR per tip of our HIVE, even during AUTO (QA11). In phase 1 (POLLEN only) the human holds it.
- An alignment jig for setting up the robot is allowed (G302 box, M p.103). Build one for S1 and S6.
- **Drive times on this page assume a TUNED path follower** (`pedro/Constants.java`: Foresight tuned, the 0.5 `maxPathSpeed` cap removed). The code ships with the cap, and at the cap every drive takes about **twice as long** (S1 → park ≈ 5–6 s, garden → Zone B ≈ 6.5–8 s). **Don't promote A2 or A3 before the follower is tuned.** A3 also needs a rehearsal with the partner.

---

## The ladder

| Rung | What it does | Our points | Needs | Gate |
|---|---|---|---|---|
| **A0 Safe** | Drive off the wall into our LZ park slot (the INIT Park choice) | 8 (LEAVE 3 + PARK 5) | Drive, Pinpoint odometry, Pedro paths | 10/10 before the first event |
| **A1 Tip & Park** (primary) | From S1, fire the 4 preloads, tip the start CELL, hold until the spill has landed, park | **28** (tip 20 + 3 + 5). With a partner that LEAVEs and PARKs: **36 + SWARM RP** | Indexer holding 4, flywheel gating, aim | ≥ 8/10 |
| **A2 Tip, Reload, Volley** (stretch) | A1's volley (no hold), then sweep our GARDEN POLLEN, fire them into the second CELL from B2, park | 28 in AUTO + 3–4 of the 8 balls needed for tip 2 | Fast enough intake, count sensor, **tuned follower** | ≥ 8/10 |
| **A3 Alliance double-tip** (stretch) | We run the **"A3 role 1"** routine (an A2 variant). A partner with a shooter runs role 2: it waits at S6 and fires its 4 into our second CELL | **56** for the alliance (2 tips + 2 LEAVE + 2 PARK) | A partner with a shooter; **both robots tuned**; a rehearsal | ≥ 8/10 together |

For scale: the best US alliance AUTO so far is **36**, and only about 7% of alliance-matches have scored that 36 (an AUTO tip plus SWARM locked in AUTO). No US alliance has made 2 AUTO tips (FTCScout, as of 2026-10-20).

---

## Poses (red)

Headings assume the shooter's "turret zero" points out of the robot's **front** (`RobotConfig.TURRET_CENTER_DEG = 0`; if ours points elsewhere, set it and the code adds the offset). All values are CAD-derived ([01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame)) with ±1 in field tolerance (M p.63). Clearances are for 18 × 18 in robots with the **real (rotated) footprint**, from the code's route test.

**Start spots.** Every start is **square to its wall**: the robot's back touches the wall. The older 151° (S2) and 31° (S7) were aim bearings, not placement headings: an 18 in robot turned about 30° cannot touch the wall at those centres.

| Name | X | Y | Heading | Used by | Notes |
|---|---|---|---|---|---|
| **S1** | 61.67 | -12.75 | 180° | A0, A1, A2, A3 role 1 | Back on our start wall, on our HIVE axis. 45.2 in to the aim point, dead ahead |
| S2 | 61.67 | -37.5 | 180° | A0; A1/A2/A3 role 1 via the side spot | 51.5 in, 29° off-axis (aim bearing 151°). The CELL is 28.7° off the turret centre, outside the as-built ±28°, so shoot-first routines first drive to the Zone A side spot. 0.9 in from the GARDEN tape |
| S3 | 45 | -61.67 | 90° | A0; A1/A2/A3 role 1 via the side spot | Back on our alliance wall, audience side. Shoot-first routines first drive straight off the wall to (45, -48) |
| S4 | 0 | -61.67 | 90° | A0; A1/A2/A3 role 1 via the side spot | Alliance wall centre, beside the HIVE. Straight off the wall to (0, -48) first |
| S5 | -61.67 | -61.67 | 0° | A0 only | Far/red corner, 6 in from the LZ |
| **S6** | -61.67 | -10.1 | 0° | A3 role 2 (and A0) | Back on the end wall opposite our start, on our HIVE axis. 45.3 in to the second-CELL aim point once it is up (aim bearing -3.3°). **Only 1.35 in from F-far** |
| S7 | -61.67 | -40.0 | 0° | A0 (A3 role 2 only with > ±32° turret travel) | 52.8 in; the second CELL is **31.1° off the turret centre**, outside the as-built ±28°, so role 2 cannot fire from here. The INIT screen shows a CHECK line for A3 role 2 at S7 |

**Shooting spots, waypoints and park slots** (the code drives exactly these; turning robots reach 12.7 in from their centre, so routes drive straight off walls and turn only in open space):

| Name | X | Y | Heading | Used by | Notes |
|---|---|---|---|---|---|
| Zone A side spot | 55 | -37 | 147.8° | A1/A2/A3 role 1 from S2–S4 | 45.5 in, 32° off-axis. 6.8 in outside the tip-1 landing box, 2.9 in from a partner on S1, 3.3 in off the audience wall |
| Spill escape | 52 | -40 | – | park drives from Zone A | Keeps the robot about 2 in outside the tip-1 landing box from S1. From the Zone A side spot the robot still faces 147.8° until (30, -42), and one corner passes only about 0.2 in from the box (A1 leaves only after its spill hold) |
| Corridor points | 30, -42 and -30, -40 | | – | all routes between the zones | Point A is 2 in lower so a robot still turning stays ≥ 2 in outside the tip-1 box from S1 and the garden (from the Zone A side spot: 1.6 in while turning, 0.2 in on the leg before). ≥ 6.1 in from the HIVE feet and legs at heading 180° (A0, A1); a robot turning on this leg passes closer (A2: ≥ 3.7 in). 3 in from a robot in the corner slot |
| Garden approach | 52, -40 → 51, -50 | | turns 180° → -90° between them | A2, A3 role 1 | Turns away from the audience wall and stays ≥ 1.95 in outside the tip-1 landing box while turning (A2/A3 role 1 leave right after the volley, while the spill may still be falling). ≈ 49 in from S1 |
| Garden sweep | 60.5, -46 → 60.5, -60 | | intake leading toward -Y | A2, A3 role 1 | 0.3 speed, intake on. Gets all 4 only if the roller reaches within 1.4 in of the wall (`GARDEN_SWEEP_X`, TODO-MEASURE; [07 Exp 8](07-testing-and-practice.md#exp-8--intake-pickup-speed)). "Option 1" (face the wall and drive into the line) is not in the code: test only |
| A2 (Zone A centre) | 48 | -12.75 | 180° | TELEOP only | 31.5 in to the aim point. Its front edge is about 3 in **inside** the tip-1 landing box: not used in AUTO |
| B2 (Zone B centre) | -48 | -12.75 | 0° | A2 volley 2 | 31.5 in to the second-CELL aim point. Its front edge is about 3 in inside the TIP-2 landing box: fine for A2 (4 balls cannot tip the empty second CELL), but move off after any tipping volley here. If the shot map says 31.5 in is too close, change `ZONE_B_SHOOT_X` (e.g. B-head -55) and re-run the route test |
| A3 role-1 wait point | -30 | -40 | 0° (square) | A3 role 1 | Waits here until 17 s from START. 2.3 in from role 2's whole park drive, 3 in from a corner-slot robot |
| Inner lane | -31 | -35.5 | – | A3 role 1 (to its spot); every INNER park from the far half | ≥ 2.4 in from a corner-slot robot; 2.3–3.1 in from the HIVE foot |
| Role-1 spot | -52 | -30 | 25.9° | A3 role 1 volley 2 | 39.5 in, 26° off-axis. Turned footprint: 0.87 in outside the TIP-2 landing box, 1.6 in from F-far, 1.1 in from a robot still at S6, 5.0 in from the corner-slot robot. **A robot still at S7 overlaps it by about 8 in** |
| Role-1 exit | -51.5, -36.5 → -31, -36.5 | | turns square on the first piece | A3 role 1 | Drops straight down, then runs along the bottom edge of the TIP-2 landing box (skims the nominal box by 0.5 in instead of crossing it 7.7 in deep); 1.5 in above the corner-slot robot |
| Far-wall escape | -50 | (same y) | – | leaving S6 only | First leg straight along +X: F-far is 1.35 in away, and a diagonal first move clips it |
| **Corner slot** | -51 | -56 | square* | first robot to park; A3 role 2 always | The LZ end nearer the field corner (red: far-wall end). 4.6 in into the LZ along the wall, 5.3 in out from it on an event field, 5.67 in off the wall |
| **Inner slot** | -27 | -56 | square* | second robot; A3 role 1 always; INIT default | 12.1 in along the LZ, 5.3 in out. Robots in the two slots end 6 in apart |
| **Solo** | -35.25 | -55 | square* | only if the partner does **not** park in AUTO | The middle of the LZ: 4.3–4.9 in inside it, 6.7 in off the red wall. It touches both slots |

\* Park heading = the robot's current heading rounded to the nearest 90°, so two parked robots can't clip each other.

Aim points: red audience CELL **(16.50, -12.74, 58.27)**; red far CELL **(-16.48, -12.74, 58.31)** (CAD; SDK).

**Park slot rule.** Set **Park** in the INIT menu (default INNER). If the partner also parks in AUTO, agree in the queue who arrives first: **the first robot takes the corner slot**, the second the inner slot. Why: a robot in the inner slot reaches up to y = -47 (blue +47), and a robot driving our corridor reaches down to y = -49, so nobody can get past an inner-slot robot to the corner slot. If the partner does not park in AUTO, any value works (INNER is clearly inside the zone and off the wall). INNER from S5 or S7 crosses the corner slot, and the INIT screen says so.

### Setting up with the jig

- **S1:** robot's back against **our start wall** (red: audience wall; blue: far wall), on our HIVE axis. Its alliance-wall-side edge (the robot's left side) sits **1.75 in** from the tile seam (red: Y = -23.5, between columns B and C; blue: Y = +23.5, between D and E); the robot then spans red Y -21.75 to -3.75 / blue Y +3.75 to +21.75. The same jig works on blue turned end-for-end. (On blue, the audience-wall spot on our HIVE axis is **not** S1: a robot there touches the F-aud FLOWER.)
- **S6:** robot's back against the end wall opposite our start (red: far wall; blue: audience wall). Its alliance-wall-side edge sits **4.4 in** from the same seam (red B|C at Y = -23.5; blue D|E at Y = +23.5), and its centre-line side is 1.1 in from the centre seam (Y = 0). **Look at the FLOWER beside it (red: F-far; blue: F-aud): if the robot touches it, the start is illegal (G304.D).** Adjust within the 1.35 in gap. S7 is a role-2 backup only once the turret travels more than ±32°; otherwise drop A3 for this match.
- Tiles vary by about ±0.5 in (EFSG §7). Re-check on every new field.

---

## A0 — Safe (any start)

LEAVE 3 + PARK 5 = **8**. Run it whenever a higher rung isn't trusted on that day. The robot drives to the **Park** slot chosen in INIT.

| Start | Path (red) | Length | Time (est.): tuned / at the 0.5 cap |
|---|---|---|---|
| S1 | S1 → (52, -40) → (30, -42) → (-30, -40) → slot | ≈ 127 in (inner, solo), 137 in (corner) | 3.5–4.5 s / 5–6 s |
| S2 | S2 → (52, -40) → (30, -42) → (-30, -40) → slot | ≈ 108 in (inner), 118 in (corner) | 3–4 s / 4.5–5.5 s |
| S3 | S3 → (30, -42) → (-30, -40) → slot | ≈ 101 in (inner), 111 in (corner) | 3–4 s / 4.5–5.5 s |
| S4 | S4 → (0, -48) (straight off the wall) → slot | ≈ 42 in (inner), 65 in (corner) | ≈ 2 s / 2.5–3.5 s |
| S5 | S5 → slot (inner goes via the lane (-31, -35.5) and crosses the corner slot) | ≈ 12 in (corner), 27 in (solo), 61 in (inner) | ≈ 1 s / 1.5 s (corner) |
| S6 | S6 → (-50, -10.1) → slot (inner via the lane (-31, -35.5)). **First leg straight along +X**: F-far is 1.35 in away | ≈ 58 in (corner, solo), 64 in (inner) | ≈ 2.5 s / 3–3.5 s |
| S7 | S7 → slot (inner goes via the lane and crosses the corner slot) | ≈ 19 in (corner), 30 in (solo), 52 in (inner) | ≈ 1.5 s / 2 s (corner) |

- If the partner also parks in AUTO, set Park to the agreed slot: **first to arrive takes the corner slot.** Solo only when the partner does not park in AUTO.
- Use the start delay (0–15 s) if the partner's AUTO needs the space first.
- **No odometry** (Pinpoint missing or Pedro not started): every path is skipped, so A0 scores **0** (the robot stays on the wall: no LEAVE). There is no "blind" drive-off routine. From S1, run A1 instead: it still fires from the start spot (tip 20).

---

## A1 — Tip & Park (primary, start S1)

### INIT checklist
- [ ] Robot placed at S1 with the jig, back touching our start wall (red: audience wall; blue: far wall).
- [ ] 4 POLLEN inside the robot.
- [ ] Selector: **Alliance = THIS match's colour, set first** (it shows RED after any app restart, e.g. a battery swap; otherwise the previous AUTO's value: read the top line `##### RED ALLIANCE #####` / `##### BLUE ALLIANCE #####` aloud). Then Start **S1** · Routine **A1** · Park (the agreed slot) · Delay **0** · Preloads **4**, then lock with A. The values shown run whether or not they are locked.
- [ ] INIT screen: the "Place robot at" line matches our colour (red (61.7, -12.8), heading 180; blue (-61.7, 12.8), heading 0). That line and the POSE line only **repeat the selection**: the jig is the placement check. `BALLS 4/4`; the HIVE line shows "vision: N tags" with N > 0 (our start-CELL tags: red 34–37, blue 42–45) and the AIM bias is near 0; no `CHECK:`, `WARN:` or `!!` lines.
- [ ] Nothing moves after INIT (G304.H).
- [ ] At the transition, read the TeleOp INIT Alliance line too and fix it with gamepad 1 X / B if it is wrong ([03](03-teleop.md#start-of-teleop-the-8-second-transition)).

### Timeline (from S1, as coded)

| Clock (s) | Robot | Notes / sources |
|---|---|---|
| 0.0 | START. The flywheel spins up to the table speed for ~45 in; the turret aims (odometry bearing 180° plus the Limelight trim). The volley is requested at once; the indexer stages the first ball (0.3 s) during the spin-up | No pre-spin allowed (R102, G304.H) |
| ≈ 0.3–0.6 | First ball out once the flywheel is "ready" (within max(2.5%, 60 rpm) of target for 3 loops) | Spin-up ≈ 0.3–0.5 s bare; ≈ 0.5 s with the 60 mm steel flywheel (est.) |
| ≈ 0.6–3.5 | **Fire all 4 as one volley.** Each ball waits for the wheel to be back at speed: at least **0.33 s** apart (gate 0.18 s + interval 0.15 s) plus flywheel recovery when the shot is confirmed (feed beam or speed dip), **0.73 s** apart when a feed is not confirmed. 4 balls ≈ 1.5 s (+ recovery) confirmed, ≈ 3.1 s if no feed is confirmed | The best robot at Saline fired 0.14–0.25 s apart (MonaShores); ours can't, as coded |
| ≈ 2–4 | Last ball arrives (0.4–0.6 s flight, est.). The 3rd ball should tip it (EFSG §12.3). After ≥ 3 balls the code **assumes TIP 1** (HIVE state B; with a feed beam fitted only beam-confirmed shots count) and keeps vision from undoing that for 5 s | Dwell 0.25–3.4 s, then the swing |
| ≈ 2–4 → ≈ 3–9 | **Hold at S1, intake off**, turret centred (the camera watches the start CELL). S1 is 10.7 in outside the tip-1 landing box. **Leave once the start-CELL tags (red 34–37, blue 42–45) were seen up and then gone for 1.0 s, or 6 s after the last shot**, whichever comes first. If the start CELL is still seen up at the end, the code records "no TIP" (state A) | Swing ≈ 0.65–0.9 s (up to 2.2 s); spill on the tiles ≈ 3 s after the threshold ball, up to ~6 s (MonaShores; R2 verify F15) |
| ≈ 3–9 → ≈ 9–15 | Drive S1 → (52, -40) → (30, -42) → (-30, -40) → our Park slot, ending square. Intake off. Settled spill balls in the way just get pushed: bulldozing is not CONTROL (G407 box D) | ≈ 127 in (corner 137 in): 3.5–4.5 s tuned, 5–6 s at the 0.5 cap |
| ≤ 15 | **Parked: partly in the LZ, not touching any wall** | LEAVE 3 + PARK 5 (QA21) |
| to 30 | Holds still. Saves pose, ball count, alliance and HIVE state every loop for TELEOP | |
| **Deadline** | At **23 s** after START (`AUTO_PARK_DEADLINE_SEC`) the code drops everything and drives to the Park slot. A1 from S1 never reaches it | |

**From S2–S4:** the robot first drives to the Zone A side spot (55, -37), heading 147.8° (blue (-55, 37), -32.2°); from S3/S4 it first drives straight off the alliance wall to (x, -48). Then the same volley, hold and park.

### Why fire all 4 when 3 tip it
- Insurance: bounce-outs are common. One team measured 44% with a fixed solid hood (CD-14343).
- At Saline, volleys that went past the threshold **may** have shortened the wait before the HIVE moved (MonaShores; weak evidence, test it in [07 Exp 1](07-testing-and-practice.md#exp-1--tip-threshold-on-our-hive)).
- Fire them as one volley so the balls arrive while the HIVE is still dwelling (typically ~2 s). Launching at a moving HIVE can stop the tip (M p.87), although the manual calls that unlikely in normal play.

---

## A2 — Tip, Reload, Volley (stretch)

Needs a **tuned follower**: at the shipped 0.5 cap the whole routine takes about 17–20 s.

| Step | What the code does | Tuned (est.) | 0.5 cap (est.) |
|---|---|---|---|
| Volley 1 | As A1 (S1, or the side spot from S2–S4). After ≥ 3 balls it assumes TIP 1. **No hold** | ≈ 2–3.5 s | same |
| To the garden | (52, -40) → (51, -50) → sweep start (60.5, -46), turning 180° → -90° between the two points, away from the wall. The turning robot stays ≥ 1.95 in outside the tip-1 landing box (it leaves while the spill may still be falling, G409) | ≈ 1.5–2 s (≈ 49 in) | ≈ 2.5 s |
| Sweep | Intake on. (60.5, -46) → (60.5, -60) at 0.3 speed. Ends at 4 balls, or 0.6 s after the sweep path ends. Our GARDEN loses 1 pt per ball taken unless refilled (M p.89) | ≈ 1.5–2 s | ≈ 2 s |
| To Zone B | (60.5, -60) → (30, -42) → (-30, -40) → **B2 (-48, -12.75), heading 0°** (blue (48, 12.75), 180°). Spins up on the way. Turns -90° → 0° on the corridor: ≥ 2.27 in from the tip-1 box, ≥ 3.7 in from the HIVE feet and legs | ≈ 4–5.5 s (≈ 128 in) | ≈ 6.5–8 s |
| Volley 2 | Fires into the second CELL **only if the HIVE tracker says it is up** (the assumed TIP 1, or vision); if not, it waits up to 1 s for vision, then keeps the balls. Cycles the gate at least 4 times (a garden ball may have passed outside the mouth sensor) | ≈ 1.5–3.5 s | same |
| Park | B2 → our Park slot: inner via the lane (-31, -35.5) ≈ 49 in, corner ≈ 43 in, solo ≈ 44 in | ≈ 1.5 s | ≈ 2–2.5 s |
| **Deadline** | 23 s after START: the code drops everything and parks, wherever it is | | |

- 3–4 of the 8 balls needed for tip 2. A2 alone cannot tip the second CELL (4 balls; 7 must not tip).
- **NECTAR:** in phase 1 the intake refuses **all** NECTAR at the mouth (`RobotConfig.ACCEPT_OWN_NECTAR = false`). Our own NECTAR is legal to hold, but our hood can't launch it yet. The sensor reacts on contact, so routes avoid NECTAR rather than rely on it.
- Not in the code: picking up settled spill POLLEN in Zone A. If it is ever added, pick up only at red Y ≤ -2 (blue Y ≥ +2): the other alliance has priority on its side in AUTO (G402).
- If the garden sweep is too slow with the servo intake, A2 stays a practice item. That decision belongs to the intake test ([07 Exp 8](07-testing-and-practice.md#exp-8--intake-pickup-speed)).

---

## A3 — Alliance double-tip (stretch, needs a partner with a shooter)

Two roles. 12303 can play **either** one: they are the routines **"A3 role 1"** and **"A3 role 2"** in the 12303 Auto INIT menu. **A3 is for tuned robots only** (tuned follower, no 0.5 cap) **and needs a rehearsal with the partner.** The time gates below count from START; a start delay does not move them.

| Role | Start | Job | Parks in |
|---|---|---|---|
| **A3 role 1 "tipper"** | S1 | A1's volley (TIP 1), garden sweep (**must** reach 4), waits at the corridor end until 17 s, fires from the role-1 spot → TIP 2 | Inner slot (always) |
| **A3 role 2 "waiter"** | S6 (S7 only with > ±32° turret travel) | Waits for the second CELL (red: far, tags 30–33; blue: audience, tags 38–41) to come up, fires 4 into it, **only if TIP 1 is confirmed by 9 s**; whatever happens it leaves for the corner slot by **13 s** (`A3_ROLE2_LEAVE_BY_SEC`: a stalled volley or a start delay is cut) | Corner slot (always) |

**Why the garden sweep must reach 4:** 4 + 4 = 8 tips the CELL. **7 must not tip** (EFSG §12.3), so 3 garden balls would leave the second CELL one short. The code has no extra sweep time: with 3 balls it fires 3 (7 in the CELL, no tip 2), and those balls still help tip 2 in TELEOP.

### A3 role 1 (tipper) timeline

Clock times at the shipped 0.5 cap. Tuned, role 1 reaches the wait point earlier and still waits until 17 s.

| Clock (s) | Robot | Notes |
|---|---|---|
| 0 → ≈ 2–3.5 | A1's volley at S1 (TIP 1 assumed after ≥ 3 balls). No hold | |
| → ≈ 6–9 | Garden approach and sweep, as A2 | |
| → ≈ 11–13 | (60.5, -60) → (30, -42) → **wait point (-30, -40)**, square heading (red 0°, blue 180°) | 2.3 in from role 2's whole park drive, 3 in from a corner-slot robot |
| until **17.0** | **Waits** (`A3_ROLE1_ZONE_B_NOT_BEFORE_SEC`), so role 2 is parked before role 1 enters its end of the field | |
| 17 → ≈ 18.5 | Lane (-31, -35.5) → role-1 spot (-52, -30), heading 25.9° (blue (52, 30), -154.1°). Spins up on the way | 39.5 in, 26° off-axis |
| ≈ 18.5 → 20–22.5 | Volley 2: 4 → 8 in the second CELL → **TIP 2** (only if the tracker says the second CELL is up; otherwise it waits up to 1 s for vision, then keeps the balls) | It still counts as AUTO if it finishes in the transition (M p.86) |
| ≈ 20–22.5 → 22–25.5 | **Leaves at once**: turns square while dropping to (-51.5, -36.5), runs east to (-31, -36.5) along the bottom edge of the TIP-2 landing box (red X -42 … -24, Y -28 … +2), then the inner slot (-27, -56) | ≈ 47 in. The exit skims the nominal box by 0.5 in. The earliest spilled balls reach the floor ≈ 1.2 s after the threshold ball, usually ≈ 3 s (MonaShores; est.); measure the TIP-2 first touch in [07 Exp 1](07-testing-and-practice.md#exp-1--tip-threshold-on-our-hive)(b) before promoting A3 |
| **Deadline** | 23 s: a volley still running is cut and the robot parks | Parked by ≈ 22–25.5 s at the cap |

**TIP 2 is not recorded by the code**: the HIVE state stays B. TeleOp INIT then says "AUTO says HIVE state B … If the OTHER cell is up (e.g. an A3 double tip), press Y after START": after a double tip the **operator presses Y at TELEOP START** (the start CELL is up again).

### A3 role 2 (waiter) timeline

| Clock (s) | Robot | Notes |
|---|---|---|
| INIT | At S6: red (-61.67, -10.1, 0°), back on the far wall; blue (61.67, 10.1, 180°), back on the audience wall. **Check the FLOWER beside it** (red F-far, blue F-aud; 1.35 in nominal gap). If it touches, adjust within the gap; S7 only with > ±32° turret travel, otherwise skip A3 | G304.D |
| 0 → trigger | Pre-aims at the second CELL (still down) and spins up. Before tip 1 the second CELL's tags face away from us, so we can't see them | |
| Trigger | The second-CELL tags (red 30–33, blue 38–41) are seen up for 0.5 s without a break (`HIVE_VISION_CONFIRM_MS`), or the HIVE tracker already says state B | **Never fire at a moving HIVE** (M p.87, QA12) |
| Trigger by **9 s** | 1 s TIP pause (retarget) + 0.3 s staging, then fires 4 into the second CELL (≈ 1.5 s confirmed, ≈ 3.1 s unconfirmed) | `A3_ROLE2_LATEST_FIRE_SEC` = 9 s |
| Trigger at 9–10 s | Records state B but **keeps the preloads** | |
| No trigger by **10 s** | **Keeps the preloads** (`A3_ROLE2_TIP_WAIT_TIMEOUT_SEC` = 10 s). `A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = true` would fire at 9 s anyway: use it only after the partner's tip timing is rehearsed | |
| After the volley (≤ ≈ 13.4 s) | S6 → (-50, -10.1), straight along +X first → corner slot (-51, -56) | ≈ 57.6 in. Parked by ≈ 16–16.5 s at the 0.5 cap |

### Who is where, when (both roles, at the 0.5 cap)

| Clock (s) | Role 1 tipper (from S1) | Role 2 waiter (from S6) |
|---|---|---|
| 0–3.5 | Fires 4 at the start CELL | Waits, pre-aimed at the second CELL |
| ≈ 3–9 | Garden approach and sweep | TIP 1 completes at ≈ 2.5–8.5 s; fires 4 if it is confirmed by 9 s |
| ≈ 9–13 | Corridor to the wait point (-30, -40) | Volley done by ≈ 13.4 s; drives S6 → (-50, -10.1) → corner slot |
| ≈ 13–17 | Waits at (-30, -40) | Parked by ≈ 16–16.5 s |
| 17–22.5 | Lane → role-1 spot → volley 2 → tip 2 | Parked |
| ≈ 20–25.5 | Exit along the TIP-2 box edge → inner slot | Parked |

Clearances (rotated footprints, 18 × 18 in robots, from the route test): 2.3 in between role 1 at the wait point and role 2's whole park drive; ≥ 2.4 in from the corner-slot robot on the lane; at the role-1 spot 0.87 in from the TIP-2 box, 1.1 in from a robot still at S6, 1.6 in from F-far, 5.0 in from the corner-slot robot; 1.5 in above the corner-slot robot on the exit; 6 in between the parked robots. A robot still at S7 would overlap the role-1 spot by about 8 in. **Rehearse with the partner's real dimensions before the first match.**

Points if everything works: 20 + 20 + 3 + 3 + 5 + 5 = **56**.

---

## Coordination scripts

Say the words in [brackets] for our colour this match.

### A3 (say this in the queue; about 60 seconds)
1. "Does your robot have a shooter and an AUTO that can wait at S6, on the end wall **opposite** our start [red: far wall / blue: audience wall], and fire its 4 preloads into our **second CELL** (the one that comes up after our tip, facing you) **after** it comes up? It only works if both robots have a tuned path follower and we rehearse it." If not, skip A3: we run A1 or A2, and you do LEAVE + PARK.
2. "We start at S1, our back on the [red: audience / blue: far] wall on our HIVE axis, and tip our start CELL with our preloads. The tip usually finishes about 3–9 s after START (allow up to 10 s). You start at S6: back on the [red: far / blue: audience] wall, centred on our HIVE, with your centre-line edge about 1 in from the centre line. Check you are not touching the FLOWER beside you [red: F-far / blue: F-aud]."
3. "Fire **only after our CELL has swung**: when you see our second CELL's tags come up [red: 30–33 / blue: 38–41]. Without vision, not before about 10 s."
4. "After you fire, park in the **corner slot** of our LOADING ZONE [red: its far-wall end / blue: its audience end], 5–6 in off the wall, **by 16 s**. We wait at the end of our corridor [red: y = -40 / blue: y = +40] until 17 s, then shoot from your end of the field and park in the inner slot."
5. "Don't touch any wall at the end of AUTO (no LEAVE, QA21). Stay on our side (G402)."
6. If they are the better tipper, swap: 12303 runs A3 role 2 (our code fires only after it sees their tip, by 9 s, leaves by 13 s and parks in the corner slot).

### SWARM RP (every match)
1. "What's your AUTO? Does it end **off the wall** and **partly in our LOADING ZONE**?"
2. If yes: "Who parks first? The first robot takes the **corner slot** [red: the far-wall end / blue: the audience end of our LOADING ZONE]; the second takes the **inner slot**." Set our Park line to match (a partner with a short LEAVE + PARK usually arrives first; our A1 parks at about 9–15 s). Both parked plus both LEAVE = **16, SWARM locked in AUTO**.
3. If they only LEAVE: we LEAVE + AUTO PARK (8) + they LEAVE (3) = 11, so **at least one robot must PARK at the end** (+5 = 16); both parking gives 21 and some slack.
4. If they can't move in AUTO: we need 13 (LEAVE + AUTO PARK + end PARK) and they must end-PARK (5) = 18. Remind them at the coach's "0:25" call. Our Park line can then be anything (INNER is the default; solo only when they don't park in AUTO).
5. The coach confirms the plan out loud at the field before placing the robots.

---

## Failure handling

What the code does (it has no "fire anyway", no jam retry and no blind drive-off).

| Problem | How the robot knows | What it does |
|---|---|---|
| Flywheel never reaches "ready" (for example, an unplugged encoder) | The volley waits on "flywheel" | Nothing is fired. The volley gives up 5 s after the robot has stopped (`AUTO_VOLLEY_TIMEOUT_SEC`) and the robot moves on with its balls (no tip). Check the encoder cable in the pit |
| No Limelight lock | No valid own tag | Aims from odometry alone (the jig makes the S1 pose exact); the vision bias stays 0 |
| Ball won't leave (jam) | No beam break and no flywheel dip within 0.4 s of a feed | The ball is counted as fired anyway (`COUNT_UNCONFIRMED_SHOTS`) and the next one is fed: no reverse, no retry. After 4 such feeds (≈ 3 s) the count is 0 and the volley ends. Without a feed beam the code then assumes the tip (≥ 3 fired → state B) and parks with the jammed balls inside; with a feed beam only confirmed shots count toward the tip. Coach: if the FEED line showed unconfirmed feeds in AUTO, the operator corrects the count (left-stick click = +1 per stuck ball) and the HIVE state (Y) at TELEOP start |
| No tip after 4 balls | A1 only: the start-CELL tags (red 34–37, blue 42–45) are still seen up at the end of the spill hold | Parks anyway (still 8) and hands TELEOP state A ("no TIP"). A2 and A3 role 1 don't hold, so they keep the assumed state B; the operator checks the HIVE at TELEOP start ([03](03-teleop.md)) |
| Target CELL faces away (wrong start or zone) | The aim solver | The volley is skipped and the balls are kept |
| Running late | Match clock | **One park deadline: 23 s after START** (`AUTO_PARK_DEADLINE_SEC`), from anywhere |
| Partner or ball blocks the path | Path timeout = leg length ÷ (0.6 × cruise) + 1.5 s (cruise 26.2 in/s at the 0.5 cap): S1 → park ≈ 9.6 s, garden → B2 ≈ 9.6 s, garden approach ≈ 4.6 s | A timed-out path stops and the robot holds where it is; the next step starts from there |
| Robot not on the selected spot | Nothing on the robot detects it: the INIT POSE line only repeats the selected start | Jig and tape check before INIT. Weak cross-check: own tags seen and AIM bias near 0 in INIT |
| Odometry dead (Pinpoint missing / Pedro not started) | INIT shows `!! NO ODOMETRY: paths are skipped (no LEAVE/PARK)` and a `WARN:` line | Paths are skipped, so A0 scores 0. From S1 run A1, which still fires from the start spot (tip 20, no LEAVE/PARK) |
| Ball count wrong at INIT | `BALLS n/4` line (= the Preloads value; a ball seen by the slot-4 sensor forces 4) | Set Preloads to 4. INIT shows `CHECK: Preloads n: a match starts with exactly 4 (G304.G)` otherwise |
| Wrong alliance selected | Top INIT line `##### RED ALLIANCE #####`; vision shows 0 own tags | Set Alliance first, every match (RED after an app restart) |
| Start not valid for the routine | `CHECK: !! INVALID START Sx FOR <ROUTINE>: ROBOT WILL NOT MOVE` | Fix Start or Routine: otherwise the robot does not move at all |
| Robot touching a wall at 30 s | Nothing on the robot detects it | Park slots are 5.67–6.7 in off the wall; check this in the 10-run test (QA21) |
| Opponent crosses into our side | – | Don't react. The coach notes the match number for the Question Box (T301, M p.149) |

---

## Promotion rule

- Run each rung **10 times in a row** on the practice field: same jig, a charged battery, and the HIVE calibrated to EFSG §12.3 ([07](07-testing-and-practice.md#exp-1--tip-threshold-on-our-hive)).
- A run is a **success** only if every item for that rung happened: tip (A1+), LEAVE, PARK, no wall contact at 30 s, nothing on the opponent's side, no ball bouncing off the robot during a spill.
- **Promote at ≥ 8/10.** Keep the lower rung selectable at all times. A2 and A3 only with a tuned follower; A3 only after a rehearsal with the partner.
- **Demote** a rung that fails 2 of its last 5 match runs, until the cause is fixed and it passes 8/10 again.
- Log every run in [07 Exp 7](07-testing-and-practice.md#exp-7--auto-reliability).

---

## INIT selector (what the drive team sets)

Gamepad 1 menu during INIT of the one AUTO OpMode, **12303 Auto**. The coach may press INIT and start (G423), but **drivers** handle the gamepads.

| Line | Choices | First run after the app starts |
|---|---|---|
| Alliance | RED, BLUE | RED |
| Start | S1–S7 | S1 |
| Routine | A0, A1, A2, A3 role 1, A3 role 2 | A1 |
| Park | INNER, CORNER, SOLO (A3 role 1 always parks INNER and role 2 always CORNER: the line says so and is ignored) | INNER |
| Delay | 0–15 s, in 1 s steps | 0 |
| Preloads | 0–4 | 4 (always 4 in a match, G304.G) |

- **Every line is remembered until the Robot Controller app restarts**; a battery swap or reboot puts all of them back to the first-run values. **Every match: set Alliance to THIS match's colour first** (the top line reads `##### RED ALLIANCE #####  must be THIS match's colour (RED after an app restart)`), then check every line.
- D-pad up/down = line, left/right = value, **A = lock/unlock** (a toggle that only stops edits; B does nothing). **The values shown run whether or not they are locked.** When locked the screen shows `>>> LOCKED <<<` under the lines.
- Changing Routine moves Start to that routine's home spot (S1 for A1, A2 and A3 role 1; S6 for A3 role 2; A0 keeps Start). Change Start afterwards if needed.
- Valid starts: A0 any; A1, A2 and A3 role 1: S1–S4; A3 role 2: S6 (S7 is accepted with a CHECK warning). Any other combination shows `CHECK: !! INVALID START Sx FOR <ROUTINE>: ROBOT WILL NOT MOVE`, and the robot does not move.
- Other `CHECK:` lines: "A3 role 2 cannot shoot from S7: the second cell is 31 deg off the turret centre (limits -28/+28). Use S6, or skip A3"; "Park INNER from S5/S7 crosses the CORNER slot: use CORNER unless the partner parks after us"; "Park SOLO touches both slots: only if the partner does not park in AUTO"; "0 preloads: nothing to shoot"; "Preloads n: a match starts with exactly 4 (G304.G)"; the S6 FLOWER warning (red F-far, blue F-aud); "A2/A3 needs a TUNED path follower … and, for A3, a rehearsal with the partner".
- Under the menu: `Place robot at (x, y), heading h = front faces … wall (square to the wall)` and "(the POSE line below only repeats this selection - check the placement with the jig)". It is **not** a placement check. With no Pinpoint: `!! NO ODOMETRY: paths are skipped (no LEAVE/PARK). From S1, A1 still fires from the start spot`. Before tuning: `NOTE: Pedro Foresight not tuned yet … paths capped at half speed`.
- AUTO INIT holds only the positional servos (turret centre, feeder gate closed, hood); the intake, spinners, wheels and flywheel stay unpowered until START. The selected alliance is saved to `/sdcard/FIRST/12303_alliance.txt` on every INIT change and at START, so TeleOp starts on the same colour even after an app restart.
- Exact buttons are in the code README: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`.

---

## Blue poses

**Rule: (x, y, heading) → (-x, -y, heading + 180°).** Don't mirror. The code applies this one function to every red pose.

| Name | Red | Blue | Blue: back against |
|---|---|---|---|
| S1 | (61.67, -12.75, 180°) | (-61.67, 12.75, 0°) | far wall (our start wall), on our HIVE axis |
| S2 | (61.67, -37.5, 180°) | (-61.67, 37.5, 0°) | far wall, column E |
| S3 | (45, -61.67, 90°) | (-45, 61.67, -90°) | blue wall, far side |
| S4 | (0, -61.67, 90°) | (0, 61.67, -90°) | blue wall, centre |
| S5 | (-61.67, -61.67, 0°) | (61.67, 61.67, 180°) | audience/blue corner |
| S6 | (-61.67, -10.1, 0°) | (61.67, 10.1, 180°) | audience wall, on our HIVE axis, beside F-aud |
| S7 | (-61.67, -40.0, 0°) | (61.67, 40.0, 180°) | audience wall, column E |
| Zone A side spot | (55, -37, 147.8°) | (-55, 37, -32.2°) | |
| Spill escape | (52, -40) | (-52, 40) | |
| Corridor points | (30, -42), (-30, -40) | (-30, 42), (30, 40) | |
| Garden approach | (52, -40) → (51, -50) | (-52, 40) → (-51, 50) | |
| Garden sweep | (60.5, -46) → (60.5, -60) | (-60.5, 46) → (-60.5, 60) | |
| B2 | (-48, -12.75, 0°) | (48, 12.75, 180°) | |
| A3 role-1 wait point | (-30, -40, 0°) | (30, 40, 180°) | |
| Inner lane | (-31, -35.5) | (31, 35.5) | |
| Role-1 spot | (-52, -30, 25.9°) | (52, 30, -154.1°) | |
| Role-1 exit | (-51.5, -36.5) → (-31, -36.5) | (51.5, 36.5) → (31, 36.5) | |
| Far-wall escape (from S6) | (-50, y) | (50, y) | |
| Corner slot / inner slot / solo | (-51, -56) / (-27, -56) / (-35.25, -55) | (51, 56) / (27, 56) / (35.25, 55) | |
| Start-CELL aim point | (16.50, -12.74, 58.27) | (-16.50, 12.76, 58.27) | |
| Second-CELL aim point | (-16.48, -12.74, 58.31) | (16.48, 12.76, 58.31) | |
| Own tags | start CELL 34–37, second CELL 30–33 | start CELL 42–45, second CELL 38–41 | |

---

## Sources

- **Manual TU05** (G302, G304, G401–G403, G407, G409, R102; p.86–87, 103–106): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA11, QA12, QA17, QA21, QA28, QA35 (pending), https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **EFSG §7, §12.3** (tiles, tip calibration): https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **CAD** (all poses, clearances and path lengths; derived by our research): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **SDK v12.0** (cluster origin = CELL opening centre): https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md
- **Code** (routes, timings, selector, as of 2026-10-20): `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` (`config/FieldConstants.java`, `config/RobotConfig.java`, `opmodes/auto/`), checked by `TeamCode/src/test/.../RouteSweepTest`
- **FTCScout** (US AUTO statistics): https://ftcscout.org
- **MonaShores** (tip timing, volley spacing; community): https://github.com/Mona-Shores-FTC-Robotics/biobuzz
- **R2 verify F15** (spill up to ~6 s): [R2-field-geometry.verify.md](../research/R2-field-geometry.verify.md)
- **CD-14343** (44% bounce-outs; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
