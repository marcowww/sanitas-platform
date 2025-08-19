<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Healthcare Staffing CQRS Platform - Copilot Instructions

## Project Overview
This is a multi-module Java project implementing a CQRS (Command Query Responsibility Segregation) architecture for a healthcare staffing platform. The system strictly separates read and write operations with event-driven communication via Kafka.

## Architecture Principles
- **CQRS Enforced**: Read/write models are completely separate
- **Event-Driven**: All communication between services via Kafka events
- **Eventual Consistency**: Read projections are eventually consistent with write models
- **Domain Events**: Fine-grained, strongly-typed events for precise updates
- **Scalable Design**: Stateless services with Redis-based read projections

## Module Structure
- `shared/`: Common DTOs and event classes
- `booking-service/`: Write-side booking management (Port 8001, PostgreSQL)
- `carer-service/`: Write-side carer management (Port 8002, PostgreSQL)  
- `view-maintenance-service/`: Event processor (Port 8003, Redis projections)
- `read-api-service/`: Read-only API (Port 8004, Redis queries)

## Key Technologies
- Java 17+, Spring Boot 3.2, Gradle
- Apache Kafka for event streaming
- Redis for read projections
- PostgreSQL for write-side persistence
- Docker Compose for local development

## Code Guidelines
1. **Event Design**: Events should be immutable, strongly-typed, and contain all necessary data
2. **Service Isolation**: Write services never query read models; read services never write
3. **Idempotency**: All event handlers must be idempotent for replay scenarios
4. **Error Handling**: Use appropriate Spring Boot error handling patterns
5. **Testing**: Include unit tests for business logic and integration tests for APIs
6. **Configuration**: Use application.yml for configuration with environment-specific overrides

## Domain Rules
- Carer eligibility based on grade, qualifications, distance, visa status, and availability
- Booking lifecycle: OPEN → BOOKED/CANCELLED → COMPLETED
- Real-time projections maintained in Redis for fast read operations

## Development Workflow
1. Infrastructure: Start with `docker-compose up -d`
2. Build: Use `./gradlew build` for all modules
3. Run: Start services in order (writes first, then processors, then reads)
4. Test: Use provided curl examples for API validation

When generating code, follow Spring Boot best practices, maintain CQRS separation, and ensure proper event handling patterns.
