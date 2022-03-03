package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.MerchantApplicationDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 商户申请表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-04-14 11:28:05
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class MerchantApplicationServiceImpl extends BaseServiceImpl implements MerchantApplicationService {

    @Autowired
    private MerchantApplicationDAO merchantApplicationDAO;
    @Autowired
    @Lazy
    private MerchantService merchantService;
    @Autowired
    private MerchantLoginService merchantLoginService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private UserService  userService;

    /**
     * 账号默认密码
     * */
    private static final String MERCHANT_DEFULT_PASSWORD="aaa123";

    @Override
    public Long saveMerchantApplication(@NonNull MerchantApplicationDTO merchantApplicationDTO, HttpServletRequest request) throws BizException {
        MerchantApplication merchantApplication = BeanUtil.copyProperties(merchantApplicationDTO, new MerchantApplication());
        merchantApplication = (MerchantApplication) this.packAddBaseProps(merchantApplication, request);
        log.info("save MerchantApplication:{}", merchantApplication);
        if (merchantApplicationDAO.insert(merchantApplication) != 1) {
            log.error("insert error, data:{}", merchantApplication);
            throw new BizException("Insert merchantApplication Error!");
        }
        return merchantApplication.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchantApplicationList(@NonNull List<MerchantApplication> merchantApplicationList, HttpServletRequest request) throws BizException {
        if (merchantApplicationList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = merchantApplicationDAO.insertList(merchantApplicationList);
        if (rows != merchantApplicationList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, merchantApplicationList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMerchantApplication(@NonNull Long id, @NonNull MerchantApplicationDTO merchantApplicationDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantApplicationDTO:{}", merchantApplicationDTO);
        MerchantApplication merchantApplication = BeanUtil.copyProperties(merchantApplicationDTO, new MerchantApplication());
        merchantApplication.setId(id);
        int cnt = merchantApplicationDAO.update((MerchantApplication) this.packModifyBaseProps(merchantApplication, request));
        if (cnt != 1) {
            log.error("update error, data:{}", merchantApplicationDTO);
            throw new BizException("update merchantApplication Error!");
        }
    }

    @Override
    public void updateMerchantApplicationSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        merchantApplicationDAO.updatex(params);
    }

    @Override
    public void logicDeleteMerchantApplication(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = merchantApplicationDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMerchantApplication(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = merchantApplicationDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MerchantApplicationDTO findMerchantApplicationById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MerchantApplicationDTO merchantApplicationDTO = merchantApplicationDAO.selectOneDTO(params);
        return merchantApplicationDTO;
    }

    @Override
    public MerchantApplicationDTO findOneMerchantApplication(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MerchantApplication merchantApplication = merchantApplicationDAO.selectOne(params);
        MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
        if (null != merchantApplication) {
            BeanUtils.copyProperties(merchantApplication, merchantApplicationDTO);
        }
        return merchantApplicationDTO;
    }

    @Override
    public List<MerchantApplicationDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantApplicationDTO> resultList = merchantApplicationDAO.selectDTO(params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:HH dd/MM/yyyy");
        for (MerchantApplicationDTO merchantApplicationDTO : resultList) {
            Long createdBy = merchantApplicationDTO.getCreatedBy();
            if (createdBy!=null&&createdBy!=0){
                AdminDTO adminById = adminService.findAdminById(createdBy);
                merchantApplicationDTO.setOperator(adminById.getRealName());
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
        return merchantApplicationDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return merchantApplicationDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = merchantApplicationDAO.groupCount(conditions);
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
        return merchantApplicationDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = merchantApplicationDAO.groupSum(conditions);
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
    public String saveNewMerchantMessage(JSONObject requestInfo, HttpServletRequest request) throws Exception{
        JSONObject newData = requestInfo.getJSONObject("merchantDTO");
        if(newData == null || newData.size() == 0){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        boolean newFlag = false;
        MerchantDTO newMerchant ;
        JSONObject merchantData;
        MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
        if(requestInfo.containsKey("id") && StringUtils.isNotEmpty(requestInfo.getString("id") )){
            Long id = requestInfo.getLong("id");
            merchantApplicationDTO = this.findMerchantApplicationById(id);
            if(merchantApplicationDTO == null || merchantApplicationDTO.getId() == null ){
                throw new BizException(I18nUtils.get("merchant.not.found", getLang(request)));

            }
            String data = merchantApplicationDTO.getData();
            JSONObject oldData = JSONObject.parseObject(data);

            // 获得最新的商户数据
            Iterator iter  = newData.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry) iter.next();
                if(entry.getValue() != null ){
                    String key = entry.getKey().toString();
                    oldData.put(key,entry.getValue());
                }
            }
            merchantData = oldData;
            newFlag = false;
        }else{
            merchantData = requestInfo.getJSONObject("merchantDTO");
            newFlag = true;
        }
        merchantData.put("userId",merchantData.getString("userId"));
        newMerchant = JSONObject.parseObject(merchantData.toString(), MerchantDTO.class);
        // 必要信息校验
        if(newMerchant.getUserId() == null){
            throw new BizException(I18nUtils.get("docusign.abn.isNull", getLang(request)));
        }
        if(StringUtils.isEmpty(newMerchant.getAbn())){
            throw new BizException(I18nUtils.get("docusign.abn.isNull", getLang(request)));
        }
        if (newMerchant.getAbn().length() > Validator.ABN && newMerchant.getAbn().length() < Validator.ACN) {
            throw new BizException(I18nUtils.get("abn.error", getLang(request)));
        }

        // 校验是否已有商户
        Map<String, Object > params = new HashMap<>(8);
        params.put("abn",newMerchant.getAbn());
        params.put("practicalName",newMerchant.getPracticalName());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO != null && merchantDTO.getId() != null && merchantDTO.getState() != StaticDataEnum.APPROVE_STATE_.getCode() ){
            throw new BizException(I18nUtils.get("merchant.exist", getLang(request)));
        }
        // 查询是否有在申请的商户
        params.put("type", StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        params.put("haveMessage",1);
        MerchantApplicationDTO checkData = this.findOneMerchantApplication(params);
        if(checkData != null && checkData.getId() != null && !checkData.getId().equals(requestInfo.getLong("id"))){
            throw new BizException(I18nUtils.get("have.no.submit.merchant", getLang(request)));

        }

        // 查询用户信息
        MerchantLoginDTO merchantLoginDTO = merchantLoginService.findMerchantLoginById(newMerchant.getUserId());
        if(merchantLoginDTO == null || merchantLoginDTO.getId() == null ){
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }

        // 计算额外折扣到期日期
        if (merchantDTO.getExtraDiscountPeriodChoice() != null) {
            merchantDTO.setMerchantApprovePassTime(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            // 取得六个月后时间
            cal.add(Calendar.MONTH, merchantDTO.getExtraDiscountPeriodChoice());
            merchantDTO.setExtraDiscountPeriod(cal.getTimeInMillis());
        }

        if(newFlag){
            merchantApplicationDTO.setAbn(newMerchant.getAbn());
            merchantApplicationDTO.setPracticalName(newMerchant.getPracticalName());
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_0.getCode());
            merchantApplicationDTO.setData(merchantData.toJSONString());
            merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
//            merchantApplicationDTO.setUserId(newMerchant.getUserId());
            merchantApplicationDTO.setEmail(merchantLoginDTO.getEmail());
            merchantApplicationDTO.setId(this.saveMerchantApplication(merchantApplicationDTO,request));
        }else{
            merchantApplicationDTO.setAbn(newMerchant.getAbn());
            merchantApplicationDTO.setPracticalName(newMerchant.getPracticalName());
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_0.getCode());
            merchantApplicationDTO.setData(merchantData.toJSONString());
//            merchantApplicationDTO.setUserId(newMerchant.getUserId());
            merchantApplicationDTO.setEmail(merchantLoginDTO.getEmail());
            this.updateMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
        }

        return merchantApplicationDTO.getId().toString();
    }

    @Override
    public void reject(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        if( !requestInfo.containsKey("id")){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        MerchantApplicationDTO merchantApplicationDTO = this.findMerchantApplicationById(requestInfo.getLong("id"));
        if(merchantApplicationDTO == null || merchantApplicationDTO.getId() == null || merchantApplicationDTO.getState() == StaticDataEnum.APPROVE_STATE_1.getCode()){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        merchantApplicationDTO.setState( StaticDataEnum.APPROVE_STATE_.getCode());
        merchantApplicationDTO.setRemark(requestInfo.getString("remark"));
        this.updateMerchantApplication(requestInfo.getLong("id"),merchantApplicationDTO,request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void newMerchantSubmitAudit(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        if( !requestInfo.containsKey("id")){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        MerchantApplicationDTO merchantApplicationDTO = this.findMerchantApplicationById(requestInfo.getLong("id"));
        if(merchantApplicationDTO == null || merchantApplicationDTO.getId() == null){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        MerchantDTO merchantData = JSONObject.parseObject(JSONObject.parseObject(merchantApplicationDTO.getData()).toJSONString(), MerchantDTO.class);
        if(merchantApplicationDTO.getMerchantId() == null ){
            MerchantDTO merchantDTO = new MerchantDTO();
            merchantDTO.setUserId(0L);
            merchantData.setId(merchantService.saveMerchant(merchantData,request));
        }else{
            merchantData.setId(merchantApplicationDTO.getMerchantId());
        }

        // 调用基础信息，以录入经纬度
        merchantService.replenishMerchant(merchantData, request);
//        merchantService.replenishBank(merchantDTO,request);
//        merchantService.replenishLogo(merchantDTO,request);
//        merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, request);
        // 调用费率修改，以计算费率
        merchantService.replenishDirectorAndOwner(merchantData,request);
        merchantService.updateRate(merchantData.getId(),JSONObject.parseObject(JSONObject.parseObject(merchantApplicationDTO.getData()).toJSONString(), MerchantDetailDTO.class),request);

        // 商户状态为入网审核中
        MerchantDTO updateMerchant = new MerchantDTO();
        updateMerchant.setState(StaticDataEnum.MERCHANT_STATE_2.getCode());
        merchantService.updateMerchant(merchantData.getId(),updateMerchant,request);

        // 更新审核表状态
        merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_2.getCode());
        merchantApplicationDTO.setMerchantId(merchantData.getId());
        this.updateMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);

    }

    @Override
    public Long newAccountSubmitAudit(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
        String email = requestInfo.getString("email");
        Long id1 = requestInfo.getLong("id");
        // 判断用户表是否存在该邮箱 为商户
        JSONObject userParam=new JSONObject(2);
        userParam.put("userType",StaticDataEnum.USER_TYPE_20.getCode());
        userParam.put("email",email);
        UserDTO oneUser = userService.findOneUser(userParam);
        if (oneUser.getId()!=null){
            // 当前邮箱已注册
            throw new BizException(I18nUtils.get("the.current.mailbox.is.registered", getLang(request)));
        }
        // 判断商户申请表是否存在该邮箱 类型为开户，审核状态为审核通过，如果有则拒绝
        JSONObject merchantParam=new JSONObject(3);
        merchantParam.put("type",StaticDataEnum.MERCHANT_APPLICATION_TYPE_0.getCode());
        merchantParam.put("state",StaticDataEnum.MERCHANT_STATE_1.getCode());
        merchantParam.put("email",email);
        MerchantApplicationDTO oneMerchantApplication = this.findOneMerchantApplication(merchantParam);
        if (null!=oneMerchantApplication.getId()){
            // 已通过申请
            throw new BizException(I18nUtils.get("the.current.application.has.passed", getLang(request)));
        }
        // 判断是修改还是新增，如果记录表有记录（类型为开户，状态为未提交审核）则修改，没有则新增
        JSONObject addMerchantParam=new JSONObject(3);
        if (id1 != null) {
            addMerchantParam.put("id",id1);
            MerchantApplicationDTO oneMerchantApplication1 = this.findOneMerchantApplication(addMerchantParam);
            if (oneMerchantApplication1.getId()!=null){
                // 已存在该记录
                oneMerchantApplication1.setEmail(email);
                this.updateMerchantApplication(id1,merchantApplicationDTO, request);
                return id1;
            }
        }
        merchantApplicationDTO.setEmail(email);
        merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_0.getCode());
        merchantApplicationDTO.setState(StaticDataEnum.MERCHANT_STATE_0.getCode());
        Long id = this.saveMerchantApplication(merchantApplicationDTO, request);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantApplicationNew(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String email = requestInfo.getString("email");
        String code = requestInfo.getString("code");
        Long id = requestInfo.getLong("id");
        UserDTO userDTO = new UserDTO();
        if (StringUtils.isAllBlank(email,code)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 修改商户申请表状态
        userDTO.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
        userDTO.setPassword(MERCHANT_DEFULT_PASSWORD);
        userDTO.setEmail(email.toLowerCase());
        userDTO.setSecurityCode(code);
        userService.saveUser(userDTO, request);
        // 修改一条记录状态
        if (id!=null){
            JSONObject param=new JSONObject();
            param.put("email",email);
            param.put("state",StaticDataEnum.MERCHANT_STATE_1.getCode());
            param.put("id",id);
            merchantApplicationDAO.updateState(param);
        }else {
            MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
            merchantApplicationDTO.setEmail(email);
            merchantApplicationDTO.setState(StaticDataEnum.MERCHANT_STATE_1.getCode());
            merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_0.getCode());
            this.saveMerchantApplication(merchantApplicationDTO,request);
        }

    }
    @Override
    public JSONObject getMerchantMessage(Long id, HttpServletRequest request) {
        MerchantApplicationDTO merchantApplicationDTO = this.findMerchantApplicationById(id);
        JSONObject result =  JSONObject.parseObject(merchantApplicationDTO.getData());
        MerchantLoginDTO merchantLoginDTO = merchantLoginService.findMerchantLoginById(result.getLong("userId"));
        result.put("userEmail",merchantLoginDTO.getEmail());
        return result;
    }

    @Override
    public List<MerchantLoginDTO> getMerchantEmails(String email) {
//        if(StringUtils.isEmpty(email)){
//            return null;
//        }
        Map<String ,Object> params = new HashMap<>(8);
        params.put("email",email);
        params.put("userType",StaticDataEnum.USER_TYPE_20.getCode());
        List<MerchantLoginDTO> list = merchantLoginService.findByEmail(params);
        log.info("list:"+list.get(0).toString());
        return list == null || list.size() == 0 ? null : list;
    }

}
