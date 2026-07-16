package com.example.parentchildvalidation.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

/**
 * A <b>child</b> of {@link Guarantee}: an unsecured personal promise.
 *
 * <p>Mirror image of {@link Collateral}. It constrains the same inherited
 * fields, in the opposite direction, by overriding their getters:</p>
 * <ul>
 *   <li>{@code guarantorName} is required (the promise leans on the guarantor);</li>
 *   <li>{@code assets} must be {@code null};</li>
 *   <li>{@code borrowerRating} must be {@code null}.</li>
 * </ul>
 */
public class Promise extends Guarantee {

    public Promise() {
        super();
        setType(GuaranteeType.PROMISE);
    }

    public Promise(String guarantorName, List<AssetDto> assets, String borrowerRating) {
        super(GuaranteeType.PROMISE, guarantorName, assets, borrowerRating);
    }

    @Override
    @NotBlank(message = "guarantorName is required for a PROMISE guarantee")
    public String getGuarantorName() {
        return super.getGuarantorName();
    }

    @Override
    @Null(message = "assets must be null for a PROMISE guarantee")
    public List<AssetDto> getAssets() {
        return super.getAssets();
    }

    @Override
    @Null(message = "borrowerRating must be null for a PROMISE guarantee")
    public String getBorrowerRating() {
        return super.getBorrowerRating();
    }
}
