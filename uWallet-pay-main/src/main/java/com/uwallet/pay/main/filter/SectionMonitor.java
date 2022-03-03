package com.uwallet.pay.main.filter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.FirebaseDTO;
import com.uwallet.pay.main.model.dto.PushAndSendMessageLogDTO;
import com.uwallet.pay.main.model.dto.RequestAnalysisDTO;
import com.uwallet.pay.main.model.entity.PushAndSendMessageLog;
import com.uwallet.pay.main.service.PushAndSendMessageLogService;
import com.uwallet.pay.main.service.RequestAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *@name  springboot AOP切面监控接口调用详情
 *@author:  Aaron_S
 *@create   2021-02-03
 *
 **/
@Aspect
@Component
@Slf4j
public class SectionMonitor {
    @Resource
    private RequestAnalysisService requestAnalysisService;
    @Autowired
    private PushAndSendMessageLogService pushAndSendMessageLogService;
    /*
      当前线程的容器
     */
    private static final ThreadLocal<JSONObject> timeTreadLocal = new ThreadLocal<>();
    /* 指定接口->注解@
       @annotation(org.springframework.web.bind.annotation.RequestMapping)
     */
    @Pointcut("execution(* com.uwallet.pay.main.controller.AppInteractiveController.*(..))")
    public void log() {
    }
    @Before("log()")
    public void before(JoinPoint joinPoint) {
        try {
            long startTimeMil = System.currentTimeMillis();
            JSONObject data = new JSONObject(7);
            data.put("time", startTimeMil);
            // 接收到请求，记录请求内容
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //获取请求的request
            HttpServletRequest request = attributes.getRequest();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            //获取所有请求参数
            String keyValue = ReadAsChars(request).replace(" ", "");

            //System.out.println("请求参数 key：value = "+ keyValue);
            //获取被拦截的方法
            Method method = methodSignature.getMethod();
            //获取被拦截的方法名
            String gatewayName = method.getName();
            //请求ID
            String requestId = "APP-" + SnowflakeUtil.generateId();
            //请求Ip
            String ip = getIp(request);
            //userId
            Long userId = getUserId(request);
            //log.info("接口方法名称：" + methodName + "(), url = "+ request.getRequestURL().toString() +", 方法类型 = "+ request.getMethod());
            log.info("========>> \n==================================\n" +
                    "APP- 接口方法名称： /" + gatewayName + ",\n RequestId: " + requestId + ",\n UserId: " + data.get("userId")
                    + ",\n IP: " + ip + "\n" + "请求参数: " + keyValue + "\n"
                    + "==================================\n");
        /*try {
           Long id = requestAnalysisService.saveRequestAnalysis(
                   RequestAnalysisDTO.builder()
                           .requestId(requestId)
                           .userId(userId)
                           .appType(0)//todo 回头补上 目前app没有适配
                           .gatewayName(gatewayName)
                           .requestMethod(request.getMethod())
                           .startTime(startTimeMil)
                           .requestParams(keyValue)
                           .requestIp(ip)
                           .build()
                   ,request);
           data.put("id",id);
        }catch (Exception e){
            log.error("SectionMonitor 保存请求记录异常,requestId:{},gatewayName:{},error msg:{}",requestId,gatewayName,e.getMessage());
        }*/
            data.put("requestId", requestId);
            data.put("gatewayName", gatewayName);
            data.put("userId", userId);
            timeTreadLocal.set(data);
        }catch (Exception e){
            log.error("监控类异常: e:{}", e.getMessage());
        }
    }


    @After("log()")
    public void after() {
    }

    //controller请求结束返回时调用
    @AfterReturning(returning = "result", pointcut = "log()")
    public Object afterReturn(Object result) {
        try{
        JSONObject data = timeTreadLocal.get();
        long startTime = data.getLong("time");
        long finishTimeMil = System.currentTimeMillis();
        double callTime = (finishTimeMil - startTime) / 1000.0;
        String requestId = data.getString("requestId");
        String gatewayName = data.getString("gatewayName");

        log.info("========>> \n==================================\n" +
                "调用接口完成: /"+gatewayName+",\n 共花费时间: "+ callTime+" s,\n RequestId: "+requestId+",\n UserId: "+data.get("userId")+"\n"
                +"==================================\n");
        /*Long id = data.getLong("id");
        if (null != id) {
            String resultStr = result.toString();
            int resultLength = resultStr.length();
            try {
                requestAnalysisService.updateRequestAnalysis(id,
                        RequestAnalysisDTO.builder()
                                //.responseData(resultStr)//结果集 太大了,现在不放
                                .finishedTime(finishTimeMil)
                                .responseSize((double) resultStr.length())
                                .completedTime(String.valueOf(callTime))
                                .build());
            }catch (Exception e){
                log.error("SectionMonitor 更新保存请求记录异常,requestId:{},id:{},error msg:{}",requestId,id,e.getMessage());
            }
            log.info("保存请求时间成功id:{}----> 结果集长度:" + resultLength,id);
        }*/
        }catch (Exception e){
            log.error("监控类异常: e:{}", e.getMessage());
        }
        return result;
    }
    public Long getUserId(HttpServletRequest request) {
        // 通过token获取操作人id
        String jwt = request.getHeader("Authorization");
        Long id = 0L;
        if (StringUtils.isNotEmpty(jwt)) {
            id = JwtUtils.getId(jwt.replace("Bearer ", ""));
        }
        return id;
    }
    // 字符串读取
    // 方法一
    public static String ReadAsChars(HttpServletRequest request)
    {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try
        {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null)
            {
                sb.append(str);
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取所有请求参数，封装为map对象
     *
     * @return
     */
    public Map<String, Object> getParameterMap(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder stringBuilder = new StringBuilder();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getParameter(key);
            String keyValue = key + " : " + value + " ; ";
            stringBuilder.append(keyValue);
            parameterMap.put(key, value);
        }
        return parameterMap;
    }

    public String getReqParameter(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Enumeration<String> enumeration = request.getParameterNames();
        //StringBuilder stringBuilder = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getParameter(key);
            JSONObject json = new JSONObject();
            json.put(key, value);
            jsonArray.add(json);
        }
        return jsonArray.toString();
    }

