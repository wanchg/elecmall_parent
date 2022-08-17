package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("ware.url")
    private String wareUrl;
    @Autowired
    private RabbitService rabbitService;

    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {
        orderInfoMapper.insert(orderInfo);
        //获取到订单详情
        orderInfo.sumTotalAmount();  //商品总数
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //第三方交易编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //订单描述,显示商品名称
        for (OrderDetail orderDetail:orderInfo.getOrderDetailList()) {
            orderInfo.setTradeBody(orderDetail.getSkuName());
        }
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());  //默认24小时
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        orderInfo.setProcessStatus(OrderStatus.UNPAID.name());
        for (OrderDetail orderDetail:orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);

        }

        //订单id
        Long orderId = orderInfo.getId();
        //发送消息
        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,orderId,MqConst.DELAY_TIME);
        return orderId;
    }

    @Override
    public String getOrderNo(String userId) {
        //使用uuid生成
        String orderNo = UUID.randomUUID().toString();
        //放入redis
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.opsForValue().set(tradeNoKey,orderNo);
        //返回
        return orderNo;

    }

    @Override
    public boolean checkOrderNo(String orderNo, String userId) {
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String orderNoValue = (String) redisTemplate.opsForValue().get(tradeNoKey);
        return orderNo.equals(orderNoValue);
    }

    @Override
    public void deleteOrderNo(String userId) {
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.delete(tradeNoKey);
    }

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        //拼接url,获取路径
        //http://localhost:9001/hasStock?skuId=10221&num=2
        //http://localhost:9001
        String check = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        //返回值  0：无库存   1：有库存
        return "1".equals(check);
    }

    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfoMapper.updateById(orderInfo);
    }


    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //封装订单详情
        QueryWrapper<OrderDetail> orderDetailQueryWrapper = new QueryWrapper<>();
        orderDetailQueryWrapper.eq("order_id",orderId);

        List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailQueryWrapper);
        if (orderInfo != null){
            orderInfo.setOrderDetailList(orderDetails);
        }

        return orderInfo;
    }
}
