# FlashCards AI - Comprehensive Test Cases

## Test Environment Setup

### Prerequisites
- Backend running on port 8080
- Frontend running on port 3000
- MongoDB running on port 27017
- Valid OpenAI API key (optional for AI tests)

### Test Data Setup
```bash
# Create test user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

## 1. Dashboard Stats Tests

### Test 1.1: Dashboard Loads with Real Data
**Objective**: Verify dashboard shows actual statistics instead of hardcoded values

**Steps**:
1. Login to the application
2. Navigate to Dashboard
3. Create a deck with flashcards
4. Study some flashcards
5. Refresh dashboard

**Expected Results**:
- Total Decks should show actual number of decks
- Study Streak should show calculated streak
- Cards Studied Today should show actual count
- Recent Activity should show actual activities

**Test Commands**:
```bash
# Create test deck
curl -X POST http://localhost:8080/api/decks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Deck",
    "description": "Test Description",
    "tags": ["test"],
    "isPublic": false
  }'

# Add flashcards
curl -X POST http://localhost:8080/api/flashcards/deck/DECK_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "front": "Test Question",
    "back": "Test Answer"
  }'

# Record study session
curl -X POST http://localhost:8080/api/study/session \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "flashcardId": "FLASHCARD_ID",
    "deckId": "DECK_ID",
    "score": 3
  }'

# Check dashboard stats
curl -X GET http://localhost:8080/api/study/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test 1.2: Stats Update After Study Session
**Objective**: Verify stats update immediately after study activities

**Steps**:
1. Note current dashboard stats
2. Complete a study session
3. Return to dashboard
4. Verify stats have updated

**Expected Results**:
- Cards Studied Today should increase
- Study Streak should update if applicable
- Recent Activity should show new entries

## 2. Deck List Card Count Tests

### Test 2.1: Correct Card Count Display
**Objective**: Verify deck list shows correct number of flashcards

**Steps**:
1. Create a new deck
2. Add multiple flashcards
3. Check deck list display
4. Verify card count matches actual count

**Expected Results**:
- Card count should match actual number of flashcards
- Count should update immediately after adding/removing cards

**Test Commands**:
```bash
# Get user decks with flashcard counts
curl -X GET http://localhost:8080/api/decks/my \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Verify flashcard count in response
# Should include flashcardCount field
```

### Test 2.2: Card Count Updates After AI Generation
**Objective**: Verify card count updates after AI-generated flashcards

**Steps**:
1. Create deck with AI generation enabled
2. Request 5 cards via AI
3. Check deck list shows 5 cards
4. Verify cards are actually created

**Expected Results**:
- Card count should show exactly 5 cards
- All 5 cards should be accessible in deck detail

## 3. Title/Description Display Tests

### Test 3.1: Correct Title Display
**Objective**: Verify deck titles display correctly instead of descriptions

**Steps**:
1. Create deck with distinct title and description
2. Check deck list display
3. Check deck detail page header

**Expected Results**:
- Deck list should show deck name (not description)
- Deck detail page should show name as main title
- Description should appear below title

**Test Commands**:
```bash
# Create deck with clear title/description
curl -X POST http://localhost:8080/api/decks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "My Test Deck",
    "description": "This is a test description for the deck",
    "tags": ["test"],
    "isPublic": false
  }'

# Verify response shows correct name field
```

## 4. Quiz Functionality Tests

### Test 4.1: Quiz Creation
**Objective**: Verify quiz can be created from deck

**Steps**:
1. Create deck with multiple flashcards
2. Navigate to deck detail
3. Click "Quiz" button
4. Verify quiz starts

**Expected Results**:
- Quiz should start with questions from deck
- Questions should be multiple choice
- Options should be shuffled

**Test Commands**:
```bash
# Create quiz
curl -X POST "http://localhost:8080/api/quiz/deck/DECK_ID?questions=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Verify quiz response includes questions array
```

### Test 4.2: Quiz Submission
**Objective**: Verify quiz can be submitted and graded

**Steps**:
1. Start a quiz
2. Answer all questions
3. Submit quiz
4. Check results

**Expected Results**:
- Quiz should submit successfully
- Results should show score and correct/incorrect answers
- Quiz history should be saved

