package com.example.parentchildvalidation.web;

import com.example.parentchildvalidation.dto.CoverTypeDto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notice there is NO {@code @Validated(SomeGroup.class)} anywhere — plain
 * {@code @Valid} is enough. The {@code @GroupSequenceProvider} on the DTO picks
 * the correct group from the incoming object's own state, so the controller
 * stays completely unaware of the parent/child distinction.
 */
@RestController
@RequestMapping("/cover-types")
public class CoverTypeController {

    @PostMapping
    public ResponseEntity<CoverTypeDto> create(@Valid @RequestBody CoverTypeDto dto) {
        // If we got here, the right group's constraints all passed.
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
