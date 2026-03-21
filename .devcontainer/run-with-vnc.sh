#!/bin/bash
# Convenience script to start VNC and run SweetHome3D

# Start VNC in the background
.devcontainer/start-vnc.sh &
VNC_PID=$!

# Wait for VNC to be ready
echo "Waiting for VNC server to start..."
sleep 3

# Build if needed
if [ ! -f "install/SweetHome3D-*.jar" ]; then
    echo "Building SweetHome3D..."
    ant jarExecutable
fi

# Run SweetHome3D with display
echo ""
echo "🚀 Launching SweetHome3D..."
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar

# Clean up on exit
kill $VNC_PID 2>/dev/null || true
