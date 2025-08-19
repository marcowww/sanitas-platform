# Healthcare Staffing CQRS Platform - API Test Collection

This collection provides comprehensive API testing examples for the Healthcare Staffing CQRS Platform.

## Prerequisites

1. Start the platform: `./start.sh`
2. Ensure all services are running on their respective ports
3. Wait for infrastructure to be fully initialized

## Environment Variables

```bash
# Base URLs
BOOKING_SERVICE_URL=http://localhost:8001
CARER_SERVICE_URL=http://localhost:8002
VIEW_MAINTENANCE_URL=http://localhost:8003
READ_API_URL=http://localhost:8004

# Test Data
FACILITY_ID=550e8400-e29b-41d4-a716-446655440000
CARER_ID=660e8400-e29b-41d4-a716-446655440001
BOOKING_ID=770e8400-e29b-41d4-a716-446655440002
```

## 1. Health Checks

### Check All Services
```bash
# Booking Service
curl -w "\\n%{http_code}\\n" http://localhost:8001/actuator/health

# Carer Service  
curl -w "\\n%{http_code}\\n" http://localhost:8002/actuator/health

# View Maintenance Service
curl -w "\\n%{http_code}\\n" http://localhost:8003/actuator/health

# Read API Service
curl -w "\\n%{http_code}\\n" http://localhost:8004/actuator/health
```

## 2. Carer Management (Write Operations)

### Create a Registered Nurse
```bash
curl -X POST http://localhost:8002/api/carers \\
  -H "Content-Type: application/json" \\
  -d '{
    "firstName": "Sarah",
    "lastName": "Johnson",
    "email": "sarah.johnson@example.com",
    "phone": "+44207123456",
    "location": "London",
    "grade": "RN",
    "qualifications": ["BLS", "ACLS", "PALS"],
    "visaStatus": "CITIZEN",
    "maxTravelDistance": 25
  }'
```

### Create a Healthcare Assistant
```bash
curl -X POST http://localhost:8002/api/carers \\
  -H "Content-Type: application/json" \\
  -d '{
    "firstName": "Michael",
    "lastName": "Brown",
    "email": "michael.brown@example.com",
    "phone": "+44207654321",
    "location": "Manchester",
    "grade": "HCA",
    "qualifications": ["BLS"],
    "visaStatus": "WORK_VISA",
    "maxTravelDistance": 30
  }'
```

### Update Carer Information
```bash
# Replace {carerId} with actual carer ID from creation response
curl -X PUT http://localhost:8002/api/carers/{carerId} \\
  -H "Content-Type: application/json" \\
  -d '{
    "updates": {
      "phone": "+44207999888",
      "maxTravelDistance": 35
    },
    "updateReason": "Contact information updated by carer"
  }'
```

### Update Carer Availability
```bash
curl -X POST http://localhost:8002/api/carers/{carerId}/availability \\
  -H "Content-Type: application/json" \\
  -d '{
    "availabilitySlots": [
      {
        "date": "2025-08-20",
        "startTime": "08:00",
        "endTime": "20:00",
        "available": true
      },
      {
        "date": "2025-08-21",
        "startTime": "08:00",
        "endTime": "16:00",
        "available": true
      },
      {
        "date": "2025-08-22",
        "startTime": "00:00",
        "endTime": "23:59",
        "available": false
      }
    ]
  }'
```

## 3. Booking Management (Write Operations)

### Create Day Shift Booking
```bash
curl -X POST http://localhost:8001/api/bookings \\
  -H "Content-Type: application/json" \\
  -d '{
    "facilityId": "550e8400-e29b-41d4-a716-446655440000",
    "shift": "DAY",
    "startTime": "2025-08-20T08:00:00",
    "endTime": "2025-08-20T20:00:00",
    "grade": "RN",
    "hourlyRate": 35.50,
    "location": "London",
    "specialRequirements": "ICU experience preferred",
    "requiredQualifications": ["BLS", "ACLS"]
  }'
```

