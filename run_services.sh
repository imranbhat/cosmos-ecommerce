#!/bin/bash


# Function to kill process on port
kill_port() {
  pid=$(lsof -ti:$1)
  if [ -n "$pid" ]; then
    echo "Killing process on port $1 (PID: $pid)"
    kill -9 $pid
  fi
}

echo "Stopping any existing services..."
kill_port 8761 # Discovery
kill_port 8080 # Gateway
kill_port 8081 # Product
kill_port 8085 # Product 2
kill_port 8082 # Inventory
kill_port 9092 # Inventory gRPC
kill_port 8083 # User
kill_port 8084 # Order

# JVM Memory options
JVM_OPTS="-Xmx256m"

echo "Starting Discovery Service..."
nohup java $JVM_OPTS -jar discovery-service/target/discovery-service-0.0.1-SNAPSHOT.jar > discovery.log 2>&1 &
echo "Waiting for Discovery Service (15s)..."
sleep 15

echo "Starting Inventory Service (gRPC + REST)..."
nohup java $JVM_OPTS -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar > inventory.log 2>&1 &
echo "Waiting for Inventory Service (10s)..."
sleep 10

echo "Starting Product Service (Instance 1)..."
nohup java $JVM_OPTS -jar product-service/target/product-service-0.0.1-SNAPSHOT.jar > product.log 2>&1 &
sleep 5

echo "Starting Product Service (Instance 2)..."
nohup java $JVM_OPTS -Dserver.port=8085 -jar product-service/target/product-service-0.0.1-SNAPSHOT.jar > product2.log 2>&1 &
sleep 5

echo "Starting User Service..."
nohup java $JVM_OPTS -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar > user.log 2>&1 &
sleep 5

echo "Starting API Gateway..."
nohup java $JVM_OPTS -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar > gateway.log 2>&1 &
sleep 10

echo "Starting Order Service..."
nohup java $JVM_OPTS -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar > order.log 2>&1 &

echo "All services started with memory limit $JVM_OPTS. Logs are being written to *.log files."
echo "Discovery: 8761"
echo "Gateway: 8080"
echo "Inventory: 8082 (REST), 9092 (gRPC)"
echo "Order: 8084"
echo "Product: 8081, 8085"
echo "User: 8083"
