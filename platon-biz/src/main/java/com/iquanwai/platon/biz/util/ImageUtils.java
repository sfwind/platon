package com.iquanwai.platon.biz.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by justin on 17/7/12.
 */
public class ImageUtils {
    private static OkHttpClient client = new OkHttpClient();
    private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);
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

        if(StringUtils.isNotEmpty(url)) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                return ImageIO.read(response.body().byteStream());
            } catch (Exception e) {
                logger.error("execute " + url + " error", e);
            }
        }

        return null;
    }

    public static BufferedImage overlapImage(BufferedImage big, BufferedImage small, int x, int y) {
        Graphics2D g = big.createGraphics();
        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
        g.dispose();
        return big;
    }


    public static void convert2PNG(InputStream in, OutputStream out)throws IOException, TranscoderException
    {
        Transcoder transcoder = new PNGTranscoder();
        try {
            TranscoderInput input = new TranscoderInput(in);
            try {
                TranscoderOutput output = new TranscoderOutput(out);
                transcoder.transcode(input, output);
            } catch (TranscoderException e){
                logger.error("transcoder error", e);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}


