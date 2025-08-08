package com.flashcards.service;

import com.flashcards.model.UserProgress;
import com.flashcards.repository.UserProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {

    private final UserProgressRepository userProgressRepository;

    public SpacedRepetitionService(UserProgressRepository userProgressRepository) {
        this.userProgressRepository = userProgressRepository;
    }

    public void updateProgress(String userId, String flashcardId, String deckId, int score) {
        UserProgress progress = userProgressRepository
                .findByUserIdAndFlashcardId(userId, flashcardId)
                .orElse(new UserProgress(userId, flashcardId, deckId));

        // Update counts
        if (score >= 3) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
            progress.setConsecutiveCorrect(progress.getConsecutiveCorrect() + 1);
        } else {
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
            progress.setConsecutiveCorrect(0);
        }

        // SM-2 Algorithm
        updateIntervalAndEaseFactor(progress, score);
        progress.setLastReviewScore(score);
        progress.setLastReviewed(LocalDateTime.now());
        progress.setNextReviewDate(calculateNextReviewDate(progress));

        userProgressRepository.save(progress);
    }

    private void updateIntervalAndEaseFactor(UserProgress progress, int score) {
        double easeFactor = progress.getEaseFactor();
        int interval = progress.getInterval();

        // Update ease factor
        easeFactor = easeFactor + (0.1 - (5 - score) * (0.08 + (5 - score) * 0.02));
        easeFactor = Math.max(1.3, easeFactor); // Minimum ease factor
        progress.setEaseFactor(easeFactor);

        // Update interval
        if (score < 3) {
            // Failed - reset to 1 day
            interval = 1;
        } else if (interval == 1) {
            // First successful review
            interval = 6;
        } else {
            // Subsequent successful reviews
            interval = (int) Math.round(interval * easeFactor);
        }

        progress.setInterval(interval);
    }

    private LocalDateTime calculateNextReviewDate(UserProgress progress) {
        return LocalDateTime.now().plusDays(progress.getInterval());
    }

    public boolean isCardDue(UserProgress progress) {
        return progress.getNextReviewDate() == null || 
               LocalDateTime.now().isAfter(progress.getNextReviewDate());
    }

    public double getCardDifficulty(UserProgress progress) {
        if (progress.getCorrectCount() + progress.getIncorrectCount() == 0) {
            return 1.0; // New card
        }
        
        double totalReviews = progress.getCorrectCount() + progress.getIncorrectCount();
        double accuracy = (double) progress.getCorrectCount() / totalReviews;
        
        // Return difficulty level 1-5 based on accuracy
        if (accuracy >= 0.9) return 1.0; // Very easy
        if (accuracy >= 0.7) return 2.0; // Easy
        if (accuracy >= 0.5) return 3.0; // Medium
        if (accuracy >= 0.3) return 4.0; // Hard
        return 5.0; // Very hard
    }
} 