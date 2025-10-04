#!/bin/bash

echo "=== Journal Index Integration - Local Server ==="
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
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo ""
echo "Starting local server..."
echo "======================="

# Set local profile
export SPRING_PROFILES_ACTIVE=local

# Run the local server
mvn spring-boot:run -Dspring-boot.run.main-class=com.teckiz.journalindex.LocalServer

echo ""
echo "=== Local Server Stopped ==="
