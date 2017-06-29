package com.iquanwai.platon.biz.repository.forum;

import com.iquanwai.platon.biz.repository.elasticsearch.ESClientFactory;
import com.iquanwai.platon.biz.repository.elasticsearch.ESUtil;
import lombok.Data;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by nethunder on 2017/6/28.
 */
@Repository
@Data
public class ForumQuestionRepository extends ESUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ESClientFactory esClientFactory;

    private TransportClient client;
    private String index;
    private String type;

    @PostConstruct
    public void init(){
        client = esClientFactory.getClient();
        index = "forumquestion";
        type  = "doc";
    }

    public boolean insert(Integer id,String topic,String description,Integer profileId) {
        try {
            XContentBuilder xContentBuilder = jsonBuilder()
                    .startObject()
                    .field("topic", topic)
                    .field("description", description)
                    .field("profileId", profileId)
                    .field("followCount", 0)
                    .field("openCount", 0)
                    .field("answerCount", 0)
                    .field("weight", 0)
                    .field("lastModifiedTime", new Date())
                    .field("addTime", new Date())
                    .field("updateTime", new Date())
                    .endObject();
            IndexResponse response = insert(id, xContentBuilder);
            if (response != null && response.getResult().getLowercase().equalsIgnoreCase("CREATED")) {
                return true;
            } else {
                logger.error("插入论坛提问失败");
                return false;
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public void update(Integer id,String topic,String description){
        UpdateRequest updateRequest = null;
        try {
            updateRequest = new UpdateRequest(getIndex(), getType(), id.toString())
                    .doc(jsonBuilder()
                            .startObject()
                            .field("topic", topic)
                            .field("description", description)
                            .endObject());
            UpdateResponse updateResponse = client.update(updateRequest).get();
            logger.info("更新结果:{}", updateResponse);
        } catch (IOException | ExecutionException | InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
