package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.dao.common.AnnounceDao;
import com.iquanwai.platon.biz.dao.common.AnnualSummaryDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.RiseUserLoginDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.Announce;
import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.RiseUserLogin;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private AnnualSummaryDao annualSummaryDao;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private AnnounceDao announceDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String uploadHeadImage(Integer profileId, String fileName, InputStream inputStream) {
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
        BufferedImage cropBufferedImage = ImageUtils.cropImage(bufferedImage, startX, startY, endX, endY);
        BufferedImage reSizeBufferedImage = ImageUtils.scaleByPercentage(cropBufferedImage, 750, 750);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            if (reSizeBufferedImage != null) {
                ImageIO.write(reSizeBufferedImage, "jpeg", os);
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        InputStream cropInputStream = new ByteArrayInputStream(os.toByteArray());

        String targetFileName = "headImage" + "-" + CommonUtils.randomString(8) + "-" + fileName + ".jpeg";
        boolean uploadResult = QiNiuUtils.uploadFile(targetFileName, cropInputStream);

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

    @Override
    public AnnualSummary loadUserAnnualSummary(Integer profileId) {
        return annualSummaryDao.loadUserAnnualSummary(profileId);
    }

    @Override
    public AnnualSummary loadUserAnnualSummary(String riseId) {
        Profile profile = profileDao.queryByRiseId(riseId);
        AnnualSummary annualSummary = annualSummaryDao.loadUserAnnualSummary(riseId);
        if (annualSummary != null) {
            List<PrizeCard> prizeCards = prizeCardDao.getAnnualPrizeCards(profile.getId());
            annualSummary.setCardCount(prizeCards.size());
        }
        return annualSummary;
    }

    @Override
    public Boolean hasAnnualSummaryAuthority(Integer profileId) {
        return annualSummaryDao.loadUserAnnualSummary(profileId) != null;
    }

    @Override
    public int loadContinuousLoginCount(Integer profileId) {
        List<RiseUserLogin> riseUserLogins = riseUserLoginDao.loadByProfileId(profileId);
        int dayCount = 1;
        Date compareDate = new Date();
        for (int i = 0; i < riseUserLogins.size(); i++) {
            RiseUserLogin riseUserLogin = riseUserLogins.get(i);
            if (DateUtils.interval(compareDate, riseUserLogin.getLoginDate()) <= 1) {
                dayCount++;
                compareDate = riseUserLogin.getLoginDate();
            } else {
                break;
            }
        }
        return dayCount;
    }

    @Override
    public int loadJoinDays(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        RiseMember firstAddRiseMember = riseMembers.stream().min(Comparator.comparing(RiseMember::getOpenDate)).orElse(null);
        if (firstAddRiseMember != null) {
            return DateUtils.interval(new Date(), firstAddRiseMember.getOpenDate());
        } else {
            return 1;
        }
    }

    @Override
    public int loadPersonalTotalPoint(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        return profile.getPoint();
    }

    @Override
    public String loadAnnounceMessage(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        int memberTypeId = riseMember == null ? 0 : riseMember.getMemberTypeId();
        List<Announce> announces = announceDao.loadByMemberTypeId(memberTypeId);
        Announce validAnnounce = announces.stream().filter(announce -> announce.getStartTime() != null
                && announce.getEndTime() != null
                && new Date().compareTo(announce.getStartTime()) >= 0
                && announce.getEndTime().compareTo(new Date()) >= 0
        ).findAny().orElse(null);

        // 将已经超时的 announce del 置为 1
        List<Integer> expiredIds = announces.stream().filter(announce -> announce.getStartTime() != null
                && announce.getEndTime() != null
                && new Date().compareTo(announce.getEndTime()) > 0
        ).map(Announce::getId).collect(Collectors.toList());
        if (expiredIds.size() > 0) {
            announceDao.delExpiredAnnounce(expiredIds);
        }

        return validAnnounce == null ? null : validAnnounce.getMessage();
    }

}
