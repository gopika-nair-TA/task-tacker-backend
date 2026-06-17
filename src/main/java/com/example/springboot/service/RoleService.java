package com.example.springboot.service; // Adjust your package!

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.Repository.RoleRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;

@Service
public class RoleService {

    @Autowired
    UserRepository userRepo ;

   public List<String> fetchRolesForEmail(String email) {
        System.out.println("🔍 1. RoleService looking up: " + email);
        
        if (email == null) return Collections.singletonList("USER");

        try {
            User user = userRepo.findByEmail(email);
            
            if (user == null) {
                System.out.println("❌ 2. DB says: User not found in app_user table!");
                return Collections.singletonList("USER");
            }
            
            System.out.println("✅ 2. DB found user: " + user.getFullName());

            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                System.out.println("❌ 3. DB says: User found, but they have ZERO roles attached in the mapping table!");
                return Collections.singletonList("USER");
            }

            System.out.println("✅ 3. DB found roles: " + user.getRoles().size());

            return user.getRoles().stream()
                    .map(Role::getName) // ⚠️ Make sure this matches your Role entity!
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("🚨 4. DATABASE CRASH: " + e.getMessage());
            e.printStackTrace(); // This will print the exact line of the error!
        }

        return Collections.singletonList("USER");
    }
   
}