package com.flashcards.service;

import com.flashcards.dto.FlashcardRequest;
import com.flashcards.model.Deck;
import com.flashcards.model.Flashcard;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.FlashcardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository flashcardRepository, DeckRepository deckRepository) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
    }

    public Flashcard createFlashcard(FlashcardRequest request, String deckId, String userId) {
        // Verify deck exists and user owns it
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to add cards to this deck");
        }

        Flashcard flashcard = new Flashcard(request.getQuestion(), request.getAnswer(), deckId, userId);
        flashcard.setTags(request.getTags());
        flashcard.setOrderIndex(request.getOrderIndex());

        Flashcard savedCard = flashcardRepository.save(flashcard);

        // Update deck card count
        deck.setCardCount(deck.getCardCount() + 1);
        deck.setUpdatedAt(LocalDateTime.now());
        deckRepository.save(deck);

        return savedCard;
    }

    public List<Flashcard> getDeckFlashcards(String deckId) {
        return flashcardRepository.findByDeckIdOrderByOrderIndex(deckId);
    }

    public Optional<Flashcard> getFlashcard(String flashcardId) {
        return flashcardRepository.findById(flashcardId);
    }

    public Flashcard updateFlashcard(String flashcardId, FlashcardRequest request, String userId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        if (!flashcard.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this flashcard");
        }

        flashcard.setQuestion(request.getQuestion());
        flashcard.setAnswer(request.getAnswer());
        flashcard.setTags(request.getTags());
        flashcard.setOrderIndex(request.getOrderIndex());
        flashcard.setUpdatedAt(LocalDateTime.now());

        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(String flashcardId, String userId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        if (!flashcard.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this flashcard");
        }

        // Update deck card count
        Deck deck = deckRepository.findById(flashcard.getDeckId())
                .orElseThrow(() -> new RuntimeException("Deck not found"));
        deck.setCardCount(deck.getCardCount() - 1);
        deck.setUpdatedAt(LocalDateTime.now());
        deckRepository.save(deck);

        flashcardRepository.delete(flashcard);
    }

    public void reorderFlashcards(String deckId, List<String> flashcardIds, String userId) {
        // Verify deck ownership
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (!deck.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to reorder cards in this deck");
        }

        // Update order index for each flashcard
        for (int i = 0; i < flashcardIds.size(); i++) {
            Flashcard flashcard = flashcardRepository.findById(flashcardIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Flashcard not found"));
            
            flashcard.setOrderIndex(i);
            flashcard.setUpdatedAt(LocalDateTime.now());
            flashcardRepository.save(flashcard);
        }
    }

    public List<Flashcard> searchFlashcards(String searchTerm) {
        return flashcardRepository.searchByQuestionOrAnswer(searchTerm);
    }

    public List<Flashcard> getFlashcardsByTag(String tag) {
        return flashcardRepository.findByTagsContaining(tag);
    }

    public List<Flashcard> getUserFlashcards(String userId) {
        return flashcardRepository.findByUserId(userId);
    }
} 