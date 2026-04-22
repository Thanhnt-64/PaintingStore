# API Test Script for Landing Page Endpoints

## URLs
- Base URL: `http://localhost:8080`
- Featured Artworks: `GET /api/public/artworks/featured`
- Upcoming Events: `GET /api/public/events`

## Test 1: Featured Artworks API

```bash
curl -X GET "http://localhost:8080/api/public/artworks/featured" \
  -H "Content-Type: application/json"
```

**Expected Response (200 OK):**
```json
{
  "data": [
    {
      "artwork_id": 5,
      "title": "Guernica",
      "artist": {
        "artist_id": 2,
        "name": "Pablo Picasso"
      },
      "price": 9500000,
      "primary_image": "https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Picasso_Guernica.jpg",
      "view_count": 3200,
      "slug": "guernica"
    },
    {
      "artwork_id": 4,
      "title": "Water Lilies",
      "artist": {
        "artist_id": 3,
        "name": "Claude Monet"
      },
      "price": 6500000,
      "primary_image": "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Monet_Water_Lilies_1906.jpg",
      "view_count": 1850,
      "slug": "water-lilies"
    },
    ...
  ],
  "total": 5
}
```

## Test 2: Upcoming Events API

```bash
curl -X GET "http://localhost:8080/api/public/events" \
  -H "Content-Type: application/json"
```

**Expected Response (200 OK):**
```json
{
  "data": [
    {
      "event_id": 1,
      "title": "Triển lãm Impressionism",
      "start_date": "2026-05-01T10:00:00",
      "end_date": "2026-05-31T18:00:00",
      "image_url": "https://example.com/impressionism.jpg"
    },
    {
      "event_id": 4,
      "title": "Watercolor Art Workshop",
      "start_date": "2026-07-10T14:00:00",
      "end_date": "2026-07-10T17:00:00",
      "image_url": "https://example.com/watercolor-workshop.jpg"
    },
    ...
  ],
  "total": 4
}
```

## Database Schema Verification

### Artworks Table
- `artwork_id`: Primary key
- `title`: Artwork title
- `slug`: URL-friendly slug
- `artist_id`: Foreign key to artists
- `price`: DECIMAL(12,2)
- `is_featured`: TINYINT(1) - marks featured artworks
- `view_count`: INT - view counter
- `created_at`, `updated_at`: Timestamps

### Artists Table
- `artist_id`: Primary key
- `name`: Artist name
- `slug`: URL-friendly slug
- `nationality`: Artist nationality
- `image_url`: Artist image
- `biography`: Artist bio

### Artwork Images Table (ONE-TO-MANY with Artworks)
- `image_id`: Primary key
- `artwork_id`: Foreign key to artworks
- `image_url`: Image URL
- `is_primary`: TINYINT(1) - marks primary image for featured display

### Events Table
- `event_id`: Primary key
- `title`: Event title
- `description`: Event description
- `start_date`: DATETIME
- `end_date`: DATETIME
- `image_url`: Event image

## Implementation Details

### PublicController
- **Endpoint**: `/api/public/artworks/featured`
  - Query: `findFeaturedWithDetails()` from ArtworkRepository
  - Filters: Only `is_featured = true` artworks
  - Sorting: By `view_count DESC`, `createdAt DESC`
  - Response format: PaginatedResponse<FeaturedArtworkDTO>

- **Endpoint**: `/api/public/events`
  - Query: `findByStartDateAfterOrderByStartDateAsc(now)` from EventRepository
  - Filters: Only events with `start_date` after current time
  - Sorting: By `start_date ASC`
  - Response format: PaginatedResponse<EventDTO>

### DTOs Used
1. **FeaturedArtworkDTO**: artwork_id, title, artist, price, primary_image, view_count, slug
2. **ArtistDTO**: artist_id, name
3. **EventDTO**: event_id, title, start_date, end_date, image_url
4. **PaginatedResponse<T>**: Generic wrapper with data[] and total count

### Notes on Database Correctness
✅ All tables created in V1__init_schema.sql match the entity domain classes
✅ Foreign key relationships properly configured (artist_id, artwork_id)
✅ Primary image selection: Uses `is_primary = true` from artwork_images, falls back to first image if no primary
✅ Featured filtering works via `is_featured` column in artworks table
✅ Event filtering works via date comparison in EventRepository
✅ Seed data provided in V4__seed_artworks_and_events.sql for testing

## How to Test

1. **Start the application:**
   ```bash
   java -jar target/ManageStore-0.0.1-SNAPSHOT.jar
   ```

2. **Ensure database is properly initialized** (Flyway will run migrations automatically)

3. **Test endpoints with curl or Postman:**
   - GET http://localhost:8080/api/public/artworks/featured
   - GET http://localhost:8080/api/public/events

4. **Verify response structure matches API specification**
