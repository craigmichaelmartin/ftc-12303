# R2 verification: Field geometry, AprilTags, start positions

Verifier pass, 2026-10-20. I re-derived each key fact from primary sources with my own tools. I did not reuse the R2 author's scripts.

## Method

- **Manual TU05:** text plus visual checks of Figs 9-2..9-17, 10-2, 10-6/10-7 and 11-1. Rendered p71 at 400 dpi and checked the Fig 9-10 dimension lines against the cell ribs.
- **Event Field Setup Guide (EFSG) V1.0:** sections 6-12 (text), pages 18 and 23 (images).
- **Other official field documents:** Field Acceptance Checklist (FAC) rev 26-27.2, Field Mitigation Guide (FMG) 26-27.1, and the Initial Field Element Assembly Guide (IFEAG) sections 6.1-6.12.
- **Official STEP CAD:** `downloads/field-cad-step/x/field-cad-step.step` (Onshape AP242, 2026-09-09). I parsed it with my own assembly walker, `scripts/r2v/stepwalk.py`, which follows NAUO, CDSR and ITEM_DEFINED_TRANSFORMATION chains and collects world-space vertex points and circles. Analysis scripts:
  - `scripts/r2v/hivecheck.py`
  - `scripts/r2v/flowercheck.py`
  - `scripts/r2v/elements.py`
  - `scripts/r2v/footbar.py`
  - `scripts/r2v/lowparts.py`
  - `scripts/r2v/openingcheck.py`
- **CAD to FTC frame:** X_ftc = Z_cad, Y_ftc = X_cad, Z_ftc = Y_cad. I derived this independently: red alliance-area tape is at CAD X < -71.65, the red garden tape is at CAD Z +68..70 (audience wall per FAC 3.5), and CAD Y is up. The mapping is a proper rotation.
- **SDK:** Vision-12.0.0.aar, re-downloaded from Maven Central into `downloads/r2v-mvn/`. SHA-1 f903fe33... matches Maven's published .sha1. 12.0.0 is the newest Vision version listed. I disassembled it with my own class-file reader, `scripts/r2v/jcls.py` (no JDK on the machine).
- **SDK README v12.0:** fetched from raw.githubusercontent.
- **Docs:** ftc-docs Field Coordinate System page and AprilTag Clusters tech tip.
- **Vendor and library docs:** Limelight docs (3A page and hardware comparison), AndyMark LL3A page, Pedro Pathing coordinates page.
- **Official Q&A:** the answers Atom feed from game-qa.firstinspires.org confirms that QA12/17/21/28 exist and are answered. The answer text comes from the board JSON another researcher saved from the same official site.
- **Opening-aperture model:** my own, `scripts/r2v/aperture.py`. The pentagon (manual Fig 9-11 inner dims) is in the 30-deg-tilted opening plane, projected along the ball direction and inset by the POLLEN radius of 1.4 in.

## Verdicts

