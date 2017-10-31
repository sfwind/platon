package com.iquanwai.platon.biz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtilTest {
    private static Logger logger = LoggerFactory.getLogger(ImageUtilTest.class);


    // 616 638
    // 616 627
    public static void main(String[] args) {
        BufferedImage bufferedImage = ImageUtils.getBufferedImageByUrl("http://f.hiphotos.baidu.com/image/pic/item/1c950a7b02087bf4e74b4f28fbd3572c10dfcf48.jpg");
        Assert.notNull(bufferedImage);
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
        System.out.println("startX = " + startX);
        System.out.println("startY = " + startY);
        System.out.println("endX = " + endX);
        System.out.println("endY = " + endY);
        bufferedImage = ImageUtils.cropImage(bufferedImage, startX, startY, endX, endY);
        try {
            ImageIO.write(bufferedImage, "jpg", new FileOutputStream("/Users/xfduan/Downloads/temp.jpg"));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
