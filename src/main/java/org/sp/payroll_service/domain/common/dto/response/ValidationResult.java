package org.sp.payroll_service.domain.common.dto.response;

import java.util.List;

/**
 * Validation result record.
 * @param isValid validation transactionStatus
 * @param field validated field name
 * @param value validated value
 * @param errors validation errors
 * @param warnings validation warnings
 */
public record ValidationResult(
    boolean isValid,
    String field,
    Object value,
    List<String> errors,
    List<String> warnings
) {
    /**
     * Creates successful validation result.
     * @param field field name
     * @param value field value
     * @return valid result
     */
    public static ValidationResult valid(String field, Object value) {
        return new ValidationResult(true, field, value, List.of(), List.of());
    }
    
    /**
     * Creates failed validation result.
     * @param field field name
     * @param value field value
     * @param errors validation errors
     * @return invalid result
     */
    public static ValidationResult invalid(String field, Object value, List<String> errors) {
        return new ValidationResult(false, field, value, errors, List.of());
    }
    
    /**
     * Checks if validation can continue in chain.
     * @return true if chain can continue
     */
    public boolean canContinueChain() {
        return errors.isEmpty() || errors.stream().noneMatch(error -> error.contains("FATAL"));
    }
}