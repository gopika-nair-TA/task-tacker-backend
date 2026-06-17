package com.example.springboot.dao;

import lombok.Data;

@Data
public class SyncUserRequest {
    private String email;
    private String name;
}