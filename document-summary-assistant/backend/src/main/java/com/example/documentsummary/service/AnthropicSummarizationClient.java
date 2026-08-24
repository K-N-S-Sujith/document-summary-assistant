package com.example.documentsummary.service;

import com.example.documentsummary.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Concrete AiSummarizationClient implementation that calls the Anthropic
 * Messages API. This is the only class that knows about Anthropic's request
 * or response shape — swap this class out to use a different provider.
 */
@Component("anthropicSummarizationClient")
public class AnthropicSummarizationClient implements AiSummarizationClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicSummarizationClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    public AnthropicSummarizationClient(@Value("${app.ai.api-url}") String apiUrl) {
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
                "model", model != null && !model.isBlank() ? model : "claude-sonnet-4-5",
                "max_tokens", 2000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String rawResponse = webClient.post()
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(60))
                    .block();

            return extractTextFromAnthropicResponse(rawResponse);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Anthropic API call failed with HTTP status {} and body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AiServiceException("Anthropic API call failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI provider call failed", e);
            throw new AiServiceException("Failed to reach the AI summarization service.", e);
        }
    }

    private String extractTextFromAnthropicResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentArray = root.path("content");
            if (contentArray.isArray() && !contentArray.isEmpty()) {
                StringBuilder text = new StringBuilder();
                for (JsonNode block : contentArray) {
                    if ("text".equals(block.path("type").asText())) {
                        text.append(block.path("text").asText());
                    }
                }
                if (!text.isEmpty()) {
                    return text.toString();
                }
            }
            throw new AiServiceException("The AI service returned an unexpected response format.");
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse the AI service response.", e);
        }
    }
}
