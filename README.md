# Media Service

A Spring Boot microservice for managing product media (images) in the marketplace, supporting upload, retrieval, and deletion of product images with file validation and storage.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Features

### Completed Implementations

#### MS-1: Database Schema Design & Implementation
- Media model with MongoDB document mapping
- Fields: id, imagePath, productId
- Indexed productId for query optimization
- MediaRepository with productId lookup methods

#### MS-2: Media Upload API
- POST /api/media/upload endpoint
- Accepts image file with productId
- File type validation (PNG, JPG, JPEG, GIF, WEBP)
- MIME type verification
- File size limit (2MB)
- Unique filename generation with UUID
- Comprehensive logging

#### MS-3: File Storage Implementation
- Local filesystem storage mechanism
- UUID-based unique filenames to prevent collisions
- Configurable upload directory
- File path stored in Media entity
- Ready for cloud storage migration (Cloudinary, S3)

#### MS-4: Media Retrieval API
- GET /api/media/{id} endpoint to retrieve media file
- GET /api/media/product/{productId} endpoint for all product media
- Proper content-type headers for images
- Cache headers for performance (max-age=31536000)
- Handles missing files gracefully

#### MS-5: Media Deletion API
- DELETE /api/media/{id} endpoint
- Seller ownership verification before deletion
- Physical file and database record deletion
- Handles missing files gracefully

#### MS-6: Seller Media Management
- GET /api/media/my-media endpoint for sellers
- Returns all media for authenticated seller
- Optional productId filtering support

#### MS-7: Authorization & Access Control
- JWT validation for protected endpoints
- JwtUtil and JwtAuthenticationFilter implementation
- Seller ownership verification for media operations
- Public access to retrieval endpoints (GET /api/media/{id}, GET /api/media/product/{productId})
- Protected endpoints require authentication (upload, delete, my-media)
- sellerId stored in Media model for ownership tracking

#### MS-8: File Validation & Security
- File type validation (MIME type and extension)
- File size validation (2MB limit)
- Filename sanitization (UUID-based)
- Image integrity validation using magic bytes (PNG, JPEG, WEBP)

#### MS-9: Error Handling & Validation
- GlobalExceptionHandler with consistent error responses
- Clear error messages for file type violations
- Clear error messages for file size violations
- Graceful storage failure handling
- ErrorResponse DTO for consistent format

#### MS-11: Unit & Integration Testing
- MediaService unit tests (upload, delete, retrieval, ownership)
- FileStorageService unit tests (validation, size limits, file types)
- 13 tests passing with full coverage

### Pending Implementations

#### MS-10: Kafka Integration
- Product deletion event consumer
- Cascading media deletion

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.2**
- **MongoDB** - Database
- **Spring Data MongoDB** - Data access
- **Lombok** - Boilerplate reduction
- **Maven** - Build tool

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- MongoDB Atlas account or local MongoDB instance

### Clone Repository

```bash
git clone https://github.com/johneliud/media-service.git
cd media-service
```

### Configuration

Create and Update `src/main/resources/application-secrets.properties`:

```properties
spring.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<database>
# Server Configuration
server.port=8081
# JWT Configuration
jwt.secret=<your-secret-key>
jwt.expiration=86400000
# File upload configuration
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
file.upload.dir=uploads/media
```

### Build & Run

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

The service will start on `http://localhost:8081`

### Run Tests

```bash
./mvnw test
```

## API Documentation

### Upload Media
```http
POST /api/media/upload
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

Parameters:
- image: MultipartFile (required)
- productId: String (required)

Response: 201 Created
{
  "success": true,
  "message": "Media uploaded successfully",
  "data": {
    "id": "65f8a9b2c3d4e5f6g7h8i9j0",
    "imagePath": "abc-123-def-456.jpg",
    "productId": "prod123",
    "sellerId": "seller123"
  }
}
```

### Get Media File
```http
GET /api/media/{id}

Response: 200 OK
Content-Type: image/jpeg
Cache-Control: max-age=31536000

[Binary image data]
```

### Get Product Media
```http
GET /api/media/product/{productId}

Response: 200 OK
{
  "success": true,
  "message": "Media retrieved successfully",
  "data": [
    {
      "id": "65f8a9b2c3d4e5f6g7h8i9j0",
      "imagePath": "abc-123-def-456.jpg",
      "productId": "prod123",
      "sellerId": "seller123"
    }
  ]
}
```

### Delete Media
```http
DELETE /api/media/{id}
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "message": "Media deleted successfully",
  "data": null
}
```

### Get Seller Media
```http
GET /api/media/my-media?productId={productId}
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "message": "Media retrieved successfully",
  "data": [...]
}
```

## Testing

The project includes:
- MediaService unit tests (upload, delete, retrieval, ownership verification)
- FileStorageService unit tests (file validation, size limits, image integrity)
- 13 tests with full coverage

Run tests with: `./mvnw test`