# Parent/Child validation in Spring Boot

A tiny, runnable Spring Boot project showing how a "parent" shape and a "child"
shape can enforce **opposite rules on the same fields**. It demonstrates the
**two** standard ways to do this, one per example:

1. **Guarantee → Collateral / Promise** — *class-level custom constraint*. The
   parent is a 3rd-party type you can't modify (it already owns all its field
   validation), so each child applies its rules with a `@Constraint` on the
   child **class**, backed by a `ConstraintValidator`.
2. **CoverType → parent / child** — *single DTO + `@GroupSequenceProvider`*. One
   class, opposite constraints on the same field bound to different validation
   groups, and a provider picks the active group at runtime from the object's state.

Neither controller passes `@Validated(SomeGroup.class)` — both use plain `@Valid`.

## Example 1 — Guarantee: a 3rd-party parent with two children (custom class-level constraint)

`Guarantee` is the **parent that owns every field** — and here it stands in for a
**3rd-party type you cannot modify**. It already carries its own validation
(`@NotNull type`); you can't add child-specific rules to it, and you can't
re-annotate or override its getters in a subclass. `Collateral` and `Promise` are
the **children**; a loan guarantee is one of the two, chosen by its `type`:

| Field (declared on parent `Guarantee`) | `Collateral` (child) | `Promise` (child) |
|-------|----------------------|-------------------|
| `type` | required *(parent's own rule)* | required *(parent's own rule)* |
| `guarantorName` | **must be null** | **required** |
| `assets` (list) | **required, non-empty** | **must be null** |
| `borrowerRating` | **required** | **must be null** |

### Why not a getter override / group sequence?

Both of those need annotations *on the inherited fields or getters*, which you
cannot add to a 3rd-party class. The way out is a **class-level constraint on your
own child class**: annotate the child, and a `ConstraintValidator` receives the
whole object and checks the rules programmatically — attaching each violation to a
specific property node so it still surfaces as a normal per-field error.

```
        Guarantee (3rd-party parent — owns all fields + @NotNull type)
                 ▲                                   ▲
     extends     │                                   │   extends
   ┌─────────────┴───────────┐        ┌──────────────┴──────────────┐
   @GuaranteeChildRules             @GuaranteeChildRules
   class Collateral                 class Promise
        │                                  │
   CollateralRulesValidator         PromiseRulesValidator
   guarantorName → must be null     guarantorName → required
   assets        → non-empty+valid  assets        → must be null
   borrowerRating→ required         borrowerRating→ must be null
```

One annotation, `@GuaranteeChildRules`, registers **two** validators; Hibernate
Validator picks the one whose target type matches the child being validated. The
collateral validator also cascades into each `AssetDto` (reusing its own
constraints as the source of truth) and reports failures at `assets[i].<field>`.

Jackson's `@JsonTypeInfo` / `@JsonSubTypes` make the JSON `"type"` select which
concrete child to deserialize into; the controller accepts a `Guarantee`, and
Spring's `@Valid` validates whichever subtype arrived. Try it:

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

## Example 2 — CoverType parent/child (single DTO + `@GroupSequenceProvider`)

The other route: keep **one class** and let a
[`@GroupSequenceProvider`](https://docs.jboss.org/hibernate/validator/8.0/reference/en-US/html_single/#section-default-group-class)
decide which validation group applies from the object's own state. Put opposite
constraints (`@NotNull` and `@Null`) on the same field, each bound to a different
group.

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
| **Example 1 (class-level custom constraint)** | |
| `dto/Guarantee.java` | **Parent** (stands in for a 3rd-party type) — owns all the fields; its own `@NotNull type`; Jackson polymorphism. |
| `dto/Collateral.java`, `dto/Promise.java` | **Children** — no new fields, no getter annotations; each carries the class-level `@GuaranteeChildRules`. |
| `validation/GuaranteeChildRules.java` | The class-level `@Constraint`, registering both child validators. |
| `validation/CollateralRulesValidator.java`, `validation/PromiseRulesValidator.java` | The programmatic rules per child; attach violations to specific property nodes. |
| `dto/GuaranteeType.java`, `dto/AssetDto.java` | Discriminator enum, and the nested asset (constraints reused by the collateral validator). |
| `web/GuaranteeController.java` | Accepts the parent `Guarantee`; plain `@Valid` validates the deserialized subtype. |
| **Example 2 (`@GroupSequenceProvider`)** | |
| `validation/ParentChecks.java`, `validation/ChildChecks.java` | Marker groups. |
| `dto/CoverTypeDto.java` | Single DTO; same fields carry opposite constraints bound to different groups. |
| `validation/CoverTypeSequenceProvider.java` | Reads the boolean flag and returns the groups to activate. |
| `web/CoverTypeController.java` | Plain `@Valid` — no group named. |
| **Shared** | |
| `web/ValidationExceptionHandler.java` | Turns a failed `@Valid` into a clean 400 JSON body. |

### Example 2's provider — the whole trick

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

### Two gotchas (provider approach only)

1. **You must include the DTO's own class** (`CoverTypeDto.class`) in the returned
   list. If you forget it, the default/ungrouped constraints — like `@NotBlank`
   on `name` — silently stop firing. The `blankName_isRejectedForParentAndChild`
   test guards exactly this.
2. **`dto` can be `null`.** Hibernate Validator probes the provider at bootstrap
   with a null bean to learn the sequence, so keep the null check.

## Run it

```bash
mvn test          # runs 28 tests proving both children of both examples
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

## Which approach to choose

| | Example 1 — class-level custom constraint | Example 2 — `@GroupSequenceProvider` |
|-|-------------------------------------------|--------------------------------------|
| Classes | one parent + one class per child | one class total |
| Where the rules live | a `ConstraintValidator` per child | grouped constraints + a provider |
| You must remember | attach each violation to a property node so it maps to a field error | include the DTO's own class in the group list; null-check `dto` |
| Fits when | you can't touch the fields' class (3rd-party parent) or the rules are cross-field / conditional | you own the DTO and it's really one payload with state-dependent rules |
| Nested cascade | validator delegates to the nested bean's own validator | `@Valid` on the field (fires when its group is active) |

Both keep controllers and services "dumb": plain `@Valid`, no `@Validated(group)`.
The class-level route is the general fallback whenever you can't put the rule on
the field itself.

## Requirements

- Java 21
- Maven 3.9+
- Spring Boot 3.3.x / Hibernate Validator 8 (Jakarta Bean Validation 3.x)
