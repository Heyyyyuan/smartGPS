package com.sky.logistics.controller;

import com.sky.logistics.common.ApiResponse;
import com.sky.logistics.common.PageResponse;
import com.sky.logistics.service.LogisticsStarterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@Api(tags = "智慧物流-告警中心")
public class AlertController {

    private final LogisticsStarterService starterService;

    public AlertController(LogisticsStarterService starterService) {
        this.starterService = starterService;
    }

    @GetMapping
    @ApiOperation("获取告警列表")
    public ApiResponse<PageResponse<Map<String, Object>>> list(@RequestParam(required = false) String severity,
                                                               @RequestParam(required = false) String type,
                                                               @RequestParam(required = false) String status,
                                                               @RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer size) {
        return ApiResponse.success(starterService.alerts(severity, type, status, page, size));
    }

    @GetMapping("/stats")
    @ApiOperation("获取告警统计")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(starterService.alertStats());
    }

    @GetMapping("/{alertId}")
    @ApiOperation("获取告警详情")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String alertId) {
        return ApiResponse.success(starterService.alertDetail(alertId));
    }

    @PostMapping("/{alertId}/acknowledge")
    @ApiOperation("确认告警")
    public ApiResponse<Map<String, Object>> acknowledge(@PathVariable String alertId, @RequestBody Map<String, Object> request) {
        return ApiResponse.success(starterService.acknowledgeAlert(alertId, request));
    }

    @PostMapping("/{alertId}/resolve")
    @ApiOperation("关闭告警")
    public ApiResponse<Map<String, Object>> resolve(@PathVariable String alertId, @RequestBody Map<String, Object> request) {
        return ApiResponse.success(starterService.resolveAlert(alertId, request));
    }
}
