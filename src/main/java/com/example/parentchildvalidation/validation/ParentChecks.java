package com.example.parentchildvalidation.validation;

/**
 * Marker group activated when a {@code CoverTypeDto} represents a <b>parent</b>
 * cover type (a top-level category that does not belong to another cover type).
 *
 * <p>It carries no methods — a validation group is just a type used as a label
 * so that constraints tagged with {@code groups = ParentChecks.class} only fire
 * when this group is active.</p>
 */
public interface ParentChecks {
}
