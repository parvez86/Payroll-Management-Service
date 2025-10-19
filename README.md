# Payroll Management System

A robust, monolithic payroll management system built with Spring Boot 3.5.6 and Java 24, designed for secure salary calculation, transfer, and reporting with ACID-compliant financial transactions.

## 🎯 Overview

This system manages exactly 10 employees across 6 hierarchical grades with automatic salary calculations and secure financial transfers. The architecture follows a modulith pattern for rapid deployment while maintaining logical separation of concerns.

### Employee Distribution
- **Grade 1**: 1 employee (highest)
- **Grade 2**: 1 employee  
- **Grade 3**: 2 employees
- **Grade 4**: 2 employees
- **Grade 5**: 2 employees
- **Grade 6**: 2 employees (lowest)

## 🏗️ Technology Stack

### Backend
- **Java 24** with Spring Boot 3.5.6
- **Spring Data JPA** for data persistence
- **PostgreSQL** as primary database
- **Spring Security** with JWT authentication
- **Liquibase** for database migrations
- **H2** for testing
- **Lombok** for boilerplate reduction
- **ModelMapper** for object mapping

### Frontend (React)
- **React** with JavaScript
- **Axios** for API communication
- **Bootstrap/CSS** for styling

## 📋 Key Features

### 🔐 Authentication & Security
- JWT-based stateless authentication
- Role-based authorization (ADMIN, EMPLOYER, EMPLOYEE)
- Protected payroll and transaction endpoints

### 👥 Employee Management
- Full CRUD operations for employees
- 4-digit unique employee ID validation
- Bank account management with balance tracking
- Grade-based hierarchical organization

### 💰 Salary Calculation
- **Formula**: Basic + HRA (20% of Basic) + Medical (15% of Basic)
- **Grade Calculation**: Basic(Grade) = Grade6Base + (6 - GradeNumber) × 5000
- Configurable Grade 6 base salary
- BigDecimal precision for all financial calculations

### 🏦 Financial Transactions
- **ACID-compliant** salary transfers
- Company account management with insufficient funds handling
- Real-time balance validation
- Immutable transaction audit trail
- Automatic rollback on transaction failures

### 📊 Reporting
- Comprehensive salary sheets
- Financial summaries with company balance
- Employee listings ordered by grade
- Transaction history and audit trails

## 🚀 Quick Start

### Prerequisites
- Java 24 or higher
- PostgreSQL 12+
- Node.js 16+ (for frontend)
- Gradle 8.0+

### Database Setup

1. **Create PostgreSQL Database**:
```sql
CREATE DATABASE payroll_db;
CREATE USER payroll_user WITH PASSWORD 'payroll_pass';
GRANT ALL PRIVILEGES ON DATABASE payroll_db TO payroll_user;
```

2. **Update Configuration** (if needed):
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payroll_db
    username: payroll_user
    password: payroll_pass
```

### Running the Application

1. **Clone and Build**:
```bash
git clone <repository-url>
cd PayrollManagementSystem/payroll_service
```

2. **Run with Gradle**:
```bash
# Development mode
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production mode
./gradlew bootRun
```

3. **Using Docker**:
```bash
# Start with PostgreSQL
docker-compose up -d

