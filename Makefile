# Makefile for Docker operations

.PHONY: help build up down restart logs clean rebuild dev prod

help:
	@echo "Available commands:"
	@echo "  make build    - Build all Docker images"
	@echo "  make up       - Start all services"
	@echo "  make down     - Stop all services"
	@echo "  make restart  - Restart all services"
	@echo "  make logs     - View logs from all services"
	@echo "  make clean    - Remove containers, networks, and volumes"
	@echo "  make rebuild  - Clean rebuild of all services"
	@echo "  make dev      - Start in development mode"
	@echo "  make prod     - Start in production mode"

build:
	docker-compose build

up:
	docker-compose up -d

down:
	docker-compose down

restart:
	docker-compose restart

logs:
	docker-compose logs -f

clean:
	docker-compose down -v
	docker system prune -f

rebuild: clean build up

dev:
	docker-compose up -d

prod:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Service-specific commands
backend-logs:
	docker-compose logs -f backend

frontend-logs:
	docker-compose logs -f frontend

crm-logs:
	docker-compose logs -f crm

mysql-logs:
	docker-compose logs -f mysql

# Database operations
db-shell:
	docker-compose exec mysql mysql -u mindful -pmindful2025 productdb

db-backup:
	docker-compose exec mysql mysqldump -u mindful -pmindful2025 productdb > backup_$$(date +%Y%m%d_%H%M%S).sql

# Testing
test-backend:
	docker-compose exec backend mvn test

# Health check
health:
	@echo "Checking service health..."
	@docker-compose ps
	@echo "\nBackend health:"
	@curl -s http://localhost:8080/actuator/health || echo "Backend not responding"
	@echo "\nFrontend health:"
	@curl -s http://localhost:3000 > /dev/null && echo "Frontend is running" || echo "Frontend not responding"
	@echo "\nCRM health:"
	@curl -s http://localhost:3001 > /dev/null && echo "CRM is running" || echo "CRM not responding"