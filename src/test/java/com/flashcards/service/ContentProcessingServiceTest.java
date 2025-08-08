package com.flashcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentProcessingServiceTest {

    @InjectMocks
    private ContentProcessingService contentProcessingService;

    @Test
    public void testExtractTextFromPdf() throws Exception {
        // Create a simple PDF in memory
        String testText = "This is a test PDF content";
        byte[] pdfBytes = createSimplePdf(testText);
        
        String result = contentProcessingService.extractTextFromPdf(pdfBytes);
        
        assertNotNull(result);
        assertTrue(result.contains(testText));
    }

    @Test
    public void testExtractTextFromPdf_EmptyContent() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProcessingService.extractTextFromPdf(new byte[0]);
        });
    }

    @Test
    public void testExtractTextFromUrl() throws Exception {
        // This is a simple test that will make an actual HTTP request
        // In a real test, you might want to mock the HTTP request
        String testUrl = "https://example.com";
        
        // This test is disabled by default to avoid external dependencies
        // Uncomment to test with a real URL
        // String result = contentProcessingService.extractTextFromUrl(testUrl);
        // assertNotNull(result);
        // assertFalse(result.isEmpty());
    }

    @Test
    public void testExtractTextFromDocument_Text() throws Exception {
        String testText = "This is a test text content";
        String result = contentProcessingService.extractTextFromDocument(testText, "text");
        assertEquals(testText, result);
    }

    @Test
    public void testExtractTextFromDocument_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProcessingService.extractTextFromDocument("content", "invalid-type");
        });
    }

    @Test
    public void testExtractTextFromDocument_NullContent() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProcessingService.extractTextFromDocument(null, "text");
        });
    }

    @Test
    public void testIsAllowedDomain() {
        assertTrue(ReflectionTestUtils.invokeMethod(contentProcessingService, "isAllowedDomain", "wikipedia.org"));
        assertFalse(ReflectionTestUtils.invokeMethod(contentProcessingService, "isAllowedDomain", "example.com"));
    }

    // Helper method to create a simple PDF in memory
    private byte[] createSimplePdf(String text) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            document.addPage(page);
            
            try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                document.save(out);
                return out.toByteArray();
            }
        }
    }
}
