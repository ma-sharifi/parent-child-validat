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
| `validation/ParentChecks.java`, `validation/ChildChecks.java` | Empty marker interfaces used as validation-group labels. |
| `dto/CoverTypeDto.java` | The single DTO. Same fields carry opposite constraints bound to different groups. Annotated `@GroupSequenceProvider(...)`. |
| `validation/CoverTypeSequenceProvider.java` | Reads the object's state and returns the groups to activate. |
| `web/CoverTypeController.java` | Uses plain `@Valid` — no group is named anywhere. |
| `web/ValidationExceptionHandler.java` | Turns a failed `@Valid` into a clean 400 JSON body. |

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
