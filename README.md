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

### Pending Implementations

#### MS-5: Media Deletion API
- DELETE /api/media/{id} endpoint
- Seller ownership verification
- Physical file and database record deletion

#### MS-6: Seller Media Management
- GET /api/media/my-media endpoint for sellers
- Filter by productId support

#### MS-7: Authorization & Access Control
- JWT validation for protected endpoints
- Seller ownership verification
- Public access to retrieval endpoints

#### MS-8: File Validation & Security
- Enhanced file integrity validation
- Filename sanitization for directory traversal prevention

#### MS-9: Error Handling & Validation
- Global exception handler
- Consistent error response format

#### MS-10: Kafka Integration
- Product deletion event consumer
- Cascading media deletion

#### MS-11: Unit & Integration Testing
- Service layer tests
- File upload/validation tests
- Ownership verification tests

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
    "productId": "prod123"
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
      "productId": "prod123"
    }
  ]
}
```

## Testing

The project includes:
- Context load tests
- MongoDB connection tests

Run tests with: `./mvnw test`