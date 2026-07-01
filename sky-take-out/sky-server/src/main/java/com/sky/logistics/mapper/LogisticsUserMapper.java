package com.sky.logistics.mapper;

import com.sky.logistics.entity.LogisticsUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogisticsUserMapper {

    LogisticsUser findByUsername(@Param("username") String username);

    LogisticsUser findById(@Param("id") String id);

    void insert(LogisticsUser user);

    void deleteById(@Param("id") String id);
}
