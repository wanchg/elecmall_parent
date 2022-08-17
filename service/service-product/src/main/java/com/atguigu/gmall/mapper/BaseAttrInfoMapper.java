package com.atguigu.gmall.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.vo.BaseAttrAndValueVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    List<BaseAttrAndValueVo> getValueByAllId(@Param("oneId") Long oneId, @Param("twoId") Long twoId, @Param("threeId") Long threeId);

    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(Long skuId);
}