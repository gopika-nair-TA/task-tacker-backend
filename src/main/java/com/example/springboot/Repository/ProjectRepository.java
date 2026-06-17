package com.example.springboot.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springboot.dao.Project;
@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {

    
}
