package com.atguigu.gmall.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TrademarkService extends IService<BaseTrademark> {
    IPage<BaseTrademark> baseTrademark(Page<BaseTrademark> page);

    void saveTrademark(BaseTrademark baseTrademark);

    void updateTrademark(BaseTrademark baseTrademark);

    List<BaseTrademark> getTrademarkList();

    BaseTrademark getTrademarkByTmId(Long tmId);
}
