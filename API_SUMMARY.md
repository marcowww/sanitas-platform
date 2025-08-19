# Healthcare Staffing CQRS Platform - API Integration Summary

## Overview
This document provides a comprehensive API reference for integrating with the Healthcare Staffing CQRS platform. The system follows a strict CQRS architecture with separate read and write operations distributed across multiple microservices.

## Service Architecture

### Write Services (Command Side)
- **Booking Service**: Port 8001 - `/api/bookings`
- **Carer Service**: Port 8002 - `/api/carers`

### Read Services (Query Side)
- **Read API Service**: Port 8004 - `/api/read`

## Base URLs
```
Booking Service:     http://localhost:8001
Carer Service:       http://localhost:8002
Read API Service:    http://localhost:8004
```

---

## 🏥 Booking Management API (Write Operations)

### Create Booking
```http
POST /api/bookings
Content-Type: application/json

{
  "facilityId": "uuid",
  "shift": "Day Shift",
  "startTime": "2025-08-20T09:00:00",
  "endTime": "2025-08-20T17:00:00",
  "grade": "RN",
  "hourlyRate": 35.00,
  "location": "London",
  "specialRequirements": "ICU experience required",
  "requiredQualifications": ["BLS", "First Aid"]
}
```

**Response**: `201 Created`
```json
{
  "id": "uuid",
  "facilityId": "uuid",
  "shift": "Day Shift",
  "startTime": "2025-08-20T09:00:00",
  "endTime": "2025-08-20T17:00:00",
  "grade": "RN",
  "hourlyRate": 35.00,
  "location": "London",
  "specialRequirements": "ICU experience required",
  "requiredQualifications": ["BLS", "First Aid"],
  "status": "OPEN",
  "createdAt": "2025-08-19T10:30:00"
}
```

### Update Booking
```http
PUT /api/bookings/{bookingId}
Content-Type: application/json

{
  "updates": {
    "location": "Manchester",
    "grade": "HCA",
    "hourlyRate": 32.00,
    "requiredQualifications": ["First Aid", "Manual Handling"]
  },
  "modificationReason": "Client requirements changed"
}
```

**Response**: `200 OK` (Returns updated booking object)

### Assign Carer to Booking
```http
POST /api/bookings/{bookingId}/book
Content-Type: application/json

{
  "carerId": "uuid",
  "bookedBy": "scheduler@hospital.com"
}
```

**Response**: `204 No Content`

### Cancel Booking
```http
POST /api/bookings/{bookingId}/cancel
Content-Type: application/json

{
  "cancellationReason": "Shift no longer required",
  "cancelledBy": "manager@hospital.com"
}
```

**Response**: `204 No Content`

### Carer Pullout
```http
POST /api/bookings/{bookingId}/pullout
Content-Type: application/json

{
  "carerId": "uuid",
  "pulloutReason": "Family emergency",
  "pulloutBy": "carer@example.com"
}
```

**Response**: `204 No Content`

### Get Booking Details
```http
GET /api/bookings/{bookingId}
```

**Response**: `200 OK` (Returns booking object)

### Get All Bookings
```http
GET /api/bookings
```

**Response**: `200 OK` (Returns array of booking objects)

---

## 👨‍⚕️ Carer Management API (Write Operations)

### Create Carer
```http
POST /api/carers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phone": "+44123456789",
  "location": "London",
  "grade": "RN",
  "qualifications": ["BLS", "First Aid", "ICU"],
  "visaStatus": "CITIZEN",
  "maxTravelDistance": 50
}
```

**Response**: `201 Created`
```json
{
  "id": "uuid",
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phone": "+44123456789",
  "location": "London",
  "grade": "RN",
  "qualifications": ["BLS", "First Aid", "ICU"],
  "visaStatus": "CITIZEN",
  "maxTravelDistance": 50,
  "createdAt": "2025-08-19T10:30:00"
}
```

### Update Carer
```http
PUT /api/carers/{carerId}
Content-Type: application/json

{
  "updates": {
    "grade": "Senior RN",
    "qualifications": ["BLS", "First Aid", "ICU", "Advanced Life Support"],
    "maxTravelDistance": 75
  },
  "updateReason": "Qualification upgrade completed"
}
```

**Response**: `200 OK` (Returns updated carer object)

