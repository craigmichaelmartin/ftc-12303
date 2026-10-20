# R4 - Early-season BIOBUZZ meta and community intel (as of 2026-10-20)

Prepared for FTC team 12303 (Shumate Sith, Rockwood MI) strategy work. Game revealed 2026-09-12; this report covers the first ~5.5 weeks of the season.

**Reliability tags used throughout**

| Tag | Meaning |
|---|---|
| [OFFICIAL] | FIRST document (Competition Manual TU05, Event Field Setup Guide V1.0, Team Updates, official Q&A board) |
| [DATA] | Machine-readable match data (FTCScout REST API, which mirrors FTC Events) analyzed by me |
| [MEASURED] | A community member's frame-by-frame or bench measurement (method stated by the source) |
| [COMMUNITY] | Forum/blog/video opinion or report, not independently verified |
| [SIM] | Simulator output or simulator assumption - NOT reality |
| [INFERENCE] | My own reasoning from the above; treat as hypothesis |

Local copies of everything downloaded are under `/private/tmp/claude-501/-Users-craigmartin-projects-ftc-12303/1db60cdd-00ce-4f71-b01b-0f877dc4c285/scratchpad/downloads/` (subfolders `eventfieldguide/`, `ftcscout/`, `gameqa/`, `teamupdates/`, `monashores/`, `chiefdelphi/`, `reddit/`, `youtube/`, `gobilda-starterbot/`, `rev-starterbot/`, `andymark-starterbot/`). Analysis scripts: `.../scratchpad/scripts/analyze_ftcscout.py`, `analyze_ftcscout2.py`, `parse_qa.py`; raw analysis output `.../scratchpad/scripts/ftcscout_analysis_out.txt` and `ftcscout_analysis2_noKZ.txt`.

---

## 0. Executive summary (the 15 things that matter most)

1. **HIVE tip thresholds are published - in the Event Field Setup Guide, not the manual.** Every CELL is calibrated with ballast washers to tip at **8 POLLEN** or **3 NECTAR + 3 POLLEN**, and to NOT tip at 7 POLLEN or 3 NECTAR + 2 POLLEN even when the last piece is "tossed in" (Event Field Setup Guide V1.0 §12, §12.3, dated 2026-09-12). [OFFICIAL]
2. **So TIP 1 costs only 3 POLLEN**: each alliance's initially-up CELL already holds its 3 staged NECTAR. One robot's 4 preloads can tip its HIVE in AUTO. After that, the newly raised CELL is empty and needs ~8 POLLEN (two full 4-piece loads) or a NECTAR-assisted mix. [OFFICIAL + INFERENCE]
3. **Tips are slow and variable in time**: on 11 AUTO tips frame-stepped from the Saline (MI) stream, the HIVE sat **0.25-3.4 s (median ~2 s)** after the threshold piece landed before moving, then swung in **0.65-0.9 s (median 0.75 s)**; threshold-piece-to-spill-on-tiles is ~3 s. Overloading past the threshold shortens the wait to ~0.25-0.5 s. [MEASURED, Mona Shores, 2026-10-07]
4. **Early-season scores are low.** Across 11 US events with match data (176 matches, 2026-09-19..10-17, all scrimmage/preview "NonAdvancement" events), the **median alliance non-penalty score is 35** (mean 39.5, 90th pct 74, max 128). Mean tips per alliance per match = **0.9**; 44% of alliance-matches had zero tips. [DATA]
5. **TIPS decide matches.** At US events the alliance with more TIPS won **102 of 104 (98%)** matches where tip counts differed; win rate by alliance tips: 0 → 28%, 1 → 49%, 2 → 78%, 3 → 90%, 4+ → 100%. HIVE points are ~60% of all non-penalty points. [DATA]
6. **Ranking points:** SWARM RP earned by ~50% of US alliances; POLLINATOR 1 (≥4 tips) only **3.4%**; POLLINATOR 2 (≥7 tips) **0%** at US events. [DATA]
7. **AUTO meta = "fire preloads, tip once, leave, park."** Only 18% of US alliances tipped in AUTO; no US alliance tipped twice in AUTO; best US alliance AUTO = 36 (one TIP + both robots LEAVE + AUTO PARK). Median alliance AUTO = 8. [DATA] At Saline "a good AUTO was one TIP plus parking, 28-36 points". [MEASURED/COMMUNITY]
8. **Michigan comparator: Saline Preview Event (USMISAS, 2026-10-02/03), 28 mostly middle-school teams.** Winners Infinity Tech 13684 (Canton, East MS) + CyBugs 10644 (Saline MS) averaged ~3.6-3.8 tips/match and 100+ points; the median team's average was ~50 points and ~1.3 tips per match. [DATA/OFFICIAL FTC Events]
9. **FLOWERS are essentially unused so far**: only 4 of 924 flower end-states had any NECTAR; flower points in 0.9% of alliance-matches. Yet at the end of US matches **36-53% of FLOWERS still hold ≥3 staged POLLEN** in the scoring volume, so one own-color NECTAR dropped in is worth **2×(3 or 4 + 1) + 5 = 13-15 points** (observed: 13-point flowers in Kazakhstan). This is the biggest under-exploited endgame value seen in the data. [DATA + INFERENCE]
10. **Bounce-outs are the #1 shooting failure.** One team measured a **44% bounce-out rate** with a fixed solid hood (backspin) (FTC 14343, 2026-09-27); Saline commentary repeatedly called bounce-outs (frame analysis of the best robot's 8 visible shots found 2 misses but no in-and-out bounce). The fix trend: zero-spin or topspin shots (counter-rollers / hood wheel, underhand toss). [MEASURED/COMMUNITY]
11. **Flower pollen extraction matters for supply**: 16 of the 24 non-preload POLLEN start inside FLOWERS (retrieval opening at the bottom). A team at Rookie Rumble (WA, 10-17) was "completely starved of pollen" in matches where no robot had a flower intake. All three detailed vendor StarterBots include a flower-extraction feature. [OFFICIAL staging + COMMUNITY]
12. **Rules that are biting**: TU05 (2026-10-15) + Q&A Q3/Q23: **no MOMENTARY exception** for holding a 5th element (G407) or an opponent NECTAR (G408); repeated = STRATEGIC. Q17: **any** deflection of a spilled piece by a robot under a tipping HIVE violates G409. Q21: a robot **touching a wall at the end of AUTO loses LEAVE** (even if parked). Q11: human NECTAR entry after a TIP is allowed **during AUTO** too. [OFFICIAL]
13. **Common top-robot architecture**: flywheel shooter (single or dual wheel) with a hood, often on a turret; full-width compliant-wheel or flap intake; a short linear transfer/indexer holding ≤4; a flower pollen extractor. Most early robots are POLLEN-only; NECTAR handling is the frontier ("Nectar is a trap" for most teams). [COMMUNITY]
14. **Michigan season is compressed**: Metro Detroit League meets 10/20, 10/24, 10/30, 11/7, 11/11, 11/19 (all at WCCCD Belleville); League Tournament 12/4-5 (capacity 24; 4 teams advanced to State-SE last season); FiM State Championships NW 12/10-12 (Midland) and SE 12/11-13 (Howell). [OFFICIAL FTC Events/FiM]
15. **Team 12303** is listed on FTC Events for 2026 ("Shumate Sith", Rockwood MI, sponsors Gibraltar School District & Shumate Middle School) with **no events assigned yet** (MD league rosters show "Teams have not yet been assigned"). Its last official events were 2023-24 Metro Detroit (rank 8 at the League Tournament, Connect 3rd); 2025-26 only shows a May 2026 off-season scrimmage. [DATA/OFFICIAL]

---

## 1. Sources and dates (primary list)

| Source | Date | What it gave | Tag |
|---|---|---|---|
| Event Field Setup Guide V1.0, https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide (PDF, 30 pp, created 2026-09-12) | 2026-09-12 | HIVE calibration rules §12-§12.4, field setup §11 | OFFICIAL |
| Competition Manual TU05 (local copy) | 2026-10-15 | Rules G402-G428, scoring §10.5, Tables 10-2/10-3, Table 13-1 | OFFICIAL |
| Team Updates 00-05, https://ftc.game/tu-combined, https://ftc.game/tu-latest | 2026-09-12 .. 2026-10-15 | TU05 G407/G408 language; TU04 chime at 1:00; TU02 G426 note | OFFICIAL |
| Official Game Q&A board, https://game-qa.firstinspires.org/boards/2027/FTC/QA (fetched 2026-10-20: 38 questions, 26 answered) | answers 2026-10-07 .. 10-19 | Q2-Q38 rulings | OFFICIAL |
| FTCScout REST API, e.g. https://api.ftcscout.org/rest/v1/events/2026/USMISAS/matches ; human pages https://ftcscout.org/events/2026/USMISAS | data through 2026-10-20 | All 12 events with match data; per-alliance score breakdowns, OPRs | DATA |
| FTC Events: https://ftc-events.firstinspires.org/2026/USMISAS/rankings , /awards ; https://ftc-events.firstinspires.org/2026/region/USMI/league/MD ; https://ftc-events.firstinspires.org/2026/team/12303 | fetched 2026-10-20 | Saline rankings/awards; MD schedule; 12303 status | OFFICIAL |
| FIRST in Michigan events page https://firstinmichigan.us/FTC/events/ and preview page https://firstinmichigan.us/FTC/preview-event/ | fetched 2026-10-20 | Season structure, registration rounds | OFFICIAL (regional) |
| Mona Shores FTC (MI) public repo https://github.com/Mona-Shores-FTC-Robotics/biobuzz (issues and `doc/` on branch `claude/simulator`) | 2026-09-27 .. 10-19 | Frame-stepped Saline measurements; Q&A review; simulator studies | MEASURED / SIM |
| Chief Delphi: FTC 14343 Escape Velocity build thread https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080 | 2026-09-13 .. 10-19 | Bounce-out measurement, tip timing, Rookie Rumble recap | MEASURED / COMMUNITY |
| Chief Delphi topics 524986, 524185, 524071, 524143, 524081, 524104, 524110, 524288, 524343, 524058, 525071, 523966, 524059, 524050 (URLs in §10) | 2026-09-12 .. 10-19 | Design/strategy discussion | COMMUNITY |
| Reddit r/FTC (RSS) e.g. https://www.reddit.com/r/FTC/comments/1xaqx10/calibration_of_hive/ | 2026-09-20 .. 10-20 | Calibration complaints, tools | COMMUNITY |
| Vendor StarterBot docs (goBILDA, REV, AndyMark; Studica blocked by Cloudflare) | 2026-09-11 .. 09-25 | StarterBot mechanisms, code | OFFICIAL (vendor) |
| YouTube metadata (titles/descriptions/dates; transcripts were not retrievable) | 2026-09-12 .. 10-08 | Robot reveals, records | COMMUNITY |
| Detroit Automation Academy "BIOBUZZ Is Here" https://www.detroitautomationacademy.com/signal/edition-01/ | 2026-09-23 | Coaching summary | COMMUNITY |

