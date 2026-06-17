package com.example.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.RoleService;
import com.example.springboot.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AdminController.class)
class AdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsOkWithUsers() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");

        when(adminService.getAllUsers()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(user))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUser_returnsCreatedWithUser() throws Exception {
        User user = new User();
        user.setEmail("newuser@example.com");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Arrays.asList(role).stream().collect(java.util.stream.Collectors.toSet()));

        when(adminService.addUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/addUser")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_returnsOkWithUpdatedUser() throws Exception {
        User user = new User();
        user.setUserId(2L);
        user.setEmail("updated@example.com");

        when(adminService.updateUser(any(Long.class), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }
}
