package org.sp.payroll_service.domain.core.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sp.payroll_service.domain.common.entity.BaseEntity;

@Entity
@Table(name = "grades")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Grade extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Grade parent;

    @Column(name = "rank", nullable = false)
    @Builder.Default
    private Integer rank=1;
}
