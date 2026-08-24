package com.example.documentsummary.exception;

/**
 * Thrown when the AI summarization provider fails, times out, or returns
 * a response that cannot be safely parsed.
 */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
