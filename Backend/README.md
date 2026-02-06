# BINM Listing Service - Backend API

BINM Listing Service is a comprehensive backend solution for an e-commerce platform, designed and implemented as part of my engineering thesis. It features a modular monolith architecture, real-time communication via WebSockets, and integration with cloud services like Azure Blob Storage.
<img width="1619" height="868" alt="image" src="https://github.com/user-attachments/assets/4f835903-9819-44f5-8c57-659b778cc742" />

<img width="1834" height="984" alt="z2" src="https://github.com/user-attachments/assets/2c3044ed-2beb-4a82-97c4-d0b14ddecdf7" />



---

## Database Schema

The application uses a relational PostgreSQL database designed with a Code-First approach. Below is a brief description of the key entities:

*   **User:** Stores user account information, including credentials, profile data, and roles (USER/ADMIN).
*   **Listing:** The core entity representing an item for sale. Contains details like title, price, description, location, and status (DRAFT, ACTIVE, etc.).
*   **Category:** Represents the hierarchical structure of product categories (tree structure with parent-child relationships).
*   **AttributeDefinition:** Defines dynamic attributes (e.g., "Color", "Size", "Mileage") available for a specific category.
*   **AttributeOption:** Stores possible values for attributes of type ENUM (e.g., "Red", "Blue" for the "Color" attribute).
*   **ListingAttribute:** Connects a specific listing with its attribute values (EAV model implementation).
*   **ListingMedia:** Stores URLs to images associated with a listing, hosted on Azure Blob Storage.
*   **Favorite:** Represents a user's saved/favorite listings.
*   **Conversation:** Represents a chat thread between a buyer and a seller regarding a specific listing.
*   **Message:** Stores individual messages within a conversation, including content, timestamp, and read status.
<img width="7562" height="5672" alt="Baza danych" src="https://github.com/user-attachments/assets/da3bf6b1-3e00-4e06-bbac-4800d6ba9db9" />

---

# API Documentation (v3.4)

Below is the updated description of all available endpoints.

**Important Note:** All endpoints whose path does **not** start with `/public/` (and the WebSocket endpoint `/ws`) are **secured** and require a valid authorization token to be sent in the header: `Authorization: Bearer <YOUR_JWT_TOKEN>`.

**Administrator Account (Default):**
*   Email: `admin@binm.com`
*   Password: `admin123`

---

## Error Handling

In case of an error, the API returns a response in JSON format with an appropriate HTTP code (4xx or 500).

**Error Response Format (`ErrorResponse`):**
```json
{
  "code": "ERROR_CODE",       // Unique error code (e.g., USER_ALREADY_EXISTS, VALIDATION_ERROR)
  "message": "Error description",    // Readable description for the developer
  "status": 400,              // HTTP Code
  "timestamp": "2023-10-27T10:00:00Z",
  "details": "..."            // Optional details (e.g., list of fields with validation errors)
}
```

---

## 1. Authentication and Public Profiles

### `POST /public/register`
> Creates a new user and sends an email with an OTP code for verification.

*   **Authentication:** Public
*   **Body:**
    ```json
    {
      "name": "John Doe",      // Required
      "email": "john.doe@example.com", // Required, unique
      "password": "password123"   // Required, min. 8 characters
    }
    ```

### `POST /public/login`
> Logs in the user.

*   **Authentication:** Public
*   **Body:**
    ```json
    {
      "email": "john.doe@example.com", // Required
      "password": "password123"          // Required
    }
    ```
*   **Success Response (200 OK):**
    *   **Body:** `{"email": "...", "token": "eyJ..."}`
    *   **Cookie:** `jwt=eyJ...; HttpOnly`

### `POST /public/send-reset-otp`
> Sends an OTP code for password reset to the provided email.

*   **Authentication:** Public
*   **URL Params:** `?email=john.doe@example.com` (Required)

### `POST /public/reset-password`
> Sets a new password using the OTP code.

*   **Authentication:** Public
*   **Body:**
    ```json
    {
      "email": "john.doe@example.com", // Required
      "otp": "123456",                     // Required
      "newPassword": "newPassword456"      // Required
    }
    ```

### `GET /public/users/{userId}`
> Retrieves public profile data of a user (seller).

