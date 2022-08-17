package com.atguigu.gmall.mapper;


import com.atguigu.gmall.model.product.BaseCategoryView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface BaseCategoryViewMapper extends BaseMapper<BaseCategoryView> {

}
