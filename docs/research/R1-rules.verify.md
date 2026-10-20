# R1 Rules Digest: adversarial fact-check notes

Checked 2026-10-20 against these sources:
- Competition Manual TU05. I read the text and visually checked the pages where digits matter: p.65-67, 71-72, 76-77, 79, 82, 88-90, 108, 123-125, 133.
- Team Updates TU00-TU05. Highlights were checked in the TU01 and TU02 PDFs.
- EFSG V1.0 §8, §11 and §12.
- FAC rev 26-27.2, FMG rev 26-27.1, FRG rev 26-27.1 and IQR rev 26-27.1.
- The AndyMark product JSON.
- The SWYFT product page, read live.
- The live Q&A board and Atom feed.
- The live FiM events page and the live FTC Events page for team 12303.
- The archived 2025-26 DECODE manual (V6).
- The REV Control Hub specifications page.

Verdict key: C = confirmed, X = corrected, R = refuted, U = unverifiable.

## Verdicts

| ID | Verdict | Evidence / notes |
|---|---|---|
| F1 | C | Table 9-1 on p.79 (checked visually) lists the 2:30 start, 2:00 AUTO end with "Buzzer x 3", the 0:08-0:01 transition, TELEOP at 2:00 with "3 Bells", the 1:00 "Chime" for "FLOWER Ownership Unlocked", the 0:20 Train Whistle and the 0:00 3-second buzzer. The text says "visual FIELD timers are the authority". G403 forbids any powered movement in the transition, and its box says INIT that makes actuators "move or twitch in any way" means wait. G410 and G426.B cover the 1:00 changes. |
| F2 | C | Table 10-2 on p.91 gives LEAVE 3; PARK 5 and 5; TIP 20 and 20; CELL contents 2; Bottom NECTAR 5; owned FLOWER 2; GARDEN 1. Table 10-4 on p.92 gives MINOR 5 and MAJOR 20. |
| F3 | C | Tables 10-2 and 10-3 on p.91 give SWARM 16 points, POLLINATOR 1 at 4 TIPS and POLLINATOR 2 at 7 TIPS for "All Other Events". FIRST Championship and Regional Championship thresholds are "TBA", and Premier Events set their own. QA14 (answered 2026-10-10) says no new RP achievements and fixed match point values. The maximum is 3+1+1+1 = 6. |
| F4 | C | EFSG §12 tunes each HIVE to tip with 8P+0N and 3P+3N. The §12.3 table says 3N+1P plus a 2nd tossed POLLEN must not tip, 3N+2P plus a tossed 3rd must tip, 6P plus a tossed 7th must not tip, and 7P plus a tossed 8th must tip. FAC items 2.3-2.6 repeat this. FAC 2.1 and FMG §2 expect some variation and put red/blue consistency first. EFSG 11.1 and FRG E.ii put 3 NECTAR in the starting up cell. |
| F5 | C | The AndyMark spec tab lists "Diameter 2.8 in., 3.6 in." and "Weight 0.055 lbs., 0.091 lbs" (ratio 1.65). Caveat: AndyMark's per-variant shipping data is 27 g POLLEN against 41 g NECTAR, a ratio of about 1.5. EFSG §12.2 only says 3P+3N has less mass than 8P, which fits both figures, so treat 1 NECTAR as about 1.5-1.65 POLLEN. The p.69 phrase "POLLEN or NECTAR are LAUNCHED" and the G417 box confirm that NECTAR counts. G408 covers controlling opponent NECTAR. |
| F6 | C | The p.87 box says LAUNCHING into the up cell is "the only allowed way". G417 forbids manipulating HIVE motion any other way. Nuance: box items D and E say "likely to be perceived as STRATEGIC". They are not automatically STRATEGIC; otherwise the penalty is a VERBAL WARNING. R105 caps height at 29 in, and Fig 9-10 and FAC 1.13 put the opening bottom at 53.5 ± 0.5 in. |
| F7 | C | G409 and its box (p.109). QA17 says deflection off a robot under a tipped HIVE is a violation, a VERBAL WARNING if not STRATEGIC, and G409's STRATEGIC penalty is a YELLOW. The p.87 box and QA12 say launching at a moving HIVE can disrupt the tip. |
| F8 | C | p.76-77 (checked visually) give 30-33 for red far ("Red Scoring Tags"), 34-37 for red audience, 38-41 for blue audience and 42-45 for blue far. p.74-75: 36h11, 3.25 in, on the bottom of each CELL facing the TILES. EFSG 11.1, FRG E.i and FAC 1.11 ("Audience-facing CELLS have AprilTags 34-41") agree. |
| F9 | C | p.88: an element counts if "at least partially within" the volume. The top-most NECTAR owns the FLOWER and scores every element "regardless of which ALLIANCE placed" it. The bottom-most NECTAR earns the bonus. Fig 10-5 counts (zoomed): A6, B5, C6, D8, E4, F4, G6, H7, so the range is 4-8. "No NECTAR means 0" is a sound inference, since points are only for an owned FLOWER. |
| F10 | C | G410 text names only NECTAR, carries a MAJOR per NECTAR, and its box says the primary FIELD timer is the cue. The §10.5.2 box says "FLOWER scoring cannot begin until there is one minute remaining… per G410". The ambiguity is real. Rule text beats box text (§1.7.1), and G418.A has no time limit for POLLEN. |
| F11 | C | G418 A/B and its box ("only allow POLLEN (not NECTAR) to be removed from the bottom of the middle ring"). p.72 gives the 3.55 in retrieval opening. FAC 2.8-2.11 covers the NECTAR not escaping and POLLEN staying on top of a NECTAR. FMG §3 gives 17.5 ± 0.125 in from the middle-ring bottom to the top-ring top. Side note: Fig 10-5 F draws a NECTAR below the middle ring, marked as not scoring; NECTAR can only legally enter through the top. |
| F12 | C | G407 rule and box on p.108 (checked visually) say "keep 4 and discard the rest". The CONTROL glossary on p.171 says "directly or transitively through other SCORING ELEMENTS". Box D-F list bulldozing, deflecting and launched balls as not CONTROL. QA3 and QA23 say there is no MOMENTARY exception. |
| F13 | C | G408 on p.108 (checked visually) reads "VERBAL WARNING. YELLOW CARD per MATCH, if STRATEGIC", with no MAJOR. QA23 says both G407 and G408 are "VERBAL WARNING. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC". The discrepancy is real, and §1.7.4 says the Q&A does not supersede the manual. QA4 says no momentary exception. TU05 adds the G408 sorter text. |
| F14 | C | G402 (p.106): "MAJOR FOUL per MATCH. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC". The box gives the A-C / D-F split, each alliance's priority, and calls crossing "a risky gameplay strategy". QA15 and QA24 were still ACCEPTED on the live board. Nuance: the violation is *disrupting* the opponent's AUTO; crossing alone is not itself the foul. Table 10-6 caps "per MATCH" penalties at 1 MAJOR (+1 YELLOW) per team per match. |
| F15 | C | §10.5 F says LEAVE and AUTO PARK are assessed at the end of AUTO. QA21 says touching any wall at the end of AUTO means no LEAVE. QA28 says a partner may push or pull you off the wall. LOADING ZONE: p.65-66 gives 23x11 in including tape, alliance-specific. EFSG 8.3 places red on A5 and blue on F2. FAC 3.2-3.4 checks seams 4-5 and 1-2 and the 11 in width. |
| F16 | C | Arithmetic from Table 10-2: 3+5+5 = 13 per robot and 26 per alliance. 13 < 16, so both robots are needed. 3+3+5+5 = 16 and 8+8 = 16. |
| F17 | C | Arithmetic is right: 3 + 3x8 = 27 and 3 + 6x8 = 51. The supply split (16/16/8) matches p.83. Treat 27 and 51 as idealized lower bounds: they assume every launched ball stays in the cell, and calibration is done with balls gently placed or tossed. |
| F18 | C | G426 (TU02 highlights checked): "one additional NECTAR" each time "the HIVE of their corresponding ALLIANCE color is TIPPED"; all remaining NECTAR at ≤60 s; MINOR per NECTAR. The box adds "re-entered at any time" and "does not have to be entered … immediately". QA11 allows entry during AUTO. G427.C requires a tile-first landing. QA6 requires a drop. QA7 allows a throw or roll if it lands in the LOADING ZONE first. QA36 is still pending. |
| F19 | C | Table 10-1 (p.82, checked visually): DRIVE COACH max 1 and may be an adult; DRIVER and HUMAN PLAYER share a combined max of 3 and must be STUDENTs. At most 1 non-STUDENT, and at most 4 people in total. G424, G425 A-D and G428 confirmed on p.116-118. |
| F20 | C | G304 A-H (p.104), including the box line "not overhang the FIELD perimeter wall". TU03 adds the pre-loads "fully contained on its own ALLIANCE side". R102 says "Any pre-loaded SCORING ELEMENTS may extend outside the starting size constraint". |
| F21 | C | G419 box: "highly interactive … defensive and high-contact". QA2, QA8 and QA20 allow shot blocking. G420, G421 (MAJOR plus a MAJOR every 3 s), G411 (MAJOR+YELLOW, no warning step) and G204 confirmed. "protect" appears only in the GARDEN not-protected lines (p.89, p.110). The G419/G420/G417 box items are "likely STRATEGIC" examples. |
| F22 | C | No rule mentions a launch location; I searched for LAUNCH and zone across the manual. LAUNCH glossary on p.173. QA10: "no rule that specifically disallows LAUNCHING your own NECTAR (or POLLEN) into an opponent HIVE". G417 box item D covers launching to impede a tip. |
| F23 | C | §10.5.3 on p.89. EFSG 8.4 places red on A1 and blue on F6. FAC 3.5 (audience-side edge of A1) and 3.6 (scoring-side edge of F6). p.83 stages 4 POLLEN in each GARDEN. |
| F24 | C | Fig 9-4/9-5 (p.67) and Fig 9-17 (p.77), checked visually: far wall at the W (B\|C) seam, red wall at seam 2 (rows 2\|3), blue wall at seam 4 (rows 4\|5), audience wall at the Y (D\|E) seam. Mapping LZ A5→F2, GARDEN A1→F6, flowers and HIVE start states confirms 180° rotational symmetry. p.72 and FAC 1.7 give 4 in at 21.5 ± 0.25 in. |
| F25 | C | R102 and R105 A-C (p.122-123). The R105 box requires mechanical limits. Fig 12-6 (checked visually) draws the orange sizing box rotated in Position B. G416 confirmed. |
| F26 | **X** | Most of F26 is confirmed: R503 is 8+8 across all configurations; R501 lists the 5201-5204 series and SR-MOTOR-DC-01; R502 is ≤8 W and ≤4 A at 6 V (p.133, checked visually); IQR p.4 lists all three Dual Mode variants at 2.65-2.81 W and 2.5 A; the R612 box and IQR p.9 ban the Travel Tuner. **Correction:** the previous servo cap was **10**, not 12. The DECODE 2025-26 manual (V6) R503 reads "ROBOTS are limited to a total of 8 motors and 10 servos". TU00 only says "reduced to 8". |
| F27 | C | The SWYFT page (read live) gives SKU SR-DRIVE-02 as the 86 mm "Module Pack of 4", with "the SWYFT Spike motor (SR-MOTOR-DC-01)" and a "12.7:1 gear ratio". That is 4 motors, and 8-4-1 = 3. R301.A covers COTS drive CHASSIS, and R303.I covers the holonomic wheel exception. |
| F28 | C | R701 (Control Hub plus ≤1 Expansion Hub). R702 Table 12-9 lists only LL_3A, and Example 6 prohibits the Limelight 3G. R707/R708 confirmed. R704.D (p.141): "FTC Dashboard, FTControl Panels, and others are prohibited. No continuous video stream is allowed." QA26: "Wi-Fi streaming is not allowed anywhere in a competition venue". |
| F29 | C | §13.6 and Table 13-1 (p.155): RS, then average match points minus fouls, average TIPS, average AUTO points, random. p.153: 5 or 6 matches, and championships may schedule more. §14 (p.167): League Meets play 5-6; the League Tournament adds each team's top 10 League Meet matches; verbal warnings and cards clear after each League Meet. |
| F30 | C | p.156-157: double elimination, T703 (no backups), Table 13-2 (2/4/6/8 alliances), and playoff DQ applies to the whole alliance. §10.6.3 says playoff cards go to the whole alliance. |
| F31 | C | §4 (p.27-30) and Table 4-1. FiM page re-read live today: Freeland Oct 30-31 is CANCELLED, so qualifiers run Nov 06-07 to Dec 05-06; League: Metro Detroit Tournament at Wayne Cty Comm College, Belleville, Dec 04-05; States-SE at Howell, Dec 12/13; Round 3 registration "Opens Wed., Oct 21, 7am". FTC Events (live): "Shumate Sith", Rockwood MI, home region "Michigan - FiM", no league listed, and "has not yet participated at any official events". |
| F32 | C | STRATEGIC is defined on p.92 ("deliberate or reckless with foreseeable consequences"). §10.6.1 and G205 confirmed. Precision: 20 of the 28 G4xx rules start at a VERBAL WARNING, but only 11 escalate to MAJOR+YELLOW. G408, G409, G414 and G415 escalate to YELLOW only. **G402, G405, G410, G411, G421, G426, G427 and G428 have no warning step.** |
| F33 | C | G403 and G404 boxes say inertia, gravity and de-energizing are not powered movement. §10.5 A and B; the p.86 box also says actions in the transition or after 0:00 "may be subject to penalties". |
| F34 | C | TU text and highlights checked. TU01 corrected "Bottom of HIVE above TILES" to 30.6 in and changed T301 wording to "representatives". TU02 highlights cover "additional", "re-entered at any time" and "does not have to be entered … immediately", plus Table 10-6. TU03, TU04 and TU05 as stated. §1.7.3 (p.19): "Every Thursday" and "published after the driver's meeting … will not apply". |
| F35 | C | The live board re-fetched at about 12:54 local is identical to the earlier snapshot: 38 questions, 26 ANSWERED and 12 ACCEPTED (15, 24, 29-38). The Atom feed was last updated 2026-10-19T20:54Z. |

