# Dokumentacja API - Serwis Ogłoszeniowy (v2.2)

Poniżej znajduje się zaktualizowany opis wszystkich dostępnych endpointów.

**Ważna uwaga:** Wszystkie endpointy, których ścieżka **nie** zaczyna się od `/public/` (oraz endpoint WebSocket `/ws`), są **zabezpieczone** i wymagają wysłania w nagłówku poprawnego tokenu autoryzacyjnego: `Authorization: Bearer <TWÓJ_TOKEN_JWT>`.

---

## 1. Autentykacja i Profile Publiczne

### `POST /public/register`
> Tworzy nowego użytkownika i wysyła email z kodem OTP do weryfikacji.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Formularz rejestracji.
*   **Body:**
    ```json
    {
      "name": "Jan Kowalski",      // Wymagane
      "email": "jan.kowalski@example.com", // Wymagane, unikalny
      "password": "password123"   // Wymagane, min. 6 znaków
    }
    ```

### `POST /public/login`
> Loguje użytkownika i zwraca token JWT w ciasteczku HTTPOnly.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Formularz logowania.
*   **Body:**
    ```json
    {
      "email": "jan.kowalski@example.com", // Wymagane
      "password": "password123"          // Wymagane
    }
    ```

### `POST /public/send-reset-otp`
> Wysyła kod OTP do resetu hasła na podany email.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Formularz "Zapomniałem hasła".
*   **URL Params:** `?email=jan.kowalski@example.com` (Wymagane)

### `POST /public/reset-password`
> Ustawia nowe hasło przy użyciu kodu OTP.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Formularz do ustawiania nowego hasła po otrzymaniu kodu.
*   **Body:**
    ```json
    {
      "email": "jan.kowalski@example.com", // Wymagane
      "otp": "123456",                     // Wymagane
      "newPassword": "newPassword456"      // Wymagane min 6
    }
    ```

