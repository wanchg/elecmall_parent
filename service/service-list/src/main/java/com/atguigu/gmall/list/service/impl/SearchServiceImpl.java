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

    //这个类的接口继承了这个接口后就有了crud
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
        //添加商品详情数据到es
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null){
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());

            //获取商品品牌信息
            BaseTrademark trademark = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());

            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());

            //获取分类信息
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory3Name(categoryView.getCategory3Name());

            //获取平台属性集合
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            //流式编程
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
        //根据id删除
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //使用redis要考虑什么
        //使用什么数据类型，key叫什么名字，缓存的三个问题
        String hotScoreKey = "hotScore";
        //参数一：key，参数二：属性 参数三：属性值
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
        * 1.根据用户的检索条件生成对应的dsl语句，使用方法
        * 2. 执行dsl语句并获取到结果集
        * 3.将查询出来的结果集进行封装
        * */

        //1.根据用户的检索条件生成对应的dsl语句，使用方法
        SearchRequest searchRequest = builderQueryDsl(searchParam);
        //2. 执行dsl语句并获取到结果集
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //3.将查询出来的结果集进行封装
        SearchResponseVo searchResponseVo = this.parseSearchResult(search);

        //福默认值
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //总数%每页的大小==0？总数%/每页的大小:总数/每页的大小+1
        Long totalPages = (searchResponseVo.getTotal()+searchResponseVo.getPageSize()-1)/searchResponseVo.getPageSize();
        searchResponseVo.setTotalPages(totalPages);  //总页数
        return searchResponseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse search) {
        //ParsedLongTerms tmNameAgg  这个类型能获取到bucket中的值


        SearchResponseVo searchResponseVo = new SearchResponseVo();

        Map<String, Aggregation> aggregationMap = search.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌id
            String keyAsString = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.getLong(keyAsString));
            //获取品牌名称
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();

            searchResponseTmVo.setTmName(tmName);

            //获取品牌url
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        searchResponseVo.setTrademarkList(trademarkList);

        //获取平台属性集合
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
         //总命中数
        searchResponseVo.setTotal(hits.totalHits);
        SearchHit[] subHits = hits.getHits();

        ArrayList<Goods> goodList = new ArrayList<>();
        for (SearchHit hit:subHits) {
            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //如果高亮中有值就要高亮中的
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

        //查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //{query---bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断用户是否根据分类id查询
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
        //判断用户是否用keyword检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            //name是检索的名称  后面是检索的值
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()));
        }
        //用户根据品牌id查询
        //前端传的是trademark=2:华为  需要分割
        if (!StringUtils.isEmpty(searchParam.getTrademark())){
            //分割
            String trademark = searchParam.getTrademark();
            String[] split = trademark.split(":");
            if (split != null && split.length == 2){
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }

        }
        //用户根据平台属性值查询
        //props=23:4G:运行内存
        String[] props = searchParam.getProps();

        if (props != null && props.length>0){
            for (String pro:props) {
                //分割
                String[] split = pro.split(":");
                if (split != null && split.length == 3){
                    //第一个元素是属性id，第二个元素是属性名称
                    //创建两个bool
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //设置平台id
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //bool---must---nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //放在外层
                    boolQueryBuilder.filter(boolQuery);
                }

            }
        }
        //{query}
        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        int current = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(current);
        searchSourceBuilder.size(searchParam.getPageSize());

        //排序
        //前端传的值1:asc  1:desc
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

        //高亮
        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field("title");
        highlight.preTags("<span style=color:red>");
        highlight.postTags("</span>");
        searchSourceBuilder.highlighter(highlight);

        //聚合
        //品牌聚合
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //销售属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        //返回值  前面是显示的值，后面是其他值显示null
        searchSourceBuilder.fetchSource(new String[]{"id","title","defaultImg","price"},null);

        //构建完成
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
