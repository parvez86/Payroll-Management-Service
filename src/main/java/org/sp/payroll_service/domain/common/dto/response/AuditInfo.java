package org.sp.payroll_service.domain.common.dto.response;

import java.time.Instant;

/**
 * Audit information record.
 * @param createdBy entity creator
 * @param createdAt creation timestamp
 * @param lastModifiedBy last modifier
 * @param lastModifiedAt last modification timestamp
 * @param version entity version
 */
public record AuditInfo(
    String createdBy,
    Instant createdAt,
    String lastModifiedBy,
    Instant lastModifiedAt,
    Long version
) {}