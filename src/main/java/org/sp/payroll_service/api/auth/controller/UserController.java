package org.sp.payroll_service.api.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.auth.dto.*;
import org.sp.payroll_service.domain.auth.service.UserService;
import org.sp.payroll_service.domain.common.dto.response.HeaderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN', 'EMPLOYER')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody @Validated UserCreateRequest request,
            @AuthenticationPrincipal HeaderResponse principal) {
        UserResponse created = userService.create(request, principal);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER', 'EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody @Validated UserUpdateRequest request,
            @AuthenticationPrincipal HeaderResponse principal) {
        UserResponse updated = userService.update(id, request, principal);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal HeaderResponse principal) {
        log.info("User delete requested for {} by {} ({})", id, principal.username(), principal.userId());
        userService.delete(id, principal);
        log.info("User {} deleted by {}", id, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
