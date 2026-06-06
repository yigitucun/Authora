package com.authora.authorization.server.authentication.web;

import com.authora.authorization.server.authentication.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        EmailVerificationService.VerificationResult result = emailVerificationService.verifyToken(token);

        if (result.verified()) {
            String clientId = result.clientId();

            if (clientId == null || clientId.isBlank() || clientId.equals("authora-dashboard")) {
                return "redirect:http://localhost:3000/login?verified=true";
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/sign-in")
                    .queryParam("verified", "true")
                    .queryParam("client_id", clientId);

            return "redirect:" + builder.toUriString();
        }

        return "redirect:/sign-in?error=verification";
    }
}