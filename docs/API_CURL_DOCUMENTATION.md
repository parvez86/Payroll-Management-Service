# Payroll Management System - API Documentation with cURL Examples

## Base Configuration
- **Base URL**: `http://localhost:20001/pms`
- **Content-Type**: `application/json`
- **Authentication**: Bearer JWT Token (required for most endpoints)

## Prerequisites

### 1. Get JWT Token (Login)
```bash
# Admin Login
curl -X POST "http://localhost:20001/pms/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Save the JWT token from response for use in other APIs
export JWT_TOKEN="YOUR_JWT_TOKEN_HERE"
```

---

## 1. Authentication APIs

### 1.1 User Registration (Admin Only)
```bash
curl -X POST "http://localhost:20001/pms/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "username": "newuser",
    "email": "newuser@company.com",
    "password": "password123",
    "role": "EMPLOYEE"
  }'
```

### 1.2 Refresh Token
```bash
curl -X POST "http://localhost:20001/pms/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### 1.3 Logout
```bash
curl -X POST "http://localhost:20001/pms/api/v1/auth/logout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN",
    "logoutFromAllDevices": false
  }'
```

---

## 2. Company Management APIs

### 2.1 Create Company
```bash
curl -X POST "http://localhost:20001/pms/api/v1/companies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "TechCorp Bangladesh Ltd",
    "description": "Leading software development company",
    "salaryFormulaId": "SALARY_FORMULA_UUID",
    "mainAccountDetails": {
      "accountName": "Main Payroll Account",
      "accountNumber": "COMP001",
      "branchId": "BRANCH_UUID",
      "initialBalance": 1000000.00
    }
  }'
```

### 2.2 Get Company by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/companies/{companyId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 2.3 Update Company
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/companies/{companyId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Updated Company Name",
    "description": "Updated description"
  }'
```

### 2.4 Delete Company
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/companies/{companyId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 2.5 Search Companies
```bash
curl -X GET "http://localhost:20001/pms/api/v1/companies?name=TechCorp&page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 2.6 Company Account Top-up
```bash
curl -X POST "http://localhost:20001/pms/api/v1/companies/{companyId}/topup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "amount": 500000.00,
    "description": "Monthly budget top-up"
  }'
```

### 2.7 Get Company Account Balance
```bash
curl -X GET "http://localhost:20001/pms/api/v1/companies/{companyId}/account" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 2.8 Get Company Transaction History
```bash
curl -X GET "http://localhost:20001/pms/api/v1/companies/{companyId}/transactions?page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 3. Employee Management APIs

### 3.1 Create Employee
```bash
curl -X POST "http://localhost:20001/pms/api/v1/employees" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "bizId": "1001",
    "name": "Ahmed Rahman",
    "mobile": "01711123456",
    "address": "Gulshan-2, Dhaka",
    "gradeId": "GRADE_UUID",
    "companyId": "COMPANY_UUID",
    "userId": "USER_UUID",
    "accountDetails": {
      "accountName": "Ahmed Rahman",
      "accountNumber": "EMP001",
      "accountType": "SAVINGS",
      "branchId": "BRANCH_UUID"
    }
  }'
```

### 3.2 Get All Employees
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees?page=0&size=20&sort=grade.rank" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.3 Get Employee by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees/{employeeId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.4 Get Employee by Business ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees/biz-id/1001" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.5 Get Employees by Grade
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees/grade/{gradeId}?page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.6 Update Employee
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/employees/{employeeId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Updated Name",
    "mobile": "01711999999",
    "address": "Updated Address",
    "gradeId": "NEW_GRADE_UUID"
  }'
```

### 3.7 Delete Employee
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/employees/{employeeId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.8 Get Employee Count by Grade
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees/stats/count-by-grade" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3.9 Get Total Employee Count
```bash
curl -X GET "http://localhost:20001/pms/api/v1/employees/stats/total-count" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 4. Payroll Management APIs

### 4.1 Create Payroll Batch
```bash
curl -X POST "http://localhost:20001/pms/api/v1/payroll/batches" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "January 2025 Payroll",
    "description": "Monthly salary disbursement for January",
    "companyId": "COMPANY_UUID",
    "payPeriodStart": "2025-01-01",
    "payPeriodEnd": "2025-01-31"
  }'
```

### 4.2 Get All Payroll Batches
```bash
curl -X GET "http://localhost:20001/pms/api/v1/payroll/batches?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.3 Get Payroll Batch by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/payroll/batches/{batchId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.4 Get Payroll Items for Batch
```bash
curl -X GET "http://localhost:20001/pms/api/v1/payroll/batches/{batchId}/items?page=0&size=50" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.5 Calculate Salaries (Preview)
```bash
curl -X GET "http://localhost:20001/pms/api/v1/payroll/batches/{batchId}/calculate" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.6 Calculate Salaries for Company
```bash
curl -X GET "http://localhost:20001/pms/api/v1/payroll/companies/{companyId}/calculate" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.7 Process Payroll (Execute Payments)
```bash
curl -X POST "http://localhost:20001/pms/api/v1/payroll/batches/{batchId}/process" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.8 Retry Failed Payroll Item
```bash
curl -X POST "http://localhost:20001/pms/api/v1/payroll/items/{payrollItemId}/retry" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 4.9 Cancel Payroll Batch
```bash
curl -X POST "http://localhost:20001/pms/api/v1/payroll/batches/{batchId}/cancel" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 5. Grade Management APIs

