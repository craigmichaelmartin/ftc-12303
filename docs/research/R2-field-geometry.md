# R2 - BIOBUZZ field geometry, AprilTags, start positions (FTC 12303)

Research date: 2026-10-20. Scope: a precise field model for AUTO path planning and turret aiming.
All lengths in inches, all angles in degrees. Every number has a source tag (see Section 15) and an uncertainty.

Companion machine-readable data (same numbers): `docs/research/R2-field-geometry-data.json`

Legend for confidence: **[V]** verified in a primary source (manual, official CAD, official guide, SDK binary); **[D]** derived by me from primary data (geometry/maths, reproducible); **[E]** estimate / physics model / community observation, needs field testing.

---

## 0. The 14 findings that matter most

1. **Coordinate frame [V/D].** Official FTC rule (stand at the Red Wall, look at the field: +X right, +Y away from the Red Wall, +Z up, origin = field centre on the tile surface). BIOBUZZ has the red ALLIANCE AREA on the audience's left (manual p68), the same "inverted square" layout ftc-docs describes for DECODE, so for BIOBUZZ: **+X points TOWARD THE AUDIENCE, +Y points TOWARD BLUE, heading 0 = facing the audience wall, CCW positive.** Blue pose = red pose rotated 180 deg: (X, Y, h) -> (-X, -Y, h+180).
2. **The real field is 141.3 in between walls, not 144 [V].** Official STEP CAD: perimeter inner faces at +/-70.67, tile pitch 23.50, seams at 0, +/-23.5, +/-47.0. An 18 in robot touching a wall has its centre 61.67 from the field centre.
3. **CRITICAL - which way the up CELL opening faces [V/D].** Each CELL is a pentagonal "house"-section box, 12.04 deep, built along the HIVE arm. Its **opening is the outer end face (the end farthest from the pivot)**; the back skin is on the pivot side. The 30 deg in Fig 9-10 is the tilt of the arm/CELL floor from horizontal. So the **up CELL's opening faces its own end wall - the RED AUDIENCE cell's opening faces the AUDIENCE wall (+X), the SCORING (far) cell's opening faces the far / scoring-table wall (-X) - tilted 30 deg upward** (opening plane 30 deg from vertical). It does **not** face straight up and does **not** face the alliance walls. Shots must come from in front of the opening (lateral off-axis <= ~35-40 deg).
4. **Consequence: red shoots from the AUDIENCE half at match start; after every red TIP the up CELL swaps ends [D].** Red AUDIENCE cell up at start -> shoot from rows 1-3 (X > ~+25). After a TIP the RED SCORING cell is up -> shoot from rows 4-6 (X < ~-25). Blue is the mirror: blue starts shooting from the FAR half. The red LOADING ZONE is at the far end of the red wall (X -46.6..-23.9), i.e. next to red's second shooting area.
5. **Up-CELL aim point [V/D].** SDK cluster origin = centre of the opening = **(+16.50, -12.74, 58.27)** for the red AUDIENCE cell at start; floor edge of the opening at Z 53.39 (manual/checklist: 53.5 +/- 0.5), apex at Z 65.68 (manual 65.6). After a red TIP the red SCORING cell's aim point is (-16.48, -12.74, 58.31).
6. **Down CELL hangs to Z = 30.6 [V]** (lowest point, front-bottom corner, |X| ~17, CAD 30.65 = manual 30.6). Opening centre of a down CELL: (+/-20.65, ..., 36.77), facing outward and 30 deg DOWN - that is where a TIP dumps its contents.
7. **SDK 12.0 data [V].** `AprilTagGameDatabase.getBioBuzzTagLibrary()` (= `getCurrentGameTagLibrary()`) holds 4 clusters, no single tags: "RED SCORING" 30-33, "RED AUDIENCE" 34-37, "BLUE AUDIENCE" 38-41, "BLUE SCORING" 42-45; member tag centres at x = -6.5/-2.75/+2.75/+6.5, y = +7.1874, z = -5.622 in the cluster frame (x = tag-image right, y = image down, z = into tag), tag size 3.25. Cluster fieldPosition is (0,0,0) and orientation identity -> **SDK robotPose is meaningless for BIOBUZZ unless you supply your own per-HIVE-state cluster poses (Section 7.4 gives them).**
8. **Tags are only usefully visible from the side the up CELL's opening faces [D].** Both stickers of a HIVE are on parallel CELL floors, so all 8 tags of a HIVE face down, tilted 30 deg toward the scoring side. From the opposite half they are edge-on/backwards. Camera needs ~40-60 deg upward elevation to see the up-CELL tags from 20-45 in away (19-28 deg for the down CELL's tags).
9. **Tip threshold [V].** Setup guide calibration: the up CELL must TIP with 8 POLLEN (7 must not), or with 3 NECTAR + 3 POLLEN (3 NECTAR + 2 POLLEN must not). Since each alliance's up CELL starts with 3 own NECTAR, **landing 3 POLLEN TIPs it** (AUTO TIP = 20 pts). The next CELL starts empty: 8 POLLEN or 3 NECTAR + 3 POLLEN.
10. **Spill lands on the side you were shooting from [D, corroborated E].** The CELL you fill becomes the down CELL and pours out of its opening, outward toward its end wall: first floor contact at |X| ~28-40 (about 30-43 in from that end wall), within roughly the HIVE's Y band +/-15, then rolls toward the wall. Peer-measured: first touch 35-47 in from the wall, ~3 s after the threshold piece (R4 note, Mona Shores data).
11. **The HIVE frame is almost transparent at robot height [V/D].** Foot bars are flat 0.07 in sheet metal; the only low obstacles are 4 molded feet (2.13 tall) at (+/-17.2..19.5, +/-22.8..24.7) and 4 legs that slope inward to the top corners (0, +/-12.2..13.8, ~41). Robots <= 18 in tall can drive under the HIVE along X in a corridor |Y| < ~18.5 (G409/G417 risks apply).
12. **Flowers [V].** Opening centres (Z 21.40): far wall red half (-68.04, -23.39); red wall (+23.39, -68.04); audience wall blue half (+68.04, +23.39); blue wall (-23.39, +68.04). 2.63 in out from the wall, on wall-panel joints.
13. **Best red AUTO start for a shooter [D]: S1 = audience wall, centred on the red HIVE axis, robot centre (+61.67, -12.75), facing 180 deg** - 45.2 in straight in front of the up opening, tags at 39 deg elevation / 63 in / 21 deg incidence. Legal (column C, touching wall, clear of flowers, garden and loading zone).
14. **Shot geometry [D].** From a 14 in exit height the opening centre is +44.3 in up; arriving level or descending needs launch angles ~77 deg (20 in) / 71 (30) / 66 (40) / 61 (50) / 56 (60) at ~4.8-5.7 m/s in vacuum (drag adds ~10-20%). The opening presents its full area to balls descending ~30 deg.

---

## 1. Coordinate convention

### 1.1 Official FTC field frame applied to BIOBUZZ [V/D]
- Rule (ftc-docs "Field Coordinate System"): origin = centre of the field on the top of the tiles; "Looking at the origin from the Red Wall, the X axis ... values increase to the right"; "Increasing Y values run out (away) from the Red Wall"; +Z up; positive rotations CCW (right-hand rule). The Red Wall = perimeter wall adjacent to the red ALLIANCE AREA. [DOCS-FCS]
- ftc-docs "Square Field (Inverted ALLIANCE AREA)" (written for DECODE): "red ALLIANCE AREA on the left when viewed from the audience ... The Y axis points across the field from the Red Wall towards the Blue Alliance. The X axis points towards the audience to the front of the field." [DOCS-FCS source rst]
- BIOBUZZ manual p68: "red ALLIANCE AREA is located on the left from the primary audience viewing direction." -> BIOBUZZ uses that inverted-square frame.
- **Result:** +X = toward the audience wall; -X = toward the far wall (the "SCORING" table side, the word used in the tag/cluster names [DOCS-CL]); +Y = toward the blue wall; -Y = toward the red wall; +Z up. Heading 0 = facing the audience wall; +90 = facing the blue wall; 180 = facing the far wall; -90 = facing the red wall.
- The SDK 12.0 BIOBUZZ library defines **no** field poses (cluster position (0,0,0), identity quaternion), so this frame is a team convention; it matches what FTC docs and the SDK localization samples assume. [SDK]
- Page-orientation note: in all manual top views (Figs 9-2, 9-5, 9-17, 10-2, 11-1) the audience is at the bottom and red on the left. In that picture **+X points DOWN the page and +Y points RIGHT**.

