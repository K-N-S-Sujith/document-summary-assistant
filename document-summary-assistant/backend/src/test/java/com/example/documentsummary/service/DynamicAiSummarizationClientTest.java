package com.example.documentsummary.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DynamicAiSummarizationClientTest {

    private AiSummarizationClient openAiClient;
    private AiSummarizationClient anthropicClient;
    private AiSummarizationClient ollamaClient;
    private AiSummarizationClient mockClient;
    private DynamicAiSummarizationClient dynamicClient;

    @BeforeEach
    void setUp() {
        openAiClient = mock(AiSummarizationClient.class);
        anthropicClient = mock(AiSummarizationClient.class);
        ollamaClient = mock(AiSummarizationClient.class);
        mockClient = mock(AiSummarizationClient.class);
        dynamicClient = new DynamicAiSummarizationClient(openAiClient, anthropicClient, ollamaClient, mockClient);
    }

    @Test
    void testResolveClient_OllamaExplicitProvider() throws Exception {
        setField(dynamicClient, "provider", "ollama");
        setField(dynamicClient, "apiUrl", "http://localhost:11434/api/generate");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(ollamaClient, resolved);
    }

    @Test
    void testResolveClient_OpenAiExplicitProvider() throws Exception {
        setField(dynamicClient, "provider", "openai");
        setField(dynamicClient, "apiUrl", "https://api.example.com");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(openAiClient, resolved);
    }

    @Test
    void testResolveClient_MockExplicitProvider() throws Exception {
        setField(dynamicClient, "provider", "mock");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(mockClient, resolved);
    }

    @Test
    void testResolveClient_AnthropicExplicitProvider() throws Exception {
        setField(dynamicClient, "provider", "anthropic");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(anthropicClient, resolved);
    }

    @Test
    void testResolveClient_AutoDetectOllamaUrl() throws Exception {
        setField(dynamicClient, "provider", "auto");
        setField(dynamicClient, "apiUrl", "http://localhost:11434/api/generate");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(ollamaClient, resolved);
    }

    @Test
    void testResolveClient_AutoDetectOpenAiUrl() throws Exception {
        setField(dynamicClient, "provider", "auto");
        setField(dynamicClient, "apiUrl", "https://api.openai.com/v1/chat/completions");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(openAiClient, resolved);
    }

    @Test
    void testResolveClient_AutoDetectOpenAiKeyPrefix() throws Exception {
        setField(dynamicClient, "provider", "auto");
        setField(dynamicClient, "apiUrl", "https://api.custom-proxy.com");
        setField(dynamicClient, "apiKey", "sk-proj-12345");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(openAiClient, resolved);
    }

    @Test
    void testResolveClient_DefaultAnthropic() throws Exception {
        setField(dynamicClient, "provider", "auto");
        setField(dynamicClient, "apiUrl", "https://api.anthropic.com/v1/messages");
        setField(dynamicClient, "apiKey", "sk-ant-12345");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(anthropicClient, resolved);
    }

    @Test
    void testResolveClient_FallbackToMockWhenKeyEmpty() throws Exception {
        setField(dynamicClient, "provider", "auto");
        setField(dynamicClient, "apiUrl", "https://api.anthropic.com/v1/messages");
        setField(dynamicClient, "apiKey", "");

        AiSummarizationClient resolved = dynamicClient.resolveClient();
        assertSame(mockClient, resolved);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
