package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

public interface AlipayService {
    String createAliPay(Long orderId) throws AlipayApiException;
}
