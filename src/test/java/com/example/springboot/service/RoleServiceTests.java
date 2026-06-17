package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;

@ExtendWith(MockitoExtension.class)
class RoleServiceTests {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private RoleService roleService;

    @Test
    void fetchRolesForEmail_nullEmail_returnsUserRole() {
        assertEquals(Collections.singletonList("USER"), roleService.fetchRolesForEmail(null));
    }

    @Test
    void fetchRolesForEmail_userNotFound_returnsUserRole() {
        when(userRepo.findByEmail("missing@example.com")).thenReturn(null);

        assertEquals(Collections.singletonList("USER"), roleService.fetchRolesForEmail("missing@example.com"));
    }

    @Test
    void fetchRolesForEmail_noRoleAssigned_returnsUserRole() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setRoles(new HashSet<>());

        when(userRepo.findByEmail("user@example.com")).thenReturn(user);

        assertEquals(Collections.singletonList("USER"), roleService.fetchRolesForEmail("user@example.com"));
    }

    @Test
    void fetchRolesForEmail_returnsUppercaseRoles() {
        User user = new User();
        user.setEmail("admin@example.com");
        Role adminRole = new Role();
        adminRole.setName("admin");
        user.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        when(userRepo.findByEmail("admin@example.com")).thenReturn(user);

        assertEquals(Collections.singletonList("ADMIN"), roleService.fetchRolesForEmail("admin@example.com"));
    }
}
