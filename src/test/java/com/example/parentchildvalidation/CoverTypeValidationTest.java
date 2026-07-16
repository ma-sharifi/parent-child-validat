package com.example.parentchildvalidation;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.parentchildvalidation.dto.CoverTypeDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure Bean Validation tests (no Spring context) proving that the single DTO
 * enforces different rules for parent vs. child, driven entirely by the
 * {@code @GroupSequenceProvider}. Note that every call is plain
 * {@code validator.validate(dto)} — no group is passed in by hand.
 */
class CoverTypeValidationTest {

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

    private Set<String> violationPaths(CoverTypeDto dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    // ----- Parent -----

    @Test
    void validParent_hasNoViolations() {
        CoverTypeDto parent = new CoverTypeDto(false, "Motor", null, new BigDecimal("100000"));
        assertThat(violationPaths(parent)).isEmpty();
    }

    @Test
    void parentWithoutCoverageLimit_isRejected() {
        CoverTypeDto parent = new CoverTypeDto(false, "Motor", null, null);
        assertThat(violationPaths(parent)).contains("coverageLimit");
    }

    @Test
    void parentThatCarriesAParentId_isRejected() {
        CoverTypeDto parent = new CoverTypeDto(false, "Motor", 42L, new BigDecimal("100000"));
        assertThat(violationPaths(parent)).contains("parentId");
    }

    // ----- Child -----

    @Test
    void validChild_hasNoViolations() {
        CoverTypeDto child = new CoverTypeDto(true, "Motor - Third Party", 42L, null);
        assertThat(violationPaths(child)).isEmpty();
    }

    @Test
    void childWithoutParentId_isRejected() {
        CoverTypeDto child = new CoverTypeDto(true, "Motor - Third Party", null, null);
        assertThat(violationPaths(child)).contains("parentId");
    }

    @Test
    void childThatCarriesItsOwnCoverageLimit_isRejected() {
        CoverTypeDto child = new CoverTypeDto(true, "Motor - Third Party", 42L, new BigDecimal("5000"));
        assertThat(violationPaths(child)).contains("coverageLimit");
    }

    // ----- Default (ungrouped) constraint still fires (gotcha #1) -----

    @Test
    void blankName_isRejectedForParentAndChild() {
        CoverTypeDto parent = new CoverTypeDto(false, "  ", null, new BigDecimal("100000"));
        CoverTypeDto child = new CoverTypeDto(true, "  ", 42L, null);
        assertThat(violationPaths(parent)).contains("name");
        assertThat(violationPaths(child)).contains("name");
    }
}
