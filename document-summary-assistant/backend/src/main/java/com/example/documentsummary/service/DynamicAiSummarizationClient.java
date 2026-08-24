package com.example.documentsummary.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Primary AI client bean that delegates to OpenAiSummarizationClient,
 * AnthropicSummarizationClient, or MockSummarizationClient based on configuration
 * or auto-detection.
 */
@Component
@Primary
public class DynamicAiSummarizationClient implements AiSummarizationClient {

    private static final Logger log = LoggerFactory.getLogger(DynamicAiSummarizationClient.class);

    private final AiSummarizationClient openAiClient;
    private final AiSummarizationClient anthropicClient;
    private final AiSummarizationClient ollamaClient;
    private final AiSummarizationClient mockClient;

    @Value("${app.ai.provider:auto}")
    private String provider;

    @Value("${app.ai.api-url:}")
    private String apiUrl;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    public DynamicAiSummarizationClient(
            @Qualifier("openAiSummarizationClient") AiSummarizationClient openAiClient,
            @Qualifier("anthropicSummarizationClient") AiSummarizationClient anthropicClient,
            @Qualifier("ollamaSummarizationClient") AiSummarizationClient ollamaClient,
            @Qualifier("mockSummarizationClient") AiSummarizationClient mockClient) {
        this.openAiClient = openAiClient;
        this.anthropicClient = anthropicClient;
        this.ollamaClient = ollamaClient;
        this.mockClient = mockClient;
    }

    @Override
    public String generate(String prompt) {
        AiSummarizationClient targetClient = resolveClient();
        return targetClient.generate(prompt);
    }

    public AiSummarizationClient resolveClient() {
        if ("mock".equalsIgnoreCase(provider) || "demo".equalsIgnoreCase(provider)
                || "mock".equalsIgnoreCase(apiKey) || "demo".equalsIgnoreCase(apiKey)) {
            log.info("Using Mock AI client (demo/offline mode)");
            return mockClient;
        }

        if ("ollama".equalsIgnoreCase(provider) || "local".equalsIgnoreCase(provider)) {
            log.info("Using Ollama client (configured via app.ai.provider={})", provider);
            return ollamaClient;
        }

        if ("openai".equalsIgnoreCase(provider)) {
            log.info("Using OpenAI client (configured via app.ai.provider=openai)");
            return openAiClient;
        }

        if ("anthropic".equalsIgnoreCase(provider)) {
            log.info("Using Anthropic client (configured via app.ai.provider=anthropic)");
            return anthropicClient;
        }

        // Auto-detection based on API URL
        if (apiUrl != null && (apiUrl.contains("11434") || apiUrl.contains("ollama"))) {
            log.info("Auto-detected Ollama provider based on API URL: {}", apiUrl);
            return ollamaClient;
        }

        if (apiUrl != null && (apiUrl.contains("openai.com") || apiUrl.contains("groq.com") || apiUrl.contains("chat/completions"))) {
            log.info("Auto-detected OpenAI/compatible provider based on API URL: {}", apiUrl);
            return openAiClient;
        }

        // Auto-detection based on API Key format
        if (apiKey != null && apiKey.startsWith("sk-ant-")) {
            log.info("Auto-detected Anthropic provider based on API Key prefix");
            return anthropicClient;
        }

        if (apiKey != null && (apiKey.startsWith("sk-proj-") || apiKey.startsWith("sk-") || apiKey.startsWith("gsk_"))) {
            log.info("Auto-detected OpenAI/compatible provider based on API Key prefix");
            return openAiClient;
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No AI API key found. Falling back to Mock AI client for demonstration.");
            return mockClient;
        }

        log.info("Defaulting to Anthropic client (API URL: {})", apiUrl);
        return anthropicClient;
    }
}
