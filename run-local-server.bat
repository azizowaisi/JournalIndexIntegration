@echo off
echo === Journal Index Integration - Local Server ===
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
mvn clean compile

if %ERRORLEVEL% neq 0 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo Starting local server...
echo =======================

REM Set local profile
set SPRING_PROFILES_ACTIVE=local

REM Run the local server
mvn spring-boot:run -Dspring-boot.run.main-class=com.teckiz.journalindex.LocalServer

echo.
echo === Local Server Stopped ===
pause
