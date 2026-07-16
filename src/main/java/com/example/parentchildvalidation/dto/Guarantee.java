package com.example.parentchildvalidation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;

/**
 * The <b>parent</b>. Treat it as a <b>3rd-party type you cannot modify</b>: it
 * owns <em>all</em> the fields and already carries its own validation (here,
 * {@code @NotNull} on {@code type}). You cannot add child-specific rules to it,
 * nor re-annotate/override its getters in a subclass.
 *
 * <p>So the children — {@link Collateral} and {@link Promise} — add their rules
 * with a <b>class-level custom constraint</b> instead
 * ({@code @GuaranteeChildRules}). See those classes and their validators.</p>
 *
 * <p>(The Jackson annotations here are only to keep this demo runnable — they let
 * the JSON {@code "type"} pick the concrete child to deserialize. For a genuine
 * 3rd-party parent you'd configure polymorphism externally, e.g. with a Jackson
 * mix-in or {@code registerSubtypes}, rather than editing the class.)</p>
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
