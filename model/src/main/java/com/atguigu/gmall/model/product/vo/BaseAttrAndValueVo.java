package com.atguigu.gmall.model.product.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseAttrAndValueVo {
    private Long attr_id;

    @ApiModelProperty(value = "属性名称")
    private String attrName;

    @ApiModelProperty(value = "分类id")
    private Long categoryId;

    @ApiModelProperty(value = "分类层级")
    private Integer categoryLevel;

    private Long attrValueId;

    @ApiModelProperty(value = "属性值名称")
    private String valueName;
}
