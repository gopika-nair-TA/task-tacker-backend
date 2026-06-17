package com.example.springboot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.dao.Project;
import com.example.springboot.dao.Task;
import com.example.springboot.service.TaskTrackerService;

@RestController
@CrossOrigin(origins = "http://52.91.4.139.sslip.io", allowCredentials = "true")
public class TaskTrackerController {

    @Autowired
    private TaskTrackerService taskService;

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = null;

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            loggedInEmail = (String) jwtToken.getTokenAttributes().get("email");
        } else {
            loggedInEmail = authentication.getName();
        }

        if (loggedInEmail == null) {
            throw new IllegalArgumentException("Could not extract email from authentication token.");
        }

        List<Project> visibleProjects = taskService.getVisibleProjects(loggedInEmail);

        if (visibleProjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(visibleProjects);
    }

    @PostMapping("/addProject")
    public ResponseEntity<Project> addProject(@RequestBody Project proj) {
        Project savedProject = taskService.addProject(proj);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<Task>> getTasks(@PathVariable("projectId") long projectId) {
        List<Task> tasksList = taskService.getTasksByProjectId(projectId);
        return ResponseEntity.ok(tasksList);
    }

    @PostMapping("/addTask")
    public ResponseEntity<Task> addTask(@RequestBody Task task) {
        Task savedTask = taskService.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Task> fetchTask(@PathVariable("taskId") long taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<String> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> payload) {
        String statusString = payload.get("status");
        taskService.updateTaskStatus(taskId, statusString);
        return ResponseEntity.ok().body("Task status updated successfully");
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task taskDetails) {
        Task updatedTask = taskService.updateTask(taskId, taskDetails);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Task successfully deleted.");
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getMyTasks(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Error: 'userId' parameter is required.");
        }

        List<Task> myTasks = taskService.getMyTasks(userId);
        return ResponseEntity.ok(myTasks);
    }
}