package com.iquanwai.platon.biz.repository.elasticsearch;

import com.iquanwai.platon.biz.util.ConfigUtils;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by nethunder on 2017/6/28.
 */
@Repository
public class ESClientFactory {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public TransportClient client;

    @PostConstruct
    public void init() {
        client = newClient();
    }

    public TransportClient getClient() {
        if (client != null) {
            return client;
        } else {
            return newClient();
        }
    }


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
    public TransportClient newClient(){
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
    private TransportClient newClient(String clusterName, boolean clientTransportSniff, int port, String... hostname) {
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
}
