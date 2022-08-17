package com.atguigu.gmall.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");  // 任意请求头
        corsConfiguration.addAllowedMethod("*");  // GET, POST, PUT 表示任意
        corsConfiguration.addAllowedOrigin("*");  // 允许任意域名
        corsConfiguration.setAllowCredentials(true);  // 允许携带cookie

        //  需要CorsConfigurationSource 这个对象 是一个接口，所以我们需要当前接口的实现类 UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        //  第一个参数表示路径，第二个参数表示设置跨域的方式等信息 CorsConfiguration
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        //  返回当前对象
        return new CorsWebFilter(configurationSource);
    }

}
