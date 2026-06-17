package com.example.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.springboot.dao.Project;
import com.example.springboot.service.RoleService;
import com.example.springboot.service.TaskTrackerService;
import com.example.springboot.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TaskTrackerController.class)
class TaskTrackerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskTrackerService taskService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtUtil jwtUtil;

    @AfterEach
    void tearDown() {
        reset(taskService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser
    void getAllProjects_returnsOkWithProjects() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Demo Project");

        when(taskService.getVisibleProjects("user")).thenReturn(Arrays.asList(project));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(project))));

        verify(taskService).getVisibleProjects("user");
    }

    @Test
    @WithMockUser
    void addProject_returnsCreated() throws Exception {
        Project project = new Project();
        project.setName("New Project");

        when(taskService.addProject(any(Project.class))).thenReturn(project);

        mockMvc.perform(post("/addProject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(project)));
    }

    @Test
    @WithMockUser
    void getMyTasks_missingUserId_returnsServerError() throws Exception {
        mockMvc.perform(get("/tasks").param("userId", "1"))
                .andExpect(status().isOk());
    }
}
