#!/bin/bash
# Start VNC server and noVNC for GUI access

set -e

DISPLAY=:1
VNCPORT=5901
RESOLUTION=${RESOLUTION:-1280x800}
DEPTH=${DEPTH:-24}

echo "Starting VNC server..."
echo "Display: $DISPLAY"
echo "Resolution: $RESOLUTION"
echo "Port: $VNCPORT"

# Start Xvfb (virtual X display)
echo "Starting Xvfb virtual display..."
Xvfb $DISPLAY -screen 0 ${RESOLUTION}x${DEPTH} &
XVFB_PID=$!
sleep 2

# Start window manager
echo "Starting window manager (openbox)..."
DISPLAY=$DISPLAY openbox &
sleep 1

# Start x11vnc server
echo "Starting x11vnc server..."
DISPLAY=$DISPLAY x11vnc -display $DISPLAY -forever -nopw -listen localhost -rfbport $VNCPORT &
VNC_PID=$!
sleep 2

echo ""
echo "╔════════════════════════════════════════════╗"
echo "║        VNC Server Started                   ║"
echo "╚════════════════════════════════════════════╝"
echo ""
echo "📊 VNC Details:"
echo "   Host: localhost"
echo "   Port: 5901"
echo "   No password required"
echo ""
echo "💡 To access GUI:"
echo "   Use a VNC client: vncviewer localhost:5901"
echo "   Or install one: apt-get install tigervnc-viewer"
echo ""
echo "💡 To run SweetHome3D with display:"
echo "   export DISPLAY=:1"
echo "   DISPLAY=:1 java -jar build/libs/SweetHome3D-*.jar"
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Wait for signals
trap "kill $XVFB_PID $VNC_PID 2>/dev/null; exit 0" SIGINT SIGTERM

wait