**Test Commands**:
```bash
# Submit quiz answers
curl -X POST http://localhost:8080/api/quiz/QUIZ_ID/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "FLASHCARD_ID_1": 0,
    "FLASHCARD_ID_2": 2
  }'

# Check quiz history
curl -X GET http://localhost:8080/api/quiz/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 5. Document Upload Tests

### Test 5.1: Text Input AI Generation
**Objective**: Verify AI generation works with text input

**Steps**:
1. Create new deck
2. Enable AI generation
3. Select "Text Input" as content source
4. Enter text content
5. Set number of cards
6. Create deck

**Expected Results**:
- Deck should be created
- AI should generate specified number of cards
- Cards should be relevant to input text

### Test 5.2: URL Content Extraction
**Objective**: Verify URL-based content extraction (mock test)

**Steps**:
1. Create new deck
2. Enable AI generation
3. Select "URL/Website" as content source
4. Enter a URL
5. Create deck

**Expected Results**:
- UI should show URL input field
- Placeholder text should guide user
- (Note: Actual URL processing would need backend implementation)

### Test 5.3: File Upload Interface
**Objective**: Verify file upload interface works

**Steps**:
1. Create new deck
2. Enable AI generation
3. Select "Upload Document" as content source
4. Verify file input appears
5. Check supported file types

**Expected Results**:
- File input should appear
- Should accept .pdf, .txt, .doc, .docx
- Helpful text should guide user

## 6. Study Mode Tests

### Test 6.1: Simple Review Mode
**Objective**: Verify study mode works without self-grading

**Steps**:
1. Navigate to a deck
2. Click "Study" button
3. Flip through cards
4. Use Previous/Next navigation

**Expected Results**:
- No grading buttons should appear
- Simple Previous/Next navigation
- Cards should flip smoothly
- Progress should be tracked

### Test 6.2: Study Session Recording
**Objective**: Verify study sessions are recorded

**Steps**:
1. Complete a study session
2. Check dashboard stats
3. Verify cards studied today increased

**Expected Results**:
- Study sessions should be recorded
- Stats should update accordingly
- No errors in console

## 7. Multilanguage Support Tests

### Test 7.1: Language Selector Display
**Objective**: Verify language selector appears in navbar

**Steps**:
1. Login to application
2. Check navbar for language selector
3. Click language selector
4. Verify language options appear

**Expected Results**:
- Language selector should be visible
- Should show current language flag
- Dropdown should show multiple language options

### Test 7.2: Language Selection
**Objective**: Verify language can be changed

**Steps**:
1. Click language selector
2. Select different language
3. Verify selection is saved
4. Check console for language change log

**Expected Results**:
- Language selection should work
- Console should log language change
- (Note: Actual translations would need i18n implementation)

## 8. Stats Update Tests

### Test 8.1: Real-time Stats Updates
**Objective**: Verify stats update in real-time across components

**Steps**:
1. Note current stats on dashboard
2. Complete study session
3. Check deck detail stats
4. Return to dashboard
5. Verify all stats updated

**Expected Results**:
- Dashboard stats should update
- Deck detail stats should update
- All components should show consistent data

**Test Commands**:
```bash
# Check deck stats
curl -X GET http://localhost:8080/api/decks/DECK_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Check study progress
curl -X GET http://localhost:8080/api/study/progress/DECK_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 9. Error Handling Tests

### Test 9.1: Network Error Handling
**Objective**: Verify graceful handling of network errors

**Steps**:
1. Disconnect internet
2. Try to perform various actions
3. Reconnect internet
4. Verify recovery

**Expected Results**:
- Should show appropriate error messages
- Should not crash application
- Should recover when connection restored

### Test 9.2: Invalid Input Handling
**Objective**: Verify proper validation and error messages

**Steps**:
1. Try to create deck with empty title
2. Try to submit quiz without answers
3. Try to access non-existent deck

**Expected Results**:
- Should show validation errors
- Should prevent invalid submissions
- Should show appropriate error messages

## 10. Performance Tests

### Test 10.1: Large Deck Performance
**Objective**: Verify performance with large decks

**Steps**:
1. Create deck with 100+ flashcards
2. Load deck detail page
3. Start study session
4. Monitor performance

**Expected Results**:
- Should load within reasonable time
- Should not freeze or crash
- Should maintain responsiveness

### Test 10.2: Multiple Concurrent Users
**Objective**: Verify system handles multiple users

**Steps**:
1. Open multiple browser tabs
2. Login with different users
3. Perform simultaneous operations
4. Monitor system behavior

**Expected Results**:
- Should handle multiple users
- Should not interfere with each other
- Should maintain data consistency

## 11. Security Tests

### Test 11.1: Authentication
**Objective**: Verify proper authentication

**Steps**:
1. Try to access protected routes without login
2. Try to access other user's data
3. Verify JWT token validation

