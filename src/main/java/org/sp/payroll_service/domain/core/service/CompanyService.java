package org.sp.payroll_service.domain.core.service;

import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.api.payroll.dto.TransactionResponse;
import org.sp.payroll_service.api.wallet.dto.AccountResponse;
import org.sp.payroll_service.domain.common.service.BaseCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining the contract for Company management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface CompanyService extends BaseCrudService<
        UUID,               // ID
        CompanyResponse,       // R (Response DTO)
        CompanyCreateRequest,// C (Creation DTO)
        CompanyUpdateRequest,  // U (Update DTO)
        CompanyFilter          // F (Filter DTO)
        > {
    
    /**
     * Top-up company account with funds.
     * @param companyId company identifier
     * @param request top-up request details
     * @return updated company response
     */
    CompletableFuture<CompanyResponse> topUpAccount(UUID companyId, CompanyTopUpRequest request);
    
    /**
     * Get company account details.
     * @param companyId company identifier
     * @return account response with balance details
     */
    CompletableFuture<AccountResponse> getCompanyAccount(UUID companyId);
    
    /**
     * Get company transaction history.
     * @param companyId company identifier
     * @param pageable pagination parameters
     * @return paginated list of transactions
     */
    CompletableFuture<Page<TransactionResponse>> getCompanyTransactions(UUID companyId, Pageable pageable);
}