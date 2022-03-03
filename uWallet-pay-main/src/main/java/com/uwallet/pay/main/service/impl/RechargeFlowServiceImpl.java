package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.dynamodbv2.xspec.NULL;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.FormatUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.AccountFlowDAO;
import com.uwallet.pay.main.dao.RechargeFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ChannelLimit;
import com.uwallet.pay.main.model.entity.RechargeFlow;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 充值交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 充值交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class RechargeFlowServiceImpl extends BaseServiceImpl implements RechargeFlowService {

    @Autowired
    private RechargeFlowDAO rechargeFlowDAO;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private AccountFlowDAO accountFlowDAO;

    @Autowired
    private ServerService serverService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private ChannelLimitService channelLimitService;

    @Autowired
    private LatPayService latPayService;

    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @Value("${omipay.noticeUrl}")
    private String notifyUrl;

    @Override
    public Long saveRechargeFlow(@NonNull RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) throws BizException {
        RechargeFlow rechargeFlow = BeanUtil.copyProperties(rechargeFlowDTO, new RechargeFlow());
        log.info("save RechargeFlow:{}", rechargeFlow);
        if (request != null) {
            rechargeFlow = (RechargeFlow) this.packAddBaseProps(rechargeFlow, request);
        } else {
            long now = System.currentTimeMillis();
            rechargeFlow.setId(SnowflakeUtil.generateId());
            rechargeFlow.setCreatedDate(now);
            rechargeFlow.setModifiedDate(now);
            rechargeFlow.setStatus(1);
        }
        if (rechargeFlowDAO.insert(rechargeFlow) != 1) {
            log.error("insert error, data:{}", rechargeFlow);
            throw new BizException("Insert rechargeFlow Error!");
        }
        return rechargeFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRechargeFlowList(@NonNull List<RechargeFlow> rechargeFlowList, HttpServletRequest request) throws BizException {
        if (rechargeFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = rechargeFlowDAO.insertList(rechargeFlowList);
        if (rows != rechargeFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, rechargeFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRechargeFlow(@NonNull Long id, @NonNull RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update rechargeFlowDTO:{}", rechargeFlowDTO);
        RechargeFlow rechargeFlow = BeanUtil.copyProperties(rechargeFlowDTO, new RechargeFlow());
        rechargeFlow.setId(id);
        if (request != null) {
            rechargeFlow = (RechargeFlow) this.packModifyBaseProps(rechargeFlow, request);
        } else {
            rechargeFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = rechargeFlowDAO.update(rechargeFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", rechargeFlowDTO);
            throw new BizException("update rechargeFlow Error!");
        }
    }

    @Override
    public void updateRechargeFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        rechargeFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteRechargeFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = rechargeFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRechargeFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = rechargeFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RechargeFlowDTO findRechargeFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RechargeFlowDTO rechargeFlowDTO = rechargeFlowDAO.selectOneDTO(params);
        return rechargeFlowDTO;
    }

    @Override
    public RechargeFlowDTO findOneRechargeFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RechargeFlow rechargeFlow = rechargeFlowDAO.selectOne(params);
        RechargeFlowDTO rechargeFlowDTO = new RechargeFlowDTO();
        if (null != rechargeFlow) {
            BeanUtils.copyProperties(rechargeFlow, rechargeFlowDTO);
        }
        return rechargeFlowDTO;
    }

    @Override
    public List<RechargeFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RechargeFlowDTO> resultList = rechargeFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return rechargeFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return rechargeFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = rechargeFlowDAO.groupCount(conditions);
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
        return rechargeFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = rechargeFlowDAO.groupSum(conditions);
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
    public void rechargeByLatPay(RechargeDTO rechargeDTO, HttpServletRequest request) throws Exception{
        log.info("recharge flow save data:{}", rechargeDTO);
        // 获取用户账户状态和子户状态、用户充值银行卡信息、用户信息
        JSONObject accountData = serverService.getAccountInfo(rechargeDTO.getUserId());
        if (accountData == null) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        JSONArray subAccountArray = accountData.getJSONArray("subAccountDTOList");
        JSONArray cardArray = accountData.getJSONArray("cardDTOList");
        JSONObject subAccount = null;
        for (int i = 0; i < subAccountArray.size(); i ++) {
            if (subAccountArray.getJSONObject(i).getInteger("type").intValue() == rechargeDTO.getAccountType().intValue()) {
                subAccount = subAccountArray.getJSONObject(i);
                break;
            }
        }
        JSONObject card = null;
        for (int i = 0; i < cardArray.size(); i ++) {
            if (cardArray.getJSONObject(i).getLong("id").longValue() == rechargeDTO.getCardId().longValue()) {
                card = cardArray.getJSONObject(i);
                break;
            }
        }
        // 判断账户、子账户是否冻结、删除，是就交易失败，否进行三方接口处理
        if (accountData.getInteger("state") == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        if (subAccount == null || subAccount.getInteger("state").intValue() == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }

        //获取支付渠道, 现在只有一条渠道可以直接去查询出来, 当有多个支付渠道时，应该通过gatewayType去u_recharge_route，并匹配一条最合适的渠道
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", rechargeDTO.getGatewayType());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //限额检查
        channelLimit(gatewayDTO.getType(), rechargeDTO.getAmount(), request);
        //查询用户
        JSONObject user = serverService.userInfoByQRCode(rechargeDTO.getUserId());
        //查询国家ISO列表
        CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(card.getString("country"));
        if(countryIsoDTO==null){
            throw new BizException(I18nUtils.get("bank.card.repetition", getLang(request)));
        }
        card.put("country", countryIsoDTO.getTwoLettersCoding());

        // 1、创建交易记录
        RechargeFlowDTO rechargeFlowDTO = new RechargeFlowDTO();
        rechargeFlowDTO.setUserId(rechargeDTO.getUserId());
        rechargeFlowDTO.setCardId(rechargeDTO.getCardId());
        rechargeFlowDTO.setTransAmount(rechargeDTO.getAmount());
        rechargeFlowDTO.setAccountType(rechargeDTO.getAccountType());
        rechargeFlowDTO.setGatewayId(gatewayDTO.getType());
        rechargeFlowDTO.setFee(rechargeDTO.getFeeAmt());
        rechargeFlowDTO.setFeeDirection(rechargeDTO.getFeeDirection());
        rechargeFlowDTO.setCharge(rechargeDTO.getCharge());
        rechargeFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
        Long flowId = this.saveRechargeFlow(rechargeFlowDTO, request);
        rechargeFlowDTO.setId(flowId);
        // 2、渠道手续费试算?

        // 3、三方流水交易记录
        Long withholdOrderNo = SnowflakeUtil.generateId();
        WithholdFlowDTO withholdFlowDTO = createWithholdFlow(flowId, withholdOrderNo, rechargeDTO, user, card, request);
        // 4、调用三方交易接口，若三方接口调用失败则整个充值交易流程失败,成功则进入账户流水记录，并调用账户系统进行余额修改
        try {
            //将渠道给商户的id、password赋值
            withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
            withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
            withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO, rechargeDTO.getAmount(), null, card, request ,getIp(request));
            //根据相应返回，如果查证为失败则整个流水为失败，成功就进入账户系统流水处理
            if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, request);
                log.info("recharge success data:{}", withholdFlowDTO);
            } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), withholdFlowDTO.getTransAmount(), request);
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, request);
                log.info("recharge failed data:{}", withholdFlowDTO);
                throw new BizException(I18nUtils.get("recharge.failed", getLang(request)));
            } else {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, request);
                log.info("recharge doubtful data:{}", withholdFlowDTO);
                throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
            }
        } catch (Exception e) {
           log.error("withhold flow update failed message:{}", e.getMessage());
           withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
           withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, request);
           throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
        }

        // 5、调用账户系统接口，记录交易流水
        Long orderNo = null;
        try {
            orderNo = createAccountFlow(flowId, rechargeDTO, request);
        } catch (BizException e) {
            rechargeFlowDTO = this.findRechargeFlowById(flowId);
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
            updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, request);
            log.error("account flow insert failed message:{}", e.getMessage());
            throw new BizException(e.getMessage());
        }
        JSONObject amountIn = new JSONObject();
        amountIn.put("userId", rechargeDTO.getUserId());
        amountIn.put("amountInUserId", rechargeDTO.getUserId());
        amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
        amountIn.put("channelSerialnumber", orderNo);
        amountIn.put("accountId", accountData.getLongValue("id"));
        amountIn.put("cardNo", card.getString("cardNo"));
        amountIn.put("transAmount", rechargeDTO.getAmount());
        amountIn.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        amountIn.put("feeAmount", rechargeDTO.getFeeAmt());
        amountIn.put("feeDirection", rechargeDTO.getFeeDirection());
        try {
            //根据结果判断交易状态
            JSONObject msg = serverService.amountIn(amountIn);
            String code = msg.getString("code");
            if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_31.getCode(), request);
            } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_32.getCode(), request);
                log.info("account flow insert failed, info:{}", msg);
                throw new BizException(I18nUtils.get("recharge.failed", getLang(request)));
            } else {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_33.getCode(), request);
                log.info("account flow insert doubtful, info:{}", msg);
                throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
            }
        } catch (Exception e) {
            accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_33.getCode(), request);
            log.error("account flow insert failed message:{}", e.getMessage());
            throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
        }

    }

    @Override
    public JSONObject rechargeByOmiPay(RechargeDTO rechargeDTO, HttpServletRequest request) throws Exception {
        log.info("create recharge flow dto, info: {}", rechargeDTO);
        // 获取用户账户状态和子户状态、用户充值银行卡信息、用户信息
        JSONObject accountData = serverService.getAccountInfo(rechargeDTO.getUserId());
        if (accountData == null) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        JSONArray subAccountArray = accountData.getJSONArray("subAccountDTOList");
        JSONObject subAccount = null;
        for (int i = 0; i < subAccountArray.size(); i ++) {
            if (subAccountArray.getJSONObject(i).getInteger("type").intValue() == rechargeDTO.getAccountType().intValue()) {
                subAccount = subAccountArray.getJSONObject(i);
                break;
            }
        }
        // 判断账户、子账户是否冻结、删除，是就交易失败，否进行三方接口处理
        if (accountData.getInteger("state") == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        if (subAccount == null || subAccount.getInteger("state").intValue() == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        //获取支付渠道, 现在只有一条渠道可以直接去查询出来, 当有多个支付渠道时，应该通过gatewayType去u_recharge_route，并匹配一条最合适的渠道
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", rechargeDTO.getGatewayType());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //限额检查
        channelLimit(gatewayDTO.getType(), rechargeDTO.getAmount(), request);
        //查询用户
        JSONObject user = serverService.userInfoByQRCode(rechargeDTO.getUserId());
        // 1、创建交易记录
        RechargeFlowDTO rechargeFlowDTO = new RechargeFlowDTO();
        rechargeFlowDTO.setUserId(rechargeDTO.getUserId());
        rechargeFlowDTO.setCardId(rechargeDTO.getCardId());
        rechargeFlowDTO.setTransAmount(rechargeDTO.getAmount());
        rechargeFlowDTO.setAccountType(rechargeDTO.getAccountType());
        rechargeFlowDTO.setGatewayId(gatewayDTO.getType());
        rechargeFlowDTO.setFee(rechargeDTO.getFeeAmt());
        rechargeFlowDTO.setFeeDirection(rechargeDTO.getFeeDirection());
        rechargeFlowDTO.setCharge(rechargeDTO.getCharge());
        rechargeFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
        log.info("create recharge flow dto, info: {}", rechargeDTO);
        Long flowId = this.saveRechargeFlow(rechargeFlowDTO, request);
        // 2、渠道手续费试算?

        // 3、三方流水交易记录
        Long withholdOrderNo = SnowflakeUtil.generateId();
        createWithholdFlow(flowId, withholdOrderNo, rechargeDTO, user, null, request);
        // 4、返回通知地址、流水id
        JSONObject returnData = new JSONObject();
        returnData.put("flowId", flowId);
        returnData.put("notifyUrl", notifyUrl);
        returnData.put("mNumber", gatewayDTO.getChannelMerchantId());
        returnData.put("secretKey", gatewayDTO.getPassword());

        return returnData;
    }

    @Override
    public void rechargeWithholdDoubtHandle() throws Exception {
        log.info("check doubtful flow");
        //查询充值交易流水中账户操作阶段可疑的流水
        Map<String, Object> params = new HashMap<>(2);
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        params.put("doubt", StaticDataEnum.STATUS_1.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_23.getCode());
        List<RechargeFlowDTO> list = find(params, null, null);
        //遍历流水集合，查证可疑流水
        for (RechargeFlowDTO rechargeFlowDTO : list) {
            // 获取用户账户状态和子户状态、用户充值银行卡信息
            JSONObject accountData = serverService.getAccountInfo(rechargeFlowDTO.getUserId());
            if (accountData == null) {
                continue;
            }
            JSONArray subAccountArray = accountData.getJSONArray("subAccountDTOList");
            JSONArray cardArray = accountData.getJSONArray("cardDTOList");
            JSONObject subAccount = null;
            for (int i = 0; i < subAccountArray.size(); i ++) {
                if (subAccountArray.getJSONObject(i).getInteger("type").intValue() == rechargeFlowDTO.getAccountType().intValue()) {
                    subAccount = subAccountArray.getJSONObject(i);
                    break;
                }
            }
            JSONObject card = null;
            for (int i = 0; i < cardArray.size(); i ++) {
                card = cardArray.getJSONObject(i);
                if (cardArray.getJSONObject(i).getLong("id").longValue() == rechargeFlowDTO.getCardId().longValue()) {
                    break;
                }
            }
            // 判断账户、子账户是否冻结、删除，是就交易失败
            if (accountData.getInteger("state") == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
                rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
                continue;
            }
            if (subAccount == null || subAccount.getInteger("state").intValue() == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
                rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
                continue;
            }
            params.clear();
            params.put("flowId", rechargeFlowDTO.getId());
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
            //三方查证参数
            withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
            //根据相应返回，如果查证为失败则整个流水为失败，成功就进入账户系统流水处理
            if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, null);
                log.info("recharge success, data:{}", withholdFlowDTO);
            } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                channelLimitRollback(System.currentTimeMillis(), withholdFlowDTO.getGatewayId(), withholdFlowDTO.getTransAmount(), null);
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, null);
                log.info("recharge failed, data:{}", withholdFlowDTO);
                continue;
            } else {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, null);
                log.info("recharge doubtful, data:{}", withholdFlowDTO);
                continue;
            }

            //调用账户系统接口，记录交易流水
            RechargeDTO rechargeDTO = new RechargeDTO();
            rechargeDTO.setUserId(rechargeFlowDTO.getUserId());
            rechargeDTO.setCardId(rechargeFlowDTO.getCardId());
            rechargeDTO.setGatewayId(rechargeFlowDTO.getGatewayId());
            rechargeDTO.setAccountType(rechargeFlowDTO.getAccountType());
            rechargeDTO.setAmount(rechargeFlowDTO.getTransAmount());
            rechargeDTO.setFeeAmt(rechargeFlowDTO.getFee());
            Long orderNo = null;
            try {
                orderNo = createAccountFlow(rechargeFlowDTO.getId(), rechargeDTO, null);
            } catch (BizException e) {
                rechargeFlowDTO = this.findRechargeFlowById(rechargeFlowDTO.getId());
                rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
                log.error("account flow insert failed message:{}", e.getMessage());
                continue;
            }
            JSONObject amountIn = new JSONObject();
            amountIn.put("userId", rechargeDTO.getUserId());
            amountIn.put("amountInUserId", rechargeDTO.getUserId());
            amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
            amountIn.put("channelSerialnumber", orderNo);
            amountIn.put("accountId", accountData.getLongValue("id"));
            amountIn.put("cardNo", card.getString("cardNo"));
            amountIn.put("transAmount", rechargeDTO.getAmount());
            amountIn.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
            amountIn.put("feeAmount", rechargeDTO.getFeeAmt());
            amountIn.put("feeDirection", rechargeDTO.getFeeDirection());
            try {
                //根据结果判断交易状态
                JSONObject msg = serverService.amountIn(amountIn);
                String code = msg.getString("code");
                if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
                    accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_31.getCode(), null);
                    log.info("account flow insert success, data:{}", msg);
                } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                    accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_32.getCode(), null);
                    log.info("account flow insert failed, data:{}", msg);
                } else {
                    accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_33.getCode(), null);
                    log.info("account flow insert doubtful, data:{}", msg);
                }
            } catch (Exception e) {
                accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_33.getCode(), null);
                log.error("account flow update failed message:{}", e.getMessage());
                continue;
            }
        }
    }

    @Override
    public void rechargeAccountDoubtHandle() throws Exception {
        log.info("check doubtful flow");
        //查询充值交易流水中账户操作阶段可疑的流水
        Map<String, Object> params = new HashMap<>(2);
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        params.put("doubt", StaticDataEnum.STATUS_1.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_33.getCode());
        List<RechargeFlowDTO> list = find(params, null, null);
        //遍历流水集合，查证可疑流水
        for (RechargeFlowDTO rechargeFlowDTO : list) {
            //获取账户系统交易记录
            AccountFlowDTO accountFlowDTO = accountFlowDAO.selectLatestByFlowId(rechargeFlowDTO.getId(), null);
            JSONObject data = null;
            try {
                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
            } catch (Exception e) {
                log.error("check doubtful flow failed message:{}", e.getMessage());
                continue;
            }
            if (data != null) {
                if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_31.getCode(), null);
                    log.info("account flow insert success, data:{}", accountFlowDTO);
                } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    FailedAndDoubtfulDealWithAccount(rechargeFlowDTO, accountFlowDTO);
                    log.info("account flow insert success, failed:{}", accountFlowDTO);
                } else {
                    accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_33.getCode(), null);
                    log.info("account flow insert doubtful, data:{}", accountFlowDTO);
                }
            } else {
                FailedAndDoubtfulDealWithAccount(rechargeFlowDTO, accountFlowDTO);
            }
        }

    }

    @Override
    public void rechargeAccountFailedHandle() throws Exception {
        log.info("check failed flow");
        //查询充值交易流水中账户操作阶段失败的流水
        Map<String, Object> params = new HashMap<>(2);
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_32.getCode());
        List<RechargeFlowDTO> list = find(params, null, null);
        //遍历流水集合，将失败流水进行再次账户改动
        for (RechargeFlowDTO rechargeFlowDTO : list) {
            //获取账户交易记录
            AccountFlowDTO accountFlowDTO = accountFlowDAO.selectLatestByFlowId(rechargeFlowDTO.getId(), null);
            try {
                FailedAndDoubtfulDealWithAccount(rechargeFlowDTO, accountFlowDTO);
            } catch (Exception e) {
                log.error("failed data handle rechargeFlowDTO:{}, accountFlowDTO", rechargeFlowDTO, accountFlowDTO, e.getMessage());
                continue;
            }
        }
    }

    @Override
    public List<RechargeBorrowDTO> selectRechargeBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws BizException {
        params = getUnionParams(params, scs, pc);
        List<RechargeBorrowDTO> resultList = rechargeFlowDAO.selectRechargeBorrow(params);
        return resultList;
    }

    @Override
    public int selectRechargeBorrowCount(Map<String, Object> params) throws BizException {
        int count = rechargeFlowDAO.selectRechargeBorrowCount(params);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void channelLimit(Long gatewayId, BigDecimal transAmount, HttpServletRequest request) throws BizException {
        Map<String,Object> map = new HashMap<>(1);
        map.put("type",gatewayId);

        GatewayDTO gatewayDTO = gatewayService.findOneGateway(map);
        BigDecimal singleMin = gatewayDTO.getSingleMinAmount();
        BigDecimal singleMax = gatewayDTO.getSingleMaxAmount();
        BigDecimal dailyTotalAmount = gatewayDTO.getDailyTotalAmount();
        // 比较交易金额是否超出当前支付渠道单笔限额
        if (transAmount.compareTo(singleMin) == -1 || transAmount.compareTo(singleMax) == 1) {
            throw new BizException(I18nUtils.get("limit.over", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(1);
        params.put("channelId", gatewayDTO.getType());
        ChannelLimitDTO channelLimitDTO = channelLimitService.findOneChannelLimit(params);
        if(channelLimitDTO.getId()==null){
            channelLimitDTO.setAccruingAmount(transAmount);
            channelLimitDTO.setChannelId(gatewayId.longValue());
            channelLimitService.saveChannelLimit(channelLimitDTO,request);
            return;
        }

        String channelLimitModifyDate = FormatUtil.getTodayTimeField(new Date(channelLimitDTO.getModifiedDate()), "yyyy-MM-dd");
        String now = FormatUtil.getTodayTimeField(new Date(), "yyyy-MM-dd");
        // 判断累计限额记录时间是否是当天，是则进行累加，否则重置
        if (channelLimitModifyDate.equals(now)) {
            channelLimitDTO.setDailyTotalAmount(dailyTotalAmount);
            channelLimitDTO.setAccruingAmount(transAmount);
            ChannelLimit channelLimit = BeanUtil.copyProperties(channelLimitDTO, new ChannelLimit());
            channelLimitService.updateAmount(channelLimit, request);
        } else {
            channelLimitDTO.setDailyTotalAmount(dailyTotalAmount);
            channelLimitDTO.setAccruingAmount(transAmount);
            channelLimitService.updateChannelLimit(channelLimitDTO.getId(), channelLimitDTO, request);
        }

    }

    @Override
    public void channelLimitRollback(Long createdDate, Long gatewayId, BigDecimal transAmount, HttpServletRequest request) {
        String now = FormatUtil.getTodayTimeField(new Date(), "yyyy-MM-dd");
        String transDate = FormatUtil.getTodayTimeField(new Date(createdDate), "yyyy-MM-dd");

        if(now.equals(transDate)){
            Map<String,Object> map = new HashMap<>();
            map.put("transAmount",transAmount);
            map.put("channelId",gatewayId);
            if(request!=null){
                map.put("channelId",gatewayId);
                map.put("modifiedBy",getUserId(request));
                map.put("ip",getIp(request));
            }
            map.put("modifiedDate",System.currentTimeMillis());
            channelLimitService.channelLimitRollback(map);
        }

    }

    /**
     * 创建三方交易流水接口
     * @param flowId
     * @param rechargeDTO
     * @param request
     * @throws Exception
     */
    private WithholdFlowDTO createWithholdFlow(Long flowId, Long orderNo, RechargeDTO rechargeDTO, JSONObject userInfo, JSONObject cardInfo, HttpServletRequest request) throws Exception {
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setFlowId(flowId);
        withholdFlowDTO.setUserId(rechargeDTO.getUserId());
        withholdFlowDTO.setGatewayId(rechargeDTO.getGatewayId());
        withholdFlowDTO.setTransAmount(rechargeDTO.getAmount());
        withholdFlowDTO.setOrdreNo(orderNo.toString());
        withholdFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        withholdFlowDTO.setCustomerEmail(userInfo.getString("email"));
        withholdFlowDTO.setCustomerPhone(userInfo.getString("phone"));
        withholdFlowDTO.setCustomerFirstname(userInfo.getString("firstName"));
        withholdFlowDTO.setCustomerLastname(userInfo.getString("lastName"));
        withholdFlowDTO.setCustomerIpaddress(getIp(request));
        if (cardInfo != null) {
            withholdFlowDTO.setBillFirstname(cardInfo.getString("firstName"));
            withholdFlowDTO.setBillLastname(cardInfo.getString("lastName"));
            withholdFlowDTO.setBillAddress1(cardInfo.getString("address1"));
            withholdFlowDTO.setBillCity(cardInfo.getString("city"));
            withholdFlowDTO.setBillCountry(cardInfo.getString("country"));
            withholdFlowDTO.setBillZip(cardInfo.getString("zip"));
        }
        withholdFlowDTO.setCurrency(StaticDataEnum.CURRENCY_TYPE.getMessage());
        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        log.info("create withhold flow dto, info: {}", rechargeDTO);
        Long id = withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        withholdFlowDTO.setId(id);
        return withholdFlowDTO;
    }

    /**
     * 创建账户交易流水
     * @param flowId
     * @param rechargeDTO
     * @param request
     * @throws Exception
     */
    private Long createAccountFlow(Long flowId, RechargeDTO rechargeDTO, HttpServletRequest request) throws Exception {
        Long orderNo = SnowflakeUtil.generateId();
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        accountFlowDTO.setFlowId(flowId);
        accountFlowDTO.setUserId(rechargeDTO.getUserId());
        accountFlowDTO.setAccountType(rechargeDTO.getAccountType());
        accountFlowDTO.setTransAmount(rechargeDTO.getAmount());
        accountFlowDTO.setFeeDirection(rechargeDTO.getFeeDirection());
        accountFlowDTO.setFee(rechargeDTO.getFeeAmt());
        accountFlowDTO.setOppositeAccountType(rechargeDTO.getAccountType());
        accountFlowDTO.setOrderNo(orderNo);
        accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        accountFlowService.saveAccountFlow(accountFlowDTO, request);
        return orderNo;
    }


    /**
     * 修改三方交易状态
     * @param withholdFlowDTO
     * @param request
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void withholdFlowStateChange(RechargeFlowDTO rechargeFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>();
        //更新充值交易流程
        if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
        } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
        } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_3.getCode()) {
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
        }
        //更新账户交易流程
        rechargeFlowDTO.setErrorCode(withholdFlowDTO.getReturnCode());
        rechargeFlowDTO.setErrorMessage(withholdFlowDTO.getReturnMessage());
        withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, request);
        this.updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, request);
    }

    /**
     * 修改账户交易状态
     * @param flowId
     * @param msg
     * @param state
     * @param request
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void accountFlowStateChange(RechargeFlowDTO rechargeFlowDTO, JSONObject msg, Integer state, HttpServletRequest request) throws BizException {
        //更新充值交易流程
        rechargeFlowDTO.setState(state);
        //更新账户交易流程
        AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(rechargeFlowDTO.getId(), 6);
        if (msg != null) {
            rechargeFlowDTO.setErrorCode(msg.getString("code"));
            rechargeFlowDTO.setErrorMessage(msg.getString("message"));
            accountFlowDTO.setReturnCode(msg.getString("code"));
            accountFlowDTO.setReturnMessage(msg.getString("message"));
        }
        if (state == StaticDataEnum.TRANS_STATE_31.getCode()) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        } else if (state == StaticDataEnum.TRANS_STATE_32.getCode()) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
        } else if (state == StaticDataEnum.TRANS_STATE_33.getCode()) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        }
        this.updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, request);
        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
    }

    /**
     * 可疑、失败账户交易流水与账户系统交互
     * @param rechargeFlowDTO
     * @param accountFlowDTO
     * @throws Exception
     */
    private void FailedAndDoubtfulDealWithAccount(RechargeFlowDTO rechargeFlowDTO, AccountFlowDTO accountFlowDTO) throws Exception {
        // 获取用户账户状态和子户状态、用户充值银行卡信息
        JSONObject accountData = serverService.getAccountInfo(rechargeFlowDTO.getUserId());
        JSONArray subAccountArray = accountData.getJSONArray("subAccountDTOList");
        JSONArray cardArray = accountData.getJSONArray("cardDTOList");
        JSONObject subAccount = null;
        for (int i = 0; i < subAccountArray.size(); i ++) {
            subAccount = subAccountArray.getJSONObject(i);
            if (subAccount.getInteger("type").intValue() == rechargeFlowDTO.getAccountType().intValue()) {
                break;
            }
        }
        JSONObject card = null;
        for (int i = 0; i < cardArray.size(); i ++) {
            card = cardArray.getJSONObject(i);
            if (cardArray.getJSONObject(i).getLong("id").longValue() == rechargeFlowDTO.getCardId().longValue()) {
                break;
            }
        }
        // 判断账户、子账户是否冻结、删除，是就交易失败
        if (accountData.getInteger("state") == null && accountData.getInteger("state") == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
            return;
        }
        if (subAccount != null && subAccount.getInteger("state").intValue() == StaticDataEnum.ACCOUNT_STATE_2.getCode()) {
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
            return;
        }
        // 记录账户操作交易流水
        RechargeDTO rechargeDTO = new RechargeDTO();
        rechargeDTO.setUserId(rechargeFlowDTO.getUserId());
        rechargeDTO.setCardId(rechargeFlowDTO.getCardId());
        rechargeDTO.setGatewayId(rechargeFlowDTO.getGatewayId());
        rechargeDTO.setAccountType(rechargeFlowDTO.getAccountType());
        rechargeDTO.setAmount(rechargeFlowDTO.getTransAmount());
        rechargeDTO.setFeeAmt(accountFlowDTO.getFee());
        rechargeDTO.setFeeDirection(accountFlowDTO.getFeeDirection());
        Long orderNo = null;
        try {
            orderNo = createAccountFlow(rechargeFlowDTO.getId(), rechargeDTO, null);
        } catch (BizException e) {
            rechargeFlowDTO = this.findRechargeFlowById(rechargeFlowDTO.getId());
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
            updateRechargeFlow(rechargeFlowDTO.getId(), rechargeFlowDTO, null);
            log.error("account flow insert failed message:{}", e.getMessage());
        }
        // 调用账户系统接口，进行账户改动
        JSONObject amountIn = new JSONObject();
        amountIn.put("userId", rechargeDTO.getUserId());
        amountIn.put("amountInUserId", rechargeDTO.getUserId());
        amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
        amountIn.put("channelSerialnumber", orderNo);
        amountIn.put("accountId", accountData.getLongValue("id"));
        amountIn.put("cardNo", card.getString("cardNo"));
        amountIn.put("transAmount", rechargeDTO.getAmount());
        amountIn.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode());
        amountIn.put("feeAmount", rechargeDTO.getFeeAmt());
        amountIn.put("feeDirection", rechargeDTO.getFeeDirection());
        try {
            //根据结果判断交易状态，改动交易流水相关字段结果值
            JSONObject msg = serverService.amountIn(amountIn);
            String code = msg.getString("code");
            if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_31.getCode(), null);
                log.info("account flow insert success, data:{}", msg);
            } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_32.getCode(), null);
                log.info("account flow insert failed, data:{}", msg);
            } else {
                accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_33.getCode(), null);
                log.info("account flow insert doubtful, data:{}", msg);
            }
        } catch (Exception e) {
            accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_33.getCode(), null);
            log.error("account flow insert failed message:{}", e.getMessage());
        }
    }

}
