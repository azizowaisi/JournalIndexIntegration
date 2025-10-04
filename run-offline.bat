@echo off
echo === Journal Index Integration - Serverless Offline ===
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Node.js is not installed or not in PATH
    echo Please install Node.js and try again
    pause
    exit /b 1
)

REM Check if npm is installed
where npm >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: npm is not installed or not in PATH
    echo Please install npm and try again
    pause
    exit /b 1
)

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo Installing dependencies...
call npm install

if %ERRORLEVEL% neq 0 (
    echo Error: npm install failed
    pause
    exit /b 1
)

echo.
echo Building Java project...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Error: Maven build failed
    pause
    exit /b 1
)

echo.
echo Starting Serverless Offline...
echo ==============================
echo Lambda functions will be available at: http://localhost:3002
echo SQS will be available at: http://localhost:9324
echo Local SQS Queue URL: http://localhost:9324/000000000000/journal-index-queue
echo.
echo To send a test message to SQS:
echo aws --endpoint-url=http://localhost:9324 sqs send-message --queue-url http://localhost:9324/000000000000/journal-index-queue --message-body "{\"journalKey\":\"TEST_001\",\"oaiUrl\":\"https://example.com/oai\"}"
echo.
echo Press Ctrl+C to stop the server
echo ==============================

REM Set local profile for Spring Boot
set SPRING_PROFILES_ACTIVE=local

REM Start serverless offline
call npx serverless offline --stage local --env local

pause
