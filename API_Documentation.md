# Dokumentacja API - Serwis Ogłoszeniowy (v3.0 - Final)

Poniżej znajduje się zaktualizowany opis wszystkich dostępnych endpointów.

**Ważna uwaga:** Wszystkie endpointy, których ścieżka **nie** zaczyna się od `/public/` (oraz endpoint WebSocket `/ws`), są **zabezpieczone** i wymagają wysłania w nagłówku poprawnego tokenu autoryzacyjnego: `Authorization: Bearer <TWÓJ_TOKEN_JWT>`.

**Konto Administratora (Domyślne):**
*   Email: `admin@binm.com`
*   Hasło: `admin123`

---

## Obsługa Błędów

W przypadku wystąpienia błędu, API zwraca odpowiedź w formacie JSON ze stosownym kodem HTTP (4xx lub 500).

**Format Odpowiedzi Błędu (`ErrorResponse`):**
```json
{
  "code": "ERROR_CODE",       // Unikalny kod błędu (np. USER_ALREADY_EXISTS, VALIDATION_ERROR)
  "message": "Opis błędu",    // Czytelny opis dla programisty
  "status": 400,              // Kod HTTP
  "timestamp": "2023-10-27T10:00:00Z",
  "details": "..."            // Opcjonalne szczegóły (np. lista pól z błędami walidacji)
}
```

---

## 1. Autentykacja i Profile Publiczne

### `POST /public/register`
> Tworzy nowego użytkownika i wysyła email z kodem OTP do weryfikacji.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "name": "Jan Kowalski",      // Wymagane
      "email": "jan.kowalski@example.com", // Wymagane, unikalny
      "password": "password123"   // Wymagane, min. 8 znaków
    }
    ```

### `POST /public/login`
> Loguje użytkownika.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "email": "jan.kowalski@example.com", // Wymagane
      "password": "password123"          // Wymagane
    }
    ```
*   **Success Response (200 OK):**
    *   **Body:** `{"email": "...", "token": "eyJ..."}`
    *   **Cookie:** `jwt=eyJ...; HttpOnly`

### `POST /public/send-reset-otp`
> Wysyła kod OTP do resetu hasła na podany email.

*   **Authentication:** Publiczny
*   **URL Params:** `?email=jan.kowalski@example.com` (Wymagane)

### `POST /public/reset-password`
> Ustawia nowe hasło przy użyciu kodu OTP.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "email": "jan.kowalski@example.com", // Wymagane
      "otp": "123456",                     // Wymagane
      "newPassword": "newPassword456"      // Wymagane
    }
    ```

### `GET /public/users/{userId}`
> Pobiera publiczne dane profilu użytkownika (sprzedawcy).

*   **Authentication:** Publiczny
*   **URL Path Variable:** `userId` (Wymagane)
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "...",
      "name": "kulmaniak",
      "memberSince": "...",
      "profileImageUrl": "..." // Może być null
    }
    ```

---

## 2. Zarządzanie Własnym Profilem (Zabezpieczone)

### `GET /user/profile`
> Pobiera pełne dane zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "...",
      "name": "Jan Kowalski",
      "email": "jan@example.com",
      "isAccountVerified": true,
      "profileImageUrl": "..."
    }
    ```

### `PATCH /user/profile`
> Aktualizuje dane profilowe użytkownika.

*   **Authentication:** Zabezpieczony
*   **Body:** (Wszystkie pola opcjonalne)
    ```json
    {
      "name": "Janusz Kowalski",
      "profileImageUrl": "https://storage.example.com/profiles/new-avatar.jpg"
    }
    ```

### `GET /user/is-authenticated`
> Szybkie sprawdzenie, czy użytkownik ma ważną sesję (token).

*   **Authentication:** Zabezpieczony
*   **Success Response (200 OK):** `true` lub `false`

### `POST /user/send-otp`
> Wysyła (ponownie) kod weryfikacyjny na email zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony

### `POST /user/verify-otp`
> Weryfikuje konto zalogowanego użytkownika za pomocą kodu OTP.

*   **Authentication:** Zabezpieczony
*   **Body:** `{"otp": "123456"}`

---

## 3. Ogłoszenia - Publiczne

### `GET /public/listings/get/{id}`
> Pobiera szczegółowe dane jednego ogłoszenia.
> **Uwaga:** Zwraca tylko ogłoszenia o statusie `ACTIVE`. Jeśli zalogowany użytkownik jest właścicielem, zwraca ogłoszenie niezależnie od statusu.

*   **Authentication:** Publiczny (opcjonalnie uwierzytelniony)
*   **URL Path Variable:** `id` (Wymagane, publiczne UUID)

### `POST /public/listings/search`
> Wyszukuje i filtruje ogłoszenia.
> **Uwaga:** Zwraca tylko ogłoszenia o statusie `ACTIVE`.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "query": "...",
      "categoryId": 72,
      "sellerUserId": null,
      "attributes": [ ... ],
      "sort": [{ "field": "priceAmount", "dir": "asc" }],
      "page": 0,
      "size": 20
    }
    ```

