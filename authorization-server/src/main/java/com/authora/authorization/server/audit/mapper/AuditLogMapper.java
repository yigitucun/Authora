package com.authora.authorization.server.audit.mapper;

import com.authora.authorization.server.audit.model.AuditLog;
import com.authora.authorization.server.common.typehandler.JsonbTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO audit_logs (id, tenant_id, actor_user_id, action, target_type, target_id, metadata, ip, user_agent, created_at)
            VALUES (#{id}, #{tenantId}, #{actorUserId}, #{action}, #{targetType}, #{targetId}, #{metadata, typeHandler=com.authora.authorization.server.common.typehandler.JsonbTypeHandler}, #{ip}, #{userAgent}, #{createdAt})
            """)
    void insert(AuditLog log);

    @Select("""
            SELECT * FROM audit_logs
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<AuditLog> findRecentByTenantId(UUID tenantId, int limit);
}

