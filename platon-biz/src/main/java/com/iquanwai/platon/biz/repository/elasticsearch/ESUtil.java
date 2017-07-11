package com.iquanwai.platon.biz.repository.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2017/6/28.
 */
public abstract class ESUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ESClientFactory esClientFactory;

    protected abstract TransportClient getClient();

    public abstract String getIndex();
    public abstract String getType();

    /**
     * 1。超时
     * 2。连接数  这个自带线程池
     * 3。连接，线程安全 安全
     */
    public ESUtil(){
    }


    /**
     * 根据id查询数据
     * @param id id
     * @param clazz 数据
     * @return 搜索到的数据
     */
    public <T> T load(String id,Class<T> clazz){
        GetResponse response = getClient().prepareGet(getIndex(),getType(),id).setOperationThreaded(false).get();
        if (response != null && response.getSource() != null) {
            return JSON.parseObject(response.getSourceAsBytes(), clazz);
        } else {
            return null;
        }
    }

    /**
     * 插入数据，由子类调用
     * @param id 文档id
     * @param bean 数据
     * @return 响应
     */
    public IndexResponse insert(Integer id,XContentBuilder bean){
        Assert.notNull(bean, "插入数据不能为null");

        boolean exist = getClient().prepareGet(getIndex(), getType(), id.toString()).get().isExists();
        if (exist) {
            logger.error("该ES文档id已存在:{}", id);
            throw new RuntimeException("该ES文档ID:" + id + "已存在");
        }
        return getClient().prepareIndex(getIndex(), getType(), id.toString())
                .setSource(bean)
                .get();
    }



    /**
     * 基本的搜索方法，具体的查询数据可以在子类里写
     *
     * @param clazz 数据
     * @param builder 查询sql
     * @param page 分页
     * @return 搜索结果
     */
    public <T> SearchResult<T> search(Class<T> clazz, QueryBuilder builder, Page page) {
        // 基本信息校验
        if (page == null) {
            page = new Page();
        }
        boolean indicesExists = indicesExists(getClient(), getIndex());
        if (!indicesExists) {
            logger.error("索引不存在:{}", getIndex());
            return SearchResult.nonResult(page);
        }
        boolean typesExists = typesExists(getClient(), getIndex(), getType());
        if (!typesExists) {
            logger.error("类型不存在:{}", getType());
            return SearchResult.nonResult(page);
        }
        /*
        * 关于SearchType
        * QUERY_THEN_FETCH 速度快，分数可能不准，不计算全部分片的数据
        * DFS_QUERY_THEN_FETCH 速度慢一些，分数会计算所有分片
        */
        SearchResult<T> result = new SearchResult<T>();
        SearchResponse response = getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(builder) // Query
                .setFrom(page.getOffset()).setSize(page.getPageSize())
                .get();
        if (response != null && response.getHits() != null) {
            int failedShards = response.getFailedShards();
            if (failedShards > 0) {
                logger.error("参与查询的分片失败:{}", response.getShardFailures());
            }
            result.setMaxScore(response.getHits().getMaxScore());
            result.setTotalHits(response.getHits().getTotalHits());
            page.setTotal(Long.valueOf(result.getTotalHits()).intValue());
            List<T> list = Lists.newArrayList();
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                rowData.put("_index", hit.getIndex());
//                rowData.put("_type", hit.getType());
                sourceAsMap.put("id", hit.getId());
                T temp = JSON.parseObject(CommonUtils.mapToJson(sourceAsMap), clazz);
                list.add(temp);
            }
            result.setHits(list);
        }

        return result;
    }

    /**
     * 判断索引是否存在
     * @param client esClient
     * @param indexName 索引名
     * @return 是否存在
     */
    public static boolean indicesExists(Client client, String indexName){
        IndicesExistsRequest ier = new IndicesExistsRequest();
        ier.indices(indexName.toLowerCase());

        return client.admin().indices().exists(ier).actionGet().isExists();
    }

    /**
     * 判断类型是否存在
     * @param client esClient
     * @param indexName 索引名
     * @param indexType 类型名
     * @return 是否存在
     */
    public static boolean typesExists(Client client, String indexName, String indexType){
        if (ESUtil.indicesExists(client, indexName)) {
            TypesExistsRequest ter = new TypesExistsRequest(new String[]{indexName.toLowerCase()}, indexType);
            return client.admin().indices().typesExists(ter).actionGet().isExists();
        }
        return false;
    }


    /**
     * 根据索引数据id删除索引,数据在子类中获取
     * @param id 对应数据ID
     */
    public void delete(String id){
        try {
            getClient().prepareDelete(getIndex(), getType(), id).execute().actionGet();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


}
