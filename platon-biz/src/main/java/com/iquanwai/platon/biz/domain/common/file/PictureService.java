package com.iquanwai.platon.biz.domain.common.file;

import com.iquanwai.platon.biz.po.common.Picture;
import com.iquanwai.platon.biz.po.common.PictureModule;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by nethunder on 2016/12/15.
 */
public interface PictureService {
    /**
     * 获取图片模块配置信息
     * @param id 模块Id
     * @return 模块配置信息
     */
    PictureModule getPictureModule(Integer id);

    /**
     * 重新加载图片配置信息
     */
    void reloadModule();

    /**
     * 检查图片是否有效
     * @param pictureModule 图片模块配置信息
     * @param picture {length: 图片大小，单位是字节; type: 图片类型 }
     * @return 返回值 {status:"0",error:"失败原因"},{status:"1",error:"0"}
     */
    Pair<Integer,String> checkAvaliable(PictureModule pictureModule, Picture picture);


    /**
     * 上传图片<br/><br/>
     *
     * TODO:增加一个步骤，如果module需要缩略图，生成一个缩略图存起来。<br/>
     * 该模块缩略图，就在该模块目录下新建一个thumb目录即可.<br/>
     * 存储位置：{Path}+"thumb/"+{Thumbnail}
     * @param pictureModule 图片模块配置信息
     * @param fileName  上传的文件名
     * @param file  文件(图片)
     * @return  图片
     * */
    Pair<Boolean,String> uploadPicture(PictureModule pictureModule,  String fileName, MultipartFile file) throws Exception;

    /**
     * 获得模块的url前缀
     * @param moduleId 模块id
     * @return 前缀
     */
    String getModulePrefix(Integer moduleId);

    /**
     * 加载图片
     * @param moduleId 模块id
     * @param referencedId 依赖id
     * @return 图片列表
     */
    List<Picture> loadPicture(Integer moduleId, Integer referencedId);


}
