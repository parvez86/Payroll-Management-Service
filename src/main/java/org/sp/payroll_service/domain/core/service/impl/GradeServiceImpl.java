package org.sp.payroll_service.domain.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.GradeCreateRequest;
import org.sp.payroll_service.api.core.dto.GradeFilter;
import org.sp.payroll_service.api.core.dto.GradeResponse;
import org.sp.payroll_service.api.core.dto.GradeUpdateRequest;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Grade;
import org.sp.payroll_service.domain.core.service.GradeService;
import org.sp.payroll_service.repository.GradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;



/**
 * Concrete service implementation for managing {@code Grade} entities, including hierarchy.
 * Handles validation, mapping, and the complex hierarchical search logic.
 */
@Service
@Slf4j
public class GradeServiceImpl extends AbstractCrudService<
        Grade,
        UUID,
        GradeResponse,
        GradeCreateRequest,
        GradeUpdateRequest,
        GradeFilter>
        implements GradeService {

    private final GradeRepository gradeRepository;

    public GradeServiceImpl(GradeRepository gradeRepository) {
        super(gradeRepository, "Grade");
        this.gradeRepository = gradeRepository;
    }


    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<GradeResponse> to GradeResponse
    public GradeResponse create(GradeCreateRequest request) {
        // Business Rule 1: Check uniqueness of name (blocking call)
        if (gradeRepository.existsByName(request.name())) {
            throw DuplicateEntryException.forEntity("Grade", "name", request.name());
        }

        // Business Rule 2: Validation of parent existence (Handled in mapToEntity)
        return super.create(request);
    }

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<GradeResponse> to GradeResponse
    public GradeResponse update(UUID id, GradeUpdateRequest request) {
        // Business Rule 1: Check uniqueness of name (excluding current entity) (blocking call)
        if (gradeRepository.existsByNameAndIdNot(request.name(), id)) {
            throw DuplicateEntryException.forEntity("Grade", "name", request.name());
        }
        return super.update(id, request);
    }

    // --- Custom Search Implementation ---
    @Override
    // FIX: Removed @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<Page<GradeResponse>> to Page<GradeResponse>
    public Page<GradeResponse> search(GradeFilter filter, Pageable pageable) {
        Specification<Grade> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (StringUtils.hasText(filter.keyword())) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (filter.parentId() != null) {
                // Filter by the parent ID (using the entity's relationship property 'parent')
                predicates.add(cb.equal(root.get("parent").get("id"), filter.parentId()));
            }

            if (filter.minRank() != null) {
                // Filter grades where rank is greater than or equal to the minRank
                predicates.add(cb.greaterThanOrEqualTo(root.get("rank"), filter.minRank()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Blocking JPA call
        Page<Grade> entityPage = specExecutor.findAll(spec, pageable);
        // Return the direct Page object
        return entityPage.map(this::mapToResponse);
    }

    // --- Abstract Mapping Implementations (No changes needed) ---

    @Override
    protected Grade mapToEntity(GradeCreateRequest creationRequest) {
        // NOTE: getParentOrNull contains a blocking JPA call, which is fine in this synchronous service method.
        Grade parent = getParentOrNull(creationRequest.parentId());

        // --- NEW LOGIC: Derive rank if parent exists, otherwise use DTO rank ---
        Integer rank = parent != null ? parent.getRank() + 1 : creationRequest.rank();

        return Grade.builder()
                .name(creationRequest.name())
                .rank(rank)
                .parent(parent)
                .build();
    }

    @Override
    protected Grade mapToEntity(GradeUpdateRequest updateRequest, Grade entity) {
        Grade parent = getParentOrNull(updateRequest.parentId());

        entity.setName(updateRequest.name());
        entity.setParent(parent);

        if (parent != null) {
            entity.setRank(parent.getRank() + 1);
        } else {
            entity.setRank(1);
        }

        return entity;
    }

    @Override
    protected GradeResponse mapToResponse(Grade entity) {
        // Handle the self-referential parent relationship
        UUID parentId = null;
        String parentName = null;

        if (entity.getParent() != null) {
            parentId = entity.getParent().getId();
            parentName = entity.getParent().getName();
        }

        return new GradeResponse(
                entity.getId(),
                entity.getName(),
                entity.getRank(),
                parentId,
                parentName,
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Private Helpers (No changes needed) ---

    /**
     * Retrieves the Parent Grade entity by ID, or throws ResourceNotFoundException if not found.
     *
     * @param parentId The UUID of the parent Grade.
     * @return The Parent Grade entity, or null if parentId is null.
     * @throws ResourceNotFoundException if the parentId is not null but no Grade is found.
     */
    private Grade getParentOrNull(UUID parentId) {
        if (parentId == null) {
            return null;
        }
        // Use findById and throw if not found
        return gradeRepository.findById(parentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Parent Grade", parentId));
    }
}