package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.springboot.Repository.ProjectRepository;
import com.example.springboot.Repository.TaskRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Project;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.Task;
import com.example.springboot.dao.User;
import com.example.springboot.model.TaskStatus;

@ExtendWith(MockitoExtension.class)
class TaskTrackerServiceTests {

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private TaskTrackerService taskTrackerService;

    @Test
    void getVisibleProjects_asAdmin_returnsAllProjects() {
        User adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setEmail("admin@example.com");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        Project project1 = new Project();
        project1.setId(10L);
        project1.setName("Project Alpha");

        Project project2 = new Project();
        project2.setId(20L);
        project2.setName("Project Beta");

        when(userRepo.findByEmail("admin@example.com")).thenReturn(adminUser);
        when(projectRepo.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<Project> result = taskTrackerService.getVisibleProjects("admin@example.com");

        assertEquals(2, result.size());
        assertEquals("Project Alpha", result.get(0).getName());
        assertEquals("Project Beta", result.get(1).getName());
    }

    @Test
    void updateTaskStatus_existingTask_updatesStatus() {
        Task existingTask = new Task();
        existingTask.setId(100L);
        existingTask.setStatus(TaskStatus.NEW);

        when(taskRepo.findById(100L)).thenReturn(Optional.of(existingTask));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskTrackerService.updateTaskStatus(100L, "IN_PROGRESS");

        assertEquals(TaskStatus.IN_PROGRESS, existingTask.getStatus());
        verify(taskRepo).save(existingTask);
    }

    @Test
    void getVisibleProjects_asRegularUser_returnsAssignedProjectsOnly() {
        Role userRole = new Role();
        userRole.setName("USER");

        User regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setEmail("member@example.com");
        regularUser.setFullName("Member User");
        regularUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        Project project1 = new Project();
        project1.setId(11L);
        project1.setName("Project One");

        Project project2 = new Project();
        project2.setId(22L);
        project2.setName("Project Two");

        Task assignedTask = new Task();
        assignedTask.setId(200L);
        assignedTask.setProject(project2);

        when(userRepo.findByEmail("member@example.com")).thenReturn(regularUser);
        when(projectRepo.findAll()).thenReturn(Arrays.asList(project1, project2));
        when(taskRepo.findByTaskAssignedTo_UserId(2L)).thenReturn(Collections.singletonList(assignedTask));

        List<Project> result = taskTrackerService.getVisibleProjects("member@example.com");

        assertEquals(1, result.size());
        assertEquals(22L, result.get(0).getId());
        assertEquals("Project Two", result.get(0).getName());
    }

    @Test
    void getVisibleProjects_userNotFound_throwsException() {
        when(userRepo.findByEmail("unknown@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> taskTrackerService.getVisibleProjects("unknown@example.com"));
    }

    @Test
    void addProject_savesAndReturnsProject() {
        Project project = new Project();
        project.setName("New Project");

        when(projectRepo.save(project)).thenReturn(project);

        Project saved = taskTrackerService.addProject(project);

        assertEquals("New Project", saved.getName());
    }

    @Test
    void getTasksByProjectId_existingProject_returnsTasks() {
        Project project = new Project();
        project.setId(5L);

        Task taskA = new Task();
        taskA.setId(21L);
        taskA.setProject(project);

        project.setTasks(Arrays.asList(taskA));
        when(projectRepo.findById(5L)).thenReturn(Optional.of(project));

        List<Task> tasks = taskTrackerService.getTasksByProjectId(5L);

        assertEquals(1, tasks.size());
        assertEquals(Long.valueOf(21L), tasks.get(0).getId());
    }

    @Test
    void addTask_resolvesProjectAndUsers_thenSavesTask() {
        Project project = new Project();
        project.setId(7L);

        User assignUser = new User();
        assignUser.setUserId(11L);

        User creatorUser = new User();
        creatorUser.setUserId(12L);

        Task task = new Task();
        task.setProject(project);
        task.setTaskAssignedTo(assignUser);
        task.setTaskCreatedBy(creatorUser);

        when(projectRepo.findById(7L)).thenReturn(Optional.of(project));
        when(userRepo.findById(11L)).thenReturn(Optional.of(assignUser));
        when(userRepo.findById(12L)).thenReturn(Optional.of(creatorUser));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task savedTask = taskTrackerService.addTask(task);

        assertEquals(Long.valueOf(7L), savedTask.getProject().getId());
        assertEquals(Long.valueOf(11L), savedTask.getTaskAssignedTo().getUserId());
        assertEquals(Long.valueOf(12L), savedTask.getTaskCreatedBy().getUserId());
    }

    @Test
    void getTaskById_existingTask_returnsTask() {
        Task task = new Task();
        task.setId(30L);
        when(taskRepo.findById(30L)).thenReturn(Optional.of(task));

        Task found = taskTrackerService.getTaskById(30L);

        assertEquals(Long.valueOf(30L), found.getId());
    }

    @Test
    void getTaskById_notFound_throwsException() {
        when(taskRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskTrackerService.getTaskById(99L));
    }

    @Test
    void updateTask_existingTask_appliesChanges() {
        Task existingTask = new Task();
        existingTask.setId(40L);
        existingTask.setTaskName("Old Name");
        existingTask.setDescription("Old Desc");

        Task taskDetails = new Task();
        taskDetails.setTaskName("New Name");
        taskDetails.setDescription("New Desc");
        taskDetails.setDueDate(java.time.LocalDate.now().plusDays(10));

        when(taskRepo.findById(40L)).thenReturn(Optional.of(existingTask));
        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskTrackerService.updateTask(40L, taskDetails);

        assertEquals("New Name", updated.getTaskName());
        assertEquals("New Desc", updated.getDescription());
    }

    @Test
    void deleteTask_existingTask_deletesTask() {
        when(taskRepo.existsById(50L)).thenReturn(true);

        taskTrackerService.deleteTask(50L);

        verify(taskRepo).deleteById(50L);
    }

    @Test
    void deleteTask_notFound_throwsException() {
        when(taskRepo.existsById(60L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> taskTrackerService.deleteTask(60L));
    }

    @Test
    void getMyTasks_returnsAssignedTasks() {
        Task assignedTask = new Task();
        assignedTask.setId(70L);

        when(taskRepo.findByTaskAssignedTo_userId(3L)).thenReturn(Collections.singletonList(assignedTask));

        List<Task> myTasks = taskTrackerService.getMyTasks(3L);

        assertEquals(1, myTasks.size());
        assertEquals(Long.valueOf(70L), myTasks.get(0).getId());
    }
}
