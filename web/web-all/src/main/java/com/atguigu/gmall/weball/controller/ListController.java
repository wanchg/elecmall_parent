package com.atguigu.gmall.weball.controller;


import com.atguigu.gmall.common.result.R;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.weball.client.ListFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {
    //如果传的是一个参数，能直接写。如果传多个参数就要传json格式，用对象接收
    @Autowired
    private ListFeignClient listFeignClient;

    //在url中拼接选中的参数
    @RequestMapping("list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        R<Map> list = listFeignClient.list(searchParam);
        Map data = list.getData();

        //orderMap  获取用户的排序规则
        Map<String,String> orderMap = getOrderMap(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        //${propsParamList}获取平台属性面包屑
        List<Map> propsParamList = getProps(searchParam.getProps());
        model.addAttribute("propsParamList",propsParamList);
        //获取品牌面包屑  trademarkParam
        String trademarkParam = getTrademarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam",trademarkParam);
        //${urlParam}  获取url参数,并拼接在超链接上
        String urlParam = getUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        model.addAllAttributes(data);
        return "list/index";
    }
    //获取用户的排序规则
    private Map<String, String> getOrderMap(String order) {
        //排序格式  1：asc
        Map<String, String> hashMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            if (split != null && split.length == 2){
                hashMap.put("type",split[0]);
                hashMap.put("sort",split[1]);
                return hashMap;
            }
        }
        hashMap.put("type","1");
        hashMap.put("sort","asc");
        return hashMap;
    }

    //获取平台属性面包屑
    private List<Map> getProps(String[] props) {
        ArrayList<Map> list = new ArrayList<>();
        //属性的格式  attrId：attrValue：attrName
        if (props != null && props.length>0){
            for (String prop:props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3){
                    //封装
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("attrId",split[0]);
                    hashMap.put("attrValue",split[1]);
                    hashMap.put("attrName",split[2]);
                    //放入list
                    list.add(hashMap);
                }

            }
        }

        return list;
    }

    //获取品牌面包屑
    private String getTrademarkParam(String trademark) {
        //参数是品牌:品牌名
        if (trademark != null && trademark.length()>0){
            String[] split = trademark.split(":");
            if (split != null && split.length == 2){
                String sp1 = split[1];
                return "品牌:"+sp1;
            }
        }
        return null;
    }

    private String getUrlParam(SearchParam searchParam) {
        //关键检索  https://re.jd.com/search?keyword=123
        //把参数和参数值拼接起来
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            stringBuilder.append("keyword=").append(searchParam.getKeyword());
        }
        //https://re.jd.com/search?category3Id=61
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            stringBuilder.append("category3Id=").append(searchParam.getCategory3Id());
        }

        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            stringBuilder.append("category2Id=").append(searchParam.getCategory2Id());
        }

        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            stringBuilder.append("category1Id=").append(searchParam.getCategory1Id());
        }
        //通过品牌检索，要和上面的值拼接
        if (!StringUtils.isEmpty(searchParam.getTrademark())){
            if (stringBuilder.length()>0){
                stringBuilder.append("&trademark=").append(searchParam.getTrademark());
            }

        }

        //通过平台属性检索，与上面的值拼接
        if (!StringUtils.isEmpty(searchParam.getProps())){
            String[] props = searchParam.getProps();
            if (stringBuilder.length()>0){
                for (String prop :props) {
                    stringBuilder.append("&props=").append(prop);

                }
            }

        }
        //在跳转路径时，会让路径变成null，在这里重新赋值
        return "list.html?"+stringBuilder.toString();
    }
}