## Errors or overstatements elsewhere in R1 (not in the key-fact list)
1. **§2.6 "aim point moves about 19 in" is wrong.** 18.84 in is the *gap between* the cells (Fig 9-9). Each cell is a pentagonal prism lying along the HIVE beam: 12.04 in deep, HIVE 42.91 in long. The opening is the **outer** pentagon face, so when the beam tilts 30° the opening plane leans 30° back from vertical and faces **outward toward that cell's end wall and upward**. Checks:
   - Fig 9-10 marks the 53.5 in and 65.6 in heights on the up cell's outer frame.
   - 65.6 − 53.5 = 12.1, which equals 14·cos 30°.
   - The bottom-face tags of the up cell face down and outward; the CAD tag normals extracted by another agent agree.
   - FRG E.i: "the cells facing down are those 'pointing at' flowers on the audience and far walls".
   - EFSG 11.1: up-cell NECTAR rest against the inner "Back Skin".
   - So after each TIP the target jumps to the other end of the HIVE (about 2.5-3 ft away), facing the opposite wall. Shooters have to work from the audience half or the far half in turn.
2. The cheat sheet and trap #11 say crossing in AUTO is a "MAJOR FOUL even if accidental". G402 penalizes *disrupting* the opponent's AUTO; crossing alone is "risky", not the foul. Accidental disruption still costs one MAJOR per match.

