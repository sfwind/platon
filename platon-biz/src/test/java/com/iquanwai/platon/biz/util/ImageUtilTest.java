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
        BufferedImage ordinaryImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/certificate_normal_bg_5.jpg?imageslim");
        BufferedImage excellentImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/certificate_bg_5.jpg?imageslim");
        BufferedImage inputImage = null;

        int year = 2017;
        int month = 8;
        String problemName = "与人沟通时条理更清晰";
        Profile profile = new Profile();
        profile.setRealName("张三");
        String certificateNo = "IQW052017040010770800330";
        int groupNo = 2;

        InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
            // inputImage = excellentImage;
            // ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月小课训练营", font.deriveFont(28f), new Color(255, 255, 255));
            // ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
            // ImageUtils.writeTextCenter(inputImage, 450, "毕业证书", font.deriveFont(92f), new Color(102, 102, 102));
            // ImageUtils.writeTextCenter(inputImage, 650, "三十文", font.deriveFont(78f), new Color(102, 102, 102));
            // ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月小课训练营中", font.deriveFont(48f), new Color(102, 102, 102));
            // ImageUtils.writeTextCenter(inputImage, 850, "小组表现优异，荣膺“优秀小组”称号", font.deriveFont(48f), new Color(102, 102, 102));
            // ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
            // ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));

            // ImageUtils.writeTextCenter(inputImage, 950, "“", font.deriveFont(48f), new Color(102, 102, 102));
            inputImage = ImageUtils.copy(excellentImage);
            ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月小课训练营", font.deriveFont(28f), new Color(255, 255, 255));
            ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
            ImageUtils.writeTextCenter(inputImage, 450, "优秀助教", font.deriveFont(92f), new Color(102, 102, 102));
            ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
            ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月小课训练营中", font.deriveFont(48f), new Color(102, 102, 102));
            ImageUtils.writeTextCenter(inputImage, 850, "表现卓越，荣膺“优秀助教”称号", font.deriveFont(48f), new Color(102, 102, 102));
            ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
            ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));

            ImageUtils.writeToFile(inputImage, "png", new File("/Users/xfduan/Downloads/Hello.png"));
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("is closed error", e);
            }
        }

    }
}
