---
name: sweethome3d-dev
description: >
  Expert SweetHome3D developer context for this codebase. Use this skill whenever
  the user is working on SweetHome3D — asking about architecture, implementing
  features, debugging issues, navigating code, working on the CostEstimatorPlugin,
  running builds, using the dev container, or asking any question about the
  sweethome codebase. Even if the user just mentions "Wall", "Room", "plugin",
  "cost estimator", "build", "XML handler", or any SweetHome3D concept, activate
  this skill.
---

# SweetHome3D Developer Context

## Architecture: 4-Layer MVC

```
model/          Pure data (Home, Room, Wall, Furniture, Light, Camera…)
swing/          Java Swing UI (90+ panels, dialogs, trees)
viewcontroller/ Controllers bridging model ↔ UI
j3d/            Java 3D rendering engine
io/             File I/O, XML serialization (HomeXMLHandler, HomeFileRecorder)
plugin/         Plugin interfaces + CostEstimatorPlugin (custom, see below)
tools/          Utility classes
```

Key entry points:
- `src/com/eteks/sweethome3d/SweetHome3D.java` — main()
- `src/com/eteks/sweethome3d/swing/HomeFramePane.java` — main UI frame
- `src/com/eteks/sweethome3d/model/Home.java` — root data model

## Core Design Patterns

- **Observer**: `PropertyChangeListener` for property changes; `CollectionListener` for list changes (add/remove furniture, walls, rooms)
- **MVC**: Models fire events → Controllers react → UI updates. Never skip the controller layer.
- **Plugin**: Plugins extend `Plugin`, return `PluginAction[]` from `getActions()`. No core modification needed.
- **Serialization**: All model properties must be reflected in `HomeXMLHandler.java` (both read and write paths).

## Key Model Classes

| Class | Location | Role |
|---|---|---|
| `Home` | model/ | Root container — rooms, furniture, walls, camera |
| `Room` | model/ | Room polygon with floor/ceiling settings |
| `Wall` | model/ | Wall segment with thickness, material, `DrywallType` |
| `HomePieceOfFurniture` | model/ | Furniture instance |
| `HomeDoorOrWindow` | model/ | Door/window (subclass of furniture) |
| `HomeLight` | model/ | Light source |
| `Camera` | model/ | 3D viewpoint |
| `DimensionLine` | model/ | Measurement annotation |
| `HomeEnvironment` | model/ | Lighting/sky/background settings |

## Build System

The project uses **Gradle** (migrated from Ant). The `build.py` script in `.devcontainer/` wraps Gradle and handles running inside or outside the container.

### Outside the container (from host)
```bash
python .devcontainer/build.py build    # Build JAR (./gradlew build)
python .devcontainer/build.py rebuild  # Clean + build
python .devcontainer/build.py clean    # Clean artifacts
python .devcontainer/build.py run      # Build and run app
python .devcontainer/build.py shell    # Open container shell
python .devcontainer/build.py start    # Start container (docker compose up -d)
python .devcontainer/build.py stop     # Stop container
```

`build.py` auto-detects whether it's running inside the container (`/.dockerenv` exists) or on the host. Outside, it proxies through `docker compose -f .devcontainer/docker-compose.yml exec sweethome-dev`.

### Inside the container
The `.bashrc` aliases are available:
```bash
build      # ./gradlew build
rebuild    # ./gradlew clean build
clean      # ./gradlew clean
start-vnc  # Start VNC server for GUI testing
```

### Running with GUI (inside container)
```bash
# Terminal 1
start-vnc

# Terminal 2
build
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar
```

### Running tests
```bash
./gradlew test    # Run all tests
# Tests live in: test/com/eteks/sweethome3d/
```

## Dev Container

Defined in `.devcontainer/`:
- `devcontainer.json` — VS Code dev container config; service `sweethome-dev`, workspace `/workspace`
- `docker-compose.yml` — Container definition
- `Dockerfile` — Image (OpenJDK at `/opt/java/openjdk`)
- Ports forwarded: 5901 (VNC), 6000 (X11)
- Java 3D native libs: `lib/java3d-1.6/`
- Java 16+ needs `--add-opens` VM args (already handled in build config)

## File Format

`.sh3d` files are ZIP archives containing `Home.xml` + bundled resources (3D models, textures, images).

## Implementing a New Feature (Checklist)

1. Add property to model class (getter/setter, fire `PropertyChangeEvent`)
2. Update `HomeXMLHandler.java` — both the read handler (XML → model) and write path (model → XML)
3. Add UI control in the appropriate `swing/` panel
4. Update the controller in `viewcontroller/` to connect UI to model
5. Internationalize strings via `package.properties` (22+ language files)
6. Write/update tests in `test/`

