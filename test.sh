#!/bin/bash
set -e

# Setup directories
mkdir -p out/overworked
mkdir -p out/overworked/common

# Copy localization files
cp overworked_src/common/*.properties out/overworked/common/

# Compile source files and tests
javac -d out/overworked -sourcepath overworked_src $(find overworked_src -name "*.java")

# Run tests
java -ea -cp out/overworked test.TestRunner
