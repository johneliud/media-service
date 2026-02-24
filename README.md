# Media Service

Microservice responsible for media file management and image serving for products.

## Overview

- **Port**: 8081
- **Technology**: Spring Boot 3.x
- **Database**: MongoDB collection `media`
- **Storage**: Local filesystem
- **Purpose**: Image upload, storage, and serving

## Features

### Media Upload
- Upload product images (sellers only)
- Supported formats: PNG, JPG, JPEG, WEBP
- Max file size: 2MB
- UUID-based file naming
- Product association

### Media Retrieval
- Get media by ID (public)
- Get media by product ID (public)
- Get seller's media (sellers only)
- Serve images with proper Content-Type

### Media Deletion
- Delete media (sellers only)
- Ownership verification
- File cleanup from filesystem

## API Endpoints

### Public Endpoints

#### Get Media by ID
```http
GET /api/media/{id}
```

Returns image with Content-Type: image/png, image/jpeg, or image/webp.

#### Get Media by Product ID
```http
GET /api/media/product/{productId}
```

Response:
```json
{
  "success": true,
  "message": "Media retrieved successfully",
  "data": [
    {
      "id": "media-id",
      "imagePath": "uuid-filename.png",
      "productId": "product-id",
      "sellerId": "seller-id"
    }
  ]
}
```

### Protected Endpoints (Sellers Only)

Require `Authorization: Bearer <token>` header and X-User-Id, X-User-Role headers (added by gateway).

#### Upload Media
```http
POST /api/media/upload
Content-Type: multipart/form-data

image: <file>
productId: <product-id>
```

Response:
```json
{
  "success": true,
  "message": "Media uploaded successfully",
  "data": {
    "id": "media-id",
    "imagePath": "uuid-filename.png",
    "productId": "product-id",
    "sellerId": "seller-id"
  }
}
```

#### Get Seller's Media
```http
GET /api/media/my-media?productId=<product-id>
```

Query Parameters:
- `productId` - Optional filter by product

#### Delete Media
```http
DELETE /api/media/{id}
```

## Data Model

### Media
```json
{
  "id": "string",
  "imagePath": "string (UUID-based filename)",
  "productId": "string",
  "sellerId": "string"
}
```

## Configuration

### Application Properties
```properties
server.port=8081
spring.data.mongodb.uri=mongodb://localhost:27017/buy01
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
file.upload-dir=uploads/media
```

## Running the Service

```bash
cd backend/media-service
mvn spring-boot:run
```

Ensure MongoDB is running on port 27017.

## File Storage

Images are stored in:
```
backend/media-service/uploads/media/
```

File naming convention:
```
{uuid}.{extension}
```

Example: `9a496e2e-f203-479b-8e04-4b4964cc206a.png`

## Image Serving

Images are served with:
- Proper Content-Type header (image/png, image/jpeg, image/webp)
- Cache-Control header: max-age=31536000 (1 year)
- Accept-Ranges header for partial content support

## Validation

### File Type Validation
Allowed MIME types:
- image/png
- image/jpeg
- image/jpg
- image/webp

### File Size Validation
- Maximum: 2MB (2,097,152 bytes)
- Enforced at Spring Boot level

## Security

- Only sellers can upload media
- Only sellers can delete media
- Sellers can only delete their own media
- Ownership verified via sellerId field
- File type validation prevents malicious uploads
- File size limit prevents DoS attacks

## Dependencies

- Spring Boot 3.x
- Spring Data MongoDB
- Spring Web (Multipart)
- Spring Kafka
- Lombok

## Kafka Integration

### Consumer Configuration
Listens for product deletion events and automatically deletes associated media.

**Topic**: `product-deleted`
**Consumer Group**: `media-service-group`

**Event Structure**:
```json
{
  "productId": "string",
  "timestamp": "ISO-8601 datetime"
}
```

### Automatic Cleanup Flow
1. Product Service publishes `ProductDeletedEvent` to Kafka
2. Media Service consumes the event
3. Media Service queries all media with matching productId
4. Media Service deletes files from filesystem
5. Media Service removes records from database

### Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=media-service-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

## Error Responses

```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

Common errors:
- 400 - Invalid file type or size
- 403 - Not authorized (not a seller or not media owner)
- 404 - Media not found
- 413 - File too large

## Usage Example

### Upload Image Flow
1. Seller creates a product via Product Service
2. Seller uploads image via Media Service with productId
3. Media Service stores file and creates database record
4. Frontend displays image using media ID

### Display Image Flow
1. Frontend requests product list
2. For each product, request media by productId
3. Get first media's ID
4. Display image using `/api/media/{id}` endpoint

## Database Indexes

Recommended indexes for performance:
```javascript
db.media.createIndex({ "productId": 1 })
db.media.createIndex({ "sellerId": 1 })
```
