# Parent/Child validation in Spring Boot with `@GroupSequenceProvider`

A tiny, runnable Spring Boot project that demonstrates how **one DTO** can enforce
**two different sets of rules** — one for "parent" objects and one for "child"
objects — without splitting into separate classes and without controllers ever
passing `@Validated(SomeGroup.class)`.

The engine behind it is Hibernate Validator's
[`@GroupSequenceProvider`](https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/#section-default-group-class):
you put opposite constraints (`@NotNull` and `@Null`) on the same field, each
bound to a different validation group, and a provider decides at runtime — based
on the object's own state — which group is active.

There are **two worked examples** in this repo:

1. **Guarantee → Collateral / Promise** (`GuaranteeDto`) — the clearest one. Two
   named children of a loan guarantee, discriminated by an enum `type`.
2. **CoverType → parent / child** (`CoverTypeDto`) — the original, discriminated
   by a boolean flag.

Both use the identical `@GroupSequenceProvider` mechanism.

## Example 1 — Guarantee: Collateral vs. Promise

A loan **Guarantee** is one of two children, chosen by its `type`:

| Field | `COLLATERAL` (child) | `PROMISE` (child) |
|-------|----------------------|-------------------|
| `type` | required | required |
| `guarantorName` | **must be null** | **required** |
| `assets` (list) | **required, non-empty** | **must be null** |
| `borrowerRating` | **required** | **must be null** |

```
                 GuaranteeDto (single class)
                 ├─ type            @NotNull                    (always)
                 ├─ guarantorName   @Null(CollateralChecks)     @NotBlank(PromiseChecks)
                 ├─ assets          @NotEmpty(CollateralChecks) @Null(PromiseChecks)
                 └─ borrowerRating  @NotNull(CollateralChecks)  @Null(PromiseChecks)
                              │
        GuaranteeSequenceProvider.getValidationGroups(dto)
                              │
        ┌─────────────────────┴─────────────────────┐
   type == COLLATERAL                          type == PROMISE
   → [GuaranteeDto, CollateralChecks]          → [GuaranteeDto, PromiseChecks]
   → assets & borrowerRating REQUIRED          → guarantorName REQUIRED
   → guarantorName must be NULL                → assets & borrowerRating must be NULL
```

`assets` is a `List<AssetDto>` carrying a cascaded `@Valid`, so each pledged
asset's own constraints are checked too — but only for a collateral, since a
promise's list must be null. Try it:

```bash
# Valid collateral -> 201  (guarantorName must be null; it's backed by assets)
curl -s -XPOST localhost:8080/guarantees -H 'Content-Type: application/json' -d '{
  "type":"COLLATERAL","guarantorName":null,
  "assets":[{"description":"Warehouse #4","estimatedValue":250000}],
  "borrowerRating":"BBB"}'

# Valid promise -> 201
curl -s -XPOST localhost:8080/guarantees -H 'Content-Type: application/json' -d '{
  "type":"PROMISE","guarantorName":"Jane Doe","assets":null,"borrowerRating":null}'

# Promise carrying assets/rating -> 400 with both fields flagged
curl -s -XPOST localhost:8080/guarantees -H 'Content-Type: application/json' -d '{
  "type":"PROMISE","guarantorName":"Jane Doe",
  "assets":[{"description":"Warehouse #4","estimatedValue":250000}],
  "borrowerRating":"BBB"}'
```

## Example 2 — CoverType parent/child

## The idea in one picture

```
                 CoverTypeDto (single class)
                 ├─ name          @NotBlank                 (always)
                 ├─ parentId      @NotNull(ChildChecks)     @Null(ParentChecks)
                 └─ coverageLimit @NotNull(ParentChecks)    @Null(ChildChecks)
                              │
        CoverTypeSequenceProvider.getValidationGroups(dto)
                              │
        ┌─────────────────────┴─────────────────────┐
   dto.isChild() == false                    dto.isChild() == true
   → [CoverTypeDto, ParentChecks]            → [CoverTypeDto, ChildChecks]
   → parentId must be null                   → parentId is required
   → coverageLimit is required               → coverageLimit must be null
```

A **parent** cover type (e.g. "Motor") owns a coverage limit and has no parent.
A **child** cover type (e.g. "Motor – Third Party") points at a parent and
inherits its limit, so it must *not* carry one of its own.

## The moving parts

| File | Role |
|------|------|
| `validation/CollateralChecks.java`, `validation/PromiseChecks.java` | Marker groups for the Guarantee example. |
| `dto/GuaranteeDto.java`, `dto/GuaranteeType.java`, `dto/AssetDto.java` | The Guarantee DTO (`@GroupSequenceProvider(...)`), its discriminator enum, and the nested asset (cascaded `@Valid`). |
| `validation/GuaranteeSequenceProvider.java` | Reads `type` and activates `CollateralChecks` or `PromiseChecks`. |
| `web/GuaranteeController.java` | Plain `@Valid` — no group named. |
| `validation/ParentChecks.java`, `validation/ChildChecks.java` | Marker groups for the CoverType example. |
| `dto/CoverTypeDto.java` | The CoverType DTO. Same fields carry opposite constraints bound to different groups. |
| `validation/CoverTypeSequenceProvider.java` | Reads the boolean flag and returns the groups to activate. |
| `web/CoverTypeController.java` | Plain `@Valid` — no group named. |
| `web/ValidationExceptionHandler.java` | Turns a failed `@Valid` into a clean 400 JSON body (shared by both). |

## The provider — the whole trick

```java
public class CoverTypeSequenceProvider implements DefaultGroupSequenceProvider<CoverTypeDto> {
    @Override
    public List<Class<?>> getValidationGroups(CoverTypeDto dto) {
        List<Class<?>> groups = new ArrayList<>();
        groups.add(CoverTypeDto.class);                 // gotcha #1 (see below)
        if (dto != null) {                              // gotcha #2 (see below)
            groups.add(dto.isChild() ? ChildChecks.class : ParentChecks.class);
        }
        return groups;
    }
}
```

## Two gotchas

1. **You must include the DTO's own class** (`CoverTypeDto.class`) in the returned
   list. If you forget it, the default/ungrouped constraints — like `@NotBlank`
   on `name` — silently stop firing. The `blankName_isRejectedForParentAndChild`
   test guards exactly this.
2. **`dto` can be `null`.** Hibernate Validator probes the provider at bootstrap
   with a null bean to learn the sequence, so keep the null check.

## Run it

```bash
mvn test          # runs the 11 tests that prove both parent and child paths
mvn spring-boot:run
```

Then:

```bash
# Valid parent  -> 201 Created
curl -s -XPOST localhost:8080/cover-types -H 'Content-Type: application/json' \
  -d '{"child":false,"name":"Motor","parentId":null,"coverageLimit":100000}'

# Valid child   -> 201 Created
curl -s -XPOST localhost:8080/cover-types -H 'Content-Type: application/json' \
  -d '{"child":true,"name":"Motor - Third Party","parentId":42,"coverageLimit":null}'

# Child with its own limit -> 400 with {"errors":{"coverageLimit": "..."}}
curl -s -XPOST localhost:8080/cover-types -H 'Content-Type: application/json' \
  -d '{"child":true,"name":"Motor - Third Party","parentId":42,"coverageLimit":5000}'
```

## When you *don't* need the provider

If parent and child are genuinely **separate classes**, you can't put `@Null` on
an *inherited* field, so the provider approach is the simplest route to keep one
type. If instead your two shapes justify two classes with no shared field-level
contradictions, plain per-class constraints (or per-endpoint
`@Validated(group)`) may be enough — reach for `@GroupSequenceProvider` when the
*same field* needs opposite rules depending on the object's state.

## Requirements

- Java 21
- Maven 3.9+
- Spring Boot 3.3.x / Hibernate Validator 8 (Jakarta Bean Validation 3.x)
