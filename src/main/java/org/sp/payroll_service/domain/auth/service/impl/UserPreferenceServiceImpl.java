package org.sp.payroll_service.domain.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.UserPreferencesFilter;
import org.sp.payroll_service.api.auth.dto.UserPreferencesRequest;
import org.sp.payroll_service.api.auth.dto.UserPreferencesResponse;
import org.sp.payroll_service.domain.auth.entity.User;
import org.sp.payroll_service.domain.auth.entity.UserPreferences;
import org.sp.payroll_service.domain.auth.service.UserPreferenceService;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.sp.payroll_service.domain.common.enums.PreferenceScope;
import org.sp.payroll_service.domain.common.enums.Role;
import org.sp.payroll_service.domain.common.enums.Theme;
import org.sp.payroll_service.domain.common.exception.ErrorCodes;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.exception.ValidationException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.repository.CompanyRepository;
import org.sp.payroll_service.repository.UserPreferencesRepository;
import org.sp.payroll_service.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.*;

/**
 * Service implementation for user preference operations.
 * Extends AbstractCrudService for standard CRUD operations and adds business logic for managing user preferences.
 * All public methods are synchronous to maintain Spring Security context.
 */
@Service
@Slf4j
public class UserPreferenceServiceImpl extends AbstractCrudService<
        UserPreferences,
        UUID,
        UserPreferencesResponse,
        UserPreferencesRequest,
        UserPreferencesRequest,
        UserPreferencesFilter>
        implements UserPreferenceService {
    
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    
    public UserPreferenceServiceImpl(UserPreferencesRepository userPreferencesRepository,
                                     UserRepository userRepository,
                                     CompanyRepository companyRepository) {
        super(userPreferencesRepository, "UserPreferences");
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }
    
    // --- ABSTRACT METHOD IMPLEMENTATIONS (Required by AbstractCrudService) ---
    
    /**
     * Maps a creation request to entity.
     * NOTE: User preferences cannot be created directly via API - only via createDefaultPreferences().
     */
    @Override
    protected UserPreferences mapToEntity(UserPreferencesRequest creationRequest, HeaderResponse headerResponse) {
        throw new ValidationException(
            "User preferences cannot be created directly. They are auto-created on user registration.",
            ErrorCodes.VALIDATION_BUSINESS_RULE
        );
    }
    
    /**
     * Maps an update request to an existing entity with validation.
     */
    @Override
    protected UserPreferences mapToEntity(UserPreferencesRequest updateRequest, UserPreferences entity, HeaderResponse headerResponse) {
        log.debug("Mapping update request to UserPreferences entity for user: {}", entity.getId());
        
        // Validate user exists
        User user = userRepository.findById(entity.getUser().getId())
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", entity.getId()));
        
        // Validate role-scope alignment
        validateRoleScopeAlignment(user.getRole(), updateRequest.selectedScope());
        
        // Validate and set company if COMPANY scope
        Company selectedCompany = null;
        if (updateRequest.selectedScope() == PreferenceScope.COMPANY) {
            UUID companyId = UUID.fromString(updateRequest.selectedCompanyId());
            selectedCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));
        }
        
        // Update fields
        entity.setSelectedScope(updateRequest.selectedScope());
        entity.setSelectedCompany(selectedCompany);
        entity.setTheme(updateRequest.theme());
        entity.setLanguage(updateRequest.language());
        entity.setUpdatedBy(headerResponse.userId());
        // updatedAt is auto-managed by @LastModifiedDate, don't set manually
        
        return entity;
    }
    
    /**
     * Maps entity to response DTO.
     */
    @Override
    protected UserPreferencesResponse mapToResponse(UserPreferences entity) {
        return new UserPreferencesResponse(
            entity.getId(),  // userId from entity ID
            entity.getSelectedScope(),
            entity.getSelectedCompany() != null ? entity.getSelectedCompany().getId() : null,
            entity.getSelectedCompany() != null ? entity.getSelectedCompany().getName() : null,
            entity.getTheme(),
            entity.getLanguage(),
            entity.getUpdatedAt()
        );
    }
    
    // --- OVERRIDE BASE CREATE TO PREVENT DIRECT CREATION ---
    
    /**
     * Create is not allowed via API. Preferences are auto-created on user registration.
     */
    @Override
    @Transactional
    public UserPreferencesResponse create(UserPreferencesRequest request, HeaderResponse principal) {
        throw new ValidationException(
            "User preferences cannot be created directly. They are auto-created on user registration.",
            ErrorCodes.VALIDATION_BUSINESS_RULE
        );
    }
    
    // --- CUSTOM BUSINESS OPERATIONS ---
    @Override
    @Transactional
    public UserPreferencesResponse createDefaultPreferences(UUID userId, Role role) {
        log.info("Creating default preferences for user: {} with role: {}", userId, role);
        
        // Check if preferences already exist
        Optional<UserPreferences> existing = userPreferencesRepository.findByUserId(userId);
        if (existing.isPresent()) {
            log.debug("Preferences already exist for user: {}", userId);
            return this.mapToResponse(existing.get());
        }
        
        // Determine default scope based on role
        PreferenceScope defaultScope = role == Role.ADMIN ? PreferenceScope.GLOBAL : PreferenceScope.COMPANY;
        
        // Get first company if COMPANY scope
        Company defaultCompany = null;
        if (defaultScope == PreferenceScope.COMPANY) {
            defaultCompany = companyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                    "No company found to set as default preference",
                    ErrorCodes.VALIDATION_BUSINESS_RULE
                ));
        }
        
        // Get user entity
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.forEntity("User", userId));
        
        UserPreferences prefs = UserPreferences.builder()
            .user(user)
            .selectedScope(defaultScope)
            .selectedCompany(defaultCompany)
            .theme(Theme.LIGHT)
            .language("en")
            .build();
        
        UserPreferences saved = userPreferencesRepository.save(prefs);
        log.info("Default preferences created for user: {} with scope: {}", userId, defaultScope);
        
        return this.mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> getPreferredCompanyId(UUID userId) {
        log.debug("Getting preferred company for user: {}", userId);

        return userPreferencesRepository.findByUserId(userId)
            .map(prefs -> prefs.getSelectedCompany() != null ? prefs.getSelectedCompany().getId() : null);
    }

    @Override
    public Optional<UserPreferencesResponse> getUserPreferenceByUserId(UUID userId) {
        log.debug("Getting user preference by user id: {}", userId);
        return userPreferencesRepository.findByUserId(userId)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PreferenceScope getPreferredScope(UUID userId) {
        log.debug("Getting preferred scope for user: {}", userId);
        
        return userPreferencesRepository.findByUserId(userId)
            .map(UserPreferences::getSelectedScope)
            .orElse(PreferenceScope.COMPANY);
    }

    @Override
    public UserPreferencesResponse updateUserPreferenceByUserId(UUID userId, UserPreferencesRequest request, HeaderResponse principal) {
        UserPreferences userPreferences = userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_PREFERENCE_NOT_FOUND));
        return this.update(userPreferences.getId(), request, principal);
    }

    // --- HELPER METHODS ---
    
    /**
     * Validate that user's role aligns with selected scope.
     * ADMIN must use GLOBAL scope, EMPLOYER/EMPLOYEE must use COMPANY scope.
     */
    private void validateRoleScopeAlignment(Role role, PreferenceScope scope) {
        if (role == Role.ADMIN && scope == PreferenceScope.COMPANY) {
            throw new ValidationException(
                "ADMIN users must use GLOBAL scope",
                ErrorCodes.VALIDATION_BUSINESS_RULE
            );
        }
        
        if ((role == Role.EMPLOYER || role == Role.EMPLOYEE) && scope == PreferenceScope.GLOBAL) {
            throw new ValidationException(
                role + " users must use COMPANY scope",
                ErrorCodes.VALIDATION_BUSINESS_RULE
            );
        }
    }
    
    /**
     * Build JPA Specification from filter criteria.
     */
    @Override
    protected Specification<UserPreferences> buildSpecificationFromFilter(UserPreferencesFilter filter) {
        log.debug("Building specification from filter");
        
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filter.selectedScope() != null) {
                predicates.add(cb.equal(root.get("selectedScope"), filter.selectedScope()));
            }
            
            if (filter.selectedCompanyId() != null) {
                predicates.add(cb.equal(root.get("selectedCompany").get("id"), filter.selectedCompanyId()));
            }
            
            if (filter.theme() != null) {
                predicates.add(cb.equal(root.get("theme"), filter.theme()));
            }
            
            if (filter.language() != null) {
                predicates.add(cb.equal(root.get("language"), filter.language()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
