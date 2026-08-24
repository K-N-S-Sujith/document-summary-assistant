# Document Summary Assistant

An AI-powered web app that turns PDFs and images into structured summaries — a summary, key points, main ideas, and improvement suggestions — in one click.

## Features

- Drag-and-drop or click-to-browse upload for PDF, JPG, and PNG files
- Automatic text extraction:
  - Native text extraction from PDFs via **Apache PDFBox**
  - Automatic fallback to **Tesseract OCR** for scanned PDFs or images
- Three summary lengths: Short, Medium, Long
- AI-generated Summary, Key Points, Main Ideas, and Improvement Suggestions
- Copy-to-clipboard and "New Document" reset
- Friendly error handling for invalid files, extraction failures, and AI/network errors
- Fully responsive UI (desktop, tablet, mobile), built with plain CSS

## Architecture

```
document-summary-assistant/
├── frontend/               React + Vite SPA
│   └── src/
│       ├── components/     FileUpload, SummaryOptions, Loading, SummaryResult
│       ├── services/       api.js — talks to the backend
│       └── App.jsx
└── backend/                Spring Boot REST API
    └── src/main/java/com/example/documentsummary/
        ├── controller/      DocumentController — HTTP layer only
        ├── service/         extraction, OCR, prompt building, AI client, orchestration
        ├── dto/             request/response shapes
        ├── exception/       custom exceptions + @ControllerAdvice handler
        └── config/          CORS configuration
```

**Request flow:** `FileUpload` → `POST /api/documents/summarize` → `FileValidationService` →
`DocumentTextExtractionService` (PDFBox or OCR, auto-detected) → `PromptBuilder` →
`AiSummarizationClient` (Anthropic) → `AiResponseParser` → JSON response → `SummaryResult` UI.

The AI provider is isolated behind the `AiSummarizationClient` interface
(`AnthropicSummarizationClient` is the current implementation), so switching providers means
adding one new class — no other code changes.

## Technology Stack

**Frontend:** React, Vite, plain CSS (no UI framework)
**Backend:** Java 21, Spring Boot 3, Maven, Apache PDFBox, Tess4J (Tesseract OCR), Spring WebFlux (for the AI HTTP client)

## How the Application Works

1. User uploads a PDF/JPG/PNG and picks a summary length.
2. The backend validates the file (type, size, non-empty).
3. For PDFs, PDFBox attempts direct text extraction. If the extracted text is too sparse, the
   PDF is treated as scanned and its pages are rendered to images for OCR.
4. For JPG/PNG, OCR runs directly on the image.
5. The extracted text is inserted into a length-specific prompt and sent to the AI provider,
   which is instructed to return strict JSON.
6. The backend safely parses that JSON (handling stray markdown fences, etc.) into a
   `SummaryResponse` and returns it to the frontend.

## Prerequisites

- Java 21+ and Maven 3.9+
- Node.js 18+ and npm
- Tesseract OCR installed locally (see [OCR Setup](#ocr-setup) below)
- Local Ollama running (or OpenAI / Anthropic API keys)

## Local Setup

### 1. Configure environment variables

Configure `.env` for your preferred AI provider:

**Local Ollama:**
```bash
AI_PROVIDER=ollama
AI_API_URL=http://localhost:11434/api/generate
AI_MODEL=codellama:latest
```

**OpenAI / Anthropic:**
```bash
AI_PROVIDER=openai
AI_API_KEY=sk-...
AI_API_URL=https://api.openai.com/v1/chat/completions
AI_MODEL=gpt-3.5-turbo
```

### 2. Run the backend

```bash
cd backend
export FRONTEND_URL=http://localhost:5173
export TESSDATA_PATH=/usr/share/tesseract-ocr/5/tessdata   # adjust to your system
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173`.

## Environment Variables

| Variable        | Where    | Description                                                 |
|------------------|----------|-------------------------------------------------------------|
| `AI_PROVIDER`   | backend  | `ollama`, `openai`, `anthropic`, `mock`, or `auto`           |
| `AI_API_KEY`     | backend  | API key for cloud AI providers (optional for Ollama)        |
| `AI_API_URL`     | backend  | Endpoint URL (defaults to `http://localhost:11434/api/generate`) |
| `AI_MODEL`       | backend  | Model name (e.g. `codellama:latest`, `llama3.2`, `gpt-3.5-turbo`) |
| `FRONTEND_URL`   | backend  | Allowed CORS origin(s), comma-separated                     |
| `TESSDATA_PATH`  | backend  | Path to Tesseract's `tessdata` directory                    |
| `PORT`           | backend  | Server port (Render sets this automatically)                |
| `VITE_API_URL`   | frontend | Backend base URL used by the frontend                       |

## API Documentation

### `POST /api/documents/summarize`

**Request:** `multipart/form-data`

| Field  | Type   | Description                          |
|--------|--------|---------------------------------------|
| file   | file   | PDF, JPG, or PNG (max 10MB)            |
| length | string | `short` \| `medium` \| `long`          |

**Success response — `200 OK`:**

```json
{
  "summary": "...",
  "keyPoints": ["...", "..."],
  "mainIdeas": ["...", "..."],
  "improvementSuggestions": ["...", "..."]
}
```

**Error response — `4xx` / `5xx`:**

```json
{
  "message": "Human-readable error message",
  "status": 400,
  "timestamp": "2026-08-24T12:00:00Z"
}
```

| Status | Meaning                                             |
|--------|------------------------------------------------------|
| 400    | Invalid file (type, size, empty) or bad request       |
| 422    | Text extraction failed (unreadable/corrupted document)|
| 502    | AI service failed or returned an unparseable response |
| 500    | Unexpected server error                                |

## OCR Setup

Tesseract must be installed locally for OCR to work (used for scanned PDFs and images).

**macOS:**
```bash
brew install tesseract
```

**Ubuntu/Debian:**
```bash
sudo apt-get install tesseract-ocr libtesseract-dev
```

**Windows:** install via the [UB-Mannheim Tesseract installer](https://github.com/UB-Mannheim/tesseract/wiki).

After installing, set `TESSDATA_PATH` to the directory containing the `.traineddata` files
(e.g. `/usr/share/tesseract-ocr/5/tessdata` on Ubuntu, `/opt/homebrew/share/tessdata` on Apple
Silicon Macs).

## Screenshots

_Add screenshots of the upload screen, loading state, and results screen here._

## Deployment

### Frontend (Vercel)

1. Import the `frontend/` directory as a new Vercel project.
2. Set the build command to `npm run build` and output directory to `dist`.
3. Add environment variable `VITE_API_URL` pointing to your deployed backend URL.

### Backend (Render)

1. Create a new Web Service pointing at the `backend/` directory.
2. Build command: `mvn clean package -DskipTests`
3. Start command: `java -jar target/document-summary-assistant-1.0.0.jar`
4. Add environment variables: `AI_API_KEY`, `AI_API_URL`, `AI_MODEL`, `FRONTEND_URL`, `TESSDATA_PATH`.
5. Ensure Tesseract is available on the Render instance (use a Docker deploy with Tesseract
   installed in the image, since Render's default Java environment does not include it).

No localhost URLs are hardcoded anywhere — both `FRONTEND_URL` (CORS) and `VITE_API_URL`
(API base URL) are environment-driven.

## Future Improvements

- Persist summary history (would require adding a database)
- Support additional file types (DOCX, TXT)
- Streaming AI responses for faster perceived performance
- Multi-language OCR and summarization
- Automated tests (unit tests for services, component tests for the frontend)
