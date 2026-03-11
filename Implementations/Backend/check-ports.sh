#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=================================="
echo "   MharRuengSang Port Status"
echo "=================================="
echo ""

# Function to check a port
check_port() {
    local port=$1
    local service_name=$2
    
    if lsof -i :$port > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Port $port ($service_name) - ${GREEN}RUNNING${NC}"
        # Show process details
        lsof -i :$port | grep LISTEN | awk '{print "  └─ Process: " $1 " (PID: " $2 ")"}'
    else
        echo -e "${RED}✗${NC} Port $port ($service_name) - ${RED}NOT RUNNING${NC}"
    fi
}

# Check all ports
check_port 8082 "Restaurant Service"
check_port 8081 "Order Service"
check_port 8080 "API Gateway"

echo ""
echo "=================================="

# Check PID files
echo ""
echo "PID Files Status:"
if [ -f "logs/restaurant-service.pid" ]; then
    pid=$(cat logs/restaurant-service.pid)
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Restaurant Service PID: $pid (running)"
    else
        echo -e "${YELLOW}⚠${NC} Restaurant Service PID: $pid (not running - stale PID file)"
    fi
else
    echo "  No restaurant-service.pid file"
fi

if [ -f "logs/order-service.pid" ]; then
    pid=$(cat logs/order-service.pid)
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Order Service PID: $pid (running)"
    else
        echo -e "${YELLOW}⚠${NC} Order Service PID: $pid (not running - stale PID file)"
    fi
else
    echo "  No order-service.pid file"
fi

if [ -f "logs/api-gateway.pid" ]; then
    pid=$(cat logs/api-gateway.pid)
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} API Gateway PID: $pid (running)"
    else
        echo -e "${YELLOW}⚠${NC} API Gateway PID: $pid (not running - stale PID file)"
    fi
else
    echo "  No api-gateway.pid file"
fi

echo ""
