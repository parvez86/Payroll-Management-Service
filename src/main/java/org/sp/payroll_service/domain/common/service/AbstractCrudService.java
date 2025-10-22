package org.sp.payroll_service.domain.common.service;

import org.sp.payroll_service.domain.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public Page<R> findPageAll(Pageable pageable) {
        Page<E> entityPage = repository.findAll(pageable);
        return entityPage.map(this::mapToResponse);
    }

    /**
     * The search method MUST be implemented in the concrete service class.
     * We provide a default implementation that executes the search synchronously
     * if the filter is a JPA Specification, otherwise it throws UnsupportedOperationException.
     *
     * @param filter Must be castable to Specification<E> in the implementation.
     */
    @SuppressWarnings("unchecked")
    public Page<R> search(F filter, Pageable pageable) {
        if (filter instanceof Specification) {
            Page<E> entityPage = specExecutor.findAll((Specification<E>) filter, pageable);
            return entityPage.map(this::mapToResponse);
        } else {
            throw new UnsupportedOperationException("Search operation must be implemented in the concrete service for " + entityName + ".");
        }
    }
}