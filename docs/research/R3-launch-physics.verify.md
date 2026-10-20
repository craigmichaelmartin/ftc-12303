# R3 verification: launch physics, shooter sizing, HIVE tip and FLOWER stack math

Verifier V3, 2026-10-20. Adversarial check of `research/R3-launch-physics.md` against primary sources.

## Sources and method

**Primary sources checked:**
- Manual TU05: pp. 62-91, 103-113, 121-124 and 171-175, read as text; pp. 67, 69-77, 81-83, 88-89 and 105 viewed as images; Fig 9-11 and Fig 10-5 re-rendered at 300-400 dpi.
- Event Field Setup Guide (EFSG) V1.0: pp. 23-28.
- Field Acceptance Checklist (FAC) rev 26-27.2.
- Team Updates TU00-TU05: TU05 (2026-10-15) is the latest. Only TU01 touched HIVE geometry (30.6 in correction).
- Official Q&A board JSON (game-qa.firstinspires.org/boards/2027/FTC/QA). Answers QA2, QA8, QA10, QA12 and QA17 were matched verbatim in the raw `board.json`. The Atom feed confirms their IDs and dates.
- Vendor pages:
  - goBILDA 5203 1:1 motor
  - Hogback 72 mm and 96 mm wheels
  - Steel Flywheel 82 mm and **60 mm**
  - AndyMark BIOBUZZ element JSON
  - AndyMark 2017 FUEL page
- SDK 12.0 README.
- gm0 "sdk-motors".
- Chief Delphi threads 394940 and 439111.
- Pedersen, FRC 449, "Flywheel Shot Analysis: Correction and Extension" (May 2026), saved under `downloads/v3-mbhs-flywheel/`.
- TWU pickleball aerodynamics page; Tandjono 2020 abstract; USA Pickleball ESM 2021 §2.D; Team 254 2017 binder.
- Official field STEP CAD, via the global instance extraction at `work/r2v/cad_instances.json`. I read that file myself, but the assembly-transform walking was done by the R2 verifier's code.

**Independent re-computation.** I wrote my own ballistic model from scratch (`v3scripts/vball.py`, `vprob.py`, `vsens.py`). It uses RK4 at 1-2 ms steps and checks rim clearance continuously between samples. I used R3's stated assumptions: C_D 0.5, ρ 1.19 kg/m³, 16 in release height, a 14 in mouth and a 0.375 in rim radius. Outputs are in `v3out/`.

