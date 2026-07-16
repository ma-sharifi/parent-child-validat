package com.example.parentchildvalidation;

import com.example.parentchildvalidation.web.GuaranteeController;
import com.example.parentchildvalidation.web.ValidationExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer proof: one endpoint, a COLLATERAL body and a PROMISE body, two
 * different rule sets — selected by the provider, not by the controller.
 */
@WebMvcTest(GuaranteeController.class)
@Import(ValidationExceptionHandler.class)
class GuaranteeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validCollateralIsCreated() throws Exception {
        String body = """
                {
                  "type": "COLLATERAL",
                  "guarantorName": null,
                  "assets": [ { "description": "Warehouse #4", "estimatedValue": 250000 } ],
                  "borrowerRating": "BBB"
                }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void validPromiseIsCreated() throws Exception {
        String body = """
                { "type": "PROMISE", "guarantorName": "Jane Doe", "assets": null, "borrowerRating": null }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void collateralMissingAssetsAndRatingIsRejected() throws Exception {
        String body = """
                { "type": "COLLATERAL", "guarantorName": null, "assets": null, "borrowerRating": null }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.assets").exists())
                .andExpect(jsonPath("$.errors.borrowerRating").exists());
    }

    @Test
    void promiseCarryingAssetsAndRatingIsRejected() throws Exception {
        String body = """
                {
                  "type": "PROMISE",
                  "guarantorName": "Jane Doe",
                  "assets": [ { "description": "Warehouse #4", "estimatedValue": 250000 } ],
                  "borrowerRating": "BBB"
                }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.assets").exists())
                .andExpect(jsonPath("$.errors.borrowerRating").exists());
    }

    @Test
    void collateralCarryingGuarantorNameIsRejected() throws Exception {
        String body = """
                {
                  "type": "COLLATERAL",
                  "guarantorName": "Acme Ltd",
                  "assets": [ { "description": "Warehouse #4", "estimatedValue": 250000 } ],
                  "borrowerRating": "BBB"
                }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.guarantorName").exists());
    }

    @Test
    void promiseMissingGuarantorNameIsRejected() throws Exception {
        String body = """
                { "type": "PROMISE", "guarantorName": null, "assets": null, "borrowerRating": null }
                """;
        mockMvc.perform(post("/guarantees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.guarantorName").exists());
    }
}
