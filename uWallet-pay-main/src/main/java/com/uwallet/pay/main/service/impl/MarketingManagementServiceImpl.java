package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.util.DateUtils;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.InviteUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.MarketingManagementDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.MarketingManagement;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 商户营销码
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:29:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class MarketingManagementServiceImpl extends BaseServiceImpl implements MarketingManagementService {

    @Autowired
    private MarketingManagementDAO marketingManagementDAO;
    @Autowired
    private ServerService serverService;
    @Autowired
    private MarketingFlowService marketingFlowService;
    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private MerchantService merchantService;

    @Value("${walletConsumption}")
    private String walletConsumption;

    private static final int MAX_PAGE_SIZE = 1000;


    @Override
    public void saveMarketingManagement(@NonNull MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException {
        MarketingManagement marketingManagement = BeanUtil.copyProperties(marketingManagementDTO, new MarketingManagement());
        log.info("save MarketingManagement:{}", marketingManagement);
        if (marketingManagementDAO.insert((MarketingManagement) this.packAddBaseProps(marketingManagement, request)) != 1) {
            log.error("insert error, data:{}", marketingManagement);
            throw new BizException("Insert marketingManagement Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMarketingManagementList(@NonNull List<MarketingManagement> marketingManagementList, HttpServletRequest request) throws BizException {
        if (marketingManagementList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = marketingManagementDAO.insertList(marketingManagementList);
        if (rows != marketingManagementList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, marketingManagementList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMarketingManagement(@NonNull Long id, @NonNull MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException {
        log.info("full update marketingManagementDTO:{}", marketingManagementDTO);

        if(marketingManagementDTO.getCreateMethod() == StaticDataEnum.MARKETING_CREATE_METHOD_2.getCode()){
            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(1);
            params.put("code", marketingManagementDTO.getCode().toLowerCase());
            MarketingManagementDTO promotionData = marketingManagementDAO.getPromotionData(params);
            if (null != promotionData && null != promotionData.getId() && promotionData.getId().compareTo(id) != 0) {
                throw new BizException(I18nUtils.get("marketing.code.has.exist", getLang(request)));
            }
        }

        //限制金额校验
        if(marketingManagementDTO.getAmountLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
            if(marketingManagementDTO.getAmount().compareTo(marketingManagementDTO.getMinTransAmount()) >= 0){
                throw new BizException(I18nUtils.get("marketing.limit.amount.error", getLang(request)));
            }
        }

        //仅限于后台页面营销码更新
        //不限制时间 直接开始活动
        if(marketingManagementDTO.getValidityLimitState().intValue() == StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_0.getCode()){
            marketingManagementDTO.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        }else {

            String validEndTimeStr = marketingManagementDTO.getValidEndTimeStr();
            String validStartTimeStr = marketingManagementDTO.getValidStartTimeStr();

            if(StringUtils.isBlank(validEndTimeStr) || StringUtils.isBlank(validStartTimeStr)){
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }

            //格式化
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            try{
                long startTimestamp = simpleDateFormat.parse(validStartTimeStr).getTime();
                long endTimestamp = simpleDateFormat.parse(validEndTimeStr).getTime();
                //当天0点
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Long todayZeroTimeStamp = calendar.getTime().getTime();
                //如果开始时间等于当天0点  直接开始活动
                if(todayZeroTimeStamp.compareTo(startTimestamp) == 0){
                    marketingManagementDTO.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
                }
                marketingManagementDTO.setValidEndTime(startTimestamp);
                marketingManagementDTO.setValidEndTime(endTimestamp);
            }catch (ParseException e){
                throw new BizException("Insert marketingManagement Error!");
            }
        }
        //营销码计算总费用
        marketingManagementDTO.setTotalAmount(marketingManagementDTO.getAmount().multiply(new BigDecimal(marketingManagementDTO.getNumber().toString())));

        //app 展示描述处理
        if(null == marketingManagementDTO.getCityLimitState()){
            marketingManagementDTO.setCityLimitState(0);
        }

        if(null == marketingManagementDTO.getRestaurantLimitState()){
            marketingManagementDTO.setRestaurantLimitState(0L);
        }
        String desc = marketingManagementDTO.getDescription();

        if(marketingManagementDTO.getCityLimitState() == 0 && marketingManagementDTO.getRestaurantLimitState() == 0){
            if(StringUtils.isBlank(desc)){
                throw new BizException(I18nUtils.get("promotion.desc.null", getLang(request)));
            }else{
                if(desc.length() > 50){
                    throw new BizException(I18nUtils.get("promotion.desc.max", getLang(request)));
                }
            }
        }

        if(marketingManagementDTO.getCityLimitState().intValue() > 0){
            desc = marketingManagementDTO.getCityName();
        }

        if(marketingManagementDTO.getRestaurantLimitState().longValue() > 0L){
            desc = marketingManagementDTO.getMerchantName();
        }

        marketingManagementDTO.setDescription(desc);

        MarketingManagement marketingManagement = BeanUtil.copyProperties(marketingManagementDTO, new MarketingManagement());
        marketingManagement.setId(id);
        int cnt = marketingManagementDAO.update((MarketingManagement) this.packModifyBaseProps(marketingManagement, request));
        if (cnt != 1) {
            log.error("update error, data:{}", marketingManagementDTO);
            throw new BizException("update marketingManagement Error!");
        }
    }


    @Override
    public void updateMarketingManagementActivityState(@NonNull Long id, @NonNull MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException {
        MarketingManagement marketingManagement = BeanUtil.copyProperties(marketingManagementDTO, new MarketingManagement());
        marketingManagement.setId(id);
        int cnt = marketingManagementDAO.update((MarketingManagement) this.packModifyBaseProps(marketingManagement, request));
        if (cnt != 1) {
            log.error("update error, data:{}", marketingManagementDTO);
            throw new BizException("update marketingManagement Error!");
        }
    }


    @Override
    public void updateMarketingManagementSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        marketingManagementDAO.updatex(params);
    }

    @Override
    public void logicDeleteMarketingManagement(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = marketingManagementDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMarketingManagement(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = marketingManagementDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MarketingManagementDTO findMarketingManagementById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MarketingManagementDTO marketingManagementDTO = marketingManagementDAO.selectOneDTO(params);
        return marketingManagementDTO;
    }

    @Override
    public MarketingManagementDTO findOneMarketingManagement(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MarketingManagement marketingManagement = marketingManagementDAO.selectOne(params);
        MarketingManagementDTO marketingManagementDTO = new MarketingManagementDTO();
        if (null != marketingManagement) {
            BeanUtils.copyProperties(marketingManagement, marketingManagementDTO);
        }
        return marketingManagementDTO;
    }

    @Override
    public List<MarketingManagementDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MarketingManagementDTO> resultList = marketingManagementDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return marketingManagementDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return marketingManagementDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = marketingManagementDAO.groupCount(conditions);
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
        return marketingManagementDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = marketingManagementDAO.groupSum(conditions);
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
    public int addUsedNumber(Map<String, Object> params) {
        return marketingManagementDAO.addUsedNumberNew(params);
    }

    @Override
    public int marketingCodeCount(Map<String, Object> params) {
        return marketingManagementDAO.marketingCodeCount(params);
    }

    @Override
    public List<MarketingManagementDTO> marketingCodeList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MarketingManagementDTO> resultList = marketingManagementDAO.marketingCodeList(params);
        params.clear();
       /* Map<String, Object> finalParams = params;
        resultList.stream().forEach(marketingManagementDTO -> {
            // 查询是否已使用
            finalParams.put("code", marketingManagementDTO.getCode());
            finalParams.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode());
            if (marketingFlowService.findOneMarketingFlow(finalParams).getId() == null) {
                marketingManagementDTO.setIsDelete(StaticDataEnum.STATUS_1.getCode());
            }

        });*/
        return resultList;
    }

    @Override
    public void saveMarketingCode(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws Exception {
        Integer type = marketingManagementDTO.getType();
        if (null == marketingManagementDTO.getType()) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        if(type.equals(StaticDataEnum.MARKETING_TYPE_1.getCode())){
            //营销码
            encapaulatePromotionMarketingManagement(marketingManagementDTO, request);
        }else if(type.equals(StaticDataEnum.MARKETING_TYPE_2.getCode())){
            //邀请码
            encapaulateInvitationMarketingManagement(marketingManagementDTO, request);
        }

        //保存
        saveMarketingManagement(marketingManagementDTO, request);
    }


    /**
     * 封装保存营销码
     * @author zhangzeyuan
     * @date 2021/11/8 16:23
     * @param marketingManagementDTO
     * @param request
     */
    private void encapaulatePromotionMarketingManagement(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws Exception{
        //营销码
        if (marketingManagementDTO.getAmount() == null) {
            throw new BizException(I18nUtils.get("promotion.code.amount.error", getLang(request)));
        }
        if (marketingManagementDTO.getNumber() == null || marketingManagementDTO.getNumber().intValue() == 0) {
            throw new BizException(I18nUtils.get("promotion.code.number.error", getLang(request)));
        }

        //限制金额校验
        if(marketingManagementDTO.getAmountLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
            if(marketingManagementDTO.getAmount().compareTo(marketingManagementDTO.getMinTransAmount()) >= 0){
                throw new BizException(I18nUtils.get("marketing.limit.amount.error", getLang(request)));
            }
        }else{
            marketingManagementDTO.setMinTransAmount(marketingManagementDTO.getAmount());
        }

        Integer createMethod = marketingManagementDTO.getCreateMethod();

        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(1);

        if(createMethod.intValue() == StaticDataEnum.MARKETING_CREATE_METHOD_0.getCode()){

            //系统生成CODE
            String code = "";
            while (true) {
                code = InviteUtil.getBindNum(5);
                params.put("code", code);
                params.put("status",StaticDataEnum.STATUS_1.getCode());

                MarketingManagementDTO exist = findOneMarketingManagement(params);
                if (exist.getId() == null) {
                    break;
                }
            }
            marketingManagementDTO.setCode(code);
        }else{
            int count =  marketingManagementDAO.countByCode(marketingManagementDTO.getCode().toLowerCase());
            if (count > 0) {
                throw new BizException(I18nUtils.get("marketing.code.has.exist", getLang(request)));
            }
        }


        //不限制时间 直接开始活动
        if(marketingManagementDTO.getValidityLimitState().intValue() == StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_0.getCode()){
            marketingManagementDTO.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        }else {
            String validEndTimeStr = marketingManagementDTO.getValidEndTimeStr();
            String validStartTimeStr = marketingManagementDTO.getValidStartTimeStr();

            if(StringUtils.isBlank(validEndTimeStr) || StringUtils.isBlank(validStartTimeStr)){
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }

            //格式化
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            try{
                long startTimestamp = simpleDateFormat.parse(validStartTimeStr).getTime();
                long endTimestamp = simpleDateFormat.parse(validEndTimeStr).getTime();
                //当天0点
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Long todayZeroTimeStamp = calendar.getTime().getTime();
                //如果开始时间等于当天0点  直接开始活动
                if(todayZeroTimeStamp.compareTo(startTimestamp) == 0){
                    marketingManagementDTO.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
                }
                marketingManagementDTO.setValidStartTime(startTimestamp);
                marketingManagementDTO.setValidEndTime(endTimestamp);

            }catch (ParseException e){
                throw new BizException("Insert marketingManagement Error!");
            }

        }

        //营销码计算总费用
        marketingManagementDTO.setTotalAmount(marketingManagementDTO.getAmount().multiply(new BigDecimal(marketingManagementDTO.getNumber().toString())));


        //app 展示描述处理

        if(null == marketingManagementDTO.getCityLimitState()){
            marketingManagementDTO.setCityLimitState(0);
        }

        if(null == marketingManagementDTO.getRestaurantLimitState()){
            marketingManagementDTO.setRestaurantLimitState(0L);
        }

        String desc = marketingManagementDTO.getDescription();

        if(marketingManagementDTO.getCityLimitState() == 0 && marketingManagementDTO.getRestaurantLimitState() == 0){
            if(StringUtils.isBlank(desc)){
                throw new BizException(I18nUtils.get("promotion.desc.null", getLang(request)));
            }else{
                if(desc.length() > 50){
                    throw new BizException(I18nUtils.get("promotion.desc.max", getLang(request)));
                }
            }
        }

        if(marketingManagementDTO.getCityLimitState().intValue() > 0){
            desc = marketingManagementDTO.getCityName();
        }

        if(marketingManagementDTO.getRestaurantLimitState().longValue() > 0L){
            desc = marketingManagementDTO.getMerchantName();
        }

        marketingManagementDTO.setDescription(desc);
    }


    /**
     * 封装保存邀请码
     * @author zhangzeyuan
     * @date 2021/11/8 16:23
     * @param marketingManagementDTO
     * @param request
     */
    private void encapaulateInvitationMarketingManagement(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws Exception{

        Integer validitySelectValue = marketingManagementDTO.getValiditySelectValue();
        if (null == validitySelectValue) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        //限制金额校验
        if(marketingManagementDTO.getAmountLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
            if(marketingManagementDTO.getAmount().compareTo(marketingManagementDTO.getMinTransAmount()) >= 0){
                throw new BizException(I18nUtils.get("marketing.limit.amount.error", getLang(request)));
            }
        }else{
            marketingManagementDTO.setMinTransAmount(marketingManagementDTO.getAmount());
        }

        int validityLimitState = 0;

        if(validitySelectValue.intValue() > 0){
            //限制有效期
            validityLimitState = StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_1.getCode();

            //开始时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long startTime = calendar.getTime().getTime();

            //根据选择的值计算结束时间
//            long endTime = startTime + (validitySelectValue.intValue() * 30 * 86400000);

            calendar.clear();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if(validitySelectValue.intValue() < 12){
                calendar.add(Calendar.MONTH, validitySelectValue.intValue());
            }else if(validitySelectValue.intValue() == 12){
                calendar.add(Calendar.YEAR, 1);
            }else if(validitySelectValue.intValue() == 13){
                calendar.add(Calendar.YEAR, 2);
            }else if(validitySelectValue.intValue() == 14){
                calendar.add(Calendar.YEAR, 3);
            }
            long endTime = calendar.getTime().getTime();

            marketingManagementDTO.setValidStartTime(startTime);
            marketingManagementDTO.setValidEndTime(endTime);
        }else {
            //不限制
            validityLimitState = StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_0.getCode();
        }
        marketingManagementDTO.setValidityLimitState(validityLimitState);
        marketingManagementDTO.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        marketingManagementDTO.setInviteValidityType(validitySelectValue);
        marketingManagementDTO.setDescription("Share & Earn");

    }


    /**
     * 参数校验
     * @param marketingManagementDTO
     * @param request
     * @throws Exception
     */
    private void checkParams(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws Exception {
        if (marketingManagementDTO.getAmount() == null) {
            throw new BizException(I18nUtils.get("promotion.code.amount.error", getLang(request)));
        }
        if (marketingManagementDTO.getNumber() == null || marketingManagementDTO.getNumber().intValue() == 0) {
            throw new BizException(I18nUtils.get("promotion.code.number.error", getLang(request)));
        }
    }

    @Override
    public void deleteMarketingCode(Long id, HttpServletRequest request) throws Exception {
        MarketingManagementDTO marketingManagementDTO = findMarketingManagementById(id);
        if (marketingManagementDTO.getId() != null) {
            Map<String, Object> params = new HashMap<>(16);
            params.put("code", marketingManagementDTO.getCode());
            params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode());
            if (marketingFlowService.findOneMarketingFlow(params).getId() != null) {
                throw new BizException(I18nUtils.get("promotion.code.used", getLang(request)));
            }
            logicDeleteMarketingManagement(id, request);
        }
    }


    @Override
    public List<JSONObject> codeUseLog(String code, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(16);
        params.put("code", code);
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode());
        List<MarketingFlowDTO> marketingFlowDTOList = marketingFlowService.find(params, null, null);
        if (marketingFlowDTOList != null && !marketingFlowDTOList.isEmpty()) {
            List<JSONObject> resultList = new ArrayList<>(marketingFlowDTOList.size());
            marketingFlowDTOList.stream().sorted((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate())).forEach(marketingFlowDTO -> {
                UserDTO userDTO = userService.findUserById(marketingFlowDTO.getUserId());
                JSONObject result = new JSONObject();
                result.put("time", new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(marketingFlowDTO.getCreatedDate())));
                result.put("loginName", userDTO.getPhone());
                resultList.add(result);
            });
            return resultList;
        }
        return new ArrayList<>();
    }

    @Override
    public JSONObject getCodeMessage(Long userId, JSONObject requestInfo, HttpServletRequest request) throws BizException {
        String code = requestInfo.getString("code");
        if(StringUtils.isEmpty(code)){
            throw new BizException(I18nUtils.get("get.invite.code.fail", getLang(request)));
        }
//        code = code.toUpperCase();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("status",StaticDataEnum.STATUS_1.getCode());

        JSONObject result = new JSONObject();
        MarketingManagementDTO marketingManagementDTO = this.findOneMarketingManagement(params);
        try{
            if (marketingManagementDTO != null && marketingManagementDTO.getId() != null) {
                // 营销码是否可使用
                if(marketingManagementDTO.getState() == StaticDataEnum.STATUS_0.getCode() || marketingManagementDTO.getNumber() <= marketingManagementDTO.getUsedNumber()){
                    throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
                }
                // 查询是否已经领取
                params.clear();
                params.put("code",code);
                params.put("userId",userId);
                int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_0.getCode(),StaticDataEnum.TRANS_STATE_3.getCode()};
                params.put("stateList",stateList);
                MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
                if(marketingFlowDTO != null &&marketingFlowDTO.getId() != null){
                    throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
                }
                result.put("message",StringUtils.isBlank(marketingManagementDTO.getDescription()) ? "" : marketingManagementDTO.getDescription());
                result.put("amount",marketingManagementDTO.getAmount().toString());
            } else {
                // 查询推荐人
                params.put("inviteCode", code);
                UserDTO inviteUser = userService.findOneUser(params);
                if (inviteUser == null || inviteUser.getId() == null) {
                    throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
                }
                // 用户是否已经不是第一次消费
                UserDTO userDTO = userService.findUserById(userId);
                if (userDTO.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()) {
                    throw new BizException(I18nUtils.get("can.not.bind.references", getLang(request)));
                }
                //查询是否已绑定推荐人
                if(!(userDTO.getInviterId() == null || StringUtils.isEmpty(userDTO.getInviterId()+""))){
                    throw new BizException(I18nUtils.get("referrer.exist", getLang(request)));
                }
                //输入了自己的码
                if(userDTO.getInviteCode() .equals(code) ){
                    throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
                }
                result.put("message","Friend invitation");
                result.put("amount",walletConsumption);
            }
        }catch(Exception e){
            log.info("查询营销码不可用，message："+e.getMessage(),e);
            result.put("message",I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }


        return result;
    }



    /**
     * 获取所有的营销码列表
     *
     * @param data
     * @param request
     * @return int
     * @author zhangzeyuan
     * @date 2021/10/27 16:25
     */
    @Override
    public JSONObject getAllPromotionList(JSONObject data, HttpServletRequest request) throws Exception{
        JSONObject result = new JSONObject();

        //查询用户所有的券列表
        //不分页
        data.put("pageStatus", "2");
        data.put("scs", "state(asc),created_date(desc)");
        JSONObject couponJsonResult = userService.getMarketingCouponAccount(data, request);
        JSONArray couponJsonArray = couponJsonResult.getJSONArray("data");

        if(CollectionUtils.isEmpty(couponJsonArray)){
            result.put("pc", couponJsonResult.get("pc"));
            result.put("data", Collections.emptyList());
            return result;
        }

        //关联查询券的其他信息
        List<MarketingAccountDTO> couponList = JSONArray.parseArray(couponJsonArray.toJSONString(), MarketingAccountDTO.class);
        //获取营销ID集合
        Set<String> marketingIds = couponList.stream().map(MarketingAccountDTO::getMarkingId).collect(Collectors.toSet());
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(1);
        paramsMap.put("ids", marketingIds);
        //查询
        List<MarketingManagementDTO> allMarketing = marketingManagementDAO.findAllMarketing(paramsMap);
        if(CollectionUtils.isEmpty(allMarketing)){
            result.put("pc", couponJsonResult.get("pc"));
            result.put("data", Collections.emptyList());
            return result;
        }

        //结果转为map marketingId, list
        Map<Long, List<MarketingManagementDTO>> marketingMap = allMarketing.stream().collect(Collectors.groupingBy(MarketingManagementDTO::getId));

        //所有数据集合
        List<AppMarketingCouponDTO> allList = new ArrayList();

        //遍历封装信息
        for(MarketingAccountDTO coupon: couponList){

            List<MarketingManagementDTO> mapValue = marketingMap.get(Long.valueOf(coupon.getMarkingId()));

            if(CollectionUtils.isEmpty(mapValue)){
                continue;
            }

            //获取营销信息
            MarketingManagementDTO marketingManagement = mapValue.get(0);

            if(null == marketingManagement || null == marketingManagement.getId()){
                continue;
            }

            //活动状态
            int activityState = marketingManagement.getActivityState().intValue();

            AppMarketingCouponDTO resultDTO = new AppMarketingCouponDTO();
            resultDTO.setCouponId(coupon.getId());
            resultDTO.setMarketingId(marketingManagement.getId());
            resultDTO.setAmount(coupon.getBalance().toString());
            resultDTO.setCode(marketingManagement.getCode());
            resultDTO.setValidityLimitState(marketingManagement.getValidityLimitState());
            //2022年1月11日16:31:53 新增需求 该时间戳领取之前的 没有限制最低消费金额的优惠券 最低消费金额为 面额 / 0.4
            String managementId = marketingManagement.getId().toString();
            BigDecimal tempMinTranAmount = BigDecimal.ZERO;
            if(managementId.equals("620846989825331200") || managementId.equals("625123077544005632")){
                //拆分的券使用限制 优惠券金额 除 0.4
                tempMinTranAmount = coupon.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
            }else{
                Long createdDate = coupon.getCreatedDate();

                HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
                map.put("code", "updatePromoMinAmt");
                StaticDataDTO staticData = staticDataService.findOneStaticData(map);

                if(marketingManagement.getAmountLimitState().intValue() == StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_0.getCode()
                        && createdDate.compareTo(Long.parseLong(staticData.getValue())) <= 0) {
                    tempMinTranAmount = coupon.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
                }else{
                    tempMinTranAmount =  marketingManagement.getMinTransAmount();
                }
            }
            resultDTO.setMinTransAmount(tempMinTranAmount.stripTrailingZeros().scale() <= 0 ? tempMinTranAmount.setScale(0).toString() : tempMinTranAmount.toString());

            // 设置为限制金额
            resultDTO.setAmountLimitState(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode());

            resultDTO.setType(marketingManagement.getType());
            resultDTO.setDescription(marketingManagement.getDescription());
            resultDTO.setOrderTime(coupon.getCreatedDate());
            resultDTO.setModifyTime(coupon.getLastMoveDate());


            //限制有效期
            if(marketingManagement.getValidityLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
                //过期时间 格式化
                Long validEndTime = marketingManagement.getValidEndTime();
//                SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);

                SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
                String monthStr = monthFormat.format(validEndTime);
                resultDTO.setExpiredTimeStr(monthStr);
            }

            //数据类型
            int dataType = 0;
            //营销券状态
            int couponState = coupon.getState().intValue();
            if(couponState == StaticDataEnum.USER_MARKETING_COUPON_STATE_USED.getCode()){
                //已使用
                resultDTO.setShowState(StaticDataEnum.MARKETING_APP_SHOW_STATE_2.getCode());

                //使用时间 格式化

                SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
                Long lastMoveDate = coupon.getLastMoveDate();
                /*
                SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                String monthStr = monthFormat.format(lastMoveDate);
                String timeStr = timeFormat.format(lastMoveDate);
                resultDTO.setUsedTimeStr(monthStr + " at " + timeStr);*/
                resultDTO.setUsedTimeStr(monthFormat.format(lastMoveDate));

                //商户名称
                paramsMap.clear();
                paramsMap.put("marketingId", coupon.getId());
                paramsMap.put("userId", data.getString("userId"));
                resultDTO.setUsedAtMerchantName(marketingFlowService.getPaidMerchantName(paramsMap));

                dataType = 2;
            }else if(couponState == StaticDataEnum.USER_MARKETING_COUPON_STATE_NOT_USED.getCode()){
                //未使用
                if(activityState == StaticDataEnum.MARKETING_ACTIVITY_STATE_2.getCode()){
                    //活动结束 过期
                    resultDTO.setShowState(StaticDataEnum.MARKETING_APP_SHOW_STATE_3.getCode());
                    dataType = 2;

                }else if(activityState == StaticDataEnum.MARKETING_ACTIVITY_STATE_3.getCode()){
                    //活动终止
                    resultDTO.setShowState(StaticDataEnum.MARKETING_APP_SHOW_STATE_4.getCode());

                    //终止时间 格式化
                    Long terminatedTime = marketingManagement.getModifiedDate();
                    SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
                    String monthStr = monthFormat.format(terminatedTime);
                    resultDTO.setTerminatedTimeStr(monthStr);

                    dataType = 2;

                }else if(activityState == StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode()){
                    //可用
                    resultDTO.setShowState(StaticDataEnum.MARKETING_APP_SHOW_STATE_1.getCode());

                    dataType = 1;
                }else if(activityState == StaticDataEnum.MARKETING_ACTIVITY_STATE_0.getCode()){
                    continue;
                }
            }else if(couponState == StaticDataEnum.USER_MARKETING_COUPON_STATE_NOT_ACTIVATED.getCode()){
                //未激活
                resultDTO.setShowState(StaticDataEnum.MARKETING_APP_SHOW_STATE_5.getCode());
                resultDTO.setDescription("Share & Earn");
                dataType = 2;
            }

            resultDTO.setDataType(dataType);
            allList.add(resultDTO);
        }

        //封装返回数据
        JSONArray resultArray = new JSONArray();
        JSONObject availableResult = new JSONObject();
        availableResult.put("dataType", 1);
        JSONArray availableArray = new JSONArray();

        JSONObject usedResult = new JSONObject();
        usedResult.put("dataType", 2);
        JSONArray usedArray = new JSONArray();

        //分组 分别 排序
        List<AppMarketingCouponDTO> group1List = new ArrayList<>();
        List<AppMarketingCouponDTO> group2List = new ArrayList<>();
        List<AppMarketingCouponDTO> group3List = new ArrayList<>();
        List<AppMarketingCouponDTO> group4List = new ArrayList<>();
        List<AppMarketingCouponDTO> group5List = new ArrayList<>();
        Map<Integer, List<AppMarketingCouponDTO>> groupMap = allList.stream().collect(Collectors.groupingBy(AppMarketingCouponDTO::getShowState));
        Set<Integer> mapKeyList = groupMap.keySet();

        for (Integer key : mapKeyList){
            int keyValue = key.intValue();
            if(keyValue == StaticDataEnum.MARKETING_APP_SHOW_STATE_1.getCode()){
                group1List = groupMap.get(key);

                //按照领取时间排序
                Comparator<AppMarketingCouponDTO> comparingByCreateDate = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
                group1List.sort(comparingByCreateDate);

            }else if(keyValue == StaticDataEnum.MARKETING_APP_SHOW_STATE_2.getCode()){
                group2List = groupMap.get(key);

                Comparator<AppMarketingCouponDTO> comparingByModifyDate = Comparator.comparing(AppMarketingCouponDTO::getModifyTime).reversed();
                group2List.sort(comparingByModifyDate);

                /*//按照动账时间排序
                List<AppMarketingCouponDTO> usedList = groupMap.get(key);

                Comparator<AppMarketingCouponDTO> comparingByModifyDate = Comparator.comparing(AppMarketingCouponDTO::getModifyTime).reversed();
                usedList.sort(comparingByModifyDate);

                usedArray = JSONArray.parseArray(JSONObject.toJSONString(usedList)) ;*/
            }else if(keyValue == StaticDataEnum.MARKETING_APP_SHOW_STATE_3.getCode()){
                group3List = groupMap.get(key);

                Comparator<AppMarketingCouponDTO> comparingByCreateDate = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
                group3List.sort(comparingByCreateDate);
            }else if(keyValue == StaticDataEnum.MARKETING_APP_SHOW_STATE_4.getCode()){
                group4List = groupMap.get(key);

                Comparator<AppMarketingCouponDTO> comparingByCreateDate = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
                group4List.sort(comparingByCreateDate);
            }else if(keyValue == StaticDataEnum.MARKETING_APP_SHOW_STATE_5.getCode()){
                group5List = groupMap.get(key);

                Comparator<AppMarketingCouponDTO> comparingByCreateDate = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
                group5List.sort(comparingByCreateDate);
            }
        }

        List<AppMarketingCouponDTO> allToPageList = new ArrayList<>();
        allToPageList.addAll(group1List);
        allToPageList.addAll(group2List);
        allToPageList.addAll(group3List);
        allToPageList.addAll(group4List);
        allToPageList.addAll(group5List);

        //排序
/*        Comparator<AppMarketingCouponDTO> comparingByState = Comparator.comparing(AppMarketingCouponDTO::getShowState);
        Comparator<AppMarketingCouponDTO> comparingByTime = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
        allList.sort(comparingByState.thenComparing(comparingByTime));*/

        //数据总数
        int total = allToPageList.size();

        //分页信息
        String s = data.getString("s");
        String p = data.getString("p");
        int pageSize = 0;
        int page = 0;
        if (StringUtils.isNumeric(s)) {
            int size = Integer.parseInt(s);
            if (size > 0 && size < MAX_PAGE_SIZE) {
                pageSize = size;
            } else {
                pageSize = total <= MAX_PAGE_SIZE ? total : MAX_PAGE_SIZE;
            }
        } else {
            // s传all的时候
            pageSize = total;
        }
        if (StringUtils.isNumeric(p)) {
            page = Integer.parseInt(p);
        } else {
            page = 1;
        }

        //分页
        List<AppMarketingCouponDTO> pageList = allToPageList.stream().skip(pageSize * (page - 1)).limit(pageSize).collect(Collectors.toList());

        //分组封装数据
        Map<Integer, List<AppMarketingCouponDTO>> allGroupMap = pageList.stream().collect(Collectors.groupingBy(AppMarketingCouponDTO::getDataType));
        Set<Integer> allMapKeyList = allGroupMap.keySet();
        for (Integer key : allMapKeyList){
            if(key == 1){
                availableArray = JSONArray.parseArray(JSONObject.toJSONString(allGroupMap.get(key))) ;
            }else if(key == 2){
                usedArray = JSONArray.parseArray(JSONObject.toJSONString(allGroupMap.get(key))) ;
            }
        }

        usedResult.put("list", usedArray);
        availableResult.put("list", availableArray);

        resultArray.add(availableResult);
        resultArray.add(usedResult);

        PagingContext pagingContext = new PagingContext();
        pagingContext.setPageSize(pageSize);
        pagingContext.setTotal(total);
        pagingContext.setMaxPages(total > 0 ? (int) Math.ceil(total / pageSize) : Constant.ZERO);
        pagingContext.setPageIndex(page);

        result.put("pc", pagingContext);
        result.put("data", resultArray);
        return result;
    }



    /**
     * 获取可用的营销码列表
     *
     * @param data
     * @param request
     * @return int
     * @author zhangzeyuan
     * @date 2021/10/27 16:25
     */
    @Override
    public JSONObject getAvailablePromotionList(JSONObject data, HttpServletRequest request) throws Exception{
        JSONObject result = new JSONObject();

        //订单金额
        BigDecimal transAmount = data.getBigDecimal("transAmount");
        //商户名称
        Long merchantId = data.getLong("merchantId");
        MerchantDTO merchantDTO = merchantService.findMerchantById(merchantId);
        if(merchantDTO == null || merchantDTO.getId() == null){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        //查询用户可用的券列表
        data.put("pageStatus", "2");
        data.put("state", 1);
        data.put("scs", "created_date(desc)");

        JSONObject couponJsonResult = userService.getMarketingCouponAccount(data, request);
        JSONArray couponJsonArray = couponJsonResult.getJSONArray("data");

        if(CollectionUtils.isEmpty(couponJsonArray)){
            result.put("pc", couponJsonResult.get("pc"));
            result.put("data", Collections.emptyList());
            return result;
        }

        //关联查询券的其他信息
        List<MarketingAccountDTO> couponList = JSONArray.parseArray(couponJsonArray.toJSONString(), MarketingAccountDTO.class);
        Set<String> marketingIds = couponList.stream().map(MarketingAccountDTO::getMarkingId).collect(Collectors.toSet());
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(1);
        paramsMap.put("ids", marketingIds);
        List<MarketingManagementDTO> allMarketing = marketingManagementDAO.findAllMarketing(paramsMap);
        if(CollectionUtils.isEmpty(allMarketing)){
            result.put("pc", couponJsonResult.get("pc"));
            result.put("data", Collections.emptyList());
            return result;
        }

        //转为map
        Map<Long, List<MarketingManagementDTO>> marketingMap = allMarketing.stream().collect(Collectors.groupingBy(MarketingManagementDTO::getId));

        List<AppMarketingCouponDTO> resultData = new ArrayList<>();

        //封装返回数据
        //遍历封装信息
        for(MarketingAccountDTO coupon: couponList){

            List<MarketingManagementDTO> mapValue = marketingMap.get(Long.valueOf(coupon.getMarkingId()));

            if(CollectionUtils.isEmpty(mapValue)){
                continue;
            }

            //获取营销信息
            MarketingManagementDTO marketingManagement = mapValue.get(0);

            if(null == marketingManagement || null == marketingManagement.getId()){
                continue;
            }
            //活动状态
            int activityState = marketingManagement.getActivityState().intValue();

            if(activityState != StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode()){
                //活动结束 过期 终止 跳过
                continue;
            }

            //app展示券状态
            int payUseState = StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_1.getCode();

            //券码商户限制
            if(marketingManagement.getRestaurantLimitState() > 0){
                if(merchantDTO.getId().compareTo(marketingManagement.getRestaurantLimitState()) != 0){
                    //不可用
                    payUseState = StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode();
                }
            }

            //城市限制
            if(marketingManagement.getCityLimitState() > 0){
                if(!merchantDTO.getCity().equals(marketingManagement.getCityLimitState().toString()) ){
                    //不可用
                    payUseState = StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode();
                }
            }

            //限制金额 所有已经领了的券 没限制金额的  限制金额为 券金额 乘 2.5
            String managementId = marketingManagement.getId().toString();
            BigDecimal tempMinTranAmount = BigDecimal.ZERO;
            if(managementId.equals("620846989825331200") || managementId.equals("625123077544005632")){
                //拆分的券使用限制 优惠券金额 除 0.4
                tempMinTranAmount = coupon.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
            }else{
                Long createdDate = coupon.getCreatedDate();


                HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
                map.put("code", "updatePromoMinAmt");
                StaticDataDTO staticData = staticDataService.findOneStaticData(map);

                if(marketingManagement.getAmountLimitState().intValue() == StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_0.getCode()
                        && createdDate.compareTo(Long.parseLong(staticData.getValue())) <= 0) {
                    tempMinTranAmount = coupon.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
                }else{
                    tempMinTranAmount =  marketingManagement.getMinTransAmount();
                }


            }
            if(transAmount.compareTo(tempMinTranAmount) < 0){
                payUseState = StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode();
            }

            AppMarketingCouponDTO resultDTO = new AppMarketingCouponDTO();
            resultDTO.setCouponId(coupon.getId());
            resultDTO.setMarketingId(marketingManagement.getId());
            resultDTO.setAmount(coupon.getBalance().toString());
            resultDTO.setCode(marketingManagement.getCode());
            // 设置为限制金额
            resultDTO.setAmountLimitState(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode());
            resultDTO.setMinTransAmount(tempMinTranAmount.stripTrailingZeros().scale() <= 0 ? tempMinTranAmount.setScale(0).toString() : tempMinTranAmount.toString());

            resultDTO.setValidityLimitState(marketingManagement.getValidityLimitState());
            resultDTO.setType(marketingManagement.getType());
            resultDTO.setDescription(marketingManagement.getDescription());
            resultDTO.setOrderTime(coupon.getCreatedDate());

            //限制有效期
            if(marketingManagement.getValidityLimitState().equals(StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_1.getCode())){
                //过期时间 格式化
                Long validEndTime = marketingManagement.getValidEndTime();
//                SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);

                SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
                String monthStr = monthFormat.format(validEndTime);
                resultDTO.setExpiredTimeStr(monthStr);
            }

            resultDTO.setPayUseState(payUseState);
            resultData.add(resultDTO);
        }

        //排序
        Comparator<AppMarketingCouponDTO> comparingByTime = Comparator.comparing(AppMarketingCouponDTO::getOrderTime).reversed();
        Comparator<AppMarketingCouponDTO> comparingByState = Comparator.comparing(AppMarketingCouponDTO::getPayUseState);
        resultData.sort(comparingByState.thenComparing(comparingByTime));

        //数据总数
        int total = resultData.size();

        //分页信息
        String s = data.getString("s");
        String p = data.getString("p");
        int pageSize = 0;
        int page = 0;
        if (StringUtils.isNumeric(s)) {
            int size = Integer.parseInt(s);
            if (size > 0 && size < MAX_PAGE_SIZE) {
                pageSize = size;
            } else {
                pageSize = total <= MAX_PAGE_SIZE ? total : MAX_PAGE_SIZE;
            }
        } else {
            // s传all的时候
            pageSize = total;
        }
        if (StringUtils.isNumeric(p)) {
            page = Integer.parseInt(p);
        } else {
            page = 1;
        }

        //分页
        List<AppMarketingCouponDTO> pageList = resultData.stream().skip(pageSize * (page - 1)).limit(pageSize).collect(Collectors.toList());

        PagingContext pagingContext = new PagingContext();
        pagingContext.setPageSize(pageSize);
        pagingContext.setTotal(total);
        pagingContext.setMaxPages(total > 0 ? (int) Math.ceil(total / pageSize) : Constant.ZERO);
        pagingContext.setPageIndex(page);

        result.put("pc", pagingContext);
        result.put("data", pageList);
        return result;
    }


    /**
     * 输入字符搜索营销码
     *
     * @param code
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/10/27 17:02
     */
    @Override
    public MarketingManagementDTO findPromotionCodeByCode(String code) {
        //查询是否是营销码
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("code", code);
//        paramMap.put("type", StaticDataEnum.MARKETING_TYPE_1.getCode());
        paramMap.put("activityState", StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        return marketingManagementDAO.getPromotionData(paramMap);
    }

    /**
     * app promotion 模块 code 搜索
     *
     * @param code
     * @return com.uwallet.pay.main.model.dto.MarketingManagementDTO
     * @author zhangzeyuan
     * @date 2021/10/29 16:20
     */
    @Override
    public MarketingManagementDTO appPromotionSearch(String code, Long userId, HttpServletRequest request) throws Exception{
        MarketingManagementDTO result = null;
        //查询是否是营销码
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(3);
        paramMap.put("code", code.toLowerCase());
        paramMap.put("type", StaticDataEnum.MARKETING_TYPE_1.getCode());
        paramMap.put("activityState", StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        result = marketingManagementDAO.getPromotionData(paramMap);
        if(null != result && null != result.getId()){
            verifyQueryPromotionCode(result, userId, code, request);
            result.setAmountLimitState(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode());
            return result;
        }
        //查询是否是邀请码
        paramMap.clear();
        paramMap.put("type", StaticDataEnum.MARKETING_TYPE_2.getCode());
        paramMap.put("activityState", StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        result = marketingManagementDAO.getPromotionData(paramMap);

        if(null != result && null != result.getId()){
            verifyQueryPromotionCode(result, userId, code, request);
            result.setAmountLimitState(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode());
            return result;
        }

        throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
    }


    /**
     * 添加营销券组件
     * @author zhangzeyuan
     * @date 2021/11/9 20:10
     * @param userId
     * @param marketingManagement
     * @param transType
     * @param notActivatedStatus
     * @param request
     * @return com.uwallet.pay.main.model.dto.MarketingAccountDTO
     */
    @Override
    public MarketingAccountDTO addMarketingPromotionCode(Long userId, MarketingManagementDTO marketingManagement, Integer transType, Boolean notActivatedStatus,
                                                         HttpServletRequest request) throws Exception{
        MarketingAccountDTO resultDTO;

        //条件校验
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);

        //校验营销活动可领取次数
        if(marketingManagement.getReceiveNumber().compareTo(marketingManagement.getNumber()) == 0){
            throw new BizException(I18nUtils.get("promotion.receive.no.count", getLang(request)));
        }

        //是否已经领过
        //todo 生产还要改
        if(!marketingManagement.getId().toString().equals("620846989825331200")){
            int[] stateList = {StaticDataEnum.TRANS_STATE_0.getCode(), StaticDataEnum.TRANS_STATE_1.getCode(), StaticDataEnum.TRANS_STATE_3.getCode()};

            map.put("userId", userId);
            map.put("marketingManageId", marketingManagement.getId());
            map.put("direction", 0);
            map.put("stateList", stateList);
            int count = marketingFlowService.count(map);
            if(count > 0){
                throw new BizException(I18nUtils.get("promotion.receive.repeat", getLang(request)));
            }
        }

        //记录红包入账流水
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setAmount(marketingManagement.getAmount());
        marketingFlowDTO.setCode(marketingManagement.getCode());
        marketingFlowDTO.setDescription(marketingManagement.getDescription());
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        marketingFlowDTO.setTransType(transType);
        marketingFlowDTO.setUserId(userId);
        marketingFlowDTO.setMarketingManageId(marketingManagement.getId());
        marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

        //封装添加卡券需要的参数
        JSONObject addJson = new JSONObject();
        addJson.put("userId", userId);
        addJson.put("balance", marketingManagement.getAmount());
        addJson.put("markingId", marketingManagement.getId());
        addJson.put("channelSerialnumber", marketingFlowDTO.getId());

        addJson.put("notActivated", notActivatedStatus);
        JSONObject accountResultJson = null;

        //更新营销码领取数量
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(2);
        paramsMap.put("id", marketingManagement.getId());
        paramsMap.put("type", marketingManagement.getType());
        int addReceivedNumber = marketingManagementDAO.addReceivedNumber(paramsMap);
        if(addReceivedNumber < 1){
            throw new BizException(I18nUtils.get("promotion.receive.no.count", getLang(request)));
        }

        try{
            accountResultJson = serverService.addPromotionCode(addJson, request);
        }catch (Exception e){
            //回滚领取数量
            marketingManagementDAO.rollBackReceivedNumber(paramsMap);
            //添加失败
            MarketingFlowDTO updateRecord = new MarketingFlowDTO();
            updateRecord.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);
            throw new BizException(I18nUtils.get("promotion.code.add.error", getLang(request)));
        }

        resultDTO =  JSONObject.parseObject(accountResultJson.toString(), MarketingAccountDTO.class);

        //成功后处理
        //更新流水状态
        MarketingFlowDTO updateRecord = new MarketingFlowDTO();
        updateRecord.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        marketingFlowDTO.setMarketingId(resultDTO.getId());
        marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);

        return resultDTO;
    }


    @Override
    public MarketingAccountDTO addInvitationPromotionCode(Long userId, Long invitedId, Integer addType, Long flowId, MarketingManagementDTO marketingManagement, Boolean notActivatedStatus, HttpServletRequest request) throws Exception{
        MarketingAccountDTO resultDTO;

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);

        int[] stateList = {StaticDataEnum.TRANS_STATE_0.getCode(), StaticDataEnum.TRANS_STATE_1.getCode(), StaticDataEnum.TRANS_STATE_3.getCode()};
        //条件校验
        map.put("id", userId);
        UserDTO user = userService.findOneUser(map);
        map.clear();
        map.put("id", invitedId);
        UserDTO inviteUser = userService.findOneUser(map);

        if(null == user || null == user.getId()){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        if(null == inviteUser || null == inviteUser.getId()){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        if(null == user.getInviterId()){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        if(user.getInviterId().compareTo(inviteUser.getId()) != 0){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        Long addUserId = 0L;
        int transType = 0;
        if(addType == StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode()){

            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode();

            if(notActivatedStatus){
                if(user.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
                    throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
                }
            }
            addUserId = userId;

            map.clear();
            map.put("userId", userId);
            map.put("direction", StaticDataEnum.DIRECTION_0.getCode());
            map.put("stateList", stateList);
//            map.put("marketingManageId", marketingManagement.getId());
            map.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode());
            int count = marketingFlowService.count(map);
            if(count > 0){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //更新邀请人 邀请人数、累计邀请金额信息
            userService.updateRegister(invitedId);
            userService.updateWalletGrandTotal(marketingManagement.getInvitedId(), marketingManagement.getAmount(), null);
        }else{
            addUserId = invitedId;
            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode();
        }

        //记录红包入账流水
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setAmount(marketingManagement.getAmount());
        marketingFlowDTO.setCode(marketingManagement.getCode());
        marketingFlowDTO.setDescription(marketingManagement.getDescription());
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        marketingFlowDTO.setTransType(transType);
        marketingFlowDTO.setUserId(addUserId);
        marketingFlowDTO.setFlowId(flowId);
        marketingFlowDTO.setMarketingManageId(marketingManagement.getId());
        marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

        //封装添加卡券需要的参数
        JSONObject addJson = new JSONObject();
        addJson.put("userId", addUserId);
        addJson.put("balance", marketingManagement.getAmount());
        addJson.put("markingId", marketingManagement.getId());
        addJson.put("channelSerialnumber", marketingFlowDTO.getId());
        addJson.put("notActivated", notActivatedStatus);


        //更新营销码领取数量
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(2);
        paramsMap.put("id", marketingManagement.getId());
        paramsMap.put("type", marketingManagement.getType());
        int addReceivedNumber = marketingManagementDAO.addReceivedNumber(paramsMap);
        if(addReceivedNumber < 1){
            throw new BizException(I18nUtils.get("promotion.receive.no.count", getLang(request)));
        }

        JSONObject accountResultJson = null;
        try{
            accountResultJson = serverService.addPromotionCode(addJson, request);
        }catch (Exception e){
            //添加失败
            //回滚领取数量
            marketingManagementDAO.rollBackReceivedNumber(paramsMap);

            MarketingFlowDTO updateRecord = new MarketingFlowDTO();
            updateRecord.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);
            throw new BizException(I18nUtils.get("promotion.code.add.error", getLang(request)));
        }

        resultDTO =  JSONObject.parseObject(accountResultJson.toString(), MarketingAccountDTO.class);

        //成功后处理
        //更新营销码领取数量
        if(addType == StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode()){
            //更新用户实得红包金额
            userService.updateWalletGrandTotal(userId, BigDecimal.ZERO, marketingManagement.getAmount());
        }

        //更新流水状态
        MarketingFlowDTO updateRecord = new MarketingFlowDTO();
        updateRecord.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        marketingFlowDTO.setMarketingId(resultDTO.getId());
        marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);
        return resultDTO;
    }



    @Override
    public MarketingAccountDTO addOldUserInvitationPromotionCode(Long userId, Integer transType, Long flowId,  MarketingManagementDTO marketingManagement, Boolean notActivatedStatus, HttpServletRequest request) throws Exception{
        MarketingAccountDTO resultDTO;

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);

        //记录红包入账流水
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setAmount(marketingManagement.getAmount());
        marketingFlowDTO.setCode(marketingManagement.getCode());
        marketingFlowDTO.setDescription(marketingManagement.getDescription());
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        marketingFlowDTO.setTransType(transType);
        marketingFlowDTO.setFlowId(flowId);
        marketingFlowDTO.setUserId(userId);
        marketingFlowDTO.setMarketingManageId(marketingManagement.getId());
        marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

        //封装添加卡券需要的参数
        JSONObject addJson = new JSONObject();
        addJson.put("userId", userId);
        addJson.put("balance", marketingManagement.getAmount());
        addJson.put("markingId", marketingManagement.getId());
        addJson.put("channelSerialnumber", marketingFlowDTO.getId());
        addJson.put("notActivated", notActivatedStatus);
        JSONObject accountResultJson = null;
        try{
            accountResultJson = serverService.addPromotionCode(addJson, request);
        }catch (Exception e){
            //添加失败
            MarketingFlowDTO updateRecord = new MarketingFlowDTO();
            updateRecord.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);
            //todo 添加失败提示
            throw new BizException(I18nUtils.get("promotion.code.add.error", getLang(request)));
        }

        resultDTO =  JSONObject.parseObject(accountResultJson.toString(), MarketingAccountDTO.class);

        //成功后处理
        //更新营销码领取数量
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(2);
        paramsMap.put("id", marketingManagement.getId());
        paramsMap.put("type", marketingManagement.getType());
        marketingManagementDAO.addReceivedNumber(paramsMap);

        //更新流水状态
        MarketingFlowDTO updateRecord = new MarketingFlowDTO();
        updateRecord.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        marketingFlowDTO.setMarketingId(resultDTO.getId());
        marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateRecord, request);
        return resultDTO;
    }




    /**
     * app通过搜索添加营销券
     *
     * @param userId
     * @param marketingId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/11/9 20:12
     */
    @Override
    public JSONObject appAddPromotionCode(Long userId, Long marketingId, String inputCode, BigDecimal transAmount, Long merchantId, HttpServletRequest request) throws Exception {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);
        map.put("id", marketingId);
        MarketingManagementDTO marketingManagementDTO = marketingManagementDAO.selectOneDTO(map);
        if(null == marketingManagementDTO || null ==marketingManagementDTO.getId()){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }


        MarketingAccountDTO addResult = null;

        if(marketingManagementDTO.getType().equals(StaticDataEnum.MARKETING_TYPE_1.getCode())){
            addResult = addMarketingPromotionCode(userId, marketingManagementDTO, StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode(), false, request);
        }else if(marketingManagementDTO.getType().equals(StaticDataEnum.MARKETING_TYPE_2.getCode())){
            //查询邀请人
            map.clear();
            map.put("inviteCode", inputCode);
            UserDTO invitedUser = userService.findOneUser(map);
            if(null == invitedUser || null == invitedUser.getId()){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            //绑定邀请关系
            UserDTO updateRecord = new UserDTO();
            updateRecord.setInviterId(invitedUser.getId());
            userService.updateUser(userId, updateRecord, request);

            addResult = addInvitationPromotionCode(userId, invitedUser.getId(), StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode(), null, marketingManagementDTO, true, request);
        }

        //封装返回数据及可用状态
        JSONObject result = new JSONObject();
        //app展示券状态
        result.put("id", addResult.getId().toString());
        result.put("balance", addResult.getBalance().setScale(2, BigDecimal.ROUND_DOWN).toString());
        result.put("type", marketingManagementDTO.getType());
        if(null != transAmount && null != merchantId){
            MerchantDTO merchantDTO = merchantService.findMerchantById(merchantId);
            if(merchantDTO == null || merchantDTO.getId() == null){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //券码商户限制
            if(marketingManagementDTO.getRestaurantLimitState() > 0){

                if(merchantDTO.getId().compareTo(marketingManagementDTO.getRestaurantLimitState()) != 0){
                    //不可用
                    result.put("payUseState", StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode());
                    return result;
                }
            }

            //城市限制
            if(marketingManagementDTO.getCityLimitState() > 0){
                if(!merchantDTO.getCity().equals(marketingManagementDTO.getCityLimitState().toString()) ){
                    //不可用
                    result.put("payUseState", StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode());
                    return result;
                }
            }

            //限制金额
            /*if(marketingManagementDTO.getAmountLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
                if(transAmount.compareTo(marketingManagementDTO.getMinTransAmount()) < 0){
                    //不可用
                    result.put("payUseState", StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode());
                    return result;
                }
            }*/
            if(transAmount.compareTo(marketingManagementDTO.getMinTransAmount()) < 0){
                //不可用
                result.put("payUseState", StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_2.getCode());
                return result;
            }

            result.put("payUseState", StaticDataEnum.MARKETING_APP_PAY_SHOW_STATE_1.getCode());
        }
        return result;
    }


    private void verifyQueryPromotionCode(MarketingManagementDTO marketingManagement, Long userId, String invitedCode, HttpServletRequest request) throws BizException{
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);

        int[] stateList = {StaticDataEnum.TRANS_STATE_0.getCode(), StaticDataEnum.TRANS_STATE_1.getCode(), StaticDataEnum.TRANS_STATE_3.getCode()};

        if(marketingManagement.getType().equals(StaticDataEnum.MARKETING_TYPE_1.getCode())){

            //是否已经领过
            map.put("userId", userId);
//            map.put("marketingManageId", marketingManagement.getId());
            map.put("code", marketingManagement.getCode());
            map.put("direction", 0);
            map.put("stateList", stateList);
            int count = marketingFlowService.count(map);
            if(count > 0){
                throw new BizException(I18nUtils.get("promotion.receive.repeat", getLang(request)));
            }

            //营销码
            //校验营销活动可领取次数
            if(marketingManagement.getReceiveNumber().compareTo(marketingManagement.getNumber()) == 0){
                throw new BizException(I18nUtils.get("promotion.receive.no.count", getLang(request)));
            }

        }else if(marketingManagement.getType().equals(StaticDataEnum.MARKETING_TYPE_2.getCode())){
            //邀请码

            //查询本人 如果本人已经绑定过邀请码 则搜索不到
            map.clear();
            map.put("id", userId);
            UserDTO user = userService.findOneUser(map);
            if(null == user || null == user.getId() || null != user.getInviterId() || user.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //根据CODE查询邀请人 邀请人不存在 或者 和本人相同 则搜索不到
            map.clear();
            map.put("inviteCode", invitedCode);
            UserDTO inviteUser = userService.findOneUser(map);
            if(null == inviteUser || null == inviteUser.getId() || inviteUser.getId().compareTo(userId) == 0){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //之前领取过营销券
            map.clear();
            map.put("userId", userId);
            map.put("direction", StaticDataEnum.DIRECTION_0.getCode());
            map.put("stateList", stateList);
            map.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode());
            int count2 = marketingFlowService.count(map);
            if(count2 > 0){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
        }
    }


    private void verifyAddPromotionCode(MarketingManagementDTO marketingManagement, Long userId,  HttpServletRequest request) throws BizException{
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);

        int[] stateList = {StaticDataEnum.TRANS_STATE_0.getCode(), StaticDataEnum.TRANS_STATE_1.getCode(), StaticDataEnum.TRANS_STATE_3.getCode()};

        if(marketingManagement.getType().equals(StaticDataEnum.MARKETING_TYPE_1.getCode())){
            //营销码
            //校验营销活动可领取次数
            if(marketingManagement.getReceiveNumber().compareTo(marketingManagement.getNumber()) == 0){
                throw new BizException(I18nUtils.get("promotion.receive.no.count", getLang(request)));
            }

            //是否已经领过
            map.put("userId", userId);
//            map.put("marketingId", marketingManagement.getId());
            map.put("marketingId", marketingManagement.getId());
            map.put("direction", 0);
            map.put("stateList", stateList);
            int count = marketingFlowService.count(map);
            if(count > 0){
                throw new BizException(I18nUtils.get("promotion.receive.repeat", getLang(request)));
            }

        }else if(marketingManagement.getType().equals(StaticDataEnum.MARKETING_TYPE_2.getCode())){
            //邀请码

            //查询本人 如果本人已经绑定过邀请码
            map.clear();
            map.put("id", userId);
            UserDTO user = userService.findOneUser(map);
            if(null == user || null == user.getId() || null == user.getInviterId()){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //根据CODE查询邀请人 邀请人不存在 或者 和本人相同
            map.clear();
            map.put("id", user.getInviterId());
            UserDTO inviteUser = userService.findOneUser(map);
            if(null == inviteUser || null == inviteUser.getId()){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //之前领取过营销券
            map.clear();
            map.put("userId", userId);
            map.put("direction", 0);
            map.put("stateList", stateList);
            map.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode());
            int count2 = marketingFlowService.count(map);
            if(count2 > 0){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            marketingManagement.setInvitedId(inviteUser.getId());
        }
    }


    /**
     * 修改注册邀请码
     *
     * @param marketingManagementDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/11/8 17:54
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvitationMarketingManagement(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException {
        //查询库里存在的邀请记录
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("type", StaticDataEnum.MARKETING_TYPE_2.getCode());
        params.put("status",StaticDataEnum.STATUS_1.getCode());
        MarketingManagement marketingManagement = marketingManagementDAO.selectOne(params);

        //限制金额校验
        if(marketingManagementDTO.getAmountLimitState().equals(StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_1.getCode())){
            if(marketingManagementDTO.getAmount().compareTo(marketingManagementDTO.getMinTransAmount()) >= 0){
                throw new BizException(I18nUtils.get("marketing.limit.amount.error", getLang(request)));
            }
        }else{
            marketingManagementDTO.setMinTransAmount(marketingManagementDTO.getAmount());
        }
        //将旧记录的ID改为新ID， 状态改为0
//        marketingManagementDAO.updateInvitationCodeNotAvailable(SnowflakeUtil.generateId(), marketingManagement.getId());
        this.logicDeleteMarketingManagement(marketingManagement.getId(),request);
        //生成一条指定ID的记录

        if(null != marketingManagementDTO.getValiditySelectValue()){
            int validitySelectValue = marketingManagementDTO.getValiditySelectValue().intValue();

            int validityLimitState = 0;

            if(validitySelectValue > 0){
                //限制有效期
                validityLimitState = StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_1.getCode();

                //开始时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startTime = calendar.getTime().getTime();


                calendar.clear();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 59);

                if(validitySelectValue < 12){
                    calendar.add(Calendar.MONTH, validitySelectValue);
                }else if(validitySelectValue == 12){
                    calendar.add(Calendar.YEAR, 1);
                }else if(validitySelectValue == 13){
                    calendar.add(Calendar.YEAR, 2);
                }else if(validitySelectValue == 14){
                    calendar.add(Calendar.YEAR, 3);
                }
                long endTime = calendar.getTime().getTime();
                marketingManagementDTO.setValidStartTime(startTime);
                marketingManagementDTO.setValidEndTime(endTime);
            }else {
                //不限制
                validityLimitState = StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_0.getCode();
            }
            marketingManagementDTO.setValidityLimitState(validityLimitState);
            marketingManagementDTO.setInviteValidityType(validitySelectValue);
        }

        MarketingManagement marketingManagementNew = BeanUtil.copyProperties(marketingManagementDTO, new MarketingManagement());

        MarketingManagement marketingManagementNew1 = (MarketingManagement) this.packAddBaseProps(marketingManagementNew, request);
//        marketingManagementNew1.setId(marketingManagement.getId());
        marketingManagementNew1.setDescription("Share & Earn");
        marketingManagementNew1.setActivityState(StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode());
        int cnt = marketingManagementDAO.insert(marketingManagementNew1);
        if (cnt != 1) {
            log.error("update error, data:{}", marketingManagementDTO);
            throw new BizException("update marketingManagement Error!");
        }
    }

    @Override
    public int addUsedNumber(Long id) throws BizException {
        int i =  marketingManagementDAO.addUsedNumber( id);
        if (i != 1) {
            log.error("addUsedNumber error, i = 0");
            throw new BizException("addUsedNumber Error!");
        }
        return i;
    }

    @Override
    public int addUsedNumberRollback(Long id) throws BizException {
        int i =  marketingManagementDAO.addUsedNumberRollback( id);
        if (i != 1) {
            log.error("addUsedNumberRollback error, i = 0");
            throw new BizException("addUsedNumberRollback Error!");
        }
        return i;
    }
    @Override
    public void marketingManagerHandle() {
        // 获得零点时间
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long time =  now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        try {
            marketingManagementDAO.startMarketing(time,System.currentTimeMillis());
        }catch (Exception e){
            log.info("营销卡券活动开始跑批异常："+e.getMessage(),e);
        }

        try {
            marketingManagementDAO.endMarketing(time-1000,System.currentTimeMillis());
        }catch (Exception e){
            log.info("营销卡券活动结束跑批异常："+e.getMessage(),e);
        }

    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long time =  now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(time-1000);
    }

    @Override
    public void updateNumber(Long id, MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException {
        MarketingManagementDTO data = this.findMarketingManagementById(id);
        if(data == null ){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        if(marketingManagementDTO.getNumber() == null ){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        MarketingManagementDTO updateData = new MarketingManagementDTO();
        updateData.setNumber( marketingManagementDTO.getNumber());
        MarketingManagement marketingManagement = BeanUtil.copyProperties(updateData, new MarketingManagement());
        marketingManagement.setId(id);
        int cnt = marketingManagementDAO.update((MarketingManagement) this.packModifyBaseProps(marketingManagement, request));
        if (cnt != 1) {
            log.error("update error, data:{}", data);
            throw new BizException("update marketingManagement Error!");
        }
    }

    @Override
    public int userPromotionCount(Map<String, Object> params) {
        return marketingManagementDAO.userPromotionCount(params);
    }

    @Override
    public List<UserPromotionDTO> userPromotionList(Map<String, Object> params) {
        return marketingManagementDAO.userPromotionList(params);
    }


}
