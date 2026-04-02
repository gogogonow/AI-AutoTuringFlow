package com.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for WebConfig CORS configuration after Spring Boot 3.2.0 upgrade
 * Testing: CORS headers, allowed origins, allowed methods
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WebConfig CORS Configuration Tests")
class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CORS - should allow configured origin")
    void testCorsAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }

    @Test
    @DisplayName("CORS - should allow GET method")
    void testCorsAllowedGetMethod() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should allow POST method")
    void testCorsAllowedPostMethod() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should allow PUT method")
    void testCorsAllowedPutMethod() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should allow DELETE method")
    void testCorsAllowedDeleteMethod() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should allow PATCH method")
    void testCorsAllowedPatchMethod() throws Exception {
        mockMvc.perform(options("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS - should work with actual GET request")
    void testCorsWithActualGetRequest() throws Exception {
        mockMvc.perform(get("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }
}