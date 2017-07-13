package com.iquanwai.platon.biz.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by justin on 17/7/12.
 */
public class ImageUtils {

    /*
    * 变成一个圆形
    * @param inputImage 需要修改的图片
    * */
    public static BufferedImage convertCircular(BufferedImage inputImage) {
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
        //获取原始图像透明度类型
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

    /*
    * 写文字
    * @param image 图片
    * @param x 写字的横坐标位置
    * @param y 写字的纵坐标位置
    * @param font 字体
    * @param color 文字颜色
    * */
    public static BufferedImage writeText(BufferedImage inputImage, int x, int y, String text, Font font, Color color) {
        Graphics2D graphics2d = inputImage.createGraphics();
        graphics2d.setColor(color);
        graphics2d.setFont(font);
        graphics2d.drawString(CommonUtils.filterEmoji(text), x, y);
        graphics2d.dispose();
        return inputImage;
    }

    /*
    * 通过url拉取图片信息
    * @param url 图片链接
    * */
    public static BufferedImage getUrlByBufferedImage(String url) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
// 连接超时
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(2500);
// 读取超时 --服务器响应比较慢,增大时间
            conn.setReadTimeout(2500);
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Accept-Language", "zh-cn");
            conn.addRequestProperty("Content-type", "image/png");
            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
            conn.connect();
            BufferedImage bufImg = ImageIO.read(conn.getInputStream());
            conn.disconnect();
            return bufImg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    public static BufferedImage overlapImage(BufferedImage big, BufferedImage small, int x, int y) {
        Graphics2D g = big.createGraphics();
        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
        g.dispose();
        return big;
    }

//    public static void main(String[] args) {
//        try {
//            long l1 = System.currentTimeMillis();
//
//            //获取图片的流
//            BufferedImage big = getUrlByBufferedImage("http://wx.qlogo.cn/mmopen/Q3auHgzwzM6LrkJRYApibxYsAEYm2CmS7JZwX09AmHsP0X2VJQSpibHyoHsQKNcvqf1hzFgJr6l40vyhH7KtGWupGmgKHwFibbiaOOS0qKuvjsQ/0");
//            BufferedImage small = getUrlByBufferedImage("http://static.iqycamp.com/images/logo.png");
//
//            //处理图片将其压缩成正方形的小图
////            BufferedImage  convertImage = writeText(url, 22, 22);
//            //裁剪成圆形 （传入的图像必须是正方形的 才会 圆形 如果是长方形的比例则会变成椭圆的）
////            convertImage = convertCircular(url);
//            big = overlapImage(big, small, 100, 100);
//            //生成的图片位置
//            String imagePath = "/Users/justin/a.png";
//            ImageIO.write(big, imagePath.substring(imagePath.lastIndexOf(".") + 1), new File(imagePath));
//            long l2 = System.currentTimeMillis();
//            System.out.println(l2 - l1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}


