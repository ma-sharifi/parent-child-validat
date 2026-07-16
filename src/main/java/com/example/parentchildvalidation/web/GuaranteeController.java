package com.example.parentchildvalidation.web;

import com.example.parentchildvalidation.dto.GuaranteeDto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Same story as {@code CoverTypeController}: plain {@code @Valid}, no
 * {@code @Validated(group)}. A COLLATERAL payload and a PROMISE payload hit the
 * same method and get different rules, chosen by {@code GuaranteeSequenceProvider}.
 */
@RestController
@RequestMapping("/guarantees")
public class GuaranteeController {

    @PostMapping
    public ResponseEntity<GuaranteeDto> create(@Valid @RequestBody GuaranteeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
