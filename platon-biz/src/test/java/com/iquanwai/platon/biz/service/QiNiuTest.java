package com.iquanwai.platon.biz.service;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

import java.io.*;

/**
 * Created by justin on 17/3/27.
 */
public class QiNiuTest {

    public static void main(String[] args) throws FileNotFoundException {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "2eKBppirRnE-OTXveBRLxfpczW94fbWPT2xMcZB0";
        String secretKey = "ikKbA53JwBRMBYPvIWi-K2Zx8MBoGHFaUc5OQMH-";
        String bucket = "quanwai";
//如果是Windows情况下，格式是 D:\\qiniu\\test.png
        String localFilePath = "/Users/justin/Downloads/rise_k48_2.png";
//默认不指定key的情况下，以文件内容的hash值作为文件名
        Auth auth = Auth.create(accessKey, secretKey);
        InputStream bis = new FileInputStream(new File(localFilePath));
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(bis, "rise_k48_2.png", upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
    }
}
