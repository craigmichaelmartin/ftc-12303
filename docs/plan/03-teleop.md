# 03 · TELEOP (2:00 → 1:00)

The plan for the middle minute of the match: **the two-zone loop.** Shoot from the zone the up CELL faces. After the tip, let the spill land, sweep it up, cross to the other zone, and shoot again. **Two loads = one tip.**

Coordinates are red, in the [01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame) frame; blue = rotate 180°. Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

---

## Start of TELEOP: the 8-second transition

- **No powered motion at all** between AUTO and TELEOP (G403, M p.106). A flywheel coasting down is fine.
- Our TeleOp INIT moves nothing, so the coach may INIT it during the transition. **Press START only at the TELEOP bells.** START wakes the actuators.
- The HIVE state, pose and ball count carry over from AUTO. **Operator: look at the HIVE and confirm which CELL is up** before the first volley; fix it with the "HIVE tipped" button if needed.
- The first volley pays for flywheel spin-up (≈ 0.5 s, est.), because G403 forbids keeping it powered through the transition.

---

## The two-zone loop

1. **Shoot from the zone the up CELL faces.** Red starts in Zone A (audience half); after an odd number of red tips, Zone B (far half). Blue is the reverse ([01 §7](01-game-and-scoring.md#7-which-way-the-cells-face-read-this-twice)). The turret fine-aims; the **chassis gives the coarse heading**, because the turret only turns about 50–75° in total as built ([05 §3.5](05-robot-tuning.md#35-turret-range-and-a-known-zero)).
2. **Each tip of an empty CELL needs 8 POLLEN = 2 robot loads** (G407 caps a robot at 4; EFSG §12.3). Both robots feed the same CELL. Call the loads out loud: "4 in" … "8, it's going".
3. **After the threshold ball:**
   - **stop firing**: launching at a moving HIVE can stop the tip (M p.87, QA12). The HIVE may sit still for 0.25–3.4 s before it moves (MonaShores);
   - **stay out of the landing box for ~3 s** (allow up to 6 s; [01 §8](01-game-and-scoring.md#8-spill-where-the-balls-go-after-a-tip));
   - then **sweep the spill**: it lands in your own zone. Take up to 4;
   - **cross to the other zone and fire.**
4. Keep moving. Use different spots in the shooting cone when someone is in your way.

### Shooting spots

Distances are horizontal, from the robot centre to the aim point. Headings assume the shooter's zero points out of the robot's front. **The shot map ([07 Exp 2](07-testing-and-practice.md#exp-2--shot-map-and-hood-angle)) decides which spots we actually use.**

**Zone A** (red audience CELL up; aim point (16.50, -12.74, 58.27)):

| Spot | X | Y | Heading | Distance | Off-axis | Notes |
|---|---|---|---|---|---|---|
| A-head | 55 | -12.75 | 180° | 38.5 in | 0° | Default |
| S1 spot | 61.67 | -12.75 | 180° | 45.2 in | 0° | Against the wall; safe during the spill |
| A2 (zone centre) | 48 | -12.75 | 180° | 31.5 in | 0° | Close. The robot's front edge is about 3 in inside the landing box, so move after the tip volley |
| A-left | 58.8 | -35.25 | 152° | 47.9 in | 28° | Red-wall side (tile B1) |
| A-right | 58.8 | 11.75 | -150° | 48.9 in | 30° | **TELEOP only**: this is blue's side in AUTO (G402) |

**Zone B** (red far CELL up; aim point (-16.48, -12.74, 58.31)):

| Spot | X | Y | Heading | Distance | Off-axis | Notes |
|---|---|---|---|---|---|---|
| B-head | -55 | -12.75 | 0° | 38.5 in | 0° | Default |
| S6 spot | -61.67 | -10.1 | 0° | 45.3 in | 3° | Against the wall |
| B2 (zone centre) | -48 | -12.75 | 0° | 31.5 in | 0° | Close; 44 in from the park pose |
| B-left | -58.8 | -35.25 | 28° | 47.9 in | 28° | Near the LZ |
| B-right | -58.8 | 11.75 | -30° | 48.9 in | 30° | Blue side |
| B-side | -52 | -30 | 26° | 39.5 in | 26° | |

- Ball-flight model: with a fixed hood giving a ~64–65° exit angle, clean-entry chance is about 82–89% from roughly 35 to 50 in head-on, and about 87% at 30° off-axis. It drops sharply closer than ~30 in to the aim point (est.).
- **Stop the robot to shoot.** At 1 ft/s, a 0.4–0.9 s flight misses by 5–11 in (est.).
- **Shared half:** when our HIVE and the opponent's are in different states, both alliances shoot from the same half. Expect crowding and defence there.

### Moving between zones

| Route | Path (red) | Length | Use |
|---|---|---|---|
| Corridor | (±30, -40), along y = -40 | A2 → B2 ≈ 125 in (est. 3–4 s) | Default. Clears the HIVE feet by about 6 in |
| Under-HIVE lane | Straight along X, robot centre between Y -9.5 and +9.5 | A2 → B2 = 96 in | **TELEOP only.** The robot must fit under the down CELL (30.6 in up). The lane narrows for taller robots (about ±6.6 at 29 in). **Measure our height first.** Never use it during or just after a tip (G409, QA17), and don't touch the frame (G417) |

---

## Ammo sources (in this order)

| # | Source | Where (red) | Notes |
|---|---|---|---|
| 1 | **Own spill** | Lands in the zone you just shot from: first touch about X 24–42 (Zone A) or X -24 to -42 (Zone B), around Y -28 to +2, then rolls toward the end wall | Predictable; arrives ≈ 3 s after the threshold ball. **Wait until it has touched the floor** (G409). The first spill also holds our 3 NECTAR: **refuse NECTAR in phase 1** |
| 2 | Loose balls in your zone | Misses and bounce-outs | |
| 3 | GARDENS (either one in TELEOP) | Red (69.3, -60.6 … -69.3), audience corner. Blue (-69.3, 60.6 … 69.3), far corner | Not protected (G411 box). Each ball taken costs that GARDEN's owner 1 pt |
| 4 | FLOWER bottoms | 16 of the 24 non-preload POLLEN start in FLOWERS (M p.83) | **Only if [Exp 5](07-testing-and-practice.md#exp-5--flower-bottom-extraction) shows our intake can pull POLLEN through the 3.55 in opening.** Only POLLEN, only from the bottom (G418.B). Any FLOWER in TELEOP |
| 5 | Opponent spill | Their zone | Contested. Never touch their NECTAR (G408) |

There are 40 POLLEN on the field: 16 preloads, 16 in FLOWERS, 8 in GARDENS (M p.83). Teams without a FLOWER intake have reported being "completely starved of pollen" (CD-14343).

---

## Pace targets

| Target | Value | Source |
|---|---|---|
| One 4-ball load per robot | every **15–20 s** | strategy brief |
| A tip | every ~30–40 s with one robot; **~20 s** with a partner | brief (arithmetic) |
| **12303 goal by the League Tournament** | **1 AUTO tip + 2–3 TELEOP tips contributed per match** | brief |
| Saline winners | 3.6–3.8 tips per match | FTC Events / FTCScout |
| "Consistent early-season wins" | about 5 tips per alliance; "a tip every 30 seconds" | CD-14343 |

---

## Alliance call-outs

Short, loud, the same words every time.

| Who | Says | When |
|---|---|---|
| Operator | "Spinning" / "**4 in**" / "Empty" / "Full" | Starting a volley / after a volley / ball count |
| Coach | "Cell has **6** … **8, it's going**" | Keeps the running count for our up CELL (both robots' volleys) |
| Human player | "**TIP!**" | Sees the damper touch the frame |
| Coach | "Stop firing … wait … **sweep**" | Threshold reached → spill lands (~3 s) |
| Driver | "Crossing **far**" / "Crossing **audience**" | Heading to the other zone |
| Coach | "Your lane / my lane" | Agree which robot shoots head-on and which from the side spot |
| Coach | "**0:20, last volley at 0:12**" | Endgame ([04](04-endgame.md)) |

---

## Control map

This table matches the starter code (`opmodes/teleop/MainTeleOp.java`, header comment). **The code is the final word**: if drivers change a button, change it in the code and update this table and `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`.

**Driver (gamepad 1)**

| Control | Action |
|---|---|
| Left stick | Drive, field-centric: push up = away from our alliance wall |
| Right stick (left/right) | Turn |
| Left bumper (hold) | Slow mode |
| Right bumper (hold) | **Aim-assist**: turns the chassis so the turret is centred on the target (the turret only swings about ±30° as built) |
| Right trigger (hold) | **Intake in.** Stops itself at 4 balls and refuses opponent NECTAR |
| Left trigger (hold) | Intake out |
| Back / Share | Heading reset: robot must face straight away from the driver |
| LB + Y | "Robot is at S1": relocalize when the robot is parked exactly on S1 |

**Operator (gamepad 2)**

| Control | Action |
|---|---|
| A / Cross | **Shooting mode** on/off (flywheel + auto-aim) |
| Right bumper | **Fire volley**: every ball, one at a time, each only when at speed and aimed |
| X / Square | Fire one ball |
| B / Circle | Stop firing |
| Y / Triangle | **"HIVE tipped"**: flip which CELL is up (A ↔ B) |
| D-pad up/down | Flywheel RPM trim ±50 |
| D-pad left/right | Turret trim (left = counter-clockwise) |
| Right stick X | Manual turret, only when auto-aim is off |
| Left bumper (hold) | **Eject**: intake and spinners run backward to clear a jam |
| Back / Share | Ball count −1 |
| Left-stick click | Ball count +1 |

In INIT without AUTO data (practice): gamepad 1 X = BLUE, B = RED; gamepad 2 d-pad up/down sets the ball count. **The count starts at 4** (match preloads, G304) so a TeleOp started without AUTO can never take in more than 4 (G407).

**Feedback cues** (as planned in the code's feedback class on 10/20)

| Who feels it | Rumble | Light bar (PS4/PS5 only) | Meaning |
|---|---|---|---|
| Driver | 2 short blips | **Red** while full | **4 loaded**: intake stopped |
| Operator | 1 short buzz | **Green** = ready, yellow = spinning up | **At speed**: fire when aimed |
| Both | 1 long buzz | – | **Tip detected / target switched**: go to the other zone |
| Driver | 1 short buzz | – | Opponent NECTAR refused at the mouth |
| Both | 3 blips | – | **1:00 left**: human enters NECTAR; FLOWER window opens |
| Both | 1 s continuous | – | **0:20 left**: final cycle / park decision |

Use **wired** PS4/PS5-style controllers: wireless is not allowed (R904, QA16), and the Logitech F310 has no rumble (SDK sample `ConceptGamepadRumble`).

---

## Defence and counter-defence

**What is legal**
- Blocking shots is "not specifically disallowed" (QA2, QA20). Contact is allowed; **damage or functional impairment** is not (QA8, G419).
- No zone is protected; the GARDEN explicitly isn't (M p.89, G411 box).

**Limits that apply to everyone, including us**

| Rule | Limit | Penalty |
|---|---|---|
| G421 | **Pin no longer than 3 s.** The count ends when the robots are 2 ft apart for more than 3 s | **MAJOR, plus a MAJOR every 3 s** |
| G419 / G420 | No damaging, tipping or entangling the opponent | Verbal warning; MAJOR + YELLOW if STRATEGIC; RED if they can't drive |
| G204 | No forcing the opponent into a foul. Examples: ramming a robot lined up at a FLOWER at 1:05 so it drops NECTAR early; knocking away NECTAR while their human is entering it | MAJOR per instance |
| G411 | No hoarding or guarding balls | MAJOR + YELLOW, **no warning step** |
| G417.D | **Never shoot at the opponent's HIVE to stop their tip** | MAJOR + YELLOW if STRATEGIC |

**Counter-defence (when they block us)**
- Keep moving. Switch between the head, left and right spots in the cone.
- Steep arcs are hard to block: a 65° shot rises past 29 in within about 6–7 in of leaving the robot (est.).
- Our mecanum drive loses pushing fights (gm0), so **evade, don't shove**. Hold position while firing (path-follower "hold").
- If pinned, the drive coach notes the time for the Question Box (T301–T302, M p.149).
- Protect hubs, wiring and the power switch. Damage to exposed parts in normal play is **not** the opponent's foul (G419 box).

**When we defend**
- Only if our shooter is down, or the opponent has one dominant scorer and our partner can tip alone.
- Screen their shooting cone **without contact**, count "1-2" on any pin, then back off 2 ft.
- Never sit in their spill (G409) or interfere with their NECTAR entry (G204).
- Saline reported defence "everywhere in TELEOP" (MonaShores), so expect it.

---

## Possession discipline (design requirement and driving rule)

| Rule | What it says | Penalty (manual text) | Q&A |
|---|---|---|---|
| **G407** (M p.108) | Never CONTROL more than **4** balls. CONTROL includes a ball in a hopper, indexer or shooter, a ball stuck on the robot, and **herding** (pushing with a flat or concave face, even through other balls). Driving into a ball by accident ("bulldozing"), being hit, and balls already launched are **not** CONTROL | Verbal warning; **MAJOR + YELLOW** per match if STRATEGIC | QA3: **no "momentary" exception**; briefly reversing a 5th out is still a warning. QA23: routine momentary over-control built into the design or strategy is likely STRATEGIC. TU05: repeated 5+ is STRATEGIC even with a "keep 4, discard the rest" sorter |
| **G408** (M p.108) | Never CONTROL the **opponent's NECTAR** | Verbal warning; **YELLOW** per match if STRATEGIC | QA4: no momentary exception; ejecting immediately is "typically not" STRATEGIC. QA23 says MAJOR + YELLOW, but the **manual governs** (§1.7.4), so **design to the stricter reading**. TU05: repeats are STRATEGIC even with an auto-discard sorter |
| G409 (M p.109) | Don't catch or deflect spilling balls before they touch something else | Verbal warning; YELLOW if STRATEGIC | QA17: a ball bouncing off a robot under the HIVE counts |
| G417 (M p.111) | Only launching into the up CELL may move the HIVE. Don't touch the HIVE, directly or through a held ball | Verbal warning; MAJOR + YELLOW if STRATEGIC | QA10: launching our balls into their HIVE isn't banned, but it only helps them |

Still waiting for answers: **QA34** (a wide intake bar with extra balls underneath) and **QA29** (lift-and-release, active expulsion, colour rejection). See [09](09-rules-cheatsheet.md#pending-qa-to-watch).

**What each person does**
- **Robot:** the count starts at 4 (the preloads), the intake auto-stops at 4, the colour sensor **refuses opponent NECTAR at the mouth before it is drawn in**, and a guard keeps balls from resting on top.
- **Driver:** don't drive the running intake into a pile with opponent NECTAR in it. Split piles first. Don't push piles with the flat front.
- **Operator:** watch the count. If a 5th ball gets in, **reverse immediately**, then tell the coach.
- **Coach:** track warnings. Warnings carry from qualifications into playoffs (M p.94), and repeated warnings for the same thing draw STRATEGIC scrutiny (M p.92). League Meet warnings clear after each meet (M p.167).

---

## Sources

- **Manual TU05** (G204, G403, G407–G411, G417–G421, p.86–94, 106–115, 149, 167): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA2, QA3, QA4, QA8, QA10, QA12, QA16, QA17, QA20, QA23; pending QA29, QA34. https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **TU05** (G407/G408 boxes): https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **EFSG §12.3**: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **CAD** (spots, distances, lane widths; derived): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **FTCScout / FTC Events** (Saline): https://ftcscout.org/events/2026/USMISAS · https://ftc-events.firstinspires.org/2026/USMISAS/rankings
- **MonaShores** (tip dwell, defence at Saline; community): https://github.com/Mona-Shores-FTC-Robotics/biobuzz
- **CD-14343** (benchmarks, "starved of pollen"; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
- **gm0** (mecanum pushing): https://gm0.org/en/latest/docs/common-mechanisms/drivetrains/holonomic.html
- **SDK samples** (`ConceptGamepadRumble`): https://github.com/FIRST-Tech-Challenge/FtcRobotController
