package com.example.springboot.dao;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Entity
@Data
@Table(name = "app_user")
public class User {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY) 
     private long userId;

    @Column(unique = true, nullable = false)
    private String ssoUid; // The unique ID from Auth0/Okta

    private String email;
    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
}