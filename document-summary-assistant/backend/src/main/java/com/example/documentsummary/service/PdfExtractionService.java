package com.example.documentsummary.service;

import com.example.documentsummary.exception.TextExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts text from PDF files using Apache PDFBox, and renders pages to
 * images when a PDF turns out to be scanned (no extractable text) so that
 * OCR can be run on it instead.
 */
@Service
public class PdfExtractionService {

    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

    /**
     * A PDF whose extracted text is shorter than this (per page, on average)
     * is treated as a scanned/image-based document rather than a text PDF.
     */
    private static final int MIN_TEXT_LENGTH_THRESHOLD = 30;

    private final org.apache.pdfbox.text.PDFTextStripper textStripper;

    public PdfExtractionService() throws IOException {
        this.textStripper = new org.apache.pdfbox.text.PDFTextStripper();
        this.textStripper.setSortByPosition(true);
        this.textStripper.setAddMoreFormatting(true);
    }

    public static class ExtractionResult {
        public final String text;
        public final boolean isScanned;

        public ExtractionResult(String text, boolean isScanned) {
            this.text = text;
            this.isScanned = isScanned;
        }
    }

    /**
     * Attempts direct text extraction. If the result looks too sparse
     * (i.e. the PDF is likely scanned), returns isScanned=true along with
     * the rendered page images so the caller can run OCR on them.
     */
    public ExtractionResult extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            String text = textStripper.getText(document);
            int pageCount = document.getNumberOfPages();
            boolean looksScanned = pageCount > 0
                    && text.trim().length() < MIN_TEXT_LENGTH_THRESHOLD * pageCount;

            return new ExtractionResult(text, looksScanned);
        } catch (IOException e) {
            throw new TextExtractionException("Unable to parse the PDF file. It may be corrupted.", e);
        }
    }

    /**
     * Renders each page of a PDF to a BufferedImage at a resolution suitable
     * for OCR. Used when a PDF is detected as scanned/image-based.
     */
    public List<BufferedImage> renderPagesAsImages(byte[] pdfBytes) {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                images.add(image);
            }
        } catch (IOException e) {
            throw new TextExtractionException("Unable to render scanned PDF pages for OCR.", e);
        }
        return images;
    }
}
