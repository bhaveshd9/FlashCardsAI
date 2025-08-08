package com.flashcards.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ContentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ContentProcessingService.class);
    private static final Set<String> ALLOWED_DOMAINS = new HashSet<>(Arrays.asList(
            "wikipedia.org", "github.com", "stackoverflow.com"
    ));
    private static final int MAX_CONTENT_LENGTH = 100000; // 100KB max content length
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Extract text content from a PDF file
     */
    public String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF content cannot be null or empty");
        }

        if (pdfBytes.length > 10 * 1024 * 1024) { // 10MB limit
            throw new IOException("PDF file size exceeds maximum allowed size (10MB)");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(true);
            stripper.setShouldSeparateByBeads(true);

            String text = stripper.getText(document);

            // Basic cleanup
            text = text.replaceAll("\u0000", "") // Remove null characters
                    .replaceAll("\uFFFD", "")  // Remove replacement characters
                    .trim();

            if (text.length() > MAX_CONTENT_LENGTH) {
                text = text.substring(0, MAX_CONTENT_LENGTH) + "\n[Content truncated due to length]";
            }

            return text;
        } catch (Exception e) {
            logger.error("Error extracting text from PDF", e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text content from a URL
     */
    public String extractTextFromUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);

            // Validate domain if needed
            String host = url.getHost().toLowerCase();
            if (!isAllowedDomain(host)) {
                logger.warn("Content extraction from domain not in allowed list: " + host);
                // Continue anyway, just log a warning
            }

            // Configure and execute request
            Document doc = Jsoup.connect(urlString)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("http://www.google.com")
                    .timeout(15000) // 15 seconds
                    .maxBodySize(2 * 1024 * 1024) // 2MB max
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            // Check for HTTP errors
            int statusCode = doc.connection().response().statusCode();
            if (statusCode != 200) {
                throw new IOException("HTTP error fetching URL: " + statusCode);
            }

            // Clean the HTML and get the cleaned HTML as a string
            String cleanedHtml = Jsoup.clean(doc.body().html(), Safelist.relaxed()
                    .addTags("p", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li", "pre", "code"));
            
            // Parse the cleaned HTML to a new document
            Document cleanedDoc = Jsoup.parse(cleanedHtml);

            // Extract and clean text
            String text = cleanedDoc.text()
                    .replaceAll("\u00A0", " ")  // Replace non-breaking spaces
                    .replaceAll("\u2013|\u2014", "-")  // Replace en/em dashes
                    .replaceAll("\\s*[\\r\\n]+\\s*", "\\n")  // Normalize newlines
                    .replaceAll("\\s*[\\u2028\\u2029]\\s*", "\\n")  // Line/paragraph separators
                    .replaceAll(WHITESPACE_PATTERN.pattern(), " ")  // Normalize spaces
                    .trim();

            if (text.length() > MAX_CONTENT_LENGTH) {
                text = text.substring(0, MAX_CONTENT_LENGTH) + "\n[Content truncated due to length]";
            }

            return text;
        } catch (Exception e) {
            throw new IOException("Failed to extract content from URL: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text content from a text document
     */
    public String extractTextFromDocument(String content, String contentType) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        if (contentType == null) {
            contentType = "text";
        }

        String processedContent;

        try {
            switch (contentType.toLowerCase()) {
                case "text":
                    processedContent = content;
                    break;
                case "url":
                    processedContent = extractTextFromUrl(content);
                    break;
                case "pdf":
                    // Content should be base64 encoded
                    try {
                        byte[] pdfBytes = java.util.Base64.getDecoder().decode(content);
                        processedContent = extractTextFromPdf(pdfBytes);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("Invalid base64 encoded PDF content", e);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported content type: " + contentType);
            }

            // Additional content validation
            if (processedContent == null || processedContent.trim().isEmpty()) {
                throw new IOException("No content could be extracted");
            }

            return processedContent;

        } catch (Exception e) {
            logger.error("Error processing content type " + contentType, e);
            throw new IOException("Failed to process content: " + e.getMessage(), e);
        }
    }

    private boolean isAllowedDomain(String host) {
        return ALLOWED_DOMAINS.contains(host);
    }
}