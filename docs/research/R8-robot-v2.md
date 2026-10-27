# R8: The robot as built, from the coach's video (FTC 12303)

Recorded 2026-10-26 for the 12303 team and for everyone who keeps the plan and the code in step with the robot.

**What this covers.** What the coach's video of the robot shows, what the coach confirmed, what is still unknown, and what it
means for the plan and the code. [R5](R5-hardware.md) (hardware) and [R7](R7-robot-audit.md) (robot audit) describe the earlier turret
version of the robot; they stay as the record of that design.

**Inputs**
- The coach's 23 s video `VID_20261026_071756.mp4`, filmed around the robot on a table. Narration: "intake wheel, shooting wheel,
  carrier wheel". Frames are cited by time, e.g. **t 6.5**, in seconds. The video and its frames are not in the repo.
- The coach's answers, 2026-10-26.
- The robot code as written for this robot: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` (`config/RobotConfig.java`,
  `config/FieldConstants.java`, `subsystems/Aim.java`, `subsystems/Carrier.java`) and its guide `README-12303.md`.

**Evidence labels**
- **[V] VIDEO**: seen in the video (time cited).
- **[C] COACH**: stated by the coach.
- **[D] DERIVED**: arithmetic from the field CAD and the code's geometry.
- **[I] INFERENCE**: a guess to check on the robot.

**Orientation.** FRONT = the intake side. BACK = the shooter side. LEFT and RIGHT are the robot's own, facing forward. The video is hand-held and moves around the robot, so check every position on the robot before building on it.

---

## 0. Bottom line

A SWYFT mecanum robot with the **intake wheel at the front** and a **fixed shooting wheel at the top of the back**. Balls ride a clear
ramp into a tunnel. A **carrier wheel** above the tunnel holds them while it is stopped and pushes one at a time into the shooting
wheel when it spins. The shooter cannot turn, so **the robot aims by turning its back to the up CELL**. No ball sensors are fitted,
and how many balls the tunnel holds has not been tested.

---

## 1. What the video shows

| Part | What is visible | Frames | Label |
|---|---|---|---|
| Drive | Mecanum wheel modules with their motors beside the wheels, seen from underneath at the front corners | t 21.0, 22.5 | [V]; SWYFT Drive V2 [C] |
| Intake wheel | A full-width shaft carrying four groups of black compliant star wheels, hanging from a horizontal bar at the front. The arm is hinged at that bar (hinges at the top of the side plates) | t 0.5, 2.0, 4.0, 19.5 | [V] |
| Intake drive | A large gear/pulley at one end of the arm turns the shaft. A goBILDA Yellow Jacket motor (yellow can) sits beside the roller shaft | gear: t 2.0, 19.5, 22.5; motor: t 21.0, 22.5 | [V]; DC motor [C] |
| Ramp | A clear, perforated polycarbonate U-channel under the front bar leads the balls in and up | t 21.0, 22.5 | [V] |
| Fingers | Two black fingers hang from a cross shaft at the tunnel entrance, just above a POLLEN | t 21.0, 22.5 | [V]; passive as far as the coach knows [C] |
| Tunnel | The channel between two aluminium U-channels; a POLLEN sits in it | top views t 9.0, 16.5 | [V] |
| Carrier wheel | A black treaded wheel above the tunnel, over the POLLEN ("carrier wheel" in the narration) | t 9.0, 10.5, 16.5 | [V] |
| Shooting wheel | One wheel on a cross shaft at the top rear, beside a red-sleeved motor ("shooting wheel" in the narration) | t 0.5, 6.5, 7.5 | [V] |
| Near the shooter motor | A white servo with a yellow label, and a yellow goBILDA motor can | t 6.5, 9.0, 15.0 | [V] |
| Hubs | REV Control Hub and Expansion Hub side by side on a top rail (a black channel), in red printed brackets | t 12.0, 13.5, 15.0, 16.5 | [V] |
| Black L-shaped plate | A large black plate stands upright at the end of the hub rail (the left side, toward the back) | t 0.5, 2.0, 4.0, 16.5, 18.0 | [V]; purpose unknown |
| Not seen | Battery, main power switch, Limelight, odometry pods, robot signs | – | [V] (the Limelight 3A and the Pinpoint with X/Y pods are on the robot [C]) |

---

## 2. What the coach confirmed

- The drive is the SWYFT Drive V2, with the goBILDA Pinpoint and the X/Y odometry pods.
- The intake wheel is driven by a **DC motor** (goBILDA Yellow Jacket). Its arm **floats**: it is hinged and has no actuator.
- The **carrier wheel is driven by a DC motor and it is the gate**: the balls wait until the carrier spins to feed one into the
  shooter. He is not sure whether the white servo next to the shooter motor drives it instead.
- The **shooter is fixed** and shoots **out of the back**, opposite the intake. The ring gear, the turret servo and the orange printed
  hood are gone. There is no hood servo and no feeder-gate servo.
- The Limelight 3A is on the robot; which way it faces was not decided.
- **No ball sensors**: no colour/distance sensor at the mouth, no "4 in the tunnel" sensor, no feed break-beam.
- **Not tested**: how many balls fit in the tunnel before the intake can't take another; NECTAR through this ball path.

---

## 3. Unknowns (measure or ask)

| # | Unknown | Why it matters | How to find out |
|---|---|---|---|
| 1 | **What the black L-shaped plate is for** | Not known, so the plan assumes nothing about it. It stands at the robot's edge, so include it in the size check (R102, R105) | Ask the coach |
| 2 | What drives the carrier wheel: a DC motor (the yellow motor can by the shooter is a candidate [I]) or the white servo | The Robot Configuration type: `carrier` is a Motor, or a Continuous Rotation Servo | Follow its wires; Check: Sensors B / X |
| 3 | How many POLLEN the tunnel holds | G407: without a mouth sensor nothing else stops a 5th ball | [07 Exp 0a](../plan/07-testing-and-practice.md#exp-0--tunnel-capacity-and-carrier-feed-do-these-first); add a stop if a 5th fits |
| 4 | The shooting-wheel motor (label) and its drive (on the motor shaft, geared or belted) | `FLYWHEEL_TICKS_PER_REV` = 28 × motor turns per wheel turn; every rpm the code shows depends on it | Read the label; mark the wheel and count motor turns |
| 5 | The shooting wheel's size and type, and where the ball leaves it | Shot distances are measured from the exit point: `SHOOTER_FORWARD_IN` / `SHOOTER_LEFT_IN` (placeholders −7 / 0) | Tape measure from the robot centre |
| 6 | The fixed exit angle and the speed ratio k | The shot-table seeds and the 30–92 in band come from a model of a 72 mm wheel at ~65° with k = 0.40 | [07 Exp 2a](../plan/07-testing-and-practice.md#2a-exit-angle-exit-point-and-speed-ratio-k-do-this-first) (240 fps video) |
| 7 | What the two black fingers do (passive? one-way?) | Whether balls can roll back out of the tunnel; whether NECTAR fits past them | Look, then test in Exp 0a and Exp 6 |
| 8 | How long one carrier push takes | `CARRIER_FEED_MS`: long enough for one ball, too short for two | [07 Exp 0b](../plan/07-testing-and-practice.md#0b-carrier-feed-timing) (Tune: Flywheel timed push) |
| 9 | Intake motor speed and gearing, star-wheel size, how far the arm swings | Pickup speed; size at every point of the arm's swing (R105) | Label, count teeth, measure |
| 10 | Limelight position, height and pitch | `CAMERA_FORWARD_IN`, `CAMERA_LEFT_IN`, `CAMERA_HEIGHT_IN`, `CAMERA_PITCH_DEG`, `CAMERA_YAW_OFFSET_DEG` | Mount it looking out of the back, pitched up ~35–40°; measure; Check: Limelight |
| 11 | Battery location and mount, main power switch, robot signs | Inspection (R603, R401–R403); a loose battery can get the robot DISABLED (G412.B) | Look; fit the switch and signs if missing |
| 12 | Robot height and footprint with the arm up and down | R102 (18 in cube at the start), R105, the under-HIVE lane | Measure |
| 13 | NECTAR through the ramp, fingers, tunnel, carrier and shooting wheel | Phase-3 options | [07 Exp 6](../plan/07-testing-and-practice.md#exp-6--nectar-go--no-go) |

---

## 4. What it means for the plan and the code

**Aiming.** The shooter points out of the back (`RobotConfig.SHOOTER_FACING_DEG = 180`), so the robot aims by turning:
- TELEOP: the driver holds RB (aim-assist) and the robot turns until its back points at the target CELL; the left stick still drives.
  The operator's d-pad left/right trims the aim by ±0.5° (left = the ball goes further left, seen from the shooter looking toward
  the cell).
- AUTO: every shooting pose already faces the CELL, and during each volley, whenever the heading is more than half the aim
  tolerance off, the robot turns itself onto the exact aim heading (operator trim and vision bias included; a new hold pose at most
  every 0.4 s), stepping off a wall first if it touches one.
- A ball is fed only with the heading inside the **aim tolerance**: half the heading error at which a POLLEN would touch the edge of
  the 20 in opening, seen from the robot centre (the robot turns about its centre, so that distance is the lever arm), kept
  between 1.5° and 4°. With shot distances measured from where the ball leaves (below): 4.0° (the cap) for every straight-on shot
  up to about 54 in and at every AUTO shooting pose, 3.2° for a 70 in shot straight on, 2.5° for a 92 in shot straight on, and
  1.8° for a 92 in shot 40° off-axis [D]. The 75° off-axis gate stays.

**Placement.** Every start spot puts the front (intake) against the wall and the back into the field. Red S1 is (61.67, −12.75),
heading 0: front on the audience wall, back toward the HIVE. Blue S1 is (−61.67, 12.75), heading 180, on the far wall.
[02 Poses](../plan/02-autonomous.md#poses-red) has every pose. A3 role 2
starts on S6 only: there it is 3.3° off its aim, inside the tolerance; from S7 it would have to turn ~31° with its front against
the far wall, so S7 is an invalid start for it (the robot does not move, and INIT says to use S6) [D].

**Shot distances.** The code measures every shot distance (the AIM telemetry line, the shot table, the 30–92 in band) from where the
ball leaves the robot once it is aimed, so it does not change while the robot turns. The ball leaves the shooter about 7 in behind the
robot centre (placeholder `SHOOTER_FORWARD_IN` = −7), so each shot is about 7 in shorter than the centre distance [D]:

| Spot | From the robot centre | Shot distance, from where the ball leaves (placeholder) |
|---|---|---|
| S1 / S6 | 45.2 / 45.3 in | 38.2 / 38.3 in |
| Zone A side spot (55, −37) | 45.5 in | 38.5 in |
| A3 role-1 spot (−52, −30) | 39.5 in | 32.5 in |
| B2 (−48, −12.75), A2's second volley | 31.5 in | **24.5 in**, below the 30–92 in band (telemetry warns; the route test checks it against a 24 in floor) |

- In Zone A the longest head-on shot is about 38 in, with the robot's front touching the audience wall [D]; longer shots need an
  off-axis spot.
- B2 stays at (−48, −12.75) until the shot map ([07 Exp 2b](../plan/07-testing-and-practice.md#2b-shot-map-builds-the-distance--rpm-table))
  decides it. If the short shot misses, move B2 back toward (−55, −12.75) (≈ 31.5 in): that spot passes every route test (front
  edge 6.7 in off the far wall, 4 in outside the TIP-2 landing box) but is only ≈ 1.6 in from F-far, and the approach drives toward
  it, so an overshoot touches it [D]. If the measured exit point is more than 7.5 in behind the centre, B2 falls under the route
  test's 24 in floor and the test fails: re-decide B2 then.

**Camera.** The Limelight is fixed to the chassis and looks out of the back, the same way as the shooter (`CAMERA_YAW_OFFSET_DEG =
180`). It sees the target CELL's tags while the robot aims; on S1 (back toward the HIVE) it sees the start CELL's tags. The vision bias
is added to the aim heading, so it also corrects odometry heading error; it is learned only while the chassis is still.

**Ball handling.** The carrier wheel is the gate and the feeder (`carrier`: a DC motor or a CR servo). For each ball it spins until
the shot is confirmed (a flywheel speed dip, or the feed beam), then stops at once, or stops after `CARRIER_FEED_MS` = 0.4 s (set
with the timed push in Tune: Flywheel, which always runs exactly that long). An unconfirmed push waits 0.25 s more, and the next push
comes at least 0.15 s later, once the flywheel is back at speed and the aim is good (whichever takes longer): 0.8 s per unconfirmed
ball, about 3.05 s for a 4-ball volley with no confirmations. While the intake pulls balls in, the carrier stays stopped and holds
them. Eject (operator LB) runs the carrier and the intake backward.

**Possession (G407/G408).** Without a mouth sensor the robot cannot count balls coming in or refuse NECTAR. INIT shows a loud warning;
the count starts at the preloads and drops by one per feed, confirmed or not; the intake refuses only while the count says 4, so
after a volley RT never stops by itself (only the mouth or slot-4 sensor stops it at 4); a TELEOP RB volley feeds 4 times; the
drivers stop intaking at 4 by eye and steer around NECTAR; an eject does not lower the count, so the operator presses back once per
ball ejected. The tunnel must therefore hold no more than 4. The ball sensors are a MUST refinement
([05 §2](../plan/05-robot-tuning.md#2-must-rules-and-inspection)), and tunnel capacity is the first experiment.

**TELEOP loop: one turn per cycle.** After a volley the spill lands behind the robot, between it and the HIVE. Once it has landed, the
robot turns around once, sweeps the spill intake-first, keeps driving to the other zone, and arrives with its back already facing the
new up CELL ([03](../plan/03-teleop.md#the-two-zone-loop)).

**Actuators and inspection.** 7 motors (4 drive, shooting wheel, intake, carrier) of the 8 that R503 allows, and no servo is needed;
if the white servo drives the carrier, it is 6 motors and 1 servo. The main power switch and the robot signs were not seen. The floating
arm must stay inside the size limits at every point of its swing (R105).

**Code.** `subsystems/Aim` (the aim heading, the distance from where the ball leaves, the tolerance, aim-assist) and
`subsystems/Carrier` (count, optional sensors, carrier) hold the aiming and the ball handling. The 69 laptop tests cover the aiming,
the carrier and every AUTO route with the front-to-wall starts.

---

## Sources

- The coach's video `VID_20261026_071756.mp4` and his answers, 2026-10-26 (not in the repo).
- Robot code: `config/RobotConfig.java` §§ 1, 4, 6–10; `config/FieldConstants.java` (start spots, shooting spots, `CELL_OPENING_WIDTH_IN`,
  `POLLEN_RADIUS_IN`); `subsystems/Aim.java`; `subsystems/Carrier.java`; `README-12303.md` §§ 2, 5, 7, 8.
- Field geometry: [R2](R2-field-geometry.md) and the official field CAD (aim points, walls, FLOWERS, landing boxes).
- Shot-band model: [R3](R3-launch-physics.md) (72 mm wheel, ~65° exit, k = 0.40).
- The earlier turret design: [R5](R5-hardware.md), [R7](R7-robot-audit.md) and [R7's fact-check](R7-robot-audit.verify.md).
- Plan pages that use this record: [05 robot refinements](../plan/05-robot-tuning.md), [07 experiments](../plan/07-testing-and-practice.md),
  [02 AUTO poses](../plan/02-autonomous.md#poses-red), the [strategy brief](strategy-brief.md) decision record.