    //execution(* com.uwallet.pay.main.service.impl.ServerServiceImpl.pushFire*(..))"
    @Pointcut("execution(* com.uwallet.pay.main.service.impl.ServerServiceImpl.pushFirebase(..)) || execution(* com.uwallet.pay.main.service.impl.ServerServiceImpl.pushFirebaseList(..))")
    public void addlog() {
    }
    @AfterReturning("addlog()")
    public void addLogBefore(JoinPoint joinPoint){
        log.info("开始记录push日志");
        try{
            Object[] args = joinPoint.getArgs();
            log.info("记录push日志,data:{}",args[0]);
            if (args[0] instanceof FirebaseDTO ){
                FirebaseDTO firebaseDTO = (FirebaseDTO)args[0];
                List<String> tokens = firebaseDTO.getTokens();
                if (CollectionUtils.isNotEmpty(tokens)){
                    List<PushAndSendMessageLog> logDTOS=new ArrayList<>();
                    for (String token : tokens) {
                        long now = System.currentTimeMillis();
                        PushAndSendMessageLog PushAndSendMessageLog = new PushAndSendMessageLog();
                        PushAndSendMessageLog.setId(SnowflakeUtil.generateId());
                        PushAndSendMessageLog.setCreatedDate(now);
                        PushAndSendMessageLog.setModifiedDate(now);
                        PushAndSendMessageLog.setStatus(1);
                        PushAndSendMessageLog.setCreatedBy(0L);
                        PushAndSendMessageLog.setModifiedBy(0L);
                        PushAndSendMessageLog.setIp("0:0:0:0:0:0:0:1");
                        PushAndSendMessageLog.setType(StaticDataEnum.SEND_TYPE_1.getCode());
                        PushAndSendMessageLog.setData(args[0].toString());
                        logDTOS.add(PushAndSendMessageLog);
                    }
                    if (null != args[1]){
                        HttpServletRequest request=(HttpServletRequest)args[1];
                        pushAndSendMessageLogService.savePushAndSendMessageLogList(logDTOS,request);
                    }else {
                        pushAndSendMessageLogService.savePushAndSendMessageLogList(logDTOS,null);
                    }

                }else {
                    PushAndSendMessageLogDTO pushAndSendMessageLogDTO = new PushAndSendMessageLogDTO();
                    pushAndSendMessageLogDTO.setData(args[0].toString());
                    pushAndSendMessageLogDTO.setType(StaticDataEnum.SEND_TYPE_1.getCode());
                    if (null != args[1]){
                        HttpServletRequest request=(HttpServletRequest)args[1];
                        pushAndSendMessageLogDTO.setUserId(firebaseDTO.getUserId());
                        pushAndSendMessageLogService.savePushAndSendMessageLog(pushAndSendMessageLogDTO,request);
                    }else {
                        pushAndSendMessageLogService.savePushAndSendMessageLog(pushAndSendMessageLogDTO,null);
                    }
                }
            }

        }catch (Exception e){
            log.error("记录push日志异常,e:{}",e);
        }

    }

    @Pointcut("execution(* com.uwallet.pay.main.service.impl.AliyunSmsServiceImpl.sendChinaSms(..))||execution(* com.uwallet.pay.main.service.impl.AliyunSmsServiceImpl.sendInternationalSms(..))")
    public void addSendLog() {
    }
    @AfterReturning("addSendLog()")
    public void addSendLogBefore(JoinPoint joinPoint){
        log.info("开始记录发送短信日志");
        try{
            Object[] args = joinPoint.getArgs();
            JSONObject data=new JSONObject();
            for (int i = 0; i < args.length; i++) {
                data.put("arg"+i+1,args[i]);
            }
            PushAndSendMessageLogDTO pushAndSendMessageLogDTO = new PushAndSendMessageLogDTO();
            pushAndSendMessageLogDTO.setData(data.toString());
            pushAndSendMessageLogDTO.setType(StaticDataEnum.SEND_TYPE_2.getCode());
            pushAndSendMessageLogService.savePushAndSendMessageLog(pushAndSendMessageLogDTO,null);
            log.info("记录发送短信日志,data:{},",data);
        }catch (Exception e){
            log.error("记录发送短信日志异常,e:{},",e);
        }



    }
}
