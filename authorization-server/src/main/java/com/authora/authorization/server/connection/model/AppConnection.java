package com.authora.authorization.server.connection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConnection {
    private UUID id;
    private String clientId;
    private UUID connectionTypeId;
    private boolean isEnabled;
    private String settings;
    private String formConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
