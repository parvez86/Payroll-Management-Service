package org.sp.payroll_service.domain.payroll.service;

import org.sp.payroll_service.api.payroll.dto.CreateEmployeeRequest;
import org.sp.payroll_service.api.payroll.dto.EmployeeFilterRequest;
import org.sp.payroll_service.api.payroll.dto.EmployeeResponse;
import org.sp.payroll_service.api.payroll.dto.EmployeeUpdateRequest;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining the contract for Employee management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the Employee domain.
 */
public interface EmployeeService extends BaseCrudService<
        UUID,               // ID
        EmployeeResponse,       // R (Response DTO)
        CreateEmployeeRequest,// C (Creation DTO)
        EmployeeUpdateRequest,  // U (Update DTO)
        EmployeeFilterRequest          // F (Filter DTO)
        > {
    
    /**
     * Find employee by business ID (4-digit identifier).
     * @param bizId employee business ID
     * @return employee response
     */
    CompletableFuture<EmployeeResponse> findByBizId(String bizId);
    
    /**
     * Get employee count grouped by grade.
     * @return employee count statistics by grade
     */
    CompletableFuture<Object> getEmployeeCountByGrade();
    
    /**
     * Get total number of employees.
     * @return total employee count
     */
    CompletableFuture<Long> getTotalEmployeeCount();
}