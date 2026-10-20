# R5 hardware report: adversarial fact-check

Checked 2026-10-20 against the TU05 manual (text and page images), the official FTC Q&A, FTC SDK 12.0.0 artifacts from Maven Central (parsed read-only, nothing executed), the SDK README, and manufacturer and documentation pages fetched directly.

My own downloads are in `scratchpad/downloads/r5v/` (mvn, readme, cd, trc, qa, brushland, pinpoint, rev). My scripts are in `scratchpad/scripts/r5verify/`:
- `page.py`: prints manual pages.
- `classann.py` and `cpstrings.py`: read Java class-file constants and annotations without running any code.
- `ballistic_check.py`: independent launch-speed calculation.
- `tagview_check.py`: independent HIVE tag geometry and camera-view model.
- `qa_answer.py`: pulls answer text out of the Q&A pages.

Verdicts: **confirmed**, **corrected** (partly wrong; corrected statement given), **refuted**, **unverifiable**.

## Summary

| ID | Verdict | One-line reason |
|---|---|---|
| F1 | confirmed | SWYFT product page matches every spec. R501 Table 12-1 lists SR-MOTOR-DC-01 (p.132). |
| F2 | corrected | SDK 12.0 has no "goBILDA 435 rpm" motor type and no SWYFT type. Its single "GoBILDA 5202/3/4 series" type is 2786 ticks/rev at 60 rpm (99.5:1). |
| F3 | confirmed | gm0 states the mecanum caveats. My own search also found no SWYFT V2 reviews. 4 × 9.2 A = 36.8 A. |
| F4 | corrected | The legality points are right, but R704.D is not limited to matches. QA26 (2026-10-19) says Wi-Fi streaming is prohibited anywhere in the venue, including pits and practice fields. R707 also allows a USB switch. |
| F5 | confirmed | Limelight 3A quick-start page and ServoCity listing ($189, currently "Sold Out"). |
| F6 | corrected | The qualitative geometry holds. Two numbers are off: down-cell cluster ≈35.5 in rather than 34.0, and the SDK cluster origin (aim point) is ≈58.3 in rather than 59.6. |
| F7 | confirmed | Reproduced independently. Pixel widths are about 10% high (pinhole model gives ≈19 px at 8 ft). Down-cell tags stay in view to ≈5.5 ft. |
| F8 | confirmed | SDK README v12.0 and the FTC Docs AprilTag-cluster page. |
| F9 | corrected | Community claims are quoted accurately, but "most teams" overgeneralises one thread. Limelight's own FTC docs mention a "default slot" for `uploadFieldmap`. |
| F10 | confirmed | Limelight FTC programming page, TRC guide, goBILDA camera page. |
| F11 | confirmed | TRC guide p.2 table and the Hugging Face model card (community-sourced numbers). |
| F12 | confirmed | goBILDA 5203-2402-0001 page. 22.65 W re-derived. |
| F13 | confirmed | My independent simulation agrees within ±0.1 m/s. |
| F14 | confirmed | Arithmetic reproduced. The inputs are assumptions. |
| F15 | confirmed | Manual p.74 (sizes and size variation) and AndyMark (masses). The design conclusion is inference. |
| F16 | confirmed | REV FGC PID page, SkyStone issue #241, and SDK 12.0 `LynxGetBulkInputDataResponse.velocities` is `short[]`. |
| F17 | confirmed | Arithmetic checks. goBILDA 312 rpm spec matches (that motor is currently "Sold Out"). |
| F18 | corrected | At 4–6:1, a 300° servo gives 50–75° of turret travel. With the SDK default 600–2400 µs range it gives only 45–68°. |
| F19 | confirmed | goBILDA servo pages, R502 (≤8 W and ≤4 A at 6 V, p.133), R504-C (p.134). |
| F20 | corrected | gm0 says top Rover Ruckus teams used "3.7:1 (1600 RPM)" or "5.2:1 (1000 RPM)" intakes, not 1150–1620 rpm. The "2× drive speed" rule is community advice, not gm0. |
| F21 | confirmed | REV Control Hub specs, REV Servo Hub page and docs, goBILDA injector page. |
| F22 | confirmed | SDK `PwmRange` constants are 600/2400/20000 µs. gm0 agrees. R612 (p.139). |
| F23 | confirmed | REV specs, R701/R503, FTC hub tips. The digital pull-up statement comes from gm0, not the cited REV page. |
| F24 | confirmed | Brushland docs ("3-4ms" bulk read, "upwards of 4 milliseconds" per I2C read) and gm0. The loop-time figure is the author's estimate. |
| F25 | confirmed | Table 12-4 (p.135), goBILDA and REV product pages. |
| F26 | confirmed | FTC hub-tips page, FTC ESD page, R605 Table 12-6 (p.137). |
| F27 | confirmed | goBILDA odometry and V2 pages, V1 user guide, SDK driver constants (address 49 = 0x31; pod resolutions 19.894 and 13.263 ticks/mm). |
| F28 | confirmed | Brushland docs and product page, REV page. Only pin 0 can output analog, and the sensor is currently out of stock. |
| F29 | corrected | G409 forbids catching **or deflecting**. QA17 rules that balls from a tipped HIVE bouncing off a robot are a violation (verbal warning if not STRATEGIC). |
| F30 | confirmed | SDK 12.0 Gamepad has the rumble/LED methods. goBILDA RGB light page. R202-J (p.126). |
| F31 | confirmed | REV TBE V2 product page and docs. Magnetic Limit Switch page. |
| F32 | confirmed | goBILDA Axon MAX MK2 and MINI MK2 pages. 7.25 W and 7.12 W re-derived. |
| F33 | confirmed | Photos reviewed. Battery label reads 3100-0012-0020. The tire print looks like "Ø72mm". |

