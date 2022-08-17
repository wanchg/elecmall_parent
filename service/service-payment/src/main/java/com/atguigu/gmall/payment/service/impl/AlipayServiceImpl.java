package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private PaymentService paymentService;


    //生成二维码
    @Override
    public String createAliPay(Long orderId) throws AlipayApiException {
        //获取orderinfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        //保存支付记录
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        //如果用户取消订单了，就不在生成二维码
        if ("ClOSED".equals(orderInfo.getProcessStatus()) || "PAID".equals(orderInfo.getProcessStatus())){
          return "取消订单或已经支付后不能在生成二维码";
        }
        //在配置类中创建
        // AlipayClient alipayClient =  new  DefaultAlipayClient( "https://openapi.alipay.com/gateway.do" , APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);  //获得初始化的AlipayClient

        AlipayClient alipayClient = alipayConfig.alipayClient();

        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
        //同步回调地址,支付成功后跳转的页面
        alipayRequest.setReturnUrl( AlipayConfig.return_payment_url );
        //异步回调地址
        alipayRequest.setNotifyUrl(  AlipayConfig.notify_payment_url ); //在公共参数中设置回跳和通知地址
        //json格式的信息
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        //map.put("total_amount",orderInfo.getTotalAmount());
        map.put("total_amount","0.01");  //测试用
        map.put("subject",orderInfo.getTradeBody());
        map.put("timeout_express","5m");  //过期时间，5分钟

        String jsonString = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonString);

        String form= "" ;

        form = alipayClient.pageExecute(alipayRequest).getBody();
        return form;
    }
}
