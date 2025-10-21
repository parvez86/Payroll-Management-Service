package org.sp.payroll_service.domain.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money amount record with currency.
 * @param amount monetary amount
 * @param currency currency code (ISO 4217)
 * @param scale decimal scale
 */
public record Money(
    @NotNull BigDecimal amount,
    @NotBlank @Size(min = 3, max = 3) String currency,
    int scale
) {
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (scale < 0 || scale > 10) {
            throw new IllegalArgumentException("Scale must be between 0 and 10");
        }
        amount = amount.setScale(scale, RoundingMode.HALF_UP);
    }
    
    /**
     * Creates money amount with default currency (INR) and scale.
     * @param amount amount
     * @return money amount in INR
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, "INR", 2);
    }
    
    /**
     * Creates money amount with specified currency and default scale.
     * @param amount amount
     * @param currency currency code
     * @return money amount
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency, 2);
    }
    
    /**
     * Creates money amount from double with specified currency.
     * @param amount amount as double
     * @param currency currency code
     * @return money amount
     */
    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency, 2);
    }
    
    /**
     * Creates INR money amount.
     * @param amount amount in INR
     * @return money in INR
     */
    public static Money inr(BigDecimal amount) {
        return new Money(amount, "INR", 2);
    }
    
    /**
     * Creates zero money with specified currency.
     * @param currency currency code
     * @return zero money
     */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency, 2);
    }
    
    /**
     * Creates zero money in INR.
     * @return zero money in INR
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, "INR", 2);
    }
    
    /**
     * Adds another money amount (same currency).
     * @param other other money amount
     * @return sum
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(amount.add(other.amount), currency, scale);
    }
    
    /**
     * Subtracts another money amount (same currency).
     * @param other other money amount
     * @return difference
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency, scale);
    }
    
    private void validateSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}