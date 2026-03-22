@echo off
REM Setup script to install plugins in the Sweet Home 3D config directory

setlocal enabledelayedexpansion

set PLUGIN_JAR=install\plugins\CostEstimatorPlugin.jar
set PLUGINS_DIR=%USERPROFILE%\.sweethome3d\plugins

if not exist "%PLUGIN_JAR%" (
    echo Error: Plugin JAR not found at %PLUGIN_JAR%
    echo Please run: gradlew jarExecutable
    pause
    exit /b 1
)

REM Create plugins directory if it doesn't exist
if not exist "%PLUGINS_DIR%" (
    mkdir "%PLUGINS_DIR%"
)

REM Copy plugin
copy "%PLUGIN_JAR%" "%PLUGINS_DIR%\"

echo.
echo Plugin installed to: %PLUGINS_DIR%
echo You can now run: java -jar install\SweetHome3D-*.jar
echo Tools menu will show "Cost Estimator..."
echo.
pause