## Important items R1 missed
1. **HIVE geometry and strategy (inference from Fig 9-7/9-9/9-10/9-11, EFSG 11.1, FRG E.i):**
   - Red's first up cell faces the audience wall, so red's first-tip shooting is from the audience half of red's side. Blue mirrors this from the far half.
   - When a cell tips, it spills out of its opening on that same outer side, which is where the alliance was shooting from. That puts shooters at G409 deflection risk.
   - The up cell's AprilTags (on its bottom face) look down and outward toward the shooting side. The down cell's tags look down and inward, under the HIVE.
2. **T405 field calibration time (p.151).** If the event offers it, robots may run OpModes, CONTROL elements, connect to laptops and be hand-placed for sensor calibration. No driving and no LAUNCHING. This is the legal window to tune the Limelight on the real field. Combine it with QA26: no Wi-Fi streaming anywhere at the venue.
3. **VERBAL WARNINGS persist.**
   - Non-G301 warnings carry from qualifications into playoffs (p.94).
   - "Multiple VERBAL WARNINGS … over multiple MATCHES for the same violation" draw STRATEGIC scrutiny (p.92 box).
   - "Accidental situations that are then deliberately used to a team's advantage will be viewed as STRATEGIC."
   - A wide intake that keeps picking up G407 or G408 warnings can be upgraded to STRATEGIC.
