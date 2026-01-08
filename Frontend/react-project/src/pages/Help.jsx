export default function Help() {
  return (
    <div className="min-h-[calc(100vh-56px)] bg-zinc-900 py-6">
      <div className="ui-container space-y-6">
        <div className="text-center">
          <h1 className="ui-h1">Centrum pomocy</h1>
          <p className="ui-muted">Jak mozemy Ci pomoc?</p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Zamowienia i dostawa</h3>
            <p className="mt-1 text-sm text-zinc-400">Informacje o statusie zamowienia, czasie dostawy i kosztach przesylki</p>
          </div>

          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Zwroty i reklamacje</h3>
            <p className="mt-1 text-sm text-zinc-400">Jak zwrocic produkt lub zglosic reklamacje</p>
          </div>

          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Platnosci</h3>
            <p className="mt-1 text-sm text-zinc-400">Dostepne metody platnosci i bezpieczenstwo transakcji</p>
          </div>

          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Konto uzytkownika</h3>
            <p className="mt-1 text-sm text-zinc-400">Zarzadzanie kontem, danymi osobowymi i preferencjami</p>
          </div>

          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Rozmiary i dopasowanie</h3>
            <p className="mt-1 text-sm text-zinc-400">Tabele rozmiarow i porady dotyczace wyboru produktow</p>
          </div>

          <div className="ui-section">
            <h3 className="ui-h2 text-lg">Kontakt</h3>
            <p className="mt-1 text-sm text-zinc-400">Skontaktuj sie z naszym zespolem obslugi klienta</p>
          </div>
        </div>
      </div>
    </div>
  )
}
