@echo off
echo ========================================
echo FlashCards AI - Test Runner
echo ========================================
echo.

echo [1/4] Checking Java installation...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo [2/4] Checking Maven installation...
mvn -version
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo [3/4] Running backend tests...
mvn clean test
if %errorlevel% neq 0 (
    echo ERROR: Backend tests failed
    echo Check the test output above for details
    pause
    exit /b 1
)

echo.
echo [4/4] Checking frontend dependencies...
cd frontend
if not exist "node_modules" (
    echo Installing frontend dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ERROR: Failed to install frontend dependencies
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo All tests completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Start MongoDB: mongod
echo 2. Start backend: mvn spring-boot:run
echo 3. Start frontend: cd frontend && npm start
echo.
echo For detailed testing guide, see TESTING.md
echo.
pause 