# SweetHome3D Developer Skill

A Claude Code skill to help with SweetHome3D development, understanding the codebase, and implementing features.

## Using the Skill

In Claude Code, you can ask the SweetHome3D developer skill for help with:

### Understanding the Codebase
- "How does the MVC architecture work in SweetHome3D?"
- "Where is the furniture model code?"
- "Explain the observer pattern in this codebase"
- "What does the viewcontroller package do?"

### Finding Code
- "Where should I add a new furniture property?"
- "How do I find the 3D rendering code?"
- "Where are the UI components defined?"
- "Which class handles file serialization?"

### Implementing Features
- "How do I add a new property to furniture?"
- "Guide me through adding a new UI dialog"
- "How should I implement a 3D rendering feature?"
- "What's the proper way to listen to model changes?"

### Debugging
- "Why isn't my model change showing in the UI?"
- "How do I trace through the event flow?"
- "Where should I add a breakpoint for this feature?"

### Architecture Questions
- "How does the Home class store data?"
- "Explain the Room → Wall → Furniture hierarchy"
- "How does the plugin system work?"
- "What's the coordinate system used?"

## Quick Facts

| Aspect | Details |
|--------|---------|
| **Architecture** | MVC (Model→View→Controller) + Java 3D rendering |
| **Package Structure** | model, swing, viewcontroller, j3d, io, plugin, tools, applet |
| **Core Classes** | Home, Room, Wall, HomePieceOfFurniture, Camera, Light |
| **Design Patterns** | MVC, Observer (PropertyChangeListener, CollectionListener), Factory, Plugin |
| **File Format** | .sh3d (ZIP with Home.xml + resources) |
| **Build System** | Ant (not Maven/Gradle) |
| **Total Classes** | ~247 Java source files |
| **Languages** | Java 8+ with Java 3D native libraries |

## Common Tasks

```bash
# Build and run
build                    # Build JAR
rebuild                  # Clean rebuild
start-vnc               # Start VNC for GUI

# Development
export DISPLAY=:1                    # Set display
java -jar install/SweetHome3D-*.jar  # Run with GUI

# Testing
ant test                # Run unit tests
ant -projecthelp        # List all Ant targets
```

## Key Files to Know

- **Main Entry**: `src/com/eteks/sweethome3d/SweetHome3D.java` (1800+ lines)
- **Main UI Frame**: `src/com/eteks/sweethome3d/swing/HomeFramePane.java`
- **Model Core**: `src/com/eteks/sweethome3d/model/Home.java`
- **3D Viewport**: `src/com/eteks/sweethome3d/j3d/Component3D.java`
- **File I/O**: `src/com/eteks/sweethome3d/io/HomeFileRecorder.java`
- **Serialization**: `src/com/eteks/sweethome3d/io/HomeXMLHandler.java`
- **Build**: `build.xml` (Ant configuration)

## Architecture Layers

```
SweetHome3D Application
    ↓
HomeFramePane (Main UI Frame - Swing)
    ├── HomePane (2D Plan + 3D View)
    │   ├── PlanComponent (2D Floor Plan)
    │   └── Component3D (Java 3D 3D Viewport)
    └── Furniture Catalog Panel

HomeController (Main Controller)
    ├── PlanController (2D editing)
    ├── HomeViewController (3D view)
    └── Other Feature Controllers

Home Model
    ├── List<Room> rooms
    ├── List<HomePieceOfFurniture> furniture
    ├── List<Wall> walls
    ├── List<HomeLight> lights
    ├── List<Camera> cameras
    └── HomeEnvironment

Serialization (I/O)
    └── HomeXMLHandler ↔ .sh3d files
```

## When to Use the Skill

✅ **Great for:**
- Explaining architecture and design patterns
- Locating files and classes
- Understanding how features work
- Planning feature implementation
- Reviewing code changes
- Understanding relationships between components

❌ **Not for:**
- Writing code for you (you write code, skill guides you)
- Compiling or running code (use terminal)
- General Java help (it's domain-specific to SweetHome3D)

## Example Conversation

**You**: "I want to add a custom texture property to walls. How should I approach this?"

**Skill Response**:
1. Add property to Wall model class
2. Fire PropertyChangeEvent when it changes
3. Update HomeXMLHandler for serialization
4. Add UI control in wall properties panel
5. Update controller to connect UI to model
6. Test with GUI

**You**: "Where's the wall model code?"

**Skill Response**:
`src/com/eteks/sweethome3d/model/Wall.java`

See HomeXMLHandler for serialization integration...

---

## Documentation Links

- **Full Developer Guide**: [.devcontainer/DEVELOPER_GUIDE.md](.devcontainer/DEVELOPER_GUIDE.md)
- **Architecture Overview**: [.devcontainer/sweethome-dev-skill.md](.devcontainer/sweethome-dev-skill.md)
- **Original Docs**: [README.old.md](README.old.md)
- **Project README**: [README.md](README.md)

## Tips

1. **Ask specific questions** - "How do I add..." is better than "Explain furniture"
2. **Reference class names** - Makes responses more precise
3. **Include context** - "I'm modifying the 3D rendering" helps guide answers
4. **Read the full response** - There's usually more detail than you see at first

Happy developing! 🏠
