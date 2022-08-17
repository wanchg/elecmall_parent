package com.atguigu.gmall;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gmall.mapper")
public class ProApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProApplication.class,args);
    }
}
