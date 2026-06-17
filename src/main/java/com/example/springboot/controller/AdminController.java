package com.example.springboot.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.dao.User;
import com.example.springboot.service.AdminService;

@RestController
@CrossOrigin(origins = "http://52.91.4.139.sslip.io", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(users);
    }

    @PostMapping("/addUser")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User savedUser = adminService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable("userId") Long userId,
            @RequestBody User updatedUserData) {
        // If user doesn't exist, service throws IllegalArgumentException. 
        // GlobalExceptionHandler will catch it automatically and return a clean 404!
        User savedUser = adminService.updateUser(userId, updatedUserData);
        return ResponseEntity.ok(savedUser);
    }
}