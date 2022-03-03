package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.dao.ParametersConfigDAO;
import com.uwallet.pay.main.model.dto.ParametersConfigDTO;
import com.uwallet.pay.main.model.entity.ParametersConfig;
import com.uwallet.pay.main.service.ParametersConfigService;
import com.uwallet.pay.main.service.RechargeRouteService;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 系统配置表增加小额免密金额
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 系统配置表增加小额免密金额
 * @author: zhoutt
 * @date: Created in 2019-12-23 16:55:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ParametersConfigServiceImpl extends BaseServiceImpl implements ParametersConfigService {

    @Autowired
    private ParametersConfigDAO parametersConfigDAO;

    @Autowired
    private ServerService serverService;

    @Autowired
    private RechargeRouteService rechargeRouteService;

    /**
     * redis 工具类
     */
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void saveParametersConfig(@NonNull ParametersConfigDTO parametersConfigDTO, HttpServletRequest request) throws BizException {
        ParametersConfig parametersConfig = BeanUtil.copyProperties(parametersConfigDTO, new ParametersConfig());
        log.info("save ParametersConfig:{}", parametersConfig);
        if (parametersConfigDAO.insert((ParametersConfig) this.packAddBaseProps(parametersConfig, request)) != 1) {
            log.error("insert error, data:{}", parametersConfig);
            throw new BizException("Insert parametersConfig Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveParametersConfigList(@NonNull List<ParametersConfig> parametersConfigList, HttpServletRequest request) throws BizException {
        if (parametersConfigList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = parametersConfigDAO.insertList(parametersConfigList);
        if (rows != parametersConfigList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, parametersConfigList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateParametersConfig(@NonNull Long id, @NonNull ParametersConfigDTO parametersConfigDTO, HttpServletRequest request) throws BizException {
        log.info("full update parametersConfigDTO:{}", parametersConfigDTO);
        if (parametersConfigDTO.getAvoidCloseAmount() == null || parametersConfigDTO.getAvoidCloseAmount().signum() == -1 || (parametersConfigDTO.getAvoidCloseAmount().compareTo(new BigDecimal("9999.99"))) == 1) {
            throw new BizException("parametersConfig Error!");
        }
        parametersConfigDTO.setServiceCharge(MathUtils.divide(parametersConfigDTO.getServiceCharge(), new BigDecimal(100)));
        parametersConfigDTO.setDiscountRate(MathUtils.divide(parametersConfigDTO.getDiscountRate(), new BigDecimal(100)));
        parametersConfigDTO.setMerchantDiscountRatePlatformProportion(parametersConfigDTO.getMerchantDiscountRatePlatformProportion().divide(new BigDecimal("100")));
        parametersConfigDTO.setExtraDiscountPayPlatform(MathUtils.divide(parametersConfigDTO.getExtraDiscountPayPlatform(), new BigDecimal(100)));
        parametersConfigDTO.setExtraDiscountCreditPlatform(MathUtils.divide(parametersConfigDTO.getExtraDiscountCreditPlatform(), new BigDecimal(100)));
        ParametersConfig parametersConfig = BeanUtil.copyProperties(parametersConfigDTO, new ParametersConfig());
        parametersConfig.setId(id);
        int cnt = parametersConfigDAO.update((ParametersConfig) this.packModifyBaseProps(parametersConfig, request));
        if (cnt != 1) {
            log.error("update error, data:{}", parametersConfigDTO);
            throw new BizException("update parametersConfig Error!");
        }

        //add by zhangzeyuan 将单卡失败次数、当日最大失败次数配置 更新到redis
        Integer cardFailedMax = parametersConfigDTO.getCardFailedMax();
        Integer userCardFailedMax = parametersConfigDTO.getUserCardFailedMax();
        if(null != cardFailedMax){
            redisUtils.set(Constant.CARD_PAY_FAILED_SINGLE_CONFIG, cardFailedMax);
        }
        if(null != userCardFailedMax) {
            redisUtils.set(Constant.USER_PAY_FAILED_MAX_CONFIG, userCardFailedMax);
        }

        // (1)同步修改充值转账路由表
//        Map<String,Object> param = new HashMap<>(1);
//        param.put("rate",parametersConfigDTO.getServiceCharge());
//        try {
//            rechargeRouteService.update(param);
//        } catch (BizException e) {
//            log.error("update rechargeRoute parametersConfig failed", parametersConfigDTO, e.getMessage(), e);
//            throw new BizException("update parametersConfig Error!");
//        }
        // (2)调取分期付系统
        if (parametersConfigDTO.getDiscountRateChange()) {
            creditRateChange(1, parametersConfigDTO.getDiscountRate());
        }
        if (parametersConfigDTO.getExtraDiscountCreditPlatformChange()) {
            creditRateChange(10, parametersConfigDTO.getExtraDiscountCreditPlatform());
        }
    }

    private void creditRateChange(Integer type, BigDecimal value) throws BizException {
        Map<String,Object> amtOutMap = new HashMap<>(2);
        amtOutMap.put("type",type);
        amtOutMap.put("value",value);
        try {
            serverService.updateParametersConfig(JSONObject.parseObject(JSON.toJSONString(amtOutMap)));
        } catch (Exception e) {
            log.error("update uWallet credit parametersConfig failed, data:{}, error message:{}, e:{}",  amtOutMap, e.getMessage(), e);
            throw new BizException("update parametersConfig Error!");
        }
    }

    @Override
    public void updateParametersConfigSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        parametersConfigDAO.updatex(params);
    }

    @Override
    public void logicDeleteParametersConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = parametersConfigDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteParametersConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = parametersConfigDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ParametersConfigDTO findParametersConfigById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ParametersConfigDTO parametersConfigDTO = parametersConfigDAO.selectOneDTO(params);
        return parametersConfigDTO;
    }

    @Override
    public ParametersConfigDTO findOneParametersConfig(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ParametersConfig parametersConfig = parametersConfigDAO.selectOne(params);
        ParametersConfigDTO parametersConfigDTO = new ParametersConfigDTO();
        if (null != parametersConfig) {
            BeanUtils.copyProperties(parametersConfig, parametersConfigDTO);
        }
        return parametersConfigDTO;
    }

    @Override
    public List<ParametersConfigDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ParametersConfigDTO> resultList = parametersConfigDAO.selectDTO(params);
        if (resultList != null && resultList.size()>0) {
            for (ParametersConfigDTO parametersConfigDTO : resultList) {
                parametersConfigDTO.setServiceCharge(MathUtils.multiply(parametersConfigDTO.getServiceCharge(), new BigDecimal("100")));
                parametersConfigDTO.setDiscountRate(MathUtils.multiply(parametersConfigDTO.getDiscountRate(), new BigDecimal("100")));
                parametersConfigDTO.setMerchantDiscountRatePlatformProportion(parametersConfigDTO.getMerchantDiscountRatePlatformProportion().multiply(new BigDecimal("100")));
                parametersConfigDTO.setExtraDiscountPayPlatform(MathUtils.multiply(parametersConfigDTO.getExtraDiscountPayPlatform(), new BigDecimal("100")));
                parametersConfigDTO.setExtraDiscountCreditPlatform(MathUtils.multiply(parametersConfigDTO.getExtraDiscountCreditPlatform(), new BigDecimal("100")));
            }
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
        return parametersConfigDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return parametersConfigDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = parametersConfigDAO.groupCount(conditions);
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
        return parametersConfigDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = parametersConfigDAO.groupSum(conditions);
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
