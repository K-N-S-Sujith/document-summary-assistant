import { useRef, useState } from 'react'

const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png']
const ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png']
const MAX_SIZE_BYTES = 10 * 1024 * 1024 // 10MB

function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function validateFile(file) {
  if (!file) return 'No file selected.'
  if (file.size === 0) return 'The selected file is empty.'
  if (file.size > MAX_SIZE_BYTES) return 'File is too large. Maximum size is 10MB.'

  const extension = file.name.split('.').pop()?.toLowerCase()
  const typeOk = ALLOWED_TYPES.includes(file.type)
  const extensionOk = ALLOWED_EXTENSIONS.includes(extension)

  if (!typeOk && !extensionOk) {
    return 'Unsupported file type. Please upload a PDF, JPG, or PNG file.'
  }
  return null
}

export default function FileUpload({ selectedFile, onFileSelected, disabled }) {
  const [isDragging, setIsDragging] = useState(false)
  const [error, setError] = useState(null)
  const inputRef = useRef(null)

  function handleFiles(fileList) {
    const file = fileList?.[0]
    const validationError = validateFile(file)
    if (validationError) {
      setError(validationError)
      onFileSelected(null)
      return
    }
    setError(null)
    onFileSelected(file)
  }

  function handleDrop(e) {
    e.preventDefault()
    setIsDragging(false)
    if (disabled) return
    handleFiles(e.dataTransfer.files)
  }

  function handleRemove() {
    setError(null)
    onFileSelected(null)
    if (inputRef.current) inputRef.current.value = ''
  }

  return (
    <div className="file-upload">
      {!selectedFile && (
        <div
          className={`dropzone ${isDragging ? 'dropzone--active' : ''}`}
          onDragOver={(e) => {
            e.preventDefault()
            if (!disabled) setIsDragging(true)
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          onClick={() => !disabled && inputRef.current?.click()}
          role="button"
          tabIndex={0}
        >
          <div className="dropzone__icon-wrapper">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
          </div>
          <p className="dropzone__title">Drag &amp; drop your document here</p>
          <p className="dropzone__subtitle">or click to browse from your computer (max 10MB)</p>
          <div className="dropzone__tags">
            <span className="tag">PDF</span>
            <span className="tag">JPG</span>
            <span className="tag">PNG</span>
          </div>
          <input
            ref={inputRef}
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            hidden
            disabled={disabled}
            onChange={(e) => handleFiles(e.target.files)}
          />
        </div>
      )}

      {selectedFile && (
        <div className="selected-file">
          <div className="selected-file__left">
            <div className="selected-file__icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </div>
            <div className="selected-file__info">
              <span className="selected-file__name">{selectedFile.name}</span>
              <span className="selected-file__size">{formatFileSize(selectedFile.size)}</span>
            </div>
          </div>
          <button
            type="button"
            className="btn btn--ghost"
            onClick={handleRemove}
            disabled={disabled}
          >
            Remove
          </button>
        </div>
      )}

      {error && <p className="error-text">{error}</p>}
    </div>
  )
}
