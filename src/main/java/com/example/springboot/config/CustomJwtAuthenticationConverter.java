package com.example.springboot.config; // Put this in your config package

import com.example.springboot.service.RoleService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final RoleService roleService;

    public CustomJwtAuthenticationConverter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Extract email from Google Token
        String email = jwt.getClaimAsString("email");

        // 2. Fetch the LIST of roles from your updated service
        // (Make sure this method name exactly matches what you named it in RoleService)
        List<String> assignedRoles = roleService.fetchRolesForEmail(email);

        // 3. Loop through the list and add "ROLE_" to every single one
        List<GrantedAuthority> authorities = assignedRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 4. Return the new authentication token with ALL injected roles!
        return new JwtAuthenticationToken(jwt, authorities);
    }
}