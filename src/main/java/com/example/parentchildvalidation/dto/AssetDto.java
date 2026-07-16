package com.example.parentchildvalidation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * A single pledged asset inside a COLLATERAL guarantee.
 *
 * <p>These constraints have no group, so they belong to the default group. They
 * fire only when the list is actually validated — which, thanks to the cascaded
 * {@code @Valid} on {@code GuaranteeDto.assets}, happens for a collateral but
 * never for a promise (whose list must be null).</p>
 */
public class AssetDto {

    @NotBlank(message = "asset description is required")
    private String description;

    @NotNull(message = "asset estimatedValue is required")
    @Positive(message = "asset estimatedValue must be positive")
    private BigDecimal estimatedValue;

    public AssetDto() {
    }

    public AssetDto(String description, BigDecimal estimatedValue) {
        this.description = description;
        this.estimatedValue = estimatedValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    @Override
    public String toString() {
        return "AssetDto{description='" + description + "', estimatedValue=" + estimatedValue + '}';
    }
}
