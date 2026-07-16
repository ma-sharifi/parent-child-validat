package com.example.parentchildvalidation.dto;

import java.util.List;

import com.example.parentchildvalidation.validation.CollateralChecks;
import com.example.parentchildvalidation.validation.GuaranteeSequenceProvider;
import com.example.parentchildvalidation.validation.PromiseChecks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.validator.group.GroupSequenceProvider;

/**
 * One DTO covering two children of a loan guarantee:
 *
 * <ul>
 *   <li><b>COLLATERAL</b> — must have a non-empty {@code assets} list and a
 *       {@code borrowerRating}.</li>
 *   <li><b>PROMISE</b> — must have <em>neither</em>; {@code assets} and
 *       {@code borrowerRating} must be null.</li>
 * </ul>
 *
 * <p>The {@code type} field is the discriminator. {@link GuaranteeSequenceProvider}
 * reads it and activates {@link CollateralChecks} or {@link PromiseChecks}, so the
 * same two fields carry opposite constraints and exactly one set fires per request.</p>
 */
@GroupSequenceProvider(GuaranteeSequenceProvider.class)
public class GuaranteeDto {

    /** Drives which group the provider activates. Always required. */
    @NotNull(message = "type is required")
    private GuaranteeType type;

    /** A promise leans on the guarantor's name; a collateral leans on assets, so it must be null. */
    @NotBlank(groups = PromiseChecks.class, message = "guarantorName is required for a PROMISE guarantee")
    @Null(groups = CollateralChecks.class, message = "guarantorName must be null for a COLLATERAL guarantee")
    private String guarantorName;

    /**
     * A collateral must pledge at least one asset; a promise must pledge none.
     * {@code @Valid} cascades into each {@link AssetDto} — but only when the list
     * is present, i.e. for a collateral.
     */
    @Valid
    @NotEmpty(groups = CollateralChecks.class, message = "assets is required for a COLLATERAL guarantee")
    @Null(groups = PromiseChecks.class, message = "assets must be null for a PROMISE guarantee")
    private List<AssetDto> assets;

    /** A collateral is rated on the borrower; a promise carries no rating. */
    @NotNull(groups = CollateralChecks.class, message = "borrowerRating is required for a COLLATERAL guarantee")
    @Null(groups = PromiseChecks.class, message = "borrowerRating must be null for a PROMISE guarantee")
    private String borrowerRating;

    public GuaranteeDto() {
    }

    public GuaranteeDto(GuaranteeType type, String guarantorName, List<AssetDto> assets, String borrowerRating) {
        this.type = type;
        this.guarantorName = guarantorName;
        this.assets = assets;
        this.borrowerRating = borrowerRating;
    }

    public GuaranteeType getType() {
        return type;
    }

    public void setType(GuaranteeType type) {
        this.type = type;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public List<AssetDto> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDto> assets) {
        this.assets = assets;
    }

    public String getBorrowerRating() {
        return borrowerRating;
    }

    public void setBorrowerRating(String borrowerRating) {
        this.borrowerRating = borrowerRating;
    }

    @Override
    public String toString() {
        return "GuaranteeDto{type=" + type + ", guarantorName='" + guarantorName + "', assets=" + assets
                + ", borrowerRating='" + borrowerRating + "'}";
    }
}
