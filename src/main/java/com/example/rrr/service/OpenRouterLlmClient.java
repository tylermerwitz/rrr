package com.example.rrr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Profile("!dev") // ✅ Active unless dev profile
public class OpenRouterLlmClient implements LlmClient {

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Override
    public String complete(String prompt) {

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

        String response = WebClient.builder()
                .build()
                .post()
                .uri("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost")
                .header("X-Title", "TestApp")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new RuntimeException("HTTP " + resp.statusCode() + ": " + b))))
                .bodyToMono(String.class)
                .block();

        System.out.println("REQUEST BODY:");
        System.out.println(body);

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