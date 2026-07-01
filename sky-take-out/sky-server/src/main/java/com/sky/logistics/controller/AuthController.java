package com.sky.logistics.controller;

import com.sky.logistics.common.ApiResponse;
import com.sky.logistics.dto.LoginDTO;
import com.sky.logistics.dto.RefreshTokenRequest;
import com.sky.logistics.dto.UserCreateDTO;
import com.sky.logistics.service.AuthService;
import com.sky.logistics.service.UserService;
import com.sky.logistics.vo.LoginUserVO;
import com.sky.logistics.vo.LoginVO;
import com.sky.logistics.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Api(tags = "智慧物流-认证与用户")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    @ApiOperation("登录")
    public ApiResponse<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return ApiResponse.success(authService.login(loginDTO));
    }

    @PostMapping("/auth/refresh")
    @ApiOperation("刷新 Token")
    public ApiResponse<LoginVO> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request == null ? null : request.getRefreshToken()));
    }

    @PostMapping("/auth/logout")
    @ApiOperation("登出")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }

    @GetMapping("/users/me")
    @ApiOperation("获取当前用户")
    public ApiResponse<LoginUserVO> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(authService.currentUser(authorization));
    }

    @PostMapping("/users")
    @ApiOperation("新增用户")
    public ApiResponse<UserVO> createUser(@RequestBody UserCreateDTO request) {
        return ApiResponse.success(userService.create(request));
    }

    @DeleteMapping("/users/{id}")
    @ApiOperation("删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}
