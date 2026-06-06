package com.authora.authorization.server.connection.mapper;

import com.authora.authorization.server.connection.dto.AuthConnectionOption;
import com.authora.authorization.server.connection.model.AppConnection;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AppConnectionMapper {
    @Select("SELECT * FROM app_connections WHERE client_id = #{clientId}")
    List<AppConnection> findByClientId(String clientId);

    @Select("SELECT * FROM app_connections WHERE client_id = #{clientId} AND is_enabled = true")
    List<AppConnection> findEnabledByClientId(String clientId);

    @Select("""
            SELECT * FROM app_connections 
            WHERE client_id = #{clientId} 
            AND connection_type_id = #{connectionTypeId}
            """)
    Optional<AppConnection> findByClientIdAndConnectionTypeId(String clientId, UUID connectionTypeId);

    @Insert("""
            INSERT INTO app_connections (id, client_id, connection_type_id, is_enabled, settings, form_config, created_at, updated_at)
            VALUES (#{id}, #{clientId}, #{connectionTypeId}, #{isEnabled}, #{settings}::jsonb, #{formConfig}::jsonb, #{createdAt}, #{updatedAt})
            """)
    void insert(AppConnection appConnection);

    @Select("""
        SELECT ac.*
        FROM app_connections ac
        JOIN connection_types ct ON ct.id = ac.connection_type_id
        WHERE ac.client_id = #{clientId}
          AND ac.is_enabled = true
          AND ct.name = #{providerName}
          AND ct.is_active = true
        LIMIT 1
        """)
    Optional<AppConnection> findEnabledByClientIdAndProviderName(
            String clientId,
            String providerName
    );

    @Update("""
            UPDATE app_connections SET
                is_enabled = #{isEnabled},
                settings = #{settings}::jsonb,
                form_config = #{formConfig}::jsonb,
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    void update(AppConnection appConnection);

    @Delete("DELETE FROM app_connections WHERE id = #{id}")
    void delete(UUID id);

    @Select("""
            SELECT ct.id as id,
                   ct.name as name,
                   ct.description as description,
                   ct.is_social as isSocial
            FROM app_connections ac
            JOIN connection_types ct ON ct.id = ac.connection_type_id
            WHERE ac.client_id = #{clientId}
              AND ac.is_enabled = true
              AND ct.is_active = true
            ORDER BY ct.name
            """)
    List<AuthConnectionOption> findEnabledOptionsByClientId(String clientId);
}
