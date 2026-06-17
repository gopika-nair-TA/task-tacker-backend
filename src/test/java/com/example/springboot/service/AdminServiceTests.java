package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.springboot.Repository.RoleRepository;
import com.example.springboot.Repository.UserRepository;
import com.example.springboot.dao.Role;
import com.example.springboot.dao.User;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllUsers_returnsAllUsers() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");

        when(userRepo.findAll()).thenReturn(Arrays.asList(user));

        assertEquals(1, adminService.getAllUsers().size());
    }

    @Test
    void addUser_mapsRolesFromDatabaseAndSaves() {
        User user = new User();
        Role incomingRole = new Role();
        incomingRole.setName("USER");
        user.setRoles(new HashSet<>(Arrays.asList(incomingRole)));

        Role databaseRole = new Role();
        databaseRole.setName("USER");

        when(roleRepo.findByName("USER")).thenReturn(Optional.of(databaseRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = adminService.addUser(user);

        assertEquals(1, saved.getRoles().size());
        assertEquals("USER", saved.getRoles().iterator().next().getName());
    }

    @Test
    void updateUser_existingUser_updatesFieldsAndRoles() {
        User existingUser = new User();
        existingUser.setUserId(2L);
        existingUser.setEmail("old@example.com");
        existingUser.setFullName("Old Name");

        User updateData = new User();
        updateData.setEmail("new@example.com");
        updateData.setFullName("New Name");
        Role incomingRole = new Role();
        incomingRole.setName("ADMIN");
        updateData.setRoles(new HashSet<>(Arrays.asList(incomingRole)));

        Role databaseRole = new Role();
        databaseRole.setName("ADMIN");

        when(userRepo.findById(2L)).thenReturn(Optional.of(existingUser));
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(databaseRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = adminService.updateUser(2L, updateData);

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("New Name", updated.getFullName());
        assertEquals(1, updated.getRoles().size());
        assertEquals("ADMIN", updated.getRoles().iterator().next().getName());
    }

    @Test
    void updateUser_userDoesNotExist_throwsException() {
        when(userRepo.findById(100L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.updateUser(100L, new User()));
    }
}
