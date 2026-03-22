#!/usr/bin/env python3
"""
Sweet Home 3D build helper script
"""

import os
import sys
import subprocess
import argparse
from pathlib import Path
from datetime import datetime

# Colors for output
class Colors:
    GREEN = '\033[0;32m'
    RED = '\033[0;31m'
    YELLOW = '\033[1;33m'
    RESET = '\033[0m'

def print_status(msg):
    """Print status message in green"""
    timestamp = datetime.now().strftime('%H:%M:%S')
    print(f"{Colors.GREEN}[{timestamp}]{Colors.RESET} {msg}")

def print_error(msg):
    """Print error message in red"""
    print(f"{Colors.RED}[ERROR]{Colors.RESET} {msg}")

def print_warning(msg):
    """Print warning message in yellow"""
    print(f"{Colors.YELLOW}[WARNING]{Colors.RESET} {msg}")

def is_in_container():
    """Check if running inside a Docker container"""
    return Path('/.dockerenv').exists()

def get_docker_compose_cmd():
    """Get the docker-compose command with proper file path"""
    script_dir = Path(__file__).parent
    compose_file = script_dir / 'docker-compose.yml'
    return ['docker', 'compose', '-f', str(compose_file)]

def run_in_container(cmd):
    """Run a command in the container if not already in one"""
    if is_in_container():
        return subprocess.run(cmd, shell=True, check=False)
    else:
        docker_cmd = get_docker_compose_cmd()
        docker_cmd.extend(['exec', 'sweethome-dev', 'bash', '-c', cmd])
        return subprocess.run(docker_cmd, check=False)

def build_gradle():
    """Build Gradle jar executable"""
    print_status("Building: using ./gradlew build")
    result = run_in_container("./gradlew build")
    if result.returncode == 0:
        print_status("Build successful!")
    return result.returncode

def clean():
    """Clean build artifacts"""
    print_status("Cleaning build artifacts")
    result = run_in_container("./gradlew clean")
    if result.returncode == 0:
        print_status("Clean successful!")
    return result.returncode

def rebuild():
    """Clean and rebuild"""
    print_status("Rebuilding (clean + build)")
    result = run_in_container("./gradlew clean build")
    if result.returncode == 0:
        print_status("Rebuild successful!")
    return result.returncode

def run_app():
    """Build and run the application"""
    if is_in_container():
        print_status("Building and running application")
        result = subprocess.run("./gradlew build", shell=True, check=False)
        if result.returncode != 0:
            print_error("Build failed")
            return 1

        # Find the jar file
        jar_files = sorted(Path('build/libs').glob('SweetHome3D-*.jar'), reverse=True)
        if not jar_files:
            print_error("Could not find built JAR file")
            return 1

        jar_file = jar_files[0]
        print_status(f"Running: {jar_file}")
        return subprocess.run([sys.executable, '-c', f'import subprocess; subprocess.run(["java", "-jar", "{jar_file}"])'], check=False).returncode
    else:
        docker_cmd = get_docker_compose_cmd()
        docker_cmd.extend(['exec', 'sweethome-dev', 'bash', '-c', './gradlew jarExecutable && java -jar build/libs/SweetHome3D-*.jar'])
        return subprocess.run(docker_cmd, check=False).returncode

def start_container():
    """Start the dev container"""
    print_status("Starting dev container")
    docker_cmd = get_docker_compose_cmd()
    docker_cmd.extend(['up', '-d'])
    result = subprocess.run(docker_cmd, check=False)
    if result.returncode == 0:
        print_status("Container started. Connect with: docker-compose -f .devcontainer/docker-compose.yml exec sweethome-dev bash")
    return result.returncode

def stop_container():
    """Stop the dev container"""
    print_status("Stopping dev container")
    docker_cmd = get_docker_compose_cmd()
    docker_cmd.extend(['down'])
    result = subprocess.run(docker_cmd, check=False)
    if result.returncode == 0:
        print_status("Container stopped")
    return result.returncode

def open_shell():
    """Open a shell in the container"""
    docker_cmd = get_docker_compose_cmd()

    # Check if container is running
    check_cmd = docker_cmd + ['exec', 'sweethome-dev', 'test', '-d', '/workspace']
    result = subprocess.run(check_cmd, capture_output=True)

    if result.returncode != 0:
        print_status("Starting container")
        start_docker_cmd = get_docker_compose_cmd()
        start_docker_cmd.extend(['up', '-d'])
        subprocess.run(start_docker_cmd)

    shell_cmd = docker_cmd + ['exec', '-it', 'sweethome-dev', 'bash']
    return subprocess.run(shell_cmd).returncode

def show_logs():
    """Show container logs"""
    docker_cmd = get_docker_compose_cmd()
    docker_cmd.extend(['logs', '-f', 'sweethome-dev'])
    return subprocess.run(docker_cmd).returncode

def show_help():
    """Display help message"""
    help_text = """
Sweet Home 3D Build Helper (Gradle)

Usage: python build.py [COMMAND] [OPTIONS]

COMMANDS:
  build           Build the main JAR executable with Gradle (default)
  clean           Clean build artifacts
  rebuild         Clean and rebuild
  run             Build and run the application
  start           Start the dev container
  stop            Stop the dev container
  shell           Open a shell in the dev container
  logs            Show container logs
  -h, --help      Show this help message

EXAMPLES:
  python build.py                    # Build JAR executable
  python build.py rebuild            # Clean and rebuild
  python build.py run                # Build and run the application
  python build.py shell              # Open container shell
"""
    print(help_text)

def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description='Sweet Home 3D Build Helper',
        prog='build.py',
        add_help=False
    )

    parser.add_argument(
        'command',
        nargs='?',
        default='build',
        help='Command to execute'
    )

    parser.add_argument(
        '-h', '--help',
        action='store_true',
        help='Show help message'
    )

    args = parser.parse_args()

    if args.help:
        show_help()
        return 0

    command = args.command

    if command == 'build':
        return build_gradle()
    elif command == 'clean':
        return clean()
    elif command == 'rebuild':
        return rebuild()
    elif command == 'run':
        return run_app()
    elif command == 'start':
        return start_container()
    elif command == 'stop':
        return stop_container()
    elif command == 'shell':
        return open_shell()
    elif command == 'logs':
        return show_logs()
    elif command in ['-h', '--help', 'help']:
        show_help()
        return 0
    else:
        print_error(f"Unknown command: {command}")
        print()
        show_help()
        return 1

if __name__ == '__main__':
    sys.exit(main())