**Expected Results**:
- Should redirect to login
- Should prevent unauthorized access
- Should validate tokens properly

### Test 11.2: Input Validation
**Objective**: Verify input sanitization

**Steps**:
1. Try to inject SQL/JavaScript in inputs
2. Try to upload malicious files
3. Try to use special characters

**Expected Results**:
- Should sanitize inputs
- Should prevent injection attacks
- Should handle special characters properly

## 12. Cross-browser Tests

### Test 12.1: Browser Compatibility
**Objective**: Verify compatibility across browsers

**Steps**:
1. Test in Chrome
2. Test in Firefox
3. Test in Safari
4. Test in Edge

**Expected Results**:
- Should work consistently across browsers
- Should maintain functionality
- Should handle browser-specific features

## Automated Test Script

```bash
#!/bin/bash
# automated-test.sh

echo "Starting FlashCards AI Comprehensive Tests..."

# Setup
BASE_URL="http://localhost:8080/api"
FRONTEND_URL="http://localhost:3000"

# Test 1: Authentication
echo "Testing authentication..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test123!"}')

if [[ $REGISTER_RESPONSE == *"success"* ]]; then
  echo "✅ Registration successful"
else
  echo "❌ Registration failed"
fi

# Test 2: Login
echo "Testing login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [[ -n $TOKEN ]]; then
  echo "✅ Login successful"
else
  echo "❌ Login failed"
fi

# Test 3: Create Deck
echo "Testing deck creation..."
DECK_RESPONSE=$(curl -s -X POST "$BASE_URL/decks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Test Deck","description":"Test Description","tags":["test"],"isPublic":false}')

DECK_ID=$(echo $DECK_RESPONSE | grep -o '"_id":"[^"]*"' | cut -d'"' -f4)

if [[ -n $DECK_ID ]]; then
  echo "✅ Deck creation successful"
else
  echo "❌ Deck creation failed"
fi

# Test 4: Add Flashcards
echo "Testing flashcard creation..."
FLASHCARD_RESPONSE=$(curl -s -X POST "$BASE_URL/flashcards/deck/$DECK_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"front":"Test Question","back":"Test Answer"}')

if [[ $FLASHCARD_RESPONSE == *"Test Question"* ]]; then
  echo "✅ Flashcard creation successful"
else
  echo "❌ Flashcard creation failed"
fi

# Test 5: Dashboard Stats
echo "Testing dashboard stats..."
STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/study/stats" \
  -H "Authorization: Bearer $TOKEN")

if [[ $STATS_RESPONSE == *"totalCards"* ]]; then
  echo "✅ Dashboard stats working"
else
  echo "❌ Dashboard stats failed"
fi

# Test 6: Quiz Creation
echo "Testing quiz creation..."
QUIZ_RESPONSE=$(curl -s -X POST "$BASE_URL/quiz/deck/$DECK_ID?questions=1" \
  -H "Authorization: Bearer $TOKEN")

QUIZ_ID=$(echo $QUIZ_RESPONSE | grep -o '"_id":"[^"]*"' | cut -d'"' -f4)

if [[ -n $QUIZ_ID ]]; then
  echo "✅ Quiz creation successful"
else
  echo "❌ Quiz creation failed"
fi

echo "All tests completed!"
```

## Test Results Template

| Test Category | Test Case | Status | Notes |
|---------------|-----------|--------|-------|
| Dashboard | Real-time stats | ✅/❌ | |
| Deck List | Card count display | ✅/❌ | |
| Title Display | Correct field usage | ✅/❌ | |
| Quiz | Creation and submission | ✅/❌ | |
| Document Upload | UI components | ✅/❌ | |
| Study Mode | Review functionality | ✅/❌ | |
| Multilanguage | Language selector | ✅/❌ | |
| Stats Update | Real-time updates | ✅/❌ | |
| Error Handling | Network errors | ✅/❌ | |
| Performance | Large decks | ✅/❌ | |
| Security | Authentication | ✅/❌ | |
| Cross-browser | Compatibility | ✅/❌ | |

## Running the Tests

1. **Manual Testing**: Follow each test case step by step
2. **Automated Testing**: Run the automated test script
3. **Browser Testing**: Test in multiple browsers
4. **Performance Testing**: Monitor with browser dev tools

## Success Criteria

All tests should pass with:
- ✅ No console errors
- ✅ Proper functionality
- ✅ Good user experience
- ✅ Responsive design
- ✅ Data consistency
- ✅ Error handling
- ✅ Security compliance 