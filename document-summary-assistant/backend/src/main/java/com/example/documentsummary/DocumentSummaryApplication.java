package com.example.documentsummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class DocumentSummaryApplication {

    private static final Logger log = LoggerFactory.getLogger(DocumentSummaryApplication.class);

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(DocumentSummaryApplication.class, args);
    }

    /**
     * Searches for a `.env` file in standard relative paths (current directory, parent directory)
     * and populates system properties for any key not already present in the environment.
     */
    private static void loadDotEnv() {
        Path[] candidatePaths = new Path[]{
                Paths.get(".env"),
                Paths.get("..", ".env"),
                Paths.get("backend", ".env")
        };

        for (Path path : candidatePaths) {
            File file = path.toFile();
            if (file.exists() && file.isFile()) {
                log.info("Loading environment variables from `.env` file at: {}", file.getAbsolutePath());
                try {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = trimmed.indexOf('=');
                        if (eqIdx > 0) {
                            String key = trimmed.substring(0, eqIdx).trim();
                            String val = trimmed.substring(eqIdx + 1).trim();
                            if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                                val = val.substring(1, val.length() - 1);
                            }
                            if (System.getenv(key) == null && System.getProperty(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to read `.env` file from {}: {}", file.getAbsolutePath(), e.getMessage());
                }
                break;
            }
        }
    }
}
