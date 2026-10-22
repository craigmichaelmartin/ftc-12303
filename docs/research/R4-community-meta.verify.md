# R4 verification notes: adversarial fact-check of "Early-season meta and community intel"

Checked 2026-10-20 against primary sources. Downloaded material is treated as data only.

## Method and evidence base

- **Competition Manual TU05.** Text plus visual checks of p72-74, p108-109 (G407/G408/G409/G410 penalty lines) and p83-91, p106-118, p123, p155, p167.
- **Event Field Setup Guide V1.0.** Re-downloaded 2026-10-20 from https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide. The md5 matches the researcher's copy, so it is still V1.0 (PDF created 2026-09-12). §12 was checked visually on pp. 26-28.
- **Field Acceptance Checklist Rev 26-27.2.** https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check, created 2026-10-16. Checked visually.
- **Field Mitigation Guide V26-27.1.** https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide, created 2026-10-16.
- **Team Updates 00-05.** From the local tu-combined.
- **Official Q&A board.** Re-fetched live; byte-identical to the researcher's snapshot. Answer timestamps (UTC) come from the embedded JSON.
- **FTCScout REST API.** Re-downloaded fresh for all 12 events with matches (no changes), plus team stats, awards and 12303 history. All statistics were recomputed with my own scripts:
  - `scratch/scripts/v4/verify_stats.py`
  - `scratch/scripts/v4/verify_teams.py`
  - Data: `scratch/downloads/v4-ftcscout/`
- **FTC Events pages.** 2026/team/12303, the MD league page, 2026/USMIMDLT and 2025/USMIMDLT, all fetched live.
- **FiM events page.** Local copy.
- **Vendor documents.** goBILDA assembly PDF and example code, the goBILDA resource-guide page (live), the REV build guide, REV docs (live), the AndyMark manual and code.
- **Limelight docs (live).** Also the FTC SDK 12.0.0 `Limelight3A` class (method names extracted from the AAR).
- **Community sources (citation checks only).** Chief Delphi JSON (local, plus a live fetch of 524986), the Mona Shores docs, YouTube metadata, the Reddit RSS and the Hive Vision GitHub README.

## Verdict summary

| ID | Verdict | One-line reason |
|---|---|---|
| F1 | confirmed | Field guide §12/§12.3, verbatim, checked visually; still V1.0 at the official URL |
| F2 | confirmed | Manual §10.3.1.B.i, field guide §11.1/§12.3 and G407 support it; 10 Saline AUTO TIPs match official data |
| F3 | unverifiable | Community frame-stepping. The matches and teams are corroborated by official data, but not the timings, and the source contradicts itself on Q26 |
| F4 | corrected | Q12 confirmed. The 14343 2N+5P tip came after their first calibration, not "before calibration" |
| F5 | confirmed | No official document lists intermediate combos. Mass arithmetic suggests "2N needs 6P" is too conservative |
| F6 | confirmed | Recomputed: 35 / 39.45 / 52.2 / 73.9 / 128; AUTO 8 (max 36); TELEOP 25 |
| F7 | confirmed | Recomputed exactly |
| F8 | confirmed | Recomputed exactly (102/104; HIVE share 59.7%) |
| F9 | confirmed | 49.7% / 3.4% / 0%. Table 10-3 checked; FTCScout swarmRp matches LEAVE+PARK ≥16 in all 352 alliance-matches |
| F10 | confirmed | 18.2 / 19.6 / 6.8 / 17.6%; max US autoHiveTips = 1. The quote is in the Mona Shores doc |
| F11 | confirmed | 58.0 / 44.9 / 67.2%. Ten robot slots were no-shows |
| F12 | confirmed | Rankings, playoff scores and awards match FTCScout/FTC Events; 21 of 28 teams are middle/elementary schools |
| F13 | confirmed | Recomputed exactly from FTCScout OPR |
| F14 | confirmed | Data exact; arithmetic right by rules. Caveats on 3 vs 4 POLLEN and on vulnerability (below) |
| F15 | confirmed | Up to 48; 48/110 KZ alliance-matches over 8; 2 matches exceed the 56 elements that exist |
| F16 | unverifiable | Community measurement, accurately cited; "dominant" is not established |
| F17 | confirmed | Assembly PDF, code and resource page. Which mechanism extracts from the FLOWER is the report's inference |
| F18 | confirmed | Build guide pp. 104-106 and REV kickoff-concepts page |
| F19 | confirmed | AndyMark manual and RobitsStarterBotAuto.java |
| F20 | unverifiable | One anecdote; the post says only "the starter bot" (the Stuy class built the goBILDA strafer StarterBot) |
| F21 | confirmed | G407/G408 text checked visually; Q3, Q4, Q23; Q34 still unanswered |
| F22 | corrected | G409/Q17 confirmed. "35-47 in" is the simulator/practice-film range; Saline measured about 35-42 in on 2 spills |
| F23 | confirmed | Q21 (10-10), Q28 (10-19), manual §10.5.F |
| F24 | confirmed | Q11, Q6, Q7 (10-10); G426/G427 text |
| F25 | confirmed | Q2, Q8 (10-13), Q20 (10-10); R105 is 18x24x29 in; Q37/Q38 pending |
| F26 | confirmed | §10.3.1.A arithmetic; G418.B; 14343 quote; all three vendors claim FLOWER extraction |
| F27 | corrected | Limelight stores 10 pipelines, not 8; maps can be uploaded from robot code; per-pipeline ID filters exist |
| F28 | confirmed | FTC Events/FTCScout dates and venues; LT capacity 24; 2025 LT advancement text |
| F29 | confirmed | FTC Events team page; FTCScout history; FiM Round 3 form date |
| F30 | unverifiable | Community and self-reported; citations accurate |
| F31 | unverifiable | Opinions, accurately quoted; "consensus" is overstated |
| F32 | confirmed | Median 51, max 119; all three alliances over 100 included 16750; quote verified |
| F33 | confirmed | YouTube metadata 2026-09-14; no 2026 events for 19049/24909; first event with matches was 09-19 |
| F34 | confirmed | Recomputed; penalty lines checked (visually for G407-G410) |
| F35 | corrected | Claims are self-reported; the ball-chase library is announced for retirement |

