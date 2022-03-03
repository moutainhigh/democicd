package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.IllionSubmitLogDAO;
import com.uwallet.pay.main.model.dto.IllionSubmitLogDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.IllionSubmitLog;
import com.uwallet.pay.main.service.IllionSubmitLogService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.IllionSubmitStatusLogService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.TimeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-13 11:11:34
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class IllionSubmitLogServiceImpl extends BaseServiceImpl implements IllionSubmitLogService {

    @Autowired
    private IllionSubmitLogDAO illionSubmitLogDAO;
    @Resource
    @Lazy
    private UserService userService;

    @Autowired
    private IllionSubmitStatusLogService illionSubmitStatusLogService;

    @Override
    public void saveIllionSubmitLog(@NonNull IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) throws BizException {
        IllionSubmitLog illionSubmitLog = BeanUtil.copyProperties(illionSubmitLogDTO, new IllionSubmitLog());
        log.info("save IllionSubmitLog:{}", illionSubmitLog);
        if (illionSubmitLogDAO.insert((IllionSubmitLog) this.packAddBaseProps(illionSubmitLog, request)) != 1) {
            log.error("insert error, data:{}", illionSubmitLog);
            throw new BizException("Insert illionSubmitLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIllionSubmitLogList(@NonNull List<IllionSubmitLog> illionSubmitLogList, HttpServletRequest request) throws BizException {
        if (illionSubmitLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = illionSubmitLogDAO.insertList(illionSubmitLogList);
        if (rows != illionSubmitLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, illionSubmitLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateIllionSubmitLog(@NonNull Long id, @NonNull IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update illionSubmitLogDTO:{}", illionSubmitLogDTO);
        IllionSubmitLog illionSubmitLog = BeanUtil.copyProperties(illionSubmitLogDTO, new IllionSubmitLog());
        illionSubmitLog.setId(id);
        int cnt = illionSubmitLogDAO.update((IllionSubmitLog) this.packModifyBaseProps(illionSubmitLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", illionSubmitLogDTO);
            throw new BizException("update illionSubmitLog Error!");
        }
    }

    @Override
    public void updateIllionSubmitLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        illionSubmitLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteIllionSubmitLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = illionSubmitLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteIllionSubmitLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = illionSubmitLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public IllionSubmitLogDTO findIllionSubmitLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        IllionSubmitLogDTO illionSubmitLogDTO = illionSubmitLogDAO.selectOneDTO(params);
        return illionSubmitLogDTO;
    }

    @Override
    public IllionSubmitLogDTO findOneIllionSubmitLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        IllionSubmitLog illionSubmitLog = illionSubmitLogDAO.selectOne(params);
        IllionSubmitLogDTO illionSubmitLogDTO = new IllionSubmitLogDTO();
        if (null != illionSubmitLog) {
            BeanUtils.copyProperties(illionSubmitLog, illionSubmitLogDTO);
        }
        return illionSubmitLogDTO;
    }

    @Override
    public List<IllionSubmitLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws BizException {
        params = getUnionParams(params, scs, pc);
        List<IllionSubmitLogDTO> resultList = illionSubmitLogDAO.selectDTO(params);
        x:for (IllionSubmitLogDTO illionSubmitLogDTO : resultList) {
            // 处理report状态
            if (illionSubmitLogDTO.getReportStatus().compareTo(StaticDataEnum.ILLION_STATUS_0.getCode())==0){
                illionSubmitLogDTO.setReportStatusStr(StaticDataEnum.ILLION_STATUS_0.getMessage());
            }else if (illionSubmitLogDTO.getReportStatus().compareTo(StaticDataEnum.ILLION_STATUS_1.getCode())==0){
                illionSubmitLogDTO.setReportStatusStr(StaticDataEnum.ILLION_STATUS_1.getMessage());
            }else if (illionSubmitLogDTO.getReportStatus().compareTo(StaticDataEnum.ILLION_STATUS_2.getCode())==0){
                illionSubmitLogDTO.setReportStatusStr(StaticDataEnum.ILLION_STATUS_2.getMessage());
            }else if (illionSubmitLogDTO.getReportStatus().compareTo(StaticDataEnum.ILLION_STATUS_3.getCode())==0){
                illionSubmitLogDTO.setReportStatusStr(StaticDataEnum.ILLION_STATUS_3.getMessage());
            }
            // 时间转换
            Long date = illionSubmitLogDTO.getDate();
            String time = TimeUtils.timeTransfer(date);
            illionSubmitLogDTO.setSimpleDate(time);
            // 用户姓名
            Long userId = illionSubmitLogDTO.getUserId();
            if (userId!=null){
                UserDTO userById = userService.findUserById(userId);
                if (userById==null){
                    log.error("查询用户信息不存在 用户id:{}",userById);
                    break x;
//                    throw new BizException("user is not exist");
                }
                String userLastName = userById.getUserLastName();
                String userFirstName = userById.getUserFirstName();
                illionSubmitLogDTO.setName(userFirstName+userLastName);
            }
            illionSubmitLogDTO.setSubmitStr(StringUtils.isBlank(illionSubmitLogDTO.getSubmittedError())?"Success":illionSubmitLogDTO.getSubmittedError());
        }
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return illionSubmitLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return illionSubmitLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = illionSubmitLogDAO.groupCount(conditions);
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
        return illionSubmitLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = illionSubmitLogDAO.groupSum(conditions);
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
    @Async("taskExecutor")
    public void saveIllionSubmitLogNew(@NonNull IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) throws BizException {
        IllionSubmitLog illionSubmitLog = BeanUtil.copyProperties(illionSubmitLogDTO, new IllionSubmitLog());
        log.info("save IllionSubmitLog:{}", illionSubmitLog);
        long now = System.currentTimeMillis();
        Long currentLoginId = getUserId(request);
        illionSubmitLog.setCreatedBy(currentLoginId);
        illionSubmitLog.setCreatedDate(now);
        illionSubmitLog.setModifiedBy(currentLoginId);
        illionSubmitLog.setModifiedDate(now);
        illionSubmitLog.setIp(getIp(request));
        illionSubmitLog.setStatus(1);
        if (illionSubmitLogDAO.insert(illionSubmitLog) != 1) {
            log.error("insert error, data:{}", illionSubmitLog);
            throw new BizException("Insert illionSubmitLog Error!");
        }
    }
    @Override
    public void updateIllionSubmitLogNew(JSONObject param, HttpServletRequest request) throws BizException {
        log.info("full update param:{}", param);
        int cnt = illionSubmitLogDAO.updateNew(param);
        if (cnt != 1) {
            log.error("update error, data:{}", param);
            throw new BizException("update illionSubmitLog Error!");
        }
    }

    @Override
    public List<IllionSubmitLogDTO> findNotIn(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws BizException {
        params = getUnionParams(params, scs, pc);
        List<IllionSubmitLogDTO> resultList = illionSubmitLogDAO.selectNotInDTO(params);
        return resultList;
    }

    @Override
    public void updateIllionSubmitLogNew(Long id, IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update illionSubmitLogDTO:{}", illionSubmitLogDTO);
        //添加用户分期付进行状态 2021-6-15 需求
        IllionSubmitLogDTO illionSubmitLogById = this.findIllionSubmitLogById(id);
        Integer reportStatus = illionSubmitLogDTO.getReportStatus();
        Long userId = illionSubmitLogDTO.getUserId();
        JSONObject stateParam=new JSONObject();
        stateParam.put("userId",illionSubmitLogById.getUserId());
        if (reportStatus==StaticDataEnum.ILLION_STATUS_1.getCode()){
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_5.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
        }else if (reportStatus==StaticDataEnum.ILLION_STATUS_0.getCode()){
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_6.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
        }

        IllionSubmitLog illionSubmitLog = BeanUtil.copyProperties(illionSubmitLogDTO, new IllionSubmitLog());
        illionSubmitLog.setId(id);
        int cnt = illionSubmitLogDAO.update((IllionSubmitLog) this.packModifyBaseProps(illionSubmitLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", illionSubmitLogDTO);
            throw new BizException("update illionSubmitLog Error!");
        }
    }

}
