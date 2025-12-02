# UI Review and Required Improvements

Date: 2025-11-30
Reviewer: Project Architect
Status: Pending Implementation

---

## Executive Summary

This document reviews the current UI implementation based on 5 screenshots and identifies gaps, missing features, and improvements needed to align with the RBAC architecture. The UI currently shows only the ADMIN role perspective and lacks critical role-based differentiation for EMPLOYER and EMPLOYEE roles.

---

## 1. Current Implementation Status

### ✅ What's Working Well

1. **Admin Role Basic Structure**
   - System Balance visible in header (BDT 412,750)
   - Company Account tab with System & Companies Overview
   - Transaction filtering interface present
   - Clean tab navigation (Employees, Payroll, Company Account, Transactions)

2. **Payroll Page Foundation**
   - Payroll Processing section with Grade 6 base salary input
   - Salary Sheet Overview with 3 KPI cards:
     - Pay to be Amount: BDT 587,250
     - Total Paid Amount: BDT 587,250
     - Company Account Balance: BDT 412,750
   - Batch information display (ID, Name, Status, Month)
   - Detailed salary breakdown table with grades, components, amounts, and status

3. **Employee Management**
   - Employee list with ID, Grade, Contact Info, Bank Account, Balance
   - Action buttons (Edit, Delete)
   - Pagination controls
   - Status filter (Active)

4. **Transaction History**
   - Comprehensive filtering:
     - Type, Category, Status
     - Debit Account ID, Credit Account ID
     - Batch ID
     - Date Range (From/To)
   - Apply Filters and Reset buttons
   - Transaction list with Date, Type, Batch ID, Batch Item ID, Debit Account ID, From Account
   - Pagination (Page 1 of 2, 11 total)

5. **Company Account Page**
   - System Balance hero section (BDT 412,750)
   - "Top Up System Account" action button
   - Account Information collapsible section

---

## 2. Critical Missing Features

### 2.1 Role-Based UI Differentiation

**Problem:** All screenshots show "ADMIN" badge only. No demonstration of EMPLOYER or EMPLOYEE views.

**Required:** Show 3 distinct UI states for each role:

#### ADMIN View Requirements
- [ ] Dashboard showing system-wide metrics
- [ ] All companies selector/list
- [ ] All employers visibility
- [ ] Read-only access indicators
- [ ] No operational mutation buttons (optional policy)

#### EMPLOYER View Requirements
- [ ] Company-scoped data only
- [ ] Company Balance + Personal Balance in header
- [ ] Employee list filtered to own company
- [ ] Payroll processing buttons visible
- [ ] Transaction filters scoped to company accounts

#### EMPLOYEE View Requirements
- [ ] Strictly read-only interface
- [ ] Self + downstream employees only
- [ ] No "Add Employee" button
- [ ] No payroll processing section
- [ ] 4-card salary overview (Own Paid, Own Unpaid, Downstream Paid, Downstream Unpaid)
- [ ] Edit/Delete action buttons disabled/hidden
- [ ] Transaction list filtered to own + downstream accounts

---

### 2.2 Dashboard/Employee Page (Screenshot 1) Missing Features

#### Header Area
- [ ] **Company Balance / My Balance KPIs** not shown
  - ADMIN: Should show "Total Companies: X", "Total Employers: Y"
  - EMPLOYER: Should show "Company Balance" + "My Balance"
  - EMPLOYEE: Should show "My Balance" + "Downstream Employees: X"

#### Employee List
- [ ] **Status Filter** only shows "Active" - missing:
  - INACTIVE option
  - ALL option
  - ON_LEAVE option (if applicable)
- [ ] **Company Filter** for ADMIN role (select which company to view)
- [ ] **Hierarchy Indicator** for downstream relationships
  - Tree icon or indentation
  - "Reports To" column
- [ ] **Export Button** (filtered by role scope)
- [ ] **Bulk Actions** (select multiple employees)
- [ ] **Search Bar** for employee name/ID/email
- [ ] **Add Employee Button** should be:
  - Visible for ADMIN/EMPLOYER
  - Hidden for EMPLOYEE

#### Data Display
- [ ] **No visual indication** this list is filtered by role
- [ ] **Grade badges** lack consistent color scheme
- [ ] **Last Salary Date** column missing
- [ ] **Employment Status** indicator missing

---

### 2.3 Payroll Page (Screenshot 2) Missing Features

#### Role-Specific Sections
- [ ] **Payroll Processing Section** should be:
  - Visible for ADMIN/EMPLOYER
  - **Completely hidden** for EMPLOYEE (not just disabled)
- [ ] **Company Filter** for ADMIN to view different companies

#### Salary Sheet Overview KPIs
Current shows 3 cards. Required variations:

**ADMIN Role:**
- [ ] Add: System Account Balance card
- [ ] Add: Companies Account Balance (aggregate)
- [ ] Add: Total Pay to Be (system-wide)
- [ ] Add: Total Paid (system-wide)

**EMPLOYER Role:** (Current implementation)
- [x] Pay to be Amount
- [x] Total Paid Amount  
- [x] Company Account Balance

**EMPLOYEE Role:**
- [ ] Replace with 4 cards:
  - Own Pay To Be
  - Own Paid
  - Downstream Pay To Be
  - Downstream Paid
- [ ] Remove Company Account Balance (privacy)

#### Batch Information
- [ ] **Batch Synchronization** - ensure ID, Name, Status, Month are always in sync between list and detail views
- [ ] **Status Badges** need distinct colors:
  - PENDING: Orange
  - PROCESSING: Blue  
  - COMPLETED: Green
  - FAILED: Red
  - CANCELLED: Gray
- [ ] **Batch Total Amount** column missing
- [ ] **Employee Count** column missing
- [ ] **View Items** action button per batch

#### Salary Breakdown Table
- [ ] **Status Column** shows only checkmark icon - need text labels
- [ ] **Hover Tooltips** for failed items showing error reason
- [ ] **Expandable Rows** for detailed breakdown
- [ ] **Total Row** at bottom showing sums

---

### 2.4 Company Account Page (Screenshots 3-4) Critical Gaps

#### Current Issues
- [ ] **Account Information Section** is collapsed/empty - no actual data shown
- [ ] **Single View Only** - doesn't adapt to role

#### ADMIN Requirements
- [ ] **System Account Section:**
  - Current Balance
  - Transaction Count
  - Last Top-Up Date
  - Top-Up History (last 5)
- [ ] **Companies List View:**
  - Tabbed interface: [System Account] [Company: TechCorp] [Company: XYZ]
  - Per-company cards showing:
    - Company Name
    - Funding Account Balance
    - Employee Count
    - Outstanding Payroll
    - Last Payroll Date
- [ ] **Aggregate Metrics:**
  - Total System Balance
  - Total Companies
  - Total Outstanding Liability

#### EMPLOYER Requirements
- [ ] **Company Funding Account:**
  - Account Number
  - Current Balance
  - Available Balance (after pending payroll)
  - Account Type
  - Branch Information
- [ ] **Personal Account:**
  - Account Number
  - Current Balance
  - Account Type
- [ ] **Quick Actions:**
  - Top Up Company Account
  - View Transaction History
  - Transfer Funds

#### EMPLOYEE Requirements
- [ ] **Personal Account Only:**
  - Account Number (partially masked)
  - Current Balance
  - Last Salary Date
  - Next Expected Salary
- [ ] **Read-Only View** - no Top Up button
- [ ] **Transaction History Link** (filtered to own transactions)

#### Missing Functionality
- [ ] **Top Up System Account** button should be role-restricted:
  - ADMIN: Yes
  - EMPLOYER: Yes (for own company)
  - EMPLOYEE: No (hidden)
- [ ] **Top Up Modal** (Screenshot 4) validation:
  - Shows minimum 1,000 BDT and maximum 10,000 BDT
  - Need to clarify if these limits are per-transaction or daily/monthly
  - Need to show current balance before top-up
  - Need confirmation screen after "Add Funds"
- [ ] **Account Activity Timeline** missing
- [ ] **Low Balance Warning** indicator

---

### 2.5 Transaction Page (Screenshot 5) Improvements Needed