### 1.2 Other frames you will meet
| Frame | Mapping from FTC-BIOBUZZ (X, Y, h) | Notes |
|---|---|---|
| Official STEP CAD (Onshape export) | X_cad = Y, Y_cad = Z (up), Z_cad = X | metres in the file; +X_cad toward blue, +Z_cad toward audience [CAD] |
| Pedro Pathing (documented conversion) | x_p = Y + 72, y_p = 72 - X, h_p = h - 90 | origin = red/audience corner, x_p toward blue, y_p toward the far wall, i.e. the manual's top view; with a 141.3 in field the walls sit at 1.33 and 142.67 [PEDRO] |
| Corner frame (simple) | same as Pedro | convenient for hand-drawn paths |
| Tile grid | columns A..F = Y bands, rows 1..6 = X bands (Section 2) | used by G304/G402 |

---

## 2. Field, tiles, seams [V]

| Item | Value | +/- | Source |
|---|---|---|---|
| Inner face of perimeter (polycarbonate) | X, Y = +/-70.67 (field 141.3 x 141.3) | 0.1 CAD; ~1 at events | [CAD]; manual p64 says "approximately 144 in" (nominal) |
| Tile surface / rail inner face | tiles end at +/-70.59; bottom rail top at Z 0.54 | 0.1 | [CAD] |
| Perimeter wall height | top rail top at Z = 11.64 | 0.1 | [CAD] |
| Wall panel joints (where FLOWERS mount) | +/-23.40 along every wall (3 panels per side) | 0.1 | [CAD]; FAC 1.5 "correct field wall joints" |
| Tile pitch | 23.50 (centre tiles); edge tiles trimmed to ~23.6 | 0.5 (tiles vary) | [CAD]; [EFSG 7.2-7.4] |
| Seams 1..5 (manual Fig 9-4, horizontal lines) | seam 1 X=+47.0, 2 X=+23.5, 3 X=0, 4 X=-23.5, 5 X=-47.0 | 0.5 | [M p67][CAD][FAC 3.2-3.3] |
| Seams V..Z (vertical lines) | V Y=-47.0, W Y=-23.5, X Y=0, Y Y=+23.5, Z Y=+47.0 | 0.5 | [M p67][CAD] |
| Column A / B / C (red side) | Y in [-70.67,-47.0] / [-47.0,-23.5] / [-23.5,0] | 0.5 | [M p67, G304.A] |
| Column D / E / F (blue side) | Y in [0,23.5] / [23.5,47.0] / [47.0,70.67] | 0.5 | |
| Row 1 / 2 / 3 | X in [47.0,70.67] / [23.5,47.0] / [0,23.5] (audience half) | 0.5 | |
| Row 4 / 5 / 6 | X in [-23.5,0] / [-47.0,-23.5] / [-70.67,-47.0] (far half) | 0.5 | |
| Tile centres | columns A..F at Y = -58.8, -35.25, -11.75, +11.75, +35.25, +58.8; rows 1..6 at X = +58.8, +35.25, +11.75, -11.75, -35.25, -58.8 | 0.5 | [D] |

AUTO side boundary (G402, G304.A): red = columns A-C = **Y <= 0**.

---

## 3. Zones and staged elements

| Zone | FTC coordinates (box) | +/- | Notes / source |
|---|---|---|---|
| **Red LOADING ZONE** | X in [-46.60, -23.91] (tile A5, between seams 5 and 4), Y in [-70.67, -59.10] | 0.25 | far end of the red wall. Checklist: 11 +/- 0.125 deep measured from inside of wall to far tape edge (CAD shows 11.6 from glass) [FAC 3.2-3.4][CAD][EFSG 8.3] |
| **Blue LOADING ZONE** | X in [23.91, 46.60] (tile F2, seams 2-1), Y in [59.10, 70.67] | 0.25 | audience end of the blue wall |
| **Red GARDEN** | X in [68.10, 70.67] (2 in tape + wall gap), Y in [-70.10, -47.41] | 0.25 | audience wall, red corner, tile A1 [FAC 3.5-3.8][CAD] |
| **Blue GARDEN** | X in [-70.67, -68.10], Y in [47.41, 70.10] | 0.25 | far wall, blue corner, tile F6 |
| Red ALLIANCE AREA (drivers) | outside red wall: Y in [-125.65, -71.65], X in [-48.41, 48.41] (96.8 x 54) | 1 | [M p68][EFSG 8.5][CAD] |
| Blue ALLIANCE AREA | Y in [71.65, 125.65], X in [-48.41, 48.41] | 1 | |
| Red NECTAR tray (5 red NECTAR + 4 reset POLLEN) | outside red wall at X ~ -7.9..+7.9, Y ~ -72..-82 | 1 | [CAD][EFSG 11.3] - human carries NECTAR ~25-45 in along the wall to the loading zone |

Staged SCORING ELEMENTS at match start (CAD positions, centres) [CAD][M p83-84][EFSG 11]:

| Elements | Position |
|---|---|
| 3 red NECTAR in red AUDIENCE (up) CELL | (9.5, -20.9/-17.3/-13.7, 49.9): against the back skin, on the red-wall side |
| 3 blue NECTAR in blue SCORING (up) CELL | (-9.6, +13.8/+17.4/+21.0, 49.9) |
| 4 POLLEN in each FLOWER | stacked at the flower axis, Z = 1.4, 4.3, 7.2, 10.1 |
| 4 POLLEN red GARDEN | (69.3, -69.3 / -66.4 / -63.5 / -60.6) along the audience wall from the red corner |
| 4 POLLEN blue GARDEN | (-69.3, +60.6 / +63.5 / +66.4 / +69.3) along the far wall from the blue corner |
| Robot preloads staged outside (8 per alliance) | red: X -46.0..-26.4 at Y ~ -73 (outside the wall beside the red LZ); blue mirrored |

---

## 4. FLOWERS [V]

All four are identical, mounted on wall-panel joints, axis vertical. Opening = 4.0 dia top ring, top surface at **Z = 21.40** (checklist 21.5 +/- 0.25), backstop 1.25 tall on the wall side (Z 22.40-22.65). Middle ring Z 3.90-5.25; retrieval opening Z 0.35-3.90 (3.55 tall) on the field side; scoring volume = between middle-ring top and top ring (~Z 5.3-20.3). [M p72-73][CAD][FAC 1.7][FMG 3]

| FLOWER | Wall / alliance side | Opening centre (X, Y) | Footprint box (rings) | +/- |
|---|---|---|---|---|
| F-far | far wall, **red half** (straddles B/C seam) | (-68.04, -23.39) | X [-70.65, -65.63], Y [-26.34, -20.45] | 0.3 (event 1) |
| F-red | red wall, audience half (straddles rows 2/3) | (+23.39, -68.04) | X [20.45, 26.34], Y [-70.65, -65.63] | 0.3 |
| F-aud | audience wall, **blue half** (straddles D/E) | (+68.04, +23.39) | X [65.63, 70.65], Y [20.45, 26.34] | 0.3 |
| F-blue | blue wall, far half (straddles rows 4/5) | (-23.39, +68.04) | X [-26.34, -20.45], Y [65.63, 70.65] | 0.3 |

Notes:
- Each FLOWER sticks ~5.0 in out from its wall and is ~5.9 in wide along the wall. The opening centre is 2.63 in from the wall face. [CAD]
- Manual hint (p83): "the CELL which 'points at' a FLOWER should be the one tilted down" - the red SCORING cell points at F-far, the blue AUDIENCE cell at F-aud. That is consistent with "the opening is the outer end face" (Section 6.2).
- Red-side FLOWERS (on columns A-C): F-far and F-red. FLOWERS are not alliance-owned; ownership comes from NECTAR (R1 covers rules).

---

## 5. HIVE frame (static structure) [V]

Frame = two leaning A-frames (each: 2 legs 45.75 long + molded top corner holding one pivot cradle + flat foot bar), joined by a 24 in Frame Top Bar, bolted through the tiles to two under-tile bars. [IFEAG parts list p6-8][EFSG 9][CAD]

