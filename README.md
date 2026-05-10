# CMS 902

## About

CMS 902 is a naval combat management system (CMS) prototype. It generates a synthetic tactical picture, renders it on a 
radar display, and persists each tick to a database. The name is a nod to Lockheed Martin's well-known CMS 330, with 902 
being Nova Scotia's area code.

I built it for two reasons:

- **To learn new software end-to-end.** JavaFX, Gradle's Kotlin DSL, JDBC against MariaDB, Docker, JUnit + Mockito, 
SonarCloud.
- **To apply what I know about the defence industry.** I wanted to take my domain knowledge, which is 
primarily managerial, and translate it to code through what tracks, scenarios, classifications, and a tactical picture 
might look like in a small CMS.

## Stack

- Java 22, JavaFX 22
- Gradle 9.3 (Kotlin DSL)
- MariaDB 11 for persistence
- JUnit 5 + Mockito 5
- Docker for the headless container
- SonarCloud - [project dashboard](https://sonarcloud.io/project/overview?id=natebriand_CMS-902)

## Architecture

Four packages, each one a layer:

- `com.cms902.simulation` - `SimulationEngine` generates tracks from the chosen scenario and ticks them forward on a 
daemon thread every 2 seconds. Time runs at 30x so movement is visible on the radar.
- `com.cms902.manager` - `TrackManager` holds the authoritative track list and fans updates out to the UI and 
persistence layers.
- `com.cms902.ui` - `RadarDisplay` (JavaFX Canvas) and `MainWindow` (scenario picker, Start/Stop, status bar).
- `com.cms902.persistence` - `TrackHistoryRepository` writes each tick to MariaDB. Optional as if no DB password is set 
it isn't registered and the app prints a note on startup.

Layers communicate strictly via listener interfaces, with a defensive copy on every cross-thread handoff. The data 
source is the intentionally swappable one: replacing `SimulationEngine` with a real sensor feed (an Arduino is the 
eventual plan) shouldn't require touching the UI or persistence code.

## Running It

### Run the app

Requires JDK 22. From the project root:

```bash
./gradlew run
```

The radar window opens immediately and the simulation defaults to PEACETIME. Use the dropdown on the left to pick a 
different scenario and click **Apply Scenario** to regenerate the tactical picture.

If you don't enable database persistence (see below) the app prints a note on startup and runs without persistence.

### Optional: Enable database persistence

Track history persists to MariaDB if you point the app at one. Easiest way is to run MariaDB in Docker:

1. **Start a MariaDB container:**

   ```bash
   docker run --name cms902-db \
     -e MYSQL_ROOT_PASSWORD=cms902pass \
     -e MYSQL_DATABASE=cms902 \
     -p 3307:3306 \
     -d mariadb:11
   ```

2. **Apply the schema.** Wait ~10 seconds for MariaDB to finish starting up, then run:

   ```bash
   docker exec -i cms902-db mariadb -u root -pcms902pass cms902 < schema.sql
   ```

3. **Set the password environment variable:**

   ```bash
   export CMS902_DB_PASSWORD=cms902pass
   ```

4. **Run the app:**

   ```bash
   ./gradlew run
   ```

Track snapshots will be inserted on every tick. To inspect them:

```bash
docker exec -it cms902-db mariadb -u root -pcms902pass cms902
```

Then run any SQL you like, e.g. `SELECT COUNT(*) FROM track_snapshots;`.

The app reads three environment variables at startup:

- `CMS902_DB_URL` (default: `jdbc:mariadb://localhost:3307/cms902`)
- `CMS902_DB_USER` (default: `root`)
- `CMS902_DB_PASSWORD` (no default - persistence disabled if unset)

### Optional: Run headlessly in Docker

A separate `HeadlessMain` entry point runs the simulation without the GUI, printing each tick to the console. 
Good for containerized deployments or environments without a display.

```bash
./gradlew jar
docker build -t cms-902 .
docker run --rm cms-902
```

The container runs for five ticks (~10 seconds) and then exits.