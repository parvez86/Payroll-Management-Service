# Payroll Management System – Dashboard & RBAC Architecture Review

Date: 2025-11-30
Author: Project Architecture Review
Status: Approved for implementation

## 1. Executive Summary
The system requires a consolidated, role-aware presentation layer with strongly enforced backend authorization. Three principal roles (ADMIN, EMPLOYER, EMPLOYEE) demand differentiated data visibility and action capabilities across: Dashboard, Employee List, Payroll, Accounts, and Transactions. This document defines:
- Role capability matrix
- KPI definitions per page and role
- Required service & repository adaptations
- API endpoint strategy
- Performance & integrity considerations
- Implementation roadmap

The guiding principles:
1. **Security First** – All filtering and action gating occurs server-side (AuthorizationService + domain services). UI never decides access unilaterally.
2. **Least Privilege** – EMPLOYEE is strictly read-only except for viewing self + downstream hierarchy.
3. **Deterministic Aggregations** – KPIs use stable, tested repository queries or materialized cache layers; no ad-hoc joins in controllers.
4. **Idempotent Seeding / Migrations** – Liquibase manages schema; no runtime schema drift.
5. **Extensibility** – Additional roles (e.g. AUDITOR) can be integrated by adding scope definitions without redesigning core services.

---
## 2. Roles & Scope Summary
| Role      | Data Visibility | Mutations Allowed | Company Scope | Hierarchy Scope |
|-----------|-----------------|-------------------|---------------|-----------------|
| ADMIN     | All companies, all employers & employees | None (supervisory – optionally allowed for system ops) | Global | All |
| EMPLOYER  | Own company employees, accounts, payroll | Create/Process/Cancel payroll, manage employees | Own company only | Direct employees (company-wide) |
| EMPLOYEE  | Self + downstream employees, own + downstream transactions/payroll items | None | Only implied by own employee record | Self + downstream chain |

Downstream determination: A tree based on grade/manager relationships (existing `getDownstreamEmployeeIds`).

---
## 3. Page / Feature Specifications
### 3.1 Dashboard / Employee Page
KPIs vary by role:
- ADMIN:
  - System Balance (aggregate of all company funding accounts + optional system holding account)
  - Total Companies
  - Total Employers
  - Total Employees
  - Company-wise cards: {Company Name, Employee Count, Active Payroll Batches (PENDING/PROCESSING), Outstanding Payroll (sum unpaid items), Company Balance}
- EMPLOYER:
  - Company Balance (funding account balance)
  - My Balance (employer’s personal account if applicable)
  - Employee Counts by grade/hierarchy
  - Outstanding payroll liability (sum of PENDING + PROCESSING items)
- EMPLOYEE:
  - My Balance (personal account)
  - Company Balance (optional – show/hide based on product spec; can remove for strict minimalism)
  - Downstream Employee Count
  - Own Outstanding Pay (items not PAID) + Downstream Outstanding Pay

### 3.2 Employee List
- ADMIN: All employees across all companies; filters by company, grade, status.
- EMPLOYER: Only employees belonging to employer’s company.
- EMPLOYEE: Only self + downstream (no CRUD). Action buttons (Add/Edit/Delete/Assign) hidden/disabled.

### 3.3 Payroll Page
Actions:
- Create / Process / Cancel / Retry: ADMIN + EMPLOYER only (if we permit ADMIN operational power; else EMPLOYER only).
- Salary Sheet Overview filtered by companyId (ADMIN can select any company; EMPLOYER fixed to own; EMPLOYEE no company-level filter—scoped to self + downstream). 

KPIs Definitions:
- ADMIN:
  - Total Pay To Be: Sum of amounts for items with status ∈ {PENDING, PROCESSING}
  - Total Paid: Sum of PAID items in time window (month or batch scope)
  - Companies Account Balance: Sum of all funding accounts current balances
  - System Account Balance: Dedicated system account (if modeled) + any global float
- EMPLOYER:
  - Total Pay To Be (company): Same as above but for one company
  - Total Paid (company): PAID items for company in period
  - Company Balance: Funding account balance
- EMPLOYEE:
  - Own Pay To Be: Sum of own items not PAID
  - Own Paid: Sum of own PAID items
  - Downstream Pay To Be: Sum of unpaid items for downstream employees
  - Downstream Paid: Sum of paid items for downstream employees

