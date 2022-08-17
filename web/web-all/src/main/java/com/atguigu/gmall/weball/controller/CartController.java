package com.atguigu.gmall.weball.controller;


import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.weball.client.CartFeignClient;
import com.atguigu.gmall.weball.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping("cart.html")
    public String cartIndex(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //添加购物车
        cartFeignClient.addToCart(Long.parseLong(skuId),Integer.parseInt(skuNum));
        //封装前端需要的数据
        /*
        * ${skuInfo.skuDefaultImg}
        * ${skuInfo.skuName}
        * ${skuInfo.id}
        * ${skuNum}
        * */
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "cart/addCart";
    }

    /**
     * 查看购物车
     * @param request
     * @return
     */
    @GetMapping("cart.html")
    public String index(HttpServletRequest request){
        return "cart/index";
    }
}
