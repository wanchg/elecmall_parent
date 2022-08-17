package com.atguigu.gmall.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    //  /resources/static/js/auth.js
    /*setToken(token) {
                         键     值          作用域             过期时间，单位天  获取路径
        return $.cookie('token', token, {domain: 'gmall.com', expires: 7, path: '/'})
    }*/
    @PostMapping("/login")
    public R login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        UserInfo userInfo1 = userService.login(userInfo);
        //需要把用户信息放在cookie中
        /*Cookie cookie = new Cookie("123", "456");
        cookie.setMaxAge(7*24*60*60);
        cookie.setDomain("gmall.com");  //在这个域名下能获取cookie
        cookie.setPath("/");  //在根路径下能获取到cookie*/

        if (!StringUtils.isEmpty(userInfo1)){
            String token = UUID.randomUUID().toString();
            //把用户信息放在缓存中,key=前缀+token
            String key = RedisConst.USER_LOGIN_KEY_PREFIX+token;
            //值是用户id和ip地址，
            //ip是为了防止伪造，在做操作时，会校验ip

            //获取ip地址
            String ip = IpUtil.getIpAddress(request);
            //把ip和id转化为json存储
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",userInfo1.getId());
            jsonObject.put("ip",ip);
            redisTemplate.opsForValue().set(key,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);


            //封装数据
            Map<String, Object> map = new HashMap<>();
            map.put("token",token);
            map.put("nickName",userInfo1.getNickName());
            return R.ok(map).code(200).message("成功");
        }
        return R.ok();
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request){
        String token = request.getHeader("token");

        String userKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
        redisTemplate.delete(userKey);
        return R.ok();
    }
}