#### Filter Enhancements
- [ ] **Company Filter** for ADMIN role (select which company's transactions)
- [ ] **Filter Context Labels** should show role scope:
  - ADMIN: "Filter by Company"
  - EMPLOYER: "(Your company employees only)"
  - EMPLOYEE: "(Your transactions + downstream only)"
- [ ] **Account ID Inputs** should be dropdowns, not text:
  - Debit Account ID → Searchable dropdown (filtered by role scope)
  - Credit Account ID → Searchable dropdown (filtered by role scope)
- [ ] **Batch ID** should be dropdown/autocomplete showing recent batches
- [ ] **Quick Filters** buttons:
  - Today
  - This Week
  - This Month
  - Last 30 Days
- [ ] **Advanced Filters** collapse/expand section
- [ ] **Save Filter Preset** functionality

#### Data Display Issues
- [ ] **Amount Column Missing** - critical data not shown!
- [ ] **Balance After Column** missing
- [ ] **From Account** shows "TechCorp Account" but no account number
- [ ] **To Account Column** missing
- [ ] **Transaction Details** truncated - need expandable rows or detail modal
- [ ] **Total Transaction Amount Sum** not displayed

#### Missing Functionality
- [ ] **Search Bar** for transaction reference/description
- [ ] **Export Transactions** button (CSV/PDF)
- [ ] **Transaction Status** indicators (PENDING, COMPLETED, FAILED, REVERSED)
- [ ] **Transaction Type Icons** for visual scanning
- [ ] **Bulk Selection** for exporting specific transactions

---

## 3. UI/UX Improvements

### 3.1 Header Area Redesign

**Current:** Only shows "System Balance: BDT 412,750"

**Proposed Layout by Role:**

```
ADMIN Header:
┌─────────────────────────────────────────────────────────────────┐
│ 💰 System Balance: BDT 412,750  │  🏢 Companies: 3  │  👔 Employers: 5  │
│ username: admin [ADMIN 🔴]                               [Logout] │
└─────────────────────────────────────────────────────────────────┘

EMPLOYER Header:
┌─────────────────────────────────────────────────────────────────┐
│ 🏢 Company Balance: BDT 200,000  │  💰 My Balance: BDT 50,000  │
│ username: employer [EMPLOYER 🔵]                         [Logout] │
└─────────────────────────────────────────────────────────────────┘

EMPLOYEE Header:
┌─────────────────────────────────────────────────────────────────┐
│ 💰 My Balance: BDT 25,000  │  👥 Downstream: 3 employees  │
│ username: employee [EMPLOYEE 🟢]                         [Logout] │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Role Badge Enhancement

- [ ] **Color Coding:**
  - ADMIN: Red badge
  - EMPLOYER: Blue badge
  - EMPLOYEE: Green badge
- [ ] **Tooltip** on hover explaining current role scope
- [ ] **Role Icon** prefix (crown for ADMIN, briefcase for EMPLOYER, user for EMPLOYEE)

### 3.3 Navigation Context

- [ ] **Breadcrumbs** showing current location
  - Example: "Home > Payroll > November 2025 Batch > Employee Details"
- [ ] **Active Tab** clear visual indicator
- [ ] **Role-Restricted Tabs** should be hidden (not just disabled)
  - Example: EMPLOYEE should not see "Company Account" tab

### 3.4 Salary Sheet Overview Card Improvements

- [ ] **Visual Hierarchy:**
  - Larger font for amounts
  - Currency symbol styling
  - Thousand separators
- [ ] **Trend Indicators:**
  - Show ↑↓ compared to last month
  - Percentage change
  - Green for positive, red for negative
- [ ] **Loading States:**
  - Skeleton loaders while aggregating
  - Last updated timestamp
- [ ] **Refresh Button** for manual data refresh
- [ ] **Info Icon** with tooltip explaining what each metric means

### 3.5 Employee List Table Enhancements

- [ ] **Grade Badge Consistency:**
  - All grades use same visual style
  - Color gradient based on rank (Grade 1 = dark, Grade 6 = light)
- [ ] **Hierarchy Visualization:**
  - Tree icon for employees with subordinates
  - Indentation for downstream levels
  - Expand/collapse for hierarchy tree view
- [ ] **Action Button States:**
  - For EMPLOYEE role: Replace edit/delete icons with view-only icon
  - Disabled state styling (grayed out)
  - Tooltip explaining why action is unavailable
- [ ] **Row Hover Effects:**
  - Highlight on hover
  - Show quick actions popup
- [ ] **Column Sorting:**
  - Clickable column headers
  - Sort indicators (↑↓)
- [ ] **Row Selection:**
  - Checkboxes for bulk operations
  - "Select All" option
  - Bulk action dropdown

### 3.6 Payroll Batch Table Improvements

- [ ] **Status Badges** with semantic colors:
  ```
  PENDING     → 🟠 Orange background
  PROCESSING  → 🔵 Blue background with spinner
  COMPLETED   → 🟢 Green background with checkmark
  FAILED      → 🔴 Red background with X icon
  CANCELLED   → ⚪ Gray background with strikethrough
  ```
- [ ] **Additional Columns:**
  - Total Amount (BDT format)
  - Employee Count
  - Success Rate (X/Y paid)
  - Created By
  - Processed By
  - Executed At
- [ ] **Row Actions:**
  - View Items button
  - Process button (if PENDING, for ADMIN/EMPLOYER)
  - Cancel button (if PENDING, for ADMIN/EMPLOYER)
  - Retry Failed button (if FAILED items exist)
  - Download Report button
- [ ] **Batch Status Timeline:**
  - Visual progress indicator
  - Timestamps for each status change

### 3.7 Transaction Filter Panel Redesign

**Current Issues:**
- Text inputs for IDs are error-prone
- No quick filter options
- No preset saving

**Proposed Design:**

```
┌─────────────────── Transaction Filters ────────────────────┐
│                                                             │
│  Quick Filters:  [Today] [This Week] [This Month] [All]   │
│                                                             │
│  ▼ Advanced Filters                                        │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ Type:        [Dropdown: ALL ▼]                      │  │
│  │ Category:    [Dropdown: ALL ▼]                      │  │
│  │ Status:      [Dropdown: ALL ▼]                      │  │
│  │ Debit Acct:  [Searchable Dropdown: Select... ▼]    │  │
│  │ Credit Acct: [Searchable Dropdown: Select... ▼]    │  │
│  │ Batch:       [Searchable Dropdown: Select... ▼]    │  │
│  │ Date Range:  [2025-10-31] to [2025-11-30]          │  │
│  │                                                      │  │
│  │ [🔍 Apply Filters]  [🔄 Reset]  [💾 Save Preset]   │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  Saved Presets: [My Salary] [Outgoing] [Failed] [+New]    │
└─────────────────────────────────────────────────────────────┘
```

### 3.8 Company Account Page Redesign

**ADMIN View:**
```
┌────────────── System & Companies Overview ──────────────┐
│                                                          │
│              System Balance                              │
│              BDT 412,750                                │
│         [💰 Top Up System Account]                      │
│                                                          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Tabs: [System Account] [TechCorp] [Company B]         │
│                                                          │
│  Company: TechCorp Bangladesh Ltd                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 💰 Funding Balance:    BDT 200,000               │  │
│  │ 👥 Employees:          11                         │  │
│  │ 💼 Outstanding Payroll: BDT 587,250               │  │
│  │ 📅 Last Payroll:       Nov 30, 2025              │  │
│  │                                                    │  │
│  │ [View Details] [Top Up] [Transaction History]    │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ▼ Account Activity (Last 5 Transactions)               │
│  [Transaction list...]                                  │
└──────────────────────────────────────────────────────────┘
```

**EMPLOYER View:**
```
┌─────────────── Company Account ────────────────┐
│                                                 │
│  Company Funding Account                       │
│  ┌──────────────────────────────────────────┐ │
│  │ Account Number:    COMP001                │ │
│  │ Branch:            Motijheel Branch       │ │
│  │ Current Balance:   BDT 200,000            │ │
│  │ Available Balance: BDT 150,000            │ │
│  │                   (50K reserved)          │ │
│  │                                            │ │
│  │ [💰 Top Up] [📊 View Transactions]        │ │
│  └──────────────────────────────────────────┘ │
│                                                 │
│  My Personal Account                           │
│  ┌──────────────────────────────────────────┐ │
│  │ Account Number:    DIR001                 │ │
│  │ Current Balance:   BDT 74,250             │ │
│  │ Last Salary:       Nov 30, 2025           │ │
│  │                                            │ │
│  │ [📊 View Transactions]                    │ │
│  └──────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

**EMPLOYEE View:**
```
┌─────────────── My Account ────────────────┐
│                                            │
│  Account Number:    SR001                 │
│  Current Balance:   BDT 60,750            │
│  Last Salary:       BDT 60,750            │
│  Salary Date:       Nov 30, 2025          │
│  Next Expected:     Dec 31, 2025          │
│                                            │
│  [📊 View Transaction History]            │
│  [📥 Download Salary Slip]                │
│                                            │
│  ▼ Salary Breakdown (Last Payment)        │
│  [Breakdown table...]                     │
└────────────────────────────────────────────┘
```

---

## 4. Functional Gaps

### 4.1 Search Functionality
- [ ] **Global Search Bar** in header
  - Search across: Employees, Batches, Transactions, Accounts
  - Autocomplete suggestions
  - Recent searches
  - Search filters by entity type

### 4.2 Bulk Actions
- [ ] **Employee List:**
  - Bulk assign grade
  - Bulk status change (ACTIVE/INACTIVE)
  - Bulk export selected
  - Bulk delete (with confirmation)
- [ ] **Transaction List:**
  - Bulk export selected
  - Bulk categorize
  - Bulk mark as reviewed

### 4.3 Error States & Empty States
- [ ] **Empty State Illustrations:**
  - No employees yet → "Add your first employee"
  - No transactions yet → "No transactions to display"
  - No batches yet → "Create your first payroll batch"
- [ ] **Error States:**
  - API failure → Retry button with error message
  - Network error → Offline indicator
  - Permission denied → Friendly access denied message
- [ ] **Loading States:**
  - Skeleton loaders for tables
  - Spinner for buttons during actions
  - Progress bar for batch processing

### 4.4 Confirmation Dialogs
- [ ] **Delete Employee:**
  - Show employee name and warning
  - "Are you sure?" message
  - Consequences explanation (e.g., "Will not delete historical payroll data")
  - Confirmation input (type "DELETE" to confirm)
- [ ] **Process Payroll:**
  - Summary of batch before execution:
    - Total employees
    - Total amount
    - Company balance before/after
  - "Confirm Process" button
  - Show progress during processing
- [ ] **Top Up Account:**
  - Show current balance
  - Show new balance after top-up
  - Confirmation: "Add BDT X to account?"
  - Success message with transaction ID

### 4.5 Audit Trail
- [ ] **Who Created Batch?** - Show in batch details
- [ ] **Who Processed Payroll?** - Show in batch history
- [ ] **Who Topped Up Account?** - Show in transaction details
- [ ] **Audit Log Page** (ADMIN only):
  - All system actions
  - User, Action, Resource, Timestamp
  - Filter by user, action type, date range

---

## 5. Performance & Scalability

### 5.1 Pagination Improvements
- [ ] Current: "Page 1 of 3 (11 total)" - Good foundation
- [ ] Add: "Go to page" input for large datasets
- [ ] Add: "Items per page" selector (10, 25, 50, 100)
- [ ] Add: "Jump to first/last" buttons
- [ ] Implement virtual scrolling for very large lists

### 5.2 Real-Time Updates
- [ ] **Payroll Processing Status:**
  - Auto-refresh during batch processing
  - WebSocket connection for live updates
  - Progress bar showing X of Y employees processed
- [ ] **Transaction List:**
  - Auto-refresh on new transactions
  - Toast notification for incoming transactions
- [ ] **Balance Updates:**
  - Real-time balance updates in header
  - Highlight changed values (flash animation)
- [ ] **WebSocket Indicator:**
  - Connected: Green dot in header
  - Disconnected: Red dot with "Reconnecting..." message

### 5.3 Caching & Data Freshness
- [ ] **KPI Cards:**
  - Show "Last updated: X seconds ago"
  - Manual refresh button
  - Auto-refresh every 30 seconds
- [ ] **Cache Invalidation:**
  - After payroll processing → refresh all related data
  - After top-up → refresh balance immediately
- [ ] **Optimistic Updates:**
  - Show expected result immediately
  - Revert if server returns error

---

## 6. Accessibility

### 6.1 ARIA Labels & Semantic HTML
- [ ] **Action Buttons:**
  - `aria-label="Edit employee Ahmed Rahman"`
  - `aria-label="Delete employee Ahmed Rahman"`
  - `aria-label="Process payroll batch November 2025"`
- [ ] **Status Badges:**
  - `role="status"`
  - `aria-label="Payroll status: Completed"`
- [ ] **Form Inputs:**
  - Proper `<label>` associations
  - `aria-describedby` for helper text
  - `aria-invalid` for validation errors

### 6.2 Keyboard Navigation
- [ ] **Tab Order:**
  - Logical tab sequence through form fields
  - Skip links to main content
  - Focus indicators visible and clear
- [ ] **Keyboard Shortcuts:**
  - `Ctrl+K` or `Cmd+K`: Global search
  - `Ctrl+S`: Save (in modals)
  - `Escape`: Close modals
  - `?`: Show keyboard shortcuts help modal
- [ ] **Shortcuts Info Modal:**
  - Accessible via `?` key or footer link
  - List all available shortcuts
  - Grouped by context (Global, Employee List, Payroll, etc.)

### 6.3 Color Contrast
- [ ] **Test All Colors:**
  - Purple backgrounds vs white text → Check WCAG AA compliance
  - Badge colors → Ensure 4.5:1 contrast ratio
  - Status indicators → Don't rely on color alone (add icons/text)
- [ ] **High Contrast Mode Support:**
  - Test with Windows High Contrast
  - Ensure borders and separators are visible
- [ ] **Dark Mode Consideration:**
  - (Future enhancement) Design dark theme variants

### 6.4 Screen Reader Support
- [ ] **Table Announcements:**
  - `<table>` with `<caption>` describing content
  - `<th scope="col">` for column headers
  - `<th scope="row">` for row headers (employee names)
- [ ] **Status Changes:**
  - Live regions for dynamic content
  - `aria-live="polite"` for non-critical updates
  - `aria-live="assertive"` for errors
- [ ] **Form Validation:**
  - Error messages in `aria-describedby`
  - `role="alert"` for critical validation errors

---

## 7. Security & Privacy

### 7.1 Sensitive Data Handling
- [ ] **Account Numbers:**
  - EMPLOYEE role: Partially masked (e.g., "SR001" → "SR**1")
  - EMPLOYER role: Full visibility for own company
  - ADMIN role: Full visibility
- [ ] **Salary Amounts:**
  - EMPLOYEE role: See own + downstream (optional masking for others)
  - EMPLOYER role: See all in company
  - ADMIN role: See all
- [ ] **Personal Information:**
  - Contact info (phone, address) restricted to:
    - Self (always)
    - Direct manager (optional)
    - HR/Admin (yes)
- [ ] **Show/Hide Toggle:**
  - Eye icon to reveal masked data temporarily
  - Re-mask after 5 seconds or on page navigation

### 7.2 Session Management
- [ ] **Session Timeout Warning:**
  - Modal appears 5 minutes before timeout
  - "Your session will expire in X minutes"
  - "Extend Session" button (refreshes token)
  - Auto-logout if no interaction
- [ ] **Concurrent Session Detection:**
  - Warn if logged in from another device/tab
  - Option to terminate other sessions
- [ ] **Logout Confirmation:**
  - "Are you sure you want to logout?" modal
  - Optional "Remember this device" for faster re-login

### 7.3 Action Authorization Feedback
- [ ] **Disabled Button Tooltips:**
  - "You don't have permission to process payroll"
  - "Only ADMIN or EMPLOYER can create batches"
- [ ] **403 Error Pages:**
  - Friendly message explaining lack of access
  - Suggest contacting admin or appropriate action
- [ ] **Audit Log for Denied Actions:**
  - Backend logs all 403s with userId, endpoint, resource

---

## 8. Priority Implementation Matrix

### P0 - Critical (Blocks Role Differentiation)
| Task | Component | Estimated Effort |
|------|-----------|------------------|
| Hide Payroll Processing section for EMPLOYEE | Payroll Page | 2h |
| Filter employee list by role scope | Employee Page | 4h |
| Add role-specific KPI cards (4-card for EMPLOYEE) | Dashboard/Payroll | 6h |
| Disable mutation buttons for EMPLOYEE | All Pages | 2h |
| Add role badge color coding | Header | 1h |
| **Total P0** | | **15h** |

### P1 - High (Core Functionality)
| Task | Component | Estimated Effort |
|------|-----------|------------------|
| Add company filter for ADMIN | All Pages | 4h |
| Implement 4-card salary overview for EMPLOYEE | Payroll Page | 4h |
| Add transaction amount column | Transaction Page | 2h |
| Show account details in Company Account page | Company Account | 6h |
| Implement ADMIN dashboard with company cards | New Page | 8h |
| Add downstream employee hierarchy | Employee Page | 6h |
| **Total P1** | | **30h** |

### P2 - Medium (UX Polish)
| Task | Component | Estimated Effort |
|------|-----------|------------------|
| Add status badge colors | Payroll Page | 2h |
| Improve filter dropdowns (searchable) | Transaction Page | 6h |
| Add breadcrumbs navigation | All Pages | 3h |
| Add global search functionality | Header | 8h |
| Implement confirmation dialogs | All Actions | 6h |
| Add empty states and error states | All Pages | 4h |
| **Total P2** | | **29h** |

### P3 - Low (Nice to Have)
| Task | Component | Estimated Effort |
|------|-----------|------------------|
| Bulk actions for employees/transactions | List Pages | 8h |
| Export functionality (CSV/PDF) | All Pages | 6h |
| Advanced filtering presets | Transaction Page | 4h |
| Keyboard shortcuts | All Pages | 6h |
| Audit log page | New Page | 8h |
| Real-time updates (WebSocket) | Infrastructure | 12h |
| **Total P3** | | **44h** |

**Grand Total Estimated Effort:** 118 hours (~3 weeks for 1 frontend developer)

---

## 9. Backend API Requirements to Support UI

Based on identified UI gaps, the following backend endpoints and enhancements are required:

### 9.1 New Endpoints Required

| Endpoint | Method | Role Access | Purpose |
|----------|--------|-------------|---------|
| `/api/v1/admin/dashboard` | GET | ADMIN | System-wide metrics: total companies, employers, employees, system balance |
| `/api/v1/admin/companies` | GET | ADMIN | List all companies with summary stats |
| `/api/v1/admin/companies/{id}/overview` | GET | ADMIN | Single company detailed metrics |
| `/api/v1/payroll/overview/employee` | GET | EMPLOYEE | 4-card salary data (own + downstream) |
| `/api/v1/payroll/overview/employer` | GET | EMPLOYER | Company payroll metrics |
| `/api/v1/accounts/scope` | GET | ALL | Role-filtered account list for dropdowns |
| `/api/v1/accounts/balances` | GET | ALL | Consolidated balance view per role |
| `/api/v1/transactions/summary` | GET | ALL | Aggregated transaction amounts by role scope |
| `/api/v1/employees/downstream/{userId}` | GET | EMPLOYEE | Hierarchical list of downstream employees |
| `/api/v1/employees/hierarchy` | GET | ALL | Tree structure of employee hierarchy |

### 9.2 Enhanced Filtering for Existing Endpoints

| Endpoint | New Query Parameters |
|----------|----------------------|
| `/api/v1/employees` | `?companyId=X` (ADMIN), `?downstreamOnly=true` (EMPLOYEE) |
| `/api/v1/payroll/batches` | `?companyId=X` (ADMIN), `?scope=self` (EMPLOYEE) |
| `/api/v1/payroll/items` | `?employeeIds=X,Y,Z` (filtered by role) |
| `/api/v1/transactions` | `?companyId=X` (ADMIN), `?scope=downstream` (EMPLOYEE) |
| `/api/v1/accounts` | `?scope=company` (EMPLOYER), `?scope=self` (EMPLOYEE) |

### 9.3 Required Repository Aggregation Methods

```java
// PayrollItemRepository
BigDecimal sumAmountByEmployeeIdAndStatusIn(UUID employeeId, Set<PayrollItemStatus> statuses);
BigDecimal sumAmountByEmployeeIdInAndStatusIn(Collection<UUID> employeeIds, Set<PayrollItemStatus> statuses);
BigDecimal sumAmountByCompanyIdAndStatusIn(UUID companyId, Set<PayrollItemStatus> statuses);

// AccountRepository
BigDecimal sumBalanceByOwnerType(OwnerType ownerType); // For system-wide metrics
BigDecimal sumBalanceByCompanyId(UUID companyId);
Optional<Account> findSystemAccount(); // Special system holding account

// TransactionRepository  
BigDecimal sumAmountByAccountIdInAndDateBetween(Collection<UUID> accountIds, LocalDate from, LocalDate to);
Long countByAccountIdInAndStatus(Collection<UUID> accountIds, TransactionStatus status);

// EmployeeRepository
List<Employee> findAllByIdInOrderByGrade(Collection<UUID> employeeIds);
List<Employee> findDownstreamEmployees(UUID managerId); // Recursive query
Long countByCompanyId(UUID companyId);
```

### 9.4 Service Layer Enhancements

```java
// AuthorizationService
RoleScope getRoleScope(UUID userId); // Returns: {role, companyIds, employeeIds, downstreamIds}
Set<UUID> getVisibleEmployeeIds(UUID userId, UUID targetCompanyId);
Set<UUID> getVisibleAccountIds(UUID userId);
boolean canMutatePayroll(UUID userId, UUID companyId);
boolean canViewEmployeeDetails(UUID requestingUserId, UUID targetEmployeeId);

// PayrollService
EmployeeSalaryOverview getEmployeeSalaryOverview(UUID employeeUserId);
CompanyPayrollOverview getCompanyPayrollOverview(UUID companyId);
SystemPayrollOverview getSystemPayrollOverview(); // ADMIN only

// DashboardService (new)
AdminDashboard getAdminDashboard();
EmployerDashboard getEmployerDashboard(UUID employerId);
EmployeeDashboard getEmployeeDashboard(UUID employeeId);
```

### 9.5 DTO Definitions Needed

```java
// EmployeeSalaryOverview (for EMPLOYEE role)
{
  "ownPaid": "60750.00",
  "ownUnpaid": "0.00",
  "downstreamPaid": "180000.00",
  "downstreamUnpaid": "0.00",
  "downstreamEmployeeCount": 3
}

// CompanyPayrollOverview (for EMPLOYER role)
{
  "companyId": "uuid",
  "companyName": "TechCorp Bangladesh Ltd",
  "totalPayToBe": "587250.00",
  "totalPaid": "587250.00",
  "companyBalance": "412750.00",
  "employeeCount": 11,
  "lastPayrollDate": "2025-11-30"
}

// AdminDashboard
{
  "systemBalance": "412750.00",
  "totalCompanies": 3,
  "totalEmployers": 5,
  "totalEmployees": 35,
  "totalOutstandingPayroll": "1200000.00",
  "companies": [
    {
      "id": "uuid",
      "name": "TechCorp Bangladesh Ltd",
      "balance": "200000.00",
      "employeeCount": 11,
      "outstandingPayroll": "587250.00",
      "lastPayrollDate": "2025-11-30"
    }
  ]
}

// RoleScope (internal service DTO)
{
  "role": "EMPLOYEE",
  "userId": "uuid",
  "companyIds": ["uuid1"],
  "employeeIds": ["uuid-self"],
  "downstreamEmployeeIds": ["uuid2", "uuid3"],
  "canMutate": false
}
```

---

## 10. Testing Requirements

### 10.1 Role-Based Access Control Tests
- [ ] ADMIN can view all companies
- [ ] ADMIN cannot mutate payroll (policy decision)
- [ ] EMPLOYER can only view own company data
- [ ] EMPLOYER can process payroll for own company
- [ ] EMPLOYER cannot view other companies
- [ ] EMPLOYEE can only view self + downstream
- [ ] EMPLOYEE cannot access payroll processing UI
- [ ] EMPLOYEE cannot edit any employee data
- [ ] Cross-company isolation verified

### 10.2 UI Component Tests
- [ ] Role badge displays correct color per role
- [ ] Header KPIs match role requirements
- [ ] Employee list filters by role scope
- [ ] Payroll page hides sections for EMPLOYEE
- [ ] Company Account page shows role-appropriate data
- [ ] Transaction filters constrained by role
- [ ] Action buttons disabled/hidden per role

### 10.3 Integration Tests
- [ ] Login as ADMIN → see all companies dashboard
- [ ] Login as EMPLOYER → see only own company
- [ ] Login as EMPLOYEE → see read-only interface
- [ ] Process payroll as EMPLOYER → updates balances
- [ ] Attempt mutation as EMPLOYEE → 403 error
- [ ] Filter transactions as EMPLOYEE → only own + downstream

### 10.4 Performance Tests
- [ ] Dashboard loads in <2s with 100 companies (ADMIN)
- [ ] Employee list loads in <1s with 1000 employees
- [ ] Payroll batch processing shows progress updates
- [ ] Transaction list pagination handles 10,000+ records
- [ ] KPI aggregations complete in <500ms

### 10.5 Security Tests
- [ ] Cannot bypass role restrictions via API
- [ ] Cannot view other company data by manipulating URLs
- [ ] Session timeout enforced
- [ ] Sensitive data masked appropriately
- [ ] Audit log captures all denied access attempts

---

## 11. Next Steps

### Phase 1: Critical Role Differentiation (Week 1)
1. Implement role-specific header KPIs
2. Hide payroll processing for EMPLOYEE
3. Filter employee list by role scope
4. Add 4-card salary overview for EMPLOYEE
5. Disable mutation buttons for EMPLOYEE

### Phase 2: Core Functionality (Week 2)
1. Build ADMIN dashboard with company cards
2. Add company filter throughout UI
3. Implement account details in Company Account page
4. Add transaction amount column
5. Build downstream employee hierarchy view

### Phase 3: UX Polish (Week 3)
1. Add status badge colors
2. Improve filter dropdowns (searchable)
3. Add breadcrumbs
4. Implement confirmation dialogs
5. Add empty/error states

### Phase 4: Nice to Have (Future Sprints)
1. Global search
2. Bulk actions
3. Export functionality
4. Keyboard shortcuts
5. Real-time updates
6. Audit log page

---

## 12. Open Questions & Decisions Needed

1. **ADMIN Mutation Rights:**
   - Should ADMIN be able to process payroll? 
   - Or strictly read-only/supervisory?
   - **Recommendation:** Read-only for safety; escalate to EMPLOYER if action needed

2. **Top-Up Limits:**
   - Are 1,000-10,000 BDT limits per-transaction, daily, or monthly?
   - **Recommendation:** Clarify business rules and add daily/monthly tracking

3. **Employee Hierarchy:**
   - Is `manager_employee_id` column present in DB?
   - Or derived from grade ordering?
   - **Recommendation:** Add explicit manager FK for clearer hierarchy

4. **System Account:**
   - Does a "system holding account" exist in DB?
   - Or is "system balance" the sum of all company accounts?
   - **Recommendation:** Create dedicated system account for clarity

5. **Real-Time Updates:**
   - Should payroll processing updates be real-time (WebSocket)?
   - Or polling-based (refresh every 5s)?
   - **Recommendation:** Polling for MVP, WebSocket for v2

6. **Multi-Company EMPLOYER:**
   - Can one EMPLOYER be assigned to multiple companies?
   - Current CompanyUserRole model supports it - is it intentional?
   - **Recommendation:** Clarify if company switcher needed for multi-company employers

---

## 13. Appendix: Reference Screenshots

1. **Screenshot 1:** Employee List (ADMIN view)
   - Shows 11 employees with grades, balances, actions
   - Status filter (Active)
   - Add Employee button visible

2. **Screenshot 2:** Payroll Page (ADMIN/EMPLOYER view)
   - Payroll Processing section (base salary input)
   - 3 KPI cards
   - Batch details
   - Salary breakdown table

3. **Screenshot 3:** Company Account Page (System overview)
   - System Balance hero section
   - Top Up button
   - Collapsed Account Information

4. **Screenshot 4:** Top Up Modal
   - Amount input with min/max validation
   - Cancel/Add Funds buttons

5. **Screenshot 5:** Transaction History
   - Rich filtering panel
   - Transaction list (11 total)
   - Pagination

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-30  
**Status:** Ready for Implementation Planning
