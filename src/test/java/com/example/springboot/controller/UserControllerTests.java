package com.example.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.springboot.service.RoleService;
import com.example.springboot.service.UserAuthService;
import com.example.springboot.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAuthService userAuthService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void syncUser_returnsOkWithResponse() throws Exception {
        Map<String, Object> response = Collections.singletonMap("email", "user@example.com");
        when(userAuthService.syncUserAndGenerateTokens(eq("user@example.com"), eq("User Name")))
                .thenReturn(response);

        String payload = objectMapper.writeValueAsString(Collections.singletonMap("email", "user@example.com"));
        payload = payload.substring(0, payload.length() - 1) + ",\"name\":\"User Name\"}";

        mockMvc.perform(post("/api/users/sync")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser
    void getUserRole_returnsOkWithRoles() throws Exception {
        Map<String, Object> response = Collections.singletonMap("roles", Collections.singletonList("USER"));
        when(userAuthService.getUserRoleMap("user@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/role").param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser
    void refreshToken_returnsOkWithAccessToken() throws Exception {
        Map<String, String> response = Collections.singletonMap("accessToken", "new-access-token");
        when(userAuthService.refreshAccessToken("refresh-token")).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(Collections.singletonMap("refreshToken", "refresh-token"));

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}

