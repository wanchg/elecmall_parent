package com.atguigu.gmall.order.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.CartFeignClient;
import com.atguigu.gmall.order.client.ProductFeignClient;
import com.atguigu.gmall.order.client.UserFeignClient;
import com.atguigu.gmall.order.service.OrderService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    //确定订单
    //加上这个的auth需要登录
    @GetMapping("auth/trade")
    public R<Map<String,Object>> trade(HttpServletRequest request){
        //创建一个map
        HashMap<String, Object> map = new HashMap<>();
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));

        //商品详情  获取选中的商品
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        int orderNum = 0;
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        for (CartInfo cartInfo:cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setCreateTime(new Date());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());

            orderDetails.add(orderDetail);
            //商品数量
            orderNum = orderNum+cartInfo.getSkuNum();

        }

        //商品总价
        OrderInfo orderInfo = new OrderInfo();
        //计算总价方法需要把商品详情赋值
        orderInfo.setOrderDetailList(orderDetails);
        orderInfo.sumTotalAmount();
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        /*
        * userAddressList   用户地址
        * detailArrayList   商品详情
        * totalNum  商品总数
        * totalAmount  商品总价
        *
        * */
        //获取交易号,并放入redis
        String tradeNo = orderService.getOrderNo(userId);

        map.put("tradeNo",tradeNo);
        map.put("totalAmount",totalAmount);
        map.put("totalNum",orderNum);
        map.put("detailArrayList",orderDetails);
        map.put("userAddressList",userAddressList);
        return R.ok(map);
    }


    //提交订单
    @PostMapping("auth/submitOrder")
    public R submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){

        //需要把后端没有获取到的userid返回
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderService.saveOrderInfo(orderInfo);

        String tradeNo = request.getParameter("tradeNo");
        boolean result = orderService.checkOrderNo(tradeNo, userId);

        //比较
        if (!result){
            return R.fail().message("不能重复提交订单,需要刷新页面");
        }
        //使用异步编排,把查询价格和查询是否有库存分两个线程
        //需要多线程所以用list
        List<CompletableFuture> completableFutureList = new ArrayList<>();
        //创建一个线程存放错误信息
        ArrayList<String> errorList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail:orderDetailList) {
            //查看是否有库存
            CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean checkStock = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!checkStock) {
                    errorList.add(orderDetail.getSkuName() + "没有库存");
                }
            },threadPoolExecutor);
            completableFutureList.add(stockCompletableFuture);

            //查询价格是否变动
            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                //实时价格
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                if (orderPrice.compareTo(skuPrice) != 0) {
                    //价格变动
                    //修改购物车的价格
                    List<CartInfo> cartInfos = cartFeignClient.loadCartCache(userId);
                    errorList.add(orderDetail.getSkuName() + "价格变动");
                }
            }, threadPoolExecutor);
            completableFutureList.add(priceCompletableFuture);
        }

        //多任务组合
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        //判断errorlist中是否有数据
        if (errorList.size()>0){
            //把错误信息拼接起来
            return R.fail().message(StringUtils.join(errorList,","));
        }

       /* //查看是否有库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail:orderDetailList) {
            boolean checkStock = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!checkStock){
                return R.fail().message(orderDetail.getSkuName()+"没有库存");
            }

            //查询价格是否变动
            //下单时的价格
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            //实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
            if (orderPrice.compareTo(skuPrice)!=0){
                //价格变动
                //修改购物车的价格
                List<CartInfo> cartInfos = cartFeignClient.loadCartCache(userId);
                return R.fail().message(orderDetail.getSkuName()+"价格变动");
            }

        }*/
        //删除交易号
        orderService.deleteOrderNo(userId);
        //支付页面需要获取订单id
        return R.ok(orderId);
    }

    //查询订单信息
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return orderInfo;
    }

    //更新订单状态
    @GetMapping("inner/updateOrderStatus/{orderId}/{processStatus}")
    public void updateOrderStatus(@PathVariable Long orderId,@PathVariable ProcessStatus processStatus){
        orderService.updateOrderStatus(orderId,processStatus);
    }

}
