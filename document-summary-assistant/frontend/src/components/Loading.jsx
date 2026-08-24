export default function Loading({ message = 'Extracting text and generating AI summary...' }) {
  return (
    <div className="loading-wrapper">
      <div className="spinner-container" aria-hidden="true">
        <div className="spinner-ring" />
        <div className="spinner-ring spinner-ring--inner" />
      </div>
      <p className="loading-text">{message}</p>
      <p className="loading-subtext">Connecting with local Ollama engine...</p>
    </div>
  )
}
