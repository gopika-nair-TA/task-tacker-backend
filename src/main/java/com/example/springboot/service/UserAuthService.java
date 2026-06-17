package com.example.springboot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.Repository.RoleRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;
import com.example.springboot.util.JwtUtil;

@Service
public class UserAuthService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> syncUserAndGenerateTokens(String email, String name) {
        User existingUser = userRepo.findByEmail(email);
        List<String> assignedRoles;

        if (existingUser == null) {
            // 🌟 NEW USER DETECTED! Auto-register them to the database.
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setSsoUid("google_" + System.currentTimeMillis());

            Role defaultRole = roleRepo.findByName("USER").orElse(null);
            
            if (defaultRole == null) {
                defaultRole = new Role();
                defaultRole.setName("USER");
                defaultRole = roleRepo.save(defaultRole);
            }
            
            newUser.setRoles(java.util.Collections.singleton(defaultRole));
            userRepo.save(newUser);

            assignedRoles = java.util.Collections.singletonList("USER");
            System.out.println("🌟 Brand new user registered: " + name + " (" + email + ")");
            
        } else {
            assignedRoles = roleService.fetchRolesForEmail(email);
            System.out.println("✅ Existing user logged in: " + name + " (" + email + ")");
        }

        System.out.println("Roles assigned to frontend: " + assignedRoles);

        // Generate enterprise tokens
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Build the payload Map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User synced successfully");
        response.put("roles", assignedRoles); 
        response.put("email", email);
        response.put("name", name); 
        response.put("accessToken", accessToken);   
        response.put("refreshToken", refreshToken); 

        return response;
    }

    public Map<String, Object> getUserRoleMap(String email) {
        List<String> roles = roleService.fetchRolesForEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("roles", roles);
        return response;
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        // Check if the refresh token exists and is valid
        if (refreshToken != null && jwtUtil.isTokenValid(refreshToken)) {
            String email = jwtUtil.extractEmail(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(email);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            
            System.out.println("🔄 Refresh Token traded for new Access Token for: " + email);
            return response;
        }
        
        // If invalid, throw an exception so the controller can return a 403
        throw new IllegalArgumentException("Refresh token expired or invalid");
    }
}