#!/bin/bash
set -e
cd "$(dirname "$0")/.."
mkdir -p out/main
cp src/resources/*.properties out/main/ 2>/dev/null || true
javac -d out/main -sourcepath src $(find src -name "*.java")
java -ea -cp out/main test.TestRunner
