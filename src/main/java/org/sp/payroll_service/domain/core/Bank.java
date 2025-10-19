package org.sp.payroll_service.domain.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import org.sp.payroll_service.domain.common.entity.BaseEntity;

/**
 * Global reference entity for financial institutions.
 */
@Entity
@Table(name = "banks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Bank extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "swift_bic_code", nullable = false, unique = true)
    private String swiftBicCode;
}