### 5.1 Create Grade
```bash
curl -X POST "http://localhost:20001/pms/api/v1/grades" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Grade 1",
    "rank": 1,
    "description": "Senior Management Level"
  }'
```

### 5.2 Get Grade by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/grades/{gradeId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 5.3 Update Grade
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/grades/{gradeId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Updated Grade Name",
    "description": "Updated description"
  }'
```

### 5.4 Delete Grade
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/grades/{gradeId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 5.5 Search Grades
```bash
curl -X GET "http://localhost:20001/pms/api/v1/grades?page=0&size=20&sort=rank" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 6. Bank Management APIs

### 6.1 Create Bank
```bash
curl -X POST "http://localhost:20001/pms/api/v1/banks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Bangladesh Bank",
    "countryCode": "BD",
    "swiftBicCode": "BBHOBDDHXXX"
  }'
```

### 6.2 Get Bank by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/banks/{bankId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 6.3 Update Bank
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/banks/{bankId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Updated Bank Name",
    "countryCode": "BD",
    "swiftBicCode": "UPDATED_BIC"
  }'
```

### 6.4 Delete Bank
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/banks/{bankId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 6.5 Search Banks
```bash
curl -X GET "http://localhost:20001/pms/api/v1/banks?name=Bangladesh&page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 7. Branch Management APIs

### 7.1 Create Branch
```bash
curl -X POST "http://localhost:20001/pms/api/v1/branches" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "branchName": "Motijheel Branch",
    "address": "Motijheel Commercial Area, Dhaka",
    "bankId": "BANK_UUID"
  }'
```

### 7.2 Get Branch by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/branches/{branchId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 7.3 Update Branch
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/branches/{branchId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "branchName": "Updated Branch Name",
    "address": "Updated Address"
  }'
```

### 7.4 Delete Branch
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/branches/{branchId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 7.5 Search Branches
```bash
curl -X GET "http://localhost:20001/pms/api/v1/branches?bankId={bankId}&page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 8. Account Management APIs

### 8.1 Create Account
```bash
curl -X POST "http://localhost:20001/pms/api/v1/accounts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "ownerType": "EMPLOYEE",
    "ownerId": "OWNER_UUID",
    "accountType": "SAVINGS",
    "accountName": "Employee Account",
    "accountNumber": "ACC001",
    "branchId": "BRANCH_UUID",
    "initialBalance": 0.00
  }'
```

### 8.2 Get Account by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/accounts/{accountId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 8.3 Update Account
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/accounts/{accountId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "accountName": "Updated Account Name",
    "overdraftLimit": 10000.00
  }'
```

### 8.4 Delete Account
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/accounts/{accountId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 8.5 Search Accounts
```bash
curl -X GET "http://localhost:20001/pms/api/v1/accounts?ownerType=EMPLOYEE&page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 8.6 Deposit Funds
```bash
curl -X POST "http://localhost:20001/pms/api/v1/accounts/{accountId}/deposit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "amount": 50000.00,
    "description": "Salary deposit"
  }'
```

### 8.7 Withdraw Funds
```bash
curl -X POST "http://localhost:20001/pms/api/v1/accounts/{accountId}/withdraw" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "amount": 10000.00,
    "description": "Withdrawal request"
  }'
```

---

## 9. Transaction Management APIs

### 9.1 Execute Money Transfer
```bash
curl -X POST "http://localhost:20001/pms/api/v1/transactions/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "debitAccountId": "DEBIT_ACCOUNT_UUID",
    "creditAccountId": "CREDIT_ACCOUNT_UUID",
    "amount": 75000.00,
    "description": "Salary transfer",
    "referenceId": "PAY_REF_001"
  }'
```

### 9.2 Get Account Balance
```bash
curl -X GET "http://localhost:20001/pms/api/v1/transactions/accounts/{accountId}/balance" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 9.3 Get Transaction History
```bash
curl -X GET "http://localhost:20001/pms/api/v1/transactions/accounts/{accountId}/history?page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 9.4 Get Transaction by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/transactions/{transactionId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 9.5 Search Transactions
```bash
curl -X GET "http://localhost:20001/pms/api/v1/transactions?status=COMPLETED&type=PAYROLL_DISBURSEMENT&page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 9.6 Reverse Transaction
```bash
curl -X POST "http://localhost:20001/pms/api/v1/transactions/{transactionId}/reverse" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "reason": "Incorrect amount transferred",
    "description": "Reversing salary overpayment"
  }'
