#!/bin/bash
# Convenience script to start VNC and run SweetHome3D with Cost Estimator plugin

echo "Building SweetHome3D..."
./gradlew build costEstimatorPlugin

# Setup Cost Estimator plugin
echo "Setting up Cost Estimator plugin..."
mkdir -p ~/.eteks/sweethome3d/plugins
if [ -f build/plugins/CostEstimatorPlugin.jar ]; then
    cp build/plugins/CostEstimatorPlugin.jar ~/.eteks/sweethome3d/plugins/
    echo "✓ Plugin installed from build"
else
    echo "⚠ Plugin JAR not found in build/plugins/"
fi

# Run SweetHome3D with display
echo ""
echo "🚀 Launching SweetHome3D with Cost Estimator..."
echo "   Tools → Cost Estimator... will be available"
echo ""

echo " Running with command DISPLAY=:1 java -jar build/libs/SweetHome3D-*.jar"
DISPLAY=:1 java -jar build/libs/SweetHome3D-*.jar

# Clean up on exit
kill $VNC_PID 2>/dev/null || true
