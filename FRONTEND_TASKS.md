# Lista Zadań Frontendowych (Backlog)

Poniżej znajduje się lista funkcjonalności do zaimplementowania po stronie frontendu, oparta na gotowym API backendowym.

---

## 1. Infrastruktura i Konfiguracja (Fundamenty)

- [ ] **Konfiguracja Klienta HTTP (np. Axios):**
    - [ ] Obsługa `Base URL` (np. `http://localhost:8080`).
    - [ ] **Interceptor:** Automatyczne dodawanie nagłówka `Authorization: Bearer <token>` do każdego zapytania (jeśli token istnieje).
- [ ] **Globalny Stan (Store - np. Redux/Pinia/Context):**
    - [ ] Przechowywanie danych zalogowanego użytkownika (`User`).
    - [ ] Przechowywanie stanu połączenia WebSocket.

## 2. Moduł Autentykacji (Auth)

- [ ] **Strona Logowania:**
    - [ ] Formularz (Email, Hasło).
    - [ ] Strzał do `POST /public/login`.
    - [ ] Zapisanie tokenu JWT (LocalStorage/Cookie).
- [ ] **Strona Rejestracji:**
    - [ ] Formularz (Imię, Email, Hasło).
    - [ ] Strzał do `POST /public/register`.
    - [ ] Przekierowanie do weryfikacji OTP.
- [ ] **Strona Weryfikacji Konta:**
    - [ ] Input na kod OTP.
    - [ ] Strzał do `POST /user/verify-otp`.
    - [ ] Przycisk "Wyślij ponownie" (`POST /user/send-otp`).
- [ ] **Proces Resetowania Hasła:**
    - [ ] Krok 1: Podaj email (`POST /public/send-reset-otp`).
    - [ ] Krok 2: Podaj OTP i nowe hasło (`POST /public/reset-password`).

## 3. Moduł Użytkownika (User Profile)

- [ ] **Widok "Mój Profil":**
    - [ ] Pobranie danych (`GET /user/profile`).
    - [ ] Wyświetlenie avatara, imienia, statusu weryfikacji.
- [ ] **Edycja Profilu:**
    - [ ] Zmiana zdjęcia:
        1. Upload pliku (`POST /user/upload/profile-image`).
        2. Odebranie URL.
        3. Wysłanie URL w `PATCH /user/profile`.
    - [ ] Zmiana imienia (`PATCH /user/profile`).

## 4. Moduł Ogłoszeń - Przeglądanie (Browsing)

- [ ] **Strona Główna:**
    - [ ] Wyświetlanie losowych ogłoszeń (`GET /public/listings/random`).
    - [ ] Drzewo kategorii w menu (`GET /public/category/all`).
- [ ] **Wyszukiwarka (Zaawansowana):**
    - [ ] Pasek wyszukiwania tekstowego (`query`).
    - [ ] Filtry dynamiczne:
        1. Po wybraniu kategorii pobierz jej atrybuty (`GET /public/category/attributes`).
        2. Wygeneruj odpowiednie inputy (Select dla ENUM, Range dla NUMBER).
    - [ ] Wysłanie zapytania (`POST /public/listings/search`).
- [ ] **Strona Szczegółów Ogłoszenia:**
    - [ ] Pobranie danych (`GET /public/listings/get/{id}`).
    - [ ] Galeria zdjęć.
    - [ ] Tabela atrybutów.
    - [ ] **Przycisk "Pokaż numer":** Dostępny tylko dla zalogowanych. Po kliknięciu strzał do `GET /user/listing/{id}/contact`.
    - [ ] **Przycisk "Obserwuj":** Obsługa serduszka (`POST/DELETE /user/interactions/favorites/{id}`). Stan początkowy z `GET .../status`.
    - [ ] **Przycisk "Napisz wiadomość":** Przekierowanie do czatu.

## 5. Moduł Ogłoszeń - Zarządzanie (Listing Management)

- [ ] **Kreator Dodawania Ogłoszenia (Wizard):**
    - [ ] Krok 1: Wybór kategorii.
    - [ ] Krok 2: Pobranie atrybutów dla kategorii i wygenerowanie formularza.
    - [ ] Krok 3: Upload zdjęć (`POST /user/upload/media-image`) -> zapisanie URLi w liście.
    - [ ] Krok 4: Uzupełnienie tytułu, opisu, ceny, lokalizacji.
    - [ ] Krok 5: Zapisanie (`POST /user/listing/create`). **Uwaga:** Ogłoszenie wpada jako DRAFT.
- [ ] **Panel "Moje Ogłoszenia":**
    - [ ] Lista z zakładkami/filtrami: Wszystkie, Aktywne, Oczekujące, Robocze (`GET /user/listing/my?status=...`).
    - [ ] **Akcje na kafelku ogłoszenia:**
        - [ ] "Edytuj" -> Przekierowanie do formularza edycji (pobranie danych z `GET .../edit-data`).
        - [ ] "Usuń" -> `DELETE ...`.
        - [ ] **"Wyślij do weryfikacji"** (dla DRAFT) -> `POST .../submit-for-approval`.
        - [ ] **"Zakończ"** (dla ACTIVE) -> `POST .../finish`.

## 6. Moduł Komunikatora (Messaging)

- [ ] **Globalna obsługa WebSocket:**
    - [ ] Nawiązanie połączenia przy starcie aplikacji (jeśli zalogowany).
    - [ ] Subskrypcja kanału `/user/queue/messages`.
    - [ ] Obsługa przychodzącej wiadomości:
        - [ ] Jeśli czat otwarty -> dodaj do widoku.
        - [ ] Jeśli czat zamknięty -> pokaż powiadomienie (Toast/Badge).
- [ ] **Widok "Wiadomości" (Inbox):**
    - [ ] Lista konwersacji po lewej (`GET /user/conversations`).
    - [ ] Okno czatu po prawej.
- [ ] **Okno Czatu:**
    - [ ] Pobranie historii po kliknięciu w konwersację (`GET .../messages`).
    - [ ] Wysłanie "Mark as Read" (`PATCH .../read`).
    - [ ] Wysyłanie wiadomości przez STOMP (`/app/chat.sendMessage`).
    - [ ] "Optymistyczne" dodawanie wysłanej wiadomości do widoku.

## 7. Moduł Ulubionych

- [ ] **Widok "Obserwowane":**
    - [ ] Lista ogłoszeń (`GET /user/interactions/favorites`).
    - [ ] Obsługa statusu ogłoszenia.

