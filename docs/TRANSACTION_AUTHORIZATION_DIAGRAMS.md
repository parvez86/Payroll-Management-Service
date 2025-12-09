# Transaction Authorization - Architecture & Flow Diagrams

## 1. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       REST API Client                           │
│        (Admin/Employer/Employee makes request)                 │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│           Spring Security Filter Chain                           │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  1. Authentication Filter                                 │ │
│  │     ├─ Extract JWT token                                  │ │
│  │     ├─ Validate token signature                           │ │
│  │     └─ Set SecurityContext with user principal            │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  2. Authorization Filter                                  │ │
│  │     ├─ Check @PreAuthorize("hasAnyRole(...)")             │ │
│  │     └─ Verify user has required role                      │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              TransactionController                               │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  GET /transactions                                         │ │
│  │  ├─ @PreAuthorize("hasAnyRole('ADMIN','EMPLOYER',         │ │
│  │  │                           'EMPLOYEE')")                 │ │
│  │  ├─ @AuthenticationPrincipal HeaderResponse principal      │ │
│  │  └─ → Call TransactionService.getTransactionHistory()     │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│            TransactionServiceImpl                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  getTransactionHistory(filter, principal, pageable)        │ │
│  │  ├─ 1. Call authorizationService.getCompanyFilterForUser() │ │
│  │  │     └─ Returns: UUID (company) or null (for ADMIN)      │ │
│  │  ├─ 2. Build Specification<Transaction> with filters       │ │
│  │  │     ├─ Add: company_id filter (if not ADMIN)            │ │
│  │  │     └─ Add: other filters (status, amount, etc)         │ │
│  │  ├─ 3. Execute: transactionRepository.findAll(spec, page) │ │
│  │  └─ 4. Map to response DTOs                                │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│      TransactionAuthorizationService                             │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  getCompanyFilterForUser(HeaderResponse principal)         │ │
│  │                                                             │ │
│  │  ├─ if (role == ADMIN)                                    │ │
│  │  │   └─ return null  (no filter, see all)                 │ │
│  │  │                                                         │ │
│  │  ├─ if (role == EMPLOYER)                                 │ │
│  │  │   ├─ Query: CompanyUserRoleRepository                  │ │
│  │  │   ├─ Find: Companies where user is EMPLOYER            │ │
│  │  │   └─ return: company_id (typically single)             │ │
│  │  │                                                         │ │
│  │  └─ if (role == EMPLOYEE)                                 │ │
│  │      ├─ Query: EmployeeRepository                         │ │
│  │      ├─ Find: Employee by user_id                         │ │
│  │      └─ return: employee's company_id                     │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│      TransactionRepository                                       │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  findAll(Specification<Transaction> spec, Pageable page)   │ │
│  │                                                             │ │
│  │  Specification builds WHERE clause:                         │ │
│  │  ┌─ For ADMIN:                                             │ │
│  │  │  SELECT * FROM transactions                             │ │
│  │  │  ORDER BY created_at DESC LIMIT 20                      │ │
│  │  │                                                          │ │
│  │  ├─ For EMPLOYER:                                          │ │
│  │  │  SELECT * FROM transactions                             │ │
│  │  │  WHERE company_id = 'uuid-123'                          │ │
│  │  │  ORDER BY created_at DESC LIMIT 20                      │ │
│  │  │                                                          │ │
│  │  └─ For EMPLOYEE:                                          │ │
│  │     SELECT * FROM transactions                             │ │
│  │     WHERE company_id = 'uuid-123'                          │ │
│  │     AND (debit_account_id = 'uuid-acc' OR                  │ │
│  │         credit_account_id = 'uuid-acc')                    │ │
│  │     ORDER BY created_at DESC LIMIT 20                      │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│            PostgreSQL Database                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  transactions table                                        │ │
│  │  ├─ Indexed on: company_id                                │ │
│  │  ├─ Indexed on: company_id, transaction_status            │ │
│  │  └─ Foreign Key: company_id → companies(id)               │ │
│  │                                                             │ │
│  │  Query results filtered at DB level (efficient!)           │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│            HTTP Response (JSON)                                  │
│  {                                                               │
│    "content": [ transaction1, transaction2, ... ],               │
│    "totalElements": 42,                                          │
│    "pageable": { "pageNumber": 0, "pageSize": 20 },              │
│    "last": false                                                 │
│  }                                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Authorization Decision Tree

