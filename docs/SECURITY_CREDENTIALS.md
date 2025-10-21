# Security Credentials - Payroll Management System

## ⚠️ DEVELOPMENT ENVIRONMENT ONLY
**WARNING: This file contains sensitive security information for development and testing purposes only. Never use these credentials in production!**

---

## System Access Information

### Application URLs
- **Local Development**: `http://localhost:8080/pms/v1/api`
- **Docker Environment**: `http://localhost:20001/pms/v1/api`
- **Swagger UI**: `http://localhost:20001/pms/v1/api/swagger-ui/index.html`

---

## Admin Account

### System Administrator
- **Username**: `admin`
- **Email**: `admin@techcorp.com`
- **Password**: `admin123`
- **Role**: `ADMIN`
- **Access Level**: Full system access, payroll processing, configuration management

---

## Employee Accounts

All employee accounts use the same password for development convenience:
**Default Password**: `admin123` (same as admin for testing)

### Grade 1 - Director Level (1 Employee)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `director001` | director@techcorp.com | EMPLOYER | 1001 | Ahmed Rahman |

### Grade 2 - Manager Level (1 Employee)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `manager001` | manager@techcorp.com | EMPLOYER | 2001 | Fatima Khatun |

### Grade 3 - Senior Level (2 Employees)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `senior001` | senior001@techcorp.com | EMPLOYEE | 3001 | Mohammad Islam |
| `senior002` | senior002@techcorp.com | EMPLOYEE | 3002 | Rashida Begum |

### Grade 4 - Developer Level (2 Employees)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `dev001` | dev001@techcorp.com | EMPLOYEE | 4001 | Karim Ahmed |
| `dev002` | dev002@techcorp.com | EMPLOYEE | 4002 | Nasir Uddin |

### Grade 5 - Junior Level (2 Employees)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `junior001` | junior001@techcorp.com | EMPLOYEE | 5001 | Sadia Rahman |
| `junior002` | junior002@techcorp.com | EMPLOYEE | 5002 | Habib Khan |

### Grade 6 - Intern Level (2 Employees)
| Username | Email | Role | Employee ID | Name |
|----------|-------|------|-------------|------|
| `intern001` | intern001@techcorp.com | EMPLOYEE | 6001 | Taslima Akter |
| `intern002` | intern002@techcorp.com | EMPLOYEE | 6002 | Rafiq Islam |

---

## Bank Account Information

### Company Account
- **Account Name**: TechCorp Main Account
- **Account Number**: `COMP001`
- **Account Type**: CURRENT
- **Initial Balance**: ৳10,00,000 (10 Lakh BDT)
- **Bank**: Bangladesh Bank, Motijheel Branch

### Employee Bank Accounts
All employee accounts start with ৳0 balance and are SAVINGS type accounts.

| Employee | Account Number | Bank Branch |
|----------|----------------|-------------|
| Ahmed Rahman (Director) | DIR001 | Bangladesh Bank, Motijheel |
| Fatima Khatun (Manager) | MGR001 | Sonali Bank, Dhanmondi |
| Mohammad Islam (Senior) | SEN001 | Dutch Bangla Bank, Gulshan |
| Rashida Begum (Senior) | SEN002 | Bangladesh Bank, Motijheel |
| Karim Ahmed (Developer) | DEV001 | Sonali Bank, Dhanmondi |
| Nasir Uddin (Developer) | DEV002 | Dutch Bangla Bank, Gulshan |
| Sadia Rahman (Junior) | JUN001 | Bangladesh Bank, Motijheel |
| Habib Khan (Junior) | JUN002 | Sonali Bank, Dhanmondi |
| Taslima Akter (Intern) | INT001 | Dutch Bangla Bank, Gulshan |
| Rafiq Islam (Intern) | INT002 | Bangladesh Bank, Motijheel |

---

## Database Credentials

### PostgreSQL Database (Docker)
- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `payroll_db`
- **Username**: `payroll_user`
- **Password**: `payroll_pass`

### Database Connection String
```
jdbc:postgresql://localhost:5432/payroll_db
```

---

## JWT Security Configuration

### JWT Settings
- **Secret Key**: Auto-generated on application startup
- **Token Expiration**: 24 hours (configurable)
- **Algorithm**: HS256
- **Header**: `Authorization: Bearer <token>`

### API Authentication
1. Login via `POST /api/auth/login`
2. Receive JWT token in response
3. Include token in `Authorization` header for all protected endpoints
4. Token format: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

---

## Role-Based Access Control

### ADMIN Role
- Full system access
- User management
- Payroll processing
- Configuration management
- Financial transactions

### EMPLOYER Role
- Employee data viewing
- Limited payroll access
- Department management
- Reporting access

### EMPLOYEE Role
- Personal profile access
- Salary slip viewing
- Leave management
- Basic reporting

---

## Security Notes

### Password Hashing
All passwords are hashed using BCrypt with strength 12:
```
$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyPw5w2cBpwpL4gUzV7Uy6
```

### Security Best Practices
1. **Change Default Passwords**: Update all default passwords before production deployment
2. **Environment Variables**: Use environment variables for sensitive configuration
3. **HTTPS Only**: Enable HTTPS/TLS for all production environments
4. **Token Rotation**: Implement JWT token rotation for enhanced security
5. **Database Security**: Use strong database credentials and connection encryption
6. **Audit Logging**: Enable comprehensive audit logging for all financial transactions

---

## Testing Scenarios

### Basic Login Test
```bash
curl -X POST http://localhost:20001/pms/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Employee Login Test
```bash
curl -X POST http://localhost:20001/pms/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"director001","password":"admin123"}'
```

---

## Emergency Access

### Database Direct Access
If application is inaccessible, connect directly to PostgreSQL:
```bash
docker exec -it payroll_service-postgres-1 psql -U payroll_user -d payroll_db
```

### Reset Admin Password
SQL command to reset admin password to `newpassword123`:
```sql
UPDATE users SET password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyPw5w2cBpwpL4gUzV7Uy6' WHERE username = 'admin';
```

---

**Last Updated**: October 20, 2025  
**Environment**: Development/Testing Only  
**Security Level**: Internal Use Only