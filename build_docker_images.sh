#!/bin/bash
set -e

echo "Starting Minikube (if not running)..."
minikube status || minikube start --cpus 4 --memory 10240

echo "Setting up Minikube Docker environment..."
eval $(minikube docker-env)

echo "Building images..."

echo "Building discovery-service..."
docker build -t discovery-service:latest ./discovery-service

echo "Building api-gateway..."
docker build -t api-gateway:latest ./api-gateway

echo "Building inventory-service..."
docker build -t inventory-service:latest ./inventory-service

echo "Building product-service..."
docker build -t product-service:latest ./product-service

echo "Building order-service..."
docker build -t order-service:latest ./order-service

echo "Building user-service..."
docker build -t user-service:latest ./user-service

echo "Images built successfully in Minikube environment!"
