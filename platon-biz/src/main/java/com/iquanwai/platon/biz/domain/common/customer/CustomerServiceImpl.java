package com.iquanwai.platon.biz.domain.common.customer;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.AnnualSummaryDao;
import com.iquanwai.platon.biz.dao.common.FeedbackDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.Feedback;
import com.iquanwai.platon.biz.po.common.Profile;
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
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private AnnualSummaryDao annualSummaryDao;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private FeedbackDao feedbackDao;
    @Autowired
    private TemplateMessageService templateMessageService;

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
    public void sendFeedback(Feedback feedback) {
        int result = feedbackDao.insert(feedback);
        if (result != -1) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());
            templateMessage.setTouser(ConfigUtils.feedbackAlarmOpenId());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            data.put("first", new TemplateMessage.Keyword("圈外用户问题来了，速速去处理吧"));
            data.put("keyword1", new TemplateMessage.Keyword("圈外用户问题"));
            data.put("keyword2", new TemplateMessage.Keyword("H5个人中心反馈问题需处理"));
            data.put("keyword3", new TemplateMessage.Keyword(
                    (DateUtils.parseDateTimeToString(new Date()))));
            templateMessage.setData(data);
            templateMessageService.sendMessage(templateMessage);
        }
    }

}
