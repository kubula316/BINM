# Dokumentacja API - Serwis Ogłoszeniowy

Poniżej znajduje się opis wszystkich dostępnych endpointów.

**Ważna uwaga:** Wszystkie endpointy oznaczone jako **"Zabezpieczone"** wymagają wysłania w nagłówku poprawnego tokenu autoryzacyjnego:

`Authorization: Bearer <TWÓJ_TOKEN_JWT>`

---

## 1. Autentykacja i Użytkownicy

Endpointy do rejestracji, logowania i zarządzania profilem użytkownika.

### `POST /public/register`
> Tworzy nowego użytkownika, ale nie aktywuje konta. Wysyła email z kodem OTP do weryfikacji.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "name": "Jan Kowalski",
      "email": "jan.kowalski@example.com",
      "password": "password123"
    }
    ```
*   **Success Response (201 Created):**
    ```json
    {
      "userId": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
      "name": "Jan Kowalski",
      "email": "jan.kowalski@example.com",
      "isAccountVerified": false
    }
    ```

### `POST /public/login`
> Loguje użytkownika i zwraca token JWT w ciasteczku HTTPOnly.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "email": "jan.kowalski@example.com",
      "password": "password123"
    }
    ```
*   **Success Response (200 OK):**
    *   Ciasteczko `jwt` jest ustawiane w przeglądarce.
    *   Body zawiera podstawowe informacje:
    ```json
    {
        "email": "jan.kowalski@example.com",
        "token": "eyJhbGciOiJIUzI1NiJ9..."
    }
    ```

### `POST /user/verify-otp`
> Weryfikuje konto użytkownika za pomocą kodu OTP otrzymanego w mailu.

*   **Authentication:** Zabezpieczony (wymaga tokenu z logowania)
*   **Body:**
    ```json
    {
      "otp": "123456"
    }
    ```
*   **Success Response:** `200 OK` (puste body)

