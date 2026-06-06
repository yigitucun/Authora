package com.authora.authorization.server.authentication.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class UserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof DisabledException) {
            getRedirectStrategy().sendRedirect(request, response, "/sign-in?error=unverified");
        } else if (exception instanceof BadCredentialsException) {
            getRedirectStrategy().sendRedirect(request, response, "/sign-in?error=credentials");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/sign-in?error=unknown");
        }
    }
}
