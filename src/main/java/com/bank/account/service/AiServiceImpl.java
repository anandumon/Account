package com.bank.account.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private final WebClient webClient;
    private final String model;
    private final Resource systemPromptResource;

    public AiServiceImpl(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.model}") String model,
            @Value("classpath:system-prompt.txt") Resource systemPromptResource
    ) {
        this.model = model;
        this.systemPromptResource = systemPromptResource;

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
    @Override
    public String askAi(String question) {

        String systemPrompt;
        try {
            systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load system prompt", e);
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", question)
                )
        );

        JsonNode response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            return "No response from Groq.";
        }

        System.out.println("GROQ RESPONSE: " + response.toPrettyString());

        JsonNode choices = response.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            return "AI returned no choices.";
        }

        JsonNode message = choices.get(0).path("message");
        if (message.isMissingNode()) {
            return "AI response missing message.";
        }

        return message.path("content").asText("No content in response.");
    }
}