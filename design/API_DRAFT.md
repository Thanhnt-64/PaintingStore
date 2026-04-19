# 🎨 Painting Store - API Draft

## Base URLs
```
Admin API:    http://localhost:8080/api/admin
Public API:   http://localhost:8080/api/public
```

---

## 📌 PUBLIC API (Web Gallery - No Auth)

### 🏠 **Landing Page**
```http
GET /api/public/artworks/featured
Description: Lấy tác phẩm nổi bật cho landing page
Response: {
  "data": [
    {
      "artwork_id": 1,
      "title": "Starry Night",
      "artist": { "artist_id": 1, "name": "Vincent Van Gogh" },
      "price": 5000000,
      "primary_image": "url/to/image.jpg",
      "view_count": 1250,
      "slug": "starry-night"
    }
  ],
  "total": 10
}
```

```http
GET /api/public/events
Description: Lấy danh sách sự kiện sắp tới
Response: {
  "data": [
    {
      "event_id": 1,
      "title": "Triển lãm Impressionism",
      "start_date": "2026-05-01T10:00:00",
      "end_date": "2026-05-31T18:00:00",
      "image_url": "url/to/event.jpg"
    }
  ],
  "total": 5
}
```

---

### 🎨 **Browse Artworks**
```http
GET /api/public/artworks
Query Params:
  - page: int (default: 1)
  - limit: int (default: 12)
  - category_id: int (optional)
  - artist_id: int (optional)
  - sort: string (popular|newest|price_asc|price_desc)

Response: {
  "data": [
    {
      "artwork_id": 1,
      "title": "Starry Night",
      "slug": "starry-night",
      "price": 5000000,
      "primary_image": "url/to/image.jpg",
      "view_count": 1250,
      "year_created": 1889,
      "artist": {
        "artist_id": 1,
        "name": "Vincent Van Gogh",
        "slug": "vincent-van-gogh"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 12,
    "total": 245,
    "total_pages": 21
  }
}
```

```http
GET /api/public/artworks/{artwork_id}
Description: Chi tiết tác phẩm + gallery

Response: {
  "artwork_id": 1,
  "title": "Starry Night",
  "slug": "starry-night",
  "description": "A swirling night sky...",
  "price": 5000000,
  "year_created": 1889,
  "type": "oil_painting",
  "view_count": 1250,
  "is_featured": true,
  "artist": {
    "artist_id": 1,
    "name": "Vincent Van Gogh",
    "slug": "vincent-van-gogh",
    "birth_year": 1853,
    "death_year": 1890,
    "nationality": "Dutch",
    "biography": "Dutch Post-Impressionist painter...",
    "image_url": "url/to/artist.jpg"
  },
  "images": [
    {
      "image_id": 1,
      "image_url": "url/to/main.jpg",
      "is_primary": true
    },
    {
      "image_id": 2,
      "image_url": "url/to/detail.jpg",
      "is_primary": false
    }
  ],
  "categories": [
    {
      "category_id": 1,
      "name": "Post-Impressionism",
      "slug": "post-impressionism"
    }
  ]
}
```

---

### 👤 **Browse Artists**
```http
GET /api/public/artists
Query Params:
  - page: int (default: 1)
  - limit: int (default: 20)
  - sort: string (name|popular)

Response: {
  "data": [
    {
      "artist_id": 1,
      "name": "Vincent Van Gogh",
      "slug": "vincent-van-gogh",
      "nationality": "Dutch",
      "birth_year": 1853,
      "death_year": 1890,
      "image_url": "url/to/artist.jpg",
      "artwork_count": 45
    }
  ],
  "pagination": { "page": 1, "limit": 20, "total": 150 }
}
```

```http
GET /api/public/artists/{artist_id}
Description: Chi tiết nghệ sĩ + danh sách tác phẩm

Response: {
  "artist_id": 1,
  "name": "Vincent Van Gogh",
  "slug": "vincent-van-gogh",
  "biography": "Dutch Post-Impressionist painter...",
  "birth_year": 1853,
  "death_year": 1890,
  "nationality": "Dutch",
  "image_url": "url/to/artist.jpg",
  "created_at": "2026-01-15T10:00:00",
  "artworks": [
    {
      "artwork_id": 1,
      "title": "Starry Night",
      "slug": "starry-night",
      "primary_image": "url/to/image.jpg",
      "price": 5000000
    }
  ]
}
```

---

