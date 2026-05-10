#!/bin/bash
set -e
cd "$(dirname "$0")/.."

CLASSES=out/classes
JAR=university-system.jar

# Compile
mkdir -p "$CLASSES"
javac -d "$CLASSES" -sourcepath src $(find src -name "*.java")

# Copy i18n resources into the class tree so they land in the JAR
cp src/resources/*.properties "$CLASSES/" 2>/dev/null || true

# Package into a fat jar with Main-Class manifest
jar --create --file="$JAR" --main-class=Main -C "$CLASSES" .

echo ""
echo "=== Build complete ==================================="
echo "  JAR : $(pwd)/$JAR"
echo ""
echo "  Run (CLI mode):"
echo "    java -jar $JAR"
echo ""
echo "  Run (REST server on port 8080):"
echo "    java -jar $JAR --server"
echo ""
echo "  Run (REST server on custom port):"
echo "    java -jar $JAR --server 9000"
echo ""
echo "  Custom data directory:"
echo "    java -Duni.data=/path/to/data -jar $JAR"
echo "======================================================"