### 3.4 Batch Synchronization
Fields: `batchId`, `batchName`, `status`, `payrollMonth`
- Ensure both list and detail views derive from same projection query to avoid stale divergence.
- Introduce DTO `PayrollBatchSyncView` eliminating repeated calculations at controller.

### 3.5 My Account Page
- ADMIN: View all company funding accounts + system account(s); read-only.
- EMPLOYER: View company funding account + personal account (if modeled). Provide links to transactions.
- EMPLOYEE: View personal account + optionally derived payroll history summary.

### 3.6 Transactions Page
- Filters (date range, type, category, status) available to all roles but results are constrained by role scope.
- ADMIN: All transactions (optionally filter by company). 
- EMPLOYER: Transactions where debit or credit account belongs to own company’s employees or company funding account.
- EMPLOYEE: Transactions where debit or credit account belongs to self or downstream employees.

---
## 4. Domain & Data Model Considerations
Existing Entities: `User`, `Company`, `Employee`, `Account`, `PayrollBatch`, `PayrollItem`, `CompanyUserRole`.
Required Additions / Clarifications:
- `CompanyUserRole`: Already extended with auditing; confirm `status`, `version` columns exist in Liquibase.
- Hierarchical employee relationship: If not explicit, add `manager_employee_id` (nullable FK) or derive from grade order + mapping table.
- System Account: Add optional `accounts` row with `owner_type=SYSTEM` for global metrics.

---
## 5. Repository Enhancements
Add targeted aggregation queries (prefer native SQL or JPQL for precision):
- PayrollItemRepository:
  - `BigDecimal sumAmountByCompanyIdAndStatus(UUID companyId, Set<PayrollItemStatus> statuses)`
  - `BigDecimal sumAmountByEmployeeIdInAndStatus(Collection<UUID> employeeIds, Set<PayrollItemStatus> statuses)`
  - `List<PayrollItem> findAllByEmployee_IdInAndPayrollBatch_PayrollMonth(Collection<UUID> employeeIds, YearMonth month)`
- AccountRepository:
  - `BigDecimal sumCompanyFundingBalances()` (ADMIN)
  - `BigDecimal sumCompanyBalance(UUID companyId)`
  - `BigDecimal getSystemAccountBalance()`
- TransactionRepository:
  - Paged queries constrained by sets of account IDs.

Leverage JPA Specifications for compound filters: status, payrollMonth, companyId, employeeId sets.

---
## 6. Service Layer Adaptations
### 6.1 AuthorizationService (central contract)
Methods:
- `RoleScope getRoleScope(UUID userId)` returns structured scope: {role, companyIds, employeeIds, downstreamEmployeeIds}
- `boolean canMutatePayroll(UUID userId, UUID companyId)` role + company check
- `Set<UUID> visibleEmployeeIds(UUID userId, UUID targetCompanyId)` returns permissible employee set.

### 6.2 PayrollService Additions
- `EmployeeSalaryOverview getSalaryOverview(UUID userId)` – calculates four EMPLOYEE KPIs.
- `CompanyPayrollOverview getCompanyOverview(UUID companyId)` – used by EMPLOYER and ADMIN.
- Input guard for mutators: throw `AccessDeniedException` if `canMutatePayroll` false.

### 6.3 TransactionService
- Filter inbound queries by authorized account IDs derived from visible employee IDs + company funding account.

### 6.4 AccountService
- Provide consolidated balance responses per role scope.

---
## 7. API Endpoints (Proposed)
| Endpoint | Method | Roles | Description |
|----------|--------|-------|-------------|
| `/admin/overview` | GET | ADMIN | System-wide KPIs and company breakdown |
| `/companies/{id}/overview` | GET | ADMIN, EMPLOYER | Company payroll + balance KPIs |
| `/payroll/overview/self` | GET | EMPLOYEE | Own + downstream salary overview |
| `/payroll/batches` | GET | ADMIN, EMPLOYER, EMPLOYEE | Filtered by scope (EMPLOYEE limited) |
| `/payroll/batches` | POST | ADMIN?, EMPLOYER | Create batch |
| `/payroll/batches/{id}/process` | POST | ADMIN?, EMPLOYER | Process batch |
| `/payroll/batches/{id}/cancel` | POST | ADMIN?, EMPLOYER | Cancel batch |
| `/payroll/items/self-and-downstream` | GET | EMPLOYEE | Items for self + downstream |
| `/transactions` | GET | ALL | Scoped filtering |
| `/accounts/balances` | GET | ALL | Role-specific balances |

