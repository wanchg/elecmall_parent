package com.atguigu.gmall.payment.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;


    @GetMapping("submit/{orderId}")
    @ResponseBody
    public String aliPay(@PathVariable Long orderId) throws AlipayApiException {
        String form = alipayService.createAliPay(orderId);
        return form;
    }

    //http://api.gmall.com/api/payment/alipay/callback/return
    @GetMapping("/callback/return")
    public String callbackReturn(){
        //跳转到支付成功页面。
        //return_order_url=http://payment.gmall.com/pay/success.html
        return "redirect:"+ AlipayConfig.return_order_url;
    }

    // 异步调用 这是个内网穿透的ip地址http://xsiv7k.natappfree.cc/api/payment/alipay/callback/notify
    @PostMapping("/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramMap) throws AlipayApiException {  //获取页面传的参数
        //Map<String, String> paramsMap = ...  //将异步通知中收到的所有参数都存放到 map 中

        //获取out_trade_no
        String outTradeNo = paramMap.get("out_trade_no");
        //1.通过out_trade_no查询数据，如果查到了说明与商家创建的订单号一致
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());

        //2.获取total_amount
        String totalAmount = paramMap.get("total_amount");

        //3.获取seller_id
        String sellerId = paramMap.get("seller_id");

        //4.获取app_id
        String appId = paramMap.get("app_id");

        //第六步 获取交易通知状态
        String tradeStatus = paramMap.get("trade_status");
        boolean  sign = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);  //调用SDK验证签名
        if (sign){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //二次验签就是执行第五步

            //如果paymentInfo 为空，说明不一致
            if (paymentInfo == null){
                return "failure";
            }

            //判断支付信息中的总金额是否与获取到的一致
            if (paymentInfo.getTotalAmount().compareTo(new BigDecimal(totalAmount)) != 0){
                return "failure";
            }

            //判断卖家id是否与获取到的一致
            //if (AlipayConfig.alipay_public_key)

            //判断appId是否与获取到的一致
            if (!AlipayConfig.APP_ID.equals(appId) ){
                return "failure";
            }

            //第六步 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家真正付款成功。
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)){

                //细节：如果交易状态是TRADE_SUCCESS或TRADE_FINISHED 但订单状态是PAID或CLOSED，返回failure
                if ("PAID".equals(paymentInfo.getPaymentStatus()) || "CLOSED".equals(paymentInfo.getPaymentStatus())){
                    return "failure";
                }

                //更新支付信息
                paymentService.paySuccess(outTradeNo,PaymentType.ALIPAY.name(),paramMap);
                return "success";
            }


        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

    //申请退钱
    @GetMapping("refund/{orderId}")
    @ResponseBody
    public R refund(@PathVariable Long orderId){
        boolean flag = paymentService.refund(orderId);
        return R.ok(flag);
    }
}
