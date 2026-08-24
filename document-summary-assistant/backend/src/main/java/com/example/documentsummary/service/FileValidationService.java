package com.example.documentsummary.service;

import com.example.documentsummary.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Validates uploaded files on the backend (in addition to frontend checks),
 * since the frontend can always be bypassed.
 */
@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    @Value("${app.upload.max-file-size-bytes}")
    private long maxFileSizeBytes;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("The uploaded file is empty. Please choose a valid document.");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new InvalidFileException("The uploaded file exceeds the maximum allowed size of 10MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidFileException("The uploaded file has no recognizable extension.");
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    "Unsupported file type. Please upload a PDF, JPG, or PNG file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Unsupported file type. Please upload a PDF, JPG, or PNG file.");
        }
    }

    public boolean isPdf(MultipartFile file) {
        return "application/pdf".equalsIgnoreCase(file.getContentType());
    }
}
