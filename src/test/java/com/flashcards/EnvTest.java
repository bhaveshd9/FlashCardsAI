package com.flashcards;

public class EnvTest {
    public static void main(String[] args) {
        System.out.println("=== Environment Variables Test ===");
        
        String mongoUri = System.getenv("MONGODB_URI");
        String jwtSecret = System.getenv("JWT_SECRET");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        
        System.out.println("MONGODB_URI exists: " + (mongoUri != null && !mongoUri.isEmpty()));
        System.out.println("MONGODB_URI length: " + (mongoUri != null ? mongoUri.length() : 0));
        System.out.println("JWT_SECRET exists: " + (jwtSecret != null && !jwtSecret.isEmpty()));
        System.out.println("OPENAI_API_KEY exists: " + (openaiKey != null && !openaiKey.isEmpty()));
        
        if (mongoUri != null) {
            System.out.println("MONGODB_URI preview: " + mongoUri.substring(0, Math.min(50, mongoUri.length())) + "...");
        } else {
            System.out.println("MONGODB_URI: null");
        }
        
        System.out.println("=== End Test ===");
    }
} 