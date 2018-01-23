package com.iquanwai.platon.biz.temp;

import com.iquanwai.platon.biz.util.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by 三十文
 */
public class MailPrint {

    public void drawMail(String nickname, String profileId) throws IOException, FontFormatException {
        String folder = "/Users/xfduan/Downloads/quanquan_mail_type5";

        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/quanquan_mail_type5.jpg?imageslim");
        Font font;
        try (
                InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
        ) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
            ImageUtils.writeText(targetImage, 455, 940, "亲爱的", font.deriveFont(40f), new Color(0, 0, 0));
            ImageUtils.writeText(targetImage, 595, 940, nickname + "，", font.deriveFont(64f), new Color(0, 0, 0));
            ImageUtils.writeToFile(targetImage, "jpg", new File(folder + File.separator + profileId + "-" + nickname + ".jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        MailPrint mailPrint = new MailPrint();
        BufferedReader reader = new BufferedReader(new FileReader(new File("/Users/xfduan/Desktop/hello.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] person = line.split(",");
            String nickname = person[1];
            String profileId = person[0];
            mailPrint.drawMail(nickname, profileId);
            System.out.println(nickname + "打印成功");
        }
        System.out.println("打印成功");
    }

}
