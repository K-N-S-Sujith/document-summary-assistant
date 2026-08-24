import { useState } from 'react'
import FileUpload from './components/FileUpload.jsx'
import SummaryOptions from './components/SummaryOptions.jsx'
import Loading from './components/Loading.jsx'
import SummaryResult from './components/SummaryResult.jsx'
import { summarizeDocument } from './services/api.js'

export default function App() {
  const [selectedFile, setSelectedFile] = useState(null)
  const [length, setLength] = useState('medium')
  const [isLoading, setIsLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  async function handleGenerate() {
    if (!selectedFile) {
      setError('Please select a file before generating a summary.')
      return
    }

    setIsLoading(true)
    setError(null)
    setResult(null)

    try {
      const data = await summarizeDocument(selectedFile, length)
      setResult(data)
    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  function handleNewDocument() {
    setSelectedFile(null)
    setResult(null)
    setError(null)
    setLength('medium')
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-container">
          <div className="brand-row">
            <div className="brand-icon">D</div>
            <h1>Document Summary <span>Assistant</span></h1>
          </div>
          <p>Extract AI summaries, key points, main ideas, and suggestions from PDF and image files.</p>
        </div>
      </header>

      <main className="app-main">
        {!result && (
          <div className="card">
            <FileUpload
              selectedFile={selectedFile}
              onFileSelected={setSelectedFile}
              disabled={isLoading}
            />

            <SummaryOptions length={length} onChange={setLength} disabled={isLoading} />

            {error && (
              <div className="error-banner">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <span>{error}</span>
              </div>
            )}

            <button
              type="button"
              className="btn btn--primary btn--full"
              onClick={handleGenerate}
              disabled={isLoading || !selectedFile}
            >
              {isLoading ? (
                'Processing Document...'
              ) : (
                <>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                  </svg>
                  <span>Generate Summary</span>
                </>
              )}
            </button>

            {isLoading && <Loading />}
          </div>
        )}

        {result && (
          <div className="card">
            <SummaryResult result={result} onNewDocument={handleNewDocument} />
          </div>
        )}
      </main>

      <footer className="app-footer">
        <p>DocuMind AI &copy; 2026 — Powered by Local Ollama &amp; Spring Boot</p>
      </footer>
    </div>
  )
}
