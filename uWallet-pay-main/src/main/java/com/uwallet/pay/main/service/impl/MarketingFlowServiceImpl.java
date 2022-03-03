package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.MarketingFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.MarketingFlow;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:30:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class MarketingFlowServiceImpl extends BaseServiceImpl implements MarketingFlowService {

    @Autowired
    private MarketingFlowDAO marketingFlowDAO;
    @Autowired
    private MarketingManagementService marketingManagementService;
    @Autowired
    @Lazy
    private UserService userService;
    @Value("${walletRegister}")
    private String walletRegister;

    @Value("${walletConsumption}")
    private String walletConsumption;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private QrPayFlowServiceImpl qrPayFlowService;
    @Autowired
    private ServerService serverService;



    @Override
    public Long saveMarketingFlow(@NonNull MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws BizException {
        MarketingFlow marketingFlow = BeanUtil.copyProperties(marketingFlowDTO, new MarketingFlow());
        log.info("save MarketingFlow:{}", marketingFlow);
        marketingFlow = (MarketingFlow) this.packAddBaseProps(marketingFlow, request);
        if (marketingFlowDAO.insert(marketingFlow) != 1) {
            log.error("insert error, data:{}", marketingFlow);
            throw new BizException("Insert marketingFlow Error!");
        }
        return marketingFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMarketingFlowList(@NonNull List<MarketingFlow> marketingFlowList, HttpServletRequest request) throws BizException {
        if (marketingFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = marketingFlowDAO.insertList(marketingFlowList);
        if (rows != marketingFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, marketingFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMarketingFlow(@NonNull Long id, @NonNull MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update marketingFlowDTO:{}", marketingFlowDTO);

        MarketingFlow marketingFlow = BeanUtil.copyProperties(marketingFlowDTO, new MarketingFlow());
        marketingFlow.setId(id);
        if(request != null){
            marketingFlow = (MarketingFlow) this.packModifyBaseProps(marketingFlow, request);
        }else{
            marketingFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = marketingFlowDAO.update(marketingFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", marketingFlowDTO);
            throw new BizException("update marketingFlow Error!");
        }
    }

    @Override
    public void updateMarketingFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        marketingFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteMarketingFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = marketingFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMarketingFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = marketingFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MarketingFlowDTO findMarketingFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MarketingFlowDTO marketingFlowDTO = marketingFlowDAO.selectOneDTO(params);
        return marketingFlowDTO;
    }

    @Override
    public MarketingFlowDTO findOneMarketingFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MarketingFlow marketingFlow = marketingFlowDAO.selectOne(params);
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        if (null != marketingFlow) {
            BeanUtils.copyProperties(marketingFlow, marketingFlowDTO);
        }
        return marketingFlowDTO;
    }

    @Override
    public List<MarketingFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MarketingFlowDTO> resultList = marketingFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return marketingFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return marketingFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = marketingFlowDAO.groupCount(conditions);
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
        return marketingFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = marketingFlowDAO.groupSum(conditions);
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
    public List<MarketingFlowDTO> findAbnormal(Map<String, Object> params) {
        return marketingFlowDAO.findAbnormal(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarketingFlowDTO createMarketingFlow(Long amountInUserId, Integer transType, String markingCode, Long flowId, HttpServletRequest request) throws Exception {

        Map<String,Object> params = new HashMap<>();

        BigDecimal amount = BigDecimal.ZERO;
        String description = "";

        if(transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode()){
            //交易类型为营销红包，查询营销配置
            params.clear();
            params.put("code",markingCode);
            params.put("state",StaticDataEnum.STATUS_1.getCode());
            params.put("status",StaticDataEnum.STATUS_1.getCode());
            MarketingManagementDTO marketingManagementDTO = marketingManagementService.findOneMarketingManagement(params);
            //配置不可用或者不存在
            if(marketingManagementDTO == null || marketingManagementDTO.getId() == null){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            //次数用尽
            if(marketingManagementDTO.getNumber()<= marketingManagementDTO.getUsedNumber()){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            //先查询该用户是否已获得这张券
            params.clear();
            params.put("code",markingCode);
            params.put("userId",amountInUserId);
            int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_0.getCode(),StaticDataEnum.TRANS_STATE_3.getCode()};
            params.put("stateList",stateList);
            MarketingFlowDTO marketingFlowDTO = this.findOneMarketingFlow(params);
            if(marketingFlowDTO != null &&marketingFlowDTO.getId() != null){
                throw new BizException(I18nUtils.get("promotion.code.used", getLang(request)));
            }
            //累加领取次数
            marketingManagementService.addUsedNumber(marketingManagementDTO.getId());
            amount = marketingManagementDTO.getAmount();
            description = marketingManagementDTO.getDescription();
        }
        else if(transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode()){
            //交易类型为注册红包
            UserDTO userDTO = userService.findUserById(amountInUserId);
            if(userDTO == null || userDTO.getId() == null){
                throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
            }
            //查询推荐人
            params.clear();
            params.put("inviteCode",markingCode);
            UserDTO inviteUser = userService.findOneUser(params);
            if(inviteUser == null || inviteUser.getId() == null ){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            //查询是否已绑定推荐人

            if(!(userDTO.getInviterId() == null || StringUtils.isEmpty(userDTO.getInviterId()+""))){
                throw new BizException(I18nUtils.get("referrer.exist", getLang(request)));
            }
            //输入了自己的码
            if(userDTO.getInviteCode() .equals(markingCode) ){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            //查询是否已经有第一笔分期付消费
            params.clear();
            params.put("payUserId",amountInUserId);
//            params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
            int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(), StaticDataEnum.TRANS_STATE_31.getCode()};
            params.put("stateList",stateList);
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
            if(qrPayFlowDTO != null && qrPayFlowDTO.getId() != null){
                throw new BizException(I18nUtils.get("can.not.bind.references", getLang(request)));
            }

            userDTO.setInviterId(inviteUser.getId());
            //累加推荐人数和预计红包
            userService.updateRegister(inviteUser.getId());
            userService.updateWalletGrandTotal(inviteUser.getId(), new BigDecimal(walletConsumption), null);
            userService.updateUser(userDTO.getId(), userDTO, request);
            amount = new BigDecimal(walletRegister);
        }
        else if(transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode()){
            //交易类型为推荐红包

            //累计消费人数和实得红包
            userService.updateConsumption(amountInUserId);
            userService.updateWalletGrandTotal(amountInUserId, null, new BigDecimal(walletConsumption));
            amount = new BigDecimal(walletConsumption);
        }else {
            throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
        }

        //创建红包流水
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setAmount(amount);
        marketingFlowDTO.setCode(markingCode);
        marketingFlowDTO.setDescription(description);
        if(flowId != null){
            marketingFlowDTO.setFlowId(flowId);
        }
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        marketingFlowDTO.setTransType(transType);
        marketingFlowDTO.setUserId(amountInUserId);
        marketingFlowDTO.setId(this.saveMarketingFlow(marketingFlowDTO,request));
        if(flowId == null){
            marketingFlowDTO.setFlowId(marketingFlowDTO.getId());
            this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
        }
        return  marketingFlowDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doMarketingAmountInResult(AccountFlowDTO accountFlowDTO, MarketingFlowDTO marketingFlowDTO, int code, HttpServletRequest request) throws BizException {
        if (code == StaticDataEnum.TRANS_STATE_1.getCode()) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        } else if (code == StaticDataEnum.TRANS_STATE_2.getCode()) {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            if(marketingFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode()){
                //回滚
                Map<String,Object> map = new HashMap<>();
                map.put("code",marketingFlowDTO.getCode());
                map.put("status",StaticDataEnum.STATUS_1.getCode());
                MarketingManagementDTO marketingManagementDTO = marketingManagementService.findOneMarketingManagement(map);
                marketingManagementService.addUsedNumberRollback(marketingManagementDTO.getId());
            }

        } else {
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        }
        //记录红包流水
        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
        this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
    }

    /**
     * 获取用户使用payo money金额
     *
     * @param userId
     * @param request
     * @return java.math.BigDecimal
     * @author zhangzeyuan
     * @date 2021/9/9 15:36
     */
    @Override
    public BigDecimal getUseRedAmountByUserId(Long userId, HttpServletRequest request) {
        return marketingFlowDAO.getUseRedAmountByUserId(userId);
    }

    @Override
    public void marketingRollBackDoubtHandle() {

        List<MarketingFlowDTO> list = marketingFlowDAO.findRollBackDoubtHandle();
        if(list == null || list.size() == 0){
            return;
        }

        for (MarketingFlowDTO marketingFlowDTO : list){
            JSONObject object;
            try{
                // 查询卡券当前状态
                object = serverService.getMarketingMessage(marketingFlowDTO.getMarketingId(),marketingFlowDTO.getUserId());
                if(object == null){
                   continue;
                }
                MarketingAccountDTO marketingAccountDTO = JSONObject.parseObject(object.toJSONString(),MarketingAccountDTO.class);
                if(marketingAccountDTO.getState() == StaticDataEnum.MARKETING_STATE_1.getCode()){
                    // 查询卡券为未使用，卡券回退成功，交易状态结果是失败
                    marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,null);
                }else if(marketingAccountDTO.getState() == StaticDataEnum.MARKETING_STATE_2.getCode()){
                    // 查询卡券为已使用，卡券未回退成功，交易状态结果是回退失败
                    marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,null);
                }

            }catch (Exception e){
                log.error("卡券回退可疑查证异常，id:" + marketingFlowDTO.getFlowId() + ",message:"+ e.getMessage(),e);
            }
        }

    }

    @Override
    public void marketingRollBackFailHandle() {
        List<MarketingFlowDTO> list = marketingFlowDAO.findRollBackFailHandle();
        if(list == null || list.size() == 0){
            return;
        }

        for (MarketingFlowDTO marketingFlowDTO : list){
            try{
                // 修改流水为处理中的状态
                marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_4.getCode());
                this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,null);
                // 请求账户回退卡券
                serverService.marketingRollBack(marketingFlowDTO.getId());
                marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                this.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,null);

            }catch (Exception e){
                log.error("卡券回退失败处理异常，id:" + marketingFlowDTO.getFlowId() + ",message:"+ e.getMessage(),e);
            }
        }
    }

    /**
     * 获取营销码使用记录
     *
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.MarketingFlowDTO>
     * @author zhangzeyuan
     * @date 2021/11/6 15:16
     */
    @Override
    public List<MarketingFlowDTO> getMarketingCodeUsedLog(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return marketingFlowDAO.getMarketingCodeUsedLog(params);
    }

    @Override
    public List<MarketingFlowDTO> getFlowList(Map<String, Object> params) {
        return marketingFlowDAO.getFlowList(params);
    }

    /**
     * 获取营销码使用记录
     *
     * @param
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/11/11 16:06
     */
    @Override
    public JSONObject getMarketingUsedLogList(Map<String, Object> params, HttpServletRequest request) throws Exception {
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("scs", "last_move_date(desc)");
        paramsJson.put("s", params.get("s"));
        paramsJson.put("p", params.get("p"));
        paramsJson.put("state", 2);
        paramsJson.put("markingId", params.get("marketingId"));
        paramsJson.put("pageStatus", 1);
        return userService.getMarketingCouponAccount(paramsJson, request);
    }

    /**
     * 获取营销码使用数量
     *
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.MarketingFlowDTO>
     * @author zhangzeyuan
     * @date 2021/11/6 15:16
     */
    @Override
    public Integer countUsedLog(Map<String, Object> params) {
        return marketingFlowDAO.countUsedLog(params);
    }

    /**
     * 获取在哪里消费的商户名称
     *
     * @param params
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/11/11 15:01
     */
    @Override
    public String getPaidMerchantName(Map<String, Object> params) {
        return marketingFlowDAO.getPaidMerchantName(params);
    }

    @Override
    public BigDecimal getRedAmountByUserId(Long userId, HttpServletRequest request) {
        return marketingFlowDAO.getRedAmountByUserId(userId);
    }

    @Override
    public List<JSONObject> getUseAvailablePromotionByUserId(JSONObject param, HttpServletRequest request) {
        return marketingFlowDAO.getUseAvailablePromotionByUserId(param);
    }
}
