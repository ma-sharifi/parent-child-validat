package com.example.parentchildvalidation.dto;

/**
 * The discriminator that decides which child a {@code GuaranteeDto} is.
 *
 * <ul>
 *   <li>{@link #COLLATERAL} — a secured guarantee backed by real assets.</li>
 *   <li>{@link #PROMISE} — an unsecured personal promise, backed by nothing.</li>
 * </ul>
 */
public enum GuaranteeType {
    COLLATERAL,
    PROMISE
}