## Per-fact notes

### F1 (confirmed)
- The quoted text is in Event Field Setup Guide V1.0 §12, p26: "While teams should expect some amount of variation…".
- §12.3 table (p27):
  - "3rd Pollen is Gently Placed → Tip" and "8th Pollen is Gently Placed → Tip" are only **Preferred**.
  - "Tossed In" → Tip is **Necessary**.
  - 7th POLLEN and 2nd POLLEN after 3N+1P tossed in → No Tip is **Necessary**.
- §12.2 says pieces are "gently placed" in the back of the Cell, contacting the Hive Back Skin.
- The official URL still serves V1.0 (same md5 as the local copy).
- The newer Field Acceptance Checklist (2026-10-16) restates the event-day test as items 2.3-2.6:
  - 2.3: place 3N+2P, should not tip.
  - 2.4: toss 1 more POLLEN, should tip.
  - 2.5: place 7P, should not tip.
  - 2.6: toss 1 more, should tip.
  - 2.1 adds "Subtle variations may occur. Prioritize consistency between red and blue HIVES on each field."

### F2 (confirmed)
- Manual §10.3.1.B.i (p84): "3 NECTAR in each upward-facing CELL of corresponding color", placed "contacting the back wall of the CELL".
- Field guide §11.1: the red audience-side CELL and the blue scoring-side CELL start up.
- G304.G as amended by TU03 (2026-10-01): exactly 4 POLLEN preloads. G407: 4 maximum.
- Official data shows each of the 10 tabulated Saline AUTO TIPs (Q9b, Q16b, Q19b, Q20b, Q25b, Q26r, Q29r, P3r, F10r, F10b) with autoHiveTips = 1 and the named teams.
- The commentator quote is in the Mona Shores doc.
- Planning note: 8 POLLEN is exactly two 4-piece loads, so a single miss or bounce-out forces a third load or a NECTAR.

### F3 (unverifiable)
- Not checkable against primary data: the timings come from a single community frame-stepping session.
- Corroborated: all 10 listed TIPs exist in official data with the right teams, and Q29 red's "20-point penalty" matches penaltyPointsByOpp = 20.
- Source caveats:
  - "Eleven TIPs were measured; ten swings are in the table."
  - Swing table minimum is 0.7 s, so the "0.65" lower bound does not appear in it.
  - The F10 red dwell of "≤3.8 s" is excluded from the 0.25-3.4 s range.
  - The doc calls Q26 "also exactly three POLLEN" in one section and an over-threshold "volley" in another. That weakens the "overload shortens dwell" inference, which rests mainly on Q9 and Q26.
