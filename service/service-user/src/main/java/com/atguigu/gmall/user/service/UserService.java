package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<UserInfo> {
    public UserInfo login(UserInfo userInfo);
}
