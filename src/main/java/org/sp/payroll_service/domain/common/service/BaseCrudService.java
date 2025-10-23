package org.sp.payroll_service.domain.common.service;

import org.sp.payroll_service.api.payroll.dto.PageResponse;
import org.springframework.data.domain.Pageable; // Added for Pageable parameter

import java.util.List;

/**
 * Generic interface for basic CRUD and advanced search/pagination operations on entities.
 * All methods are synchronous to maintain Spring Security context.
 * Virtual thread performance is achieved at the controller level through manual execution.
 *
 * @param <ID> The type of the entity's identifier (usually UUID or Long).
 * @param <R> The Response DTO type returned to the client.
 * @param <C> The Creation Request DTO type.
 * @param <U> The Update Request DTO type.
 * @param <F> The Filter/Search DTO type for optimal searching.
 */
public interface BaseCrudService<ID, R, C, U, F> {

    // Basic CRUD Operations
    R create(C request);
    R findById(ID id);
    List<R> findAll();
    R update(ID id, U request);
    void delete(ID id);

    // Advanced Query Operations

    /**
     * Retrieves a page of all resources, supporting standard pagination.
     * @param pageable The Spring Data Pageable object defining page number, size, and sorting.
     * @return A Page of Response DTOs.
     */
    PageResponse<R> findPageAll(Pageable pageable);

    /**
     * Executes a custom, optimal search query based on complex criteria.
     *
     * @param filter The Filter DTO containing search criteria (e.g., transactionStatus, date range, keywords).
     * @param pageable The Spring Data Pageable object for pagination and sorting of results.
     * @return A Page of Response DTOs matching the filter.
     */
    PageResponse<R> search(F filter, Pageable pageable);
}