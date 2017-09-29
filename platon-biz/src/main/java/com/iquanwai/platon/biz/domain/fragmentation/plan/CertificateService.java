package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.RiseCertificate;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 发送训练营证书通知
     * @param year 开营年份
     * @param month 开营月份
     * */
    void sendCertificate(Integer year, Integer month);

    /**
     * 获取我的证书
     * @param profileId 用户id
     * */
    List<RiseCertificate> getCertificates(Integer profileId);

    /**
     * 发送小课训练营毕业用户的商学院录取消息
     * @param year 开营年份
     * @param month 开营月份
     * */
    void sendOfferMsg(Integer year, Integer month);
}
