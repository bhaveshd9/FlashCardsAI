package com.flashcards.dto;

public class AiGenerationRequest {
    private String text;
    private String topic;
    private int numberOfCards = 5;
    private String difficulty = "medium"; // easy, medium, hard
    private String contentType = "text"; // text, url, pdf

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    
    public int getNumberOfCards() { return numberOfCards; }
    public void setNumberOfCards(int numberOfCards) { this.numberOfCards = numberOfCards; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
} 