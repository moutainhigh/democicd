package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.docusign.esign.api.EnvelopesApi;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.CustomThreadPoolTaskExecutor;
import com.uwallet.pay.main.config.DocuSignConfig;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.docuSignComponent.DocuSignClient;
import com.uwallet.pay.main.docuSignComponent.DocuSignClientCallable;
import com.uwallet.pay.main.service.DocuSignService;
import com.uwallet.pay.main.util.AmazonAwsUploadUtil;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author aaron.S
 * @since 2020/8/18
 */

@Service
@Slf4j
public class DocuSignServiceImpl extends BaseServiceImpl implements DocuSignService {
    @Autowired
    private CustomThreadPoolTaskExecutor threadPool;
    @Autowired
    private DocuSignClient docuSignClient;
    @Value("${spring.docuSignContractPath}")
    private String docuSignContractPath;
    @Value("${server.type}")
    private String serverType;
    private static final String accountId= DocuSignConfig.accountId_test;
    private static final List<String> fileList_full= Arrays.asList("2 LATPAY Merchant Services Facilitation Agreement",
                                                              "LPS_Merchant_Agreement - U Wallet - 20200902 final",
                                                              "Merchant Application Form",
                                                              "9 Merchant Agreement - UPS and Merchant for OmiPay Services",
                                                              "17 Merchant Application Form - U Laypay");
    private static final List<String> fileList_part= Arrays.asList(
                                                              "Merchant Application Form",
                                                              "17 Merchant Application Form - U Laypay");
    private static final List<String> fileList_single= Arrays.asList("Merchant Application Form");
    /**
     * 配置文件 生产环境的 服务器类型标识符
     */
    private static final String SERVER_TYPE_PROD="prod";



    @Override
    public JSONObject genSignUrl(JSONObject jsonObject, HttpServletRequest request) throws Exception {
        //参数校验
        this.checkParams(jsonObject,request);

        // 设置account id-数字格式,非GUID格式 自动切换生产/测试 账号 account id
        String accountId = serverType.equals(SERVER_TYPE_PROD) ? DocuSignConfig.accountId_prod : DocuSignConfig.accountId_test;
        //获取线程池
        ThreadPoolTaskExecutor executor = threadPool.taskExecutor();
        Future future = executor.submit(new DocuSignClientCallable(jsonObject,docuSignClient.getApiClient(),accountId,request));
        //获取执行结果
        return JSONObject.parseObject(future.get().toString());
    }

    @Override
    public JSONObject getDocument(@NotNull JSONObject jsonObject, HttpServletRequest request) throws Exception {
        this.checkGetDocParams(jsonObject,request);
        String envelopId = jsonObject.getString("envelopId");

        List<String> currentFileList;
        int count ;
        /*
        合同类型 0:全部合同、1:非全部
         */
        if (jsonObject.getInteger("contractType").equals(Constant.ZERO)){
            count = fileList_full.size();
            currentFileList = fileList_full;
        }else {
//            currentFileList = fileList_part;
//            count = fileList_part.size();
            currentFileList = fileList_single;
            count = fileList_single.size();
        }
        JSONObject data=new JSONObject( count );
        /*
           官方SDK中获取信封内容的类
         */
        String accountId = serverType.equals(SERVER_TYPE_PROD) ? DocuSignConfig.accountId_prod : DocuSignConfig.accountId_test;
        EnvelopesApi envelopeApi = new EnvelopesApi(docuSignClient.getApiClient());
        for (int i = 0; i < count; i++) {
            int id = i +1;
            byte[] document = envelopeApi.getDocument(accountId, envelopId, (id)+"");
            data.put(currentFileList.get(i),this.uploadContract(envelopId,document,i,currentFileList));
        }
        data.put("envelopId",envelopId);
        return data;
    }

    private Object uploadContract(String envelopId, byte[] document, int i,List<String> currentFileList) throws Exception{
        /*
           将返回的文档转换为-MultipartFile,获取文件后缀
           将文件上传到 <测试: 服务器>/<生产: 亚马逊文件服务器>,返回服务器文件地址
         */
        InputStream inputStream = new ByteArrayInputStream(document);
        MultipartFile file = new MockMultipartFile(envelopId+"-"+currentFileList.get(i)+".pdf","orgFileName.pdf", ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        String uploadPath = AmazonAwsUploadUtil.upload(file, docuSignContractPath+"/"+currentFileList.get(i)+"_"+envelopId+".pdf");
        return uploadPath;
    }

    private void checkGetDocParams(JSONObject jsonObject, HttpServletRequest request) throws BizException{
        if (StringUtils.isBlank(jsonObject.getString("envelopId"))){
            throw new BizException(I18nUtils.get("envelopId",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("contractType"))){
            throw new BizException(I18nUtils.get("contractType",getLang(request)));
        }
    }


    private void checkParams(JSONObject jsonObject,HttpServletRequest request) throws BizException{
        //authorisedTitle
        if (StringUtils.isBlank(jsonObject.getString("authorisedTitle"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("templateId"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("signerName"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("signerEmail"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("clientUserId"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (StringUtils.isBlank(jsonObject.getString("callBackUrl"))){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (jsonObject.getJSONObject("textTabs") == null){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
    }
}
