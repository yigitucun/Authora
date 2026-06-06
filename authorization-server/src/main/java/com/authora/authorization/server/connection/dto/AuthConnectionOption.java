package com.authora.authorization.server.connection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthConnectionOption {
    private UUID id;
    private String name;
    private String description;
    private boolean isSocial;
}

