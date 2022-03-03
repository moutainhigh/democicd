package com.uwallet.pay.main.util;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Lenovo
 */
@Component
@Slf4j
public class BrazeUtil {
    private static String brazeUrl;
    private static String brazeToken;
    private static String appleAppID;
    private static String androidAppID;
    @Value("${Braze.brazeUrl}")
    private void setBrazeUrl(String brazeUrl) {
        BrazeUtil.brazeUrl = brazeUrl;
    }

    @Value("${Braze.brazeToken}")
    private void setBrazeToken(String brazeToken) {
        BrazeUtil.brazeToken = brazeToken;
    }

    @Value("${Braze.androidAppID}")
    private void setAndroidAppID(String androidAppID) {
        BrazeUtil.androidAppID = androidAppID;
    }
    @Value("${Braze.appleAppID}")
    private void setAppleAppID(String appleAppID) {
        BrazeUtil.appleAppID = appleAppID;
    }


    /**
     * @param type 发送类型 BRAZE_MESSAGE_NODE_1
     * @param title 发送标题
     * @param body 发送内容
     * @param extra 自定义参数 extra.put("route",2) extra.put("voice", StaticDataEnum.VOICE_0.getCode());
     * @param ids 发送id列表
     */
    public static void sendPush(Integer type, String title, String body, JSONObject extra, List<String> ids) throws Exception {
        log.info("三方发送消息,类型:{},标题:{},内容:{},参数:{},用户:{}",type,title,body,extra,ids);
        String url=brazeUrl+"/messages/send";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer "+brazeToken);
        JSONObject requestParam=new JSONObject();
        if (ids.size()>1){
            // 批量请求
            requestParam.put("broadcast",true);
        }else {
            // 单个推送
            requestParam.put("broadcast",false);
        }
        // 三方唯一标识ID数组(userID)
        requestParam.put("external_user_ids",ids);
        // 已选择加入 ( opted_in) 的用户、已订阅或已选择加入 ( subscribed) 的用户或所有用户（包括未订阅的用户）发送消息（all）
        requestParam.put("recipient_subscription_state","all");
        // 参数
        Map<String,Object> message = new HashMap<>();
        message.put("extra",extra);
        if (type.intValue()==StaticDataEnum.BRAZE_MESSAGE_NODE_1.getCode()){
            message.put("alert",title);
            message.put("content-available",body);
            // 用于覆盖之前ID
            // message.put("collapse_id","这是消息ID");
            requestParam.put("apple_push",message);
        }else if (type.intValue()==StaticDataEnum.BRAZE_MESSAGE_NODE_2.getCode()){
            message.put("alert",title);
            message.put("title",title);
            // 优先级
            message.put("priority",1);
            message.put("content-available",body);
            // 用于覆盖之前ID
            // message.put("collapse_id","这是消息ID");
            requestParam.put("android_push",message);
        }
        try{
            String s = HttpClientUtils.postByHeader(url, head, requestParam.toJSONString());
            JSONObject jsonObject = JSONObject.parseObject(s);
            String message1 = jsonObject.getString("message");
            if (!message1.equals(StaticDataEnum.BREAZE_REAPONSES_SUCCESS.getMessage())&&!message1.equals(StaticDataEnum.BREAZE_REAPONSES_QUEUED.getMessage())){
                throw new BizException("send failed");
            }
        } catch (Exception e) {
            log.error("请求三方异常;{}",e);
            throw e;
        }
    }
    public static void trackUser(UserDTO userDTO) throws Exception {
        log.info("三方发送同步用户消息,用户:{}",userDTO);
        String url=brazeUrl+"/users/track";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer "+brazeToken);
        JSONObject requestParam=new JSONObject();
        JSONObject user=new JSONObject();
        user.put("external_id",userDTO.getId().toString());
        user.put("first_name",userDTO.getUserFirstName());
        user.put("last_name",userDTO.getUserLastName());
        user.put("email",userDTO.getEmail());
        user.put("phone","+"+userDTO.getPhone());
        user.put("push_subscribe","subscribed");
        String birth = userDTO.getBirth();
        if (StringUtils.isNotBlank(birth)){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date parse = simpleDateFormat.parse(birth);
            SimpleDateFormat simpleDateFormatNew = new SimpleDateFormat("yyyy-MM-dd");
            String format = simpleDateFormatNew.format(parse);
            user.put("dob",format);
        }
        if (userDTO.getSex()!=null){
            switch(userDTO.getSex()){
                case 1:
                    user.put("gender",StaticDataEnum.GENDER_Female.getMessage());
                    break;
                case 2:
                    user.put("gender",StaticDataEnum.GENDER_Male.getMessage());
                default:
                    user.put("gender",StaticDataEnum.GENDER_Prefer.getMessage());
            }
        }
        List<JSONObject> arr=new ArrayList();
        arr.add(user);
        requestParam.put("attributes",arr);
        try {
            log.info("请求报文：{}",requestParam);
            String s = HttpClientUtils.postByHeader(url, head, requestParam.toJSONString());
            JSONObject jsonObject = JSONObject.parseObject(s);
            String message1 = jsonObject.getString("message");
            if (!message1.equals(StaticDataEnum.BREAZE_REAPONSES_SUCCESS.getMessage())&&!message1.equals(StaticDataEnum.BREAZE_REAPONSES_QUEUED.getMessage())){
                throw new BizException("track failed");
            }
        } catch (Exception e) {
            log.error("请求三方异常;{}",e);
            throw e;
        }
    }
}
