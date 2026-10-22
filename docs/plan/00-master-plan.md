# FTC 12303 · BIOBUZZ 2026-27 · Master plan

Written 2026-10-20 from the team's strategy brief. **The strategy decisions are final; the details get updated as we test.** This page is the summary. Files 01–09 hold the details. Tags such as (M p.91) and (QA21) are explained in the [source key](#source-key); **est.** means our own estimate.

---

## The plan on one page

**Win with TIPS.**
- When the two alliances' tip counts differed, the one with more tips won 98% of early-season US matches (102 of 104). 2 tips won 78% of the time, 3 tips 90% (FTCScout).
- A tip is worth 20 points. So is one MAJOR foul, given the other way (M p.91–92).

**Tip math** (EFSG §12.3):
- Each alliance's starting up CELL already holds its 3 NECTAR, so **~3 POLLEN tip it**.
- Every later CELL starts empty: **~8 POLLEN = two full robot loads** (a robot may hold at most 4, G407).

**The up CELL faces outward, toward its own end wall, tilted 30°.** You can only score from the half of the field it faces, so **our shooting zone flips after every tip** (CAD; M p.71). The tipped CELL spills toward the zone we were just shooting from, about 3 s after the threshold ball (allow up to 6 s; MonaShores).

**Robot: we KEEP the current design and tune it.**
- Fix the inspection blockers.
- Add possession sensors.
- Tune the flywheel and hood.
- Give the turret more range (5-turn servo) and a calibrated zero.
- Power the servos at 6 V.

Anything bigger is **OPTIONAL**.

| Phase | What we do | Points |
|---|---|---|
| **AUTO** | **A1 Tip & Park** from S1: fire all 4 preloads, tip the start CELL, hold until the spill has landed, park off the wall in our LOADING ZONE slot. Stretch, for a tuned robot only: A2 (garden reload) and A3 (alliance double tip) | **28**; **36 + SWARM RP** with a partner that LEAVEs and PARKs |
| **TELEOP** | **Two-zone loop**: shoot from the zone the up CELL faces · two loads = one tip · after the threshold ball stop; the operator presses Y on the human player's "TIP!"; wait until the spill has landed (usually ~3 s, up to ~6 s), sweep it, cross, fire | One 4-ball load every 15–20 s per robot |
| **ENDGAME** | Keep tipping; final volley at ~0:12 (Zone B, the half next to our LZ) or ~0:15 (Zone A); human enters NECTAR at 1:00 on the field timer; both robots park | 10 (+ SWARM) |
| **Phase 3** (only if NECTAR tests pass) | NECTAR to speed up tips (6 balls instead of 8); FLOWERS (~13 pts each) | |

**Goal by the League Tournament (12/4–5): 1 AUTO tip + 2–3 TELEOP tips contributed per match.**

---

## Files