# Build and run application
./gradlew build
java -jar build/libs/payroll_service-0.0.1-SNAPSHOT.jar
```

### Development Setup

**Windows Users**:
```bash
# Run the setup script
scripts/dev-setup.bat
```

**Linux/Mac Users**:
```bash
# Run the setup script
chmod +x scripts/dev-setup.sh
./scripts/dev-setup.sh
```

## 🔧 API Documentation

The application provides comprehensive API documentation through Swagger UI:

**Local Development**: http://localhost:8080/api/swagger-ui.html

### Core Endpoints

#### Authentication
```
POST /api/auth/login          # Generate JWT token
POST /api/auth/logout         # Invalidate token
```

#### Employee Management
```
GET    /api/employees         # List all employees (ordered by grade)
POST   /api/employees         # Create new employee with bank account
GET    /api/employees/{id}    # Get employee details
PUT    /api/employees/{id}    # Update employee
DELETE /api/employees/{id}    # Soft delete employee
```

#### Payroll Operations
```
GET    /api/salary/calculate  # Calculate salary sheet (no transfer)
POST   /api/salary/transfer   # Execute ACID-compliant payment
PUT    /api/config/base-salary # Configure Grade 6 base salary
```

#### Company Account
```
GET    /api/company/balance   # Get company account balance
POST   /api/company/topup     # Add funds to company account
GET    /api/company/transactions # View transaction history
```

## 📁 Project Structure

```
src/main/java/org/sp/payroll_service/
├── config/                    # Configuration classes
│   ├── SecurityConfig.java   # JWT & Security configuration
│   └── DatabaseConfig.java   # Database configuration
├── domain/                    # Domain entities
│   ├── auth/                 # Authentication entities
│   ├── common/               # Base entities and enums
│   ├── core/                 # Core business entities
│   ├── payroll/              # Payroll-specific entities
│   └── wallet/               # Financial entities
├── repository/               # Data access layer
├── service/                  # Business logic layer
│   ├── AuthUserService.java # JWT & user management
│   ├── LedgerService.java   # Financial transactions
│   └── PayrollService.java  # Payroll operations
├── web/                      # REST controllers
└── utils/                    # Utility classes
```

## 💾 Database Schema

### Key Entities

- **Employee**: Core employee information with 4-digit unique ID
- **Grade**: Hierarchical rank system (1-6)
- **BankAccount**: Employee bank account with BigDecimal balance
- **CompanyAccount**: Singleton company account for salary payments
- **PayrollTransaction**: Immutable audit trail for all transactions

### Critical Business Rules

1. **Employee ID**: Must be exactly 4 digits and unique
2. **Financial Precision**: All monetary values use BigDecimal
3. **Grade Distribution**: Enforced 1,1,2,2,2,2 employee limit per grade
4. **Transaction Integrity**: ACID compliance with automatic rollback

## 🔒 Security Features

### JWT Configuration
- **Secret**: Configurable via environment variable
- **Expiration**: 24 hours (configurable)
- **Issuer**: PayrollManagementSystem

### Authorization Levels
- **ADMIN**: Full system access including salary transfers
- **EMPLOYER**: Employee management and reporting
- **EMPLOYEE**: View-only access to personal information

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Integration tests only
./gradlew integrationTest
```

## 📊 Monitoring & Health Checks

**Health Endpoint**: http://localhost:8080/api/actuator/health

**Available Metrics**:
- Database connectivity
- Application health
- Custom payroll metrics

## 🔧 Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payroll_db
SPRING_DATASOURCE_USERNAME=payroll_user
SPRING_DATASOURCE_PASSWORD=payroll_pass

# JWT Security
JWT_SECRET=your-secret-key-here

# Application Settings
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Application Properties

Key configurations in `application.yml`:

```yaml
app:
  payroll:
    grade6-base-salary: 50000.00  # Grade 6 base salary
    hra-percentage: 0.20          # 20% HRA
    medical-percentage: 0.15      # 15% Medical
    grade-increment: 5000.00      # Increment per grade
```

## 🚀 Deployment

### Production Deployment

1. **Build Production JAR**:
```bash
./gradlew clean build -Pprod
```

2. **Run with Production Profile**:
```bash
java -jar -Dspring.profiles.active=prod build/libs/payroll_service-0.0.1-SNAPSHOT.jar
```

3. **Docker Deployment**:
```bash
docker build -t payroll-service .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod payroll-service
```

## 📚 Development Guidelines

### Code Standards
- Follow proper JavaDoc practices
- Use BigDecimal for all financial calculations
- Implement proper exception handling
- Maintain transaction boundaries with @Transactional

### Financial Transaction Pattern
```java
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class LedgerService {
    public PayrollResult processSalaryTransfer() {
        // 1. Validate company account balance
        // 2. Execute atomic transfer
        // 3. Handle insufficient funds with rollback
        // 4. Create audit records
        // 5. Return success/failure status
    }
}
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**:
   - Verify PostgreSQL is running
   - Check connection credentials
   - Ensure database exists

2. **JWT Token Invalid**:
   - Check token expiration
   - Verify JWT secret configuration
   - Ensure proper Authorization header format

3. **Transaction Rollback**:
   - Check company account balance
   - Verify transaction isolation settings
   - Review audit logs for details

## 📞 Support

For technical support or questions:
- Review the API documentation at `/swagger-ui.html`
- Check application logs for detailed error information
- Verify configuration settings in `application.yml`

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: October 2025  
**Java Version**: 24  
**Spring Boot Version**: 3.5.6