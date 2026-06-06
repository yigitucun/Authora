package com.authora.authorization.server.user.controller;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.user.dto.UpdateUserVerificationRequest;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final com.authora.authorization.server.user.mapper.UserMapper userMapperAuth;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");

        User currentUser = userMapperAuth.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<User> users = userMapperAuth.findAllByTenantId(currentUser.getTenantId());

        return ResponseEntity.ok(users.stream().map(user -> Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "isVerified", user.isVerified(),
                "createdAt", user.getCreatedAt().toString()
        )).toList());
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<?> updateVerification(
            @PathVariable UUID id,
            @RequestBody UpdateUserVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        String email = jwt.getClaimAsString("email");
        User currentUser = userMapperAuth.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User targetUser = userMapperAuth.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!targetUser.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        userMapperAuth.updateVerified(targetUser.getId(), request.verified(), LocalDateTime.now());
        auditLogService.log(currentUser.getTenantId(), currentUser.getId(), "USER_VERIFICATION_UPDATED", "USER", targetUser.getId().toString(),
                Map.of("verified", request.verified()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("id", targetUser.getId().toString(), "isVerified", request.verified()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        String email = jwt.getClaimAsString("email");
        User currentUser = userMapperAuth.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User targetUser = userMapperAuth.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!targetUser.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        userMapperAuth.deleteById(targetUser.getId());
        auditLogService.log(currentUser.getTenantId(), currentUser.getId(), "USER_DELETED", "USER", targetUser.getId().toString(),
                Map.of("email", targetUser.getEmail()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.noContent().build();
    }
}