- Saline had 24 AUTO TIPs in total; about 11 were measured.

### F4 (corrected)
- Q12 confirmed verbatim, answered 2026-10-10 05:12 UTC.
- The Reddit post exists: "Calibration of HIVE", 2026-10-20 12:33 UTC, "6 nectars/9pollen", on a team's newly arrived field.
- Stuy (CD 525071 #8, 2026-10-10): "we built some stuff wrong in the field… It is accurate now."
- Correction: 14343 (CD 524080 #12, 2026-09-27) had already calibrated its red hive that day. They called the 2N+5P tip "admittedly sloppy… this shouldn't tip, our hive may need some more calibration". It was not an uncalibrated hive.
- Inference: by mass, 2N+5P (about 0.457 lb) exceeds 3N+3P (about 0.438 lb), which must tip. That tip may therefore have been correct behavior.

### F5 (confirmed)
- Searched the manual, field guide, Field Acceptance Checklist, Mitigation Guide, TU00-05 and all 38 Q&As. The only official counts are 8P/0N and 3P/3N (tip) and 7P and 3N+2P (no tip).
- The CD quotes match: 524080 #5 (2026-09-14) gives "0n 8p 1n 7p 2n 6p 3n 3p 4n 1p 5n 0p" with no source; 525071 #7 (2026-10-10) gives "7p 1n, 3p 3n, and 5n 0p".
- Inference, using AndyMark's 0.055/0.091 lb and ignoring lever arm:
  - Calibration puts the tip mass between about 0.385 lb (7P, 3N+2P) and about 0.44 lb (8P, 3N+3P).
  - So 2N+5P (0.457) and 5N (0.455) should tip, and 7P+1N (0.476) clearly tips.
  - 1N+6P (0.421) and 4N+1P (0.419) are marginal.
  - The community "2N needs 6P" line is probably one POLLEN too conservative.

### F6-F11, F13 (confirmed; recomputed from fresh FTCScout data)
- **Scope.** 11 US events, 176 matches, 352 alliance-matches, all of type NonAdvancement.
- **Scores.** Non-penalty median 35, mean 39.45, p75 52.2, p90 73.9, max 128. AUTO median 8, max 36. TELEOP (dcPoints) median 25.
- **TIPS.** Mean 0.912. Distribution 0:155, 1:119, 2:46, 3:20, 4:10, 5:2. P(≥1/2/3/4) = 56.0 / 22.2 / 9.1 / 3.4%. Per match: mean 1.82, max 7. One-alliance max 5.
- **Win rate by alliance TIPS.** 27.7 / 48.7 / 78.3 / 90 / 100%. Where TIP counts differed (104 matches), the alliance with more TIPS won 102 times. HIVE share of non-penalty points is 59.7%.
- **RP.** SWARM 49.7% (175/352), POLLINATOR 1 3.4% (12), POLLINATOR 2 0.
- **AUTO.** TIP 18.2%, LEAVE+PARK ≥16 19.6%, both 6.8%, zero 17.6%. Max autoHiveTips at US events is 1.
- **Per robot.** LEAVE 58.0%, AUTO PARK 44.9%, END PARK 67.2%. 10 of 704 slots were no-shows.
- **OPR across 132 US team-events.** Median 15.4, p75 23.7, p90 38.8, max 80.1 (16750). 13684 is 79.6. Saline median 21.5, p75 31.2. Top TIPS-OPR is 3.05.
- **Caveats.** These are scrimmage/preview events. "TIPS decide matches" is a correlation, partly mechanical: one TIP is 20 points against a median alliance score of 35.
- **Minor report errors (not key facts).** The report's "Playoffs (n=20)" row is Saline-only; all US playoffs are 24 alliance-matches (median 54, mean 60, 1.58 TIPS). Rookie Rumble lists 23 teams, but only 19 played.

### F12 (confirmed)
- **Rankings (FTCScout, which mirrors FTC Events):**
  - #1 13684: RS 4.8, 105.6, 3.8 TIPS, 5-0.
  - #2 10644: 101.6, 3.6.
  - Medians: 50.3 points, 1.3 TIPS, 14.4 AUTO.
- **Playoffs:** 128-41 (P3), 107-84 (P7), Final 113-76 (P10).
- **Awards:** Winner 13684 + 10644; Finalist 15465 + 15229; Inspire 13684 / 10136 / 23392; Design 1st 10644.
- **Teams:** 21 of 28 list a middle or elementary school.

### F14 (confirmed, with caveats)
- Exactly 4 of 924 flower end-states contain NECTAR: KZ m34, m38 and P5 are 3 POLLEN + 1 NECTAR = 13; OHCON m5 is a lone blue NECTAR = 7. Flower points appear in 4 of 462 alliance-matches (0.87%).
- US flowers with ≥3 POLLEN: 42% (red wall), 53% (blue wall), 44% (scoring wall), 36% (audience wall).
- The rules arithmetic follows §10.5.2 (p88), and the owner is credited "regardless of which ALLIANCE placed".
- **Caveat 1 (geometry).** Manual Fig. 9-12: bottom ring 0.43 in plus retrieval opening 3.55 in puts the middle ring about 4 in up. The bottom staged POLLEN (2.8 in, on the tiles) cannot be in the scoring volume. Realistically 3 staged POLLEN count, giving 13 points; 15 needs an added POLLEN. FTCScout has 4-POLLEN records in 126 of 704 US flower states, which probably reflects scorer convention.
- **Caveat 2 (vulnerability).** Checklist 2.9 shows POLLEN below a NECTAR can still be pulled out of the bottom ring. An opponent can strip those POLLEN, leaving about 7 points, or cap with their own NECTAR to take ownership. The capped alliance keeps the 5-point bottom bonus.
- **Caveat 3 (constraints).** G410 limits this to the last 60 s, the NECTAR must come in through the LOADING ZONE (G426/G427), and it needs a 4.0 in opening reached at 21.5 in.

### F15 (confirmed)
- KZASN1: 48 of 110 alliance-matches report more than 8 pieces left in the up-CELL (max 48). NP max is 248, with one POLLINATOR 2.
- In 2 matches, the two alliances' leftovers plus flower and garden contents exceed the 56 SCORING ELEMENTS that exist (m30001: 22 + 43 = 65), which is definitely impossible.
- For a single alliance, a value of 9-48 contradicts a §12.3-calibrated HIVE. It is not strictly physically impossible if a HIVE jammed.

### F16 (unverifiable)
- CD 524080 #12 (2026-09-27): "24 / 43 shots into the hive (counting only shots that actually landed in the hive…) … bounceout rate of 44.18%". This was a Ri3D with a fixed solid hood, on their own field.
- Mona Shores frame analysis saw no in-and-out bounce in the 8 countable Infinity Tech shots.
- So "dominant shooting failure" is plausible but not established.
- CyberCoyotes issue #21 (opened 2026-10-16) is a test plan. Its physics is consistent: a hood wheel at about 2x flywheel RPM gives zero spin when the hood wheel is half the diameter.
- SimpleBot description: "under hand toss creates topspin which seems to help".

### F17 (confirmed)
- **Code.** LAUNCHER_TARGET_VELOCITY = 1250 and MIN = 1200 ticks/s (28 ticks/rev, so 1250 ticks/s is about 2,679 rpm). PIDF (40, 0, 0, 12.5). AUTO launches until autoTimer > 10 s, then drives -120 mm.
- **Assembly PDF (2026-09-11).**
  - Drop-center plates; 19.2:1 Yellow Jacket drive.
  - 50.9:1 Yellow Jacket intake with 48 mm Gecko wheels.
  - Two 72 mm Gecko wheels on speed servos.
  - Windmill torque servo.
  - Hogback 96 mm QTY 5: four on the drive, one on the launcher (step 57).
  - One 19.2:1 Yellow Jacket converted to 1:1.
- **Code zip** files are dated 2026-09-25.
- **FLOWER extraction.** The goBILDA resource page says "Pull POLLEN out of the FLOWER and directly into the intake". The PDF and code mention only "pull elements out of corners", so tying FLOWER extraction to the corner servos is an inference.

### F18 (confirmed)
- Build guide: 2x 20:1 UltraPlanetary HD Hex drive; Core Hex with 8x 40A flaps; feeder of four 1 in grip wheels on a Smart Robot Servo.
- Launcher: two 90 mm grip wheels plus four 90T gears on a 252 mm shaft, HD Hex with 1:1 output (p105-106). The "pollen poker" is on p85.
- REV docs: "Comb out Pollen from the Flowers using the servo-powered poker"; "Auto… launch pre-loads… either side".

### F19 (confirmed)
- Manual: "Simple Skid-Steer Chassis", servo rubber-band rollers ("consumable"), gravity-fed hopper, one-motor coaxial flinger with elastic or springs, FLOWER roller or finger versions.
- Manual: "up against the field wall with the launcher centered on the goal".
- Code: dpad delay "for coordinating the tipping of the goal with an alliance partner", 5x launch(), then drive 18 in.

### F20 (unverifiable)
- CD 525071 #10 (2026-10-19): "In the starter bot, the RPM speed is too high…". No vendor is named.
- Post #1 says the class built "the gobilda strafing starter bot", so the attribution is plausible. It is still one anecdote: shooting distance and field calibration are unknown. "Known StarterBot issue" is overstated.

### F21 (confirmed)
- Visually checked (p108-109):
  - G407: "VERBAL WARNING. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC", plus the "keep 4 and discard the rest" orange box.
  - G408: "VERBAL WARNING. YELLOW CARD per MATCH, if STRATEGIC".
- Q3, Q4 (10-10) and Q23 (10-13) say there is no MOMENTARY exception. Q34 and Q29 are still unanswered.
- Nuances:
  - G407 example C: a momentary 5th piece reversed quickly is likely not STRATEGIC, but it is still a VERBAL WARNING.
  - Q4: immediately ejecting opponent NECTAR "would typically not be considered" STRATEGIC.
  - Q23's "MAJOR FOUL and YELLOW CARD" for G408 conflicts with the manual. TU05 changed only G408's orange box, not its violation line.

### F22 (corrected)
- The rule part is right. G409 (p109) and Q17 (answered 2026-10-13) say a robot under a HIVE that has spilled pieces bounce off it violates G409, as a VERBAL WARNING unless STRATEGIC. The rule covers any TIPPED HIVE.
- The G409 orange box adds that it is "not the intent to penalize accidental catches", and example D (one or two POLLEN landing while driving by) is not STRATEGIC.
- Correction on the spill zone: "35-47 in" is the simulator's assumed range (the "filmed" practice-field range). Saline measured about 35-42 in from the wall the CELL faces, on 2 clean spills (Q26, Q29), from a camera only about 15° above the floor. That is low precision, and the report treats it as firmer than it is.

### F23, F24, F25 (confirmed)
- **F23.** Q21 (10-10) says LEAVE is judged at the end of AUTO, and touching any wall then means no LEAVE. Q28 (10-19): being pushed off by a partner counts. Manual §10.5.F agrees.
- **F24.**
  - Q11 (10-10): NECTAR entry is allowed after each own-HIVE TIP in AUTO.
  - Q6: the NECTAR must be dropped in.
  - Q7: it may be thrown or rolled if released before it touches the tile, touches the LOADING ZONE tile first, and does not end up scored.
  - G426.B: from 60 s left, all remaining NECTAR may be entered. G426 and G427 are each a MINOR FOUL per NECTAR.
- **F25.** Q2 and Q8 (10-13) and Q20 (10-10): blocking is not specifically disallowed, but G419 (impairment), G420 and G407/G408 (if CONTROL occurs) still apply. R105 (p123) is 18 x 24 x 29 in, physically constrained. G416 is a MAJOR FOUL if STRATEGIC. Q37 and Q38 are pending.

### F26 (confirmed)
- §10.3.1.A: 16 POLLEN in FLOWERS, 4+4 in GARDENS, 16 preloads, so 16 of the 24 non-preload POLLEN start in FLOWERS.
- G418.B: POLLEN may be removed only from the bottom.
- CD 524080 #27 (2026-10-19): "completely starved of pollen".
- All three vendors claim FLOWER extraction (see F17-F19).

### F27 (corrected)
- Limelight docs: "LimelightOS stores up to 10 unique vision pipelines" (getting-started/pipelines), and FTC docs: "You can set up 10 different pipelines". The "8 pipelines" claim is wrong.
- AprilTag pipelines have per-pipeline **ID Filters** ("specify exactly which tags you care about").
- The FTC SDK 12.0.0 `Limelight3A` has `uploadFieldmap(LLFieldMap, Integer)` and `pipelineSwitch(int)`. FTC docs show `limelight.uploadFieldmap(fieldMap, null); // null means use the default slot`.
- So maps can be uploaded from the OpMode at INIT according to alliance; no manual swapping is needed. Robot code can switch pipelines and filter `getFiducialResults()` by ID (only the cluster of the CELL known to be up).
- There is no orientation filter. Whether there is truly "one map slot" is not documented.
- The "9.4 in higher" and "tags vanish when tipping" claims are Mona Shores community notes and remain unverified.
- The tag IDs are confirmed by Checklist Fig. 4:
  - Red Scoring 33|32|31|30
  - Red Audience 34|35|36|37
  - Blue Audience 38|39|40|41
  - Blue Scoring 45|44|43|42

### F28, F29 (confirmed)
- **MD meets.** Meets are on 10/20, 10/24, 10/30, 11/7, 11/11 and 11/19 at Wayne County Community College, Belleville.
- **League Tournament (USMIMDLT).** 12/4-5/2026, capacity 24 (FTC Events). The 2025 LT said "4 teams advance to Michigan State Championship-SE" (18 teams).
- **State championships.** Championship NW is 12/10-12 at Northwood University, Midland. SE is 12/11-13 at Parker MS, Howell. FiM lists match days as Dec 11/12 and 12/13.
- **FTC Events 12303 page.** "has not yet participated at any official events. (Scrimmages not shown)"; sponsors "Gibraltar School District & Shumate Middle School"; no league shown.
- **MD league page.** "Teams have not yet been assigned to this league."
- **FiM page.** Round 3 form "Opens Wed., Oct 21, 7am".
- **FTCScout history:**
  - 2023: USMIMDLT rank 8 (Connect 3rd), USMIMDM4 rank 8.
  - 2024: none.
  - 2025: USMITRS Troy MAYhem 2026-05-15/16, rank 20, Innovate 2nd.
  - Rookie year 2016.

### F30, F31 (unverifiable; citations accurate)
- **F30.**
  - Mona Shores piece-physics doc: first POLLEN at 2.8-3.0 s; intervals 0.14, 0.24 (P3) and 0.20, 0.25 (F10).
  - "Under the HIVE" (Q16, Q25) comes from robot position; the doc says the shot flight was not visible. The audience-wall shots were in F10.
  - WXYZ video title: "100 pts in 125s TeleOp" (self-reported, 2026-09-17). FUN: "custom molded intake and a two flywheel shooter".
  - FUN on 21062: "holding 4 game elements while preventing additional elements from entering".
- **F31.**
  - necessaryevil, CD 524185 #3 (2026-09-16): shooting 4 "in under a second… limiting factor is gonna be element acquisition".
  - CD 524986 (2026-10-06): "Nectar is a trap" (Peyton_Yeung); "10/10 pollen beats…" (necessaryevil).
  - CD 524071 #3: "Defense is inherently worse specialization as a 2-team alliance".
  - "Consensus" means 3-4 posters, and the same 14343 author wrote on 09-27 that "nectar is the way".

### F32 (confirmed)
- FTCScout USWABON1: median alliance 51, max 119. Alliances over 100 were 111 (16750+20403), 105 (99999+16750) and 119 (16750+28139).
- The quote (CD 524080 #27, 2026-10-19) is verbatim, including "score a tip every 30 seconds".
- Note: team "99999" is a placeholder that ranked 4th of 19. 14343 says its rookie squad, running a mecanum goBILDA StarterBot, placed 4th of 19, so 99999 is probably that bot (inference).

### F33 (confirmed)
- Video published 2026-09-14T06:30 PDT with description "Auto 16p TeleOp 108p".
- FTCScout lists no 2026 events for 19049 or 24909, and the first 2026 event with matches was 09-19.
- The official US high is now 128 non-penalty points (Saline P3).

### F34 (confirmed)
- US: 8.0% of alliance-matches had any foul committed; 0.099 majors and 0.037 minors per alliance-match. Saline majors: 2/90 = 2.2%.
- Penalty lines checked (G407-G410 visually; G411 and G417 in text): G411 "MAJOR FOUL and YELLOW CARD per MATCH" (the rule itself requires STRATEGIC conduct); G417 "VERBAL WARNING. MAJOR FOUL and YELLOW CARD per MATCH, if STRATEGIC"; G410 "MAJOR FOUL per NECTAR".

### F35 (corrected)
- The Reddit v2.0.0 post (2026-10-12) claims up to 25 detections per frame, 97.3% precision, and an SSD-MobileNetV2 model for the Limelight 3A. It also lists "Retirement of the Ball Chase Library" under "What's Next".
- The README: classes yellow_pollen, red_nectar, blue_nectar. `ftc_ball_chase_lib` ships in the repo (not yet marked deprecated). The OpenCV Control Hub track reports only about 9-11% raw per-frame candidate precision ("treat as a candidate signal").
- All performance numbers are self-reported.

## Important facts, risks and rules the report missed

1. **New official field documents.** The Field Acceptance Checklist (Rev 26-27.2) and Field Mitigation Guide (V26-27.1), both dated 2026-10-16, are out. The report calls them "coming soon".
   - Every event day, each HIVE must pass the 3N+2P / toss-3rd and 7P / toss-8th tests.
   - "Subtle variations may occur. Prioritize consistency between red and blue HIVES on each field."
   - Mitigation guide: "Some variation in Hive operation is expected within the boundaries described by the calibration process."
   - Use the same tests to calibrate the team's practice HIVE.
2. **FLOWER mechanics (Checklist 2.8-2.10).** NECTAR cannot pass the middle ring. POLLEN below a NECTAR can still be pulled from the bottom ring. POLLEN above a NECTAR cannot be removed.
   - An endgame NECTAR locks every POLLEN above it.
   - Opponents can strip the POLLEN beneath it (13 → about 7 points) or cap it with their own NECTAR (taking ownership; you keep the 5 bottom bonus).
3. **TIP timing.** Manual §10.5.A/B (p86): TIP assessment continues until all elements and robots are at rest after the match, and a TIP completed before TELEOP starts (during the 8 s transition) counts as an AUTO TIP.
   - With about 0.25-3.4 s dwell plus a 0.75 s swing, a threshold-crossing volley in the last seconds of AUTO or TELEOP can still score.
   - Do not stop shooting early. Do not launch at a moving HIVE (§10.5.1 orange box, Q12).
4. **League ranking rules (manual §14, p167; FTC Events MD page).**
   - League Tournament rankings use each team's top 10 League Meet matches plus LT matches.
   - Fewer than 10 meet matches means zeros for the missing ones ("League rankings are always averaged based on 10 matches, regardless of the number played").
   - Meets run 5-6 quals per team, so 12303 must attend at least 2 meets, and 3 or more to drop bad matches.
   - Advancement uses LT performance, except the qualification-ranking points.
   - Cards and verbal warnings clear after each meet. A team may be in only one league.
5. **Q10 (2026-10-10).** No rule stops launching your own NECTAR or POLLEN into the opponent's HIVE. A wrong-HIVE shot just helps them. Use tag IDs (red 30-37, blue 38-45) to confirm the target.
6. **G403 (p106-107).** No powered movement during the 8 s AUTO→TELEOP transition. If TELEOP INIT twitches actuators (turret homing, flywheel spin-up, servo init), wait for TELEOP to start before pressing INIT. This matters for 12303's servo-driven turret.
7. **Table 13-1 sort order.** Ranking Score, then average non-penalty points, then **average TIPS**, then average AUTO points. TIPS are an explicit tiebreaker.
8. **G417 and §10.5.1.**
   - Launching into the up-CELL is the only legal way to TIP.
   - Contacting the HIVE directly or through a CONTROLLED element is likely STRATEGIC (MAJOR + YELLOW CARD).
   - Deliberately launching at external CELL faces or to impede an opponent TIP is listed as STRATEGIC.
   - A missed shot that hits the CELL is not.
9. **Michigan comparator.** Saline had 24 AUTO TIPs in 90 alliance-matches (26.7%), against 18.2% US-wide, and 21 of its 28 teams are middle or elementary schools. Michigan middle-school competition is stronger than the US baseline.
10. **Mass-model inference (AndyMark weights).** The tip mass lies between about 0.385 and 0.44 lb. Expect 2N+5P and 5N to tip; 1N+6P and 4N+1P are marginal. Each NECTAR is about 1.65 POLLEN.
11. **G408 penalty conflict.** The manual line (TU05) says YELLOW CARD only if STRATEGIC; Q23 says MAJOR + YELLOW CARD. Design to the stricter reading.
12. **Absent robots' preloads (§10.3.1).** They are placed at the center of the LOADING ZONE against the wall, which gives the present partner 4 extra POLLEN.
13. **StarterBot-class results.** A StarterBot-class robot (14343's rookie mecanum goBILDA StarterBot, probably FTCScout "99999") ranked 4th of 19 at Rookie Rumble with 58.6 average non-penalty points and 1.8 TIPS.
