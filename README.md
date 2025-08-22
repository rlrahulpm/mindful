# Mindful - Multi-Tenant Product Management Platform

A full-stack enterprise web application built with React (TypeScript) frontend and Spring Boot backend, featuring multi-tenant organization structure, comprehensive role-based access control, and advanced product management capabilities.

## Architecture

- **Frontend**: React 18 + TypeScript + React Router + Axios + Material Icons
- **Backend**: Spring Boot 3.2.1 + Spring Security + JWT + JPA/Hibernate + Flyway
- **Database**: MySQL (persistent storage with migrations)
- **Authentication**: JWT-based with bcrypt password hashing and 24-hour sessions
- **Multi-tenancy**: Organization-based data isolation with hierarchical admin roles

## Features

### Multi-Tenant Architecture
- Organization-based data isolation and access control
- Global super administrators with full system access
- Organization super administrators with org-scoped permissions
- Automatic user-organization association and data filtering

### Advanced Authentication & Authorization
- Admin-only user creation (no public signup)
- Extended JWT tokens (24-hour expiration) with organization claims
- Role-based access control with granular module permissions
- Secure login with bcrypt password hashing and automatic token refresh
- Protected routes and API endpoints with organization scoping

### Product Management System
- Multi-module product architecture (Product Basics, Goals, Personas, etc.)
- Organization-scoped product isolation
- Dynamic module system with configurable access permissions
- Product creation with comprehensive metadata and tracking

### Admin Dashboard & Role Management
- Modern Material Icons-based interface with consistent design language
- Dynamic role creation and management with module-level permissions
- User management with organization filtering
- Professional card-based layouts with smooth hover animations
- Responsive design optimized for desktop, tablet, and mobile

## Project Structure

```
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/productapp/
│   │       ├── controller/  # REST controllers (Auth, Admin, Products, ProductBasics)
│   │       ├── entity/      # JPA entities (User, Product, Role, ProductBasics)
│   │       ├── model/       # Domain models (Organization)
│   │       ├── repository/  # Data repositories with organization filtering
│   │       ├── security/    # JWT security with multi-tenant support
│   │       ├── dto/         # Data transfer objects
│   │       └── util/        # JWT utilities with organization claims
│   └── src/main/resources/
│       ├── application.yml  # Application configuration
│       └── db/migration/    # Flyway database migrations
├── frontend/                # React TypeScript application
│   ├── src/
│   │   ├── components/      # React components (AdminDashboard, ProductModules, etc.)
│   │   ├── context/         # Authentication context with role management
│   │   ├── services/        # API service layer with organization support
│   │   ├── types/           # TypeScript definitions for multi-tenant data
│   │   └── utils/           # JWT utilities and helper functions
│   └── public/              # Static assets with Material Icons
├── start-backend.sh         # Backend startup script
├── start-frontend.sh        # Frontend startup script
└── test-backend.sh          # Backend testing script
```

## API Endpoints

### Authentication (Multi-tenant)
- `POST /api/auth/login` - User login (returns JWT with organization claims)
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout
- *Note: Public signup disabled - users created by administrators only*

### Admin Management (Protected - Superadmin only)
- `GET /api/admin/roles` - Get all roles with module permissions
- `POST /api/admin/roles` - Create new role with module access
- `PUT /api/admin/roles/{id}` - Update role permissions
- `DELETE /api/admin/roles/{id}` - Delete role
- `GET /api/admin/users` - Get organization users (filtered by admin scope)
- `POST /api/admin/users` - Create new user
- `GET /api/admin/product-modules` - Get available product modules

### Products (Protected - Organization scoped)
- `GET /api/products` - Get organization's products
- `POST /api/products` - Create new product
- `GET /api/products/{id}` - Get specific product
- `GET /api/products/{id}/basics` - Get product basics
- `POST /api/products/{id}/basics` - Save product basics

