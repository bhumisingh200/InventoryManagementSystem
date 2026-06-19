@echo off
echo ======================================================
echo          Inventory Management System Launcher
echo ======================================================
echo [1] Start Application (Standard Mode)
echo [2] Re-compile and Start Application
echo [3] Run Automated Verification Tests
echo ======================================================
set /p choice="Choose an option [1-3]: "

if "%choice%"=="1" (
    echo Starting application...
    java -cp "out;lib/*" ui.App
) else if "%choice%"=="2" (
    echo Compiling source files...
    javac -d out -cp "lib/*" src/VerificationTests.java src/controller/*.java src/database/*.java src/model/*.java src/service/*.java src/ui/*.java
    if %errorlevel% neq 0 (
        echo Compilation failed.
        pause
        exit /b %errorlevel%
    )
    echo Starting application...
    java -cp "out;lib/*" ui.App
) else if "%choice%"=="3" (
    echo Running backend verification tests...
    java -cp "out;lib/*" VerificationTests
) else (
    echo Invalid choice. Exiting...
)
pause
