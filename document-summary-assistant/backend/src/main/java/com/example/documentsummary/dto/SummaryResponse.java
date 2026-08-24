package com.example.documentsummary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Structured response returned to the frontend after a successful summarization.
 */
public class SummaryResponse {

    private String summary;

    @JsonProperty("keyPoints")
    private List<String> keyPoints;

    @JsonProperty("mainIdeas")
    private List<String> mainIdeas;

    @JsonProperty("improvementSuggestions")
    private List<String> improvementSuggestions;

    public SummaryResponse() {
    }

    public SummaryResponse(String summary, List<String> keyPoints, List<String> mainIdeas,
                            List<String> improvementSuggestions) {
        this.summary = summary;
        this.keyPoints = keyPoints;
        this.mainIdeas = mainIdeas;
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public List<String> getMainIdeas() {
        return mainIdeas;
    }

    public void setMainIdeas(List<String> mainIdeas) {
        this.mainIdeas = mainIdeas;
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }
}
