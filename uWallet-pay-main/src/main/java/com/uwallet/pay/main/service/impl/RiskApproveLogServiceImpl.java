package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.RiskApproveLogDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.RiskApproveLog;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 用户风控审核日志
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户风控审核日志
 * @author: baixinyue
 * @date: Created in 2020-03-25 10:11:54
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class RiskApproveLogServiceImpl extends BaseServiceImpl implements RiskApproveLogService {

    @Autowired
    private RiskApproveLogDAO riskApproveLogDAO;

    @Autowired
    private ServerService serverService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private UserStepService userStepService;

    @Value("${uWallet.invest}")
    private String investUrl;

    @Override
    public void saveRiskApproveLog(@NonNull RiskApproveLogDTO riskApproveLogDTO, HttpServletRequest request) throws BizException {
        RiskApproveLog riskApproveLog = BeanUtil.copyProperties(riskApproveLogDTO, new RiskApproveLog());
        log.info("save RiskApproveLog:{}", riskApproveLog);
        if (riskApproveLogDAO.insert((RiskApproveLog) this.packAddBaseProps(riskApproveLog, request)) != 1) {
            log.error("insert error, data:{}", riskApproveLog);
            throw new BizException("Insert riskApproveLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRiskApproveLogList(@NonNull List<RiskApproveLog> riskApproveLogList, HttpServletRequest request) throws BizException {
        if (riskApproveLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = riskApproveLogDAO.insertList(riskApproveLogList);
        if (rows != riskApproveLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, riskApproveLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRiskApproveLog(@NonNull Long id, @NonNull RiskApproveLogDTO riskApproveLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update riskApproveLogDTO:{}", riskApproveLogDTO);
        RiskApproveLog riskApproveLog = BeanUtil.copyProperties(riskApproveLogDTO, new RiskApproveLog());
        riskApproveLog.setId(id);
        int cnt = riskApproveLogDAO.update((RiskApproveLog) this.packModifyBaseProps(riskApproveLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", riskApproveLogDTO);
            throw new BizException("update riskApproveLog Error!");
        }
    }

    @Override
    public void updateRiskApproveLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        riskApproveLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteRiskApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = riskApproveLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRiskApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = riskApproveLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RiskApproveLogDTO findRiskApproveLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RiskApproveLogDTO riskApproveLogDTO = riskApproveLogDAO.selectOneDTO(params);
        return riskApproveLogDTO;
    }

    @Override
    public RiskApproveLogDTO findOneRiskApproveLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RiskApproveLog riskApproveLog = riskApproveLogDAO.selectOne(params);
        RiskApproveLogDTO riskApproveLogDTO = new RiskApproveLogDTO();
        if (null != riskApproveLog) {
            BeanUtils.copyProperties(riskApproveLog, riskApproveLogDTO);
        }
        return riskApproveLogDTO;
    }

    @Override
    public List<RiskApproveLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RiskApproveLogDTO> resultList = riskApproveLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return riskApproveLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return riskApproveLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = riskApproveLogDAO.groupCount(conditions);
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
        return riskApproveLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = riskApproveLogDAO.groupSum(conditions);
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
    public JSONObject getUserInfo(Long userId, HttpServletRequest request) throws Exception {
        JSONObject userInfo = null;
        try {
            userInfo = serverService.userInfoByQRCode(userId);
        } catch (Exception e) {
            log.info("find user info failed, e: {}, error message:{}", e, e.getMessage());
            throw new BizException(e.getMessage());
        }
        return userInfo;
    }

    @Override
    public void check(JSONObject checkData, HttpServletRequest request) throws Exception {
        log.info("risk check, data:{}", checkData);
        Integer state = checkData.getInteger("state");
        List<String> openBusiness = (List<String>) JSONObject.parse(checkData.getJSONArray("openBusiness").toJSONString());
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", checkData.getLong("id"));
        params.put("state", StaticDataEnum.RISK_MANUAL_CHECK_0.getCode());
        RiskApproveLogDTO riskApproveLogDTO = findOneRiskApproveLog(params);
        UserDTO userDTO = userService.findUserById(riskApproveLogDTO.getUserId());
        //更新审核记录状态
        riskApproveLogDTO.setState(state);
        riskApproveLogDTO.setRemark(checkData.getString("remark"));
        riskApproveLogDTO.setApprovedBy(getUserId(request));
        riskApproveLogDTO.setOpenBusiness(StringUtils.strip(openBusiness.toString(), "[]"));
        updateRiskApproveLog(riskApproveLogDTO.getId(), riskApproveLogDTO, request);
        if (state == StaticDataEnum.RISK_MANUAL_CHECK_1.getCode()) {
            //遍历开通业务
            openBusiness.forEach(system -> {
                if (system.equals(StaticDataEnum.SYSTEM_ID_10.getMessage())) {
                    userDTO.setPaymentState(StaticDataEnum.USER_BUSINESS_5.getCode());
                } else if (system.equals(StaticDataEnum.SYSTEM_ID_20.getMessage())) {
                    userDTO.setInvestState(StaticDataEnum.USER_BUSINESS_5.getCode());
                } else {
                    userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_5.getCode());
                }
            });
//            if (userDTO.getPaymentState().intValue() != StaticDataEnum.USER_BUSINESS_5.getCode()) {
//                userDTO.setPaymentState(StaticDataEnum.USER_BUSINESS_2.getCode());
//            }
//            if (userDTO.getInvestState().intValue() == StaticDataEnum.USER_BUSINESS_5.getCode()) {
//                //人工审核成功调用理财系统同步数据
//                String investInfoSupplementUrl = investUrl + "/server/userSynchronization";
//                JSONObject returnMsg = JSONObject.parseObject(HttpClientUtils.sendPost(investInfoSupplementUrl, riskApproveLogDTO.getData()));
//                if (ErrorCodeEnum.FAIL_CODE.getCode().equals(returnMsg.getString("code"))) {
//                    throw new BizException(I18nUtils.get("query.failed", getLang(request)));
//                }
//            } else {
//                userDTO.setInvestState(StaticDataEnum.USER_BUSINESS_2.getCode());
//            }
            //人工审核成功调用理财系统同步数据
//            String investInfoSupplementUrl = investUrl + "/server/userSynchronization";
//            JSONObject returnMsg = JSONObject.parseObject(HttpClientUtils.sendPost(investInfoSupplementUrl, riskApproveLogDTO.getData()));
//            if (ErrorCodeEnum.FAIL_CODE.getCode().equals(returnMsg.getString("code"))) {
//                throw new BizException(I18nUtils.get("query.failed", getLang(request)));
//            }
            if (userDTO.getInstallmentState() != StaticDataEnum.USER_BUSINESS_5.getCode()) {
                userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_2.getCode());
            }
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_11.getCode(), null, null, null, request);
        } else if (state == StaticDataEnum.RISK_MANUAL_CHECK_2.getCode()) {
//            userDTO.setPaymentState(StaticDataEnum.USER_BUSINESS_4.getCode());
//            userDTO.setInvestState(StaticDataEnum.USER_BUSINESS_4.getCode());
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_4.getCode());
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_12.getCode(), null, null, null, request);
        }
        //更新用户理财开通状态
        userService.updateUser(userDTO.getId(), userDTO, request);
    }

    @Override
    public List<InvestApproveDTO> approveList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<InvestApproveDTO> resultList = riskApproveLogDAO.approveList(params);
        return resultList;
    }

    @Override
    public int approveListCount(Map<String, Object> params) {
        return riskApproveLogDAO.approveListCount(params);
    }

    @Override
    public List<InvestApproveDTO> approveLogList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<InvestApproveDTO> resultList = riskApproveLogDAO.approveLogList(params);
        return resultList;
    }

    @Override
    public int approveLogCount(Map<String, Object> params) {
        return riskApproveLogDAO.approveLogCount(params);
    }

}