| ID | Verdict | Key evidence / correction |
|---|---|---|
| F1 | confirmed | ftc-docs rule: origin at field centre on the tiles; standing at the Red Wall, +X is to the right and +Y points away from it. Inverted-square variant: "X axis points towards the audience". Manual p68: red ALLIANCE AREA is on the audience's left. The SDK DECODE library (same inverted layout) puts the goals at X = -58.37, the far side, which is consistent. The (X,Y,h) -> (-X,-Y,h+180) mapping holds because the field is 180-deg symmetric (F19). |
| F2 | confirmed | CAD: polycarbonate inner faces at +/-70.67 (bottom/top rails at +/-70.58-70.59), wall top 11.64, tile tooth-lines centred at 0, +/-23.5, +/-47.0, edge tiles to +/-70.58. Seam numbering checked against manual Fig 9-4. Manual 9.1: the CAD is the official representation (+/-1 in); p64 gives "approximately 144 in". |
| F3 | confirmed | IFEAG 6.7, 6.9, 6.10 and 6.12 quotes verified. My CAD parse of the up red AUDIENCE cell: Back Skin at X 1.70-9.10 (pivot end), outer rib X 11.32-20.06, ballast washers and wing nut at X 18.3-19.0 (outer end), Base Tube from X 4.93 (Z 43.95, pivot) to 19.94. Manual Fig 9-15: "Front of Cell" 9.938 in from the reference holes, with the arm tube running past the other end. |
| F4 | confirmed | Manual Fig 9-10 at 400 dpi: the "Bottom/Top of HIVE Opening" lines (53.5/65.6) attach to the OUTER rib. That rib is tilted 30 deg from vertical, and the 30 deg is drawn between the horizontal and the cell floor/arm. Manual p83: "the CELL which 'points at' a FLOWER should be the one tilted down" (red SCORING points at F-far, consistent). The CAD red and blue hives are exact mirrors, so the stable states are symmetric and a TIP is 2 x 30.1 deg. |
| F5 | confirmed | EFSG 11.1 text and figure; manual Fig 10-2 and Fig 9-17; CAD (red AUDIENCE and blue SCORING cells up, with NECTAR inside). |
| F6 | confirmed | CAD: floor front edge (19.27, Z 53.36); apex 65.50 (top skin) / 65.70 (rib front face). SDK origin recomputed from the CAD tag centres plus the SDK member offsets: (16.51, -12.74, 58.25). FAC 1.13: 53.5 +/- 0.5. The blue value is the mirror. |
| F7 | confirmed | CAD: down outer rib lowest point (-17.09, -13.47, 30.65); up-cell base tube 43.95; back rib 46.10; highest point 65.93. A -60.2 deg rotation of the opening centre gives (-20.63, 36.75). |
| F8 | confirmed | CAD bearing-circle centres: red Y -13.46..-12.03 at Z 43.950, axis along Y; blue Y 12.04..13.48. Rib extents: red Y -23.24..-2.24, blue 2.26..23.26 (centres -12.74 / +12.76). |
| F9 | corrected | Bytecode: `getCurrentGameTagLibrary()` = `Builder().addLibrary(getSampleTagLibrary()).addLibrary(getBioBuzzTagLibrary())`, so it ALSO contains the sample single tags (IDs 581-586). It is not identical to `getBioBuzzTagLibrary()`. Everything else is verified: 4 clusters with names and first IDs 30/34/38/42; members (+/-6.5, +/-2.75, 7.1874, -5.622); size 3.25; fieldPosition (0,0,0) INCH; identity quaternion; `getBioBuzzCluster(int)` private static; public constructors; `Builder.addCluster` public. processFrame calls `computeRobotPose(raw, cluster.fieldPosition, cluster.fieldOrientation, t)` unconditionally. Note: `clusterMembers` and the extents are package-private, so user code cannot read member offsets and must hard-code them. |
| F10 | confirmed | The SDK cluster object points (-s/2+x, s/2+y, z), (s/2+x, s/2+y, z), ... use the same corner order as the single-tag `poseFromTrapezoid` (AprilTag convention: x right, y down, z into the tag). Manual Fig 9-15: 9.938 - 2.75 = 7.188 = the SDK's 7.1874. README v12.0: "Origin of each cluster is located in the center of the Cell opening". The CAD origin lies in the outer-rib front-face plane (b = 21.45). |
| F11 | confirmed | I recomputed tag centres from the CAD sticker edges, e.g. 34-37 at (13.10, Z 49.79) and 30-33 at (-11.61, Z 35.53), and the state-B rotation (11.59, 35.49). I derived the quaternions from the axes: (0.683,-0.183,-0.183,0.683), (0.683,0.183,-0.183,-0.683), and state B (0.683,0.183,0.183,0.683). The convention matches the SDK ITD library (tag 12: q = (0.7071,-0.7071,0,0) at (0,70.25,5.75); tag 15 also checks). ID order matches EFSG 9.3. |
| F12 | confirmed | All floors are parallel (the cells sit on the same side of a straight arm), so n_vis = (+/-0.5, 0, -0.866) for all 8 tags of a HIVE. Elevations/ranges check out with the report's assumed 10 in camera height. ftc-docs: "an AprilTag Cluster on a scorable CELL is only visible to a robot in a scorable position". Limelight 3A V-FOV is 42 deg (Limelight docs), so a 40 deg pitch covers about 19-61 deg. |
| F13 | confirmed | ftc-docs heuristic (|roll|<90 plus alliance name). Rotation math: tag-row delta = 2*b*sin30 with b = 14.27 -> 14.27 in; opening centres 21.45 in. Mona Shores HiveGeometry.java uses HIVE_BOTTOM 25.5 (actually the hive centre-to-centre) and 18.84*sin30 = 9.42 (18.84 is the gap between cells), both wrong. Note: down-cell tags on the camera's side face away and are not detectable, so the docs heuristic rarely misfires in practice. |
| F14 | confirmed | EFSG 12.3 table (3N+1P, 2nd tossed: No Tip, necessary; 3N+2P, 3rd tossed: Tip, necessary; 0N+6P, 7th: No Tip; 0N+7P, 8th tossed: Tip). FAC 2.3-2.6. Caveats: "gently placed" tips are only "Preferred"; FAC 2.1/FMG item 2 say "subtle variations may occur". Calibration pieces are placed against the back skin. |
| F15 | unverifiable | Direction and exit edge are confirmed by geometry: the filled cell stays at the same end, its opening rotates to face outward and 30 deg down, and the CAD floor edge in the down state is at (-17.78, Z 31.96). Landing distance and timing rest on a physics model plus peer video analysis (Mona Shores), not primary sources. That peer data varies: issue #167 gives a 0.25-3.4 s dwell and "about 3 s" to tile contact, but the frame-stepped Saline M3 TIP shows a 3.27 s dwell + 2.20 s swing + 0.28 s = about 5.8 s. Treat ~3 s as optimistic and budget up to ~6 s. |
| F16 | confirmed | CAD flower axes at (-68.04,-23.39), (23.39,-68.04), (68.04,23.39), (-23.39,68.04). Top ring top Z 21.40; middle ring 3.90-5.25; bottom ring top 0.35 (retrieval gap 3.55, matching manual p72). Footprint 5.88 along the wall x 5.04 out; backstop 22.40-22.65; FAC 1.7 21.5 +/- 0.25. |
| F17 | corrected | CAD tape: X -46.60..-23.91, far tape edge at Y -59.10, i.e. 11.57 in from the glass (11.49 from the rail). But FAC 3.4 requires 11 +/- 0.125 in "from inside of field wall to furthest edge of tape". On an event field the LZ ends at Y ~ -59.67 (+/-0.125), so "Y -59.10 (11 +/- 0.125 deep)" is internally inconsistent by ~0.5 in. Location (A5, far end of the red wall, seams 4-5) and the blue mirror (F2) are confirmed. |
| F18 | confirmed | CAD: red garden tape X 68.10-70.10, Y -70.10..-47.41; 4 POLLEN at (69.30, -69.31/-66.42/-63.53/-60.64); blue at F6. Manual p84 places them in a line from the corner nearest the alliance area. Nuance: the manual defines the GARDEN by the tape's outside edge, and FAC 3.5 puts the tape against the audience-side edge of tile A1. At events it spans about X 68.6-70.6, not out to the glass. |
| F19 | confirmed | Flowers, LZs, gardens, hive start states and tag layout all map under (X,Y) -> (-X,-Y) (tag ID +8: 34 at (13.10,-19.24) <-> 42 at (-13.10,19.26)). A mirror across Y=0 fails (no flower at (-68,+23)). |
| F20 | confirmed | G304 A, C, D, E satisfied (box Y -21.75..-3.75, column C, touching the audience wall; F-aud is on the blue side; LZ is far away). Fig 11-1 shows a similar red start on the audience wall in column C. Geometry: 45.17 in; elevation 39.3, range 62.8, incidence 20.6 (10 in camera height assumed). |
| F21 | confirmed | S6 box Y -19.1..-1.1 is 1.35 in from F-far (Y -20.45); 45.27 in to (-16.48,-12.74); S7 52.78 in at 31.1 deg; state-A tags are backside from there. Caution: the 1.35 in clearance from a FLOWER (G304.D) is within the +/-1 in field tolerance (manual 9.1). |
| F22 | corrected | Legality and angles verified (S3 59.8 deg, S4 108.6 deg). But "must drive to Y ~ -12 before shooting" is over-strong for S3. With the 30-deg up-tilted opening, my aperture model keeps ~60% of the head-on effective area at 60 deg off-axis for 30-60 deg descent (about 78-82 vs 135 in^2). S3 is a harder, longer (56.6 in) shot, not a blocked one. S4 (109 deg) is effectively blocked except steep (>60 deg) lobs into a sliver (~11-31 in^2). |
| F23 | corrected | Not a hard 35-40 deg cone. The opening faces 30 deg up, so it is also open from above (a strip about 4-7 in deep in X). Effective area for a 2.8 in POLLEN, by off-axis angle psi and descent beta: about 135 in^2 head-on (beta 30-45); ~110 at 40 deg; ~80 at 60; ~47-59 at 75; ~27-45 from the pure side with 45-75 deg descent; ~0-31 from behind (beta > 60). Practical scoring is best within ~40 deg, but steep lobs from wide angles can enter. G417 example H says honest misses that hit the cell exterior are not strategic. The Mona Shores "wedges" sim is peer evidence only. |
| F24 | confirmed | Recomputed: from Z 14 to 58.27 (dz 1.1245 m), vz0 4.697 m/s. 20/30/40/50/60 in -> 77.3/71.3/65.7/60.5/55.9 deg; 4.82/4.96/5.15/5.39/5.67 m/s. Head-on aperture factor cos(beta-30): 0.866 level, 1.0 at 30 deg. The drag +10-20% is an unverified estimate. |
| F25 | confirmed | CAD: foot bars are 0.07 in sheet with vertices only at the ends; they rise to ~2.1 in only inside the feet. Feet X +/-17.20-19.48, Y +/-22.77-24.65, Z 0.07-2.13. Legs run (+/-18.6, +/-23.8, 0.5) -> (~0, +/-12.7, 41.1). Top bar Z 40.95-41.95. Omission: 1/4-20 wing nuts sit on the foot bars at X +/-16.0-17.1, Z up to 0.57 (elevator-bolt tops at 0.41), just inside each foot. |
| F26 | confirmed | 30.65 - 18 = 12.65; 30.65 - 29 = 1.65; leg inner face at 18 in ~ |Y| 18.5; the path lengths are plausible. Rule nuance: G402 does not forbid entering blue's side in AUTO. It forbids disrupting the opponent's AUTO (MAJOR FOUL) and calls crossing "risky". The 0.25 in "red-only lane" is a self-imposed constraint. G409 (Q17: any deflection is a violation, verbal warning unless strategic) and G417.E (contacting the HIVE) apply under the hive. |
| F27 | corrected | Panel position (Z 34.07-40.02, X +/-1.55..4.28, Y +/-15.06) and "may not be present" (manual p69) are confirmed. The occlusion claim is not: a line of sight from a robot camera to the far (down) cell's tags (Z ~35.5) passes below the panels' bottom (34.07) unless the camera is ~30+ in high, which is above the 29 in limit. The A-frame legs (at Z ~30, |X| ~5, |Y| ~15.5) are the likelier occluders of the outer far tags. |
| F28 | confirmed | CAD: red NECTAR (9.47, -20.94/-17.32/-13.70, 49.92), touching the back-skin plane. Flower POLLEN at Z 1.40/4.29/7.18/10.07. 8 red pre-loads at (-45.97..-26.37, -73.02) outside the wall. 5 red NECTAR at X -3.6..3.7, Y -73.5/-76.7. Manual Fig 10-2 and p84 agree. (The tray holds no POLLEN in CAD; EFSG 11.3 says its 4 POLLEN are reset spares.) |
| F29 | confirmed | Arithmetic checks (108.5 / 134.5 / 44.2 in). PARK overlap is 4.9 in using CAD tape, or 4.3 in with the FAC-compliant LZ edge; still PARK. LEAVE needs no wall contact at the end of AUTO (Q21 official answer). |
| F30 | corrected | Limelight docs (primary): LL3A is an OV5647 COLOR ROLLING SHUTTER, 640x480 @ 90 fps (color pipelines). AprilTag pipelines run 50 fps @ 640x480 or 20 fps @ 1280x960, both with 2x downscale. FOV is H 54.5 / V 42 deg (published, not assumed). Tag size at 640 px: about 32 px at 63 in and 22 px at 90 in, not 35/24, and the 2x-downscaled detector sees about half that. Detection at ~90 in is marginal at 640x480; use 1280x960 if latency allows. |
| F31 | confirmed | Pedro docs: `new Pose(y + 72, 72 - x, heading - Math.PI/2)`. In the BIOBUZZ frame the origin falls at the red/audience corner, +x toward blue, +y toward the far wall. |
| F32 | confirmed | G304 A-H (p104) and the Fig 11-1 note. The report omits G304.B ("not attached to, entangled with, or suspended from any FIELD element"), which is minor. R102 also says pre-loaded SCORING ELEMENTS may extend outside the 18 in cube. |

