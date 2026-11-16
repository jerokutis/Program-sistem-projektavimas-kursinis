import { useState, type FormEvent } from 'react'
import './supplier.css'

type GraphicsCard = {
  id: number
  name: string
  price: number
  lengthMm: number
  powerConsumptionW: number | null
  published: boolean
}

type SupplierDashboardProps = {
  companyId: string
}

const euro = new Intl.NumberFormat('lt-LT', {
  style: 'currency',
  currency: 'EUR',
})

function errorMessage(status: number) {
  if (status === 400) {
    return 'Patikrink pavadinimą, kainą, ilgį ir galią.'
  }

  if (status === 401) {
    return 'Netinkamas įmonės API raktas.'
  }

  if (status === 403) {
    return 'Šis raktas priklauso kitai įmonei.'
  }

  if (status === 404) {
    return 'Vaizdo plokštė šios įmonės kataloge nerasta.'
  }

  return 'Nepavyko atlikti veiksmo. Pabandyk dar kartą.'
}

export default function SupplierDashboard({
  companyId,
}: SupplierDashboardProps) {
  const [enteredKey, setEnteredKey] = useState('')
  const [apiKey, setApiKey] = useState('')
  const [cards, setCards] = useState<GraphicsCard[]>([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const publicUrl = `/api/companies/${companyId}/graphics-cards`
  const managementUrl = `/api/companies/${companyId}/management/graphics-cards`

  async function runAction(action: () => Promise<void>) {
    if (busy) {
      return
    }

    setBusy(true)
    setError('')


    try {
      await action()
    } catch (cause) {
      if (cause instanceof TypeError) {
        setError('Nepavyko susisiekti su serveriu.')
      } else if (cause instanceof Error) {
        setError(cause.message)
      } else {
        setError('Nepavyko atlikti veiksmo.')
      }
    } finally {
      setBusy(false)
    }
  }

  async function openDashboard(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const key = enteredKey.trim()

    await runAction(async () => {
      const response = await fetch(managementUrl, {
        headers: { 'X-API-Key': key },
      })

      if (!response.ok) {
        throw new Error(errorMessage(response.status))
      }

      const data: GraphicsCard[] = await response.json()
      setCards(data)
      setApiKey(key)
      setEnteredKey('')
    })
  }

  async function createCard(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const form = event.currentTarget
    const fields = new FormData(form)

    await runAction(async () => {
      const response = await fetch(publicUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': apiKey,
        },
        body: JSON.stringify({
          name: String(fields.get('name')).trim(),
          price: Number(fields.get('price')),
          lengthMm: Number(fields.get('lengthMm')),
          powerConsumptionW: Number(fields.get('powerConsumptionW')),
        }),
      })

      if (!response.ok) {
        throw new Error(errorMessage(response.status))
      }

      const created: Omit<GraphicsCard, 'published'> = await response.json()
      setCards((current) => [...current, { ...created, published: false }])
      form.reset()
    })
  }

  async function publishCard(card: GraphicsCard) {
    await runAction(async () => {
      const response = await fetch(`${managementUrl}/${card.id}/publish`, {
        method: 'PUT',
        headers: { 'X-API-Key': apiKey },
      })

      if (!response.ok) {
        throw new Error(errorMessage(response.status))
      }

      const publishedCard: GraphicsCard = await response.json()

      setCards((current) =>
        current.map((item) => {
          if (item.id === publishedCard.id) {
            return publishedCard
          }

          return item
        }),
      )
    })
  }

  async function deleteCard(card: GraphicsCard) {
    if (!window.confirm(`Ištrinti „${card.name}“?`)) {
      return
    }

    await runAction(async () => {
      const response = await fetch(`${managementUrl}/${card.id}`, {
        method: 'DELETE',
        headers: { 'X-API-Key': apiKey },
      })

      if (!response.ok) {
        throw new Error(errorMessage(response.status))
      }

      setCards((current) => current.filter((item) => item.id !== card.id))
    })
  }

  function closeDashboard() {
    setApiKey('')
    setEnteredKey('')
    setCards([])
    setError('')
  }

  return (
    <main className="supplier-dashboard">
      <h1>Tiekėjo valdymas</h1>

      {!apiKey && (
        <form className="supplier-form" onSubmit={openDashboard}>
          <label htmlFor="supplier-key">Pasirinktos įmonės API raktas</label>
          <input
            id="supplier-key"
            type="password"
            autoComplete="off"
            value={enteredKey}
            onChange={(event) => setEnteredKey(event.target.value)}
            required
          />
          <button type="submit" disabled={busy || !enteredKey.trim()}>
            Atverti valdymą
          </button>
        </form>
      )}

      {error && <p role="alert">{error}</p>}

      {apiKey && (
        <>
          <button type="button" disabled={busy} onClick={closeDashboard}>
            Atsijungti
          </button>

          <form className="supplier-form" onSubmit={createCard}>
            <h2>Nauja vaizdo plokštė</h2>

            <label htmlFor="card-name">Pavadinimas</label>
            <input id="card-name" name="name" maxLength={100} required />

            <label htmlFor="card-price">Pardavimo kaina (€)</label>
            <input
              id="card-price"
              name="price"
              type="number"
              min="0.01"
              max="99999999.99"
              step="0.01"
              required
            />

            <label htmlFor="card-length">Ilgis (mm)</label>
            <input
              id="card-length"
              name="lengthMm"
              type="number"
              min="1"
              step="1"
              required
            />

            <label htmlFor="card-power">Energijos sąnaudos (W)</label>
            <input
              id="card-power"
              name="powerConsumptionW"
              type="number"
              min="1"
              step="1"
              required
            />

            <button type="submit" disabled={busy}>
              Pridėti juodraštį
            </button>
          </form>

          <section>
            <h2>Įmonės vaizdo plokštės</h2>

            {cards.length === 0 && <p>Įmonės katalogas tuščias.</p>}

            {cards.map((card) => (
              <article className="supplier-card" key={card.id}>
                <h3>{card.name}</h3>
                <p>
                  {euro.format(card.price)} · {card.lengthMm} mm
                </p>

                {card.published && <p>Paskelbta</p>}
                {!card.published && <p>Juodraštis</p>}

                <div className="supplier-actions">
                  {!card.published && (
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => publishCard(card)}
                    >
                      Paskelbti
                    </button>
                  )}

                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => deleteCard(card)}
                  >
                    Ištrinti
                  </button>
                </div>
              </article>
            ))}
          </section>
        </>
      )}
    </main>
  )
}
