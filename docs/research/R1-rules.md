# R1 — BIOBUZZ Rules Digest (strategy-focused) for FTC 12303

Prepared 2026-10-20 for the coach of FTC 12303 (Shumate Sith, Rockwood MI, home region "Michigan - FiM"). Scope: what the rules allow and forbid, what scores, and what is still unsettled. It does not recommend a strategy; later work will use this file to build one.

## 0. Sources, versions, conventions

| Tag | Source (version / date) | Where |
|---|---|---|
| **M p.N** | Competition Manual **TU05** (2026-10-15), 175 pp. Local PDF in scratchpad `manual/manual.pdf`; official copy at https://ftc-resources.firstinspires.org/ftc/game/manual | page N |
| **TU00–TU05** | Team Updates 00 (09-12), 01 (09-17), 02 (09-24), 03 (10-01), 04 (10-08), 05 (10-15) | https://ftc-resources.firstinspires.org/ftc/game/tu-0N (combined: /ftc/game/tu-combined) |
| **QAn** | Official Team Q&A board, BIOBUZZ, read 2026-10-20. Q1–Q38 exist: 26 answered, 12 pending ("ACCEPTED", no answer yet) | https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/n (feed: /boards/2027/FTC/QA/answers.atom) |
| **FAC** | Field Acceptance Checklist rev 26-27.2 (2026-10-16) | https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check |
| **EFSG** | Event Field Setup Guide V1.0 (2026-09-12), incl. §12 HIVE Calibration | https://ftc-resources.firstinspires.org/ftc/archive/2027/field/eventfieldguide |
| **FMG** | Field Mitigation Guide rev 26-27.1 (2026-10-16) | https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide |
| **FRG** | Field Reset Guide rev 26-27.1 | https://ftc-resources.firstinspires.org/ftc/archive/2027/event/field-reset |
| **IC** | Inspection Checklist rev 26-27.1 (2026-10-09) | https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check |
| **IQR** | Inspection Quick Reference rev 26-27.1 (2026-09-14) | https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-reference |
| **G1P** | Game One Page rev 26-27.1 | https://ftc-resources.firstinspires.org/ftc/archive/2027/event/game-one-page |
| **AM** | AndyMark BIOBUZZ Scoring Elements product data (diameters and weights) | https://andymark.com/products/biobuzz-scoring-elements |
| **SWYFT** | SWYFT Drive V2 product page | https://swyftrobotics.com/products/swyft-drive-v2 |
| **FiM** | FIRST in Michigan 2026-27 FTC events page | https://firstinmichigan.us/FTC/events/ |

How much weight each source carries (M p.17-19, §1.7):
- Rule text beats orange-box text.
- The manual beats the Q&A ("responses in the Q&A do not supersede the text in the manual").
- Head Referees and Lead Robot Inspectors make the final call at each event (T401, M p.150; M p.23).
- Team Updates published after an event's drivers meeting do not apply to that event (M p.19).

Markings used in this file:
- **(V)**: verified in the cited primary text.
- **(I)**: my inference or analysis. Treat these as unverified.

The text extraction of the manual drops some digits, so every number used here (AprilTag IDs, servo limits, laser standard, ranking table, drive-team table, audio cues) was checked against the rendered PDF pages.

---

## 1. One-page driver / human-player / coach cheat sheet

**Clock (go by the FIELD timer; audio cues are only a courtesy, M p.79 §9.11)**

| Field timer | What happens | Audio |
|---|---|---|
| 2:30 → 2:00 | **AUTO, 30 s.** Drivers give no input; only start, stop, or safety actions are allowed (G401) | Cavalry charge / buzzer ×3 at end |
| 8 s transition | **No powered movement at all** (G403). Pick up controllers. Only INIT or START TeleOp if INIT/START moves nothing | "Drivers, pick up your controllers, 3-2-1" |
| 2:00 | **TELEOP** starts | 3 bells |
| **1:00** | **NECTAR may now enter FLOWERS** (G410). **Human may enter ALL remaining NECTAR** (G426.B) | Chime (TU04) |
| 0:20 | Reminder only. No rule changes at 0:20 | Train whistle |
| 0:00 | **Stop everything** (G404). Press ■. End-game PARK, FLOWERS, GARDEN and CELL contents are scored once everything is at rest | 3-s buzzer |

**Before the match (G304, R102, 10.3.4)**
- Robot fits in an 18 in cube and is motionless after INIT.
- Fully on your own side: red = columns A–C, blue = D–F.
- Touching the perimeter wall.
- Not touching a FLOWER or inside its scoring volume.
- Not in a LOADING ZONE.
- Touching **exactly 4 pre-load POLLEN**. They can be in or on the robot, or on tiles touching it on your own side.
- An OpMode is selected and INIT pressed; an AUTO OpMode needs the 30 s timer on (G305).

**Drivers**
- AUTO:
  - Stay on your own side. In AUTO the elements and FLOWERS on the other side belong to the opponent (G402, MAJOR FOUL even if accidental).
  - End AUTO **not touching any wall** (LEAVE, 3 pts; QA21) and, if possible, **partly inside your own LOADING ZONE** (AUTO PARK, 5 pts).
- Scoring in the HIVE:
  - Shoot only into **your own HIVE's UP cell**.
  - After each TIP the *other* cell is up. Red alternates audience cell (tags 34–37) and far cell (30–33). Blue alternates far cell (42–45) and audience cell (38–41) (M p.76; FRG).
  - Pause shooting while your HIVE is mid-tip, so the tip completes (M p.87 box; QA12).
  - Never touch the HIVE frame or cells (G417). Never shoot at the opponent HIVE to stop it tipping (G417.D).
  - Stay out from under a HIVE that is about to tip. Falling elements may not hit or bounce off you before they touch something else (G409; QA17).
- Element control:
  - **Never control more than 4 elements** (G407). Pushing a pile with a flat or concave face counts as control ("herding"). There is no "momentary" allowance (QA3).
  - **Never intake, hold or herd opponent-color NECTAR** (G408). There is no momentary allowance (QA4). Repeated incidents are treated as STRATEGIC even with an auto-reject sorter (TU05).
- FLOWERS:
  - Take POLLEN **only out of the bottom retrieval opening** (G418.B). It is legal in any flower during TELEOP; in AUTO, own side only (G402).
  - Put elements **only into the top** (G418.A).
  - Never pull through the side, shake the flower, force NECTAR out, ram the wall, or hold NECTAR against the pipes (G418 box).
- Last 60 s:
  - The **first NECTAR** in a FLOWER earns **+5** ("bottom-most").
  - The **top-most NECTAR owns the FLOWER**: 2 pts for every POLLEN and NECTAR inside, whoever put them there.
- Contact:
  - Pin limit is **3 s**; separate by 2 ft (G421).
  - Do not damage, tip or entangle opponents (G419, G420).
- End: be **partly inside your own LOADING ZONE at 0:00** (PARK 5). Press stop at the buzzer.

**Human player (a student drive-team member; the coach may NOT handle elements, G424)**
- NECTAR entry timing (G426):
  - **One NECTAR per TIP of your own HIVE.** AUTO tips count, and you may enter it during AUTO (QA11).
  - At ≤1:00 on the field timer, **all remaining NECTAR**.
  - You do not have to enter it right away.
- **Release the NECTAR before it touches anything.** It must hit the TILE inside your LOADING ZONE before it touches a robot or field element (G427, QA6). After that you may throw or roll it toward your robot (QA7).
- Never touch a robot, an element on a tile or robot, or a field element (G425). Never remove elements from the field (G428).
- Retrieve your own NECTAR that left the field only if you can reach it from the ALLIANCE AREA (G422 box). Hand any POLLEN or opponent NECTAR that leaves the field to FIELD STAFF (G427 box).

**Point values** (M p.91)

| Item | Points |
|---|---|
| LEAVE | 3 |
| PARK | 5 at end of AUTO + 5 at end of match |
| HIVE TIP | **20** (AUTO or TELEOP) |
| Element left in your UP cell at end | 2 each |
| Element in a FLOWER you own | 2 each |
| Bottom NECTAR Bonus | 5 per flower |
| Element in your GARDEN | 1 each |
| MINOR foul (credited to opponent) | 5 |
| **MAJOR foul (credited to opponent)** | **20 (= one TIP)** |

RP: WIN 3, TIE 1. SWARM = LEAVE+PARK ≥ 16. POLLINATOR 1 = ≥4 TIPS. POLLINATOR 2 = ≥7 TIPS (regular events).

**Tipping rule of thumb** (official field calibration, EFSG §12.3 / FAC 2.3–2.6)
- First tip: the starting UP cell already holds 3 NECTAR, so roughly **3 POLLEN** tip it.
- Every later tip, from an empty cell: about **8 POLLEN**, or 3 NECTAR + 3 POLLEN.

---

## 2. Scoring, RP math, ranking, playoffs, advancement

### 2.1 Point table (Table 10-2, M p.91) (V)

| Achievement | AUTO | TELEOP/end | Assessed when (M p.86 §10.5) |
|---|---|---|---|
| LEAVE: robot no longer touching the perimeter wall | 3 | – | End of AUTO only (QA21) |
| PARK: robot at least partially in the LOADING ZONE | 5 | 5 | End of AUTO; end of match |
| HIVE TIP | 20 | 20 | Throughout the match and until everything is at rest after the match. Tips completed before TELEOP starts (including during the 8 s transition) count as AUTO |
| POLLEN/NECTAR remaining in an upward-facing CELL | – | 2 each | After everything is at rest at the end |
| Bottom NECTAR Bonus (per FLOWER) | – | 5 | Throughout the match; final count at end at rest |
| POLLEN/NECTAR in an owned FLOWER | – | 2 each | Same as above |
| POLLEN/NECTAR in GARDEN | – | 1 each | End of TELEOP, at rest |
| SWARM RP: alliance LEAVE + PARK points ≥ threshold | | 1 RP | |
| POLLINATOR 1 RP: TIPS ≥ threshold | | 1 RP | |
| POLLINATOR 2 RP: TIPS ≥ threshold | | 1 RP | |
| WIN / TIE | | 3 / 1 RP | |

**RP thresholds (Table 10-3, M p.91) (V)**

