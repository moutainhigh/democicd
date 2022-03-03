package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.PushAndSendMessageLogDAO;
import com.uwallet.pay.main.model.dto.IllionSubmitLogDTO;
import com.uwallet.pay.main.model.dto.KycSubmitLogDTO;
import com.uwallet.pay.main.model.dto.PushAndSendMessageLogDTO;
import com.uwallet.pay.main.model.entity.PushAndSendMessageLog;
import com.uwallet.pay.main.service.IllionSubmitLogService;
import com.uwallet.pay.main.service.KycSubmitLogService;
import com.uwallet.pay.main.service.PushAndSendMessageLogService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.TimeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-16 15:56:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class PushAndSendMessageLogServiceImpl extends BaseServiceImpl implements PushAndSendMessageLogService {

    @Autowired
    private PushAndSendMessageLogDAO pushAndSendMessageLogDAO;
    @Autowired
    private KycSubmitLogService kycSubmitLogService;
    @Autowired
    private IllionSubmitLogService illionSubmitLogService;

    @Override
    public void savePushAndSendMessageLog(@NonNull PushAndSendMessageLogDTO pushAndSendMessageLogDTO, HttpServletRequest request) throws BizException {
        PushAndSendMessageLog pushAndSendMessageLog = BeanUtil.copyProperties(pushAndSendMessageLogDTO, new PushAndSendMessageLog());
        log.info("save PushAndSendMessageLog:{}", pushAndSendMessageLog);
        if (request!=null){
            if (pushAndSendMessageLogDAO.insert((PushAndSendMessageLog) this.packAddBaseProps(pushAndSendMessageLog, request)) != 1) {
                log.error("insert error, data:{}", pushAndSendMessageLog);
                throw new BizException("Insert pushAndSendMessageLog Error!");
            }
        }else {
            long now = System.currentTimeMillis();
            pushAndSendMessageLog.setId(SnowflakeUtil.generateId());
            pushAndSendMessageLog.setCreatedDate(now);
            pushAndSendMessageLog.setModifiedDate(now);
            pushAndSendMessageLog.setStatus(1);
            // todo 考虑没有request对象是否userId等是否初始化
            pushAndSendMessageLog.setCreatedBy(0L);
            pushAndSendMessageLog.setModifiedBy(0L);
            pushAndSendMessageLog.setIp("0:0:0:0:0:0:0:1");
            pushAndSendMessageLogDAO.insert(pushAndSendMessageLog);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePushAndSendMessageLogList(@NonNull List<PushAndSendMessageLog> pushAndSendMessageLogList, HttpServletRequest request) throws BizException {
        if (request!=null){
            if (pushAndSendMessageLogList.size() == 0) {
                throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
            }
            int rows = pushAndSendMessageLogDAO.insertList(pushAndSendMessageLogList);
            if (rows != pushAndSendMessageLogList.size()) {
                log.error("数据库实际插入成功数({})与给定的({})不一致", rows, pushAndSendMessageLogList.size());
                throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
            }
        }else {
            if (pushAndSendMessageLogList.size() == 0) {
                throw new BizException("参数长度不能为0");
            }
            int rows = pushAndSendMessageLogDAO.insertList(pushAndSendMessageLogList);
            if (rows != pushAndSendMessageLogList.size()) {
                log.error("数据库实际插入成功数({})与给定的({})不一致", rows, pushAndSendMessageLogList.size());
                throw new BizException("批量保存异常");
            }
        }

    }

    @Override
    public void updatePushAndSendMessageLog(@NonNull Long id, @NonNull PushAndSendMessageLogDTO pushAndSendMessageLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update pushAndSendMessageLogDTO:{}", pushAndSendMessageLogDTO);
        PushAndSendMessageLog pushAndSendMessageLog = BeanUtil.copyProperties(pushAndSendMessageLogDTO, new PushAndSendMessageLog());
        pushAndSendMessageLog.setId(id);
        int cnt = pushAndSendMessageLogDAO.update((PushAndSendMessageLog) this.packModifyBaseProps(pushAndSendMessageLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", pushAndSendMessageLogDTO);
            throw new BizException("update pushAndSendMessageLog Error!");
        }
    }

    @Override
    public void updatePushAndSendMessageLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        pushAndSendMessageLogDAO.updatex(params);
    }

    @Override
    public void logicDeletePushAndSendMessageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = pushAndSendMessageLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePushAndSendMessageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = pushAndSendMessageLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PushAndSendMessageLogDTO findPushAndSendMessageLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PushAndSendMessageLogDTO pushAndSendMessageLogDTO = pushAndSendMessageLogDAO.selectOneDTO(params);
        return pushAndSendMessageLogDTO;
    }

    @Override
    public PushAndSendMessageLogDTO findOnePushAndSendMessageLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PushAndSendMessageLog pushAndSendMessageLog = pushAndSendMessageLogDAO.selectOne(params);
        PushAndSendMessageLogDTO pushAndSendMessageLogDTO = new PushAndSendMessageLogDTO();
        if (null != pushAndSendMessageLog) {
            BeanUtils.copyProperties(pushAndSendMessageLog, pushAndSendMessageLogDTO);
        }
        return pushAndSendMessageLogDTO;
    }

    @Override
    public List<PushAndSendMessageLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PushAndSendMessageLogDTO> resultList = pushAndSendMessageLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return pushAndSendMessageLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return pushAndSendMessageLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = pushAndSendMessageLogDAO.groupCount(conditions);
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
        return pushAndSendMessageLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = pushAndSendMessageLogDAO.groupSum(conditions);
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
    public List<JSONObject> getBillingRecord(Map<String, Object> param, HttpServletRequest request) throws Exception {
        JSONObject result=new JSONObject();
        List<JSONObject> resultList=new ArrayList<>();
        // kyc
        JSONObject kycParam = this.addParam(param,false);
        int count = kycSubmitLogService.count(kycParam);
        kycParam.put("isRequest", StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
        kycParam.put("kycStatus",StaticDataEnum.KYC_CHECK_STATE_3.getCode());
        // 当前只展示status>0 status为-1不展示
        List<KycSubmitLogDTO> kycSubmitLogDTOS = kycSubmitLogService.notInFind(kycParam, null, null);
        result.put("kyc",count-kycSubmitLogDTOS.size());
        // illion 时间需特殊处理
        JSONObject illionParam = this.addParam(param,true);
        int illinNum = illionSubmitLogService.count(illionParam);
        illionParam.put("submittedStatus",StaticDataEnum.ILLION_SUBMIT_STATUS_0.getCode());
        illionParam.put("reportStatus",StaticDataEnum.ILLION_STATUS_2.getCode());
        List<IllionSubmitLogDTO> illionSubmitLogDTOS = illionSubmitLogService.findNotIn(illionParam, null, null);
        result.put("illion",illinNum-illionSubmitLogDTOS.size());
        // push
        JSONObject pushParam = this.addParam(param,false);
        pushParam.put("type",StaticDataEnum.SEND_TYPE_1.getCode());
        List<PushAndSendMessageLogDTO> pushDTOS = pushAndSendMessageLogDAO.selectDTO(pushParam);
        result.put("push",pushDTOS==null?0:pushDTOS.size());
        // 短信
        JSONObject messageParam = this.addParam(param,false);
        pushParam.put("type",StaticDataEnum.SEND_TYPE_2.getCode());
        List<PushAndSendMessageLogDTO> messageParamDtos = pushAndSendMessageLogDAO.selectDTO(messageParam);
        result.put("send",messageParamDtos==null?0:messageParamDtos.size());
        // 时间
        Long start = kycParam.getLong("start");
        Long monthTime = kycParam.getLong("monthTime");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM");
        if (start!=null){
            Date date = new Date(start);
            String format = simpleDateFormat.format(date);
            result.put("time",format);
        }else {
            Date date = new Date(monthTime);
            String format = simpleDateFormat.format(date);
            result.put("time",format);
        }
        resultList.add(result);
        return resultList;
    }
    private JSONObject addParam(Map<String,Object> endParam,boolean isIllion) throws ParseException {
        // 不传默认当前月份
        JSONObject firstParam=new JSONObject();
        Object start = endParam.get("start");
        Object end = endParam.get("end");
        long monthTime = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // illion时间需要特殊处理
        if (start==null||end==null){
            if (isIllion){
                monthTime= simpleDateFormat.parse(TimeUtils.timeTransfer(monthTime)).getTime();
            }
            firstParam.put("monthTime",monthTime);
        }else {
            if (isIllion){
                start= simpleDateFormat.parse(TimeUtils.timeTransfer(Long.parseLong(start.toString()))).getTime();
                end= simpleDateFormat.parse(TimeUtils.timeTransfer(Long.parseLong(end.toString()))).getTime();
            }
            firstParam.put("start",start);
            firstParam.put("end",end);
        }
        return firstParam;
    }

}
