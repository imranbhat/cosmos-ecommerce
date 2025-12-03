#!/bin/bash

# Base URLs
PRODUCT_SERVICE="http://localhost:8081"
INVENTORY_SERVICE="http://localhost:8082"
USER_SERVICE="http://localhost:8083"
ORDER_SERVICE="http://localhost:8084"

echo "--------------------------------------------------"
echo "Testing Order Management System"
echo "--------------------------------------------------"

# 1. Create a Product
echo "1. Creating Product..."
PRODUCT_RESPONSE=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "price": 1200.00, "description": "High-end gaming laptop"}')
echo "Response: $PRODUCT_RESPONSE"
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Created Product ID: $PRODUCT_ID"
echo "--------------------------------------------------"

# 2. Create a User
echo "2. Creating User..."
USER_RESPONSE=$(curl -s -X POST $USER_SERVICE/users \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "email": "john@example.com"}')
echo "Response: $USER_RESPONSE"
USER_ID=$(echo $USER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Created User ID: $USER_ID"
echo "--------------------------------------------------"

# 3. Create Inventory
echo "3. Creating Inventory..."
INVENTORY_RESPONSE=$(curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PRODUCT_ID, \"quantity\": 100}")
echo "Response: $INVENTORY_RESPONSE"
echo "--------------------------------------------------"

# 4. Create Order
echo "4. Creating Order..."
ORDER_RESPONSE=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PRODUCT_ID, \"userId\": $USER_ID, \"quantity\": 1}")
echo "Response: $ORDER_RESPONSE"
echo "--------------------------------------------------"

echo "Test Complete."
