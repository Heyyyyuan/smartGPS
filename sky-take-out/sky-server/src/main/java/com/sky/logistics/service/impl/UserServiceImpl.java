package com.sky.logistics.service.impl;

import com.sky.logistics.dto.UserCreateDTO;
import com.sky.logistics.entity.LogisticsUser;
import com.sky.logistics.mapper.LogisticsUserMapper;
import com.sky.logistics.service.UserService;
import com.sky.logistics.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Set<String> ROLES = new HashSet<>(
            Arrays.asList("SHIPPER", "DISPATCHER", "WAREHOUSE", "ADMIN", "DRIVER")
    );

    private final LogisticsUserMapper userMapper;

    public UserServiceImpl(LogisticsUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserVO create(UserCreateDTO createDTO) {
        if (createDTO == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        String username = trimToNull(createDTO.getUsername());
        String password = trimToNull(createDTO.getPassword());
        String name = trimToNull(createDTO.getName());
        String role = trimToNull(createDTO.getRole());

        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("角色不能为空");
        }

        role = role.toUpperCase();
        if (!ROLES.contains(role)) {
            throw new IllegalArgumentException("角色不正确");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        LogisticsUser user = new LogisticsUser();
        user.setId(newUserId());
        user.setUsername(username);
        user.setPasswordHash(md5(password));
        user.setName(name);
        user.setRole(role);
        user.setPhone(trimToNull(createDTO.getPhone()));

        userMapper.insert(user);
        return toVO(userMapper.findById(user.getId()));
    }

    @Override
    @Transactional
    public void delete(String id) {
        String safeId = trimToNull(id);
        if (!StringUtils.hasText(safeId)) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }

        LogisticsUser user = userMapper.findById(safeId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        userMapper.deleteById(safeId);
    }

    private UserVO toVO(LogisticsUser user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String newUserId() {
        return "USR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String md5(String rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
