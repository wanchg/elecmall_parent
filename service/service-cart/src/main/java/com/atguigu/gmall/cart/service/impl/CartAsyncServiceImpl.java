package com.atguigu.gmall.cart.service.impl;


import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class CartAsyncServiceImpl implements CartAsyncService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    //新增
    @Override
    @Async
    public void saveCartInfo(CartInfo cartInfo) {

        try {
            cartInfoMapper.insert(cartInfo);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //更新
    @Override
    @Async
    public void updateAsyncCartInfo(CartInfo cartInfo) {
        //现在redis中有id了
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",cartInfo.getUserId());
        cartInfoQueryWrapper.eq("sku_id",cartInfo.getSkuId());
        cartInfoMapper.update(cartInfo,cartInfoQueryWrapper);
        //cartInfoMapper.updateById(cartInfo);
    }

    @Override
    @Async
    public void deleteAsyncCartList(String userTempId) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userTempId);
        cartInfoMapper.delete(cartInfoQueryWrapper);
    }

    @Override
    @Async
    public void deleteAsyncCartList(CartInfo cartInfo) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",cartInfo.getUserId());
        cartInfoMapper.delete(cartInfoQueryWrapper);
    }

    @Override
    @Async
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userId);
        cartInfoQueryWrapper.eq("sku_id",skuId);
        cartInfoMapper.update(cartInfo,cartInfoQueryWrapper);
    }

    @Override
    @Async
    public void deleteAsyncCartList(String userId, Long skuId) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userId);
        cartInfoQueryWrapper.eq("sku_id",skuId);
        cartInfoMapper.delete(cartInfoQueryWrapper);

    }
}
