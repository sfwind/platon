package com.iquanwai.platon.biz.repository.forum;

import com.iquanwai.platon.biz.repository.elasticsearch.ESUtil;
import lombok.Data;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by nethunder on 2017/6/28.
 */
@Repository
@Data
public class ForumQuestionRepository extends ESUtil {
    private TransportClient client;
    private String index;
    private String type;

    @PostConstruct
    public void init(){
        client = getClient();
        index = "bank";
        type  = "account";
    }

    public void insert() {
        try {
            IndexResponse response = client.prepareIndex(getIndex(), "tweet", "1")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "kimchy")
                            .field("postDate", new Date())
                            .field("message", "trying out Elasticsearch")
                            .endObject()
                    )
                    .get();
        } catch (IOException e) {

        }
    }
}
