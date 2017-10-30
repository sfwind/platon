package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.domain.common.file.FileUploadService;
import com.iquanwai.platon.biz.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private FileUploadService fileUploadService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String uploadHeadImage(Integer profileId, String fileName, InputStream inputStream) {
        logger.info(DateUtils.parseDateTimeToString(new Date()));

        BufferedImage bufferedImage = ImageUtils.getBufferedImageByInputStream(inputStream);
        if (bufferedImage == null) {
            return null;
        }

        int startX, startY, endX, endY;
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        if (height > width) {
            startX = 0;
            startY = (height - width) / 2;
            endX = width;
            endY = (height + width) / 2;
        } else {
            startX = (width - height) / 2;
            startY = 0;
            endX = (width + height) / 2;
            endY = height;
        }

        logger.info(DateUtils.parseDateTimeToString(new Date()));
        BufferedImage cropBufferedImage = ImageUtils.cropImage(bufferedImage, startX, startY, endX, endY);
        logger.info(DateUtils.parseDateTimeToString(new Date()));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(cropBufferedImage, "jpeg", os);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        InputStream cropInputStream = new ByteArrayInputStream(os.toByteArray());

        String targetFileName = "headImage" + "-" + CommonUtils.randomString(8) + "-" + fileName;
        boolean uploadResult = QiNiuUtils.uploadFile(targetFileName, cropInputStream);
        logger.info(DateUtils.parseDateTimeToString(new Date()));

        return uploadResult ? ConfigUtils.getPicturePrefix() + targetFileName : null;
    }

    @Override
    public int updateHeadImageUrl(Integer profileId, String headImgUrl) {
        return profileDao.updateHeadImgUrl(profileId, headImgUrl);
    }

    @Override
    public int updateNickName(Integer profileId, String nickName) {
        return profileDao.updateNickName(profileId, nickName);
    }

}