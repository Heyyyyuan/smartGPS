package com.sky.logistics.entity;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CargoStatusLog {
    private String id;
    private String cargoId;
    private String status;
    private Double lat;
    private Double lng;
    private String remark;
    private String operatorId;
    private OffsetDateTime createdAt;
}
