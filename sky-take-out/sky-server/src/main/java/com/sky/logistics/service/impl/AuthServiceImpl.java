package com.sky.logistics.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.logistics.common.LogisticsAuthException;
import com.sky.logistics.dto.LoginDTO;
import com.sky.logistics.entity.LogisticsUser;
import com.sky.logistics.mapper.LogisticsUserMapper;
import com.sky.logistics.service.AuthService;
import com.sky.logistics.vo.LoginUserVO;
import com.sky.logistics.vo.LoginVO;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final LogisticsUserMapper userMapper;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(LogisticsUserMapper userMapper, JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new LogisticsAuthException("用户名或密码不能为空");
        }

        LogisticsUser user = userMapper.findByUsername(loginDTO.getUsername());

        if (user == null) {
            throw new LogisticsAuthException("用户名或密码错误");
        }

        String passwordHash = md5(loginDTO.getPassword());

        if (!passwordHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw new LogisticsAuthException("用户名或密码错误");
        }

        return buildLoginVO(user);
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new LogisticsAuthException("刷新令牌不能为空");
        }

        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), refreshToken);
        String userId = String.valueOf(claims.get(JwtClaimsConstant.USER_ID));
        LogisticsUser user = userMapper.findById(userId);
        if (user == null) {
            throw new LogisticsAuthException("用户不存在");
        }

        return buildLoginVO(user);
    }

    @Override
    public LoginUserVO currentUser(String authorization) {
        String token = extractBearerToken(authorization);
        if (!StringUtils.hasText(token)) {
            throw new LogisticsAuthException("未登录");
        }

        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        String userId = String.valueOf(claims.get(JwtClaimsConstant.USER_ID));
        LogisticsUser user = userMapper.findById(userId);

        if (user == null) {
            throw new LogisticsAuthException("用户不存在");
        }

        return buildUserVO(user);
    }

    private LoginVO buildLoginVO(LogisticsUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        claims.put(JwtClaimsConstant.ROLE, user.getRole());

        String accessToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );

        String refreshToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl() * 24,
                claims
        );

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn((int) (jwtProperties.getAdminTtl() / 1000))
                .user(buildUserVO(user))
                .build();
    }

    private LoginUserVO buildUserVO(LogisticsUser user) {
        return LoginUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .phone(user.getPhone())
                .permissions(permissionsOf(user.getRole()))
                .build();
    }

    private List<String> permissionsOf(String role) {
        if ("DISPATCHER".equals(role)) {
            return Arrays.asList("VEHICLE_READ", "COMMAND_SEND", "ALERT_READ");
        }
        if ("WAREHOUSE".equals(role)) {
            return Arrays.asList("VEHICLE_WRITE", "CARGO_WRITE", "BINDING_WRITE");
        }
        if ("SHIPPER".equals(role)) {
            return Arrays.asList("CARGO_READ", "POSITION_READ", "ASSISTANT_CHAT");
        }
        if ("ADMIN".equals(role)) {
            return Arrays.asList("SYSTEM_ADMIN", "VEHICLE_WRITE", "CARGO_WRITE", "ALERT_WRITE");
        }
        if ("DRIVER".equals(role)) {
            return Arrays.asList("TASK_READ", "STATUS_REPORT");
        }
        return Collections.emptyList();
    }

    private String md5(String rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return authorization;
    }
}
