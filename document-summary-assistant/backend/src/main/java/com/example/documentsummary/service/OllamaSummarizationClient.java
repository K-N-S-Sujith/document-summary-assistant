package com.example.documentsummary.service;

import com.example.documentsummary.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Concrete AiSummarizationClient implementation that calls local Ollama endpoints
 * (e.g., http://localhost:11434/api/generate or http://localhost:11434/v1/chat/completions).
 */
@Component("ollamaSummarizationClient")
public class OllamaSummarizationClient implements AiSummarizationClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaSummarizationClient.class);

    private final WebClient webClient;
    private final String rawApiUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:codellama:latest}")
    private String model;

    public OllamaSummarizationClient(@Value("${app.ai.api-url:http://localhost:11434/api/generate}") String apiUrl) {
        String effectiveUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl : "http://localhost:11434/api/generate";
        this.rawApiUrl = effectiveUrl;
        this.webClient = WebClient.builder()
                .baseUrl(effectiveUrl)
                .build();
    }

    @Override
    public String generate(String prompt) {
        String effectiveModel = (model != null && !model.isBlank()) ? model : "codellama:latest";
        boolean isChatCompletions = rawApiUrl.contains("chat/completions");

        Map<String, Object> requestBody;
        if (isChatCompletions) {
            requestBody = Map.of(
                    "model", effectiveModel,
                    "stream", false,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
        } else {
            requestBody = Map.of(
                    "model", effectiveModel,
                    "prompt", prompt,
                    "stream", false
            );
        }

        try {
            WebClient.RequestBodySpec requestSpec = webClient.post()
                    .header("Content-Type", "application/json");

            if (apiKey != null && !apiKey.isBlank()) {
                requestSpec.header("Authorization", "Bearer " + apiKey);
            }

            String rawResponse = requestSpec
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(120))
                    .block();

            return extractTextFromOllamaResponse(rawResponse);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama provider call failed", e);
            WebClientResponseException wcre = findWebClientResponseException(e);
            if (wcre != null) {
                log.error("Ollama API call failed with HTTP status {} and body: {}",
                        wcre.getStatusCode(), wcre.getResponseBodyAsString());
                throw new AiServiceException("Ollama API call failed with HTTP status " + wcre.getStatusCode() + ": " + wcre.getResponseBodyAsString(), e);
            }
            throw new AiServiceException("Failed to connect to local Ollama service at " + rawApiUrl + ". Please ensure Ollama is running.", e);
        }
    }

    private WebClientResponseException findWebClientResponseException(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof WebClientResponseException wcre) {
                return wcre;
            }
            current = current.getCause();
        }
        return null;
    }

    public String extractTextFromOllamaResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            // Native /api/generate format: {"response": "..."}
            JsonNode responseNode = root.path("response");
            if (responseNode.isTextual() && !responseNode.asText().isBlank()) {
                return responseNode.asText();
            }

            // OpenAI compatible /v1/chat/completions format: {"choices": [{"message": {"content": "..."}}]}
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode contentNode = choices.get(0).path("message").path("content");
                if (contentNode.isTextual() && !contentNode.asText().isBlank()) {
                    return contentNode.asText();
                }
            }

            // Native /api/chat format: {"message": {"content": "..."}}
            JsonNode chatMessageContent = root.path("message").path("content");
            if (chatMessageContent.isTextual() && !chatMessageContent.asText().isBlank()) {
                return chatMessageContent.asText();
            }

            throw new AiServiceException("The Ollama service returned an empty or unrecognized response format.");
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse the Ollama service response.", e);
        }
    }
}
