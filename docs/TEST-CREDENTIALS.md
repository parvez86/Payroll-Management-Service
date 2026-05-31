# Test User Credentials - Payroll Management System

## ✅ VERIFIED CREDENTIALS (Updated May 31, 2026)

**Authentication:** BCrypt (Cost 12) - Spring Security  
**Admin Password:** `admin123`  
**All Other Users:** `password123`

## ADMIN Role (1 user)
| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | System administrator with global access |

## EMPLOYER Role (3 users)
| Username | Password | Role | Company | Description |
|----------|----------|------|---------|-------------|
| employer_techcorp | password123 | EMPLOYER | TechCorp Bangladesh Ltd | CEO of TechCorp |
| employer_innovate | password123 | EMPLOYER | InnovateBD Solutions | CEO of InnovateBD |
| employer_digitalbd | password123 | EMPLOYER | Digital Bangladesh Corp | CEO of DigitalBD |

## EMPLOYEE Role (31 users)
| Username | Password | Role | Company | Grade | Description |
|----------|----------|------|---------|-------|-------------|
| director001 | password123 | EMPLOYEE | TechCorp Bangladesh Ltd | 1 | Director |
| manager001 | password123 | EMPLOYEE | TechCorp Bangladesh Ltd | 2 | Manager |
| inn_director | password123 | EMPLOYEE | InnovateBD Solutions | 1 | Director |
| inn_manager | password123 | EMPLOYEE | InnovateBD Solutions | 2 | Manager |

---

## Quick Login Credentials by Role Type

### For Testing ADMIN Access:
```
Username: admin
Password: admin123
```

### For Testing EMPLOYER Access (Choose any):
```
1) TechCorp CEO:
   Username: employer_techcorp
   Password: password123

2) InnovateBD CEO:
   Username: employer_innovate
   Password: password123

3) Digital Bangladesh CEO:
   Username: employer_digitalbd
   Password: password123
```

### For Testing EMPLOYEE Access (Choose any 2):
```
1) TechCorp Director:
   Username: director001
   Password: password123

2) TechCorp Manager:
   Username: manager001
   Password: password123

3) InnovateBD Director:
   Username: inn_director
   Password: password123

4) InnovateBD Manager:
   Username: inn_manager
   Password: password123
```

---

## All Users Summary

### Total: 35 Users
- **1 ADMIN**: admin (password: admin123)
- **3 EMPLOYERS**: password123
  - employer_techcorp (TechCorp Bangladesh Ltd)
  - employer_innovate (InnovateBD Solutions)
  - employer_digitalbd (Digital Bangladesh Corp) 
- **31 EMPLOYEES**: password123 (Distributed across 3 companies)

### Company Distribution:
1. **TechCorp Bangladesh Ltd**: 11 employees
2. **InnovateBD Solutions**: 10 employees
3. **Digital Bangladesh Corp**: 10 employees

---

## Password Configuration

**ADMIN Password:** `admin123`  
**BCrypt Hash:** `$2a$12$W03o/Ixsd.fqxJ8f5VOrfOMeRMiXAfccv1.VR71zIlJuXCTFtvlRK`

**All Other Users Password:** `password123`  
**BCrypt Hash:** `$2a$12$G41iuJ9./uwwOvAtPGlxVu1oxXLBeUHvblPiepVoCUUNbPxqpS5XS`

In production, enforce:
- Unique passwords per user
- Strong password policies
- Mandatory password change on first login
- MFA for ADMIN and EMPLOYER roles

## API Login Endpoint
```
POST /pms/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid",
  "username": "admin",
  "role": "ADMIN",
  "expiresIn": 86400
}
```

---

## Testing Scenarios

### Scenario 1: Admin Access
- Login as: `admin / password123`
- Expected: View all companies, employers, employees
- Can: View system-wide dashboard
- Cannot: Process payroll (read-only policy)

### Scenario 2: Employer Access  
- Login as: `employer_techcorp / password123`
- Expected: View only TechCorp employees
- Can: Create/process payroll for own company
- Cannot: View other companies (InnovateBD, Digital Bangladesh)

### Scenario 3: Employee Access
- Login as: `director001 / password123`
- Expected: View only self + downstream subordinates
- Can: View own salary details and reports
- Cannot: Edit employee data, process payroll, create batches

---

## Database Verification Queries

### Check all users by role:
```sql
SELECT username, email, role 
FROM users 
ORDER BY 
  CASE role 
    WHEN 'ADMIN' THEN 1 
    WHEN 'EMPLOYER' THEN 2 
    WHEN 'EMPLOYEE' THEN 3 
  END, 
  username;
```

### Check company-user role assignments:
```sql
SELECT 
  u.username,
  u.role as user_role,
  c.name as company,
  cur.role_on_company,
  cur.active
FROM company_user_roles cur
JOIN users u ON cur.user_id = u.id
JOIN companies c ON cur.company_id = c.id
WHERE cur.active = true
ORDER BY c.name, u.username;
```

### Check employee distribution:
```sql
SELECT 
  c.name as company,
  COUNT(e.id) as employee_count
FROM companies c
LEFT JOIN employees e ON e.company_id = c.id
GROUP BY c.name
ORDER BY employee_count DESC;
```
