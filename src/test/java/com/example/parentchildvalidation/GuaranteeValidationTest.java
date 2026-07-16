package com.example.parentchildvalidation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.parentchildvalidation.dto.AssetDto;
import com.example.parentchildvalidation.dto.Collateral;
import com.example.parentchildvalidation.dto.Guarantee;
import com.example.parentchildvalidation.dto.Promise;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The parent {@link Guarantee} (a stand-in for an unmodifiable 3rd-party type)
 * holds every field. Each child applies its rules with a class-level custom
 * constraint ({@code @GuaranteeChildRules}) — no getter overrides, no groups.
 * Validating a {@link Collateral} or {@link Promise} directly proves the child's
 * validator runs and reports against the right property paths.
 */
class GuaranteeValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private Set<String> violationPaths(Guarantee guarantee) {
        return validator.validate(guarantee).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private List<AssetDto> oneAsset() {
        return List.of(new AssetDto("Warehouse #4", new BigDecimal("250000")));
    }

    // ----- COLLATERAL: guarantorName null; assets + borrowerRating required -----

    @Test
    void validCollateral_hasNoViolations() {
        Guarantee collateral = new Collateral(null, oneAsset(), "BBB");
        assertThat(violationPaths(collateral)).isEmpty();
    }

    @Test
    void collateralWithGuarantorName_isRejected() {
        Guarantee collateral = new Collateral("Acme Ltd", oneAsset(), "BBB");
        assertThat(violationPaths(collateral)).contains("guarantorName");
    }

    @Test
    void collateralWithoutAssets_isRejected() {
        Guarantee collateral = new Collateral(null, null, "BBB");
        assertThat(violationPaths(collateral)).contains("assets");
    }

    @Test
    void collateralWithEmptyAssets_isRejected() {
        Guarantee collateral = new Collateral(null, List.of(), "BBB");
        assertThat(violationPaths(collateral)).contains("assets");
    }

    @Test
    void collateralWithoutBorrowerRating_isRejected() {
        Guarantee collateral = new Collateral(null, oneAsset(), null);
        assertThat(violationPaths(collateral)).contains("borrowerRating");
    }

    @Test
    void collateralWithInvalidNestedAsset_cascadesAndIsRejected() {
        // Blank description + non-positive value: cascaded @Valid on the child's
        // overridden getAssets() must catch it.
        List<AssetDto> bad = List.of(new AssetDto("  ", new BigDecimal("-1")));
        Guarantee collateral = new Collateral(null, bad, "BBB");
        assertThat(violationPaths(collateral)).contains("assets[0].description", "assets[0].estimatedValue");
    }

    // ----- PROMISE: guarantorName required; assets + borrowerRating null -----

    @Test
    void validPromise_hasNoViolations() {
        Guarantee promise = new Promise("Jane Doe", null, null);
        assertThat(violationPaths(promise)).isEmpty();
    }

    @Test
    void promiseWithoutGuarantorName_isRejected() {
        Guarantee promise = new Promise(null, null, null);
        assertThat(violationPaths(promise)).contains("guarantorName");
    }

    @Test
    void promiseWithAssets_isRejected() {
        Guarantee promise = new Promise("Jane Doe", oneAsset(), null);
        assertThat(violationPaths(promise)).contains("assets");
    }

    @Test
    void promiseWithBorrowerRating_isRejected() {
        Guarantee promise = new Promise("Jane Doe", null, "BBB");
        assertThat(violationPaths(promise)).contains("borrowerRating");
    }

    // ----- Parent's own default constraint still fires on both children -----

    @Test
    void missingType_isRejected() {
        Guarantee promise = new Promise("Jane Doe", null, null);
        promise.setType(null);
        assertThat(violationPaths(promise)).contains("type");
    }
}
