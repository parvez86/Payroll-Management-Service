package org.sp.payroll_service.domain.common.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.common.enums.CompanyRoleType;
import org.sp.payroll_service.domain.common.enums.Role;
import org.sp.payroll_service.domain.common.service.AuthorizationService;
import org.sp.payroll_service.domain.core.service.CompanyUserRoleService;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.EmployeeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuthorizationService.
 * Centralizes all role-based access control logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {
    
    private final CompanyUserRoleService companyUserRoleService;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Specification<Employee> applyEmployeeAccessFilter(HeaderResponse principal) {
        return (root, query, cb) -> {
            if (principal == null || principal.role() == null) {
                log.warn("No principal or role provided - denying access");
                return cb.disjunction(); // No access
            }
            
            switch (principal.role()) {
                case ADMIN:
                    // ADMIN sees all employees
                    log.debug("ADMIN {} - granting access to all employees", principal.username());
                    return cb.conjunction();
                    
                case EMPLOYER:
                    // EMPLOYER sees employees in companies they manage
                    List<UUID> employerCompanyIds = companyUserRoleService.getUserCompanyIds(
                        principal.userId(),
                        CompanyRoleType.EMPLOYER,
                        CompanyRoleType.COMPANY_ADMIN
                    );
                    
                    if (employerCompanyIds.isEmpty()) {
                        log.warn("EMPLOYER {} has no company access - denying access", principal.username());
                        return cb.disjunction();
                    }
                    
                    log.debug("EMPLOYER {} - filtering by companies: {}", principal.username(), employerCompanyIds);
                    return root.get("company").get("id").in(employerCompanyIds);
                    
                case EMPLOYEE:
                    // EMPLOYEE sees self + downstream subordinates
                    List<UUID> accessibleEmployeeIds = getAccessibleEmployeeIds(principal);
                    
                    if (accessibleEmployeeIds.isEmpty()) {
                        log.warn("EMPLOYEE {} has no accessible employees - denying access", principal.username());
                        return cb.disjunction();
                    }
                    
                    log.debug("EMPLOYEE {} - filtering by employee IDs: {}", principal.username(), accessibleEmployeeIds.size());
                    return root.get("id").in(accessibleEmployeeIds);
                    
                default:
                    log.warn("Unknown role {} for user {} - denying access", principal.role(), principal.username());
                    return cb.disjunction();
            }
        };
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UUID> getAccessibleCompanyIds(HeaderResponse principal) {
        if (principal == null || principal.role() == null) {
            return Collections.emptyList();
        }
        
        switch (principal.role()) {
            case ADMIN:
                // ADMIN can access all companies
                return companyRepository.findAll().stream()
                    .map(company -> company.getId())
                    .collect(Collectors.toList());
                    
            case EMPLOYER:
                // EMPLOYER can access companies they have roles for
                return companyUserRoleService.getUserCompanyIds(
                    principal.userId(),
                    CompanyRoleType.EMPLOYER,
                    CompanyRoleType.COMPANY_ADMIN
                );
                
            case EMPLOYEE:
                // EMPLOYEE can access their company
                return getEmployeeForUser(principal.userId())
                    .map(emp -> List.of(emp.getCompany().getId()))
                    .orElse(Collections.emptyList());
                    
            default:
                return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UUID> getAccessibleEmployeeIds(HeaderResponse principal) {
        if (principal == null || principal.role() == null) {
            return Collections.emptyList();
        }
        
        switch (principal.role()) {
            case ADMIN:
                // ADMIN can access all employees
                return employeeRepository.findAll().stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());
                    
            case EMPLOYER:
                // EMPLOYER can access employees in their companies
                List<UUID> companyIds = companyUserRoleService.getUserCompanyIds(
                    principal.userId(),
                    CompanyRoleType.EMPLOYER,
                    CompanyRoleType.COMPANY_ADMIN
                );
                
                if (companyIds.isEmpty()) {
                    return Collections.emptyList();
                }
                
                return employeeRepository.findAll().stream()
                    .filter(emp -> companyIds.contains(emp.getCompany().getId()))
                    .map(Employee::getId)
                    .collect(Collectors.toList());
                    
            case EMPLOYEE:
                // EMPLOYEE can access self + downstream subordinates
                Optional<Employee> currentEmployeeOpt = getEmployeeForUser(principal.userId());
                
                if (currentEmployeeOpt.isEmpty()) {
                    log.warn("EMPLOYEE {} not found in employee table", principal.username());
                    return Collections.emptyList();
                }
                
                Employee currentEmployee = currentEmployeeOpt.get();
                List<UUID> accessibleIds = new ArrayList<>();
                
                // Add self
                accessibleIds.add(currentEmployee.getId());
                
                // Add subordinates
                accessibleIds.addAll(getDownstreamEmployeeIds(currentEmployee.getId()));
                
                log.debug("EMPLOYEE {} can access {} employees (self + subordinates)", 
                    principal.username(), accessibleIds.size());
                
                return accessibleIds;
                    
            default:
                return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UUID> getDownstreamEmployeeIds(UUID employeeId) {
        log.debug("Getting downstream employee IDs for employee: {}", employeeId);
        
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            log.warn("Employee {} not found", employeeId);
            return Collections.emptyList();
        }
        
        Employee employee = employeeOpt.get();
        UUID companyId = employee.getCompany().getId();
        Integer gradeRank = employee.getGrade().getRank();
        
        // Find all employees in same company with lower grades (higher rank numbers)
        List<UUID> subordinateIds = employeeRepository
            .findByCompanyIdAndGradeRankGreaterThan(companyId, gradeRank)
            .stream()
            .map(Employee::getId)
            .collect(Collectors.toList());
        
        log.debug("Found {} downstream employees for employee {}", subordinateIds.size(), employeeId);
        return subordinateIds;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canAccessEmployee(HeaderResponse principal, UUID employeeId) {
        if (principal == null || principal.role() == null) {
            return false;
        }
        
        // ADMIN can access all
        if (principal.role() == Role.ADMIN) {
            return true;
        }
        
        // Check if employee is in accessible list
        List<UUID> accessibleEmployeeIds = getAccessibleEmployeeIds(principal);
        return accessibleEmployeeIds.contains(employeeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canAccessCompany(HeaderResponse principal, UUID companyId) {
        if (principal == null || principal.role() == null) {
            return false;
        }
        
        // ADMIN can access all
        if (principal.role() == Role.ADMIN) {
            return true;
        }
        
        // Check if company is in accessible list
        List<UUID> accessibleCompanyIds = getAccessibleCompanyIds(principal);
        return accessibleCompanyIds.contains(companyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeForUser(UUID userId) {
        log.debug("Getting employee for user: {}", userId);
        return employeeRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public <T> Specification<T> applyCompanyBasedAccessFilter(HeaderResponse principal) {
        return (root, query, cb) -> {
            if (principal == null || principal.role() == null) {
                log.warn("No principal or role provided - denying access");
                return cb.disjunction();
            }
            
            switch (principal.role()) {
                case ADMIN:
                    // ADMIN sees all
                    return cb.conjunction();
                    
                case EMPLOYER:
                    // EMPLOYER sees only data from companies they manage
                    List<UUID> employerCompanyIds = companyUserRoleService.getUserCompanyIds(
                        principal.userId(),
                        CompanyRoleType.EMPLOYER,
                        CompanyRoleType.COMPANY_ADMIN
                    );
                    
                    if (employerCompanyIds.isEmpty()) {
                        log.warn("EMPLOYER {} has no company access", principal.username());
                        return cb.disjunction();
                    }
                    
                    return root.get("company").get("id").in(employerCompanyIds);
                    
                case EMPLOYEE:
                    // EMPLOYEE sees only data from their company
                    Optional<Employee> empOpt = getEmployeeForUser(principal.userId());
                    if (empOpt.isEmpty()) {
                        log.warn("EMPLOYEE {} not found", principal.username());
                        return cb.disjunction();
                    }
                    
                    return cb.equal(root.get("company").get("id"), empOpt.get().getCompany().getId());
                    
                default:
                    log.warn("Unknown role {} - denying access", principal.role());
                    return cb.disjunction();
            }
        };
    }
    
    @Override
    @Transactional(readOnly = true)
    public <T> Specification<T> applyEmployeeBasedAccessFilter(HeaderResponse principal) {
        return (root, query, cb) -> {
            if (principal == null || principal.role() == null) {
                log.warn("No principal or role provided - denying access");
                return cb.disjunction();
            }
            
            switch (principal.role()) {
                case ADMIN:
                    // ADMIN sees all
                    return cb.conjunction();
                    
                case EMPLOYER:
                    // EMPLOYER sees data for employees in companies they manage
                    List<UUID> employerCompanyIds = companyUserRoleService.getUserCompanyIds(
                        principal.userId(),
                        CompanyRoleType.EMPLOYER,
                        CompanyRoleType.COMPANY_ADMIN
                    );
                    
                    if (employerCompanyIds.isEmpty()) {
                        log.warn("EMPLOYER {} has no company access", principal.username());
                        return cb.disjunction();
                    }
                    
                    // Filter by employee's company
                    return root.get("employee").get("company").get("id").in(employerCompanyIds);
                    
                case EMPLOYEE:
                    // EMPLOYEE sees only their own data + subordinates' data
                    List<UUID> accessibleEmployeeIds = getAccessibleEmployeeIds(principal);
                    
                    if (accessibleEmployeeIds.isEmpty()) {
                        log.warn("EMPLOYEE {} has no accessible employees", principal.username());
                        return cb.disjunction();
                    }
                    
                    return root.get("employee").get("id").in(accessibleEmployeeIds);
                    
                default:
                    log.warn("Unknown role {} - denying access", principal.role());
                    return cb.disjunction();
            }
        };
    }
}
