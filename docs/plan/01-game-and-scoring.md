# 01 · Game and scoring

What scores, what the ranking points need, how many balls tip the HIVE, why tips win, and where everything is on the field.

Source tags such as (M p.91) and (QA21) are explained in the [source key](00-master-plan.md#source-key). **est.** means our own estimate from a model, not a measured fact. Blue numbers are the red numbers rotated 180° (section 6).

---

## 1. Match clock

Go by the **field timer**. Sounds are only a courtesy (M p.79, Table 9-1).

| Field timer | Period | Sound | What changes |
|---|---|---|---|
| 2:30 → 2:00 | **AUTO** (30 s, robot runs code only) | buzzer ×3 at end | Drivers hands-off (G401) |
| 8 s | **Transition** | "pick up your controllers, 3-2-1" | **No powered motion at all** (G403) |
| 2:00 | **TELEOP** starts | 3 bells | |
| **1:00** | Last minute | **chime** (TU04) | NECTAR may go into FLOWERS (G410). Human may enter **all** remaining NECTAR (G426.B) |
| 0:20 | Reminder only | train whistle | Nothing changes (M p.79) |
| 0:00 | End | 3-s buzzer | Stop. No powered motion (G404). Things are scored when everything is at rest |

---

## 2. Points

Table 10-2 (M p.91); fouls Table 10-4 (M p.92).

| What | AUTO | TELEOP / end | When it is judged |
|---|---|---|---|
| LEAVE: robot not touching any perimeter wall | 3 | – | End of AUTO only (QA21) |
| PARK: robot at least partly inside own LOADING ZONE | 5 | 5 | End of AUTO; end of match |
| **HIVE TIP** | **20** | **20** | Any time, until everything is at rest after the match. A tip that finishes before TELEOP starts (even in the 8 s transition) counts as AUTO (M p.86) |
| Ball left in your **up** CELL at the end | – | 2 each | At rest after the match |
| FLOWER you own: each POLLEN/NECTAR in its scoring volume | – | 2 each | End, at rest |
| Bottom NECTAR Bonus (per FLOWER) | – | 5 | End, at rest |
| Ball in your GARDEN | – | 1 each | End of TELEOP, at rest |
| MINOR foul (points go to the opponent) | 5 | 5 | |
| **MAJOR foul (points go to the opponent)** | **20** | **20** | One MAJOR = one TIP given away |

Scoring is done by volunteers watching. Make every achievement "obvious and unambiguous" (M p.86).

---

## 3. Ranking points (RP)

RP per qualification match (Table 10-3, M p.91). Maximum 6.

| RP | Need (League Meets, Qualifiers, League Tournaments) | Notes |
|---|---|---|
| WIN / TIE | 3 / 1 | |
| SWARM | alliance LEAVE + PARK points ≥ **16** | Needs both robots: one robot can earn at most 13 |
| POLLINATOR 1 | ≥ **4** TIPS | 3.4% of US alliances so far (FTCScout) |
| POLLINATOR 2 | ≥ **7** TIPS | 0% of US alliances so far (FTCScout) |

State championship (Regional Championship) thresholds are "TBA in a Team Update" (Table 10-3). Point values are fixed and no new RP types will be added (QA14). "TIPS" means tips of **your own** HIVE: that is our reading of the manual; ask the Head Referee to confirm ([09](09-rules-cheatsheet.md#ask-the-head-referee)).

### SWARM combinations (arithmetic from Table 10-2)

| Robot 1 | Robot 2 | Total | SWARM? |
|---|---|---|---|
| LEAVE + AUTO PARK (8) | LEAVE + AUTO PARK (8) | 16 | Yes, locked in AUTO |
| LEAVE (3) + end PARK (5) | LEAVE (3) + end PARK (5) | 16 | Yes, no slack |
| LEAVE + AUTO PARK + end PARK (13) | end PARK only (5) | 18 | Yes |
| LEAVE + AUTO PARK + end PARK (13) | nothing | 13 | **No**: a dead partner means no SWARM |
| end PARK (5) | LEAVE + end PARK (8) | 13 | **No** |

A robot touching **any** wall at the end of AUTO gets no LEAVE, even if it is parked (QA21). A partner pushing you off the wall still counts (QA28).

### Ranking order (Table 13-1, M p.155)

1. Ranking Score (average RP per match)
2. Average match points **without** foul points
3. **Average TIPS**
4. Average AUTO points

Tips appear twice in this list, as points and as a tiebreaker.

### League Tournament ranking (M p.167)

The League Tournament ranks you on your **top 10 League Meet matches plus** your League Tournament matches. Missing meet matches count as **zero**. Meets play 5 or 6 matches each, so a team must play at least 2 meets to have 10 matches. See [00 calendar](00-master-plan.md#michigan-calendar-confirm-with-fim).

---

## 4. How many balls tip the HIVE

### Official calibration (EFSG §12.3, repeated as FAC 2.3–2.6)

Every event HIVE is tuned with washers so that:

| CELL already holds | Next ball | Must happen |
|---|---|---|
| 3 NECTAR + 1 POLLEN | 2nd POLLEN tossed in | **No** tip |
| 3 NECTAR + 2 POLLEN | 3rd POLLEN tossed in | **Tip** |
| 6 POLLEN | 7th POLLEN tossed in | **No** tip |
| 7 POLLEN | 8th POLLEN tossed in | **Tip** |

(A *gently placed* 3rd or 8th ball tipping is only "preferred"; a *tossed* one tipping is "necessary".)

### What that means for us

| Situation | Balls needed |
|---|---|
| **First tip**: each alliance's starting up CELL already holds its 3 own NECTAR (M p.84) | **≈3 POLLEN**. One robot's 4 preloads are enough, with one spare |
| **Every later tip**: the newly raised CELL is empty | **≈8 POLLEN**, or 3 NECTAR + 3 POLLEN |
| One robot may hold at most 4 balls (G407) | **Two full loads = one tip** after the first |
| Exactly 7 in the CELL | Must **not** tip. 14 points if it is still there at the end, but no 20-point tip |

- POLLEN is 2.8 in and 0.055 lb; NECTAR is 3.6 in and 0.091 lb (AM). One NECTAR weighs about 1.5–1.65 POLLEN.
- Mixes the calibration does not list, by weight (est.): 2 NECTAR + 5 POLLEN and 5 NECTAR should tip; 1 NECTAR + 6 POLLEN and 4 NECTAR + 1 POLLEN are borderline. Community charts disagree; only the table above is official.
- A launched ball's push is worth at most about a quarter of a POLLEN (est.). **Plan tips by count, not by how hard you shoot.**
- Real HIVEs vary: "Subtle variations may occur" (FAC 2.1); placement and HIVE motion change the count (QA12). A tip scores when the damper touches the frame, however many balls it took (QA12).
- **Do not shoot at a HIVE that is already tipping.** Launching at a moving HIVE can stop the tip (M p.87, QA12).

### Tip timing (community measurement)

From frame-by-frame video of 11 AUTO tips at the Saline (MI) event (MonaShores):

| Phase | Time |
|---|---|
| Threshold ball lands → HIVE starts to move ("dwell") | 0.25–3.4 s, typically ~2 s. Seemed shorter when the volley went past the threshold (weak evidence) |
| Swing to the other side | 0.65–0.9 s |
| Threshold ball → spilled balls on the tiles | about 3 s |
| One slow tip, frame-stepped | about 5.8 s in total |

Plan for **~3 s, and allow up to 6 s.**

---

## 5. Why tips win (early-season data)

US match data, 11 events, 176 matches, 19 Sep – 17 Oct 2026 (FTCScout). These were scrimmages and preview events.

| Fact | Value |
|---|---|
| Alliance with more TIPS won | **102 of 104** matches where tip counts differed (98%) |
| Win rate by alliance TIPS | 0 → 28% · 1 → 49% · **2 → 78%** · **3 → 90%** · 4+ → 100% |
| Share of all non-penalty points from the HIVE | about 60% |
| Median alliance score (no penalties) | 35 points (90th percentile 74, max 128) |
| Average TIPS per alliance per match | 0.9; 44% of alliance-matches had **zero** |
| AUTO tip | 18% of alliance-matches; **no US alliance has tipped twice in AUTO** |
| Best US alliance AUTO | **36** = 1 tip + 2 LEAVE + 2 AUTO PARK. Only ~7% of alliance-matches had both an AUTO tip and LEAVE+PARK ≥ 16 |
| Robots that LEAVE / AUTO PARK / end PARK | 58% / 45% / 67% |
| FLOWERS | Almost unused: 1 of 704 US end-of-match FLOWER states had any NECTAR (4 of 924 including Kazakhstan) |

**Michigan.** At the Saline Preview Event (2–3 Oct, 28 mostly middle-school teams), the winning teams 13684 and 10644 averaged 3.8 and 3.6 tips per match and over 100 points. The median team averaged about 1.3 tips and 50 points (FTC Events, FTCScout). Michigan middle schools are already ahead of the US average.

**Benchmark from Washington:** "5 tips is a pretty good benchmark for consistent early season wins … score a tip every 30 seconds" (CD-14343).

Caveat: one tip is 20 points against a median score of 35, so "more tips wins" is partly arithmetic. It is still the clearest signal we have.

---

## 6. The field in our coordinate frame

We use the official FTC field frame for BIOBUZZ (FTC docs field-coordinate page; the manual puts red on the audience's left, M p.68):

- **Origin** at the field centre, on the tile surface.
- **+X points toward the AUDIENCE wall.** −X points toward the far wall (the scoring-table side).
- **+Y points toward the BLUE wall.** Red is on the −Y side.
- **Heading 0° = facing the audience wall**; angles increase counter-clockwise (90° faces blue, 180° faces the far wall).
- Inches everywhere.

**Blue = red rotated 180°:** (x, y, heading) → (−x, −y, heading + 180°). The field is rotationally symmetric, **not mirrored**: a mirror puts FLOWERS where none exist (CAD).

### Quick map (top view, not to scale; audience at the bottom, like the manual's figures)

```
                         FAR WALL  (X = -70.7)
           A          B          C    :     D          E          F
      +----------+----------+----------:----------+----------+----------+
  6   | S5       | S7      @F   S6     :          |          | blue     |
      |          |  (F-far) |  (B2)    :          |          | GARDEN   |
      +----------+----------+----------:----------+----------+----------+
  5   | RED LZ   |   ZONE B (red, after an odd number of red tips)      @F
      |  (park)  | corridor y≈-40      :          |          | (F-blue) |
      +----------+----------+----------:----------+----------+----------+
  4   |          |          | red FAR  : blue FAR |          |          |
      |          |          | cell     : cell UP  |          |          |
      +----------+----------+---HIVE---:---HIVE---+----------+----------+
  3   |          |          | red AUD  : blue AUD |          |          |
     @F (F-red)  |          | cell UP  : cell     |          |          |
      +----------+----------+----------:----------+----------+----------+
  2   |          |   ZONE A (red, start)          |          | BLUE LZ  |
      |          | corridor y≈-40      :          |          |          |
      +----------+----------+----------:----------+----------+----------+
  1   | red      | S2       |   S1     :          @F         |          |
      | GARDEN   |          |  (A2)    :          |  (F-aud) |          |
      +----------+----------+----------:----------+----------+----------+
                        AUDIENCE WALL  (X = +70.7)
   RED wall (Y = -70.7) is the left edge        BLUE wall (Y = +70.7) is the right edge
   ":" = centre line (Y = 0). In AUTO, red owns columns A-C, blue owns D-F (G402).
```

### Coordinates (red side; CAD-derived unless marked)

| Thing | Red value (inches) | Notes | Source |
|---|---|---|---|
| Walls (inner face) | X and Y = ±70.67 | Field is **141.3 in** wall to wall, not 144 | CAD; M p.64 says "approximately 144" |
| Tile seams | 0, ±23.5, ±47.0 | Columns A–F run along Y; rows 1–6 run along X | CAD; M p.67 |
| Red side in AUTO | Y ≤ 0 (columns A–C) | Blue: Y ≥ 0 (columns D–F) | G402 (M p.106) |
| Red HIVE pivot | (0, -12.74), 43.95 high | Blue pivot (0, +12.76) | CAD; M p.69 |
| **Red audience CELL aim point** (up at start) | **(16.50, -12.74), 58.27 high** | Centre of the opening; same as the SDK cluster origin | CAD; SDK |
| **Red far CELL aim point** (up after tip 1) | **(-16.48, -12.74), 58.31 high** | | CAD |
| Up-CELL opening | lip 53.4–53.5 high, top 65.6 high; about 20 in wide | Event tolerance on the lip: ±0.5 in (FAC 1.13) | M p.71–72; CAD |
| Lowest point of a down CELL | 30.6 high | Robot height limit is 29 in, so only 1.6 in of margin | TU01; R105 |
| AprilTags | red far CELL 30–33, red audience CELL 34–37, blue audience 38–41, blue far 42–45 | 36h11, 3.25 in, on the **underside** of each CELL | M p.74–77 |
| Red LOADING ZONE (tile A5) | X -46.60 … -23.91, Y -70.67 … -59.10 | On event fields it is 11.0 ± 0.125 in deep, so its edge is near Y -59.67 | CAD; FAC 3.4 |
| Red GARDEN (tile A1) | X 68.10 … 70.67, Y -70.10 … -47.41 | 4 POLLEN at (69.3, -69.3 / -66.4 / -63.5 / -60.6) | CAD; M p.83 |
| FLOWER F-red (red wall) | (23.39, -68.04) | Top opening 4.0 in wide, 21.4–21.5 in high | CAD; M p.72; FAC 1.7 |
| FLOWER F-far (far wall, red half) | (-68.04, -23.39) | | CAD |
| FLOWER F-aud (audience wall, blue half) | (68.04, 23.39) | | CAD |
| FLOWER F-blue (blue wall) | (-23.39, 68.04) | | CAD |
| HIVE footprint | X ±19.47, Y ±24.73 | Real obstacles at robot height: 4 molded feet (2.1 in tall) at the corners. The foot bars are flat 0.07 in plates you can drive over | CAD |
| Under-HIVE lane (TELEOP only) | robot centre between Y -9.5 and +9.5 | For an 18 in tall robot; narrower if taller (about ±6.6 at 29 in). Risky: G409/G417 | CAD (derived) |
| **Start S1** | **(61.67, -12.75), heading 180°** | Back touching our start wall (red: audience wall), 45.2 in in front of the red audience opening. Every start spot is square to its wall | CAD (derived) |
| Start S2 | (61.67, -37.5), heading 180° | 51.5 in from the opening, 29° off-axis (151° is the aim bearing, not the placement heading). Shoot-first routines drive to the Zone A side spot (55, -37) before firing | CAD (derived); code |
| Starts S3, S4 | (45, -61.67) and (0, -61.67), heading 90° | Back on the red wall. Shoot-first routines drive straight off the wall to y = -48, then to the side spot | code |
| Start S5 | (-61.67, -61.67), heading 0° | Far/red corner, 6 in from the LZ (A0 only) | CAD (derived) |
| **Start S6** | **(-61.67, -10.1), heading 0°** | Same spot as S1 but on the end wall opposite our start, for the second (far) CELL. **Only 1.35 in from F-far** | CAD (derived) |
| Start S7 | (-61.67, -40.0), heading 0° | Backup for S6 only with > ±32° turret travel: from here the far CELL is 31.1° off the turret centre (as built ±28°) | CAD (derived); code |
| Zone A centre "A2" | (48, -12.75) | Shooting zone while the red audience CELL is up. Its front edge is about 3 in inside the tip-1 landing box (TELEOP only; AUTO uses the side spot (55, -37)) | strategy brief; code |
| Zone B centre "B2" | (-48, -12.75) | Shooting zone while the red far CELL is up; 44 in from the solo park pose. Its front edge is about 3 in inside the TIP-2 landing box | strategy brief; code |
| Corridor between zones | (30, -42) → (-30, -40) | Clears the HIVE feet and legs by about 6 in at heading 180° (less for a robot still turning on it: ≥ 3.7 in for A2), and a robot parked in the corner slot by 3 in. Leaving S1, go through (52, -40) first to stay out of the spill landing box | CAD (derived); code route test |
| **Solo park pose (one robot)** | **(-35.25, -55)** | 4.3–4.9 in inside the LZ, 6.7 in off the red wall: earns PARK **and** LEAVE. It touches both two-robot slots, so use it only when the partner does not park in AUTO | CAD (derived); QA21 |
| Park slots (two robots) | corner slot (-51, -56), inner slot (-27, -56) | The corner slot is the LZ end nearer the field corner (red: far-wall end; blue (51, 56): audience end). Robots 6 in apart, 5.67 in off the wall, each 5.3 in into the LZ from its edge on an event field. The first robot to park takes the corner slot | derived ([02](02-autonomous.md#poses-red)) |

---

## 7. Which way the CELLS face (read this twice)

- A CELL is a box lying along the HIVE arm. **Its open end is the outer end, facing the end wall nearest to it (audience or far wall), tilted 30° upward.** It does not face up, and it does not face the alliance walls (M p.71; EFSG; CAD).
- At the start, the **red audience CELL** is up. Its opening faces the **audience wall (+X)**, so red shoots from the **audience half** (Zone A). Blue's far CELL is up and faces the far wall, so blue shoots from the far half (M p.83).
- **After every tip the other CELL comes up, facing the opposite end wall. Your shooting zone flips** (Zone A ↔ Zone B).
- Good shots come from in front of the opening, within about **40° of head-on**. Steep lobs from wider angles can still go in, but the scorable area shrinks quickly (est.; CAD-based model). From behind, you hit the CELL's back or roof. Deliberately launching at the outside of a CELL can be a G417 violation (M p.111–112).
- The 4 AprilTags of each CELL are on its **underside**. All 8 tags of a HIVE face down and toward the side you shoot from. **After your HIVE tips, its tags face the other half of the field**, so a robot that stays put loses sight of them. Our camera therefore cannot see our own tip from the zone we fired from: in TELEOP the operator presses Y ("HIVE tipped") when the human player calls "TIP!" ([03](03-teleop.md#the-two-zone-loop)).
- If your HIVE and the opponent's HIVE are in different states, **both alliances end up shooting from the same half**. Expect traffic and defence there.

---

## 8. Spill: where the balls go after a tip

- On a tip, the CELL you just filled swings down. Its contents pour out of the opening **toward the wall it faced, which is the zone you were just shooting from** (CAD).
- Red's **first** spill holds the 3 red NECTAR plus the POLLEN that tipped it.
- **Landing box** (first contact with the floor) for the red HIVE. Uncertainty is about ±10 in (CAD model plus MonaShores video):

| Tip | Balls land at (first contact) | Then they roll |
|---|---|---|
| Red audience CELL dumps (red tips 1, 3, 5 …) | X +24 … +42, Y -28 … +2 | Toward the audience wall; mostly columns B–C, some into D |
| Red far CELL dumps (red tips 2, 4, 6 …) | X -42 … -24, Y -28 … +2 | Toward the far wall, near F-far and the red LZ |
| Blue: rotate the boxes 180° | X -42 … -24, Y -2 … +28 (blue tip 1) | |

- Measured at Saline: first touch about 35–42 in from that end wall on 2 clean spills; balls spread 2–3 ft sideways within 0.3 s and mostly stop against the wall or robots within ~2 s. NECTAR rolls farther than POLLEN (MonaShores).
- **G409:** a robot may not catch or deflect a spilling ball until that ball has touched something else. **A ball bouncing off a robot under a tipping HIVE counts as a violation** (QA17). Stay out of the landing box for ~3 s after the threshold ball (allow up to 6 s). Then the spill is free ammo in your own zone.
- Part of the red spill band (Y up to +2) is on blue's side. In AUTO, blue has priority over balls on its side (G402), so pick up only at Y ≤ -2.

---

## Sources

- **M p.N, G/R rules**: Competition Manual TU05 (2026-10-15), https://ftc-resources.firstinspires.org/ftc/game/manual
- **QA n**: https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/n (QA12, QA14, QA17, QA21, QA28)
- **TU01, TU04**: https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **EFSG §12**: Event Field Setup Guide V1.0, https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **FAC**: Field Acceptance Checklist rev 26-27.2, https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
- **CAD**: official field STEP CAD, https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step (coordinates extracted and cross-checked by our research)
- **SDK**: FtcRobotController v12.0 README, https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md
- **Field frame**: https://ftc-docs.firstinspires.org/en/latest/game_specific_resources/field_coordinate_system/field-coordinate-system.html
- **AM**: AndyMark scoring elements, https://andymark.com/products/biobuzz-scoring-elements
- **FTCScout**: https://ftcscout.org (US events with data as of 2026-10-20; Saline: https://ftcscout.org/events/2026/USMISAS)
- **FTC Events (Saline rankings)**: https://ftc-events.firstinspires.org/2026/USMISAS/rankings
- **MonaShores** (community, frame-by-frame): https://github.com/Mona-Shores-FTC-Robotics/biobuzz (issue #167, `doc/` folder)
- **CD-14343** (community): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
