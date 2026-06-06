package com.authora.authorization.server.client.mapper;

import com.authora.authorization.server.client.model.RegisteredClientModel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface RegisteredClientMapper {

    @Select("SELECT * FROM oauth2_registered_client WHERE tenant_id = #{tenantId}")
    List<RegisteredClientModel> findByTenantId(UUID tenantId);

    @Select("SELECT * FROM oauth2_registered_client WHERE id = #{id}")
    Optional<RegisteredClientModel> findById(String id);

    @Select("SELECT * FROM oauth2_registered_client WHERE client_id = #{clientId}")
    Optional<RegisteredClientModel> findByClientId(String clientId);

    @Select("SELECT EXISTS(SELECT 1 FROM oauth2_registered_client WHERE client_id = #{clientId})")
    boolean existsByClientId(String clientId);

    @Insert("""
            INSERT INTO oauth2_registered_client (
                id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
                client_name, client_authentication_methods, authorization_grant_types,
                redirect_uris, post_logout_redirect_uris, scopes,
                client_settings, token_settings, tenant_id
            ) VALUES (
                #{id}, #{clientId}, #{clientIdIssuedAt}, #{clientSecret}, #{clientSecretExpiresAt},
                #{clientName}, #{clientAuthenticationMethods}, #{authorizationGrantTypes},
                #{redirectUris}, #{postLogoutRedirectUris}, #{scopes},
                #{clientSettings}, #{tokenSettings}, #{tenantId}
            )
            """)
    void insert(RegisteredClientModel client);

    @Update("""
            UPDATE oauth2_registered_client SET
                client_secret = #{clientSecret},
                client_name = #{clientName},
                client_authentication_methods = #{clientAuthenticationMethods},
                authorization_grant_types = #{authorizationGrantTypes},
                redirect_uris = #{redirectUris},
                post_logout_redirect_uris = #{postLogoutRedirectUris},
                scopes = #{scopes},
                client_settings = #{clientSettings},
                token_settings = #{tokenSettings}
            WHERE id = #{id}
            """)
    void update(RegisteredClientModel client);

    @Delete("DELETE FROM oauth2_registered_client WHERE id = #{id}")
    void delete(String id);

}