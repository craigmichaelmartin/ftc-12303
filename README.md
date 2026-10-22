# FTC 12303 · BIOBUZZ (2026–27)

Robot code, strategy plan and playbook for FIRST Tech Challenge team 12303.

## Highlights

**Strategy**

- **Win with TIPS.** Each HIVE TIP is worth 20 points. In early-season matches where the tip counts differed,
  the alliance with more TIPS won 98% of the time.
- **AUTO: tip, leave, park.** Our start cell already holds 3 NECTAR, so our 4 preloads tip it. That's 28 points
  for us, or 36 and the SWARM ranking point with a partner that also leaves and parks. Stretch goal: a
  two-robot double tip.
- **TELEOP: the two-zone loop.** The up cell faces its own end wall and swaps ends after every TIP, and the
  spill lands where we just shot from. So: shoot, sweep the spill, cross to the other end, shoot again. Two
  loads of 4 make each TIP after the first.
- **Last minute:** keep tipping, fire whatever is on board at about 0:12, then both robots park. POLLEN only
  until NECTAR passes its tests.

**Robot and code**

- **[Pedro Pathing 3](https://pedropathing.com/docs/pathing/pedro3)** drives the AUTO paths, on
  **goBILDA Pinpoint** odometry with X/Y pods.
- **Limelight 3A AprilTags aim the turret** at the cell opening (each HIVE tag cluster marks the center of a cell
  opening). Position comes from odometry, because the tags move when the HIVE tips; the aim fuses the two.
- **Turret flywheel shooter** on a SWYFT mecanum drive. We keep the design and tune it: feeding gated on
  flywheel speed, a steel flywheel, more turret travel, ball sensors.
- **Never more than 4 balls.** The robot counts every ball, stops the intake at 4 and refuses NECTAR at the
  mouth (G407 and G408 have no momentary exception).
- **Driver + operator controls** with auto-aim, one-button volleys and rumble cues. One INIT menu sets
  alliance, start spot, AUTO routine, park slot and delay.
- **Laptop tests** sweep every AUTO route on both alliances for walls, FLOWERS, HIVE legs, the partner robot
  and the spill zone, and check ball handling and HIVE tracking.

## Start here

- **Loading the code onto the robot:** [README-12303, section 1](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md#1-getting-the-code-onto-the-robot)
- **Robot code guide** (configuration names, tuning order, controls, AUTO): [README-12303](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/README-12303.md)
- **Team plan:** [docs/plan/00-master-plan.md](docs/plan/00-master-plan.md) and the other files in [docs/plan/](docs/plan/)
- **Playbook page:** https://claude.ai/artifact/2GiUUCR3xXbCDsAw74ALuF (a copy is in [docs/playbook/](docs/playbook/))
- **Research behind the plan:** [docs/research/](docs/research/)

## Quick start

1. Install Android Studio (Narwhal 3 Feature Drop or newer).
2. `File > New > Project from Version Control`, URL `https://github.com/craigmichaelmartin/ftc-12303.git`.
3. Plug the laptop into the Control Hub's USB-C port, pick `TeamCode`, and press Run.

The Driver Station app must be version 12.0, and the Robot Configuration must use the names in
README-12303 section 2.

## Where things are

| Path | What it is |
|---|---|
| `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` | Our robot code. Settings live in `config/RobotConfig.java` |
| `TeamCode/src/test/` | Laptop tests: `./gradlew :TeamCode:testDebugUnitTest` |
| `docs/` | Team plan, playbook copy and research |
| Everything else | FIRST's FTC SDK 12.0 (FtcRobotController), which our code builds on. We only change `build.dependencies.gradle` and `TeamCode/build.gradle` |

## SDK updates

This repo is a GitHub fork of FIRST's [FtcRobotController](https://github.com/FIRST-Tech-Challenge/FtcRobotController).
Our code is on `main`; `master` mirrors FIRST's SDK. When FIRST releases an update:

1. On GitHub, switch to the `master` branch and click **Sync fork**.
2. Once per clone: `git config merge.ours.driver true` (this keeps our README when FIRST changes theirs).
3. `git checkout main`, `git pull`, `git merge origin/master`, then build, run the laptop tests and check the
   robot before pushing.

If git still reports a conflict in `README.md`, keep ours: `git checkout --ours README.md && git add README.md`.
