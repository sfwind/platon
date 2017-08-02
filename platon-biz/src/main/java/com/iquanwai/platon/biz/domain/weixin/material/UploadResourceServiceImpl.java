package com.iquanwai.platon.biz.domain.weixin.material;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by justin on 17/8/2.
 */
@Service
public class UploadResourceServiceImpl implements UploadResourceService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RestfulHelper restfulHelper;

    @Override
    public String uploadResource(BufferedImage bufferedImage, String path) {
        try {
            //生成临时图片
            File file = new File(path);
            ImageIO.write(bufferedImage, "jpg", file);
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
        } catch (IOException e) {
            logger.error("生成图片失败", e);
        }
        return null;
    }
}