| File | What's in it |
|---|---|
| [01-game-and-scoring.md](01-game-and-scoring.md) | Points, RP math, tip thresholds, why tips win, field coordinates, which way CELLS face, spill behaviour |
| [02-autonomous.md](02-autonomous.md) | AUTO ladder A0–A3 with exact poses and timelines, partner scripts, blue rotation, failure handling, ≥ 8/10 promotion rule |
| [03-teleop.md](03-teleop.md) | Two-zone loop, shooting spots, ammo sources, call-outs, control map, defence, possession rules |
| [04-endgame.md](04-endgame.md) | 1:00 and 0:25 scripts (the coach's clock calls), parking, human-player script, phase-3 NECTAR/FLOWER math |
| [05-robot-tuning.md](05-robot-tuning.md) | Inspection blockers and MUST / TUNE / OPTIONAL refinements with part numbers and prices |
| [06-software.md](06-software.md) | Software architecture and competition rules for code. Pointer to `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md` |
| [07-testing-and-practice.md](07-testing-and-practice.md) | Experiments with data sheets; driver drills with targets; weekly time budget |
| [08-match-day-and-scouting.md](08-match-day-and-scouting.md) | Event checklists, partner script, scouting sheet, alliance selection, penalty traps |
| [09-rules-cheatsheet.md](09-rules-cheatsheet.md) | Rules by match phase, Q&A rulings, pending questions, questions for the Head Referee |

---

## Michigan calendar (confirm with FiM)

| Date | Event | Notes |
|---|---|---|
| **Wed 10/21, 7:00 am** | **FiM Round 3 event registration opens** | FiM events page. **Confirm with FiM** |
| Tue 10/20 | Metro Detroit League Meet Red | Today |
| Sat 10/24 | Meet Yellow | |
| Fri 10/30 | Meet Pink | |
| Sat 11/7 | Meet Green | |
| Wed 11/11 | Meet Purple | Fits week 4 |
| Thu 11/19 | Meet Orange | Fits week 5 |
| **Fri–Sat 12/4–5** | **Metro Detroit League Tournament** | Capacity 24. Last season 4 teams advanced to State SE (FTC Events 2025/USMIMDLT) |
| **12/11–13** | **Michigan State Championship SE**, Parker Middle School, Howell | FTC Events lists 12/11–13; FiM lists the match days as Sat–Sun 12/12–13. Thresholds for State RPs are "TBA" (Table 10-3) |

- All Metro Detroit meets and the League Tournament are at Wayne County Community College District, Belleville (FTC Events MD league page).
- On 10/20, 12303 is not yet listed in a league, and the league page says "Teams have not yet been assigned" (FTC Events).
- **League Tournament ranking = our top 10 League Meet matches + our League Tournament matches. Missing meet matches count as zero** (M p.167). Meets play 5–6 matches each, so we need **at least 2 meets** (3 lets us drop bad matches).
- The coach expects the first competition 6+ weeks out. If that means skipping all meets, **we start the League Tournament with up to 10 zero matches**. Ask FiM which meets 12303 can attend; 11/11 and 11/19 fit our timeline.
- Teams advance only from their first three entry-level events in their home region (M §4).
- Thanksgiving is Thu 11/26 (week 6): plan robot hours around it.

---

## Six-week timeline

Owners left blank on purpose: fill them in at the next team meeting. Robot time is about 4–8 h per week (≈ 30–45 h total).

### Week 1 · 10/20–10/26: blockers, sensors, flywheel, first tests
- [ ] Register and confirm league placement with FiM (Round 3 opens 10/21, 7 am). Owner: ____
- [ ] Inspection blockers: Limelight **confirmed 3A** (coach, 10/21); main power switch, signs (a grounding strap is recommended, not required) ([05 §1](05-robot-tuning.md#1-inspection-blockers-fix-these-in-week-1)). Owner: ____
- [ ] Possession sensors (mouth, slot 4, feed beam) + software auto-stop at 4 ([05 §2](05-robot-tuning.md#2-must-rules-and-inspection)). Owner: ____
- [ ] Flywheel velocity control + "ready" gate ([06 §5](06-software.md#5-flywheel-control-and-the-distance--rpm-table)). Owner: ____
- [ ] Set-up checks and HIVE calibration; Exp 1 tip threshold; Exp 2a hood angle; start Exp 2b shot map ([07](07-testing-and-practice.md)). Owner: ____
- [ ] X/Y odometry pods are already on the robot (coach, 10/21): run **Check: Odometry** (push the robot 48 in forward, then 48 in left): it says whether a Pinpoint is found, names the pod from its counts per inch (goBILDA 4-bar ≈ 505, goBILDA swing-arm ≈ 337, SWYFT 38 mm ≈ 872) and prints the RobotConfig section 3 lines for pod type and directions; starter code on the robot; **Check: Sensors** passes; laptop tests pass ([06 §9](06-software.md#9-tests-tuning-and-logging)). Owner: ____
- [ ] Order parts ([05 §6](05-robot-tuning.md#6-shopping-list)); some are out of stock. Owner: ____
- [ ] Answer the robot questions ([05 §7](05-robot-tuning.md#7-questions-only-the-robot-can-answer-measure-in-week-1)): the "measure on robot" rows in the [07 set-up checks](07-testing-and-practice.md#set-up-checks-week-1-before-any-experiment) (ring and pinion teeth, flywheel drive ratio, hood contact surface, intake layout, 60 mm flywheel fit). Owner: ____

### Week 2 · 10/27–11/2: turret, Limelight, A0/A1, Pedro
- [ ] Turret: 5-turn servo (optional Through Bore Encoder on an idler gear, feedback only), plus hard stops and Tune: Turret Calibrator zero/ratio calibration ([05 §3.5](05-robot-tuning.md#35-turret-range-and-a-known-zero)). Owner: ____
- [ ] Limelight on the turret (35–40° up), one pipeline set over USB (ID filter 30–45, 82.55 mm, Full 3D), vision bias working. Owner: ____
- [ ] Pedro tuning at home (remove the 0.5 speed cap once tuned), then **AutoTune compiled out again** (`PEDRO_AUTOTUNE = false`, re-deploy; [06 §8](06-software.md#8-competition-rules-for-software-checklist)). Owner: ____
- [ ] **A0 10/10 and A1 ≥ 8/10** ([02](02-autonomous.md), Exp 7). Owner: ____
- [ ] Exp 2b/2c shot map and bounce-outs; Exp 3 flywheel recovery with and without the 60 mm steel flywheel. Owner: ____
- [ ] Drills DR8 (human player) and DR9 (transition). Owner: ____

### Week 3 · 11/3–11/9: TELEOP automation and driving
- [ ] Auto-aim, one-button volley, auto-stop at 4, rumble/LED cues, HIVE override ([06](06-software.md)). Owner: ____
- [ ] Drills DR1–DR5; **load cycle ≤ 20 s** ([07](07-testing-and-practice.md#driver-drills)). Owner: ____
- [ ] Bounce-out fixes: compliant hood, consistent squeeze ([05 §3.3](05-robot-tuning.md#33-hood-angle-and-bounce-outs)). Owner: ____
- [ ] Servos on 6 V power ([05 §3.6](05-robot-tuning.md#36-intake-power-keep-the-servo-give-it-6-v)). Owner: ____

### Week 4 · 11/10–11/16: stretch AUTOs and NECTAR decision
- [ ] A2, A3 role 1, A3 role 2 routines (12303 Auto): only with a tuned follower; promote only at ≥ 8/10; A3 also needs a rehearsal with the partner. Owner: ____
- [ ] Exp 5 FLOWER extraction, Exp 6 NECTAR go/no-go, Exp 8 intake speed. Owner: ____
- [ ] Decide on OPTIONAL changes (DC-motor intake, extractor) from the data. Owner: ____
- [ ] Drills DR6 (endgame) and DR7 (defence). Owner: ____
- [ ] Meet Purple 11/11, if registered. Owner: ____

### Week 5 · 11/17–11/23: match simulations and reliability
- [ ] **10+ full match simulations**, timed and scored (DR10). Owner: ____
- [ ] Scouting sheet ready; FTCScout data pulled; partner script practised ([08](08-match-day-and-scouting.md)). Owner: ____
- [ ] Reliability fixes; spare-parts kit; 3–4 batteries in rotation. Owner: ____
- [ ] Portfolio (optional: cover + 15 pages, < 15 MB, content since 2026-01-01; A201) and interview prep (A203). Owner: ____
- [ ] Meet Orange 11/19, if registered. Owner: ____

### Week 6 · 11/24–11/30: freeze and practise
- [ ] **Freeze** code and mechanisms: bug fixes only, each re-tested. Owner: ____
- [ ] Driving practice and full matches. Owner: ____
- [ ] Checklists printed ([08](08-match-day-and-scouting.md)); competition build verified; Head Referee questions ready ([09](09-rules-cheatsheet.md#ask-the-head-referee)). Owner: ____

### 12/1–12/3 → events
- [ ] Self-inspection with the Inspection Checklist; pack the pit and queue bags. Owner: ____
- [ ] **12/4–5 League Tournament.** 12/11–13 State SE if we advance.

---

## Success metrics

| Metric | Target | By | Measured in |
|---|---|---|---|
| A1 AUTO in practice | ≥ 8/10 runs | End of W2 | Exp 7 |
| AUTO in matches | Tip + LEAVE + PARK in ≥ 80% of matches | League Tournament | Match log |
| SWARM RP | ≥ 80% of matches (US average so far 50%, Saline 73%: FTCScout) | League Tournament | Match log |
| Tips contributed by 12303 | **1 AUTO + 2–3 TELEOP per match** | League Tournament | Match log / scouting |
| Load cycle | ≤ 20 s (stretch 15 s) | W3–W4 | DR1 |
| Shot accuracy | ≥ 8/10 clean from 3 spots per zone | W3 | DR2, Exp 2 |
| Bounce-outs | ≤ 5% | W3 | Exp 2c |
| Flywheel ready from idle | < 1.0 s | W2 | Exp 3 |
| Fouls | 0 MAJOR; 0 G407/G408 warnings | From W3 | Match log |
| End PARK | Every match unless broken | League Tournament | Match log |
| Reliability | 0 matches lost to wiring, battery or connection | League Tournament | Match log |
| Inspection | Pass on the first try | First event | Inspection Checklist |

---

## Top risks

| # | Risk | Why it matters | What we do | Owner |
|---|---|---|---|---|
| 1 | Too few League Meet matches | Missing matches count as zero in the League Tournament ranking (M p.167) | Confirm FiM placement now; play ≥ 2 meets if at all possible | |
| 2 | Inspection blockers (Limelight model, power switch, signs) | Robot can't play (G303) | Week-1 fixes ([05 §1](05-robot-tuning.md#1-inspection-blockers-fix-these-in-week-1)) | |
| 3 | The indexer isn't finished | A1 needs the robot to hold and feed 4 balls | Build it first (W1–W2) | |
| 4 | Inconsistent shots, bounce-outs (44% in one team's test, CD-14343) | Tips need balls to stay in | Speed-gated feeding, 60 mm flywheel, compliant hood, Exp 2–3 | |
| 5 | G407/G408 fouls from the full-width roller (no momentary exception; QA34 pending) | MAJOR fouls and cards | Sensors + auto-stop + DR4 | |
| 6 | Turret has only ~50–75° of travel and no feedback | Aim offset if the pinion skips a tooth after a hit | 5-turn servo (+ optional encoder feedback); re-check the zero after hits; chassis-aim fallback (`TURRET_PINNED`) | |
| 7 | Slow servo intake | Too few loads per match | 6 V power, Exp 8, OPTIONAL DC-motor intake | |
| 8 | Our HIVE differs from event HIVEs (variation; dwell 0.25–3.4 s) | Wrong ball counts and timing | Calibrate to EFSG §12.3; fire full volleys; the coach keeps the count | |
| 9 | Wi-Fi streaming at the venue (Dashboard, Panels, Pedro AutoTune) | Rule violation (R704.D, QA26) | `PEDRO_AUTOTUNE = false` and re-deploy before events; every 12303 OpMode shows an INIT warning line ([06 §8](06-software.md#8-competition-rules-for-software-checklist)) | |
| 10 | Parts out of stock (Brushland sensor, 312 rpm motor) | Delays | Order alternatives now ([05 §6](05-robot-tuning.md#6-shopping-list)) | |
| 11 | Only 4–8 h of robot time per week | The plan slips | Follow the time budget ([07](07-testing-and-practice.md#suggested-use-of-robot-time-48-h-per-week-est)); freeze in W6 | |
| 12 | Plan and code drift apart (poses, buttons) | Confusion at events | The code README is the final word; update these docs when the code changes; the laptop route test checks every AUTO leg | |

---

## Words we use

| Word | Meaning |
|---|---|
| HIVE | The see-saw with a CELL on each end, at the field centre. Each alliance has one |
| CELL / up CELL | The box at each end of a HIVE. The **up CELL** is the one tilted up, the one we can score in |
| TIP | The HIVE swings to its other side (the damper touches the frame): 20 points |
| Threshold ball | The ball that makes a CELL tip |
| Spill / landing box | The balls a tip dumps out / where they first hit the floor |
| Start CELL / second CELL | Our CELL that is up at the start (red: audience CELL, tags 34–37; blue: far CELL, 42–45) / our other CELL, up after TIP 1 (red 30–33, blue 38–41) |
| Our start wall | The end wall behind S1 (red: audience wall; blue: far wall) |
| Zone A / Zone B | Where we shoot while our start CELL / our second CELL is up. Zone B is the half next to our LZ |
| POLLEN / NECTAR | Small yellow ball (2.8 in) / large alliance-coloured ball (3.6 in). We may never control the opponent's NECTAR |
| FLOWER / GARDEN | Tubes on the walls (NECTAR scoring in the last minute) / taped strips on the walls (1 point per ball) |
| LOADING ZONE (LZ) | Our 23 × 11 in taped box on our alliance wall: where we PARK and where NECTAR comes in |
| Corner / inner slot, solo | The two-robot park spots in our LZ (the corner slot is the end nearer the field corner; the first robot to arrive takes it) / the one-robot pose in the middle |
| LEAVE / PARK | Off every wall at the end of AUTO (3) / at least partly in our LZ (5, at the end of AUTO and of the match) |
| RP, SWARM, POLLINATOR | Ranking points. SWARM = LEAVE + PARK ≥ 16. POLLINATOR = ≥ 4 / ≥ 7 tips |
| CONTROL | Holding or herding a ball (G407/G408). We may control at most 4 |
| Load / volley | One robot's 4 balls / firing them one after another |
| Pose | Position and heading on the field: (x, y, heading) |
| Odometry, Pinpoint | Tracking position with small free-spinning wheels; Pinpoint is goBILDA's computer for it |
| Pedro Pathing | The library that drives AUTO paths |
| Limelight, AprilTag | Our camera; the printed square codes on the CELLS it recognises |
| Turret / hood / flywheel / indexer | Rotating shooter base / curved guide that sets the launch angle / spinning wheel that throws the ball / internal ball conveyor |
| PIDF / LUT | Speed controller / lookup table of measured settings |
| MINOR / MAJOR / STRATEGIC | 5-point foul / 20-point foul / deliberate or reckless (makes penalties bigger) |

---

## Source key

| Tag | Source |
|---|---|
| **M p.N**, rule numbers (G…, R…, T…, E…, A…) | Competition Manual TU05 (2026-10-15): https://ftc-resources.firstinspires.org/ftc/game/manual |
| **QA n** | Official Q&A, question n: https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/n |
| **TU0n** | Team Updates: https://ftc-resources.firstinspires.org/ftc/game/tu-combined |
| **EFSG** | Event Field Setup Guide V1.0: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide |
| **FAC / FMG** | Field Acceptance Checklist / Field Mitigation Guide: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check · https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide |
| **IC / IQR** | Inspection Checklist / Inspection Quick Reference: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check · https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-reference |
| **CAD** | Official field STEP CAD; all coordinates were extracted and cross-checked by our research team: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step |
| **SDK** | FtcRobotController v12.0 README: https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md |
| **AM** | AndyMark BIOBUZZ scoring elements: https://andymark.com/products/biobuzz-scoring-elements |
| **FTCScout** | US match data, 11 events, as of 2026-10-20: https://ftcscout.org |
| **FTC Events** | https://ftc-events.firstinspires.org/2026/region/USMI/league/MD · https://ftc-events.firstinspires.org/2026/team/12303 · https://ftc-events.firstinspires.org/2025/USMIMDLT |
| **FiM** | FIRST in Michigan events page: https://firstinmichigan.us/FTC/events/ |
| **MonaShores** | Community frame-by-frame measurements of Saline matches: https://github.com/Mona-Shores-FTC-Robotics/biobuzz |
| **CD-14343** | Community build thread (FTC 14343): https://www.chiefdelphi.com/t/ftc-14343-escape-velocity-biobuzz-build-thread/524080 |
| **"our model" / "our research"** | Our research team's working notes and models (2026-10-20). The facts they rest on are cited with the primary sources above |
| **est.** | Estimate from our models (ball flight, flywheel, geometry). Confirm it with the experiments in [07](07-testing-and-practice.md) |
