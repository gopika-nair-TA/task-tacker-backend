package com.example.springboot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtUtilTests {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generateAccessToken_extractEmailAndValidate() {
        String token = jwtUtil.generateAccessToken("test@example.com");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void generateRefreshToken_extractEmailAndValidate() {
        String token = jwtUtil.generateRefreshToken("refresh@example.com");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("refresh@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid-token"));
    }
}
