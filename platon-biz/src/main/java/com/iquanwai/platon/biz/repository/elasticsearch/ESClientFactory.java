package com.iquanwai.platon.biz.repository.elasticsearch;

import com.iquanwai.platon.biz.util.ConfigUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by nethunder on 2017/6/28.
 */
@Repository
public class ESClientFactory {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public TransportClient client;

    @PostConstruct
    public void init() {
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (UnknownHostException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public TransportClient getClient() {
        if (client != null) {
            return client;
        } else {
            throw new RuntimeException("client is null");
        }
    }

    static Pattern badChars = Pattern.compile("\\s*[\\s~!\\^&\\(\\)\\-\\+:\\|\\\\\"\\\\$]+\\s*");

    /**
     * 关闭对应client
     * @param client
     */
    public static void close(Client client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
            }
            client = null;
        }
    }

    public static void flush(Client client, String indexName, String indexType) {
        try{
            client.admin().indices().flush(new FlushRequest(indexName.toLowerCase(), indexType)).actionGet();
        }catch(Exception e){}
    }

    /**
     * 初始化并连接elasticsearch集群，返回连接后的client
     * @return 返回连接的集群的client
     */
    public Client newClient(){
        String clusterName = ConfigUtils.getValue("es.clusterName");
        Integer port = ConfigUtils.getIntValue("es.port");
        Boolean clientTransportSniff = ConfigUtils.getBooleanValue("es.clientTransportSniff");
        String hostNames[] = ConfigUtils.getValue("es.hostname").split(",");
        return newClient(clusterName, clientTransportSniff, port, hostNames);
    }