```
                    ┌─ Request arrives ─┐
                    │ with JWT token    │
                    └────────┬──────────┘
                             │
                    ┌────────▼──────────┐
                    │ Extract principal │
                    │ from JWT token    │
                    └────────┬──────────┘
                             │
                    ┌────────▼──────────┐
                    │ What is user's    │
                    │ role?             │
                    └────────┬──────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
                ▼            ▼            ▼
        ┌───────────┐ ┌───────────┐ ┌──────────┐
        │   ADMIN   │ │ EMPLOYER  │ │ EMPLOYEE │
        └─────┬─────┘ └─────┬─────┘ └────┬─────┘
              │             │            │
        ┌─────▼──────┐ ┌────▼──────┐ ┌──▼───────┐
        │ No filter  │ │ Filter by │ │ Filter by│
        │ (null)     │ │ company_id│ │ company &│
        │            │ │ (THEIRS)  │ │ account  │
        └─────┬──────┘ └────┬──────┘ └──┬───────┘
              │             │            │
              └─────────────┼────────────┘
                            │
                    ┌───────▼────────┐
                    │ Build Spec with│
                    │ WHERE clauses  │
                    └───────┬────────┘
                            │
                    ┌───────▼────────┐
                    │ Execute query  │
                    │ at DB level    │
                    └───────┬────────┘
                            │
                    ┌───────▼────────┐
                    │ Return filtered│
                    │ results to user│
                    └────────────────┘
```

---

## 3. Database Schema (Transactions)

```
┌─────────────────────────────────────────────────┐
│          transactions (table)                    │
├────────────────────────┬────────┬────────────────┤
│ Column Name            │ Type   │ Key            │
├────────────────────────┼────────┼────────────────┤
│ id                     │ UUID   │ PK             │
│ debit_account_id       │ UUID   │ FK → accounts  │
│ credit_account_id      │ UUID   │ FK → accounts  │
│ amount                 │ DECIMAL│                │
│ transaction_status     │ VARCHAR│ Indexed        │
│ company_id ⭐          │ UUID   │ FK → companies │
│ created_at             │ TIMESTAMP │            │
│ updated_at             │ TIMESTAMP │            │
│ created_by             │ UUID   │                │
│ updated_by             │ UUID   │                │
└────────────────────────┴────────┴────────────────┘
              ⭐ = NEW COLUMN (added by migration)

Indexes Created:
┌─────────────────────────────────────────────────┐
│ Index Name                                      │
├─────────────────────────────────────────────────┤
│ idx_transactions_company_id                    │
│   ON (company_id)                              │
│                                                 │
│ idx_transactions_company_status                │
│   ON (company_id, transaction_status)          │
└─────────────────────────────────────────────────┘

Foreign Keys:
┌──────────────────────────────────────────────────┐
│ fk_transaction_company                           │
│   transactions.company_id → companies.id         │
│   ON DELETE: RESTRICT                            │
└──────────────────────────────────────────────────┘
```

---

## 4. Request Flow Examples

### Example 1: ADMIN Request
```
ADMIN Login
  │
  ├─ Token created with role=ADMIN
  │
  └─ GET /transactions
     │
     ├─ Controller receives request
     │  ├─ @PreAuthorize("hasAnyRole(...'ADMIN'...)") ✓ PASS
     │  └─ Forward to Service
     │
     ├─ Service.getTransactionHistory()
     │  ├─ Call authService.getCompanyFilterForUser(admin_principal)
     │  │  └─ Returns: null (ADMIN has no filter)
     │  ├─ Build Specification
     │  │  └─ No company_id WHERE clause
     │  └─ Query: "SELECT * FROM transactions ORDER BY created_at DESC"
     │
     └─ Returns: ALL transactions (from all companies)
        ├─ TechCorp: 15 transactions
        ├─ InnovateBD: 10 transactions
        └─ DhakaBiz: 6 transactions
```

