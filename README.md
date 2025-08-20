# Healthcare Staffing CQRS Platform

A comprehensive **Command Query Responsibility Segregation (CQRS)** architecture implementation for a healthcare staffing platform, built with **Java 17**, **Spring Boot**, **Kafka**, **Redis**, and **PostgreSQL**.

## 🏗️ Architecture Overview

This project implements a strict CQRS pattern with complete separation between read and write operations:

- **Write-side services** handle commands and emit domain events to Kafka
- **Event-driven processors** consume events and maintain Redis-based projections
- **Read-side services** provide fast, precomputed views from Redis
- **No direct coupling** between read and write models

## 📦 Modules

### 1. **shared** (Common Module)
- Common DTOs and event classes
- Booking events: `BookingCreated`, `BookingModified`, `BookingCancelled`, `BookingBooked`, `BookingPullout`
- Carer events: `NewCarer`, `CarerUpdated`, `CarerAvailabilityChanged`
- Read-side DTOs: `EligibleShiftDto`, `EligibleCarerDto`

### 2. **booking-service** (Write Side)
- **Port:** 8001
- **Database:** PostgreSQL (port 5432)
- **Purpose:** Manage booking lifecycle
- **APIs:**
  - `POST /api/bookings` - Create booking
  - `PUT /api/bookings/{id}` - Update booking
  - `POST /api/bookings/{id}/cancel` - Cancel booking
  - `POST /api/bookings/{id}/book` - Assign carer
  - `POST /api/bookings/{id}/pullout` - Remove carer

### 3. **carer-service** (Write Side)
- **Port:** 8002
- **Database:** PostgreSQL (port 5433)
- **Purpose:** Manage carer profiles and availability
- **APIs:**
  - `POST /api/carers` - Create carer
  - `PUT /api/carers/{id}` - Update carer
  - `POST /api/carers/{id}/availability` - Update availability

### 4. **view-maintenance-service** (Event Processor)
- **Port:** 8003
- **Storage:** Redis
- **Purpose:** Process events and maintain projections
- **Projections:**
  - `AvailableShiftsPerCarer:{carerId}` - Eligible shifts for each carer
  - `EligibleCarersPerShift:{bookingId}` - Eligible carers for each shift
- **Rules Engine:** Applies deterministic eligibility rules

### 5. **read-api-service** (Read Side)
- **Port:** 8004
- **Storage:** Redis (read-only)
- **Purpose:** Fast read operations
- **APIs:**
  - `GET /api/read/carer/{id}/eligible-shifts` - Get eligible shifts for carer
  - `GET /api/read/shift/{id}/eligible-carers` - Get eligible carers for shift
  - Filtering and sorting capabilities

### 6. **booking-orchestration-service** (Orchestration Layer)
- **Port:** 8005
- **Purpose:** Coordinate complex booking operations across services
- **Pattern:** Synchronous orchestration with compensation-based rollback
- **APIs:**
  - `POST /api/booking-orchestration/assign-carer` - Assign carer to booking with two-phase operation
  - `POST /api/booking-orchestration/remove-carer` - Remove carer from booking with rollback
- **Features:**
  - Circuit breakers for service resilience
  - Automatic rollback on failure
  - Carer availability blocking
  - End-to-end booking coordination

## 🔧 Technology Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Apache Kafka** (Event streaming)
- **Redis** (Read projections)
- **PostgreSQL** (Write-side persistence)
- **Gradle** (Build tool)
- **Docker Compose** (Local development)

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Gradle 8+

### 1. Start Infrastructure

```bash
# Start Kafka, Redis, PostgreSQL
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 2. Build the Project

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :booking-service:build
```

### 3. Run Services

```bash
# Terminal 1 - Booking Service
./gradlew :booking-service:bootRun

# Terminal 2 - Carer Service  
./gradlew :carer-service:bootRun

# Terminal 3 - View Maintenance Service
./gradlew :view-maintenance-service:bootRun

# Terminal 4 - Read API Service
./gradlew :read-api-service:bootRun

# Terminal 5 - Booking Orchestration Service
./gradlew :booking-orchestration-service:bootRun
```

### 4. Verify Deployment

```bash
# Check service health
curl http://localhost:8001/actuator/health  # Booking Service
curl http://localhost:8002/actuator/health  # Carer Service
curl http://localhost:8003/actuator/health  # View Maintenance
curl http://localhost:8004/actuator/health  # Read API
curl http://localhost:8005/actuator/health  # Booking Orchestration
```

## 📋 Usage Examples

### Create a Carer

```bash
curl -X POST http://localhost:8002/api/carers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+44123456789",
    "location": "London",
    "grade": "RN",
    "qualifications": ["BLS", "ACLS"],
    "visaStatus": "CITIZEN",
    "maxTravelDistance": 50
  }'
```

### Create a Booking

```bash
curl -X POST http://localhost:8001/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": "550e8400-e29b-41d4-a716-446655440000",
    "shift": "DAY",
    "startTime": "2025-08-20T08:00:00",
    "endTime": "2025-08-20T20:00:00",
    "grade": "RN",
    "hourlyRate": 35.00,
    "location": "London",
    "specialRequirements": "ICU experience preferred",
    "requiredQualifications": ["BLS"]
  }'
```

