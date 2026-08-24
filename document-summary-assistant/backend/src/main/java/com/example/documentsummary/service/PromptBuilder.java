package com.example.documentsummary.service;

import com.example.documentsummary.dto.SummaryLength;
import org.springframework.stereotype.Component;

/**
 * Builds the prompt sent to the AI provider based on the requested summary
 * length. Kept separate from the AI client so prompt tuning doesn't touch
 * networking code.
 */
@Component
public class PromptBuilder {

    private static final int MAX_INPUT_CHARS = 15000;

    public String buildPrompt(String documentText, SummaryLength length) {
        String truncated = documentText.length() > MAX_INPUT_CHARS
                ? documentText.substring(0, MAX_INPUT_CHARS) + "\n\n[Content truncated]"
                : documentText;

        String lengthInstructions = switch (length) {
            case SHORT -> "Write a summary of 3-5 sentences and list 3-5 key points.";
            case MEDIUM -> "Write a summary of 1-3 paragraphs and list 5-8 key points.";
            case LONG -> "Write a detailed, thorough summary and list 8-12 key points.";
        };

        return """
                You are an assistant that analyzes documents and produces structured summaries.

                %s
                Also identify the main ideas of the document (2-5 items) and suggest concrete
                improvements to the document's clarity, structure, or content (2-5 items).

                Respond with ONLY a valid JSON object, no markdown code fences, no preamble,
                in exactly this shape:

                {
                  "summary": "string",
                  "keyPoints": ["string", "..."],
                  "mainIdeas": ["string", "..."],
                  "improvementSuggestions": ["string", "..."]
                }

                Document content:
                ---
                %s
                ---
                """.formatted(lengthInstructions, truncated);
    }
}
