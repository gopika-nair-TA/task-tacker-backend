package com.example.springboot.config; // <-- Adjust to your actual package name

// <-- Ensure this points to your JwtUtil
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.springboot.util.JwtUtil;

import java.io.IOException;
import java.util.ArrayList;

@Component // 🌟 This tells Spring to manage this class so we can inject it into SecurityConfig!
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Extract the Authorization header from the incoming request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. If the header is missing or doesn't start with "Bearer ", ignore it and move on.
        // (This allows public endpoints like /api/users/sync to pass through without errors)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract the actual token string (removing "Bearer ")
            jwt = authHeader.substring(7);
            
            // 4. Extract the user's email from the token
            userEmail = jwtUtil.extractEmail(jwt);

            // 5. If we found an email AND the user isn't already authenticated in this request cycle...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 6. Check if the token is still valid (not expired)
                if (jwtUtil.isTokenValid(jwt)) {
                    
                    // 7. Create the Spring Security Authentication object
                    // Note: We are using an empty ArrayList for roles here because your React frontend 
                    // is currently handling role-based routing. If you want Spring to enforce method-level 
                    // @PreAuthorize("hasRole('ADMIN')"), you would load the user's actual roles from the DB here.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, 
                            null, 
                            new ArrayList<>() 
                    );
                    
                    // Add request details (like IP address, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 8. Officially log the user into the Spring Security Context!
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If the token is expired or tampered with, JwtUtil will throw an exception.
            // We catch it silently here. The SecurityContext remains null, meaning Spring Security 
            // will block the request and return a 401 Unauthorized automatically.
            // 🌟 This is PERFECT, because your React Interceptor is waiting for that exact 401 error to trigger a refresh!
            System.out.println("JWT Token validation failed or expired: " + e.getMessage());
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }
}