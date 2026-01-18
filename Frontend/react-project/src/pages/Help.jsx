export default function Help() {
  const helpItems = [
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
      ),
      title: 'Zamowienia i dostawa',
      description: 'Informacje o statusie zamowienia, czasie dostawy i kosztach przesylki',
    },
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M16 15v-1a4 4 0 00-4-4H8m0 0l3 3m-3-3l3-3m9 14V5a2 2 0 00-2-2H6a2 2 0 00-2 2v16l4-2 4 2 4-2 4 2z" />
        </svg>
      ),
      title: 'Zwroty i reklamacje',
      description: 'Jak zwrocic produkt lub zglosic reklamacje',
    },
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
      ),
      title: 'Platnosci',
      description: 'Dostepne metody platnosci i bezpieczenstwo transakcji',
    },
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
      ),
      title: 'Konto uzytkownika',
      description: 'Zarzadzanie kontem, danymi osobowymi i preferencjami',
    },
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
        </svg>
      ),
      title: 'Rozmiary i dopasowanie',
      description: 'Tabele rozmiarow i porady dotyczace wyboru produktow',
    },
    {
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      ),
      title: 'Kontakt',
      description: 'Skontaktuj sie z naszym zespolem obslugi klienta',
    },
  ]

  return (
    <div className="min-h-[calc(100vh-64px)] py-8 sm:py-12">
      <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 space-y-8">
        <div className="text-center">
          <div className="inline-flex items-center gap-2 rounded-full bg-emerald-500/10 border border-emerald-500/20 px-4 py-1.5 text-emerald-400 text-sm font-medium mb-4">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            Wsparcie
          </div>
          <h1 className="text-3xl sm:text-4xl font-bold text-white mb-3">Centrum pomocy</h1>
          <p className="text-slate-400 text-lg max-w-xl mx-auto">Jak mozemy Ci pomoc? Znajdz odpowiedzi na najczesciej zadawane pytania.</p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {helpItems.map((item, index) => (
            <div
              key={index}
              className="group rounded-2xl border border-slate-800/50 bg-slate-800/30 p-6 transition-all hover:bg-slate-800/50 hover:border-slate-700/50 hover:shadow-lg hover:shadow-slate-950/50 cursor-pointer"
            >
              <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/20 flex items-center justify-center text-emerald-400 mb-4 group-hover:scale-110 transition-transform">
                {item.icon}
              </div>
              <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-emerald-400 transition-colors">{item.title}</h3>
              <p className="text-sm text-slate-400 leading-relaxed">{item.description}</p>
            </div>
          ))}
        </div>

        <div className="rounded-2xl border border-slate-800/50 bg-gradient-to-br from-slate-800/80 via-slate-800/50 to-slate-900/80 p-8 text-center">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-white mb-2">Potrzebujesz wiecej pomocy?</h2>
          <p className="text-slate-400 mb-6 max-w-md mx-auto">Nasz zespol jest gotowy, aby odpowiedziec na Twoje pytania</p>
          <button className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-500/25 transition-all hover:from-emerald-500 hover:to-teal-500 hover:shadow-emerald-500/40">
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
            Skontaktuj sie
          </button>
        </div>
      </div>
    </div>
  )
}
