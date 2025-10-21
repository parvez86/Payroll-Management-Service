package org.sp.payroll_service.domain.auth.service;

import jakarta.validation.constraints.NotBlank;
import org.sp.payroll_service.api.auth.dto.UserCreateRequest;
import org.sp.payroll_service.api.auth.dto.UserFilter;
import org.sp.payroll_service.api.auth.dto.UserResponse;
import org.sp.payroll_service.api.auth.dto.UserUpdateRequest;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.common.service.BaseCrudService;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining the contract for User management operations.
 * It extends the generic CrudService, binding it to the specific
 * DTOs and ID type of the User domain.
 */
public interface UserService extends BaseCrudService<
        UUID,               // ID
        UserResponse,       // R (Response DTO)
        UserCreateRequest,// C (Creation DTO)
        UserUpdateRequest,  // U (Update DTO)
        UserFilter          // F (Filter DTO)
        > {
    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return a CompletableFuture containing the UserResponse, or null if not found
     */
    @Async("virtualThreadExecutor")
    CompletableFuture<UserResponse> findByUsername(@NotBlank String username);
}