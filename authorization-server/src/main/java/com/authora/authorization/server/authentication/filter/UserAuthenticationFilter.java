package com.authora.authorization.server.authentication.filter;

import com.authora.authorization.server.authentication.token.UserAuthenticationToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import java.io.IOException;

public class UserAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final String POST_METHOD = "POST";

    public UserAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(new RegexRequestMatcher("/sign-in","POST"));
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public @Nullable Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (!request.getMethod().equals(POST_METHOD)){
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String clientId = request.getParameter("clientId");
        UserAuthenticationToken userAuthenticationToken = new UserAuthenticationToken(email,password,clientId);
        userAuthenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(userAuthenticationToken);
    }
}
