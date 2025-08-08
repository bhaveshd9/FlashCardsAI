package com.flashcards.dto;

import lombok.Data;

@Data
public class ExportStatsRequest {
    private String format = "json"; // json, csv
    private String deckId; // optional, if null export all decks
    private String dateFrom; // optional, ISO date format
    private String dateTo; // optional, ISO date format
} 