package org.sp.payroll_service.domain.common.service;

import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Abstract base service class providing generic, asynchronous CRUD operations for entities.
 *
 * @param <E> The JPA Entity type (e.g., User, Company).
 * @param <ID> The type of the entity's identifier (e.g., UUID).
 * @param <R> The Response DTO type.
 * @param <C> The Creation Request DTO type.
 * @param <U> The Update Request DTO type.
 * @param <F> The Filter/Search DTO type.
 */
public abstract class AbstractCrudService<
        E extends BaseEntity,
        ID,
        R,
        C,
        U,
        F> implements BaseCrudService<ID, R, C, U, F> {

    protected final JpaRepository<E, ID> repository;
    // We cast the repository to JpaSpecificationExecutor for the search/pagination logic
    protected final JpaSpecificationExecutor<E> specExecutor;
    protected final String entityName;

    @SuppressWarnings("unchecked")
    protected AbstractCrudService(JpaRepository<E, ID> repository, String entityName) {
        this.repository = repository;
        this.specExecutor = (JpaSpecificationExecutor<E>) repository; // Safe cast since all your Repos extend JpaSpecificationExecutor
        this.entityName = entityName;
    }

    // --- Abstract Methods for Mapping ---
    protected abstract E mapToEntity(C creationRequest);
    protected abstract E mapToEntity(U updateRequest, E entity);
    protected abstract R mapToResponse(E entity);
    protected List<R> mapToResponse(List<E> entityList){
        return (entityList == null) ? Collections.emptyList() : entityList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // --- BASE CRUD IMPLEMENTATIONS (Transactional & Asynchronous) ---

    @Override
    @Transactional
    public R create(C request) {
        E entity = mapToEntity(request);
        E savedEntity = repository.save(entity);
        return mapToResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public R findById(ID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, entityName + " not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<R> findAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public R update(ID id, U request) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, entityName + " not found with ID: " + id));

        E updatedEntity = mapToEntity(request, entity);
        E savedEntity = repository.save(updatedEntity);
        return mapToResponse(savedEntity);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, entityName + " not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // --- ADVANCED QUERY IMPLEMENTATIONS ---

    @Override
    @Transactional(readOnly = true)
    public PageResponse<R> findPageAll(Pageable pageable) {
        Page<E> entityPage = repository.findAll(pageable);
        return PageResponse.from(
                this.mapToResponse(entityPage.getContent()),
                entityPage.getTotalElements(),
                entityPage.getPageable()
        );
    }

    /**
     * The search method provides a default implementation that executes the search synchronously.
     * Subclasses can override this method to provide custom pagination hardening and filtering logic.
     * This method returns PageResponse with complete pagination metadata.
     *
     * @param filter The filter criteria (subclasses should override buildSpecificationFromFilter to handle)
     * @param pageable The pagination and sorting information
     * @return PageResponse containing mapped results and pagination metadata
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<R> search(F filter, Pageable pageable) {
        Specification<E> spec = buildSpecificationFromFilter(filter);
        Page<E> entityPage = specExecutor.findAll(spec, pageable);
        return PageResponse.from(
                this.mapToResponse(entityPage.getContent()),
                entityPage.getTotalElements(),
                entityPage.getPageable()
        );
    }

    /**
     * Hook to build a Specification from the filter.
     * Subclasses should override when F is not a Specification or when custom filtering is needed.
     * This is the extension point for custom filtering logic.
     *
     * @param filter The filter DTO
     * @return A JPA Specification for querying
     */
    protected Specification<E> buildSpecificationFromFilter(F filter) {
        // Default: if F is a Specification<E>, use it; otherwise no filtering.
        if (filter instanceof Specification) {
            @SuppressWarnings("unchecked")
            Specification<E> spec = (Specification<E>) filter;
            return spec;
        }
        // No filtering by default
        return (root, query, cb) -> cb.conjunction();
    }
}