| Part | FTC coordinates | +/- | Path-planning meaning |
|---|---|---|---|
| Overall footprint | X [-19.47, 19.47], Y [-24.73, 24.73] (manual: 38.95 deep x 49.46 wide) | 0.5 | |
| Frame feet (molded, 4) | X +/-[17.20, 19.48], Y +/-[22.77, 24.65], Z 0-2.13 | 0.5 | **hard obstacles** at the 4 corners |
| Foot bars (2) | X [-19.47, 19.47], Y +/-[22.75, 24.73]; **flat 0.07 in sheet metal** (raised only at the feet) | 0.3 | drive-over-able |
| Legs (4, ~1 in square) | centreline ~(+/-18.4, +/-23.6, 0.7) -> (+/-0.7, +/-12.9, 41.0) | 0.5 | slope inward; see Section 10 |
| Top corners / pivot cradles | X +/-1.52, Y [-13.74,-10.49] and [10.51,13.76], Z 39.5-44.2 | 0.2 | |
| Frame Top Bar | X +/-0.5, Y [-12.0, 12.0], Z 40.95-41.95 | 0.2 | |
| BIOBUZZ ACM panels (may be absent, manual p69) | X [-4.28,-1.55] and [1.55,4.28], Y [-15.06,15.07], Z 34.07-40.02 | 0.3 | can occlude the far CELL's tags |
| Under-tile bars | X +/-[15.78,17.28], Y +/-23.9, below the tiles; bolts at (+/-16.53, +/-23.75) | 0.5 | no obstacle |
| **Pivot axes** | red (0.00, **-12.74**, **43.95**), blue (0.00, **+12.76**, 43.95), axis parallel to Y | 0.2 (event 0.5) | nominal +/-12.75 (25.5 c-c, Fig 9-10) |

Checklist 1.8: each foot-bar end sits at the centre of the 4th tile tooth from the tile junction +/-0.5 (Fig 3). The foot bars lie along seams W and Y (Y = +/-23.5); their ends are ~4 in inside the seam-2/seam-4 junctions (X = +/-23.5), i.e. at X = +/-19.5, so the frame is centred on the field. [FAC][CAD]

---

## 6. HIVE and CELL geometry (the part that matters for aiming)

### 6.1 What the CELL is [V]
- Each CELL = two pentagonal Goal Ribs (20 wide x 14 tall opening, 7.61 to the shoulders) joined by clear skins: bottom skin (floor + 2 side walls), top skin (2 roof slopes), and a **back skin on ONE end**. Depth 12.04 outer (11.6 between ribs). [M p70-72][IFEAG 6.2-6.10][CAD]
- "The Hive Back Skin should attach on the Goal Rib which is closest to the AprilTag Sticker"; "The Cell Base Tube should protrude from the side of the assembly with the Hive Back Skin" (toward the pivot); the long ballast screw "must be installed in the hole furthest from the Hive Back Skin, closest to the opening of the Hive". [IFEAG 6.7, 6.9, 6.10]
- Therefore **open end = outer end (away from the pivot); back skin = pivot end.** Confirmed in CAD (front rib at b = 0..-2.0, back skin at b = -12.0, measured along the arm from the opening) and by Fig 9-15 ("Front of Cell" 9.938 in from the reference holes; the arm tube runs past the other end). [CAD][M p75]
- The pentagon's point (roof apex) is the top of the opening; the floor (with the AprilTag sticker underneath) is the flat 20 in side, on the arm.

### 6.2 What the 30 deg means, and where the opening points [V/D]
- Fig 9-10 measures 30 deg between horizontal and the up CELL's floor/arm. CAD: sticker/floor tilt 30.11 deg; a TIP rotates the HIVE 60.2 deg about its pivot. [M p71][CAD]
- Up CELL: floor rises 30 deg going outward; the opening (outer end face) plane is 30 deg from vertical; **opening normal = outward along the arm, 30 deg above horizontal** (red AUDIENCE: (+0.866, 0, +0.5)). Balls inside roll to the back skin.
- Down CELL: same rigid rotation the other way; opening normal = outward, **30 deg below horizontal** - contents pour out.
- Top view: both CELLS of a HIVE show their pentagon points in the same direction (red toward the far wall, blue toward the audience in the start state) because the whole rocker is rotated rigidly (Fig 9-17 matches).
- Entry condition for a ball: velocity . normal < 0. For a ball moving toward the HIVE from the opening side, that holds for any descent and even for rises up to 60 deg; the presented aperture is A*cos(beta-30) (beta = descent angle): 0.87 level, **1.00 at 30 deg descending**, 0.97 at 45, 0.87 at 60, 0.50 straight down. [D]
- A shot from behind (pivot side) would need >60 deg descent through a 7 in deep slot just past the roof apex - impractical; Mona Shores' shot-map sim also found scoring only in wedges in front of each opening (R4). Launching at the outside of a CELL is a G417 example if deliberate. [M p111-112]

### 6.3 Cell key points, both stable states [V/D]
States per HIVE: **State A** = match start (red AUDIENCE up; blue SCORING up). **State B** = after an odd number of that HIVE's TIPs. Tables give the red HIVE; blue = 180 deg rotation (X,Y)->(-X,-Y) with "audience" <-> "scoring" swapped. Uncertainty: +/-0.2 vs CAD; at events +/-0.5 vertical (checklist 1.13), +/-1 horizontal, tilt +/-1.5 deg.

| Red CELL | State | Up? | Opening centre = SDK cluster origin (X, Y, Z) | Opening normal (outward) | Opening floor edge (X, Z) | Opening apex (X, Z) | Cell volume centre | Lowest point |
|---|---|---|---|---|---|---|---|---|
| RED AUDIENCE (34-37) | A | **UP** | **(16.50, -12.74, 58.27)** | (+0.866, 0, +0.5) -> audience, 30 deg up | (19.33, 53.39) | (12.20, 65.68) | (11.29, -12.74, 55.24) | ~44 (base tube near pivot); back rib 46.1 |
| RED SCORING (30-33) | A | down | (-20.65, -12.74, 36.77) | (-0.866, 0, -0.5) -> far wall, 30 deg down | (-17.82, 31.90) | (-24.95, 44.19) | (-15.44, -12.74, 39.79) | **30.65** at X ~ -17.1 |
| RED AUDIENCE (34-37) | B | down | (20.62, -12.74, 36.74) | (+0.866, 0, -0.5) -> audience, 30 deg down | (17.79, 31.86) | (24.92, 44.15) | (15.41, -12.74, 39.76) | 30.6 at X ~ +17.1 |
| RED SCORING (30-33) | B | **UP** | **(-16.48, -12.74, 58.31)** | (-0.866, 0, +0.5) -> far wall, 30 deg up | (-19.31, 53.43) | (-12.18, 65.72) | (-11.27, -12.74, 55.29) | ~44 |

| Blue CELL | State | Up? | Opening centre (X, Y, Z) | Opening normal |
|---|---|---|---|---|
| BLUE SCORING (42-45) | A | **UP** | (-16.50, +12.76, 58.27) | (-0.866, 0, +0.5) -> far wall |
| BLUE AUDIENCE (38-41) | A | down | (+20.65, +12.76, 36.77) | (+0.866, 0, -0.5) |
| BLUE AUDIENCE (38-41) | B | **UP** | (+16.48, +12.76, 58.31) | (+0.866, 0, +0.5) -> audience |
| BLUE SCORING (42-45) | B | down | (-20.62, +12.76, 36.74) | (-0.866, 0, -0.5) |

Lateral extent of every CELL: inner skins Y = centreline +/-10.10 (opening ~20.2 wide), ribs +/-10.5 -> red CELLS span Y [-23.24, -2.24], blue [2.26, 23.26]; 4.5 in gap between the HIVES. [CAD]

Top-view footprint (state A), useful for tall mechanisms: red AUDIENCE (up) X [1.7, 20.1]; red SCORING (down) X [-25.0, -6.7] and Z >= 30.65; blue mirrored. Highest point of any CELL: front rib of the up CELL, Z 65.93. [CAD]

Cross-checks against the manual: opening bottom 53.39 vs 53.5 (Fig 9-10, checklist 53.5 +/- 0.5); opening top 65.68 vs 65.6; HIVE bottom 30.65 vs 30.6; overall HIVE length 42.91 = 12.04 + 18.84 + 12.04 (18.84 in Fig 9-9 is the GAP between CELLS, not centre spacing; CELL centres are 15.44 from the pivot along the arm). [M p71][CAD][D]

