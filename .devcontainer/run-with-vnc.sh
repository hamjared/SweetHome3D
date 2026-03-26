#!/bin/bash
# Convenience script to start VNC and run SweetHome3D with Cost Estimator plugin
#
# Log level options (can be combined):
#   --log-level LEVEL          Set com.eteks.sweethome3d package log level (default: DEBUG)
#   --root-log-level LEVEL     Set root log level (default: INFO)
#   --log LOGGER=LEVEL         Set a specific logger level, e.g. --log com.eteks.sweethome3d.model.Wall=TRACE
#
# Log levels: TRACE DEBUG INFO WARN ERROR OFF

# Defaults
SH3D_LOG_LEVEL="DEBUG"
ROOT_LOG_LEVEL="INFO"
EXTRA_LOGGERS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    --log-level)
      SH3D_LOG_LEVEL="$2"
      shift 2
      ;;
    --root-log-level)
      ROOT_LOG_LEVEL="$2"
      shift 2
      ;;
    --log)
      EXTRA_LOGGERS+=("$2")
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--log-level LEVEL] [--root-log-level LEVEL] [--log LOGGER=LEVEL ...]"
      exit 1
      ;;
  esac
done

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

# Generate log4j2 config with requested levels
TMPCONFIG=$(mktemp /tmp/log4j2-XXXXXX.xml)

EXTRA_LOGGER_XML=""
for entry in "${EXTRA_LOGGERS[@]}"; do
  logger_name="${entry%%=*}"
  logger_level="${entry##*=}"
  EXTRA_LOGGER_XML="${EXTRA_LOGGER_XML}
    <Logger name=\"${logger_name}\" level=\"${logger_level}\" additivity=\"false\">
      <AppenderRef ref=\"Console\"/>
    </Logger>"
done

cat > "$TMPCONFIG" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
    </Console>
  </Appenders>
  <Loggers>
    <Logger name="com.eteks.sweethome3d" level="${SH3D_LOG_LEVEL}" additivity="false">
      <AppenderRef ref="Console"/>
    </Logger>
${EXTRA_LOGGER_XML}
    <Root level="${ROOT_LOG_LEVEL}">
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
EOF

echo ""
echo "Log levels: com.eteks.sweethome3d=${SH3D_LOG_LEVEL}, root=${ROOT_LOG_LEVEL}"
for entry in "${EXTRA_LOGGERS[@]}"; do
  echo "  ${entry}"
done
echo ""
echo "🚀 Launching SweetHome3D with Cost Estimator..."
echo "   Tools → Cost Estimator... will be available"
echo ""

echo "Running: DISPLAY=:1 java -Dlog4j2.configurationFile=... -jar build/libs/SweetHome3D-*.jar"
DISPLAY=:1 java -Dlog4j2.configurationFile="file:${TMPCONFIG}" -jar build/libs/SweetHome3D-*.jar

rm -f "$TMPCONFIG"

# Clean up on exit
kill $VNC_PID 2>/dev/null || true
