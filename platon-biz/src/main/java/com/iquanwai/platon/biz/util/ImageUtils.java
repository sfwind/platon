package com.iquanwai.platon.biz.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
        graphics2d.setFont(font);
        graphics2d.setColor(color);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.drawString(CommonUtils.filterEmoji(text), x, y);
        graphics2d.dispose();
        return inputImage;
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
                return ImageIO.read(response.body().byteStream());
            } catch (Exception e) {
                logger.error("execute " + url + " error", e);
            }
        }
        return null;
    }

    // 大图内嵌小图
    public static BufferedImage overlapImage(BufferedImage big, BufferedImage small, int x, int y) {
        Graphics2D g = big.createGraphics();
        g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
        g.dispose();
        return big;
    }

    // 将 svg 图的 base64 转成 png 的 base64
    public static String convertSvg2Png(String svgBase64) {
        System.out.println("svgBase64 = " + svgBase64);
        svgBase64 = svgBase64.replace(" ", "+");
        String testCode = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjwhLS0gR2VuZXJhdG9yOiBBZG9iZSBJbGx1c3RyYXRvciAxNi4wLjAsIFNWRyBFeHBvcnQgUGx1Zy1JbiAuIFNWRyBWZXJzaW9uOiA2LjAwIEJ1aWxkIDApICAtLT4NCjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+DQo8c3ZnIHZlcnNpb249IjEuMSIgaWQ9IkxheWVyXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4Ig0KCSB3aWR0aD0iMTAwcHgiIGhlaWdodD0iMTAwcHgiIHZpZXdCb3g9IjAgMCAxMDAgMTAwIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCAxMDAgMTAwIiB4bWw6c3BhY2U9InByZXNlcnZlIj4NCjxnPg0KCTxwYXRoIGQ9Ik03MS42MzMsMi4yOEgyOC4zNjdjLTQuMjEyLDAtNy42MzUsMy40MjMtNy42MzUsNy42MzV2ODAuMTY4YzAsNC4yMTIsMy40MjMsNy42MzYsNy42MzUsNy42MzZoNDMuMjY2DQoJCWM0LjIxMywwLDcuNjM1LTMuNDI0LDcuNjM1LTcuNjM2VjkuOTE2Qzc5LjI2OCw1LjcwMyw3NS44NDYsMi4yOCw3MS42MzMsMi4yOHogTTQzLjAwMSw5LjI1NGMwLTEuMDQzLDAuODQxLTEuODg0LDEuODg0LTEuODg0DQoJCWgxMC4yM2MxLjA0NCwwLDEuODg0LDAuODQxLDEuODg0LDEuODg0djAuMDVjMCwxLjA0My0wLjg0LDEuODg0LTEuODg0LDEuODg0aC0xMC4yM2MtMS4wNDMsMC0xLjg4NC0wLjg0LTEuODg0LTEuODg0VjkuMjU0eg0KCQkgTTUwLDk0Ljg1NWMtMi4yNzcsMC00LjEzNi0xLjg1Ny00LjEzNi00LjEzNmMwLTIuMjc3LDEuODU4LTQuMTM2LDQuMTM2LTQuMTM2YzIuMjc4LDAsNC4xMzYsMS44NTgsNC4xMzYsNC4xMzYNCgkJQzU0LjEzNiw5Mi45OTgsNTIuMjc4LDk0Ljg1NSw1MCw5NC44NTV6IE03Mi45MDUsODIuNDQ5YzAsMC42OTktMC41NzIsMS4yNzItMS4yNzIsMS4yNzJIMjguMzY3Yy0wLjcsMC0xLjI3Mi0wLjU3My0xLjI3Mi0xLjI3Mg0KCQlWMTcuNTUxYzAtMC43LDAuNTcyLTEuMjcyLDEuMjcyLTEuMjcyaDQzLjI2NmMwLjcsMCwxLjI3MiwwLjU3MywxLjI3MiwxLjI3MlY4Mi40NDl6Ii8+DQoJPHBhdGggZD0iTTY2LjU1Niw3NC44MTNoLTMzLjExYy0wLjY5NiwwLTEuMjYxLDAuNTY0LTEuMjYxLDEuMjYxdjAuMDI0YzAsMC42OTYsMC41NjQsMS4yNjEsMS4yNjEsMS4yNjFoMzMuMTENCgkJYzAuNjk1LDAsMS4yNi0wLjU2NCwxLjI2LTEuMjYxdi0wLjAyNEM2Ny44MTUsNzUuMzc4LDY3LjI1MSw3NC44MTMsNjYuNTU2LDc0LjgxM3oiLz4NCgk8cGF0aCBkPSJNNjYuNTU2LDY3LjE3OWgtMzMuMTFjLTAuNjk2LDAtMS4yNjEsMC41NjQtMS4yNjEsMS4yNnYwLjAyNGMwLDAuNjk2LDAuNTY0LDEuMjYsMS4yNjEsMS4yNmgzMy4xMQ0KCQljMC42OTUsMCwxLjI2LTAuNTY0LDEuMjYtMS4yNnYtMC4wMjRDNjcuODE1LDY3Ljc0Myw2Ny4yNTEsNjcuMTc5LDY2LjU1Niw2Ny4xNzl6Ii8+DQoJPHBhdGggZD0iTTUwLjg0LDQ2LjIzMmMtMi45MjcsMC01LjE4LTIuMTk1LTUuMTgtMi4xOTVsLTIuMDQ4LDIuODRjMCwwLDIuMDc4LDIuMzEsNS44NTMsMi43NXYyLjY5MmgyLjUxNnYtMi42OTINCgkJYzMuNTY5LTAuNDY5LDUuNjQ4LTMuMDEzLDUuNjQ4LTUuOTk5YzAtNi41NTUtOS44Ni01Ljk0LTkuODYtOS40MjJjMC0xLjQ2NSwxLjM3NC0yLjQ4OCwzLjEzLTIuNDg4DQoJCWMyLjYwNCwwLDQuNTk0LDEuODE1LDQuNTk0LDEuODE1bDEuNjM5LTMuMDc0YzAsMC0xLjc1NS0xLjg0NC01LjE1LTIuMTM1di0yLjY5MmgtMi41MTZ2Mi43NTENCgkJYy0zLjI0OCwwLjUyNi01LjUwMiwyLjg2Ni01LjUwMiw1Ljg4YzAsNi4yOTEsOS44OSw1Ljk0LDkuODksOS40NTJDNTMuODUzLDQ1LjQxNSw1Mi40MTgsNDYuMjMyLDUwLjg0LDQ2LjIzMnoiLz4NCgk8cGF0aCBkPSJNNjYuNTU2LDU5LjU0M2gtMzMuMTFjLTAuNjk2LDAtMS4yNjEsMC41NjQtMS4yNjEsMS4yNnYwLjAyNWMwLDAuNjk2LDAuNTY0LDEuMjYsMS4yNjEsMS4yNmgzMy4xMQ0KCQljMC42OTUsMCwxLjI2LTAuNTY0LDEuMjYtMS4yNnYtMC4wMjVDNjcuODE1LDYwLjEwOCw2Ny4yNTEsNTkuNTQzLDY2LjU1Niw1OS41NDN6Ii8+DQo8L2c+DQo8L3N2Zz4NCg==";
        System.out.println("testCode  = " + testCode);
        System.out.println("testCode = " + testCode.equals(svgBase64));
        // 对进入 base64 数据进行解码
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] svgBytes = decoder.decodeBuffer(svgBase64);
            // 准备开始对图片解码
            PNGTranscoder t = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(out);
            t.transcode(input, output);
            out.flush();
            // 将输出留转换成 byte[] 并进行 base64 加密
            byte[] target = out.toByteArray();
            return new BASE64Encoder().encode(target);
        } catch (IOException | TranscoderException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public static String convertSvg2Png(String svgBase64, OutputStream out) {
        // 对进入 base64 数据进行解码
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] svgBytes = decoder.decodeBuffer(svgBase64);
            // 准备开始对图片解码
            PNGTranscoder t = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
            TranscoderOutput output = new TranscoderOutput(out);
            t.transcode(input, output);
            out.flush();
        } catch (IOException | TranscoderException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            File file = new File("/Users/xfduan/Pictures/dollor.svg");
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = IOUtils.toByteArray(inputFile);
            System.out.println("buffer = " + new BASE64Encoder().encode(buffer));
            IOUtils.closeQuietly(inputFile);
            String base64 = new BASE64Encoder().encode(buffer);
            System.out.println("base64 = " + base64);
            base64 = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMDAiIGhlaWdodD0iMjAwIj4KICAgICAgICAgICAgICAgICAgICA8Zm9yZWlnbk9iamVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIj4KICAgICAgICAgICAgICAgICAgICAgIDxkaXYgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWwiPgogICAgICAgICAgICAgICAgICAgICAgICA8ZGl2PgogICAgICAgICAgICAgICAgICBIZWxsbwogICAgICAgICAgICAgICAgICA8aW1nIHNyYz0iaHR0cHM6Ly9zdGF0aWMuaXF5Y2FtcC5jb20vaW1hZ2VzL2ZyYWdtZW50L2ZyZWVfbGltaXRfY2FsbF8xLnBuZz9pbWFnZXNsaW0iPjwvaW1nPgogICAgICAgICAgICAgICAgPC9kaXY+CiAgICAgICAgICAgICAgICAgICAgICA8L2Rpdj4KICAgICAgICAgICAgICAgICAgICA8L2ZvcmVpZ25PYmplY3Q+CiAgICAgICAgICAgICAgICAgIDwvc3ZnPg==";
            // base64 = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjwhLS0gR2VuZXJhdG9yOiBBZG9iZSBJbGx1c3RyYXRvciAxNi4wLjAsIFNWRyBFeHBvcnQgUGx1Zy1JbiAuIFNWRyBWZXJzaW9uOiA2LjAwIEJ1aWxkIDApICAtLT4NCjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+DQo8c3ZnIHZlcnNpb249IjEuMSIgaWQ9IkxheWVyXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4Ig0KCSB3aWR0aD0iMTAwcHgiIGhlaWdodD0iMTAwcHgiIHZpZXdCb3g9IjAgMCAxMDAgMTAwIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCAxMDAgMTAwIiB4bWw6c3BhY2U9InByZXNlcnZlIj4NCjxnPg0KCTxwYXRoIGQ9Ik03MS42MzMsMi4yOEgyOC4zNjdjLTQuMjEyLDAtNy42MzUsMy40MjMtNy42MzUsNy42MzV2ODAuMTY4YzAsNC4yMTIsMy40MjMsNy42MzYsNy42MzUsNy42MzZoNDMuMjY2DQoJCWM0LjIxMywwLDcuNjM1LTMuNDI0LDcuNjM1LTcuNjM2VjkuOTE2Qzc5LjI2OCw1LjcwMyw3NS44NDYsMi4yOCw3MS42MzMsMi4yOHogTTQzLjAwMSw5LjI1NGMwLTEuMDQzLDAuODQxLTEuODg0LDEuODg0LTEuODg0DQoJCWgxMC4yM2MxLjA0NCwwLDEuODg0LDAuODQxLDEuODg0LDEuODg0djAuMDVjMCwxLjA0My0wLjg0LDEuODg0LTEuODg0LDEuODg0aC0xMC4yM2MtMS4wNDMsMC0xLjg4NC0wLjg0LTEuODg0LTEuODg0VjkuMjU0eg0KCQkgTTUwLDk0Ljg1NWMtMi4yNzcsMC00LjEzNi0xLjg1Ny00LjEzNi00LjEzNmMwLTIuMjc3LDEuODU4LTQuMTM2LDQuMTM2LTQuMTM2YzIuMjc4LDAsNC4xMzYsMS44NTgsNC4xMzYsNC4xMzYNCgkJQzU0LjEzNiw5Mi45OTgsNTIuMjc4LDk0Ljg1NSw1MCw5NC44NTV6IE03Mi45MDUsODIuNDQ5YzAsMC42OTktMC41NzIsMS4yNzItMS4yNzIsMS4yNzJIMjguMzY3Yy0wLjcsMC0xLjI3Mi0wLjU3My0xLjI3Mi0xLjI3Mg0KCQlWMTcuNTUxYzAtMC43LDAuNTcyLTEuMjcyLDEuMjcyLTEuMjcyaDQzLjI2NmMwLjcsMCwxLjI3MiwwLjU3MywxLjI3MiwxLjI3MlY4Mi40NDl6Ii8+DQoJPHBhdGggZD0iTTY2LjU1Niw3NC44MTNoLTMzLjExYy0wLjY5NiwwLTEuMjYxLDAuNTY0LTEuMjYxLDEuMjYxdjAuMDI0YzAsMC42OTYsMC41NjQsMS4yNjEsMS4yNjEsMS4yNjFoMzMuMTENCgkJYzAuNjk1LDAsMS4yNi0wLjU2NCwxLjI2LTEuMjYxdi0wLjAyNEM2Ny44MTUsNzUuMzc4LDY3LjI1MSw3NC44MTMsNjYuNTU2LDc0LjgxM3oiLz4NCgk8cGF0aCBkPSJNNjYuNTU2LDY3LjE3OWgtMzMuMTFjLTAuNjk2LDAtMS4yNjEsMC41NjQtMS4yNjEsMS4yNnYwLjAyNGMwLDAuNjk2LDAuNTY0LDEuMjYsMS4yNjEsMS4yNmgzMy4xMQ0KCQljMC42OTUsMCwxLjI2LTAuNTY0LDEuMjYtMS4yNnYtMC4wMjRDNjcuODE1LDY3Ljc0Myw2Ny4yNTEsNjcuMTc5LDY2LjU1Niw2Ny4xNzl6Ii8+DQoJPHBhdGggZD0iTTUwLjg0LDQ2LjIzMmMtMi45MjcsMC01LjE4LTIuMTk1LTUuMTgtMi4xOTVsLTIuMDQ4LDIuODRjMCwwLDIuMDc4LDIuMzEsNS44NTMsMi43NXYyLjY5MmgyLjUxNnYtMi42OTINCgkJYzMuNTY5LTAuNDY5LDUuNjQ4LTMuMDEzLDUuNjQ4LTUuOTk5YzAtNi41NTUtOS44Ni01Ljk0LTkuODYtOS40MjJjMC0xLjQ2NSwxLjM3NC0yLjQ4OCwzLjEzLTIuNDg4DQoJCWMyLjYwNCwwLDQuNTk0LDEuODE1LDQuNTk0LDEuODE1bDEuNjM5LTMuMDc0YzAsMC0xLjc1NS0xLjg0NC01LjE1LTIuMTM1di0yLjY5MmgtMi41MTZ2Mi43NTENCgkJYy0zLjI0OCwwLjUyNi01LjUwMiwyLjg2Ni01LjUwMiw1Ljg4YzAsNi4yOTEsOS44OSw1Ljk0LDkuODksOS40NTJDNTMuODUzLDQ1LjQxNSw1Mi40MTgsNDYuMjMyLDUwLjg0LDQ2LjIzMnoiLz4NCgk8cGF0aCBkPSJNNjYuNTU2LDU5LjU0M2gtMzMuMTFjLTAuNjk2LDAtMS4yNjEsMC41NjQtMS4yNjEsMS4yNnYwLjAyNWMwLDAuNjk2LDAuNTY0LDEuMjYsMS4yNjEsMS4yNmgzMy4xMQ0KCQljMC42OTUsMCwxLjI2LTAuNTY0LDEuMjYtMS4yNnYtMC4wMjVDNjcuODE1LDYwLjEwOCw2Ny4yNTEsNTkuNTQzLDY2LjU1Niw1OS41NDN6Ii8+DQo8L2c+DQo8L3N2Zz4NCg==";
            OutputStream out = new FileOutputStream(new File("/Users/xfduan/Pictures/bbb.png"));
            ImageUtils.convertSvg2Png(base64, out);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    // public static void main(String[] args) {
    //     try {
    //         long l1 = System.currentTimeMillis();
    //
    //         //获取图片的流
    //
    //         BufferedImage big = getBufferedImageByUrl("http://wx.qlogo.cn/mmopen/Q3auHgzwzM6LrkJRYApibxYsAEYm2CmS7JZwX09AmHsP0X2VJQSpibHyoHsQKNcvqf1hzFgJr6l40vyhH7KtGWupGmgKHwFibbiaOOS0qKuvjsQ/64");
    //         BufferedImage small = getBufferedImageByUrl("http://static.iqycamp.com/images/logo.png");
    //         long l2 = System.currentTimeMillis();
    //         System.out.println(l2 - l1);
    //         // 处理图片将其压缩成正方形的小图
    //         // BufferedImage convertImage = writeText(big, 22, 22);
    //         // 裁剪成圆形 （传入的图像必须是正方形的 才会 圆形 如果是长方形的比例则会变成椭圆的）
    //         big = convertCircular(big);
    //         // big = overlapImage(big, small, 100, 100);
    //         //生成的图片位置
    //         String imagePath = "/Users/xfduan/Pictures/a.png";
    //         ImageIO.write(small, imagePath.substring(imagePath.lastIndexOf(".") + 1), new File(imagePath));
    //
    //         long l3 = System.currentTimeMillis();
    //         System.out.println(l3 - l2);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}


