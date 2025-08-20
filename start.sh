#!/bin/bash

# Healthcare Staffing CQRS Platform - Startup Script

echo "🏥 Healthcare Staffing CQRS Platform"
echo "=================================="

# Set JAVA_HOME for consistency
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/24.0.2/libexec/openjdk.jdk/Contents/Home

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -i :$port >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is ready${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}Attempt $attempt/$max_attempts - $service_name not ready yet...${NC}"
        sleep 2
        ((attempt++))
    done
    
    echo -e "${RED}✗ $service_name failed to start within expected time${NC}"
    return 1
}

# Function to wait for a TCP service to be ready
wait_for_tcp_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        # Try netcat first, fallback to telnet-style test
        if nc -z "$host" "$port" 2>/dev/null || (exec 3<>/dev/tcp/"$host"/"$port") 2>/dev/null; then
            exec 3>&- 2>/dev/null  # Close the connection if opened
            echo -e "${GREEN}✓ $service_name is ready${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}Attempt $attempt/$max_attempts - $service_name not ready yet...${NC}"
        sleep 2
        ((attempt++))
    done
    
    echo -e "${RED}✗ $service_name failed to start within expected time${NC}"
    return 1
}

# Step 1: Check prerequisites
echo -e "\n${BLUE}Step 1: Checking prerequisites...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker is not installed${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}✗ Docker Compose is not installed${NC}"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites are met${NC}"

# Step 2: Start infrastructure
echo -e "\n${BLUE}Step 2: Starting infrastructure services...${NC}"

docker-compose up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Failed to start infrastructure services${NC}"
    echo -e "${YELLOW}Checking what went wrong...${NC}"
    docker-compose ps
    exit 1
fi

echo -e "${GREEN}✓ Infrastructure services started${NC}"

# Show container status
echo -e "\n${BLUE}Container Status:${NC}"
docker-compose ps

# Step 3: Wait for infrastructure to be ready
echo -e "\n${BLUE}Step 3: Waiting for infrastructure to be ready...${NC}"

# Wait for Kafka (TCP check)
wait_for_tcp_service "localhost" "9092" "Kafka"

# Wait for Redis (TCP check)
wait_for_tcp_service "localhost" "6379" "Redis"

# Wait for PostgreSQL databases (TCP check)
wait_for_tcp_service "localhost" "5432" "PostgreSQL (Booking)"
wait_for_tcp_service "localhost" "5433" "PostgreSQL (Carer)"

# Optional: Wait for development UIs (HTTP check)
echo -e "${YELLOW}Checking development tools (optional)...${NC}"
if curl -s "http://localhost:9080" >/dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka UI is ready at http://localhost:9080${NC}"
else
    echo -e "${YELLOW}⚠ Kafka UI not accessible (this is optional)${NC}"
fi

if curl -s "http://localhost:9081" >/dev/null 2>&1; then
    echo -e "${GREEN}✓ Redis Commander is ready at http://localhost:9081${NC}"
else
    echo -e "${YELLOW}⚠ Redis Commander not accessible (this is optional)${NC}"
fi

echo -e "${GREEN}✓ Infrastructure is ready${NC}"

# Step 4: Build the project
echo -e "\n${BLUE}Step 4: Building the project...${NC}"

./gradlew build -x test

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Project built successfully${NC}"

# Step 5: Start services
echo -e "\n${BLUE}Step 5: Starting application services...${NC}"

# Function to start a service in background
start_service() {
    local service=$1
    local port=$2
    
    if check_port $port; then
        echo -e "${YELLOW}⚠ Port $port is already in use, skipping $service${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Starting $service on port $port...${NC}"
    nohup ./gradlew :$service:bootRun > logs/$service.log 2>&1 &
    local pid=$!
    echo $pid > pids/$service.pid
    
    return 0
}

# Create log and pid directories
mkdir -p logs pids

# Start services in order
start_service "booking-service" 8001
start_service "carer-service" 8002
start_service "view-maintenance-service" 8003
start_service "read-api-service" 8004
start_service "booking-orchestration-service" 8005

# Step 6: Wait for services to be ready
echo -e "\n${BLUE}Step 6: Waiting for services to be ready...${NC}"

sleep 10  # Give services time to start

wait_for_service "http://localhost:8001/actuator/health" "Booking Service"
wait_for_service "http://localhost:8002/actuator/health" "Carer Service"
wait_for_service "http://localhost:8003/actuator/health" "View Maintenance Service"
wait_for_service "http://localhost:8004/actuator/health" "Read API Service"
wait_for_service "http://localhost:8005/actuator/health" "Booking Orchestration Service"

# Step 7: Display status
echo -e "\n${GREEN}🎉 Healthcare Staffing CQRS Platform is running!${NC}"
echo -e "\n${BLUE}Service URLs:${NC}"
echo -e "  📝 Booking Service:         http://localhost:8001"
echo -e "  👥 Carer Service:          http://localhost:8002"
echo -e "  ⚙️  View Maintenance:       http://localhost:8003"
echo -e "  📖 Read API:               http://localhost:8004"
echo -e "  🔀 Booking Orchestration:  http://localhost:8005"
echo -e "\n${BLUE}Development Tools:${NC}"
echo -e "  📊 Kafka UI:               http://localhost:9080"
echo -e "  🔴 Redis Commander:        http://localhost:9081"
echo -e "\n${BLUE}Health Checks:${NC}"
echo -e "  curl http://localhost:8001/actuator/health"
echo -e "  curl http://localhost:8002/actuator/health"
echo -e "  curl http://localhost:8003/actuator/health"
echo -e "  curl http://localhost:8004/actuator/health"
echo -e "  curl http://localhost:8005/actuator/health"

echo -e "\n${BLUE}Example API Calls:${NC}"
echo -e "  # Create a carer"
echo -e "  curl -X POST http://localhost:8002/api/carers \\"
echo -e "    -H 'Content-Type: application/json' \\"
echo -e "    -d '{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"phone\":\"+44123456789\",\"location\":\"London\",\"grade\":\"RN\",\"qualifications\":[\"BLS\"],\"visaStatus\":\"CITIZEN\",\"maxTravelDistance\":50}'"
echo -e "\n  # Assign carer to booking via orchestration"
echo -e "  curl -X POST http://localhost:8005/api/booking-orchestration/assign-carer \\"
echo -e "    -H 'Content-Type: application/json' \\"
echo -e "    -d '{\"bookingId\":\"<booking-id>\",\"carerId\":\"<carer-id>\",\"bookedBy\":\"admin\",\"startTime\":\"2023-12-01T09:00:00\",\"endTime\":\"2023-12-01T17:00:00\"}'"

echo -e "\n${YELLOW}📋 To stop all services, run: ./stop.sh${NC}"
echo -e "${YELLOW}📋 To view logs: tail -f logs/{service-name}.log${NC}"
echo -e "${YELLOW}📋 Check README.md for detailed API documentation${NC}"