**Result.** The model reproduces R3's fixed-hood tables:
- Mid speeds agree within 0.01-0.02 m/s.
- Windows agree within ±0.2 percentage points.
- Probabilities agree within 1 point (65°: 21/89/86/78/68/61% vs R3's 22/89/86/78/68/60%).

## Verdicts

| ID | Verdict | Key evidence / correction |
|---|---|---|
| F1 | corrected (minor) | **Geometry confirmed:**<br>• Fig 9-9/9-10: the frames are perpendicular to the 30° arm.<br>• Fig 10-1 render and EFSG p23: NECTAR rests at the pivot end, "adjacent to the Hive Back Skin". EFSG p26: ballast "at the end of each Cell nearest its opening". Fig 9-15: "Front of Cell", with the arm running past the other end.<br>• 21.46·cos30 + 1.36·sin30 = 19.27 in; 53.5 + 14·cos30 = 65.62 in.<br>• Official CAD: lip 53.39 in high at 19.33 in from the pivot, apex 65.68 in, outward normal (−0.87, 0, 0.5). FAC 1.13: lip 53.5 ± 0.5 in.<br>**Wrong:** "52.7 in from the wall". CAD perimeter panels sit at |x| 70.52-71.65 in, so the field is about 141.3 in inside, not 144. EFSG 7.2: tiles "will likely be wider than the inside of the perimeter". **The lip is about 51.3 in from the wall it faces.** |
| F2 | confirmed | **Start state:**<br>• Fig 10-2 (p83): the up red CELL with red NECTAR is the audience-side CELL; blue's up CELL is the far one.<br>• EFSG p23: red "Audience" CELL and blue "Scoring" CELL tilted up.<br>• FAC 1.11.<br>**Alternation:** the TIP definition (p87, p175) swaps the CELLs. With the opening facing the near end wall (F1), the alternation follows. |
| F3 | corrected (minor) | With the CAD field, the robot touches the wall at 61.67 in from the centre, so D ≈ 70.6 − 9 − 19.3 ≈ **42.3 in** (≈48-49 in for a rear launcher), not 43.7.<br>This is not a hard rule limit: G412's box says contact with the wall outside the FIELD is not a violation, and G304's no-overhang note applies only at the start. A launcher overhanging the wall could go a few inches farther. |
| F4 | corrected | **Exit speeds and windows reproduced.** At 65°: 5.67 m/s (D 27 in), 5.31 (36), 5.46 (48), 6.12 (72), 6.66 (90); windows ±2.6-5.3%.<br>**Entry is not "descending 0-53°":**<br>• 65° hood: rising 35° at D 27 in, rising 23° at D 30, ~0° at D 36, descending 54° at D 90.<br>• 62° hood: rising 44° at D 27.<br>• R3's own tables show −23°/−28° at D 30.<br>**Flight time is 0.27-0.92 s.** The angle window is only about ±1.3° at D 27 in. |
| F5 | confirmed (model) | Independently reproduced. Mean P over 36-72 in:<br>• Errors σ 3%/1.5°: 60° 82%, 62.5° 82%, 65° 80%.<br>• Errors σ 2%/0.75°: 96 / 96 / 94%.<br>The adjustable-hood 88% is consistent with R3's per-distance maxima but was not recomputed.<br>Caveats: pure model; no bounce-out, lateral or heading error; σ values assumed. |
| F6 | confirmed (model, partly reproduced) | 2-D, head-on, D = 25.7 in: 65° 43%, 70° 85%, 72° 91%, 62.5° 18%. The azimuth zone map was not re-run.<br>Note: the "first tile row X ≈ 33-93 in" reaches into column D, which is blue's side during AUTO (G402). |
| F7 | confirmed (model) | Reproduced:<br>• D 36 in at 50.5°: 10.70-13.85 m/s, ±0.5°.<br>• D 48 in at 43.5°: 9.89-15.0 m/s, ±1.15°.<br>• D 60 in: ±1.5°. D 72 in: ±1.6°. The ±0.2° figure comes from R3's D 30-33 in rows.<br>12 m/s at k 0.4 on a 72 mm wheel needs 7,958 rpm. |
| F8 | corrected | **Confirmed:**<br>• AndyMark spec: "Diameter: 2.8 in., 3.6 in."; "Weight: 0.055 lbs., 0.091 lbs". Variant data: NECTAR 0.091 lb.<br>• Mass per area 0.628 / 0.629 g/cm².<br>• My runs: same required speed within 0.3%; NECTAR windows 7-13% narrower.<br>**But:** "identical" assumes equal C_D. The hole patterns differ visibly (Figs 9-13/9-14), and perforated-ball C_D depends on hole size (floorball 0.39-0.72). A 0.1 change in C_D shifts the required speed by about 0.7-1.3%. **Treat identical flight as an untested assumption.** |
| F9 | confirmed (approx.) | My runs:<br>• Vacuum needs 2.7-6.1% less speed.<br>• C_D 0.35 vs 0.65: 1.7-3.9% spread.<br>• Magnus C_L 0.15: −1.6% to +0.5%. That is slightly beyond ±1% at 60° / D 36 in. |
| F10 | confirmed | **TWU (Lindsey, 2025-05-12):** C_D 0.45 (SD 0.092) for 26-hole and 0.33 (SD 0.083) for 40-hole balls; 5-13 m/s, 300 fps, RK4 fit; backspin C_L about 0.17-0.2; Steyn 0.30 ± 0.02 and C_L ≈ 0.195·S; Emond C_D 0.6.<br>**Tandjono 2020** (AIP Conf Proc 2227, 020021): 0.39 / 0.51 / 0.58 / 0.72, numerical, Re 10⁵.<br>**Rossmann & Rau 2007** exists; its values were not verified. |
| F11 | corrected (wording) | **Physics confirmed:**<br>• The CD quotes are verbatim.<br>• Pedersen 2026 App. A gives V_bf = (2I_w/r_w)ω / (4I_w/r_w² + I_b/r_b² + m_b), i.e. k = 0.5/(1+x), exactly R3's formula.<br>• Brettle: efficiency < 50%.<br>**Wording:** "teams report 0.2-0.45" is one poster's "My wild guess is most teams are between 20-45%", plus one team's "30%". Both are FRC contexts. |
| F12 | confirmed | goBILDA 5203-2402-0001 page: 12 VDC, 6,000 RPM, 0.25 A, 9.2 A, 1.47 kg·cm (20.45 oz-in), "28 PPR at Output Shaft", 391 g.<br>0.1442 × 628.3 / 4 = 22.6 W; scaled by V² this gives 20.8 W at 11.5 V. |
| F13 | corrected (incomplete) | **Confirmed:**<br>• The rim reads "Ø72mm - 50A" in the photo zoom.<br>• 72 mm Hogback: 55 g (SKU 3626-0014-0072). 96 mm Hogback: 82 g.<br>• 82 mm steel flywheel: 152 g, 1651 g·cm², "Built for our 96mm Diameter Hogback Traction Wheel".<br>**Missed:** goBILDA **Steel Flywheel 60 mm, 115 g, 551 g·cm²** (SKU 3628-0014-0060), "Built for compact attachment to the 72mm Diameter version of our Hogback Traction Wheel". |
| F14 | confirmed (arithmetic) | 60v/(πdk):<br>• 72 mm wheel, 5.3-6.66 m/s: 3,515-4,416 rpm (59-74% of free speed; 61-77% at 11.5 V).<br>• 96-104 mm wheel: 2,433-3,312 rpm.<br>"Recommended" is a judgement. The cheaper first step is the 60 mm flywheel; the hood is built around the 72 mm wheel. |
| F15 | confirmed (model) | The drop formula x/(1+x) matches Pedersen App. A. Recomputed:<br>• 18.7 / 27.6%, 5.7 / 9.1%, 7.45 / 11.8%, with R3's unmeasured inertias.<br>• Bare 72 mm wheel recovery: 0.080 s at 12 V, 0.090 s at 11.5 V.<br>R3's own table shows +11% to **+28%** at 11.5 V, not 10-25%.<br>72 mm wheel plus the 60 mm flywheel: drop ≈ 10.6% (POLLEN) and 16% (NECTAR); spin-up ≈ 0.5 s. |
| F16 | corrected | **The arithmetic holds**, but a 1-1.5% gate sits at the measurement limit:<br>• gm0: the REV hub computes velocity from a "5 value ring buffer" with "a new value added to it every 10 ms".<br>• That gives steps of about 20-25 ticks/s, which is ≈1.0-1.5% at 1,650-2,100 ticks/s with a 28-count encoder, plus about 20-25 ms of lag.<br>**Fix:** gate at about ±2-3% with a short dwell or debounce, or add a higher-resolution sensor. Tune the feed interval empirically. |
| F17 | confirmed | **Arithmetic:** ±0.5 V around 12 V gives ±4.2% speed.<br>**Team 254 binder p28 (verbatim):** "Once RPM has stabilized around the setpoint, start gathering average kF samples … switch to pure kF control for essentially open loop shooting". So 254 supports feedforward plus a stability gate, not "closed loop required".<br>**Real battery range is wider** than 11.5-12.5 V: fresh packs exceed 13 V. That makes open-loop worse. |
| F18 | corrected | EFSG §12.3 (p27): only the **tossed** 3rd/8th POLLEN tipping is "Necessary"; a gently placed 3rd/8th tipping is only "Preferred". The no-tip cases (2nd POLLEN tossed onto 3N+1P; 7th tossed onto 6P) are Necessary.<br>FAC 2.3-2.6 also check placed 3N+2P → no tip, then a tossed POLLEN → tip; placed 7P → no tip, then a tossed 8th → tip. FAC 2.1: "Subtle variations may occur".<br>Ballast at the opening end and "some amount of variation" are confirmed (EFSG p26). |
| F19 | confirmed (nuance) | **3 NECTAR at start:** manual p84 B.i and EFSG p23. The calibration implies the 3rd launched POLLEN tips the CELL if all three stay in.<br>**Manual p87's warning** is specifically about LAUNCHING "at the downward-facing CELL while a HIVE is tipping".<br>**QA12:** "There are no rules that prevent adding a NECTAR to the CELL before the HIVE comes to rest … Variations in placement … and dynamics … may result in the HIVE tipping variations." |
| F20 | unverifiable | **Model.** The only primary anchors are the calibration points and the mass ratio 0.091/0.055 = 1.65.<br>I re-did R3's torque arithmetic: 3.82 / 4.36 / 4.47 / 4.43 lb·in.<br>I checked the sign of the impulse effect: tipping needs tan γ > z/y ≈ 0.99, so steeper than about 45°.<br>The HIVE's moment of inertia and damping are unpublished. |
| F21 | unverifiable | The USAP §2.D.5 bounce of 30-34 in from 78 in onto granite gives e ≈ 0.59-0.66 (verified).<br>The CELL bounce-out thresholds come from a 2-D model with an assumed e = 0.4-0.55. There is no primary data. |
| F22 | corrected | **G417 text and examples A-H confirmed** (pp. 111-112).<br>**"Honest misses are not a violation" is wrong:** example H says such misses are "likely to not be perceived as STRATEGIC". The base penalty is a VERBAL WARNING; MAJOR FOUL + YELLOW CARD applies only if STRATEGIC.<br>**Frame contact:** accidental bumps on the frame while picking up POLLEN are example G (likely not STRATEGIC). Direct or transitive HIVE contact is example E. |
| F23 | confirmed (caveats) | **Volume definition:** p88: "at least partially within … between the top ring and the middle ring" (CAD Reference 10-4).<br>**Top of volume:** Fig 10-5 H counts a NECTAR sitting on the top ring, so the top is the top-ring top: CAD 21.40 in, FAC 21.5 ± 0.25.<br>**Floor:** not dimensioned in the manual. The CAD middle ring is at Z 3.90-5.25 per the R2 extraction.<br>**Stacking:** pitch geometry re-derived. My count of Fig 10-5: A6 B5 C6 D8 E4 F4 G6 H7, matching R3. |
| F24 | confirmed (caveat) | Table 10-2 (2 points per element plus the 5-point Bottom NECTAR Bonus), p88 definitions, and Fig 10-5 D (8 elements → 21 points) all check out.<br>Fig 10-5's convention, where the 2nd POLLEN from the bottom is not counted, favours the low end: **3 counted, 11 points**.<br>G410 restricts only NECTAR, so POLLEN top-ups before 1:00 are legal. |
| F25 | corrected | **Confirmed:**<br>• About 2.3-2.8 in drop per pull, and a 4.8 in drop from 2 pulls that can flip ownership (R3 model).<br>• G418.B: only POLLEN, only from the bottom. FAC 2.9: POLLEN under a NECTAR can be removed and the NECTAR "does not escape".<br>**Wrong:** each pull shifts the whole stack down one slot, so the lowest counted element drops out of the volume. **The count falls by 1 (−2 points) unless an element stranded above the top ring drops in.** R3's own `flower.txt` shows 6 → 5 after the first pull. The report's §9.4 "count usually does not drop" is wrong. |
| F26 | confirmed | Radial clearance (4.0 − 2.8)/2 = 0.6 in and (4.0 − 3.6)/2 = 0.2 in. Top at 21.5 in (FAC ± 0.25), 1.25 in backstop (p72), 29 in robot height (R105.A).<br>Wording note: p88 says "Placing … into the top … is the only allowable way to score", while G418.A says "enter … into the top". Launching into the top is not expressly banned, but the wording is ambiguous. |
| F27 | confirmed | **IDs** checked visually (p76, Fig 9-17) and against FAC 1.11.<br>**Offset:** 9.938 − 2.75 = 7.19 in from "Front of Cell", which gives 6.23 in horizontally and 3.6 in vertically. CAD: tags at 13.1 in from the pivot and 49.8 in high.<br>**SDK 12.0 README:** "the Origin of each cluster is located in the center of the Cell opening for easy aiming"; tags "are not suitable for absolute Field Localization". |
| F28 | confirmed (minor numeric) | **Legality:** G304.A-H (p104). Fig 11-1 (p105) shows exactly this red start in C1 against the audience wall. Fig 9-5 puts the red CELL in column C.<br>**Distance:** with the CAD field, D ≈ 42.3 in rather than 44. P ≈ 89% either way (my run at 43.7 in: 89%). |
| F29 | confirmed | Arithmetic: 12 in/s × 0.3-0.9 s = about 4-11 in. |
| F30 | corrected | **Wrong width:** "20 in" is the **inner clear width**. Both extension lines in Fig 9-11 land on the inner frame edges, and the CAD inner skins are at ±10.10 in. Usable centre-path width is therefore 20·cos(az) − 3.55 = **16.4 / 13.8 / 10.6 / 6.5 in** at 0/30/45/60°, not 14.9/12.5/9.5/5.7.<br>**Lateral offset reproduced:** ±4.5 → 4.0 / 3.4 / 2.9%.<br>**Azimuth:** my model gives ±5.1/5.2/5.3/**4.6**% at 0/30/45/60°, against R3's 5.6% at 60°. Neither model handles 3-D rim clearance. |
| F31 | confirmed | **Arithmetic checks.**<br>**254 binder p17:** "Fixed hood at 14º", "0.5” foam provides optimal compression". **p26:** "Interpolating Tree Map determines the correct flywheel RPM for the current distance".<br>**AndyMark:** the 5 in Gopher ResisDent softball "was used as 'fuel' during the 2017 … Steamworks" (0.156 lb).<br>A thick, soft lining might grip both sizes; "cannot" applies to rigid or thin-foam hoods. |
| F32 | confirmed (model) | 0.406/0.362 = 1.12; 0.472/0.455 = 1.04. Same C_D caveat as F8. |
| F33 | confirmed (model) | Reproduced at 65°: mean 79/80/81% for 12/16/20 in release height; at D 24 in: 2/21/56%. |

## Missed facts and risks

1. **Field size.** The official CAD field is about 141.3 in inside, not 144 (perimeter about ±70.6 in from centre; EFSG 7.2). Every lip-to-wall distance in R3 is about 1.4 in too long. The C1 head-on distance is about 42 in.

2. **CELL opening tolerances.**
   - FAC 1.13 sets the lip at 53.5 ± 0.5 in, tighter than the ±1 in R3 used.
   - FAC 1.7 sets the FLOWER top at 21.5 ± 0.25 in.
   - The CELL clear opening is about 20 in wide (inner), not 18.5 in.

3. **Matched flywheel for the team's wheel.** goBILDA's 60 mm, 551 g·cm² steel flywheel bolts to the team's 72 mm Hogback. It cuts the POLLEN drop per shot from about 19% to about 11%, with about 0.5 s spin-up.

4. **G403: the flywheel cannot be powered in the 8 s transition.** Any powered movement of a MECHANISM is forbidden; coasting is allowed. TELEOP's first volley therefore pays spin-up time. G404 has the same effect at match end.

5. **Velocity resolution.** REV hub velocity from a 28-count encoder is quantised at about 20-25 ticks/s, roughly 1-1.5% (gm0). A 1-1.5% RPM gate is not reliably resolvable.

6. **Shot blocking is legal (QA2, QA8).** Opponents may extend up to 29 in (R105) over a shooter. A 65° arc from 16 in reaches about 28-29 in within about 6-7 in horizontally of release, so steep arcs are much harder to block than flat shots.

7. **G402 in AUTO.**
   - Columns D-F are blue's side; navigating there is "risky" (MAJOR FOUL if it disrupts blue).
   - R3's suggested red spot D1, and its zone map out to X ≈ 93 in, sit on blue's side.
   - D1 is beside blue's audience-wall FLOWER, at about X 94-95 in.

8. **Spill zone and G409.** A TIP pours the formerly-up CELL's contents toward the wall it faced, onto the shooting side.
   - Per the CAD, the down-CELL opening is at about 20.7 in from centre and 36.8 in high, aimed 30° down.
   - QA17: elements bouncing off a robot under a TIPPED HIVE are a G409 violation (VERBAL WARNING if not STRATEGIC).
   - Close-in spots such as C2 (D ≈ 26 in) sit in or beside the spill zone.

9. **HIVE clearance.** The down CELL hangs to 30.6 in (TU01); robots may be 29 in tall (R105). That leaves about 1.6 in nominal clearance. A full-height turret or hood under the down CELL risks direct HIVE contact (G417.E, likely STRATEGIC).

10. **SDK AprilTag origin.** The SDK 12.0 cluster origin is the CELL-opening centre, so SDK AprilTagProcessor users get the aim point directly. Limelight users must apply the offset themselves. Tags move with the HIVE, so they cannot be used for field localisation.

11. **More Q&A rulings.**
    - **QA12:** no rule against launching into a moving HIVE, but it may disrupt the TIP. A TIP is scored when the damper contacts the frame, however many elements caused it. Placement and dynamics vary.
    - **QA10:** launching into the opponent's HIVE is not specifically disallowed.

12. **FLOWER rules R3 understated.**
    - POLLEN may enter FLOWERS at any time; only NECTAR is time-limited (G410, MAJOR FOUL per NECTAR).
    - Each bottom pull lowers an owned FLOWER's count by one unless a stranded element drops in (see F25).

13. **HIVE calibration varies by event.** FAC 2.1 says "Subtle variations may occur" and only tossed-ball tipping is mandatory. Verify tip counts at each event's practice matches.
