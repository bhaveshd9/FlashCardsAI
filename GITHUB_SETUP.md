# GitHub Setup Guide

This guide will help you set up a GitHub repository for the FlashCardsAI project.

## Prerequisites
- Git installed on your system
- GitHub account
- The FlashCardsAI project ready

## Step 1: Initialize Git Repository

Open your terminal in the project root directory and run:

```bash
# Initialize git repository
git init

# Add all files to staging
git add .

# Create initial commit
git commit -m "Initial commit: FlashCardsAI - Complete Flashcard Application with AI Generation"
```

## Step 2: Create .gitignore File

Create a `.gitignore` file in the project root with the following content:

```gitignore
# Java
*.class
*.jar
*.war
*.ear
*.log
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

# Spring Boot
application-local.properties
application-dev.properties

# Environment Variables
.env
.env.local
.env.development
.env.test
.env.production

# Node.js
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*
.pnpm-debug.log*

# React
build/
dist/
.cache/
.parcel-cache/

# IDE
.idea/
.vscode/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Runtime data
pids/
*.pid
*.seed
*.pid.lock

# Coverage directory used by tools like istanbul
coverage/

# Dependency directories
jspm_packages/

# Optional npm cache directory
.npm

# Optional REPL history
.node_repl_history

# Output of 'npm pack'
*.tgz

# Yarn Integrity file
.yarn-integrity

# dotenv environment variables file
.env

# next.js build output
.next

# Nuxt.js build / generate output
.nuxt
dist

# Storybook build outputs
.out
.storybook-out

# Temporary folders
tmp/
temp/

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# MongoDB
*.lock

# API Keys and Secrets
.env
.env.local
.env.development.local
.env.test.local
.env.production.local
```

## Step 3: Create GitHub Repository

1. Go to [GitHub](https://github.com) and sign in
2. Click the "+" icon in the top right corner
3. Select "New repository"
4. Fill in the repository details:
   - **Repository name**: `FlashCardsAI`
   - **Description**: `A comprehensive flashcard application with AI-powered content generation, built with Spring Boot and React`
   - **Visibility**: Choose Public or Private
   - **Do NOT** initialize with README, .gitignore, or license (we'll add these manually)
5. Click "Create repository"

## Step 4: Connect Local Repository to GitHub

After creating the repository, GitHub will show you the commands. Run these in your terminal:

```bash
# Add the remote origin (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/FlashCardsAI.git

# Set the main branch (if not already set)
git branch -M main

# Push to GitHub
git push -u origin main
```

## Step 5: Create README.md

Create a comprehensive README.md file:

```markdown
# FlashCardsAI

A comprehensive flashcard application with AI-powered content generation, built with Spring Boot and React.

## Features

- **User Authentication**: Secure login/registration with JWT
- **Deck Management**: Create, edit, delete, and organize flashcard decks
- **AI Content Generation**: Generate flashcards from text, URLs, or PDFs using OpenAI
- **Study Modes**: Multiple study modes including flip cards and quizzes
- **Progress Tracking**: Track study progress and statistics
- **Admin Panel**: User and content management for administrators
- **Feedback System**: User feedback collection and management

## Tech Stack

### Backend
- **Spring Boot 3.2.0**: Main framework
- **Spring Security**: Authentication and authorization
- **Spring Data MongoDB**: Database operations
- **JWT**: Token-based authentication
- **OpenAI API**: AI content generation
- **Maven**: Build tool

### Frontend
- **React 18**: UI framework
- **React Router**: Navigation
- **Axios**: HTTP client
- **Tailwind CSS**: Styling
- **Lucide React**: Icons
- **React Hot Toast**: Notifications

### Database
- **MongoDB**: NoSQL database

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- MongoDB (local or Atlas)
- OpenAI API key

### Backend Setup
1. Clone the repository
2. Create `.env` file in the root directory:
   ```
   MONGODB_URI=your_mongodb_connection_string
   JWT_SECRET=your_jwt_secret
   OPENAI_API_KEY=your_openai_api_key
   EMAIL_HOST=smtp.gmail.com
   EMAIL_PORT=587
   EMAIL_USERNAME=your_email
   EMAIL_PASSWORD=your_email_password
   ```
3. Run the backend:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```

## API Documentation

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Decks
- `GET /api/decks/my` - Get user's decks
- `POST /api/decks` - Create new deck
- `PUT /api/decks/{id}` - Update deck
- `DELETE /api/decks/{id}` - Delete deck

### Flashcards
- `GET /api/flashcards/deck/{deckId}` - Get deck's flashcards
- `POST /api/flashcards/deck/{deckId}` - Add flashcard to deck
- `PUT /api/flashcards/{id}` - Update flashcard
- `DELETE /api/flashcards/{id}` - Delete flashcard

### AI Generation
- `POST /api/ai/generate` - Generate flashcards using AI

### Study
- `POST /api/study/session` - Record study session
- `GET /api/study/stats` - Get study statistics

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, email support@flashcardsai.com or create an issue in the GitHub repository.
```

## Step 6: Add README and Push

```bash
# Add the README
git add README.md
git commit -m "Add comprehensive README"

# Push the changes
git push origin main
```

## Step 7: Set Up Branch Protection (Optional)

1. Go to your repository on GitHub
2. Click "Settings" tab
3. Click "Branches" in the left sidebar
4. Click "Add rule"
5. Set branch name pattern to `main`
6. Enable "Require a pull request before merging"
7. Enable "Require status checks to pass before merging"
8. Click "Create"

## Step 8: Set Up GitHub Actions (Optional)

Create `.github/workflows/ci.yml` for continuous integration:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run backend tests
      run: mvn test
      
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
        
    - name: Install frontend dependencies
      run: |
        cd frontend
        npm ci
        
    - name: Run frontend tests
      run: |
        cd frontend
        npm test -- --watchAll=false
```

## Step 9: Verify Setup

1. Visit your GitHub repository
2. Verify all files are uploaded correctly
3. Check that the README displays properly
4. Test the clone URL to ensure others can clone your repository

## Additional Tips

- **Issues**: Use GitHub Issues to track bugs and feature requests
- **Projects**: Use GitHub Projects for project management
- **Wiki**: Consider adding a wiki for detailed documentation
- **Releases**: Create releases for stable versions
- **Security**: Enable security alerts and dependency scanning

## Troubleshooting

### If you get authentication errors:
```bash
# Configure Git with your GitHub credentials
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Use GitHub CLI or Personal Access Token for authentication
```

### If you need to update the remote URL:
```bash
git remote set-url origin https://github.com/YOUR_USERNAME/FlashCardsAI.git
```

### If you need to force push (be careful):
```bash
git push -f origin main
```

Your FlashCardsAI project is now successfully hosted on GitHub! 🎉 