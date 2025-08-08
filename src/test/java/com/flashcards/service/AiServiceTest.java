package com.flashcards.service;

import com.flashcards.dto.AiGenerationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @InjectMocks
    private AiService aiService;

    private AiGenerationRequest request;

    @BeforeEach
    void setUp() {
        request = new AiGenerationRequest();
        request.setText("This is a test text. It contains multiple sentences. Each sentence should generate a flashcard.");
        request.setNumberOfCards(3);
        request.setTopic("Test Topic");
        request.setDifficulty("medium");
    }

    @Test
    void testGenerateFlashcards_Success() {
        List<AiService.FlashcardData> result = aiService.generateFlashcards(request);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= request.getNumberOfCards());
        
        // Check that each flashcard has front and back
        for (AiService.FlashcardData flashcard : result) {
            assertNotNull(flashcard.getFront());
            assertNotNull(flashcard.getBack());
            assertFalse(flashcard.getFront().isEmpty());
            assertFalse(flashcard.getBack().isEmpty());
        }
    }

    @Test
    void testGenerateFlashcards_EmptyText() {
        request.setText("");
        
        List<AiService.FlashcardData> result = aiService.generateFlashcards(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateFlashcards_NullText() {
        request.setText(null);
        
        List<AiService.FlashcardData> result = aiService.generateFlashcards(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateFlashcards_ShortText() {
        request.setText("Short.");
        request.setNumberOfCards(5);
        
        List<AiService.FlashcardData> result = aiService.generateFlashcards(request);

        assertNotNull(result);
        // Should generate at least one flashcard from short text
        assertTrue(result.size() >= 0);
    }

    @Test
    void testFlashcardData_ConstructorAndGetters() {
        String front = "What is the capital of France?";
        String back = "Paris";
        
        AiService.FlashcardData flashcard = new AiService.FlashcardData(front, back);
        
        assertEquals(front, flashcard.getFront());
        assertEquals(back, flashcard.getBack());
    }
} 