#!/bin/bash
# Build and run tests using JDK 21 (avoids Lombok/TypeTag issues on JDK 24+).
# Usage: ./scripts/build-with-jdk21.sh [compile|test|package]

set -e
export JAVA_HOME="${JAVA_HOME_21:-$(/usr/libexec/java_home -v 21 2>/dev/null || true)}"
if [ -z "$JAVA_HOME" ]; then
  echo "JDK 21 not found. Set JAVA_HOME_21 or install JDK 21."
  exit 1
fi
echo "Using JAVA_HOME=$JAVA_HOME"
"$JAVA_HOME/bin/java" -version

case "${1:-test}" in
  compile) ./mvnw -q clean compile ;;
  test)    ./mvnw -q clean test ;;
  package) ./mvnw -q clean package -DskipTests ;;
  *)       ./mvnw -q clean test ;;
esac
