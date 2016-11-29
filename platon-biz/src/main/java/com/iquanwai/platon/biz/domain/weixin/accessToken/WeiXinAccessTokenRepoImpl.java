package com.iquanwai.platon.biz.domain.weixin.accessToken;


import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class WeiXinAccessTokenRepoImpl implements WeiXinAccessTokenRepo {
    private OkHttpClient client = new OkHttpClient();

    private Logger logger = LoggerFactory.getLogger(WeiXinAccessTokenRepoImpl.class);

    public String getAccessToken() {
        Map<String, String> map = Maps.newHashMap();
        map.put("appid", ConfigUtils.getAppid());
        map.put("secret", ConfigUtils.getSecret());
        String url = CommonUtils.placeholderReplace(ACCESS_TOKEN_URL, map);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();

            Map<String, Object> gsonMap = CommonUtils.jsonToMap(json);

            if(MapUtils.isNotEmpty(gsonMap)){
                return (String)gsonMap.get("access_token");
            }
        } catch (Exception e) {
            logger.error("execute "+url+" error", e);
        }

        return "";
    }

}
