package com.example.documentsummary.service;

import com.example.documentsummary.dto.SummaryResponse;
import com.example.documentsummary.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Safely parses the AI provider's raw text output into a SummaryResponse.
 * Handles cases where the model wraps JSON in markdown fences or adds
 * stray text around the JSON object.
 */
@Component
public class AiResponseParser {

    private static final Logger log = LoggerFactory.getLogger(AiResponseParser.class);
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*}");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SummaryResponse parse(String rawAiOutput) {
        String jsonCandidate = extractJsonObject(rawAiOutput);

        try {
            JsonNode root = objectMapper.readTree(jsonCandidate);

            String summary = root.path("summary").asText("");
            List<String> keyPoints = toStringList(root.path("keyPoints"));
            List<String> mainIdeas = toStringList(root.path("mainIdeas"));
            List<String> improvementSuggestions = toStringList(root.path("improvementSuggestions"));

            if (summary.isBlank()) {
                throw new AiServiceException("The AI response did not contain a summary.");
            }

            return new SummaryResponse(summary, keyPoints, mainIdeas, improvementSuggestions);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI JSON response: {}", rawAiOutput, e);
            throw new AiServiceException(
                    "The AI service returned a response we couldn't understand. Please try again.", e);
        }
    }

    private String extractJsonObject(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            throw new AiServiceException("The AI service returned an empty response.");
        }

        String cleaned = rawOutput
                .replace("```json", "")
                .replace("```", "")
                .trim();

        Matcher matcher = JSON_OBJECT_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group();
        }

        throw new AiServiceException("The AI service response did not contain valid JSON.");
    }

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                String value = item.asText("").trim();
                if (!value.isEmpty()) {
                    result.add(value);
                }
            }
        }
        return result;
    }
}