## Missed facts and risks

1. **Shared shooting half.** At the start, red shoots from the audience half and blue from the far half. After one alliance has made an odd number of TIPs while the other has made an even number, both up-openings face the same end. Both alliances then shoot from the same half, which brings congestion, robots in the shooting lanes, and pushing/defense risk. (Not in the report.)
2. **Hold fire during a TIP.** Manual p87 and Q12: a TIP scores only when the opposite damper contacts the frame, and LAUNCHING at a HIVE in motion or at the down CELL "may disrupt its movement and may result in the TIP not being achieved". Turret code should stop firing from threshold detection until the swing completes, then retarget to the opposite end.
3. **HIVE dwell.** Peer measurement: the HIVE can sit 0.25-3.4 s after the threshold piece before moving, then swing for 0.65-2.2 s. AUTO and spill-avoidance timers should allow about 6 s from the threshold piece to the end of the spill.
4. **Limelight 3A specifics.** H54.5/V42 FOV, rolling shutter (image skew while the turret or chassis slews), and AprilTag pipelines limited to 50 fps at 640x480 or 20 fps at 1280x960.
5. **Event-field LZ depth.** It is 11.0 +/- 0.125 from the wall (FAC 3.4), not the CAD's 11.57. The garden tape is against the tile edge (FAC 3.5). Tune PARK poses with margin.
6. **No-show pre-loads.** If a robot is a no-show, its 4 pre-load POLLEN are placed "in approximately the center of the LOADING ZONE against the perimeter wall" (manual p83, G304 note). That is extra POLLEN near the LZ.
7. **Pre-loads outside the cube.** R102: pre-loaded SCORING ELEMENTS may extend outside the 18 in start cube, and pre-loads may sit on the tiles touching the robot on its own side (10.3.4).
8. **Placement order.** If either alliance asks, robots are placed alternately: red 1, blue 1, red 2, blue 2 (10.3.4). S1 is a prime spot, so partners must agree on who uses it.
9. **G402 priority.** Each alliance has priority over field and scoring elements on its side in AUTO. Elements deflected across "likely not penalized". Official Q&A Q15/Q24 on "priority" and per-instance penalties are still unanswered (accepted, 2026-10-02/10-10). Watch for rulings.
10. **SDK details.** `getCurrentGameTagLibrary()` also includes sample tags 581-586. Cluster member offsets are not readable through the public API, so hard-code them.
11. **Frame occluders.** The A-frame legs, not the ACM panels, are the main occluders of far-cell tags. Wing nuts (0.57 in) stick up just inside each frame foot.
12. **Field tolerance.** Field dimensions carry +/-1 in tolerance (manual 9.1). S6's 1.35 in clearance from F-far risks a G304.D contact on some fields.
13. **Spill crossing sides.** Red's TIP spill band (Y about -28..+2) reaches the centre line, so some red spill can roll into blue's side during AUTO, where blue has priority (G402).
14. **Q&A answers.** Q21 (no LEAVE if touching any wall at the end of AUTO, even when parked) and Q28 (LEAVE counts even if a partner pushes you off the wall) are official answers. The report treated Q21 as unverified.

