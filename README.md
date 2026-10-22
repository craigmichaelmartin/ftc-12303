# FTC 12303 · BIOBUZZ (2026–27)

Robot code, strategy plan and playbook for FIRST Tech Challenge team 12303.

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