### 6.4 Tip mechanics and threshold [V, E]
- TIP = HIVE goes to the other stable state AND the previously free damper contacts the frame (manual 10.5.1, Fig 10-3). Assessed throughout the match; TIPs complete before TELEOP count as AUTO. [M p86-87]
- Calibration (Event Field Setup Guide 12, checklist 2.3-2.6): 3 NECTAR + 1 POLLEN, 2nd POLLEN tossed -> **must not TIP**; 3 NECTAR + 2 POLLEN, 3rd POLLEN tossed -> **must TIP**; 0 NECTAR + 6 POLLEN, 7th tossed -> must not; 7 POLLEN + 8th tossed -> must TIP. Ballast washers sit on the screw at the opening end of each CELL. "[3] Pollen + [3] Nectar has less mass than [8] Pollen." [EFSG 12][FAC 2.3-2.6]
- So: start-state up CELL (3 own NECTAR inside) TIPs on the 3rd POLLEN; every later CELL starts empty (needs ~8 POLLEN-equivalents, e.g. 3 NECTAR + 3 POLLEN).
- Timing (peer measurement, R4 from Mona Shores' Saline stream): dwell 0.25-3.4 s (median ~2 s) after the threshold piece, swing 0.65-0.9 s, spill reaches tiles ~3 s after the threshold piece; overloading shortens the dwell. [E]

---

## 7. AprilTags

### 7.1 Physical tags [V]
- 36h11, 3.25 in, four per sticker ("AprilTag Cluster"), sticker 17.0 x 5.0 in with two alignment notches 14.0 apart on the "bottom" edge (red/blue stripe, ID labels), applied to the clear Hive Bottom Skin (the CELL floor) aligned to its two reference holes, printed face toward the tiles, bottom edge toward the field centre (i.e. toward the back skin/pivot). [M p74-76][IFEAG 6.1][ART][CAD]
- Tag centres: 2.75 in above the notch edge (2.25 below the top edge); x = -6.5, -2.75, +2.75, +6.5 from the sticker centre for IDs base+0..base+3 (left to right when the tag image is upright). Reference-hole line is 9.938 in from the CELL front, so tag centres are **7.19 in behind the opening plane**. [ART][M p75]
- IDs: 30-33 red SCORING (far) CELL; 34-37 red AUDIENCE; 38-41 blue AUDIENCE; 42-45 blue SCORING (verified visually, manual p76-77; checklist Fig 4 "Audience-facing CELLS have AprilTags 34-41 (in order)").
- Seen from the scoring side with the image upright, IDs read left->right: red AUDIENCE 34,35,36,37 (34 nearest the red wall); red SCORING 33,32,31,30 from the audience side (upside-down image) - matches Fig 9-17. [M p77][D]

### 7.2 SDK 12.0 library (decompiled constants from Vision-12.0.0.aar, `org.firstinspires.ftc.vision.apriltag`) [V]
```java
// AprilTagGameDatabase (public static unless noted)
AprilTagLibrary getBioBuzzTagLibrary();          // getCurrentGameTagLibrary() returns BIOBUZZ tags in 12.0
private static ArrayList<AprilTagClusterMemberMetadata> getBioBuzzCluster(int firstId);  // private!
// clusters built as:
new AprilTagClusterMetadata(getBioBuzzCluster(30), "RED SCORING",   "RED SCORING",   new VectorF(0,0,0), DistanceUnit.INCH, Quaternion.identityQuaternion());
new AprilTagClusterMetadata(getBioBuzzCluster(34), "RED AUDIENCE",  "RED AUDIENCE",  ...same...);
new AprilTagClusterMetadata(getBioBuzzCluster(38), "BLUE AUDIENCE", "BLUE AUDIENCE", ...same...);
new AprilTagClusterMetadata(getBioBuzzCluster(42), "BLUE SCORING",  "BLUE SCORING",  ...same...);
// getBioBuzzCluster(first): tag size 3.25 (inch), members (id, positionInClusterPlane):
//   first+0 -> (-6.5 , 7.1874, -5.622)   first+1 -> (-2.75, 7.1874, -5.622)
//   first+2 -> ( 2.75, 7.1874, -5.622)   first+3 -> ( 6.5 , 7.1874, -5.622)
```
- Cluster frame = AprilTag convention: corners are solved as (x -/+ s/2, y +/- s/2, z) exactly like single tags, i.e. **x = tag-image right, y = tag-image down, z = into the tag**. Origin = 7.19 in toward the tag-image top (toward the opening) and 5.62 in into the CELL = **centre of the opening on the outer face of the front rib** (CAD check: b = 0.00, 5.64 above the floor). Release notes: "the Origin of each cluster is located in the center of the Cell opening"; "since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization." [SDK][DOCS-CL][CAD]
- Classes/fields: `AprilTagClusterDetection` {`percentClusterFound` (int), `metadata` (AprilTagClusterMetadata: `name`, `shortName`, `fieldPosition`, `fieldOrientation`, extents, `clusterMembers`)} plus inherited `ftcPose` (x, y, z, yaw, pitch, roll, range, bearing, elevation), `rawPose`, `robotPose`, `frameAcquisitionNanoTime`. `AprilTagSingleDetection` for non-cluster tags (none in BIOBUZZ). Public constructors exist for `AprilTagClusterMemberMetadata(int id, VectorF pos, double size)`, `AprilTagClusterMetadata(ArrayList, String, String, VectorF, DistanceUnit, Quaternion)` and `AprilTagLibrary.Builder().addCluster(...)`. The processor calls `computeRobotPose(rawPose, cluster.fieldPosition, cluster.fieldOrientation, ...)` for clusters, so a custom per-state library yields a real field `robotPose`. [SDK]
- ftcPose (camera-relative): x right, y forward, z up; range/bearing/elevation to the cluster origin = opening centre; bearing positive CCW (turn left). [DOCS-ADV]
- ftc-docs targeting heuristic: name starts with your alliance letter and |ftcPose.roll| < 90 -> scorable. Example from the docs: from the audience half, RED SCORING roll 159.3 (50% tags), RED AUDIENCE roll -19.7 (100%). **Caveat [D]:** |roll|<90 really means "this CELL's opening faces the camera". From the far half in state A the down RED SCORING cluster would also look upright (if visible at all). Robust state check: the opening-centre height (up ~58.3 vs down ~36.8 in; difference 21.5) or the tag-row height (49.8 vs 35.5; difference 14.3). Note: community code uses ~9.4 in for this delta (18.84 x sin 30) - that is wrong, because 18.84 is the gap between CELLS, not their centre spacing.

### 7.3 Tag poses in both states (FTC-BIOBUZZ frame) [D from CAD]
Quaternion q = (w, x, y, z) = rotation whose columns are the tag's (image-right, image-down, into-tag) axes in field coordinates; this is the SDK `fieldOrientation` convention (verified against the INTO THE DEEP library: tag 12 on the blue wall has q = (0.7071, -0.7071, 0, 0)). n_vis = outward normal of the printed face. Position uncertainty: +/-0.3 within the CELL; +/-1 in field frame at events; orientation +/-2 deg.

| ID | CELL | State | Up? | X | Y | Z | n_vis | q (w, x, y, z) |
|---|---|---|---|---|---|---|---|---|
| 30 | RED SCORING | A | down | -11.61 | -6.24 | 35.52 | (0.50, 0, -0.87) | (0.6828, 0.1837, -0.1837, -0.6828) |
| 31 | RED SCORING | A | down | -11.61 | -9.99 | 35.52 | same | same |
| 32 | RED SCORING | A | down | -11.61 | -15.49 | 35.52 | same | same |
| 33 | RED SCORING | A | down | -11.61 | -19.24 | 35.52 | same | same |
| 34 | RED AUDIENCE | A | UP | 13.10 | -19.24 | 49.80 | (0.50, 0, -0.87) | (0.6828, -0.1837, -0.1837, 0.6828) |
| 35 | RED AUDIENCE | A | UP | 13.10 | -15.49 | 49.80 | same | same |
| 36 | RED AUDIENCE | A | UP | 13.10 | -9.99 | 49.80 | same | same |
| 37 | RED AUDIENCE | A | UP | 13.10 | -6.24 | 49.80 | same | same |
| 38 | BLUE AUDIENCE | A | down | 11.61 | 6.26 | 35.52 | (-0.50, 0, -0.87) | (0.6828, 0.1837, 0.1837, 0.6828) |
| 39 | BLUE AUDIENCE | A | down | 11.61 | 10.01 | 35.52 | same | same |
| 40 | BLUE AUDIENCE | A | down | 11.61 | 15.51 | 35.52 | same | same |
| 41 | BLUE AUDIENCE | A | down | 11.61 | 19.26 | 35.52 | same | same |
| 42 | BLUE SCORING | A | UP | -13.10 | 19.26 | 49.80 | (-0.50, 0, -0.87) | (0.6828, -0.1837, 0.1837, -0.6828) |
| 43 | BLUE SCORING | A | UP | -13.10 | 15.51 | 49.80 | same | same |
| 44 | BLUE SCORING | A | UP | -13.10 | 10.01 | 49.80 | same | same |
| 45 | BLUE SCORING | A | UP | -13.10 | 6.26 | 49.80 | same | same |
| 30 | RED SCORING | B | UP | -13.08 | -6.24 | 49.84 | (-0.50, 0, -0.87) | (0.6828, -0.1836, 0.1836, -0.6828) |
| 31 | RED SCORING | B | UP | -13.08 | -9.99 | 49.84 | same | same |
| 32 | RED SCORING | B | UP | -13.08 | -15.49 | 49.84 | same | same |
| 33 | RED SCORING | B | UP | -13.08 | -19.24 | 49.84 | same | same |
| 34 | RED AUDIENCE | B | down | 11.58 | -19.24 | 35.48 | (-0.50, 0, -0.87) | (0.6828, 0.1836, 0.1836, 0.6828) |
| 35 | RED AUDIENCE | B | down | 11.58 | -15.49 | 35.48 | same | same |
| 36 | RED AUDIENCE | B | down | 11.58 | -9.99 | 35.48 | same | same |
| 37 | RED AUDIENCE | B | down | 11.58 | -6.24 | 35.48 | same | same |
| 38 | BLUE AUDIENCE | B | UP | 13.08 | 6.26 | 49.84 | (0.50, 0, -0.87) | (0.6828, -0.1836, -0.1836, 0.6828) |
| 39 | BLUE AUDIENCE | B | UP | 13.08 | 10.01 | 49.84 | same | same |
| 40 | BLUE AUDIENCE | B | UP | 13.08 | 15.51 | 49.84 | same | same |
| 41 | BLUE AUDIENCE | B | UP | 13.08 | 19.26 | 49.84 | same | same |
| 42 | BLUE SCORING | B | down | -11.58 | 19.26 | 35.48 | (0.50, 0, -0.87) | (0.6828, 0.1836, -0.1836, -0.6828) |
| 43 | BLUE SCORING | B | down | -11.58 | 15.51 | 35.48 | same | same |
| 44 | BLUE SCORING | B | down | -11.58 | 10.01 | 35.48 | same | same |
| 45 | BLUE SCORING | B | down | -11.58 | 6.26 | 35.48 | same | same |

Cluster pose for a custom library = (opening centre from 6.3, same q as its tags). Per-tag offset to the opening centre in the tag's own frame (x right, y down, z into tag): base+0 (+6.5, -7.19, +5.62); base+1 (+2.75, -7.19, +5.62); base+2 (-2.75, -7.19, +5.62); base+3 (-6.5, -7.19, +5.62) - i.e. 9.5 in (inner tags) / 11.2 in (outer tags) away. Use this as a Limelight "point of interest" offset (Limelight reportedly supports POI offsets; verify its axis convention) or in your own maths.

Sketch (state-specific library; rebuild/swap the processor or do the maths yourself when a TIP is detected):
```java
ArrayList<AprilTagClusterMemberMetadata> m = new ArrayList<>();
float[] xs = {-6.5f, -2.75f, 2.75f, 6.5f};
for (int k = 0; k < 4; k++) m.add(new AprilTagClusterMemberMetadata(34 + k, new VectorF(xs[k], 7.1874f, -5.622f), 3.25));
AprilTagClusterMetadata redAudienceUp = new AprilTagClusterMetadata(m, "RED AUDIENCE", "RED AUDIENCE",
    new VectorF(16.50f, -12.74f, 58.27f), DistanceUnit.INCH, new Quaternion(0.6828f, -0.1837f, -0.1837f, 0.6828f, 0));
AprilTagLibrary lib = new AprilTagLibrary.Builder().addCluster(redAudienceUp) /* + others for this state */ .build();
```

### 7.4 Where a robot camera can see them [D]
Model: camera at the robot centre, 10 in high; "visible" = incidence (angle between line of sight and face normal) < ~60 deg and range < ~100 in. Line-of-sight occlusion by legs/panels not modelled (the ftc-docs example shows the far CELL at 50% tags from the audience side).

| Camera at (robot centre) | State A: RED AUDIENCE tags (UP) elev / range / incidence | State A: RED SCORING tags (down) | State B: RED AUDIENCE (down) | State B: RED SCORING (UP) |
|---|---|---|---|---|
| S1 (61.7, -12.8) | 39 / 63 / 21 | 19 / 78 / 41 | back side - invisible | back side |
| S2 (61.7, -37.5) | 36 / 67 / 29 | 18 / 81 / 44 | invisible | invisible |
| S3 (45.0, -61.7) | 34 / 71 / 44 | 19 / 79 / 50 | 85 deg (no) | 86 (no) |
| S4 (0.0, -61.7) | 38 / 64 / 64 (marginal) | 27 / 56 / 60 (marginal) | 60 (marginal) | 64 (marginal) |
| S5 (-61.7, -61.7) | back side | back side | 16 / 92 / 50 | 30 / 80 / 42 |
| S6 (-61.7, -10.1) | back side | back side | 19 / 78 / 41 | 39 / 63 / 21 |
| S7 (-61.7, -40.0) | back side | back side | 18 / 82 / 44 | 36 / 68 / 31 |
| A1 (36, -12.8) | **60** / 46 / 0 | 28 / 54 / 32 | 74 (oblique) | 81 (oblique) |
| A2 (48, -12.8) | 49 / 53 / 11 | 23 / 65 / 37 | 85 | 87 |
| A3 (42, -36) | 47 / 54 / 26 | 24 / 64 / 40 | 82 | 85 |
| B1 (-36, -12.8) | 81 (oblique) | 74 (oblique) | 28 / 54 / 32 | **60** / 46 / 0 |
| B2 (-48, -12.8) | 87 | 85 | 23 / 65 / 37 | 49 / 53 / 11 |
| B3 (-42, -36) | 85 | 81 | 24 / 64 / 40 | 47 / 54 / 26 |

Coarse visibility map of the state-A red tags (incidence `#` < 40, `+` < 55, `.` < 65; H = HIVE footprint; far wall at top, audience at bottom, red wall left):
```
RED AUDIENCE tags (UP), state A                 RED SCORING tags (down), state A
X=-69..-21: (nothing visible - far half)        X=-29: ....  X=-21: ......+++++++++......
X= -9      ......HHHHHHHH|HHHHHHHH              X= -9    ......+++++#HHHHHHHH|HHHHHHHH....
X= +3  ........++++++HHHHHHHH|HHHHHHHH......    X= +3 .....++++++####HHHHHHHH|HHHHHHHH+.......
X=+15 ....+++++++####HHHHHHHH|HHHHHHHH+++.....  X=+15 ...+++++++#####HHHHHHHH|HHHHHHHH++++......
X=+27 ++++++++#######################++++++++.  X=+27 .+++++++++###################+++++++++....
X=+43 ++++++###########################++++++   X=+43 ++++++++++++###############+++++++++++++.
X=+59 +++++#############################++++++  X=+59 ++++++++++++++++++++++++++++++++++++++++.
       Y: -69 (red wall) ............ +69 (blue wall)
```
Practical camera guidance [D]:
- Pitch a fixed camera up ~40 deg (with a ~42 deg vertical FOV that covers ~19-61 deg elevation) to see the up-CELL tags at 20-60 in and the down-CELL tags at the bottom of the frame. A turret-mounted camera keeps yaw aligned automatically.
- Apparent tag size at 640x480 (Limelight 3A runs 640x480 @ 90 fps [AM-LL]; FOV not found in an accessible primary source, ~54-60 deg H assumed): ~35 px at 63 in, ~24 px at 90 in -> detection is fine across the useful shooting area.
- From under/behind the HIVE the tags are edge-on; a robot passing under the HIVE may see "wrong" down tags (R4 notes this is a reported Limelight issue).

---

## 8. Legal starting positions (G304, R102) [V rules, D coordinates]

Requirements (G304): fully in own side (red: Y <= 0, columns A-C, no overhang of the wall), not attached to field elements, **touching the perimeter wall**, not contacting or in the scoring volume of a FLOWER, **not in the LOADING ZONE**, inside the 18 in cube, touching exactly 4 preload POLLEN that are also on own side, motionless after INIT. Fig 11-1 shows legal examples (red: far-left corner, near F-far, around F-red, two along the audience wall). [M p104-105, R102]

Robot = 18 x 18 footprint, centre listed. Uncertainty +/-0.5 (walls/zones +/-1 at events).

| ID | Red start (centre X, Y) | Box | Touches | Clearances / legality | What it is good for |
|---|---|---|---|---|---|
| **S1** | **(61.67, -12.75)** | X [52.7,70.7], Y [-21.8,-3.8] (column C) | audience wall | F-aud is on blue side (Y>=20.45); garden ends Y=-47.4; no LZ | **Shoot immediately** into red AUDIENCE (up) opening, 45.2 straight on, bearing 180; tags clearly visible. Spill of TIP 1 lands ~20-35 in in front of it. |
| S2 | (61.67, -37.5) | X [52.7,70.7], Y [-46.5,-28.5] (column B) | audience wall | 0.9 in from garden tape (Y -47.41); garden POLLEN at Y <= -59.3 | Second shooter: 51.5 in, bearing 151, 29 deg off-axis (OK); or move-then-shoot |
| S3 | (45.0, -61.67) | X [36,54], Y [-70.7,-52.7] | red wall | 9.7 in from F-red (X <= 26.34); clear of garden (X >= 68.1) | 60 deg off-axis - must drive toward Y ~ -12 to shoot; close to F-red bottom POLLEN |
| S4 | (0.0, -61.67) | X [-9,9], Y [-70.7,-52.7] | red wall | 14.9 in from LZ, 11.5 in from F-red | Beside the HIVE - cannot shoot from here (109 deg off-axis); flexible to either end |
| S5 | (-61.67, -61.67) | X [-70.7,-52.7], Y [-70.7,-52.7] | far + red walls | 6 in from LZ (X >= -46.6) | Waiting robot for state B (66.6 in, 47 deg - marginal); next to LZ for PARK |
| S6 | (-61.67, -10.1) | X [-70.7,-52.7], Y [-19.1,-1.1] (column C) | far wall | 1.35 in from F-far (Y <= -20.45); 1.1 in from centre line | **Mirror of S1 for state B**: 45.3 in straight on to the red SCORING opening once it is up; invisible tags before TIP 1 |
| S7 | (-61.67, -40.0) | X [-70.7,-52.7], Y [-49,-31] | far wall | 4.7 in from F-far footprint (Y >= -26.34) | State-B shooter (52.8 in, 31 deg); a short strafe from F-far's bottom POLLEN |
| (alt) | audience wall near garden (61.67, -48) | overlaps garden tape | audience wall | legal (garden is not a no-start zone) but crowded by 4 garden POLLEN at Y <= -59.3 | |

**Symmetry:** the field is **180-degree rotationally symmetric, not mirrored** - FLOWERS, LOADING ZONES, GARDENS, HIVE start states and tag ID layout all map under (X,Y) -> (-X,-Y) (e.g. red LZ far end of red wall <-> blue LZ audience end of blue wall; F-far (red half) <-> F-aud (blue half)). A left-right mirror does NOT work (there is no flower at (+68, -23)). [CAD][D]

Blue equivalents (X, Y, heading) -> (-X, -Y, heading+180): S1' (-61.67, +12.75) facing 0 (toward the HIVE from the far wall) -> shoots blue SCORING (up) at start; S2' (-61.67, +37.5); S3' (-45.0, +61.67); S4' (0.0, +61.67); S5' (61.67, 61.67); S6' (61.67, +10.1); S7' (61.67, +40.0).

LEAVE/PARK geometry: LEAVE = not touching any perimeter wall at end of AUTO (peer note: Q&A Q21 says a robot touching a wall at end of AUTO gets no LEAVE even if parked - not independently verified). A robot centred at (-35.25, -55) overlaps the red LZ by 4.9 in while staying 6.7 in off the wall -> PARK and LEAVE. [M p90][R4]

---

## 9. Distances and angles to the red CELL openings [D]

Bearing = field heading from robot centre to the opening centre (CCW from +X). Turret yaw = bearing - chassis heading (wrap to +/-180). dZ = opening centre height - 14 in assumed exit height. psi = horizontal off-axis angle between the shot direction and the up opening's normal (0 = dead ahead; ~>40 = side wall starts to block; >90 = behind the CELL).

### 9.1 State A (match start; red AUDIENCE cell up, faces +X)
| Robot centre | to RED AUDIENCE (UP) opening: dist / bearing / dZ | to RED SCORING (down) opening | psi (up cell) | Verdict / LOS notes |
|---|---|---|---|---|
| S1 (61.7, -12.8) | 45.2 / 180.0 / +44.3 | 82.3 / 180.0 / +22.8 | 0 | **YES** - clear arc; nothing between |
| S2 (61.7, -37.5) | 51.5 / 151.3 / +44.3 | 86.0 / 163.3 | 29 | YES - CELL side wall trims ~13% of width |
| S3 (45.0, -61.7) | 56.6 / 120.2 | 81.9 / 143.3 | 60 | NO - side wall/roof block |
| S4 (0.0, -61.7) | 51.6 / 71.4 | 53.1 / 112.9 | 109 | NO - beside/behind |
| S5 (-61.7, -61.7) | 92.2 / 32.0 | 63.9 / 50.0 | 148 | NO - behind |
| S6 (-61.7, -10.1) | 78.2 / -1.9 | 41.1 / -3.7 | 178 | NO - behind (wait here for state B) |
| S7 (-61.7, -40.0) | 82.8 / 19.2 | 49.3 / 33.6 | 161 | NO |
| A1 (36.0, -12.8) | 19.5 / 180.0 / +44.3 | 56.6 / 180.0 | 0 | YES but needs ~79 deg launch; inside the spill landing zone |
| A2 (48.0, -12.8) | 31.5 / 180.0 / +44.3 | 68.6 / 180.0 | 0 | YES - good compromise |
| A3 (42.0, -36.0) | 34.5 / 137.6 | 66.8 / 159.6 | 42 | marginal |
| A4 (40.0, -50.0) | 44.1 / 122.2 | 71.2 / 148.4 | 58 | NO |
| B1..B4 (far half) | 52-64 | 15-40 | 146-180 | NO (behind the up CELL) |

### 9.2 State B (after odd # of red TIPs; red SCORING cell up, faces -X)
| Robot centre | to RED SCORING (UP) opening: dist / bearing / dZ | to RED AUDIENCE (down) | psi | Verdict |
|---|---|---|---|---|
| S6 (-61.7, -10.1) | 45.3 / -3.3 / +44.3 | 82.3 / -1.8 / +22.7 | 3 | **YES** (mirror of S1) |
| S7 (-61.7, -40.0) | 52.8 / 31.1 | 86.7 / 18.3 | 31 | YES |
| S5 (-61.7, -61.7) | 66.6 / 47.3 | 95.7 / 30.7 | 47 | marginal |
| B1 (-36.0, -12.8) | 19.5 / 0.0 | 56.6 / 0.0 | 0 | YES (steep; spill zone) |
| B2 (-48.0, -12.8) | 31.5 / 0.0 | 68.6 / 0.0 | 0 | YES - 44 in from the LZ park pose |
| B3 (-42.0, -36.0) | 34.5 / 42.3 | 66.8 / 20.4 | 42 | marginal |
| B4 (-36.0, -48.0) | 40.3 / 61.0 | 66.7 / 31.9 | 61 | NO |
| S1, S2, A1-A4 (audience half) | 52-82 | 15-48 | 141-180 | NO |

LOS blockers summary: from the correct side, nothing on the field sits between a robot and the up opening (the down CELL of the OTHER alliance is at |Y| 2.3-23.3 and Z <= 50.5, so only cross-field shots from the opponent half could graze it). The frame legs, top bar, panels and pivots are all behind the opening plane. Partner/opponent robots are the only dynamic blockers. Shots from behind or the side hit the CELL's back skin/roof/side walls (G417 example C if deliberate). [D][M p112]

### 9.3 Launch envelope to the up-opening centre (vacuum, 14 in exit) [D]
| Horizontal distance | Arrive level (beta 0) | Arrive 30 deg descending (full aperture) | Arrive 45 deg descending |
|---|---|---|---|
| 20 in | 77 deg / 4.82 m/s | 79 / 4.82 | 80 / 4.86 |
| 25 | 74 / 4.88 | 76 / 4.88 | 78 / 4.93 |
| 30 | 71 / 4.96 | 74 / 4.95 | 76 / 5.01 |
| 40 | 66 / 5.15 | 70 / 5.10 | 73 / 5.18 |
| 50 | 61 / 5.39 | 67 / 5.27 | 70 / 5.36 |
| 60 | 56 / 5.67 | 64 / 5.45 | 68 / 5.54 |
| 72 | 51 / 6.05 | 61 / 5.67 | 66 / 5.76 |
| 90 | 45 / 6.70 | 57 / 6.00 | 63 / 6.09 |
Shots arriving while still rising (launch flatter than the "level" column) present < 87% aperture and tend to hit the roof inside; drag on perforated balls (terminal velocity estimated ~14 m/s for both POLLEN and NECTAR) adds ~10-20% speed. Peer observations report frequent bounce-outs (R4). [D][E]

---

## 10. Clearances [V/D]

| Question | Answer | +/- |
|---|---|---|
| Lowest HIVE point (down CELL front-bottom corner) | 30.65 (manual 30.6), at \|X\| ~17.1, Y = HIVE centreline +/-10.5 | 0.5 |
| Other low items above the floor inside the frame | ACM panels Z >= 34.07 (X +/-1.6..4.3, Y +/-15); top bar Z 40.95; pivots/dampers ~40-46; up-CELL base tube ~44 | 0.3 |
| 18 in robot under the HIVE? | **Yes**: >= 12.6 in vertical clearance everywhere | |
| 29 in fully-expanded robot (R105) under a down CELL? | only 1.6 in margin at the CELL front corner (+/-0.5 event tolerance) - avoid | |
| Floor-level obstacles of the frame | 4 molded feet, 2.13 tall, at X +/-[17.2,19.48], Y +/-[22.77,24.65]; foot bars are 0.07 in flat plates | 0.5 |
| Corridor between the A-frame legs (robot driving along X) | inner face of legs at robot-top height h: \|Y\| ~ 20.1 (h=12), 19.6 (14), 19.1 (16), **18.5 (18)**, 18.0 (20), 16.9 (24), 15.6 (29); legs at \|X\| ~ 13.4 / 12.5 / 11.7 / 10.8 / 9.9 / 8.1 / 5.9 respectively | 1 |
| Usable under-HIVE lane for an 18x18x18 robot | centre \|Y\| <= ~9.5 (TELEOP: straddle Y=0 for +/-9.5 margin). In AUTO a red robot must stay at Y<=0 -> centre exactly Y ~ -9.25 with ~0.25 in margin: not practical | 1 |
| Driving along Y through an A-frame | only within \|X\| <~ 1 of centre for an 18 in wide x 18 in tall robot (leg centrelines at \|X\| 10.7 at 18 in height, ~1 in tube): treat as blocked | 1 |
| Rules when under the HIVE | contacting the HIVE/frame strategically = G417; catching/deflecting spilled pieces = G409 (peer note: Q17 says any deflection under a tipping HIVE violates G409). Manual G409 examples explicitly contemplate robots driving under the HIVE. | [M p109-112] |

Path-planning obstacle set for robots <= 18 in tall: walls (+/-70.67), 4 FLOWERS (5.9 x 5.0 boxes in Section 4), 4 frame feet, 4 leg segments projected for z in [0,18]: from (+/-18.4, +/-23.6) to (+/-10.7, +/-19.0) (inflate 1 in), staged POLLEN/NECTAR where relevant. Everything else of the HIVE is above 30 in. Waypoints (+/-30, -40) clear the red-side feet by ~20 in centre-to-centre. Useful lengths: S1 -> LZ park (-35,-55) via (30,-40): 109 in; S1 -> B2 (-48,-12.75) via the red corridor (30,-40),(-30,-40): 134 in; via under-HIVE lane at Y=0 (TELEOP): 117 in; B2 -> LZ park: 44 in; S1 -> F-red pickup pose (23.4,-56): 58 in.

---

## 11. Spill zone [D, corroborated E]

- Which CELL spills: the up CELL you just filled rotates 60.2 deg to become the down CELL; its floor tilts 30 deg toward its open end and the contents roll out of the opening. Exit edge (opening floor edge, down position): **(+/-17.8, Y = HIVE centreline +/-10, Z ~31.9)**, heading outward (toward that CELL's end wall) and ~30 deg downward. [CAD][D]
- Physics estimate (thin-shell rolling ball down 12 in of 30 deg floor, exit 0.8-2.0 m/s): first floor contact at |X| ~ 28-40 (10-22 in beyond the exit edge), then rolling/bouncing outward toward the end wall. Lateral spread ~ the CELL width (+/-10 around Y = -12.75 for red) plus scatter. [E]
- Peer measurement (R4, Mona Shores, Saline event): "pieces spill toward the wall the CELL faces (red audience CELL -> audience wall), first touch ~35-47 in from that wall, spread 2-3 ft either side within 0.3 s, mostly at rest against the wall/robots within ~2 s"; POLLEN rolling deceleration ~3.9 in/s^2, NECTAR ~1.5 in/s^2 (NECTAR rolls farther); spill on tiles ~3 s after the threshold piece. 35-47 in from the audience wall = X ~ +24..+36, consistent with the estimate. [E][R4]

| TIP | Spill side | Landing box (first contact, red HIVE) | Rest area |
|---|---|---|---|
| Red A->B (AUDIENCE CELL dumps) | audience half | X [+24, +42], Y [-28, +2] | up to the audience wall, mostly columns B-C, some into D |
| Red B->A (SCORING CELL dumps) | far half | X [-42, -24], Y [-28, +2] | toward the far wall, near F-far and the red LZ |
| Blue A->B (SCORING CELL dumps) | far half | X [-42, -24], Y [-2, +28] | toward far wall |
| Blue B->A (AUDIENCE CELL dumps) | audience half | X [+24, +42], Y [-2, +28] | toward audience wall |
Uncertainty of the boxes: +/-10 in. Implications: the spill lands on the side you were just shooting from (in front of S1/A1/A2 for red's first TIP) and the next up CELL faces the opposite end; for ~3 s after the threshold piece stay out of the landing box (G409). Start-state contents of red's first spill: 3 red NECTAR + >= 3 POLLEN.

---

## 12. ASCII top view (state A = match start)

Scale: 1 column = 2 in of Y, 1 row = 3 in of X (quantisation +/-1 in Y, +/-1.5 in X). Audience at the bottom, red on the left, i.e. +X down the page, +Y to the right.
Legend: `|`/`-` walls; `.`/`:` tile seams; `@` FLOWER opening; `R` red LOADING ZONE; `B` blue LOADING ZONE; `g` red GARDEN; `G` blue GARDEN; `p` preload POLLEN staged outside; `r`/`b` red/blue CELL footprints; `V` red AUDIENCE up-opening centre (opening faces DOWN the page = audience); `A` blue SCORING up-opening centre (faces UP the page = far wall); `x` down-CELL opening centres; `o` pivots, `=` top bar; `#` frame feet; `1`..`7` red start positions S1..S7 (robot centres); `~` ALLIANCE AREA tape.
```
                                                  FAR ("SCORING"-table) WALL, X = -70.7
Y(in)    -125                       -71         -47        -23.5       0           +23.5       +47         +71                     +125
X= -72                              |---------------------------------------------------------------------|
X= -69                              |           :          @           :           :           GGGGGGGGGGG|
X= -66                              |           :          :           :           :           :          |
X= -63                              |   5       :  7       :      6    :           :           :          |
X= -60                              |           :          :           :           :           :          |
X= -57                              |           :          :           :           :           :          |
X= -54                              |           :          :           :           :           :          |
X= -51                              |           :          :           :           :           :          |
X= -48  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~|RRRRR................................................................|~~~~~~~~~~~~~~~~~~~~~~~~~~~~
X= -45  ~                         pp|RRRRR      :          :           :           :           :          |                           ~
X= -42  ~                         pp|RRRRR      :          :           :           :           :          |                           ~
X= -39  ~                         pp|RRRRR      :          :           :           :           :          |                           ~
X= -36  ~                         pp|RRRRR      :          :           :           :           :          |                           ~
X= -33  ~                         pp|RRRRR      :          :           :           :           :          |                           ~
X= -30  ~ RED ALLIANCE AREA       pp|RRRRR      :          :           :           :           :          |  BLUE ALLIANCE AREA       ~
X= -27  ~ (drivers, 5 red         pp|RRRRR      :          :           :           :           :          |  (5 blue NECTAR           ~
X= -24  ~  NECTAR tray at X~0)      |RRRRR.................rrrrrrrrrrrr..................................@|   tray at X~0)            ~
X= -21  ~                           |           :          rrrrrrxrrrrr:bbbbbbbbbbbb           :          |                           ~
X= -18  ~                           |           :          #rrrrrrrrrrr:bbbbbbbbbbb#           :          |                           ~
X= -15  ~                           |           :          rrrrrrrrrrrr:bbbbbAbbbbbb           :          |                           ~
X= -12  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X=  -9  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X=  -6  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X=  -3  ~                           |           :          :           :bbbbbbbbbbbb           :          |                           ~
X=  +0  ~                    tray   |...4........................o===========o............................| tray                      ~
X=  +3  ~                           |           :          rrrrrrrrrrrr:           :           :          |                           ~
X=  +6  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X=  +9  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X= +12  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X= +15  ~                           |           :          rrrrrrrrrrrr:bbbbbbbbbbbb           :          |                           ~
X= +18  ~                           |           :          #rrrrrVrrrrr:bbbbbbbbbbb#           :          |                           ~
X= +21  ~                           |           :          rrrrrrrrrrrr:bbbbbxbbbbbb           :          |                           ~
X= +24  ~                           |@..................................bbbbbbbbbbbb.................BBBBB|                           ~
X= +27  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +30  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +33  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +36  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +39  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +42  ~                           |           :          :           :           :           :     BBBBB| p                         ~
X= +45  ~                           |   3       :          :           :           :           :     BBBBB| p                         ~
X= +48  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~|................................................................BBBBB|~~~~~~~~~~~~~~~~~~~~~~~~~~~~
X= +51                              |           :          :           :           :           :          |
X= +54                              |           :          :           :           :           :          |
X= +57                              |           :          :           :           :           :          |
X= +60                              |           :          :           :           :           :          |
X= +63                              |           :   2      :     1     :           :           :          |
X= +66                              |           :          :           :           :           :          |
X= +69                              |ggggggggggg:          :           :           @           :          |
X= +72                              |---------------------------------------------------------------------|
                                                  AUDIENCE WALL, X = +70.7   (audience sits below this line)
```
Reading the map: at the start red (left) fires DOWN-page from S1/S2 into `V`; blue fires UP-page into `A` from its far-wall starts (mirror of S1/S2 at the top right). After TIPs the `V`/`A` move to the other end of each HIVE.

Simplified side view (looking from the red wall toward +Y; X increases to the right = toward audience), red HIVE, state A:
```
 Z(in)
 65.7 |                          apex .
 58.3 |                    opening ctr o--> normal (audience, 30 deg up)   <- RED AUDIENCE CELL (UP)
 53.4 |                      floor edge '
 44.0 |            pivot O (X=0)
 36.8 | <--o opening ctr (normal: far wall, 30 deg down)    <- RED SCORING CELL (down)
 30.6 |    ' lowest corner (X=-17)
    0 +---------#----------------------+----------------------#--------------> X
            foot X=-19.5               0                   foot X=+19.5
```

---

## 13. Uncertainty summary
| Quantity | Typical +/- | Basis |
|---|---|---|
| Fixed elements (walls, tape, FLOWERS, frame) vs CAD | 0.1-0.3 | CAD extraction |
| Same at an event | ~1 in (manual 9.1), tape 0.125 vs seams, flower height 0.25, foot bars 0.5 | [M p63][FAC] |
| Up-opening heights | 0.5 (checklist 1.13) | [FAC] |
| CELL positions horizontally, tag positions in field frame | ~1 | calibration "tweaks", damper compression |
| HIVE tilt / tip rotation | 30.1 +/- 1.5 / 60.2 +/- 3 deg | [CAD][E] |
| Tag position within CELL | 0.3 | notch/reference-hole alignment [IFEAG 6.1] |
| Visibility angles | 2-3 deg | depends on camera height/position |
| Launch angles / speeds | angles 2 deg; speeds +10-20% for drag | vacuum model |
| Spill landing box | 10 in | physics model + one peer dataset |

---

## 14. Open questions / needs physical testing
1. Real practice-field size: if the team's practice field uses 24 in tiles or a different perimeter, measure wall-to-wall and the HIVE pivot location before tuning odometry/auto paths (CAD says 141.3 in).
2. Actual hive tilt and opening height on the team's practice HIVE (damper compression changes it a few tenths of an inch; checklist allows +/-0.5).
3. Ball flight with drag and spin: perforated POLLEN/NECTAR curve and slow; the vacuum launch table needs empirical correction per hood angle/flywheel speed; bounce-out behaviour off the back skin.
4. Shooter exit height and turret position on the robot (assumed 14 in at the robot centre) - recompute Section 9 with the real numbers.
5. Limelight 3A FOV and whether its pipeline can apply a point-of-interest offset in tag space (Limelight docs not reachable from here); otherwise use the per-tag offsets in Section 7.3.
6. Occlusion of tags by A-frame legs/BIOBUZZ panels from specific shooting spots (panels may be absent at some events).
7. Spill landing variability (count, speed, how far NECTAR rolls) - film a few TIPs on the practice field.
8. Official Q&A board was not readable without login; peer notes cite Q11, Q17, Q21 (not independently verified here).
9. Whether a tall turret/hood on the team's robot fits the under-HIVE lane (need actual robot top height and width at the top).

---

## 15. Sources
- **[M]** BIOBUZZ Competition Manual TU05 (2026-10-15), local copy `.../scratchpad/manual/manual.pdf`: Sec 9 p63-79 (Figs 9-1..9-17), Sec 10 p83-91 (Fig 10-2 staging, 10.5 scoring, Table 10-2), G304 p104 + Fig 11-1 p105, G402 p107, G409 p109, G417 p111-112, R102/R105 p122-123. Online: https://ftc-resources.firstinspires.org/ftc/game/manual
- **[CAD]** Official BIOBUZZ field STEP CAD (AP242, Onshape export 2026-09-09, zip v26-27.2): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step ; Onshape: https://cad.onshape.com/documents/a355e772e3d24813de7852ee/w/f106353168f1f92100b81259/e/95d1e1e442b4138cccaf2d73 . Parsed with my own reader (`.../scratchpad/scripts/steptree.py`, `steptree_v.py`, `tags.py`, `circles.py`, `fieldmodel.py`).
- **[PFR]** Playing Field Resources 2026-27: https://ftc-resources.firstinspires.org/ftc/archive/2027/field
- **[EFSG]** Event Field Setup Guide V1.0 (2026-09-12): https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide (sections 8.3-8.5 tape, 9 frame/hive, 10 flowers, 11 setup, 12 hive calibration)
- **[FAC]** Field Acceptance Checklist rev 26-27.2: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
- **[FMG]** Field Mitigation Guide 26-27.1 (2026-10-16): https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide
- **[IFEAG]** Initial Field Element Assembly Guide V1.0: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/initialfieldguide (parts list p5-8; Cell Assembly 6.1-6.12)
- **[ART]** AprilTag Production Art V1: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/apriltag-art
- **[SDK]** FtcRobotController v12.0 (released 2026-09-12) README: https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md ; samples (ConceptAprilTag.java, RobotAutoDriveToAprilTagOmni.java) at tag v12.0; Vision 12.0.0 AAR: https://repo1.maven.org/maven2/org/firstinspires/ftc/Vision/12.0.0/Vision-12.0.0.aar (class constants read with my own parser `.../scratchpad/scripts/classdump.py`)
- **[DOCS-FCS]** ftc-docs Field Coordinate System: https://ftc-docs.firstinspires.org/en/latest/game_specific_resources/field_coordinate_system/field-coordinate-system.html (source: github.com/FIRST-Tech-Challenge/ftcdocs, docs/source/game_specific_resources/field_coordinate_system/field-coordinate-system.rst)
- **[DOCS-CL]** ftc-docs AprilTag Clusters tech tip: https://ftc-docs.firstinspires.org/apriltag-clusters
- **[DOCS-ADV]** ftc-docs AprilTag detection values / reference frame: https://ftc-docs.firstinspires.org/apriltag-detection-values , https://ftc-docs.firstinspires.org/apriltag/vision_portal/apriltag_reference_frame/apriltag-reference-frame.html
- **[PEDRO]** Pedro Pathing coordinates: https://pedropathing.com/docs/pathing/reference/coordinates
- **[AM-LL]** Limelight 3A (AndyMark): https://ftc.andymark.com/products/limelight-3a
- **[R4]** Peer research note `.../scratchpad/research/R4-community-meta.md` (cites Mona Shores 19429/20245 measurements and https://github.com/Mona-Shores-FTC-Robotics/biobuzz ). Community code there (`HiveGeometry.java`) uses HIVE bottom 25.5 in and a 9.4 in up/down height delta; the manual/CAD values are 30.6 in and 14.3 in (tag rows) / 21.5 in (opening centres).
