package com.atguigu.gmall.controller;


import com.atguigu.gmall.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product")
public class TestController {
    @Autowired
    private TestService testService;

    @RequestMapping("/lock")
    public void testRedis() throws InterruptedException {
        testService.testRedis();
    }

}
