package com.example.documentsummary.exception;

/**
 * Thrown when an uploaded file fails validation (bad type, empty, too large).
 */
public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}
