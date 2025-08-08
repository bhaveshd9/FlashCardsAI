# FlashCardsAI

FlashCardsAI is a modern, AI-powered flashcard application that helps users create, manage, and study digital flashcards. The application features intelligent content extraction from various sources, spaced repetition algorithms, and a user-friendly interface.

## Features

- **AI-Powered Content Extraction**: Extract content from text, URLs, and files (PDF, TXT, Markdown)
- **Smart Flashcard Generation**: Automatically generate flashcards from your content
- **Spaced Repetition**: Optimized study sessions using spaced repetition algorithms
- **User Authentication**: Secure user accounts with JWT authentication
- **Responsive Design**: Works on desktop and mobile devices
- **Activity Tracking**: Monitor your study progress and activity history

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Database**: MongoDB
- **Authentication**: JWT
- **AI Integration**: OpenAI API (for content processing)
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **State Management**: React Context API
- **Styling**: Tailwind CSS
- **Routing**: React Router
- **HTTP Client**: Axios

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 16.x or higher
- MongoDB 6.0 or higher
- Maven 3.8 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/flashcardsai.git
   cd flashcardsai
   ```

2. **Backend Setup**
   ```bash
   cd backend
   mvn clean install
   ```

3. **Frontend Setup**
   ```bash
   cd ../frontend
   npm install
   ```

4. **Configuration**
   - Copy `.env.example` to `.env` and update the values
   - Set up your OpenAI API key in the backend configuration

5. **Running the Application**
   - Start MongoDB
   - Run the Spring Boot application
   - In a new terminal, start the React development server:
     ```bash
     cd frontend
     npm start
     ```

## Project Structure

```
flashcardsai/
├── frontend/               # React frontend
│   ├── public/             # Static files
│   ├── src/                # Source files
│   │   ├── components/     # React components
│   │   ├── context/        # React context providers
│   │   ├── services/       # API services
│   │   └── App.js          # Main App component
│   └── package.json        # Frontend dependencies
│
├── src/
│   ├── main/
│   │   ├── java/com/flashcards/
│   │   │   ├── config/     # Configuration classes
│   │   │   ├── controller/ # REST controllers
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   ├── model/      # Domain models
│   │   │   ├── repository/ # Data access layer
│   │   │   ├── security/   # Security configuration
│   │   │   ├── service/    # Business logic
│   │   │   └── FlashCardsAiApplication.java
│   │   └── resources/      # Configuration files
│   └── test/               # Tests
│
├── .gitignore             # Git ignore file
└── README.md              # This file
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with ❤️ using modern web technologies
- Special thanks to all contributors
- Icons by [Lucide](https://lucide.dev/)

---

Happy Learning! 🚀
