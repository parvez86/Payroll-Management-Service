package org.sp.payroll_service.domain.core.service;

import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;

/**
 * Interface defining the contract for Branch management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface BranchService extends BaseCrudService<
        UUID,               // ID
        BranchResponse,       // R (Response DTO)
        BranchCreateRequest,// C (Creation DTO)
        BranchUpdateRequest,  // U (Update DTO)
        BranchFilter          // F (Filter DTO)
        > {
}