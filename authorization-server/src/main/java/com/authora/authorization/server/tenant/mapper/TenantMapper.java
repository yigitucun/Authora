package com.authora.authorization.server.tenant.mapper;

import com.authora.authorization.server.tenant.model.Tenant;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface TenantMapper {

    @Select("SELECT * FROM tenants WHERE id = #{id}")
    Optional<Tenant> findById(UUID id);

    @Insert("""
            INSERT INTO tenants (id, name, created_at, updated_at)
            VALUES (#{id}, #{name}, #{createdAt}, #{updatedAt})
            """)
    void insert(Tenant tenant);

    @Update("""
            UPDATE tenants SET
                name = #{name},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    void update(Tenant tenant);

    @Update("""
        UPDATE tenants SET
            company_name = #{companyName},
            usage_type = #{usageType},
            company_size = #{companySize},
            onboarding_completed = true,
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void updateOnboarding(Tenant tenant);

    @Update("""
        UPDATE tenants SET
            name = #{name},
            company_name = #{companyName},
            usage_type = #{usageType},
            company_size = #{companySize},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void updateSettings(Tenant tenant);


}