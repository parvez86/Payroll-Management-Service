package org.sp.payroll_service.domain.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Base entity with UUID identifier and audit fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity extends BaseAuditingEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}