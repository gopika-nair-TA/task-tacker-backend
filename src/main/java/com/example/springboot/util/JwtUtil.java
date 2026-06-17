package com.example.springboot.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // IMPORTANT: In production, store this in application.properties!
    // This must be a secure, random string at least 32 characters long.
    private final String SECRET_KEY = "ThisIsASecretKeyForTickMateApplicationThatIsVerySecure";
    
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Access Token: Expires in 1 Hour
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; 
    
    // Refresh Token: Expires in 7 Days
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; 

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}