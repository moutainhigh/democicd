package com.uwallet.pay.core.util;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * author: baixinyue
 * createDate: 2019/12/06
 * description: 二维码生成工具类
 */
@Slf4j
public class QRCodeUtil {

    //二位码图片宽度
    private static final int width = 300;

    //二维码图片高度
    private static final int height = 300;

    //默认输出二维码图片格式
    private static final String format = "jpg";

    //文字高度
    private static final int wordHeight = 340;

    /**
     * 生成二维码参数
     */
    private static final Map<EncodeHintType, Object> hints = new HashMap<>();

    //默认logo宽度
    private static final int logo_width = 60;

    //默认logo高度
    private static final int logo_height = 60;

    static {
        //字符编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错等级L/M/Q/H其中L为最低，H为最高
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //二维码与图片边距
        hints.put(EncodeHintType.MARGIN, 2);
    }

    /**
     * 绘制二维码，如果传入logoPath则生成带logo二维码，没有则默认生成普通二维码
     *
     * @param content
     * @param logoPath
     * @return
     * @throws Exception
     */
    public static BufferedImage createQRCode(String content, String logoPath) throws Exception {
        BufferedImage image = drawQR(content, width, height);
        if (StringUtils.isNotEmpty(logoPath)) {
            image = insertLogo(image, logoPath);
        }

        return image;
    }

    /**
     * 指定二维码长款，绘制二维码，如果传入logoPath则生成带logo二维码，没有则默认生成普通二维码
     *
     * @param content
     * @param imgWidth
     * @param imgHeight
     * @param logoPath
     * @return
     * @throws Exception
     */
    public static BufferedImage createQRCode(String content, int imgWidth, int imgHeight, String logoPath) throws Exception {
        BufferedImage image = drawQR(content, imgWidth, imgHeight);
        if (StringUtils.isNotEmpty(logoPath)) {
            image = insertLogo(image, logoPath);
        }

        return image;
    }

    public static JSONObject writeFile(BufferedImage image, String imageName, String path) throws Exception {
        String fullPath = path + imageName + "." + format;
        ImageIO.write(image, format, new File(fullPath));
        JSONObject object = new JSONObject();
        object.put("code", imageName);
        object.put("path", imageName + "." + format);
        return object;
    }

    /**
     * 把带logo的二维码下面加上文字
     *
     * @param image
     * @param words
     * @return
     */
    public static BufferedImage insertWords(BufferedImage image, String words) {
        //创建一个带透明色的BufferedImage对象
        BufferedImage outImage = new BufferedImage(image.getWidth(), wordHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D outg = outImage.createGraphics();
        setGraphics2D(outg);

        // 画二维码到新的面板
        outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        // 画文字到新的面板
        outg.setColor(Color.BLACK);
        // 字体、字型、字号
        outg.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        //文字长度
        int strWidth = outg.getFontMetrics().stringWidth(words);
        //总长度减去文字长度的一半  （居中显示）
        int wordStartX = (width - strWidth) / 2;
        int wordStartY = height + 10;
        // 画文字
        outg.drawString(words, wordStartX, wordStartY);
        outg.dispose();
        outImage.flush();
        return outImage;
    }

    /**
     * 绘制二维码
     *
     * @param content   二维码存储信息
     * @param imgWidth  二维码图片宽度
     * @param imgHeight 二维码图片高度
     * @return
     * @throws WriterException
     */
    private static BufferedImage drawQR(String content, int imgWidth, int imgHeight) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, imgWidth, imgHeight, hints);
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_BGR);
        //绘制二维码
        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000
                        : 0xFFFFFFFF);
            }
        }
        return image;
    }

    /**
     * 为二维码插入logo
     *
     * @param image
     * @param logoPath
     * @return
     * @throws IOException
     */
    private static BufferedImage insertLogo(BufferedImage image, String logoPath) throws IOException {
        //读取logo
        BufferedImage logo = ImageIO.read(new File(logoPath));
        //压缩logo
        Image finalLogo;
        int templateWidth = logo.getWidth();
        int templateHeight = logo.getHeight();
        if (templateWidth > logo_width) {
            templateWidth = logo_width;
        }
        if (templateHeight > logo_height) {
            templateHeight = logo_height;
        }
        Image scaledInstance = logo.getScaledInstance(templateWidth, templateHeight, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics graphics = tag.getGraphics();
        graphics.drawImage(scaledInstance, 0, 0, null);
        graphics.dispose();
        finalLogo = scaledInstance;
        //获取二维码长宽
        Graphics2D graphics2D = image.createGraphics();
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
        //将logo定位到中间位置
        int logoWidth = finalLogo.getWidth(null);
        int logoHeight = finalLogo.getHeight(null);
        int x = (imgWidth - logoWidth) / 2;
        int y = (imgHeight - logoHeight) / 2;
        graphics2D.drawImage(finalLogo, x, y, logoWidth, logoHeight, null);
        Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 6, 6);
        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.draw(shape);
        graphics2D.dispose();
        return image;
    }

    /**
     * 设置 Graphics2D 属性  （抗锯齿）
     *
     * @param graphics2D
     */
    private static void setGraphics2D(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        Stroke s = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        graphics2D.setStroke(s);
    }


    /**
     * 生成base64二维码信息
     *
     * @param contents
     * @param width
     * @param height
     * @return
     */
    public static String createQrCodeBase64(String contents, int width, int height) {
        String binary = null;
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    contents, BarcodeFormat.QR_CODE, width, height, hints);
            //读取文件转换为字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedImage image = toBufferedImage(bitMatrix);
            //转换成png格式的IO流
            ImageIO.write(image, "png", out);
            byte[] bytes = out.toByteArray();

            //将字节数组转为二进制
            BASE64Encoder encoder = new BASE64Encoder();
            binary = encoder.encodeBuffer(bytes).trim();
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return binary;
    }


    /**
     * Img流数据处理
     *
     * @param matrix
     * @return
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }


    /**
     * 获取图片base64字符串
     *
     * @param path
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/4/25 9:36
     */
    public static String getImgBase64Str(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }


        URL url = null;

        InputStream is = null;

        ByteArrayOutputStream outStream = null;

        HttpURLConnection httpUrl = null;

        /*InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(data);*/
        return "";
    }


    /**
     * 获取图片base64字符串
     *
     * @param imgUrl
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/4/25 9:36
     */
    public static String imageUrlToBase64(String imgUrl) {
        if (StringUtils.isBlank(imgUrl)) {
            return "";
        }
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            is = httpUrl.getInputStream();

            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            return Base64.encodeBase64String(outStream.toByteArray());
        } catch (IOException e) {
            log.error("图片url转base64 报错", e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("图片url转base64 关闭输入流报错", e.getMessage());
                }
            }

            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    log.error("图片url转base64 关闭输出流报错", e.getMessage());
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
        return "";
    }


}
