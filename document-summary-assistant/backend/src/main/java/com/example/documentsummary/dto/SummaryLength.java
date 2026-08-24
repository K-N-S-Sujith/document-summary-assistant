package com.example.documentsummary.dto;

/**
 * Represents the requested length/detail level of the generated summary.
 */
public enum SummaryLength {
    SHORT,
    MEDIUM,
    LONG;

    /**
     * Parses a user-supplied string (case-insensitive) into a SummaryLength.
     * Defaults to MEDIUM if the value is null or unrecognized-safe fallback is desired by caller.
     */
    public static SummaryLength fromString(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        try {
            return SummaryLength.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid length value: '" + value + "'. Must be one of short, medium, long.");
        }
    }
}
