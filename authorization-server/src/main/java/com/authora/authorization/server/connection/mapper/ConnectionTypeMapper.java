package com.authora.authorization.server.connection.mapper;

import com.authora.authorization.server.connection.model.ConnectionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ConnectionTypeMapper {

    @Select("SELECT * FROM connection_types WHERE id = #{id}")
    Optional<ConnectionType> findById(UUID id);

    @Select("SELECT * FROM connection_types WHERE name = #{name}")
    Optional<ConnectionType> findByName(String name);

    @Select("SELECT * FROM connection_types WHERE is_active = true")
    List<ConnectionType> findAllActive();
}