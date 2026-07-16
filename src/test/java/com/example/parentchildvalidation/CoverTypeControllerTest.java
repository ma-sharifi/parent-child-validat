package com.example.parentchildvalidation;

import com.example.parentchildvalidation.web.CoverTypeController;
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
 * End-to-end through the web layer with plain {@code @Valid}. Same endpoint,
 * two payloads, two different rule sets — selected by the provider, not by the
 * controller.
 */
@WebMvcTest(CoverTypeController.class)
@Import(ValidationExceptionHandler.class)
class CoverTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validParentIsCreated() throws Exception {
        String body = """
                { "child": false, "name": "Motor", "parentId": null, "coverageLimit": 100000 }
                """;
        mockMvc.perform(post("/cover-types").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void validChildIsCreated() throws Exception {
        String body = """
                { "child": true, "name": "Motor - Third Party", "parentId": 42, "coverageLimit": null }
                """;
        mockMvc.perform(post("/cover-types").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void childCarryingItsOwnLimitIsRejected() throws Exception {
        String body = """
                { "child": true, "name": "Motor - Third Party", "parentId": 42, "coverageLimit": 5000 }
                """;
        mockMvc.perform(post("/cover-types").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.coverageLimit").exists());
    }

    @Test
    void parentMissingLimitIsRejected() throws Exception {
        String body = """
                { "child": false, "name": "Motor", "parentId": null, "coverageLimit": null }
                """;
        mockMvc.perform(post("/cover-types").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.coverageLimit").exists());
    }
}
