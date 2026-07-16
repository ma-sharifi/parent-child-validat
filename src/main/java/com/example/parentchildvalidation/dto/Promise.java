package com.example.parentchildvalidation.dto;

import java.util.List;

import com.example.parentchildvalidation.validation.GuaranteeChildRules;

/**
 * A <b>child</b> of {@link Guarantee}: an unsecured personal promise.
 *
 * <p>Mirror image of {@link Collateral}. No fields, no getter overrides — the
 * rules live in the class-level {@link GuaranteeChildRules} constraint, checked
 * by {@code PromiseRulesValidator}:</p>
 * <ul>
 *   <li>{@code guarantorName} is required;</li>
 *   <li>{@code assets} must be {@code null};</li>
 *   <li>{@code borrowerRating} must be {@code null}.</li>
 * </ul>
 */
@GuaranteeChildRules
public class Promise extends Guarantee {

    public Promise() {
        super();
        setType(GuaranteeType.PROMISE);
    }

    public Promise(String guarantorName, List<AssetDto> assets, String borrowerRating) {
        super(GuaranteeType.PROMISE, guarantorName, assets, borrowerRating);
    }
}
