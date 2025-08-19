#!/bin/bash

# Healthcare Staffing CQRS Platform - Test Data Generator
# Generates 100 carers and 1000 bookings with realistic data

set -e

echo "🧪 Healthcare Staffing CQRS Test Data Generator"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CARER_SERVICE_URL="http://localhost:8002"
BOOKING_SERVICE_URL="http://localhost:8001"
READ_API_URL="http://localhost:8004"
NUM_CARERS=1
NUM_BOOKINGS=1

# Arrays for random data generation
FIRST_NAMES=("James" "John" "Robert" "Michael" "William" "David" "Richard" "Charles" "Joseph" "Thomas" 
             "Christopher" "Daniel" "Paul" "Mark" "Donald" "Steven" "Andrew" "Kenneth" "Paul" "Joshua"
             "Kevin" "Brian" "George" "Timothy" "Ronald" "Jason" "Edward" "Jeffrey" "Ryan" "Jacob"
             "Mary" "Patricia" "Jennifer" "Linda" "Elizabeth" "Barbara" "Susan" "Jessica" "Sarah" "Karen"
             "Lisa" "Nancy" "Betty" "Helen" "Sandra" "Donna" "Carol" "Ruth" "Sharon" "Michelle"
             "Laura" "Sarah" "Kimberly" "Deborah" "Dorothy" "Lisa" "Nancy" "Karen" "Betty" "Helen")

LAST_NAMES=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez"
            "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin"
            "Lee" "Perez" "Thompson" "White" "Harris" "Sanchez" "Clark" "Ramirez" "Lewis" "Robinson"
            "Walker" "Young" "Allen" "King" "Wright" "Scott" "Torres" "Nguyen" "Hill" "Flores"
            "Green" "Adams" "Nelson" "Baker" "Hall" "Rivera" "Campbell" "Mitchell" "Carter" "Roberts")

LOCATIONS=("London" "Manchester" "Birmingham" "Leeds" "Glasgow" "Liverpool" "Newcastle" "Sheffield" 
           "Bristol" "Edinburgh" "Leicester" "Coventry" "Bradford" "Cardiff" "Belfast" "Nottingham"
           "Plymouth" "Stoke-on-Trent" "Wolverhampton" "Derby" "Southampton" "Portsmouth" "York" "Dundee"
           "Brighton" "Blackpool" "Middlesbrough" "Bolton" "Bournemouth" "Norwich" "Oldham" "Swindon")

GRADES=("HCA" "RN" "SN" "RMN" "RGN")
QUALIFICATIONS=("BLS" "ALS" "CPR" "First Aid" "Mental Health" "Dementia Care" "Medication Management" 
                "Infection Control" "Manual Handling" "Safeguarding" "NVQ Level 2" "NVQ Level 3")
VISA_STATUSES=("CITIZEN" "PERMANENT_RESIDENT" "WORK_VISA" "STUDENT_VISA")

FACILITY_NAMES=("General Hospital" "Medical Centre" "Care Home" "Community Hospital" "Specialist Clinic"
                "Rehabilitation Center" "Mental Health Unit" "Nursing Home" "Day Care Centre" "Private Hospital")

