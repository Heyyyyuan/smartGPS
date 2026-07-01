package com.sky.logistics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.logistics.entity.Vehicle;
import com.sky.logistics.mapper.LogisticsVehicleMapper;
import com.sky.logistics.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private static final String HEARTBEAT_PREFIX = "logistics:device:heartbeat:";

    private final StringRedisTemplate redisTemplate;
    private final LogisticsVehicleMapper vehicleMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceServiceImpl(StringRedisTemplate redisTemplate,
                              LogisticsVehicleMapper vehicleMapper) {
        this.redisTemplate = redisTemplate;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public Map<String, Object> getDeviceStatus(String status, String keyword,
                                                Integer page, Integer size) {
        List<Vehicle> vehicles = vehicleMapper.findAll();
        List<Map<String, Object>> devices = new ArrayList<>();
        int onlineCount = 0;
        int offlineCount = 0;

        for (Vehicle v : vehicles) {
            String imei = v.getDeviceImei();
            if (imei == null || imei.isEmpty()) continue;

            String key = HEARTBEAT_PREFIX + imei;
            String heartbeatJson = redisTemplate.opsForValue().get(key);
            boolean online = heartbeatJson != null;

            if (keyword != null && !keyword.isEmpty()) {
                if (!imei.contains(keyword)
                        && (v.getPlate() == null || !v.getPlate().contains(keyword))) {
                    continue;
                }
            }

            String deviceStatus = online ? "ONLINE" : "OFFLINE";
            if (status != null && !status.isEmpty() && !status.equals(deviceStatus)) {
                continue;
            }

            if (online) onlineCount++; else offlineCount++;

            Map<String, Object> device = new LinkedHashMap<>();
            device.put("imei", imei);
            device.put("plate", v.getPlate());
            device.put("status", deviceStatus);

            if (online) {
                try {
                    Map data = objectMapper.readValue(heartbeatJson, Map.class);
                    device.put("lastHeartbeat", data.get("lastHeartbeat"));
                } catch (Exception ignored) {}
            }
            devices.add(device);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("devices", devices);
        result.put("onlineCount", onlineCount);
        result.put("offlineCount", offlineCount);
        result.put("total", onlineCount + offlineCount);
        return result;
    }

    @Override
    public Map<String, Object> getDeviceDetail(String imei) {
        String key = HEARTBEAT_PREFIX + imei;
        String heartbeatJson = redisTemplate.opsForValue().get(key);

        Vehicle vehicle = vehicleMapper.findByImei(imei);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("imei", imei);
        detail.put("plate", vehicle != null ? vehicle.getPlate() : null);
        detail.put("status", heartbeatJson != null ? "ONLINE" : "OFFLINE");

        if (heartbeatJson != null) {
            try {
                Map data = objectMapper.readValue(heartbeatJson, Map.class);
                detail.put("lastHeartbeat", data.get("lastHeartbeat"));
            } catch (Exception ignored) {}
        }
        return detail;
    }
}
