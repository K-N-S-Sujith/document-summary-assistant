package com.example.documentsummary.controller;

import com.example.documentsummary.dto.SummaryResponse;
import com.example.documentsummary.service.DocumentSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentSummaryService documentSummaryService;

    public DocumentController(DocumentSummaryService documentSummaryService) {
        this.documentSummaryService = documentSummaryService;
    }

    /**
     * Accepts a PDF or image file plus a desired summary length, and returns
     * an AI-generated structured summary.
     */
    @PostMapping(value = "/summarize", consumes = "multipart/form-data")
    public ResponseEntity<SummaryResponse> summarize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "length", defaultValue = "medium") String length) {

        SummaryResponse response = documentSummaryService.summarize(file, length);
        return ResponseEntity.ok(response);
    }
}
