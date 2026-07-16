package com.example.parentchildvalidation.dto;

import java.util.List;

import com.example.parentchildvalidation.validation.GuaranteeChildRules;

/**
 * A <b>child</b> of {@link Guarantee}: a secured guarantee.
 *
 * <p>It adds no fields and — crucially — <b>does not</b> re-annotate or override
 * the inherited getters (the parent is a 3rd-party type that already owns its
 * validation). Instead the child-specific rules live in the class-level
 * {@link GuaranteeChildRules} constraint, checked by
 * {@code CollateralRulesValidator}:</p>
 * <ul>
 *   <li>{@code guarantorName} must be {@code null};</li>
 *   <li>{@code assets} must be a non-empty list of valid assets;</li>
 *   <li>{@code borrowerRating} is required.</li>
 * </ul>
 */
@GuaranteeChildRules
public class Collateral extends Guarantee {

    public Collateral() {
        super();
        setType(GuaranteeType.COLLATERAL);
    }

    public Collateral(String guarantorName, List<AssetDto> assets, String borrowerRating) {
        super(GuaranteeType.COLLATERAL, guarantorName, assets, borrowerRating);
    }
}