### `GET /public/listings/random`
> Zwraca stronę z losowymi ogłoszeniami.
> **Uwaga:** Zwraca tylko ogłoszenia o statusie `ACTIVE`.

*   **Authentication:** Publiczny

### `GET /public/listings/user/{userId}`
> Pobiera wszystkie aktywne ogłoszenia danego użytkownika.

*   **Authentication:** Publiczny
*   **URL Path Variable:** `userId` (Wymagane)

---

## 4. Ogłoszenia - Zarządzanie (Zabezpieczone)

### `GET /user/listing/my`
> Pobiera listę ogłoszeń zalogowanego użytkownika.
> **Uwaga:** Zwraca ogłoszenia we wszystkich statusach.

*   **Authentication:** Zabezpieczony
*   **URL Params:**
    *   `?page=0&size=10`
    *   `?status=ACTIVE` (Opcjonalne - filtruje po statusie: DRAFT, WAITING, ACTIVE, REJECTED, SUSPENDED, COMPLETED)

### `GET /user/listing/{publicId}/edit-data`
> Pobiera pełne dane ogłoszenia do wypełnienia formularza edycji.

*   **Authentication:** Zabezpieczony

### `POST /user/listing/create`
> Tworzy nowe ogłoszenie.
> **Uwaga:** Nowe ogłoszenie ma status `DRAFT`.

*   **Authentication:** Zabezpieczony
*   **Body:** (Wymagane: categoryId, title, priceAmount, currency)

### `PUT /user/listing/{publicId}/update`
> Aktualizuje istniejące ogłoszenie.
> **Uwaga:** Jeśli ogłoszenie było `ACTIVE` lub `REJECTED`, jego status zmienia się na `DRAFT`.

*   **Authentication:** Zabezpieczony

### `DELETE /user/listing/{publicId}/delete`
> Usuwa ogłoszenie.

*   **Authentication:** Zabezpieczony

### `POST /user/listing/{publicId}/submit-for-approval`
> Przesyła ogłoszenie do weryfikacji (zmienia status z `DRAFT` na `WAITING`).

*   **Authentication:** Zabezpieczony

### `POST /user/listing/{publicId}/finish`
> Kończy ogłoszenie (zmienia status na `COMPLETED`).

*   **Authentication:** Zabezpieczony

### `GET /user/listing/{publicId}/contact`
> Pobiera numer telefonu kontaktowego dla ogłoszenia.

*   **Authentication:** Zabezpieczony
*   **Success Response:** `{"phoneNumber": "..."}`

---

## 5. Kategorie

### `GET /public/category/all`
> Pobiera pełne drzewo wszystkich kategorii.

*   **Authentication:** Publiczny

### `GET /public/category/attributes`
> Pobiera listę atrybutów dostępnych dla danej kategorii.

*   **Authentication:** Publiczny
*   **URL Params:** `?categoryId=<ID>`

### `GET /public/category/path`
> Pobiera ścieżkę kategorii.

*   **Authentication:** Publiczny
*   **URL Params:** `?id=<ID>`

