package com.authora.authorization.server.user.mapper;

import com.authora.authorization.server.user.model.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(UUID id);

    @Select("SELECT * FROM users WHERE tenant_id = #{tenantId} AND email = #{email}")
    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    @Select("SELECT * FROM users WHERE tenant_id = #{tenantId}")
    List<User> findAllByTenantId(UUID tenantId);

    @Insert("""
            INSERT INTO users (id, tenant_id, email, password, is_tenant_admin, is_verified, created_at, updated_at)
            VALUES (#{id}, #{tenantId}, #{email}, #{password}, #{isTenantAdmin}, #{isVerified}, #{createdAt}, #{updatedAt})
            """)
    void insert(User user);

    @Update("""
            UPDATE users SET
                email = #{email},
                password = #{password},
                is_tenant_admin = #{isTenantAdmin},
                is_verified = #{isVerified},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    void update(User user);

    @Update("""
            UPDATE users SET
                is_verified = #{isVerified},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    void updateVerified(UUID id, boolean isVerified, java.time.LocalDateTime updatedAt);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(UUID id);

    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE tenant_id = #{tenantId} AND email = #{email})")
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
    boolean existsByEmail(String email);


}