### `GET /public/users/{userId}`
> Pobiera publiczne dane profilu użytkownika (sprzedawcy).

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Strona profilu sprzedawcy, wyświetlanie autora ogłoszenia.
*   **URL Path Variable:** `userId` (Wymagane, np. `/public/users/89bc977b-c63f-448d-896c-8174d75ab708`)
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "89bc977b-c63f-448d-896c-8174d75ab708",
      "name": "kulmaniak",
      "memberSince": "2025-11-30T16:03:27.35657Z"
    }
    ```

---

## 2. Zarządzanie Własnym Profilem (Zabezpieczone)

### `GET /user/profile`
> Pobiera pełne dane zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Panel 'Moje Konto', wyświetlanie danych w formularzach.

### `GET /user/is-authenticated`
> Szybkie sprawdzenie, czy użytkownik ma ważną sesję (token).

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Kluczowe przy starcie aplikacji (wczytaniu strony), aby sprawdzić, czy pokazać interfejs dla zalogowanego czy anonimowego użytkownika.
*   **Success Response (200 OK):** `true` lub `false`

### `POST /user/send-otp`
> Wysyła (ponownie) kod weryfikacyjny na email zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Przycisk "Wyślij kod ponownie" na stronie weryfikacji konta.

### `POST /user/verify-otp`
> Weryfikuje konto zalogowanego użytkownika za pomocą kodu OTP.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Formularz do wpisania kodu OTP.
*   **Body:**
    ```json
    {
      "otp": "123456" // Wymagane
    }
    ```

---

## 3. Ogłoszenia - Publiczne

### `GET /public/listings/get/{id}`
> Pobiera szczegółowe dane jednego ogłoszenia.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Strona ze szczegółami ogłoszenia.
*   **URL Path Variable:** `id` (Wymagane, publiczne UUID ogłoszenia, np. `/public/listings/get/d888cad2-9fc6-4629-ba86-4c106b5382b1`)

### `POST /public/listings/search`
> Wyszukuje i filtruje ogłoszenia.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Główny silnik wyszukiwarki i listowania ogłoszeń.
*   **Body:**
    ```json
    {
      "query": "gitara elektryczna", // Opcjonalne, wyszukuje w tytule i opisie
      "categoryId": 72,       // Opcjonalne
      "sellerUserId": null,   // Opcjonalne
      "attributes": [         // Opcjonalne
        { "key": "brand", "type": "ENUM", "op": "eq", "value": "fender" },
        { "key": "year",  "type": "NUMBER", "op": "between", "from": "2008", "to": "2015" }
      ],
      "sort": [{ "field": "priceAmount", "dir": "asc" }], // Opcjonalne
      "page": 0,              // Opcjonalne
      "size": 20              // Opcjonalne
    }
    ```

### `GET /public/listings/random`
> Zwraca stronę z losowymi ogłoszeniami.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Strona główna, sekcja "Proponowane".
*   **URL Params:** `?page=0&size=10` (Opcjonalne)

### `GET /public/listings/user/{userId}`
> Pobiera wszystkie aktywne ogłoszenia danego użytkownika.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Strona profilu sprzedawcy, do wyświetlenia listy jego przedmiotów.
*   **URL Path Variable:** `userId` (Wymagane)
*   **URL Params:** `?page=0&size=10` (Opcjonalne)

---

## 4. Ogłoszenia - Zarządzanie (Zabezpieczone)

### `GET /user/listing/my`
> Pobiera listę ogłoszeń zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Panel 'Moje Ogłoszenia'.
*   **URL Params:** `?page=0&size=10` (Opcjonalne)

### `GET /user/listing/{publicId}/edit-data`
> Pobiera pełne dane ogłoszenia do wypełnienia formularza edycji.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Wywoływany po kliknięciu 'Edytuj', aby pobrać dane i wypełnić nimi formularz.
*   **URL Path Variable:** `publicId` (Wymagane)

### `POST /user/listing/create`
> Tworzy nowe ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Formularz dodawania ogłoszenia.
*   **Body:**
    ```json
    {
      "categoryId": 72,
      "title": "Nowe Audi A4",
      "description": "Super opis...",
      "priceAmount": 50000.00,
      "negotiable": true,
      "locationCity": "Gdańsk",
      "locationRegion": "Pomorskie",
      "contactPhoneNumber": "123456789", // Opcjonalne
      "mediaUrls": [
        "https://twoj-storage.blob.core.windows.net/media/audi-a4-1.jpg"
      ],
      "attributes": [
        { "key": "condition", "value": "new" }
      ]
    }
    ```

### `PUT /user/listing/{publicId}/update`
> Aktualizuje istniejące ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Formularz edycji ogłoszenia.
*   **URL Path Variable:** `publicId` (Wymagane)
*   **Body:** Zawiera tylko te pola, które mają zostać zmienione.
    ```json
    {
      "title": "Zaktualizowany tytuł",
      "contactPhoneNumber": "987654321"
    }
    ```

### `DELETE /user/listing/{publicId}/delete`
> Usuwa ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Przycisk 'Usuń' w panelu 'Moje Ogłoszenia'.
*   **URL Path Variable:** `publicId` (Wymagane)

### `GET /user/listing/{publicId}/contact`
> Pobiera numer telefonu kontaktowego dla ogłoszenia.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Wywoływany po kliknięciu przycisku "Pokaż numer telefonu".
*   **URL Path Variable:** `publicId` (Wymagane)
*   **Success Response (200 OK):**
    ```json
    {
      "phoneNumber": "123456789"
    }
    ```

---

## 5. Kategorie

### `GET /public/category/all`
> Pobiera pełne drzewo wszystkich kategorii.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Budowa menu nawigacyjnego.

### `GET /public/category/attributes`
> Pobiera listę atrybutów dostępnych dla danej kategorii.

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Dynamiczne budowanie filtrów wyszukiwania lub pól w formularzu dodawania ogłoszenia.
*   **URL Params:** `?categoryId=<ID_KATEGORII>` (Wymagane)

### `GET /public/category/path`
> Pobiera ścieżkę kategorii (np. Elektronika > Telefony).

*   **Authentication:** Publiczny
*   **Zastosowanie na Froncie:** Wyświetlanie "okruszków" (breadcrumbs) na stronie kategorii lub ogłoszenia.
*   **URL Params:** `?id=<ID_KATEGORII>` (Wymagane)

---

## 6. Interakcje (Ulubione) - Zabezpieczone

### `GET /user/interactions/favorites`
> Pobiera listę ulubionych ogłoszeń zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Strona "Moje ulubione".
*   **URL Params:** `?page=0&size=10` (Opcjonalne)

### `POST /user/interactions/favorites`
> Dodaje ogłoszenie do ulubionych.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Przycisk "Dodaj do ulubionych" (serduszko) na karcie ogłoszenia.
*   **Body:**
    ```json
    {
      "entityId": "d888cad2-9fc6-4629-ba86-4c106b5382b1",
      "entityType": "LISTING"
    }
    ```

### `DELETE /user/interactions/favorites`
> Usuwa ogłoszenie z ulubionych.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Przycisk "Usuń z ulubionych" (wypełnione serduszko) na karcie ogłoszenia.
*   **Body:**
    ```json
    {
      "entityId": "d888cad2-9fc6-4629-ba86-4c106b5382b1",
      "entityType": "LISTING"
    }
    ```

### `GET /user/interactions/favorites/status`
> Sprawdza, czy dane ogłoszenie jest w ulubionych.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Do ustalenia początkowego stanu przycisku "serduszka" przy renderowaniu ogłoszenia.
*   **URL Params:** `?entityId=<UUID_OGŁOSZENIA>&entityType=LISTING` (Wymagane)
*   **Success Response (200 OK):**
    ```json
    {
      "isFavorite": true
    }
    ```

---

## 7. Media (Zabezpieczone)

### `POST /user/upload/media-image`
> Wysyła plik z obrazkiem ogłoszenia i zwraca jego URL.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Używane w formularzu dodawania/edycji ogłoszenia. Użytkownik wybiera plik, frontend go wysyła, dostaje URL i dodaje go do listy `mediaUrls`.
*   **Body:** `form-data` z kluczem `file`.

### `POST /user/upload/profile-image`
> Wysyła plik z nowym zdjęciem profilowym.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Używane w panelu edycji profilu użytkownika.
*   **Body:** `form-data` z kluczem `file`.

---

## 8. Komunikator (WebSocket & REST)

### 8.1. Konfiguracja WebSocket (STOMP)

*   **Endpoint połączenia:** `http://localhost:8080/ws`
*   **Biblioteki:** SockJS + StompJS
*   **Autentykacja:** Token JWT musi być przekazany w nagłówku `Authorization` podczas nawiązywania połączenia STOMP (ramka `CONNECT`).
    ```javascript
    stompClient.connect({'Authorization': 'Bearer ' + token}, ...)
    ```

