package org.sp.payroll_service.domain.common.repository;

import org.sp.payroll_service.domain.common.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository interface with auditing and soft-deletion support.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find an entity by ID (excluding DELETED transactionStatus).
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.status <> 'DELETED'")
    Optional<T> findById(@Param("id") ID id);

    /**
     * Find an entity by ID, including DELETED transactionStatus.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdIncludingDeleted(@Param("id") ID id);


    /**
     * Find an entity by ID and specific status.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.status = :status")
    Optional<T> findByIdAndStatus(@Param("id") ID id, @Param("status") EntityStatus status);

    /**
     * Find all active entities.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.status <> 'DELETED' ORDER BY e.createdAt DESC")
    List<T> findAllActive();

    /**
     * Find all active entities with pagination support.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.status <> 'DELETED'")
    Page<T> findAllActive(org.springframework.data.domain.Pageable pageable);

    /**
     * Find all entities with pagination support (including deleted).
     */
    @Query("SELECT e FROM #{#entityName} e")
    Page<T> findAllIncludingDeleted(org.springframework.data.domain.Pageable pageable);

    /**
     * Count all active entities.
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.status <> 'DELETED'")
    long countActive();

    /**
     * Check if an active entity exists by ID.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.status <> 'DELETED'")
    boolean existsByIdActive(@Param("id") ID id);

    /**
     * Soft delete an entity by ID.
     * Updates the transactionStatus field to DELETED.
     */
    @Query("UPDATE #{#entityName} e SET e.status = 'DELETED', e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.status <> 'DELETED'")
    @Modifying
    @Transactional
    int softDeleteById(@Param("id") ID id);

    /**
     * Bulk soft delete entities by IDs.
     * Updates the transactionStatus field to DELETED.
     */
    @Query("UPDATE #{#entityName} e SET e.status = 'DELETED', e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.status <> 'DELETED'")
    @Modifying
    @Transactional
    int softDeleteByIds(@Param("ids") List<ID> ids);

    /**
     * Find entities created after a specific timestamp.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt > :timestamp AND e.status <> 'DELETED' ORDER BY e.createdAt DESC")
    List<T> findCreatedAfter(@Param("timestamp") Instant timestamp);

    /**
     * Find entities modified after a specific timestamp.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt > :timestamp AND e.status <> 'DELETED' ORDER BY e.updatedAt DESC")
    List<T> findModifiedAfter(@Param("timestamp") Instant timestamp);

    /**
     * Health check query to verify repository connectivity.
     */
    @Query("SELECT CURRENT_TIMESTAMP")
    Instant getDatabaseTimestamp();
}