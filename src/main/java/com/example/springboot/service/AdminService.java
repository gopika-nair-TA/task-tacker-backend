package com.example.springboot.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.Repository.RoleRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    // Fetch all users
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Add a new user with proper role mapping
    public User addUser(User user) {
        Set<Role> actualDatabaseRoles = new HashSet<>();

        if (user.getRoles() != null) {
            for (Role incomingRole : user.getRoles()) {
                Optional<Role> realRole = roleRepo.findByName(incomingRole.getName());
                realRole.ifPresent(actualDatabaseRoles::add);
            }
        }

        user.setRoles(actualDatabaseRoles);
        return userRepo.save(user);
    }

    // Update an existing user with proper role mapping
    public User updateUser(Long userId, User updatedUserData) {
        Optional<User> existingUserOptional = userRepo.findById(userId);

        if (!existingUserOptional.isPresent()) {
            throw new IllegalArgumentException("User not found with ID " + userId);
        }

        User existingUser = existingUserOptional.get();

        // Update basic fields
        existingUser.setFullName(updatedUserData.getFullName());
        existingUser.setEmail(updatedUserData.getEmail());

        // Update roles
        Set<Role> actualDatabaseRoles = new HashSet<>();
        if (updatedUserData.getRoles() != null) {
            for (Role incomingRole : updatedUserData.getRoles()) {
                Optional<Role> realRole = roleRepo.findByName(incomingRole.getName());
                realRole.ifPresent(actualDatabaseRoles::add);
            }
        }

        existingUser.setRoles(actualDatabaseRoles);
        return userRepo.save(existingUser);
    }
}