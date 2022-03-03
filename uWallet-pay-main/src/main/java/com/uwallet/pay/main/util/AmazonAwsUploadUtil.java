package com.uwallet.pay.main.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.SnowflakeUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * 亚马逊-amazon aws 澳大利亚区文件上传工具类
 * 上传文件至服务器，返回文件路径中的key
 * 通过-->>上传地址/BUCKET_NAME/key/文件名.后缀 访问上传内容
 * 如果BUCKET_NAME为“”则没有该层级目录 变为->上传地址/key/文件名.后缀
 *
 * @Author shao
 */
@Slf4j
@Configuration
public class AmazonAwsUploadUtil {
    /**
     * bucket名称
     */
    private final static String BUCKET_NAME = "";
    /**
     * 密钥Id
     */
    private final static String ACCESS_KEY_ID = "AKIAS7GMWT7BE4IOGA6I";
    /**
     * 密钥
     */
    private final static String SECRET_ACCESS_KEY = "sE48nkpujZnzaM0F4wFNopJA5DPPyf4WfRCX036O";
    /**
     * 区域代号,澳大利亚区
     */
    private final static String REGION = "ap-southeast-2";
    /**
     * 上传链接地址
     */
    private final static String END_POINT = "https://uwallet.s3-ap-southeast-2.amazonaws.com/";
    /**
     * 初始化创建AmazonS3对象
     */
    private static final AwsClientBuilder.EndpointConfiguration END_POINT_CONFIG = new AwsClientBuilder.EndpointConfiguration(END_POINT, REGION);

    private static final AWSCredentials AWS_CREDENTIALS = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY);

    private static final AmazonS3 S_3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
            .withEndpointConfiguration(END_POINT_CONFIG)
            .enablePathStyleAccess().build();

    /**
     * 【测试服务器】
     * 配置文件信息当前服务器类型：
     */
    private static String type;

    /**
     * 【测试服务器】本地上传路径
     */
    private static String basePath;

    @Value("${spring.basePath}")
    private void setBasePath(String basePath) {
        AmazonAwsUploadUtil.basePath = basePath;
    }

    @Value("${server.type}")
    private void setType(String type) {
        AmazonAwsUploadUtil.type = type;
    }

    /**
     * 图片上传限制大小 2MB 单位字节,该尺寸以下不压缩 26m = 26106429
     */
    public static final int LIMIT_IMG_SIZE = 2097152;

    /**
     * //按比例压缩
     * Thumbnails.of(image.getInputStream()).scale(0.7f).outputQuality(0.25f).toFile(file);
     * //按尺寸压缩
     * Thumbnails.of(image.getInputStream()).size(100,100).keepAspectRatio(false).toFile(file);
     *
     * @param img
     * @return
     */
    public static MultipartFile compressImg(MultipartFile img) {
        String filename = img.getOriginalFilename();
        //文件名非空 且 文件大小超过限制
        if (StringUtils.isNotBlank(filename) && img.getSize() > LIMIT_IMG_SIZE) {
            long startTime = System.currentTimeMillis();
            String imageSuffix = filename.substring(filename.lastIndexOf(".") + 1);
            if (imageSuffix.equalsIgnoreCase("jpg") || imageSuffix.equalsIgnoreCase("png") || imageSuffix.equalsIgnoreCase("jpeg")) {
                try {
                    log.info("执行图片压缩,图片原始大小:{}",img.getSize());
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Thumbnails.of(img.getInputStream())
                            .scale(0.7f) //图片比例缩放
                            .outputQuality(0.7f) //输出质量
                            .toOutputStream(outputStream);
                    byte[] bytes = outputStream.toByteArray();
                    InputStream inputStream = new ByteArrayInputStream(bytes);
                    MockMultipartFile multipartFile = new MockMultipartFile(img.getOriginalFilename(), img.getOriginalFilename(), img.getContentType(), inputStream);
                    System.out.println(multipartFile.getSize());
                    //保存图片到本地 查看质量
                    //AmazonAwsUploadUtil.saveFile(multipartFile.getBytes(), "/Users/aarons/Desktop/illion", filename);
                    log.info("执行图片压缩,图片原始压缩后大小:{},耗时(毫秒):{}",multipartFile.getSize(),System.currentTimeMillis()-startTime);
                    return multipartFile;
                } catch (Exception e) {
                    log.error("图片压缩失败,将返回原文件,imgName:{}", img.getName());
                    return img;
                }
            }
        }
        return img;
    }
    /**
     * 根据byte数组，生成文件 ==>测试预览使用,生产可删除
     * filePath  文件路径
     * fileName  文件名称（需要带后缀，如*.jpg、*.java、*.xml）
     */
    public static void saveFile(byte[] bfile, String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists() && !dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件上传方法
     *
     * @param file 需要上传的文件
     * @param key  上传文件的key 拼接规则请参考开头作者处
     * @return 返回key
     * @throws BizException
     */
    public static String upload(MultipartFile file, String key) throws BizException {
        long startTime = System.currentTimeMillis();
        if (file.isEmpty()) {
            throw new BizException("File not found!");
        }
        if (!StringUtils.isNotBlank(key)) {
            throw new BizException("Key can not be null!");
        }
        //如果是图片文件 则压缩文件 (0.7f), 不是图片/图片小于2M 则返回原文件
        file = compressImg(file);
        
        if ("test".equals(type) || "dev".equals(type)) {
            BufferedOutputStream bos = null;
            String path = basePath + key.substring(0, key.lastIndexOf("/") + 1);
            String fileName = key.substring(key.lastIndexOf("/") + 1);
            File uploadFile = new File(path, fileName);
            File filePathExitsTest = new File(path);
            if (!filePathExitsTest.exists()) {
                // 多级文件夹目录
                filePathExitsTest.mkdirs();
            }
            try {
                InputStream inputStream = file.getInputStream();
                bos = new BufferedOutputStream(new FileOutputStream(uploadFile));
                byte[] bytes = new byte[2048];
                while (inputStream.read(bytes) != -1) {
                    bos.write(bytes);
                    bos.flush();
                }
            } catch (IllegalStateException | IOException e) {
                log.error("文件上传失败: "+ e.getMessage());
                throw new BizException("file.upload.failed");
            } finally {
                try {
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new BizException("file.upload.failed");
                }
            }
            log.info("图片上传执行完毕,耗时(毫秒):{}",System.currentTimeMillis()-startTime);
            return key;
        } else {
            if (S_3 != null) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                // 设置文件类型
                objectMetadata.setContentType(file.getContentType());
                // 文件大小
                objectMetadata.setContentLength(file.getSize());
                try {
                    PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, key, file.getInputStream(), objectMetadata);
                    // 设置可以公开访问
                    putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
                    S_3.putObject(putObjectRequest);
                } catch (Exception e) {
                    throw new BizException(e.getMessage());
                }
                log.info("图片上传执行完毕,耗时(毫秒):{}",System.currentTimeMillis()-startTime);
                return key;
            } else {
                throw new BizException("Failed initialization AmazonS3Client");
            }
        }
    }
}

