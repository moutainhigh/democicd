package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.PaymentChannelDAO;
import com.uwallet.pay.main.model.dto.PaymentChannelDTO;
import com.uwallet.pay.main.model.entity.PaymentChannel;
import com.uwallet.pay.main.service.PaymentChannelService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 三方支付通道表
 * </p>
 *
 * @package: com.uwallet.pay.main.main.service.impl
 * @description: 三方支付通道表
 * @author: baixinyue
 * @date: Created in 2020-02-13 10:31:26
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class PaymentChannelServiceImpl extends BaseServiceImpl implements PaymentChannelService {

    @Autowired
    private PaymentChannelDAO paymentChannelDAO;

    @Override
    public void savePaymentChannel(@NonNull PaymentChannelDTO paymentChannelDTO, HttpServletRequest request) throws BizException {
        PaymentChannel paymentChannel = BeanUtil.copyProperties(paymentChannelDTO, new PaymentChannel());
        log.info("save PaymentChannel:{}", paymentChannel);
        if (paymentChannelDAO.insert((PaymentChannel) this.packAddBaseProps(paymentChannel, request)) != 1) {
            log.error("insert error, data:{}", paymentChannel);
            throw new BizException("Insert paymentChannel Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePaymentChannelList(@NonNull List<PaymentChannel> paymentChannelList, HttpServletRequest request) throws BizException {
        if (paymentChannelList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = paymentChannelDAO.insertList(paymentChannelList);
        if (rows != paymentChannelList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, paymentChannelList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePaymentChannel(@NonNull Long id, @NonNull PaymentChannelDTO paymentChannelDTO, HttpServletRequest request) throws BizException {
        log.info("full update paymentChannelDTO:{}", paymentChannelDTO);
        PaymentChannel paymentChannel = BeanUtil.copyProperties(paymentChannelDTO, new PaymentChannel());
        paymentChannel.setId(id);
        int cnt = paymentChannelDAO.update((PaymentChannel) this.packModifyBaseProps(paymentChannel, request));
        if (cnt != 1) {
            log.error("update error, data:{}", paymentChannelDTO);
            throw new BizException("update paymentChannel Error!");
        }
    }

    @Override
    public void updatePaymentChannelSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        paymentChannelDAO.updatex(params);
    }

    @Override
    public void logicDeletePaymentChannel(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = paymentChannelDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePaymentChannel(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = paymentChannelDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PaymentChannelDTO findPaymentChannelById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PaymentChannelDTO paymentChannelDTO = paymentChannelDAO.selectOneDTO(params);
        return paymentChannelDTO;
    }

    @Override
    public PaymentChannelDTO findOnePaymentChannel(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PaymentChannel paymentChannel = paymentChannelDAO.selectOne(params);
        PaymentChannelDTO paymentChannelDTO = new PaymentChannelDTO();
        if (null != paymentChannel) {
            BeanUtils.copyProperties(paymentChannel, paymentChannelDTO);
        }
        return paymentChannelDTO;
    }

    @Override
    public List<PaymentChannelDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PaymentChannelDTO> resultList = paymentChannelDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return paymentChannelDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return paymentChannelDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = paymentChannelDAO.groupCount(conditions);
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
        return paymentChannelDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = paymentChannelDAO.groupSum(conditions);
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

}
