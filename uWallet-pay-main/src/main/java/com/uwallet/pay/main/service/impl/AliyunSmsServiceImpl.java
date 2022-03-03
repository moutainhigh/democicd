package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.service.AliyunSmsService;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.util.TestEnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: liming
 * @Date: 2019/10/29 09:43
 * @Description: 阿里云短信服务
 */
@Slf4j
@Service
public class AliyunSmsServiceImpl implements AliyunSmsService {

    @Autowired
    private StaticDataService staticDataService;

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessSecret}")
    private String accessSecret;

    @Async("taskExecutor")
    @Override
    public void sendChinaSms(String phone, String modelCode, JSONObject templateParams) throws BizException {
        // todo 测试环境短信挡板
        if(TestEnvUtil.isTestEnv()){
            return;
        }
        //todo
        if (1==1){
            return;
        }
        String signName = StaticDataEnum.U_WALLET.getMessage();
        Map<String, String> params = new HashMap<>(4);
        params.put("To", phone);
        params.put("From", signName);
        params.put("TemplateCode", modelCode);
        params.put("TemplateParam", templateParams.toJSONString());

        log.info("aliyun send china message, phone{} , msg:{}",phone, templateParams.toJSONString());
        JSONObject jsonObject = this.postAPI("SendMessageWithTemplate", params);
        if (jsonObject == null) {
            throw new BizException("SMS SDK call failed");
        }
        log.info("aliyun return data:{}", jsonObject);
        Object code = jsonObject.get("ResponseCode");
        if (!"OK".equals(code.toString())) {
            log.info("挡板");
//            throw new BizException("SMS SDK call failed");
        }
    }

    @Override
    public void sendInternationalSms(String phone, String message) throws BizException {
        Map<String, String> params = new HashMap<>(4);
        String signName = StaticDataEnum.U_WALLET.getMessage();
        params.put("To", phone);
        params.put("Message", message);
        params.put("From", signName);
        log.info("aliyun send message, phone{} , msg:{}",phone, params);
        // todo 测试环境短信挡板
        Map<String, Object> map = new HashMap<>(1);
        map.put("code", "sendSmsTest");
        StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(map);
        if(null != staticDataDTO && null != staticDataDTO.getId() && StringUtils.isNotBlank(staticDataDTO.getValue())){
            return;
        }
        JSONObject jsonObject = this.postAPI("SendMessageToGlobe", params);
        log.info("aliyun return data, msg:{}", jsonObject.toJSONString());
        if (jsonObject == null) {
            throw new BizException("SMS SDK call failed");
        }
        Object code = jsonObject.get("ResponseCode");
        if (!"OK".equals(code.toString())) {
            log.info("挡板");
//            throw new BizException("SMS SDK call failed");
        }

        /*//初始化acsClient,<accessKeyId>和"<accessSecret>"在短信控制台查询即可。
        DefaultProfile profile = DefaultProfile.getProfile("ap-southeast-1", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        //域名，请勿修改
        request.setDomain("sms-intl.ap-southeast-1.aliyuncs.com");
        //API版本号，请勿修改
        request.setVersion("2018-05-01");
        //API名称
        request.setAction("SendMessageToGlobe");
        //接收号码，格式为：国际码+号码，必填
        request.putQueryParameter("To", "86" + phone);
        //发送方senderId，选填
        //request.putQueryParameter("From", "1234567890");
        //短信内容，必填
        request.putQueryParameter("Message", message);
        String result = null;
        try {
            CommonResponse response = client.getCommonResponse(request);
            result =  response.getData();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return result;*/
    }


    @Override
    public boolean sendInternationalSmsV2(String phone, String message){
        HashMap<String, String> params = Maps.newHashMapWithExpectedSize(4);
        String signName = StaticDataEnum.U_WALLET.getMessage();
        params.put("To", phone);
        params.put("Message", message);
        params.put("From", signName);
        // todo 测试环境短信挡板
        Map<String, Object> map = new HashMap<>(1);
        map.put("code", "sendSmsTest");
        StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(map);
        if(null != staticDataDTO && null != staticDataDTO.getId() && StringUtils.isNotBlank(staticDataDTO.getValue())){
            return true;
        }

        try {
            log.info("阿里云发送国际短信, 参数信息，phone{} , msg:{}",phone, params);
            JSONObject resultJsonObj = this.postAPI("SendMessageToGlobe", params);
            log.info("aliyun return data, msg:{}", resultJsonObj.toJSONString());
            if (null == resultJsonObj || StringUtils.isBlank(resultJsonObj.getString("ResponseCode"))) {
                log.info("阿里云发送国际短信 返回信息为空， phone: {}", phone);
                return false;
            }

            if (!"OK".equals(resultJsonObj.getString("ResponseCode"))) {
                log.info("阿里云发送国际短信 返回为失败， phone: {}", phone);
                return false;
            }

        }catch (Exception e){
            log.info("阿里云发送国际短信 异常， phone: {},e :{}", phone, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 远程接口调用
     *
     * @param action 调用的方法名称
     * @param params 参数
     * @return
     */
    private JSONObject postAPI(String action, Map<String, String> params) {
        //初始化acsClient,<accessKeyId>和"<accessSecret>"在短信控制台查询即可。
        DefaultProfile profile = DefaultProfile.getProfile("ap-southeast-1", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        //域名，请勿修改
        request.setDomain("sms-intl.ap-southeast-1.aliyuncs.com");
        //API版本号，请勿修改
        request.setVersion("2018-05-01");
        request.setAction(action);
        params.entrySet().forEach(a -> request.putQueryParameter(a.getKey(), a.getValue()));
        JSONObject data = null;
        try {
            CommonResponse response = client.getCommonResponse(request);
            data = JSON.parseObject(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return data;
    }

}
