package com.example.documentsummary.service;

/**
 * Abstraction over the AI provider used for summarization. Keeping this as
 * an interface means the concrete provider (Anthropic, OpenAI, etc.) can be
 * swapped by providing a different implementation bean, without touching
 * any calling code.
 */
public interface AiSummarizationClient {

    /**
     * Sends the given prompt to the AI provider and returns the raw text
     * response (expected to be a JSON string per the prompt's instructions).
     */
    String generate(String prompt);
}
