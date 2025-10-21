# Payroll Management System - API Documentation

## Base URL
```
http://localhost:8080/pms/v1/api
```

## Authentication
All APIs (except login) require JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

---

## 🔐 Authentication APIs

### 1. Login
- **Endpoint**: `POST /auth/login`
- **Description**: Authenticate user and get JWT token
- **Request Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN"
    }
  }
}
```
- **Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

### 2. Logout
- **Endpoint**: `POST /auth/logout`
- **Description**: Invalidate JWT token
- **Headers**: Authorization: Bearer <token>
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

---

## 👥 Employee Management APIs

### 1. Get All Employees
- **Endpoint**: `GET /employees`
- **Description**: Retrieve all employees with their details
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": [
    {
      "id": "1001",
      "name": "John A",
      "grade": 1,
      "address": "Dhaka",
      "mobile": "01700000001",
      "bankAccount": {
        "type": "Current",
        "name": "John A",
        "number": "1234567890",
        "balance": 5000,
        "bank": "Global Bank",
        "branch": "Main"
      },
      "salary": null,
      "createdAt": "2025-01-01T00:00:00Z",
      "updatedAt": "2025-01-01T00:00:00Z"
    }
  ]
}
```

### 2. Get Employee by ID
- **Endpoint**: `GET /employees/{id}`
- **Description**: Retrieve specific employee details
- **Path Parameters**: 
  - `id` (string): 4-digit employee ID
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Employee retrieved successfully",
  "data": {
    "id": "1001",
    "name": "John A",
    "grade": 1,
    "address": "Dhaka",
    "mobile": "01700000001",
    "bankAccount": {
      "type": "Current",
      "name": "John A",
      "number": "1234567890",
      "balance": 5000,
      "bank": "Global Bank",
      "branch": "Main"
    }
  }
}
```
- **Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Employee not found",
  "data": null
}
```

### 3. Create Employee
- **Endpoint**: `POST /employees`
- **Description**: Create a new employee
- **Request Body**:
```json
{
  "id": "1011",
  "name": "New Employee",
  "grade": 3,
  "address": "Sylhet",
  "mobile": "01700000011",
  "bankAccount": {
    "type": "Savings",
    "name": "New Employee",
    "number": "1234567900",
    "balance": 0,
    "bank": "Test Bank",
    "branch": "Main"
  }
}
```
- **Response** (201 Created):
```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": {
    "id": "1011",
    "name": "New Employee",
    "grade": 3,
    "address": "Sylhet",
    "mobile": "01700000011",
    "bankAccount": {
      "type": "Savings",
      "name": "New Employee",
      "number": "1234567900",
      "balance": 0,
      "bank": "Test Bank",
      "branch": "Main"
    },
    "createdAt": "2025-01-01T12:00:00Z"
  }
}
```
- **Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "errors": [
      "Employee ID must be 4 digits",
      "Grade limit exceeded for grade 3",
      "Mobile number already exists"
    ]
  }
}
```

### 4. Update Employee
- **Endpoint**: `PUT /employees/{id}`
- **Description**: Update existing employee
- **Path Parameters**: 
  - `id` (string): 4-digit employee ID
- **Request Body**: (Same as Create Employee)
- **Response**: (Same as Create Employee with 200 OK)

### 5. Delete Employee
- **Endpoint**: `DELETE /employees/{id}`
- **Description**: Delete an employee
- **Path Parameters**: 
  - `id` (string): 4-digit employee ID
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Employee deleted successfully",
  "data": null
}
```

---

## 💰 Payroll Management APIs

### 1. Calculate Salaries
- **Endpoint**: `POST /payroll/calculate`
- **Description**: Calculate salaries for all employees based on grade 6 basic salary
- **Request Body**:
```json
{
  "grade6Basic": 25000
}
```
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Salaries calculated successfully",
  "data": {
    "employees": [
      {
        "id": "1001",
        "name": "John A",
        "grade": 1,
        "salary": {
          "basic": 50000,
          "houseRent": 10000,
          "medicalAllowance": 7500,
          "gross": 67500
        }
      }
    ],
    "totalSalaryRequired": 450000,
    "calculatedAt": "2025-01-01T12:00:00Z"
  }
}
```

### 2. Get Salary Calculation
- **Endpoint**: `GET /payroll/calculate`
- **Description**: Get current salary calculations
- **Query Parameters**:
  - `grade6Basic` (number): Basic salary for grade 6
- **Response**: (Same as Calculate Salaries)

### 3. Process Salary Transfer
- **Endpoint**: `POST /payroll/transfer`
- **Description**: Transfer salaries from company account to employee accounts
- **Request Body**:
```json
{
  "employeeIds": ["1001", "1002", "1003"],
  "grade6Basic": 25000
}
```
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Salary transfer completed",
  "data": {
    "transferResults": [
      {
        "employeeId": "1001",
        "name": "John A",
        "salaryAmount": 67500,
        "status": "SUCCESS",
        "transferredAt": "2025-01-01T12:00:00Z"
      },
      {
        "employeeId": "1002",
        "name": "Jane B",
        "salaryAmount": 62500,
        "status": "FAILED",
        "reason": "Insufficient company balance"
      }
    ],
    "totalTransferred": 67500,
    "totalFailed": 62500,
    "companyBalanceAfter": 32500
  }
}
```