---

## 2. (a) HIVE TIP thresholds

### 2.1 What FIRST publishes [OFFICIAL]

The Competition Manual (TU05) does **not** give a count; it only says each HIVE "will hold its position until enough POLLEN or NECTAR are LAUNCHED into the upwards-facing CELL" (manual §9.6, p69). The count is in the **Event Field Setup Guide V1.0, §12 "Hive Calibration" (pp. 26-28)**:

> "A consistent Hive tipping point is important to the BIOBUZZ gameplay. While teams should expect some amount of variation, Field Staff must make every effort to ensure each Hive is correctly calibrated."
> "Each Hive should be calibrated to tip when [2] combinations of scoring elements are placed in an upward facing Cell (See Section 12.3): [8] Pollen + [0] Nectar, and [3] Pollen + [3] Nectar."

§12.3 Calibration Requirements (verbatim table):

| Hive State | Test Action | Desired Result | Requirement |
|---|---|---|---|
| [3] Nectar + [1] Pollen | 2nd Pollen is "Tossed In" | No Tip | Necessary |
| [3] Nectar + [2] Pollen | 3rd Pollen is Gently Placed | Tip | Preferred |
| [3] Nectar + [2] Pollen | 3rd Pollen is "Tossed In" | Tip | Necessary |
| [0] Nectar + [6] Pollen | 7th Pollen is "Tossed In" | No Tip | Necessary |
| [0] Nectar + [7] Pollen | 8th Pollen is Gently Placed | Tip | Preferred |
| [0] Nectar + [7] Pollen | 8th Pollen is "Tossed In" | Tip | Necessary |

Other calibration facts (§12.1-12.4): tuning is by adding/removing **Ballast Washers** on a 10-32 × 2.0 in screw at the end of each CELL nearest its opening; "Pollen & Nectar should be 'gently placed' in the back of the Cell contacting the Hive Back Skin"; "[3] Pollen + [3] Nectar has less mass than [8] Pollen"; each CELL is calibrated separately and "the calibration of one Cell affects the other"; some hives are "tricky" and need the CELL attachment "tweaked". Field setup §11.1: red's audience-side CELL and blue's scoring(far)-side CELL start up, each with 3 own-color NECTAR "adjacent to the Hive Back Skin". The Fun Robotics Network posted a calibration demo by 8680 Kraken-Pinion: https://youtu.be/U5TY2FS0CRE (2026-09-12).

Official Q&A Q12 (answered 2026-10-10): "Variations in placement of the POLLEN and NECTAR in the CELL and dynamics of the HIVE in motion may result in the HIVE tipping variations. Per Section 10.5.1 the HIVE TIP will only be scored when the damper contacts the HIVE frame - and will be scored regardless of how many NECTAR and POLLEN were used to do so. LAUNCHING at a HIVE in motion may disrupt the motion... teams should be careful to allow for the HIVE TIP to complete." The same answer confirms no rule prevents adding NECTAR before the HIVE comes to rest.

### 2.2 What that means (arithmetic) [INFERENCE]

