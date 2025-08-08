# FlashCards AI - Test Both Fixes
Write-Host "========================================" -ForegroundColor Green
Write-Host "Testing Both Fixes: Deck Count + AI Generation" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Test 1: Backend Compilation
Write-Host "1. Testing Backend Compilation..." -ForegroundColor Yellow
mvn clean compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Backend compilation failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✅ Backend compilation successful" -ForegroundColor Green
Write-Host ""

# Test 2: Start Backend Server
Write-Host "2. Starting Backend Server..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/k", "mvn spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15
Write-Host ""

# Test 3: Test Backend Connectivity
Write-Host "3. Testing Backend Connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test" -Method GET
    Write-Host "✅ Backend is responding: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend not responding" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Test 4: Create Test User
Write-Host "4. Creating Test User..." -ForegroundColor Yellow
$registerBody = @{
    username = "testuser4"
    email = "test4@example.com"
    password = "Test123!"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $registerBody -ContentType "application/json"
    Write-Host "✅ User created successfully" -ForegroundColor Green
} catch {
    Write-Host "⚠️ User might already exist or error occurred" -ForegroundColor Yellow
}
Write-Host ""

# Test 5: Login and Get JWT Token
Write-Host "5. Logging in to get JWT token..." -ForegroundColor Yellow
$loginBody = @{
    email = "test4@example.com"
    password = "Test123!"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ Login successful, JWT token obtained" -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Test 6: Create Test Decks
Write-Host "6. Creating Test Decks..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$deck1Body = @{
    name = "Test Deck 1"
    description = "Test Description 1"
    tags = @("test")
    isPublic = $false
} | ConvertTo-Json

$deck2Body = @{
    name = "Test Deck 2"
    description = "Test Description 2"
    tags = @("test")
    isPublic = $false
} | ConvertTo-Json

try {
    $deck1Response = Invoke-RestMethod -Uri "http://localhost:8080/api/decks" -Method POST -Headers $headers -Body $deck1Body
    Write-Host "✅ First deck created: $($deck1Response.name)" -ForegroundColor Green
    
    $deck2Response = Invoke-RestMethod -Uri "http://localhost:8080/api/decks" -Method POST -Headers $headers -Body $deck2Body
    Write-Host "✅ Second deck created: $($deck2Response.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Deck creation failed" -ForegroundColor Red
}
Write-Host ""

# Test 7: Test Deck Count
Write-Host "7. Testing Deck Count..." -ForegroundColor Yellow
try {
    $decksResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/decks/my" -Method GET -Headers $headers
    $deckCount = $decksResponse.Count
    Write-Host "✅ Found $deckCount decks" -ForegroundColor Green
    
    if ($deckCount -eq 2) {
        Write-Host "✅ Deck count is correct (2)" -ForegroundColor Green
    } else {
        Write-Host "❌ Deck count is incorrect (expected 2, got $deckCount)" -ForegroundColor Red
    }
    
    foreach ($deck in $decksResponse) {
        Write-Host "  - $($deck.name) (ID: $($deck.id))" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Failed to get decks" -ForegroundColor Red
}
Write-Host ""

# Test 8: Test AI Generation
Write-Host "8. Testing AI Generation with 'car brands origin countries'..." -ForegroundColor Yellow
$aiBody = @{
    text = "car brands origin countries"
    numberOfCards = 5
    topic = "Car Brands"
    difficulty = "medium"
} | ConvertTo-Json

try {
    $aiResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/generate" -Method POST -Headers $headers -Body $aiBody
    Write-Host "✅ AI generation successful" -ForegroundColor Green
    Write-Host "Generated $($aiResponse.Count) flashcards:" -ForegroundColor Cyan
    
    $hasTesla = $false
    foreach ($card in $aiResponse) {
        Write-Host "  - $($card.front) | $($card.back)" -ForegroundColor White
        if ($card.front -like "*Tesla*" -or $card.back -like "*Tesla*") {
            $hasTesla = $true
        }
    }
    
    if ($hasTesla) {
        Write-Host "✅ Tesla question found in AI generation" -ForegroundColor Green
    } else {
        Write-Host "❌ Tesla question not found in AI generation" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ AI generation failed" -ForegroundColor Red
}
Write-Host ""

# Test 9: Test Dashboard Stats
Write-Host "9. Testing Dashboard Stats..." -ForegroundColor Yellow
try {
    $statsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/study/stats" -Method GET -Headers $headers
    Write-Host "✅ Dashboard stats retrieved" -ForegroundColor Green
    Write-Host "Stats: $($statsResponse | ConvertTo-Json)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Failed to get dashboard stats" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Green
Write-Host "Test Summary:" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Yellow
Write-Host "- Deck count should show 2 (not 0)" -ForegroundColor White
Write-Host "- AI generation should include Tesla question" -ForegroundColor White
Write-Host "- Dashboard stats should show real data" -ForegroundColor White
Write-Host ""

Write-Host "Manual Testing Instructions:" -ForegroundColor Yellow
Write-Host "1. Open browser to http://localhost:3000" -ForegroundColor White
Write-Host "2. Login with test4@example.com / Test123!" -ForegroundColor White
Write-Host "3. Check Dashboard - should show 2 decks (not 0)" -ForegroundColor White
Write-Host "4. Go to Decks page - should show 2 decks" -ForegroundColor White
Write-Host "5. Create new deck with AI generation" -ForegroundColor White
Write-Host "6. Enter 'car brands origin countries' as description" -ForegroundColor White
Write-Host "7. Enable AI generation, set 5 cards" -ForegroundColor White
Write-Host "8. Verify it generates Tesla question" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to start frontend for manual testing"

# Start Frontend
Write-Host "Starting Frontend..." -ForegroundColor Yellow
Set-Location frontend
Start-Process -FilePath "cmd" -ArgumentList "/k", "npm start" -WindowStyle Normal
Set-Location ..

Write-Host "Frontend started at http://localhost:3000" -ForegroundColor Green
Read-Host "Press Enter to exit" 