package com.example.documentsummary.service;

import com.example.documentsummary.exception.TextExtractionException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Wraps Tesseract OCR (via tess4j) to extract text from images and from
 * rendered pages of scanned PDFs.
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    @Value("${app.ocr.tessdata-path}")
    private String tessdataPath;

    private Tesseract createTesseractInstance() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");
        return tesseract;
    }

    /**
     * Runs OCR on a single image (e.g. an uploaded JPG/PNG).
     */
    public String extractTextFromImageBytes(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new TextExtractionException("The uploaded file is not a valid image.");
            }
            return createTesseractInstance().doOCR(image);
        } catch (IOException e) {
            throw new TextExtractionException("Unable to read the uploaded image file.", e);
        } catch (TesseractException e) {
            log.error("OCR failed for image", e);
            throw new TextExtractionException(
                    "We couldn't extract text from this image. It may be too low quality.", e);
        }
    }

    /**
     * Runs OCR on a set of rendered pages (used for scanned PDFs), concatenating
     * the result with a page separator.
     */
    public String extractTextFromImages(List<BufferedImage> pages) {
        Tesseract tesseract = createTesseractInstance();
        StringBuilder combined = new StringBuilder();

        for (int i = 0; i < pages.size(); i++) {
            try {
                String pageText = tesseract.doOCR(pages.get(i));
                combined.append(pageText);
                if (i < pages.size() - 1) {
                    combined.append("\n\n--- Page ").append(i + 2).append(" ---\n\n");
                }
            } catch (TesseractException e) {
                log.error("OCR failed on page {}", i + 1, e);
                throw new TextExtractionException(
                        "We couldn't extract text from page " + (i + 1) + " of the scanned document.", e);
            }
        }

        return combined.toString();
    }
}