### Create Night Shift Booking
```bash
curl -X POST http://localhost:8001/api/bookings \\
  -H "Content-Type: application/json" \\
  -d '{
    "facilityId": "550e8400-e29b-41d4-a716-446655440000",
    "shift": "NIGHT",
    "startTime": "2025-08-20T20:00:00",
    "endTime": "2025-08-21T08:00:00",
    "grade": "HCA",
    "hourlyRate": 18.75,
    "location": "Manchester",
    "specialRequirements": "Must have experience with elderly patients",
    "requiredQualifications": ["BLS"]
  }'
```

### Update Booking
```bash
curl -X PUT http://localhost:8001/api/bookings/{bookingId} \\
  -H "Content-Type: application/json" \\
  -d '{
    "updates": {
      "hourlyRate": 37.50,
      "specialRequirements": "ICU experience required, COVID vaccination mandatory"
    },
    "modificationReason": "Rate increase and additional safety requirements"
  }'
```

### Assign Carer to Booking
```bash
curl -X POST http://localhost:8001/api/bookings/{bookingId}/book \\
  -H "Content-Type: application/json" \\
  -d '{
    "carerId": "{carerId}",
    "bookedBy": "scheduler@hospital.com"
  }'
```

### Cancel Booking
```bash
curl -X POST http://localhost:8001/api/bookings/{bookingId}/cancel \\
  -H "Content-Type: application/json" \\
  -d '{
    "cancellationReason": "Shift no longer required due to low patient volume",
    "cancelledBy": "manager@hospital.com"
  }'
```

### Carer Pullout
```bash
curl -X POST http://localhost:8001/api/bookings/{bookingId}/pullout \\
  -H "Content-Type: application/json" \\
  -d '{
    "carerId": "{carerId}",
    "pulloutReason": "Family emergency",
    "pulloutBy": "carer@example.com"
  }'
```

## 4. Read Operations (Queries)

### Get Eligible Shifts for Carer
```bash
# Basic query
curl -w "\\n" http://localhost:8004/api/read/carer/{carerId}/eligible-shifts

# With distance filter
curl -w "\\n" "http://localhost:8004/api/read/carer/{carerId}/eligible-shifts?maxDistance=20"

# Sorted by distance
curl -w "\\n" "http://localhost:8004/api/read/carer/{carerId}/eligible-shifts?sortByDistance=true"
```

### Get Eligible Carers for Shift
```bash
# Basic query
curl -w "\\n" http://localhost:8004/api/read/shift/{shiftId}/eligible-carers

# With distance filter
curl -w "\\n" "http://localhost:8004/api/read/shift/{shiftId}/eligible-carers?maxDistance=15"

# Sorted by distance
curl -w "\\n" "http://localhost:8004/api/read/shift/{shiftId}/eligible-carers?sortByDistance=true"
```

### Filter by Location
```bash
curl -w "\\n" http://localhost:8004/api/read/carer/{carerId}/eligible-shifts/location/London
```

### Filter by Grade
```bash
curl -w "\\n" http://localhost:8004/api/read/shift/{shiftId}/eligible-carers/grade/RN
```

### Check Specific Eligibility
```bash
curl -w "\\n" http://localhost:8004/api/read/carer/{carerId}/shift/{shiftId}/eligible
```

### Get Counts
```bash
# Count eligible shifts for carer
curl -w "\\n" http://localhost:8004/api/read/carer/{carerId}/eligible-shifts/count

# Count eligible carers for shift
curl -w "\\n" http://localhost:8004/api/read/shift/{shiftId}/eligible-carers/count
```

## 5. Monitoring and Debugging

### Check Kafka Topics (via Kafka UI)
```bash
open http://localhost:9080
```

### Check Redis Data (via Redis Commander)
```bash
open http://localhost:9081
```

### View Service Metrics
```bash
# Booking Service metrics
curl http://localhost:8001/actuator/metrics

# View Maintenance Service metrics  
curl http://localhost:8003/actuator/metrics

# Read API Service metrics
curl http://localhost:8004/actuator/metrics
```

### Check Application Logs
```bash
# View real-time logs
tail -f logs/booking-service.log
tail -f logs/carer-service.log
tail -f logs/view-maintenance-service.log
tail -f logs/read-api-service.log
```

## 6. Load Testing Scenarios

