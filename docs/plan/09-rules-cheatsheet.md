# 09 · Rules cheat sheet (by match phase)

Based on Competition Manual **TU05 (2026-10-15)** and the official Q&A board as read on **2026-10-20** (38 questions: 26 answered, 12 pending).
- Rule text beats the orange "box" text.
- The manual beats the Q&A ("responses in the Q&A do not supersede the text in the manual").
- At an event, the Head Referee has the final word (M p.17–19; T401).
- Team Updates come out **every Thursday**; the next one is expected 2026-10-22 (M p.19).

Source tags: see the [source key](00-master-plan.md#source-key).

---

## Penalty basics (Table 10-4, M p.92)

| Penalty | Effect |
|---|---|
| MINOR | 5 points to the opponent |
| **MAJOR** | **20 points to the opponent**: as much as a tip |
| Verbal warning | Where most G4xx rules start, unless the act is STRATEGIC ("deliberate or reckless with foreseeable consequences", M p.92) |
| YELLOW card | A warning card. A second YELLOW becomes a RED. Cards carry into later matches and clear at the end of qualifications; League Meet cards and warnings clear after each meet (M p.94, p.167) |
| RED card | Disqualified for that match: 0 points and 0 RP in qualifications. In playoffs it hits the whole alliance (M p.95) |

**No warning step:** G402, G405, G410, G411, G421, G426, G427, G428. Warnings carry from qualifications into playoffs (M p.94). Repeated warnings for the same thing invite STRATEGIC calls (M p.92).

---

## Before the match

| Rule | In plain words |
|---|---|
| Table 10-1 (M p.82) | Drive team of at most 4: **1 drive coach** (may be an adult) + **up to 3 students** as drivers and human player |
| G424 | The coach may not touch balls, so **the coach can't be the human player** |
| G301 | Don't delay the match. Warning → MAJOR → DISABLED |
| G302 | What you bring must fit in the ALLIANCE AREA. **An alignment jig is allowed.** No wireless; no fake AprilTags or lights aimed at the field |
| G303 | Robot inspected; signs show the correct alliance colour |
| **G304** (M p.104) | Fully on our side, not overhanging the wall · touching the perimeter wall · **not touching a FLOWER** · **not in the LZ** · inside the 18 in cube · touching **exactly 4 POLLEN** preloads (on our side) · **motionless after INIT** |
| G305 | Select and INIT an OpMode. An AUTO OpMode needs the **30 s timer** on |
| R102 / R103 | 18 × 18 × 18 in, all parts still; **preloads may stick out**; INIT may hold servos in place |
| Placement (M p.85) | If either alliance asks: red 1, blue 1, red 2, blue 2 |

## AUTO (2:30 → 2:00)

| Rule | In plain words | Penalty |
|---|---|---|
| G401 | Hands off from the countdown. Allowed: start, stop, safety | Warning; MAJOR + YELLOW if STRATEGIC |
| **G402** | Don't disrupt the opponent's AUTO. Red = columns A–C, blue = D–F; each alliance has priority over the balls and FLOWERS on its side. Crossing is "risky". Balls deflected across are "likely not" penalized | **MAJOR per match, even by accident**; + YELLOW if STRATEGIC |
| Scoring | LEAVE 3 (judged at the end of AUTO: **touching any wall = no LEAVE**, QA21; being pushed off by a partner counts, QA28) · AUTO PARK 5 · tip 20. A tip that finishes before TELEOP starts counts as AUTO (M p.86) | |
| Human | 1 NECTAR per AUTO tip of our HIVE may go in during AUTO (QA11) | |
| G409 | Applies in AUTO too | |

## Transition (8 seconds)

| Rule | In plain words | Penalty |
|---|---|---|
| **G403** | **No powered movement.** Coasting is fine. You may stop AUTO and INIT or start TELEOP, but **if INIT moves or twitches anything, wait for TELEOP**. Our TELEOP wakes its actuators at START, so **we press START only at the TELEOP bells** | Warning; MAJOR + YELLOW if STRATEGIC |

## TELEOP (2:00 → 0:00)

| Rule | In plain words | Penalty | Q&A |
|---|---|---|---|
| G405 | Don't knock balls out of the field on purpose. Scoring attempts and robot contact are excused | MAJOR per ball | QA37 pending |
| G406 | Don't damage the field or the balls. Routinely gouging or marking balls counts | Warning; MAJOR + YELLOW if STRATEGIC; DISABLED if more damage is likely | R201 |
| **G407** | **At most 4 balls controlled.** Hopper or shooter balls count; herding with a flat face counts; bulldozing, being hit, and launched balls don't | Warning; MAJOR + YELLOW per match if STRATEGIC | QA3, QA23: no momentary exception. TU05: repeats are STRATEGIC even with a sorter. QA29, QA34 pending |
| **G408** | **Never control opponent NECTAR** | Warning; YELLOW per match if STRATEGIC (manual). QA23 says MAJOR + YELLOW; the manual governs, but design for the stricter one | QA4: no momentary exception |
| **G409** | Don't catch or deflect spilling balls until they touch something else | Warning; YELLOW if STRATEGIC | **QA17: a ball bouncing off a robot under the HIVE counts** |
| G410 | NECTAR into a FLOWER only with ≤ 60 s left (field timer) | **MAJOR per NECTAR** | |
| G411 | No hoarding or guarding balls. The GARDEN is not protected | **MAJOR + YELLOW**, no warning | QA38 pending |
| G412–G416 | Robot under control; stop when told; number and colour visible; don't grab field elements; stay within 18 × 24 × 29 in (R105) | Various | |
| **G417** | Only launching into the **up** CELL may move the HIVE. Don't touch the HIVE, even through a held ball. **Never shoot to stop the opponent's tip** | Warning; MAJOR + YELLOW if STRATEGIC | QA10: launching into their HIVE isn't banned (it only helps them). QA12: launching at a moving HIVE can stop the tip |
| G418 | FLOWERS: balls go in **only through the top**; **only POLLEN** comes out, **only through the bottom**. No shaking, no pulling through the side | Warning; MAJOR + YELLOW if STRATEGIC | QA30 pending |
| G419 / G420 | Don't damage, impair, tip or entangle an opponent. Damage to exposed parts (switch, wiring, delicate intake) in normal play isn't the opponent's fault | Warning; MAJOR + YELLOW if STRATEGIC; RED if they can't drive | QA8: contact is allowed; impairment isn't |
| **G421** | **Pin no longer than 3 s.** The count ends at 2 ft apart for more than 3 s | **MAJOR + a MAJOR every 3 s** | (The text says "G421.D"; only A–C exist) |
| G204 | Don't force opponents into fouls. Example: ramming a robot lined up at a FLOWER at 1:05 so it drops NECTAR early | MAJOR per instance | |
| Defence | Blocking shots is "not specifically disallowed" | – | QA2, QA20 |

## Last 60 seconds

- **G410:** NECTAR may enter FLOWERS (MAJOR per NECTAR if early). Use the **field timer**, with a 1 s margin.
- **G426.B:** the human may enter **all** remaining NECTAR.
- **FLOWER scoring** (M p.88):
  - the **top-most** NECTAR owns the FLOWER and gets 2 per POLLEN/NECTAR inside, whoever put them there;
  - the **bottom-most** NECTAR earns +5;
  - a FLOWER with no NECTAR scores 0.
- 0:20 is a reminder only (M p.79).

## End of match

- **G404:** no powered movement after 0:00. Press ■ and put the controllers down.
- Everything is scored at rest. Tips are judged until everything is at rest (M p.86).
- Wait for the field-reset signal before touching the robot (M p.97). The robot must be removable with power off (R203).

## Human player (all match)

| Rule | In plain words | Penalty |
|---|---|---|
| G422 | Stay in the ALLIANCE AREA (breaking the plane is fine). You may pick up our NECTAR that left the field if you can reach it | Warning; MINOR if STRATEGIC |
| G423 | After the start, coaches and other teams keep hands off the gamepads. The coach may hold the DS and press OpMode / INIT / start / stop | Warning; MAJOR + YELLOW if STRATEGIC |
| G425 | Don't touch robots, balls touching a tile or robot, or field elements. Don't shake the wall | Warning; MAJOR + YELLOW if STRATEGIC |
| **G426** | NECTAR: **1 per tip of our HIVE**, **all remaining at ≤ 1:00**. It needn't go in right away. NECTAR that left the field may be re-entered "at any time" | **MINOR per NECTAR** |
| **G427** | Only through the LZ: no tools, our own alliance, and it must **touch the LZ tile first**. QA6: drop it, don't place it. QA7: you may throw or roll it once released. Humans never score directly | **MINOR per NECTAR** |
| G428 | Don't remove balls from the field (give stray ones to field staff) | MINOR per ball |

## Robot rules worth knowing (inspection)

| Rule | Short version |
|---|---|
| R105 / G416 | 18 × 24 × 29 in after the start, **enforced mechanically** (software limits don't count); a moving part must fit at every point of its travel (for us: the floating intake arm) |
| R201 / R202 | No sharp edges, nothing that damages balls or tiles; no fake AprilTags; lights only briefly |
| R401–R403 | ≥ 2 signs on surfaces ≥ 90° apart; ≥ 6.5 × 2.5 in, solid red or blue; white numerals about 2.25 in; not stacked or lit |
| R501–R505 | Legal motors include the SWYFT Spike and goBILDA 520x. Servos ≤ 8 W and ≤ 4 A at 6 V. **≤ 8 motors and ≤ 8 servos** across all configurations. Approved power devices only (Servo Hub / 6 V injector OK) |
| R601 / R603 / R605 | One legal 12 V NiMH battery (ours is legal) · **one approved main power switch** · frame not a current path; listed grounding straps only |
| R612 | No goBILDA Servo Travel Tuner |
| R701 / R702 | One Control Hub + at most one Expansion Hub. **Limelight 3A is the only programmable vision coprocessor; the 3G is prohibited** |
| R704 | Only DS telemetry may be streamed. **No FTC Dashboard, Panels or video stream** (QA26: anywhere in the venue) |
| R705 / R711 | Names `12303-RC` / `12303-DS`; change the Wi-Fi password; Bluetooth off |
| R707 / R708 | USB is for vision devices, USB hubs/switches and the Expansion Hub only |
| R904 | Gamepads must be wired (QA16: any model the DS supports) |

---

## Q&A rulings that matter to us (answered)

Board: https://game-qa.firstinspires.org/boards/2027/FTC/QA (question n is at `/qa/n`).

| QA | Ruling (short) | What it means for us |
|---|---|---|
| 2, 20 | Blocking shots is not specifically disallowed | Expect defence; we may screen too |
| 3 | A 5th ball is a violation; **no momentary exception** | Auto-stop at 4 |
| 4 | Opponent NECTAR: no momentary exception; ejecting it immediately is "typically not" STRATEGIC | Refuse it at the mouth |
| 6 | The human must **drop** NECTAR | Drill it |
| 7 | The human may throw or roll NECTAR if it is released first, lands in the LZ first, and doesn't score | Roll it to a partner who can use it |
| 8 | Contact is allowed; damage or **functional impairment** isn't (not just visible marks) | Defend frame to frame |
| 10 | Launching our balls into the opponent's HIVE isn't forbidden | It only helps them: aim at our own tags only |
| 11 | AUTO tips unlock NECTAR; the human may enter it during AUTO | Phase 1: hold it |
| 12 | A tip scores when the damper touches the frame, whatever the count. Placement and motion vary. Launching at a moving HIVE may stop the tip | Stop firing after the threshold ball |
| 14 | No new RP types; point values fixed; championship thresholds may change | Plan stays valid |
| 16 | Any gamepad the DS supports, wired | Wired PS4/PS5 for rumble |
| **17** | Balls bouncing off a robot under a tipping HIVE = **G409 violation** | Spill discipline |
| **21** | LEAVE is judged at the end of AUTO; **touching any wall = no LEAVE** | Park poses 5.7–6.7 in off the wall |
| 22 | A potentiometer taken from a servo, used as a sensor, is an allowed custom circuit | Allowed if we ever need a position sensor |
| **23** | **No momentary exception for G407 or G408**; routine design-based over-control is likely STRATEGIC | Possession is a design requirement |
| 25 | Camera mirrors aren't pre-approved; inspectors judge safety | – |
| **26** | **No Wi-Fi streaming anywhere in the venue** (R704.D) | Competition build; tune the Limelight over USB |
| 28 | A partner may push or pull a robot off the wall for LEAVE | Helps a partner with no AUTO |

## Pending Q&A to watch

Check the answers feed weekly: https://game-qa.firstinspires.org/boards/2027/FTC/QA/answers.atom

| QA | Topic | Why 12303 cares |
|---|---|---|
| 15, 24 | G402: does intaking on the other side or from opposing FLOWERS count as AUTO interference? Should the penalty be per instance? | Our AUTO stays on our side anyway |
| 29 | G407/G408: lift-and-release, active expulsion, colour rejection designs | **Our mouth-reject design** |
| 30 | NECTAR resting against the outside of FLOWER pipes | Phase-3 FLOWERS |
| 31 | Joysticks / other USB input devices | – |
| 32 | 2 in vs 2.25 in sign numerals | Make ours 2.25 in |
| 33 | E510: cooling a hot motor | Flywheel and drive motors in long events |
| **34** | **G407: a wide intake bar with extra balls underneath** | **Our full-width intake wheel** |
| **35** | **G409: waiting with the intake on just outside the AUTO spill area** | AUTO and TELEOP spill sweeps |
| 36 | G426: when NECTAR that left the field may be re-entered | Human player |
| 37 | G405: a defender deflecting shots out of the field | Defence |
| 38 | G411: blocking access to balls without contact | Defence; LZ blocking at the end |

---

## Ask the Head Referee

Ask at the drivers meeting; most important first. Write the answers here for next time.

| # | Question | Why | Answer at event |
|---|---|---|---|
| 1 | With 4 balls inside, is a 5th ball pressed against our spinning intake wheel "controlled"? | G407; QA34 pending | |
| 2 | How close may a robot wait to a tipping HIVE's spill? | G409; QA35 pending | |
| 3 | May an opponent sit in our LOADING ZONE in the last seconds? Is blocking our NECTAR entry G411 or G204? | No rule protects the LZ | |
| 4 | Which tips count for POLLINATOR (our own HIVE only)? Who gets the 20 if an opponent's ball tips our HIVE? | Table 10-3 wording; QA10 | |
| 5 | Is POLLEN into a FLOWER before 1:00 penalized? (G410 names only NECTAR; the §10.5.2 box suggests otherwise) | Rule vs box text | |
| 6 | G408: is it YELLOW only (manual) or MAJOR + YELLOW (QA23)? | Penalty level | |
| 7 | Our intake arm floats on a hinge (no actuator) and rides over balls: must the robot fit inside 18 × 24 × 29 in at every point of the arm's swing (Fig 12-6)? | R105 | |
| 8 | How may we tune the Limelight here: USB tether to a laptop? | R704.D, QA26, T405 | |
| 9 | Did today's HIVEs pass their calibration checks (FAC 2.1–2.7)? | Tip counts | |
| 10 | G421 cites "G421.D": does that mean B (2 ft from where the pin started)? | Typo | |
| 11 | May NECTAR that left the field be re-entered at any time, not tied to tips or 1:00? | G426 box; QA36 | |
| 12 | Is launching a ball into the top of a FLOWER legal, or must it be placed? | G418.A vs §10.5.2 box | |

---

## Sources

- **Competition Manual TU05**: https://ftc-resources.firstinspires.org/ftc/game/manual (pages as cited)
- **Team Updates TU00–TU05**: https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **Q&A board**: https://game-qa.firstinspires.org/boards/2027/FTC/QA · feed: https://game-qa.firstinspires.org/boards/2027/FTC/QA/answers.atom
- **Field Acceptance Checklist**: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
- **Inspection Checklist**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check