| Event level | SWARM | POLLINATOR 1 | POLLINATOR 2 |
|---|---|---|---|
| Regular ("All Other") events, i.e. League Meets, Qualifiers, League Tournaments | 16 points | 4 TIPS | 7 TIPS |
| Regional Championship (e.g., a state championship) and FIRST Championship | TBA in a Team Update | TBA | TBA |
| Premier Events | Set their own | Set their own | Set their own |

QA14 (V) confirms:
- No new RP types will be added.
- Match point values are fixed.
- Only the thresholds (and, for Championship and Premier events, the scoring elements) may change.

**Scoring is done by human volunteers.** Errors in the live score are not an ARENA FAULT. Make every achievement "obvious and unambiguous" (M p.86 box).

**Fouls (Table 10-4, M p.92)** (V):
- MINOR = 5 pts credited to the opponent. MAJOR = 20 pts credited to the opponent.
- YELLOW CARD = warning; a second yellow becomes a RED. RED = disqualified for the match.
- In qualifications a disqualified team gets 0 match points and 0 RP (its partner is unaffected, T601).
- In playoffs, cards and disqualifications apply to the whole alliance (M p.95, p.156).

### 2.2 HIVE: how a TIP works (V unless marked)
- **Definition** (M p.87, glossary M p.175): the HIVE moves to its other stable state, the down cell becomes the up cell, and the previously free damper touches the frame. Volunteers judge by eye.
- **The only legal way to tip** is LAUNCHING POLLEN or NECTAR into the **upward-facing CELL** (M p.87 box; G417).
- **NECTAR counts toward tipping** (M p.69: "until enough POLLEN or NECTAR are LAUNCHED into the upwards-facing CELL"; G417 box).
- **Starting state** (M p.83; FRG E.i; EFSG 11.1):
  - Red HIVE: audience-side cell up, holding 3 red NECTAR.
  - Blue HIVE: far-side ("scoring"-side) cell up, holding 3 blue NECTAR.
  - The cells pointing at the audience-wall and far-wall FLOWERS start down.
- **Official calibration — the tip threshold.** EFSG §12 says each HIVE is tuned to tip with **8 POLLEN**, or with **3 POLLEN + 3 NECTAR**, gently placed at the back of the cell. The required results (EFSG §12.3) are:

  | Cell already holds | Next element | Required result |
  |---|---|---|
  | 3 NECTAR + 1 POLLEN | 2nd POLLEN tossed in | No tip (necessary) |
  | 3 NECTAR + 2 POLLEN | 3rd POLLEN gently placed | Tip (preferred) |
  | 3 NECTAR + 2 POLLEN | 3rd POLLEN tossed in | Tip (necessary) |
  | 6 POLLEN | 7th POLLEN tossed in | No tip (necessary) |
  | 7 POLLEN | 8th POLLEN gently placed | Tip (preferred) |
  | 7 POLLEN | 8th POLLEN tossed in | Tip (necessary) |

  - FAC 2.3–2.6 repeats this as a pre-event functional test: 3 NECTAR + 2 POLLEN → no tip; one more tossed POLLEN → tip. 7 POLLEN → no tip; one more tossed → tip.
  - FMG: "some variation… is expected"; fields prioritize consistency between the red and blue HIVE on the same field.
  - QA12: placement inside the cell and HIVE motion cause variation. A TIP scores when the damper touches the frame, however many elements it took.
- **Element masses (AM)** (V): POLLEN 2.8 in, 0.055 lb. NECTAR 3.6 in, 0.091 lb. One NECTAR ≈ 1.65 POLLEN.
  - (I) The threshold is therefore about 0.39–0.44 lb of contents: 7 POLLEN = 0.385 lb does not tip; 8 POLLEN = 0.44 lb and 3 NECTAR + 3 POLLEN = 0.438 lb do.
  - (I) About 5 NECTAR alone (0.455 lb) should tip; 4 NECTAR (0.364 lb) probably will not.
  - (I) The first tip needs about 0.15 lb more: ~3 POLLEN or 2 NECTAR.
  - (I) Elements that bounce out of the cell do not count.
- **What happens on a tip** (M p.81 §10.1; M p.69):
  - The up cell turns down and spills its contents onto the field.
  - The alliance may enter one more NECTAR (G426.A).
  - No robot may catch or deflect the spilling elements until they touch something else (G409).
  - Launching at a cell while the HIVE is mid-tip can stop the tip; pause if needed (M p.87 box).
- **Leftover in the cell:** elements in your up cell at the end score 2 each.
- (I) Example: 7 POLLEN sitting in the cell = 14 pts. One more POLLEN would turn that into a 20-pt TIP plus a NECTAR unlock, but the spilled elements no longer score.

### 2.3 FLOWER: how scoring works (V unless marked)
- **Geometry** (M p.72-73; FMG; FAC 1.7):
  - Top opening 4.0 in wide, 21.5 ± 0.25 in above the tiles, with a 1.25 in backstop.
  - The middle ring sits about 4 in above the tiles: bottom of middle ring to top of top ring = 17.5 ± 0.125 in (FMG).
  - Bottom retrieval opening 3.55 in tall × 3.57 in deep. Bottom-ring hole 2.79 in.
  - Four pipes between the top and middle rings.
- **Scoring volume:** between the top ring and the middle ring. An element counts if it is **at least partially** inside (M p.88).
  - A NECTAR resting on the middle ring counts.
  - An element sitting entirely above the top ring does not (Figure 10-5 G, M p.89).
- **NECTAR cannot pass the middle ring; POLLEN can** (M p.112 G418 box).
  - FAC 2.8–2.10: with 2 POLLEN under a NECTAR, both POLLEN can be pulled out the bottom and the NECTAR stays. POLLEN loaded on top of a NECTAR stays on top of it.
  - FAC 2.11: POLLEN cannot escape except through the top.
- **Owner** (M p.88): the alliance whose NECTAR is **top-most** in the scoring volume. The owner scores **2 pts for every POLLEN and NECTAR in the volume**, whoever placed them.
- **Bottom NECTAR Bonus** (M p.88): **5 pts** to the alliance with the **bottom-most** NECTAR in the volume. This is separate from ownership; one alliance can have the bonus while the other owns the flower (Figure 10-5 C, E–H).
- **Unowned flower** (no NECTAR in the volume): scores **0**. There is no ownership without NECTAR.
- **Starting contents:** 4 POLLEN per FLOWER, stacked from the tile up, zig-zagging (EFSG 11.2).
  - (I) About 2 sit below or at the middle ring, so roughly 2–3 are in the volume.
- **Capacity:** the Figure 10-5 examples count 4–8 elements in the volume; the full example D has 1 NECTAR + 7 POLLEN = 8.
  - (I) A full owned flower is worth about 14–16 pts, plus 5 if you also have the bottom NECTAR.
  - (I) All four flowers with bottom bonuses is roughly 75–85 pts theoretical.
- **Timing:** NECTAR may enter the scoring volume only in the last 60 s; MAJOR FOUL per NECTAR (G410). The rule text limits only NECTAR; see the POLLEN question in §6. NECTAR placed early is still scored but penalized (M p.88 box).
- **Legal ways in and out:**
  - In: only POLLEN or NECTAR, only through the top (G418.A).
  - Out: only POLLEN, only through the bottom (G418.B).
  - No rule limits which FLOWER (own or opponent side) in TELEOP. In AUTO, G402 gives each alliance priority over elements on its own side.
- (I) **Pulling bottom POLLEN changes the score:**
  - Each POLLEN pulled from the bottom drops the whole column. That usually pushes one counted element below the middle ring, costing the owner 2 pts, until a NECTAR comes to rest on the middle ring.
  - POLLEN stacked above a NECTAR can never be drained.
  - Draining also opens room at the top. A NECTAR that was sitting fully above the top ring would drop in and could take ownership. An opponent can drain a "full" flower to make room for their own top NECTAR.
  - The bottom bonus cannot be taken away: NECTAR cannot be removed, and the order of NECTAR in the column cannot change.

### 2.4 GARDEN (M p.66, p.89-90) (V)
- **Location:**
  - Red GARDEN: tile **A1**, along the **audience wall** (FAC 3.5; EFSG 8.4).
  - Blue GARDEN: tile **F6**, along the **far wall** (FAC 3.6).
  - Each is about 23 in × 2 in and infinitely tall, and includes its tape.
- **Scoring:** 1 pt per POLLEN or NECTAR (either color) at least partially in the GARDEN at the end. The points go to the GARDEN's color, whoever put the element there.
- **Not protected:** either alliance may remove elements at any time (M p.89; G411 box).
- **Start:** 4 POLLEN staged in each GARDEN (M p.83).
  - (I) Pulling them out to shoot costs that GARDEN's owner 4 pts at the end unless they are replaced.

### 2.5 LEAVE / PARK / SWARM RP math
- **LEAVE** (3, AUTO only): at the end of AUTO the robot must not touch **any** perimeter wall (M p.90; QA21). Being pushed off the wall by your partner counts (QA28).
- **PARK** (5 at end of AUTO, 5 at end of match): robot at least partially inside the LOADING ZONE, which is an infinitely tall volume that includes the tape (M p.65-66, p.90 Figure 10-7). The LOADING ZONE belongs to the alliance whose ALLIANCE AREA is next to it.
- **LOADING ZONE locations:** red on tile **A5** (red wall, far end); blue on tile **F2** (blue wall, audience end). Each is 23 × 11 in (FAC 3.2–3.4; EFSG 8.3).
- **One robot's options:**
  - AUTO points: 0, 3 (LEAVE only), 5 (PARK while touching the wall), or 8 (LEAVE + PARK, in the LOADING ZONE without touching the wall).
  - End-of-match points: 0 or 5.
  - Maximum: 13 per robot, **26 per alliance**.
- **SWARM needs ≥16 (I, arithmetic):**

