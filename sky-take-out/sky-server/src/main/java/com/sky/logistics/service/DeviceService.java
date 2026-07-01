package com.sky.logistics.service;

import java.util.Map;

public interface DeviceService {

    Map<String, Object> getDeviceStatus(String status, String keyword, Integer page, Integer size);

    Map<String, Object> getDeviceDetail(String imei);
}
