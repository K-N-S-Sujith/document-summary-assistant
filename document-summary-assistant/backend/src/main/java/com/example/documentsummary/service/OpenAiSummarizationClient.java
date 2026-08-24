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
 * Concrete AiSummarizationClient implementation that calls OpenAI or OpenAI-compatible
 * Chat Completions endpoints (e.g., https://api.openai.com/v1/chat/completions).
 */
@Component("openAiSummarizationClient")
public class OpenAiSummarizationClient implements AiSummarizationClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiSummarizationClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    public OpenAiSummarizationClient(@Value("${app.ai.api-url}") String apiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    @Override
    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException(
                    "AI service is not configured. Please set the AI_API_KEY environment variable.");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model != null && !model.isBlank() ? model : "gpt-3.5-turbo",
                "max_tokens", 2000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String rawResponse = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(60))
                    .block();

            return extractTextFromOpenAiResponse(rawResponse);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI provider call failed", e);
            WebClientResponseException wcre = findWebClientResponseException(e);
            if (wcre != null) {
                log.error("OpenAI API call failed with HTTP status {} and body: {}",
                        wcre.getStatusCode(), wcre.getResponseBodyAsString());
                if (wcre.getStatusCode().value() == 429) {
                    throw new AiServiceException("OpenAI API rate limit or quota exceeded (HTTP 429). Please check your OpenAI API key billing details or usage limits.", e);
                }
                if (wcre.getStatusCode().value() == 401) {
                    throw new AiServiceException("OpenAI API authentication failed (HTTP 401). Please check your AI_API_KEY in .env.", e);
                }
                throw new AiServiceException("OpenAI API call failed with HTTP status " + wcre.getStatusCode() + ".", e);
            }
            throw new AiServiceException("Failed to reach the OpenAI summarization service: " + e.getMessage(), e);
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

    public String extractTextFromOpenAiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode messageNode = choices.get(0).path("message");
                JsonNode contentNode = messageNode.path("content");
                if (contentNode.isTextual() || !contentNode.asText("").isBlank()) {
                    return contentNode.asText();
                }
            }
            throw new AiServiceException("The OpenAI service returned an unexpected response format.");
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse the OpenAI service response.", e);
        }
    }
}
