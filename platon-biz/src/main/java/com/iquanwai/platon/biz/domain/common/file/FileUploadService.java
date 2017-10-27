package com.iquanwai.platon.biz.domain.common.file;

import java.io.InputStream;

public interface FileUploadService {
    String uploadFtpImageFile(String prefix, String originFileName, InputStream uploadFileStream);
}