| Alliance combination | Total | SWARM? |
|---|---|---|
| Both LEAVE + both end PARK | 6 + 10 = 16 | **Yes (no slack)** |
| Both LEAVE + AUTO PARK (8 each), no end PARK | 16 | **Yes (no slack)** |
| Both LEAVE + AUTO PARK + end PARK | 26 | Yes (+10 slack) |
| A: LEAVE + AUTO PARK + end PARK (13); B: LEAVE only (3) | 16 | Yes |
| A: 13; B: end PARK only (5) | 18 | Yes |
| A: LEAVE + AUTO PARK (8); B: LEAVE + end PARK (8) | 16 | Yes |
| Both end PARK + only one LEAVE | 13 | No |
| Both end PARK + one AUTO PARK touching the wall (no LEAVE) | 15 | No |
| Both end PARK + one robot LEAVE + AUTO PARK (8) | 18 | Yes |
| Both AUTO PARK touching the wall + both end PARK | 20 | Yes |
| One robot only (partner dead or absent) | max 13 | **No** |

  - (I) SWARM always needs both robots. A partner with no AUTO can still get it if the other robot scores LEAVE + AUTO PARK (8) and both end-PARK (8 + 10 = 18).
  - (I) The LOADING ZONE is only 23 in wide; two robots each need just *partial* overlap, so plan who takes which end.
  - (I) The human player must drop NECTAR onto an uncovered part of the zone's tile (G427.C), so a robot parked in the zone can block NECTAR entry, especially in the last 60 s.

### 2.6 POLLINATOR RP math (I, from EFSG §12 calibration and AM masses)
- "TIPS" means tips of your own HIVE. This is my reading: the manual's RP rows only say "the number of TIPS", but TIP points and NECTAR unlocks are tied to the HIVE of the alliance's color (M p.81; G426.A; QA11).
- **POLLEN-equivalents needed** (1 NECTAR ≈ 1.65 POLLEN), assuming every launched element stays in the cell:

| Tips | POLLEN-equivalents |
|---|---|
| 1 | ~3 |
| 2 | ~11 |
| 3 | ~19 |
| **4 (POLLINATOR 1)** | **~27** |
| 5 | ~35 |
| 6 | ~43 |
| **7 (POLLINATOR 2)** | **~51** |

- **Supply:**
  - 40 POLLEN on the field: 16 pre-loads, 16 in FLOWERS, 8 in GARDENS (M p.83).
  - 8 own NECTAR: 3 start in the up cell; 5 are entered one per tip, or all at 1:00.
  - Every tip spills its contents back onto the field to be collected again.
  - Opponents may take spilled POLLEN but may never control your NECTAR (G408). NECTAR is "safe ammo" that weighs 1.65× a POLLEN.
- **AUTO supply on your own side:**
  - Own pre-loads: 8 per alliance.
  - Own two FLOWERS: 8 POLLEN, all removable through the bottom if no NECTAR is in them.
  - Own GARDEN: 4.
  - Total 20, plus whatever spills from your own tips.
- Other rules that bound tipping:
  - 4-element carry limit per robot (G407).
  - Pause while the HIVE is mid-tip.
  - Each tip flips which cell is up, so the aim point moves about 19 in toward the audience or far wall.

### 2.7 Ranking (M p.154-155 §13.6.3) (V)
- RP per qualification match: WIN 3 or TIE 1, + SWARM 1, + POLLINATOR 1, + POLLINATOR 2. **Maximum 6.**
- **RANKING SCORE** = average RP per qualification match. SURROGATE matches are excluded. A disqualified match counts 0 for every sort criterion.
- **Tiebreakers (Table 13-1):**
  1. RS
  2. Average alliance match points **excluding foul points**
  3. **Average number of TIPS**
  4. **Average AUTO points**
  5. Random
- **Matches per event:** 5 or 6 qualification matches per team (M p.153). League Meets also play 5–6 (M p.167).
- **Schedule:** the algorithm assigns partners. A SURROGATE match is always the team's 3rd match and does not count toward ranking, but cards from it still carry forward (M p.154).
- **League play (Section 14, M p.167):**
  - League Meets have only qualification matches.
  - Every team should play at least 10 League Meet matches.
  - League Tournament rankings combine each team's **top 10 League Meet matches** (sorted by Table 13-1) with its League Tournament matches. Missing matches count as zeros.
  - Verbal warnings and cards clear after each League Meet.
  - One league per team per season.
- **Cards** (M p.93-94): a YELLOW carries into later matches; a second YELLOW is a RED. Cards clear at the end of qualifications.

### 2.8 Playoffs (M p.156-163) (V)
- **Alliances of 2:** the alliance lead plus 1 pick. There are no backup robots (T703); every team plays every round.
- **Number of alliances** depends on teams that played qualifications: 4–10 teams → 2; 11–20 → 4; 21–40 → 6; 41–64 → 8 (Table 13-2).
- **Double elimination.**
  - The higher seed is red in round 1.
  - Ties are replayed.
  - 2-alliance events play finals of up to 3 matches.
  - Larger brackets play a finals match plus an "if required" match (Tables 13-3 to 13-6).
- At least 8 minutes between a team's playoff matches (T406).
- Playoff cards apply to the whole alliance; two yellows become a red, which disqualifies the alliance (M p.95).
- **Alliance selection** (T701): send 1–2 STUDENT representatives or the team is ineligible. A team that declines an invitation cannot be picked later (T702).

### 2.9 Advancement (M p.27-31, §4) (V)
- **Which events count:** teams advance only from events in their HOME REGION, and only from their **first three** entry-level events (Qualifier or League Tournament) in chronological order.
- **How the region sets spots:** the regional partner (FiM) sets the advancement numbers.
- **Advancement points (Table 4-1):**

| Category | Points |
|---|---|
| Qualification rank | 2–16 (inverse-error-function curve) |
| Alliance lead or pick | 21 − alliance/draft number |
| Playoff: winner | 40 |
| Playoff: finalist | 20 |
| Playoff: 3rd | 10 |
| Playoff: 4th | 5 |
| Inspire Award 1st / 2nd / 3rd | 60 / 30 / 15 |
| Other judged award 1st / 2nd / 3rd | 12 / 6 / 3 |

- **Advancement tiebreakers (Table 4-2):** after award points, playoff points and alliance points, they use average qualification points (excluding fouls), then **average qualification AUTO points**.
- **League Tournaments** (§14): advancement uses League Tournament performance, except that qualification-rank points use the combined (top-10 meets + tournament) ranking.
- **Judged awards** (§6) require the Initial Interview (A203). The portfolio is optional but limited to a cover page + 15 pages, US Letter or A4, <15 MB, content since 2026-01-01 (A201).

### 2.10 Michigan context (FiM page, read 2026-10-20; confirm with FiM)
- **Leagues listed:** Avondale SKILLS & PLAY, Capital, **Metro Detroit**, OCCRA, South Central, West Michigan. The page lists no League Meet dates.
- **Qualifiers:** Weeks 2–6.
  - Nearby: Southgate (Nov 6–7), Bedford/Temperance (Nov 20–21), Detroit (Dec 4–5), Grosse Pointe Woods (Dec 5–6).
