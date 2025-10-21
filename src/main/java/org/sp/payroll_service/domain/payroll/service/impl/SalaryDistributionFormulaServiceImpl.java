package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaCreateRequest;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaFilter;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaResponse;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaUpdateRequest;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.sp.payroll_service.domain.payroll.service.SalaryDistributionFormulaService;
import org.sp.payroll_service.repository.SalaryDistributionFormulaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.persistence.criteria.Predicate;

/**
 * Concrete service implementation for managing {@code SalaryDistributionFormula} entities.
 * <p>
 * This class extends the generic {@code AbstractCrudService} to inherit boilerplate
 * asynchronous CRUD operations and adds domain-specific business logic, such as:
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
     * @return A {@code CompletableFuture} containing the {@code SalaryDistributionFormulaResponse} DTO.
     * @throws DuplicateEntryException if a formula with the same name already exists.
     */
    @Override
    @Transactional
    public CompletableFuture<SalaryDistributionFormulaResponse> create(SalaryDistributionFormulaCreateRequest request) {
        // Business Rule: Check uniqueness of the formula name before persisting
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
     * @return A {@code CompletableFuture} containing the {@code SalaryDistributionFormulaResponse} DTO.
     * @throws DuplicateEntryException if the new name is already taken by another formula.
     */
    @Override
    @Transactional
    public CompletableFuture<SalaryDistributionFormulaResponse> update(UUID id, SalaryDistributionFormulaUpdateRequest request) {
        // Business Rule: Check uniqueness of the name, excluding the current entity
        checkUniquenessOnUpdate(id, request.name());

        // Delegate to abstract base class logic for fetching and mapping
        return super.update(id, request);
    }

    // --- Custom Search Implementation (Overrides AbstractCrudService.search) ---

    /**
     * Executes a dynamic search for salary formulas based on the provided filter criteria and pagination settings.
     * Uses JPA Specifications to build the query chain.
     *
     * @param filter The {@code SalaryDistributionFormulaFilter} DTO containing search criteria.
     * @param pageable The pagination and sorting information.
     * @return A {@code CompletableFuture} containing a {@code Page} of {@code SalaryDistributionFormulaResponse} DTOs.
     */
    @Override
    @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<SalaryDistributionFormulaResponse>> search(SalaryDistributionFormulaFilter filter, Pageable pageable) {
        log.debug("Executing search for SalaryFormulas with filter: {}", filter);

        Specification<SalaryDistributionFormula> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Filter by Name (Case-insensitive LIKE search)
            if (StringUtils.hasText(filter.name())) {
                String pattern = "%" + filter.name().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            // Filter by Formula Type (Exact match)
            if (filter.baseSalaryGrade() != null) {
                predicates.add(cb.equal(root.get("baseSalaryGrade"), filter.baseSalaryGrade()));
            }

            // Filter by Status (Is Active)
            if (filter.createdBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.createdBy()));
            }

            // Combine all predicates with AND logic
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Query execution
        Page<SalaryDistributionFormula> entityPage = formulaRepository.findAll(spec, pageable);
        Page<SalaryDistributionFormulaResponse> responsePage = entityPage.map(this::mapToResponse);

        return CompletableFuture.completedFuture(responsePage);
    }

    // --- Abstract Mapping Implementations ---

    /**
     * Maps a {@code SalaryDistributionFormulaCreateRequest} DTO to a new {@code SalaryDistributionFormula} entity.
     *
     * @param creationRequest The incoming creation DTO.
     * @return The new, transient {@code SalaryDistributionFormula} entity.
     */
    @Override
    protected SalaryDistributionFormula mapToEntity(SalaryDistributionFormulaCreateRequest creationRequest) {
        return SalaryDistributionFormula.builder()
                .name(creationRequest.name())
                .baseSalaryGrade(creationRequest.baseSalaryGrade()) // Raw formula string/expression
                .hraPercentage(creationRequest.hraPercentage())
                .medicalPercentage(creationRequest.medicalPercentage())
                .gradeIncrementAmount(creationRequest.gradeIncrementAmount())
                .build();
    }

    /**
     * Applies changes from a {@code SalaryDistributionFormulaCreateRequest} DTO to an existing {@code SalaryDistributionFormula} entity.
     *
     * @param updateRequest The incoming update DTO.
     * @param entity The existing {@code SalaryDistributionFormula} entity.
     * @return The updated, detached {@code SalaryDistributionFormula} entity.
     */
    @Override
    protected SalaryDistributionFormula mapToEntity(SalaryDistributionFormulaUpdateRequest updateRequest, SalaryDistributionFormula entity) {
        entity.setName(updateRequest.name());
        entity.setBaseSalaryGrade(updateRequest.baseSalaryGrade());
        entity.setHraPercentage(updateRequest.hraPercentage());
        entity.setMedicalPercentage(updateRequest.medicalPercentage());
        entity.setGradeIncrementAmount(updateRequest.gradeIncrementAmount());
        return entity;
    }

    /**
     * Maps a persistent {@code SalaryDistributionFormula} entity to its public-facing {@code SalaryDistributionFormulaResponse} DTO.
     *
     * @param entity The persistent {@code SalaryDistributionFormula} entity.
     * @return The {@code SalaryDistributionFormulaResponse} DTO.
     */
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

    // --- Private Helper: Uniqueness Check ---

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
}
