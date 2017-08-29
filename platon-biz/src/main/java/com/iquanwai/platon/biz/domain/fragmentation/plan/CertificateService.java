package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.RiseCertificate;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/8/29.
 */
@Service
public interface CertificateService {
    /**
     * 获取证书
     * @param certificateNo 证书号
     * */
    RiseCertificate getCertificate(String certificateNo);
}
