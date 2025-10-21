package org.sp.payroll_service.api.wallet.dto;

import org.sp.payroll_service.domain.common.enums.OwnerType;
import java.util.UUID;

/**
 * DTO for filtering and searching Account entities.
 */
public record AccountFilter(
    // Search by accountName or accountNumber
    String keyword, 
    
    // Filter by the type of owner (e.g., filter only COMPANY accounts)
    OwnerType ownerType, 
    
    // Filter by a specific owner ID
    UUID ownerId 
) {}