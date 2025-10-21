package org.sp.payroll_service.domain.core.service;

import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;

/**
 * Interface defining the contract for Bank management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface BankService extends BaseCrudService<
        UUID,               // ID
        BankResponse,       // R (Response DTO)
        BankCreateRequest,// C (Creation DTO)
        BankUpdateRequest,  // U (Update DTO)
        BankFilter          // F (Filter DTO)
        > {
}