### Update Carer Availability
```http
POST /api/carers/{carerId}/availability
Content-Type: application/json

{
  "availabilitySlots": [
    {
      "date": "2025-08-20",
      "startTime": "09:00",
      "endTime": "17:00",
      "shiftType": "Day Shift"
    },
    {
      "date": "2025-08-21",
      "startTime": "21:00",
      "endTime": "09:00",
      "shiftType": "Night Shift"
    }
  ]
}
```

**Response**: `204 No Content`

### Get Carer Details
```http
GET /api/carers/{carerId}
```

**Response**: `200 OK` (Returns carer object)

### Get All Carers
```http
GET /api/carers
```

**Response**: `200 OK` (Returns array of carer objects)

### Get Carer Availability
```http
GET /api/carers/{carerId}/availability?fromDate=2025-08-20&toDate=2025-08-27
```

**Response**: `200 OK`
```json
[
  {
    "id": "uuid",
    "carerId": "uuid",
    "date": "2025-08-20",
    "startTime": "09:00",
    "endTime": "17:00",
    "shiftType": "Day Shift",
    "available": true
  }
]
```

---

## 📊 Read API (Query Operations)

### Get Eligible Carers for Shift
```http
GET /api/read/shift/{shiftId}/eligible-carers
GET /api/read/shift/{shiftId}/eligible-carers?maxDistance=30
GET /api/read/shift/{shiftId}/eligible-carers?sortByDistance=true
```

**Response**: `200 OK`
```json
[
  {
    "carerId": "uuid",
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phone": "+44123456789",
    "location": "London",
    "grade": "RN",
    "qualifications": ["BLS", "First Aid", "ICU"],
    "visaStatus": "CITIZEN",
    "maxTravelDistance": 50,
    "distanceKm": 12.5,
    "available": true
  }
]
```

### Get Eligible Carers by Grade
```http
GET /api/read/shift/{shiftId}/eligible-carers/grade/{grade}
```

### Get Eligible Shifts for Carer
```http
GET /api/read/carer/{carerId}/eligible-shifts
GET /api/read/carer/{carerId}/eligible-shifts?maxDistance=30
GET /api/read/carer/{carerId}/eligible-shifts?sortByDistance=true
```

**Response**: `200 OK`
```json
[
  {
    "bookingId": "uuid",
    "facilityId": "uuid",
    "facilityName": "City Hospital",
    "shift": "Day Shift",
    "startTime": "2025-08-20T09:00:00",
    "endTime": "2025-08-20T17:00:00",
    "grade": "RN",
    "hourlyRate": 35.00,
    "location": "London",
    "specialRequirements": "ICU experience required",
    "requiredQualifications": ["BLS", "First Aid"],
    "status": "OPEN",
    "distanceKm": 12.5
  }
]
```

### Get Eligible Shifts by Location
```http
GET /api/read/carer/{carerId}/eligible-shifts/location/{location}
```

### Check Carer Eligibility for Shift
```http
GET /api/read/carer/{carerId}/shift/{shiftId}/eligible
```

**Response**: `200 OK`
```json
{
  "carerId": "uuid",
  "shiftId": "uuid",
  "eligible": true
}
```

### Get Counts
```http
GET /api/read/carer/{carerId}/eligible-shifts/count
GET /api/read/shift/{shiftId}/eligible-carers/count
```

**Response**: `200 OK`
```json
{
  "count": 15
}
```

### Health Check
```http
GET /api/read/health
```

**Response**: `200 OK`
```json
{
  "message": "Read API Service is healthy",
  "timestamp": 1692441600000
}
```

---

## 📋 Data Models

### Booking Object
```json
{
  "id": "uuid",
  "facilityId": "uuid",
  "shift": "string",
  "startTime": "iso-datetime",
  "endTime": "iso-datetime",
  "grade": "string",
  "hourlyRate": "decimal",
  "location": "string",
  "specialRequirements": "string",
  "requiredQualifications": ["string"],
  "status": "OPEN|BOOKED|CANCELLED|COMPLETED",
  "assignedCarerId": "uuid|null",
  "createdAt": "iso-datetime",
  "updatedAt": "iso-datetime"
}
```

### Carer Object
```json
{
  "id": "uuid",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "location": "string",
  "grade": "string",
  "qualifications": ["string"],
  "visaStatus": "CITIZEN|SETTLED|VISA_REQUIRED",
  "maxTravelDistance": "integer",
  "createdAt": "iso-datetime",
  "updatedAt": "iso-datetime"
}
```