## Sources

- **Manual and field documents:**
  - Manual TU05: local `manual/manual.pdf`; p63-77, 83-91, 104-112, 122-123.
  - EFSG: https://ftc-resources.firstinspires.org/ftc/field/eventfieldguide
  - FAC: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-check
  - FMG: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-mitigation-guide
  - IFEAG: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/initialfieldguide
  - Field STEP CAD: https://ftc-resources.firstinspires.org/ftc/archive/2027/field/field-cad-step
- **SDK:**
  - Vision 12.0.0: https://repo1.maven.org/maven2/org/firstinspires/ftc/Vision/12.0.0/Vision-12.0.0.aar (sha1 verified)
  - README v12.0: https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/v12.0/README.md
- **ftc-docs:**
  - Field coordinate system: https://ftc-docs.firstinspires.org/en/latest/game_specific_resources/field_coordinate_system/field-coordinate-system.html
  - AprilTag clusters: https://ftc-docs.firstinspires.org/apriltag-clusters
- **Limelight:**
  - LL3A: https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a
  - Hardware comparison: https://docs.limelightvision.io/docs/docs-limelight/getting-started/hardware-comparison
  - AndyMark: https://ftc.andymark.com/products/limelight-3a
- **Pedro Pathing:** https://pedropathing.com/docs/pathing/reference/coordinates
- **Official Q&A feed:** https://game-qa.firstinspires.org/boards/2027/FTC/QA/answers.atom
- **Peer (unverified):** Mona Shores docs and code saved in `downloads/monashores/`.
