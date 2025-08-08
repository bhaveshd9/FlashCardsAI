@echo off
echo ========================================
echo Flashcards AI - MongoDB Setup
echo ========================================

echo.
echo Checking if MongoDB is installed...
where mongod >nul 2>nul
if %errorlevel% neq 0 (
    echo MongoDB is not installed or not in PATH
    echo.
    echo Please install MongoDB Community Server from:
    echo https://www.mongodb.com/try/download/community
    echo.
    echo After installation, run this script again.
    pause
    exit /b 1
)

echo MongoDB found! Checking if it's running...
netstat -ano | findstr :27017 >nul
if %errorlevel% equ 0 (
    echo MongoDB is already running on port 27017
) else (
    echo Starting MongoDB...
    net start MongoDB
    if %errorlevel% neq 0 (
        echo Failed to start MongoDB service
        echo Please start MongoDB manually or check installation
        pause
        exit /b 1
    )
    echo MongoDB started successfully!
)

echo.
echo Testing MongoDB connection...
mongo --host localhost --port 27017 --eval "db.runCommand('ping')" >nul 2>nul
if %errorlevel% equ 0 (
    echo MongoDB connection successful!
    echo.
    echo Creating database 'flashcards_ai'...
    mongo --host localhost --port 27017 --eval "use flashcards_ai; db.createCollection('users'); db.createCollection('decks'); db.createCollection('flashcards'); db.createCollection('user_progress');"
    echo Database setup complete!
) else (
    echo Failed to connect to MongoDB
    echo Please check if MongoDB is running properly
)

echo.
echo ========================================
echo Setup complete! You can now run:
echo mvn spring-boot:run
echo ========================================
pause 