## Detail per fact

**F1, confirmed.** swyftrobotics.com/kits/swyft-drive-v2:
- SR-DRIVE-02 is "86mm 40A / SWYFT Drive V2 Module Pack of 4", $389.99.
- 12.7:1 only, all-metal: 4140 steel pinion and planets, 6061 ring gear.
- Motor is the Spike SR-MOTOR-DC-01, a "550 Motor": 6000 RPM, 9.2 A max, 1.47 kg·cm.
- 6.4 ft/s; 575 g; "up to 40 lbs"; M4 holes front, M3 back.
- Manual Table 12-1 (p.132) lists "SWYFT Robotics SWYFT Spike Motor SR-MOTOR-DC-01".
- Free speed π·0.086 m·(6000/12.7)/60 = 2.13 m/s = 6.98 ft/s.

**F2, corrected.** Confirmed parts:
- Encoder "JST XH 4-Pin" and power "JST VH 2-Pin". REV encoder ports are JST-PH (REV spec), so an adapter is needed.
- The SWYFT sample reverses the right side, uses BRAKE, and multiplies strafe by 1.1.
- 28 × 12.7 = 355.6 counts per wheel rev; 33.4 counts/in.

SWYFT's page says "28 PPR". Reading that as 28 counts per motor rev matches goBILDA's convention (the SDK's goBILDA type uses 28 per motor rev, for example 2786 = 28 × 99.5). Still, verify it on the robot by turning a wheel one revolution.

What is wrong: SDK 12.0.0 (`com/qualcomm/hardware/motors`) has no per-ratio goBILDA types and no SWYFT type. The single built-in "GoBILDA 5202/3/4 series" type is annotated `ticksPerRev=2786.0, gearing=99.5, maxRPM=60.0`, giving 2786 ticks/s max.
- Choosing that type is reasonable: its max ticks/s is about 2786 versus the Spike's 2800.
- Do not call it "435 rpm".
- `getMotorType().getTicksPerRev()` returns 2786, not 355.6. So `setVelocity(rate, AngleUnit)` and `getVelocity(AngleUnit)` would be about 7.8× wrong for SWYFT wheels. Use the ticks-per-second APIs.

**F3, confirmed.**
- gm0 holonomic page:
  - "rollers ... forming an X shape";
  - lower friction and traction;
  - pushing power is "the primary disadvantage";
  - "can't strafe as fast as they can drive forward".
- Two searches of my own found no SWYFT Drive V2 reviews or failure reports. That is consistent with the claim but proves nothing.
- 4 × 9.2 A = 36.8 A against the 20 A ATM fuse (Table 12-4, R601-A). Fuse trip is time-dependent, so brief stalls will not necessarily blow it.

**F4, corrected.** Confirmed parts:
- Table 12-9 (p.140, checked visually) lists only "Limelight Vision Limelight 3A, LL_3A".
- R702 Example 6 (p.141) names the "LimeLight Vision Limelight 3G" as prohibited.
- The Limelight 4 is absent from the table.

R707 (p.143) allows "A. webcams and optical vision sensors per R708, B. USB hub **or USB switch**, and C. a REV Expansion Hub".

R704.D (p.141) prohibits third-party streaming tools ("FTC Dashboard, FTControl Panels, and others") and says "No continuous video stream is allowed". The rule text has no "during matches" limit. Official QA26 (answered 2026-10-19) says: "Wi-Fi streaming is not allowed anywhere in a competition venue (per R704.D), so Wi-Fi streaming (even in the pit and practice field) is prohibited."

So the report's "during matches" and "They are fine in practice" (§10) are wrong for any time at an event. This also undercuts §3.6's "re-tune at every venue" if tuning means viewing the Limelight web UI over the Control Hub's Wi-Fi. (Inference: tune over a direct USB-C tether to a laptop, or before the event.)

**F5, confirmed.** Limelight 3A quick-start page:
- "OV5647 Color Rolling Shutter (640x480 @ 90FPS)"; "H:54.5° V:42°".
- AprilTag "20fps @ 1280x960 (2x downscale), 50fps @ 640x480 (2x downscale)"; 90 fps colour.
- "does not support Google Coral"; 10 pipelines; 4.1–5.75 V over USB; 4 W max.
- "USBC to USBA cable ... Control Hub's USB 3.0 Port".
- Status light active "about 15-20 seconds" after connecting.

ServoCity lists SKU 3122-0002-0001 at $189.00, currently **Sold Out**.

**F6, corrected (numbers only).**

Confirmed:
- Tags sit on the CELL bottom face, "facing downward ... bottom edge oriented towards the center of the FIELD" (p.75).
- Both cells of a HIVE are oriented the same way (Fig 9-7/9-10 front views show both pentagons apex-up).
- The HIVE rests at 30° (Fig 9-10).
- Every tag on a HIVE therefore faces down and 30° toward that HIVE's up-cell side.
- From that side, down-cell tags appear inverted.
- A TIP swings all 8 tag normals to 30° toward the other half of the field.
- Fig 9-10 shows red tags but not blue tags from the audience side.
- FTC Docs agree: "unscorable CELLS will have AprilTag Clusters that are mostly upside-down" and "we're likely looking at the 'far' CELL that is pointed down".

The numbers:
- SDK 12.0 `AprilTagGameDatabase.getBioBuzzCluster` places the tags 7.1874 in behind the opening plane (= 9.938 − 2.75, Fig 9-15) and 5.622 in below the cluster origin. The tag x-positions are ±2.75 and ±6.5 in.
- 5.62 in is about the centroid of the 20 × 14 in pentagon (5.56 in).
- Combined with the manual's 53.5 in opening bottom, the SDK origin is about **58.3 in** high, not the 59.6 in mid-height. The up-cell cluster is ≈49.9 in, which agrees with the report.
- The report's down-cell **34.0 in** comes from solving the 53.5 in and 30.6 in heights as if they were the same edge. That implies a 45.8 in long HIVE, which contradicts Fig 9-9's 42.91 in.
- Using 42.91 in gives a down-cell cluster of ≈**35.6 in**. The 30.6 in "Bottom of HIVE" is then the frame's lowest point, about 1.4 in below the opening edge.
- None of this changes the report's conclusions.

**F7, confirmed (derived).** My model (`tagview_check.py`, lens at 12 in):
- At 35° pitch, the up-cell tags are in frame with 3° margin from D = 26 to 120 in. This matches exactly.
- Down-cell tags appear for D ≤ 66 in (≈5.5 ft; the report says ≈5 ft).
- At 40° pitch, down-cell tags appear for D ≤ 40 in and the up-tag range ends at 90 in. This matches "≈3 ft" and "≈7.5 ft".
- Obliquity is 42.9° at 10 ft and 45.6° at 12 ft. This matches.
- Pixel width: a pinhole model (f = 621 px) gives ≈19 px at 8 ft and ≈13 px at 12 ft. The report's 21 and 14 px come from a px-per-degree approximation, about 10% high.

**F8, confirmed.**
- README "Version 12.0 (20260907-090034)" Breaking Changes: clusters; "Full 6DOF pose can be estimated from a cluster even if only a single member tag is visible".
- "the Origin of each cluster is located in the center of the Cell opening".
- "Unfortunately, since BIOBUZZ AprilTags move, they are not suitable for absolute Field Localization."
- FTC Docs: "only a minimum of one of the tags needs to be detected"; "If the absolute value of the property is LESS than 90 ... the AprilTag Cluster is right-side up and the CELL is scorable."

**F9, corrected.**
- Chief Delphi topic 524110, post #2, says verbatim: "limelight only has one map slot and all 8 pipelines share it ... no way to filter the upside-down tags".
- SDK 12.0 `Limelight3A` has `uploadFieldmap(LLFieldMap, Integer)` → `boolean`, which POSTs to `/upload-fieldmap?index=`.
- Limelight's own FTC programming page shows `uploadFieldmap(fieldMap, null)` with the note "null means use the default slot". The REST docs say only "Optionally include an 'index' parameter" and leave it undefined. So the "one slot" claim comes only from the forum; the vendor's wording hints at slots.
- "Most teams use Pinpoint" overstates one thread (topic 524104). Several posters say this (#3, #4, #15, #21, #24), and others build vision localisation (#16, #22, #23, #29).
- The "within 1 inch" quote is post #8. It refers to last season's OTOS-versus-Pinpoint comparison and says "in most cases".

**F10, confirmed.**
- Limelight: "change its pipeline in a matter of milliseconds, but your code will not wait".
- TRC guide: "switching takes only a frame or two"; skip results where `getPipelineIndex() != slot`.
- TRC's floor mount is tilted down, covering about 6 in to 4 ft (sample `MOUNT_ANGLE_DEG = 25.0`).
- goBILDA 3122-0004-0001 is $74.99, OV9782, UVC, 70° FOV.
- Note: the SDK 12.0 `builtinwebcamcalibrations.xml` lists only goBILDA SKU 3122-0003-0001. Whether the global-shutter camera 3122-0004-0001 is covered is unverified, though goBILDA's page claims a calibration file loads automatically.

**F11, confirmed (community sources).**
- TRC guide table: Hue 18–35, Saturation 110–255, Value 100–255.
- Hugging Face card (FTC 8565): 52.8 ms (≈18.9 fps) versus 86.5 ms for SSD-MobileNetV2 300×300 on a 3A. Single class, unpublished dataset.

**F12, confirmed.**
- goBILDA page: $54.99; 0.25 A; 9.2 A; "1.47 kg.cm (20.45 oz-in)".
- "28 PPR at Output Shaft"; "3.5mm FH-MC Bullet Connectors"; "4-Pos JST XH"; 391 g.
- 0.25 × 0.1442 N·m × 628.3 rad/s = 22.65 W.

**F13, confirmed (derived).**
- My simulation (Cd 0.5, k = 0.0478/m for both balls, launch at 12–18 in, aim at 59.6 in, descent ≥10°): 4.86–5.18 m/s at 2 ft; 5.31–5.57 at 4 ft; 5.84–6.06 at 6 ft; 6.39–6.59 at 8 ft; 6.94–7.12 at 10 ft; 7.48–7.65 at 12 ft.
- Launch angles 52–76°, arrival descent 12–40°.
- Ideal exit at half the surface speed gives 2,580–4,060 rpm for a 72 mm wheel. With 70–90% efficiency that is ≈2,900–5,800 rpm, so "3,000–5,000" is a fair rough range.
- Aiming at the SDK origin (≈58.3 in) lowers the speeds slightly.

**F14, confirmed (derived; model inputs assumed).**
- At 4500 rpm with I = 1e-4: 11.1 J flywheel energy, POLLEN shot 2.24 J (10.7% drop), NECTAR shot 3.71 J (18.4%).
- With I = 4e-4: 2.5% and 4.3%.
- 0–70% spin-up: 0.52 s and 2.10 s (τm = I·ω/τs).
- The ×1.5 loss factor and the frictionless spin-up are assumptions.

**F15, confirmed.**
- Manual p.74: POLLEN "approximately 2.8 in", NECTAR "approximately 3.6 in"; "not perfectly spherical and may vary in size".
- AndyMark lists 0.055 lb and 0.091 lb but does not say which mass belongs to which ball. The larger ball being the heavier is the obvious mapping.
- The hood recommendation is sound inference.

**F16, confirmed.**
- REV FGC page: measure maxV with RUN_WITHOUT_ENCODER at power 1; F = 32767/maxV; P = 0.1F; I = 0.1P; D = 0; "only remembered for the duration of the OpMode".
- SkyStone #241: velocity is `int16`, so the 8192-count TBE overflows at 4 rev/s (240 rpm).
- SDK 12.0 still declares `velocities` as `short[]` and `encoders` as `int[]`.
- Nuance from the issue thread: the hub's internal loop reportedly uses 32-bit values, so the overflow affects reported `getVelocity()`.

**F17, confirmed (derived).**
- 360/(28×5) = 2.57°/count.
- atan(8.6/120) = 4.1°; atan(8.6/60) = 8.2°.
- goBILDA 5203-2402-0019: 312 rpm, 24.3 kg·cm, "537.7 PPR at the Output Shaft". 312/5 = 62.4 rpm; 360/2688.5 = 0.134°/count. The motor is currently "Sold Out".

**F18, corrected.**
- The photo does show a ≈60T aluminium gear on a grey servo meshing with the large ring gear. In the front photo the gear-and-servo assembly is being held by hand.
- 300°/4–6 = **50–75°**, not "60–75°".
- The SDK default 600–2400 µs gives only 1800 µs × 0.15°/µs = 270° of servo travel, so **45–68°** of turret travel unless `setPwmRange(500, 2500)` is used.
- 5-turn servo: 2000-0025-0502/0503, $49.99, 1800°, 0.90°/µs, 500–2500 µs.

**F19, confirmed.**
- 25-2: 17.2/21.6/25.2 kg·cm and 40/50/60 rpm; 2.0/2.5/3.0 A; $36.99.
- 25-3: 7.9/9.3/10.8 kg·cm and 90/115/145 rpm; 2.0/2.5/3.0 A; $36.99.
- R502 (p.133, visual): "≤ 8 watts @6V", "≤ 4 amps @6V"; 2.77 W and 2.75 W.
- Programmer 3102-0001-0001 is $12.99.
- R504-C allows "modification for continuous rotation".

**F20, corrected.**
- Servo surface-speed arithmetic is right, assuming direct drive. The photo shows a gear train, so the real ratio is unknown.
- gm0's wording is "it is highly recommended that teams dedicate at least one motor to their intake".
- The top-team figure is "3.7:1 (1600 RPM) motor or a 5.2:1 (1000 RPM) motor", from Rover Ruckus.
- "2× drive speed ≈13 ft/s" comes from a Chief Delphi thread, not gm0.
- The 5203 1150 rpm motor checks out: 5.2:1, 7.9 kg·cm, 145.1 PPR, ≈23 W by the R502-style formula.

**F21, confirmed.**
- REV Control Hub specs: each servo port pair shares 2 A; 5 A absolute maximum shared with the +5 V ports.
- REV Servo Hub REV-11-1855: $90, 6 V default (5–7.4 V "after future software updates"), 15 A total, about 3 A per pin, RC/DS app 10.0 required, RS485, ID 1–10. Per-channel current is "Not accessible thought the FTC SDK".
- goBILDA injector 3125-0001-0001: $69.99, 6 V at 24 A, outputs stay off until PWM above 500 µs.

**F22, confirmed.** SDK `PwmControl$PwmRange` constants are 600.0 and 2400.0 with a 20000.0 µs frame. gm0 confirms that `setPwmRange` extends this to 500–2500 µs. R612 names the "goBILDA Servo Travel Tuner" as prohibited.

**F23, confirmed.**
- Per hub: 4 motor, 4 encoder, 6 servo, 8 digital channels, 4 analog channels, 4 I2C buses.
- R701 (p.140): one Control Hub plus "no more than one additional REV Expansion Hub". R503 (p.134): 8 motors and 8 servos.
- FTC hub tips: "Port 0 shares an I2C bus with the IMU"; the two RS485 data ports are redundant.
- The digital pull-up behaviour is documented by gm0's SDK-sensors page, not by the REV digital page the report cites.

**F24, confirmed.**
- Brushland: "read every single non-i2c port on a hub in only 3-4ms" and "A standard I2C read on the control hub will take upwards of 4 milliseconds".
- gm0: MANUAL mode requires `clearBulkCache()` every loop.
- The 8–20 ms loop time is the author's estimate.

**F25, confirmed.**
- Both packs are in Table 12-4 (p.135).
- goBILDA: $64.99, 3000 mAh, "20A mini blade", 597 g.
- REV Slim: $60, 3000 mAh, 10-cell, 20 A ATM, 567 g, 113.5 × 90.5 × 23 mm, "Minimum no-load voltage: 9.0V".

**F26, confirmed.**
- FTC hub tips: "The USB 2.0 port shares the same USB bus as the internal Control Hub radio"; "USB C-to-C cables do not work properly with the Control Hub".
- FTC ESD page: ferrites, non-conductive mounting, air gap above 3/8 in, REV-31-1269, cameras on USB 3.0.
- R605 Table 12-6 lists REV-31-1269 and "Swyft Grounding Cable SR-Ground-01".
- REV-31-1269 is $5.50, 470 Ω.

**F27, confirmed.**
- goBILDA odometry page: Pinpoint V2 3110-0002-0002 $79.99; 4-Bar pod 3110-0001-0002 $99.99; 4-Bar pack 3203-3110-0002 $279.99.
- The V2 page itself states 1500 Hz and 256,000 events/s.
- V1 user guide: I²C 0x31; return to "within about 10mm (~0.5")".
- SDK 12.0 driver constants: 19.894 and 13.263 ticks/mm, plus the integer 49 (= 0x31).
- SWYFT pod: $64.99, 38 mm, 4096 PPR, giving 34.31 ticks/mm (derived). The page shows two SKUs: SR-WHEELTRACKER-01 and SR-ODOMETRY-LINEAR-01.

**F28, confirmed with caveats.**
- Brushland: "emulate the behavior of the Rev Color Sensor V3"; digital HSV thresholds with a max-distance option; analog modes.
- Specs: 940 nm, VCSEL, Class 1, 100 mm. That satisfies R710 A–C (p.143).
- Product page: $25.00, currently **out of stock**. Pin 0 can be analog or digital; pin 1 is digital only.
- The sensor's I2C address is settable; default 0x52.
- REV Color Sensor V3: $20.75, APDS-9151, fixed 0x52.

**F29, corrected.**
- G407, G408 and G417 are confirmed (pp.108–112).
- G409 reads "may not catch **or deflect**". QA17 (2026-10-13): "A ROBOT that drives under a HIVE and has SCORING ELEMENTS bounce and deflect off their ROBOT is in violation of G409", which is a verbal warning if not STRATEGIC.
- That the spill exits toward the launcher's side is a sound derivation: the former up-cell opening faces outward and 30° below horizontal after the tip.

**F30, confirmed.**
- SDK 12.0 Gamepad contains `rumble`, `rumbleBlips`, `runRumbleEffect`, `setLedColor`, `runLedEffect`.
- goBILDA 3118-0808-0002: $24.99, 5–15 V, PWM 500–2500 µs.
- R202-J (p.126): lighting flashing faster than 5 Hz "will invite additional scrutiny".
- R503 counts only R501/R502 actuators, and R607 treats the light as a CUSTOM CIRCUIT.

**F31, confirmed.**
- REV-11-3174: $48.00; 8192 counts/rev; "+/- 0.5 degree"; 10000 RPM.
- "only supports incremental encoder input through the motor encoder ports".
- REV-31-1462: $17.50, active-low, 10 mm top / 5 mm side.

**F32, confirmed.**
- MAX MK2 2004-0025-0002, $99.99: 34 kg·cm, 83 rpm, 4,000 mA at 6 V; 350°; 4th-wire analog. R502 power = 7.25 W.
- MINI MK2 2004-0025-0001, $99.99: 25 kg·cm, 111 rpm, 3,800 mA; 7.12 W.
- R502's box adds that stall current is judged "regardless of any user or VENDOR adjustable software limits". The MAX's 4.0 A is therefore legal only exactly at the limit.

**F33, confirmed.**
- Drive photo: "SWYFT" / "SWYFTROBOTICS.COM DRIVE V2", yellow XT30, black perforated plate. Steel versus anodised aluminium cannot be told from the photo.
- Both hubs are vertical in red printed holders.
- Battery label reads "12V NiMH Nested Batt…" and SKU 3100-0012-002x.
- Turret: large ring gear and ≈60T gear on a servo.
- Shooter: orange C-hood, single tired wheel with print that looks like "Ø72mm", and a coaxial motor with a red sleeve.
- Intake: two shafts of black compliant star wheels, coupled by two large meshing spur gears, so they counter-rotate.

## Important facts, risks or rules the report missed

1. **R704.D applies across the whole venue (QA26, 2026-10-19).** No Wi-Fi streaming in pits or practice fields. That rules out FTC Dashboard, Panels, the Pedro web tuner, and the Limelight video UI over the Control Hub's Wi-Fi at events. Tune Limelight pipelines before the event or over a wired USB tether (inference). The report says these tools are "fine in practice".
2. **There is no MOMENTARY exception for G407 or G408 (QA3, QA4, QA23).**
   - Any CONTROL of opponent NECTAR, for any duration, is a violation (verbal warning).
   - A design that routinely ingests and then auto-ejects opponent NECTAR "is likely to be perceived as STRATEGIC" (major foul plus yellow card).
   - So "colour-sense at the intake mouth and auto-eject" is risky. Sense before contact, or gate the intake by size or colour so NECTAR never enters.
3. **Deflection counts under G409 (QA17).** Spill falls toward the launching side. A robot shooting from about 2–4 ft that causes the TIP may get G409 calls when balls bounce off it. Top guards reduce catches but not deflection calls. Plan to shoot from beyond the spill zone or back away right after the tipping shot.
4. **TIP scoring (QA12).** A TIP scores only when the damper contacts the frame. Launching into a moving HIVE may disrupt the TIP. The asker's claim of "5 nectar or 8 pollen" was not confirmed by the answer.
5. **SDK motor types (SDK 12.0).** There is no SWYFT type and only a single goBILDA 5202/3/4 type (2786 ticks/rev, 60 rpm). Angle-unit velocity APIs will be wrong for SWYFT wheels.
6. **SDK cluster geometry.** The aim point is the pentagon centroid, about 58.3 in high. Tags are 7.19 in behind and 5.62 in below it.
7. **Supply risks (pages checked 2026-10-20):**
   - Brushland Color Rangefinder: out of stock.
   - ServoCity Limelight 3A: sold out.
   - goBILDA 5203 312 rpm: sold out.
   - AndyMark POLLEN: sold out (practice elements).
8. **Servo travel at the SDK default range.** 600–2400 µs cuts every 300° goBILDA servo to 270°. That affects the existing turret, hood and gate.
9. **Second camera through a USB hub.** A powered hub may be powered only by a hub's 5 V aux port or a legal COTS USB pack (R611). UVC cameras on such a hub are legal (QA1).
10. **Rotating turret and hood within expansion limits.** They must stay inside the 18 × 24 in horizontal limit at every point of travel (R105 / Fig 12-6, p.125) and fit the 18 in starting cube (R102).