---

## 6. Interakcje (Ulubione) - Zabezpieczone

### `GET /user/interactions/favorites`
> Pobiera listę ulubionych ogłoszeń.
> **Uwaga:** Zwraca tylko ogłoszenia o statusie `ACTIVE`.

*   **Authentication:** Zabezpieczony

### `POST /user/interactions/favorites/{listingId}`
> Dodaje ogłoszenie do ulubionych.

*   **Authentication:** Zabezpieczony

### `DELETE /user/interactions/favorites/{listingId}`
> Usuwa ogłoszenie z ulubionych.

*   **Authentication:** Zabezpieczony

### `GET /user/interactions/favorites/status`
> Sprawdza, czy dane ogłoszenie jest w ulubionych.

*   **Authentication:** Zabezpieczony
*   **URL Params:** `?entityId=...&entityType=LISTING`

---

## 7. Media (Zabezpieczone)

### `POST /user/upload/media-image`
> Wysyła plik z obrazkiem ogłoszenia i zwraca jego URL.

*   **Authentication:** Zabezpieczony
*   **Body:** `form-data` z kluczem `file`.

### `POST /user/upload/profile-image`
> Wysyła plik z nowym zdjęciem profilowym.
> **Uwaga:** Zwraca tylko URL. Aby zapisać go w profilu, należy użyć `PATCH /user/profile`.

*   **Authentication:** Zabezpieczony
*   **Body:** `form-data` z kluczem `file`.

---

## 8. Komunikator (WebSocket & REST)

### 8.1. Konfiguracja WebSocket (STOMP)

*   **Endpoint:** `http://localhost:8080/ws`
*   **Autentykacja:** Token JWT w nagłówku `Authorization` podczas `CONNECT`.

### 8.2. Odbieranie Wiadomości

*   **Subskrypcja:** `/user/queue/messages`.

### 8.3. Wysyłanie Wiadomości

*   **Destination:** `/app/chat.sendMessage`
*   **Body:** `{"listingId": "...", "recipientId": "...", "content": "..."}`

### 8.4. Zarządzanie Konwersacjami (REST)

### `GET /user/conversations`
> Pobiera listę wszystkich konwersacji użytkownika.

*   **Authentication:** Zabezpieczony

### `GET /user/conversations/{conversationId}/messages`
> Pobiera historię wiadomości dla danej konwersacji.

*   **Authentication:** Zabezpieczony

### `PATCH /user/conversations/{conversationId}/read`
> Oznacza wszystkie wiadomości w konwersacji jako przeczytane.

*   **Authentication:** Zabezpieczony

---

## 9. Ogłoszenia - Administracja (Zabezpieczone - Rola ADMIN)

### `GET /admin/listings/waiting`
> Pobiera listę ogłoszeń oczekujących na zatwierdzenie.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)
*   **URL Params:** `?page=0&size=10`

### `GET /admin/listings/{publicId}`
> Pobiera szczegóły ogłoszenia oczekującego na zatwierdzenie.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)
*   **URL Path Variable:** `publicId` (Wymagane)

### `POST /admin/listings/{publicId}/approve`
> Zatwierdza ogłoszenie (zmienia status z `WAITING` na `ACTIVE`).

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)

### `POST /admin/listings/{publicId}/reject`
> Odrzuca ogłoszenie (zmienia status z `WAITING` na `REJECTED`).

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)
*   **Body:** `{"reason": "..."}`

---

## 10. Kategorie - Administracja (Zabezpieczone - Rola ADMIN)

### `POST /admin/categories`
> Tworzy nową kategorię.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)
*   **Body:** `{"name": "...", "parentId": 123}`

### `PUT /admin/categories/{id}`
> Aktualizuje kategorię.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)

### `DELETE /admin/categories/{id}`
> Usuwa kategorię.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)

### `POST /admin/categories/{id}/attributes`
> Dodaje atrybut do kategorii.

*   **Authentication:** Zabezpieczony (Wymaga roli ADMIN)
*   **Body:** `{"key": "...", "label": "...", "type": "STRING"}`
