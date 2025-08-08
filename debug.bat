@echo off
echo ========================================
echo FlashCards AI - Debug Tool
echo ========================================
echo.

echo [1/6] Checking environment variables...
if not exist ".env" (
    echo ERROR: .env file not found
    echo Please create .env file with required variables
    goto :end
) else (
    echo .env file found
)

echo.
echo [2/6] Checking MongoDB connection...
netstat -an | findstr :27017
if %errorlevel% neq 0 (
    echo WARNING: MongoDB might not be running on port 27017
    echo Start MongoDB with: mongod
) else (
    echo MongoDB appears to be running
)

echo.
echo [3/6] Checking backend port...
netstat -an | findstr :8080
if %errorlevel% neq 0 (
    echo Backend not running on port 8080
) else (
    echo Backend appears to be running on port 8080
)

echo.
echo [4/6] Checking frontend port...
netstat -an | findstr :3000
if %errorlevel% neq 0 (
    echo Frontend not running on port 3000
) else (
    echo Frontend appears to be running on port 3000
)

echo.
echo [5/6] Testing backend connectivity...
curl -s http://localhost:8080/api/auth/test > nul
if %errorlevel% neq 0 (
    echo ERROR: Backend is not responding
    echo Start backend with: mvn spring-boot:run
) else (
    echo Backend is responding correctly
)

echo.
echo [6/6] Checking for common issues...

echo.
echo === Common Issues and Solutions ===
echo.
echo 1. MongoDB Connection Issues:
echo    - Ensure MongoDB is running: mongod
echo    - Check MONGODB_URI in .env file
echo    - Verify database name in application.properties
echo.
echo 2. JWT Token Issues:
echo    - Check JWT_SECRET in .env file
echo    - Verify token expiration settings
echo    - Ensure proper Authorization header format
echo.
echo 3. AI Generation Issues:
echo    - Check OPENAI_API_KEY in .env file
echo    - Verify OPENAI_API_URL is correct
echo    - Test with simple text input first
echo.
echo 4. Frontend Build Issues:
echo    - Run: cd frontend && npm install
echo    - Check for missing Lucide React icons
echo    - Verify all dependencies are installed
echo.
echo 5. CORS Issues:
echo    - Verify CORS configuration in SecurityConfig.java
echo    - Check backend is running on correct port
echo    - Ensure frontend proxy settings are correct
echo.

:end
echo.
echo ========================================
echo Debug check completed
echo ========================================
echo.
echo For detailed testing guide, see TESTING.md
echo.
pause 