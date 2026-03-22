#!/bin/bash

echo "=== Plugin Loading Diagnostics ==="
echo ""

echo "1. Check plugin directory exists:"
ls -la ~/.eteks/sweethome3d/plugins/ 2>&1 || echo "   ERROR: Directory not found"
echo ""

echo "2. Check plugin JAR structure:"
unzip -l ~/.eteks/sweethome3d/plugins/CostEstimatorPlugin.jar 2>&1 | grep -E "ApplicationPlugin|CostEstimator" || echo "   ERROR: Properties files not found"
echo ""

echo "3. App version info:"
grep "CURRENT_VERSION\|APPLICATION_BUILD_ID" /workspace/src/com/eteks/sweethome3d/model/Home.java | head -3
echo ""

echo "4. Start app and capture ALL output (20 seconds timeout):"
export DISPLAY=:1
timeout 20 java -jar /workspace/install/SweetHome3D-*.jar 2>&1 | tee /tmp/app-output.log &
sleep 5

echo ""
echo "5. Check for plugin-related messages in output:"
grep -i "plugin\|PluginManager\|costestimator\|costestimate\|cost estimator" /tmp/app-output.log | head -30 || echo "   No plugin messages found"

echo ""
echo "6. Check for any errors/exceptions:"
grep -i "error\|exception\|invalid\|failed" /tmp/app-output.log | head -20 || echo "   No errors found"

echo ""
echo "7. Full output sample (first 50 lines):"
head -50 /tmp/app-output.log

echo ""
echo "=== End Diagnostics ==="
