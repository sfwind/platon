package com.iquanwai.platon.biz.domain.fragmentation.certificate;

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

    /**
     * 生成单人全勤奖
     * @param planId 课程 id
     */
    void generatePersonalFullAttendance(Integer planId);

    /**
     * 发送专项课证书通知
     * @param year 开营年份
     * @param month 开营月份
     * @param memberTypeId 身份类型
     */
    void sendCertificate(Integer year, Integer month, Integer memberTypeId);

    /**
     * 获取我的证书
     * @param profileId 用户id
     */
    List<RiseCertificate> getCertificates(Integer profileId);

    /**
     * 发送专项课结课用户的商学院录取消息
     * @param year 开营年份
     * @param month 开营月份
     */
    void sendOfferMsg(Integer year, Integer month);

    /**
     * 根据选择的年月以及身份情况，生成对应的全勤奖
     * @param year 生成奖学金年份
     * @param month 生成奖学金月份
     * @param memberTypeId 身份类型
     */
    void generateBatchFullAttendance(Integer year, Integer month, Integer memberTypeId);

    /**
     * 对指定身份类型，生成对于月份主修课程的结课证书
     * @param year 生成结课证书的年份
     * @param month 生成结课证书的月份
     * @param memberTypeId 身份类型
     */
    void generateGraduateCertificateByMemberType(Integer year, Integer month, Integer memberTypeId);

    /**
     * 插入特殊人员证书
     * @param memberIds 学号列表
     * @param year 正在学习的人员
     * @param month 正在学习的月份
     * @param memberTypeId 身份类型
     * @param type 证书类型
     */
    void insertSpecialCertificate(List<String> memberIds, Integer year, Integer month, Integer memberTypeId, Integer type);
}