- **League Tournaments:** **Metro Detroit League Tournament** at Wayne County Community College, Belleville, **Dec 4–5**.
- **State events:** **MI FTC States-SE** (Howell, Dec 12–13) and States-NW (Midland, Dec 11–12).
- **Registration:** Round 3 event registration opens **Wed Oct 21, 7am**.
- **Team status:** FTC Events (https://ftc-events.firstinspires.org/2026/team/12303) shows 12303 with home region "Michigan - FiM", no league listed and no events yet.
- (I) The season is short: the first qualifiers are about 2.5 weeks away.

---

## 3. Rules by match phase

### 3.0 Field map essentials (M p.65-77, p.83; FAC; EFSG)
- **Grid:** 144 × 144 in, a 6 × 6 grid of roughly 24 in tiles (M p.64).
  - Columns **A–F** run from the red wall to the blue wall; the red ALLIANCE AREA is on the audience's left (M p.68).
  - Rows **1–6** run from the audience wall to the far wall (M p.67 Figure 9-5).
- **HIVE frame** at field center: 49.46 in along the red–blue axis × 38.95 in deep; pivots 43.95 in up (M p.69).
  - The red HIVE sits on the red half, the blue HIVE on the blue half; their centers are 25.5 in apart (M p.71).
  - The frame foot bars rest on top of the tiles and sink slightly into the foam (EFSG 9.2; FAC 1.8–1.9).
  - (I) The triangular end frames stand at the red and blue ends, so the space under the HIVEs opens toward the audience and far walls.
- **Cells:**
  - Each HIVE has 2 cells. Gap between cells 18.84 in; each cell 12.04 in deep; HIVE 42.91 in long (M p.71 Figure 9-9).
  - The up cell is tilted 30°. Its opening is 20 in wide × 14 in tall (pentagon), with the bottom 53.5 ± 0.5 in and the top 65.6 in above the tiles (M p.71-72; FAC 1.13).
  - The lowest point of a down cell is **30.6 in** above the tiles (TU01). The robot height limit is 29 in.
- **AprilTags** (M p.74-77, verified visually): 36h11 family, 3.25 in, four per cell, on the **underside** of each cell facing the tiles.

  | Cell | Tag IDs |
  |---|---|
  | Red far-side cell | 30–33 |
  | Red audience-side cell | 34–37 |
  | Blue audience-side cell | 38–41 |
  | Blue far-side cell | 42–45 |

- **FLOWERS:** four, on the walls at tile seams (read from Figures 9-2, 9-4, 10-2; I, medium-high confidence).

  | Side | Flowers |
  |---|---|
  | Red side | Far wall at the B\|C seam; red wall at the row 2\|3 seam |
  | Blue side | Audience wall at the D\|E seam; blue wall at the row 4\|5 seam |

- **LOADING ZONES:** red A5, blue F2. **GARDENS:** red A1 (audience wall), blue F6 (far wall).
  - (I) The layout is 180° rotationally symmetric.
- **Element staging** (M p.83-84; FRG):
  - 4 POLLEN in each FLOWER.
  - 4 POLLEN in each GARDEN, in a line from the corner nearest that alliance's ALLIANCE AREA.
  - 4 pre-load POLLEN per robot.
  - 3 own-color NECTAR in each starting up cell, against the back wall on the side nearest that alliance's area.
  - 5 own NECTAR in each ALLIANCE AREA tray.
  - A no-show robot's pre-loads go in its LOADING ZONE against the wall (M p.83).

### 3.1 Pre-match
- **Drive team** (Table 10-1, M p.82, verified visually):
  - Up to 4 people: **1 DRIVE COACH** (may be an adult) plus **up to 3 STUDENTS** acting as DRIVERS and/or HUMAN PLAYERS.
  - At most 1 non-student. Badges must be visible.
  - If the alliance cannot agree on positions, Red 1 / Blue 1 stands nearest the audience (M p.84).
- **G301 Don't delay** (M p.102):
  - Verbal warning, then a MAJOR FOUL for the upcoming match on a repeat in the same phase.
  - DISABLED if not match-ready within 2 min with no good-faith effort.
  - A MOMENTARY "wiggle test" is allowed; it must not drive or touch elements except pre-loads.
- **G302:** extra items must fit in the ALLIANCE AREA and stay under 6 ft 6 in. No wireless communication with outside the arena (C). No mimicking field AprilTags or shining lights or lasers onto the field (E, box).
- **G303:** the robot must be safe, inspected and re-inspected, the only team item on the field, and showing the correct alliance color on its SIGNS. RED CARD if a robot that fails G303.B (inspection) plays.
- **G304 setup** (M p.104):
  - A. Fully on your own side and **not overhanging the wall**.
  - B. Not attached to or entangled with field elements.
  - C. **Touching the perimeter wall.**
  - D. Not touching a FLOWER or inside its scoring volume.
  - E. Not in a LOADING ZONE.
  - F. In STARTING CONFIGURATION (R102, R103).
  - G. **Exactly 4 pre-load POLLEN**, fully on your own side (TU03).
  - H. Motionless after INIT.
  - Figure 11-1 (M p.105) shows legal starts: corners, along the far or audience wall, along your own alliance wall, even wrapped around (not touching) a FLOWER.
- **R102 / R103** (M p.122):
  - 18 × 18 × 18 in; all parts stationary.
  - **Pre-loaded elements may stick out** of the 18 in cube.
  - The robot may hold its STARTING CONFIGURATION using INIT that positions servos and motors. Beware motor heating while you wait.
- **G305:** select an OpMode and press INIT. An AUTO OpMode needs the 30 s timer enabled.
  - Teams without an AUTO should still INIT one (for example BasicOpMode) and may use auto-loading of TeleOp (M p.105 box).
- **Placement order** (M p.85): if either alliance asks, robots are placed red 1st, blue 1st, red 2nd, blue 2nd. In qualifications Red 1 / Blue 1 place first.
- **No-show:** an absent or DISABLED robot's team still earns points if the robot passed inspection and at least 1 STUDENT drive-team member is present (M p.85). At least 1 drive-team member must report to every qualification match or the team is disqualified (G203).

### 3.2 AUTO (30 s)
- **G401:** no direct or indirect interaction with the robot or Driver Station. Exceptions: start within a MOMENTARY margin of match start, stop, or safety. Verbal warning; MAJOR + YELLOW if STRATEGIC.
- **G402 No AUTO opponent interference** (M p.106):
  - The rule: "may not disrupt AUTO for the opposing ALLIANCE." **MAJOR FOUL per MATCH** even if not strategic; MAJOR + YELLOW per match if STRATEGIC.
  - Columns A–C are red, D–F blue. **Each alliance has priority over the field and scoring elements on its side.**
  - "Navigating into the opposing ALLIANCE'S side… is a risky gameplay strategy that may be seen as STRATEGIC."
  - Elements that cross the center line after deflecting off a field element or robot "will likely not be penalized."
  - Pending: QA15 asks about intaking on the other side or from opposing FLOWERS. QA24 asks to make the penalty per-instance.
- **Scoring available in AUTO:**
  - LEAVE and AUTO PARK, assessed at the end of AUTO.
  - HIVE TIPS, 20 each. Tips completed before TELEOP starts count as AUTO (M p.86 B).
  - Taking POLLEN from your own FLOWER bottoms or your own GARDEN.
  - (I) Elements put into FLOWERS or GARDENS in AUTO earn no AUTO points; they are counted only at the end.
- **Human player in AUTO:** may enter 1 NECTAR per AUTO tip of their own HIVE and must follow all human rules (QA11). (I) An AUTO routine could pick up that NECTAR from the LOADING ZONE.
- **G409 applies in AUTO.** Pending: QA35 asks whether a robot may wait with intake running just outside the spill area.

### 3.3 AUTO → TELEOP transition (8 s)
- **G403** (M p.106-107):
  - **No powered movement of the robot or any mechanism.** Verbal warning; MAJOR + YELLOW if STRATEGIC.
  - Coasting from inertia, gravity or de-energizing is fine, so a flywheel spinning down is fine.
  - You may stop AUTO, INIT or START TeleOp, **but if INIT moves or twitches anything, wait until TELEOP begins.**
  - (I) Keep servo moves out of TeleOp `init()`, or wait for the bells before pressing INIT/START.
- Achievements made during the transition may be penalized (M p.86 box).

### 3.4 TELEOP (2:00 → 1:00)
**Scoring elements**

| Rule | What it says | Violation |
|---|---|---|
| **G405** Keep elements in bounds | No deliberate ejection from the field, directly or by bouncing. Shots that leave during scoring attempts, or from robot-to-robot interaction, are not deliberate. Pending QA37: a defender deflecting shots out | MAJOR per element |
| **G406** No damage or mess | Routinely gouging, tearing or marking elements violates this (see R201). (I) A high-compression flywheel or a sharp intake is a risk | Verbal warning; MAJOR + YELLOW if STRATEGIC; DISABLED if more damage is likely |
| **G407** **≤ 4 elements controlled at once** (box text in §3.4.1) | Verbal warning; MAJOR + YELLOW per match if STRATEGIC |
| **G408** **No CONTROL of opponent NECTAR** | No momentary exception (QA4/QA23). Repeated incidents are likely STRATEGIC even with an auto-discard sorter (TU05) | Verbal warning; YELLOW per match if STRATEGIC |
| **G409** **No catching or deflecting HIVE spill** | Until the element touches something else. Box: an accidental hit while passing is not STRATEGIC. STRATEGIC examples: repeatedly parking under the HIVE waiting for a tip; a wide-open top mechanism that catches; repeatedly positioning so balls bounce in a useful direction. QA17: being bounced off while driving under = violation (verbal warning if not STRATEGIC) | Verbal warning; YELLOW per match if STRATEGIC |
| **G411** **No hoarding** | An alliance may not STRATEGICALLY stop the opponent from reaching elements. Scoring with your own elements is fine. **Corralling elements into part of the field and/or positioning a robot to block access is a violation.** The GARDEN is not protected. Pending: QA38 | MAJOR + YELLOW per match |

**Robot**

| Rule | What it says | Violation |
|---|---|---|
| **G412** Under control | Nothing the robot controls may disrupt anything outside the field or hit people | DISABLED + verbal warning; YELLOW if repeated |
| **G413** Stop when told | Stop when a referee orders DISABLE | — |
| **G414** Identifiable | Team number and color must stay visible | — |
| **G415** No grabbing field elements | No grabbing, grasping, attaching to, entangling with, or hanging from field elements. Concave shapes that wrap around a FLOWER for alignment are fine | Verbal warning; YELLOW if STRATEGIC; DISABLED if damage likely |
| **G416** Stay within R105 limits | Stay inside the expansion limits; no deliberate detaching of parts | Verbal warning; MAJOR per instance if STRATEGIC |
| **G417** **HIVE** | Only move the HIVE by LAUNCHING into an up cell (box text in §3.4.2) | Verbal warning; MAJOR + YELLOW per match if STRATEGIC |
| **G418** **FLOWER** | Enter elements only through the top; remove only POLLEN, only through the bottom (box text in §3.4.3) | Verbal warning; MAJOR + YELLOW per match if STRATEGIC |

#### 3.4.1 G407 box: what counts as CONTROL (M p.108; TU05; QA3, QA23) (V)
- **CONTROL** (glossary, M p.171):
  - The element is fully supported by, or stuck in, on or under the robot; or the robot intentionally pushes it to a desired place or direction ("herding").
  - It requires contact, directly or **through other elements**.
  - Typically either "fully supported by the robot" or "moving it in a preferred direction with a flat or concave face."
- **Counts as CONTROL:** a ball sitting in a hopper, indexer or shooter; a ball stuck on top of the robot.
- **Not CONTROL:**
  - Bulldozing: inadvertent contact while driving.
  - Deflecting.
  - A **launched** element that has left the robot (box D–F).
- **Likely STRATEGIC:**
  - Picking up 6 or more and moving them to score.
  - Repeated greater-than-MOMENTARY control of 5 or more.
  - Repeated control of 5 or more even with a "keep 4 and discard the rest" sorter (TU05).
- **Likely not STRATEGIC:** briefly controlling 5 and quickly reversing one back out. This is still a violation (verbal warning).
- **There is no momentary exception** (QA3). QA23: routine momentary over-control "as part of the ROBOT design and/or strategy" is likely STRATEGIC.
- The rule encourages guards against balls lodging on top and intakes that cannot actively pick up more than 4.
- Pending:
  - QA34: a wide intake bar with extra balls underneath.
  - QA29: lift-and-release, active expulsion, color rejection.

#### 3.4.2 G417 box: HIVE (M p.111-112) (V)
- **Likely STRATEGIC:**
  - Ramming the frame.
  - Deliberately launching at the outside faces of a cell.
  - **Impeding the TIP of an opponent's HIVE by launching at it.**
  - **Touching a HIVE directly or through a controlled element.**
  - Repeating after a warning.
- **Not STRATEGIC:** accidentally bumping the frame while collecting POLLEN; a missed shot that hits the outside of the cell.

#### 3.4.3 G418 box: FLOWER (M p.112-113) (V)
- **Not a violation:**
  - Touching the FLOWER or its contents while trying to score or remove.
  - Driving into a FLOWER and touching its contents.
- **Likely STRATEGIC:**
  - Pulling POLLEN through the side.
  - Grabbing and shaking the FLOWER.
  - Forcing NECTAR out of the middle ring.
  - Ramming the wall to knock elements out.
  - Holding NECTAR against the pipes so it meets the scoring criteria. Pending: QA30.
- **Not STRATEGIC:** knocking a POLLEN out the top while picking up off the tiles; knocking out a scored POLLEN while scoring another.

**Opponent interaction (M p.113-115).** G419 and G420 do not stack; only the most severe penalty applies.

| Rule | What it says | Violation |
|---|---|---|
| **G419** Do not damage or functionally impair an opponent | "BIOBUZZ is a highly interactive game and may include defensive and high-contact gameplay." Damage to exposed or unprotected parts (power switch, wiring, delicate intakes) in normal play is unlikely to be a violation. QA8: G419 does not restrict contact, only damage or impairment (not limited to lasting marks) | Verbal warning; MAJOR + YELLOW per instance if STRATEGIC; MAJOR + RED if STRATEGIC and the opponent can no longer drive |
| **G420** Do not tip, attach to or entangle an opponent | Entangling exposed parts in normal play is unlikely to be a violation (TU02). One frame-to-frame hit that tips a robot is not STRATEGIC | As G419; RED if STRATEGIC and CONTINUOUS (≥10 s) or the opponent can no longer drive |
| **G421** **Pin limit 3 s**, direct or against a field element | The count ends when robots are 2 ft apart for >3 s, either robot has moved 2 ft from the pin spot for >3 s, or the pinner gets pinned. (The text cites "G421.D", meaning B; see §6) | **MAJOR per instance + MAJOR every 3 s it continues** |

**Defense in practice (V)**
- No zone on the field is protected this season. The only "protected" mentions say the GARDEN is *not* protected (M p.89; G411 box).
- Shot blocking with an extension over an opponent is not prohibited in itself (QA2, QA8, QA20). The limits are G419/G420, and G407/G408 if the blocker ends up controlling the blocked balls.
- **G204 (forcing violations)** (M p.100-101): MAJOR per instance; MAJOR + YELLOW per match if repeated; the forced alliance is not penalized. Examples:
  - Ramming a robot lined up at a FLOWER at 1:05 so it drops NECTAR early.
  - Driving through the opponent's LOADING ZONE and deflecting NECTAR being entered before it lands (LOADING ZONE "protection").

**Human rules (M p.115-118)**

| Rule | What it says | Violation |
|---|---|---|
| **G422** Stay in your ALLIANCE AREA | Anywhere inside it; breaking the plane is fine. May retrieve own NECTAR that left the field if reachable | Verbal warning; MINOR per instance if STRATEGIC |
| **G423** Coach and other teams: hands off the gamepads | The coach may hold or troubleshoot the Driver Station and press OpMode, INIT, start and stop | Verbal warning; MAJOR + YELLOW if STRATEGIC |
| **G424** Coach may not touch elements | Except for safety. **So the coach cannot be the human player** | Verbal warning; MINOR per instance if STRATEGIC |
| **G425** No contact | No contact with robots, elements touching a robot or tile, or field elements, directly or via cords or clothing. No disrupting scoring, e.g., hitting or shaking the wall to knock elements out of a FLOWER. QA6: drop NECTAR, don't place it | Verbal warning; MAJOR + YELLOW if STRATEGIC |
| **G426** NECTAR entry timing | One additional NECTAR per TIP of your own HIVE; all remaining at ≤60 s (field timer). NECTAR need not be entered immediately (TU02). NECTAR that left the field "may be re-entered at any time" via G427 (TU02 box; pending QA36) | **MINOR per NECTAR** |
| **G427** NECTAR entry method | No tools; a member of the matching alliance; must touch the LOADING ZONE tile before a robot or field element. QA7: may throw or roll it in a useful direction if released first, it lands in the zone first, and it doesn't end up scoring in the HIVE or a FLOWER. "Humans scoring into a HIVE or FLOWER is never allowed" (G202) | **MINOR per NECTAR** |
| **G428** Don't remove elements from the field | An element counts as "on the field" if it touches a tile or is in or on a robot, field element or other element | MINOR per element |

**Elements that leave the field** (M p.97 §10.8.2): FIELD STAFF put POLLEN back on the field at the nearest safe spot; NECTAR goes back to its alliance's drive team for re-entry. Delays are not an ARENA FAULT.

### 3.5 Last 60 seconds (1:00 → 0:00)
- **NECTAR may go into FLOWER scoring volumes** (G410: MAJOR per NECTAR if early, measured by the field timer).
  - Make it "obvious and unambiguous" that you are not early (box).
  - (I) Keep a 1–2 s margin past 1:00.
- **All remaining NECTAR may be entered** by the human player (G426.B).
- **FLOWER play:** the first NECTAR in each flower earns +5 (bottom bonus); the top-most NECTAR owns the flower (2 per element). This applies to any FLOWER, including those on the opponent's side (no rule limits it in TELEOP). See §2.3.
- **End-game positions:**
  - Robots must be at least partly in their own LOADING ZONE at 0:00 for PARK.
  - Elements must be in your GARDEN, your up cell, or owned FLOWERS when everything comes to rest.
- **No rule changes at 0:20**; the whistle is a cue only.

### 3.6 End of match and after
- **G404:** no powered movement after the end of TELEOP until the Head Referee signals. Press ■ and put the controllers down. Coasting is fine.
  - Achievements made after 0:00 may be penalized (M p.86 box).
  - A TIP still counts if it completes after 0:00 from a shot already in flight, because tip assessment continues until everything is at rest (M p.86 A). (I)
- **Field reset** (M p.97; TU05): wait for the signal before retrieving the robot. The robot must be removable with power off, and elements must be removable from it (R203).
- **Question Box** (M p.148-149):
  - Up to 2 team members may address the Head Referee, at least 1 a STUDENT (T301).
  - Qualification questions must come before alliance selection; playoff questions before the next round (T302). Best within 3 matches.
  - **No video review** (T401).
- **Replays** (T201): only for ARENA FAULTs that change the outcome. Robot or programming problems are not grounds.

---

## 4. Robot construction constraints (Section 12; IC; IQR)

### 4.1 Size and shape
- **R102 Start:** 18 × 18 × 18 in, fully stationary; pre-loads may stick out (M p.122).
- **R103:** self-supported; may hold position with INIT-powered servos or motors.
- **R104:** no weight limit.
- **R105 Expansion** (M p.123-125):
  - After the start the robot must stay within **18 × 24 in footprint × 29 in tall**, with height measured from the field surface.
  - It must be **physically constrained**: software limits do not count, and inspectors measure maximum *mechanical* extension (Figure 12-3).
  - No deliberate detaching of parts.
  - Two independent extensions on opposite sides together must fit (Figure 12-5).
  - A pivoting horizontal extension must fit at every point of its travel (Figure 12-6).
  - (I) Figure 12-6 shows the 18 × 24 box re-oriented to follow the rotating extension, so at every turret angle the whole robot must fit inside *some* 18 × 24 rectangle. Confirm with the Lead Robot Inspector.
  - Robots are measured in a stable pose with wheels on the tiles.
- **Height implications (I):**
  - The robot's 29 in cap is below the up cell's lowest opening edge (53.5 in), so TIPs require real launching; you cannot reach in.
  - Clearance under a down cell is about 1.6 in for a 29 in robot (30.6 − 29). Touching the HIVE is G417.
  - The FLOWER top is at 21.5 in, so a mechanism within 29 in can drop elements in from above.
- **R201** No damage or mess:
  - Traction devices known to damage tiles are not allowed (IQR p.10 lists examples).
  - No sharp edges; no abrasive surfaces that scratch elements.
  - **Gouging or marking scoring elements violates this rule and G406.**
- **R202** Safety and fair play: nothing on the robot that mimics 36h11 AprilTags; bright lights only briefly while targeting; flashing above 5 Hz invites scrutiny; nothing designed to flip robots.
- **R203:** removable without power.
- **R204:** no suction or floor-grabbing.
- **R801:** no pneumatic actuators, vacuums or blowers. **Flywheels and rollers are explicitly not counted as high-speed airflow devices** (M p.144).

### 4.2 Fabrication
- **R101:** the team must build the robot and its major mechanisms.
- **R301:** COTS major mechanisms built for a game task are **prohibited**. Exceptions: COTS drive chassis, and official FTC StarterBot mechanisms (M p.127).
- **R303:** COTS parts must be single degree of freedom. Turntables, slides, single-speed gearboxes and so on are fine. Holonomic wheels and dead-wheel odometry kits are allowed exceptions.
- **R305:** no scoring elements or replicas used as robot parts.

### 4.3 Actuators
- **R501 legal motors** (M p.132) include:
  - goBILDA Yellow Jacket 5201/5202/5203/5204 series, which covers the 6000 rpm Yellow Jacket.
  - **SWYFT Robotics SWYFT Spike SR-MOTOR-DC-01.**
  - REV HD Hex and Core Hex; AndyMark NeveRest; Studica Maverick; TETRIX; NFR Yuksel; WATTOS Stingray (added in TU00).
  - Any gearbox may be used.
- **R502 servos** (verified visually, M p.133): ≤ **8 W** mechanical power and ≤ **4 A** stall current at 6 V. Linear servos ≤ 1 A.
  - **goBILDA Dual Mode Servo** is listed as an example (2000-0025-0003).
  - IQR p.4 pre-verifies **all three variants**:

    | Part number | Variant | Power | Stall |
    |---|---|---|---|
    | 2000-0025-0002 | Torque | 2.77 W | 2.5 A |
    | 2000-0025-0003 | Speed | 2.65 W | 2.5 A |
    | 2000-0025-0004 | Super Speed | 2.81 W | 2.5 A |

  - Servos not on the IQR list need manufacturer documentation.
- **R503: max 8 motors and 8 servos**, counted across every configuration (servos were cut from 12 to 8, TU00).
- **R504:** no modifying actuators. Manufacturer-specified changes (for example continuous-rotation mode, soft limits) are allowed.
- **R505:** only approved power regulators may drive actuators:
  - Control Hub or Expansion Hub ports: 2 motors per port, 2 servos per port.
  - REV Servo Hub; REV Servo Power Module; goBILDA 6V Servo Power Injector; Studica Servo Power Block (2 servos per port).
  - REV SPARKmini (2 motors per device).
  - Hub servo ports supply 5 V; the power modules and injectors supply 6 V.
- **R506:** no relays, solenoids or electromagnets.
- **R612 / IQR p.9: illegal** — goBILDA Servo Travel Tuner, goBILDA 8-channel Servo Power Distribution Board, goBILDA 4-channel servo extension over CAT6.

### 4.4 Power
- **R601:** exactly one legal 12 V NiMH battery. The list includes the goBILDA 12V NiMH **Nested** Battery 3100-0012-0020 and the Matrix 14-0014.
  - Fuse: 20 A ATM mini blade (the same rating or lower).
  - Connectors may be swapped (Anderson, XT30, etc.).
- **R602:** other batteries only for self-contained devices or LEDs. USB packs ≤100 Wh, ≤5V/5A or 12V/5A USB-PD per port. Exceptions: powered USB hubs and phones (QA1).
- **R603:** one legal main switch (Table 12-5, e.g., goBILDA Floodgate 3103-0005-0001, REV REV-31-1387), mounted where the team can reach it.
  - Protect it: G419 says an exposed power switch hit in normal play is not the opponent's foul.
- **R604:** no modified or self-resetting fuses.
- **R605:** the frame must not carry current. Grounding only via the listed straps (AndyMark am-4648a, REV REV-31-1269, **Swyft Grounding Cable SR-Ground-01**) to a fully COTS XT30 device.
- **R606–R613:** electronics inspectable; custom circuits ≤5 V regulated except for LEDs; no altered power paths and no boost/buck converters on actuator paths.
  - Wire gauges: 18 AWG for 12 V and motors; 22 AWG for servos and LEDs; 28 AWG for signals. Wires that come attached to COTS parts are exempt.
  - Colors: + red/yellow/white/brown/black-with-stripe; − black or blue.
  - Powered USB hubs: powered from a COTS pack or the hub's 5 V aux port.
  - No mixing power between ports.

### 4.5 Control system (M p.140-145)
- **R701:** one REV Control Hub plus at most **one** REV Expansion Hub. 12303's Control Hub + Expansion Hub is legal.
- **R702:** coprocessor software may not be modified. The **only** reprogrammable vision coprocessor allowed is the **Limelight 3A (LL_3A)** (Table 12-9).
  - **The Limelight 3G is prohibited** (Example 6), as are the OpenMV and OAK-1.
  - Configurable-only devices (HuskyLens, Pixy2) are allowed.
  - QA26: the HuskyLens 2 is legal only over I2C, with a re-pinned cable and Wi-Fi off.
- **R707 / R708:** **USB is only for** UVC webcams and allowed vision devices (single sensor, no stereo), USB hubs or switches, and an Expansion Hub.
  - (I) Ethernet-only cameras have no legal connection path; together with R702 this effectively means **only the 3A** among Limelights.
- **R704 Wi-Fi:**
  - Only the official RC/DS link may carry wireless communication.
  - Laptops must be off the robot network during matches.
  - **Only DS-app telemetry may be streamed.** "FTC Dashboard, FTControl Panels, and others are prohibited. No continuous video stream is allowed."
  - QA26: **no Wi-Fi streaming anywhere in the venue, including pits and practice field.**
  - (I) Don't stream the Limelight feed over the Control Hub Wi-Fi at events; ask the FTA how to tune it on site.
- **R705:** name the devices `12303-RC` and `12303-DS`.
- **R711:** change the Control Hub Wi-Fi password from the default; Bluetooth off; the Driver Station forgets other networks.
- **E301:** no personal hotspots, Bluetooth or 2.4/5 GHz devices at the venue.
- **R709:** a GoPro-style recorder is OK with wireless off.
- **R710:** lasers only inside sensors, Class 1 / IEC 62471 exempt, and non-visible. Many ToF sensors contain lasers.
- **Operator console:**
  - **R901:** one Driver Station device powered on (REV Driver Hub recommended).
  - **Gamepads:** the TU00 rule that listed allowed gamepads is removed. **Any gamepad** works if the DS app supports it, **wired only** (QA16; R904). Pending QA31: joysticks.
  - **R902:** touchscreen accessible.
  - **R903:** console within 3 ft × 1.5 ft × 2 ft; heavier than 20 lb invites scrutiny.
- **Signs (R401–R403):**
  - At least 2 SIGNS, ≥90° apart, each at least 6.5 × 2.5 in, mounted on the robot structure.
  - A solid red or blue rectangle with white 2.25 in team numbers, not stacked, not lit, not LED.
  - Reversible signs must not show the wrong color.
  - TU05: small decorative gaps OK if readable at 12 ft. Pending QA32: whether 2 in numbers are OK.
  - The IC requires both colors to be available at inspection.

### 4.6 Inspection, events and inspection docs
- **Inspection:** required before qualification or playoff matches. Re-inspect after significant changes (I303).
- **E117:** one robot per event.
- **E510:** no heating or cooling of parts for an advantage. Pending QA33: cooling a hot motor.
- **E511:** charge batteries at ≤3 A average.
- **Q&A procedure:** only Lead Coach 1 or 2 can post, through the FIRST dashboard. Moderators answer Monday to Thursday (M p.19).
- **Self-inspect** with the IC (rev 10/09/26).

### 4.7 12303-specific compliance notes (I, from the coach's list and photos; verify on the robot)

| Item | Status / what to check |
|---|---|
| SWYFT Drive V2 SR-DRIVE-02 | 4 modules, each with an integrated **SWYFT Spike** motor (SWYFT page: 12.7:1 all-metal gearbox, 6000 rpm 550-size motor, mecanum-style rollers) → **4 of the 8 motors**. The motor is on the R501 list; COTS drive modules are allowed (R301.A, R303). Inspectors may check that the rollers don't damage tiles (R201; IQR p.10). The 110 mm variant has "traction spikes"; the team's 86 mm pack is not described with them |
| goBILDA 6000 rpm Yellow Jacket (flywheel) | Legal (R501). Flywheels are not airflow devices (R801 box). Avoid gouging or marking POLLEN (G406, R201). Keep it unpowered in the transition (G403) and after 0:00 (G404) |
| goBILDA Dual Mode Servos (intake, indexer, possibly turret pinion) | All three variants are pre-verified (IQR). Max 2 per hub port. Max 8 servos total. No Servo Travel Tuner on the robot |
| Turret ring and pinion | A COTS turntable is single DoF (R303.E). The hood and flywheel must keep the whole robot within 18 × 24 × 29 at every turret angle (R105). In STARTING CONFIGURATION the turret must fit inside 18 in, height included |
| Limelight 3A | Legal over USB. If the camera is not a 3A (e.g., 3G, 2+, 3, 4) it is not legal (R702, R707) |
| REV Control Hub + Expansion Hub | Legal (R701); name 12303-RC (R705); non-default Wi-Fi password (R711) |
| goBILDA NiMH battery (photo) | Legal if it is 3100-0012-0020 (Nested) or the Matrix 14-0014 (R601) — check the label |
| Wide roller intake | G407 risk: it can touch or herd 5+ balls. It must not take a 5th once 4 are inside. Guard the top so falling balls don't land on the robot (G407, G409). Opponent-NECTAR pickup risk (G408) — see §8 |

---

## 5. Official Q&A rulings and Team Update changes

### 5.1 Answered Q&A (26 of 38; board read 2026-10-20) — https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N

| QA | Rule | Ruling (paraphrased; (V) from the answer text) | Why it matters |
|---|---|---|---|
| 1 | R602/R611 | UVC webcams on a powered USB hub are legal however the hub is legally powered, including a COTS USB pack | Camera power |
| 2 | G407/G408 | **Blocking or defending shots is not specifically disallowed** and is not by itself against the spirit of the game. G407/G408 apply only if the blocker CONTROLS elements | Defense |
| 3 | G407 | Controlling a 5th element **is a violation; no MOMENTARY exception.** Verbal warning if not STRATEGIC; more if STRATEGIC. Briefly reversing one out is likely not STRATEGIC but is still a violation. Opponent NECTAR adds G408 | Intake design |
| 4 | G408 | **No MOMENTARY exception** for opponent NECTAR. Ejecting it immediately with no competitive gain is typically not STRATEGIC | Intake design |
| 5, 9, 13 | G408 | Referred to QA4 (and QA23): bypass paths, color sorters, piles, boxing in | — |
| 6 | G425 | The human player **must drop** NECTAR into the LOADING ZONE, with no exception for the NECTAR being entered | Human play |
| 7 | G427 | The human **may throw or roll** NECTAR in a useful direction if it is released before touching the tile, touches the LOADING ZONE tile first, and does not end up scoring in the HIVE or a FLOWER | **Fast feeding to a waiting robot (I)** |
| 8 | G419 | A shot-blocking extension over an opponent: contact is not restricted, but damage or **functional impairment** is, and is not limited to visible marks. Beware G420 | Defense limits |
| 10 | — | **No rule forbids launching your own NECTAR or POLLEN into the opponent's HIVE** | (I) Only helps the opponent; G417.D still forbids launching to *impede* their tip |
| 11 | G426/G401 | **Tips in AUTO unlock NECTAR**, and the human may enter it, following all human rules, during AUTO | AUTO plans |
| 12 | 10.5.1 | Adding elements before the HIVE comes to rest is allowed. The TIP scores when the damper touches the frame, regardless of element count. Placement and motion cause variation. Launching at a moving HIVE can disrupt it. (The asker's "5 NECTAR or 8 POLLEN" claim was not confirmed; see EFSG §12) | Tip timing |
| 14 | 10.5.5 | No new RP types will be added; **match point values are fixed**; RP thresholds may rise at Regional and World Championships; elements may change at Championship and Premier events | Planning stability |
| 16 | 12.9 | **No gamepad restrictions** (list removed in TU00). It must work with the DS app; no wireless | Console |
| 17 | G409 | **Deflection counts:** a robot driving under a tipped HIVE that has balls bounce off it violates G409 (verbal warning if not STRATEGIC) | Stay clear of spills |
| 18 | I101 | No team size or hard age limits; only the STUDENT definition | — |
| 19 | — | The Atom feed works; a printable list is under "Additional Views" | Monitoring |
| 20 | G205 | Blocking: see QA2 and QA8 | Defense |
| 21 | 10.5 | **LEAVE is checked only at the end of AUTO.** A robot touching any wall then (even inside its LOADING ZONE) gets no LEAVE | AUTO end position |
| 22 | R504/R607 | A potentiometer taken out of a servo is a CUSTOM CIRCUIT sensor and is allowed | Sensors |
| 23 | G407/G408 | **Neither rule has a momentary exception.** The G407 box example is a "likely not STRATEGIC" judgment, and per §1.7.1 boxes don't override rules. **Routine momentary over-control or opponent-NECTAR control built into a design or strategy is likely STRATEGIC** | Intake design |
| 25 | R201/R302 | Mirrors for a camera are not pre-approved; the rules are permissive; inspectors judge safety (shatter, edges) | Vision design |
| 26 | R702/R708/R704 | HuskyLens 2: I2C only, with a re-pinned cable; not USB (not UVC). **"Wi-Fi streaming is not allowed anywhere in a competition venue (per R704.D)"** | Limelight tuning at events |
| 27 | R403 | Sign fonts: see TU05 | — |
| 28 | 10.5 | **A partner may push or pull a robot off the wall for LEAVE.** The end state counts however it was reached, as long as no rule is broken | AUTO helper plays |

**Discrepancy to note:** QA23 says G407 and G408 both carry "VERBAL WARNING. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC." The TU05 manual text of **G408** is "VERBAL WARNING. YELLOW CARD per MATCH, if STRATEGIC" — no MAJOR foul (M p.108, verified visually). The manual governs.

### 5.2 Pending questions (accepted, unanswered as of 2026-10-20) — watch these

| QA | Topic |
|---|---|
| 15 | G402: what counts as AUTO interference (intaking on the other side; from opposing FLOWERS) |
| 24 | G402: change the penalty to per instance |
| 29 | G407/G408: lift-and-release, active expulsion, color-reject designs |
| 30 | G418 / 10.5.2: NECTAR resting against the outside of FLOWER pipes |
| 31 | Joysticks or other USB input devices |
| 32 | 2 in vs 2.25 in sign numbers |
| 33 | E510: cooling a hot motor |
| **34** | **G407: a wide intake bar with extra balls underneath** |
| **35** | **G409: waiting with intake on just outside the AUTO tip spill area** |
| **36** | **G426: re-entry timing for NECTAR that left the field** |
| **37** | **G405: a defender deflecting shots out of the field** |
| **38** | **G411: blocking access to elements without contact** |

### 5.3 Team Updates (V)
- **TU00 (09-12)**, changes from last season:
  - New Competition Integrity Contract (CIC) with a Sporting Ethics Code; **STRATEGIC** is now a defined term; most game violations start as a VERBAL WARNING unless STRATEGIC.
  - G202 (follow the CIC) added; G406 merges the old damage rules.
  - **Gamepad list removed.**
  - R101 added.
  - **R105 expansion is now 18 × 24 × 29** with no deliberate detaching, enforced in-match by G416.
  - R201 added. WATTOS Stingray motor added.
  - **Servos cut to 8.**
  - R801 renamed and broadened.
  - Section 13 rule numbers changed.
  - T301: 2 people may talk to the Head Referee. T701: 2 STUDENT representatives at alliance selection.
- **TU01 (09-17):**
  - Figure 9-10 fix: **bottom of HIVE is 30.6 in above the tiles.**
  - G204 example B now cites G427.
  - §13.3: "two representatives" (previously "two STUDENTS").
- **TU02 (09-24):**
  - Table 10-6 penalty interpretations rewritten. "Per MATCH, if STRATEGIC" means at most 1 MAJOR + 1 YELLOW per team per match; per-instance language added.
  - G420 box: entangling exposed parts in normal play is not likely a violation.
  - G423/G425: penalties go to the team of the drive-team member involved.
  - **G426:** "one *additional* NECTAR"; **NECTAR that leaves the field may be re-entered at any time per G427**; NECTAR need not be entered immediately after a tip.
- **TU03 (10-01):** pre-load POLLEN on tiles must be **fully on your own side** (10.3.4, G304.G).
- **TU04 (10-08):** the 1:00 "FLOWER Ownership Unlocked" cue is a **Chime**; Escalation Guidelines published.
- **TU05 (10-15):**
  - §10.8.4 field-reset signal wording.
  - **G407 box: repeated control of 5+ is likely STRATEGIC even with a "keep 4 and discard the rest" sorter.**
  - **G408 box: repeated opponent-NECTAR control is likely STRATEGIC even with an auto-discard sorter.**
  - R403 box: decorative gaps in numbers OK if readable at 12 ft.
- Team Updates post **every Thursday**; the next is expected 2026-10-22. RP thresholds for Regional and World Championships will arrive in a Team Update.

---

## 6. Ambiguities to ask the Head Referee (at the drivers meeting) or the Q&A (Lead Coach account)

1. **POLLEN into FLOWERS before 1:00.**
   - G410 restricts only NECTAR, and the 1:00 cue is "FLOWER *Ownership* Unlocked".
   - But the §10.5.2 box says "FLOWER scoring cannot begin until there is one minute remaining… per G410. Achievements scored prior… are still scored, but subject to penalties."
   - Ask: is putting POLLEN into a FLOWER before 1:00 penalized? My reading is no: rule text beats box text (§1.7.1), and POLLEN in an unowned FLOWER scores nothing anyway.
2. **Which TIPS count for POLLINATOR**, and who gets the 20 pts if the opponent's launch tips our HIVE? QA10 allows launching into the opponent's HIVE. Expected: tips of your own-color HIVE.
3. **G402 scope** (pending QA15/24):
   - Is taking POLLEN from an opposing-side FLOWER in AUTO interference even if the opponent never goes there?
   - Is shooting from your side across the center line OK? (Expected yes.)
   - Who has priority over your own HIVE's spill that rolls across the center line?
4. **G407 with wide roller intakes** (pending QA34/29): with 4 inside, is a 5th ball pressed against the spinning rollers "controlled"? Is the 4-ball line inside an indexer one CONTROL chain?
5. **G409 spill zone** (pending QA35): how close may a robot wait to the spill area? What happens if your own launched balls bounce off the cell rim onto you during a tip?
6. **LOADING ZONE defense:**
   - No rule protects a LOADING ZONE.
   - May an opponent park in or block our zone in the last minute to deny PARK and SWARM?
   - Is blocking our NECTAR entry G411 (preventing access to elements) or G204?
7. **G411 guarding** (pending QA38): is shielding pollen in a corner or guarding your own GARDEN without contact a violation? The box says "actively positioning a ROBOT to prevent opposing ALLIANCE access is a violation."
8. **GARDEN and elements inside a robot:** do elements held inside a robot that is partly over the GARDEN count? The GARDEN is "infinitely tall" and the criterion is only "at least partially in the GARDEN zone." Minor value, but worth knowing.
9. **Launching into a FLOWER vs placing:** G418.A only limits *where* elements enter (the top), while the §10.5.2 box says "Placing… into the top… is the only allowable way to score." Is shooting into the top of a FLOWER from a distance legal? Expected yes if it enters through the top.
10. **G421 typo:** the text refers to "G421.D", but only A–C exist. It presumably means B (2 ft from where the pin started).
11. **G408 penalty level:** the manual says Verbal / YELLOW if STRATEGIC; QA23 says MAJOR + YELLOW. Ask which the referees apply.
12. **R105 with a turret:** is the 18 × 24 box re-oriented as the turret rotates (Figure 12-6)? Confirm at inspection with the hood fully raised.
13. **NECTAR re-entry after leaving the field** (pending QA36): the TU02 box says "at any time." Confirm it is not tied to tips or the 1:00 mark.
14. **Limelight tuning at events:** given R704.D and QA26, how may teams tune a Limelight 3A in the pits (direct USB to a laptop?) without "Wi-Fi streaming"? Ask the FTA.
15. **HIVE calibration consistency:** ask the FTA or Head Referee whether the HIVEs passed FAC 2.1–2.7 that morning, and expect small differences between fields (FMG).
16. **Defender deflecting shots out of the field** (pending QA37): is that G405 on the defender?
17. **NECTAR against FLOWER pipes** (pending QA30): does a NECTAR bulging into the scoring cylinder from outside count, and is it a G418 risk while sliding up the outside?

---

## 7. Direct answers to the research questions

| Question | Answer (citations) |
|---|---|
| **Where may a robot launch from?** | **No launch zone or location limit exists in BIOBUZZ.** The only zones are the ALLIANCE AREA, LOADING ZONE and GARDEN (M p.65-66). Limits come from G402 (stay on your own side in AUTO), G417 (don't touch the HIVE or shoot at it improperly), G405 (no deliberate ejection) and G412 (safety). (V for the absence of a rule; I for completeness) |
| **What exactly is LAUNCH?** | "An action by a ROBOT in which the SCORING ELEMENT is **shot into the air, propelled across the floor to a desired location or in a preferred direction, or thrown in a forceful way**" (glossary M p.173). A launched element no longer touching the robot is not CONTROLLED (G407 box F). (I) Rolling or "launching" along the floor (e.g., toward the GARDEN or a partner) therefore ends control |
| **Dunk or place into a cell?** | **No.** "LAUNCHING into the upward-facing CELL is the only allowed way to earn a HIVE TIP" (M p.87). G417 forbids moving the HIVE any other way and lists touching a HIVE "directly or transitively through a CONTROLLED SCORING ELEMENT" as STRATEGIC. It is also physically impossible: max robot height 29 in vs. a cell-opening bottom of 53.5 in (R105, M p.71) |
| **NECTAR into the HIVE? Does it count toward tipping?** | **Yes**, and it counts: "until enough POLLEN or NECTAR are LAUNCHED" (M p.69); "POLLEN and/or NECTAR remaining in CELL" 2 pts. A NECTAR (0.091 lb) weighs ~1.65 POLLEN (0.055 lb), so it counts more toward the tip (AM). Calibration treats 3 NECTAR + 3 POLLEN like 8 POLLEN (EFSG §12). Only your own NECTAR can be controlled (G408) |
| **POLLEN into FLOWERS before 1:00?** | No rule text prohibits it (G410 covers only NECTAR), but a §10.5.2 box suggests otherwise; see §6 #1. **Elements in an unowned FLOWER score nothing**: ownership needs a NECTAR, and the 2 pts are only "in an owned FLOWER" (M p.88, p.91) |
| **Pull POLLEN from any FLOWER's bottom in TELEOP?** | **Yes.** G418.B allows removing POLLEN through the bottom of "a FLOWER", with no side or time limit in TELEOP. In AUTO, G402 gives opponents priority on their side |
| **Score NECTAR in opponent-side FLOWERS?** | **Yes**, in the last 60 s, through the top (G410, G418.A); no rule limits which FLOWER. You own it if your NECTAR is top-most |
| **Legal FLOWER interference or descoring** | Only pulling POLLEN out the bottom (which drops the column), and topping with your own NECTAR to take ownership. **NECTAR can never be removed.** No side-pulling, shaking, ramming the wall, forcing NECTAR out, or holding NECTAR against the pipes (G418 box). Humans may not disrupt FLOWERS (G425.C) |
| **Shooting into or blocking the opponent's HIVE** | Launching at the opponent's HIVE **to impede its TIP** is a STRATEGIC G417 violation (MAJOR + YELLOW). Launching your own elements into their up cell is not prohibited (QA10) but helps them. You cannot physically block a cell 53 in up with a 29 in robot. Blocking *shots* near the shooter is allowed (QA2/QA8/QA20) |
| **Robot-to-robot contact limits** | Contact and defense are expected ("highly interactive… high-contact", G419 box). No damage or impairment (G419), no tipping or entangling (G420), **3 s pin limit** (G421: MAJOR + MAJOR every 3 s), no forcing violations (G204) |
| **Protected zones** | **None.** The GARDEN is explicitly unprotected (M p.89; G411 box) |
| **LOADING ZONE protection** | Only indirect: deflecting NECTAR that is being entered is G204 against the offender (example B), and blocking access to elements may be G411. Occupying the zone is not addressed (see §6 #6) |
| **G407 4-element limit** | CONTROL = fully supported by, or stuck in, on or under the robot, or herded with a flat or concave face, including through other elements (glossary). **A ball sitting in a hopper counts.** A ball in the shooter counts until it leaves; once launched and out of contact it does not. Bulldozing and deflecting are not CONTROL. No momentary exception (QA3); repeats are STRATEGIC even with a discard sorter (TU05) |
| **G408 opponent NECTAR** | Never control it; no momentary exception (QA4/QA23). Repeats are STRATEGIC even with an auto-discard sorter (TU05). The penalty text is Verbal / YELLOW if STRATEGIC (see §5.1 discrepancy) |
| **G409 catching spill** | Don't catch or deflect spilling elements until they touch something else. Deflecting counts (QA17). Waiting under the HIVE or catching with an open top mechanism is STRATEGIC (YELLOW) |
| **G411 hoarding** | No STRATEGIC denial of access; corralling or guarding is a violation (MAJOR + YELLOW). Using your own elements for scoring is fine. The GARDEN is not protected |
| **G402 AUTO** | Crossing the center line is "risky… may be seen as STRATEGIC". Disrupting the opponent's AUTO is a MAJOR per match even if not strategic. Each alliance has priority over field and scoring elements on its side, so your own FLOWERS, own GARDEN and own-side spills are yours to use in AUTO. Elements deflected across the line "will likely not be penalized" |
| **G403 / G404** | No powered motion during the 8 s transition or after 0:00. Coasting is OK. Don't INIT TeleOp during the transition if INIT moves anything |
| **G304 / R102 / R103 start** | See §3.1. **Pre-load POLLEN may sit on tiles touching the robot**, fully on your own side, and may stick out beyond 18 in |
| **Human player rules** | See §3.4 human table and the §1 cheat sheet: 1 NECTAR per own tip (AUTO or TELEOP), all at ≤1:00; drop it so it hits the LOADING ZONE tile first; may aim a throw or roll; re-entry "any time" for NECTAR that left the field; at most 3 students across driver and human-player roles; the coach cannot touch elements |
| **LEAVE / PARK / SWARM** | §2.5: max 13 per robot, 26 per alliance; 16 needs both robots, e.g., both LEAVE + both end PARK |
| **Ranking, playoffs, advancement** | §2.7–2.10 |
| **Robot rules** | §4 |
| **Common foul traps** | §8 |

---

## 8. Common foul traps with practical advice (I, grounded in the cited rules)

| # | Trap | Rule (penalty) | Prevention |
|---|---|---|---|
| 1 | Intake keeps running with 4 inside; a 5th ball rides on the rollers or bar, or the robot pushes a pile | G407 (Verbal → MAJOR + YELLOW if STRATEGIC) | Sense "full" with a beam break or color sensor and **stop and reverse the intake at 4**. Add a hard mechanical stop. Don't push piles; separate balls first. Pending QA34 |
| 2 | Wide intake swallows a 3.6 in opponent NECTAR | G408 (Verbal → YELLOW if STRATEGIC; repeats = STRATEGIC per TU05) | Choose one: geometry that rejects 3.6 in balls (pollen-only intake, which also gives up your own NECTAR); a color sensor that stops intake *before* the ball is fully supported; or drivers trained to avoid opponent NECTAR. Rejection *after* intake is still a violation |
| 3 | Driving under or next to the HIVE as it tips; balls bounce off the turret or hood | G409 (Verbal → YELLOW) | Shoot from positions clear of the spill area; add top guards or covers; don't loiter under an up cell that is about to tip |
| 4 | Turret or hood hits the HIVE frame or a cell when shooting close; driving into the frame | G417 (Verbal → MAJOR + YELLOW) | Keep a minimum shooting distance; avoid high-speed contact with the frame |
| 5 | Shooting at a HIVE mid-tip (yours stalls; theirs = G417.D) | M p.87 box; G417 | Pause after the shot you expect to tip it; never aim at the opponent HIVE |
| 6 | NECTAR into a FLOWER at 1:02 by the Driver Station clock | G410 (**MAJOR per NECTAR**) | **Use the field timer**; keep a 1–2 s margin; the coach calls "GO" at 0:59 |
| 7 | Human enters NECTAR without a tip, or before 1:00 | G426 (MINOR per NECTAR) | The coach counts own-HIVE tips aloud; NECTAR stays visible in the tray (G426 box) |
| 8 | Human sets NECTAR down instead of dropping it, it touches a robot first, or the human touches a ball on the tile | G427/G425 (MINOR per NECTAR / Verbal → MAJOR + YELLOW) | Release from above the zone onto an empty part of the tile; practice the drop-and-roll (QA7) |
| 9 | Servos twitch when TeleOp INIT runs during the transition; flywheel commanded on | G403 (Verbal → MAJOR + YELLOW) | No actuator commands in `init()`; wait for the bells before START; the coach handles OpMode buttons (allowed by the G423 box) |
| 10 | Flywheel or drive still powered after 0:00 | G404 | Press ■ at the buzzer; optionally add a 2:00 auto-stop timer in TeleOp |
| 11 | AUTO path crosses into columns D–F (red) or grabs opposing-side FLOWER POLLEN | G402 (**MAJOR per match even if accidental**) | Geofence AUTO paths to your own columns; use only own-side FLOWERS and GARDEN |
| 12 | Robot touching the wall at the end of AUTO | No foul, but loses LEAVE (QA21) and can cost SWARM | Finish AUTO a few inches off every wall (inside the LOADING ZONE if parking) |
| 13 | Pinning while defending near walls or FLOWERS | G421 (MAJOR + MAJOR every 3 s) | Count "1-2" and back off 2 ft |
| 14 | Defense damages an exposed intake or wiring, or a wedge tips a robot | G419/G420 | Defend frame to frame; no wedges; protect your own main switch and wiring (their damage in normal play is not the opponent's foul) |
| 15 | Hoarding: guarding a corner pile or your GARDEN with the robot | G411 (MAJOR + YELLOW) | Score elements rather than guard them |
| 16 | Shooting pollen out of the field on purpose (e.g., clearing) | G405 (MAJOR per element) | Only scoring attempts are excused |
| 17 | Tearing or marking balls with the flywheel or intake | G406 / R201 | Use compliant wheels and limit compression; check the balls after testing |
| 18 | Starting setup errors: robot not touching the wall, 3 or 5 pre-loads, pre-load across the center line, touching a FLOWER, in the LOADING ZONE, larger than 18 in | G304 (match held / DISABLED) | Pre-match checklist; a team alignment jig is allowed (G302 box) |
| 19 | Slow queueing or repairs at the field | G301 (Verbal → MAJOR next match → DISABLED) | Have the robot match-ready during the ~3 min between matches |
| 20 | Deliberately taking a foul because it is "worth it" (e.g., early NECTAR) | G205 (YELLOW or RED; especially in a team's last qualification match) | Don't |
| 21 | Coach picks up a ball; non-drive-team members touch gamepads | G424 / G423 | Assign roles: 2 drivers + 1 human player + coach |
| 22 | Wi-Fi streaming (Dashboard, Panels, Limelight video) or a hotspot at the venue | R704 / E301 / QA26 | Competition build with FTC Dashboard off; phones' hotspots off |
| 23 | Using more than 8 motors or 8 servos across configurations; an unlisted servo; a Servo Travel Tuner on the robot | R503 / R502 / R612 | Count everything, including spare configurations (I302); keep IQR data handy |

---

## Appendix A — source URLs used
- Manual (TU05): https://ftc-resources.firstinspires.org/ftc/game/manual (sections at /ftc/game/manual-08 … manual-16); local copy in scratchpad `manual/manual.pdf`.
- Team Updates: https://ftc-resources.firstinspires.org/ftc/game/tu-00 … tu-05; combined https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- Q&A board: https://game-qa.firstinspires.org/boards/2027/FTC/QA (ftc.game/qa redirects here); question pages /qa/1 … /qa/38; Atom feed /boards/2027/FTC/QA/answers.atom
- Field docs: https://ftc-resources.firstinspires.org/ftc/archive/2027/field (field-check, eventfieldguide, field-mitigation-guide; Onshape field CAD and FLOWER scoring-volume CAD linked there)
- Event docs: https://ftc-resources.firstinspires.org/ftc/archive/2027/event (inspection-check, inspection-reference, game-one-page, field-reset, league, tournament-guide)
  - The "HR meeting notes" and "Ref tablet guide" pages say "Resource Coming Soon!" as of 2026-10-20 — check them later.
- AndyMark elements: https://andymark.com/products/biobuzz-scoring-elements
- SWYFT Drive V2: https://swyftrobotics.com/products/swyft-drive-v2
- goBILDA battery: https://www.gobilda.com/12v-nimh-nested-battery-3000mah-mh-fc-xt30-connector/
- FiM events: https://firstinmichigan.us/FTC/events/
- Team 12303 on FTC Events: https://ftc-events.firstinspires.org/2026/team/12303

Downloaded copies (untrusted data) are in scratchpad `downloads/qa/` (board.json, qa-all.txt, answers.atom), `downloads/ftcres/` (TU PDFs, FAC, EFSG, FMG, FRG, IC, IQR, G1P, etc.), `downloads/fim/` and `downloads/andymark/`.
