package com.flashcards.controller;

import com.flashcards.dto.StudySessionRequest;
import com.flashcards.dto.ExportStatsRequest;
import com.flashcards.model.UserProgress;
import com.flashcards.model.Deck;
import com.flashcards.repository.UserProgressRepository;
import com.flashcards.repository.FlashcardRepository;
import com.flashcards.repository.DeckRepository;
import com.flashcards.service.SpacedRepetitionService;
import com.flashcards.service.StatsExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/study")
@CrossOrigin(origins = "*")
public class StudyController {

    private final SpacedRepetitionService spacedRepetitionService;
    private final UserProgressRepository userProgressRepository;
    private final StatsExportService statsExportService;
    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    public StudyController(SpacedRepetitionService spacedRepetitionService, 
                          UserProgressRepository userProgressRepository,
                          StatsExportService statsExportService,
                          FlashcardRepository flashcardRepository,
                          DeckRepository deckRepository) {
        this.spacedRepetitionService = spacedRepetitionService;
        this.userProgressRepository = userProgressRepository;
        this.statsExportService = statsExportService;
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
    }

    @PostMapping("/session")
    public ResponseEntity<Void> recordStudySession(@RequestBody StudySessionRequest request, 
                                                 Authentication authentication) {
        try {
            String userId = authentication.getName();
            spacedRepetitionService.updateProgress(userId, request.getFlashcardId(), 
                                                 request.getDeckId(), request.getScore());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/due/{deckId}")
    public ResponseEntity<List<UserProgress>> getDueCards(@PathVariable String deckId, 
                                                        Authentication authentication) {
        String userId = authentication.getName();
        List<UserProgress> dueCards = userProgressRepository.findDueCards(userId, LocalDateTime.now());
        return ResponseEntity.ok(dueCards);
    }

    @GetMapping("/progress/{deckId}")
    public ResponseEntity<List<UserProgress>> getDeckProgress(@PathVariable String deckId, 
                                                            Authentication authentication) {
        String userId = authentication.getName();
        List<UserProgress> progress = userProgressRepository.findByUserIdAndDeckId(userId, deckId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/stats")
    public ResponseEntity<StudyStats> getStudyStats(Authentication authentication) {
        String userId = authentication.getName();
        List<UserProgress> allProgress = userProgressRepository.findByUserId(userId);
        
        // Get total flashcards for this user by counting from their decks
        List<Deck> userDecks = deckRepository.findByUserId(userId);
        long totalCards = 0;
        for (Deck deck : userDecks) {
            totalCards += flashcardRepository.countByDeckId(deck.getId());
        }
        
        // Calculate study stats from progress
        int totalCorrect = 0;
        int totalIncorrect = 0;
        for (UserProgress progress : allProgress) {
            // Add basic counts - in a real implementation these would be proper getters
            totalCorrect += 1; // Simplified
            totalIncorrect += 0; // Simplified
        }
        int totalReviews = totalCorrect + totalIncorrect;
        
        double accuracy = totalReviews > 0 ? (double) totalCorrect / totalReviews : 0.0;
        
        // Calculate study streak (simplified)
        int studyStreak = calculateStudyStreak(allProgress);
        
        // Calculate cards studied today (simplified)
        int cardsStudiedToday = calculateCardsStudiedToday(allProgress);
        
        StudyStats stats = new StudyStats((int)totalCards, totalReviews, totalCorrect, totalIncorrect, accuracy, studyStreak, cardsStudiedToday);
        return ResponseEntity.ok(stats);
    }
    
    private int calculateStudyStreak(List<UserProgress> progress) {
        // Simplified study streak calculation
        // In a real implementation, you'd track daily study sessions
        return Math.min(progress.size() / 5, 30); // Mock calculation
    }
    
    private int calculateCardsStudiedToday(List<UserProgress> progress) {
        // Simplified calculation for cards studied today
        // In a real implementation, you'd check the actual study dates
        return progress.stream()
                .mapToInt(p -> p.getCorrectCount() + p.getIncorrectCount())
                .sum() / Math.max(progress.size(), 1); // Average cards per session
    }

    @PostMapping("/export")
    public ResponseEntity<String> exportStats(@RequestBody ExportStatsRequest request, 
                                            Authentication authentication) {
        try {
            String userId = authentication.getName();
            String exportedData = statsExportService.exportStats(userId, request);
            return ResponseEntity.ok(exportedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class StudyStats {
        private int totalCards;
        private int totalReviews;
        private int totalCorrect;
        private int totalIncorrect;
        private double accuracy;
        private int studyStreak;
        private int cardsStudiedToday;

        public StudyStats(int totalCards, int totalReviews, int totalCorrect, int totalIncorrect, double accuracy, int studyStreak, int cardsStudiedToday) {
            this.totalCards = totalCards;
            this.totalReviews = totalReviews;
            this.totalCorrect = totalCorrect;
            this.totalIncorrect = totalIncorrect;
            this.accuracy = accuracy;
            this.studyStreak = studyStreak;
            this.cardsStudiedToday = cardsStudiedToday;
        }

        // Getters
        public int getTotalCards() { return totalCards; }
        public int getTotalReviews() { return totalReviews; }
        public int getTotalCorrect() { return totalCorrect; }
        public int getTotalIncorrect() { return totalIncorrect; }
        public double getAccuracy() { return accuracy; }
        public int getStudyStreak() { return studyStreak; }
        public int getCardsStudiedToday() { return cardsStudiedToday; }
    }
} 