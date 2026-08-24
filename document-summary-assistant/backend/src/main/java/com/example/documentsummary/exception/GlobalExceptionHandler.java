package com.example.documentsummary.exception;

import com.example.documentsummary.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Central place for translating exceptions into friendly, safe HTTP responses.
 * Never leaks stack traces or internal details (e.g. API keys) to the client.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex) {
        log.warn("Invalid file upload: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Upload exceeded max size: {}", ex.getMessage());
        return buildResponse("The uploaded file is too large. Maximum allowed size is 10MB.",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TextExtractionException.class)
    public ResponseEntity<ErrorResponse> handleTextExtraction(TextExtractionException ex) {
        log.error("Text extraction failed: {}", ex.getMessage(), ex);
        return buildResponse(
                "We couldn't read the content of this document. Please check the file and try again.",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiService(AiServiceException ex) {
        log.error("AI service failed: {}", ex.getMessage(), ex);
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "The summarization service is currently unavailable. Please try again shortly.";
        return buildResponse(msg, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected server error", ex);
        return buildResponse(
                "Something went wrong on our end. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        ErrorResponse body = new ErrorResponse(message, status.value());
        return ResponseEntity.status(status).body(body);
    }
}
