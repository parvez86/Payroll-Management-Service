package org.sp.payroll_service.api.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check and system transactionStatus controller.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "System health and transactionStatus endpoints")
public class HealthController {

    /**
     * Basic health check endpoint.
     * @return health transactionStatus
     */
    @GetMapping
    @Operation(summary = "Health Check", description = "Returns system health transactionStatus")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "transactionStatus", "UP",
            "timestamp", Instant.now(),
            "service", "Payroll Management System",
            "version", "1.0.0"
        ));
    }

    /**
     * System information endpoint.
     * @return system details
     */
    @GetMapping("/info")
    @Operation(summary = "System Information", description = "Returns system information")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "application", Map.of(
                "name", "Payroll Management System",
                "description", "ACID-compliant payroll processing system",
                "version", "1.0.0",
                "build", Map.of(
                    "java", "24",
                    "spring-boot", "3.5.6"
                )
            ),
            "features", Map.of(
                "authentication", "JWT",
                "database", "PostgreSQL",
                "transactions", "ACID-compliant",
                "architecture", "Monolith"
            )
        ));
    }
}