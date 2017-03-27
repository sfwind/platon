package com.iquanwai.platon.biz.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by justin on 17/3/27.
 */
public class QiNiuUtils {

    private static Logger logger = LoggerFactory.getLogger(QiNiuUtils.class);

    public static boolean uploadFile(String filename, InputStream is){
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "2eKBppirRnE-OTXveBRLxfpczW94fbWPT2xMcZB0";
        String secretKey = "ikKbA53JwBRMBYPvIWi-K2Zx8MBoGHFaUc5OQMH-";
        String bucket = "quanwai";

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(is, filename, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            logger.info("upload file {} successful, hash is {}", putRet.key, putRet.hash);
            return true;
        } catch (QiniuException ex) {
            Response r = ex.response;
            try {
                logger.error("error response {}", r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }

        return false;
    }
}
