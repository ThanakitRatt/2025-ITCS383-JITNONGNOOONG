#!/bin/bash

# MharRuengSang - Start All Microservices
# This script starts all services in background and shows their logs

echo "🚀 Starting MharRuengSang Microservices..."
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to check if port is available
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ Port $1 is already in use${NC}"
        echo "   Kill process: lsof -ti :$1 | xargs kill -9"
        return 1
    fi
    return 0
}

# Function to start service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo -e "${BLUE}Starting $service_name on port $port...${NC}"
    cd "$service_dir"
    mvn spring-boot:run -Dspring-boot.run.arguments="--eureka.client.enabled=false" > "../logs/${service_name}.log" 2>&1 &
    echo $! > "../logs/${service_name}.pid"
    echo -e "${GREEN}✓ $service_name started (PID: $(cat ../logs/${service_name}.pid))${NC}"
}

# Create logs directory
mkdir -p logs

# Check ports
echo "Checking ports..."
check_port 8080 || exit 1
check_port 8081 || exit 1
check_port 8082 || exit 1

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Start services
echo ""
echo "Starting services..."
start_service "restaurant-service" "$SCRIPT_DIR/restaurant-service" 8082
sleep 5

start_service "order-service" "$SCRIPT_DIR/order-service" 8081
sleep 5

start_service "api-gateway" "$SCRIPT_DIR/api-gateway" 8080
sleep 3

echo ""
echo -e "${GREEN}=========================================="
echo "✅ All services started!"
echo "==========================================${NC}"
echo ""
echo "📍 Service URLs:"
echo "   🌐 API Gateway:        http://localhost:8080"
echo "   🍽️  Restaurant Service: http://localhost:8082"
echo "   📦 Order Service:       http://localhost:8081"
echo ""
echo "📋 Logs:"
echo "   tail -f logs/restaurant-service.log"
echo "   tail -f logs/order-service.log"
echo "   tail -f logs/api-gateway.log"
echo ""
echo "🛑 To stop all services, run: ./stop-all.sh"
echo ""

# Follow all logs in parallel
echo "Following logs (Ctrl+C to exit)..."
echo "==========================================="
tail -f logs/*.log
