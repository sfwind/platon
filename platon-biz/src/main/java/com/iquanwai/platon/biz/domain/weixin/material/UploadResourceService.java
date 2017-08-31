package com.iquanwai.platon.biz.domain.weixin.material;

import java.awt.image.BufferedImage;

/**
 * Created by justin on 17/8/2.
 */
public interface UploadResourceService {

    /**
     * 在缓存目录生成图片
     * @param bufferedImage 图片缓存
     * @return mediaId
     */
    String uploadResource(BufferedImage bufferedImage);

    /**
     * @param bufferedImage 图片
     * @param path 图片保存路径
     * */
    String uploadResource(BufferedImage bufferedImage, String path);

    String UPLOAD_IMAGE_MATERIAL_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token={access_token}&type=image";
}
