package org.sp.payroll_service.domain.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.common.enums.CompanyRoleType;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.core.entity.CompanyUserRole;
import org.sp.payroll_service.domain.core.service.CompanyUserRoleService;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.CompanyUserRoleRepository;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CompanyUserRoleService.
 * Provides centralized company-user role management with temporal validity checks.
 * All methods are synchronous (blocking) to maintain transaction consistency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyUserRoleServiceImpl implements CompanyUserRoleService {
    
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<UUID> getUserCompanyIds(UUID userId) {
        log.debug("Getting all company IDs for user: {}", userId);
        return companyUserRoleRepository.findCompanyIdsByUserId(userId, Instant.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UUID> getUserCompanyIds(UUID userId, CompanyRoleType... roles) {
        if (roles == null || roles.length == 0) {
            return getUserCompanyIds(userId);
        }
        
        log.debug("Getting company IDs for user: {} with roles: {}", userId, Arrays.toString(roles));
        return companyUserRoleRepository.findCompanyIdsByUserIdAndRoles(
            userId, 
            Arrays.asList(roles), 
            Instant.now()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToCompany(UUID userId, UUID companyId) {
        log.debug("Checking if user {} has access to company {}", userId, companyId);
        List<UUID> companyIds = getUserCompanyIds(userId);
        return companyIds.contains(companyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, UUID companyId, CompanyRoleType role) {
        log.debug("Checking if user {} has role {} on company {}", userId, role, companyId);
        return companyUserRoleRepository.existsByUserIdAndCompanyIdAndRoleIn(
            userId,
            companyId,
            List.of(role),
            Instant.now()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, UUID companyId, CompanyRoleType... roles) {
        if (roles == null || roles.length == 0) {
            return hasAccessToCompany(userId, companyId);
        }
        
        log.debug("Checking if user {} has any role {} on company {}", userId, Arrays.toString(roles), companyId);
        return companyUserRoleRepository.existsByUserIdAndCompanyIdAndRoleIn(
            userId,
            companyId,
            Arrays.asList(roles),
            Instant.now()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserRole> getActiveRolesForUser(UUID userId) {
        log.debug("Getting all active roles for user: {}", userId);
        return companyUserRoleRepository.findByUserIdAndActiveTrue(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserRole> getActiveRolesForCompany(UUID companyId) {
        log.debug("Getting all active roles for company: {}", companyId);
        return companyUserRoleRepository.findByCompanyIdAndActiveTrue(companyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyUserRole> getRole(UUID userId, UUID companyId, CompanyRoleType role) {
        log.debug("Getting role {} for user {} on company {}", role, userId, companyId);
        return companyUserRoleRepository.findByUserIdAndCompanyIdAndRoleAndActiveTrue(userId, companyId, role);
    }
    
    @Override
    @Transactional
    public CompanyUserRole assignRole(UUID userId, UUID companyId, CompanyRoleType role, UUID createdBy) {
        log.info("Assigning role {} to user {} on company {} by {}", role, userId, companyId, createdBy);
        
        // Check if role already exists (idempotent operation)
        Optional<CompanyUserRole> existingRole = getRole(userId, companyId, role);
        if (existingRole.isPresent()) {
            log.info("Role {} already exists for user {} on company {} - returning existing", 
                role, userId, companyId);
            return existingRole.get();
        }
        
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", userId));
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));
        
        // Create new role assignment with default full access
        CompanyUserRole newRole = CompanyUserRole.builder()
            .user(user)
            .company(company)
            .roleOnCompany(role)
            .active(true)
            .createdBy(createdBy)
            .build();
        
        CompanyUserRole saved = companyUserRoleRepository.save(newRole);
        log.info("Role {} successfully assigned to user {} on company {}", role, userId, companyId);
        
        return saved;
    }
    
    @Override
    @Transactional
    public void revokeRole(UUID roleId, UUID revokedBy) {
        log.info("Revoking role {} by {}", roleId, revokedBy);
        
        CompanyUserRole role = companyUserRoleRepository.findById(roleId)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("CompanyUserRole", roleId));
        
        role.setActive(false);
        role.setUpdatedBy(revokedBy);
        
        companyUserRoleRepository.save(role);
        log.info("Role {} revoked", roleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCompanyManager(UUID userId) {
        log.debug("Checking if user {} is a company manager", userId);
        return companyUserRoleRepository.existsByUserIdAndActiveTrue(userId, Instant.now());
    }
}
