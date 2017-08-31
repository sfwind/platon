package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 17/8/29.
 */
@Service
public class CertificateServiceImpl implements CertificateService {
    @Autowired
    private RiseCertificateDao riseCertificateDao;
    @Autowired
    private AccountService accountService;

    @Override
    public RiseCertificate getCertificate(String certificateNo) {
        RiseCertificate riseCertificate = riseCertificateDao.loadByCertificateNo(certificateNo);
        buildDetail(riseCertificate);
        //删除profileId
        riseCertificate.setProfileId(null);
        return riseCertificate;
    }

    private void buildDetail(RiseCertificate riseCertificate){
        Integer type = riseCertificate.getType();
        Profile profile = accountService.getProfile(riseCertificate.getProfileId());
        switch (type){
            case Constants.CERTIFICATE.TYPE.CLASS_LEADER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中担任班长一职，表现突出，荣膺\"优秀班长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.CLASS_LEADER);
            break;
            case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中担任组长一职，表现优异，荣膺\"优秀班长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.GROUP_LEADER);
            break;
            case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中成绩名列前茅，荣膺\"优秀学员\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_MEMBER);
            break;
            case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                String monthStr = NumberToHanZi.formatInteger(riseCertificate.getMonth());
                String groupNoStr = NumberToHanZi.formatInteger(riseCertificate.getGroupNo());
                riseCertificate.setName(monthStr+"月小课"+groupNoStr+"组");
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中小组表现优异，荣膺\"优秀小组\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_GROUP);
            break;
        }
    }
}