### `GET /user/profile`
> Pobiera dane zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
      "name": "Jan Kowalski",
      "email": "jan.kowalski@example.com",
      "isAccountVerified": true
    }
    ```

---

## 2. Ogłoszenia - Operacje Zabezpieczone

CRUD dla ogłoszeń zalogowanego użytkownika.

### `POST /user/listing/create`
> Tworzy nowe ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **Body:**
    ```json
    {
      "categoryId": 72,
      "title": "Audi A4 B8 2.0 TDI S-Line",
      "description": "Sprzedam prywatne auto...",
      "priceAmount": 32500.00,
      "negotiable": true,
      "locationCity": "Warszawa",
      "locationRegion": "Mazowieckie",
      "mediaUrls": [
        "https://twoj-storage.blob.core.windows.net/media/audi-1.jpg"
      ],
      "attributes": [
        { "key": "condition", "value": "used" },
        { "key": "brand", "value": "audi" },
        { "key": "year", "value": "2010" }
      ]
    }
    ```
*   **Success Response (200 OK):** Pełne `ListingDto` nowo stworzonego ogłoszenia.

### `PUT /user/listing/update`
> Aktualizuje istniejące ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **URL Params:** `?id=<ID_OGŁOSZENIA>` (np. `?id=76`)
*   **Body:** Zawiera tylko te pola, które mają zostać zmienione.
    ```json
    {
      "title": "OKAZJA! Audi A4 B8 2.0 TDI",
      "priceAmount": 31500.00,
      "attributes": [
        { "key": "year", "value": "2011" },
        { "key": "mileage", "value": "220000" }
      ]
    }
    ```
*   **Success Response (200 OK):** Pełne `ListingDto` zaktualizowanego ogłoszenia.

### `DELETE /user/listing/delete`
> Usuwa ogłoszenie.

*   **Authentication:** Zabezpieczony
*   **URL Params:** `?id=<ID_OGŁOSZENIA>` (np. `?id=76`)
*   **Success Response:** `204 No Content` (puste body)

### `GET /user/listing/my`
> Pobiera listę ogłoszeń zalogowanego użytkownika.

*   **Authentication:** Zabezpieczony
*   **URL Params:** `?page=0&size=10` (opcjonalne)
*   **Success Response (200 OK):** Stronicowana lista "okładek" ogłoszeń (`ListingCoverDto`).
    ```json
    {
      "content": [
        {
          "id": 76,
          "publicId": "d888cad2-9fc6-4629-ba86-4c106b5382b1",
          "title": "Audi A4 B8 2.0 TDI S-Line",
          "seller": { "id": "89bc977b-c63f-448d-896c-8174d75ab708", "name": "kulmaniak" },
          "priceAmount": 32500.00,
          "negotiable": true,
          "coverImageUrl": "https://twoj-storage.blob.core.windows.net/media/audi-a4-1.jpg"
        }
      ],
      "pageable": { ... },
      "totalPages": 1,
      "totalElements": 1,
      "last": true,
      "size": 10,
      "number": 0,
      "sort": { ... },
      "numberOfElements": 1,
      "first": true,
      "empty": false
    }
    ```

---

## 3. Ogłoszenia - Endpointy Publiczne

Endpointy do przeglądania i wyszukiwania ogłoszeń dostępne dla wszystkich.

### `GET /public/listings/get`
> Pobiera szczegółowe dane jednego ogłoszenia.

*   **Authentication:** Publiczny
*   **URL Params:** `?id=<ID_OGŁOSZENIA>` (np. `?id=76`)
*   **Success Response (200 OK):** Pełne `ListingDto` z wszystkimi atrybutami, mediami i danymi sprzedawcy.

### `POST /public/listings/search`
> Wyszukuje i filtruje ogłoszenia. Może służyć jako główny endpoint do listowania.

*   **Authentication:** Publiczny
*   **Body:**
    ```json
    {
      "categoryId": 72,
      "sellerUserId": null,
      "attributes": [
        { "key": "brand", "type": "ENUM", "op": "eq", "value": "audi" },
        { "key": "year",  "type": "NUMBER", "op": "between", "from": "2008", "to": "2015" }
      ],
      "sort": [{ "field": "priceAmount", "dir": "asc" }],
      "page": 0,
      "size": 20
    }
    ```
*   **Success Response (200 OK):** Stronicowana lista "okładek" ogłoszeń (`ListingCoverDto`).

### `GET /public/listings/random`
> Zwraca stronę z losowymi ogłoszeniami. Idealne na stronę główną.

*   **Authentication:** Publiczny
*   **URL Params:** `?page=0&size=10` (opcjonalne)
*   **Success Response (200 OK):** Stronicowana lista "okładek" ogłoszeń (`ListingCoverDto`).

---

## 4. Kategorie i Atrybuty

Endpointy pomocnicze do budowania interfejsu wyszukiwania i dodawania ogłoszeń.

### `GET /public/category/all`
> Pobiera pełne drzewo wszystkich kategorii.

*   **Authentication:** Publiczny
*   **Success Response (200 OK):** Lista kategorii z zagnieżdżonymi dziećmi.

### `GET /public/category/attributes`
> Pobiera listę atrybutów (wraz z opcjami) dostępnych dla danej kategorii.

*   **Authentication:** Publiczny
*   **URL Params:** `?categoryId=<ID_KATEGORII>` (np. `?categoryId=72`)
*   **Success Response (200 OK):** Lista definicji atrybutów.
    ```json
    [
      {
        "id": 1,
        "categoryId": 72,
        "key": "brand",
        "label": "Marka",
        "type": "ENUM",
        "required": true,
        "unit": null,
        "sortOrder": 10,
        "options": [
          { "id": 1, "value": "audi", "label": "Audi", "sortOrder": 0 },
          { "id": 2, "value": "bmw", "label": "BMW", "sortOrder": 1 }
        ]
      },
      {
        "id": 2,
        "categoryId": 72,
        "key": "year",
        "label": "Rok produkcji",
        "type": "NUMBER",
        "required": true,
        "unit": null,
        "sortOrder": 20,
        "options": []
      }
    ]
    ```
