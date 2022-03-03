package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.uwallet.pay.core.config.CustomThreadPoolConfig;
import com.uwallet.pay.core.util.CustomThreadPoolTaskExecutor;
import com.uwallet.pay.core.util.ListUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.MessageBatchSendLogDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.MessageBatchSendLog;
import com.uwallet.pay.main.service.MessageBatchSendLogService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.FireBaseUtil;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 批量发送消息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 批量发送消息表
 * @author: xucl
 * @date: Created in 2021-05-11 14:18:13
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class MessageBatchSendLogServiceImpl extends BaseServiceImpl implements MessageBatchSendLogService {

    @Resource
    private MessageBatchSendLogDAO messageBatchSendLogDAO;
    @Autowired
    private StaticDataService staticDataService;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private CustomThreadPoolTaskExecutor threadPool;
    @Autowired
    @Lazy
    UserService userService;
    @Autowired
    @Lazy
    private ServerService serverService;
    @Autowired
    @Lazy
    private MessageBatchSendLogService messageBatchSendLogService;
    @Value("${spring.pushFirebaseFilePath}")
    private String pushFirebaseFilePath;

    @Value("${spring.pushFirebaseUrl}")
    private String pushFirebaseUrl;

    @Override
    public Long saveMessageBatchSendLog(@NonNull MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) throws BizException {
        MessageBatchSendLog messageBatchSendLog = BeanUtil.copyProperties(messageBatchSendLogDTO, new MessageBatchSendLog());
        log.info("save MessageBatchSendLog:{}", messageBatchSendLog);
        MessageBatchSendLog baseEntity = (MessageBatchSendLog)this.packAddBaseProps(messageBatchSendLog, request);
        if (messageBatchSendLogDAO.insert(baseEntity) != 1) {
            log.error("insert error, data:{}", messageBatchSendLog);
            throw new BizException("Insert messageBatchSendLog Error!");
        }
        return messageBatchSendLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessageBatchSendLogList(@NonNull List<MessageBatchSendLog> messageBatchSendLogList, HttpServletRequest request) throws BizException {
        if (messageBatchSendLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = messageBatchSendLogDAO.insertList(messageBatchSendLogList);
        if (rows != messageBatchSendLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, messageBatchSendLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMessageBatchSendLog(@NonNull Long id, @NonNull MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update messageBatchSendLogDTO:{}", messageBatchSendLogDTO);
        MessageBatchSendLog messageBatchSendLog = BeanUtil.copyProperties(messageBatchSendLogDTO, new MessageBatchSendLog());
        messageBatchSendLog.setId(id);
        int cnt = messageBatchSendLogDAO.update((MessageBatchSendLog) this.packModifyBaseProps(messageBatchSendLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", messageBatchSendLogDTO);
            throw new BizException("update messageBatchSendLog Error!");
        }
    }

    @Override
    public void updateMessageBatchSendLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        messageBatchSendLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteMessageBatchSendLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = messageBatchSendLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMessageBatchSendLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = messageBatchSendLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MessageBatchSendLogDTO findMessageBatchSendLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MessageBatchSendLogDTO messageBatchSendLogDTO = messageBatchSendLogDAO.selectOneDTO(params);
        return messageBatchSendLogDTO;
    }

    @Override
    public MessageBatchSendLogDTO findOneMessageBatchSendLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MessageBatchSendLog messageBatchSendLog = messageBatchSendLogDAO.selectOne(params);
        MessageBatchSendLogDTO messageBatchSendLogDTO = new MessageBatchSendLogDTO();
        if (null != messageBatchSendLog) {
            BeanUtils.copyProperties(messageBatchSendLog, messageBatchSendLogDTO);
        }
        return messageBatchSendLogDTO;
    }

    @Override
    public List<MessageBatchSendLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MessageBatchSendLogDTO> resultList = messageBatchSendLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return messageBatchSendLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return messageBatchSendLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = messageBatchSendLogDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return messageBatchSendLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = messageBatchSendLogDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }

    @Override
    public int getUserCount(MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) throws Exception{
        int result = 0 ;
        Map<String,Object> params = new HashMap<>(8);
        if(messageBatchSendLogDTO.getTerritory() != null){
            params.put("userState",messageBatchSendLogDTO.getTerritory()+"");
            if(messageBatchSendLogDTO.getCity() != null ){
                Map<String ,Object> params1 =  new HashMap<>(8);
                params1.put("code","activityCity");
                params1.put("parent",messageBatchSendLogDTO.getTerritory());
                params1.put("value",messageBatchSendLogDTO.getCity()+"");
                StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params1);
                if(staticDataDTO == null || staticDataDTO.getId() == null ){
                    throw new Exception("Unknown City");
                }
                if(staticDataDTO.getValue().equals(StaticDataEnum.ACTIVITY_CITY_STATUS_1.getCode()+"")){
                    params.put("userCity",staticDataDTO.getEnName());
                }else {
                    params1.clear();;
                    params1.put("code","activityCity");
                    params1.put("parent",messageBatchSendLogDTO.getTerritory());
                    params1.put("value",StaticDataEnum.ACTIVITY_CITY_STATUS_1.getCode()+"");
                    StaticDataDTO staticDataDTO1 = staticDataService.findOneStaticData(params1);
                    params.put("notInUserCity",staticDataDTO1.getEnName());
                }
            }
        }

        Calendar calendar ;
        Long now ;
        Long startTime;
        if(messageBatchSendLogDTO.getUserStatus() != null) {
            switch (messageBatchSendLogDTO.getUserStatus()){
                case 1 :
                    // 未绑卡也未开通分期付(未绑卡，分期付未走到获取报告)
                    result = messageBatchSendLogDAO.getNoCardNoCreditUserCount(params);
                    break;
                case 2:
                    // 未申请过分期付
                    result = messageBatchSendLogDAO.getNoCreditUserCount(params);
                    break;
                case 3:
                    // 未添加过银行卡
                    params.put("cardState", StaticDataEnum.STATUS_0.getCode());
                    result = messageBatchSendLogDAO.getNoCardUserCount(params);
                    break;
                case 4:
                    // 分期付开通拒绝
                    result = messageBatchSendLogDAO.getCreditRefuseUserCount(params);
                    break;
                case 5:
                    // 处于KYC失败状态的用户
                    result = messageBatchSendLogDAO.getKYCRefuseUserCount(params);
                    break;
                case 6 :
                    // 用户未获取到illion报告（KYC过了，illion失败）
                    result = messageBatchSendLogDAO.getNoIllionUserCount(params);
                    break;
                case 7:
                    // 有红包的用户
                    result = messageBatchSendLogDAO.getHaveRedEnvelopeUserCount(params);
                    break;
                case 8:
                    // 未有任何交易的用户
                    result = messageBatchSendLogDAO.getNoTradeUserCount(params);
                    break;
                case 9:
                    // 消费过但已一个月以上未有新交易
                    now = System.currentTimeMillis();
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(now);
                    calendar.add(Calendar.MONTH, -1);
                    startTime = calendar.getTimeInMillis();
                    params.put("start",startTime);
                    params.put("end" ,now);
                    result = messageBatchSendLogDAO.getNoTradeLongTimeUserCount(params);
                    break;
                case 10:
                    // 分期付已逾期一周以上的用户
                    now =  System.currentTimeMillis();
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(now);
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    startTime = calendar.getTimeInMillis();
                    params.put("endTime",startTime);
                    result = messageBatchSendLogDAO.getOverdueLongTimeUserCount(params);
                    break;
                case 11:
                    // 已产生逾期费的用户
                    result = messageBatchSendLogDAO.getHaveDemurrageUserCount(params);
                    break;
                default:
                    result = 0;

            }

        }else{
            result = messageBatchSendLogDAO.getNoCardUserCount(params);
        }

        return result;
    }

    public static void main(String[] args) {
        Long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        Long endTime = calendar.getTimeInMillis();
        System.out.println(endTime);
    }


    @Override
    public List<MessageBatchSendLogDTO> findNew(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MessageBatchSendLogDTO> resultList = messageBatchSendLogDAO.selectDTONew(params);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for (MessageBatchSendLogDTO messageBatchSendLogDTO : resultList) {
            Long sendTime = messageBatchSendLogDTO.getSendTime();
            if (sendTime!=null){
                String format = simpleDateFormat.format(new Date(sendTime));
                messageBatchSendLogDTO.setSendTime1(format);
                messageBatchSendLogDTO.setSendTime2(format);
            }
        }
        return resultList;
    }

    @Override
    public void updateMessageBatchSendLogNew(JSONObject param, HttpServletRequest request) throws BizException, ParseException {
        this.verifyParam(param,request);
        Integer accountStatus = param.getInteger("accountStatus");
        Integer activityCity = param.getInteger("activityCity");
        Integer messageType = param.getInteger("messageType");
        Integer findNumber = param.getInteger("findNumber");
        Integer messageManagePushRedirect = param.getInteger("messageManagePushRedirect");
        String content = param.getString("content");
        String title = param.getString("title");
        Integer territoryState = param.getInteger("territoryState");
//        Long sendTime = param.getLong("sendTime");
        String sendTimes = param.getString("sendTime");
        if (StringUtils.isBlank(sendTimes)){
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        // 18:16:13 20/01/2022
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Long sendTime=simpleDateFormat.parse(sendTimes).getTime();
        Long id = param.getLong("id");
        // 是否有id 没有新增，有则修改
        // 获取发送时间计算过期时间，
        long timeNow = System.currentTimeMillis();
        MessageBatchSendLogDTO messageBatchSendLog = new MessageBatchSendLogDTO();
        if (messageType.compareTo(StaticDataEnum.SEND_TYPE_PUSH.getCode())==0){
            messageBatchSendLog.setTitle("");
        }else if (messageType.compareTo(StaticDataEnum.SEND_TYPE_APP_MESSAGE.getCode())==0){
            messageBatchSendLog.setTitle(title);
        }
        messageBatchSendLog.setSendType(messageType);
        messageBatchSendLog.setCity(activityCity);
        messageBatchSendLog.setTerritory(territoryState);
        messageBatchSendLog.setUserStatus(accountStatus);
        messageBatchSendLog.setContent(content);
        messageBatchSendLog.setFindNumber(findNumber);
        messageBatchSendLog.setPushRedirect(messageManagePushRedirect);
        messageBatchSendLog.setSendTime(sendTime);
        Long expiredTime=sendTime-timeNow;
        if (sendTime<=timeNow){
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        // 新增/修改一条记录
        if (id!=null){
            // 修改
            messageBatchSendLog.setId(id);
            this.updateMessageBatchSendLog(id,messageBatchSendLog,request);
            MessageBatchSendLogDTO messageBatchSendLogById = this.findMessageBatchSendLogById(id);
            if (messageBatchSendLogById.getState()==StaticDataEnum.SEND_MESSAGE_STATE_1.getCode()){
                // 状态为未发送
                // 修改则重新赋予时间（判断是否大于当前时间）redis key为 Message:id value=id 过期时间为发送时间-当前时间
                redisUtils.set(StaticDataEnum.MESSAGE_PREFIX.getMessage()+id,id,expiredTime/1000);
            }

        }else {
            // 新增
            Long aLong = this.saveMessageBatchSendLog(messageBatchSendLog,request);
            // 存放redis 修改则重新赋予时间（判断是否大于当前时间）redis key为 Message:id value=id 过期时间为发送时间-当前时间
            redisUtils.set(StaticDataEnum.MESSAGE_PREFIX.getMessage()+aLong,aLong,expiredTime/1000);
        }

    }

    @Override
    public void updateState(Long id, JSONObject jsonObject, HttpServletRequest request) throws BizException {
        if (id==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 判断条件 小于当前时间不可更改 发送成功不可更改
        MessageBatchSendLogDTO messageBatchSendLogById = this.findMessageBatchSendLogById(id);
        if (messageBatchSendLogById==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        long timeMillis = System.currentTimeMillis();
        Long sendTime = messageBatchSendLogById.getSendTime();
        if (sendTime.compareTo(timeMillis)<=0){
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        Integer state = messageBatchSendLogById.getState();
        if (state==StaticDataEnum.SEND_MESSAGE_STATE_2.getCode()){
            throw new BizException(I18nUtils.get("send.message.is.send", getLang(request)));
        }else if (state==StaticDataEnum.SEND_MESSAGE_STATE_3.getCode()){
            // 可用设置redis
            redisUtils.set(StaticDataEnum.MESSAGE_PREFIX.getMessage()+id,id,(sendTime-timeMillis)/1000);
            messageBatchSendLogById.setState(StaticDataEnum.SEND_MESSAGE_STATE_1.getCode());
        }else if (state==StaticDataEnum.SEND_MESSAGE_STATE_1.getCode()){
            // 不可用删除redis
            redisUtils.del(StaticDataEnum.MESSAGE_PREFIX.getMessage()+id);
            messageBatchSendLogById.setState(StaticDataEnum.SEND_MESSAGE_STATE_3.getCode());

        }
        this.updateMessageBatchSendLog(id,messageBatchSendLogById,request);
    }

    @Override
    public void batchSendMessage(Long id) throws Exception {
        log.info("开始批量发送消息,执行id:{}", id);
        if (id == null) {
            throw new BizException("id为空");
        }
        MessageBatchSendLogDTO messageBatchSendLogById = this.findMessageBatchSendLogById(id);
        if (messageBatchSendLogById == null) {
            throw new BizException("id错误或查不出该记录");
        }
        Integer territory = messageBatchSendLogById.getTerritory();
        Integer userStatus = messageBatchSendLogById.getUserStatus();
        Integer city = messageBatchSendLogById.getCity();
        messageBatchSendLogById.setTerritory(territory == StaticDataEnum.SEND_TYPE_ALL.getCode() ? null : territory);
        messageBatchSendLogById.setCity(city == StaticDataEnum.SEND_TYPE_ALL.getCode() ? null : city);
        messageBatchSendLogById.setUserStatus(userStatus == StaticDataEnum.SEND_TYPE_ALL.getCode() ? null : userStatus);
        // 获取该条件用户
        // 进行批量发送 可以指定每批数量或每批线程平均数量 GROUP_TEN_THOUSAND_NUM 当前使用的线程平均分配
        List<UserDTO> userDTOS = null;
        try {
            userDTOS = this.getUserList(messageBatchSendLogById, null);
        } catch (Exception e) {
            log.error("获取用户列表异常:{}", e);
            throw e;
        }
        if (CollectionUtils.isEmpty(userDTOS)) {
            JSONObject param = new JSONObject(2);
            param.put("sendSuccessNumber", 0);
            param.put("id", messageBatchSendLogById.getId());
            this.updateBatchNumber(param);
            return;
        }
        Integer sendType = messageBatchSendLogById.getSendType();
        List<List<UserDTO>> partition = Lists.partition(userDTOS, CustomThreadPoolConfig.GROUP_TEN_THOUSAND_NUM_NEW);
            if (sendType == StaticDataEnum.SEND_TYPE_PUSH.getCode()) {
                System.setProperty("proxyHost", "localhost");
                System.setProperty("proxyPort", "1080");
                //如果FirebaseApp没有初始化
                if (!FireBaseUtil.isInit("Payo")) {
                    //初始化FirebaseApp
                    FireBaseUtil.initSDK(pushFirebaseFilePath, pushFirebaseUrl, "Payo");
                }
                List<String> tokensOut = new ArrayList<>();
                int count=0;
                if (sendType == StaticDataEnum.SEND_TYPE_PUSH.getCode()) {
                    for (List<UserDTO> dtos : partition) {
                        List<String> tokens = new ArrayList<>();
                        for (UserDTO userDTO : dtos) {
                            String pushToken = userDTO.getPushToken();
                            if (StringUtils.isNotBlank(pushToken)) {
                                tokens.add(userDTO.getPushToken());// push
                                tokensOut.add(pushToken);
                            }
                        }
                        if (tokens.size()>0){
                            // 设置主题
                            int i = FireBaseUtil.registrationTopic("Payo", tokens, "a");
                            count=i+count;
                        }
                    }

                    // 按主题推送
                    FireBaseUtil.sendTopicMes("Payo", "a", "", messageBatchSendLogById.getContent());
                    for (List<UserDTO> dtos : partition) {
                        List<String> tokens = new ArrayList<>();
                        for (UserDTO userDTO : dtos) {
                            String pushToken = userDTO.getPushToken();
                            if (StringUtils.isNotBlank(pushToken)) {
                                tokens.add(userDTO.getPushToken());// push
                            }
                        }
                        if (tokens.size()>0){
                            // 取消主题
                            FireBaseUtil.cancelTopic("Payo", tokens, "a");
                        }
                    }
                    JSONObject param = new JSONObject(2);
                    param.put("sendSuccessNumber", count);
                    param.put("id", messageBatchSendLogById.getId());
                    messageBatchSendLogService.updateBatchNumber(param);
                }
            } else if (sendType == StaticDataEnum.SEND_TYPE_APP_MESSAGE.getCode()) {
                // todo 分组大小固定1000
                for (List<UserDTO> list : partition) {
                    userService.batchSendMessage(list, messageBatchSendLogById);
                }
            }
    }

    @Override
    public List<UserDTO> getUserList(MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) throws Exception {
        List<UserDTO> result;
        Map<String,Object> params = new HashMap<>(8);
        if(messageBatchSendLogDTO.getTerritory() != null){
            params.put("userState",messageBatchSendLogDTO.getTerritory()+"");
            if(messageBatchSendLogDTO.getCity() != null ){
                Map<String ,Object> params1 =  new HashMap<>(8);
                params1.put("code","activityCity");
                params1.put("parent",messageBatchSendLogDTO.getTerritory());
                params1.put("value",messageBatchSendLogDTO.getCity()+"");
                StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params1);
                if(staticDataDTO == null || staticDataDTO.getId() == null ){
                    throw new Exception("Unknown City");
                }
                if(staticDataDTO.getValue().equals(StaticDataEnum.ACTIVITY_CITY_STATUS_1.getCode()+"")){
                    params.put("userCity",staticDataDTO.getEnName());
                }else {
                    params1.clear();
                    params1.put("code","activityCity");
                    params1.put("parent",messageBatchSendLogDTO.getTerritory());
                    params1.put("value",StaticDataEnum.ACTIVITY_CITY_STATUS_1.getCode()+"");
                    StaticDataDTO staticDataDTO1 = staticDataService.findOneStaticData(params1);
                    params.put("notInUserCity",staticDataDTO1.getEnName());
                }
            }
        }

        Calendar calendar ;
        Long now ;
        Long startTime;
        if(messageBatchSendLogDTO.getUserStatus() != null) {
            switch (messageBatchSendLogDTO.getUserStatus()){
                case 1 :
                    // 未绑卡也未开通分期付(未绑卡，分期付未走到获取报告)
                    result = messageBatchSendLogDAO.getNoCardNoCreditUserList(params);
                    break;
                case 2:
                    // 未申请过分期付
                    result = messageBatchSendLogDAO.getNoCreditUserList(params);
                    break;
                case 3:
                    // 未添加过银行卡
                    params.put("cardState", StaticDataEnum.STATUS_0.getCode());
                    result = messageBatchSendLogDAO.getNoCardUserList(params);
                    break;
                case 4:
                    // 分期付开通拒绝
                    result = messageBatchSendLogDAO.getCreditRefuseUserList(params);
                    break;
                case 5:
                    // 处于KYC失败状态的用户
                    result = messageBatchSendLogDAO.getKYCRefuseUserList(params);
                    break;
                case 6 :
                    // 用户未获取到illion报告（KYC过了，illion失败）
                    result = messageBatchSendLogDAO.getNoIllionUserList(params);
                    break;
                case 7:
                    // 有红包的用户
                    result = messageBatchSendLogDAO.getHaveRedEnvelopeUserList(params);
                    break;
                case 8:
                    // 未有任何交易的用户
                    result = messageBatchSendLogDAO.getNoTradeUserList(params);
                    break;
                case 9:
                    // 消费过但已一个月以上未有新交易
                    now = System.currentTimeMillis();
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(now);
                    calendar.add(Calendar.MONTH, -1);
                    startTime = calendar.getTimeInMillis();
                    params.put("start",startTime);
                    params.put("end" ,now);
                    result = messageBatchSendLogDAO.getNoTradeLongTimeUserList(params);
                    break;
                case 10:
                    // 分期付已逾期一周以上的用户
                    now =  System.currentTimeMillis();
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(now);
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    startTime = calendar.getTimeInMillis();
                    params.put("endTime",startTime);
                    result = messageBatchSendLogDAO.getOverdueLongTimeUserList(params);
                    break;
                case 11:
                    // 已产生逾期费的用户
                    result = messageBatchSendLogDAO.getHaveDemurrageUserList(params);
                    break;
                default:
                    result = null;

            }

        }else{
            result = messageBatchSendLogDAO.getNoCardUserList(params);
        }

        return result;
    }

    /**
     * 参数校验
     * @param param
     * @return
     */
    public boolean verifyParam(JSONObject param,HttpServletRequest request) throws BizException, ParseException {
        if (param==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Integer accountStatus = param.getInteger("accountStatus");
        Integer activityCity = param.getInteger("activityCity");
        Integer messageType = param.getInteger("messageType");
        Integer findNumber = param.getInteger("findNumber");
        Integer messageManagePushRedirect = param.getInteger("messageManagePushRedirect");
        String content = param.getString("content");
        String title = param.getString("title");
        Integer territoryState = param.getInteger("territoryState");
        //        Long sendTime = param.getLong("sendTime");
        String sendTimes = param.getString("sendTime");
        if (StringUtils.isBlank(sendTimes)){
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        // 18:16:13 20/01/2022
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Long sendTime=simpleDateFormat.parse(sendTimes).getTime();
        // 判断提交类型 区别校验 公共参数不单独校验
        if (messageType==null||territoryState==null||activityCity==null||accountStatus==null||findNumber==null||StringUtils.isBlank(content)||sendTime==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (messageType.compareTo(StaticDataEnum.SEND_TYPE_PUSH.getCode())==0){
            if (messageManagePushRedirect==null){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }else if (messageType.compareTo(StaticDataEnum.SEND_TYPE_APP_MESSAGE.getCode())==0){
            if (StringUtils.isBlank(title)){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }
        // 判断时间是否大于当前时间
        if (sendTime.compareTo(System.currentTimeMillis())<=0){
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        return true;
    }
    @Override
    public void updateBatchNumber(JSONObject param){
        int i=messageBatchSendLogDAO.updateBatchNumber(param);
        if (i!=1){
            log.error("修改状态异常 data:{}", param);
        }
    }

}
