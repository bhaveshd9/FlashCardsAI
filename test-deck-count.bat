@echo off
echo Testing Deck Count Issue...
echo.

echo 1. Testing Backend API...
echo.

echo Testing /api/decks/my endpoint...
curl -s -X GET "http://localhost:8080/api/decks/my" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
echo.
echo.

echo 2. Testing with a sample user...
echo.

echo Creating test user...
curl -s -X POST "http://localhost:8080/api/auth/register" -H "Content-Type: application/json" -d "{\"username\":\"testuser2\",\"email\":\"test2@example.com\",\"password\":\"Test123!\"}"
echo.
echo.

echo Logging in...
set LOGIN_RESPONSE=
for /f "delims=" %%i in ('curl -s -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"test2@example.com\",\"password\":\"Test123!\"}"') do set LOGIN_RESPONSE=%%i
echo Login Response: %LOGIN_RESPONSE%
echo.

echo Creating a test deck...
curl -s -X POST "http://localhost:8080/api/decks" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" -d "{\"name\":\"Test Deck\",\"description\":\"Test Description\",\"tags\":[\"test\"],\"isPublic\":false}"
echo.
echo.

echo Getting user decks...
curl -s -X GET "http://localhost:8080/api/decks/my" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
echo.
echo.

echo 3. Testing Dashboard Stats...
echo.

echo Getting study stats...
curl -s -X GET "http://localhost:8080/api/study/stats" -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
echo.
echo.

echo Test completed!
pause 