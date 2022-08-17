package com.atguigu.gmall.weball.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PassportController {

    @GetMapping("login.html")
    public String loginIndex(HttpServletRequest request){
        //获取当前路径，用于登录后返回
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);

        return "login";
    }
}
