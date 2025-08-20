#!/Users/marco/BookingSystemCQRS/.venv/bin/python
"""
Healthcare Staffing CQRS Platform - Test Data Generator
Generates 100 carers and 1000 bookings with realistic data
"""

import json
import random
import requests
import time
from datetime import datetime, timedelta
from typing import List, Dict, Any

# Configuration
CARER_SERVICE_URL = "http://localhost:8002"
BOOKING_SERVICE_URL = "http://localhost:8001"
READ_API_URL = "http://localhost:8004"
NUM_CARERS = 100
NUM_BOOKINGS = 1000

# Sample data for generation
FIRST_NAMES = [
    "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas",
    "Christopher", "Daniel", "Paul", "Mark", "Donald", "Steven", "Andrew", "Kenneth", "Joshua", "Kevin",
    "Brian", "George", "Timothy", "Ronald", "Jason", "Edward", "Jeffrey", "Ryan", "Jacob", "Nicholas",
    "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen",
    "Lisa", "Nancy", "Betty", "Helen", "Sandra", "Donna", "Carol", "Ruth", "Sharon", "Michelle",
    "Laura", "Kimberly", "Deborah", "Dorothy", "Amy", "Angela", "Ashley", "Brenda", "Emma", "Olivia"
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
    "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
    "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts"
]

LOCATIONS = [
    "London", "Manchester", "Birmingham", "Leeds", "Glasgow", "Liverpool", "Newcastle", "Sheffield",
    "Bristol", "Edinburgh", "Leicester", "Coventry", "Bradford", "Cardiff", "Belfast", "Nottingham",
    "Plymouth", "Stoke-on-Trent", "Wolverhampton", "Derby", "Southampton", "Portsmouth", "York", "Dundee",
    "Brighton", "Blackpool", "Middlesbrough", "Bolton", "Bournemouth", "Norwich", "Oldham", "Swindon"
]

GRADES = ["HCA", "RN", "SN", "RMN", "RGN"]

QUALIFICATIONS = [
    "BLS", "ALS", "CPR", "First Aid", "Mental Health", "Dementia Care", "Medication Management",
    "Infection Control", "Manual Handling", "Safeguarding", "NVQ Level 2", "NVQ Level 3"
]

VISA_STATUSES = ["BRITISH_CITIZEN", "PERMANENT_RESIDENT", "WORK_VISA", "STUDENT_VISA"]

FACILITY_NAMES = [
    "General Hospital", "Medical Centre", "Care Home", "Community Hospital", "Specialist Clinic",
    "Rehabilitation Center", "Mental Health Unit", "Nursing Home", "Day Care Centre", "Private Hospital"
]

def print_colored(text: str, color: str = ""):
    """Print colored text to console"""
    colors = {
        "red": "\033[0;31m",
        "green": "\033[0;32m",
        "yellow": "\033[1;33m",
        "blue": "\033[0;34m",
        "reset": "\033[0m"
    }
    if color in colors:
        print(f"{colors[color]}{text}{colors['reset']}")
    else:
        print(text)

def check_services() -> bool:
    """Check if all required services are running"""
    print_colored("🔍 Checking if services are running...", "blue")
    
    services = [
        (CARER_SERVICE_URL, "Carer Service"),
        (BOOKING_SERVICE_URL, "Booking Service"),
        (READ_API_URL, "Read API Service")
    ]
    
    for url, name in services:
        try:
            response = requests.get(f"{url}/actuator/health", timeout=5)
            if response.status_code == 200:
                print_colored(f"✓ {name} is running", "green")
            else:
                print_colored(f"❌ {name} is not healthy (status: {response.status_code})", "red")
                return False
        except requests.exceptions.RequestException:
            print_colored(f"❌ {name} is not running at {url}", "red")
            print_colored("Please start the services using: ./start.sh", "yellow")
            return False
    
    return True

