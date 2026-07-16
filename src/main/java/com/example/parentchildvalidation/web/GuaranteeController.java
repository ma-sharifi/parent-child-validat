package com.example.parentchildvalidation.web;

import com.example.parentchildvalidation.dto.Guarantee;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller accepts the parent type {@link Guarantee}. Jackson reads the
 * JSON {@code "type"} and deserializes into {@code Collateral} or {@code Promise},
 * then plain {@code @Valid} validates whichever subtype arrived — so the child's
 * own getter constraints fire. No groups, no {@code @Validated} anywhere.
 */
@RestController
@RequestMapping("/guarantees")
public class GuaranteeController {

    @PostMapping
    public ResponseEntity<Guarantee> create(@Valid @RequestBody Guarantee guarantee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guarantee);
    }
}
