package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Picture;
import com.iquanwai.platon.biz.po.common.PictureModule;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by nethunder on 2017/3/28.
 */
@RestController
@RequestMapping("/rise/file")
public class FileController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PictureService pictureService;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/editor/image/upload/{moduleId}",method = {RequestMethod.POST} )
    public ResponseEntity<Map<String, Object>> editorImageUpload(LoginUser loginUser, @RequestParam("file") MultipartFile file,
                                                                 @PathVariable("moduleId") Integer moduleId,
                                                                 HttpServletRequest request) {

        if (moduleId != null && file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            Long fileSize = file.getSize();
            String contentType = file.getContentType();
            String remoteIp = request.getHeader("X-Forwarded-For");



            PictureModule pictureModule = pictureService.getPictureModule(moduleId);
            Picture picture = new Picture(fileSize, null, contentType, remoteIp);
            Pair<Boolean,String> uploadPicture;
            if (pictureModule != null) {
                Pair<Integer,String> checkResult = pictureService.checkAvaliable(pictureModule, picture);
                if (checkResult.getLeft() == 1) {
                    // 可上传
                    try {
                        OperationLog operationLog = OperationLog.create().openid(loginUser!=null?loginUser.getOpenId():"")
                                .module("文件")
                                .function("上传图片")
                                .action("移动端上传图片")
                                .memo(moduleId + "");
                        operationLogService.log(operationLog);
                        uploadPicture = pictureService.uploadPicture(pictureModule, fileName,  file);
                    } catch (FileNotFoundException e) {
                        logger.error("upload image error:上传图片失败，模块目录:" + pictureModule.getModuleName() + "未创建!", e);
                        return WebUtils.error("该模块目录未创建");
                    } catch (Exception e) {
                        logger.error("upload image error:上传图片失败", e);
                        return WebUtils.error(e.getLocalizedMessage());
                    }
                    if (uploadPicture.getLeft()) {
                        String url = ConfigUtils.getPicturePrefix() + uploadPicture.getRight();
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("picUrl", url);
                        return WebUtils.result(map);
                    } else {
                        logger.error("上传失败", uploadPicture.getRight());
                        return WebUtils.error(uploadPicture.getRight());
                    }

                } else {
                    // 不可上传
                    logger.error("upload image error:上传校验失败," + checkResult.getRight());
                    return WebUtils.error(checkResult.getRight());
                }
            } else {
                logger.error("upload image error:无该图片模块," + moduleId);
                return WebUtils.error("无该模块:" + moduleId);
            }
        } else {
            // 模块名为空,禁止上传
            if (moduleId == null) {
                logger.error("upload image error:moduleId为空");
                return WebUtils.error("请求异常，请重试");
            } else {
                logger.error("upload image error:文件解析失败");
                return WebUtils.error("该图片无法解析，请重新选择");
            }
        }
    }
}
