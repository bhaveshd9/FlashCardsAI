package com.flashcards.controller;

import com.flashcards.dto.DeckRequest;
import com.flashcards.model.Deck;
import com.flashcards.service.DeckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = "*")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping
    public ResponseEntity<Deck> createDeck(@RequestBody DeckRequest request, Authentication authentication) {
        try {
            String userId = authentication.getName();
            Deck deck = deckService.createDeck(request, userId);
            return ResponseEntity.ok(deck);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Deck>> getMyDecks(Authentication authentication) {
        String userId = authentication.getName();
        List<Deck> decks = deckService.getUserDecks(userId);
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Deck>> getPublicDecks() {
        List<Deck> decks = deckService.getPublicDecks();
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<Deck> getDeck(@PathVariable String deckId) {
        return deckService.getDeck(deckId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<Deck> updateDeck(@PathVariable String deckId, 
                                         @RequestBody DeckRequest request, 
                                         Authentication authentication) {
        try {
            String userId = authentication.getName();
            Deck deck = deckService.updateDeck(deckId, request, userId);
            return ResponseEntity.ok(deck);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable String deckId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            deckService.deleteDeck(deckId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{deckId}/duplicate")
    public ResponseEntity<Deck> duplicateDeck(@PathVariable String deckId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            Deck deck = deckService.duplicateDeck(deckId, userId);
            return ResponseEntity.ok(deck);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Deck>> searchDecks(@RequestParam String q) {
        List<Deck> decks = deckService.searchDecks(q);
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Deck>> getDecksByTag(@PathVariable String tag) {
        List<Deck> decks = deckService.getDecksByTag(tag);
        return ResponseEntity.ok(decks);
    }
} 