package com.example.documentsummary.service;

import com.example.documentsummary.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OllamaSummarizationClientTest {

    private OllamaSummarizationClient client;

    @BeforeEach
    void setUp() {
        client = new OllamaSummarizationClient("http://localhost:11434/api/generate");
    }

    @Test
    void testExtractTextFromOllamaResponse_NativeApiGenerate() {
        String json = "{\n" +
                "  \"model\": \"codellama:latest\",\n" +
                "  \"response\": \"{\\\"summary\\\": \\\"Ollama summary\\\", \\\"keyPoints\\\": [\\\"p1\\\"]}\",\n" +
                "  \"done\": true\n" +
                "}";

        String extracted = client.extractTextFromOllamaResponse(json);
        assertEquals("{\"summary\": \"Ollama summary\", \"keyPoints\": [\"p1\"]}", extracted);
    }

    @Test
    void testExtractTextFromOllamaResponse_ChatCompletionsFormat() {
        String json = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "{\\"summary\\": \\"Chat completions summary\\"}"
                      }
                    }
                  ]
                }
                """;

        String extracted = client.extractTextFromOllamaResponse(json);
        assertEquals("{\"summary\": \"Chat completions summary\"}", extracted);
    }

    @Test
    void testExtractTextFromOllamaResponse_InvalidJsonStructure() {
        String json = "{\"invalid\": \"response\"}";

        assertThrows(AiServiceException.class, () -> client.extractTextFromOllamaResponse(json));
    }
}
