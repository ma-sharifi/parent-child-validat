package com.example.parentchildvalidation.validation;

/**
 * Marker group activated when a {@code CoverTypeDto} represents a <b>child</b>
 * cover type (a product that belongs to a parent cover type).
 *
 * <p>It carries no methods — a validation group is just a type used as a label
 * so that constraints tagged with {@code groups = ChildChecks.class} only fire
 * when this group is active.</p>
 */
public interface ChildChecks {
}
