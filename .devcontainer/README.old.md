# Sweet Home 3D Dev Container

This directory contains the Docker Compose configuration for developing Sweet Home 3D locally.

## Quick Start

### Option 1: Using VS Code (Recommended)
1. Install the "Dev Containers" extension in VS Code
2. Open this folder in VS Code
3. Click the green `><` icon in the bottom-left corner
4. Select "Reopen in Container"
5. Wait for the container to build (first time only)

### Option 2: Manual Docker Compose

```bash
# Build the container
docker-compose -f .devcontainer/docker-compose.yml up -d

# Connect to the container
docker-compose -f .devcontainer/docker-compose.yml exec sweethome-dev bash
```

## Common Build Targets

Once inside the container:

```bash
# Build the main JAR executable (default)
ant jarExecutable

# Build everything
ant build

# Build specific components
ant application    # Build SweetHome3D.jar without applet classes
ant furniture      # Build Furniture.jar
ant textures       # Build Textures.jar
ant examples       # Build Examples.jar
ant help           # Build Help.jar

# Clean build artifacts
ant clean

# View all available targets
ant -projecthelp
```

## Running the Application with GUI

### Option 1: VNC Server + Client ⭐ **Recommended**

```bash
# Terminal 1 (in container): Start VNC server
start-vnc

# Terminal 2 (in container): Build and run the app
python build
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar
```

**Then connect with a VNC viewer:**
```bash
# On your local machine
vncviewer localhost:5901
```

**Via Remote SSH + VS Code:**
- VS Code will forward port 5901 automatically
- Use any VNC viewer pointing to `localhost:5901`
- Common viewers: RealVNC, TigerVNC, TightVNC

**Adjust display size:**
```bash
RESOLUTION=1920x1080 start-vnc
```

### Option 2: Convenience Script

One-liner that starts VNC and runs the app:
```bash
run-with-vnc
```

Then connect with `vncviewer localhost:5901` from your machine.

### Option 3: Headless (No GUI)

Run without display:
```bash
java -Djava.awt.headless=true -jar install/SweetHome3D-*.jar
```

### Troubleshooting VNC

**Port already in use:**
```bash
# Kill existing VNC processes
pkill -f vnc
pkill -f Xvfb
```

**Can't connect with VNC viewer:**
- Check VS Code port forwarding (should show 5901 in Ports tab)
- Try `localhost:5901` in your VNC viewer
- Make sure a VNC viewer is installed locally

**No VNC viewer installed?**
```bash
# On macOS
brew install vnc-viewer

# On Ubuntu/Debian
sudo apt-get install tigervnc-viewer

# Or download: https://www.realvnc.com/en/connect/download/viewer/
```

**GUI looks small/large:**
```bash
# Modify before running start-vnc
RESOLUTION=1440x900 start-vnc
```

## Environment Details

- **Java**: OpenJDK 11 (Eclipse Temurin)
- **Build Tool**: Apache Ant
- **Java 3D**: Included (lib/java3d-1.6/)
- **Workspace**: `/workspace` (mounted from host)
- **Build Cache**: Docker volume `sweethome-cache` (persistent between container starts)

## VM Arguments for Development

If you encounter issues with Java 16+, add these VM arguments:
```
--add-opens=java.desktop/java.awt=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.desktop/com.apple.eio=ALL-UNNAMED
--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED
```

## Cleaning Up

```bash
# Stop the container
docker-compose -f .devcontainer/docker-compose.yml down

# Remove the container and volumes
docker-compose -f .devcontainer/docker-compose.yml down -v
```

## Troubleshooting

**Build fails with "cannot find symbol":**
- Ensure all dependencies in `lib/` and `libtest/` are present
- Try: `ant clean && ant jarExecutable`

**GUI not appearing:**
- X11 forwarding is optional; the app builds fine headless
- For Linux, ensure your host has X11 available and DISPLAY is set

**Out of memory errors:**
- Increase ANT_OPTS in docker-compose.yml
- Change `-Xmx2048m` to a larger value (e.g., `-Xmx4096m`)

## Making Modifications

The workspace directory is mounted as a volume, so:
1. Edit files on your host machine with your preferred IDE
2. Changes are immediately visible in the container
3. Run builds from within the container to test your changes

Good luck with your Sweet Home 3D modifications!
