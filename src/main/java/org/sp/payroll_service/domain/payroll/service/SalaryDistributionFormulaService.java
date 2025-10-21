package org.sp.payroll_service.domain.payroll.service;

import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaCreateRequest;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaFilter;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaResponse;
import org.sp.payroll_service.api.payroll.dto.SalaryDistributionFormulaUpdateRequest;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;

/**
 * Interface defining the contract for Salary formula management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface SalaryDistributionFormulaService extends BaseCrudService<
        UUID,               // ID
        SalaryDistributionFormulaResponse,       // R (Response DTO)
        SalaryDistributionFormulaCreateRequest,// C (Creation DTO)
        SalaryDistributionFormulaUpdateRequest,  // U (Update DTO)
        SalaryDistributionFormulaFilter          // F (Filter DTO)
        > {
}