package com.iquanwai.platon.biz.service;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.ImageUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by justin on 16/10/12.
 */
public class TemplateMessageServiceTest extends TestBase {
    @Autowired
    private TemplateMessageService templateMessageService;

    private ImageUtils imageUtil;

    @Test
    public void testSend(){
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser("o-Es21RVF3WCFQMOtl07Di_O9NVo");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getMessageReplyCode());
        data.put("first", new TemplateMessage.Keyword("Hi " + "向哲" + "，" +
                "你的职业发展核心能力和心理品质量表，有新的他评问卷完成，请知晓。\n"));
        data.put("keyword1", new TemplateMessage.Keyword("风之伤"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("职业发展核心能力和心理品质量表-他评"));
        templateMessageService.sendMessage(templateMessage);
    }


    @Test
    public void getImg() throws Exception {
        BufferedImage bufferedImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/dailytalk/005.png");
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer=null;
        while(it.hasNext()) {
            writer=it.next();
            break;
            //System.out.println(it.next());
        }
        if(writer!=null) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            ImageOutputStream output = ImageIO.createImageOutputStream(new File("/Users/iquanwai_yang/Desktop/test.png"));

            writer.setOutput(output);
            writer.write(null,new IIOImage(bufferedImage,null,null), params);
            output.flush();
            writer.dispose();
            System.out.println("ok");
        }

    }
}
