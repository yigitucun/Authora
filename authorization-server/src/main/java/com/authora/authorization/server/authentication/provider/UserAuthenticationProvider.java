package com.authora.authorization.server.authentication.provider;

import com.authora.authorization.server.authentication.token.UserAuthenticationToken;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {

    private final UserMapper userMapper;
    private final RegisteredClientMapper registeredClientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        UserAuthenticationToken token = (UserAuthenticationToken) authentication;
        String email = (String) token.getPrincipal();
        String password = (String) token.getCredentials();
        String clientId = token.getClientId();

        User user;
        if (clientId != null && !clientId.isBlank() && !clientId.equals("authora-dashboard")) {
            var clientModel = registeredClientMapper.findByClientId(clientId)
                    .orElseThrow(() -> new BadCredentialsException("Incorrect email or password"));
            user = userMapper.findByTenantIdAndEmail(clientModel.getTenantId(), email)
                    .orElseThrow(() -> new BadCredentialsException("Incorrect email or password"));
        } else {
            user = userMapper.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Incorrect email or password"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Incorrect email or password");
        }

        if (!user.isVerified()) {
            throw new DisabledException("Email not verified");
        }

        return new UserAuthenticationToken(
                user.getEmail(),
                null,
                clientId,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UserAuthenticationToken.class.isAssignableFrom(authentication);
    }
}