### 4. Get Salary Sheet
- **Endpoint**: `GET /payroll/salary-sheet`
- **Description**: Get complete salary sheet for all employees
- **Query Parameters**:
  - `grade6Basic` (number): Basic salary for grade 6
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Salary sheet generated successfully",
  "data": {
    "employees": [
      {
        "id": "1001",
        "name": "John A",
        "grade": 1,
        "salary": {
          "basic": 50000,
          "houseRent": 10000,
          "medicalAllowance": 7500,
          "gross": 67500,
          "isPaid": true,
          "paidAt": "2025-01-01T12:00:00Z"
        }
      }
    ],
    "summary": {
      "totalEmployees": 10,
      "totalSalaryRequired": 450000,
      "totalPaid": 300000,
      "totalPending": 150000,
      "companyBalance": 100000
    },
    "generatedAt": "2025-01-01T12:00:00Z"
  }
}
```

---

## 🏦 Company Account APIs

### 1. Get Company Account Details
- **Endpoint**: `GET /company/account`
- **Description**: Get company account balance and details
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Company account details retrieved",
  "data": {
    "accountNumber": "COMP-001",
    "accountName": "Company Main Account",
    "currentBalance": 500000,
    "bank": "Central Bank",
    "branch": "Head Office",
    "lastUpdated": "2025-01-01T12:00:00Z"
  }
}
```

### 2. Top-up Company Account
- **Endpoint**: `POST /company/topup`
- **Description**: Add money to company account
- **Request Body**:
```json
{
  "amount": 100000,
  "description": "Monthly fund allocation"
}
```
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Account topped up successfully",
  "data": {
    "previousBalance": 500000,
    "topupAmount": 100000,
    "newBalance": 600000,
    "transactionId": "TXN-20250101120000",
    "timestamp": "2025-01-01T12:00:00Z"
  }
}
```

### 3. Get Transaction History
- **Endpoint**: `GET /company/transactions`
- **Description**: Get company account transaction history
- **Query Parameters**:
  - `limit` (number, optional): Number of transactions to return (default: 50)
  - `offset` (number, optional): Pagination offset (default: 0)
- **Response** (200 OK):
```json
{
  "success": true,
  "message": "Transaction history retrieved",
  "data": {
    "transactions": [
      {
        "id": "TXN-20250101120000",
        "type": "TOPUP",
        "amount": 100000,
        "description": "Monthly fund allocation",
        "balanceAfter": 600000,
        "timestamp": "2025-01-01T12:00:00Z"
      },
      {
        "id": "TXN-20250101110000",
        "type": "SALARY_TRANSFER",
        "amount": -67500,
        "description": "Salary transfer to John A (1001)",
        "balanceAfter": 500000,
        "timestamp": "2025-01-01T11:00:00Z"
      }
    ],
    "totalCount": 25,
    "hasMore": true
  }
}
```

---

## 📊 Validation Rules

### Employee Validation
- **Employee ID**: Exactly 4 digits, must be unique
- **Name**: Required, 2-50 characters
- **Grade**: Must be 1-6, respect grade distribution limits:
  - Grade 1: Max 1 employee
  - Grade 2: Max 1 employee  
  - Grade 3: Max 2 employees
  - Grade 4: Max 2 employees
  - Grade 5: Max 2 employees
  - Grade 6: Max 2 employees
- **Mobile**: 11 digits starting with 017/018/019/015/016/013/014
- **Bank Account Number**: 10-20 digits

### Salary Calculation Rules
- **Basic Salary Formula**: `grade6Basic + (6 - grade) * 5000`
- **House Rent**: 20% of basic salary
- **Medical Allowance**: 15% of basic salary
- **Gross Salary**: Basic + House Rent + Medical Allowance

### Business Rules
- Total employees: Exactly 10
- Company account balance must be sufficient for salary transfers
- Employee ID must be 4 digits and unique across the system

---

## 🚨 Error Response Format

All error responses follow this format:
```json
{
  "success": false,
  "message": "Error description",
  "data": {
    "errorCode": "VALIDATION_ERROR",
    "details": ["Specific error messages"],
    "timestamp": "2025-01-01T12:00:00Z"
  }
}
```

### Common Error Codes
- `VALIDATION_ERROR`: Input validation failed
- `EMPLOYEE_NOT_FOUND`: Employee ID doesn't exist
- `GRADE_LIMIT_EXCEEDED`: Grade distribution limit reached
- `INSUFFICIENT_BALANCE`: Company account has insufficient funds
- `DUPLICATE_EMPLOYEE_ID`: Employee ID already exists
- `UNAUTHORIZED`: Invalid or missing JWT token
- `FORBIDDEN`: User doesn't have required permissions

---

## 🧪 Test Data

### Default Admin Credentials
```
Username: admin
Password: admin123
```

### Sample Employee Data (10 employees as per requirement)
- Grade 1: 1 employee (ID: 1001)
- Grade 2: 1 employee (ID: 1002)  
- Grade 3: 2 employees (IDs: 1003, 1004)
- Grade 4: 2 employees (IDs: 1005, 1006)
- Grade 5: 2 employees (IDs: 1007, 1008)
- Grade 6: 2 employees (IDs: 1009, 1010)

### Default Company Account
```
Initial Balance: 500,000 BDT
Account Number: COMP-001
Bank: Central Bank
```

---

## 📝 Notes for Backend Implementation

1. **JWT Token**: Should expire in 24 hours
2. **Database**: Maintain referential integrity between Employee and BankAccount
3. **Transactions**: Use database transactions for salary transfers
4. **Logging**: Log all salary transfers and account modifications
5. **Validation**: Enforce all business rules at API level
6. **Error Handling**: Return consistent error response format
7. **Security**: Validate JWT tokens on all protected endpoints
8. **Performance**: Add pagination for large data sets