*   **Authentication:** Public
*   **URL Path Variable:** `userId` (Required)
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "...",
      "name": "coolseller",
      "memberSince": "...",
      "profileImageUrl": "..." // Can be null
    }
    ```

---

## 2. Managing Own Profile (Secured)

### `GET /user/profile`
> Retrieves full data of the logged-in user.

*   **Authentication:** Secured
*   **Success Response (200 OK):**
    ```json
    {
      "userId": "...",
      "name": "John Doe",
      "email": "john@example.com",
      "isAccountVerified": true,
      "profileImageUrl": "..."
    }
    ```

### `PATCH /user/profile`
> Updates user profile data.

*   **Authentication:** Secured
*   **Body:** (All fields optional)
    ```json
    {
      "name": "Johnny Doe",
      "profileImageUrl": "https://storage.example.com/profiles/new-avatar.jpg"
    }
    ```

### `GET /user/is-authenticated`
> Quick check if the user has a valid session (token).

*   **Authentication:** Secured
*   **Success Response (200 OK):** `true` or `false`

### `POST /user/send-otp`
> Sends (resends) a verification code to the logged-in user's email.

*   **Authentication:** Secured

### `POST /user/verify-otp`
> Verifies the logged-in user's account using the OTP code.

*   **Authentication:** Secured
*   **Body:** `{"otp": "123456"}`

---

## 3. Listings - Public

### `GET /public/listings/get/{id}`
> Retrieves detailed data of a single listing.
> **Note:** Returns only listings with `ACTIVE` status. If the logged-in user is the owner, returns the listing regardless of status.

*   **Authentication:** Public (optionally authenticated)
*   **URL Path Variable:** `id` (Required, public UUID)

### `POST /public/listings/search`
> Searches and filters listings.
> **Note:** Returns only listings with `ACTIVE` status.

*   **Authentication:** Public
*   **Body:**
    ```json
    {
      "query": "...",
      "categoryId": 72,
      "sellerUserId": null,
      "attributes": [ ... ],
      "latitude": 52.2297,   // Optional (requires longitude and radiusKm)
      "longitude": 21.0122,  // Optional
      "radiusKm": 10,        // Optional (radius in km)
      "sort": [{ "field": "priceAmount", "dir": "asc" }],
      "page": 0,
      "size": 20
    }
    ```

### `GET /public/listings/random`
> Returns a page with random listings.
> **Note:** Returns only listings with `ACTIVE` status.

*   **Authentication:** Public

### `GET /public/listings/user/{userId}`
> Retrieves all active listings of a given user.

*   **Authentication:** Public
*   **URL Path Variable:** `userId` (Required)

---

## 4. Listings - Management (Secured)

### `GET /user/listing/my`
> Retrieves a list of the logged-in user's listings.
> **Note:** Returns listings in all statuses.

*   **Authentication:** Secured
*   **URL Params:**
    *   `?page=0&size=10`
    *   `?status=ACTIVE` (Optional - filters by status: DRAFT, WAITING, ACTIVE, REJECTED, SUSPENDED, COMPLETED)

### `GET /user/listing/{publicId}/edit-data`
> Retrieves full listing data to populate the edit form.

*   **Authentication:** Secured

### `POST /user/listing/create`
> Creates a new listing.
> **Note:** The new listing has `DRAFT` status.

*   **Authentication:** Secured
*   **Body:** (Required: categoryId, title, priceAmount, currency)

### `PUT /user/listing/{publicId}/update`
> Updates an existing listing.
> **Note:** If the listing was `ACTIVE` or `REJECTED`, its status changes to `DRAFT`.

*   **Authentication:** Secured

### `DELETE /user/listing/{publicId}/delete`
> Deletes a listing.

*   **Authentication:** Secured

### `POST /user/listing/{publicId}/submit-for-approval`
> Submits the listing for verification (changes status from `DRAFT` to `WAITING`).

*   **Authentication:** Secured

### `POST /user/listing/{publicId}/finish`
> Finishes the listing (changes status to `COMPLETED`).

*   **Authentication:** Secured

### `GET /user/listing/{publicId}/contact`
> Retrieves the contact phone number for the listing.

*   **Authentication:** Secured
*   **Success Response:** `{"phoneNumber": "..."}`

---

## 5. Categories

### `GET /public/category/all`
> Retrieves the full tree of all categories.

*   **Authentication:** Public

### `GET /public/category/attributes`
> Retrieves a list of attributes available for a given category.

*   **Authentication:** Public
*   **URL Params:** `?categoryId=<ID>`

### `GET /public/category/path`
> Retrieves the category path.

*   **Authentication:** Public
*   **URL Params:** `?id=<ID>`

---

## 6. Interactions (Favorites) - Secured

### `GET /user/interactions/favorites`
> Retrieves a list of favorite listings.
> **Note:** Returns only listings with `ACTIVE` status.

*   **Authentication:** Secured

### `POST /user/interactions/favorites`
> Adds a listing to favorites.

*   **Authentication:** Secured
*   **Body:**
    ```json
    {
      "entityId": "...",
      "entityType": "LISTING"
    }
    ```

### `DELETE /user/interactions/favorites`
> Removes a listing from favorites.

*   **Authentication:** Secured
*   **Body:**
    ```json
    {
      "entityId": "...",
      "entityType": "LISTING"
    }
    ```

### `GET /user/interactions/favorites/status`
> Checks if a given listing is in favorites.

*   **Authentication:** Secured
*   **URL Params:** `?entityId=...&entityType=LISTING`

---

## 7. Media (Secured)

### `POST /user/upload/media-image`
> Uploads a listing image file and returns its URL.

*   **Authentication:** Secured
*   **Body:** `form-data` with key `file`.

### `POST /user/upload/profile-image`
> Uploads a new profile image file.
> **Note:** Returns only the URL. To save it in the profile, use `PATCH /user/profile`.

*   **Authentication:** Secured
*   **Body:** `form-data` with key `file`.

---

## 8. Messenger (WebSocket & REST)

### 8.1. WebSocket Configuration (STOMP)

*   **Endpoint:** `http://localhost:8080/ws`
*   **Authentication:** JWT Token in `Authorization` header during `CONNECT`.