    /**
     * 初始化并连接elasticsearch集群，返回连接后的client
     * @param clusterName 中心节点名称
     * @param clientTransportSniff 是否自动发现新加入的节点
     * @param port 节点端口
     * @param hostname 集群节点所在服务器IP，支持多个
     * @return 返回连接的集群的client
     */
    private Client newClient(String clusterName, boolean clientTransportSniff, int port, String... hostname) {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", clientTransportSniff)
                .build();
        TransportClient transportClient = new PreBuiltTransportClient(settings);
        if(hostname!=null){
            for(String host: hostname) {
                try {
                    transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
                } catch (UnknownHostException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return transportClient;
    }







    public static SearchHits search(String indexName, List<String> indexTypes, QueryBuilder query, List<FieldSortBuilder> sortBuilders, int from, int size) throws NoNodeAvailableException, IndexMissingException {
        if(getClient() == null ) {
            return null;
        }
        indexName = indexName.toLowerCase();

        // 去掉不存在的索引
        IndicesExistsRequest ier = new IndicesExistsRequest();
        ier.indices(indexName);
        boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();
        if(exists){
            getClient().admin().indices().open(new OpenIndexRequest(indexName)).actionGet();
        }else{
            Index index = new Index(indexName);
            //throw new IndexMissingException(index);
            return null;
        }

        try {
            getClient().admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
        } catch (IndexMissingException e) {
            e.printStackTrace();
        }

        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(indexName);

        if(indexTypes != null && indexTypes.size() > 0) {
            String[] types = new String[indexTypes.size()];
            for(int i=0; i<indexTypes.size(); i++) {
                types[i] = indexTypes.get(i).toLowerCase();
            }
            searchRequestBuilder.setTypes(types);
        }

        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequestBuilder.setFrom(from);
        searchRequestBuilder.setSize(size);
        searchRequestBuilder.setExplain(false);
        searchRequestBuilder.setQuery(query);
        if(sortBuilders!=null && sortBuilders.size()>0){
            for(FieldSortBuilder sortBuilder: sortBuilders){
                searchRequestBuilder.addSort(sortBuilder);
            }
        }

        return searchRequestBuilder.execute().actionGet().getHits();
    }

    /**
     * 查询数据
     * @param indexName 索引名称
     * @param indexType 索引类型
     * @param id 数据id
     * @return 如果不存在，返回<code>null</code>
     */
    public static Map<String, Object> query(String indexName, String indexType, String id) {
        if(getClient() == null ) {
            return null;
        }
        if( StringUtil.isEmpty(indexName) || StringUtil.isEmpty(indexType) ||  StringUtil.isEmpty(id)) {
            return null;
        }
        indexName = indexName.toLowerCase();
        indexType = indexType.toLowerCase();

        IndicesExistsRequest ier = new IndicesExistsRequest();
        ier.indices(indexName);
        boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();
        if(!exists){
            // 索引不存在
            return null;
        }

        getClient().admin().indices().open(new OpenIndexRequest(indexName)).actionGet();
        getClient().admin().indices().refresh(new RefreshRequest(indexName)).actionGet();

        GetRequest gr = new GetRequest(indexName, indexType, id);

        ActionFuture<GetResponse> future = getClient().get(gr);
        GetResponse response = future.actionGet();
        return swapResult(response);
    }

    /**
     * 初始化索引
     * @param client
     * @param indexName
     * @param indexType
     * @param cols
     * @return 初始化成功,返回true；否则返回false
     * @throws Exception
     */
    public static boolean initIndex(Client client, String indexName, String indexType, List<Column> cols) throws Exception {
        if(StringUtil.isEmpty(indexName) || StringUtil.isEmpty(indexType) || CollectionUtils.isEmpty(cols)) {
            return false;
        }

        indexName = indexName.toLowerCase();
        indexType = indexType.toLowerCase();

        if(indicesExists(client, indexName)) {
            OpenIndexRequestBuilder openIndexBuilder = new OpenIndexRequestBuilder(client.admin().indices());
            openIndexBuilder.setIndices(indexName).execute().actionGet();
        }else{
            client.admin().indices().prepareCreate(indexName).execute().actionGet();
        }

        TypesExistsRequest ter = new TypesExistsRequest(new String[]{indexName.toLowerCase()}, indexType);
        boolean typeExists = client.admin().indices().typesExists(ter).actionGet().isExists();

        if(typeExists) {
            return true;
        }

        XContentBuilder mapping = jsonBuilder()
                .startObject()
                .startObject(indexType)
                .startObject("properties");
//		mapping.startObject("_all").field("type", "string").field("store", "yes").field("term_vector", "no").field("analyzer", "ik").endObject();
        for (Column col : cols) {
            //(varchar、numeric、timestamp)
            String colName = col.getName().toLowerCase();
            String colType = col.getData_type().toLowerCase().trim();
            if("varchar".equals(colType)) {
                mapping.startObject(colName).field("type", "string").field("store", "yes").field("analyzer", "ik").field("include_in_all", true).endObject();
            }else if("numeric".equals(colType)) {
                if(col.getData_scale()>0) {
                    mapping.startObject(colName).field("type", "float").field("index", "not_analyzed").field("include_in_all", false).endObject();
                }else{
                    mapping.startObject(colName).field("type", "long").field("index", "not_analyzed").field("include_in_all", false).endObject();
                }
            }else if("timestamp".equals(colType)) {
                mapping.startObject(colName).field("type", "date").field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd").field("index", "not_analyzed").field("include_in_all", false).endObject();
            }else {
                mapping.startObject(colName).field("type", "string").field("store", "yes").field("analyzer", "ik").field("include_in_all", true).endObject();
            }

        }
        mapping.endObject()
                .endObject()
                .endObject();

        System.out.println(mapping.string());

        PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        PutMappingResponse response = client.admin().indices().putMapping(mappingRequest).actionGet();

        return response.isAcknowledged();
    }

//	public static SearchHits search(String indexName, String indexType, String[] keywords, String[] channelIdArr, int from, int size) throws NoNodeAvailableException, IndexMissingException {
//		if(getClient() == null ) {
//			return null;
//		}
//
//		// 去掉不存在的索引
//		IndicesExistsRequest ier = new IndicesExistsRequest();
//		ier.indices(new String[]{indexName});
//		boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();
//		if(exists){
//			getClient().admin().indices().open(new OpenIndexRequest(indexName)).actionGet();
//		}else{
//			Index index = new Index(indexName);
//			throw new IndexMissingException(index);
//		}
//
//		try {
//			getClient().admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
//		} catch (IndexMissingException e) {
//			e.printStackTrace();
//		}
//
//		SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(indexName);
//
//		searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//		searchRequestBuilder.setFrom(from);
//		searchRequestBuilder.setSize(size);
//		searchRequestBuilder.setExplain(true);
//
//		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//
//		StringBuffer totalKeys = new StringBuffer();
//		for(String keyword: keywords) {
//			totalKeys.append(keyword);
//		}
//
//		if(!totalKeys.toString().equals("*")){
//			for(String keyword: keywords) {
//				if( keyword == null || keyword.trim().length() == 0 ) {
//					continue;
//				}
//				keyword = badChars.matcher(keyword).replaceAll("");
//				if( keyword == null || keyword.trim().length() == 0 ) {
//					continue;
//				}
//
//				if(keyword.indexOf("*")!=-1 || keyword.indexOf("×")!=-1 || keyword.indexOf("?")!=-1 || keyword.indexOf("？")!=-1){
//					keyword = keyword.replaceAll("×", "*").replaceAll("？", "?");
//					BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
//					for(String indexColumnName: Content.indexColumnNames) {
//						subBoolQuery.should(QueryBuilders.wildcardQuery(indexColumnName.toLowerCase(), keyword));
//					}
//					boolQuery.must(subBoolQuery);
//				}else{
//					QueryStringQueryBuilder qb = QueryBuilders.queryString("\""+keyword+"\"");
//					boolQuery.must(qb);
//				}
//			}
//		}else {
//			//boolQuery.should(QueryBuilders.queryString("*"));
//		}
//
//		if(channelIdArr!=null && channelIdArr.length>0){
//			TermsQueryBuilder inQuery = QueryBuilders.inQuery("channelid_", channelIdArr);
//			boolQuery.must(inQuery);
//		}
//
//		searchRequestBuilder.setQuery(boolQuery);
//
//
//		return searchRequestBuilder.execute().actionGet().getHits();
//	}

    public static String preReadString(String read, int maxLength) {
        if(read==null||read.trim().length()==0){
            return "";
        }
        read = read.trim();

        if(read.length()<=maxLength){
            return read;
        }

//		if(keywords!=null && keywords.length>0){
//			for(String keyword: keywords) {
//				if( keyword == null || keyword.trim().length() == 0 ) {
//					continue;
//				}
//				keyword = badChars.matcher(keyword).replaceAll("");
//				int loc = read.indexOf(keyword);
//				if(loc != -1){
//					if(loc <= maxLength) {
//						return read.substring(0, maxLength);
//					}else{
//						int aft = read.length()-loc;
//						if(aft>(maxLength/2)){
//							return read.substring(loc-maxLength/2, loc+maxLength/2+1);
//						}else{
//							return read.substring(loc-maxLength+aft, loc+aft);
//						}
//					}
//				}
//			}
//		}
        return read.substring(0, maxLength);

    }

    public static List<Map<String, Object>> swapResult(SearchHits hits) {
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();

        if(hits == null || hits.getTotalHits() <= 0) {
            return datas;
        }

        for(int i=0; i<hits.hits().length; i++) {
            SearchHit hit = hits.getAt(i);

            Map<String, Object> rowData = hit.sourceAsMap();
            rowData.put("_index", hit.getIndex());
            rowData.put("_type", hit.getType());
            rowData.put("_id", hit.getId());

            datas.add(rowData);
        }

        return datas;
    }

    public static Map<String, Object> swapResult(GetResponse response) {
        if(response == null || !response.isExists()) {
            return null;
        }

        Map<String, Object> rowData = response.getSourceAsMap();
        rowData.put("_index", response.getIndex());
        rowData.put("_type", response.getType());
        rowData.put("_id", response.getId());

        return rowData;
    }
}
