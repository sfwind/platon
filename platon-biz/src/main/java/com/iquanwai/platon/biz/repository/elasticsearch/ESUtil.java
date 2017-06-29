package com.iquanwai.platon.biz.repository.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.util.page.Page;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by nethunder on 2017/6/28.
 */
public abstract class ESUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ESClientFactory esClientFactory;

    protected TransportClient getClient(){
        return esClientFactory.getClient();
    }

    private TransportClient client;
    public abstract String getIndex();
    public abstract String getType();

    @PostConstruct
    public void init(){
        client = getClient();
    }

    /**
     * 1。超时
     * 2。连接数  这个自带线程池
     * 3。连接，线程安全 安全
     */
    public ESUtil(){
//        try {
//            client = new PreBuiltTransportClient(Settings.EMPTY)
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
//        } catch (UnknownHostException e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
    }


    /**
     * 根据id查询数据
     * @param id id
     * @param clazz 数据
     * @return 搜索到的数据
     */
    public <T> T load(String id,Class<T> clazz){
        GetRequest request = new GetRequest(getIndex(), getType(), id);
        ActionFuture<GetResponse> getResponseActionFuture = client.get(request);
        try {
            GetResponse response = getResponseActionFuture.get();
            if (response != null && response.getSource() != null) {
                return JSON.parseObject(response.getSourceAsBytes(), clazz);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 基本的搜索方法，具体的查询数据可以在子类里写
     *
     * @param clazz 数据
     * @param builder 查询sql
     * @param page 分页
     * @return 搜索结果
     */
    public <T> SearchResult search(Class<T> clazz, QueryBuilder builder, Page page) {
        /*
        * 关于SearchType
        * QUERY_THEN_FETCH 速度快，分数可能不准，不计算全部分片的数据
        * DFS_QUERY_THEN_FETCH 速度慢一些，分数会计算所有分片
        */
        SearchResult result = new SearchResult();
        SearchResponse response = client.prepareSearch(getIndex())
                .setTypes(getType())
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(builder) // Query
                .setFrom(page.getOffset()).setSize(page.getPageSize())
                .get();
        if (response != null && response.getHits() != null) {
            result.setMaxScore(response.getHits().getMaxScore());
            result.setTotalHits(response.getHits().getTotalHits());
            List<DocValue> hits = Lists.newArrayList();
            for (SearchHit hit : response.getHits()) {
                T temp = JSON.parseObject(hit.getSourceAsString(), clazz);
                DocValue<T> dataHit = new DocValue<T>();
                dataHit.setData(temp);
                dataHit.setId(Integer.valueOf(hit.getId()));
                hits.add(dataHit);
            }
            result.setHits(hits);
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
            client.prepareDelete(getIndex(), getType(), id).execute().actionGet();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 根据索引名称删除索引
     * @param indexName 索引名称
     */
    public static void deleteIndex(String indexName){
        IndicesExistsRequest ier = new IndicesExistsRequest();
        ier.indices(indexName.toLowerCase());

        boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();
        if (exists) {
            getClient().admin().indices().prepareDelete(indexName.toLowerCase()).execute().actionGet();
        }
    }
}