# Function to get random element from array
get_random_element() {
    local arr=("$@")
    local index=$((RANDOM % ${#arr[@]}))
    echo "${arr[$index]}"
}

# Function to get random qualifications (1-4 qualifications per carer)
get_random_qualifications() {
    local num_quals=$((RANDOM % 4 + 1))
    local selected_quals=()
    local temp_quals=("${QUALIFICATIONS[@]}")
    
    for ((i=0; i<num_quals; i++)); do
        if [ ${#temp_quals[@]} -eq 0 ]; then break; fi
        local idx=$((RANDOM % ${#temp_quals[@]}))
        selected_quals+=("\"${temp_quals[$idx]}\"")
        # Remove selected qualification to avoid duplicates
        temp_quals=("${temp_quals[@]:0:$idx}" "${temp_quals[@]:$((idx+1))}")
    done
    
    echo "[$(IFS=','; echo "${selected_quals[*]}")]"
}

# Function to generate random date in the future (next 30 days)
get_random_future_date() {
    local days_ahead=$((RANDOM % 30 + 1))
    local future_date=$(date -v+${days_ahead}d +"%Y-%m-%dT%H:%M:%S")
    echo "$future_date"
}

# Function to generate random hourly rate
get_random_hourly_rate() {
    local base_rate=$((RANDOM % 20 + 15)) # £15-£35
    local decimal=$((RANDOM % 100))
    printf "%.2f" "$base_rate.$decimal"
}

# Function to check if services are running
check_services() {
    echo -e "${BLUE}Checking if services are running...${NC}"
    
    if ! curl -s "$CARER_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}❌ Carer Service is not running at $CARER_SERVICE_URL${NC}"
        echo -e "${YELLOW}Please start the services using: ./start.sh${NC}"
        exit 1
    fi
    
    if ! curl -s "$BOOKING_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}❌ Booking Service is not running at $BOOKING_SERVICE_URL${NC}"
        echo -e "${YELLOW}Please start the services using: ./start.sh${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ All services are running${NC}"
}

# Function to create a carer
create_carer() {
    local first_name=$(get_random_element "${FIRST_NAMES[@]}")
    local last_name=$(get_random_element "${LAST_NAMES[@]}")
    local email="$(echo $first_name | tr '[:upper:]' '[:lower:]').$(echo $last_name | tr '[:upper:]' '[:lower:]')@example.com"
    local phone="+44$(printf "%09d" $((RANDOM % 1000000000)))"
    local location=$(get_random_element "${LOCATIONS[@]}")
    local grade=$(get_random_element "${GRADES[@]}")
    local qualifications=$(get_random_qualifications)
    local visa_status=$(get_random_element "${VISA_STATUSES[@]}")
    local max_distance=$((RANDOM % 100 + 10)) # 10-110 km
    
    local carer_data="{
        \"firstName\": \"$first_name\",
        \"lastName\": \"$last_name\",
        \"email\": \"$email\",
        \"phone\": \"$phone\",
        \"location\": \"$location\",
        \"grade\": \"$grade\",
        \"qualifications\": $qualifications,
        \"visaStatus\": \"$visa_status\",
        \"maxTravelDistance\": $max_distance
    }"
    
    local response=$(curl -s -X POST "$CARER_SERVICE_URL/api/carers" \
        -H "Content-Type: application/json" \
        -d "$carer_data")
    
    echo "DEBUG: Carer creation response: $response" >&2
    
    # Extract carer ID from response
    echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4
}

# Function to generate random UUID
generate_uuid() {
    python3 -c "import uuid; print(uuid.uuid4())"
}

# Function to get random required qualifications (1-3 qualifications per booking)
get_random_required_qualifications() {
    local num_quals=$((RANDOM % 3 + 1))
    local selected_quals=()
    local temp_quals=("BLS" "First Aid" "CPR" "ALS" "Mental Health")
    
    for ((i=0; i<num_quals; i++)); do
        if [ ${#temp_quals[@]} -eq 0 ]; then break; fi
        local idx=$((RANDOM % ${#temp_quals[@]}))
        selected_quals+=("\"${temp_quals[$idx]}\"")
        # Remove selected qualification to avoid duplicates
        temp_quals=("${temp_quals[@]:0:$idx}" "${temp_quals[@]:$((idx+1))}")
    done
    
    echo "[$(IFS=','; echo "${selected_quals[*]}")]"
}

# Function to create a booking
create_booking() {
    local location=$(get_random_element "${LOCATIONS[@]}")
    local facility_id=$(generate_uuid)
    local grade=$(get_random_element "${GRADES[@]}")
    local shift=$(get_random_element "DAY" "NIGHT" "EARLY" "LATE")
    local start_time=$(get_random_future_date)
    local hourly_rate=$(get_random_hourly_rate)
    local duration=$((RANDOM % 8 + 4)) # 4-12 hours
    local required_qualifications=$(get_random_required_qualifications)
    
    # Calculate end time
    local end_time=$(date -j -v+${duration}H -f "%Y-%m-%dT%H:%M:%S" "$start_time" +"%Y-%m-%dT%H:%M:%S")
    
    local booking_data="{
        \"facilityId\": \"$facility_id\",
        \"shift\": \"$shift\",
        \"startTime\": \"$start_time\",
        \"endTime\": \"$end_time\",
        \"grade\": \"$grade\",
        \"hourlyRate\": $hourly_rate,
        \"location\": \"$location\",
        \"specialRequirements\": \"Experience preferred\",
        \"requiredQualifications\": $required_qualifications
    }"
    
    local response=$(curl -s -X POST "$BOOKING_SERVICE_URL/api/bookings" \
        -H "Content-Type: application/json" \
        -d "$booking_data")
    
    echo "DEBUG: Booking creation response: $response" >&2
    
    # Extract booking ID from response
    echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4
}

# Function to randomly assign some bookings to carers
assign_random_bookings() {
    echo -e "\n${BLUE}Step 3: Randomly assigning some bookings to carers...${NC}"
    
    # Get list of carers
    local carers=$(curl -s "$READ_API_URL/api/read/carers" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    local carer_array=($carers)
    
    # Get list of bookings
    local bookings=$(curl -s "$READ_API_URL/api/read/bookings" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    local booking_array=($bookings)
    
    if [ ${#carer_array[@]} -eq 0 ] || [ ${#booking_array[@]} -eq 0 ]; then
        echo -e "${YELLOW}⚠ No carers or bookings found for assignment${NC}"
        return
    fi
    
    # Assign 20% of bookings randomly
    local assignments_to_make=$((${#booking_array[@]} / 5))
    local assignments_made=0
    
    for booking_id in "${booking_array[@]}"; do
        if [ $assignments_made -ge $assignments_to_make ]; then
            break
        fi
        
        # 20% chance to assign this booking
        if [ $((RANDOM % 5)) -eq 0 ]; then
            local random_carer_idx=$((RANDOM % ${#carer_array[@]}))
            local carer_id="${carer_array[$random_carer_idx]}"
            
            curl -s -X POST "$BOOKING_SERVICE_URL/api/bookings/$booking_id/book" \
                -H "Content-Type: application/json" \
                -d "{\"carerId\": \"$carer_id\", \"bookedBy\": \"test-script@example.com\"}" > /dev/null
            
            assignments_made=$((assignments_made + 1))
            
            if [ $((assignments_made % 10)) -eq 0 ]; then
                echo -e "${GREEN}  ✓ Assigned $assignments_made bookings${NC}"
            fi
        fi
    done
    
    echo -e "${GREEN}✓ Assigned $assignments_made bookings to carers${NC}"
}

# Main execution
main() {
    echo -e "\n${BLUE}This script will generate:${NC}"
    echo -e "  📋 $NUM_CARERS carers"
    echo -e "  🏥 $NUM_BOOKINGS bookings"
    echo -e "  🔗 Random assignments"
    echo
    
    read -p "Continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Cancelled${NC}"
        exit 0
    fi
    
    check_services
    
    echo -e "\n${BLUE}Step 1: Creating $NUM_CARERS carers...${NC}"
    local created_carers=0
    
    for ((i=1; i<=NUM_CARERS; i++)); do
        local carer_id=$(create_carer)
        if [ -n "$carer_id" ]; then
            created_carers=$((created_carers + 1))
        fi
        
        # Progress indicator
        if [ $((i % 10)) -eq 0 ]; then
            echo -e "${GREEN}  ✓ Created $i carers${NC}"
        fi
        
        # Small delay to avoid overwhelming the system
        sleep 0.1
    done
    
    echo -e "${GREEN}✓ Created $created_carers carers${NC}"
    
    echo -e "\n${BLUE}Step 2: Creating $NUM_BOOKINGS bookings...${NC}"
    local created_bookings=0
    
    for ((i=1; i<=NUM_BOOKINGS; i++)); do
        local booking_id=$(create_booking)
        if [ -n "$booking_id" ]; then
            created_bookings=$((created_bookings + 1))
        fi
        
        # Progress indicator
        if [ $((i % 50)) -eq 0 ]; then
            echo -e "${GREEN}  ✓ Created $i bookings${NC}"
        fi
        
        # Small delay to avoid overwhelming the system
        sleep 0.05
    done
    
    echo -e "${GREEN}✓ Created $created_bookings bookings${NC}"
    
    # Wait a bit for events to be processed
    echo -e "\n${BLUE}Waiting for events to be processed...${NC}"
    sleep 5
    
    assign_random_bookings
    
    echo -e "\n${GREEN}🎉 Test data generation completed!${NC}"
    echo -e "\n${BLUE}Summary:${NC}"
    echo -e "  👥 Carers created: $created_carers"
    echo -e "  📋 Bookings created: $created_bookings"
    echo
    echo -e "${BLUE}You can now test the system with:${NC}"
    echo -e "  curl $READ_API_URL/api/read/carers"
    echo -e "  curl $READ_API_URL/api/read/bookings"
    echo -e "  curl $READ_API_URL/api/read/carer/{carerId}/eligible-shifts"
    echo -e "  curl $READ_API_URL/api/read/shift/{shiftId}/eligible-carers"
    echo
    echo -e "${BLUE}Access development tools:${NC}"
    echo -e "  📊 Kafka UI: http://localhost:9080"
    echo -e "  🔴 Redis Commander: http://localhost:9081"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
