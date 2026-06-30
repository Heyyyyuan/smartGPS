package com.sky.logistics.mapper;

import com.sky.logistics.entity.Cargo;
import com.sky.logistics.entity.CargoRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogisticsCargoMapper {

    List<CargoRecord> findPage(@Param("status") String status,
                               @Param("keyword") String keyword,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    Long count(@Param("status") String status,
               @Param("keyword") String keyword);

    CargoRecord findByCargoId(@Param("cargoId") String cargoId);

    int insert(Cargo cargo);
}
