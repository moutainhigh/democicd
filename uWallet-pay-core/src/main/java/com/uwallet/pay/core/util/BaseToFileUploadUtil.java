package com.uwallet.pay.core.util;

import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 *
 * @description: base64图片上传工具类
 * @author: Rainc
 * @date: Created in 2019-12-25 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 */
public class BaseToFileUploadUtil {

    /**
     * base64字符串转化成图片
     * @param imgStr
     * @param path
     * @return
     */
    public static String GenerateImage(String imgStr, String path) {
        // 创建目录
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + ".jpg";

        // 对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) {
            return "";
        }

        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(path + fileName);
            out.write(b);
            out.flush();
            out.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
