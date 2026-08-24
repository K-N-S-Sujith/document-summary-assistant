package com.example.documentsummary.service;

import com.example.documentsummary.dto.SummaryLength;
import com.example.documentsummary.dto.SummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestrates the full summarization workflow:
 * validate -> extract text -> build prompt -> call AI -> parse response.
 */
@Service
public class DocumentSummaryService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSummaryService.class);

    private final FileValidationService fileValidationService;
    private final DocumentTextExtractionService textExtractionService;
    private final PromptBuilder promptBuilder;
    private final AiSummarizationClient aiSummarizationClient;
    private final AiResponseParser aiResponseParser;

    public DocumentSummaryService(FileValidationService fileValidationService,
                                   DocumentTextExtractionService textExtractionService,
                                   PromptBuilder promptBuilder,
                                   AiSummarizationClient aiSummarizationClient,
                                   AiResponseParser aiResponseParser) {
        this.fileValidationService = fileValidationService;
        this.textExtractionService = textExtractionService;
        this.promptBuilder = promptBuilder;
        this.aiSummarizationClient = aiSummarizationClient;
        this.aiResponseParser = aiResponseParser;
    }

    public SummaryResponse summarize(MultipartFile file, String lengthParam) {
        SummaryLength length = SummaryLength.fromString(lengthParam);

        fileValidationService.validate(file);

        log.info("Extracting text from uploaded file: {}", file.getOriginalFilename());
        String documentText = textExtractionService.extract(file);

        String prompt = promptBuilder.buildPrompt(documentText, length);

        log.info("Requesting AI summary (length={})", length);
        String rawAiOutput = aiSummarizationClient.generate(prompt);

        return aiResponseParser.parse(rawAiOutput);
    }
}
