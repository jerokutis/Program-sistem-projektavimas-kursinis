import { useState } from 'react'
import ComputerConfigurator from './features/configurator/ComputerConfigurator'
import useCatalogOptions from './features/configurator/hooks/useCatalogOptions'
import SupplierDashboard from './features/supplier/SupplierDashboard'
import './layout/workspace.css'

type Company = {
  id: number
  name: string
}

type View = 'configurator' | 'supplier'

export default function App() {
  const companies = useCatalogOptions<Company>('/api/companies')
  const [chosenId, setChosenId] = useState('')
  const [budget, setBudget] = useState('')
  const [view, setView] = useState<View>('configurator')

  let companyId = ''
  let status = 'Kraunami tiekėjai...'

  if (companies.status === 'error') {
    status = 'Nepavyko gauti tiekėjų. Atnaujink puslapį.'
  }

  if (companies.status === 'ready') {
    status = 'Tiekėjų nėra.'

    if (companies.items.length > 0) {
      companyId = chosenId || String(companies.items[0].id)
    }
  }

  return (
    <div className="workspace">
      <aside className="workspace-sidebar">
        <div className="supplier-switch">
          <label htmlFor="company">Komponentų tiekėjas</label>

          {companyId && companies.status === 'ready' && (
            <select
              id="company"
              value={companyId}
              onChange={(event) => setChosenId(event.target.value)}
            >
              {companies.items.map((company) => (
                <option key={company.id} value={company.id}>
                  {company.name}
                </option>
              ))}
            </select>
          )}

          {!companyId && <p role="status">{status}</p>}
        </div>

        <nav aria-label="Pagrindinis meniu">
          <span className="nav-heading">Meniu</span>

          <button
            type="button"
            aria-pressed={view === 'configurator'}
            onClick={() => setView('configurator')}
          >
            <span className="nav-grid" aria-hidden="true">
              <i />
              <i />
              <i />
              <i />
            </span>
            Komplektavimas
          </button>

          <button
            type="button"
            aria-pressed={view === 'supplier'}
            onClick={() => setView('supplier')}
          >
            Tiekėjo valdymas
          </button>
        </nav>
      </aside>

      <div className="workspace-content">
        {companyId && view === 'configurator' && (
          <ComputerConfigurator
            key={companyId}
            companyId={companyId}
            budget={budget}
            onBudgetChange={setBudget}
          />
        )}

        {companyId && view === 'supplier' && (
          <SupplierDashboard key={companyId} companyId={companyId} />
        )}

        {!companyId && (
          <main>
            <h1>Kompiuterio komplektavimas</h1>
          </main>
        )}
      </div>
    </div>
  )
}
