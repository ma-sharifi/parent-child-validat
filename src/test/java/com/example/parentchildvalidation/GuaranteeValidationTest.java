package com.example.parentchildvalidation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.parentchildvalidation.dto.AssetDto;
import com.example.parentchildvalidation.dto.GuaranteeDto;
import com.example.parentchildvalidation.dto.GuaranteeType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the two children of a guarantee enforce opposite rules from one DTO,
 * driven only by {@code type}. Every call is plain {@code validator.validate(dto)}.
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

    private Set<String> violationPaths(GuaranteeDto dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private List<AssetDto> oneAsset() {
        return List.of(new AssetDto("Warehouse #4", new BigDecimal("250000")));
    }

    // ----- COLLATERAL: must HAVE assets + borrowerRating -----

    @Test
    void validCollateral_hasNoViolations() {
        // A collateral leans on assets, so guarantorName must be null.
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, null, oneAsset(), "BBB");
        assertThat(violationPaths(collateral)).isEmpty();
    }

    @Test
    void collateralWithGuarantorName_isRejected() {
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, "Acme Ltd", oneAsset(), "BBB");
        assertThat(violationPaths(collateral)).contains("guarantorName");
    }

    @Test
    void collateralWithoutAssets_isRejected() {
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, null, null, "BBB");
        assertThat(violationPaths(collateral)).contains("assets");
    }

    @Test
    void collateralWithEmptyAssets_isRejected() {
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, null, List.of(), "BBB");
        assertThat(violationPaths(collateral)).contains("assets");
    }

    @Test
    void collateralWithoutBorrowerRating_isRejected() {
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, null, oneAsset(), null);
        assertThat(violationPaths(collateral)).contains("borrowerRating");
    }

    @Test
    void collateralWithInvalidNestedAsset_cascadesAndIsRejected() {
        // Blank description + non-positive value: cascaded @Valid must catch it.
        List<AssetDto> bad = List.of(new AssetDto("  ", new BigDecimal("-1")));
        GuaranteeDto collateral = new GuaranteeDto(GuaranteeType.COLLATERAL, null, bad, "BBB");
        Set<String> paths = violationPaths(collateral);
        assertThat(paths).contains("assets[0].description", "assets[0].estimatedValue");
    }

    // ----- PROMISE: must have NEITHER (both null) -----

    @Test
    void validPromise_hasNoViolations() {
        GuaranteeDto promise = new GuaranteeDto(GuaranteeType.PROMISE, "Jane Doe", null, null);
        assertThat(violationPaths(promise)).isEmpty();
    }

    @Test
    void promiseWithAssets_isRejected() {
        GuaranteeDto promise = new GuaranteeDto(GuaranteeType.PROMISE, "Jane Doe", oneAsset(), null);
        assertThat(violationPaths(promise)).contains("assets");
    }

    @Test
    void promiseWithBorrowerRating_isRejected() {
        GuaranteeDto promise = new GuaranteeDto(GuaranteeType.PROMISE, "Jane Doe", null, "BBB");
        assertThat(violationPaths(promise)).contains("borrowerRating");
    }

    @Test
    void promiseWithoutGuarantorName_isRejected() {
        GuaranteeDto promise = new GuaranteeDto(GuaranteeType.PROMISE, null, null, null);
        assertThat(violationPaths(promise)).contains("guarantorName");
    }

    // ----- Default (ungrouped) constraints still fire (gotcha #1) -----

    @Test
    void missingType_hasNoGuarantorNameRuleButTypeItselfIsRejected() {
        // guarantorName's rules live in group-bound constraints; with no type the
        // provider adds neither group, so only the default @NotNull on type fires.
        GuaranteeDto noType = new GuaranteeDto(null, "Anyone", null, null);
        assertThat(violationPaths(noType)).contains("type");
    }
}