### PropertyChangeEvent pattern
```java
// In model class — fire after every mutation
this.firePropertyChange(PROPERTY_FOO, oldValue, newValue);

// In UI — listen and update
model.addPropertyChangeListener(MyModel.PROPERTY_FOO, event -> {
    SwingUtilities.invokeLater(() -> updateUI((Type) event.getNewValue()));
});
```

### CollectionListener pattern
```java
home.addFurnitureListener(event -> {
    if (event.getType() == CollectionEvent.Type.ADD) { ... }
    else if (event.getType() == CollectionEvent.Type.DELETE) { ... }
});
```

## Common Pitfalls

- **Forget to fire PropertyChangeEvent** — UI stays stale, observers never notified
- **Forget to remove listeners** — Memory leaks when objects are disposed
- **Missing XML serialization** — New property silently lost on save/load
- **UI update off EDT** — Call `SwingUtilities.invokeLater()` for any Swing mutation from non-UI threads
- **Java 3D natives** — Must be on `java.library.path`; handled by run scripts but can bite manual invocations
- **Old Ant assumptions** — This project uses Gradle now, not Ant; `build.xml` may still exist but `./gradlew` is authoritative

---

## CostEstimatorPlugin

A custom plugin living at `src/com/eteks/sweethome3d/plugin/costestimator/`. It adds a cost estimation feature to the app via the plugin system.

### Class Map

| Class | Role |
|---|---|
| `CostEstimatorPlugin` | Plugin entry point — extends `Plugin`, returns `CostEstimatorAction` |
| `CostEstimatorAction` | `PluginAction` that launches the dialog |
| `CostEstimatorDialog` | Main UI dialog — shows cost report, lets user mark wet rooms |
| `CostRatesDialog` | Dialog for editing per-unit cost rates |
| `CostEstimatorPanel` | Panel embedded in the dialog |
| `CostCalculator` | **Pure logic** — takes `Home + CostRates + wetRoomIndices → CostReport` |
| `CostRates` | Value object holding all cost rates (USD, serializable) |
| `CostReport` | Result — array of `LineItem` + grand total |

### What CostCalculator Does

Calculates these line items from the `Home` model:

| Item | Source |
|---|---|
| Framing | `wall.getLength()` → linear feet × rate |
| Drywall | wall surface area (length × wallHeight × sides per `DrywallType`) × rate, −10% for openings |
| Paint | same surface as drywall × rate |
| Flooring | `room.getArea()` → sq ft × rate |
| Electrical (base) | room count × rate |
| Electrical (fixtures) | `HomeLight` count × rate |
| Plumbing (standard) | non-wet room count × rate |
| Plumbing (wet rooms) | wet room count × higher rate |
| Doors | `HomeDoorOrWindow` with no sashes × rate |
| Windows | `HomeDoorOrWindow` with sashes × rate |
| Furniture | sum of `piece.getPriceValueAddedTaxIncluded()` for non-door/non-light pieces |

`Wall.DrywallType` can be `BOTH_SIDES`, `SINGLE_SIDE`, or `NO_DRYWALL` — affects drywall and paint totals.

### Default Rates (CostRates)

```
Framing:           $15 / linear ft
Drywall:           $2.50 / sq ft
Paint:             $1.50 / sq ft
Flooring:          $5.00 / sq ft
Electrical base:   $150 / room
Electrical fixture:$100 / fixture
Plumbing:          $500 / room
Plumbing (wet):    $2500 / wet room
Per door:          $250
Per window:        $450
```

### Plugin Registration

`ApplicationPlugin.properties` at the package root registers the plugin with SweetHome3D:
```properties
name=Cost Estimator
class=com.eteks.sweethome3d.plugin.costestimator.CostEstimatorPlugin
```

The JAR is built separately and installed at `~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar`.

### Extending the Plugin

To add a new cost category:
1. Add rate fields to `CostRates` (getter/setter)
2. Add calculation logic to `CostCalculator.calculate()`
3. Add UI controls to `CostRatesDialog` for editing the new rates
4. Localize labels in `CostEstimatorPlugin.properties`

---

## Internationalization

String resources live in `package.properties` files per package (22+ languages). When adding new UI text:
- Add key to `src/com/eteks/sweethome3d/swing/package.properties` (English baseline)
- Follow existing key naming: `ClassName.propertyName`
- Access via `preferences.getLocalizedString(ClassName.class, "key")`
