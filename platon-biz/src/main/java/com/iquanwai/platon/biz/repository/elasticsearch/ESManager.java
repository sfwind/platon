package com.iquanwai.platon.biz.repository.elasticsearch;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by nethunder on 2017/6/29.
 */
@Repository
@Data
public class ESManager {
    private Logger logger = LoggerFactory.getLogger(ESManager.class);

    @Autowired
    private ESClientFactory esClientFactory;
    private TransportClient client;



    @PostConstruct
    public void init(){
        client = esClientFactory.getClient();
    }


    /**
     * 根据索引名称删除索引
     * @param indexName 索引名称
     */
    public void deleteIndex(String indexName){
        IndicesExistsRequest ier = new IndicesExistsRequest();
        ier.indices(indexName.toLowerCase());

        boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();
        if (exists) {
            getClient().admin().indices().prepareDelete(indexName.toLowerCase()).execute().actionGet();
        }
    }

    /**
     * 初始化索引
     * @param indexName 索引名称
     * @param indexType 类型名称
     * @param cols 字段属性
     * @return 初始化成功,返回true；否则返回false
     */
    public boolean initIndex(String indexName, String indexType, List<Column> cols) {

        if(StringUtils.isEmpty(indexName) || StringUtils.isEmpty(indexType) || CollectionUtils.isEmpty(cols)) {
            return false;
        }

        indexName = indexName.toLowerCase();
        indexType = indexType.toLowerCase();

        if(ESUtil.indicesExists(client, indexName)) {
//            OpenIndexRequestBuilder openIndexBuilder = new OpenIndexRequestBuilder(getClient(),getClient().admin().indices().create);
//            openIndexBuilder.setIndices(indexName).execute().actionGet();
        }else{
            client.admin().indices().prepareCreate(indexName).execute().actionGet();
        }

        TypesExistsRequest ter = new TypesExistsRequest(new String[]{indexName.toLowerCase()}, indexType);
        boolean typeExists = client.admin().indices().typesExists(ter).actionGet().isExists();

        if(typeExists) {
            return true;
        }
        XContentBuilder mapping = null;
        try {
            mapping = jsonBuilder()
                    .startObject()
                    .startObject(indexType)
                    .startObject("properties");
//		mapping.startObject("_all").field("type", "string").field("store", "yes").field("term_vector", "no").field("analyzer", "ik").endObject();
            for (Column col : cols) {
                //(varchar、numeric、timestamp)
                String colName = col.getName().toLowerCase();
                String colType = col.getType().toLowerCase().trim();
                if ("string".equals(colType)) {
                    mapping.startObject(colName).field("type", "string").field("store", "yes").field("analyzer", "ik_max_word").field("include_in_all", true).endObject();
                } else if ("number".equals(colType)) {
                    if (col.getDataScale() > 0) {
                        mapping.startObject(colName).field("type", "float").field("index", "not_analyzed").field("include_in_all", false).endObject();
                    } else {
                        mapping.startObject(colName).field("type", "long").field("index", "not_analyzed").field("include_in_all", false).endObject();
                    }
                } else if ("timestamp".equals(colType)) {
                    mapping.startObject(colName).field("type", "date").field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd").field("index", "not_analyzed").field("include_in_all", false).endObject();
                } else {
                    mapping.startObject(colName).field("type", "string").field("store", "yes").field("analyzer", "ik_max_word").field("include_in_all", true).endObject();
                }
            }
            mapping.endObject()
                    .endObject()
                    .endObject();

            System.out.println(mapping.string());
        } catch (Exception e){
            logger.error(e.getLocalizedMessage(), e);
        }
        if (mapping == null) {
            return false;
        }

        PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        PutMappingResponse response = client.admin().indices().putMapping(mappingRequest).actionGet();

        return response.isAcknowledged();
    }



}
