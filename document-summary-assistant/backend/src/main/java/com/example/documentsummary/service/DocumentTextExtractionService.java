package com.example.documentsummary.service;

import com.example.documentsummary.exception.TextExtractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Decides, per uploaded file, whether it is a normal (text) PDF, a
 * scanned/image-based PDF, or a plain image — and routes it to the
 * appropriate extraction strategy (PDFBox vs OCR).
 */
@Service
public class DocumentTextExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractionService.class);
    private static final int MIN_VIABLE_TEXT_LENGTH = 20;

    private final PdfExtractionService pdfExtractionService;
    private final OcrService ocrService;
    private final FileValidationService fileValidationService;

    public DocumentTextExtractionService(PdfExtractionService pdfExtractionService,
                                          OcrService ocrService,
                                          FileValidationService fileValidationService) {
        this.pdfExtractionService = pdfExtractionService;
        this.ocrService = ocrService;
        this.fileValidationService = fileValidationService;
    }

    public String extract(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            if (fileValidationService.isPdf(file)) {
                return extractFromPdf(bytes);
            }
            return extractFromImage(bytes);
        } catch (IOException e) {
            throw new TextExtractionException("Unable to read the uploaded file.", e);
        }
    }

    private String extractFromPdf(byte[] bytes) {
        PdfExtractionService.ExtractionResult result = pdfExtractionService.extractText(bytes);

        if (!result.isScanned) {
            log.info("PDF detected as text-based. Using PDFBox extraction.");
            return requireNonEmpty(result.text);
        }

        log.info("PDF detected as scanned/image-based. Falling back to OCR.");
        var pageImages = pdfExtractionService.renderPagesAsImages(bytes);
        String ocrText = ocrService.extractTextFromImages(pageImages);
        return requireNonEmpty(ocrText);
    }

    private String extractFromImage(byte[] bytes) {
        String text = ocrService.extractTextFromImageBytes(bytes);
        return requireNonEmpty(text);
    }

    private String requireNonEmpty(String text) {
        if (text == null || text.trim().length() < MIN_VIABLE_TEXT_LENGTH) {
            throw new TextExtractionException(
                    "We couldn't find enough readable text in this document to generate a summary.");
        }
        return text.trim();
    }
}
