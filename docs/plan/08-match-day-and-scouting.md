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
- [ ] Limelight boots (green light after 15–20 s); lens clean; mount tight, still looking out of the back at its pitch.
- [ ] Pinpoint pods touching the floor; springs OK.
- [ ] In **Check: Sensors**: intake in (A), carrier toward the shooting wheel (B) and backward (X) run the right way; once fitted, the colour sensors read POLLEN / red / blue correctly.
- [ ] Flywheel spin test (ready in under 1 s); nothing rubbing; **bolts checked**.
- [ ] Intake arm swings freely on its hinge and drops back to the floor; star wheels and the side gear/pulley tight. Tunnel stop in place (if one is fitted): the tunnel holds 4.
- [ ] Guard on the shooting wheel. **Signs in the right colour** for the next match.
- [ ] Competition build: **INIT telemetry shows no AutoTune / Dashboard / Panels warning** (`PEDRO_AUTOTUNE = false`; every 12303 OpMode warns in INIT; [06 §8](06-software.md#8-competition-rules-for-software-checklist)).
- [ ] Driver Station charged; 2 **wired** gamepads (R904, QA16); spare cable.
- [ ] Queue bag: start jig, spare battery, tools, scouting notes on the next partner and opponents.

## Queue checklist

- [ ] Partner talk ([script below](#partner-conversation-script-about-2-minutes-in-the-queue)): AUTO roles, start spots, who parks first (corner slot / inner slot).
- [ ] Our colour this match; signs match it; the driver sets the AUTO selector Alliance to it at the field.
- [ ] Human player knows the NECTAR plan; the tray has 5 NECTAR (M p.83).

## On-field checklist (set-up)

- [ ] Robot at the agreed start, placed with the jig:
  - [ ] front (intake) touching the wall, back (shooter) into the field (S1, S6: back toward the HIVE);
  - [ ] **not touching a FLOWER** (check S6: red F-far, blue F-aud!);
  - [ ] not in the LZ;
  - [ ] fully on our side (G304).
- [ ] **Exactly 4 POLLEN** preloads touching the robot (G304.G).
- [ ] AUTO OpMode **12303 Auto** selected, **30 s timer on** (G305). Selector: **Alliance = THIS match's colour, set first** (it shows RED after any app restart or battery swap, otherwise the previous AUTO's value: read the top line `##### RED ALLIANCE #####` / `BLUE` aloud and compare it with our signs), then start / routine / park / delay / **preloads 4** → **LOCKED**. Every line carries over from the last run until the app restarts, and the values shown run whether or not they are locked: check every line.
- [ ] INIT. Telemetry: selector lines right, own tags seen ("vision: N tags" with N > 0), AIM bias near 0, no `CHECK:` lines except the FLOWER reminder that always shows on S6 (then check by eye that the robot does not touch the FLOWER), and no `WARN:` / `!!` lines except, while the robot has no mouth sensor, the `!!!! NO MOUTH SENSOR` lines right under the selection menu and the `WARN: no mouth sensor` line (then the drivers stop at 4 by eye in TELEOP). The "Place robot at" and POSE lines only repeat the selected spot: the jig is the placement check. **Nothing moves** after INIT (G304.H): AUTO INIT moves nothing.
- [ ] Hands off from the countdown (G401).

## During the match: who does what

| Person | Job |
|---|---|
| Driver (gamepad 1) | Drives, runs the intake (without a mouth sensor: stops at 4 balls by eye), **holds RB to aim** (the robot turns its back onto the CELL), calls "Crossing far / audience" |
| Operator (gamepad 2) | Shooting mode, volleys and trims; **presses Y on every "TIP!"** and checks the target on arrival in the new zone (operator light not RED; PURPLE = the driver must hold RB); fixes the ball count (without a mouth sensor an eject does not lower it: press back once per ball ejected); calls "4 in", "Empty", "Full" |
| Human player (a student) | NECTAR ([04 script](04-endgame.md#human-player-script-whole-match)); calls "TIP!" (the operator's cue for Y) |
| Drive coach | Watches the **field timer**; keeps the CELL count and calls "8, it's going"; makes the endgame calls ("NECTAR, GO" at 0:59, "0:25", "0:12, last volley", "Park"); presses stop / INIT / start on the DS (allowed by G423). **May not touch balls** (G424) **or handle gamepads once the match starts** (G423) |

- **Transition:** INIT TELEOP (it moves nothing). **Read the INIT Alliance line**: with AUTO data it shows `Alliance X (from AUTO …)`; without it, `!! NO AUTO DATA - ALLIANCE = X …` (after an app restart or reboot, or if AUTO was not run). If the colour is wrong, the driver presses gamepad 1 X (blue) or B (red) now. Press **START at the TELEOP bells** (G403). In the no-AUTO-data case the driver then points the robot's front straight away from the drivers and presses back, and relocalizes with LB + Y once it is on our S1. After an A3 double tip (INIT says "AUTO says HIVE state B …"), the operator presses Y at START.
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

1. **Hello.** "We're 12303. We shoot POLLEN out of the back of our robot, tip the start CELL in AUTO, and park."
2. **AUTO.** "What's your AUTO? Where do you start? Do you end **off the wall** and **in our LOADING ZONE**?"
   - Pick roles: A1 / A2 / A3 ([02 scripts](02-autonomous.md#coordination-scripts)).
   - Decide who takes S1. Only one robot fits there. If the partner takes it, we can still run A1 from S2–S4 (the robot first drives to the Zone A side spot).
   - If either alliance asks, robots are placed red 1, blue 1, red 2, blue 2 (M p.85).
3. **SWARM.** "The first robot to park takes the **corner slot** [red: the far-wall end / blue: the audience end of our LOADING ZONE]; the second takes the **inner slot**. Both stay off every wall at the end of AUTO." Set our Park line to match.
4. **TELEOP.** "Two loads = one tip. We call '4 in'. At 8 we stop and wait until the spill has landed (usually about 3 s, up to 6 s), then both go to the other zone."
   - Agree lanes: who shoots head-on and who shoots from the side.
   - Agree who sweeps which spill.
5. **NECTAR.** "Can you use NECTAR? If yes, our human rolls it toward you. If not, we hold it until 1:00."
6. **Defence.** "If they have one big scorer, who screens? Nobody pins longer than 3 s."
7. **Endgame.** "Final volley at 0:12 (0:15 if you are in the half away from our LOADING ZONE: red = audience side, blue = far side). Both park; same slots."
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
| 1 | A 5th ball in the intake, or one riding on top of the robot | G407 (warning → MAJOR + YELLOW if STRATEGIC); no momentary exception (QA3) | The tunnel holds 4 at most; auto-stop at 4 (with the mouth sensor; without it the driver stops at 4 by eye); top guard; don't push piles |
| 2 | Opponent NECTAR in the intake | G408 (warning → YELLOW if STRATEGIC; QA23 says MAJOR + YELLOW: plan for the stricter one) | Drivers steer clear; a mouth sensor (when fitted) refuses it, but only on contact |
| 3 | Spill bouncing off our robot | G409 (warning → YELLOW); QA17 | Stay out of the landing box until the balls land (usually ~3 s, up to ~6 s) |
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
