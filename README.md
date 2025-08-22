# Product Management Web Application

A full-stack web application built with React (TypeScript) frontend and Spring Boot backend, featuring JWT authentication and product management capabilities.

## Architecture

- **Frontend**: React 18 + TypeScript + React Router + Axios
- **Backend**: Spring Boot 3.2.1 + Spring Security + JWT + JPA/Hibernate
- **Database**: MySQL (persistent storage)
- **Authentication**: JWT-based with bcrypt password hashing

## Features

### Authentication
- User registration with email/password
- Secure login with JWT tokens
- Password encryption with bcrypt
- Protected routes and API endpoints
- Auto-logout on token expiration

### Product Management
- Add new products with names
- View all user's products
- Product creation timestamps
- User-specific product isolation

## Project Structure

```
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/productapp/
│   │       ├── controller/  # REST controllers
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data repositories
│   │       ├── security/    # Security configuration
│   │       ├── config/      # Application configuration
│   │       ├── dto/         # Data transfer objects
│   │       └── util/        # Utility classes
│   └── src/main/resources/
│       └── application.yml  # Application configuration
├── frontend/                # React application
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── context/         # React context providers
│   │   ├── services/        # API service layer
│   │   └── types/           # TypeScript type definitions
│   └── public/              # Static assets
└── start-backend.sh         # Backend startup script
└── start-frontend.sh        # Frontend startup script
```

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Products (Protected)
- `GET /api/products` - Get user's products
- `POST /api/products` - Create new product
- `GET /api/products/{id}` - Get specific product

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

1. **Register**: Navigate to http://localhost:3000 and click "Sign Up"
2. **Login**: Enter your credentials to access the dashboard
3. **Add Products**: Use the "Add Product" form to create new products
4. **View Products**: See all your products listed below the form
5. **Logout**: Click the logout button when done

## Database Configuration

The application uses MySQL for persistent data storage:
- **URL**: `jdbc:mysql://localhost:3306/productdb`
- **Username**: `mindful`
- **Password**: `mindful2025`
- **Tables**: Auto-created by Hibernate on first run

## Security Features

- JWT tokens with 24-hour expiration
- CORS configuration for frontend-backend communication
- Password hashing with bcrypt (10 rounds)
- Protected API endpoints requiring authentication
- User isolation (users can only access their own data)

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