# Sweet Home 3D Dev Container - Quick Start

A complete Docker Compose dev environment for building and running Sweet Home 3D locally.

## Getting Started (30 seconds)

### Step 1: Open in VS Code
1. Open this folder in VS Code
2. Click the green `><` button (bottom-left)
3. Select **"Reopen in Container"**
4. Wait for the build to complete (first time only, ~2-3 min)

That's it! You're ready to build.

## Building the App

Once the container is open, open a terminal in VS Code and run:

```bash
# Build the executable JAR
build

# Or use Ant directly
ant jarExecutable
```

The built JAR will be in `install/SweetHome3D-*.jar`

## Running the App with GUI

You'll need a VNC viewer on your local machine:
- **macOS**: `brew install vnc-viewer`
- **Ubuntu/Debian**: `sudo apt-get install tigervnc-viewer`
- **Windows**: Download [RealVNC Viewer](https://www.realvnc.com/en/connect/download/viewer/)

Then in the container:

```bash
# Terminal 1: Start the VNC server
start-vnc

# Terminal 2: Build and run the app
build
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar
```

**On your local machine**, open your VNC viewer and connect to:
```
localhost:5901
```

You should see the SweetHome3D GUI!

## Quick Commands

All available from any container terminal:

| Command | What it does |
|---------|-------------|
| `build` | Build the main JAR executable |
| `rebuild` | Clean and rebuild everything |
| `clean` | Remove all build artifacts |
| `start-vnc` | Start VNC server for GUI access |
| `run-with-vnc` | Build and run with VNC in one command |

## Remote SSH + VS Code?

If you're using VS Code Remote SSH:
1. VS Code automatically forwards port 5901
2. Just follow the steps above
3. Use any VNC viewer on your local machine

## SSH Access

Your SSH keys are automatically available in the container. Just use git normally:

```bash
git clone git@github.com:youruser/repo.git
git push origin main
```

SSH permissions are automatically fixed when the container starts.

## Editing Code

Edit files on your host machine (your preferred IDE). Changes are immediately visible in the container. Just rebuild to test!

## More Details?

See [README.old.md](README.old.md) for comprehensive documentation.

## Troubleshooting

**VNC viewer won't connect?**
- Make sure `start-vnc` is still running in Terminal 1
- Check VS Code Ports tab (should show 5901)
- Try killing old VNC: `pkill -f vnc; pkill -f Xvfb`

**Build fails?**
- Try cleaning first: `clean` then `build`
- Check that `lib/` and `libtest/` directories have content

**Can't see SweetHome3D window?**
- Make sure you set `export DISPLAY=:1` before running the JAR
- VNC viewer must be connected first

Enjoy!
