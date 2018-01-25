package com.iquanwai.platon.biz.util;

import com.iquanwai.platon.biz.domain.weixin.accesstoken.AccessTokenService;
import com.iquanwai.platon.biz.exception.WeixinException;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 8/3/16.
 */
@Service
public class RestfulHelper {
    @Autowired
    private AccessTokenService accessTokenService;

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Constants.HTTP_TIMEOUT.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.HTTP_TIMEOUT.READ_TIMEOUT, TimeUnit.SECONDS)
            .build();

    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private MediaType XML = MediaType.parse("text/xml; charset=utf-8");

    private Logger logger = LoggerFactory.getLogger(RestfulHelper.class);

    public String post(String requestUrl, String json) {
        if (StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(json)) {
            String accessToken = accessTokenService.getAccessToken();
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, json))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                try {
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                } catch (WeixinException e) {
                    //refresh token and try again
                    accessToken = accessTokenService.refreshAccessToken(false);
                    url = requestUrl.replace("{access_token}", accessToken);
                    request = new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(JSON, json))
                            .build();
                    response = client.newCall(request).execute();
                    body = response.body().string();
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

    public String postXML(String requestUrl, String xml) {
        logger.info("requestUrl: {}\nxml: {}", requestUrl, xml);
        if (StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(xml)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(RequestBody.create(XML, xml))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                // if(CommonUtils.isError(body)){
                //     logger.error("execute {} return error, error message is {}", requestUrl, body);
                // }
                logger.info("body:{}", body);
                return body;
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return "";
    }


    public String get(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            String accessToken = accessTokenService.getAccessToken();
            logger.info("accessToken is {}", accessToken);
            String url = requestUrl.replace("{access_token}", accessToken);
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                try {
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                } catch (WeixinException e) {
                    //refresh token and try again
                    accessToken = accessTokenService.refreshAccessToken(false);
                    url = requestUrl.replace("{access_token}", accessToken);
                    request = new Request.Builder().url(url).build();
                    response = client.newCall(request).execute();
                    body = response.body().string();
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return "";
    }

    public ResponseBody getPlain(String requestUrl) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                return response.body();
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return null;
    }

    public String uploadImage(String requestUrl, File file, String imageType) {
        if (StringUtils.isNotEmpty(requestUrl)) {
            String accessToken = accessTokenService.getAccessToken();
            String url = requestUrl.replace("{access_token}", accessToken);

            RequestBody fileBody = RequestBody.create(MediaType.parse("image/" + imageType), file);

            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("media", file.getPath(), fileBody)
                    .setType(MultipartBody.FORM)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                try {
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                } catch (WeixinException e) {
                    //refresh token and try again
                    accessToken = accessTokenService.refreshAccessToken(false);
                    url = requestUrl.replace("{access_token}", accessToken);
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    response = client.newCall(request).execute();
                    body = response.body().string();
                    if (CommonUtils.isError(body)) {
                        logger.error("execute {} return error, error message is {}", url, body);
                    }
                }
                return body;
            } catch (Exception e) {
                logger.error("execute " + requestUrl + " error", e);
            }
        }
        return null;
    }

}
