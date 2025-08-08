package com.flashcards.dto;

import lombok.Data;

@Data
public class ContentExtractionRequest {
    private String content;
    private String contentType; // "text", "url", or "pdf"
    private String mimeType; // For PDF uploads
    private String fileName; // Original filename for PDFs
}
