# 04 · ENDGAME (1:00 → 0:00)

Phase 1 (POLLEN-only robot): **keep tipping, fire everything at about 0:12, and get both robots into the LOADING ZONE.** Phase 3 (only if the NECTAR tests pass): use NECTAR to speed up tips or to own FLOWERS.

Coordinates are red; blue = rotate 180° ([02 blue poses](02-autonomous.md#blue-poses)). Source tags: see the [source key](00-master-plan.md#source-key). **est.** = estimate.

---

## What changes, and when

| Field timer | Cue | Rule change |
|---|---|---|
| **1:00** | Chime, "FLOWER Ownership Unlocked" (TU04; M p.79) | NECTAR may enter a FLOWER (G410). Early NECTAR is a **MAJOR foul per NECTAR**: two early = 40 points to the opponent. The human may enter **all** remaining NECTAR (G426.B) |
| 0:20 | Train whistle | **Nothing.** It's a reminder only (M p.79) |
| 0:00 | 3-s buzzer | **Stop.** No powered motion (G404). Press ■ and put the controllers down |

- The **primary field timer** is the cue, not a phone and not the Driver Station clock (G410 box, G426 box).
- A tip is judged until everything is at rest after the match (M p.86). So a ball **launched before 0:00** that finishes a tip after the buzzer should still count (our reading of M p.86). Never launch after 0:00 (G404).

---

## 1:00 script

| Who | Does |
|---|---|
| Coach | Watches the **field timer**. Says "**NECTAR, GO**" at 0:59 (one second of margin) |
| Human player | Enters **all remaining NECTAR** into our LOADING ZONE ([human player script](#human-player-script-whole-match)). Rolls it toward a partner only if the partner can use NECTAR; otherwise drops it on the **wall strip** of the LZ, away from the parking slots |
| Driver + operator | Keep tipping. Phase 1: **steer around all NECTAR**, especially in our LZ after 1:00. A mouth sensor refuses NECTAR at the mouth (always the opponent's; ours while `ACCEPT_OWN_NECTAR = false`, because NECTAR through the tunnel, carrier and shooting wheel is untested), but it only reacts on contact; without a mouth sensor nothing refuses it |

---

## 0:25 script (the coach calls the last cycle)

The coach calls the clock from the **field timer**: "**0:25**" (last cycle), "**0:12, last volley**" (0:15 if we are in Zone A), "**Park**". The 0:20 train whistle and our 0:20 rumble are only reminders. The coach makes the call. Our robot should be in Zone B, the half next to our LZ (red: far half; blue: audience half), for the last volley when it can: Zone B → LZ is **44 in ≈ 3 s**, against about 110–125 in from Zone A. **Operator: before the final volley, check the target** (operator light not RED; the AIM line names this zone).

| Situation at 0:25 | Plan |
|---|---|
| Our up CELL faces **Zone B**, and we hold balls | Final volley **by 0:12** → drive to our parking slot (≈ 3 s) |
| Our up CELL faces **Zone A**, and we hold balls | Final volley **by 0:15** → park (≈ 110–125 in, 3–4.5 s, est.; allow 5 s in traffic) |
| Our volley will make 8 in the CELL (**tip**) | Fire it: a tip is 20, PARK is 5. Then park if there is time: wait until the spill has landed, or leave along red y ≤ -38 (blue y ≥ +38) first, because a straight drive from B2 to the LZ crosses the landing box (G409) |
| Our volley won't tip | Fire, then park. 4 balls left in the up CELL = up to 8 points (2 each); PARK = 5 |
| No balls on board | Go park at 0:10 |
| **SWARM depends on our end PARK** (AUTO didn't lock it) | **PARK comes first.** Be in the LZ by 0:05: the RP matters more than a few points |

Rule of thumb: do both whenever the clock allows. If you must choose, **a tip (20) beats everything**, then **a PARK that SWARM depends on**. Otherwise a non-tipping volley (up to 8) and a PARK (5) are close, so pick the surer one.

Planning ahead (coach, around 0:40): if the HIVE is in Zone A's state and the CELL is close to 8, spend the next loads finishing that tip. The HIVE then flips to Zone B, and the final volley happens 44 in from the LZ.

---

## Park plan

| Robots | Red poses | Blue poses |
|---|---|---|
| One robot (solo) | (-35.25, -55) | (35.25, 55) |
| Two robots | corner slot (-51, -56) + inner slot (-27, -56) | corner slot (51, 56) + inner slot (27, 56) |

- PARK = at least partly inside our LZ when the match ends (M p.90). Both robots = **10 points**, and often the SWARM RP ([01 §3](01-game-and-scoring.md#swarm-combinations-arithmetic-from-table-10-2)).
- **Touching the wall is fine at the end of the match.** The no-wall rule is only for LEAVE at the end of AUTO (QA21).
- Same slot rule as AUTO: **the first robot to arrive takes the corner slot** (the LZ end nearer the field corner: red far-wall end, blue audience end). A robot in the inner slot blocks our corridor (red y ≈ -40, blue y ≈ +40).
- No rule protects a LOADING ZONE. If an opponent sits in it, push in gently and **don't pin longer than 3 s** (G421). Ask the Head Referee about LZ blocking ([09](09-rules-cheatsheet.md#ask-the-head-referee)).
- Stop on the buzzer: the coach or driver presses ■ (G404).

---

## Human player script (whole match)

**Before the match**
- [ ] Count our NECTAR in the tray: **5** (M p.83).
- [ ] Stand near the LZ end of our ALLIANCE AREA: **red = the far (scoring-table) end of the red wall; blue = the audience end of the blue wall.** The NECTAR tray is at mid-wall, about 25–45 in from the LZ (CAD).
- [ ] Know the partner's NECTAR plan: can they use it?
- [ ] Remember: the coach **cannot** be the human player (G424).

**During AUTO and TELEOP (before 1:00)**
- Each tip of **our** HIVE unlocks **one** NECTAR (G426.A). AUTO tips count, and the NECTAR may go in during AUTO (QA11). An early NECTAR is a **MINOR foul per NECTAR**.
- The coach confirms the tip count out loud. NECTAR doesn't have to go in right away (G426 box).
- **Phase 1: hold it** (keep it visible in the tray, G426 box), unless the partner can use it. Phase 3: drop it in for our robot.
- Call "**TIP!**" when the damper touches the frame. It is the operator's cue to press Y (our camera cannot see our own tip from the zone we fired from).

**At 1:00 (field timer):** enter all remaining NECTAR.

**Every time you enter NECTAR**
- **Drop it; don't place it.** Let go above the LZ so it **touches the LZ tile first**, before any robot or field element (G427.C, QA6). No tools (G427.A).
- After releasing it, you may roll or throw it toward a robot, as long as it lands in the LZ first and doesn't score in the HIVE or a FLOWER (QA7).
- Drop it on an **uncovered** part of the LZ. Our park poses leave a 5.7–6.7 in strip along the wall for this.

**Never**
- Touch a robot, a ball that is touching a tile or robot, or a field element (G425). Don't hit or shake the wall.
- Remove balls from the field (G428). POLLEN or opponent NECTAR that leaves the field goes to the field staff (G427 box).
- Leave the ALLIANCE AREA (G422). You may pick up **our** NECTAR that left the field if you can reach it from there, and re-enter it through the LZ "at any time" (G426 box; QA36 on timing is still pending).

---

## Phase-3 options (only after the NECTAR go/no-go passes)

Decide with [07 Exp 6](07-testing-and-practice.md#exp-6--nectar-go--no-go). The strategy brief says: **only pursue these if the robot can place NECTAR into the 4.0 in opening at 21.4 in reliably** (FLOWERS), or launch it reliably (HIVE).

### (a) NECTAR as a tip accelerant

| Fact | Value | Source |
|---|---|---|
| Empty CELL tips with | 3 NECTAR + 3 POLLEN = **6 balls instead of 8** | EFSG §12.3 |
| Other mixes by weight | 2 NECTAR + 5 POLLEN should tip; 1 NECTAR + 6 POLLEN is borderline | est. (AM masses) |
| Our NECTAR per match | 8: 3 start in our up CELL and spill out with tip 1; 5 in the tray (one per tip, the rest at 1:00) | M p.83–84; G426 |
| Safe ammo | The opponent may never control our NECTAR | G408 |
| Saving | About 2 balls per tip, roughly half a load (≈ 7–10 s, est.) | |
| Robot needs | The intake, ramp, tunnel, carrier and shooting wheel pass a 3.6 in ball; its own RPM table (probably 5–20% higher than POLLEN, est.: measure it) | our ball-flight model |

### (b) FLOWERS (last 60 seconds only)

How ownership works (M p.88):
- The alliance with the **top-most** NECTAR in the scoring volume owns the FLOWER and gets **2 points per POLLEN and NECTAR** in it, no matter who put them there.
- The alliance with the **bottom-most** NECTAR gets the **+5 Bottom NECTAR Bonus**.
- A FLOWER with no NECTAR has no owner and scores 0.

| Situation | Counted | Points |
|---|---|---|
| FLOWER still has its staged POLLEN (usually 3 of the 4 count) + **1 of our NECTAR** | 3 POLLEN + 1 NECTAR | 4 × 2 + 5 = **13** |
| Same with 4 POLLEN counted (or we add one POLLEN first) | 4 + 1 | 5 × 2 + 5 = **15** |
| A lone NECTAR in an emptied FLOWER | 1 | 2 + 5 = **7** |
| Topped up: 7 POLLEN + 1 NECTAR (manual Fig 10-5 D) | 8 | 16 + 5 = **21** |
| Opponent caps our FLOWER with their NECTAR | – | They own it; we keep the +5 if ours is the bottom-most |
| Opponent pulls the POLLEN out from under our NECTAR | – | 13 → about 7 (est.; FAC 2.9 shows they can) |

- **Data:** at the end of US matches, 36–53% of FLOWERS still held at least 3 staged POLLEN, but only 1 of 704 US FLOWER end-states had any NECTAR (4 of 924 including Kazakhstan; FTCScout). One NECTAR often finds a 13-point FLOWER.
- **Geometry:** the top opening is 4.0 in wide at 21.4–21.5 in (CAD; FAC 1.7: 21.5 ± 0.25 in), with a 1.25 in backstop on the wall side (M p.72). A 3.6 in NECTAR has only 0.2 in of clearance on each side, so **place it** (a dropper reaching at least 22.75 in) rather than shoot it (est.).
- **Which FLOWERS:** in TELEOP, any of them. Red side: F-red (23.39, -68.04), F-far (-68.04, -23.39). Blue side: F-aud (68.04, 23.39), F-blue (-23.39, 68.04).
- **Compare:** a tip is 20 points, counts toward the POLLINATOR RP and unlocks a NECTAR. A FLOWER is about 13 points and earns no RP.
- **Foul risks:** early NECTAR (G410, MAJOR **per NECTAR**); entering anywhere but the top (G418.A); holding NECTAR against the pipes (G418 box H; QA30 pending).

---

## GARDEN

- 1 point per ball at least partly inside **our** GARDEN at the end (M p.89). Anyone may add or remove balls; it is not protected (G411 box).
- **Ignore it**, except for free pushes on the way to park. The red GARDEN is in the audience corner, far from our LZ, so this only happens when coming from Zone A.

---

## Endgame points at a glance

| Action | Points |
|---|---|
| TIP | 20 (+ unlocks 1 NECTAR, + counts toward POLLINATOR) |
| End PARK | 5 per robot (+ may complete SWARM) |
| Ball left in our up CELL | 2 each |
| FLOWER: our NECTAR on staged POLLEN | about 13 |
| GARDEN | 1 each |
| MAJOR foul (e.g. early FLOWER NECTAR) | **-20** (given to the opponent) |
| MINOR foul (e.g. early NECTAR entry) | -5 (given to the opponent) |

---

## Sources

- **Manual TU05** (Table 9-1 p.79; p.83–91; G404, G410, G411, G418, G421–G428): https://ftc-resources.firstinspires.org/ftc/game/manual
- **TU04** (1:00 chime): https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- **Q&A**: QA6, QA7, QA11, QA21; pending QA30, QA36. https://game-qa.firstinspires.org/boards/2027/FTC/QA/qa/N
- **EFSG §12.3**: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
- **FAC 1.7, 2.9**: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
- **CAD** (park poses, FLOWER positions; derived): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **AM** (ball masses): https://andymark.com/products/biobuzz-scoring-elements
- **FTCScout** (FLOWER end states): https://ftcscout.org
