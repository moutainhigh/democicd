package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.RechargeFlowDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.dao.WithholdFlowDAO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.WithholdFlow;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.uwallet.pay.main.util.UploadFileUtil.getLang;

/**
 * <p>
 * 代收三方流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 代收三方流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:50:03
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class WithholdFlowServiceImpl extends BaseServiceImpl implements WithholdFlowService {

    @Autowired
    private WithholdFlowDAO withholdFlowDAO;

    @Autowired
    private RechargeFlowService rechargeFlowService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Override
    public Long saveWithholdFlow(@NonNull WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {
        WithholdFlow withholdFlow = BeanUtil.copyProperties(withholdFlowDTO, new WithholdFlow());
        log.info("save WithholdFlow:{}", withholdFlow);
        if (request != null) {
            withholdFlow = (WithholdFlow) this.packAddBaseProps(withholdFlow, request);
        } else {
            long now = System.currentTimeMillis();
            withholdFlow.setId(SnowflakeUtil.generateId());
            withholdFlow.setCreatedDate(now);
            withholdFlow.setModifiedDate(now);
            withholdFlow.setStatus(1);
        }
        if (withholdFlowDAO.insert(withholdFlow) != 1) {
            log.error("insert error, data:{}", withholdFlow);
            throw new BizException("Insert withholdFlow Error!");
        }
        return withholdFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithholdFlowList(@NonNull List<WithholdFlow> withholdFlowList, HttpServletRequest request) throws BizException {
        if (withholdFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = withholdFlowDAO.insertList(withholdFlowList);
        if (rows != withholdFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, withholdFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateWithholdFlow(@NonNull Long id, @NonNull WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update withholdFlowDTO:{}", withholdFlowDTO);
        WithholdFlow withholdFlow = BeanUtil.copyProperties(withholdFlowDTO, new WithholdFlow());
        withholdFlow.setId(id);
        if (request != null) {
            withholdFlow = (WithholdFlow) this.packModifyBaseProps(withholdFlow, request);
        } else {
            withholdFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = withholdFlowDAO.update(withholdFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", withholdFlowDTO);
            throw new BizException("update withholdFlow Error!");
        }
    }

    @Override
    public void updateWithholdFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        withholdFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteWithholdFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = withholdFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteWithholdFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = withholdFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public WithholdFlowDTO findWithholdFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        WithholdFlowDTO withholdFlowDTO = withholdFlowDAO.selectOneDTO(params);
        return withholdFlowDTO;
    }

    @Override
    public WithholdFlowDTO findOneWithholdFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        WithholdFlow withholdFlow = withholdFlowDAO.selectOne(params);
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        if (null != withholdFlow) {
            BeanUtils.copyProperties(withholdFlow, withholdFlowDTO);
        }
        return withholdFlowDTO;
    }

    @Override
    public List<WithholdFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WithholdFlowDTO> resultList = withholdFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<WithholdFlowDTO> withholdFlowList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WithholdFlowDTO> resultList = withholdFlowDAO.withholdFlowList(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return withholdFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return withholdFlowDAO.count(params);
    }

    @Override
    public int countWithholdFlowList(@NonNull Map<String, Object> params) {
        return withholdFlowDAO.countWithholdFlowList(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = withholdFlowDAO.groupCount(conditions);
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
        return withholdFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = withholdFlowDAO.groupSum(conditions);
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
    public List<Map> selectResults(Long[] flowIds, HttpServletRequest request) throws BizException {
        if (flowIds == null || flowIds.length == 0) {
            log.info("flow ids can not empty, data:{}", flowIds);
            throw new BizException(I18nUtils.get("search.info.not.empty", getLang(request)));
        }
        return withholdFlowDAO.selectResults(flowIds);
    }

    @Override
    public void saveOmiPayOrderNo (JSONObject requestData, HttpServletRequest request) throws BizException {
        //查询流水记录
        Map<String, Object> params = new HashMap<>(1);
        params.put("flowId", requestData.getLong("flowId"));
        WithholdFlowDTO withholdFlowDTO = findOneWithholdFlow(params);
        //如果返回码成功则保存单号
        if (!StringUtils.isEmpty(requestData.getString("omiPayOrderNo"))) {
            //保存omiPay订单号
            withholdFlowDTO.setOmiPayOrderNo(requestData.getString("omiPayOrderNo"));
            // sdk返回状态为 0 ，则直接订单为失败
            if (requestData.getInteger("paymentState") != null && requestData.getInteger("paymentState").equals(new Integer(0))) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(withholdFlowDTO.getFlowId());
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
                qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
                updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            log.info("save withhold flow dto, dto:{}", withholdFlowDTO);
            updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, request);
        } else {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            log.info("save withhold flow dto, dto:{}", withholdFlowDTO);
            updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, request);
            //更新流水记录
            if (withholdFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode()) {
                RechargeFlowDTO rechargeFlowDTO = rechargeFlowService.findRechargeFlowById(requestData.getLong("flowId"));
                rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
                log.info("save recharge flow dto, dto:{}", rechargeFlowDTO);
                rechargeFlowService.updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, request);
            } else {
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(requestData.getLong("flowId"));
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                log.info("save qr pay flow dto, dto:{}", qrPayFlowDTO);
                qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
                //限额回滚
                rechargeFlowService.channelLimitRollback(qrPayFlowDTO.getCreatedDate(),qrPayFlowDTO.getGatewayId(),withholdFlowDTO.getTransAmount(),null);

            }
        }
    }

    @Override
    public void updateWithholdFlowForConcurrency(@NonNull Long id, @NonNull WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update withholdFlowDTO:{}", withholdFlowDTO);
        WithholdFlow withholdFlow = BeanUtil.copyProperties(withholdFlowDTO, new WithholdFlow());
        withholdFlow.setId(id);
        if (request != null) {
            withholdFlow = (WithholdFlow) this.packModifyBaseProps(withholdFlow, request);
        } else {
            withholdFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = withholdFlowDAO.updateForConcurrency(withholdFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", withholdFlowDTO);
            throw new BizException("update withholdFlow Error!");
        }
    }


}
