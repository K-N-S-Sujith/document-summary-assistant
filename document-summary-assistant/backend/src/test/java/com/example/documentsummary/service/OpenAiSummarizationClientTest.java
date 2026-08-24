package com.example.documentsummary.service;

import com.example.documentsummary.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiSummarizationClientTest {

    private OpenAiSummarizationClient client;

    @BeforeEach
    void setUp() {
        client = new OpenAiSummarizationClient("https://api.openai.com/v1/chat/completions");
    }

    @Test
    void testExtractTextFromOpenAiResponse_ValidJson() {
        String json = """
                {
                  "id": "chatcmpl-123",
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "{\\"summary\\": \\"Test summary\\", \\"keyPoints\\": [\\"p1\\"]}"
                      }
                    }
                  ]
                }
                """;

        String extracted = client.extractTextFromOpenAiResponse(json);
        assertEquals("{\"summary\": \"Test summary\", \"keyPoints\": [\"p1\"]}", extracted);
    }

    @Test
    void testExtractTextFromOpenAiResponse_InvalidJsonStructure() {
        String json = "{\"invalid\": \"response\"}";

        assertThrows(AiServiceException.class, () -> client.extractTextFromOpenAiResponse(json));
    }
}