### EligibleCarerDto
```json
{
  "carerId": "uuid",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "location": "string",
  "grade": "string",
  "qualifications": ["string"],
  "visaStatus": "string",
  "maxTravelDistance": "integer",
  "distanceKm": "double",
  "available": "boolean"
}
```

### EligibleShiftDto
```json
{
  "bookingId": "uuid",
  "facilityId": "uuid",
  "facilityName": "string",
  "shift": "string",
  "startTime": "iso-datetime",
  "endTime": "iso-datetime",
  "grade": "string",
  "hourlyRate": "decimal",
  "location": "string",
  "specialRequirements": "string",
  "requiredQualifications": ["string"],
  "status": "string",
  "distanceKm": "double"
}
```

---

## 🚫 Error Handling

### HTTP Status Codes
- `200 OK` - Successful GET request
- `201 Created` - Successful POST request (resource created)
- `204 No Content` - Successful PUT/POST request (no response body)
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Business rule violation
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "timestamp": "2025-08-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/bookings"
}
```

---

## 🔄 Event-Driven Architecture

### Key Events Published
- `CarerCreated` - New carer registered
- `CarerModified` - Carer details updated
- `CarerAvailabilityUpdated` - Availability changed
- `BookingCreated` - New booking created
- `BookingModified` - Booking details updated
- `BookingAssigned` - Carer assigned to booking
- `BookingCancelled` - Booking cancelled
- `CarerPulledOut` - Carer pulled out of booking

### Event Processing
- Events are processed asynchronously via Kafka
- Read projections are eventually consistent
- All event handlers are idempotent for replay scenarios

---

## 🎯 Business Rules

### Eligibility Criteria
1. **Grade Matching**: Carer grade must match or exceed booking grade
2. **Qualifications**: Carer must possess all required qualifications
3. **Distance**: Booking location within carer's travel distance
4. **Visa Status**: Appropriate visa status for facility requirements
5. **Availability**: Carer available during shift times

### Grade Hierarchy
- `HCA` (Healthcare Assistant)
- `RN` (Registered Nurse)
- `Senior RN`
- `Charge Nurse`

### Visa Status Types
- `CITIZEN` - Full work rights
- `SETTLED` - Permanent residence
- `VISA_REQUIRED` - Work visa needed

---

## 🔧 Integration Guidelines

### Authentication
Currently no authentication required for development environment.
Production deployment should implement:
- JWT token validation
- Role-based access control
- API rate limiting

### Rate Limiting
Recommended limits for production:
- Write operations: 100 requests/minute per client
- Read operations: 1000 requests/minute per client

### Caching Strategy
- Read projections cached in Redis
- Cache TTL: Real-time (event-driven updates)
- Cache invalidation: Automatic via event processing

### Monitoring Endpoints
- Health checks: `/actuator/health` on each service
- Metrics: `/actuator/metrics` on each service
- Kafka UI: http://localhost:9080 (development)
- Redis Commander: http://localhost:9081 (development)

---

## 📈 Performance Characteristics

### Response Times (Development)
- Write operations: < 100ms
- Read operations: < 10ms
- Event processing: < 500ms
- Projection updates: < 1s (eventual consistency)

### Scalability Features
- Stateless services enable horizontal scaling
- Kafka partitioning for high throughput
- Redis clustering for read scaling
- Database read replicas supported

---

## 🛠️ SDK Examples

### Java Integration
```java
// Create booking
BookingRequest request = new BookingRequest();
request.setFacilityId(facilityId);
request.setGrade("RN");
// ... set other fields

RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Booking> response = restTemplate.postForEntity(
    "http://localhost:8001/api/bookings", 
    request, 
    Booking.class
);
```

### JavaScript Integration
```javascript
// Get eligible carers
const response = await fetch(`http://localhost:8004/api/read/shift/${shiftId}/eligible-carers`);
const eligibleCarers = await response.json();
```

### cURL Examples
See individual API sections above for detailed cURL examples.

---

**Last Updated**: August 19, 2025  
**API Version**: 1.0.0  
**Documentation Version**: 1.0.0

For technical support or integration assistance, please refer to the main repository documentation or contact the development team.
