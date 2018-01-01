package com.iquanwai.platon.biz.domain.weixin.customer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/7/8.
 */
@Service
public class CustomerMessageServiceImpl implements CustomerMessageService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean sendCustomerMessage(String openid, String message, Integer type) {
        String result = "";
        if (Constants.WEIXIN_MESSAGE_TYPE.TEXT == type) {
            TextCustomerMessage customerMessage = new TextCustomerMessage(openid, message);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(customerMessage);
            result = restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if(Constants.WEIXIN_MESSAGE_TYPE.IMAGE == type){
            ImageCustomerMessage customerMessage = new ImageCustomerMessage(openid, message);
            Gson gson = new Gson();
            String json = gson.toJson(customerMessage);
            result = restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        } else if(Constants.WEIXIN_MESSAGE_TYPE.VOICE == type){
            VoiceCustomerMessage customerMessage = new VoiceCustomerMessage(openid, message);
            Gson gson = new Gson();
            String json = gson.toJson(customerMessage);
            result = restfulHelper.post(SEND_CUSTOMER_MESSAGE_URL, json);
        }

        return StringUtils.isNoneEmpty(result);
    }
}
