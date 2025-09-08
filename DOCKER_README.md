# Docker Setup for Mindful Project

This project is fully containerized using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB+ available RAM

## Quick Start

1. **Copy environment file:**
   ```bash
   cp .env.docker .env
   ```

2. **Build and start all services:**
   ```bash
   docker-compose up -d
   ```

   Or using Make:
   ```bash
   make up
   ```

## Services

The following services will be available:

- **Backend API**: http://localhost:8080
- **Frontend**: http://localhost:3000
- **CRM**: http://localhost:3001
- **phpMyAdmin**: http://localhost:8081
- **MySQL**: localhost:3306

## Available Commands

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild services
docker-compose build

# Clean everything (including volumes)
docker-compose down -v
```

### Using Make

```bash
# Build all images
make build

# Start services
make up

# Stop services
make down

# View logs
make logs

# Clean rebuild
make rebuild

# Start in production mode
make prod

# Check service health
make health
```

## Development Mode

The default `docker-compose.yml` includes development overrides:
- Source code is mounted for hot reload
- Debug logging is enabled
- Database DDL is set to `update`

## Production Mode

For production deployment:

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Or:
```bash
make prod
```

## Database Management

### Access MySQL Shell
```bash
docker-compose exec mysql mysql -u mindful -pmindful2025 productdb
```

### Backup Database
```bash
make db-backup
```

### Access phpMyAdmin
Navigate to http://localhost:8081
- Server: mysql
- Username: mindful
- Password: mindful2025

## Troubleshooting

### Port Conflicts
If you encounter port conflicts, modify the ports in `.env` file:
```env
BACKEND_PORT=8080
FRONTEND_PORT=3000
CRM_PORT=3001
MYSQL_PORT=3306
PHPMYADMIN_PORT=8081
```

### Memory Issues
If containers are running out of memory, increase Docker's memory allocation in Docker Desktop settings.

### Build Issues
```bash
# Clean rebuild
make clean
make build
make up
```

### View Service Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f crm
```

## Environment Variables

Key environment variables (configure in `.env`):

- `DATABASE_USERNAME`: MySQL user
- `DATABASE_PASSWORD`: MySQL password
- `JWT_SECRET`: JWT signing secret
- `DDL_AUTO`: Hibernate DDL mode (update/validate)
- `LOG_LEVEL`: Application log level

## Security Notes

1. Change default passwords in production
2. Use strong JWT secrets
3. Remove phpMyAdmin in production
4. Use SSL/TLS for production deployments
5. Implement proper firewall rules

## Docker Image Sizes

Approximate image sizes:
- Backend: ~250MB (multi-stage build)
- Frontend: ~25MB (nginx alpine)
- CRM: ~25MB (nginx alpine)
- MySQL: ~450MB

## Performance Optimization

The setup includes:
- Multi-stage builds for smaller images
- Health checks for all services
- Resource limits (can be added in docker-compose.yml)
- Nginx caching for static assets
- Connection pooling for database

## Updating Services

To update a specific service:
```bash
docker-compose build [service-name]
docker-compose up -d [service-name]
```

Example:
```bash
docker-compose build backend
docker-compose up -d backend
```