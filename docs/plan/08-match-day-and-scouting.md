# 08 · Match day and scouting

Checklists for event day, the partner talk, what to scout, how to pick partners, and the fouls that catch teams. Source tags: see the [source key](00-master-plan.md#source-key).

---

## Event day at a glance

| When | What | Rules |
|---|---|---|
| Arrival | Load in, set up the pit, **inspection**. A robot must pass before it plays; re-inspect after big changes | G303, I303; Inspection Checklist |
| Before matches | **Drivers meeting.** Ask the Head Referee our open questions ([09](09-rules-cheatsheet.md#ask-the-head-referee)). Team Updates published after this meeting don't apply to this event | M p.19 |
| If offered | **Field calibration time** (≥ 30 min before quals). Robots may run OpModes, extend mechanisms and be hand-placed, but **no driving and no launching**. Check Limelight exposure and tag detection here, over a **USB cable**, not Wi-Fi | T405 (M p.151); QA26 |
| All day | **No Wi-Fi streaming anywhere in the venue**, pits and practice field included. No hotspots | R704.D; QA26; E301 |
| Qualifications | 5–6 matches per team (League Meets: also 5–6). At least 5 min between back-to-back matches | M p.153, p.167; T406 |
| Alliance selection | Send 1–2 **student** representatives. A team that declines an invitation can't be picked later | T701, T702 (M p.156) |
| Playoffs | Alliances of 2, no backups, double elimination; cards apply to the whole alliance. At least 8 min between our matches | M p.156–157; T703; T406 |

Ask the FTA or Head Referee whether the HIVEs passed their calibration checks that morning (FAC 2.1–2.7). Small differences between fields are expected (FMG).

---

## Pit checklist (before every queue call)

- [ ] Fresh battery: labelled, voltage written down (fresh packs read above 13 V), strapped in its cradle.
- [ ] Main power switch reachable; XT30s seated; RS485 and hub power plugged; ferrites on.
- [ ] Limelight boots (green light after 15–20 s); lens clean; mount tight.
- [ ] Pinpoint pods touching the floor; springs OK.
- [ ] Intake, indexer and feeder gate run in the test OpMode; colour sensors read POLLEN / red / blue correctly.
- [ ] Flywheel spin test (ready in under 1 s); nothing rubbing; **bolts checked**.
- [ ] Turret homes; hard stops intact; cables in their service loop.
- [ ] Guards on (turret centre, flywheel). **Signs in the right colour** for the next match.
- [ ] Competition build: **INIT telemetry shows no AutoTune / Dashboard warning** ([06 §8](06-software.md#8-competition-rules-for-software-checklist)).
- [ ] Driver Station charged; 2 **wired** gamepads (R904, QA16); spare cable.
- [ ] Queue bag: start jig, spare battery, tools, scouting notes on the next partner and opponents.

## Queue checklist

- [ ] Partner talk ([script below](#partner-conversation-script-about-2-minutes-in-the-queue)): AUTO roles, start spots, parking slots.
- [ ] Our colour this match; signs match it.
- [ ] Human player knows the NECTAR plan; the tray has 5 NECTAR (M p.83).

## On-field checklist (set-up)

- [ ] Robot at the agreed start, placed with the jig:
  - [ ] touching the wall;
  - [ ] **not touching a FLOWER** (check S6!);
  - [ ] not in the LZ;
  - [ ] fully on our side (G304).
- [ ] **Exactly 4 POLLEN** preloads touching the robot (G304.G).
- [ ] AUTO OpMode selected, **30 s timer on** (G305). Selector: alliance / start / routine / delay → **LOCKED**.
- [ ] INIT. Telemetry shows the right pose and sees our tags. **Nothing moves** (G304.H).
- [ ] Hands off from the countdown (G401).

## During the match: who does what

| Person | Job |
|---|---|
| Driver (gamepad 1) | Drives, runs the intake, calls "Crossing far / audience" |
| Operator (gamepad 2) | Flywheel, aim and fire; HIVE override; calls "4 in", "Empty", "Full" |
| Human player (a student) | NECTAR ([04 script](04-endgame.md#human-player-script-whole-match)); calls "TIP!" |
| Drive coach | Watches the **field timer**; keeps the CELL count and calls "8, it's going"; makes the endgame calls; presses stop / INIT / start on the DS (allowed by G423). **May not touch balls** (G424) **or handle gamepads once the match starts** (G423) |

- **Transition:** INIT TELEOP (it moves nothing); press **START at the TELEOP bells** (G403).
- **End:** press ■ at 0:00 and put the controllers down (G404). Wait for the field-reset signal before collecting the robot (M p.97).

## After the match

- [ ] Wait for the signal, power off, take the robot.
- [ ] Write down: AUTO result, tips, fouls or warnings, anything that broke.
- [ ] Swap the battery; charge at ≤ 3 A average (E511).
- [ ] Check bolts, wiring, guards.
- [ ] **Question Box**, if needed:
  - up to 2 people, at least 1 a student (T301);
  - qualification questions before alliance selection (T302);
  - there is **no video review** (T401).
- [ ] Update the scouting sheet.

---

## Partner conversation script (about 2 minutes, in the queue)

1. **Hello.** "We're 12303. We shoot POLLEN from a turret, tip the start CELL in AUTO, and park."
2. **AUTO.** "What's your AUTO? Where do you start? Do you end **off the wall** and **in our LOADING ZONE**?"
   - Pick roles: A1 / A2 / A3 ([02 scripts](02-autonomous.md#coordination-scripts)).
   - Decide who takes S1. Only one robot fits there.
   - If either alliance asks, robots are placed red 1, blue 1, red 2, blue 2 (M p.85).
3. **SWARM.** "The first robot to park takes the far slot; the second takes the near slot. Both stay off every wall at the end of AUTO."
4. **TELEOP.** "Two loads = one tip. We call '4 in'. At 8 we stop and wait about 3 s for the spill, then both go to the other zone."
   - Agree lanes: who shoots head-on and who shoots from the side.
   - Agree who sweeps which spill.
5. **NECTAR.** "Can you use NECTAR? If yes, our human rolls it toward you. If not, we hold it until 1:00."
6. **Defence.** "If they have one big scorer, who screens? Nobody pins longer than 3 s."
7. **Endgame.** "Final volley at 0:12 (0:15 from the audience side). Both park; same slots."
8. **Problems.** "Anything that tends to fail on your robot? If you lose connection, tell us and we'll change the plan."

---

## Scouting sheet (one line per robot per match)

Before the event, pull each team's non-penalty OPR, TIPS OPR and AUTO OPR from FTCScout. (OPR is a statistical estimate of how many points a team adds to its alliance.)

| Section | Field | Record as |
|---|---|---|
| Match | Match #, team #, colour / station | |
| **AUTO** | Start spot | S1 / S2 / S6 / other |
| | LEAVE / AUTO PARK / touching a wall at 0:30 | Y/N each |
| | AUTO tip (and the time) | Y/N, s |
| | Preloads fired | 0–4 |
| | Could play the A3 waiter role (has a shooter, can wait) | Y/N/? |
| **TELEOP** | Zones used | A / B / both |
| | Loads fired; seconds per load | count; s |
| | Accuracy (made ÷ fired); bounce-outs seen | %; count |
| | Ammo sources | spill / garden / FLOWER bottom / floor |
| | FLOWER extractor; NECTAR use | Y/N; none / HIVE / FLOWER |
| | Tips caused (fired the threshold ball) | count |
| **Endgame** | End PARK; FLOWER NECTAR points; last-volley time | Y/N; pts; s |
| **Defence** | Played defence (did it work?); coped with defence | Y/N + note; good / ok / poor |
| **Fouls** | G407 / G408 / G409 / G417 / G421 / G402 / other; cards | rule + count |
| **Reliability** | Died / disabled / lost connection / broke | what + when |
| Notes | Drive-team communication; human-player NECTAR legal? | free text |

---

## Alliance-selection priorities

**What we want in a partner, in order:**
1. **Tips contributed per match**: loads × accuracy, plus a reliable AUTO tip. Ideally a robot that can play the A3 waiter role; at least one that LEAVEs and PARKs (for SWARM).
2. **Reliability**: no dead matches, few fouls. One MAJOR gives the opponent 20 points (M p.92).
3. **End PARK every match** (SWARM RP).
4. **Covers our gaps**: a FLOWER extractor (ammo), NECTAR/FLOWER play (phase 3), defence.
5. **Data**: our sheets first; FTCScout OPR (non-penalty, TIPS, AUTO) second.

**Rules:**
- The number of alliances depends on how many teams played qualifications: 4–10 → 2; 11–20 → 4; 21–40 → 6; 41–64 → 8 (Table 13-2, M p.156).
- Send 1–2 student representatives (T701).
- A team that declines can't be picked later (T702).
- No backup robots (T703).

**What we tell captains about 12303:** our AUTO (A1: tip + LEAVE + PARK = 28), loads per match, accuracy, which A3 roles we can play, our foul record. Fill these in from our own data before each event.

---

## Penalty traps (the ones that catch teams)

| # | Trap | Rule (penalty) | How we avoid it |
|---|---|---|---|
| 1 | A 5th ball in the intake, or one riding on top of the robot | G407 (warning → MAJOR + YELLOW if STRATEGIC); no momentary exception (QA3) | Auto-stop at 4; top guard; don't push piles |
| 2 | Opponent NECTAR in the intake | G408 (warning → YELLOW if STRATEGIC; QA23 says MAJOR + YELLOW: plan for the stricter one) | Refuse at the mouth before contact; drivers steer clear |
| 3 | Spill bouncing off our robot | G409 (warning → YELLOW); QA17 | Stay out of the landing box for ~3 s |
| 4 | Touching the HIVE, or shooting at the opponent's HIVE | G417 (warning → MAJOR + YELLOW if STRATEGIC) | Keep a minimum distance; aim at own tags only |
| 5 | NECTAR into a FLOWER before 1:00 | G410 (**MAJOR per NECTAR**, no warning) | Use the field timer, keep a 1 s margin, code lockout |
| 6 | Human enters NECTAR too early | G426 (MINOR per NECTAR) | Coach counts our tips out loud |
| 7 | NECTAR set down instead of dropped, or it touches a robot first | G427 (MINOR per NECTAR); G425 | Drop it above an uncovered part of the LZ |
| 8 | Robot twitches during the transition | G403 (warning → MAJOR + YELLOW) | TELEOP INIT moves nothing; START at the bells |
| 9 | Still powered after 0:00 | G404 | Press ■ at the buzzer |
| 10 | Disturbing the opponent's AUTO | G402 (**MAJOR per match, even by accident**) | All AUTO paths stay on our side |
| 11 | Pinning longer than 3 s | G421 (MAJOR + a MAJOR every 3 s) | Count "1-2", back off 2 ft |
| 12 | Guarding balls or a corner | G411 (MAJOR + YELLOW, **no warning**) | Score balls; don't guard them |
| 13 | Knocking balls out of the field on purpose | G405 (MAJOR per ball) | Only scoring attempts are excused |
| 14 | Set-up errors: not touching the wall, wrong preload count, touching a FLOWER, in the LZ, too big | G304 (match held, or DISABLED) | On-field checklist + jig |
| 15 | Wi-Fi streaming or a hotspot at the venue | R704.D, QA26, E301 | Competition build; phone hotspots off |
| 16 | Coach touches a ball or handles a gamepad during the match | G424, G423 | Clear roles |
| 17 | Slow to the field | G301 (warning → MAJOR → DISABLED) | Match-ready within the ~5 min between matches |

Warnings carry from qualifications into playoffs (M p.94). Repeated warnings for the same thing draw STRATEGIC scrutiny (M p.92). League Meet cards and warnings clear after each meet (M p.167).

---

## Sources

- **Manual TU05** (p.19, p.83–97, p.102–118, p.136–143, p.149–157, p.167; rules as cited): https://ftc-resources.firstinspires.org/ftc/game/manual
- **Q&A**: QA3, QA16, QA17, QA23, QA26. https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **FAC / FMG**:
  - https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
  - https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide
- **Inspection Checklist**: https://ftc-resources.firstinspires.org/ftc/archive/2027/event/inspection-check
- **FTCScout** (OPR, match data): https://ftcscout.org
