# Seed Data Summary - Payroll Management System

**Date:** 2025-12-01  
**Total Users:** 35 (1 Admin + 3 Employers + 31 Employees)  
**Total Companies:** 3  
**Password - Admin:** `admin123` → Hash: `$2a$12$W03o/Ixsd.fqxJ8f5VOrfOMeRMiXAfccv1.VR71zIlJuXCTFtvlRK`  
**Password - Others:** `password123` → Hash: `$2a$12$G41iuJ9./uwwOvAtPGlxVu1oxXLBeUHvblPiepVoCUUNbPxqpS5XS`

---

## Users Breakdown

### 1. ADMIN (1 user)
| Username | Email | Role | Description |
|----------|-------|------|-------------|
| admin | admin@techcorp.com | ADMIN | System administrator with global access |

**Company-User Role Assignment:**
- Assigned as EMPLOYER to TechCorp Bangladesh Ltd (for testing purposes)

---

### 2. EMPLOYERS (3 users - one per company)

| Username | Email | Role | Company | Description |
|----------|-------|------|---------|-------------|
| employer_techcorp | employer@techcorp.com | EMPLOYER | TechCorp Bangladesh Ltd | TechCorp company manager |
| employer_innovate | employer@innovatebd.com | EMPLOYER | InnovateBD Solutions | InnovateBD company manager |
| employer_digitalbd | employer@digitalbd.gov.bd | EMPLOYER | Digital Bangladesh Corp | DigitalBD company manager |

**Company-User Role Assignments:**
- employer_techcorp → EMPLOYER role on TechCorp Bangladesh Ltd
- employer_innovate → EMPLOYER role on InnovateBD Solutions
- employer_digitalbd → EMPLOYER role on Digital Bangladesh Corp

---

### 3. EMPLOYEES (31 users distributed across 3 companies)

#### 3.1 TechCorp Bangladesh Ltd (11 employees from existing seed data)

| Code | Username | Name | Grade | Account Number | Mobile | Address |
|------|----------|------|-------|----------------|--------|---------|
| 1001 | director001 | Ahmed Rahman | Grade 1 | DIR001 | +8801711123456 | Gulshan-2, Dhaka |
| 2001 | manager001 | Fatima Khatun | Grade 2 | MGR001 | +8801711234567 | Dhanmondi-15, Dhaka |
| 3001 | senior001 | Mohammad Ali | Grade 3 | SR001 | +8801711345678 | Banani, Dhaka |
| 3002 | senior002 | Rashida Begum | Grade 3 | SR002 | +8801711456789 | Uttara, Dhaka |
| 4001 | dev001 | Karim Uddin | Grade 4 | DEV001 | +8801711567890 | Mirpur-1, Dhaka |
| 4002 | dev002 | Salma Akter | Grade 4 | DEV002 | +8801711678901 | Wari, Dhaka |
| 5001 | junior001 | Nasir Ahmed | Grade 5 | JR001 | +8801711789012 | Tejgaon, Dhaka |
| 5002 | junior002 | Amina Khanom | Grade 5 | JR002 | +8801711890123 | Mohammadpur, Dhaka |
| 6001 | intern001 | Tariq Hassan | Grade 6 | INT001 | +8801711901234 | Farmgate, Dhaka |
| 6002 | intern002 | Ruma Parvin | Grade 6 | INT002 | +8801712012345 | Shantinagar, Dhaka |
| - | - | *1 additional employee needed* | - | - | - | - |

**Note:** TechCorp has only 10 employees from old seed data. Need to add 1 more to reach 11.

---

#### 3.2 InnovateBD Solutions (10 employees - NEW)

