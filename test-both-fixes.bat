@echo off
echo ========================================
echo Testing Both Fixes: Deck Count + AI Generation
echo ========================================
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

echo 2. Starting Backend Server...
start "Backend Server" cmd /k "mvn spring-boot:run"
timeout /t 15 /nobreak >nul
echo.

echo 3. Testing API Endpoints...
echo.

echo Testing backend connectivity...
curl -s http://localhost:8080/api/test
if %errorlevel% neq 0 (
    echo ❌ Backend not responding
    pause
    exit /b 1
)
echo ✅ Backend is responding
echo.

echo 4. Testing User Registration and Login...
echo.

echo Creating test user...
curl -s -X POST "http://localhost:8080/api/auth/register" -H "Content-Type: application/json" -d "{\"username\":\"testuser3\",\"email\":\"test3@example.com\",\"password\":\"Test123!\"}"
echo.
echo.

echo Logging in to get JWT token...
for /f "delims=" %%i in ('curl -s -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"test3@example.com\",\"password\":\"Test123!\"}"') do set LOGIN_RESPONSE=%%i
echo Login Response: %LOGIN_RESPONSE%
echo.

echo 5. Testing Deck Creation...
echo.

echo Creating test deck...
for /f "delims=" %%i in ('curl -s -X POST "http://localhost:8080/api/decks" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" -d "{\"name\":\"Test Deck 1\",\"description\":\"Test Description 1\",\"tags\":[\"test\"],\"isPublic\":false}"') do set DECK_RESPONSE=%%i
echo Deck Creation Response: %DECK_RESPONSE%
echo.

echo Creating second test deck...
for /f "delims=" %%i in ('curl -s -X POST "http://localhost:8080/api/decks" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" -d "{\"name\":\"Test Deck 2\",\"description\":\"Test Description 2\",\"tags\":[\"test\"],\"isPublic\":false}"') do set DECK_RESPONSE2=%%i
echo Second Deck Creation Response: %DECK_RESPONSE2%
echo.

echo 6. Testing Deck Count...
echo.

echo Getting user decks...
for /f "delims=" %%i in ('curl -s -X GET "http://localhost:8080/api/decks/my" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"') do set DECKS_RESPONSE=%%i
echo Decks Response: %DECKS_RESPONSE%
echo.

echo 7. Testing AI Generation with Car Brands...
echo.

echo Testing AI generation with "car brands origin countries"...
for /f "delims=" %%i in ('curl -s -X POST "http://localhost:8080/api/ai/generate" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" -d "{\"text\":\"car brands origin countries\",\"numberOfCards\":5,\"topic\":\"Car Brands\",\"difficulty\":\"medium\"}"') do set AI_RESPONSE=%%i
echo AI Generation Response: %AI_RESPONSE%
echo.

echo 8. Testing Dashboard Stats...
echo.

echo Getting study stats...
for /f "delims=" %%i in ('curl -s -X GET "http://localhost:8080/api/study/stats" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"') do set STATS_RESPONSE=%%i
echo Stats Response: %STATS_RESPONSE%
echo.

echo ========================================
echo Test Summary:
echo ========================================
echo.
echo Expected Results:
echo - Deck count should show 2 (not 0)
echo - AI generation should include "Tesla is from which country?|USA"
echo - Dashboard stats should show real data
echo.
echo Please check the responses above and verify:
echo 1. Deck count is correct (should be 2)
echo 2. AI generation includes Tesla question
echo 3. No errors in responses
echo.
echo Press any key to continue...
pause >nul

echo.
echo Starting Frontend for manual testing...
cd frontend
start "Frontend Server" cmd /k "npm start"
cd ..
echo.

echo ========================================
echo Manual Testing Instructions:
echo ========================================
echo.
echo 1. Open browser to http://localhost:3000
echo 2. Login with test3@example.com / Test123!
echo 3. Check Dashboard - should show 2 decks (not 0)
echo 4. Go to Decks page - should show 2 decks
echo 5. Create new deck with AI generation
echo 6. Enter "car brands origin countries" as description
echo 7. Enable AI generation, set 5 cards
echo 8. Verify it generates Tesla question
echo.
echo Press any key to exit...
pause >nul 