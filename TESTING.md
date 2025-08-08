# FlashCards AI - Testing Guide

## Overview
This guide provides comprehensive testing procedures for the FlashCards AI application, covering both backend and frontend functionality.

## Backend Testing

### Unit Tests
Run backend unit tests:
```bash
mvn test
```

### Integration Tests
Test API endpoints:
```bash
# Test authentication
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Test AI generation (with authentication)
curl -X POST http://localhost:8080/api/ai/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"text":"car brands","numberOfCards":5,"topic":"Automotive","difficulty":"medium"}'

# Test quiz creation
curl -X POST "http://localhost:8080/api/quiz/deck/DECK_ID?questions=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Frontend Testing

### Manual Testing Checklist

#### 1. User Authentication
- [ ] User registration with valid data
- [ ] User login with correct credentials
- [ ] User login with incorrect credentials
- [ ] JWT token persistence across page refreshes
- [ ] Logout functionality

#### 2. Deck Management
- [ ] Create new deck with basic information
- [ ] Create new deck with AI generation enabled
- [ ] Edit deck title, description, and tags
- [ ] Delete deck
- [ ] View deck list
- [ ] Navigate to deck detail

#### 3. AI Flashcard Generation (NEW: Moved to Deck Creation)
- [ ] Create deck with AI generation checkbox enabled
- [ ] Enter text content for AI generation
- [ ] Set number of cards (1-20)
- [ ] Verify exact number of cards generated
- [ ] Test with different content types:
  - Topic-based: "car brands", "JavaScript basics"
  - Sentence-based: Paste paragraphs with periods
  - Mixed content

#### 4. Flashcard Management
- [ ] Add manual flashcards to existing deck
- [ ] Edit flashcard front and back
- [ ] Delete flashcards
- [ ] View flashcards in deck detail

#### 5. Study Mode
- [ ] Start study session
- [ ] Flip flashcards
- [ ] Grade performance (1-5 scale)
- [ ] View progress statistics
- [ ] Complete study session
- [ ] View session results

#### 6. Quiz Mode (NEW)
- [ ] Start quiz from deck detail page
- [ ] Answer MCQ questions (4 options each)
- [ ] Navigate between questions
- [ ] Submit quiz
- [ ] View detailed results with correct/incorrect answers
- [ ] Review quiz history
- [ ] Take quiz again

#### 7. Progress Tracking
- [ ] View study statistics
- [ ] Track completion percentages
- [ ] Monitor daily study streaks

#### 8. Admin Panel (Admin users only)
- [ ] View overall statistics
- [ ] Manage users
- [ ] Manage decks
- [ ] Review feedback

#### 9. Feedback System
- [ ] Submit feedback
- [ ] View feedback history
- [ ] Check feedback status

## Common Issues and Solutions

### AI Generation Issues
**Problem**: Generating more cards than requested
- **Solution**: Fixed in AiService.getRandomCards() method to return exact count

**Problem**: AI not using provided text content
- **Solution**: Updated generateSimpleFlashcards() to properly handle text input

**Problem**: AI generation in wrong location
- **Solution**: Moved AI generation to deck creation for better UX

### Authentication Issues
**Problem**: "Failed to find user" error
- **Solution**: Check if user exists in MongoDB and JWT token is valid

**Problem**: 401 Unauthorized errors
- **Solution**: Ensure JWT token is included in Authorization header

### Database Issues
**Problem**: MongoDB connection refused
- **Solution**: Ensure MongoDB service is running on port 27017

**Problem**: Database name not found
- **Solution**: Check application.properties for correct database configuration

### Frontend Issues
**Problem**: Cards not displaying content
- **Solution**: Fixed field name mapping between frontend (front/back) and backend (question/answer)

**Problem**: Mirrored text in study mode
- **Solution**: Removed problematic CSS transforms

## Performance Testing

### Load Testing
```bash
# Test with multiple concurrent users
ab -n 1000 -c 10 http://localhost:8080/api/auth/test
```

### Memory Usage
Monitor application memory usage during:
- Large deck creation
- AI generation with many cards
- Multiple concurrent study sessions

## Security Testing

### Authentication
- [ ] Test JWT token expiration
- [ ] Verify user authorization for deck operations
- [ ] Test password strength requirements

### Input Validation
- [ ] Test XSS prevention
- [ ] Verify SQL injection protection
- [ ] Test input sanitization

## Quiz System Testing (NEW)

### Quiz Creation
- [ ] Verify quiz generates correct number of questions
- [ ] Test MCQ generation with 4 options
- [ ] Ensure correct answer is included in options

### Quiz Taking
- [ ] Test answer selection
- [ ] Verify navigation between questions
- [ ] Test quiz submission

### Quiz Results
- [ ] Verify score calculation
- [ ] Test result display with correct/incorrect indicators
- [ ] Check quiz history storage

### Quiz History
- [ ] Test quiz history retrieval
- [ ] Verify deck-specific quiz history
- [ ] Test quiz deletion

## Debug Mode

Enable debug logging in `application.properties`:
```properties
logging.level.com.flashcards=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Sample Test Data

### Test User
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

### Test Deck
```json
{
  "title": "Test Deck",
  "description": "A test deck for testing",
  "tags": ["test", "sample"],
  "useAI": true,
  "aiText": "car brands",
  "aiNumberOfCards": 5
}
```

### Test AI Generation
```json
{
  "text": "car brands",
  "numberOfCards": 5,
  "topic": "Automotive",
  "difficulty": "medium"
}
```

## Continuous Integration

### Automated Testing
Set up CI/CD pipeline to run:
1. Unit tests
2. Integration tests
3. Frontend build tests
4. Security scans

### Test Reports
Generate test reports:
```bash
mvn surefire-report:report
```

## Feedback System Details

### Where Feedback Goes
1. **Storage**: MongoDB `feedback` collection
2. **Flow**: User submits → Admin reviews → Status updated
3. **Categories**: General, Bug Report, Feature Request, Improvement
4. **Status**: Pending, In Progress, Resolved, Closed

### Feedback Testing
- [ ] Submit feedback with all categories
- [ ] Test feedback validation
- [ ] Verify admin can update feedback status
- [ ] Test feedback history display

## Recent Updates (Latest)

### AI Generation Improvements
- ✅ Fixed exact card count generation
- ✅ Improved text content processing
- ✅ Moved AI generation to deck creation
- ✅ Enhanced topic-based generation

### Quiz System Implementation
- ✅ Complete MCQ quiz functionality
- ✅ Quiz history and progress tracking
- ✅ Detailed results with answer review
- ✅ Integration with deck management

### Bug Fixes
- ✅ Fixed mirrored text in study mode
- ✅ Resolved field name mapping issues
- ✅ Improved authentication flow
- ✅ Enhanced error handling 