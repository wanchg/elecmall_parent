package com.atguigu.gmall.user.service.impl;


import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {
    //登录
    @Override
    public UserInfo login(UserInfo userInfo) {
        //查询是否有该用户
        String loginName = userInfo.getLoginName();
        String passwd = userInfo.getPasswd();
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",loginName);
        //数据库使用的MD5加密密码，需要用密文
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfoQueryWrapper.eq("passwd",newPassword);
        UserInfo userInfo1 = baseMapper.selectOne(userInfoQueryWrapper);
        if (userInfo1 != null){
            return userInfo1;
        }
        return null;
    }
}
