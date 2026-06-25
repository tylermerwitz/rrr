package com.example.rrr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Profile("!dev") // ✅ Active unless dev profile
public class OpenRouterLlmClient implements LlmClient {

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    private WebClient client = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .defaultHeader("HTTP-Referer", "http://localhost:8080")
            .defaultHeader("X-Title", "RegressionRealm")
            .build();

    @Override
    public String complete(String prompt) {

        System.out.println("API KEY: " + apiKey);

        String body = """
            {
              "model": "%s",
              "response_format": { "type": "json_object" },
              "messages": [
                {"role": "system", "content": "You MUST return only valid JSON with no markdown or commentary."},
                {"role": "user", "content": %s}
              ]
            }
        """.formatted(model, toJson(prompt));

        String response = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractContent(response);
    }

    private String extractContent(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(raw);
            return node.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    private String toJson(String text) {
        try {
            return new ObjectMapper().writeValueAsString(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}