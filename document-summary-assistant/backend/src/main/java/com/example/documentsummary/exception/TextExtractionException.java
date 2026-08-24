package com.example.documentsummary.exception;

/**
 * Thrown when text cannot be extracted from a document, either via PDFBox
 * parsing or Tesseract OCR.
 */
public class TextExtractionException extends RuntimeException {
    public TextExtractionException(String message) {
        super(message);
    }

    public TextExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
