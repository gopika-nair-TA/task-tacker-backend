package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
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
import com.example.springboot.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTests {

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserAuthService userAuthService;

    @Test
    void syncUserAndGenerateTokens_existingUser_returnsRolesAndTokens() {
        when(userRepo.findByEmail("member@example.com")).thenReturn(new User());
        when(roleService.fetchRolesForEmail("member@example.com")).thenReturn(Arrays.asList("USER"));
        when(jwtUtil.generateAccessToken("member@example.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("member@example.com")).thenReturn("refresh-token");

        Map<String, Object> payload = userAuthService.syncUserAndGenerateTokens("member@example.com", "Member");

        assertEquals("member@example.com", payload.get("email"));
        assertEquals("access-token", payload.get("accessToken"));
        assertEquals("refresh-token", payload.get("refreshToken"));
    }

    @Test
    void syncUserAndGenerateTokens_newUser_createsUserAndReturnsTokens() {
        when(userRepo.findByEmail("newuser@example.com")).thenReturn(null);
        when(roleRepo.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepo.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessToken("newuser@example.com")).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken("newuser@example.com")).thenReturn("new-refresh");

        Map<String, Object> payload = userAuthService.syncUserAndGenerateTokens("newuser@example.com", "New User");

        assertEquals("newuser@example.com", payload.get("email"));
        assertEquals("new-access", payload.get("accessToken"));
        assertEquals("new-refresh", payload.get("refreshToken"));
    }

    @Test
    void refreshAccessToken_validToken_returnsNewAccessToken() {
        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-token")).thenReturn("member@example.com");
        when(jwtUtil.generateAccessToken("member@example.com")).thenReturn("refreshed-access");

        Map<String, String> response = userAuthService.refreshAccessToken("valid-token");

        assertEquals("refreshed-access", response.get("accessToken"));
    }

    @Test
    void refreshAccessToken_invalidToken_throwsException() {
        when(jwtUtil.isTokenValid("bad-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userAuthService.refreshAccessToken("bad-token"));
    }
}
