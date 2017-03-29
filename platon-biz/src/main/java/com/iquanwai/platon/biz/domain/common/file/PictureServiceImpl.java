package com.iquanwai.platon.biz.domain.common.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.PictureDao;
import com.iquanwai.platon.biz.po.common.Picture;
import com.iquanwai.platon.biz.po.common.PictureModule;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.QiNiuUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nethunder on 2016/12/15.
 */
@Service
public class PictureServiceImpl implements PictureService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Integer,PictureModule> moduleMap = Maps.newConcurrentMap();
    private Map<Integer,String> prefixMap = Maps.newConcurrentMap();

    @Autowired
    private PictureDao pictureDao;

    @PostConstruct
    public void initPictureModule(){
        List<PictureModule> moduleList = pictureDao.loadAll(PictureModule.class);
        if(moduleList!=null){
            moduleList.forEach(item -> {
                moduleMap.put(item.getId(), item);
                prefixMap.put(item.getId(), ConfigUtils.getUploadDomain()+"/images/"+item.getModuleName()+"/");
            });

        }

    }

    @Override
    public PictureModule getPictureModule(Integer id) {
        PictureModule pictureModule = moduleMap.get(id);
        if(pictureModule==null){
            logger.error("moduleId: {} is invalid!",id);
            return null;
        } else {
            return pictureModule;
        }
    }

    @Override
    public void reloadModule() {
        initPictureModule();
    }

    @Override
    public Pair<Integer,String> checkAvaliable(PictureModule pictureModule, Picture picture) {
        Map<String, String> map = Maps.newHashMap();
        Integer sizeLimit = pictureModule.getSizeLimit();
        if(picture.getLength()==null){
            return new ImmutablePair<Integer,String>(0,"该图片大小未知，无法上传");
        }
        if(picture.getType()==null){
            return new ImmutablePair<Integer, String>(0, "该图片类型未知，无法上传");
        }

        if(sizeLimit!=null && picture.getLength()>sizeLimit){
            return new ImmutablePair<Integer, String>(0, "该图片过大，请压缩后上传");
        }
        List<String> typeList = pictureModule.getTypeLimit()==null? Lists.newArrayList():Lists.newArrayList(pictureModule.getTypeLimit().split(","));
        long matchTypeCount = typeList.stream().filter(contentType -> contentType.equals(picture.getType())).count();
        if(matchTypeCount==0){
            return new ImmutablePair<Integer, String>(0, pictureModule.getModuleName() + "模块不支持该图片类型");
        }
        // 通过校验开始上传
        return new ImmutablePair<Integer,String>(1,null);
    }

    @Override
    public Pair<Boolean,String> uploadPicture(PictureModule pictureModule, String fileName,  MultipartFile file) throws Exception {
        // 获取模块名对应的路径
        String path = pictureModule.getPath();
        // 文件名
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf("."), fileName.length()) : "";
        // 命名规则 {module}-{date}-{rand(8)}-{referId}.{filename的后缀}
        Date today = new Date();
        String realName = pictureModule.getModuleName()+"-"+ DateUtils.parseDateToString3(today)+"-"+ CommonUtils.randomString(9)+"-"+suffix;
        //获取该文件的文件名
        File targetFile = new File(path, realName);
        Boolean result = false;
        // 保存
        try {
            result = QiNiuUtils.uploadFile(realName, file.getInputStream());
//            file.transferTo(targetFile);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
        return new MutablePair<>(result,realName);
    }


    @Override
    public String getModulePrefix(Integer moduleId) {
        String prefix = prefixMap.get(moduleId);
        if(prefix==null){
            logger.error("moduleId: {} is invalid",moduleId);
        }
        return prefix;
    }

    @Override
    public List<Picture> loadPicture(Integer moduleId, Integer referencedId) {
        return pictureDao.picture(moduleId, referencedId);
    }

}
