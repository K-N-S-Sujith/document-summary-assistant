import { useState } from 'react'

function Section({ title, icon, children }) {
  return (
    <section className="result-section">
      <h3 className="result-section__title">
        <span className="title-icon">{icon}</span>
        <span>{title}</span>
      </h3>
      {children}
    </section>
  )
}

function BulletList({ items }) {
  if (!items || items.length === 0) return <p className="empty-hint">Nothing extracted.</p>
  return (
    <ul className="bullet-list">
      {items.map((item, index) => (
        <li key={index}>{item}</li>
      ))}
    </ul>
  )
}

export default function SummaryResult({ result, onNewDocument }) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    const text = [
      'Summary:',
      result.summary,
      '',
      'Key Points:',
      ...(result.keyPoints || []).map((p) => `- ${p}`),
      '',
      'Main Ideas:',
      ...(result.mainIdeas || []).map((p) => `- ${p}`),
      '',
      'Improvement Suggestions:',
      ...(result.improvementSuggestions || []).map((p) => `- ${p}`),
    ].join('\n')

    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className="summary-result">
      <div className="result-header">
        <h2>Document Analysis</h2>
      </div>

      <Section
        title="Executive Summary"
        icon={
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
        }
      >
        <p className="summary-text">{result.summary}</p>
      </Section>

      <Section
        title="Key Points"
        icon={
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <polyline points="9 11 12 14 22 4" />
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
          </svg>
        }
      >
        <BulletList items={result.keyPoints} />
      </Section>

      <Section
        title="Main Ideas"
        icon={
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
        }
      >
        <BulletList items={result.mainIdeas} />
      </Section>

      <Section
        title="Improvement Suggestions"
        icon={
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
            <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z" />
          </svg>
        }
      >
        <BulletList items={result.improvementSuggestions} />
      </Section>

      <div className="result-actions">
        <button type="button" className="btn btn--secondary" onClick={handleCopy}>
          {copied ? (
            <>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              <span>Copied to Clipboard</span>
            </>
          ) : (
            <>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              <span>Copy Analysis</span>
            </>
          )}
        </button>
        <button type="button" className="btn btn--primary" onClick={onNewDocument}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span>New Document</span>
        </button>
      </div>
    </div>
  )
}
