package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.WholeSalesFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.model.entity.WholeSalesFlow;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 整体销售流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 整体销售流水表
 * @author: zhoutt
 * @date: Created in 2020-10-17 14:33:59
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class WholeSalesFlowServiceImpl extends BaseServiceImpl implements WholeSalesFlowService {

    @Autowired
    private WholeSalesFlowDAO wholeSalesFlowDAO;

    @Autowired
    @Lazy
    private MerchantService merchantService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private UserActionService userActionService;

    @Autowired
    @Lazy
    private MerchantApplicationService merchantApplicationService;
    /**
     * 整体出售最小金额
     */
    private final static BigDecimal MIN_WHOLE_SALE_AMOUNT = new BigDecimal("1");

    private final static BigDecimal MAX_WHOLE_SALE_AMOUNT = new BigDecimal("10000");

    @Override
    public Long saveWholeSalesFlow(@NonNull WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws BizException {
        WholeSalesFlow wholeSalesFlow = BeanUtil.copyProperties(wholeSalesFlowDTO, new WholeSalesFlow());
        log.info("save WholeSalesFlow:{}", wholeSalesFlow);
        WholeSalesFlow wholeSalesFlow1=(WholeSalesFlow) this.packAddBaseProps(wholeSalesFlow, request);
        if (wholeSalesFlowDAO.insert(wholeSalesFlow1) != 1) {
            log.error("insert error, data:{}", wholeSalesFlow);
            throw new BizException("Insert wholeSalesFlow Error!");
        }
        return wholeSalesFlow1.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWholeSalesFlowList(@NonNull List<WholeSalesFlow> wholeSalesFlowList, HttpServletRequest request) throws BizException {
        if (wholeSalesFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = wholeSalesFlowDAO.insertList(wholeSalesFlowList);
        if (rows != wholeSalesFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, wholeSalesFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateWholeSalesFlow(@NonNull Long id, @NonNull WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update wholeSalesFlowDTO:{}", wholeSalesFlowDTO);
        WholeSalesFlow wholeSalesFlow = BeanUtil.copyProperties(wholeSalesFlowDTO, new WholeSalesFlow());
        wholeSalesFlow.setId(id);
        if (request != null) {
            wholeSalesFlow = (WholeSalesFlow) this.packModifyBaseProps(wholeSalesFlow, request);
        } else {
            wholeSalesFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = wholeSalesFlowDAO.update(wholeSalesFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", wholeSalesFlowDTO);
            throw new BizException("update wholeSalesFlow Error!");
        }
    }

    @Override
    public void updateWholeSalesFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        wholeSalesFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteWholeSalesFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = wholeSalesFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteWholeSalesFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = wholeSalesFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public WholeSalesFlowDTO findWholeSalesFlowById(@NonNull Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowDAO.selectOneDTO(params);
        BigDecimal cal = new BigDecimal("100");
        if (wholeSalesFlowDTO.getAmount() != null && wholeSalesFlowDTO.getMerchantDiscount() != null) {
            wholeSalesFlowDTO.setSettlementAmount(wholeSalesFlowDTO.getAmount().multiply(new BigDecimal("1").subtract(wholeSalesFlowDTO.getMerchantDiscount()).setScale(2, RoundingMode.HALF_UP)));
        }
        wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().multiply(cal));
        wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().multiply(cal));
        wholeSalesFlowDTO.setCustomerPayDiscount(wholeSalesFlowDTO.getCustomerPayDiscount().multiply(cal));
        MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
        if (merchantDTO.getId() == null) {
            throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
        }
        UserDTO userDTO = userService.findUserById(merchantDTO.getUserId());
        wholeSalesFlowDTO.setEmail(userDTO.getEmail());
        wholeSalesFlowDTO.setPracticalName(merchantDTO.getPracticalName());
        return wholeSalesFlowDTO;
    }

    @Override
    public WholeSalesFlowDTO findOneWholeSalesFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        WholeSalesFlow wholeSalesFlow = wholeSalesFlowDAO.selectOne(params);
        WholeSalesFlowDTO wholeSalesFlowDTO = new WholeSalesFlowDTO();
        if (null != wholeSalesFlow) {
            BeanUtils.copyProperties(wholeSalesFlow, wholeSalesFlowDTO);
        }
        return wholeSalesFlowDTO;
    }

    @Override
    public List<WholeSalesFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WholeSalesFlowDTO> resultList = wholeSalesFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return wholeSalesFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return wholeSalesFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = wholeSalesFlowDAO.groupCount(conditions);
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
        return wholeSalesFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = wholeSalesFlowDAO.groupSum(conditions);
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
    public int wholeSalesInterestOrderCount(Map<String, Object> params) {
        return wholeSalesFlowDAO.wholeSalesInterestOrderCount(params);
    }

    @Override
    public List<WholeSalesFlowDTO> wholeSaleInterestOrderList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WholeSalesFlowDTO> resultList = wholeSalesFlowDAO.wholeSaleInterestOrderList(params);
        return resultList;
    }

    @Override
    public int wholeSaleOrderCount(Map<String, Object> params) {
        return wholeSalesFlowDAO.wholeSaleOrderCount(params);
    }

    @Override
    public List<WholeSalesFlowDTO> wholeSaleOrderList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WholeSalesFlowDTO> resultList = wholeSalesFlowDAO.wholeSaleOrderList(params);
        BigDecimal cal = new BigDecimal("100");
        resultList.stream().forEach(wholeSalesFlowDTO -> {
            wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerPayDiscount(wholeSalesFlowDTO.getCustomerPayDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
        });
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void interestOrderAudit(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
        MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
        merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_2.getCode());
        // 判断是否后台申请意向
        JSONObject param=new JSONObject();
        param.put("wholeSaleId",wholeSalesFlowDTO.getId());
        MerchantApplicationDTO oneMerchantApplication = merchantApplicationService.findOneMerchantApplication(param);
        if (oneMerchantApplication.getId()!=null){
            if (wholeSalesFlowDTO.getApproveState() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()){
                oneMerchantApplication.setState(StaticDataEnum.APPROVE_STATE_1.getCode());
            }else{
                oneMerchantApplication.setState(StaticDataEnum.APPROVE_STATE_.getCode());
            }
            merchantApplicationService.updateMerchantApplication(oneMerchantApplication.getId(),oneMerchantApplication,request);
        }
        if (wholeSalesFlowDTO.getApproveState() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
            check(wholeSalesFlowDTO, request);
            // 入账
            BigDecimal cal = new BigDecimal("100");
            wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setSettlementAmount(wholeSalesFlowDTO.getAmount().multiply(MathUtils.subtract(new BigDecimal("1"), wholeSalesFlowDTO.getMerchantDiscount())));
            merchantAmountIn(wholeSalesFlowDTO, request);
            /*
               2021-01-26添加逻辑, 在整体出售审批通过时,
               将u_merchant表 have_whole_sell 字段更新为 1:有整体出售额度
             */
            merchantService.updateHaveWholeSell(wholeSalesFlowDTO.getMerchantId(),StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode(),request);
        } else {
            wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_2.getCode());
            merchantDTO.setWholeSaleApproveState(StaticDataEnum.WHOLE_SALE_STATE_2.getCode());
            updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, request);
            merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, request);
        }

        try {
            sendMessage(wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("send whole sale message failed");
        }

    }

    private void check(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findOneParametersConfig(new HashMap<>(2));
        if (wholeSalesFlowDTO.getAmount().compareTo(MIN_WHOLE_SALE_AMOUNT) == -1
                || wholeSalesFlowDTO.getAmount().compareTo(parametersConfigDTO.getWholeSaleAmount()) == 1) {
//            throw new BizException(I18nUtils.get("whole.sale.audit.failed", getLang(request)));
            throw new BizException(I18nUtils.get("whole.sale.apply.amount.error", getLang(request), new String[]{parametersConfigDTO.getWholeSaleAmount().toString()}));
        }
        if (wholeSalesFlowDTO.getMerchantDiscount() == null) {
            throw new BizException(I18nUtils.get("whole.sale.audit.failed", getLang(request)));
        }
        if (wholeSalesFlowDTO.getCustomerDiscount() == null) {
            throw new BizException(I18nUtils.get("whole.sale.audit.failed", getLang(request)));
        }
    }

    @Override
    public void wholeSaleOrderAudit(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        if (wholeSalesFlowDTO.getApproveState() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
            check(wholeSalesFlowDTO, request);
            BigDecimal cal = new BigDecimal("100");
            wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerPayDiscount(wholeSalesFlowDTO.getCustomerPayDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setSettlementAmount(wholeSalesFlowDTO.getAmount().multiply(MathUtils.subtract(new BigDecimal("1"), wholeSalesFlowDTO.getMerchantDiscount())));
            merchantAmountIn(wholeSalesFlowDTO, request);
            /*
               2021-01-26添加逻辑, 在整体出售审批通过时,
               将u_merchant表 have_whole_sell 字段更新为 1:有整体出售额度
             */
            merchantService.updateHaveWholeSell(wholeSalesFlowDTO.getMerchantId(),StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode(),request);
        } else {
            wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_2.getCode());
            updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, request);
        }

        try {
            sendMessage(wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("send whole sale message failed");
        }
    }

    /**
     * 正式订单审核通过入账
     * @param wholeSalesFlowDTO
     * @param request
     * @throws Exception
     */
    private void merchantAmountIn(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        AccountFlowDTO accountFlowDTO = createAccountFlow(wholeSalesFlowDTO, request);
        JSONObject accountData = serverService.getAccountInfo(accountFlowDTO.getUserId());
        JSONObject amountIn = new JSONObject();
        amountIn.put("userId", accountFlowDTO.getUserId());
        amountIn.put("amountInUserId", accountFlowDTO.getUserId());
        amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
        amountIn.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        amountIn.put("channelSerialnumber", accountFlowDTO.getOrderNo());
        amountIn.put("accountId", accountData.getLongValue("id"));
        amountIn.put("transAmount", accountFlowDTO.getTransAmount());
        amountIn.put("transType", accountFlowDTO.getTransType());
        JSONObject msg = null;
        try {
            // 调用账户系统入账，根据结果判断交易状态
            msg = serverService.amountIn(amountIn);
            String code = msg.getString("code");
            if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_1.getCode());
                wholeSalesFlowDTO.setPassTime(System.currentTimeMillis());
                wholeSalesFlowDTO.setOrderType(StaticDataEnum.WHOLE_SALE_ORDER_TYPE_1.getCode());

                MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
                merchantDTO.setWholeSaleUserDiscount(wholeSalesFlowDTO.getCustomerDiscount());
                merchantDTO.setWholeSaleMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount());
                if (merchantDTO.getWholeSaleApproveState().intValue() != StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
                    merchantDTO.setWholeSaleApproveState(StaticDataEnum.WHOLE_SALE_STATE_1.getCode());
                }
                merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, request);

                CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
                creditMerchantDTO.setMerchantId(merchantDTO.getId());
                creditMerchantDTO.setDiscountPackage(wholeSalesFlowDTO.getCustomerDiscount());
                serverService.updateMerchant(merchantDTO.getId(), JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
                log.info("whole sale amount in success, data:{}", msg);
            } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
                log.info("whole sale amount in failed, data:{}", msg);
            } else {
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
                log.info("whole sale amount in doubtful, data:{}", msg);
            }
        } catch (Exception e) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
            log.error("whole sale amount in failed message:{}", e.getMessage());
        } finally {
            updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, request);
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
//            try {
//                //
//                sendMessage(wholeSalesFlowDTO, request);
//            } catch (Exception e) {
//                log.info("send whole sale message failed");
//            }
        }
    }

    /**
     * 创建账户交易流水
     * @param wholeSalesFlowDTO
     * @param request
     * @throws Exception
     */
    private AccountFlowDTO createAccountFlow(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
        Long orderNo = SnowflakeUtil.generateId();
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        accountFlowDTO.setFlowId(wholeSalesFlowDTO.getId());
        accountFlowDTO.setUserId(merchantDTO.getUserId());
        accountFlowDTO.setTransAmount(wholeSalesFlowDTO.getAmount());
        accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        accountFlowDTO.setOrderNo(orderNo);
        accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_23.getCode());
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        Long id = accountFlowService.saveAccountFlow(accountFlowDTO, request);
        accountFlowDTO.setId(id);
        return accountFlowDTO;
    }

    @Override
    public void wholeSaleAmountInFailedHandle() throws Exception {
        log.info("whole sale amount in failed handle");
        Map<String, Object> params = new HashMap<>(16);
        params.put("state", StaticDataEnum.TRANS_STATE_2.getCode());
        params.put("approveState", StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
        List<WholeSalesFlowDTO> wholeSalesFlowDTOList = find(params, null, null);
        wholeSalesFlowDTOList.stream().forEach(wholeSalesFlowDTO -> {
            try {
                merchantAmountIn(wholeSalesFlowDTO, null);
            } catch (Exception e) {
                log.info("whole sale amount in, data:{}, error message:{}, error:{}", wholeSalesFlowDTO, e.getMessage(), e);
            }
        });
    }

    @Override
    public void wholeSaleAmountInDoubfulHandle() throws Exception {
        log.info("whole sale amount in doubleful handle");
        Map<String, Object> params = new HashMap<>(16);
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        params.put("approveState", StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
        params.put("double", StaticDataEnum.STATUS_1.getCode());
        List<WholeSalesFlowDTO> wholeSalesFlowDTOList = find(params, null, null);
        wholeSalesFlowDTOList.forEach(wholeSalesFlowDTO -> {
            params.clear();
            params.put("flowId", wholeSalesFlowDTO.getId());
            params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
            params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_23.getCode());
            AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(params);
            JSONObject data = null;
            // 查询账户流水状态
            try {
                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
            } catch (Exception e) {
                log.error("check doubtful flow failed message:{}", e.getMessage());
            }
            if (data != null) {
                if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_1.getCode());
                    wholeSalesFlowDTO.setPassTime(System.currentTimeMillis());
                    MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
                    merchantDTO.setWholeSaleUserDiscount(wholeSalesFlowDTO.getCustomerDiscount());
                    merchantDTO.setWholeSaleMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount());
                    if (merchantDTO.getWholeSaleApproveState().intValue() != StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
                        merchantDTO.setWholeSaleApproveState(StaticDataEnum.WHOLE_SALE_STATE_1.getCode());
                    }

                    CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
                    creditMerchantDTO.setMerchantId(merchantDTO.getId());
                    creditMerchantDTO.setDiscountPackage(wholeSalesFlowDTO.getCustomerDiscount());
                    try {
                        merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, null);
                        serverService.updateMerchant(merchantDTO.getId(), JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), null);
                    } catch (BizException e) {
                        e.printStackTrace();
                    }
                    log.info("wallet booked success, data:{}", accountFlowDTO);
                } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
                    log.info("wallet booked failed, data:{}", accountFlowDTO);
                } else {
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
                    log.info("wallet booked doubtful, data:{}", accountFlowDTO);
                }
            } else {
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                wholeSalesFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                wholeSalesFlowDTO.setApproveState(StaticDataEnum.WHOLE_SALE_STATE_4.getCode());
                log.info("wallet booked success, failed:{}", accountFlowDTO);
            }
            try {
                accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
                updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, null);
            } catch (BizException e) {
                log.info("account flow update failed, account flow:{}", accountFlowDTO);
            }
        });
    }

    @Override
    public void rateModify(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        if (wholeSalesFlowDTO.getMerchantDiscount() == null || wholeSalesFlowDTO.getCustomerDiscount() == null) {
            throw new BizException(I18nUtils.get("rate.null", getLang(request)));
        }
        BigDecimal cal = new BigDecimal("100");
        wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
        wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
        try {
            updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("whole sale order interest audit failed, data: {}, error message: {}, e: {}", wholeSalesFlowDTO, e.getMessage(), e);
            throw new BizException(I18nUtils.get("whole.sale.audit.failed", getLang(request)));
        }
    }

    @Override
    public JSONObject merchantOrderDetails(Long merchantId, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        MerchantDTO merchantDTO = merchantService.findMerchantById(merchantId);
        UserDTO userDTO = userService.findUserById(merchantDTO.getUserId());
        result.put("email", userDTO.getEmail());
        result.put("corporateName", merchantDTO.getPracticalName());
        BigDecimal totalAmount = wholeSalesFlowDAO.merchantWholeSaleTotalAmount(merchantId);
        result.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        JSONObject accountData = serverService.getAccountInfo(userDTO.getId());
        JSONArray subAccountList = accountData.getJSONArray("subAccountDTOList");
        for (int i = 0; i < subAccountList.size(); i ++) {
            JSONObject subAccount = subAccountList.getJSONObject(i);
            if (subAccount.getInteger("type").intValue() == StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode()) {
                result.put("remainingAmount", subAccount.getBigDecimal("balance"));
                break;
            }
        }
        Map<String, Object> params = new HashMap<>(16);
        params.put("merchantId", merchantId);
        List<WholeSalesFlowDTO> wholeSalesFlowDTOList = wholeSalesFlowDAO.wholeSaleOrderList(params);
        BigDecimal cal = new BigDecimal("100");
        wholeSalesFlowDTOList.stream().forEach(wholeSalesFlowDTO -> {
            wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerPayDiscount(wholeSalesFlowDTO.getCustomerPayDiscount().multiply(cal).setScale(4, RoundingMode.HALF_UP));
        });
        result.put("wholeSaleOrderList", wholeSalesFlowDTOList);
        return result;
    }

    @Override
    public Long wholeSaleApply(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        MerchantDTO merchantDTO = merchantService.findMerchantById(wholeSalesFlowDTO.getMerchantId());
        boolean merchantAvailable = merchantDTO.getId() != null && merchantDTO.getIsAvailable().equals(StaticDataEnum.STATUS_0.getCode());
        if (merchantDTO.getId() == null
                || merchantAvailable
                ) {
            throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
        }
        wholeSalesFlowDTO.setUserId(merchantDTO.getUserId());
        // 判断审核状态，如果通过和审核中则拒绝(只限后台)2021/04/27修改
        Integer fromWhere = wholeSalesFlowDTO.getFromWhere();
        if (fromWhere!=null){
            JSONObject param=new JSONObject();
            param.put("merchantId",merchantDTO.getId());
            List<WholeSalesFlowDTO> wholeSalesFlowDTOS = wholeSalesFlowDAO.selectDTO(param);
            if (wholeSalesFlowDTOS.size()>0){
                for (WholeSalesFlowDTO salesFlowDTO : wholeSalesFlowDTOS) {
                    if (salesFlowDTO.getApproveState().intValue()!=StaticDataEnum.WHOLE_SALE_STATE_2.getCode()){
                        throw new BizException(I18nUtils.get("merchant.wholesale.review", getLang(request)));
                    }
                }
            }

        }
        // || merchantDTO.getWholeSaleApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_2.getCode()
        // 2021/4/27修改后台 审核拒绝也可再次申请整体出售  后台审核中和审核通过不可再次申请意向 app不变
        Map<String, Object> params = new HashMap<>(16);
        params.put("merchantId", wholeSalesFlowDTO.getMerchantId());
        List<WholeSalesFlowDTO> wholeSalesFlowDTOList = find(params, null, null);
        wholeSalesFlowDTOList.stream().forEach(wholeSalesFlowDTO1 -> {
            if (wholeSalesFlowDTO1.getApproveState().equals(StaticDataEnum.WHOLE_SALE_STATE_0.getCode())
                    || wholeSalesFlowDTO1.getApproveState().equals(StaticDataEnum.WHOLE_SALE_STATE_4.getCode())) {
                throw new RuntimeException(I18nUtils.get("whole.sale.apply.fail", getLang(request)));
            }
        });
        if (merchantDTO.getWholeSaleApproveState().equals(StaticDataEnum.WHOLE_SALE_STATE_1.getCode())) {
            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findOneParametersConfig(new HashMap<>(2));
            if (wholeSalesFlowDTO.getAmount().compareTo(MIN_WHOLE_SALE_AMOUNT) == -1
                    || wholeSalesFlowDTO.getAmount().compareTo(parametersConfigDTO.getWholeSaleAmount()) == 1) {
                throw new BizException(I18nUtils.get("whole.sale.apply.amount.error", getLang(request), new String[]{parametersConfigDTO.getWholeSaleAmount().toString()}));
            }
            if (wholeSalesFlowDTO.getMerchantDiscount() == null) {
                throw new BizException(I18nUtils.get("whole.sale.apply.fail", getLang(request)));
            }
            if (wholeSalesFlowDTO.getCustomerDiscount() == null) {
                throw new BizException(I18nUtils.get("whole.sale.apply.fail", getLang(request)));
            }
            BigDecimal cal = new BigDecimal("100");
            wholeSalesFlowDTO.setMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setCustomerDiscount(wholeSalesFlowDTO.getCustomerDiscount().divide(cal).setScale(4, RoundingMode.HALF_UP));
            wholeSalesFlowDTO.setSettlementAmount(wholeSalesFlowDTO.getAmount().multiply(new BigDecimal("1").subtract(wholeSalesFlowDTO.getMerchantDiscount())));
            wholeSalesFlowDTO.setOrderType(StaticDataEnum.WHOLE_SALE_ORDER_TYPE_1.getCode());
        } else {
            wholeSalesFlowDTO.setOrderType(StaticDataEnum.WHOLE_SALE_ORDER_TYPE_0.getCode());
            try{
                //发邮件
                //查询模板
                MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_21.getCode() + "");
                //邮件内容
                String sendMsg = mailTemplateDTO.getEnSendContent();
                Map<String, Object> userSearchParams = new HashMap<>(4);
                userSearchParams.put("merchantId", wholeSalesFlowDTO.getMerchantId());
                List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
                if (!CollectionUtils.isEmpty(userDTOList)) {
                    userSearchParams.clear();
                    for (UserDTO userDTO1 : userDTOList) {
                        long wholeSaleAction = 29L;
                        userSearchParams.put("userId", userDTO1.getId());
                        userSearchParams.put("actionId", wholeSaleAction);
                        UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                        if (userActionDTO.getId() != null) {
                            //发站内信
                            NoticeDTO noticeDTO= new NoticeDTO();
                            noticeDTO.setContent(sendMsg);
                            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                            noticeDTO.setUserId(userDTO1.getId());
                            noticeService.saveNotice(noticeDTO,request);
                            //记录邮件流水
                            userService.saveMailLog(userDTO1.getEmail(),sendMsg,0,request);
                        }
                    }
                }
            }catch (Exception e){
                log.info("MerchantServiceImpl.passMerchant,发送邮件异常"+e.getMessage(),e);
            }
        }
        wholeSalesFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_24.getCode());
        wholeSalesFlowDTO.setState(StaticDataEnum.WHOLE_SALE_STATE_0.getCode());
        return saveWholeSalesFlow(wholeSalesFlowDTO, request);
    }

    @Override
    public int appWholeSaleOrderCount(Map<String, Object> params) {
        return wholeSalesFlowDAO.appWholeSaleOrderCount(params);
    }

    @Override
    public List<JSONObject> appWholeSaleOrder(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<String> orderDateList = wholeSalesFlowDAO.appWholeSaleOrder(params);
        List<JSONObject> orderList = null;
        if (orderDateList != null && !orderDateList.isEmpty()) {
            Long merchantId = Long.parseLong(params.get("merchantId").toString());
            orderList = new ArrayList<>(orderDateList.size());
            params.clear();
            params.put("merchantId", merchantId);
            Map<String, Object> finalParams = params;
            List<JSONObject> finalOrderList = orderList;
            orderDateList.stream().forEach(orderDate -> {
                finalParams.put("createdDate", orderDate);
                List<WholeSalesFlowDTO> wholeSalesFlowDTOList = wholeSalesFlowDAO.wholeSaleOrderList(finalParams);
                Optional<BigDecimal> totalAmount = wholeSalesFlowDTOList.stream().filter((wholeSalesFlowDTO -> wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()
                        || wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_3.getCode()))
                        .map(WholeSalesFlowDTO :: getAmount)
                        .reduce(BigDecimal::add);
                JSONObject result = new JSONObject();
                result.put("date", orderDate);
                result.put("totalAmount", totalAmount.isPresent() ? totalAmount.get() : new BigDecimal("0.00"));
                result.put("totalCount", wholeSalesFlowDTOList.size());
                result.put("orderList", wholeSalesFlowDTOList);
                finalOrderList.add(result);
            });
        }
        return orderList;
    }

    @Override
    public WholeSalesFlowDTO findLatestWholeSaleFlowDTO(Map<String, Object> params) {
        return wholeSalesFlowDAO.findLatestWholeSaleFlowDTO(params);
    }

    @Override
    public JSONObject findInterestRejectInfo(Long id, HttpServletRequest request) throws Exception {
        WholeSalesFlowDTO wholeSalesFlowDTO = findWholeSalesFlowById(id, request);
        AdminDTO adminDTO = adminService.findAdminById(wholeSalesFlowDTO.getModifiedBy());
        JSONObject result = new JSONObject();
        result.put("remark", wholeSalesFlowDTO.getRemark());
        result.put("admin", adminDTO.getUserName());
        result.put("date", new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(wholeSalesFlowDTO.getModifiedDate())));
        return result;
    }

    @Override
    public List<WholeSalesFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return wholeSalesFlowDAO.clearedDetailTransFlowList( params);
    }

    @Override
    public Integer clearedDetailTransFlowCount(Long id) {
        return wholeSalesFlowDAO.clearedDetailTransFlowCount( id);
    }

    @Override
    public int countMerchantClearList(Map<String, Object> params) {
        return wholeSalesFlowDAO.countMerchantClearList(params);
    }

    @Override
    public void addClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        params.put("batchId",clearBatchDTO.getId());
        wholeSalesFlowDAO.addClearBatchId(params);
    }

    @Override
    public List<ClearFlowDetail> getDataByBatchId(Long id) {
        return wholeSalesFlowDAO.getDataByBatchId(id);
    }

    @Override
    public int dealWholeSaleClear(Long id) {
        return wholeSalesFlowDAO.dealWholeSaleClear(id,System.currentTimeMillis());
    }

    @Override
    public List<WholeSalesFlowDTO> merchantClearMessageList(Map<String, Object> params) {
        return wholeSalesFlowDAO.merchantClearMessageList(params);
    }

    @Override
    public int updateClearBatchToFail(Map<String, Object> params) {
        return wholeSalesFlowDAO.updateClearBatchToFail(params);
    }

    @Override
    public int getMerchantClearMessageCount(Map<String, Object> params) {
        return wholeSalesFlowDAO.getMerchantClearMessageCount(params);
    }


    private void sendMessage(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception {
        Integer sendNode = null;
        String[] params = null;
        if (wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
            sendNode = StaticDataEnum.SEND_NODE_22.getCode();
            params = new String[]{wholeSalesFlowDTO.getAmount().toString()};
        } else {
            if (wholeSalesFlowDTO.getOrderType().equals(StaticDataEnum.WHOLE_SALE_ORDER_TYPE_0.getCode())) {
                sendNode = StaticDataEnum.SEND_NODE_23.getCode();
            } else {
                sendNode = StaticDataEnum.SEND_NODE_24.getCode();
            }
            params = new String[]{wholeSalesFlowDTO.getRemark()};
        }
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode.toString());
        String title = mailTemplateDTO.getEnMailTheme();
        String content = userService.templateContentReplace(params, mailTemplateDTO.getEnSendContent());
        Map<String, Object> userSearchParams = new HashMap<>(4);
        userSearchParams.put("merchantId", wholeSalesFlowDTO.getMerchantId());
        List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
        if (!CollectionUtils.isEmpty(userDTOList)) {
            userSearchParams.clear();
            for (UserDTO userDTO1 : userDTOList) {
                long wholeSaleAction = 29L;
                userSearchParams.put("userId", userDTO1.getId());
                userSearchParams.put("actionId", wholeSaleAction);
                UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                if (userActionDTO.getId() != null) {
                    NoticeDTO noticeDTO= new NoticeDTO();
                    noticeDTO.setContent(content);
                    noticeDTO.setTitle(title);
                    noticeDTO.setUserId(userDTO1.getId());
                    noticeService.saveNotice(noticeDTO,request);
//                    if (wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()
//                            && StringUtils.isNotEmpty(userDTO1.getPushToken())) {
//                        FirebaseDTO firebaseDTO = new FirebaseDTO();
//                        firebaseDTO.setAppName("UWallet");
//                        firebaseDTO.setUserId(userDTO1.getId());
//                        firebaseDTO.setTitle(title);
//                        firebaseDTO.setBody(content);
//                        firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
//                        serverService.pushFirebase(firebaseDTO,request);
//                    }
                    // 2021-05-20 申请拒绝也发送push信息
                    if ((wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()||wholeSalesFlowDTO.getApproveState().intValue() == StaticDataEnum.WHOLE_SALE_STATE_2.getCode())
                            && StringUtils.isNotEmpty(userDTO1.getPushToken())) {
                        FirebaseDTO firebaseDTO = new FirebaseDTO();
                        firebaseDTO.setAppName("UWallet");
                        firebaseDTO.setUserId(userDTO1.getId());
                        firebaseDTO.setTitle(title);
                        firebaseDTO.setBody(content);
                        firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
                        serverService.pushFirebase(firebaseDTO,request);
                    }
                    userService.saveMailLog(userDTO1.getEmail(),content,0,request);
                }
            }
        }
    }

}
