package com.atguigu.gmall.mapper;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface BaseSaleAttrMapper extends BaseMapper<BaseSaleAttr> {
}
