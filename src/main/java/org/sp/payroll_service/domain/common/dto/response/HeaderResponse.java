package org.sp.payroll_service.domain.common.dto.response;

import org.sp.payroll_service.domain.common.enums.Role;

import java.util.UUID;

public record HeaderResponse(
        UUID userId,
        String username,
        Role role,
        String jti
) {
}
