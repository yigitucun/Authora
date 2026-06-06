package com.authora.authorization.server.config.security;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.authentication.filter.UserAuthenticationFailureHandler;
import com.authora.authorization.server.authentication.filter.UserAuthenticationFilter;
import com.authora.authorization.server.authentication.filter.UserAuthenticationSuccessHandler;
import com.authora.authorization.server.authentication.filter.SocialAuthenticationSuccessHandler;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.authentication.provider.UserAuthenticationProvider;
import com.nimbusds.jose.jwk.source.JWKSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class DefaultSecurityConfig {

    private final UserAuthenticationProvider authenticationProvider;
    private final RegisteredClientMapper registeredClientMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        UserAuthenticationFilter authenticationFilter = new UserAuthenticationFilter(authenticationManager());
        authenticationFilter.setAuthenticationSuccessHandler(
                new UserAuthenticationSuccessHandler(registeredClientMapper, userMapper, auditLogService)
        );
        authenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2LoginSuccessHandler)
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .loginPage("/sign-in")
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(authorizationRequestResolver())
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )

                        .failureUrl("/sign-in?error=oauth2")
                )
                .authorizeHttpRequests(staticfiles-> staticfiles
                        .requestMatchers("/css/**","/fonts/**","/images/**","/.well-known/**","/favicon.ico").permitAll()
                )
                .authorizeHttpRequests(request->request
                        .requestMatchers("/sign-in","/sign-up","/verify-email","/error","/oauth2/**","/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("http://localhost:3000/dashboard")
                        .permitAll()
                )
                .securityContext(context->context.requireExplicitSave(false))
                .csrf(csrf->csrf.ignoringRequestMatchers("/sign-in", "/verify-email"));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public ClientIdOAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        return new ClientIdOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

}