### 8.2. Receiving Messages

*   **Subscription:** `/user/queue/messages`.

### 8.3. Sending Messages

*   **Destination:** `/app/chat.sendMessage`
*   **Body:** `{"listingId": "...", "recipientId": "...", "content": "..."}`

### 8.4. Conversation Management (REST)

### `GET /user/conversations`
> Retrieves a list of all user conversations.

*   **Authentication:** Secured

### `GET /user/conversations/{conversationId}/messages`
> Retrieves message history for a given conversation.

*   **Authentication:** Secured

### `PATCH /user/conversations/{conversationId}/read`
> Marks all messages in a conversation as read.

*   **Authentication:** Secured

---

## 9. Listings - Administration (Secured - ADMIN Role)

### `GET /admin/listings/waiting`
> Retrieves a list of listings waiting for approval.

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Params:** `?page=0&size=10`

### `GET /admin/listings/waiting/{publicId}`
> Retrieves details of a listing waiting for approval.

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Path Variable:** `publicId` (Required)

### `POST /admin/listings/{publicId}/approve`
> Approves a listing (changes status from `WAITING` to `ACTIVE`).

*   **Authentication:** Secured (Requires ADMIN role)

### `POST /admin/listings/{publicId}/reject`
> Rejects a listing (changes status from `WAITING` to `REJECTED`).

*   **Authentication:** Secured (Requires ADMIN role)
*   **Body:** `{"reason": "..."}`

---

## 10. Categories - Administration (Secured - ADMIN Role)

### `POST /admin/categories`
> Creates a new category.

*   **Authentication:** Secured (Requires ADMIN role)
*   **Body:** `{"name": "...", "parentId": 123}`

### `PUT /admin/categories/{id}`
> Updates a category.

*   **Authentication:** Secured (Requires ADMIN role)

### `DELETE /admin/categories/{id}`
> Deletes a category.

*   **Authentication:** Secured (Requires ADMIN role)

### `POST /admin/categories/{id}/attributes`
> Adds an attribute to a category.

*   **Authentication:** Secured (Requires ADMIN role)
*   **Body:** `{"key": "...", "label": "...", "type": "STRING"}`

---

## 11. Attributes - Administration (Secured - ADMIN Role)

### `POST /admin/attributes`
> Creates a new attribute definition (not directly assigned to a category in this endpoint or creating a global definition).

*   **Authentication:** Secured (Requires ADMIN role)
*   **Body:** (Dependent on AttributeDefinition DTO implementation)

### `PUT /admin/attributes/{id}`
> Updates an existing attribute definition.

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Path Variable:** `id` (Attribute ID)

### `POST /admin/attributes/{id}/options`
> Adds a new option to an ENUM type attribute (selection list).

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Path Variable:** `id` (Attribute ID)
*   **Body:** `{"value": "...", "label": "...", "sortOrder": 1}`

### `PUT /admin/attributes/options/{optionId}`
> Updates an existing attribute option.

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Path Variable:** `optionId` (Option ID)

### `DELETE /admin/attributes/options/{optionId}`
> Deletes an attribute option.

*   **Authentication:** Secured (Requires ADMIN role)
*   **URL Path Variable:** `optionId` (Option ID)
