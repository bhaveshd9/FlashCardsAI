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
            String text = request.getText() != null ? request.getText() : "";
            
            // Check if we should use OpenAI API for custom prompts
            if (apiKey != null && !apiKey.trim().isEmpty() && 
                (text.toLowerCase().contains("question should be when") || 
                 text.toLowerCase().contains("when we can use") ||
                 text.toLowerCase().contains("when to use") ||
                 text.length() > 100)) {
                
                System.out.println("Using OpenAI API for flashcard generation");
                return generateWithOpenAI(request);
            } else {
                System.out.println("Using fallback generation method");
                return generateCustomFlashcards(request);
            }
        } catch (Exception e) {
            System.err.println("Error in generateFlashcards: " + e.getMessage());
            e.printStackTrace();
            // Fallback to custom generation
            return generateCustomFlashcards(request);
        }
    }

    private String buildPrompt(AiGenerationRequest request) {
        String text = request.getText() != null ? request.getText() : "";
        String topic = request.getTopic() != null ? request.getTopic() : "General";
        
        // Check if the text contains specific instructions for flashcard format
        if (text.toLowerCase().contains("question should be when") || 
            text.toLowerCase().contains("when we can use") ||
            text.toLowerCase().contains("when to use")) {
            
            // Custom prompt for "when to use" format
            return String.format(
                "Generate %d flashcards about %s. " +
                "Follow this specific format:\n" +
                "- Each question should ask 'When do we use X?' or 'When should we use X?'\n" +
                "- Each answer should be the specific data structure, algorithm, or concept name\n\n" +
                "Instructions: %s\n\n" +
                "Please respond with JSON format:\n" +
                "[\n" +
                "  {\"question\": \"When do we use X?\", \"answer\": \"Data Structure Name\"},\n" +
                "  {\"question\": \"When should we use Y?\", \"answer\": \"Another Data Structure\"}\n" +
                "]\n\n" +
                "Make sure each question asks about WHEN to use something, and the answer is the specific name.",
                request.getNumberOfCards(),
                topic,
                text
            );
        } else {
            // Default prompt for regular flashcards
            return String.format(
                "Generate %d flashcards from the following text. " +
                "Topic: %s, Difficulty: %s\n\n" +
                "Text: %s\n\n" +
                "Please respond with JSON format:\n" +
                "[\n" +
                "  {\"question\": \"Question 1\", \"answer\": \"Answer 1\"},\n" +
                "  {\"question\": \"Question 2\", \"answer\": \"Answer 2\"}\n" +
                "]\n\n" +
                "Make questions clear and answers concise.",
                request.getNumberOfCards(),
                topic,
                request.getDifficulty(),
                text
            );
        }
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

    private List<FlashcardData> generateWithOpenAI(AiGenerationRequest request) {
        try {
            String prompt = buildPrompt(request);
            System.out.println("OpenAI Prompt: " + prompt);
            
            String response = callOpenAI(prompt);
            System.out.println("OpenAI Response: " + response);
            
            return parseFlashcards(response);
        } catch (Exception e) {
            System.err.println("Error calling OpenAI: " + e.getMessage());
            e.printStackTrace();
            return generateCustomFlashcards(request);
        }
    }
    
    private List<FlashcardData> parseFlashcards(String response) {
        List<FlashcardData> flashcards = new ArrayList<>();
        try {
            // Clean the response - remove any markdown formatting
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            JsonNode jsonArray = objectMapper.readTree(cleanResponse);
            if (jsonArray.isArray()) {
                for (JsonNode card : jsonArray) {
                    // Try question/answer first (preferred format)
                    String question = card.path("question").asText();
                    String answer = card.path("answer").asText();
                    
                    // Fallback to front/back if question/answer are empty
                    if (question.isEmpty() && answer.isEmpty()) {
                        question = card.path("front").asText();
                        answer = card.path("back").asText();
                    }
                    
                    if (!question.isEmpty() && !answer.isEmpty()) {
                        flashcards.add(new FlashcardData(question, answer));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            System.err.println("Raw response: " + response);
        }
        return flashcards;
    }

    private List<FlashcardData> generateCustomFlashcards(AiGenerationRequest request) {
        String text = request.getText() != null ? request.getText() : "";
        String topic = request.getTopic() != null ? request.getTopic() : "General";
        int numberOfCards = request.getNumberOfCards();
        
        System.out.println("Custom Generation - Text: '" + text + "', Topic: '" + topic + "', Cards: " + numberOfCards);
        
        String normalizedText = text.toLowerCase().trim();
        String normalizedTopic = topic.toLowerCase().trim();
        
        // Check for specific "when to use" format request
        if (normalizedText.contains("question should be when") || 
            normalizedText.contains("when we can use") ||
            normalizedText.contains("when to use") ||
            normalizedText.contains("when should we use") ||
            normalizedText.contains("particular data structure") ||
            (normalizedTopic.equals("pds") || normalizedTopic.contains("data structure"))) {
            
            System.out.println("Detected 'when to use' format request - generating specialized flashcards");
            return generateWhenToUseFlashcards(topic, numberOfCards);
        }
        
        return generateSimpleFlashcards(request);
    }
    
    private List<FlashcardData> generateWhenToUseFlashcards(String topic, int numberOfCards) {
        List<FlashcardData> flashcards = new ArrayList<>();
        
        // Normalize topic for better detection
        String normalizedTopic = topic.toLowerCase().trim();
        
        // Generate "when to use" flashcards for Python data structures
        // Check for various ways to indicate Python data structures
        if (normalizedTopic.contains("python") || 
            normalizedTopic.contains("data structure") ||
            normalizedTopic.equals("pds") ||
            normalizedTopic.contains("python data structures") ||
            normalizedTopic.contains("py data") ||
            normalizedTopic.contains("data struct")) {
            
            String[] dataStructureCards = {
                "When you need to store an ordered, mutable collection that allows duplicates?|List",
                "When you need an immutable, ordered collection that allows duplicates?|Tuple", 
                "When you need to store unique elements with no duplicates?|Set",
                "When you need to store key-value pairs with fast lookup?|Dictionary",
                "When you need a LIFO (Last In, First Out) data structure?|Stack",
                "When you need a FIFO (First In, First Out) data structure?|Queue",
                "When you need to store data in a tree-like hierarchical structure?|Tree",
                "When you need to represent relationships between objects?|Graph",
                "When you need to store a large amount of data with fast search operations?|Hash Table",
                "When you need to maintain sorted data with efficient insertion and deletion?|Binary Search Tree"
            };
            
            String[] selectedCards = getRandomCards(dataStructureCards, numberOfCards);
            for (String card : selectedCards) {
                String[] parts = card.split("\\|", 2);
                if (parts.length == 2) {
                    flashcards.add(new FlashcardData(parts[0].trim(), parts[1].trim()));
                }
            }
        } else {
            // Generate generic "when to use" cards for other topics
            for (int i = 0; i < numberOfCards; i++) {
                flashcards.add(new FlashcardData(
                    "When should you use " + topic + " concept " + (i + 1) + "?",
                    "Specific use case for " + topic + " " + (i + 1)
                ));
            }
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