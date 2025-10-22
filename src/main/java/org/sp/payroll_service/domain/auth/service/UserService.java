package org.sp.payroll_service.domain.auth.service;

import jakarta.validation.constraints.NotBlank;
import org.sp.payroll_service.api.auth.dto.*;
import org.sp.payroll_service.domain.common.service.BaseCrudService;

import java.util.UUID;

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
     * Find a user by token.
     *
     * @param username the username to search for
     * @return a UserResponse, or null if not found
     */
    UserResponse findByUsername(@NotBlank String username);

    /**
     * Find a user by token.
     *
     * @param accessToken the accessToken of the user
     * @return a UserResponse, or null if not found
     */
    UserDetailsResponse me (@NotBlank String accessToken);
}