package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.GatewayDAO;
import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.entity.Gateway;
import com.uwallet.pay.main.service.GatewayService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.RegexUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 支付渠道信息表

 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 支付渠道信息表

 * @author: Strong
 * @date: Created in 2019-12-12 10:16:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class GatewayServiceImpl extends BaseServiceImpl implements GatewayService {

    @Autowired
    private GatewayDAO gatewayDAO;

    @Override
    public void saveGateway(@NonNull GatewayDTO gatewayDTO, HttpServletRequest request) throws BizException {
        Gateway gateway = BeanUtil.copyProperties(gatewayDTO, new Gateway());
        log.info("save Gateway:{}", gateway);
        if (gatewayDAO.insert((Gateway) this.packAddBaseProps(gateway, request)) != 1) {
            log.error("insert error, data:{}", gateway);
            throw new BizException("Insert gateway Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGatewayList(@NonNull List<Gateway> gatewayList, HttpServletRequest request) throws BizException {
        if (gatewayList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = gatewayDAO.insertList(gatewayList);
        if (rows != gatewayList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, gatewayList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateGateway(@NonNull Long id, @NonNull GatewayDTO gatewayDTO, HttpServletRequest request) throws BizException {
        log.info("full update gatewayDTO:{}", gatewayDTO);
        Gateway gateway = BeanUtil.copyProperties(gatewayDTO, new Gateway());
        gateway.setId(id);
        int cnt = gatewayDAO.update((Gateway) this.packModifyBaseProps(gateway, request));
        if (cnt != 1) {
            log.error("update error, data:{}", gatewayDTO);
            throw new BizException("update gateway Error!");
        }
    }

    @Override
    public void updateGatewaySelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        gatewayDAO.updatex(params);
    }

    @Override
    public void logicDeleteGateway(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = gatewayDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteGateway(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = gatewayDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public GatewayDTO findGatewayById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        GatewayDTO gatewayDTO = gatewayDAO.selectOneDTO(params);
        return gatewayDTO;
    }

    @Override
    public GatewayDTO findOneGateway(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Gateway gateway = gatewayDAO.selectOne(params);
        GatewayDTO gatewayDTO = new GatewayDTO();
        if (null != gateway) {
            BeanUtils.copyProperties(gateway, gatewayDTO);
        }
        return gatewayDTO;
    }

    @Override
    public List<GatewayDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<GatewayDTO> resultList = gatewayDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return gatewayDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return gatewayDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = gatewayDAO.groupCount(conditions);
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
        return gatewayDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = gatewayDAO.groupSum(conditions);
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
    public List<GatewayDTO> getPayType() {
        return gatewayDAO.getPayType();
    }

    @Override
    public Map<String,Object> getChannelFee(JSONObject data, HttpServletRequest request) throws Exception {
//        BigDecimal amount = data.getBigDecimal("amount");
//        if(amount == null || !RegexUtils.isTransAmt(amount.toString())){
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
        Map<String,Object> params = new HashMap<>(8);
        params.put("gatewayType", StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode());
        params.put("state",StaticDataEnum.STATUS_1.getCode());
        List<GatewayDTO> gatewayDTOList = this.find(params, null, null);
        if(gatewayDTOList == null || gatewayDTOList.size() != 1){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
        GatewayDTO gatewayDTO = gatewayDTOList.get(0);
        Map<String,Object> map = new HashMap<>(4);
        if(gatewayDTO.getRate() != null && gatewayDTO.getRate() .compareTo( BigDecimal.ZERO) > 0){
            if(StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() == gatewayDTO.getRateType()){
                map.put("feeType",StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode());
            }else{
                map.put("feeType",StaticDataEnum.CHANNEL_RATE_TYPE_1.getCode());
            }
            map.put("rate",gatewayDTO.getRate());
        }else{
            map.put("feeType",StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode());
            map.put("rate",BigDecimal.ZERO);
        }

//        BigDecimal channelFee = BigDecimal.ZERO;
//        if(gatewayDTO.getRate() != null && gatewayDTO.getRate() .compareTo( BigDecimal.ZERO) > 0){
//            if(StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() == gatewayDTO.getRateType()){
//                channelFee = gatewayDTO.getRate();
//            }else{
//                channelFee = amount.multiply(gatewayDTO.getRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
//            }
//        }
        return map;
    }

    @Override
    public void updateState(Long id, HttpServletRequest request) throws Exception {
        GatewayDTO gatewayDTO = this.findGatewayById(id);
        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        gatewayDTO.setState((gatewayDTO.getState()+1)%2);
        this.updateGateway(id,gatewayDTO,request);
    }

    @Override
    public BigDecimal getGateWayFee(GatewayDTO gatewayDTO, BigDecimal amount) {
        BigDecimal charge;
        if(gatewayDTO == null){
            charge = BigDecimal.ZERO;
        }else if(gatewayDTO.getRateType() != null && StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() == gatewayDTO.getRateType()){
            charge = gatewayDTO.getRate() == null ? BigDecimal.ZERO :  gatewayDTO.getRate();
        }else{
            charge = amount.multiply(gatewayDTO.getRate() == null ? BigDecimal.ZERO :  gatewayDTO.getRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
        }
        return charge;
    }

    @Override
    public JSONObject getPayGateWay(HttpServletRequest request) throws BizException {
        JSONObject param=new JSONObject();
        param.put("gatewayType", 0);
        param.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO oneGateway = this.findOneGateway(param);
        if (oneGateway.getId()==null){
            throw new BizException("channel error");
        }
        param.clear();
        log.info("当前通道:{}",oneGateway);
        Long type = oneGateway.getType();
        param.put("type",type);
        return param;
    }


}
