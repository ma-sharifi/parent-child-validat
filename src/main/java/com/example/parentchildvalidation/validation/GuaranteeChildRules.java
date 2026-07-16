package com.example.parentchildvalidation.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * A <b>class-level</b> constraint you put on a child of the (3rd-party) parent
 * {@code Guarantee}.
 *
 * <p>Why class-level? The parent owns all the fields and already carries its own
 * validation, and you cannot re-annotate an inherited field or its getter to add
 * child-specific rules. So instead of per-field annotations, a single constraint
 * on the child <em>class</em> hands the whole object to a
 * {@link jakarta.validation.ConstraintValidator} that checks the rules
 * programmatically and reports each problem against the right property.</p>
 *
 * <p>Two validators are registered — {@link CollateralRulesValidator} and
 * {@link PromiseRulesValidator}. Hibernate Validator picks the one whose target
 * type matches the child being validated, so each child keeps its own rules.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { CollateralRulesValidator.class, PromiseRulesValidator.class })
public @interface GuaranteeChildRules {

    String message() default "invalid guarantee";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
