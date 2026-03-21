# SweetHome3D Developer Guide

A practical guide for contributing to and modifying SweetHome3D.

## Quick Setup

```bash
# Open in VS Code and select "Reopen in Container"
# Then build
build

# Run with GUI
start-vnc          # Terminal 1
export DISPLAY=:1 && java -jar install/SweetHome3D-*.jar  # Terminal 2
```

## Project Structure

```
src/com/eteks/sweethome3d/
├── model/              # Data models (Home, Room, Wall, Furniture, etc.)
├── swing/              # Swing UI components and dialogs
├── viewcontroller/     # MVC controllers (bridge between model & UI)
├── j3d/               # Java 3D rendering engine
├── io/                # File I/O, serialization
├── plugin/            # Plugin system
├── applet/            # Applet support
├── tools/             # Utility classes
├── SweetHome3D.java   # Main application entry point
└── HomeFramePane.java # Main UI frame layout
```

## Understanding the Architecture

### Model Layer
Core data structures in `model/` package:

**Home** - The project container
```
Home
├── List<Room> rooms
├── List<HomePieceOfFurniture> furniture
├── List<Wall> walls
├── List<DimensionLine> dimensionLines
├── List<HomeDoorOrWindow> doorsAndWindows
├── List<HomeLight> lights
├── List<Camera> cameras
└── HomeEnvironment (lighting, background, sky)
```

**Key Model Classes:**
- `Room.java` - Rooms with walls
- `Wall.java` - Walls with materials/thickness
- `CatalogPieceOfFurniture.java` - Furniture definition in catalog
- `HomePieceOfFurniture.java` - Furniture instance in a home
- `HomeDoorOrWindow.java` - Doors/windows (specialized furniture)
- `HomeLight.java` - Light sources
- `Camera.java` - Viewpoint for 3D scene
- `DimensionLine.java` - Measurement annotations

### View Layer
UI components in `swing/` package:

**Key UI Components:**
- `FurnitureCatalogTree.java` - Furniture catalog tree panel
- `FurnitureCatalogListPanel.java` - Furniture list view
- `PlanComponent.java` - 2D floor plan view
- `CompassPanel.java` - 2D compass
- `HomePane.java` - Main view splitter
- `Component3DTransferHandler.java` - 3D view and drag-drop

### Controller Layer
Controllers in `viewcontroller/` package:

**Key Controllers:**
- `HomeController.java` - Main application controller
- `PlanController.java` - 2D plan editing
- `HomeViewController.java` - 3D view controller
- `FurnitureController.java` - Furniture management

### Rendering
Java 3D in `j3d/` package:

- `Component3D.java` - 3D viewport
- `Component3DManager.java` - 3D scene management
- `Object3D.java` - Base 3D object
- `Room3D.java` - 3D room rendering
- `Furniture3D.java` - 3D furniture rendering

### I/O & Persistence
File handling in `io/` package:

- `HomeFileRecorder.java` - Save/load .sh3d files
- `HomeXMLHandler.java` - XML serialization
- `DefaultFurnitureCatalog.java` - Furniture catalog loading
- `DefaultTexturesCatalog.java` - Texture catalog

## Common Development Tasks

### Adding a New Feature to Furniture

1. **Extend Model** - Add property to furniture class (e.g., `CatalogPieceOfFurniture.java`)
   ```java
   private boolean myNewFeature;

   public boolean isMyNewFeature() { return myNewFeature; }
   public void setMyNewFeature(boolean value) {
       this.myNewFeature = value;
       firePropertyChange("MY_NEW_FEATURE", ...);
   }
   ```

2. **Update UI** - Add control in `swing/FurniturePanel.java` or related
3. **Implement 3D** - Update `Furniture3D.java` if it affects rendering
4. **Persist** - Update XML handler in `io/HomeXMLHandler.java`
5. **Test** - Add test case in `test/` directory

### Modifying the 2D Plan View

1. Locate `PlanComponent.java` in swing package
2. Paint operations typically in `paintComponent()` method
3. Mouse handling in `PlanMouseListener`
4. Call `PlanController` for model changes

### Adding 3D Rendering Feature

1. Update furniture model if needed
2. Modify `Furniture3D.java` to create 3D geometry
3. Test with `start-vnc` and GUI
4. Check Java 3D initialization in `j3d/Component3DManager.java`

## Using the Observer Pattern

SweetHome3D heavily uses PropertyChangeListener and CollectionListener:

```java
// Listen to furniture changes
home.addPropertyChangeListener(Home.PROPERTY_FURNITURE, event -> {
    List<HomePieceOfFurniture> furniture = (List<HomePieceOfFurniture>) event.getNewValue();
    // Update UI
});

// Listen to individual furniture changes
furniture.addPropertyChangeListener(HomePieceOfFurniture.PROPERTY_X, event -> {
    float newX = (float) event.getNewValue();
    // React to position change
});

// Listen to collection changes
home.getFurniture().addCollectionListener(new CollectionListener() {
    public void collectionChanged(CollectionEvent event) {
        if (event.getType() == CollectionEvent.Type.ADD) {
            // Furniture added
        }
    }
});
```

## Building & Testing

```bash
# Build main JAR
build

# Full clean rebuild
rebuild

# Run tests
ant test

# Build documentation
ant javadoc

# See all targets
ant -projecthelp
```

## Debugging

**In VS Code:**
1. Set breakpoints in code
2. Run with Java debugger attached
3. VS Code extensions installed: `vscjava.vscode-java-debug`

**Console Output:**
```bash
# Run with output
java -jar install/SweetHome3D-*.jar
```

## Code Style & Conventions

- **Naming**: CamelCase for classes, camelCase for variables
- **Properties**: Use PROPERTY_* constants for PropertyChangeListener events
- **Listeners**: Add/remove listeners symmetrically
- **Memory**: Proper cleanup in destructors
- **i18n**: Use resource bundles for strings

## File Format (.sh3d)

The .sh3d format is a ZIP file containing:
```
project.sh3d
├── Home.xml          # Project data
├── furniture/        # 3D model files
├── textures/         # Texture images
└── ...
```

HomeXMLHandler handles serialization.

## Useful Resources

- **Java 3D**: lib/java3d-1.6/
- **Catalogs**: Default furniture/texture catalogs in io/
- **i18n**: package_*.properties files in main package
- **Tests**: test/com/eteks/sweethome3d/

## Tips

1. **Understanding flow**: Trace from `SweetHome3D.main()` through `HomeApplication` → `HomeController` → specific controllers
2. **Finding listeners**: Search for `PropertyChangeListener` and `CollectionListener`
3. **3D coordinates**: Y-up coordinate system (not Z-up like some engines)
4. **Performance**: Java 3D can be slow; check Component3DManager for optimization hints
5. **Plugins**: Can add features without core modifications via plugin system

## Common Pitfalls

- **Property events**: Forget to fire PropertyChangeEvent after model changes
- **Memory leaks**: Not removing listeners before object destruction
- **Serialization**: Forgetting to update HomeXMLHandler when adding new properties
- **Java 3D**: Missing VM arguments for Java 16+
- **Threading**: UI updates must be on EDT (Event Dispatch Thread)

## Next Steps

1. Build the application and explore GUI
2. Look at a simple feature (e.g., wall color) and trace through model→view→controller
3. Run tests to understand testing patterns
4. Pick a feature and implement it end-to-end

Happy coding!
