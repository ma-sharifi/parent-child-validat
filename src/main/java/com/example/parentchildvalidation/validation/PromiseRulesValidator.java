package com.example.parentchildvalidation.validation;

import com.example.parentchildvalidation.dto.Promise;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * The PROMISE rules — the mirror image of {@link CollateralRulesValidator}:
 * {@code guarantorName} is required, {@code assets} and {@code borrowerRating}
 * must be null. Again, applied to inherited fields without touching the parent.
 */
public class PromiseRulesValidator implements ConstraintValidator<GuaranteeChildRules, Promise> {

    @Override
    public boolean isValid(Promise guarantee, ConstraintValidatorContext context) {
        if (guarantee == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        String guarantorName = guarantee.getGuarantorName();
        if (guarantorName == null || guarantorName.isBlank()) {
            addPropertyViolation(context, "guarantorName",
                    "guarantorName is required for a PROMISE guarantee");
            valid = false;
        }

        if (guarantee.getAssets() != null) {
            addPropertyViolation(context, "assets", "assets must be null for a PROMISE guarantee");
            valid = false;
        }

        if (guarantee.getBorrowerRating() != null) {
            addPropertyViolation(context, "borrowerRating",
                    "borrowerRating must be null for a PROMISE guarantee");
            valid = false;
        }

        return valid;
    }

    private void addPropertyViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
