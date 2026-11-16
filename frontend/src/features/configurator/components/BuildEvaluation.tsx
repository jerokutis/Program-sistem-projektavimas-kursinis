import { useEffect, useState } from 'react'

type BuildParts = {
  processorId: number
  motherboardId: number
  memoryId: number
  graphicsCardId: number
  storageId: number
  powerSupplyId: number
  coolerId: number
  caseId: number
}

type BuildEvaluationProps = {
  companyId: string
  parts: BuildParts
  budget: string
}

type EvaluationResult = {
  compatibilityIssues: string[]
}

export default function BuildEvaluation({
  companyId,
  parts,
  budget,
}: BuildEvaluationProps) {
  const [result, setResult] = useState<EvaluationResult | null>(null)
  const [error, setError] = useState('')

  const normalizedBudget = budget.trim().replace(',', '.')
  const amount = Number(normalizedBudget)
  const validBudget =
    /^\d{1,8}(\.\d{1,2})?$/.test(normalizedBudget) && amount > 0
  const hasAllParts = Object.values(parts).every((id) => id > 0)

  useEffect(() => {
    if (!hasAllParts || !validBudget) {
      return
    }

    const controller = new AbortController()

    async function evaluateBuild() {
      try {
        const response = await fetch(
          `/api/companies/${companyId}/builds/evaluate`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ ...parts, budget: amount }),
            signal: controller.signal,
          },
        )

        if (response.status === 404) {
          throw new Error(
            'Pasirinktos detalės kataloge nebėra. Atnaujink sąrašą ir pasirink iš naujo.',
          )
        }

        if (!response.ok) {
          throw new Error('Nepavyko patikrinti komplektacijos suderinamumo.')
        }

        const data: EvaluationResult = await response.json()

        if (!controller.signal.aborted) {
          setResult(data)
        }
      } catch (cause) {
        if (controller.signal.aborted) {
          return
        }

        if (cause instanceof TypeError) {
          setError('Nepavyko susisiekti su serveriu.')
        } else if (cause instanceof Error) {
          setError(cause.message)
        } else {
          setError('Nepavyko patikrinti komplektacijos suderinamumo.')
        }
      }
    }

    const timer = window.setTimeout(evaluateBuild, 300)

    return () => {
      window.clearTimeout(timer)
      controller.abort()
    }
  }, [companyId, parts, amount, hasAllParts, validBudget])

  if (budget.trim() !== '' && !validBudget) {
    return (
      <p role="alert">
        Įvesk biudžetą nuo 0,01 iki 99 999 999,99 € su ne daugiau kaip dviem
        skaitmenimis po kablelio.
      </p>
    )
  }

  if (!hasAllParts || !validBudget) {
    return null
  }

  if (error) {
    return <p role="alert">{error}</p>
  }

  if (!result) {
    return <p role="status">Tikrinamas suderinamumas…</p>
  }

  return (
    <div className="compatibility-result" aria-live="polite">
      <h3>Suderinamumas</h3>

      {result.compatibilityIssues.length === 0 && (
        <p>Suderinamumo problemų nerasta.</p>
      )}

      {result.compatibilityIssues.length > 0 && (
        <ul>
          {result.compatibilityIssues.map((issue) => (
            <li key={issue}>{issue}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
