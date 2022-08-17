package com.atguigu.gmall.list.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private SearchService searchService;

    @GetMapping("inner/createIndex")
    public R createIndex(){
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return R.ok();
    }

    //商品上架
    @GetMapping("inner/upperGoods/{skuId}")
    public R upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);
        return R.ok();
    }
    //商品下架
    @GetMapping("inner/lowerGoods/{skuId}")
    public R lowerGoods(@PathVariable Long skuId){
        searchService.lowerGoods(skuId);
        return R.ok();
    }

    /**
     * 更新商品incrHotScore
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/incrHotScore/{skuId}")
    public R incrHotScore(@PathVariable("skuId") Long skuId) {
        // 调用服务层
        searchService.incrHotScore(skuId);
        return R.ok();
    }

    /**
     * 搜索商品
     * @param searchParam
     * @return
     * @throws IOException
     */
    @PostMapping
    public R list(@RequestBody SearchParam searchParam) throws IOException {
        SearchResponseVo response = searchService.search(searchParam);
        return R.ok(response);
    }
}
