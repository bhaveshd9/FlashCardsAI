package com.flashcards.controller;

import com.flashcards.dto.AiGenerationRequest;
import com.flashcards.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<AiService.FlashcardData>> generateFlashcards(@RequestBody AiGenerationRequest request) {
        try {
            System.out.println("AI Generation Request: " + request.getText() + ", Cards: " + request.getNumberOfCards());
            List<AiService.FlashcardData> flashcards = aiService.generateFlashcards(request);
            System.out.println("Generated " + flashcards.size() + " flashcards");
            for (AiService.FlashcardData card : flashcards) {
                System.out.println("Card: " + card.getFront() + " -> " + card.getBack());
            }
            return ResponseEntity.ok(flashcards);
        } catch (Exception e) {
            System.err.println("AI Generation Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
} 