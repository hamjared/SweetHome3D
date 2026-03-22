FROM eclipse-temurin:21-jdk-noble

# Install Gradle, Ant, Python, and necessary build tools
RUN apt-get update && apt-get install -y \
    gradle \
    ant \
    ant-contrib \
    git \
    curl \
    wget \
    build-essential \
    libx11-dev \
    libxext-dev \
    libxrender-dev \
    libxrandr-dev \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s /usr/bin/python3 /usr/bin/python

# Install display server and VNC for GUI access
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11-utils \
    openbox \
    x11vnc \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/opt/java/openjdk

# Create a workspace directory
WORKDIR /workspace

# Copy the project
COPY . /workspace/

# Copy custom bashrc for nicer terminal
COPY .devcontainer/.bashrc /root/.bashrc

# Copy VNC startup scripts
COPY .devcontainer/start-vnc.sh /usr/local/bin/start-vnc
COPY .devcontainer/run-with-vnc.sh /usr/local/bin/run-with-vnc

# Make scripts executable
RUN chmod +x /usr/local/bin/start-vnc /usr/local/bin/run-with-vnc

# Build the project on container start
# (optional - can be run manually)
CMD ["/bin/bash"]
