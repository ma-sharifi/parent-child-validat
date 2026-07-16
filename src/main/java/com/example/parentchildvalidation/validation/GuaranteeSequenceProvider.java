package com.example.parentchildvalidation.validation;

import java.util.ArrayList;
import java.util.List;

import com.example.parentchildvalidation.dto.GuaranteeDto;
import com.example.parentchildvalidation.dto.GuaranteeType;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Picks the validation groups for a {@link GuaranteeDto} from its own
 * {@code type}: COLLATERAL turns on {@link CollateralChecks}, PROMISE turns on
 * {@link PromiseChecks}. That is what lets one class enforce the two children's
 * opposite rules.
 */
public class GuaranteeSequenceProvider implements DefaultGroupSequenceProvider<GuaranteeDto> {

    @Override
    public List<Class<?>> getValidationGroups(GuaranteeDto dto) {
        List<Class<?>> groups = new ArrayList<>();

        // GOTCHA #1: include the DTO's own class or the default constraints
        // (@NotNull type, @NotBlank guarantorName) stop firing.
        groups.add(GuaranteeDto.class);

        // GOTCHA #2: dto is null when Hibernate Validator probes the provider at
        // bootstrap. Also guard against a missing type (its own @NotNull will report it).
        if (dto != null && dto.getType() != null) {
            groups.add(dto.getType() == GuaranteeType.COLLATERAL
                    ? CollateralChecks.class
                    : PromiseChecks.class);
        }

        return groups;
    }
}
