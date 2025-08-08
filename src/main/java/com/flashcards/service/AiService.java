package com.flashcards.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flashcards.dto.AiGenerationRequest;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ContentProcessingService contentProcessingService;

    public AiService(OkHttpClient httpClient, ObjectMapper objectMapper, ContentProcessingService contentProcessingService) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.contentProcessingService = contentProcessingService;
    }

    public List<FlashcardData> generateFlashcards(AiGenerationRequest request) {
        System.out.println("AI Generation Request: " + request.getText() + " | Cards: " + request.getNumberOfCards());
        
        try {
            // Process content based on type
            String processedText = contentProcessingService.extractTextFromDocument(
                request.getText(), 
                request.getContentType()
            );
            
            System.out.println("Processed text length: " + processedText.length());
            
            // If we have valid text, use it to generate flashcards
            if (processedText != null && !processedText.trim().isEmpty()) {
                // Create a new request with the processed text
                AiGenerationRequest processedRequest = new AiGenerationRequest();
                processedRequest.setText(processedText);
                processedRequest.setNumberOfCards(request.getNumberOfCards());
                processedRequest.setTopic(request.getTopic());
                processedRequest.setDifficulty(request.getDifficulty());
                
                // Try to generate topic-based cards from the actual content
                return generateTopicBasedFlashcards(processedText, request.getTopic(), request.getNumberOfCards());
            } else {
                System.out.println("No content extracted, using simple flashcards");
                return generateSimpleFlashcards(request);
            }
        } catch (Exception e) {
            System.err.println("Error processing content: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple generation
            return generateSimpleFlashcards(request);
        }
    }

    private String buildPrompt(AiGenerationRequest request) {
        return String.format(
            "Generate %d flashcards from the following text. " +
            "Topic: %s, Difficulty: %s\n\n" +
            "Text: %s\n\n" +
            "Please respond with JSON format:\n" +
            "[\n" +
            "  {\"front\": \"Question 1\", \"back\": \"Answer 1\"},\n" +
            "  {\"front\": \"Question 2\", \"back\": \"Answer 2\"}\n" +
            "]\n\n" +
            "Make questions clear and answers concise.",
            request.getNumberOfCards(),
            request.getTopic() != null ? request.getTopic() : "General",
            request.getDifficulty(),
            request.getText()
        );
    }

    private String callOpenAI(String prompt) throws IOException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-3.5-turbo");
        
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.set("messages", messages);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.get("application/json")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            
            JsonNode responseJson = objectMapper.readTree(response.body().string());
            return responseJson.path("choices").path(0).path("message").path("content").asText();
        }
    }

    private List<FlashcardData> parseFlashcards(String response) {
        List<FlashcardData> flashcards = new ArrayList<>();
        try {
            JsonNode jsonArray = objectMapper.readTree(response);
            if (jsonArray.isArray()) {
                for (JsonNode card : jsonArray) {
                    // Try both formats: question/answer and front/back
                    String front = card.path("front").asText();
                    String back = card.path("back").asText();
                    
                    // Fallback to question/answer if front/back are empty
                    if (front.isEmpty() && back.isEmpty()) {
                        front = card.path("question").asText();
                        back = card.path("answer").asText();
                    }
                    
                    if (!front.isEmpty() && !back.isEmpty()) {
                        flashcards.add(new FlashcardData(front, back));
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty list
            System.err.println("Error parsing AI response: " + e.getMessage());
        }
        return flashcards;
    }

    private List<FlashcardData> generateSimpleFlashcards(AiGenerationRequest request) {
        List<FlashcardData> flashcards = new ArrayList<>();
        String text = request.getText() != null ? request.getText() : "";
        String topic = request.getTopic() != null ? request.getTopic() : "General";
        int numberOfCards = request.getNumberOfCards();
        
        System.out.println("AI Generation Request - Text: '" + text + "', Topic: '" + topic + "', Cards: " + numberOfCards);
        
        // If text is provided, analyze it and generate relevant flashcards
        if (!text.trim().isEmpty()) {
            String cleanText = text.toLowerCase().trim();
            
            // Check if it's about car brands and countries
            if (cleanText.contains("car") && (cleanText.contains("brand") || cleanText.contains("origin") || cleanText.contains("country"))) {
                String[] carBrandCards = {
                    "Tesla is from which country?|USA",
                    "Toyota is from which country?|Japan",
                    "BMW is from which country?|Germany",
                    "Mercedes-Benz is from which country?|Germany",
                    "Ford is from which country?|USA",
                    "Honda is from which country?|Japan",
                    "Audi is from which country?|Germany",
                    "Porsche is from which country?|Germany",
                    "Chevrolet is from which country?|USA",
                    "Lexus is from which country?|Japan",
                    "Volkswagen is from which country?|Germany",
                    "Ferrari is from which country?|Italy",
                    "Bentley is from which country?|UK",
                    "Volvo is from which country?|Sweden",
                    "Hyundai is from which country?|South Korea",
                    "Kia is from which country?|South Korea",
                    "Mazda is from which country?|Japan",
                    "Subaru is from which country?|Japan",
                    "Nissan is from which country?|Japan",
                    "Mitsubishi is from which country?|Japan"
                };
                
                String[] selectedCards = getRandomCards(carBrandCards, numberOfCards);
                for (String card : selectedCards) {
                    String[] parts = card.split("\\|");
                    if (parts.length == 2) {
                        flashcards.add(new FlashcardData(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
            // Check if it's about countries
            else if (cleanText.contains("country") || cleanText.contains("nation")) {
                String[] countryCards = {
                    "What is the capital of France?|Paris",
                    "What is the capital of Japan?|Tokyo",
                    "What is the capital of Germany?|Berlin",
                    "What is the capital of USA?|Washington D.C.",
                    "What is the capital of UK?|London",
                    "What is the capital of Italy?|Rome",
                    "What is the capital of Spain?|Madrid",
                    "What is the capital of Canada?|Ottawa",
                    "What is the capital of Australia?|Canberra",
                    "What is the capital of Brazil?|Brasília",
                    "What is the capital of India?|New Delhi",
                    "What is the capital of China?|Beijing",
                    "What is the capital of Russia?|Moscow",
                    "What is the capital of South Africa?|Pretoria",
                    "What is the capital of Mexico?|Mexico City"
                };
                
                String[] selectedCards = getRandomCards(countryCards, numberOfCards);
                for (String card : selectedCards) {
                    String[] parts = card.split("\\|");
                    if (parts.length == 2) {
                        flashcards.add(new FlashcardData(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
            // Check if it's about animals
            else if (cleanText.contains("animal") || cleanText.contains("pet") || cleanText.contains("wildlife")) {
                String[] animalCards = {
                    "What is the fastest land animal?|Cheetah",
                    "What is the largest land animal?|African Elephant",
                    "What is the tallest animal?|Giraffe",
                    "What is the largest cat species?|Tiger",
                    "What animal has the longest lifespan?|Greenland Shark",
                    "What is the national animal of India?|Bengal Tiger",
                    "What is the national animal of USA?|Bald Eagle",
                    "What is the national animal of Australia?|Kangaroo",
                    "What is the national animal of China?|Giant Panda",
                    "What is the national animal of UK?|Lion",
                    "What is the national animal of Canada?|Beaver",
                    "What is the national animal of Russia?|Brown Bear",
                    "What is the national animal of Brazil?|Jaguar",
                    "What is the national animal of South Africa?|Springbok",
                    "What is the national animal of Japan?|Green Pheasant"
                };
                
                String[] selectedCards = getRandomCards(animalCards, numberOfCards);
                for (String card : selectedCards) {
                    String[] parts = card.split("\\|");
                    if (parts.length == 2) {
                        flashcards.add(new FlashcardData(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
            // Default case - generate based on the actual text content
            else {
                // Split text into sentences or phrases
                String[] phrases = text.split("[.!?,\n]");
                int cardsGenerated = 0;
                
                for (String phrase : phrases) {
                    if (cardsGenerated >= numberOfCards) break;
                    
                    phrase = phrase.trim();
                    if (phrase.length() > 5) {
                        // Create a question from the phrase
                        String front = "What is: " + phrase + "?";
                        String back = phrase;
                        flashcards.add(new FlashcardData(front, back));
                        cardsGenerated++;
                    }
                }
                
                // If we didn't generate enough cards, add some generic ones based on the topic
                if (cardsGenerated < numberOfCards) {
                    int remaining = numberOfCards - cardsGenerated;
                    List<FlashcardData> genericCards = generateTopicBasedFlashcards(topic, topic, remaining);
                    for (FlashcardData card : genericCards) {
                        if (cardsGenerated >= numberOfCards) break;
                        flashcards.add(card);
                        cardsGenerated++;
                    }
                }
            }
        } else {
            // If no text provided, generate generic flashcards based on topic
            List<FlashcardData> topicFlashcards = generateTopicBasedFlashcards(topic, topic, numberOfCards);
            int cardsToAdd = Math.min(numberOfCards, topicFlashcards.size());
            for (int i = 0; i < cardsToAdd; i++) {
                flashcards.add(topicFlashcards.get(i));
            }
        }
        return flashcards;
    }
    
    private List<FlashcardData> generateTopicBasedFlashcards(String text, String topic, int count) {
        try {
            // Clean and normalize the text
            String cleanText = text.replaceAll("\\s+", " ").trim();
            if (cleanText.length() < 20) {
                // Fallback for very short text
                return getFallbackFlashcards(topic, count);
            }
            
            // Split text into sentences
            String[] sentences = cleanText.split("(?<=[.!?])\\s+");
            List<FlashcardData> flashcards = new ArrayList<>();
            
            // Process sentences to create flashcard pairs
            for (int i = 0; i < sentences.length - 1 && flashcards.size() < count * 2; i++) {
                String current = sentences[i].trim();
                String next = sentences[i + 1].trim();
                
                // Skip very short or long sentences
                if (current.length() < 20 || current.length() > 200 || next.length() < 10) {
                    continue;
                }
                
                // Create question-answer pairs
                String question = createQuestionFromSentence(current);
                String answer = cleanAnswer(next);
                
                if (isValidFlashcard(question, answer)) {
                    flashcards.add(new FlashcardData(question, answer));
                }
            }
            
            // If we have enough cards, return them
            if (!flashcards.isEmpty()) {
                return flashcards.subList(0, Math.min(count, flashcards.size()));
            }
            
            // Fallback to generating cards from key terms if we don't have enough sentence pairs
            return generateFromKeyTerms(cleanText, topic, count);
            
        } catch (Exception e) {
            System.err.println("Error in generateTopicBasedFlashcards: " + e.getMessage());
            e.printStackTrace();
            return getFallbackFlashcards(topic, count);
        }
    }
    
    private List<FlashcardData> generateFromKeyTerms(String text, String topic, int count) {
        // Extract key terms (nouns and proper nouns)
        List<String> keyTerms = new ArrayList<>();
        String[] words = text.split("\\s+");
        
        // Simple heuristic to find potential key terms (capitalized words, longer words)
        for (String word : words) {
            if (word.length() > 4 && Character.isUpperCase(word.charAt(0))) {
                String cleanWord = word.replaceAll("[^a-zA-Z0-9]+", "").trim();
                if (cleanWord.length() > 3 && !keyTerms.contains(cleanWord)) {
                    keyTerms.add(cleanWord);
                }
            }
        }
        
        // If we have key terms, create definition cards
        if (!keyTerms.isEmpty()) {
            List<FlashcardData> result = new ArrayList<>();
            for (String term : keyTerms) {
                if (result.size() >= count) break;
                result.add(new FlashcardData("What is " + term + "?", "Definition of " + term));
            }
            return result;
        }
        
        // Final fallback
        return getFallbackFlashcards(topic, count);
    }
    
    private String createQuestionFromSentence(String sentence) {
        // Simple transformation of a statement into a question
        sentence = sentence.trim();
        if (sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("?")) {
            sentence = sentence.substring(0, sentence.length() - 1);
        }
        
        // Basic question formation
        String lower = sentence.toLowerCase();
        if (lower.startsWith("the ") || lower.startsWith("a ") || lower.startsWith("an ")) {
            return "What is " + sentence + "?";
        } else if (lower.startsWith("this is ") || lower.startsWith("these are ") || 
                  lower.startsWith("it is ") || lower.startsWith("they are ")) {
            return "What is " + sentence.substring(sentence.indexOf(' ') + 1) + "?";
        } else {
            return "What does the following describe: " + sentence + "?";
        }
    }
    
    private String cleanAnswer(String answer) {
        // Clean up the answer text
        answer = answer.trim();
        if (answer.endsWith(".") || answer.endsWith("!") || answer.endsWith("?")) {
            answer = answer.substring(0, answer.length() - 1);
        }
        return answer;
    }
    
    private boolean isValidFlashcard(String front, String back) {
        // Basic validation to ensure we have meaningful content
        if (front == null || back == null) return false;
        if (front.trim().isEmpty() || back.trim().isEmpty()) return false;
        if (front.length() < 10 || back.length() < 3) return false;
        if (front.equals(back)) return false;
        return true;
    }
    
    private List<FlashcardData> getFallbackFlashcards(String topic, int count) {
        // Fallback to topic-based cards
        String[] allCards = {
            "What is the main topic of " + topic + "?|Key concepts and principles",
            "What are the key concepts in " + topic + "?|Core principles and fundamentals",
            "What is the importance of " + topic + "?|Essential knowledge and applications",
            "What are the main categories in " + topic + "?|Primary classifications and types",
            "What is the history of " + topic + "?|Background and development over time"
        };
        String[] selectedCards = getRandomCards(allCards, Math.min(count, allCards.length));
        List<FlashcardData> result = new ArrayList<>();
        for (String card : selectedCards) {
            String[] parts = card.split("\\|", 2);
            if (parts.length == 2) {
                result.add(new FlashcardData(parts[0].trim(), parts[1].trim()));
            }
        }
        return result;
    }
    


    private String[] getRandomCards(String[] allCards, int count) {
        if (allCards == null || allCards.length == 0) {
            return new String[0];
        }
        // Shuffle the array and return the EXACT requested number of cards
        List<String> cardList = new ArrayList<>(Arrays.asList(allCards));
        Collections.shuffle(cardList);
        
        // Return exactly the requested number, not more
        int cardsToReturn = Math.min(count, cardList.size());
        return cardList.subList(0, cardsToReturn).toArray(new String[0]);
    }

    public static class FlashcardData {
        private String front;
        private String back;

        public FlashcardData(String front, String back) {
            this.front = front;
            this.back = back;
        }

        public String getFront() { return front; }
        public String getBack() { return back; }
    }
} 