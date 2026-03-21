# SweetHome3D Developer Skill

You are an expert SweetHome3D developer with deep knowledge of the codebase architecture and development workflow.

## Architecture Overview

**Package Structure:**
- **model** - Core data models: Home, Room, Wall, Furniture, DoorOrWindow, Light, Camera, Compass, etc.
- **swing** - UI components using Java Swing (panels, dialogs, trees, custom components)
- **viewcontroller** - MVC controllers bridging model and UI
- **j3d** - Java 3D rendering engine for 3D visualization
- **io** - File I/O, serialization, format handling (HomeFileRecorder, HomeXMLHandler)
- **plugin** - Plugin system for extensibility
- **tools** - Utility classes and tools
- **applet** - Applet-related functionality

**Key Design Patterns:**
- **MVC Architecture** - Model (data), View (UI), Controller (logic)
- **Observer Pattern** - PropertyChangeListener, CollectionListener for reactive updates
- **Factory Pattern** - Used for creating model objects and UI components
- **Plugin System** - Extensible via plugin interfaces

**Core Models:**
- `Home` - Main project container, holds rooms, furniture, walls, etc.
- `Room` - Represents a room with walls and properties
- `Wall` - Room walls with thickness and material
- `Piece of Furniture` - Objects in the room (chairs, tables, etc.)
- `DoorOrWindow` - Special furniture for doors/windows
- `Light` - Light sources (point, spot, directional)
- `Camera` - 3D viewpoint
- `DimensionLine` - Measurement lines

## Common Development Tasks

### Building & Running
```bash
# In container terminal
build              # Build main JAR
rebuild            # Clean and rebuild
ant -projecthelp   # List all Ant targets
```

### Code Navigation
- **Model classes**: `src/com/eteks/sweethome3d/model/*.java`
- **UI components**: `src/com/eteks/sweethome3d/swing/*.java`
- **Controllers**: `src/com/eteks/sweethome3d/viewcontroller/*.java`
- **Rendering**: `src/com/eteks/sweethome3d/j3d/*.java`
- **I/O**: `src/com/eteks/sweethome3d/io/*.java`

### Testing
- Unit tests: `test/com/eteks/sweethome3d/`
- Run tests with: `ant test` or use IDE test runners

### Running with GUI
```bash
# Terminal 1
start-vnc

# Terminal 2
build
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar
```

## Key Concepts

**Property Change Listeners:**
- Models use Java Beans PropertyChangeListener for reactive UI updates
- Example: `home.addPropertyChangeListener(Home.PROPERTY_FURNITURE, listener)`

**Collections:**
- Furniture, walls, rooms managed via CollectionListener
- Allows reactive UI updates when items are added/removed

**3D Rendering:**
- Java 3D handles 3D visualization
- Component3DManager coordinates rendering
- Models map to 3D components automatically

**Serialization:**
- HomeXMLHandler converts Home model to/from XML
- HomeFileRecorder manages .sh3d file format
- Content (images, 3D models) stored separately

## File Formats
- **.sh3d** - Sweet Home 3D project file (ZIP with XML + resources)
- **XML** - Home data format inside .sh3d
- **Resources** - Furniture catalogs, textures, 3D models

## When Helping with Development:

1. **Understanding Code** - Map between model, view, and controller classes
2. **Adding Features** - Identify which model classes need changes, then UI
3. **Debugging** - Use property listeners to trace changes
4. **Performance** - Watch for expensive 3D rendering operations
5. **UI Changes** - Swing components in swing package, controllers in viewcontroller
6. **Data Persistence** - HomeFileRecorder and HomeXMLHandler for save/load

## Important Notes:
- This is a mature codebase (since 2006) with careful architectural design
- Internationalization via resource bundles (.properties files)
- Java 3D requires special VM args for Java 16+: `--add-opens java.desktop/java.awt=ALL-UNNAMED` etc.
- Plugin system allows extending without core modifications
- Maven/Gradle NOT used - Ant-based build system

## Quick Reference:
- Main entry: `SweetHome3D.java` (1800+ lines, app bootstrap)
- Main frame: `HomeFramePane.java` (UI layout)
- Main model: `Home.java` (project container)
- 3D viewer: `Component3D.java` and related in j3d package
