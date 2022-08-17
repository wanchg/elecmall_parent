package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.mapper.TrademarkMapper;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.service.TrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TrademarkServiceImpl extends ServiceImpl<TrademarkMapper,BaseTrademark> implements TrademarkService {

    @Autowired
    private TrademarkMapper trademarkMapper;
    @Override
    public IPage<BaseTrademark> baseTrademark(Page<BaseTrademark> page) {
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.orderByDesc("id");
        IPage<BaseTrademark> trademarkIPage = trademarkMapper.selectPage(page, baseTrademarkQueryWrapper);
        return trademarkIPage;
    }

    @Override
    public void saveTrademark(BaseTrademark baseTrademark) {
        trademarkMapper.insert(baseTrademark);

    }

    @Override
    public void updateTrademark(BaseTrademark baseTrademark) {
        Long id = baseTrademark.getId();
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.eq("id",id);
        baseMapper.update(baseTrademark,baseTrademarkQueryWrapper);
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        List<BaseTrademark> baseTrademarkList = baseMapper.selectList(null);
        return baseTrademarkList;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        BaseTrademark baseTrademark = trademarkMapper.selectById(tmId);
        return baseTrademark;
    }
}
