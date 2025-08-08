package com.flashcards.service;

import com.flashcards.dto.DeckRequest;
import com.flashcards.model.Deck;
import com.flashcards.model.Flashcard;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.FlashcardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public DeckService(DeckRepository deckRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public Deck createDeck(DeckRequest request, String userId) {
        Deck deck = new Deck(request.getName(), request.getDescription(), userId);
        deck.setPublic(request.isPublic());
        deck.setTags(request.getTags());
        deck.setCoverImageUrl(request.getCoverImageUrl());
        
        return deckRepository.save(deck);
    }

    public List<Deck> getUserDecks(String userId) {
        List<Deck> decks = deckRepository.findByUserId(userId);
        
        // Add flashcard counts to each deck
        for (Deck deck : decks) {
            long flashcardCount = flashcardRepository.countByDeckId(deck.getId());
            deck.setFlashcardCount((int) flashcardCount);
        }
        
        return decks;
    }

    public List<Deck> getPublicDecks() {
        return deckRepository.findByIsPublicTrue();
    }

    public Optional<Deck> getDeck(String deckId) {
        return deckRepository.findById(deckId);
    }

    public Deck updateDeck(String deckId, DeckRequest request, String userId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this deck");
        }

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setPublic(request.isPublic());
        deck.setTags(request.getTags());
        deck.setCoverImageUrl(request.getCoverImageUrl());
        deck.setUpdatedAt(LocalDateTime.now());

        return deckRepository.save(deck);
    }

    public void deleteDeck(String deckId, String userId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this deck");
        }

        // Delete all flashcards in the deck
        List<Flashcard> flashcards = flashcardRepository.findByDeckIdOrderByOrderIndex(deckId);
        flashcardRepository.deleteAll(flashcards);

        // Delete the deck
        deckRepository.delete(deck);
    }

    public Deck duplicateDeck(String deckId, String userId) {
        Deck originalDeck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        // Create new deck
        Deck newDeck = new Deck(
            originalDeck.getName() + " (Copy)",
            originalDeck.getDescription(),
            userId
        );
        newDeck.setPublic(false); // Duplicated decks are private by default
        newDeck.setTags(originalDeck.getTags());
        
        Deck savedDeck = deckRepository.save(newDeck);

        // Copy all flashcards
        List<Flashcard> originalCards = flashcardRepository.findByDeckIdOrderByOrderIndex(deckId);
        for (Flashcard originalCard : originalCards) {
            Flashcard newCard = new Flashcard(
                originalCard.getQuestion(),
                originalCard.getAnswer(),
                savedDeck.getId(),
                userId
            );
            newCard.setTags(originalCard.getTags());
            newCard.setOrderIndex(originalCard.getOrderIndex());
            flashcardRepository.save(newCard);
        }

        // Update card count
        savedDeck.setCardCount(originalCards.size());
        return deckRepository.save(savedDeck);
    }

    public List<Deck> searchDecks(String searchTerm) {
        return deckRepository.searchByNameOrDescription(searchTerm);
    }

    public List<Deck> getDecksByTag(String tag) {
        return deckRepository.findByTagsContaining(tag);
    }
} 