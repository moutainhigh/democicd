package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.UserStepDAO;
import com.uwallet.pay.main.model.dto.UserStepDTO;
import com.uwallet.pay.main.model.dto.UserStepLogDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.model.entity.UserStep;
import com.uwallet.pay.main.model.entity.UserStepLog;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.service.UserStepLogService;
import com.uwallet.pay.main.service.UserStepService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 用户权限阶段
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户权限阶段
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:51:35
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class UserStepServiceImpl extends BaseServiceImpl implements UserStepService {

    @Autowired
    private UserStepDAO userStepDAO;

    @Autowired
    private UserStepLogService userStepLogService;

    @Autowired
    private StaticDataService staticDataService;

    /**
     * 查询数据字典
     */
    private static final String USER_STEP = "userStep";

    @Override
    public void saveUserStep(@NonNull UserStepDTO userStepDTO, HttpServletRequest request) throws BizException {
        UserStep userStep = BeanUtil.copyProperties(userStepDTO, new UserStep());
        log.info("save UserStep:{}", userStep);
        if (userStepDAO.insert((UserStep) this.packAddBaseProps(userStep, request)) != 1) {
            log.error("insert error, data:{}", userStep);
            throw new BizException("Insert userStep Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserStepList(@NonNull List<UserStep> userStepList, HttpServletRequest request) throws BizException {
        if (userStepList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userStepDAO.insertList(userStepList);
        if (rows != userStepList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userStepList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserStep(@NonNull Long id, @NonNull UserStepDTO userStepDTO, HttpServletRequest request) throws BizException {
        log.info("full update userStepDTO:{}", userStepDTO);
        UserStep userStep = BeanUtil.copyProperties(userStepDTO, new UserStep());
        userStep.setId(id);
        int cnt = userStepDAO.update((UserStep) this.packModifyBaseProps(userStep, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userStepDTO);
            throw new BizException("update userStep Error!");
        }
    }

    @Override
    public void updateUserStepSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userStepDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserStep(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userStepDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserStep(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userStepDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserStepDTO findUserStepById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserStepDTO userStepDTO = userStepDAO.selectOneDTO(params);
        return userStepDTO;
    }

    @Override
    public UserStepDTO findOneUserStep(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserStep userStep = userStepDAO.selectOne(params);
        UserStepDTO userStepDTO = new UserStepDTO();
        if (null != userStep) {
            BeanUtils.copyProperties(userStep, userStepDTO);
        }
        return userStepDTO;
    }

    @Override
    public List<UserStepDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserStepDTO> resultList = userStepDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userStepDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userStepDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userStepDAO.groupCount(conditions);
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
        return userStepDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userStepDAO.groupSum(conditions);
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
    public void createUserStep(Long userId, HttpServletRequest request) throws Exception {
        // 创建认证步骤
        Map<String, List<StaticData>> stringObjectMap = staticDataService.findByCodeList(new String[]{USER_STEP});
        List<StaticData> staticDataList = stringObjectMap.get(USER_STEP);
        for (StaticData staticData : staticDataList) {
            UserStepDTO userStepDTO = createUserStepDTO(userId, new Integer(staticData.getValue()));
            saveUserStep(userStepDTO, request);
        }
    }

    private UserStepDTO createUserStepDTO(Long userId, Integer step) {
        UserStepDTO userStepDTO = new UserStepDTO();
        userStepDTO.setUserId(userId);
        userStepDTO.setStep(step);
        return userStepDTO;
    }

    @Async("taskExecutor")
    @Override
    public void userStepModifyAndUserStepLogSave(Long userId, Integer step, Integer stepState, String reason, String batchNo, String kycInfo, HttpServletRequest request){
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        params.put("step", step);
        UserStepDTO userStepDTO = findOneUserStep(params);
        try {
            if (StaticDataEnum.USER_STEP_1.getCode() == step) {
                kycStepRecord(userStepDTO, stepState, reason, batchNo, kycInfo, request);
            } else if (StaticDataEnum.USER_STEP_2.getCode() ==  step) {
                illionStepRecord(userStepDTO, stepState, reason, batchNo, request);
            } else {
                installmentRiskStepStepRecord(userStepDTO, stepState, reason, batchNo, request);
            }
        } catch (Exception e) {
            log.info("user step modify and user step log save failed, userId:{}, step:{}, stepState:{}, error message:{}, e:{}", userId, step, stepState, e.getMessage(), e);
        }
    }

    /**
     * kyc认证记录
     * @param userStepDTO
     * @param stepState
     * @param reason
     * @param batchNo
     */
    private void kycStepRecord(UserStepDTO userStepDTO, Integer stepState, String reason, String batchNo, String kycInfo, HttpServletRequest request) throws Exception {
        UserStepLogDTO userStepLogDTO = new UserStepLogDTO();
        userStepLogDTO.setStepId(userStepDTO.getId());
        userStepLogDTO.setRefuseReason(reason);
        userStepLogDTO.setRiskBatchNo(batchNo);
        userStepLogDTO.setKycInfo(kycInfo);
        if (StaticDataEnum.USER_STEP_LOG_STATE_11.getCode() ==  stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_1.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_11.getCode());
        } else if (StaticDataEnum.USER_STEP_LOG_STATE_12.getCode() == stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_2.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_12.getCode());
        } else if (StaticDataEnum.USER_STEP_LOG_STATE_14.getCode() == stepState){
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_5.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_14.getCode());
        } else {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_3.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_13.getCode());
        }
        updateUserStep(userStepDTO.getId(), userStepDTO, request);
        userStepLogService.saveUserStepLog(userStepLogDTO, request);
    }

    /**
     * illion认证记录
     * @param userStepDTO
     * @param stepState
     * @param reason
     * @param batchNo
     */
    private void illionStepRecord(UserStepDTO userStepDTO, Integer stepState, String reason, String batchNo, HttpServletRequest request) throws Exception {
        UserStepLogDTO userStepLogDTO = new UserStepLogDTO();
        userStepLogDTO.setStepId(userStepDTO.getId());
        userStepLogDTO.setRefuseReason(reason);
        userStepLogDTO.setRiskBatchNo(batchNo);
        if (StaticDataEnum.USER_STEP_LOG_STATE_21.getCode() ==  stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_1.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_21.getCode());
        } else if (StaticDataEnum.USER_STEP_LOG_STATE_22.getCode() == stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_2.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_22.getCode());
        }
        updateUserStep(userStepDTO.getId(), userStepDTO, request);
        userStepLogService.saveUserStepLog(userStepLogDTO, request);
    }

    /**
     * 分期付认证记录
     * @param userStepDTO
     * @param stepState
     * @param reason
     * @param batchNo
     */
    private void installmentRiskStepStepRecord(UserStepDTO userStepDTO, Integer stepState, String reason, String batchNo, HttpServletRequest request) throws Exception {
        UserStepLogDTO userStepLogDTO = new UserStepLogDTO();
        userStepLogDTO.setStepId(userStepDTO.getId());
        userStepLogDTO.setRefuseReason(reason);
        userStepLogDTO.setRiskBatchNo(batchNo);
        if (StaticDataEnum.USER_STEP_LOG_STATE_31.getCode() ==  stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_1.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_31.getCode());
        } else if (StaticDataEnum.USER_STEP_LOG_STATE_32.getCode() == stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_2.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_32.getCode());
        } else if (StaticDataEnum.USER_STEP_LOG_STATE_33.getCode() == stepState) {
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_3.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_33.getCode());
        } else {
            // 分期付落地审核成功，此时修改illion阶段为审核中
            userStepDTO.setStepState(StaticDataEnum.USER_STEP_STATE_1.getCode());
            userStepLogDTO.setStepStatus(StaticDataEnum.USER_STEP_LOG_STATE_31.getCode());
            // 查询illion阶段, 并更正为认证中
            //todo map默认大小设置
            Map<String, Object> params = new HashMap<>(1);
            params.put("userId", userStepDTO.getUserId());
            params.put("step", StaticDataEnum.USER_STEP_2.getCode());
            //todo 校验,如果查不到 后端返回的是 new UserStepDTO()
            UserStepDTO illionStep = findOneUserStep(params);
            illionStep.setStepState(StaticDataEnum.USER_STEP_STATE_3.getCode());
            updateUserStep(illionStep.getId(), illionStep, request);
        }
        updateUserStep(userStepDTO.getId(), userStepDTO, request);
        userStepLogService.saveUserStepLog(userStepLogDTO, request);
    }

    @Override
    public List<UserStepDTO> findUserStepByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        List<UserStepDTO> userStepDTOList = find(params, null, null);
        return userStepDTOList;
    }

    @Override
    public UserStepLogDTO findUserStepLatestLog(Long userId, Integer step) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        params.put("step", step);
        UserStepDTO userStepDTO = findOneUserStep(params);
        UserStepLogDTO userStepLogDTO = userStepLogService.findLatestStepLog(userStepDTO.getId());
        return userStepLogDTO;
    }
}
