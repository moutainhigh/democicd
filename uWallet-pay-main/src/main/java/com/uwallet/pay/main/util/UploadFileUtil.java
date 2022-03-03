package com.uwallet.pay.main.util;

import com.uwallet.pay.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * @description：文件上传
 * @date：2019/5/24
 * @author：Rainc
 */
@Slf4j
@Configuration
public class UploadFileUtil {

    /**
     * 图片类型
     */
    private static String imgSuffix;

    /**
     * 文件类型
     */
    private static String fileSuffix;

    @Value("${spring.imgSuffix}")
    private void setImgSuffix(String imgSuffix) {
        UploadFileUtil.imgSuffix = imgSuffix;
    }

    @Value("${spring.fileSuffix}")
    private void setFileSuffix(String fileSuffix) {
        UploadFileUtil.fileSuffix = fileSuffix;
    }

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    public static String uploadFile(MultipartFile file, String path, HttpServletRequest request) throws BizException {
        // 创建目录
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        if (file.isEmpty()) {
            log.info(fileName + "文件内容为空");
        }
        if (checkFile(fileName)) {
            fileName = System.currentTimeMillis() + "_" + fileName;
            File dest = new File(path + fileName);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                throw new BizException(fileName + I18nUtils.get("file.upload.failed", getLang(request)));
            }
        } else {
            throw new BizException(fileName + I18nUtils.get("illegal.file", getLang(request)));
        }
        return fileName;
    }

    /**
     * 图片上传
     *
     * @param file
     * @return
     */
    public static String uploadImg(MultipartFile file, String path, HttpServletRequest request) throws BizException {
        // 创建目录
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        if (file.isEmpty()) {
            log.info(fileName + "文件内容为空");
        }
        if (checkImg(fileName)) {
            fileName = System.currentTimeMillis() + "_" + fileName;
            File dest = new File(path + fileName);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                throw new BizException(fileName + I18nUtils.get("img.upload.failed", getLang(request)));
            }
        } else {
            throw new BizException(fileName + I18nUtils.get("illegal.img", getLang(request)));
        }
        return fileName;
    }

    /**
     * 校验图片类型
     *
     * @param fileName
     * @return
     */
    public static boolean checkImg(String fileName) {
        boolean flag = false;
        // 获取文件后缀
        String sfx = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (imgSuffix.contains(sfx.trim().toLowerCase())) {
            flag = true;
        }
        return flag;
    }

    /**
     *  校验banner文件类型
     * @author zhangzeyuan
     * @date 2021/5/7 14:49
     * @param fileName
     * @return boolean
     */
    public static boolean checkBannerImgFileTypeNew(String fileName) {
        String bannerImgSuffix = "jpg,jpeg,png,bmp,heic,gif";
        boolean flag = false;
        // 获取文件后缀
        String sfx = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (bannerImgSuffix.contains(sfx.trim().toLowerCase())) {
            flag = true;
        }
        return flag;
    }

    /**
     * 校验文件类型
     *
     * @param fileName
     * @return
     */
    public static boolean checkFile(String fileName) {
        boolean flag = false;
        // 获取文件后缀
        String sfx = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (fileSuffix.contains(sfx.trim().toLowerCase())) {
            flag = true;
        }
        return flag;
    }

    /**
     * 将数据写入文件
     *
     * @param jsonStr
     * @throws IOException
     */
    public static void writeDataToFile(String jsonStr, String fileName, String filePath) {
        try {
            // 文件目录
            Path rootLocation = Paths.get(filePath);
            if (Files.notExists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            Path path = rootLocation.resolve(fileName + ".txt");
            byte[] strToBytes = jsonStr.getBytes();
            Files.write(path, strToBytes);
        } catch (IOException e) {
            log.error("writeDataToFile, 文件写入异常");
        }
    }

    /**
     * 读取磁盘文件
     *
     * @param path 文件  如：E:\file\1571299177220_creditorWatch_creditScore.txt
     * @return
     * @throws IOException
     */
    public static String readTxt(String path) {
        StringBuffer content = new StringBuffer();
        try {
            File file = new File(path);
            InputStreamReader in = new InputStreamReader(new FileInputStream(file),"UTF-8");
            BufferedReader br = new BufferedReader(in);
            String s;
            while ((s = br.readLine()) != null) {
                content = content.append(s);
            }
        } catch (IOException e) {
            log.error("readTxt, 读取磁盘文件异常");
        }
        return content.toString();
    }

    /**
     * 获取当前语言，默认保持英文
     * @author faker
     * @param request
     * @return
     */
    public static Locale getLang(HttpServletRequest request) {
        Locale lang = Locale.US;
        // 获取当前语言
        String headerLang = request.getHeader("lang");
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.isNotEmpty(headerLang) && "zh-CN".equals(headerLang)) {
            lang = Locale.SIMPLIFIED_CHINESE;
        }
        return lang;
    }
}
