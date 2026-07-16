package com.example.parentchildvalidation.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

/**
 * A <b>child</b> of {@link Guarantee}: a secured guarantee.
 *
 * <p>It adds no fields — it inherits them all — and only constrains the
 * inherited fields by overriding their getters:</p>
 * <ul>
 *   <li>{@code guarantorName} must be {@code null} (it is backed by assets, not a name);</li>
 *   <li>{@code assets} must be a non-empty list (and each asset is cascaded with {@code @Valid});</li>
 *   <li>{@code borrowerRating} is required.</li>
 * </ul>
 */
public class Collateral extends Guarantee {

    public Collateral() {
        super();
        setType(GuaranteeType.COLLATERAL);
    }

    public Collateral(String guarantorName, List<AssetDto> assets, String borrowerRating) {
        super(GuaranteeType.COLLATERAL, guarantorName, assets, borrowerRating);
    }

    @Override
    @Null(message = "guarantorName must be null for a COLLATERAL guarantee")
    public String getGuarantorName() {
        return super.getGuarantorName();
    }

    @Override
    @Valid
    @NotEmpty(message = "assets is required for a COLLATERAL guarantee")
    public List<AssetDto> getAssets() {
        return super.getAssets();
    }

    @Override
    @NotNull(message = "borrowerRating is required for a COLLATERAL guarantee")
    public String getBorrowerRating() {
        return super.getBorrowerRating();
    }
}
