#!/usr/bin/env bash
# Run Winga backend (requires Maven or Java + pre-built JAR)
set -e
cd "$(dirname "$0")"

if command -v mvn &>/dev/null; then
  echo "Starting backend with Maven..."
  mvn spring-boot:run
elif [ -f target/winga-backend-*.jar ]; then
  echo "Starting backend with JAR..."
  java -jar target/winga-backend-*.jar
else
  echo "Maven (mvn) is not installed. Install it first:"
  echo "  macOS:  brew install maven"
  echo "  Then run:  mvn spring-boot:run"
  echo ""
  echo "Or run the main class from your IDE: com.winga.WingaApplication"
  exit 1
fi