### Scenario 1: High Carer Registration
```bash
# Create multiple carers rapidly
for i in {1..10}; do
  curl -X POST http://localhost:8002/api/carers \\
    -H "Content-Type: application/json" \\
    -d "{
      \"firstName\": \"Carer$i\",
      \"lastName\": \"Test\",
      \"email\": \"carer$i@example.com\",
      \"phone\": \"+4420700000$i\",
      \"location\": \"London\",
      \"grade\": \"RN\",
      \"qualifications\": [\"BLS\"],
      \"visaStatus\": \"CITIZEN\",
      \"maxTravelDistance\": 25
    }" &
done
wait
```

### Scenario 2: High Booking Creation
```bash
# Create multiple bookings rapidly
for i in {1..10}; do
  curl -X POST http://localhost:8001/api/bookings \\
    -H "Content-Type: application/json" \\
    -d "{
      \"facilityId\": \"550e8400-e29b-41d4-a716-446655440000\",
      \"shift\": \"DAY\",
      \"startTime\": \"2025-08-2${i}T08:00:00\",
      \"endTime\": \"2025-08-2${i}T20:00:00\",
      \"grade\": \"RN\",
      \"hourlyRate\": 35.00,
      \"location\": \"London\",
      \"requiredQualifications\": [\"BLS\"]
    }" &
done
wait
```

## 7. Error Testing

### Invalid Data Tests
```bash
# Invalid carer data
curl -X POST http://localhost:8002/api/carers \\
  -H "Content-Type: application/json" \\
  -d '{
    "firstName": "",
    "email": "invalid-email",
    "grade": "INVALID_GRADE"
  }'

# Invalid booking data
curl -X POST http://localhost:8001/api/bookings \\
  -H "Content-Type: application/json" \\
  -d '{
    "facilityId": "invalid-uuid",
    "hourlyRate": -10.00
  }'
```

### Non-existent Resource Tests
```bash
# Query non-existent carer
curl http://localhost:8004/api/read/carer/99999999-9999-9999-9999-999999999999/eligible-shifts

# Update non-existent booking
curl -X PUT http://localhost:8001/api/bookings/99999999-9999-9999-9999-999999999999 \\
  -H "Content-Type: application/json" \\
  -d '{"updates": {}, "modificationReason": "test"}'
```

## 8. Integration Testing

### End-to-End Workflow
```bash
#!/bin/bash
echo "Starting end-to-end test..."

# 1. Create carer
CARER_RESPONSE=$(curl -s -X POST http://localhost:8002/api/carers \\
  -H "Content-Type: application/json" \\
  -d '{
    "firstName": "TestCarer",
    "lastName": "E2E",
    "email": "test.e2e@example.com",
    "phone": "+44123456789",
    "location": "London",
    "grade": "RN",
    "qualifications": ["BLS"],
    "visaStatus": "CITIZEN",
    "maxTravelDistance": 50
  }')

CARER_ID=$(echo $CARER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Created carer: $CARER_ID"

# 2. Create booking
BOOKING_RESPONSE=$(curl -s -X POST http://localhost:8001/api/bookings \\
  -H "Content-Type: application/json" \\
  -d '{
    "facilityId": "550e8400-e29b-41d4-a716-446655440000",
    "shift": "DAY",
    "startTime": "2025-08-25T08:00:00",
    "endTime": "2025-08-25T20:00:00",
    "grade": "RN",
    "hourlyRate": 35.00,
    "location": "London",
    "requiredQualifications": ["BLS"]
  }')

BOOKING_ID=$(echo $BOOKING_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Created booking: $BOOKING_ID"

# 3. Wait for projections to update
echo "Waiting for projections to update..."
sleep 5

# 4. Check eligibility
ELIGIBLE_SHIFTS=$(curl -s "http://localhost:8004/api/read/carer/$CARER_ID/eligible-shifts")
echo "Eligible shifts: $ELIGIBLE_SHIFTS"

ELIGIBLE_CARERS=$(curl -s "http://localhost:8004/api/read/shift/$BOOKING_ID/eligible-carers")
echo "Eligible carers: $ELIGIBLE_CARERS"

echo "End-to-end test completed!"
```

---

**💡 Tips:**
- Use `jq` to format JSON responses: `curl ... | jq .`
- Monitor logs during testing: `tail -f logs/*.log`
- Check Redis data in real-time via Redis Commander
- Use Kafka UI to monitor event flow
- Replace placeholder IDs with actual values from responses
