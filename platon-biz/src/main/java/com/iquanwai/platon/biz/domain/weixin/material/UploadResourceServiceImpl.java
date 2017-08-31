package com.iquanwai.platon.biz.domain.weixin.material;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by justin on 17/8/2.
 */
@Service
public class UploadResourceServiceImpl implements UploadResourceService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RestfulHelper restfulHelper;


    @Override
    public String uploadResource(BufferedImage bufferedImage) {
        //生成临时图片
        String path = System.getProperty("java.io.tmpdir");
        Assert.notNull(path, "java.io.tmpdir 不能为空");
        return this.uploadResource(bufferedImage, path);
    }

    @Override
    public String uploadResource(BufferedImage bufferedImage, String path) {
        //生成临时图片
        File file = new File(path);
        ImageUtils.writeToFile(bufferedImage, "jpg", file);
        String json = restfulHelper.uploadImage(UPLOAD_IMAGE_MATERIAL_URL, file, "jpg");
        if (json == null) {
            file.delete();
            return null;
        } else {
            Gson gson = new Gson();
            MaterialResponse materialResponse = gson.fromJson(json, MaterialResponse.class);
            // 删除临时文件
            file.delete();
            return materialResponse.getMedia_id();
        }
    }
}
