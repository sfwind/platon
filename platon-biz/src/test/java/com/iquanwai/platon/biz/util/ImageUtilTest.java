package com.iquanwai.platon.biz.util;

import com.iquanwai.platon.biz.po.common.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtilTest {
    private static Logger logger = LoggerFactory.getLogger(ImageUtilTest.class);


    public static void main(String[] args) {
        BufferedImage ordinaryImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/certificate_normal_bg_1.jpg?imageslim");
        BufferedImage excellentImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/certificate_bg.jpg?imageslim");
        BufferedImage inputImage = null;

        int year = 2017;
        int month = 8;
        String problemName = "与人沟通时条理更清晰";
        Profile profile = new Profile();
        profile.setRealName("张三");
        String certificateNo = "IQW052017040010770800330";
        int groupNo = 2;


        inputImage = excellentImage;
        if (inputImage != null) {
            Graphics2D graphics2D = inputImage.createGraphics();
            ImageUtils.writeTextCenter(graphics2D, 160, 375, "圈外同学 • " + month + "月小课训练营", 20f, new Color(255, 255, 255));
            ImageUtils.writeTextCenter(graphics2D, 200, 375, "《" + problemName + "》", 32f, new Color(255, 255, 255));
            ImageUtils.writeTextCenter(graphics2D, 405, 375, "优秀团队", 72f, new Color(102, 102, 102));
            ImageUtils.writeTextCenter(graphics2D, 545, 375, NumberToHanZi.formatInteger(month) + "月小课" + NumberToHanZi.formatInteger(groupNo) + "组", 50f, new Color(102, 102, 102));
            ImageUtils.writeTextCenter(graphics2D, 610, 375, "在【圈外同学】" + year + "年" + month + "月小课训练营中", 32f, new Color(102, 102, 102));
            ImageUtils.writeTextCenter(graphics2D, 660, 375, "小组表现优异，荣膺“优秀小组”称号", 32f, new Color(102, 102, 102));
            ImageUtils.writeTextCenter(graphics2D, 765, 375, "特发此证，以资鼓励", 32f, new Color(102, 102, 102));
            ImageUtils.writeTextCenter(graphics2D, 1285, 375, "证书编号：" + certificateNo, 20f, new Color(182, 144, 47));
            graphics2D.dispose();
        }
        ImageUtils.writeToFile(inputImage, "png", new File("/Users/xfduan/Downloads/Hello.png"));


    }
}