Note: Decide whether ADMIN should perform mutations. If not, remove POST privileges for ADMIN.

---
## 8. Security & Enforcement Layers
1. Controller – obtains `HeaderResponse principal`.
2. Service – calls AuthorizationService to derive permitted sets; never trusts client-supplied filters fully.
3. Repository – queries constrained by provided sets (avoid fetching all then filtering in memory).
4. Exception Handling – `AccessDeniedException` mapped to 403 with structured body `{ code: "ACCESS_DENIED", message, timestamp }`.
5. Auditing – log denied attempt including userId, endpoint, requested resource ids.

---
## 9. Performance & Caching
- KPIs (system/company overview) can be cached (e.g. Caffeine) with short TTL (30–60s) for ADMIN dashboard.
- Aggregations use index coverage: ensure indexes on `(company_id, payroll_status)`, `(employee_id, payroll_item_status)`, accounts table primary PK.
- Avoid N+1 when loading batches: use fetch joins or projection queries for batch list.

---
## 10. Liquibase & Schema Integrity
- Confirm final `company_user_roles` table includes: `id, company_id, user_id, role_on_company, access_scope, active, status, version, valid_from, valid_to, created_at, created_by, updated_at, updated_by`.
- Future migration: add `manager_employee_id` column in `employees` if hierarchical chain is not explicit.
- Idempotent seeding: Keep only admin role insert guarded via WHERE NOT EXISTS.

---
## 11. Testing Strategy
### Unit
- AuthorizationService: role scope derivation logic (build synthetic hierarchies).
- PayrollService: KPIs return correct sums with mixed statuses.
### Integration
- Role-based endpoints return correct HTTP status (403 vs 200).
- Liquibase migrations apply cleanly to empty and pre-populated DB.
### Security
- Attempt mutation as EMPLOYEE -> expect 403.
- Cross-company access attempt by EMPLOYER -> expect 403.
### Performance Smoke
- Aggregation queries scale with >10k payroll items.

---
## 12. Implementation Roadmap
| Step | Description | Owner | Effort |
|------|-------------|-------|--------|
| 1 | Add repository aggregation methods | Backend | S |
| 2 | Extend AuthorizationService contract | Backend | S |
| 3 | Implement Salary/Company overview DTOs & services | Backend | M |
| 4 | Guard existing Payroll/Transaction mutators | Backend | S |
| 5 | Create new endpoints (`/admin/overview`, `/payroll/overview/self`) | Backend | S |
| 6 | UI adjustments – hide/show actions per role | Frontend | M |
| 7 | Add caching layer for admin KPIs | Backend | S |
| 8 | Add hierarchical employee tests | QA | M |
| 9 | Load test payroll aggregation queries | DevOps | M |

---
## 13. Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| Incorrect hierarchy calculation | Data leakage | Centralize downstream logic; test edge cases (cycles, null managers) |
| Missing indexes for aggregations | Slow dashboards | Add composite indexes; analyze with EXPLAIN |
| Frontend bypass attempt | Unauthorized mutation | Server-side gating only; never rely on UI |
| Liquibase drift vs entity | Startup failure | Keep entity-to-schema diff tests in CI |

---
## 14. Glossary
- **Downstream Employees**: Subordinate employees in a hierarchy beneath the requesting employee.
- **Funding Account**: Company main account used for salary disbursements.
- **Outstanding Payroll**: Sum of payroll item amounts not yet marked PAID.

---
## 15. Summary
This architecture upgrades the system to a robust, principle-driven RBAC implementation with clear separation of concerns, deterministic aggregations, and scalable querying. Implementation is incremental, low-risk, and positions the platform for future roles and compliance features.

---
## 16. Next Actions
Proceed with Step 1 (repository aggregations) and Step 2 (AuthorizationService extensions), then integrate endpoints and UI gates.
