package com.uwallet.pay.main.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.PosQrPayFlowDAO;
import com.uwallet.pay.main.model.dto.PosQrPayFlowDTO;
import com.uwallet.pay.main.model.dto.PosTransactionRecordDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.PosQrPayFlow;
import com.uwallet.pay.main.service.PosQrPayFlowService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
 * @package: com.fenmi.generator.service.impl
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2021-03-22 15:46:35
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class PosQrPayFlowServiceImpl extends BaseServiceImpl implements PosQrPayFlowService {

    @Resource
    private PosQrPayFlowDAO posQrPayFlowDAO;
    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public void savePosQrPayFlow(@NonNull PosQrPayFlowDTO posQrPayFlowDTO, HttpServletRequest request) throws BizException {
        PosQrPayFlow posQrPayFlow = BeanUtil.copyProperties(posQrPayFlowDTO, new PosQrPayFlow());
        log.info("save PosQrPayFlow:{}", posQrPayFlow);
        if (posQrPayFlowDAO.insert((PosQrPayFlow) this.packAddBaseProps(posQrPayFlow, request)) != 1) {
            log.error("insert error, data:{}", posQrPayFlow);
            throw new BizException("Insert posQrPayFlow Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePosQrPayFlowList(@NonNull List<PosQrPayFlow> posQrPayFlowList, HttpServletRequest request) throws BizException {
        if (posQrPayFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = posQrPayFlowDAO.insertList(posQrPayFlowList);
        if (rows != posQrPayFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, posQrPayFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePosQrPayFlow(@NonNull Long id, @NonNull PosQrPayFlowDTO posQrPayFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update posQrPayFlowDTO:{}", posQrPayFlowDTO);
        PosQrPayFlow posQrPayFlow = BeanUtil.copyProperties(posQrPayFlowDTO, new PosQrPayFlow());
        posQrPayFlow.setId(id);
        int cnt = posQrPayFlowDAO.update((PosQrPayFlow) this.packModifyBaseProps(posQrPayFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", posQrPayFlowDTO);
            throw new BizException("update posQrPayFlow Error!");
        }
    }

    @Override
    public void updatePosQrPayFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        posQrPayFlowDAO.updatex(params);
    }

    @Override
    public void logicDeletePosQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = posQrPayFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePosQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = posQrPayFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PosQrPayFlowDTO findPosQrPayFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PosQrPayFlowDTO posQrPayFlowDTO = posQrPayFlowDAO.selectOneDTO(params);
        return posQrPayFlowDTO;
    }

    @Override
    public PosQrPayFlowDTO findOnePosQrPayFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PosQrPayFlow posQrPayFlow = posQrPayFlowDAO.selectOne(params);
        PosQrPayFlowDTO posQrPayFlowDTO = new PosQrPayFlowDTO();
        if (null != posQrPayFlow) {
            BeanUtils.copyProperties(posQrPayFlow, posQrPayFlowDTO);
        }
        return posQrPayFlowDTO;
    }

    @Override
    public List<PosQrPayFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PosQrPayFlowDTO> resultList = posQrPayFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return posQrPayFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return posQrPayFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = posQrPayFlowDAO.groupCount(conditions);
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
        return posQrPayFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = posQrPayFlowDAO.groupSum(conditions);
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

    /**
     * pos 扫描二维码获取订单金额
     *
     * @param jsonObject
     * @param request
     * @return
     */
    @Override
    public JSONObject showPosTransAmount(JSONObject jsonObject, HttpServletRequest request) throws BizException {
        String transNo = jsonObject.getJSONObject("data").getString("transNo");
        if (StringUtils.isBlank(transNo)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        //查询 订单状态为创建成功未处理 状态的 订单信息
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("showThirdTransNo", transNo);
        PosQrPayFlow posQrPayFlow = posQrPayFlowDAO.selectOne(map);
        if(Objects.isNull(posQrPayFlow)){
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }
        int orderStatus = posQrPayFlow.getOrderStatus().intValue();
        if(StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode() == orderStatus
            || StaticDataEnum.POS_ORDER_STATUS_SUCCESS.getCode() == orderStatus
            || StaticDataEnum.POS_ORDER_STATUS_FAIL.getCode() == orderStatus){
            //如果订单状态为 支付成功、支付失败、处理中 则返回失败
            throw new BizException(I18nUtils.get("pos.qrcode.disabled", getLang(request)));
        }

        //更新订单状态
        PosQrPayFlow updateRecord = new PosQrPayFlow();
        updateRecord.setId(posQrPayFlow.getId());
        updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_SCANNED.getCode());
        posQrPayFlowDAO.update(updateRecord);

        if(request == null ){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        Long userId = getUserId(request);
        if(userId == null ){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        UserDTO userDTO = userService.findUserById(userId);
        if(userDTO == null || userDTO.getId() == null){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }


        JSONObject result = new JSONObject(3);
        result.put("transAmount", posQrPayFlow.getTransAmount().toString());
        result.put("merchantId", posQrPayFlow.getMerchantId().toString());
        result.put("stripeState",userDTO.getStripeState() == null ? 0 : userDTO.getStripeState());
        return result;
    }

    /**
     * 获取POS订单历史记录列表
     *
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.PosQrPayFlowDTO>
     * @author zhangzeyuan
     * @date 2021/3/23 13:54
     */
    @Override
    public List<PosTransactionRecordDTO> listPosTransaction(Map<String, Object> params) {
        return posQrPayFlowDAO.listPosTransaction(params);
    }

    /**
     * 获取POS订单历史记录列表
     *
     * @param params
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/3/23 13:57
     */
    @Override
    public Integer countPosTransaction(Map<String, Object> params) {
        return posQrPayFlowDAO.countPosTransaction(params);
    }

    /**
     * 根据三方订单号 更新系统订单号
     *
     * @param sysTransNo
     * @param thirdNo
     * @author zhangzeyuan
     * @date 2021/3/25 15:50
     */
    @Override
    public void updateSysTransNoByThirdNo(String sysTransNo, String thirdNo, Long userId, Long modifyTime) {
        posQrPayFlowDAO.updateSysTransNoByThirdTransNo(sysTransNo, thirdNo, userId, modifyTime);
    }

    /**
     * 根据系统订单号更新流水状态
     *
     * @param sysTransNo
     * @param orderStatus
     * @param userId
     * @param modifyTime
     * @author zhangzeyuan
     * @date 2021/3/30 10:32
     */
    @Override
    public void updateOrderStatusBySysTransNo(String sysTransNo, Integer orderStatus, Long userId, Long modifyTime) throws BizException{
        int modifyCount = posQrPayFlowDAO.updateOrderStatusBySysTransNo(sysTransNo, orderStatus, userId, modifyTime);
        if (modifyCount != 1) {
            log.info("update pos qr_pay_flow 0 , sysTransNo:{}, orderStatus:{}", sysTransNo, orderStatus);
//            throw new BizException("update pos_qrPayFlow Error!");
        }
    }

}
