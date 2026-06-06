package com.authora.authorization.server.authentication.web;

import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.connection.dto.AuthConnectionOption;
import com.authora.authorization.server.connection.mapper.AppConnectionMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final RegisteredClientMapper registeredClientMapper;
    private final AppConnectionMapper appConnectionMapper;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @GetMapping("/sign-in")
    public String signInPage(
            @RequestParam(value = "client_id", required = false) String clientIdParam,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "verified", required = false) String verified,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        String clientId = clientIdParam;

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (clientId == null || clientId.isBlank()) {
            if (savedRequest != null) {
                String[] clientIds = savedRequest.getParameterValues("client_id");
                if (clientIds != null && clientIds.length > 0) {
                    clientId = clientIds[0];
                }
            }
        }
        if (clientId == null || clientId.isBlank()) {
            if (error == null && registered == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Direct access is not allowed");
            }
            clientId = "authora-dashboard";
        }

        model.addAttribute("clientId", clientId);

        if (clientId.equals("authora-dashboard")) {
            model.addAttribute("isB2C", false);
            model.addAttribute("appName", "Authora");
            model.addAttribute("connections", List.of());
        } else {
            RegisteredClientModel clientModel = registeredClientMapper.findByClientId(clientId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid client_id"));
            model.addAttribute("isB2C", true);
            model.addAttribute("appName", clientModel.getClientName());
            model.addAttribute("connections", appConnectionMapper.findEnabledOptionsByClientId(clientId));
        }

        if (error != null) {
            switch (error) {
                case "unverified" -> model.addAttribute("error", "Lütfen e-posta adresinizi doğrulayın.");
                case "credentials" -> model.addAttribute("error", "Hatalı e-posta veya şifre.");
                case "verification" -> model.addAttribute("error", "Doğrulama bağlantısı geçersiz veya süresi dolmuş.");
                case "oauth2" -> model.addAttribute("error", "Sosyal giriş başarısız oldu. E-posta bilgisi alınamadı.");
                default -> model.addAttribute("error", "Bir hata oluştu.");
            }
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Kayıt başarılı! Giriş yapabilirsiniz.");
        }

        if (verified != null) {
            model.addAttribute("successMessage", "E-posta doğrulandı! Giriş yapabilirsiniz.");
        }

        return "auth/sign-in/index";
    }
}