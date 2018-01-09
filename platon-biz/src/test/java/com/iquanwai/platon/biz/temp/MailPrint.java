package com.iquanwai.platon.biz.temp;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.common.MaterialPrintDao;
import com.iquanwai.platon.biz.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 三十文
 */
public class MailPrint extends TestBase {

    @Autowired
    private MaterialPrintDao materialPrintDao;

    public void drawMail() throws IOException, FontFormatException {
        String folder = "/Users/xfduan/Downloads/quanquan";

        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/quanquan_mail.jpg?imageslim");
        Font font;
        try (
                InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
        ) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
            ImageUtils.writeText(targetImage, 455, 940, "亲爱的三十文，", font.deriveFont(40f), new Color(0, 0, 0));
            ImageUtils.writeToFile(targetImage, "jpg", new File(folder + File.separator + "三十文.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        MailPrint mailPrint = new MailPrint();
        mailPrint.drawMail();
    }

}
