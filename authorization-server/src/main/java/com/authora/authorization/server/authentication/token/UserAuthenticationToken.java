package com.authora.authorization.server.authentication.token;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;


public class UserAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final String clientId;
    private final Object principal;
    private final Object credentials;

    public UserAuthenticationToken(Object principal, Object credentials, String clientId) {
        super(Collections.emptyList());
        this.clientId = clientId;
        this.principal = principal;
        this.credentials=credentials;
        setAuthenticated(false);
    }

    public UserAuthenticationToken(Object principal, Object credentials, String clientId, @Nullable Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.clientId = clientId;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return this.credentials;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return this.principal;
    }
}
