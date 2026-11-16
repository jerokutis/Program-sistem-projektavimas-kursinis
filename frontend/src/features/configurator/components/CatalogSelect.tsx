import useCatalogOptions from '../hooks/useCatalogOptions'

type CatalogItem = {
  id: number
  name: string
  price: number
}

type CatalogSelectProps<T extends CatalogItem> = {
  id: string
  label: string
  placeholder: string
  emptyMessage: string
  url: string
  value: string
  onChange: (id: string, price: number) => void
  describe: (item: T) => string
}

const euro = new Intl.NumberFormat('lt-LT', {
  style: 'currency',
  currency: 'EUR',
})

export default function CatalogSelect<T extends CatalogItem>({
  id,
  label,
  placeholder,
  emptyMessage,
  url,
  value,
  onChange,
  describe,
}: CatalogSelectProps<T>) {
  const catalog = useCatalogOptions<T>(url)

  if (catalog.status === 'loading') {
    return <p>{label} – kraunama...</p>
  }

  if (catalog.status === 'error') {
    return <p role="alert">{label} – nepavyko gauti sąrašo.</p>
  }

  if (catalog.items.length === 0) {
    return <p>{emptyMessage}</p>
  }

  return (
    <div>
      <label htmlFor={id}>{label}</label>
      <select
        id={id}
        value={value}
        onChange={(event) => {
          const id = event.target.value
          const item = catalog.items.find((option) => String(option.id) === id)
          onChange(id, item?.price ?? 0)
        }}
      >
        <option value="">{placeholder}</option>
        {catalog.items.map((item) => (
          <option key={item.id} value={item.id}>
            {item.name} · {describe(item)} · {euro.format(item.price)}
          </option>
        ))}
      </select>
    </div>
  )
}
