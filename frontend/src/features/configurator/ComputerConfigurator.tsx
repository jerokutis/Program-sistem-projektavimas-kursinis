import { useState } from 'react'
import BuildEvaluation from './components/BuildEvaluation'
import CatalogSelect from './components/CatalogSelect'

import './budget.css'

const euro = new Intl.NumberFormat('lt-LT', {
  style: 'currency',
  currency: 'EUR',
})

type ConfiguratorProps = {
  companyId: string
  budget: string
  onBudgetChange: (value: string) => void
}

type CatalogItem = {
  id: number
  name: string
  price: number
}

type Motherboard = CatalogItem & {
  socket: string
  memoryType: string
}

type Processor = CatalogItem & {
  socket: string
}

type Memory = CatalogItem & {
  memoryType: string
  capacityGb: number
}

type GraphicsCard = CatalogItem & {
  lengthMm: number
}

type Storage = CatalogItem & {
  capacityGb: number
}

type ComputerCase = CatalogItem & {
  maxGpuLengthMm: number
}

type Cooler = CatalogItem & {
  heightMm: number
}

type PowerSupply = CatalogItem & {
  wattage: number
}

const emptyBuild = {
  motherboardId: 0,
  processorId: 0,
  memoryId: 0,
  graphicsCardId: 0,
  storageId: 0,
  caseId: 0,
  coolerId: 0,
  powerSupplyId: 0,
}

type PartId = keyof typeof emptyBuild

