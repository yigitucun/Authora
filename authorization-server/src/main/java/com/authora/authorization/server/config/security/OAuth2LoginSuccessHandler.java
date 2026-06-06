package com.authora.authorization.server.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final OAuth2UserRegistrationService oAuth2UserRegistrationService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("HANDLERIN HANDLE METODU TETİKLENDİ!");
        super.handle(request, response, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        System.out.println("DB KAYIT İŞLEMİ TETİKLENDİ");
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String registrationId = token.getAuthorizedClientRegistrationId();

        String provider = registrationId;
        String clientId = null;
        if (registrationId.contains("__")) {
            String[] parts = registrationId.split("__");
            provider = parts[0];
            clientId = parts[1];
        }

        log.info("Sosyal giriş başarılı! Email: {}, Provider: {}, ClientId: {}", email, provider, clientId);

        try {
            oAuth2UserRegistrationService.processOAuth2PostLogin(email, name, provider, clientId);
        } catch (Exception e) {
            log.error("DB kayıt hatası!", e);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}