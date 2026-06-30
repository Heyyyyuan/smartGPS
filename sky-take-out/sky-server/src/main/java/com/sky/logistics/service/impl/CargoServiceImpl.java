package com.sky.logistics.service.impl;

import com.sky.logistics.common.PageResponse;
import com.sky.logistics.dto.CargoCreateDTO;
import com.sky.logistics.dto.CargoLocationDTO;
import com.sky.logistics.dto.CargoQueryDTO;
import com.sky.logistics.entity.Cargo;
import com.sky.logistics.entity.CargoRecord;
import com.sky.logistics.mapper.LogisticsCargoMapper;
import com.sky.logistics.service.CargoService;
import com.sky.logistics.vo.CargoLocationVO;
import com.sky.logistics.vo.CargoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CargoServiceImpl implements CargoService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_STATUS = "CREATED";

    private final LogisticsCargoMapper cargoMapper;

    public CargoServiceImpl(LogisticsCargoMapper cargoMapper) {
        this.cargoMapper = cargoMapper;
    }

    @Override
    public PageResponse<CargoVO> page(CargoQueryDTO queryDTO) {
        int page = normalizePage(queryDTO == null ? null : queryDTO.getPage());
        int size = normalizeSize(queryDTO == null ? null : queryDTO.getSize());
        int offset = (page - 1) * size;
        String status = trimToNull(queryDTO == null ? null : queryDTO.getStatus());
        String keyword = trimToNull(queryDTO == null ? null : queryDTO.getKeyword());

        Long total = cargoMapper.count(status, keyword);
        if (total == null || total == 0) {
            return new PageResponse<>(Collections.<CargoVO>emptyList(), page, size, 0L, 0);
        }

        List<CargoRecord> cargoList = cargoMapper.findPage(status, keyword, offset, size);
        List<CargoVO> content = cargoList == null
                ? Collections.<CargoVO>emptyList()
                : cargoList.stream().map(this::toVO).collect(Collectors.toList());
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResponse<>(content, page, size, total, totalPages);
    }

    @Override
    public CargoVO detail(String cargoId) {
        String safeCargoId = trimToNull(cargoId);
        if (!StringUtils.hasText(safeCargoId)) {
            throw new IllegalArgumentException("货物 ID 不能为空");
        }

        CargoRecord cargo = cargoMapper.findByCargoId(safeCargoId);
        if (cargo == null) {
            throw new IllegalArgumentException("货物不存在");
        }

        return toVO(cargo);
    }

    @Override
    @Transactional
    public CargoVO create(CargoCreateDTO createDTO) {
        if (createDTO == null) {
            throw new IllegalArgumentException("货物信息不能为空");
        }

        String cargoId = trimToNull(createDTO.getCargoId());
        if (!StringUtils.hasText(cargoId)) {
            throw new IllegalArgumentException("货物 ID 不能为空");
        }
        if (cargoMapper.findByCargoId(cargoId) != null) {
            throw new IllegalArgumentException("货物 ID 已存在");
        }
        validateWeight(createDTO.getWeight());

        Cargo cargo = new Cargo();
        cargo.setCargoId(cargoId);
        cargo.setCargoType(trimToNull(createDTO.getCargoType()));
        cargo.setWeight(createDTO.getWeight());
        cargo.setStatus(DEFAULT_STATUS);
        fillLocation(cargo, createDTO.getOrigin(), true);
        fillLocation(cargo, createDTO.getDestination(), false);

        cargoMapper.insert(cargo);
        return toVO(cargoMapper.findByCargoId(cargoId));
    }

    private CargoVO toVO(CargoRecord cargo) {
        return CargoVO.builder()
                .cargoId(cargo.getCargoId())
                .cargoType(cargo.getCargoType())
                .weight(cargo.getWeight())
                .status(cargo.getStatus())
                .origin(toLocationVO(cargo.getOriginName(), cargo.getOriginLat(), cargo.getOriginLng()))
                .destination(toLocationVO(cargo.getDestinationName(), cargo.getDestinationLat(), cargo.getDestinationLng()))
                .vehicleId(cargo.getVehicleId())
                .vehiclePlate(cargo.getVehiclePlate())
                .driverName(cargo.getDriverName())
                .driverPhone(cargo.getDriverPhone())
                .loadedAt(cargo.getLoadedAt())
                .deliveredAt(cargo.getDeliveredAt())
                .createdAt(cargo.getCreatedAt())
                .updatedAt(cargo.getUpdatedAt())
                .build();
    }

    private CargoLocationVO toLocationVO(String name, Double lat, Double lng) {
        return CargoLocationVO.builder()
                .name(name)
                .lat(lat)
                .lng(lng)
                .build();
    }

    private void fillLocation(Cargo cargo, CargoLocationDTO location, boolean origin) {
        if (location == null) {
            return;
        }

        if (origin) {
            cargo.setOriginName(trimToNull(location.getName()));
            cargo.setOriginLat(location.getLat());
            cargo.setOriginLng(location.getLng());
        } else {
            cargo.setDestinationName(trimToNull(location.getName()));
            cargo.setDestinationLat(location.getLat());
            cargo.setDestinationLng(location.getLng());
        }
    }

    private void validateWeight(BigDecimal weight) {
        if (weight != null && weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("货物重量不能小于 0");
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
