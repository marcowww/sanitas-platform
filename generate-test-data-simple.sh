#!/bin/bash

# Healthcare Staffing CQRS Platform - Simple Test Data Generator
# Creates 100 carers and 1000 bookings

set -e

# Configuration
CARER_SERVICE_URL="http://localhost:8002"
BOOKING_SERVICE_URL="http://localhost:8001"
NUM_CARERS=100
NUM_BOOKINGS=1000

echo "🧪 Healthcare Staffing CQRS Test Data Generator (Simple)"
echo "=================================================="
echo
echo "This script will generate:"
echo "  👥 $NUM_CARERS carers"
echo "  📋 $NUM_BOOKINGS bookings"
echo

read -p "Continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelled"
    exit 0
fi

# Check services
echo "🔍 Checking if services are running..."
if ! curl -s http://localhost:8002/actuator/health > /dev/null; then
    echo "❌ Carer Service is not running"
    exit 1
fi

if ! curl -s http://localhost:8001/actuator/health > /dev/null; then
    echo "❌ Booking Service is not running"
    exit 1
fi

echo "✓ All services are running"

# Arrays for sample data
FIRST_NAMES=("James" "John" "Robert" "Mary" "Patricia" "Jennifer" "Michael" "Linda" "William" "Elizabeth" "David" "Barbara" "Richard" "Susan" "Joseph" "Jessica" "Thomas" "Sarah" "Charles" "Karen")
LAST_NAMES=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin" "Lee")
LOCATIONS=("London" "Manchester" "Birmingham" "Leeds" "Glasgow" "Liverpool" "Newcastle" "Sheffield" "Bristol" "Edinburgh" "Leicester" "Coventry" "Bradford" "Cardiff" "Belfast" "Nottingham")
GRADES=("HCA" "RN" "SN" "RMN" "RGN")
VISA_STATUSES=("CITIZEN" "PERMANENT_RESIDENT" "WORK_VISA" "STUDENT_VISA")
SHIFTS=("DAY" "NIGHT" "EARLY" "LATE")

# Function to get random array element
get_random_element() {
    local arr=("$@")
    local random_index=$((RANDOM % ${#arr[@]}))
    echo "${arr[$random_index]}"
}

# Function to generate random UUID
generate_uuid() {
    python3 -c "import uuid; print(uuid.uuid4())"
}

# Function to generate future datetime
generate_future_datetime() {
    python3 -c "
import datetime, random
days = random.randint(1, 30)
hours = random.randint(6, 22)
minutes = random.choice([0, 15, 30, 45])
dt = datetime.datetime.now() + datetime.timedelta(days=days)
dt = dt.replace(hour=hours, minute=minutes, second=0, microsecond=0)
print(dt.isoformat())
"
}

echo
echo "👥 Creating $NUM_CARERS carers..."

for i in $(seq 1 $NUM_CARERS); do
    FIRST_NAME=$(get_random_element "${FIRST_NAMES[@]}")
    LAST_NAME=$(get_random_element "${LAST_NAMES[@]}")
    EMAIL="${FIRST_NAME,,}.${LAST_NAME,,}@example.com"
    PHONE="+44$(shuf -i 1000000000-9999999999 -n 1)"
    LOCATION=$(get_random_element "${LOCATIONS[@]}")
    GRADE=$(get_random_element "${GRADES[@]}")
    VISA_STATUS=$(get_random_element "${VISA_STATUSES[@]}")
    MAX_DISTANCE=$((RANDOM % 100 + 10))
    
    curl -s -X POST "$CARER_SERVICE_URL/api/carers" \
        -H "Content-Type: application/json" \
        -d "{
            \"firstName\": \"$FIRST_NAME\",
            \"lastName\": \"$LAST_NAME\",
            \"email\": \"$EMAIL\",
            \"phone\": \"$PHONE\",
            \"location\": \"$LOCATION\",
            \"grade\": \"$GRADE\",
            \"qualifications\": [\"BLS\", \"First Aid\"],
            \"visaStatus\": \"$VISA_STATUS\",
            \"maxTravelDistance\": $MAX_DISTANCE
        }" > /dev/null
    
    if [ $((i % 10)) -eq 0 ]; then
        echo "  ✓ Created $i carers"
    fi
done

echo "✓ Created $NUM_CARERS carers"

echo
echo "📋 Creating $NUM_BOOKINGS bookings..."

for i in $(seq 1 $NUM_BOOKINGS); do
    FACILITY_ID=$(generate_uuid)
    LOCATION=$(get_random_element "${LOCATIONS[@]}")
    GRADE=$(get_random_element "${GRADES[@]}")
    SHIFT=$(get_random_element "${SHIFTS[@]}")
    START_TIME=$(generate_future_datetime)
    HOURLY_RATE=$(python3 -c "import random; print(round(random.uniform(15.0, 45.0), 2))")
    
    # Calculate end time (4-12 hours later)
    DURATION=$((RANDOM % 9 + 4))
    END_TIME=$(python3 -c "
import datetime
start = datetime.datetime.fromisoformat('$START_TIME')
end = start + datetime.timedelta(hours=$DURATION)
print(end.isoformat())
")
    
    curl -s -X POST "$BOOKING_SERVICE_URL/api/bookings" \
        -H "Content-Type: application/json" \
        -d "{
            \"facilityId\": \"$FACILITY_ID\",
            \"shift\": \"$SHIFT\",
            \"startTime\": \"$START_TIME\",
            \"endTime\": \"$END_TIME\",
            \"grade\": \"$GRADE\",
            \"hourlyRate\": $HOURLY_RATE,
            \"location\": \"$LOCATION\",
            \"specialRequirements\": \"Experience preferred\",
            \"requiredQualifications\": [\"BLS\"]
        }" > /dev/null
    
    if [ $((i % 50)) -eq 0 ]; then
        echo "  ✓ Created $i bookings"
    fi
done

echo "✓ Created $NUM_BOOKINGS bookings"

echo
echo "🎉 Test data generation completed!"
echo
echo "📊 Summary:"
echo "  👥 Carers created: $NUM_CARERS"
echo "  📋 Bookings created: $NUM_BOOKINGS"
echo
echo "🧪 You can now test the system with:"
echo "  curl http://localhost:8004/api/read/carers"
echo "  curl http://localhost:8004/api/read/bookings"
echo
echo "🔧 Access development tools:"
echo "  📊 Kafka UI: http://localhost:9080"
echo "  🔴 Redis Commander: http://localhost:9081"
