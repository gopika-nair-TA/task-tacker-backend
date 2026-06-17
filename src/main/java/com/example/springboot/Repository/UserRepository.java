package com.example.springboot.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springboot.dao.User;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {

  //  List<String> findbyEmailId(String email);

    User findByEmail(String email);

}