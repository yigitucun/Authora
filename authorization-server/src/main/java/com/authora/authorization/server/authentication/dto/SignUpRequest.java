package com.authora.authorization.server.authentication.dto;

import com.authora.authorization.server.authentication.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@UniqueEmail
public class SignUpRequest {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "An email is required")
    private String email;
    @NotBlank(message = "A password is required")
    @Size(min = 8,message = "The password must be at least 8 characters long")
    private String password;
    private String clientId;
}
