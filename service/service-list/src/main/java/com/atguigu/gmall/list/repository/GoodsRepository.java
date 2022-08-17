package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


//继承需要两个泛型，实体类和形参类型
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {

}
