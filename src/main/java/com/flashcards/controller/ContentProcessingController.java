package com.flashcards.controller;

import com.flashcards.dto.ContentExtractionRequest;
import com.flashcards.service.ContentProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*")
public class ContentProcessingController {

    private final ContentProcessingService contentProcessingService;

    @Autowired
    public ContentProcessingController(ContentProcessingService contentProcessingService) {
        this.contentProcessingService = contentProcessingService;
    }

    @PostMapping(value = "/extract", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> extractContent(@RequestBody ContentExtractionRequest request) {
        try {
            String extractedText;
            
            switch (request.getContentType().toLowerCase()) {
                case "text":
                    extractedText = request.getContent();
                    break;
                case "url":
                    extractedText = contentProcessingService.extractTextFromUrl(request.getContent());
                    break;
                case "pdf":
                    // For PDF, content should be base64 encoded
                    byte[] pdfBytes = java.util.Base64.getDecoder().decode(request.getContent());
                    extractedText = contentProcessingService.extractTextFromPdf(pdfBytes);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported content type: " + request.getContentType());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", extractedText);
            response.put("contentType", request.getContentType());
            response.put("length", extractedText.length());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to process content: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
