#!/bin/bash
set -e

echo "=== Manual Cost Estimator Plugin Build ==="
echo ""

# Clean up previous build
rm -rf /tmp/cost-estimator-plugin
mkdir -p /tmp/cost-estimator-plugin
cd /tmp/cost-estimator-plugin

echo "1. Copying compiled plugin classes..."
mkdir -p com/eteks/sweethome3d/plugin
cp -r /workspace/build/classes/java/main/com/eteks/sweethome3d/plugin/costestimator com/eteks/sweethome3d/plugin/

echo "2. Copying properties files..."
cp /workspace/src/com/eteks/sweethome3d/plugin/costestimator/ApplicationPlugin.properties .
cp /workspace/src/com/eteks/sweethome3d/plugin/costestimator/CostEstimatorPlugin.properties com/eteks/sweethome3d/plugin/costestimator/

echo "3. Creating JAR with ApplicationPlugin.properties at root..."
jar cf CostEstimatorPlugin-manual.jar \
  ApplicationPlugin.properties \
  com/

echo "4. Verifying JAR structure..."
echo "   Files in JAR:"
jar tf CostEstimatorPlugin-manual.jar | head -20

echo ""
echo "   Looking for ApplicationPlugin.properties at root:"
jar tf CostEstimatorPlugin-manual.jar | grep "^ApplicationPlugin.properties$" && echo "   ✓ FOUND AT ROOT!" || echo "   ERROR: Not at root!"

echo ""
echo "5. Installing plugin to correct location..."
mkdir -p ~/.eteks/sweethome3d/plugins
cp CostEstimatorPlugin-manual.jar ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar

echo ""
echo "6. Verifying installation..."
ls -lh ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar

echo ""
echo "✓ Manual plugin build complete!"
echo ""
echo "Now run: run-with-vnc"