4. **Fouls with no warning step:**
   - G402: MAJOR per match.
   - G405: MAJOR per element.
   - G410: MAJOR per NECTAR (two early NECTAR = 40 pts).
   - G411: MAJOR + YELLOW.
   - G421: MAJOR, plus a MAJOR every 3 s.
   - G426, G427, G428: MINOR each.
5. **REV hub servo power.** The Control Hub spec allows 2 A per servo-port pair (0-1, 2-3, 4-5) and 5 A total, shared with the +5V ports (docs.revrobotics.com control-hub-basics). The IQR lists Dual Mode servos at 2.5 A stall. Spread the servos across port pairs, or power them through a goBILDA 6V Servo Power Injector or REV Servo Hub (both legal under R505). These also give 6 V instead of 5 V.
6. **No flywheel pre-spin.**
   - R102 requires "all parts … fully stationary" at the start, and G304.H requires the robot "fully motionless following completion of OpMode initialization".
   - G403 forces the flywheel to coast through the 8 s transition.
   - Budget spin-up time at the start of AUTO and again at the start of TELEOP.
7. **Element and field variation.** "POLLEN and NECTAR are not perfectly spherical and may vary in size" (p.74). Manual dimensions are ±1 in unless noted (p.63). FAC tolerances: FLOWER top 21.5 ± 0.25 in, CELL opening bottom 53.5 ± 0.5 in.
8. **POLLEN does not fall out of a FLOWER on its own.** FAC 2.8: the "bottom ring prevents static POLLEN from exiting". FAC 2.9: removal takes "two sideways hand movements". The bottom-ring hole is 2.79 in, against 2.8 in POLLEN. AUTO extraction from a FLOWER needs an active sideways push through the 3.55 in opening.
9. **G202.B.** "throwing MATCHES, deliberately missing RPs, or playing beneath your ability to manipulate rankings" is a violation. This matters for any "don't tip / don't park" ranking idea.
10. **T406.** There is a minimum of 5 min between back-to-back qualification matches, and 8 min in playoffs.
11. **G426.A wording.** The text unlocks NECTAR whenever the alliance's own-color HIVE is TIPPED, by anyone. QA11 phrases it as "their ALLIANCE causes a TIP". This only matters in the edge case where an opponent tips your HIVE (QA10).
