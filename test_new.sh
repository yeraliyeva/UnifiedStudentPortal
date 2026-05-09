#!/bin/bash
set -e
mkdir -p out/new
cp new_src/resources/*.properties out/new/ 2>/dev/null || true
javac -d out/new -sourcepath new_src $(find new_src -name "*.java")
java -ea -cp out/new test.TestRunner
