.PHONY: help build run stop clean restart logs test docker-build docker-run docker-stop docker-clean

# Default target
help:
	@echo "User Service - Makefile Commands"
	@echo ""
	@echo "Development:"
	@echo "  make build        - Build the application with Gradle"
	@echo "  make run          - Run the application locally"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build - Build Docker image"
	@echo "  make docker-run   - Start Docker containers (PostgreSQL + App)"
	@echo "  make docker-stop  - Stop Docker containers"
	@echo "  make docker-restart - Restart Docker containers"
	@echo "  make docker-clean - Stop and remove containers, volumes, and images"
	@echo "  make logs         - View application logs"
	@echo ""
	@echo "Testing:"
	@echo "  make test         - Run API tests against running service"
	@echo "  make test-docker  - Build, run Docker, and test (full pipeline)"
	@echo ""
	@echo "Database:"
	@echo "  make db-access    - Access PostgreSQL database via psql"
	@echo ""

# Build the application
build:
	@echo "Building application..."
	./gradlew installDist --no-daemon
	@echo "Build complete!"

# Run the application locally
run:
	@echo "Starting application locally..."
	./gradlew run --no-daemon

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	./gradlew clean --no-daemon
	rm -rf build/
	@echo "Clean complete!"

# Docker: Build image
docker-build:
	@echo "Building Docker image..."
	./gradlew installDist --no-daemon
	docker-compose build
	@echo "Docker build complete!"

# Docker: Start containers
docker-run:
	@echo "Starting Docker containers..."
	docker-compose up -d
	@echo "Waiting for service to start..."
	@sleep 10
	@echo "Containers started!"
	@docker-compose ps

# Docker: Stop containers
docker-stop:
	@echo "Stopping Docker containers..."
	docker-compose down
	@echo "Containers stopped!"

# Docker: Restart containers
docker-restart: docker-stop docker-run

# Docker: Clean everything
docker-clean:
	@echo "Removing all Docker containers, volumes, and images..."
	docker-compose down -v --remove-orphans
	docker rmi $$(docker images -q user-service-user-service) 2>/dev/null || true
	@echo "Docker clean complete!"

# View logs
logs:
	docker-compose logs -f user-service-app

# View logs for specific time
logs-tail:
	docker-compose logs --tail=100 -f user-service-app

# Run API tests
test:
	@echo "Running API tests..."
	@chmod +x test-api.sh
	./test-api.sh

# Full pipeline: Build, Docker, Test
test-docker: clean docker-build docker-run test docker-stop
	@echo ""
	@echo "========================================"
	@echo "Full test pipeline complete!"
	@echo "========================================"

# Access database
db-access:
	@echo "Connecting to PostgreSQL database..."
	docker exec -it user-service-db psql -U postgres -d userservice

# Health check
health:
	@echo "Checking service health..."
	@curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/ || echo "Service not responding"

# Show container status
status:
	docker-compose ps

# Show environment variables
env:
	@echo "Current environment configuration:"
	@cat .env
