#!/bin/bash

# MharRuengSang - Stop All Microservices

echo "🛑 Stopping MharRuengSang Microservices..."

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Function to stop service
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                echo -e "${RED}Force killing $service_name${NC}"
                kill -9 $pid
            fi
            rm "$pid_file"
            echo -e "${GREEN}✓ $service_name stopped${NC}"
        else
            echo "⚠️  $service_name was not running"
            rm "$pid_file"
        fi
    else
        echo "⚠️  No PID file found for $service_name"
    fi
}

# Stop services
stop_service "api-gateway"
stop_service "order-service"
stop_service "restaurant-service"

# Clean up any remaining processes on these ports
echo ""
echo "Cleaning up ports..."
for port in 8080 8081 8082; do
    pid=$(lsof -ti :$port)
    if [ ! -z "$pid" ]; then
        echo "Killing process on port $port (PID: $pid)"
        kill -9 $pid
    fi
done

echo ""
echo -e "${GREEN}✅ All services stopped${NC}"
