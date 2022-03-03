package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.dao.UserStepLogDAO;
import com.uwallet.pay.main.model.dto.UserDetailRepaymentDTO;
import com.uwallet.pay.main.model.dto.UserStepLogDTO;
import com.uwallet.pay.main.model.entity.UserStepLog;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.service.UserStepLogService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 用户权限阶段记录
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户权限阶段记录
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:52:45
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class UserStepLogServiceImpl extends BaseServiceImpl implements UserStepLogService {

    @Autowired
    private UserStepLogDAO userStepLogDAO;

    @Override
    public void saveUserStepLog(@NonNull UserStepLogDTO userStepLogDTO, HttpServletRequest request) throws BizException {
        UserStepLog userStepLog = BeanUtil.copyProperties(userStepLogDTO, new UserStepLog());
        log.info("save UserStepLog:{}", userStepLog);
        if (userStepLogDAO.insert((UserStepLog) this.packAddBaseProps(userStepLog, request)) != 1) {
            log.error("insert error, data:{}", userStepLog);
            throw new BizException("Insert userStepLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserStepLogList(@NonNull List<UserStepLog> userStepLogList, HttpServletRequest request) throws BizException {
        if (userStepLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userStepLogDAO.insertList(userStepLogList);
        if (rows != userStepLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userStepLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserStepLog(@NonNull Long id, @NonNull UserStepLogDTO userStepLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update userStepLogDTO:{}", userStepLogDTO);
        UserStepLog userStepLog = BeanUtil.copyProperties(userStepLogDTO, new UserStepLog());
        userStepLog.setId(id);
        int cnt = userStepLogDAO.update((UserStepLog) this.packModifyBaseProps(userStepLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userStepLogDTO);
            throw new BizException("update userStepLog Error!");
        }
    }

    @Override
    public void updateUserStepLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userStepLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserStepLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userStepLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserStepLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userStepLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserStepLogDTO findUserStepLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserStepLogDTO userStepLogDTO = userStepLogDAO.selectOneDTO(params);
        return userStepLogDTO;
    }

    @Override
    public UserStepLogDTO findOneUserStepLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserStepLog userStepLog = userStepLogDAO.selectOne(params);
        UserStepLogDTO userStepLogDTO = new UserStepLogDTO();
        if (null != userStepLog) {
            BeanUtils.copyProperties(userStepLog, userStepLogDTO);
        }
        return userStepLogDTO;
    }

    @Override
    public List<UserStepLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserStepLogDTO> resultList = userStepLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userStepLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userStepLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userStepLogDAO.groupCount(conditions);
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
        return userStepLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userStepLogDAO.groupSum(conditions);
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
    public UserStepLogDTO findLatestStepLog(Long stepId) {
        return userStepLogDAO.findLatestStepLog(stepId);
    }

    @Override
    public List<UserStepLogDTO> findStepLog(Long stepId) {
        List<UserStepLogDTO> userStepLogDTOList = userStepLogDAO.findStepLog(stepId);
        userStepLogDTOList.forEach(userStepLogDTO -> {
            userStepLogDTO.setKycInfoObject(JSONObject.parseObject(userStepLogDTO.getKycInfo()));
        });
        return userStepLogDTOList;
    }

    @Override
    public JSONObject findStepLogNew(@NonNull Map<String, Object> params, HttpServletRequest request) throws BizException {
        JSONObject result=new JSONObject();
        Object step = params.get("step");
        Object userId = params.get("userId");
        if (userId==null){
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        if (step!=null){
            String stepValue= step+"";
            String userIdValue=(String) userId;
            // 等于3查询illion和风控 审核数据
            if (stepValue.equals("3")){
                params.put("step","2");
                params.put("userId",userIdValue);
                List<UserStepLogDTO> userStepLogDTOListIllion=userStepLogDAO.findStepLogNew(params);
                params.put("step","3");
                List<UserStepLogDTO> userStepLogDTOListRisk=userStepLogDAO.findStepLogNew(params);
                result.put("illionData",userStepLogDTOListIllion);
                result.put("RiskData",userStepLogDTOListRisk);
            }else {
                params.put("step",stepValue);
                params.put("userId",userIdValue);
                List<UserStepLogDTO> userStepLogDTOListIllion=userStepLogDAO.findStepLogNew(params);
                for (UserStepLogDTO userStepLogDTO : userStepLogDTOListIllion) {
                    String kycInfo = userStepLogDTO.getKycInfo();
                    if (StringUtils.isNotBlank(kycInfo)){
                        JSONObject jsonObject = JSONObject.parseObject(kycInfo);
                        userStepLogDTO.setKycInfoObject(jsonObject);
                    }
                }
                result.put("kycData",userStepLogDTOListIllion);
            }
            return result;
            // 查询kyc审核数据
        }

        return null;
    }

    @Override
    public int findKycLogListCount(Map<String, Object> params) {
        return userStepLogDAO.findKycLogListCount(params);
    }

    @Override
    public List<UserStepLogDTO> findKycLogList(Map<String, Object> params) {
        return userStepLogDAO.findKycLogList(params);
    }
}
