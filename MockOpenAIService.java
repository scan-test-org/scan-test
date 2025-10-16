package com.test.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

/**
 * Mock OpenAI Service for Testing
 * This is a test class - do not use in production
 */
@Service
public class MockOpenAIService {
    
    // Test API Key - DO NOT USE IN PRODUCTION
    private static final String OPENAI_API_KEY = "sk-4a79aae22608494a86e33921b7e1f4c5";
    private static final String OPENAI_BASE_URL = "https://api.openai.com/v1";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public MockOpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Mock method to test OpenAI Chat API
     */
    public String testChatCompletion(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = OPENAI_BASE_URL + "/chat/completions";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("Error testing OpenAI API: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Mock method to test OpenAI Image Generation
     */
    public String testImageGeneration(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", prompt);
            requestBody.put("size", "1024x1024");
            requestBody.put("quality", "standard");
            requestBody.put("n", 1);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = OPENAI_BASE_URL + "/images/generations";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("Error testing OpenAI Image API: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Test method to verify API key validity
     */
    public boolean testApiKey() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = OPENAI_BASE_URL + "/models";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.err.println("API Key test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        MockOpenAIService service = new MockOpenAIService();
        
        System.out.println("Testing OpenAI API Key...");
        boolean keyValid = service.testApiKey();
        System.out.println("API Key Valid: " + keyValid);
        
        if (keyValid) {
            System.out.println("\nTesting Chat Completion...");
            String chatResponse = service.testChatCompletion("Hello, how are you?");
            System.out.println("Chat Response: " + chatResponse);
            
            System.out.println("\nTesting Image Generation...");
            String imageResponse = service.testImageGeneration("A beautiful sunset over mountains");
            System.out.println("Image Response: " + imageResponse);
        }
    }
}
