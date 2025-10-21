package org.sp.payroll_service.domain.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Added for Pageable parameter

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generic interface for basic CRUD and advanced search/pagination operations on entities.
 * All methods return CompletableFuture to support asynchronous, non-blocking execution (Virtual Threads).
 *
 * @param <ID> The type of the entity's identifier (usually UUID or Long).
 * @param <R> The Response DTO type returned to the client.
 * @param <C> The Creation Request DTO type.
 * @param <U> The Update Request DTO type.
 * @param <F> The Filter/Search DTO type for optimal searching.
 */
public interface BaseCrudService<ID, R, C, U, F> {

    // Basic CRUD Operations
    CompletableFuture<R> create(C request);
    CompletableFuture<R> findById(ID id);
    CompletableFuture<List<R>> findAll();
    CompletableFuture<R> update(ID id, U request);
    CompletableFuture<Void> delete(ID id);

    // Advanced Query Operations

    /**
     * Retrieves a page of all resources, supporting standard pagination.
     * @param pageable The Spring Data Pageable object defining page number, size, and sorting.
     * @return A CompletableFuture containing a Page of Response DTOs.
     */
    CompletableFuture<Page<R>> findPageAll(Pageable pageable);

    /**
     * Executes a custom, optimal search query based on complex criteria.
     *
     * @param filter The Filter DTO containing search criteria (e.g., transactionStatus, date range, keywords).
     * @param pageable The Spring Data Pageable object for pagination and sorting of results.
     * @return A CompletableFuture containing a Page of Response DTOs matching the filter.
     */
    CompletableFuture<Page<R>> search(F filter, Pageable pageable);
}