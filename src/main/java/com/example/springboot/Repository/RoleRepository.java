package com.example.springboot.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springboot.dao.Project;
import com.example.springboot.dao.Role;
@Repository
public interface RoleRepository extends JpaRepository<Role,Long> 
 {

   Optional<Role> findByName(String name);

  //  String findbyEmailId(String email);
    
}
