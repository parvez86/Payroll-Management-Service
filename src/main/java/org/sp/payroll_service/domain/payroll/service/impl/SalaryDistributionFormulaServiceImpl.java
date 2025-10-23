package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.sp.payroll_service.domain.payroll.service.SalaryDistributionFormulaService;
import org.sp.payroll_service.repository.SalaryDistributionFormulaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;

/**
 * Concrete service implementation for managing {@code SalaryDistributionFormula} entities.
 * <p>
 * This class extends the generic {@code AbstractCrudService} to inherit boilerplate
 * **synchronous (blocking)** CRUD operations and adds domain-specific business logic, such as:
 * <ul>
 * <li>Unique constraint checks for formula name.</li>
 * <li>Custom search implementation using JPA Specifications.</li>
 * </ul>
 */
@Service
@Slf4j
public class SalaryDistributionFormulaServiceImpl extends AbstractCrudService<
        SalaryDistributionFormula,
        UUID,
        SalaryDistributionFormulaResponse,
        SalaryDistributionFormulaCreateRequest,
        SalaryDistributionFormulaUpdateRequest,
        SalaryDistributionFormulaFilter>
        implements SalaryDistributionFormulaService {

    private final SalaryDistributionFormulaRepository formulaRepository;

    /**
     * Constructs the SalaryDistributionFormulaServiceImpl.
     *
     * @param formulaRepository The JPA repository for SalaryDistributionFormula entities.
     */
    public SalaryDistributionFormulaServiceImpl(SalaryDistributionFormulaRepository formulaRepository) {
        super(formulaRepository, "SalaryFormula");
        this.formulaRepository = formulaRepository;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    /**
     * Creates a new salary formula after performing a uniqueness check on the formula name.
     *
     * @param request The DTO containing the formula creation data.
     * @return The {@code SalaryDistributionFormulaResponse} DTO of the newly created formula.
     * @throws DuplicateEntryException if a formula with the same name already exists.
     */
    @Override
    @Transactional
    // FIX: Removed CompletableFuture<...>
    public SalaryDistributionFormulaResponse create(SalaryDistributionFormulaCreateRequest request) {
        // Business Rule: Check uniqueness of the formula name before persisting (blocking call)
        if (formulaRepository.existsByName(request.name())) {
            throw DuplicateEntryException.forEntity("SalaryFormula", "name", request.name());
        }
        return super.create(request); // Delegates to abstract base class logic
    }

    /**
     * Updates an existing salary formula, performing a uniqueness check on the new name.
     *
     * @param id The ID of the formula to update.
     * @param request The DTO containing the update data.
     * @return The {@code SalaryDistributionFormulaResponse} DTO of the updated formula.
     * @throws DuplicateEntryException if the new name is already taken by another formula.
     */
    @Override
    @Transactional
    // FIX: Removed CompletableFuture<...>
    public SalaryDistributionFormulaResponse update(UUID id, SalaryDistributionFormulaUpdateRequest request) {
        // Business Rule: Check uniqueness of the name, excluding the current entity (blocking call)
        checkUniquenessOnUpdate(id, request.name());

        // Delegate to abstract base class logic for fetching and mapping
        return super.update(id, request);
    }

    // --- Abstract Mapping Implementations (No changes needed) ---

    @Override
    protected SalaryDistributionFormula mapToEntity(SalaryDistributionFormulaCreateRequest creationRequest) {
        return SalaryDistributionFormula.builder()
                .name(creationRequest.name())
                .baseSalaryGrade(creationRequest.baseSalaryGrade())
                .hraPercentage(creationRequest.hraPercentage())
                .medicalPercentage(creationRequest.medicalPercentage())
                .gradeIncrementAmount(creationRequest.gradeIncrementAmount())
                .build();
    }

    @Override
    protected SalaryDistributionFormula mapToEntity(SalaryDistributionFormulaUpdateRequest updateRequest, SalaryDistributionFormula entity) {
        entity.setName(updateRequest.name());
        entity.setBaseSalaryGrade(updateRequest.baseSalaryGrade());
        entity.setHraPercentage(updateRequest.hraPercentage());
        entity.setMedicalPercentage(updateRequest.medicalPercentage());
        entity.setGradeIncrementAmount(updateRequest.gradeIncrementAmount());
        return entity;
    }

    @Override
    protected SalaryDistributionFormulaResponse mapToResponse(SalaryDistributionFormula entity) {
        return new SalaryDistributionFormulaResponse(
                entity.getId(),
                entity.getName(),
                entity.getBaseSalaryGrade(),
                entity.getHraPercentage(),
                entity.getMedicalPercentage(),
                entity.getGradeIncrementAmount(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Private Helper: Uniqueness Check (No changes needed) ---

    /**
     * Performs uniqueness validation for the formula name during an update operation.
     * Ensures that the new name is not already taken by *another* formula.
     *
     * @param currentId The ID of the formula being updated (to exclude from the search).
     * @param newName The requested new name.
     * @throws DuplicateEntryException if the name is already in use by a different formula.
     */
    private void checkUniquenessOnUpdate(UUID currentId, String newName) {
        formulaRepository.findByName(newName).ifPresent(formula -> {
            if (!formula.getId().equals(currentId)) {
                throw DuplicateEntryException.forEntity("SalaryFormula", "name", newName);
            }
        });
    }

    @Override
    protected Specification<SalaryDistributionFormula> buildSpecificationFromFilter(SalaryDistributionFormulaFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(filter.name())) {
                String pattern = "%" + filter.name().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            // Filter by Formula Type (Exact match)
            // NOTE: Assuming filter.baseSalaryGrade() is a string or compatible type for comparison
            if (filter.baseSalaryGrade() != null) {
                predicates.add(cb.equal(root.get("baseSalaryGrade"), filter.baseSalaryGrade()));
            }

            // Filter by Status (Is Active)
            // NOTE: The filter property (createdBy) seems inconsistent with the field used (createdBy)
            if (filter.createdBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.createdBy()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}