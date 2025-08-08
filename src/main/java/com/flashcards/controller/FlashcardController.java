package com.flashcards.controller;

import com.flashcards.dto.FlashcardRequest;
import com.flashcards.model.Flashcard;
import com.flashcards.service.FlashcardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = "*")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping("/deck/{deckId}")
    public ResponseEntity<Flashcard> createFlashcard(@PathVariable String deckId, 
                                                   @RequestBody FlashcardRequest request, 
                                                   Authentication authentication) {
        try {
            String userId = authentication.getName();
            Flashcard flashcard = flashcardService.createFlashcard(request, deckId, userId);
            return ResponseEntity.ok(flashcard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/deck/{deckId}")
    public ResponseEntity<List<Flashcard>> getDeckFlashcards(@PathVariable String deckId) {
        List<Flashcard> flashcards = flashcardService.getDeckFlashcards(deckId);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/{flashcardId}")
    public ResponseEntity<Flashcard> getFlashcard(@PathVariable String flashcardId) {
        return flashcardService.getFlashcard(flashcardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{flashcardId}")
    public ResponseEntity<Flashcard> updateFlashcard(@PathVariable String flashcardId, 
                                                   @RequestBody FlashcardRequest request, 
                                                   Authentication authentication) {
        try {
            String userId = authentication.getName();
            Flashcard flashcard = flashcardService.updateFlashcard(flashcardId, request, userId);
            return ResponseEntity.ok(flashcard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable String flashcardId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            flashcardService.deleteFlashcard(flashcardId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deck/{deckId}/reorder")
    public ResponseEntity<Void> reorderFlashcards(@PathVariable String deckId, 
                                                @RequestBody List<String> flashcardIds, 
                                                Authentication authentication) {
        try {
            String userId = authentication.getName();
            flashcardService.reorderFlashcards(deckId, flashcardIds, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Flashcard>> searchFlashcards(@RequestParam String q) {
        List<Flashcard> flashcards = flashcardService.searchFlashcards(q);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Flashcard>> getFlashcardsByTag(@PathVariable String tag) {
        List<Flashcard> flashcards = flashcardService.getFlashcardsByTag(tag);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Flashcard>> getMyFlashcards(Authentication authentication) {
        String userId = authentication.getName();
        List<Flashcard> flashcards = flashcardService.getUserFlashcards(userId);
        return ResponseEntity.ok(flashcards);
    }
} 