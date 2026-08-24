const API_BASE_URL =
  import.meta.env.VITE_API_URL || 'https://document-summary-assistant-o43b.onrender.com'

export async function summarizeDocument(file, length) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('length', length)

  let response

  try {
    response = await fetch(`${API_BASE_URL}/api/documents/summarize`, {
      method: 'POST',
      body: formData,
    })
  } catch (networkError) {
    throw new Error(
      'Unable to reach the server. Please check your connection and try again.'
    )
  }

  let data

  try {
    data = await response.json()
  } catch (parseError) {
    throw new Error('Received an unexpected response from the server.')
  }

  if (!response.ok) {
    throw new Error(
      data?.message || 'Something went wrong while generating the summary.'
    )
  }

  return data
}