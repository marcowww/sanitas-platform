#!/bin/bash

# Healthcare Staffing CQRS Platform - Stop Script

echo "🛑 Stopping Healthcare Staffing CQRS Platform"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to stop a service
stop_service() {
    local service=$1
    local pid_file="pids/$service.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null; then
            echo -e "${YELLOW}Stopping $service (PID: $pid)...${NC}"
            kill $pid
            sleep 2
            
            # Force kill if still running
            if ps -p $pid > /dev/null; then
                echo -e "${YELLOW}Force stopping $service...${NC}"
                kill -9 $pid
            fi
            
            echo -e "${GREEN}✓ $service stopped${NC}"
        else
            echo -e "${YELLOW}⚠ $service was not running${NC}"
        fi
        
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠ No PID file found for $service${NC}"
    fi
}

# Parse input argument: no arg or 'all' => stop everything; otherwise stop named service
TARGET="$1"
if [ -z "$TARGET" ] || [ "$TARGET" = "all" ]; then
    MODE="all"
else
    MODE="single"
    SERVICE="$TARGET"
fi

# Ordered list of services to stop (preferred order)
SERVICES=(
  "read-api-service"
  "view-maintenance-service"
  "carer-service"
  "booking-service"
  "booking-orchestration-service"
)

if [ "$MODE" = "all" ]; then
    echo -e "\n${BLUE}Stopping application services...${NC}"
    for s in "${SERVICES[@]}"; do
        stop_service "$s"
    done

    # Clean up any remaining Java processes for this project
    echo -e "\n${BLUE}Cleaning up any remaining processes...${NC}"
    pkill -f "healthcare-staffing" 2>/dev/null || true

    # Stop infrastructure
    echo -e "\n${BLUE}Stopping infrastructure services...${NC}"
    docker-compose down

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Infrastructure services stopped${NC}"
    else
        echo -e "${RED}✗ Failed to stop some infrastructure services${NC}"
    fi

    # Clean up pid directory
    if [ -d "pids" ]; then
        rm -rf pids
    fi

    echo -e "\n${GREEN}🏁 All services stopped successfully${NC}"
    echo -e "${YELLOW}📋 Logs are preserved in the logs/ directory${NC}"
else
    echo -e "\n${BLUE}Stopping single service: $SERVICE${NC}"
    # Validate service name
    found=false
    for s in "${SERVICES[@]}"; do
        if [ "$s" = "$SERVICE" ]; then
            found=true
            break
        fi
    done

    if [ "$found" = false ]; then
        echo -e "${RED}✗ Unknown service: $SERVICE${NC}"
        echo -e "${YELLOW}Valid services: ${SERVICES[*]}${NC}"
        exit 1
    fi

    stop_service "$SERVICE"
    echo -e "\n${GREEN}✅ $SERVICE stopped${NC}"
    echo -e "${YELLOW}📋 Logs are preserved in the logs/ directory${NC}"
fi
