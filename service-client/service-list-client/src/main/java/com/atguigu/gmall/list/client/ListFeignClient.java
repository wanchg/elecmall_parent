package com.atguigu.gmall.list.client;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    /**
     * 更新商品incrHotScore
     *
     * @param skuId
     * @return
     */
    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    public R incrHotScore(@PathVariable("skuId") Long skuId);



        /**
         * 搜索商品
         * @param listParam
         * @return
         */
        @PostMapping("/api/list")
        R list(@RequestBody SearchParam listParam);

        /**
         * 上架商品
         * @param skuId
         * @return
         */
        @GetMapping("/api/list/inner/upperGoods/{skuId}")
        R upperGoods(@PathVariable("skuId") Long skuId);

        /**
         * 下架商品
         * @param skuId
         * @return
         */
        @GetMapping("/api/list/inner/lowerGoods/{skuId}")
        R lowerGoods(@PathVariable("skuId") Long skuId);


}
