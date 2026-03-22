#!/bin/bash
set -e

echo "=== Cost Estimator Plugin Diagnostics ==="
echo ""

echo "1. Check if plugin JAR built:"
if [ -f "install/plugins/CostEstimatorPlugin.jar" ]; then
    ls -lh install/plugins/CostEstimatorPlugin.jar
else
    echo "ERROR: Plugin JAR not found at install/plugins/CostEstimatorPlugin.jar"
    exit 1
fi
echo ""

echo "2. Check plugin JAR contents for ApplicationPlugin.properties:"
echo "   Looking for files named *ApplicationPlugin.properties*"
unzip -l install/plugins/CostEstimatorPlugin.jar | grep -i "ApplicationPlugin.properties" || echo "   ERROR: Not found!"
echo ""

echo "3. Check if plugin is installed in ~/.eteks/sweethome3d/plugins:"
if [ -f ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar ]; then
    ls -lh ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar
else
    echo "   ERROR: Not installed"
fi
echo ""

echo "4. Check installed plugin JAR contents:"
unzip -l ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar 2>/dev/null | grep -i "ApplicationPlugin.properties" || echo "   ERROR: Not found!"
echo ""

echo "5. Check app version (for version compatibility):"
grep "CURRENT_VERSION" src/com/eteks/sweethome3d/model/Home.java | head -1
echo ""

echo "6. Start app and capture plugin loading messages (first 100 lines with stderr):"
export DISPLAY=:1
timeout 10 java -jar install/SweetHome3D-*.jar 2>&1 | grep -i "plugin\|cost\|error" | head -20 || echo "   (No plugin messages detected)"