### Example 2: EMPLOYER Request
```
EMPLOYER (TechCorp) Login
  │
  ├─ Token created with role=EMPLOYER, userId=uuid-emp
  │
  └─ GET /transactions
     │
     ├─ Controller receives request
     │  ├─ @PreAuthorize("hasAnyRole(...'EMPLOYER'...)") ✓ PASS
     │  └─ Forward to Service with principal
     │
     ├─ Service.getTransactionHistory(filter, employer_principal, page)
     │  ├─ Call authService.getCompanyFilterForUser(employer_principal)
     │  │  ├─ Query: SELECT company_id FROM company_user_roles
     │  │  │           WHERE user_id = 'uuid-emp' AND role = 'EMPLOYER'
     │  │  └─ Returns: 'uuid-techcorp' (their company)
     │  ├─ Build Specification
     │  │  └─ Add WHERE: company_id = 'uuid-techcorp'
     │  └─ Query: "SELECT * FROM transactions 
     │            WHERE company_id = 'uuid-techcorp'
     │            ORDER BY created_at DESC"
     │
     └─ Returns: ONLY TechCorp transactions
        ├─ TechCorp: 15 transactions (✓ visible)
        ├─ InnovateBD: 0 transactions (✗ filtered out)
        └─ DhakaBiz: 0 transactions (✗ filtered out)
```

### Example 3: EMPLOYEE Request
```
EMPLOYEE (director001) Login
  │
  ├─ Token created with role=EMPLOYEE, userId=uuid-dir
  │
  └─ GET /transactions
     │
     ├─ Controller receives request
     │  ├─ @PreAuthorize("hasAnyRole(...'EMPLOYEE'...)") ✓ PASS
     │  └─ Forward to Service with principal
     │
     ├─ Service.getTransactionHistory(filter, employee_principal, page)
     │  ├─ Call authService.getCompanyFilterForUser(employee_principal)
     │  │  ├─ Query: SELECT employee WHERE user_id = 'uuid-dir'
     │  │  ├─ Get: company_id = 'uuid-techcorp', account_id = 'uuid-acc-123'
     │  │  └─ Returns: 'uuid-techcorp' (their company)
     │  ├─ Build Specification
     │  │  ├─ Add WHERE: company_id = 'uuid-techcorp'
     │  │  └─ Add WHERE: (debit_account_id = 'uuid-acc-123' 
     │  │                 OR credit_account_id = 'uuid-acc-123')
     │  └─ Query: "SELECT * FROM transactions 
     │            WHERE company_id = 'uuid-techcorp'
     │            AND (debit_account_id = 'uuid-acc-123'
     │                 OR credit_account_id = 'uuid-acc-123')
     │            ORDER BY created_at DESC"
     │
     └─ Returns: ONLY their account's transactions
        ├─ Received salary: 1 transaction ✓
        ├─ Transfer out: 0 transactions
        └─ Other employees' transfers: all filtered ✗
```

---

## 5. Security Layers

```
Layer 1: ROUTE LEVEL
┌──────────────────────────────────────────┐
│ @PreAuthorize("hasAnyRole('ADMIN'...)")  │
│ Spring Security checks role annotation   │
│ ❌ No match → 403 Forbidden               │
│ ✓ Match → continue to next layer          │
└──────────────────────────────────────────┘
           │
           ▼
Layer 2: SERVICE LEVEL
┌──────────────────────────────────────────┐
│ authService.getCompanyFilterForUser()    │
│ Determines data scope for user           │
│ Returns: company_id (or null for ADMIN)  │
└──────────────────────────────────────────┘
           │
           ▼
Layer 3: QUERY LEVEL
┌──────────────────────────────────────────┐
│ Specification adds WHERE clauses         │
│ Database executes filtered query         │
│ Only matching rows returned to app       │
└──────────────────────────────────────────┘
           │
           ▼
Layer 4: ENTITY LEVEL
┌──────────────────────────────────────────┐
│ Foreign key constraints                  │
│ company_id → companies(id) mandatory     │
│ Prevents orphaned transactions           │
└──────────────────────────────────────────┘

If ANY layer fails → Request blocked
```

---

## 6. Company Isolation