### 📂 **Browse Categories**
```http
GET /api/public/categories
Description: Lấy danh mục (hỗ trợ nested)

Response: {
  "data": [
    {
      "category_id": 1,
      "name": "Painting",
      "slug": "painting",
      "parent_id": null,
      "children": [
        {
          "category_id": 2,
          "name": "Oil Painting",
          "slug": "oil-painting",
          "parent_id": 1
        }
      ]
    }
  ]
}
```

```http
GET /api/public/categories/{category_id}
Description: Chi tiết danh mục + tác phẩm trong danh mục

Query Params:
  - page: int (default: 1)
  - limit: int (default: 12)

Response: {
  "category_id": 1,
  "name": "Painting",
  "slug": "painting",
  "parent_id": null,
  "artworks": [
    {
      "artwork_id": 1,
      "title": "Starry Night",
      "primary_image": "url/to/image.jpg",
      "price": 5000000
    }
  ],
  "pagination": { "page": 1, "limit": 12, "total": 50 }
}
```

---

### 🔍 **Search & Filter**
```http
GET /api/public/artworks/search
Query Params:
  - q: string (search by title, artist name)
  - category_id: int (optional, multi-select)
  - artist_id: int (optional)
  - year_from: int (optional)
  - year_to: int (optional)
  - price_min: decimal (optional)
  - price_max: decimal (optional)
  - sort: string (popular|newest|price_asc|price_desc)
  - page: int (default: 1)
  - limit: int (default: 12)

Response: {
  "data": [ /* artworks */ ],
  "pagination": { /* pagination info */ },
  "filters_applied": {
    "search": "Van Gogh",
    "category_ids": [1, 2],
    "price_range": { "min": 1000000, "max": 10000000 }
  }
}
```

---

## 🔐 ADMIN API (Web Admin - Auth Required)

### 👤 **Authentication**
```http
POST /api/admin/auth/login
Content-Type: application/json

Request: {
  "username": "admin@example.com",
  "password": "password123"
}

Response: {
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "admin": {
    "admin_id": 1,
    "username": "admin@example.com",
    "role": "admin"
  }
}
```

---

### 🎨 **Manage Artworks**
```http
GET /api/admin/artworks
Query Params:
  - page: int
  - limit: int
  - status: string (published|draft|all)

Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "artwork_id": 1,
      "title": "Starry Night",
      "artist_id": 1,
      "artist_name": "Vincent Van Gogh",
      "price": 5000000,
      "is_published": true,
      "is_featured": true,
      "created_at": "2026-01-15T10:00:00",
      "updated_at": "2026-02-20T15:30:00"
    }
  ]
}
```

```http
POST /api/admin/artworks
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request: {
  "title": "Starry Night",
  "type": "oil_painting",
  "artist_id": 1,
  "slug": "starry-night",
  "year_created": 1889,
  "description": "A swirling night sky...",
  "price": 5000000,
  "is_published": true,
  "is_featured": true,
  "category_ids": [1, 2],
  "images": [file1, file2, ...]
}

Response: {
  "artwork_id": 1,
  "title": "Starry Night",
  "message": "Artwork created successfully"
}
```

```http
GET /api/admin/artworks/{artwork_id}
Headers: Authorization: Bearer {token}

Response: {
  "artwork_id": 1,
  "title": "Starry Night",
  "type": "oil_painting",
  "artist_id": 1,
  "slug": "starry-night",
  "year_created": 1889,
  "description": "...",
  "price": 5000000,
  "is_published": true,
  "is_featured": true,
  "view_count": 1250,
  "images": [ /* images */ ],
  "categories": [ /* categories */ ],
  "created_at": "2026-01-15T10:00:00",
  "updated_at": "2026-02-20T15:30:00"
}
```

```http
PUT /api/admin/artworks/{artwork_id}
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request: { /* same as POST */ }

Response: {
  "message": "Artwork updated successfully"
}
```

```http
DELETE /api/admin/artworks/{artwork_id}
Headers: Authorization: Bearer {token}

Response: {
  "message": "Artwork deleted successfully"
}
```

---

### 👤 **Manage Artists**
```http
GET /api/admin/artists
Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "artist_id": 1,
      "name": "Vincent Van Gogh",
      "slug": "vincent-van-gogh",
      "nationality": "Dutch",
      "birth_year": 1853,
      "death_year": 1890,
      "artwork_count": 45
    }
  ]
}
```

