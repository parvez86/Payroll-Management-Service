package org.sp.payroll_service.domain.core.service;

import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;

/**
 * Interface defining the contract for Grade management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface GradeService extends BaseCrudService<
        UUID,               // ID
        GradeResponse,       // R (Response DTO)
        GradeCreateRequest,// C (Creation DTO)
        GradeUpdateRequest,  // U (Update DTO)
        GradeFilter          // F (Filter DTO)
        > {
}