### Query Eligible Shifts

```bash
# Get eligible shifts for a carer
curl http://localhost:8004/api/read/carer/{carerId}/eligible-shifts

# Get eligible carers for a shift
curl http://localhost:8004/api/read/shift/{shiftId}/eligible-carers

# Filter by distance
curl "http://localhost:8004/api/read/carer/{carerId}/eligible-shifts?maxDistance=25&sortByDistance=true"
```

### Assign Carer to Booking (Orchestration)

```bash
# Assign carer to booking with orchestration
curl -X POST http://localhost:8005/api/booking-orchestration/assign-carer \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "9a103b9a-874f-495c-8999-66e9e35758c5",
    "carerId": "54a86261-e8b2-4e87-80cf-4d845f117af6",
    "bookedBy": "Healthcare Admin",
    "startTime": "2025-08-25T09:00:00",
    "endTime": "2025-08-25T17:00:00"
  }'

# Remove carer from booking
curl -X POST http://localhost:8005/api/booking-orchestration/remove-carer \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "9a103b9a-874f-495c-8999-66e9e35758c5",
    "carerId": "54a86261-e8b2-4e87-80cf-4d845f117af6",
    "pulloutReason": "Emergency unavailable",
    "pulloutBy": "Healthcare Admin"
  }'
```

## 🔧 Configuration

### Environment Variables

```bash
# Database URLs
BOOKING_DB_URL=jdbc:postgresql://localhost:5432/booking_db
CARER_DB_URL=jdbc:postgresql://localhost:5433/carer_db

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| booking-service | 8001 | Booking write operations |
| carer-service | 8002 | Carer write operations |
| view-maintenance-service | 8003 | Event processing |
| read-api-service | 8004 | Read operations |
| booking-orchestration-service | 8005 | Booking coordination |
| Kafka | 9092 | Event streaming |
| Kafka UI | 9080 | Development UI |
| Redis | 6379 | Read projections |
| Redis Commander | 9081 | Development UI |
| PostgreSQL (Booking) | 5432 | Booking data |
| PostgreSQL (Carer) | 5433 | Carer data |

## 🎯 Eligibility Rules

The system applies these deterministic rules for carer-shift matching:

1. **Grade Matching**: Carer grade must match booking grade
2. **Qualifications**: Carer must have all required qualifications
3. **Distance**: Booking location must be within carer's travel distance
4. **Visa Status**: Visa requirements vary by facility
5. **Availability**: Carer must be available during shift times

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :booking-service:test

# Generate test reports
./gradlew test jacocoTestReport
```

## 📊 Monitoring

### Development Tools

- **Kafka UI**: http://localhost:9080 - Monitor Kafka topics and messages
- **Redis Commander**: http://localhost:9081 - Inspect Redis data
- **Spring Boot Actuator**: `/actuator/health`, `/actuator/metrics`

### Key Metrics

- Event processing latency
- Projection update times
- Redis hit rates
- Database connection pools

## 🔄 Event Flow

```
1. Command → Write Service (booking/carer)
2. Write Service → Database + Kafka Event
3. Kafka Event → View Maintenance Service
4. View Maintenance → Apply Rules + Update Redis
5. Client Query → Read API Service → Redis
```

## 🎯 Orchestration Pattern

The booking orchestration service implements a **synchronous orchestration pattern** for complex booking operations:

### Two-Phase Booking Assignment

```
1. Phase 1: Block Carer Availability
   - POST to carer-service /availability/block
   - Circuit breaker protection
   - Automatic retry on transient failures

2. Phase 2: Assign Booking
   - POST to booking-service /book
   - Updates booking status to BOOKED
   - Links carer to booking

3. Rollback on Failure:
   - If Phase 2 fails → Unblock carer availability
   - If Phase 1 fails → Return error immediately
   - Compensation-based transaction management
```

### Benefits

- **Consistency**: Ensures carer availability and booking assignment are synchronized
- **Resilience**: Circuit breakers prevent cascading failures
- **Rollback**: Automatic compensation on partial failures
- **Simplicity**: Synchronous calls are easier to reason about than event choreography

## 📈 Scalability Considerations

- **Horizontal Scaling**: All services are stateless
- **Event Replay**: View maintenance service supports event replay
- **Partitioning**: Kafka topics can be partitioned by entity ID
- **Caching**: Redis projections provide sub-millisecond reads
- **Idempotency**: All event processors are idempotent

## 🛠️ Development

### Adding New Event Types

1. Define event class in `shared/events/`
2. Update producer configuration in write service
3. Add consumer handler in view-maintenance-service
4. Update projection logic

### Extending Eligibility Rules

1. Modify `EligibilityRulesEngine`
2. Update projection calculations
3. Add corresponding tests

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes with tests
4. Submit a pull request

---

**Built with ❤️ for scalable healthcare staffing solutions with CQRS architecture and sophisticated booking orchestration**
