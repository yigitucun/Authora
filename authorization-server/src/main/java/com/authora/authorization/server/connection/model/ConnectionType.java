package com.authora.authorization.server.connection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionType {
    private UUID id;
    private String name;
    private String description;
    private boolean isSocial;
    private String requiredFields;
    private String settingsSchema;
    private boolean isActive;
    private LocalDateTime createdAt;
}
