package com.example.springboot.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springboot.dao.Project;
import com.example.springboot.dao.Task;
@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {

    List<Task> findByTaskAssignedTo_userId(Long userId);

    List<Task> findByTaskCreatedBy_UserId(Long myUserId);

    List<Task> findByTaskAssignedTo_UserId(Long myUserId);

    
}
