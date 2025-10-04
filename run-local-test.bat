@echo off
echo === Journal Index Integration - Local Test Runner ===
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java and try again
    pause
    exit /b 1
)

echo Building project...
mvn clean compile test-compile

if %ERRORLEVEL% neq 0 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo Running local test...
echo ====================

REM Set test profile
set SPRING_PROFILES_ACTIVE=test

REM Run the local test
mvn exec:java -Dexec.mainClass="com.teckiz.journalindex.LocalMain" -Dexec.classpathScope=test

echo.
echo === Local Test Completed ===
pause
