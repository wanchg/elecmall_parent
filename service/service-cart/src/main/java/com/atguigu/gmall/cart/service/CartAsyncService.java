package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

public interface CartAsyncService {
    //新增
    void saveCartInfo(CartInfo cartInfo);
    //更新
    void updateAsyncCartInfo(CartInfo cartInfo);
    //根据临时id删除
    void deleteAsyncCartList(String userTempId);
    //根据实体类删除
    void deleteAsyncCartList(CartInfo cartInfo);
    //更新选中条件
    void checkCart(String userId, Integer isChecked, Long skuId);
    //根据用户id和skuid删除
    void deleteAsyncCartList(String userId, Long skuId);
}
