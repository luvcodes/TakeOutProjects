package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @author ryanw
 */
public interface UserService {
    /**
     * 微信登陆
     * */
    User wxLogin(UserLoginDTO userLoginDTO);
}