```
┌─────────────────────────────────────────────────────┐
│              TechCorp Bangladesh Ltd                │
│              company_id = 'uuid-1'                  │
├─────────────────────────────────────────────────────┤
│  Transactions (6 total)                             │
│  ├─ TXN-001: Transfer to employee #1                │
│  ├─ TXN-002: Transfer to employee #2                │
│  ├─ TXN-003: Employee reimbursement                 │
│  ├─ TXN-004: Salary batch #1                        │
│  ├─ TXN-005: Salary batch #1 continued              │
│  └─ TXN-006: Interest deposit                       │
│                                                      │
│  Access:                                             │
│  ✓ ADMIN - can see all 6                            │
│  ✓ EMPLOYER (TechCorp) - can see all 6              │
│  ✓ EMPLOYEE (TechCorp) - can see only their own     │
│  ✗ EMPLOYER (InnovateBD) - cannot see any           │
│  ✗ EMPLOYEE (InnovateBD) - cannot see any           │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              InnovateBD Solutions                    │
│              company_id = 'uuid-2'                  │
├─────────────────────────────────────────────────────┤
│  Transactions (4 total)                             │
│  ├─ TXN-101: Transfer to employee #1                │
│  ├─ TXN-102: Salary batch #1                        │
│  ├─ TXN-103: Office expense reimbursement           │
│  └─ TXN-104: Annual bonus                           │
│                                                      │
│  Access:                                             │
│  ✓ ADMIN - can see all 4                            │
│  ✗ EMPLOYER (TechCorp) - cannot see any             │
│  ✓ EMPLOYER (InnovateBD) - can see all 4            │
│  ✗ EMPLOYEE (TechCorp) - cannot see any             │
│  ✓ EMPLOYEE (InnovateBD) - can see only their own   │
└─────────────────────────────────────────────────────┘

Perfect Isolation ✓
```

---

## 7. Permission Matrix

```
                    │ ADMIN │ EMPLOYER │ EMPLOYEE │
────────────────────┼───────┼──────────┼──────────┤
View own account    │  ✓    │    ✓     │    ✓     │
View company        │  ✓    │    ✓     │    ✗     │
accounts            │       │          │          │
View all global     │  ✓    │    ✗     │    ✗     │
transactions        │       │          │          │
View company        │  ✓    │    ✓     │    ✗     │
transactions        │       │          │          │
View own            │  ✓    │    ✗     │    ✓     │
transactions        │       │          │          │
Create transaction  │  ✓    │    ✓     │    ✗     │
Approve transaction │  ✓    │    ✓     │    ✗     │
Reverse transaction │  ✓    │    ✗     │    ✗     │
────────────────────┼───────┼──────────┼──────────┤
```

---

## 8. Migration Process

```
Before Migration (v0.0.1)
┌────────────────────────┐
│ transactions table     │
├────────────────────────┤
│ NO company_id column   │
│ NO company isolation   │
│ NO role-based filtering│
└────────────────────────┘
           │
           ▼
   Run Migration
   011-add-company-to-transactions.xml
           │
      ┌────┴────┐
      │          │
      ▼          ▼
  Step 1:    Step 2:
  Add column Backfill
  company_id from source
      │         │
      └────┬────┘
           │
           ▼
  Step 3: Add FK constraint
           │
           ▼
  Step 4: Create indexes
           │
           ▼
After Migration (v0.0.2)
┌────────────────────────┐
│ transactions table     │
├────────────────────────┤
│ ✓ company_id column    │
│ ✓ FK constraint        │
│ ✓ Performance indexes  │
│ ✓ Company isolation    │
│ ✓ Role-based filtering │
└────────────────────────┘
```

---

## 9. Query Execution Plan

```
User Request
  │
  ├─ Authentication ✓
  │
  ├─ Authorization ✓
  │
  └─ Service Processing
     │
     ├─ getCompanyFilterForUser()
     │  └─ Determines: NULL (admin) or UUID (employer/employee)
     │
     ├─ buildSpecification()
     │  └─ Creates JPA Specification with WHERE clauses
     │
     └─ transactionRepository.findAll(spec, pageable)
        │
        ├─ Hibernate converts Specification to SQL
        │  │
        │  └─ SELECT * FROM transactions
        │     [WHERE company_id = ?]  ← Added if not ADMIN
        │     [WHERE account_id IN (...)]  ← For EMPLOYEE
        │     ORDER BY created_at DESC
        │     LIMIT 20
        │
        ├─ Database Query Optimizer
        │  │
        │  └─ Use index: idx_transactions_company_id
        │     └─ Fast lookup of company's transactions
        │
        └─ Return Page<Transaction>
           │
           ├─ Map to TransactionResponse DTOs
           │
           └─ Serialize to JSON
              │
              └─ Return to client
```

---

This completes the Transaction Authorization Implementation with comprehensive diagrams showing the system architecture, flow, and security layers.
