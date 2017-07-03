package com.iquanwai.platon.biz.domain.common.message;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/6/28.
 */
@Service
public class ShortMessageServiceImpl implements ShortMessageService {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private RedisUtil redisUtil;

    private static final long TIMEOUT = 60L;

    private static final String CAN_SEND_KEY = "send:sms:period:key";

    private static final int SUCCESS = 200;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Pair<Boolean, String> sendShortMessage(SMSDto smsDto) {
        String shortMessageUrl = ConfigUtils.sendShortMessageUrl();
        Gson gson = new Gson();
        String body = restfulHelper.post(shortMessageUrl, gson.toJson(smsDto));
        ResultDto resultDto = gson.fromJson(body, ResultDto.class);
        if (resultDto != null) {
            if (resultDto.getCode() == SUCCESS) {
                return new ImmutablePair<>(true, null);
            }else{
                logger.error("send error {}", body);
                return new ImmutablePair<>(false, resultDto.getMsg().getDesc());
            }
        }
        logger.error("send error {}", body);
        return new ImmutablePair<>(false, "发送失败");
    }

    @Override
    public boolean canSend(Integer profileId) {
        String value = redisUtil.get(CAN_SEND_KEY + profileId);
        if (value != null) {
            return false;
        }
        redisUtil.set(CAN_SEND_KEY + profileId, "send", TIMEOUT);
        return true;
    }
}
