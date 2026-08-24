package com.example.documentsummary.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of AiSummarizationClient for offline testing and demo mode.
 * Returns a valid structured JSON response without requiring external API keys.
 */
@Component("mockSummarizationClient")
public class MockSummarizationClient implements AiSummarizationClient {

    private static final Logger log = LoggerFactory.getLogger(MockSummarizationClient.class);

    @Override
    public String generate(String prompt) {
        log.info("Generating mock AI summary response (demo/offline mode)");

        // Extract a snippet of text from prompt to personalize mock output
        String snippet = "Uploaded Document";
        int idx = prompt.indexOf("Document content:");
        if (idx != -1) {
            String content = prompt.substring(idx).replaceAll("---", "").trim();
            if (content.length() > 50) {
                snippet = content.substring(0, 50).replaceAll("[\"\\r\\n]", " ") + "...";
            }
        }

        return """
                {
                  "summary": "This is a demonstration summary generated in Mock Mode for '%s'. The document contains structured information covering key concepts, core requirements, and practical recommendations.",
                  "keyPoints": [
                    "Successfully parsed document content and validated formatting.",
                    "Demonstrates backend extraction and AI client orchestration.",
                    "Provides key takeaways and actionable improvement suggestions.",
                    "Fully functional offline mock response mode for development."
                  ],
                  "mainIdeas": [
                    "Demonstrating document text processing pipeline.",
                    "Providing clear and structured summaries."
                  ],
                  "improvementSuggestions": [
                    "Provide a paid OpenAI, Anthropic, or Groq API key in .env for live AI responses.",
                    "Consider formatting document headings clearly for optimal AI parsing."
                  ]
                }
                """.formatted(snippet);
    }
}