### Modules (Protected - Role-based access)
- `GET /api/modules/product/{productId}` - Get accessible modules for product
- `GET /api/user-role-modules` - Get current user's accessible modules

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 16+ and npm
- MySQL 8.0+ (running on localhost:3306)
- Git

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Mindful
```

### 2. Database Setup
```bash
# Install MySQL (if not already installed)
brew install mysql
brew services start mysql

# Create database and user
mysql -u root -p
```

```sql
CREATE DATABASE productdb;
CREATE USER 'mindful'@'localhost' IDENTIFIED BY 'mindful2025';
GRANT ALL PRIVILEGES ON productdb.* TO 'mindful'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

*Note: Database schema and initial data will be automatically created via Flyway migrations on first startup.*

### 3. Backend Setup
```bash
cd backend
mvn clean install
```

### 4. Frontend Setup
```bash
cd frontend
npm install
```

### 5. Running the Application

#### Option 1: Using the provided scripts
```bash
# Start backend (from project root)
./start-backend.sh

# Start frontend (from project root, in a new terminal)
./start-frontend.sh
```

#### Option 2: Manual startup
```bash
# Terminal 1: Start backend
cd backend
mvn spring-boot:run

# Terminal 2: Start frontend  
cd frontend
npm start
```

The application will be available at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- MySQL Database: localhost:3306/productdb

## Usage

### For Global Super Administrators (rlrahul2030@gmail.com)
1. **Login**: Navigate to http://localhost:3000 and enter global admin credentials
2. **Admin Dashboard**: Access comprehensive role and user management
3. **Create Roles**: Define granular permissions for different product modules
4. **Manage Users**: Create organization users and assign roles
5. **Cross-Organization Access**: View and manage all organizations

### For Organization Super Administrators
1. **Login**: Access with organization admin credentials
2. **Scoped Management**: Manage users and roles within your organization
3. **Product Management**: Create and manage organization-specific products
4. **Role Assignment**: Assign module access to users in your organization

### For Regular Users
1. **Login**: Access with user credentials provided by administrators
2. **Product Access**: View and work with products based on assigned role permissions
3. **Module Navigation**: Access Product Basics, Goals, Personas and other modules as permitted
4. **Data Isolation**: Only see data belonging to your organization

## Database Configuration

The application uses MySQL for persistent data storage:
- **URL**: `jdbc:mysql://localhost:3306/productdb`
- **Username**: `mindful`
- **Password**: `mindful2025`
- **Tables**: Auto-created by Hibernate on first run

## Security Features

- **Multi-tenant Data Isolation**: Organization-based data access control
- **JWT tokens with extended 24-hour expiration** for improved user experience
- **Role-based Access Control (RBAC)** with granular module permissions
- **Hierarchical Admin Structure**: Global vs Organization super administrators
- **Password hashing with bcrypt** (10 rounds) for secure credential storage
- **Protected API endpoints** with JWT authentication and organization filtering
- **CORS configuration** for secure frontend-backend communication
- **No public signup** - admin-only user creation for enterprise security

## Environment Configuration

### Backend (application.yml)
- Server port: 8080
- JWT secret: Configurable via `jwt.secret`
- JWT expiration: Configurable via `jwt.expiration`

### Frontend
- API base URL: http://localhost:8080/api
- Development server port: 3000

## Development Notes

- The frontend automatically redirects to login on 401 responses
- JWT tokens are stored in localStorage
- CORS is configured to allow requests from localhost:3000
- Database schema is auto-created on startup
- All API requests include proper error handling

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Production Deployment

For production deployment:

1. Update JWT secret in application.yml
2. Configure proper database (PostgreSQL/MySQL)
3. Build frontend: `npm run build`
4. Update CORS configuration for production domain
5. Configure HTTPS
6. Set appropriate JWT expiration times

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3000 and 8080 are available
2. **CORS errors**: Check that backend CORS is configured for frontend URL
3. **JWT errors**: Verify JWT secret configuration
4. **Database issues**: Check H2 console at /h2-console

### Logs
- Backend logs: Console output from Spring Boot
- Frontend logs: Browser developer console

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request