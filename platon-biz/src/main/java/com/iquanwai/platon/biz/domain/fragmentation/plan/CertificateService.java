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
     */
    RiseCertificate getCertificate(String certificateNo);

    /**
     * 准备打印证书图片用到
     * @param certificateId 证书 id
     */
    RiseCertificate getNextCertificate(Integer certificateId);

    /**
     * 将图片的 base64 码转为图片保存
     * @param base64Str base64 码
     * @param imgPath 存储路径
     */
    boolean convertCertificateBase64(String base64Str, String imgPath);

    /**
     * 证书下载完成时间更新
     * @param certificateNo 证书号
     */
    int updateDownloadTime(String certificateNo);

    void generateCertificate(Integer year, Integer month, Integer problemId);

    void generateFullAttendanceCoupon(Integer year, Integer month, Integer problemId);

    /**
     * 发送训练营证书通知
     * @param year 开营年份
     * @param month 开营月份
     */
    void sendCertificate(Integer year, Integer month);

    void sendFullAttendanceCoupon(Integer year, Integer month);

    /**
     * 获取我的证书
     * @param profileId 用户id
     */
    List<RiseCertificate> getCertificates(Integer profileId);

    /**
     * 发送小课训练营结课用户的商学院录取消息
     * @param year 开营年份
     * @param month 开营月份
     */
    void sendOfferMsg(Integer year, Integer month);
}
