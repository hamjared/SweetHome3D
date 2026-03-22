#!/bin/bash
# Convenience script to start VNC and run SweetHome3D with Cost Estimator plugin

# Start VNC in the background
.devcontainer/start-vnc.sh &
VNC_PID=$!

# Wait for VNC to be ready
echo "Waiting for VNC server to start..."
sleep 3

# Build if needed
if [ ! -f "install/SweetHome3D-"*.jar ]; then
    echo "Building SweetHome3D..."
    ./gradlew jarExecutable
fi

# Setup Cost Estimator plugin
echo "Setting up Cost Estimator plugin..."
mkdir -p ~/.sweethome3d/plugins
if [ ! -f ~/.sweethome3d/plugins/CostEstimatorPlugin.jar ]; then
    if [ -f install/plugins/CostEstimatorPlugin.jar ]; then
        cp install/plugins/CostEstimatorPlugin.jar ~/.sweethome3d/plugins/
        echo "✓ Plugin installed from build"
    else
        echo "⚠ Plugin JAR not found in install/plugins/"
    fi
else
    echo "✓ Plugin already installed (skipping copy)"
fi

# Run SweetHome3D with display
echo ""
echo "🚀 Launching SweetHome3D with Cost Estimator..."
echo "   Tools → Cost Estimator... will be available"
echo ""
export DISPLAY=:1
java -jar install/SweetHome3D-*.jar

# Clean up on exit
kill $VNC_PID 2>/dev/null || true
