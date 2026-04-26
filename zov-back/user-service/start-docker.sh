#!/bin/bash

echo "========================================"
echo "User Service - Docker Build & Run"
echo "========================================"
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating default..."
    cp .env.example .env 2>/dev/null || echo "Please create .env file manually"
fi

# Build and start services
echo "Building and starting services..."
docker-compose up -d --build

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Services started successfully!"
    echo ""
    echo "Waiting for application to be ready..."
    sleep 10
    
    # Check health
    echo "Checking application health..."
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/)
    
    if [ "$HTTP_CODE" = "404" ]; then
        echo "✅ Application is running (HTTP $HTTP_CODE)"
    else
        echo "⚠️  Application responded with HTTP $HTTP_CODE"
    fi
    
    echo ""
    echo "========================================"
    echo "Useful commands:"
    echo "========================================"
    echo "  docker-compose logs -f     # View logs"
    echo "  docker-compose ps          # Check status"
    echo "  docker-compose down        # Stop services"
    echo "  docker-compose down -v     # Stop and remove volumes"
    echo "========================================"
else
    echo "❌ Failed to start services"
    exit 1
fi
