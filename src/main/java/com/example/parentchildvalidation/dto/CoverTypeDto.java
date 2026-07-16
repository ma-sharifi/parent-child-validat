package com.example.parentchildvalidation.dto;

import java.math.BigDecimal;

import com.example.parentchildvalidation.validation.ChildChecks;
import com.example.parentchildvalidation.validation.CoverTypeSequenceProvider;
import com.example.parentchildvalidation.validation.ParentChecks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.group.GroupSequenceProvider;

/**
 * A single DTO used for both <b>parent</b> and <b>child</b> cover types.
 *
 * <p>The whole point of this class is that <em>the same field</em> can carry two
 * opposite constraints, each bound to a different validation group. At runtime
 * {@link CoverTypeSequenceProvider} inspects the object's own state (here, the
 * {@link #child} flag) and turns exactly one of those groups on. So there is no
 * need for a {@code ParentCoverTypeDto} / {@code ChildCoverTypeDto} split and no
 * need for controllers to pass {@code @Validated(SomeGroup.class)}.</p>
 *
 * <h2>The rules being expressed</h2>
 * <ul>
 *   <li><b>name</b> — always required (default group; note it has no {@code groups} attribute).</li>
 *   <li><b>parentId</b> — a child <em>must</em> reference its parent; a parent <em>must not</em>.</li>
 *   <li><b>coverageLimit</b> — a parent <em>must</em> define its own limit; a child <em>must not</em>
 *       (it inherits the parent's limit).</li>
 * </ul>
 */
@GroupSequenceProvider(CoverTypeSequenceProvider.class)
public class CoverTypeDto {

    /**
     * The discriminator that drives which group the provider activates.
     * Could equally be {@code parentId != null} — any piece of the object's own
     * state works, as long as the provider reads the same thing.
     */
    private boolean child;

    /** Always required, regardless of parent/child. Default (ungrouped) constraint. */
    @NotBlank
    private String name;

    /** A child must point at its parent; a parent must not have one. */
    @NotNull(groups = ChildChecks.class, message = "parentId is required for a child cover type")
    @Null(groups = ParentChecks.class, message = "parentId must be null for a parent cover type")
    private Long parentId;

    /** A parent owns the coverage limit; a child inherits it and must leave it null. */
    @NotNull(groups = ParentChecks.class, message = "coverageLimit is required for a parent cover type")
    @Positive(groups = ParentChecks.class, message = "coverageLimit must be positive")
    @Null(groups = ChildChecks.class, message = "coverageLimit must be null for a child cover type (it is inherited)")
    private BigDecimal coverageLimit;

    public CoverTypeDto() {
    }

    public CoverTypeDto(boolean child, String name, Long parentId, BigDecimal coverageLimit) {
        this.child = child;
        this.name = name;
        this.parentId = parentId;
        this.coverageLimit = coverageLimit;
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public BigDecimal getCoverageLimit() {
        return coverageLimit;
    }

    public void setCoverageLimit(BigDecimal coverageLimit) {
        this.coverageLimit = coverageLimit;
    }

    @Override
    public String toString() {
        return "CoverTypeDto{child=" + child + ", name='" + name + "', parentId=" + parentId
                + ", coverageLimit=" + coverageLimit + '}';
    }
}
