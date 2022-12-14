package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ProductFeignClient;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchServiceImpl implements SearchService {

    //???????????????????????????????????????????????????crud
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void upperGoods(Long skuId) {

        Goods goods = new Goods();
        //???????????????????????????es
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null){
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());

            //????????????????????????
            BaseTrademark trademark = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());

            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());

            //??????????????????
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory3Name(categoryView.getCategory3Name());

            //????????????????????????
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            //????????????
            List<SearchAttr> searchAttrs = attrList.stream().map((baseAttrInfo) -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getAttrValueList().get(0).getAttrId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrs);
        }


        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        //??????id??????
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //??????redis???????????????
        //???????????????????????????key???????????????????????????????????????
        String hotScoreKey = "hotScore";
        //????????????key????????????????????? ?????????????????????
        Double count = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId, 1);

        if (count%10 == 0){
            Optional<Goods> repository = goodsRepository.findById(skuId);
            Goods goods = repository.get();
            goods.setHotScore(count.longValue());

            goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        /*
        * 1.??????????????????????????????????????????dsl?????????????????????
        * 2. ??????dsl???????????????????????????
        * 3.???????????????????????????????????????
        * */

        //1.??????????????????????????????????????????dsl?????????????????????
        SearchRequest searchRequest = builderQueryDsl(searchParam);
        //2. ??????dsl???????????????????????????
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //3.???????????????????????????????????????
        SearchResponseVo searchResponseVo = this.parseSearchResult(search);

        //????????????
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //??????%???????????????==0?????????%/???????????????:??????/???????????????+1
        Long totalPages = (searchResponseVo.getTotal()+searchResponseVo.getPageSize()-1)/searchResponseVo.getPageSize();
        searchResponseVo.setTotalPages(totalPages);  //?????????
        return searchResponseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse search) {
        //ParsedLongTerms tmNameAgg  ????????????????????????bucket?????????


        SearchResponseVo searchResponseVo = new SearchResponseVo();

        Map<String, Aggregation> aggregationMap = search.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //????????????id
            String keyAsString = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.getLong(keyAsString));
            //??????????????????
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();

            searchResponseTmVo.setTmName(tmName);

            //????????????url
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        searchResponseVo.setTrademarkList(trademarkList);

        //????????????????????????
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");

        List<SearchResponseAttrVo> responseAttrVos = attrIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //attrId
            Number keyAsNumber = bucket.getKeyAsNumber();
            searchResponseAttrVo.setAttrId(keyAsNumber.longValue());
            //attrName
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //attrValueList
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> stringList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(stringList);

            return searchResponseAttrVo;

        }).collect(Collectors.toList());

        searchResponseVo.setAttrsList(responseAttrVos);

        SearchHits hits = search.getHits();
         //????????????
        searchResponseVo.setTotal(hits.totalHits);
        SearchHit[] subHits = hits.getHits();

        ArrayList<Goods> goodList = new ArrayList<>();
        for (SearchHit hit:subHits) {
            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //???????????????????????????????????????
            if (hit.getHighlightFields().get("title")!= null){
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                Text title = titles[0];
                goods.setTitle(title.string());
            }
            goodList.add(goods);
        }
        searchResponseVo.setGoodsList(goodList);

        return searchResponseVo;
    }

    private SearchRequest builderQueryDsl(SearchParam searchParam) {

        //?????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //{query---bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //??????????????????????????????id??????
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            //query--bool--filter--term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            //query--bool--filter--term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            //query--bool--filter--term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }
        //?????????????????????keyword??????
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            //name??????????????????  ?????????????????????
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()));
        }
        //??????????????????id??????
        //???????????????trademark=2:??????  ????????????
        if (!StringUtils.isEmpty(searchParam.getTrademark())){
            //??????
            String trademark = searchParam.getTrademark();
            String[] split = trademark.split(":");
            if (split != null && split.length == 2){
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }

        }
        //?????????????????????????????????
        //props=23:4G:????????????
        String[] props = searchParam.getProps();

        if (props != null && props.length>0){
            for (String pro:props) {
                //??????
                String[] split = pro.split(":");
                if (split != null && split.length == 3){
                    //????????????????????????id?????????????????????????????????
                    //????????????bool
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //????????????id
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //bool---must---nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //????????????
                    boolQueryBuilder.filter(boolQuery);
                }

            }
        }
        //{query}
        searchSourceBuilder.query(boolQueryBuilder);

        //??????
        int current = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(searchParam.getPageSize());

        //??????
        //???????????????1:asc  1:desc
        //1:hotScore 2:price
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            String field = "";
            if (split != null && split.length == 2){
                switch (split[0]){
                    case "1":  field = "hotScore"; break;
                    case "2": field = "price"; break;
                }
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }

        }

        //??????
        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field("title");
        highlight.preTags("<span style=color:red>");
        highlight.postTags("</span>");
        searchSourceBuilder.highlighter(highlight);

        //??????
        //????????????
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //??????????????????
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        //?????????  ????????????????????????????????????????????????null
        searchSourceBuilder.fetchSource(new String[]{"id","title","defaultImg","price"},null);

        //????????????
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
