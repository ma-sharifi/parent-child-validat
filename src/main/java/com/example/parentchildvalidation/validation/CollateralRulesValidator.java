package com.example.parentchildvalidation.validation;

import java.util.List;
import java.util.Set;

import com.example.parentchildvalidation.dto.AssetDto;
import com.example.parentchildvalidation.dto.Collateral;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * The COLLATERAL rules, applied to the inherited fields without touching the
 * parent: {@code guarantorName} must be null, {@code assets} must be non-empty
 * (and each pledged asset valid), {@code borrowerRating} is required.
 *
 * <p>Each violation is attached to a specific property node, so Spring turns it
 * into a field error and the existing {@code ValidationExceptionHandler} reports
 * it under that field name — exactly like a normal per-field constraint.</p>
 */
public class CollateralRulesValidator implements ConstraintValidator<GuaranteeChildRules, Collateral> {

    /**
     * Reused to cascade into each {@link AssetDto} so its own constraints stay the
     * single source of truth. Static so it works both under Spring and in a plain
     * {@code Validation.buildDefaultValidatorFactory()} unit test.
     */
    private static final Validator ASSET_VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public boolean isValid(Collateral guarantee, ConstraintValidatorContext context) {
        if (guarantee == null) {
            return true; // null handling is a separate concern (@NotNull elsewhere)
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (guarantee.getGuarantorName() != null) {
            addPropertyViolation(context, "guarantorName",
                    "guarantorName must be null for a COLLATERAL guarantee");
            valid = false;
        }

        List<AssetDto> assets = guarantee.getAssets();
        if (assets == null || assets.isEmpty()) {
            addPropertyViolation(context, "assets", "assets is required for a COLLATERAL guarantee");
            valid = false;
        } else {
            valid &= validateEachAsset(assets, context);
        }

        if (guarantee.getBorrowerRating() == null) {
            addPropertyViolation(context, "borrowerRating",
                    "borrowerRating is required for a COLLATERAL guarantee");
            valid = false;
        }

        return valid;
    }

    private boolean validateEachAsset(List<AssetDto> assets, ConstraintValidatorContext context) {
        boolean valid = true;
        for (int i = 0; i < assets.size(); i++) {
            AssetDto asset = assets.get(i);
            if (asset == null) {
                continue;
            }
            Set<ConstraintViolation<AssetDto>> violations = ASSET_VALIDATOR.validate(asset);
            for (ConstraintViolation<AssetDto> violation : violations) {
                String leaf = violation.getPropertyPath().toString();
                addIndexedViolation(context, "assets", i, leaf, violation.getMessage());
                valid = false;
            }
        }
        return valid;
    }

    private void addPropertyViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }

    /** Builds a path like {@code assets[2].description}. */
    private void addIndexedViolation(ConstraintValidatorContext context, String iterable, int index,
                                     String leaf, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(iterable)
                .addPropertyNode(leaf).inIterable().atIndex(index)
                .addConstraintViolation();
    }
}
