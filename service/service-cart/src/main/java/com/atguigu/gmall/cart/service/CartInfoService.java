package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartInfoService {

    /**
     * 添加购物车
     * @param userId
     * @param skuId
     * @param skuNum
     */
    public void addToCart(String userId,Long skuId,Integer skuNum);

    /**
     * 查询购物车
     * @param userId
     * @param userTempId
     * @return
     */
    public List<CartInfo> getCartList(String userId, String userTempId);

    //更新选中条件
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除选中状态
     *
     * @param userId
     * @param skuId
     */
    void deleteCartList(String userId, Long skuId);


    //根据用户Id 查询购物车列表
    List<CartInfo> getCartCheckedList(String userId);

    //通过userId 查询数据库并放入缓存
    public List<CartInfo> loadCartCache(String userId);
}
