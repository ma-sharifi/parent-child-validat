package com.example.parentchildvalidation.validation;

import java.util.ArrayList;
import java.util.List;

import com.example.parentchildvalidation.dto.CoverTypeDto;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Decides, per instance, which validation groups apply to a {@link CoverTypeDto}.
 *
 * <p>Hibernate Validator calls {@link #getValidationGroups(CoverTypeDto)} every
 * time the DTO is validated and activates every group in the returned list. That
 * is what lets one class behave like two: a parent instance turns on
 * {@link ParentChecks}, a child instance turns on {@link ChildChecks}.</p>
 */
public class CoverTypeSequenceProvider implements DefaultGroupSequenceProvider<CoverTypeDto> {

    @Override
    public List<Class<?>> getValidationGroups(CoverTypeDto dto) {
        List<Class<?>> groups = new ArrayList<>();

        // GOTCHA #1: you MUST include the DTO's own class, otherwise the default
        // (ungrouped) constraints — like @NotBlank on name — stop firing.
        groups.add(CoverTypeDto.class);

        // GOTCHA #2: Hibernate Validator probes the provider with a null bean at
        // bootstrap to learn the sequence, so guard against null.
        if (dto != null) {
            groups.add(dto.isChild() ? ChildChecks.class : ParentChecks.class);
        }

        return groups;
    }
}
