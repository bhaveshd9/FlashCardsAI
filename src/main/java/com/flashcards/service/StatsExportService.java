package com.flashcards.service;

import com.flashcards.dto.ExportStatsRequest;
import com.flashcards.model.UserProgress;
import com.flashcards.model.Deck;
import com.flashcards.model.Flashcard;
import com.flashcards.repository.UserProgressRepository;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.FlashcardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsExportService {

    private final UserProgressRepository userProgressRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public StatsExportService(UserProgressRepository userProgressRepository,
                             DeckRepository deckRepository,
                             FlashcardRepository flashcardRepository) {
        this.userProgressRepository = userProgressRepository;
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public String exportStats(String userId, ExportStatsRequest request) {
        List<UserProgress> progressList = userProgressRepository.findByUserId(userId);
        
        // Filter by deck if specified
        if (request.getDeckId() != null) {
            progressList = progressList.stream()
                    .filter(progress -> progress.getDeckId().equals(request.getDeckId()))
                    .collect(Collectors.toList());
        }

        // Filter by date range if specified
        if (request.getDateFrom() != null || request.getDateTo() != null) {
            progressList = filterByDateRange(progressList, request.getDateFrom(), request.getDateTo());
        }

        Map<String, Object> stats = buildStats(userId, progressList);

        if ("csv".equalsIgnoreCase(request.getFormat())) {
            return exportToCSV(stats);
        } else {
            return exportToJSON(stats);
        }
    }

    private List<UserProgress> filterByDateRange(List<UserProgress> progressList, String dateFrom, String dateTo) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom, formatter) : LocalDateTime.MIN;
        LocalDateTime to = dateTo != null ? LocalDateTime.parse(dateTo, formatter) : LocalDateTime.MAX;
        
        return progressList.stream()
                .filter(progress -> {
                    LocalDateTime reviewed = progress.getLastReviewed();
                    return reviewed != null && !reviewed.isBefore(from) && !reviewed.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildStats(String userId, List<UserProgress> progressList) {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic stats
        stats.put("userId", userId);
        stats.put("exportDate", LocalDateTime.now().toString());
        stats.put("totalCardsReviewed", progressList.size());
        
        int totalCorrect = progressList.stream().mapToInt(UserProgress::getCorrectCount).sum();
        int totalIncorrect = progressList.stream().mapToInt(UserProgress::getIncorrectCount).sum();
        int totalReviews = totalCorrect + totalIncorrect;
        
        stats.put("totalCorrect", totalCorrect);
        stats.put("totalIncorrect", totalIncorrect);
        stats.put("totalReviews", totalReviews);
        stats.put("accuracy", totalReviews > 0 ? (double) totalCorrect / totalReviews : 0.0);
        
        // Deck-wise stats
        Map<String, Object> deckStats = new HashMap<>();
        Map<String, List<UserProgress>> progressByDeck = progressList.stream()
                .collect(Collectors.groupingBy(UserProgress::getDeckId));
        
        for (Map.Entry<String, List<UserProgress>> entry : progressByDeck.entrySet()) {
            String deckId = entry.getKey();
            List<UserProgress> deckProgress = entry.getValue();
            
            Deck deck = deckRepository.findById(deckId).orElse(null);
            if (deck != null) {
                Map<String, Object> deckStat = new HashMap<>();
                deckStat.put("deckName", deck.getName());
                deckStat.put("totalCards", deck.getCardCount());
                deckStat.put("cardsReviewed", deckProgress.size());
                
                int deckCorrect = deckProgress.stream().mapToInt(UserProgress::getCorrectCount).sum();
                int deckIncorrect = deckProgress.stream().mapToInt(UserProgress::getIncorrectCount).sum();
                int deckReviews = deckCorrect + deckIncorrect;
                
                deckStat.put("totalCorrect", deckCorrect);
                deckStat.put("totalIncorrect", deckIncorrect);
                deckStat.put("accuracy", deckReviews > 0 ? (double) deckCorrect / deckReviews : 0.0);
                
                deckStats.put(deckId, deckStat);
            }
        }
        
        stats.put("deckStats", deckStats);
        
        return stats;
    }

    private String exportToJSON(Map<String, Object> stats) {
        // Simple JSON export - in production, use Jackson ObjectMapper
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(",\n");
        }
        
        // Remove last comma
        if (json.charAt(json.length() - 2) == ',') {
            json.setLength(json.length() - 2);
            json.append("\n");
        }
        
        json.append("}");
        return json.toString();
    }

    private String exportToCSV(Map<String, Object> stats) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("Metric,Value\n");
        
        // Basic stats
        csv.append("User ID,").append(stats.get("userId")).append("\n");
        csv.append("Export Date,").append(stats.get("exportDate")).append("\n");
        csv.append("Total Cards Reviewed,").append(stats.get("totalCardsReviewed")).append("\n");
        csv.append("Total Correct,").append(stats.get("totalCorrect")).append("\n");
        csv.append("Total Incorrect,").append(stats.get("totalIncorrect")).append("\n");
        csv.append("Total Reviews,").append(stats.get("totalReviews")).append("\n");
        csv.append("Overall Accuracy,").append(stats.get("accuracy")).append("\n");
        
        // Deck stats
        @SuppressWarnings("unchecked")
        Map<String, Object> deckStats = (Map<String, Object>) stats.get("deckStats");
        if (deckStats != null) {
            csv.append("\nDeck Statistics:\n");
            csv.append("Deck ID,Deck Name,Total Cards,Cards Reviewed,Total Correct,Total Incorrect,Accuracy\n");
            
            for (Map.Entry<String, Object> entry : deckStats.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> deckStat = (Map<String, Object>) entry.getValue();
                csv.append(entry.getKey()).append(",")
                   .append(deckStat.get("deckName")).append(",")
                   .append(deckStat.get("totalCards")).append(",")
                   .append(deckStat.get("cardsReviewed")).append(",")
                   .append(deckStat.get("totalCorrect")).append(",")
                   .append(deckStat.get("totalIncorrect")).append(",")
                   .append(deckStat.get("accuracy")).append("\n");
            }
        }
        
        return csv.toString();
    }
} 