### 8.2. Odbieranie Wiadomości

*   **Subskrypcja:** Klient powinien zasubskrybować kanał: `/user/queue/messages`.
*   **Działanie:** Na ten kanał będą przychodzić **wszystkie** prywatne wiadomości skierowane do zalogowanego użytkownika, niezależnie od tego, z której konwersacji pochodzą.

### 8.3. Wysyłanie Wiadomości

*   **Destination:** `/app/chat.sendMessage`
*   **Body (JSON):**
    ```json
    {
      "listingId": "d888cad2-9fc6-4629-ba86-4c106b5382b1", // UUID ogłoszenia
      "recipientId": "89bc977b-c63f-448d-896c-8174d75ab708", // User ID odbiorcy
      "content": "Cześć, czy to ogłoszenie jest aktualne?"
    }
    ```

### 8.4. Zarządzanie Konwersacjami (REST)

### `GET /user/conversations`
> Pobiera listę wszystkich konwersacji użytkownika.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Panel "Wiadomości" (lista czatów po lewej stronie).
*   **Success Response (200 OK):**
    ```json
    [
      {
        "id": 123,
        "listing": { ... }, // Skrócone dane ogłoszenia (CoverDto)
        "otherParticipantName": "Janusz",
        "lastMessageContent": "Tak, aktualne",
        "lastMessageTimestamp": "2025-12-18T12:00:00Z"
      }
    ]
    ```

### `GET /user/conversations/{conversationId}/messages`
> Pobiera historię wiadomości dla danej konwersacji.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Po kliknięciu w konkretną rozmowę, aby załadować historię czatu.
*   **URL Path Variable:** `conversationId` (Wymagane)
*   **URL Params:** `?page=0&size=20` (Opcjonalne)

### `PATCH /user/conversations/{conversationId}/read`
> Oznacza wszystkie wiadomości w konwersacji jako przeczytane.

*   **Authentication:** Zabezpieczony
*   **Zastosowanie na Froncie:** Wywoływany automatycznie po wejściu użytkownika w okno czatu.
*   **URL Path Variable:** `conversationId` (Wymagane)
*   **Success Response:** `204 No Content`
