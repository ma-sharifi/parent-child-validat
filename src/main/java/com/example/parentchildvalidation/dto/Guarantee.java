package com.example.parentchildvalidation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;

/**
 * The <b>parent</b>. It owns <em>all</em> the fields; the children add no state,
 * only the rules that apply to those inherited fields.
 *
 * <p>Because a field cannot be re-annotated in a subclass, each child instead
 * <b>overrides the getter</b> and puts its constraint there — see
 * {@link Collateral} and {@link Promise}. When you validate a child instance,
 * Hibernate Validator collects the constraints from the whole hierarchy, so the
 * parent's default rules (e.g. {@code @NotNull type}) and the child's
 * getter rules all fire together.</p>
 *
 * <p>The Jackson annotations make the JSON {@code "type"} field select which
 * concrete child to deserialize into, so a controller can accept a
 * {@code Guarantee} and Spring validates the actual subtype's rules.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Collateral.class, name = "COLLATERAL"),
        @JsonSubTypes.Type(value = Promise.class, name = "PROMISE")
})
public abstract class Guarantee {

    /** Always required. Discriminates the child both for JSON and for the reader. */
    @NotNull(message = "type is required")
    private GuaranteeType type;

    private String guarantorName;

    private List<AssetDto> assets;

    private String borrowerRating;

    protected Guarantee() {
    }

    protected Guarantee(GuaranteeType type, String guarantorName, List<AssetDto> assets, String borrowerRating) {
        this.type = type;
        this.guarantorName = guarantorName;
        this.assets = assets;
        this.borrowerRating = borrowerRating;
    }

    public GuaranteeType getType() {
        return type;
    }

    public void setType(GuaranteeType type) {
        this.type = type;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public List<AssetDto> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDto> assets) {
        this.assets = assets;
    }

    public String getBorrowerRating() {
        return borrowerRating;
    }

    public void setBorrowerRating(String borrowerRating) {
        this.borrowerRating = borrowerRating;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{type=" + type + ", guarantorName='" + guarantorName
                + "', assets=" + assets + ", borrowerRating='" + borrowerRating + "'}";
    }
}
