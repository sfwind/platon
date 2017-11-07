package com.iquanwai.platon.biz.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 17/7/12.
 */
public class ImageUtils {
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Constants.HTTP_TIMEOUT.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.HTTP_TIMEOUT.READ_TIMEOUT, TimeUnit.SECONDS)
            .build();
    private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 图片拷贝
     */
    public static BufferedImage copy(BufferedImage inputImage) {
        BufferedImage targetImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        targetImage.setData(inputImage.getData());
        return targetImage;
    }

    /*
    * 变成一个圆形
    * @param inputImage 需要修改的图片
    * */
    public static BufferedImage convertCircular(BufferedImage inputImage) {
        if (inputImage == null) {
            logger.error("input image is null");
            return null;
        }
        //透明底的图片
        BufferedImage image = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, inputImage.getWidth(), inputImage.getHeight());
        Graphics2D graphics2d = image.createGraphics();
        graphics2d.setClip(shape);
        graphics2d.drawImage(inputImage, 0, 0, null);
        graphics2d.dispose();
        return image;
    }

    /*
    * 压缩图片尺寸
    * @param inputImage 需要压缩的图片
    * @param newWidth 新的宽度
    * @param newHeight 新的高度
    * */
    public static BufferedImage scaleByPercentage(BufferedImage inputImage, int newWidth, int newHeight) {
        if (inputImage == null) {
            logger.error("input image is null");
            return null;
        }
        int type = inputImage.getColorModel().getTransparency();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        //开启抗锯齿
        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //使用高质量压缩
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        BufferedImage img = new BufferedImage(newWidth, newHeight, type);
        Graphics2D graphics2d = img.createGraphics();
        graphics2d.setRenderingHints(renderingHints);
        graphics2d.drawImage(inputImage, 0, 0, newWidth, newHeight, 0, 0, width, height, null);
        graphics2d.dispose();
        return img;
    }

    /**
     * 图片裁剪
     * @param inputImage 需要裁剪的图片
     * @param startX 裁剪开始 x 坐标
     * @param startY 裁剪开始 y 坐标
     * @param endX 裁剪截止 x 坐标
     * @param endY 裁剪截止 y 坐标
     */
    public static BufferedImage cropImage(BufferedImage inputImage, int startX, int startY, int endX, int endY) {
        Assert.isTrue(startX >= 0);
        Assert.isTrue(startY >= 0);
        endX = endX > inputImage.getWidth() ? inputImage.getWidth() : endX;
        endY = endY > inputImage.getHeight() ? inputImage.getHeight() : endY;

        BufferedImage result = new BufferedImage(endX - startX, endY - startY, inputImage.getType());
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int rgb = inputImage.getRGB(x, y);
                result.setRGB(x - startX, y - startY, rgb);
            }
        }
        return result;
    }

    /*
    * 写文字
    * @param image 图片
    * @param x 写字的横坐标位置
    * @param y 写字的纵坐标位置
    * @param font 字体
    * @param color 文字颜色
    * */
    public static BufferedImage writeText(BufferedImage inputImage, int x, int y, String text, Font font, Color color) {
        if (inputImage == null) {
            logger.error("input image is null");
            return null;
        }
        Graphics2D graphics2d = inputImage.createGraphics();
        graphics2d.setFont(font);
        graphics2d.setColor(color);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.drawString(CommonUtils.filterEmoji(text), x, y);
        graphics2d.dispose();
        return inputImage;
    }

    /**
     * 居中书写文字
     * @param inputImage 待绘制内容
     * @param y y 轴
     * @param text 内容
     * @param font 字体
     * @param color 颜色
     */
    public static BufferedImage writeTextCenter(BufferedImage inputImage, int y, String text, Font font, Color color) {
        Assert.notNull(inputImage, "input image is null");
        Graphics2D graphics2d = inputImage.createGraphics();
        graphics2d.setFont(font);
        graphics2d.setColor(color);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
        // 计算出中心点 x 位置
        int centerX = inputImage.getWidth() / 2;
        int textWidth = fontMetrics.stringWidth(text);
        graphics2d.drawString(text, centerX - textWidth / 2, y);
        graphics2d.dispose();
        return inputImage;
    }

    /**
     * 判断在一个图片域中绘制文字是否会溢出
     * @param inputImage 待绘制的图片
     * @param text 待绘制文字
     * @param font 字体
     */
    public static boolean isTextOverflow(BufferedImage inputImage, String text, Font font) {
        Assert.notNull(inputImage, "input image is null");
        Graphics2D graphics2D = inputImage.createGraphics();
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        return fontMetrics.stringWidth(text) >= inputImage.getWidth();
    }

    /*
    * 通过url拉取图片信息
    * @param url 图片链接
    * */
    public static BufferedImage getBufferedImageByUrl(String url) {
        if (StringUtils.isNotEmpty(url)) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                String xErrorNo = response.header("X-ErrNo");
                if (xErrorNo != null && "-6101".equalsIgnoreCase(xErrorNo)) {
                    return null;
                }
                ImageIO.setUseCache(false);
                return ImageIO.read(response.body().byteStream());
            } catch (Exception e) {
                logger.error("execute " + url + " error", e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
        return null;
    }

    /*
    * 通过url拉取图片信息
    * @param url 图片链接
    * */
    public static BufferedImage getBufferedImageByInputStream(InputStream inputStream) {
        try {
            ImageIO.setUseCache(false);
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            logger.error("read inputStream error", e);
        }
        return null;
    }

    // 大图内嵌小图
    public static BufferedImage overlapImage(BufferedImage big, BufferedImage small, int x, int y) {
        if (big == null || small == null) {
            logger.error("input image is null");
            return big;
        }
        Graphics2D g = big.createGraphics();
        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
        g.dispose();
        return big;
    }

    public static void writeToFile(BufferedImage image, String format, File file) {
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            logger.error("write to file error", e);
        }
    }

    public static void writeToOutputStream(BufferedImage image, String format, OutputStream outputStream) {
        try {
            ImageIO.write(image, format, outputStream);
        } catch (IOException e) {
            logger.error("write to outputStream error", e);
        }
    }
}
