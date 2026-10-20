# 02 · Autonomous (AUTO, the first 30 seconds)

AUTO is a ladder of four routines. **Build them in order, and never use a rung in a match until it passes ≥ 8 of 10 practice runs** (see [promotion rule](#promotion-rule)). All poses are given for **red**. **Blue = rotate 180°** (see [blue poses](#blue-poses)).

Coordinates are inches in the field frame from [01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame): +X toward the audience, +Y toward blue, heading 0° = facing the audience, counter-clockwise positive. Source tags are explained in the [source key](00-master-plan.md#source-key). **est.** = our estimate; time it on the practice field.

The code keeps its own copy of these poses in `TeamCode/.../config/FieldConstants.java`. If the code and this page disagree, the practice field decides, and then both get updated.

---

## Rules that shape AUTO

- **Hands off.** From the countdown to the end of AUTO, nobody touches the robot or Driver Station except to press start, stop, or for safety (G401, M p.106). Everything is chosen in INIT.
- **Legal start** (G304, M p.104):
  - fully on our side (red: Y ≤ 0, columns A–C), not hanging over the wall;
  - touching the perimeter wall;
  - not touching a FLOWER and not in the LOADING ZONE (LZ);
  - inside the 18 in cube (R102), though preloads may stick out;
  - touching exactly 4 preload POLLEN;
  - **motionless after INIT**.
- **No flywheel pre-spin.** All parts must be still at the start (R102, G304.H). Spin-up happens after START.
- **Stay on our side.** Disrupting the opponent's AUTO is a MAJOR foul per match, even when accidental, and crossing the centre line is "a risky gameplay strategy" (G402, M p.106). Each alliance has priority over the balls and FLOWERS on its side.
- **LEAVE** = not touching **any** wall when AUTO ends (QA21). **PARK** = at least partly in our LZ. A partner pushing you off the wall still counts (QA28).
- A tip that finishes during the 8 s transition still counts as an AUTO tip (M p.86).
- **G409 applies in AUTO**: don't let spilling balls hit the robot before they touch the floor (QA17). Still unanswered: QA35 asks whether a robot may wait with its intake running just outside the spill area.
- The human player may enter 1 NECTAR per tip of our HIVE, even during AUTO (QA11). In phase 1 (POLLEN only) the human holds it.
- An alignment jig for setting up the robot is allowed (G302 box, M p.103). Build one for S1 and S6.

---

## The ladder

| Rung | What it does | Our points | Needs | Gate |
|---|---|---|---|---|
| **A0 Safe** | Drive off the wall into the LZ park pose | 8 (LEAVE 3 + PARK 5) | Drive, Pinpoint odometry, Pedro paths | 10/10 before the first event |
| **A1 Tip & Park** (primary) | From S1, fire the 4 preloads, tip the start CELL, park | **28** (tip 20 + 3 + 5). With a partner that LEAVEs and PARKs: **36 + SWARM RP** | Indexer holding 4, flywheel gating, aim | ≥ 8/10 |
| **A2 Tip, Reload, Volley** (stretch) | A1, then sweep the red GARDEN POLLEN, fire them into the far CELL from Zone B, park | 28 in AUTO + 3–4 of the 8 balls needed for tip 2 | Fast enough intake, count sensor | ≥ 8/10 |
| **A3 Alliance double-tip** (stretch) | We run an A2 variant. A partner with a shooter waits at S6 and fires its 4 into the far CELL | **56** for the alliance (2 tips + 2 LEAVE + 2 PARK) | A partner with a shooter; both robots reliable | ≥ 8/10 together |

For scale: the best US alliance AUTO so far is **36**, and only about 7% of alliance-matches have reached an AUTO tip plus SWARM. No US alliance has made 2 AUTO tips (FTCScout, as of 2026-10-20).

---

## Poses (red)

Headings assume the shooter's "turret zero" points out of the robot's **front**. If ours points elsewhere, the code adds that offset (`RobotConfig`). All values are CAD-derived ([01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame)) with ±1 in field tolerance (M p.63). Clearances assume 18 × 18 in robots.

| Name | X | Y | Heading | Used by | Notes |
|---|---|---|---|---|---|
| **S1** | 61.67 | -12.75 | 180° | A0, A1, A2, A3-R1 | Back on the audience wall, on the red HIVE axis. 45.2 in to the aim point, dead ahead |
| S2 | 61.67 | -37.5 | 151° | A0 | 51.5 in, 29° off-axis. 0.9 in from the GARDEN tape |
| S5 | -61.67 | -61.67 | 0° | A0 | Far/red corner, 6 in from the LZ |
| **S6** | -61.67 | -10.1 | 0° | A3-R2 | Back on the far wall. 45.3 in to the far-CELL aim point once it is up. **Only 1.35 in from F-far** |
| S7 | -61.67 | -40.0 | 31° | A3-R2 backup | Use if S6 touches F-far. 52.8 in, 31° off-axis |
| Spill escape | 52 | -40 | – | leaving S1 | Keeps the robot about 2 in outside the tip-1 landing box |
| Corridor points | 30, -40 and -30, -40 | | – | all | Clear the HIVE feet by about 6 in, and a robot parked in the far slot by 3 in |
| Garden approach / pick | 50, -61 → 60.5, -61 | | 0° | A2, A3-R1 | Option 1: face the audience wall and drive into the line of balls (est.) |
| Garden sweep (option 2) | 60.5, -42 → 60.5, -60 | | intake leading toward -Y | A2, A3-R1 | Drive along the wall. Gets all 4 only if the roller reaches within 1.4 in of the wall (code's current choice; test both, [07 Exp 8](07-testing-and-practice.md#exp-8--intake-pickup-speed)) |
| A2 (Zone A centre) | 48 | -12.75 | 180° | TELEOP | 31.5 in to the aim point |
| B2 (Zone B centre) | -48 | -12.75 | 0° | A2 | 31.5 in to the far-CELL aim point. If the shot map says that is too close, use B-head (-55, -12.75, 0°) |
| B approach | -30 | -37 | – | A3-R1 | Climbs above the far-slot robot before turning toward B-side |
| B-side | -52 | -30 | 26° | A3-R1 | 39.5 in, 26° off-axis. 1 in outside the tip-2 landing box; 1.9 in from a robot still at S6 |
| Exit | -28 | -38 | – | A2, A3-R1 | From Zone B to the near slot without touching the far-slot robot |
| **Park, one robot** | -35.25 | -55 | 0° | A0–A2 | 4.3–4.9 in inside the LZ, 6.7 in off the red wall: PARK **and** LEAVE |
| **Far slot** | -51 | -56 | 0° | two robots | Whichever robot parks **first** takes this slot. 4.6 in inside the LZ along the wall, 5.3 in out from it |
| **Near slot** | -27 | -56 | 0° | two robots | Second robot. 12 in along the wall, 5.3 in out. The two robots end 6 in apart, 5.7 in off the wall |

Aim points: red audience CELL **(16.50, -12.74, 58.27)**; red far CELL **(-16.48, -12.74, 58.31)** (CAD; SDK).

**Why "first robot takes the far slot":** a robot in the near slot (Y up to -47) blocks the y = -40 corridor, because a robot there reaches down to Y -49.

### Setting up with the jig

- **S1:** robot's back against the audience wall. Its red-wall-side edge sits **1.75 in** from the tile seam at Y = -23.5 (between columns B and C); the robot then spans Y -21.75 to -3.75.
- **S6:** robot's back against the far wall. Its red-wall-side edge sits **4.4 in** from the same B|C seam, and its centre-line side is 1.1 in from the centre seam (Y = 0). **Look at F-far: if the robot touches it, the start is illegal (G304.D). Move to S7.**
- Tiles vary by about ±0.5 in (EFSG §7). Re-check on every new field.

---

## A0 — Safe (any start)

LEAVE 3 + PARK 5 = **8**. Run it whenever a higher rung isn't trusted on that day.

| Start | Path | Length | Time (est.) |
|---|---|---|---|
| S1 | S1 → (52, -40) → (30, -40) → (-30, -40) → park | ≈ 127 in | 3.5–4.5 s |
| S2 | S2 → (30, -40) → (-30, -40) → park | ≈ 108 in | 3–4 s |
| S5 | S5 → park (-35.25, -55) | ≈ 27 in | ~1.5 s |
| S6 | S6 → (-50, -10.1) → (-40, -40) → park. **First leg straight along +X**: F-far is 1.35 in away, and a diagonal first move clips it | ≈ 59 in | ~2.5 s |
| **A0-blind** (odometry broken) | Drive straight 12 in off the wall by time | – | LEAVE only (3) |

- If the partner also parks in AUTO, use the two slots instead of the one-robot pose. **First to arrive takes the far slot.**
- Use the start delay (0–10 s) if the partner's AUTO needs the space first.

---

## A1 — Tip & Park (primary, start S1)

### INIT checklist
- [ ] Robot placed at S1 with the jig, back touching the audience wall.
- [ ] 4 POLLEN inside the robot; count sensors read 4.
- [ ] Selector: **RED · S1 · A1 · delay 0**, then lock.
- [ ] Telemetry shows pose (61.67, -12.75, 180°), ball count 4, and the Limelight sees tags 34–37.
- [ ] Nothing moves after INIT (G304.H).

### Timeline

| Clock (s) | Robot | Notes / sources |
|---|---|---|
| 0.0 | START. Flywheel to the table speed for ~45 in. Turret aims: odometry bearing 180° plus Limelight trim | No pre-spin allowed (R102) |
| ≈ 0.5–1.0 | Flywheel "ready": within ±2–3% for 3 or more loops | Spin-up 0.3–0.5 s on the bare wheel; ≈ 0.5 s with the 60 mm steel flywheel (est.) |
| ≈ 1.0–2.5 | **Fire all 4 as one quick volley.** Feed the next ball as soon as the wheel is ready again, 0.15–0.5 s apart (est.) | The best robot at Saline fired 0.14–0.25 s apart (MonaShores) |
| ≈ 3 | Last ball arrives (0.4–0.6 s flight, est.) | The 3rd ball should tip it (EFSG §12.3) |
| ≈ 3 → 6–9 | **Hold at S1, intake off.** S1 is outside the landing box. Leave when tags 34–37 have been gone for 1 s, or 6 s after the last shot, whichever comes first | Dwell 0.25–3.4 s + swing ≈ 0.75 s; spill on the tiles ≈ 3 s after the threshold ball, up to ~6 s (MonaShores) |
| ≈ 6–9 → 10–14 | Drive S1 → spill escape (52, -40) → (30, -40) → (-30, -40) → park pose or our agreed slot. The code ends at the nearest square heading (multiple of 90°) so two parked robots can't clip each other. Keep the intake off. Settled spill balls in the way just get pushed: bulldozing is not CONTROL (G407 box D) | ≈ 127 in, 3.5–4.5 s (est.) |
| ≤ 14 | **Parked: partly in the LZ, not touching any wall** | LEAVE 3 + PARK 5 (QA21) |
| 14–30 | Hold still. Save pose, ball count (0) and HIVE state (B, far CELL up) every loop for TELEOP | |
| **Hard deadline** | Start the park drive by **24 s**, whatever else is happening | |

### Why fire all 4 when 3 tip it
- Insurance: bounce-outs are common. One team measured 44% with a fixed solid hood (CD-14343).
- At Saline, volleys that went past the threshold seemed to shorten the wait before the HIVE moved (MonaShores; weak evidence).
- Fire them close together so no ball arrives after the HIVE has started to move. Launching at a moving HIVE can stop the tip (M p.87).

---

## A2 — Tip, Reload, Volley (stretch)

| Clock (s) | Robot | Notes |
|---|---|---|
| 0 → ≈ 9 | A1 steps, up to leaving S1 | |
| ≈ 9–12 | S1 → garden approach (50, -61, 0°) | ≈ 50 in. This path stays outside the landing box |
| ≈ 12–15 | Intake on. Creep to garden pick (60.5, -61, 0°), or run sweep option 2. Stop at count 4 or after 3 s | Option 1 likely gets 3: the corner ball at (69.3, -69.3) may be out of reach (est.). Our GARDEN loses 1 pt per ball taken unless refilled (M p.89) |
| ≈ 15–19 | (60.5, -61) → (30, -40) → (-30, -40) → B2 (-48, -12.75, 0°). Spin up on the way | ≈ 130 in, 3.5–4.5 s (est.) |
| ≈ 19–21 | Fire into the far CELL, which is up and empty after tip 1 | 3–4 of the 8 balls for tip 2 |
| ≈ 21–24 | B2 → park (-35.25, -55). If the partner is in the far slot: B2 → exit (-28, -38) → near slot (-27, -56) | 44–50 in, ≈ 3 s |
| **Deadlines** | Leave the garden by **17 s** even if the count is below 4. Leave Zone B for park by **26 s** | |

- Optional extra ammo: settled spill POLLEN in Zone A. Pick up only at **Y ≤ -2**, because blue has priority on its side in AUTO (G402). **Refuse all NECTAR in phase 1**: own NECTAR is legal to hold, but our hood can't launch it yet.
- If the garden sweep is too slow with the servo intake, A2 stays a practice item. That decision belongs to the intake test ([07 Exp 8](07-testing-and-practice.md#exp-8--intake-pickup-speed)).

---

## A3 — Alliance double-tip (stretch, needs a partner with a shooter)

Two roles. 12303 must be able to play **either** one, with a separate OpMode for each role.

| Role | Start | Job | Parks in |
|---|---|---|---|
| **A3-R1 "tipper"** | S1 | A1, garden sweep (**must** reach 4), fire from B-side → TIP 2 | Near slot (-27, -56) |
| **A3-R2 "waiter"** | S6 (or S7) | Wait for the far CELL to come up, fire 4 into it | Far slot (-51, -56) |

**Why the garden sweep must reach 4:** 4 + 4 = 8 tips the CELL. **7 must not tip** (EFSG §12.3), so 3 garden balls would leave the far CELL one short.

### A3-R1 (tipper) timeline

| Clock (s) | Robot | Notes |
|---|---|---|
| 0 → ≈ 9 | A1 at S1 (fire 4, hold for the spill) | |
| ≈ 9–15 | Garden sweep. If the count is still 3 at 15 s, keep sweeping until 16.5 s, then go anyway: the balls still help tip 2 in TELEOP | |
| ≈ 15–19 | (60.5, -61) → (30, -40) → B approach (-30, -37) → B-side (-52, -30, 26°) | ≈ 120 in. Closest approach: 1.9 in to the far-slot robot |
| ≈ 19–21 | Fire 4 → 8 in the CELL → **TIP 2** | It still counts as AUTO if it finishes in the transition (M p.86) |
| ≈ 21–24 | **Leave at once**: (-52, -30) → exit (-28, -38) → near slot (-27, -56). Get out of the tip-2 landing box (X -42 … -24, Y -28 … +2) before the spill lands | ≈ 43 in. First spilled balls reach the floor at least ~1.2 s after the threshold ball, usually ~3 s (MonaShores; est.) |
| **Deadline** | Leave Zone B for park by **26 s** | |

### A3-R2 (waiter) timeline

| Clock (s) | Robot | Notes |
|---|---|---|
| INIT | At S6 (-61.67, -10.1, 0°). **Check F-far** (1.35 in nominal gap). If it touches, use S7 (-61.67, -40.0, 31°) | G304.D |
| 0 → trigger | Wait. Spinning the flywheel up after START is fine | Before tip 1 the far CELL's tags face away from us, so we can't see them |
| Trigger | The far-CELL tags (red 30–33) are seen upright and steady (`HIVE_VISION_CONFIRM_MS`), or the HIVE tracker already says state B. The code waits at most 10 s (`A3_ROLE2_TIP_WAIT_TIMEOUT_SEC`) | **Never fire at a moving HIVE** (M p.87, QA12) |
| Trigger → +2 | Aim, fire 4 into the far CELL | |
| +2 → +5 | S6 → (-50, -10.1), straight along +X first → far slot (-51, -56) | ≈ 58 in |
| **No-tip fallback** | If no tip is seen within 10 s, the code **keeps the preloads**, parks in the far slot, and the 4 balls are shot in TELEOP. `A3_ROLE2_FIRE_WITHOUT_TIP_CONFIRM = true` fires anyway at the timeout; use it only after the partner's tip timing is rehearsed | |

### Who is where, when (both roles)

| Clock (s) | R1 tipper (from S1) | R2 waiter (from S6) |
|---|---|---|
| 0–3 | Fires 4 at the audience CELL | Waits |
| 3–9 | Holds at S1 during the spill | Waits; the far CELL comes up at about 6–10 s |
| 9–15 | Garden sweep | Fires 4 (≈ 10–12), drives to the far slot (≈ 12–16) |
| 15–19 | Corridor → B approach → B-side | Parked |
| 19–21 | Fires 4 → tip 2 | Parked |
| 21–24 | Exit → near slot | Parked |

Clearances (CAD-derived, 18 × 18 in robots): 3 in at the corridor point, 1.9 in on the way to B-side, 1.6 in on the exit, 6 in between the parked robots. **Rehearse with the partner's real dimensions before the first match.**

Points if everything works: 20 + 20 + 3 + 3 + 5 + 5 = **56**.

---

## Coordination scripts

### A3 (say this in the queue; about 60 seconds)
1. "Does your robot have a shooter and an AUTO that can wait at the far wall and fire its 4 preloads into the far CELL **after** it comes up?" If not, skip A3: we run A1 or A2, and you do LEAVE + PARK.
2. "We start at S1 and tip the start CELL at about 3–9 s. You start at S6: back on the far wall, centred on our HIVE, with your centre-line edge about 1 in from the centre line. Check you are not touching the far-wall FLOWER."
3. "Fire **only after our CELL has swung**: at about 10 s, or when you see tags 30–33 (blue: 38–41)."
4. "After you fire, park at the **far end** of our LOADING ZONE, 5–6 in off the wall. We come through at y = -40 and shoot from the far-wall side at about 19 s, then park at the near end."
5. "Don't touch any wall at the end of AUTO (no LEAVE, QA21). Stay on our side (G402)."
6. If they are the better tipper, swap: 12303 runs the waiter role (A3-R2).

### SWARM RP (every match)
1. "What's your AUTO? Does it end **off the wall** and **partly in our LOADING ZONE**?"
2. If yes: "Who parks first? The first robot takes the **far slot** (far end of the LZ); the second takes the **near slot**." Both parked plus both LEAVE = **16, SWARM locked in AUTO**.
3. If they only LEAVE: we LEAVE + AUTO PARK (8) + they LEAVE (3) = 11, so **both robots must PARK at the end** (+10 = 21).
4. If they can't move in AUTO: we need 13 (LEAVE + AUTO PARK + end PARK) and they must end-PARK (5) = 18. Remind them at 0:20.
5. The coach confirms the plan out loud at the field before placing the robots.

---

## Failure handling

| Problem | How the robot knows | What it does |
|---|---|---|
| Flywheel never reaches "ready" (for example, an unplugged encoder) | Not ready by 3 s | Fire anyway, 0.4 s apart, at table power with battery compensation (est.). Missed balls stay in Zone A for TELEOP |
| No Limelight lock | No valid tag result by 1.5 s | Aim from odometry alone; the jig makes the S1 pose exact |
| Ball won't leave (jam) | No shot seen 0.6 s after a feed (wheel-speed dip or break-beam) | Reverse the feeder 0.25 s and retry once. Still stuck: stop feeding, go park |
| No tip after 4 balls | Tags 34–37 still visible 6 s after the last shot | Go park anyway (still 8). TELEOP starts with the HIVE in state A; one more ball should tip it |
| Running late | Match clock | Park-drive deadline: 24 s from the Zone A side, 26 s from Zone B |
| Partner or ball blocks the path | Path follower timeout (est. 2 s over the expected time) | Skip to the next leg toward park |
| Pose wrong at INIT | INIT telemetry shows a pose that isn't S1 | Re-place the robot. If odometry is dead, select **A0-blind** |
| Ball count wrong at INIT | Slot sensors | Use the sensor count, not a hard-coded 4 |
| Robot touching a wall at 30 s | Nothing on the robot detects it | Park poses are 5.7–6.7 in off the wall; check this in the 10-run test (QA21) |
| Opponent crosses into our side | – | Don't react. The coach notes the match number for the Question Box (T301, M p.149) |

---

## Promotion rule

- Run each rung **10 times in a row** on the practice field: same jig, a charged battery, and the HIVE calibrated to EFSG §12.3 ([07](07-testing-and-practice.md#exp-1--tip-threshold-on-our-hive)).
- A run is a **success** only if every item for that rung happened: tip (A1+), LEAVE, PARK, no wall contact at 30 s, nothing on blue's side, no ball bouncing off the robot during a spill.
- **Promote at ≥ 8/10.** Keep the lower rung selectable at all times.
- **Demote** a rung that fails 2 of its last 5 match runs, until the cause is fixed and it passes 8/10 again.
- Log every run in [07 Exp 7](07-testing-and-practice.md#exp-7--auto-reliability).

---

## INIT selector (what the drive team sets)

Gamepad menu during INIT. The coach may press INIT and start (G423), but **drivers** handle the gamepads.

| Item | Choices | Default if not locked |
|---|---|---|
| Alliance | RED, BLUE | RED |
| Start | S1, S2, S5, S6, S7 | S1 |
| Routine | A0, A0-blind, A1, A2, A3-R1, A3-R2 | **A0** (safest) |
| Start delay | 0–10 s | 0 |

- D-pad up/down moves the cursor, left/right changes the value, A locks, B unlocks.
- Show one big confirmation line, e.g. `RED | S1 | A1 | delay 0 | LOCKED`.
- Exact buttons are in the code README: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`.

---

## Blue poses

**Rule: (x, y, heading) → (-x, -y, heading + 180°).** Don't mirror. The code applies this one function to every red pose.

| Name | Red | Blue |
|---|---|---|
| S1 | (61.67, -12.75, 180°) | (-61.67, 12.75, 0°) |
| S2 | (61.67, -37.5, 151°) | (-61.67, 37.5, -29°) |
| S5 | (-61.67, -61.67, 0°) | (61.67, 61.67, 180°) |
| S6 | (-61.67, -10.1, 0°) | (61.67, 10.1, 180°) |
| S7 | (-61.67, -40.0, 31°) | (61.67, 40.0, -149°) |
| Spill escape | (52, -40) | (-52, 40) |
| Corridor points | (30, -40), (-30, -40) | (-30, 40), (30, 40) |
| Garden approach / pick | (50, -61, 0°) / (60.5, -61, 0°) | (-50, 61, 180°) / (-60.5, 61, 180°) |
| B2 | (-48, -12.75, 0°) | (48, 12.75, 180°) |
| B approach / B-side / exit | (-30, -37) / (-52, -30, 26°) / (-28, -38) | (30, 37) / (52, 30, -154°) / (28, 38) |
| Park (one robot) | (-35.25, -55, 0°) | (35.25, 55, 180°) |
| Far slot / near slot | (-51, -56) / (-27, -56) | (51, 56) / (27, 56) |
| Start-CELL aim point | (16.50, -12.74, 58.27) | (-16.50, 12.76, 58.27) |
| Second-CELL aim point | (-16.48, -12.74, 58.31) | (16.48, 12.76, 58.31) |
| Own tags | start CELL 34–37, second CELL 30–33 | start CELL 42–45, second CELL 38–41 |

---

## Sources

- **Manual TU05** (G302, G304, G401–G403, G407, G409, R102; p.86–87, 103–106): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA11, QA12, QA17, QA21, QA28, QA35 (pending), https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **EFSG §7, §12.3** (tiles, tip calibration): https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **CAD** (all poses, clearances and path lengths; derived by our research): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **SDK v12.0** (cluster origin = CELL opening centre): https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md
- **FTCScout** (US AUTO statistics): https://ftcscout.org
- **MonaShores** (tip timing, volley spacing; community): https://github.com/Mona-Shores-FTC-Robotics/biobuzz
- **CD-14343** (44% bounce-outs; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
