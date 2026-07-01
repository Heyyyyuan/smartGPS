package com.sky.logistics.service;

import com.sky.logistics.dto.UserCreateDTO;
import com.sky.logistics.vo.UserVO;

public interface UserService {

    UserVO create(UserCreateDTO createDTO);

    void delete(String id);
}
