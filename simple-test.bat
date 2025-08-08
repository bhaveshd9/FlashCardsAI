@echo off
echo ========================================
echo Simple Test for Both Fixes
echo ========================================
echo.

echo 1. First, make sure backend is running:
echo    - Open a new terminal
echo    - Run: mvn spring-boot:run
echo    - Wait for it to start (should see "Started FlashcardsAiApplication")
echo.

echo 2. Test Backend API (run this in a new terminal):
echo    curl -s http://localhost:8080/api/test
echo.

echo 3. Test User Registration:
echo    curl -s -X POST "http://localhost:8080/api/auth/register" -H "Content-Type: application/json" -d "{\"username\":\"testuser5\",\"email\":\"test5@example.com\",\"password\":\"Test123!\"}"
echo.

echo 4. Test Login (copy the token from response):
echo    curl -s -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"test5@example.com\",\"password\":\"Test123!\"}"
echo.

echo 5. Test Deck Creation (replace YOUR_TOKEN with the token from step 4):
echo    curl -s -X POST "http://localhost:8080/api/decks" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d "{\"name\":\"Test Deck 1\",\"description\":\"Test Description 1\",\"tags\":[\"test\"],\"isPublic\":false}"
echo.

echo 6. Test Second Deck Creation:
echo    curl -s -X POST "http://localhost:8080/api/decks" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d "{\"name\":\"Test Deck 2\",\"description\":\"Test Description 2\",\"tags\":[\"test\"],\"isPublic\":false}"
echo.

echo 7. Test Deck Count (should show 2 decks):
echo    curl -s -X GET "http://localhost:8080/api/decks/my" -H "Authorization: Bearer YOUR_TOKEN"
echo.

echo 8. Test AI Generation with "car brands origin countries":
echo    curl -s -X POST "http://localhost:8080/api/ai/generate" -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d "{\"text\":\"car brands origin countries\",\"numberOfCards\":5,\"topic\":\"Car Brands\",\"difficulty\":\"medium\"}"
echo.

echo 9. Test Dashboard Stats:
echo    curl -s -X GET "http://localhost:8080/api/study/stats" -H "Authorization: Bearer YOUR_TOKEN"
echo.

echo ========================================
echo Expected Results:
echo ========================================
echo.
echo Step 2: Should return "Backend is running!"
echo Step 3: Should return success message
echo Step 4: Should return JWT token
echo Step 5: Should return deck creation success
echo Step 6: Should return second deck creation success
echo Step 7: Should return array with 2 decks (not 0)
echo Step 8: Should return 5 flashcards including "Tesla is from which country?|USA"
echo Step 9: Should return stats object
echo.

echo ========================================
echo Manual Frontend Testing:
echo ========================================
echo.
echo 1. Start frontend: cd frontend && npm start
echo 2. Open http://localhost:3000
echo 3. Login with test5@example.com / Test123!
echo 4. Check Dashboard - should show 2 decks (not 0)
echo 5. Go to Decks page - should show 2 decks
echo 6. Create new deck with AI generation
echo 7. Enter "car brands origin countries" as description
echo 8. Enable AI generation, set 5 cards
echo 9. Verify it generates Tesla question
echo.

pause 