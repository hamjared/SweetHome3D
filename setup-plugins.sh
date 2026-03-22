#!/bin/bash
# Setup script to install plugins in the Sweet Home 3D config directory

PLUGINS_DIR="$HOME/.sweethome3d/plugins"
PLUGIN_JAR="install/plugins/CostEstimatorPlugin.jar"

if [ ! -f "$PLUGIN_JAR" ]; then
    echo "❌ Error: Plugin JAR not found at $PLUGIN_JAR"
    echo "   Please run: ./gradlew jarExecutable"
    exit 1
fi

# Create plugins directory if it doesn't exist
mkdir -p "$PLUGINS_DIR"

# Copy plugin
cp "$PLUGIN_JAR" "$PLUGINS_DIR/"

echo "✓ Plugin installed to: $PLUGINS_DIR"
echo "✓ You can now run: java -jar install/SweetHome3D-*.jar"
echo "✓ Tools → Cost Estimator will be available in the menu"
