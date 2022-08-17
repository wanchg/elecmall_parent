package com.atguigu.gmall.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import springfox.documentation.spring.web.json.Json;

import java.util.List;


@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    //从配置文件中获取拦截控制器
    @Value("${authUrls.url}")
    private String authUrls;
    private AntPathMatcher pathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取用户访问的路径
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        //判断该路径是否与被判断路径相符,符合的不让访问
        //内部数据接口路径
        if (pathMatcher.match("/**/inner/**",path)){
            //获取响应对象
            ServerHttpResponse response = exchange.getResponse();
            //不能在执行了
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //根据request获取到userId
        String userId = getUserId(request);

        //根据request获取userTempId
        String userTempId = getUserTempId(request);


        //如果userid==-1 表示是盗用的
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }
        //用户访问这些trade.html,myOrder.html,list.html控制器时，需要登录
        String[] split = authUrls.split(",");
        for (String url:split) {
            //表示path的值在url中，并且redis中没有用户数据，需要登录
            if (url.indexOf(path) != -1 && StringUtils.isEmpty(userId)){
                //跳转到登录页面
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
                request.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());

                return response.setComplete();
            }

        }

        //   /api/**/auth/**   需要登录
        if (pathMatcher.match("/api/**/auth/**",path)){
            if (StringUtils.isEmpty(userId)){
                //获取响应对象
                ServerHttpResponse response = exchange.getResponse();
                //不能在执行了
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }

        //全部通过后，把用户id放在后端微服务
        //可以在请求头中获取  HttpServletRequest request
        if (!StringUtils.isEmpty(userId)){
            //把userid放在请求头中
            request.mutate().header("userId",userId).build();
            ServerWebExchange build = exchange.mutate().request(request).build();
            return chain.filter(build);
        }

        //全部通过后，把用户临时id放在后端微服务
        //可以在请求头中获取  HttpServletRequest request
        if (!StringUtils.isEmpty(userTempId)){
            //把userTempid放在请求头中
            request.mutate().header("userTempId",userTempId).build();
            ServerWebExchange build = exchange.mutate().request(request).build();
            return chain.filter(build);
        }

        //默认返回值
        return chain.filter(exchange);
    }

    //获取用户临时id
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        //从cookie中获取id
        HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
        if (httpCookie != null){
            String value = httpCookie.getValue();
            userTempId = value;
        }else {
            //如果cookie中没有userTempId，从header中获取
            List<String> stringList = request.getHeaders().get(0);
            if (!CollectionUtils.isEmpty(stringList)){
                String value = stringList.get(0);
                userTempId = value;
            }

        }
        return userTempId;
    }

    //把提示信息输出到页面
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        //提示信息在permission中
        R<Object> build = R.build(null, permission);
        //把result转化为json
        String jsonString = JSON.toJSONString(build);
        //输出jsonString
        DataBuffer wrap = response.bufferFactory().wrap(jsonString.getBytes());
        //设置响应头
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        //把jsonString输出
        return response.writeWith(Mono.just(wrap));
    }

    //获取用户id
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        //获取在header中的token
        List<String> stringList = request.getHeaders().get("token");
        if (!StringUtils.isEmpty(stringList)){
            token = stringList.get(0);
        }else {
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (cookie != null){
                token = cookie.getValue();
            }
        }
        //组成key
        String userKey = "user:login:"+token;
        //从redis中获取用户id
        String obj = (String) redisTemplate.opsForValue().get(userKey);
        if (StringUtils.isEmpty(obj)){
            return "";
        }
        //本质是JSONObject
        JSONObject jsonObject = JSON.parseObject(obj, JSONObject.class);

        //再获取userid之前，先判断ip
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        String ip = (String) jsonObject.get("ip");
        if (gatwayIpAddress.equals(ip)){
            String userId = (String) jsonObject.get("userId");
            return userId;
        }else {
            return "-1";
        }


    }
}