def generate_random_qualifications() -> List[str]:
    """Generate 1-4 random qualifications for a carer"""
    num_quals = random.randint(1, 4)
    return random.sample(QUALIFICATIONS, num_quals)

def generate_future_datetime() -> str:
    """Generate a random datetime in the next 30 days"""
    days_ahead = random.randint(1, 30)
    hours = random.randint(6, 22)  # Working hours
    minutes = random.choice([0, 15, 30, 45])  # Quarter-hour increments
    
    future_date = datetime.now() + timedelta(days=days_ahead)
    future_date = future_date.replace(hour=hours, minute=minutes, second=0, microsecond=0)
    
    return future_date.isoformat()

def create_carer() -> str:
    """Create a random carer and return the carer ID"""
    first_name = random.choice(FIRST_NAMES)
    last_name = random.choice(LAST_NAMES)
    email = f"{first_name.lower()}.{last_name.lower()}@example.com"
    phone = f"+44{random.randint(1000000000, 9999999999)}"
    location = random.choice(LOCATIONS)
    grade = random.choice(GRADES)
    qualifications = generate_random_qualifications()
    visa_status = random.choice(VISA_STATUSES)
    max_distance = random.randint(10, 110)
    
    carer_data = {
        "firstName": first_name,
        "lastName": last_name,
        "email": email,
        "phone": phone,
        "location": location,
        "grade": grade,
        "qualifications": qualifications,
        "visaStatus": visa_status,
        "maxTravelDistance": max_distance
    }
    
    try:
        response = requests.post(
            f"{CARER_SERVICE_URL}/api/carers",
            json=carer_data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        
        if response.status_code in [200, 201]:
            return response.json().get("id", "")
        else:
            print_colored(f"Failed to create carer: {response.status_code} - {response.text}", "red")
            return ""
    except requests.exceptions.RequestException as e:
        print_colored(f"Error creating carer: {e}", "red")
        return ""

def create_booking() -> str:
    """Create a random booking and return the booking ID"""
    import uuid
    
    location = random.choice(LOCATIONS)
    facility_id = str(uuid.uuid4())  # Generate random facility ID
    grade = random.choice(GRADES)
    shift = random.choice(["DAY", "NIGHT", "EARLY", "LATE"])
    start_time = generate_future_datetime()
    hourly_rate = round(random.uniform(15.0, 45.0), 2)
    duration_hours = random.randint(4, 12)
    
    # Calculate end time
    start_dt = datetime.fromisoformat(start_time)
    end_dt = start_dt + timedelta(hours=duration_hours)
    end_time = end_dt.isoformat()
    
    # Generate realistic requirements
    special_requirements = random.choice([
        "ICU experience preferred",
        "Experience with elderly patients",
        "Mental health experience required",
        "Pediatric experience preferred",
        "Dementia care experience",
        "Previous A&E experience",
        "Wound care experience",
        "Medication management skills",
        None
    ])
    
    # Generate required qualifications based on grade
    if grade in ["RN", "RGN"]:
        required_quals = random.sample(["BLS", "ALS", "First Aid", "Medication Management"], k=random.randint(1, 3))
    elif grade == "HCA":
        required_quals = random.sample(["BLS", "First Aid", "Manual Handling"], k=random.randint(1, 2))
    else:
        required_quals = random.sample(QUALIFICATIONS[:6], k=random.randint(1, 2))
    
    booking_data = {
        "facilityId": facility_id,
        "shift": shift,
        "startTime": start_time,
        "endTime": end_time,
        "grade": grade,
        "hourlyRate": hourly_rate,
        "location": location,
        "specialRequirements": special_requirements,
        "requiredQualifications": required_quals
    }
    
    try:
        response = requests.post(
            f"{BOOKING_SERVICE_URL}/api/bookings",
            json=booking_data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        
        if response.status_code in [200, 201]:
            return response.json().get("id", "")
        else:
            print_colored(f"Failed to create booking: {response.status_code} - {response.text}", "red")
            return ""
    except requests.exceptions.RequestException as e:
        print_colored(f"Error creating booking: {e}", "red")
        return ""

def get_all_carers() -> List[str]:
    """Get all carer IDs from the carer service"""
    try:
        response = requests.get(f"{CARER_SERVICE_URL}/api/carers", timeout=10)
        if response.status_code == 200:
            carers = response.json()
            return [carer.get("id") for carer in carers if carer.get("id")]
        return []
    except requests.exceptions.RequestException:
        return []

def get_all_bookings() -> List[str]:
    """Get all booking IDs from the booking service"""
    try:
        response = requests.get(f"{BOOKING_SERVICE_URL}/api/bookings", timeout=10)
        if response.status_code == 200:
            bookings = response.json()
            return [booking.get("id") for booking in bookings if booking.get("id")]
        return []
    except requests.exceptions.RequestException:
        return []

def assign_random_bookings():
    # Booking assignment removed: this function intentionally left blank
    return

def main():
    """Main execution function"""
    print_colored("🧪 Healthcare Staffing CQRS Test Data Generator", "blue")
    print_colored("=" * 50, "blue")
    
    print(f"\nThis script will generate:")
    print(f"  📋 {NUM_CARERS} carers")
    print(f"  🏥 {NUM_BOOKINGS} bookings")
    print(f"  🔗 Random assignments")
    
    response = input("\nContinue? (y/N): ").strip().lower()
    if response != 'y':
        print_colored("Cancelled", "yellow")
        return
    
    if not check_services():
        return
    
    # Step 1: Create carers
    print_colored(f"\n👥 Step 1: Creating {NUM_CARERS} carers...", "blue")
    created_carers = 0
    carer_ids = []
    
    for i in range(1, NUM_CARERS + 1):
        carer_id = create_carer()
        if carer_id:
            created_carers += 1
            carer_ids.append(carer_id)
        
        if i % 10 == 0:
            print_colored(f"  ✓ Created {i} carers", "green")
        
        time.sleep(0.1)  # Small delay to avoid overwhelming the system
    
    print_colored(f"✓ Created {created_carers} carers", "green")
    
    # Step 2: Create bookings
    print_colored(f"\n🏥 Step 2: Creating {NUM_BOOKINGS} bookings...", "blue")
    created_bookings = 0
    
    for i in range(1, NUM_BOOKINGS + 1):
        booking_id = create_booking()
        if booking_id:
            created_bookings += 1
        
        if i % 50 == 0:
            print_colored(f"  ✓ Created {i} bookings", "green")
        
        time.sleep(0.05)  # Small delay to avoid overwhelming the system
    
    print_colored(f"✓ Created {created_bookings} bookings", "green")
    
    # Wait for events to be processed
    print_colored("\n⏳ Waiting for events to be processed...", "blue")
    time.sleep(5)
    
    # Summary
    print_colored("\n🎉 Test data generation completed!", "green")
    print_colored("\n📊 Summary:", "blue")
    print(f"  👥 Carers created: {created_carers}")
    print(f"   Bookings created: {created_bookings}")
    
    print_colored("\n🧪 You can now test the system with:", "blue")
    print(f"  curl {READ_API_URL}/api/read/carers")
    print(f"  curl {READ_API_URL}/api/read/bookings")
    print(f"  curl {READ_API_URL}/api/read/carer/{{carerId}}/eligible-shifts")
    print(f"  curl {READ_API_URL}/api/read/shift/{{shiftId}}/eligible-carers")
    
    print_colored("\n🔧 Access development tools:", "blue")
    print("  📊 Kafka UI: http://localhost:9080")
    print("  🔴 Redis Commander: http://localhost:9081")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print_colored("\n\nOperation cancelled by user", "yellow")
    except Exception as e:
        print_colored(f"\nError: {e}", "red")
