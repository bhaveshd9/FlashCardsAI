@echo off
echo Testing FlashCards AI Fixes...
echo.

echo 1. Testing Backend Compilation...
mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ Backend compilation failed
    pause
    exit /b 1
)
echo ✅ Backend compilation successful
echo.

echo 2. Testing Frontend Dependencies...
cd frontend
if not exist node_modules (
    echo Installing frontend dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Frontend dependency installation failed
        pause
        exit /b 1
    )
)
echo ✅ Frontend dependencies ready
cd ..
echo.

echo 3. Testing MongoDB Connection...
netstat -an | findstr :27017
if %errorlevel% neq 0 (
    echo ⚠️  MongoDB not running on port 27017
    echo Please start MongoDB before testing
) else (
    echo ✅ MongoDB is running
)
echo.

echo 4. Testing Backend Startup...
echo Starting backend server...
start "Backend Server" cmd /k "mvn spring-boot:run"
timeout /t 10 /nobreak >nul
echo.

echo 5. Testing Frontend Startup...
echo Starting frontend server...
cd frontend
start "Frontend Server" cmd /k "npm start"
cd ..
echo.

echo 6. Testing API Endpoints...
timeout /t 5 /nobreak >nul
curl -s http://localhost:8080/api/test
if %errorlevel% neq 0 (
    echo ❌ Backend API not responding
) else (
    echo ✅ Backend API is responding
)
echo.

echo 7. Testing Frontend...
timeout /t 5 /nobreak >nul
curl -s http://localhost:3000
if %errorlevel% neq 0 (
    echo ❌ Frontend not responding
) else (
    echo ✅ Frontend is responding
)
echo.

echo ========================================
echo 🎉 All tests completed!
echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo.
echo Please test the following manually:
echo 1. Dashboard stats (should show real data)
echo 2. Deck list card counts (should be accurate)
echo 3. Title/description display (should be correct)
echo 4. Quiz functionality (should work)
echo 5. Document upload UI (should appear)
echo 6. Study mode (should be simple review)
echo 7. Language selector (should be in navbar)
echo 8. Stats updates (should be real-time)
echo.
echo Press any key to exit...
pause >nul 