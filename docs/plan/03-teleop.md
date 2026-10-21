# 03 · TELEOP (2:00 → 1:00)

The plan for the middle minute of the match: **the two-zone loop.** Shoot from the zone the up CELL faces. After the tip, let the spill land, sweep it up, cross to the other zone, and shoot again. **Two loads = one tip.**

Coordinates are red, in the [01 §6](01-game-and-scoring.md#6-the-field-in-our-coordinate-frame) frame; blue = rotate 180°. Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

---

## Start of TELEOP: the 8-second transition

- **No powered motion at all** between AUTO and TELEOP (G403, M p.106). A flywheel coasting down is fine.
- Our TeleOp INIT moves nothing, so the coach may INIT it during the transition. **Press START only at the TELEOP bells.** START wakes the actuators.
- **Read the INIT Alliance line before START.** With AUTO data it reads `Alliance RED (from AUTO, n s ago) - wrong? gamepad1 X = BLUE / B = RED`. Without AUTO data (an app restart or Control Hub reboot between AUTO and TELEOP, or AUTO not run) it reads `!! NO AUTO DATA - ALLIANCE = …` and takes the colour from the last AUTO INIT (RED if there is none). If the colour is wrong, the driver presses gamepad 1 **X (BLUE) or B (RED)** in INIT. With AUTO data this also rotates the handed-over pose 180° and keeps the ball count and HIVE state.
- The HIVE state, pose and ball count carry over from AUTO. **Operator: look at the HIVE and confirm which CELL is up** before the first volley; fix it with Y ("HIVE tipped") if needed. From our LOADING ZONE the camera can never see the start CELL's tags while that CELL is up, so if the start CELL is up but telemetry says "HIVE state B" (for example after an A3 double tip, when TeleOp INIT says "AUTO says HIVE state B … press Y after START"), only this check catches it: press Y.
- The first volley pays for flywheel spin-up (≈ 0.5 s, est.), because G403 forbids keeping it powered through the transition.

---

## The two-zone loop

1. **Shoot from the zone the up CELL faces.** Red starts in Zone A (audience half); after an odd number of red tips, Zone B (far half). Blue is the reverse ([01 §7](01-game-and-scoring.md#7-which-way-the-cells-face-read-this-twice)). The turret fine-aims; the **chassis gives the coarse heading**, because the turret only turns about 50–75° in total as built ([05 §3.5](05-robot-tuning.md#35-turret-range-and-a-known-zero)).
2. **Each tip of an empty CELL needs 8 POLLEN = 2 robot loads** (G407 caps a robot at 4; EFSG §12.3). Both robots feed the same CELL. Call the loads out loud: "4 in" … "8, it's going".
3. **After the threshold ball:**
   - **stop firing**: launching at a moving HIVE can stop the tip (M p.87, QA12). The HIVE may sit still for 0.25–3.4 s before it moves (MonaShores). The robot's own 1 s pause starts only when the target switches, which is after the swing, so it does not protect a tipping HIVE: fire each load as one volley and start no new volley after "8, it's going";
   - **Operator: press Y ("HIVE tipped") once when the human player calls "TIP!"** The camera cannot see our own tip from the zone we fired from (all our tags then face the other half, [01 §7](01-game-and-scoring.md#7-which-way-the-cells-face-read-this-twice)); without Y the robot keeps aiming at the old CELL. If the camera already switched the target (both pads buzzed twice), a Y within 3 s only confirms that switch (one strong short buzz on the operator pad), so pressing on "TIP!" is always right;
   - **stay out of the landing box until the balls have landed**: usually ~3 s, up to ~6 s after the threshold ball ([01 §8](01-game-and-scoring.md#8-spill-where-the-balls-go-after-a-tip));
   - then **sweep the spill**: it lands in your own zone. Take up to 4;
   - **cross to the other zone and fire.** On arrival, if the operator light is **RED** (target CELL faces away) or the AIM line doesn't name this zone (red after an odd number of tips: "ZONE B (FAR half)"), press Y once before firing.
4. Keep moving. Use different spots in the shooting cone when someone is in your way.

### Shooting spots

Distances are horizontal, from the robot centre to the aim point. Headings assume the shooter's zero points out of the robot's front. **The shot map ([07 Exp 2](07-testing-and-practice.md#exp-2--shot-map-and-hood-angle)) decides which spots we actually use.**

**Zone A** (red audience CELL up; aim point (16.50, -12.74, 58.27)):

| Spot | X | Y | Heading | Distance | Off-axis | Notes |
|---|---|---|---|---|---|---|
| A-head | 55 | -12.75 | 180° | 38.5 in | 0° | Default |
| S1 spot | 61.67 | -12.75 | 180° | 45.2 in | 0° | Against the wall; 10.7 in outside the landing box, so safe during the spill |
| A2 (zone centre) | 48 | -12.75 | 180° | 31.5 in | 0° | Close. The robot's front edge is about 3 in inside the landing box. The robot does not detect our tip from here, so move off this spot after every volley that may reach the threshold |
| A-side (AUTO side spot) | 55 | -37 | 148° | 45.5 in | 32° | Red-wall side; 6.8 in outside the landing box |
| A-left | 58.8 | -35.25 | 152° | 47.9 in | 28° | Red-wall side (tile B1) |
| A-right | 58.8 | 11.75 | -150° | 48.9 in | 30° | **TELEOP only**: this is blue's side in AUTO (G402) |

**Zone B** (red far CELL up; aim point (-16.48, -12.74, 58.31)):

| Spot | X | Y | Heading | Distance | Off-axis | Notes |
|---|---|---|---|---|---|---|
| B-head | -55 | -12.75 | 0° | 38.5 in | 0° | Default |
| S6 spot | -61.67 | -10.1 | 0° | 45.3 in | 3° | Against the wall |
| B2 (zone centre) | -48 | -12.75 | 0° | 31.5 in | 0° | Close; 44 in from the solo park pose. The robot's front edge is about 3 in inside the tip-2 landing box, so move after a tipping volley |
| B-left | -58.8 | -35.25 | 28° | 47.9 in | 28° | Near the LZ |
| B-right | -58.8 | 11.75 | -30° | 48.9 in | 30° | Blue side |
| B-side (A3 role-1 spot) | -52 | -30 | 26° | 39.5 in | 26° | 0.87 in outside the tip-2 landing box |

- Ball-flight model: with a fixed hood giving a ~64–65° exit angle, clean-entry chance is about 82–89% from roughly 35 to 50 in head-on, and about 87% at 30° off-axis. It drops sharply closer than ~30 in to the aim point (est.).
- The Driver Station warns `!! distance N in is outside the reliable shot band (30-92 in)` (R3's 27–90 in band measured to the lip, plus 2.8 in to the opening centre) and `! off-axis N deg (> 40): shots start to hit the cell's side wall`. With auto-aim the robot never fires when the opening faces more than 75° away from it (`!! TARGET CELL FACES AWAY`, RED operator light).
- **Stop the robot to shoot.** At 1 ft/s, a 0.4–0.9 s flight misses by 5–11 in (est.). The code waits until the chassis moves less than 6 in/s and turns less than 20°/s.
- **Shared half:** when our HIVE and the opponent's are in different states, both alliances shoot from the same half. Expect crowding and defence there.

### Moving between zones

| Route | Path (red) | Length | Use |
|---|---|---|---|
| Corridor | (30, -42) → (-30, -40) | A2 → B2 ≈ 125 in (est. 3–4 s) | Default. Clears the HIVE feet by about 6 in (less while the robot is still turning) |
| Under-HIVE lane | Straight along X, robot centre between Y -9.5 and +9.5 | A2 → B2 = 96 in | **TELEOP only.** The robot must fit under the down CELL (30.6 in up). The lane narrows for taller robots (about ±6.6 at 29 in). **Measure our height first.** Never use it during or just after a tip (G409, QA17), and don't touch the frame (G417) |

---

## Ammo sources (in this order)

| # | Source | Where (red) | Notes |
|---|---|---|---|
| 1 | **Own spill** | Lands in the zone you just shot from: first touch about X 24–42 (Zone A) or X -24 to -42 (Zone B), around Y -28 to +2, then rolls toward the end wall | Predictable; arrives ≈ 3 s after the threshold ball (up to ~6 s). **Wait until it has touched the floor** (G409). The first spill also holds our 3 NECTAR: in phase 1 the intake refuses all NECTAR at the mouth (`ACCEPT_OWN_NECTAR = false`), but the sensor reacts on contact, so steer around the NECTAR |
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
| Human player | "**TIP!**" | Sees the damper touch the frame. **The operator presses Y on this call** |
| Coach | "Stop firing … wait … **sweep**" | Threshold reached → spill lands (usually ~3 s, up to ~6 s) |
| Driver | "Crossing **far**" / "Crossing **audience**" | Heading to the other zone |
| Coach | "Your lane / my lane" | Agree which robot shoots head-on and which from the side spot |
| Coach | "**0:25**" … "**0:12, last volley**" … "**Park**" | Endgame, from the field timer ([04](04-endgame.md)): last cycle, final volley (0:15 if we are in Zone A), drive to the LZ |

---

## Control map

This table matches the code (`opmodes/teleop/MainTeleOp.java`, header comment, and `README-12303.md` §7). **The code is the final word**: if drivers change a button, change it in the code and update this table and `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md`.

**Driver (gamepad 1)**

| Control | Action |
|---|---|
| Left stick | Drive, field-centric: push up = away from the drivers |
| Right stick (left/right) | Turn |
| Left bumper (hold) | Slow mode (× 0.35) |
| Right bumper (hold) | **Aim-assist**: turns the chassis so the turret is centred on the target (the turret only swings about ±28° as built) |
| Right trigger (hold) | **Intake in.** Stops itself at 4 balls. Refuses NECTAR at the mouth: always the opponent's, ours too while `ACCEPT_OWN_NECTAR = false` (phase 1) |
| Left trigger (hold) | Intake out |
| Back / Share | Heading reset: the robot must face straight away from the drivers |
| LB + Y | Relocalize on **our** S1: only when the robot is still and parked exactly on our S1 (red: back on the audience wall at (61.67, -12.75); blue: back on the far wall at (-61.67, 12.75)) |

**Operator (gamepad 2)**

| Control | Action |
|---|---|
| A / Cross | **Shooting mode** on/off (flywheel + auto-aim). Turning it OFF also cancels a requested volley |
| Right bumper | **Fire volley**: every ball, one at a time, each only when ready (firing gates below) |
| X / Square | Fire one ball (works even if the count says 0) |
| B / Circle | Stop firing |
| Y / Triangle | **"HIVE tipped"**: press once on every "TIP!". It flips the target CELL. Within 3 s of a camera switch it only **confirms** that switch (one strong short buzz): press again to really flip. After Y, vision cannot change the target for 5 s; after that, 0.5 s of disagreeing vision can switch it back. If vision keeps flipping it wrongly, set `HIVE_VISION_AUTO_SWITCH = false` and re-deploy |
| D-pad up/down | Flywheel RPM trim ±50 |
| D-pad left/right | Turret trim ±0.5° (left = counter-clockwise) |
| Right stick X | Manual turret, whenever auto-aim is off |
| Right-stick click | **Manual aim** on/off: RB / X then fire at the manual turret angle as soon as the flywheel is at speed (no aim, off-axis or stillness check). For when odometry or vision aims wrong |
| Left bumper (hold) | **Eject**: intake and spinners run backward to clear a jam |
| Back / Share | Ball count −1 |
| Left-stick click | Ball count +1 |

**INIT** (nothing moves, G403): gamepad 1 X = BLUE, B = RED; gamepad 2 d-pad up/down sets the ball count. **The count starts at 4** without AUTO data (match preloads, G304), so a TeleOp started without AUTO can never take in more than 4 (G407). Without AUTO data the robot is also assumed to be on our S1: if it isn't, after START point it straight away from the drivers and press back, then relocalize with LB + Y when it is on S1.

**Firing gates.** With auto-aim a ball is fed only when the flywheel is at speed (within max(2.5%, 60 rpm) for 3 loops), the target opening faces the robot within 75°, the turret is on a reachable target within 2°, the chassis is still (< 6 in/s and < 20°/s), and there is no TIP pause (1 s after any target change; a requested volley is then cancelled: press RB again). With `TURRET_PINNED = true` (or no turret servo configured) the turret is held at centre and the **chassis** must point at the target within 3° (driver RB aim-assist does that). Manual aim fires on flywheel speed only. If a feed is not confirmed (no beam break and no flywheel dip within 0.4 s), the ball still counts as fired: there is no automatic jam retry. Clear jams with operator LB and fix the count with back / left-stick click.

**TELEOP does not hold position while firing**: the motors only brake. If the robot is pushed, firing pauses ("moving") until it is under 6 in/s and 20°/s again.

**Feedback cues** (`subsystems/Feedback.java`)

| Who feels it | Rumble | Meaning |
|---|---|---|
| Driver | 2 short blips | **4 loaded**: intake stopped |
| Driver | 1 short buzz | NECTAR refused at the mouth |
| Operator | 1 light short buzz | **Flywheel at speed** (again after every shot) |
| Operator | 1 strong short buzz | Your Y **confirmed** the camera's switch (no change) |
| Operator | 4 blips | A feed was **not confirmed** (jam? check the count). On every shot = dip detection not tuned and no feed beam. TeleOp also shows `!! FEED NOT CONFIRMED …` for 3 s |
| Both | 1 long buzz | **Operator Y switched the target CELL** |
| Both | 2 long buzzes | **The camera switched the target CELL** |
| Both | 3 blips | **1:00 left** (our own clock; the field timer is the authority): human enters NECTAR; FLOWER window opens |
| Both | 1 s continuous | **0:20 left**: a reminder (the coach's field-timer calls rule: "0:25", "0:12, last volley", "Park") |

**After any target switch, shoot from the end the up CELL faces (look at the HIVE). If you are already there, stay.** For 5 s the Driver Station also shows `>> TARGET SWITCHED: GO TO OUR AUDIENCE END` (or `FAR END`) `(stay if you are already there) <<`, correct for our colour.

**Light bars (PS4/PS5 only).** Driver: RED = full (4), YELLOW = 1–3 balls, BLUE = empty. Operator: BLUE = shooting mode off, **RED = target CELL faces away (wrong zone, or the HIVE tipped: press Y)**, PURPLE = turret at its limit (turn the chassis), GREEN = ready to fire, YELLOW = spinning up / aiming.

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
- Our mecanum drive loses pushing fights (gm0), so **evade, don't shove**. TELEOP does not hold position: release the sticks (the motors brake). If we are pushed, firing pauses until the robot is still, so re-aim with RB.
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
- **Robot:** the count starts at 4 (the preloads). A ball is counted once it has passed the mouth sensor; the intake stops at 4, and a short reverse pulse pushes a 5th ball away (with 3 counted, a ball right behind ball 4 counts ball 4 at once so the pulse hits the 5th; two touching balls are only split once `MOUTH_ONE_BALL_MAX_MS` is measured). The slot-4 sensor forces "full" whatever the count says. The colour sensor **refuses NECTAR at the mouth** (always the opponent's; ours too while `ACCEPT_OWN_NECTAR = false`) and keeps reversing for as long as the ball stays there. A guard keeps balls from resting on top.
- **Driver:** the mouth sensor only reacts on contact, so don't drive the running intake into a pile with NECTAR in it. Split piles first. Don't push piles with the flat front.
- **Operator:** watch the count. If a 5th ball gets in, **eject immediately** (hold LB), then tell the coach.
- **Coach:** track warnings. Warnings carry from qualifications into playoffs (M p.94), and repeated warnings for the same thing draw STRATEGIC scrutiny (M p.92). League Meet warnings clear after each meet (M p.167).

---

## Sources

- **Manual TU05** (G204, G403, G407–G411, G417–G421, p.86–94, 106–115, 149, 167): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA2, QA3, QA4, QA8, QA10, QA12, QA16, QA17, QA20, QA23; pending QA29, QA34. https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **TU05** (G407/G408 boxes): https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **EFSG §12.3**: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **CAD** (spots, distances, lane widths; derived): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **Code** (controls, cues, firing gates, as of 2026-10-20): `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/teleop/MainTeleOp.java`, `subsystems/Feedback.java`, `Robot.java`; `README-12303.md` §7
- **FTCScout / FTC Events** (Saline): https://ftcscout.org/events/2026/USMISAS · https://ftc-events.firstinspires.org/2026/USMISAS/rankings
- **MonaShores** (tip dwell, defence at Saline; community): https://github.com/Mona-Shores-FTC-Robotics/biobuzz
- **CD-14343** (benchmarks, "starved of pollen"; community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
- **gm0** (mecanum pushing): https://gm0.org/en/latest/docs/common-mechanisms/drivetrains/holonomic.html
- **SDK samples** (`ConceptGamepadRumble`): https://github.com/FIRST-Tech-Challenge/FtcRobotController
