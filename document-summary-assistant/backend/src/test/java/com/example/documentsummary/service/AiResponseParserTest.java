package com.example.documentsummary.service;

import com.example.documentsummary.dto.SummaryResponse;
import com.example.documentsummary.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiResponseParserTest {

    private AiResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new AiResponseParser();
    }

    @Test
    void testParse_ValidJsonWithMarkdownFences() {
        String raw = """
                ```json
                {
                  "summary": "This document outlines project specs.",
                  "keyPoints": ["Point 1", "Point 2"],
                  "mainIdeas": ["Idea 1"],
                  "improvementSuggestions": ["Fix typos"]
                }
                ```
                """;

        SummaryResponse response = parser.parse(raw);
        assertNotNull(response);
        assertEquals("This document outlines project specs.", response.getSummary());
        assertEquals(2, response.getKeyPoints().size());
        assertEquals("Point 1", response.getKeyPoints().get(0));
        assertEquals(1, response.getMainIdeas().size());
        assertEquals(1, response.getImprovementSuggestions().size());
    }

    @Test
    void testParse_MissingSummaryField() {
        String raw = "{\"keyPoints\": [\"Point 1\"]}";
        assertThrows(AiServiceException.class, () -> parser.parse(raw));
    }
}