export default function ComputerConfigurator({
  companyId,
  budget,
  onBudgetChange,
}: ConfiguratorProps) {
  const [parts, setParts] = useState(emptyBuild)
  const [prices, setPrices] = useState(emptyBuild)

  const totalCents = Object.values(prices).reduce(
    (sum, cents) => sum + cents,
    0,
  )

  const normalizedBudget = budget.trim().replace(',', '.')
  const budgetCents = Math.round(Number(normalizedBudget) * 100)
  const validBudget =
    /^\d{1,8}(\.\d{1,2})?$/.test(normalizedBudget) && budgetCents > 0
  const overBudget = validBudget && totalCents > budgetCents

  let balance = ''

  if (validBudget) {
    balance = 'Liko ' + euro.format((budgetCents - totalCents) / 100)

    if (overBudget) {
      balance =
        'Biudžetas viršytas ' + euro.format((totalCents - budgetCents) / 100)
    }
  }

  const selectedCount = Object.values(parts).filter((id) => id > 0).length

  function selectPart(field: PartId, value: string, price: number) {
    setPrices((current) => ({ ...current, [field]: Math.round(price * 100) }))
    setParts((current) => ({ ...current, [field]: Number(value) }))
  }

  function selectedValue(field: PartId) {
    if (parts[field] === 0) {
      return ''
    }

    return String(parts[field])
  }

  function catalogUrl(category: string, filters: PartId[] = []) {
    const url = `/api/companies/${companyId}/${category}`
    const params = new URLSearchParams()

    for (const field of filters) {
      if (parts[field] > 0) {
        params.set(field, String(parts[field]))
      }
    }

    if (params.size === 0) {
      return url
    }

    return `${url}/compatible?${params}`
  }

  let powerSupplyUrl = catalogUrl('power-supplies')

  if (parts.processorId && parts.graphicsCardId) {
    powerSupplyUrl = catalogUrl('power-supplies', [
      'processorId',
      'graphicsCardId',
    ])
  }

  return (
    <main className="configurator">
      <h1>Kompiuterio komplektavimas</h1>

      <section className="parts-list">
        <header className="parts-heading">
          <h2>Komponentai</h2>
          <span aria-live="polite">Pasirinkta {selectedCount} iš 8</span>
        </header>

        <CatalogSelect<Motherboard>
          id="motherboard"
          label="Pagrindinė plokštė"
          placeholder="Pasirink pagrindinę plokštę"
          emptyMessage="Tinkamų pagrindinių plokščių nėra."
          url={catalogUrl('motherboards', [
            'processorId',
            'memoryId',
            'storageId',
          ])}
          value={selectedValue('motherboardId')}
          onChange={(value, price) => selectPart('motherboardId', value, price)}
          describe={(board) => `${board.socket} · ${board.memoryType}`}
        />

        <CatalogSelect<Processor>
          id="processor"
          label="Procesorius"
          placeholder="Pasirink procesorių"
          emptyMessage="Tinkamų procesorių nėra."
          url={catalogUrl('processors', [
            'motherboardId',
            'coolerId',
            'graphicsCardId',
            'powerSupplyId',
          ])}
          value={selectedValue('processorId')}
          onChange={(value, price) => selectPart('processorId', value, price)}
          describe={(processor) => processor.socket}
        />

        <CatalogSelect<Memory>
          id="memory"
          label="Operatyvioji atmintis (RAM)"
          placeholder="Pasirink RAM"
          emptyMessage="Tinkamos RAM nėra."
          url={catalogUrl('memories', ['motherboardId'])}
          value={selectedValue('memoryId')}
          onChange={(value, price) => selectPart('memoryId', value, price)}
          describe={(memory) =>
            `${memory.capacityGb} GB · ${memory.memoryType}`
          }
        />

        <CatalogSelect<GraphicsCard>
          id="graphics-card"
          label="Vaizdo plokštė"
          placeholder="Pasirink vaizdo plokštę"
          emptyMessage="Tinkamų vaizdo plokščių nėra."
          url={catalogUrl('graphics-cards', [
            'caseId',
            'processorId',
            'powerSupplyId',
          ])}
          value={selectedValue('graphicsCardId')}
          onChange={(value, price) =>
            selectPart('graphicsCardId', value, price)
          }
          describe={(card) => `${card.lengthMm} mm`}
        />

        <CatalogSelect<Storage>
          id="storage"
          label="SSD diskas"
          placeholder="Pasirink SSD"
          emptyMessage="Tinkamų SSD nėra."
          url={catalogUrl('storages', ['motherboardId'])}
          value={selectedValue('storageId')}
          onChange={(value, price) => selectPart('storageId', value, price)}
          describe={(storage) => `${storage.capacityGb} GB`}
        />

        <CatalogSelect<ComputerCase>
          id="computer-case"
          label="Korpusas"
          placeholder="Pasirink korpusą"
          emptyMessage="Tinkamų korpusų nėra."
          url={catalogUrl('cases', ['graphicsCardId', 'coolerId'])}
          value={selectedValue('caseId')}
          onChange={(value, price) => selectPart('caseId', value, price)}
          describe={(item) => `vaizdo plokštei iki ${item.maxGpuLengthMm} mm`}
        />

        <CatalogSelect<Cooler>
          id="cooler"
          label="Procesoriaus aušintuvas"
          placeholder="Pasirink aušintuvą"
          emptyMessage="Tinkamų aušintuvų nėra."
          url={catalogUrl('coolers', ['processorId', 'caseId'])}
          value={selectedValue('coolerId')}
          onChange={(value, price) => selectPart('coolerId', value, price)}
          describe={(cooler) => `${cooler.heightMm} mm`}
        />

        <CatalogSelect<PowerSupply>
          id="power-supply"
          label="Maitinimo blokas"
          placeholder="Pasirink maitinimo bloką"
          emptyMessage="Tinkamų maitinimo blokų nėra."
          url={powerSupplyUrl}
          value={selectedValue('powerSupplyId')}
          onChange={(value, price) => selectPart('powerSupplyId', value, price)}
          describe={(supply) => `${supply.wattage} W`}
        />
      </section>

      <aside className="budget-summary" aria-labelledby="budget-heading">
        <h2 id="budget-heading">Komplektacijos suvestinė</h2>

        <div className="budget-row">
          <label htmlFor="budget">Planuojamas biudžetas (€)</label>
          <input
            id="budget"
            type="text"
            inputMode="decimal"
            placeholder="Pvz., 1000,00"
            value={budget}
            onChange={(event) => onBudgetChange(event.target.value)}
          />
        </div>

        <p className="live-total">
          Pasirinktų dalių kaina{' '}
          <strong>{euro.format(totalCents / 100)}</strong>
        </p>

        {validBudget && (
          <div className="budget-meter" data-over={overBudget}>
            <progress
              aria-label="Panaudotas biudžetas"
              max={budgetCents}
              value={Math.min(totalCents, budgetCents)}
            />
            <p role="status">{balance}</p>
          </div>
        )}

        <BuildEvaluation
          key={JSON.stringify([companyId, parts, budget])}
          companyId={companyId}
          parts={parts}
          budget={budget}
        />
      </aside>
    </main>
  )
}
