package com.atguigu.gmall.mapper;


import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import zipkin2.Call;

@Component
@Mapper
public interface TrademarkMapper extends BaseMapper<BaseTrademark> {
}
