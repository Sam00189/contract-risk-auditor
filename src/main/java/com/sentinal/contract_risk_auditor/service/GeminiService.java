package com.sentinal.contract_risk_auditor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateRemedy(String clauseTitle, String clauseContent, String riskCategory) {
        try {
            String prompt = """
                You are an expert contract lawyer. 
                Analyze the following contract clause flagged for '%s' risk:
                Clause Title: %s
                Clause Content: %s
                
                Provide:
                1. A brief explanation of the operational risk.
                2. A professionally rewritten, balanced replacement clause that protects the service provider.
                Keep your total response under 100 words, direct and actionable.
                """.formatted(riskCategory, clauseTitle, clauseContent);

            // Construct Gemini REST JSON Payload
            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", new Object[]{part});
            Map<String, Object> requestBodyMap = Map.of("contents", new Object[]{content});

            String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();
            } else {
                return "Gemini API call failed with status: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            return "Error calling Gemini: " + e.getMessage();
        }
    }
}