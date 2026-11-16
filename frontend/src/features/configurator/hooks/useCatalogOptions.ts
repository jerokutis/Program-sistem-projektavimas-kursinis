import { useEffect, useState } from 'react'

type CatalogResult<T> =
  { status: 'loading' } | { status: 'error' } | { status: 'ready'; items: T[] }

export default function useCatalogOptions<T>(url: string): CatalogResult<T> {
  const [response, setResponse] = useState<{
    url: string
    result: CatalogResult<T>
  } | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    async function loadOptions() {
      try {
        const response = await fetch(url, {
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error('Nepavyko gauti katalogo duomenų.')
        }

        const items: T[] = await response.json()

        if (!controller.signal.aborted) {
          setResponse({ url, result: { status: 'ready', items } })
        }
      } catch {
        if (!controller.signal.aborted) {
          setResponse({ url, result: { status: 'error' } })
        }
      }
    }

    void loadOptions()

    return () => controller.abort()
  }, [url])

  if (!response || response.url !== url) {
    return { status: 'loading' }
  }

  return response.result
}
