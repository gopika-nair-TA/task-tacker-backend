package com.example.springboot.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.Repository.ProjectRepository;
import com.example.springboot.Repository.TaskRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Project;
import com.example.springboot.dao.Task;
import com.example.springboot.dao.User;
import com.example.springboot.model.TaskStatus;

@Service
public class TaskTrackerService {

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private UserRepository userRepo;

    // Fetch projects based on the logged-in user's roles
    public List<Project> getVisibleProjects(String loggedInEmail) {
        User currentUser = userRepo.findByEmail(loggedInEmail);
        if (currentUser == null) {
            throw new IllegalArgumentException("User not found in database.");
        }

        Long myUserId = currentUser.getUserId();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        boolean isManager = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MANAGER"));

        List<Project> allProjects = projectRepo.findAll();

        if (isAdmin) {
            return allProjects;
        } else if (isManager) {
            List<Task> tasksICreated = taskRepo.findByTaskCreatedBy_UserId(myUserId);
            List<Task> tasksAssignedToMe = taskRepo.findByTaskAssignedTo_UserId(myUserId);
            
            String myFullName = currentUser.getFullName() != null ? currentUser.getFullName().trim() : "";
            String myEmail = currentUser.getEmail() != null ? currentUser.getEmail().trim() : "";

            return allProjects.stream()
                    .filter(project -> {
                        // Checks both the saved Email string or Full Name string for bulletproof ownership matching
                        boolean iOwnThisProject = project.getProjectOwner() != null && 
                            (project.getProjectOwner().equalsIgnoreCase(myEmail) || project.getProjectOwner().equalsIgnoreCase(myFullName));
                        
                        boolean iCreatedTaskHere = tasksICreated.stream().anyMatch(t -> t.getProject().getId().equals(project.getId()));
                        boolean iAmAssignedTaskHere = tasksAssignedToMe.stream().anyMatch(t -> t.getProject().getId().equals(project.getId()));
                        return iOwnThisProject || iCreatedTaskHere || iAmAssignedTaskHere;
                    })
                    .collect(Collectors.toList());
        } else {
            // Re-added the regular user filter so your team members can see their assigned projects!
            List<Task> tasksAssignedToMe = taskRepo.findByTaskAssignedTo_UserId(myUserId);
            return allProjects.stream()
                    .filter(project -> tasksAssignedToMe.stream().anyMatch(t -> t.getProject().getId().equals(project.getId())))
                    .collect(Collectors.toList());
        }
    }

    public Project addProject(Project proj) {
        return projectRepo.save(proj);
    }

    public List<Task> getTasksByProjectId(long projectId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID " + projectId));
        return project.getTasks();
    }

    public Task addTask(Task task) {
        Long projectId = task.getProject().getId();
        Project realProject = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID " + projectId));
        task.setProject(realProject);

        if (task.getTaskAssignedTo() != null && task.getTaskAssignedTo().getUserId() != 0) {
            Long assignedId = task.getTaskAssignedTo().getUserId();
            User realAssignedUser = userRepo.findById(assignedId)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned User not found with ID " + assignedId));
            task.setTaskAssignedTo(realAssignedUser);
        }

        if (task.getTaskCreatedBy() != null && task.getTaskCreatedBy().getUserId() != 0) {
            Long creatorId = task.getTaskCreatedBy().getUserId();
            User realCreatorUser = userRepo.findById(creatorId)
                    .orElseThrow(() -> new IllegalArgumentException("Creator User not found with ID " + creatorId));
            task.setTaskCreatedBy(realCreatorUser);
        }

        return taskRepo.save(task);
    }

    public Task getTaskById(long taskId) {
        return taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID " + taskId));
    }

    public void updateTaskStatus(Long taskId, String statusString) {
        Task existingTask = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        existingTask.setStatus(TaskStatus.valueOf(statusString));
        taskRepo.save(existingTask);
    }

    public Task updateTask(Long taskId, Task taskDetails) {
        Task existingTask = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot update task. Task not found with ID " + taskId));

        existingTask.setTaskName(taskDetails.getTaskName());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setTaskAssignedTo(taskDetails.getTaskAssignedTo());
        existingTask.setDueDate(taskDetails.getDueDate());

        if (taskDetails.getStatus() != null) {
            existingTask.setStatus(taskDetails.getStatus());
        }

        return taskRepo.save(existingTask);
    }

    public void deleteTask(Long taskId) {
        if (!taskRepo.existsById(taskId)) {
            throw new IllegalArgumentException("Cannot delete task. Task not found with ID " + taskId);
        }
        taskRepo.deleteById(taskId);
    }

    public List<Task> getMyTasks(Long userId) {
        return taskRepo.findByTaskAssignedTo_userId(userId);
    }
}