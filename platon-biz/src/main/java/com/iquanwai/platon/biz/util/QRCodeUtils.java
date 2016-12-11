package com.iquanwai.platon.biz.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class QRCodeUtils {
    static Logger logger = LoggerFactory.getLogger(QRCodeUtils.class);

    private static final int HEIGHT = 300;
    private static final int WIDTH = 300;
    private static final int IMAGE_WIDTH = 70;
    private static final int IMAGE_HEIGHT = 70;
    private static final int IMAGE_HALF_WIDTH = IMAGE_WIDTH / 2;
    private static final int FRAME_WIDTH = 2;

    /**
     * 给二维码图片添加Logo
     *
     * @param qrPic
     * @param logoPic
     */
    public static Image addLogoToQRCode(InputStream qrPic, InputStream logoPic) {
        LogoConfig logoConfig = new LogoConfig();
        try {
            if (qrPic == null || logoPic == null) {
                logger.error("invalid logo or qr Input stream");
                return null;
            }

            /**
             * 读取二维码图片，并构建绘图对象
             */
            BufferedImage image = ImageIO.read(qrPic);
            if (image == null) {
                return null;
            }

            /**
             * 如果该图片不是彩色的，转换成彩色模式
             */
            if (!image.getColorModel().getColorSpace().isCS_sRGB()) {
                BufferedImage colorPic = new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                ColorSpace rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                ColorConvertOp colorConvertOp = new ColorConvertOp(rgb, null);
                colorConvertOp.filter(image, colorPic);
                image = colorPic;
            }

            try {
                if (qrPic != null) {
                    qrPic.close();
                }
            } catch (IOException ex) {
                logger.error("error occurs when close qr code inputStream", ex);
                return null;
            }

//            image = zoom(image, 100, 100);
            Graphics2D g = image.createGraphics();

            /**
             * 读取Logo图片
             */
            BufferedImage logo = ImageIO.read(logoPic);
            try {
                if (logoPic != null) {
                    logoPic.close();
                }
            } catch (IOException ex) {
                logger.error("error occurs when close logoPic inputStream", ex);
                return null;
            }
            /**
             * 设置logo的大小,设置为二维码图片的20%
             */
            int widthLogo = logo.getWidth(null) > image.getWidth() * 2 / 10 ? (image.getWidth() * 2 / 10) : logo.getWidth(null),
                    heightLogo = logo.getHeight(null) > image.getHeight() * 2 / 10 ? (image.getHeight() * 2 / 10) : logo.getWidth(null);

            // 计算图片放置位置
            /**
             * logo放在中心
             */
            int x = (image.getWidth() - widthLogo) / 2;
            int y = (image.getHeight() - heightLogo) / 2;
            /**
             * logo放在右下角
             */
//            int x = (image.getWidth() - widthLogo);
//            int y = (image.getHeight() - heightLogo);
            //开始绘制图片
            g.drawImage(logo, x, y, widthLogo, heightLogo, null);
//            g.drawRoundRect(x, y, widthLogo, heightLogo, 15, 15);
            g.setStroke(new BasicStroke(logoConfig.getBorder()));
            g.setColor(logoConfig.getBorderColor());
            g.drawRect(x, y, widthLogo, heightLogo);

            g.dispose();
            logo.flush();
            image.flush();
            return image;
        } catch (Exception e) {
            logger.error("error occurs when adding log to qrCode", e);
            return null;
        }
    }

    /**
     * 缩放图片
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage zoom(BufferedImage bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }
        if (width < 1 || height < 1) {
            return null;
        }
        float oldWidth = bitmap.getWidth(null);
        float oldHeight = bitmap.getHeight(null);
        float xRatio = oldWidth / width;
        float yRatio = oldHeight / height;

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int x = 0, y = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                x = (int) (i * xRatio);
                if (x > oldWidth) {
                    x = (int) oldWidth;
                }
                y = (int) (j * yRatio);
                if (y > oldHeight) {
                    y = (int) oldHeight;
                }
                result.setRGB(i, j, bitmap.getRGB(x, y));
            }
        }
        return result;
    }



    public static BufferedImage genQRCodeWithLogo(String content, InputStream srcLogoImage) throws WriterException,
            IOException {
        // 读取源图像
        BufferedImage scaleImage = scale(srcLogoImage, IMAGE_WIDTH,
                IMAGE_HEIGHT, false);

        int[][] srcPixels = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
        for (int i = 0; i < scaleImage.getWidth(); i++) {
            for (int j = 0; j < scaleImage.getHeight(); j++) {
                srcPixels[i][j] = scaleImage.getRGB(i, j);
            }
        }
        java.util.Hashtable hint = new java.util.Hashtable();
        hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 生成二维码
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,
                WIDTH, HEIGHT, hint);
        int[] pixels = new int[WIDTH * HEIGHT];

        // 二维矩阵转为一维像素数组
        int halfW = matrix.getWidth() / 2;
        int halfH = matrix.getHeight() / 2;

        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                // 读取图片
                if (x > halfW - IMAGE_HALF_WIDTH
                        && x < halfW + IMAGE_HALF_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH
                        && y < halfH + IMAGE_HALF_WIDTH) {
                    pixels[y * WIDTH + x] = srcPixels[x - halfW
                            + IMAGE_HALF_WIDTH][y - halfH + IMAGE_HALF_WIDTH];
                }
                // 在图片四周形成边框
                else if ((x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && x < halfW - IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
                        + IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW + IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
                        + IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
                        - IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH + IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
                        + IMAGE_HALF_WIDTH + FRAME_WIDTH)) {
                    pixels[y * WIDTH + x] = 0xfffffff;
                } else {
                    // 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
                    pixels[y * WIDTH + x] = matrix.get(x, y) ? 0xff000000
                            : 0xfffffff;
                }
            }
        }

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        image.getRaster().setDataElements(0, 0, WIDTH, HEIGHT, pixels);

        return image;
    }

    public static BufferedImage genQRCode(String content, int width, int height) {
        java.util.Hashtable hint = new java.util.Hashtable();
        hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 生成二维码
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,
                    width, height, hint);
        } catch (WriterException e) {
            return null;
        }

        matrix = deleteWhite(matrix);
        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xff000000 : 0xfffffff);
            }
        }
        return image;
    }

    private static BitMatrix deleteWhite(BitMatrix matrix){
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    private static BufferedImage scale(InputStream srcImageFile, int height,
                                       int width, boolean hasFiller) throws IOException {
        double ratio = 0.0; // 缩放比例
//        File file = new File(srcImageFile);
        BufferedImage srcImage = ImageIO.read(srcImageFile);
        Image destImage = srcImage.getScaledInstance(width, height,
                BufferedImage.SCALE_SMOOTH);
        // 计算比例
        if ((srcImage.getHeight() > height) || (srcImage.getWidth() > width)) {
            if (srcImage.getHeight() > srcImage.getWidth()) {
                ratio = (new Integer(height)).doubleValue()
                        / srcImage.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue()
                        / srcImage.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform
                    .getScaleInstance(ratio, ratio), null);
            destImage = op.filter(srcImage, null);
        }
        if (hasFiller) {// 补白
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = image.createGraphics();
            graphic.setColor(Color.white);
            graphic.fillRect(0, 0, width, height);
            if (width == destImage.getWidth(null))
                graphic.drawImage(destImage, 0, (height - destImage
                                .getHeight(null)) / 2, destImage.getWidth(null),
                        destImage.getHeight(null), Color.white, null);
            else
                graphic.drawImage(destImage,
                        (width - destImage.getWidth(null)) / 2, 0, destImage
                                .getWidth(null), destImage.getHeight(null),
                        Color.white, null);
            graphic.dispose();
            destImage = image;
        }
        return (BufferedImage) destImage;
    }

    public static class LogoConfig {
        // logo默认边框颜色
        public final Color DEFAULT_BORDERCOLOR = Color.WHITE;
        // logo默认边框宽度
        public final int DEFAULT_BORDER = 2;
        // logo大小默认为照片的1/5
        public final int DEFAULT_LOGOPART = 5;

        private final int border = DEFAULT_BORDER;
        private Color borderColor;
        private int logoPart;


        public LogoConfig() {
        }

        public LogoConfig(Color borderColor, int logoPart) {
            this.borderColor = borderColor;
            this.logoPart = logoPart;
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public int getBorder() {
            return border;
        }

        public int getLogoPart() {
            return logoPart;
        }
    }

    public static void image2FS(Image image, String path){
        try {
            // TODO:改成ftp
            ImageIO.write((RenderedImage) image, "jpg", new File(path));
        } catch (IOException e) {
//            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}
