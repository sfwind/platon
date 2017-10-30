package com.iquanwai.platon.biz.domain.common.file;

import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String FTP_IMAGE_STORE = "/data/static/images/";
    private final static String STATIC_IMAGE = "images/";

    @Override
    public String uploadFtpImageFile(String prefix, String originFileName, InputStream uploadFileStream) {
        FTPUtil ftpUtil = new FTPUtil();
        int dotIndex = originFileName.lastIndexOf(".");
        String sufFileName = originFileName.substring(dotIndex);
        String targetFileName = prefix + "-" + CommonUtils.randomString(8) + sufFileName;
        try {
            ftpUtil.connect();
            ftpUtil.setBinaryType();
            boolean result = ftpUtil.storeFile(FTP_IMAGE_STORE + targetFileName, uploadFileStream);
            if (result) {
                return ConfigUtils.getPicturePrefix() + STATIC_IMAGE + targetFileName;
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                ftpUtil.disconnect();
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

}
