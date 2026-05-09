#!/bin/bash
set -e

mkdir -p out/main
cp src/resources/*.properties out/main/ 2>/dev/null || true

javac -d out/main -sourcepath src $(find src -name "*.java")

echo "Build OK. Run with:  java -cp out/main Main"
