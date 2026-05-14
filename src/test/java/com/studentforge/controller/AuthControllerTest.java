package com.studentforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentforge.dto.request.LoginRequest;
import com.studentforge.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void register_Created() throws Exception {
        String email = "r_" + System.currentTimeMillis() + "@t.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegisterRequest(email, "password123", "A", "B"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_Ok() throws Exception {
        String email = "l_" + System.currentTimeMillis() + "@t.com";
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                        new RegisterRequest(email, "password123", "A", "B"))));
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new LoginRequest(email, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_BadPassword_Unauthorized() throws Exception {
        String email = "lp_" + System.currentTimeMillis() + "@t.com";
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                        new RegisterRequest(email, "password123", "A", "B"))));
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new LoginRequest(email, "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturnNewTokens() throws Exception {
        String email = "ref_" + System.currentTimeMillis() + "@t.com";
        var regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new RegisterRequest(email, "password123", "A", "B"))))
                .andExpect(status().isCreated())
                .andReturn();
        String refreshToken = mapper.readTree(regResult.getResponse().getContentAsString()).get("refreshToken").asText();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_InvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}