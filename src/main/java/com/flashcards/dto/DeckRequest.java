package com.flashcards.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeckRequest {
    private String name;
    private String description;
    private boolean isPublic;
    private List<String> tags;
    private String coverImageUrl;
} 