#!/bin/bash

echo "=== Journal Index Integration - Local Test Runner ==="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java and try again"
    exit 1
fi

echo "Building project..."
mvn clean compile test-compile

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo ""
echo "Running local test..."
echo "===================="

# Set test profile
export SPRING_PROFILES_ACTIVE=test

# Run the local test
mvn exec:java -Dexec.mainClass="com.teckiz.journalindex.LocalMain" -Dexec.classpathScope=test

echo ""
echo "=== Local Test Completed ==="