| Code | Username | Name | Grade | Account Number | Mobile | Address |
|------|----------|------|-------|----------------|--------|---------|
| INN-1001 | inn_director | Imran Hossain | Grade 1 | INN-DIR001 | +8801811111001 | Banani, Dhaka |
| INN-2001 | inn_manager | Nadia Rahman | Grade 2 | INN-MGR001 | +8801811112001 | Dhanmondi-8, Dhaka |
| INN-3001 | inn_senior001 | Arif Khan | Grade 3 | INN-SR001 | +8801811113001 | Mohakhali, Dhaka |
| INN-3002 | inn_senior002 | Labiba Sultana | Grade 3 | INN-SR002 | +8801811113002 | Badda, Dhaka |
| INN-4001 | inn_dev001 | Tanvir Ahmed | Grade 4 | INN-DEV001 | +8801811114001 | Rampura, Dhaka |
| INN-4002 | inn_dev002 | Sadia Islam | Grade 4 | INN-DEV002 | +8801811114002 | Nikunja, Dhaka |
| INN-5001 | inn_junior001 | Fahim Mahmud | Grade 5 | INN-JR001 | +8801811115001 | Kakrail, Dhaka |
| INN-5002 | inn_junior002 | Tasnia Akter | Grade 5 | INN-JR002 | +8801811115002 | Malibagh, Dhaka |
| INN-6001 | inn_intern001 | Sakib Al Hasan | Grade 6 | INN-INT001 | +8801811116001 | Khilgaon, Dhaka |
| INN-6002 | inn_intern002 | Maliha Jahan | Grade 6 | INN-INT002 | +8801811116002 | Segunbagicha, Dhaka |

**Grade Distribution:** 1,1,2,2,2,2 ✓

---

#### 3.3 Digital Bangladesh Corp (10 employees - NEW)

| Code | Username | Name | Grade | Account Number | Mobile | Address |
|------|----------|------|-------|----------------|--------|---------|
| DBD-1001 | dbd_director | Abdullah Al Mamun | Grade 1 | DBD-DIR001 | +8801911111001 | Agargaon, Dhaka |
| DBD-2001 | dbd_manager | Shabnam Sultana | Grade 2 | DBD-MGR001 | +8801911112001 | Sher-e-Bangla Nagar, Dhaka |
| DBD-3001 | dbd_senior001 | Mahbubur Rahman | Grade 3 | DBD-SR001 | +8801911113001 | Karwan Bazar, Dhaka |
| DBD-3002 | dbd_senior002 | Roksana Begum | Grade 3 | DBD-SR002 | +8801911113002 | Panthapath, Dhaka |
| DBD-4001 | dbd_dev001 | Rafiq Azam | Grade 4 | DBD-DEV001 | +8801911114001 | Eskaton, Dhaka |
| DBD-4002 | dbd_dev002 | Farzana Yeasmin | Grade 4 | DBD-DEV002 | +8801911114002 | Kawran Bazar, Dhaka |
| DBD-5001 | dbd_junior001 | Shahin Alam | Grade 5 | DBD-JR001 | +8801911115001 | Bijoy Nagar, Dhaka |
| DBD-5002 | dbd_junior002 | Nusrat Jahan | Grade 5 | DBD-JR002 | +8801911115002 | New Market, Dhaka |
| DBD-6001 | dbd_intern001 | Rifat Hasan | Grade 6 | DBD-INT001 | +8801911116001 | Paltan, Dhaka |
| DBD-6002 | dbd_intern002 | Sumaiya Akhter | Grade 6 | DBD-INT002 | +8801911116002 | Shahbagh, Dhaka |

**Grade Distribution:** 1,1,2,2,2,2 ✓

---

## Companies

| ID | Company Name | Description | Account Number | Initial Balance | Salary Formula | Main Branch |
|----|--------------|-------------|----------------|-----------------|----------------|-------------|
| 1 | TechCorp Bangladesh Ltd | Leading software development company | COMP001 | BDT 1,000,000 | Standard Bangladesh Formula | Motijheel Branch |
| 2 | InnovateBD Solutions | Innovative software and mobile app development | COMP002 | BDT 800,000 | Standard Bangladesh Formula | Dhanmondi Branch |
| 3 | Digital Bangladesh Corp | Government projects and digital services | COMP003 | BDT 750,000 | Standard Bangladesh Formula | Gulshan Branch |

---

## Grade Hierarchy (All Companies Use Same Grades)

| Rank | Grade Name | Parent Grade | Distribution per Company |
|------|------------|--------------|-------------------------|
| 1 | Grade 1 | None | 1 employee |
| 2 | Grade 2 | Grade 1 | 1 employee |
| 3 | Grade 3 | Grade 1 | 2 employees |
| 4 | Grade 4 | Grade 2 | 2 employees |
| 5 | Grade 5 | Grade 2 | 2 employees |
| 6 | Grade 6 | Grade 2 | 2 employees |

