package com.example.springboot.config; // <-- Keep your actual package name!

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// 🌟 Import the custom filter we built earlier!
import com.example.springboot.config.JwtAuthenticationFilter; // <-- Adjust to your actual package name

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 🌟 Replace CustomJwtAuthenticationConverter with our new Filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            // 🌟 1. MAKE IT STATELESS: Tell Spring we are using JWTs, not server sessions!
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                
                // 🌟 2. THE WHITELIST: Allow users to log in and refresh tokens without being blocked!
                .antMatchers("/api/users/sync", "/api/auth/refresh").permitAll()
                
                // Everything else requires our internal Access Token
                .anyRequest().authenticated()
            )
            
            // 🌟 3. ADD OUR FILTER: Execute our custom JWT checker before Spring's default security
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
            // (Notice .oauth2ResourceServer is completely gone!)
            
        return http.build();
    }

    // THE BULLETPROOF CORS FILTER (Remains perfectly intact!)
    @Bean
    public FilterRegistrationBean<CorsFilter> customCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "https://tickmateui9988.z22.web.core.windows.net",
            "http://52.91.4.139.sslip.io"
        ));
        
        config.setAllowedHeaders(Arrays.asList("*")); 
        config.setAllowedMethods(Arrays.asList("*"));
        
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); 
        return bean;
    }
}