package org.sp.payroll_service.domain.wallet.service;

import org.sp.payroll_service.api.wallet.dto.AccountFilter;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.api.wallet.dto.CreateAccountRequest;
import org.sp.payroll_service.api.wallet.dto.UpdateAccountRequest;
import org.sp.payroll_service.domain.common.enums.OwnerType;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining the contract for Account management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface AccountService extends BaseCrudService<
        UUID,               // ID
        AccountResponse,       // R (Response DTO)
        CreateAccountRequest,// C (Creation DTO)
        UpdateAccountRequest,  // U (Update DTO)
        AccountFilter          // F (Filter DTO)
        > {

    /**
     * Finds an account by the ID of its owner (Employee or Company).
     * @param ownerId The UUID of the owner.
     * @return CompletableFuture resolving to the AccountResponse.
     */
    CompletableFuture<AccountResponse> findByOwnerId(UUID ownerId, OwnerType ownerType);
}