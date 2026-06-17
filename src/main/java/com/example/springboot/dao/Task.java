package com.example.springboot.dao;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.example.springboot.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Data
public class Task {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     private String taskName;
    private String description;
     private LocalDate createdOn;
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status; // Enum: TODO, IN_PROGRESS, DONE

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties("tasks") // Prevent infinite recursion
    private Project project;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User taskCreatedBy;

     @ManyToOne
    @JoinColumn(name = "assignedUserId")
    private User taskAssignedTo;
}