```http
POST /api/admin/artists
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request: {
  "name": "Vincent Van Gogh",
  "slug": "vincent-van-gogh",
  "birth_year": 1853,
  "death_year": 1890,
  "nationality": "Dutch",
  "biography": "Dutch Post-Impressionist painter...",
  "image": file
}

Response: {
  "artist_id": 1,
  "message": "Artist created successfully"
}
```

```http
PUT /api/admin/artists/{artist_id}
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request: { /* same as POST */ }

Response: {
  "message": "Artist updated successfully"
}
```

```http
DELETE /api/admin/artists/{artist_id}
Headers: Authorization: Bearer {token}

Response: {
  "message": "Artist deleted successfully"
}
```

---

### 📂 **Manage Categories**
```http
GET /api/admin/categories
Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "category_id": 1,
      "name": "Painting",
      "slug": "painting",
      "parent_id": null,
      "artwork_count": 150
    }
  ]
}
```

```http
POST /api/admin/categories
Headers: Authorization: Bearer {token}
Content-Type: application/json

Request: {
  "name": "Oil Painting",
  "slug": "oil-painting",
  "parent_id": 1
}

Response: {
  "category_id": 2,
  "message": "Category created successfully"
}
```

```http
PUT /api/admin/categories/{category_id}
Headers: Authorization: Bearer {token}
Content-Type: application/json

Request: { /* same as POST */ }

Response: {
  "message": "Category updated successfully"
}
```

```http
DELETE /api/admin/categories/{category_id}
Headers: Authorization: Bearer {token}

Response: {
  "message": "Category deleted successfully"
}
```

---

### 🎭 **Manage Events**
```http
GET /api/admin/events
Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "event_id": 1,
      "title": "Triển lãm Impressionism",
      "start_date": "2026-05-01T10:00:00",
      "end_date": "2026-05-31T18:00:00",
      "image_url": "url/to/event.jpg"
    }
  ]
}
```

```http
POST /api/admin/events
Headers: Authorization: Bearer {token}
Content-Type: multipart/form-data

Request: {
  "title": "Triển lãm Impressionism",
  "description": "Triển lãm các tác phẩm Impressionist...",
  "start_date": "2026-05-01T10:00:00",
  "end_date": "2026-05-31T18:00:00",
  "image": file
}

Response: {
  "event_id": 1,
  "message": "Event created successfully"
}
```

```http
PUT /api/admin/events/{event_id}
DELETE /api/admin/events/{event_id}
Headers: Authorization: Bearer {token}
```

---

### 📊 **Audit Logs**
```http
GET /api/admin/audit-logs
Query Params:
  - admin_id: int (optional)
  - target_table: string (optional)
  - page: int
  - limit: int

Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "log_id": 1,
      "admin": { "admin_id": 1, "username": "admin@example.com" },
      "action": "UPDATE",
      "target_table": "artworks",
      "target_id": 1,
      "created_at": "2026-02-20T15:30:00"
    }
  ],
  "pagination": { "page": 1, "limit": 20, "total": 500 }
}
```

---

### 👥 **Manage Admins (Super Admin Only)**
```http
GET /api/admin/admins
Headers: Authorization: Bearer {token}

Response: {
  "data": [
    {
      "admin_id": 1,
      "username": "admin@example.com",
      "role": "admin",
      "created_at": "2026-01-01T10:00:00"
    }
  ]
}
```

```http
POST /api/admin/admins
Headers: Authorization: Bearer {token}
Content-Type: application/json

Request: {
  "username": "newadmin@example.com",
  "password": "password123",
  "role_id": 2
}

Response: {
  "admin_id": 2,
  "message": "Admin created successfully"
}
```

```http
DELETE /api/admin/admins/{admin_id}
Headers: Authorization: Bearer {token}

Response: {
  "message": "Admin deleted successfully"
}
```

---

## 📝 Common Response Format

### Success Response
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation successful",
  "timestamp": "2026-04-18T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "email", "message": "Invalid email format" }
    ]
  },
  "timestamp": "2026-04-18T10:30:00Z"
}
```

---

## 🔑 HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Auth required |
| 403 | Forbidden - No permission |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error |

---

## 📋 Pagination Standard

Tất cả list API sử dụng format:
```json
{
  "data": [ /* items */ ],
  "pagination": {
    "page": 1,
    "limit": 12,
    "total": 245,
    "total_pages": 21
  }
}
```