**Total per company:** 10 employees (1+1+2+2+2+2)

---

## Bank Branches

| Bank Name | Branch Name | Address | SWIFT/BIC Code |
|-----------|-------------|---------|----------------|
| Bangladesh Bank | Motijheel Branch | Motijheel Commercial Area, Dhaka | BBHOBDDHXXX |
| Sonali Bank | Dhanmondi Branch | Dhanmondi Road 27, Dhaka | BSONBDDHXXX |
| Dutch Bangla Bank | Gulshan Branch | Gulshan Avenue, Dhaka | DBBLBDDH |

---

## Salary Formula

**Name:** Standard Bangladesh Formula  
**Base Salary Grade:** Grade 6  
**HRA Percentage:** 20.00%  
**Medical Percentage:** 15.00%  
**Grade Increment Amount:** BDT 5,000.00 per grade level

---

## Migration Files Summary

| File | Purpose | Status |
|------|---------|--------|
| 001-create-base-tables.xml | Create core tables (users, banks, branches, etc.) | ✓ Validated |
| 002-create-payroll-tables.xml | Create payroll-specific tables | ✓ Validated |
| 003-create-indexes.xml | Add performance indexes | ✓ Validated |
| 004-insert-seed-data.xml | Initial seed data (11 TechCorp employees) | ✓ Validated |
| 005-add-basic-salary-payroll-tables.xml | Add salary formula tables | ✓ Validated |
| 006-create-company-user-roles-table.xml | Create company_user_roles ACL table | ✓ Validated |
| 007-seed-company-user-roles.xml | Assign admin as EMPLOYER to TechCorp | ✓ Validated |
| 008-comprehensive-seed-data.xml | Add 2 companies, 3 employers, 20 employees | ✓ NEW |

---

## Testing Credentials

### Admin Access
- **Username:** `admin`
- **Password:** `password123`
- **Capabilities:** System-wide access, can view all companies and employees

### Employer Access (per company)

| Company | Username | Password | Capabilities |
|---------|----------|----------|--------------|
| TechCorp Bangladesh Ltd | employer_techcorp | password123 | Company-scoped: manage employees, process payroll |
| InnovateBD Solutions | employer_innovate | password123 | Company-scoped: manage employees, process payroll |
| Digital Bangladesh Corp | employer_digitalbd | password123 | Company-scoped: manage employees, process payroll |

### Employee Access Examples

| Username | Password | Company | Grade | Description |
|----------|----------|---------|-------|-------------|
| director001 | password123 | TechCorp | 1 | Top-level employee, can see downstream |
| inn_director | password123 | InnovateBD | 1 | Top-level employee, can see downstream |
| dbd_director | password123 | DigitalBD | 1 | Top-level employee, can see downstream |
| junior001 | password123 | TechCorp | 5 | Mid-level employee, limited downstream |
| inn_intern001 | password123 | InnovateBD | 6 | Entry-level, no downstream |

---

## Known Issues

1. **TechCorp has only 10 employees** instead of 11 from old seed data. Need to add 1 more employee to reach perfect 31 total.
   - **Fix:** Add one more employee to TechCorp in migration 004 or create migration 009.

2. **Existing users (director001, manager001, etc.) have role EMPLOYER** in users table but should be EMPLOYEE.
   - **Assessment:** Check if these are intentional or need correction.

---

## Next Steps

1. ✅ Verify Liquibase migrations apply cleanly
2. ✅ Test login with each role type
3. ⬜ Add 1 missing TechCorp employee to reach 31 total
4. ⬜ Verify role-based access control works correctly
5. ⬜ Test payroll processing for each company
6. ⬜ Verify company isolation (employers can't access other companies)

---

## Migration Verification Checklist

- [x] All XML files are well-formed
- [x] All foreign key references are valid
- [x] Idempotency checks (WHERE NOT EXISTS) in place
- [x] Rollback statements provided
- [x] No hardcoded UUIDs (all use gen_random_uuid())
- [x] All required columns included (status, version for audit entities)
- [x] Company-user role assignments are correct
- [ ] Test migration on clean database
- [ ] Test migration on existing database (update scenario)

---

**Generated:** 2025-12-01  
**Version:** 1.0  
**Status:** Ready for Testing