```

---

## 10. Salary Distribution Formula APIs

### 10.1 Create Salary Formula
```bash
curl -X POST "http://localhost:20001/pms/api/v1/salary-formulas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Standard Bangladesh Formula",
    "baseSalaryGrade": 6,
    "hraPercentage": 0.20,
    "medicalPercentage": 0.15,
    "gradeIncrementAmount": 5000.00
  }'
```

### 10.2 Get Salary Formula by ID
```bash
curl -X GET "http://localhost:20001/pms/api/v1/salary-formulas/{formulaId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 10.3 Update Salary Formula
```bash
curl -X PUT "http://localhost:20001/pms/api/v1/salary-formulas/{formulaId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Updated Formula",
    "hraPercentage": 0.25,
    "medicalPercentage": 0.20
  }'
```

### 10.4 Delete Salary Formula
```bash
curl -X DELETE "http://localhost:20001/pms/api/v1/salary-formulas/{formulaId}" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 10.5 Search Salary Formulas
```bash
curl -X GET "http://localhost:20001/pms/api/v1/salary-formulas?page=0&size=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 11. Health Check APIs

### 11.1 Basic Health Check
```bash
curl -X GET "http://localhost:20001/pms/health"
```

### 11.2 System Information
```bash
curl -X GET "http://localhost:20001/pms/health/info"
```

---

## Sample Usage Workflow

### 1. Login and Setup
```bash
# 1. Login as admin
curl -X POST "http://localhost:20001/pms/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 2. Set JWT token
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 3. Check system health
curl -X GET "http://localhost:20001/pms/health"
```

### 2. Create Basic Data
```bash
# 1. Create bank
BANK_ID=$(curl -X POST "http://localhost:20001/pms/api/v1/banks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"name": "Test Bank", "countryCode": "BD", "swiftBicCode": "TESTBDDHXXX"}' | jq -r '.id')

# 2. Create branch
BRANCH_ID=$(curl -X POST "http://localhost:20001/pms/api/v1/branches" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{\"branchName\": \"Main Branch\", \"address\": \"Test Address\", \"bankId\": \"$BANK_ID\"}" | jq -r '.id')

# 3. Create salary formula
FORMULA_ID=$(curl -X POST "http://localhost:20001/pms/api/v1/salary-formulas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"name": "Test Formula", "baseSalaryGrade": 6, "hraPercentage": 0.20, "medicalPercentage": 0.15, "gradeIncrementAmount": 5000.00}' | jq -r '.id')
```

### 3. Run Payroll Process
```bash
# 1. Calculate salaries (preview)
curl -X GET "http://localhost:20001/pms/api/v1/payroll/companies/{companyId}/calculate" \
  -H "Authorization: Bearer $JWT_TOKEN"

# 2. Create payroll batch
BATCH_ID=$(curl -X POST "http://localhost:20001/pms/api/v1/payroll/batches" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"name": "Test Payroll", "description": "Test run", "companyId": "'$COMPANY_ID'", "payPeriodStart": "2025-01-01", "payPeriodEnd": "2025-01-31"}' | jq -r '.id')

# 3. Process payroll
curl -X POST "http://localhost:20001/pms/api/v1/payroll/batches/$BATCH_ID/process" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## Error Handling Examples

### Common Error Responses
```json
{
  "timestamp": "2025-01-20T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/employees"
}
```

### Authentication Error
```json
{
  "timestamp": "2025-01-20T10:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/v1/employees"
}
```

### Insufficient Funds Error
```json
{
  "timestamp": "2025-01-20T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient company account balance for payroll processing",
  "path": "/api/v1/payroll/batches/{batchId}/process"
}
```

---

## Notes

1. **Authentication**: Most APIs require JWT token. Login first and use the token in subsequent requests.

2. **Pagination**: Use `page`, `size`, and `sort` query parameters for paginated endpoints.

3. **UUID Format**: All ID fields expect UUID format (e.g., `550e8400-e29b-41d4-a716-446655440000`).

4. **Date Format**: Use ISO 8601 format for dates (e.g., `2025-01-20T10:30:00.000Z`).

5. **Amount Format**: Use decimal numbers for monetary amounts (e.g., `75000.00`).

6. **Error Handling**: Always check response status codes and handle errors appropriately.

7. **ACID Transactions**: Payroll processing uses ACID-compliant transactions with automatic rollback on failures.

8. **Business Rules**: 
   - Maximum 10 employees with grade distribution 1,1,2,2,2,2
   - Employee IDs must be exactly 4 digits
   - Account numbers must be unique across the system

---

**Version**: 1.0.0  
**Last Updated**: October 20, 2025  
**Base URL**: http://localhost:20001/pms  
**Environment**: Development