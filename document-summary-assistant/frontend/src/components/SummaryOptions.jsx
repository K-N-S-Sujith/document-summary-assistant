const OPTIONS = [
  { value: 'short', label: 'Short', hint: '3-5 sentences' },
  { value: 'medium', label: 'Medium', hint: '1-3 paragraphs' },
  { value: 'long', label: 'Long', hint: 'Detailed summary' },
]

export default function SummaryOptions({ length, onChange, disabled }) {
  return (
    <div className="summary-options">
      <span className="summary-options__label">Summary length</span>
      <div className="summary-options__buttons">
        {OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={`length-btn ${length === option.value ? 'length-btn--active' : ''}`}
            onClick={() => onChange(option.value)}
            disabled={disabled}
          >
            <span>{option.label}</span>
            <small>{option.hint}</small>
          </button>
        ))}
      </div>
    </div>
  )
}
