import './Help.css'

function Help() {
  return (
    <div className="help-page">
      <div className="help-container">
        <h1>Centrum pomocy</h1>
        <p className="subtitle">Jak możemy Ci pomóc?</p>

        {/* <div className="search-box">
          <input type="text" placeholder="Wyszukaj odpowiedzi..." />
        </div> */}

        <div className="help-sections">
          <div className="help-card">
            <h3>Zamówienia i dostawa</h3>
            <p>Informacje o statusie zamówienia, czasie dostawy i kosztach przesyłki</p>
          </div>

          <div className="help-card">
            <h3>Zwroty i reklamacje</h3>
            <p>Jak zwrócić produkt lub zgłosić reklamację</p>
          </div>

          <div className="help-card">
            <h3>Płatności</h3>
            <p>Dostępne metody płatności i bezpieczeństwo transakcji</p>
          </div>

          <div className="help-card">
            <h3>Konto użytkownika</h3>
            <p>Zarządzanie kontem, danymi osobowymi i preferencjami</p>
          </div>

          <div className="help-card">
            <h3>Rozmiary i dopasowanie</h3>
            <p>Tabele rozmiarów i porady dotyczące wyboru produktów</p>
          </div>

          <div className="help-card">
            <h3>Kontakt</h3>
            <p>Skontaktuj się z naszym zespołem obsługi klienta</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Help