- **TIP 1 needs only 3 POLLEN** in each alliance's starting up-CELL (3 staged NECTAR + 3 POLLEN). One robot's 4 preloads (G304.G requires exactly 4 contacting each robot) are enough with one spare. Confirmed in play: at Saline Q26 the commentator called "putting three pollen up and in. They're going to get a tip" (Mona Shores review, https://github.com/Mona-Shores-FTC-Robotics/biobuzz/blob/claude/simulator/doc/saline-preview-day2.md).
- After TIP 1 the raised CELL is the formerly-down CELL; at match start it is empty, so **TIP 2 needs 8 POLLEN-equivalents** = two full 4-piece loads (G407 caps a robot at 4). With both robots shooting full loads, an alliance needs ≥2 loads per tip after the first.
- Mass: POLLEN ~0.055 lb, NECTAR ~0.091 lb (vendor/community figures carried in the shared brief; manual §9.8 gives only diameters ~2.8 in and ~3.6 in and warns the balls "are not perfectly spherical and may vary in size"), so 1 NECTAR ≈ 1.65 POLLEN by mass; 3N+3P ≈ 7.95 POLLEN-equivalent. Lever arm also matters (pieces "against the back skin" give more torque).
- A partially loaded up-CELL holds its pieces; anything left in an up-CELL at the end scores 2 points each (Table 10-2). At US events the most common end state was exactly 3 pieces left (the 3 staged NECTAR never tipped = 6 points) [DATA].

### 2.3 Community-reported combinations (UNVERIFIED) [COMMUNITY]

- FTC 14343 (CD, 2026-09-14) posted a "distribution" chart: **0N 8P, 1N 7P, 2N 6P, 3N 3P, 4N 1P, 5N 0P** - implying 1-2 NECTAR save almost nothing and 3+ NECTAR save a lot. Source of the chart not stated. https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080
- The same author told another team (CD topic 525071, 2026-10-10): "8 pollen should tip every time, as should 7p 1n, 3p 3n, and 5n 0p".
- The asker of official Q12 assumed "5 nectar or 8 pollen" (the answer did not confirm or deny a number).
- Only the 8P and 3N+3P points are official. Treat intermediate combos as plausible but untested.

### 2.4 Measured tip dynamics [MEASURED]

From Mona Shores' frame-stepping of the Saline Day 2 stream (https://www.youtube.com/watch?v=lr6iMORuxMs), docs `saline-tip-measurements.md` and issue #167 (2026-10-07), https://github.com/Mona-Shores-FTC-Robotics/biobuzz/issues/167:

| AUTO TIP | Robot | Pollen in (s after START) | HIVE moves | On stop | Wait | Swing |
|---|---|---|---|---|---|---|
| Q9 blue | Clague GearCats 7305 | 6.6 | 7.1 | 8.0 | 0.5 | 0.9 |
| Q16 blue | Infinity Tech 13684 | 11.0 | 13.4 | 14.1 | 2.4 | 0.7 |
| Q19 blue | Team KUDOS 15229 | 10.3 | 12.4 | 13.1 | 2.1 | 0.75 |
| Q20 blue | CyBugs 10644 | 4.2 | 7.6 | 8.3 | 3.4 | 0.7 |
| Q25 blue | Infinity Tech | 10.3 | 11.0 | 11.8 | 0.75 | 0.8 |
| Q26 red | Frost RoboFalcons 10136 | 11.2 | 11.5 | 12.2 | 0.25 | 0.7 |
| Q29 red | CyBugs | 3.7 | 5.6 | 6.3 | 1.9 | 0.75 |
| Final M10 red | Infinity Tech | 5.0 (more until ~8.4) | 8.8 | 9.6 | ≤3.8 | 0.8 |
| Final M10 blue | Team KRASH 15465 | 8.9 | 12.0 | 12.9 | 3.1 | 0.9 |
| Playoff M3 red | Infinity Tech | 4.1 | 7.4 | 9.6 | 3.3 | 2.2 (1.3 s creep + 0.9 s swing) |

Key findings: swing **0.65-0.9 s (median 0.75 s)**; **dwell 0.25-3.4 s (median ~2 s)**, short (0.25-0.5 s) "when a volley carried the load well past the threshold", 2-3.4 s "when the load was exactly three POLLEN"; threshold piece to spill on tiles **~3 s**; fastest AUTO TIP completion **6.3 s** (CyBugs); top robots 8-14 s; first spilled piece touches tiles ~0.28 s after the rocker hits its stop. Hypothesized mechanism: pieces landing near the front of the CELL give less torque until they roll to the back skin.

FTC 14343 bench test on their own field (CD, 2026-09-27): "Tipping with 8 pollen is SLOW (one test took 4.76 seconds from pollen contact to a scored TIP)"; a 2N+5P load (which "shouldn't tip; our hive may need some more calibration") took 2.78 s. Community member rwwin (CD 524104, 2026-09-14): "even with extra pollen/nectar the tip rate is slow enough you could waste a lot of time waiting for the pivot to finish its swing."

Spill behaviour (Mona Shores `saline-piece-physics.md`, issue #168, 2026-10-07): pieces spill toward the wall the CELL faces (red audience CELL → audience wall), first touch ~35-47 in from that wall (filmed range), spread 2-3 ft either side within 0.3 s, mostly at rest against the wall/robots within ~2 s; POLLEN rolling deceleration on event tiles ~3.9 in/s², NECTAR ~1.5 in/s² (rolls farther); human-player NECTAR entered 2.3 s after a TIP in one clear case.

### 2.5 Field-to-field variability [COMMUNITY]

- Field guide itself: "teams should expect some amount of variation" [OFFICIAL].
- Reddit r/FTC "Calibration of HIVE" (2026-10-20): "we calibrated it, but it's taking random amounts of pollen and nectar to tip ie: 6 nectars/9pollen" https://www.reddit.com/r/FTC/comments/1xaqx10/calibration_of_hive/
- Stuy teams (CD 525071, 2026-10-10): initial tests were off because "we built some stuff wrong in the field"; fixed after calibrating.
- FTC 14343: their HIVE tipped at 2N+5P before calibration (2026-09-27).
- FTC 12737 (CD 524059, Ri24H at kickoff): "if you take 4 pollen and hit the edge of the hive it will tip over using momentum" (uncalibrated kickoff field).
- Practical expectation: practice fields at team shops are often mis-built/uncalibrated; event fields should match §12.3 but shot momentum ("tossed" vs launched) and landing position change timing and occasionally the count. A launched piece hits harder than a "tossed" one, so the 7th POLLEN could occasionally tip an event HIVE [INFERENCE].
- Simulators disagree and should not be trusted for the threshold: a GitHub sim reported "12 POLLEN or 8 NECTAR" (search snippet, repo now 404); Soul080/ftc-sim uses a 0.200 kg mass threshold tuned to §12.3 (https://github.com/Soul080/ftc-sim) [SIM].

### 2.6 Does "3 staged NECTAR + one robot's 4 preloads" tip it? Yes.
3N + 3P is the official tip point; 3N + 4P is one POLLEN over, which per the Saline data also shortens the dwell. [OFFICIAL + MEASURED]

---

## 3. (b) Match scores, TIPs, RP rates, what top robots do, cycle times

### 3.1 Data set [DATA]

All 12 events with any played matches on FTCScout as of 2026-10-20 (all FTC Events type "NonAdvancement" - scrimmages/preview events; no official league meet or qualifier results yet; Metro Detroit Meet Red is in progress today):

| Code | Event | Date | Location | Teams | Matches |
|---|---|---|---|---|---|
| USUTSAS1 | Robot in 7 Days Scrimmage | 2026-09-19 | Sandy UT | 15 | 2 playoff |
| KZASN1 | AI & Digital Bridge Cup 2026 | 2026-09-24 | Astana, Kazakhstan | 35 | 44 Q + 11 P |
| USMISAS | **Saline Preview Event (FiM "Week 0")** | 2026-10-02/03 | Saline MI | 28 | 35 Q + 10 P |
| USTXWPLUN | Lubbock-A Scrimmage | 2026-10-03 | Lubbock TX | 20 | 25 Q |
| USTXCECCN | CTX Corpus Christi Training Event | 2026-10-04 | Corpus Christi TX | 10 | 13 Q |
| USAKJUN1 | Southeast Scrimmage #1 | 2026-10-10 | Juneau AK | 6 | 5 Q |
| USMNMAN | Mille Lacs League Week 0 | 2026-10-10 | Mayer MN | 18 | 9 Q |
| USNVLVS | Southern Tinker & Play Scrimmage | 2026-10-10 | Las Vegas NV | 17 | 16 Q |
| USNVRES | Northern Tinker & Play Scrimmage | 2026-10-10 | Reno NV | 9 | 8 Q |
| USOHCON | Central Ohio League Scrimmage | 2026-10-10 | Columbus OH | 16 | 20 Q |
| USCOMSS | Pikes Peak Scrimmage | 2026-10-17 | Manitou Springs CO | 4 | 9 Q |
| USWABON1 | Rookie Rumble | 2026-10-17 | Bothell WA | 23 | 24 Q |

**Data-quality caveat - Kazakhstan (KZASN1):** its "POLLEN/NECTAR left in up-facing CELL" field reaches 41-48 for single alliances (physically impossible: a CELL tips at 8 POLLEN), so its HIVE scoring appears mis-entered or non-standard. I report US events (11 events, 176 matches, 352 alliance-matches) as the baseline and KZ separately.

### 3.2 US baseline (11 events) [DATA]

| Metric (per alliance per match) | Value |
|---|---|
| Non-penalty score | median **35**, mean 39.5, p75 52, p90 74, max **128** |
| Score incl. penalties | median 37, mean 41.6, max 150 |
| AUTO points | median 8, mean 11.6, p90 31, max 36 |
| TELEOP points | median 25, mean 27.8, max 110 |
| TIPS | mean **0.9**; distribution 0:155, 1:119, 2:46, 3:20, 4:10, 5:2 |
| P(≥1 / ≥2 / ≥3 / ≥4 TIPS) | 56% / 22% / 9.1% / 3.4% |
| TIPS per match (both alliances) | mean 1.82, median 1, max 7 |
| AUTO TIP | 18.2% of alliance-matches; never more than 1 |
| Pieces left in up-CELL at end | mode 3 (untipped staged NECTAR), max 8 |
| GARDEN points | mean 1.2 (51% of alliances scored ≥1) |
| FLOWER points | essentially 0 (1 alliance-match out of 352: 7 pts) |
| SWARM RP | **49.7%** |
| POLLINATOR 1 RP (≥4 TIPS) | **3.4%** |
| POLLINATOR 2 RP (≥7 TIPS) | **0%** |
| Per-robot LEAVE / AUTO PARK / END PARK | 58% / 45% / 67% |
| Any foul committed | 8.0% of alliance-matches; majors 0.10, minors 0.04 per alliance-match |
| Share of non-penalty points | AUTO 29% (leave 3.5, park 4.5, hive 3.6 avg), TELEOP HIVE 50%, end PARK 17%, GARDEN 3%, FLOWER 0.1%; all HIVE points (auto+teleop) 60% |
| Win rate by alliance TIPS | 0 → 28%, 1 → 49%, 2 → 78%, 3 → 90%, 4+ → 100% |
| Alliance with more TIPS won | 102 of 104 (98%) |
| Winners' TIPS / losers' TIPS | mean 1.40 / 0.51 (median 1 / 0) |
| Playoffs (n=20 alliance-matches) | median 56.5, mean 65.1, tips mean 1.8 |

Per-event highlights:

| Event | Median NP | Max NP | Mean TIPS | P(≥1 TIP) | SWARM | POLL1 |
|---|---|---|---|---|---|---|
| USMISAS (Saline MI) | 49.5 | 128 | 1.5 | 86% | 73% | 8.9% |
| USWABON1 (Rookie Rumble WA) | 51 | 119 | 1.4 | 85% | 71% | 6.2% |
| USNVLVS (Las Vegas) | 50 | 108 | 1.5 | 78% | 31% | 3.1% |
| USNVRES (Reno) | 38 | 90 | 1.3 | 75% | 63% | 0 |
| USOHCON (Columbus OH) | 28.5 | 86 | 0.6 | 40% | 73% | 0 |
| USMNMAN (MN) | 27.5 | 72 | 0.6 | 50% | 22% | 0 |
| USCOMSS (CO) | 24.5 | 42 | 0.5 | 50% | 22% | 0 |
| USTXWPLUN (Lubbock) | 18 | 47 | 0.1 | 10% | 10% | 0 |
| USTXCECCN (Corpus Christi) | 15.5 | 32 | 0 | 0% | 12% | 0 |
| KZASN1 (Kazakhstan, caveat) | 68 | 248 | 1.9 | 87% | 72% | 12.7% (+1 POLL2) |

Team OPR (non-penalty) across 132 US team-events: median **15.4**, p75 23.7, p90 38.8, max 80.1. Saline only: median 21.5, p75 31.2, max 79.6. TIPS-OPR: median 0.2, p90 1.27, max 3.05. AUTO-OPR: median 4.6, p90 14.4, max 33.2. [DATA]

Top US OPRs:

| Team | Event | NP OPR | TIPS OPR | AUTO OPR | Avg NP | Rank |
|---|---|---|---|---|---|---|
| 16750 Technototes (Bellevue WA, HS) | USWABON1 | 80.1 | 2.95 | 33.2 | 96.2 | 1 |
| 13684 Infinity Tech (Canton MI, East MS) | USMISAS | 79.6 | 3.05 | 18.3 | 105.6 | 1 |
| 10644 CyBugs (Saline MS, MI) | USMISAS | 64.0 | 2.47 | 19.5 | 101.6 | 2 |
| 16158 VC Silver Circuits (NV) | USNVRES | 62.9 | 2.20 | 20.2 | 77.7 | 1 |
| 10084 Team Calamari (OH) | USOHCON | 52.3 | 1.94 | 23.1 | 65.8 | 1 |
| 23849 Droid Force (WA) | USWABON1 | 50.6 | 1.54 | 21.9 | 69.0 | 6 |
| 18858 ARC (NV) | USNVLVS | 45.8 | 1.92 | 21.4 | 73.4 | 2 |
| 10538 Team KILTS (Howell MI, MS) | USMISAS | 44.6 | 1.82 | 17.3 | 64.4 | 4 |

KZ top: 19163 MLP (OPR 125.0, alliance scores up to 248 with 7 TIPS in a playoff) - treat with the data caveat. The only POLLINATOR 2 RP so far was in KZ.

Unofficial "record": "FTC BIOBUZZ | 124 NP Points | World Record | 19049 High Five & 24909 StarLight" (YouTube, published 2026-09-14, AUTO 16 + TELEOP 108), https://www.youtube.com/watch?v=495akYrSr2U - a practice match two days after kickoff, not an FTC Events result (19049 has no 2026 events on FTCScout). FTC 14343 says High Five "exploited the lack of Ri30H flower intake mechanisms to grab a cool 52 points off of the flowers" [COMMUNITY, unverified].

### 3.3 Saline Preview Event deep dive (Michigan, 2026-10-02/03) [OFFICIAL/DATA/MEASURED]

- 28 teams, many middle schools (Saline MS, East MS Canton, Frost MS Livonia, Highlander Way MS Howell, Clague MS / Slauson MS Ann Arbor...). FTC Events rankings columns: Ranking Points, Avg Match, **Avg Tips**, Auto Points (Table 13-1 sort: RS, avg non-foul match points, avg TIPS, avg AUTO points). https://ftc-events.firstinspires.org/2026/USMISAS/rankings

| Rank | Team | RS | Avg match | Avg TIPS | Auto pts (alliance avg) | W-L |
|---|---|---|---|---|---|---|
| 1 | 13684 Infinity Tech | 4.80 | 105.6 | 3.80 | 28.0 | 5-0 |
| 2 | 10644 CyBugs | 4.40 | 101.6 | 3.60 | 32.0 | 5-0 |
| 3 | 15465 Team KRASH | 3.20 | 55.8 | 1.60 | 15.2 | 4-1 |
| 4 | 10538 Team KILTS | 3.00 | 64.4 | 2.20 | 22.4 | 4-1 |
| 8 | 10136 Frost RoboFalcons | 2.60 | 63.6 | 1.80 | 28.4 | 3-2 |
| 14 | 7305 Clague GearCats | 2.20 | 58.6 | 1.80 | 20.8 | 3-2 |
| median of 28 (my calc from the table) | | | ~50.3 | ~1.3 | ~14.4 | |
| 28 | 30612 Quantum Vikings | 0.60 | 32.6 | 0.80 | 11.0 | 0-4-1 |

- Awards (https://ftc-events.firstinspires.org/2026/USMISAS/awards): Winning Alliance 13684 + 10644 (won final 113-76 vs 15465 + 15229; playoff scores 128, 107, 113); Inspire 1st 13684, 2nd 10136, 3rd 23392; Design 1st 10644.
- AUTO: "a good AUTO was one TIP plus parking, 28-36 points for the alliance. No alliance made two TIPs in AUTO in 45 matches"; a typical alliance scored 8-16 in AUTO (leave/park or nothing). [MEASURED/COMMUNITY: Mona Shores `saline-preview-day2.md`]
- Infinity Tech (robot "InfiniBee") shooting: first POLLEN 2.8-3.0 s after START, next two **0.14-0.25 s apart**; 6 of 8 clearly visible shots scored (one rim, one over); in Q16/Q25 it shot "from directly under the HIVE"; in the Final it drove to the audience wall and shot from there. [MEASURED]
- Fastest robot seen: 17039 THOR cruised ~57 in/s (peak 65-78 in/s); most others 20-25 in/s. [MEASURED]
- Commentary themes (Mona Shores caption review): frequent bounce-outs ("two of them went in and bounced right back out"); shooting at the lower (wrong) CELL; Frost RoboFalcons "shooting the back side of that hive"; pieces leaving the field; two robots "tied up with some of their wiring... traveling together for the next 35 seconds"; **defence "everywhere in TELEOP"**; robots and human players waiting for TIPs; TELEOP scoring mostly "low hanging fruit" from piles in mid-field and under the HIVE.
- Saline stream links: Day 1 https://youtube.com/live/PxsqeKP_bp8 ; Day 2 https://www.youtube.com/watch?v=lr6iMORuxMs ; Playoff M3 clip (128-41) https://www.youtube.com/watch?v=OuAYb94T-v0 . FUN Robotics "What Early BIOBUZZ Robots Are Teaching Us" (2026-10-08) covers Saline and Kazakhstan: https://www.youtube.com/watch?v=kgC98T7L27M

### 3.4 Rookie Rumble (WA, 2026-10-17) recap [COMMUNITY + DATA]
FTC 14343 (CD 524080, 2026-10-19): "The median alliance at rookie rumble scored 51 points and every alliance that scored over 100 points had 16750 Technototes... It seems like **5 tips is a pretty good benchmark for consistent early season wins**... I expect 5 tips to be achievable in auto for strong teams once we reach States... score a tip every 30 seconds." Their robot ran POLLEN-only (shooter zip-tied to a static mode) and placed 7th/19; "We had several matches where no robots had flower intakes, causing us to be completely starved of pollen." [FTCScout confirms median 51, max 119.]

### 3.5 What early top/featured robots look like [COMMUNITY unless noted]

| Team / robot | Date | Architecture notes | Source |
|---|---|---|---|
| 13684 Infinity Tech "InfiniBee" (MI) | 2026-10-03 | Very fast 3-shot volleys (0.14-0.25 s apart); TIP + park AUTO; shot from under HIVE and from audience wall | Mona Shores measurements (MEASURED) |
| 19705 WXYZ Ri3D | 2026-09-17/19 | "custom molded intake and a two flywheel shooter"; "100 pts in 125s TeleOp" solo; VEX Push Back-style flower intake (per 14343) | https://www.youtube.com/watch?v=Cw-tVeKDDIo ; https://www.youtube.com/watch?v=DOWwamIKOog |
| 21062 Quant | 2026-10-03 | "Sorter-Spindexer, capable of holding 4 game elements while preventing additional elements from entering"; "flexible shooter can score both game elements" | https://www.youtube.com/watch?v=XU2UTrHp_eE |
| 14343 Escape Velocity (WA) | 09-13..10-19 | Turret + hood shooter for both elements; flywheel on sprung MGN rails with counter-rollers; 4 shooter iterations; passive dual-element hood abandoned for an active switch; mecanum; "amp" flower mech + Push Back-style flower intake; scaled-up "six-seven" intake wheel | CD 524080 |
| CyberCoyotes 11940/22091 | 10-04..10-19 | Turret (~270°, believed 48T ring / 16T pinion, mag-switch zero), ~4 in flywheel + ~2 in powered **hood wheel** (hood/flywheel RPM ratio ~2 ≈ zero spin), servo shot gate, distance→RPM shot table from Limelight, "fallback TeleOp: turret locked, 2 to 3 preset shots, no vision" | https://github.com/CyberCoyotes/2026-2027-BIOBUZZ/issues |
| Mona Shores 19429/20245 (MI) | 09-27..10-19 | Turret, two flywheels, feeder, front FLOWER extractor, fixed forward Limelight HIVE tracker; ground intake chosen to catch spills (G409-legal spots only) | https://github.com/Mona-Shores-FTC-Robotics/biobuzz |
| Peyton Yeung "BIOBUZZ SimpleBot" | 2026-10-05 | 4 motors + 2 servos on one Control Hub; Robits skid steer; compliant-wheel intake + belt conveyor; cam-actuated servo floor for compression; servo kicker; **underhand launcher (3 in stealth wheels, NeveRest 1:1) - "topspin seems to help keep the pollen in the goal"**; no flower mech so "it feels like a hunt to get additional pollen"; 4th shot sometimes off (flywheel droop) | https://www.youtube.com/watch?v=cC5v2OGcwso |
| Stuy 37245/37246 | 10-19 | Zero-spin two-roller shooter "basically 100% accurate at the position where the bot is on the wall" | CD 525071 |
| 18858 ARC (NV) | 10-10 | Repo named "...catapultV1"; OPR 45.8 at Las Vegas scrimmage | https://github.com/Adelson-Robotics/18858-2026-biobuzz-ftc-catapultV1 ; DATA |
| 9707 BEAST, 24070 TERBO FORGE | 09-16..10-01 | Rapid-fire flywheel shooter tests | https://www.youtube.com/shorts/oIj4boZY8zk ; https://www.youtube.com/shorts/pDWPFROSc4I |
| ITKAN | n/a | "dual-turret robot" cited as inspiring but "most teams are not ITKAN" | CD 524986 |

Community consensus on archetypes:
- Flywheels dominate; catapults viable for simple robots but slower to reload/settle (CD 524143). The AndyMark StarterBot uses a one-motor rotary catapult.
- "A single stream fixed shooter can shoot 4 game elements in under a second... your limiting factor is gonna be element acquisition, not throughput" (necessaryevil, CD 524185, 2026-09-16).
- "topsin is desirable" (CD 524185); multiple teams converged on zero-spin or topspin to reduce bounce-outs.
- "Nectar is a trap" for teams that are not top-tier; "10/10 pollen beats 5/10 pollen and 5/10 nectar any day of the week" (CD 524986, 2026-10-06). Counter-view: 14343 thinks NECTAR capability matters later (3+ NECTAR sharply cut tip cost).
- Handling two ball sizes is "one of the unsung challenges this year" (CD 524288); approaches: foam/spring floor compliance, two lanes, dual heads, active hood switch, passive sorting by size.
- Localization: use goBILDA Pinpoint dead-wheel odometry for pose; use AprilTags only for aiming and HIVE state because the tags move with the CELLs (CD 524104, 524081).

### 3.6 Cycle times and tempo [MEASURED/COMMUNITY/SIM]

- One TIP's mechanics: shots ~0.5-1.5 s + dwell ~0.25-3.4 s + swing ~0.75 s ⇒ ~3 s from threshold piece to spill [MEASURED].
- Benchmarks: "score a tip every 30 seconds" ≈ 5 tips per match for strong early teams (14343) [COMMUNITY]. Saline winners averaged 3.6-3.8 TIPS per alliance match [DATA].
- Each post-TIP-1 TIP needs ~2 full robot loads (8 POLLEN), so a robot contributing one load every ~15 s gives the alliance ~a TIP per 30 s if the partner matches it [INFERENCE].
- Simulator defaults (BIOBUZZ Strategy Lab by FTC 6603, https://biobuzz-strategy-lab.com): drive 3.1 ft/s, intake 0.85 s/element, launch 0.65 s/element, align 1.0 s/trip, flower placement 2.5 s/element, POLLEN accuracy 68%, NECTAR 58% [SIM - assumptions, not measurements].

---

## 4. (c) Popular AUTO routines and their points

### 4.1 What happens in real matches [DATA]
- US: AUTO LEAVE+PARK ≥16 (both robots LEAVE and AUTO PARK) in **19.6%** of alliance-matches; AUTO TIP 18.2%; both 6.8%; zero AUTO points 17.6%; ≤8 AUTO points 55%.
- Most common SWARM paths (autoLeave, autoPark, endPark points): (6,10,10) ×46, (3,5,10) ×37, (6,5,10) ×25, (6,10,5) ×20 - i.e., SWARM (≥16 LEAVE+PARK points, Table 10-3) can be locked in during AUTO by both robots leaving and parking (6+10=16), or completed with end PARKs.
- Max US alliance AUTO = 36 = 20 (TIP) + 6 (2 LEAVE) + 10 (2 AUTO PARK). KZ recorded one 76-point AUTO (3 TIPS) and one 2-TIP AUTO (caveat on KZ data).

### 4.2 Routine catalogue

| Routine | Points (alliance) | Who runs it | Notes / sources |
|---|---|---|---|
| A. Do nothing / drive off wall | 0-3 | many rookies | 17.6% of US alliances scored 0 in AUTO |
| B. LEAVE + PARK in LOADING ZONE | 8 per robot | most common | Must NOT touch the wall at the end of AUTO or LEAVE is lost (Q&A Q21, 2026-10-10). A partner pushing a robot off the wall counts (Q28, 2026-10-19). |
| C. StarterBot: fire 4 preloads from the wall in line with the up-CELL, then drive off the wall | 20 (TIP) + 3 | goBILDA, AndyMark, REV example code | goBILDA: launcher on for 10 s then reverse 120 mm; AndyMark: selectable start delay "for coordinating the tipping of the goal with an alliance partner", 5 catapult cycles, drive 18 in; REV: "simple start-up sequence to launch pre-loaded game pieces", either side. No PARK in vendor code. |
| D. Fire preloads → TIP → LEAVE → PARK | up to 28 per robot (36 with partner leave+park) | Saline top teams (Infinity Tech, CyBugs, Frost, KRASH, KUDOS) | Fastest TIP done 6.3 s after START (CyBugs). |
| E. Two-robot coordination: robot 1 tips the start CELL; robot 2 waits at the far end, fires its 4 preloads into the newly raised CELL after the TIP | 20 + partial (4 POLLEN sit in the CELL; 2 pts each if still there at match end) | planned by several teams | Needs 4 more pieces for TIP 2 (flower/garden/spill). Mona Shores "Qualifier Auto": wait for partner's TIP 1, then fire. |
| F. Multi-TIP AUTO: tip with preloads, collect from FLOWER/GARDEN/spill, fire at other CELL, repeat, park | 2-3 TIPS claimed in sims | aspirational | 14343's plan: preloads → TIP, grab pollen from flower or garden, nudge partner into park, shoot other cell, reload at other flower, second TIP, park. Mona Shores sims claim 2-3 TIPs (51-76 pts) but they note real Saline AUTOs were much slower (sim TIP 1 ≈4.6 s vs real 7.4 s; no dwell modeled) [SIM]. No US alliance has done 2 AUTO TIPS yet. |
| G. Human NECTAR in AUTO | +1 NECTAR available per TIP | legal | Q11 (2026-10-10): entry after each TIP of your HIVE is allowed in AUTO; must be dropped/rolled (released before touching tile) into the LOADING ZONE (Q6, Q7); G401 no signalling the robot. |

### 4.3 AUTO pitfalls seen/flagged
- G402: stay on your own side (columns A-C red, D-F blue) in AUTO; Q15/Q24 on what counts as interference still **pending**.
- G409: a robot that drives/waits in the spill zone during a TIP and gets hit violates G409 (Q17) - plan AUTO wait spots outside the landing area (spill lands toward the wall the CELL faces).
- Waiting on a fixed timer for a TIP is fragile given the 0.25-3.4 s dwell; teams use vision (tags vanish when the CELL starts moving) or a retry ("fire until TIP, retry once") (Mona Shores #188, 2026-10-18).

---

## 5. (d) Endgame and match-strategy trends

### 5.1 FLOWERS
- Rules recap [OFFICIAL]: scoring volume = between top and middle ring; an element counts if "at least partially" in it (§10.5.2). Owner = alliance with top-most own-color NECTAR; owner gets 2 pts per POLLEN and NECTAR in the volume "regardless of which ALLIANCE placed" them; Bottom NECTAR Bonus 5 pts to the alliance with the bottom-most NECTAR. NECTAR into a FLOWER before 60 s left = MAJOR FOUL per NECTAR (G410). Only enter via the top; only remove POLLEN from the bottom (G418). FLOWER Ownership Unlocked chime at 1:00 (TU04). Top opening 4.0 in dia at 21.5 in with 1.25 in backstop; NECTAR is 3.6 in.
- Data [DATA]: 4 of 924 end-of-match flower states had NECTAR; 3 were 13-point flowers in KZ (3 POLLEN + 1 NECTAR = 4×2 + 5), 1 was a 7-point flower in Ohio (1 NECTAR alone = 2 + 5).
- End-of-match flower contents at US events (count of pieces recorded in the scoring volume; FTCScout fields `redAllianceFlowerState`, `blueAllianceFlowerState`, `scoringFlowerState`, `audienceFlowerState`, which I read as the flowers on the red-alliance wall, blue-alliance wall, far/scoring-table wall and audience wall): ≥3 POLLEN in 42% / 53% / 44% / 36% of matches respectively; empty in 39-55%. [DATA] My reading is that the 4 staged POLLEN usually register as 3-4 pieces in the volume because the stack rises above the middle ring (manual Fig 9-12 shows the middle ring just above the 3.55 in retrieval opening) [INFERENCE].
- Implication [INFERENCE]: in roughly 4 of 10 flowers, one own-color NECTAR dropped in with ≥60 s left earns 13-15 points, the same as ~2/3 of a TIP, and does not consume a TIP's worth of shooting. Opponents can steal ownership by capping above it; the bottom bonus stays with whoever is lowest.
- Community views:
  - 14343 (2026-09-14): flowers/gardens mostly not worth it; "The most worthwhile acquisition is the bottom nectar, which is a full 7 points", worthwhile to steal points if an opponent fills a flower.
  - 14343 (2026-09-27) "flower starvation": park 7 POLLEN + 1 NECTAR in each flower to choke a POLLEN-only opponent ("40 - 7*4 = 12 pollen available"); a replier noted it burns your own NECTAR and may draw G411 (hoarding).
  - 12737 (Ri24H): "if one team flowers, then we have to flower."
  - Mona Shores flower-backboard study (2026-10-05): a capped flower is worth 7-13 vs a TIP's 20; flowers count toward neither POLLINATOR RP; a slow high lob may work as well as a backboard (sim) [SIM].
  - CD 524071 (2026-09-13): expects archetypes "hive bots" vs "flower specialists"; few robots will do both.
  - Brogan M. Pratt videos ("Every Way to Score in BIOBUZZ", 2026-09-14, https://www.youtube.com/watch?v=FzDAVe-6azk ; "BIOBUZZ Strategy: How to Win Auto, Teleop, and Flowers", 2026-09-20, https://www.youtube.com/watch?v=ITIsy1B5IYc - chapter "Flowers, Worth it?"). Transcripts not retrievable.
  - UP Robotics FTC Helper (AI chatbot, community): "max 23 points with just the flower" - internally inconsistent; low reliability. https://ftc.uprobotics.tech/questions/11

### 5.2 NECTAR flood at 60 s
G426.B: with ≤60 s left all remaining NECTAR may be entered (via LOADING ZONE, G427). TU02 note: NECTAR need not be entered immediately after a TIP. Human players at Saline held NECTAR until a TIP (Mona Shores). Practical technique vetted against Q7: underhand roll released in the air inside the LOADING ZONE, along the wall, into a waiting intake (Mona Shores `human-nectar.md`). [OFFICIAL + COMMUNITY]

### 5.3 GARDEN
1 point per element at least partly inside at the end; either alliance may add/remove; not protected (G411 note "A GARDEN is not a protected zone"). Average 1.2 pts/alliance at US events [DATA]. 4 staged POLLEN per garden start there and count for whoever's garden it is if untouched [INFERENCE from staging]. Widely viewed as the lowest-value task ("The gardens are worse than the flowers", 14343) [COMMUNITY].

### 5.4 PARK and SWARM
END PARK = 5 per robot in own LOADING ZONE; 67% of US robots parked at the end [DATA]. SWARM achieved by ~50% of US alliances; locking it in AUTO (2 LEAVE + 2 AUTO PARK = 16) removes endgame pressure. Train whistle at 0:20 is the cue. [OFFICIAL + DATA]

### 5.5 Defense
- Saline: defense "everywhere in TELEOP" (Mona Shores commentary review).
- Official Q&A: shot blocking with an extended part (within R105, 18×24×29 in) is not disallowed (Q2, Q8, Q20, answered 10-10..10-13); G419 (damage/functional impairment), G420 (tip/entangle), G421 (3-count pins) still apply. Q37/Q38 (deflecting shots out of the field; body-blocking access to elements) are **pending**.
- CD 524071: defense-only robots are a weak choice in 2-robot FTC alliances; build a basic scorer and play defense situationally. CD 524185: tank/skid drives resist pushing better than mecanum (opinion).
- Ghosttt (CD 523966) brainstormed a "plate lowered onto the field" anti-push device [COMMUNITY].

---

## 6. (e) Common failure modes and penalties

| Failure mode | Evidence | Mitigation ideas seen |
|---|---|---|
| Bounce-outs from the CELL | 14343 measured 24/43 kept = **44% bounce-out** with a fixed solid hood (backspin), 2026-09-27; Saline commentary repeatedly called bounce-outs (frame analysis of 8 visible Infinity Tech shots found 1 rim hit and 1 over-shot but no in-and-out bounce) | Zero-spin (two-roller / hood wheel at ~2× flywheel RPM), topspin (underhand toss), counter-rollers; more pieces already in the CELL seemed to reduce bounce-outs slightly (weak data) |
| Shooter RPM wrong / droop | Stuy: goBILDA StarterBot "RPM speed is too high, and it consistently misses the shots - the first shot makes it in because the starting speed is lower" (CD 525071, 2026-10-19); SimpleBot: "Occasionally, the 4th pollen is a little off the mark" (flywheel droop) | Tune velocity per distance; gate feeding on measured velocity ("wait until the flywheel had spun up", 14343); more flywheel inertia or second motor |
| Shooting the wrong (lower) CELL | Saline playoff commentary | Vision HIVE-state check (tags 9.4 in higher when up; tags vanish as a CELL starts to swing); driver cue |
| Waiting for the TIP | dwell 0.25-3.4 s; robots "going to wait for the tip" | Overload past threshold; plan next action during dwell; don't fire at a moving HIVE (Q12 warns it can disrupt the TIP) |
| Catching/deflecting the spill (G409) | Q17: any deflection by a robot under a tipping HIVE is a violation; VERBAL WARNING, YELLOW CARD per MATCH if STRATEGIC | Stay out of the landing area (toward the wall the CELL faces) for ~3 s after the threshold piece; guards so pieces can't rest on top of the robot |
| Over-possession (G407) / opponent NECTAR (G408) | TU05 (2026-10-15): repeated control of 5+ is likely STRATEGIC "even if the ROBOT has a 'keep 4 and discard the rest' sorting mechanism"; Q3/Q4/Q23: no MOMENTARY exception; Q34 (wide intakes trapping a 5th) **pending** | Stop the intake at 4 (sensor), narrow/gated mouth, color rejection BEFORE control |
| Jams / two ball sizes | CD 524288 "unsung challenge"; 14343 removed a transfer stage for "extreme compression" | Compliant wheels, adjustable compression, test with both sizes |
| Starved of POLLEN | 14343 at Rookie Rumble without flower intakes | Flower retrieval mechanism; collect gardens; spill pickup |
| Robot entanglement / pieces out of field | Saline commentary | Cable management; G405 is only for deliberate ejection |
| Elastic fatigue (catapults) | AndyMark Robits manual: rubber bands "should be treated as a consumable" | Springs; spare bands |
| HIVE calibration differences | see §2.5 | Practice with a correctly calibrated HIVE; design margin (shoot all 4) |
| Limelight confusion | CD 524110 (2026-09-27): "limelight only has one map slot and all 8 pipelines share it. You have to make 2 maps for each team color"; "no way to filter the upside-down tags in software... rotate limelight upward enough so that only 4 tags can be seen"; robot "may still see wrong down tags when passing through the hive"; Limelight stream (:5801) issue after Control Hub update | Use tags only for aim + HIVE state; Pinpoint for pose; one cluster in view; exposure/gain tuned for tags in the CELL's shadow (CyberCoyotes #10) |

Penalty statistics [DATA]: US events - 8.0% of alliance-matches had any foul committed; 0.10 MAJOR and 0.04 MINOR fouls per alliance-match; mean 2.2 penalty points committed per alliance-match. Saline 2.2% of alliance-matches had a major (Q29 "red 40" in AUTO was one TIP plus a 20-point penalty). KZ was far stricter/messier: 30% with a major, 10.9 penalty points per alliance-match. FTCScout does not break fouls down by rule.

Penalty values to remember [OFFICIAL, TU05 manual text checked visually for G407/G408]: G402 MAJOR per match (+YC if STRATEGIC); G405 MAJOR per element (deliberate ejection); G407 VERBAL WARNING, MAJOR + YELLOW CARD per match if STRATEGIC; G408 VERBAL WARNING, YELLOW CARD per match if STRATEGIC (note: Q23 described both G407/G408 as "MAJOR FOUL and YELLOW CARD" - discrepancy with the TU05 text for G408); G409 VERBAL WARNING, YELLOW CARD per match if STRATEGIC; G410 MAJOR per NECTAR; G411 MAJOR + YELLOW CARD per match; G417 VERBAL WARNING, MAJOR + YELLOW CARD per match if STRATEGIC (ramming the frame, launching at external CELL faces, impeding an opponent TIP); G426 MINOR per NECTAR entered early. MINOR = 5, MAJOR = 20 to the opponent.

---

## 7. (f) StarterBot designs in detail (baseline a middle-school team can borrow from)

FIRST's team-resources page lists all four (versions: goBILDA, REV, AndyMark "26-27.2, updated May 4, 2026"; Studica "26-27.3, updated Sep 14, 2026"): https://ftc-resources.firstinspires.org/ftc/team . Overview video "BIOBUZZ presented by RTX StarterBots" (2026-09-12) https://www.youtube.com/watch?v=HQwDTDON5I4

### 7.1 Comparison table

| | goBILDA StarterBot | REV DUO Starter Bot | AndyMark Robits StarterBot | Studica |
|---|---|---|---|---|
| Docs | https://www.gobilda.com/ftc-starter-bot-resource-guide-2026-2027-season/ ; assembly PDF (37 pp, 2026-09-11) https://www.gobilda.com/content/user_manuals/3200-2627-0003_assembly-instructions.min.pdf ; code zip (2026-09-25) | https://www.revrobotics.com/duo/ftc-starter-bot/ ; https://docs.revrobotics.com/ftc-kickoff-concepts ; build guide PDF (126 pp) | https://andymark.com/pages/2026-2027-robits-starterbot-base ; user manual PDF; Java/Blocks code | https://www.studica.com/ftc-starter-bot-resource-guide-2026-2027 (Cloudflare-blocked to my tools) |
| Kit / cost | 2026-27 FTC Starter Kit 3200-4008-2627 $899.99 (or Upgrade Pack $249.99) + REV Control Hub | FTC Starter Kit V3.1 + DUO Control Bundle + 2 mm polycarb grid sheet | Robits Core Kit; dedicated kit am-5832 $525 ($569 w/ battery, charger, tools) + control system | "training tool", 6 mm hex, Maverick motors (per Studica blog) |
| Drive | Drop-center 6WD skid steer, 2× 19.2:1 Yellow Jacket (312 rpm), 96 mm wheels + omnis (mecanum variant uses Strafer kit) | Skid steer, 2× 20:1 UltraPlanetary HD Hex, chain, 90 mm traction/grip + omnis | Skid steer, NeveRest 19.2:1 (13.7 option), belts, 3 in stealth + omni wheels; mecanum CAD variant | n/a |
| Intake | Full-width roller: 16 "Intake Roller" + 48 mm Gecko wheels, chain-driven by 50.9:1 Yellow Jacket (117 rpm nominal); two front-corner 72 mm Gecko wheels on speed servos "pull elements out of corners" and flowers | Core Hex motor with 8 × 40A intake flaps; polycarb intake floor/lid | Servo-powered rubber-band rollers (needs servo programmer for continuous mode); "rubber bands should be treated as a consumable" | n/a |
| Storage / feed | Ramp; "windmill" torque servo (continuous) feeds the flywheel; holds up to 4 | Feeder roller (four 1 in grip wheels) on a Smart Robot Servo | Passive gravity hopper (angled floor) to the launcher | n/a |
| Launcher | **Single 96 mm Hogback wheel on a 19.2:1 Yellow Jacket converted to 1:1 (~6000 rpm free)** with a large angled grid-plate hood; velocity PIDF (40,0,0,12.5); target 1250 ticks/s (≈2,680 rpm), feed only when >1200 | **Two 90 mm grip wheels flanked by four unmeshed 90-tooth plastic gears on a 252 mm hex shaft, direct-driven by an HD Hex motor with a 1:1 UltraPlanetary output** (build guide pp. 104-106), polycarb launcher floor/hood ("chute") | "Coaxial flinger": one-motor wind-and-fire rotary catapult (presumably the kit's 50.9:1 NeveRest; AUTO code winds at 0.65 power for 1 s per shot), elastic bands or springs, adjustable hood end; launch channel 4 in wide | n/a |
| Flower | Side Gecko servos pull POLLEN out of the FLOWER into the intake | "servo-powered poker" on the side combs POLLEN out of FLOWERS | Roller version (CR servo + trimmed compliant wheel) or finger version (positional servo, more reach, shock-sensitive) | (FUN showed a Studica-parts flower de-score mech https://www.youtube.com/shorts/E18TU8m_BEo) |
| NECTAR | Not addressed (POLLEN only) | Not addressed | Not addressed | n/a |
| Shooting spot | Starts against the wall and shoots | Shoots from start position, either side | "up against the field wall with the launcher centered on the goal" (diagram: alliance-colored squares at the audience and far walls in line with each CELL; launcher biased to one side) | n/a |
| AUTO code | Launch 10 s then reverse 120 mm (LEAVE) | Launch preloads; customizable side | Selectable start delay (dpad), 5 × launch (1 s wind + 0.5 s), drive 18 in | n/a |
| TeleOp | Arcade; triggers intake; right bumper spins launcher and auto-feeds at speed | Subsystem control + "auto launch sequence for the Hive" | Split arcade; bumpers intake/flower; right trigger fire | n/a |
| Known issues | Stuy: default RPM "too high", first shot OK then misses | - | Elastic fatigue; ball not fully seated after reset; 4 in channel lets ball wander | - |

goBILDA also published a match video (2026-09-25): https://www.youtube.com/watch?v=xQvEtIjN2Rw and overview https://youtu.be/UXOh-F85t0o . REV reveal: https://www.youtube.com/watch?v=Azqc3gvZ0LM . AndyMark "How do I play BIOBUZZ?": https://youtu.be/rmjtBcABNGk .

### 7.2 Other low-resource baselines
- Robonauts (FRC 118) FTC Everybot 2026-27: "Will list" = omni drivetrain, 3D-printed parts, defense, PARK, LEAVE, shoot POLLEN into HIVE, remove POLLEN from FLOWER, intake POLLEN from floor; **not yet released** as of 2026-10-18 (CD 524058).
- Peyton Yeung SimpleBot (above): 4 motors / 2 servos, one hub, underhand topspin launcher.
- Relevance to 12303 [INFERENCE]: 12303's photographed robot (single rubber-tired flywheel driven by a 6000 rpm Yellow Jacket, C-shaped hood) is architecturally the goBILDA StarterBot launcher on a turret. Its known weaknesses (backspin bounce-outs, RPM tuning, 4th-shot droop) and the goBILDA code's velocity-gated feeding pattern apply directly.

---

## 8. (g) FIRST in Michigan 2026-27 structure and team 12303

### 8.1 Michigan schedule [OFFICIAL - FTC Events / FiM, fetched 2026-10-20]

Metro Detroit League (code MD), all at Wayne County Community College (District), 9555 Haggerty Rd, Belleville MI (https://ftc-events.firstinspires.org/2026/region/USMI/league/MD):

| Event | Code | Date |
|---|---|---|
| Meet Red | USMIMDM1 | Tue 2026-10-20 (in progress; no teams listed on FTC Events) |
| Meet Yellow | USMIMDM2 | Sat 2026-10-24 |
| Meet Pink | USMIMDM3 | Fri 2026-10-30 |
| Meet Green | USMIMDM4 | Sat 2026-11-07 |
| Meet Purple | USMIMDM5 | Wed 2026-11-11 |
| Meet Orange | USMIMDM6 | Thu 2026-11-19 |
| **League Tournament** | USMIMDLT | Fri-Sat 2026-12-04/05 (capacity 24 teams) |

- League page: "Teams have not yet been assigned to this league." Last season's MD League Tournament (2025-12-05/06, 18 teams) stated "4 teams advance to Michigan State Championship-SE" (https://ftc-events.firstinspires.org/2025/USMIMDLT).
- Other FiM leagues: Avondale SKILLS & PLAY, Capital (CP), OCCRA (OC), South Central (SC), West Michigan (WM); their tournaments are 12/4-6.
- FiM qualifiers (non-league path): Freeland 10/30-31 (FiM page marks it CANCELLED); 11/6-7 Belding, Coloma, Hartland (Howell), Bullock Creek (Midland), Royal Oak, Southgate; 11/13-14 Chelsea, Kentwood, Lake Orion, Rochester (Lakeview/Battle Creek 11/14-15); 11/20-21 Fruitport, Houghton, Madison Hts, Midland, Novi, Bedford (Temperance); 12/4-5 Ann Arbor, Detroit, Flint, Pellston, Macomb CC (Warren); 12/5-6 Grosse Pte. Woods.
- **State championships**: "Michigan - FiM Championship NW" 12/10-12 at Northwood University, Midland; "Michigan - FiM Championship SE" 12/11-13 at Parker Middle School, Howell (FiM page shows "Dec 11/12" NW and "Dec 12/13" SE).
- FiM registration: Round 3 form "Opens Wed., Oct 21, 7am"; off-season form opens Tue March 10 (https://firstinmichigan.us/FTC/events/).
- RP thresholds for Regional Championships (e.g., the FiM Championships) are "TBA" (Table 10-3); FIRST says they will be announced in Team Updates [OFFICIAL]. Q14: no new RP types; point values fixed.
- Week 0: Saline Preview Event (10/2-3) - already played.
- MI teams compete early: a CD poster notes "MI competes very early on" (CD 524288).

### 8.2 Team 12303 status [DATA/OFFICIAL]
- FTC Events 2026: "Team 12303 - Shumate Sith (2026)", Rockwood MI, "2026 Sponsors: Gibraltar School District & Shumate Middle School", Region Michigan - FiM, "Team 12303 has not yet participated at any official events. (Scrimmages not shown)" (https://ftc-events.firstinspires.org/2026/team/12303).
- FTCScout: rookie year 2016; website https://sites.google.com/view/shumate-robotics/home (requires Google login); 2026 events: none.
- History (FTCScout): 2019-20 Finalist (19MIQT8); 2020-21 Innovate 1st + 4th-ranked (USMIBLQT12); 2021-22 Inspire 2nd, Think 1st, Finalist (USMIDHQ), Finalist (USMIGIOS), played at FiM State (USMICMP2); 2022-23 five events incl. SE league tournament; 2023-24 Metro Detroit Meet 4 (rank 8) and **MD League Tournament rank 8, Connect 3rd**; 2024-25 none; 2025-26 only "Troy MAYhem" off-season scrimmage (2026-05-15/16, rank 20, OPR 50.9 in DECODE scoring, Innovate 2nd).
- Nearby comparators: Michigan middle-school teams 13684, 10644, 10645, 10136, 10538, 7305 are already scoring 1.8-3.8 TIPS/match.

---

## 9. Community tools worth knowing

| Tool | What | Link | Tag |
|---|---|---|---|
| Hive Vision v2.0.0 (2026-10-12) | Limelight 3A SSD-MobileNetV2 detector for yellow/red/blue balls (25 detections/frame, 97.3% precision claimed) + OpenCV Control Hub track + "Ball-Chase API" | https://github.com/hivevisionftc/Hive-Vision ; Reddit https://www.reddit.com/r/FTC/comments/1x3r4af/hive_vision_update_v200/ | COMMUNITY |
| Infinity Tech 1:18 scale printable field with articulating hives (2026-10-09) | Strategy board for alliance talks | https://makerworld.com/en/models/3341226-ftc-biobuzz-field-1-18-scale-2026-2027 | COMMUNITY |
| Pedro Pathing visualizer | Has a BIOBUZZ field map | CD 524081 | COMMUNITY |
| xRC Simulator BIOBUZZ (2026-09-20) | Driving sim | https://xrcsimulator.org/downloads/ | SIM |
| Soul080 ftc-sim (pushed 2026-10-12) | Browser sim; rules table; uses §12.3 calibration; note its tile-row mapping (rows counted from far wall, red garden on far wall) contradicts manual Fig 9-2/9-5, so don't use it for geometry | https://github.com/Soul080/ftc-sim ; https://soul080.github.io/ftc-sim/ | SIM |
| BIOBUZZ Strategy Lab (FTC 6603) | Monte Carlo strategy sim; no published conclusions | https://biobuzz-strategy-lab.com | SIM |
| CycleTrack (FTC 16255) | Driver practice / cycle-time tracking | https://cycletrack16255.com/ | COMMUNITY |
| gatool cheat sheet | Penalty lookup | CD 524552, https://gatool.org | COMMUNITY |
| Mona Shores repo | Saline measurements, Q&A review, HIVE-tracker lessons for Limelight (tag row ~9.4 in higher when UP; tags vanish as a CELL starts to tip; 2+ tags needed for accurate range) | https://github.com/Mona-Shores-FTC-Robotics/biobuzz | MEASURED/SIM |
| UP Robotics FTC Helper | AI-generated Q&A chatbot ("AI answers can be wrong") | https://ftc.uprobotics.tech/ | low reliability |
| Detroit Automation Academy newsletter | Coaching summary, drills (2026-09-23) | https://www.detroitautomationacademy.com/signal/edition-01/ | COMMUNITY |

No BIOBUZZ-specific gm0 (gm0.org) update or Studica strategy content could be retrieved (Studica pages behind a Cloudflare JS challenge; search snippet only: Studica's strategy article lists HIVE TIP 20, AUTO/TELEOP PARK 5, Bottom NECTAR Bonus 5 and notes "teams don't need a robot that does everything").

---

## 10. Additional source URLs (Chief Delphi / Reddit / YouTube)

- CD "FTC BIOBUZZ Questions/Help" (2026-10-05/06): https://www.chiefdelphi.com/t/ftc-biobuzz-questions-help/524986
- CD "The perfect biobuzz robot? (for most teams)" (2026-09-16): https://www.chiefdelphi.com/t/the-perfect-biobuzz-robot-for-most-teams/524185
- CD "Would a defense robot be a good idea for biobuzz?" (2026-09-13): https://www.chiefdelphi.com/t/would-a-defense-robot-be-a-good-idea-for-biobuzz/524071
- CD "Catapults" (2026-09-14): https://www.chiefdelphi.com/t/catapults/524143
- CD "FTC auto-aiming" (2026-09-13, coach of 8th graders asking): https://www.chiefdelphi.com/t/ftc-auto-aiming/524081
- CD "Idea about FTC Biobuzz localization?" (2026-09-14..26): https://www.chiefdelphi.com/t/idea-about-ftc-biobuzz-localization/524104
- CD "FTC BioBuzz Limelight Settings/FMAP" (2026-09-14..30): https://www.chiefdelphi.com/t/ftc-biobuzz-limelight-settings-fmap/524110
- CD "How much weight does it take to compress a nectar" (2026-09-19): https://www.chiefdelphi.com/t/how-much-weight-does-it-take-to-compress-a-nectar-in-biobuzz/524288
- CD "What type of launcher are teams building" (2026-09-21): https://www.chiefdelphi.com/t/what-type-of-launcher-are-teams-building-for-the-biobuzz-season/524343
- CD Robonauts Everybot (2026-09-12..10-18): https://www.chiefdelphi.com/t/the-2026-2027-robonauts-ftc-everybot-low-resource-build/524058
- CD Stuy build blog (2026-10-09..19): https://www.chiefdelphi.com/t/stuy-ftc-37244-37245-37246-37247-build-blog-26-27/525071
- CD 17134 Ghosttt build thread: https://www.chiefdelphi.com/t/ftc-17134-ghosttt-build-thread-open-alliance-2026-27/523966
- CD 12737 build thread: https://www.chiefdelphi.com/t/12737-2026-2027-season-build-thread-biobuzz/524059
- CD FUN BIOBUZZ content thread (Ri30H, 2026-09-12..19): https://www.chiefdelphi.com/t/fun-ftc-biobuzz-content-thread/524050
- Reddit "Is scoring with flowers worth it?" (2026-10-03): https://www.reddit.com/r/FTC/comments/1wwutc8/is_scoring_with_flowers_worth_it/
- Reddit "3D Printable FTC Biobuzz Game Field" (2026-10-09): https://www.reddit.com/r/FTC/comments/1x1q9bg/3d_printable_ftc_biobuzz_game_field/
- FUN "Breaking Down BIOBUZZ Strategies" (2026-09-12): https://www.youtube.com/watch?v=yk3qig3L3Zg
- FUN Ri30H Day 1 recap (2026-09-12): https://www.youtube.com/watch?v=xXOKL8JuE88 ; Day 2 GT (dual shooter concept): https://www.youtube.com/watch?v=bR91YppIwNk
- FUN prototype shorts (2026-09-12/13): pivoting intake https://www.youtube.com/watch?v=vDd_NoH-QLo ; flower lift/pass-through https://www.youtube.com/watch?v=KAwayfhN5dQ ; flower dropper https://www.youtube.com/watch?v=5dlcGfkOLkk ; transfer & kicker https://www.youtube.com/watch?v=sF3aWmby_ls ; stinger descore https://www.youtube.com/watch?v=mkRDGOajRuM
- Official kickoff broadcast (2026-09-12): https://www.youtube.com/watch?v=gO98TkgY0kI ; game animation: https://www.youtube.com/watch?v=sUH3z5a5S9I

---

## 11. Open questions / gaps

1. Exact intermediate tip combos (1-2 NECTAR + POLLEN, 4-5 NECTAR) on a correctly calibrated event HIVE, and whether a hard-launched 7th POLLEN can tip it - only bench testing on a §12.3-calibrated HIVE can answer.
2. How much each additional piece beyond the threshold shortens the dwell (Saline suggests overload → ~0.25-0.5 s) - testable on the team's field.
3. Real bounce-out rate of 12303's single-flywheel + fixed-hood launcher, and whether adding a hood roller/compliant hood or topspin cuts it.
4. Pending official Q&A: Q15/Q24 (AUTO interference), Q29 (active 5th-piece expulsion), Q30 (NECTAR against the outside of FLOWER pipes), Q34 (wide intakes trapping a 5th piece), Q35 (waiting just outside the spill zone in AUTO), Q36 (re-entry of NECTAR that left the field), Q37 (deflected shots out of bounds), Q38 (body-blocking access to elements).
5. G408 penalty discrepancy: TU05 text says "VERBAL WARNING. YELLOW CARD per MATCH, if STRATEGIC" while Q23 says G407 and G408 both carry "MAJOR FOUL and YELLOW CARD" if STRATEGIC.
6. Whether 12303 is registered/placed in the Metro Detroit league (FTC Events shows no league assignment for any team yet; FiM Round 3 registration opens 10/21 7am).
7. Regional Championship (FiM State) RP thresholds - TBA.
8. Kazakhstan data reliability (HIVE leftover counts up to 48) - can't use its TIP/score numbers as a benchmark.
9. Robot designs of the Saline winners (turret? flywheel count?) are not documented in text; FUN's 2026-10-08 rundown video and team Instagram (https://www.instagram.com/reel/DeHZXA9x6aS/) would show them.
10. Studica StarterBot details (site blocked to automated fetch).

---

## 12. Strategy implications for 12303 (my inferences from the above - to be validated by the strategy work)

1. **AUTO first target = "4 preloads → TIP → LEAVE → PARK (off the wall)"**. TIP 1 needs only 3 POLLEN; firing all 4 overloads the CELL and shortens the dwell. With a partner that leaves and parks, that is 36 alliance AUTO points and the SWARM RP locked in AUTO - a combination only ~7% of US alliance-matches have achieved so far (36 is also the US AUTO maximum to date). Avoid touching the wall at the end of AUTO (Q21) and avoid standing in the spill landing zone for ~3 s after the last shot (G409/Q17).
2. **TIPS win matches**: at early events, 2 alliance TIPS ≈ 78% win rate and 3 ≈ 90%. A robot that can independently add one full 4-POLLEN load every ~15-20 s in TELEOP (half a TIP) plus the AUTO TIP would be top-quartile in Michigan right now (Saline team-OPR median 21.5, p75 31.2; top 79.6).
3. **Shooter quality > shooter speed**: the community's measured problem is bounce-outs (44% with a fixed hood/backspin), not rate of fire. 12303's single flywheel + C-hood is the backspin archetype; test a hood roller / counter-roller (zero spin at ~2× flywheel RPM per CyberCoyotes' reasoning), compliant hood, or topspin before chasing more RPM. Gate feeding on measured flywheel velocity (goBILDA code pattern; 14343 lesson) and expect 4th-shot droop.
4. **Supply is the real bottleneck** ("limiting factor is element acquisition"): 16 of 24 non-preload POLLEN start inside FLOWERS. A simple flower-bottom extractor (goBILDA side Gecko wheels / REV "poker" / AndyMark roller-or-finger) materially increases cycles, and the spill after each TIP lands toward the wall the CELL faces - a predictable pickup area once it has touched the floor.
5. **Turret value**: the up-CELL alternates ends after every TIP, and Mona Shores' shot-map simulation found scoring positions only in wedges in front of each CELL's opening, none beside the HIVE [SIM]. On the real field Infinity Tech also shot from under the HIVE, and a commentator described Frost RoboFalcons "shooting the back side" (unverified). The turret saves chassis rotation and lets the robot aim while driving to the next wedge; it does not remove the need to relocate after each TIP.
6. **Limelight**: use AprilTags to (a) confirm which CELL is up (tag row ~9.4 in higher when up; tags disappear as a CELL starts to tip) and (b) aim/range at the up-CELL; use odometry (e.g., goBILDA Pinpoint) for pose. One field map per Limelight, swap by alliance color; aim the camera so only one cluster is in view. Hive Vision offers a ready Limelight 3A POLLEN/NECTAR detector for AUTO pickups.
7. **Possession discipline is a design requirement**: intake must stop at 4 (sensor or geometry); never take in opponent NECTAR (TU05, Q3/Q4/Q23).
8. **Endgame**: (a) both robots PARK (5 each; only 67% of robots do now); (b) human player enters remaining NECTAR at 1:00; (c) if (and only if) the robot can place an own-color NECTAR into a 4.0 in opening at 21.5 in, a FLOWER that still holds its staged POLLEN is worth 13-15 points for one NECTAR, and ~40% of flowers still do at match end. Otherwise skip flowers (consistent with "Nectar is a trap" advice for most teams).
9. **Calendar**: Metro Detroit meets run 10/20-11/19 with the League Tournament 12/4-5 and State-SE 12/11-13 - about 6.5 weeks from today to the LT. Confirm league placement/registration with FiM (Round 3 registration opens 10/21 7am).
