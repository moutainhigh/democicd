package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ApiMerchantApplicationDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ApiMerchant;
import com.uwallet.pay.main.model.entity.ApiMerchantApplication;
import com.uwallet.pay.main.model.entity.ContactPerson;
import com.uwallet.pay.main.service.ApiMerchantApplicationService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.ApiMerchantService;
import com.uwallet.pay.main.service.ContactPersonService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * h5 api 商户申请表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: h5 api 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-09-23 10:25:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ApiMerchantApplicationServiceImpl extends BaseServiceImpl implements ApiMerchantApplicationService {

    @Autowired
    private ApiMerchantApplicationDAO apiMerchantApplicationDAO;
    @Autowired
    private ApiMerchantService apiMerchantService;
    @Autowired
    private ContactPersonService contactPersonService;

    @Override
    public Long saveApiMerchantApplication(@NonNull ApiMerchantApplicationDTO apiMerchantApplicationDTO, HttpServletRequest request) throws BizException {
        ApiMerchantApplication apiMerchantApplication = BeanUtil.copyProperties(apiMerchantApplicationDTO, new ApiMerchantApplication());
        log.info("save ApiMerchantApplication:{}", apiMerchantApplication);
        apiMerchantApplication = (ApiMerchantApplication) this.packAddBaseProps(apiMerchantApplication, request);
        if (apiMerchantApplicationDAO.insert(apiMerchantApplication) != 1) {
            log.error("insert error, data:{}", apiMerchantApplication);
            throw new BizException("Insert apiMerchantApplication Error!");
        }
        return  apiMerchantApplication.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiMerchantApplicationList(@NonNull List<ApiMerchantApplication> apiMerchantApplicationList, HttpServletRequest request) throws BizException {
        if (apiMerchantApplicationList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = apiMerchantApplicationDAO.insertList(apiMerchantApplicationList);
        if (rows != apiMerchantApplicationList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, apiMerchantApplicationList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateApiMerchantApplication(@NonNull Long id, @NonNull ApiMerchantApplicationDTO apiMerchantApplicationDTO, HttpServletRequest request) throws BizException {
        log.info("full update apiMerchantApplicationDTO:{}", apiMerchantApplicationDTO);
        ApiMerchantApplication apiMerchantApplication = BeanUtil.copyProperties(apiMerchantApplicationDTO, new ApiMerchantApplication());
        apiMerchantApplication.setId(id);
        int cnt = apiMerchantApplicationDAO.update((ApiMerchantApplication) this.packModifyBaseProps(apiMerchantApplication, request));
        if (cnt != 1) {
            log.error("update error, data:{}", apiMerchantApplicationDTO);
            throw new BizException("update apiMerchantApplication Error!");
        }
    }

    @Override
    public void updateApiMerchantApplicationSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        apiMerchantApplicationDAO.updatex(params);
    }

    @Override
    public void logicDeleteApiMerchantApplication(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = apiMerchantApplicationDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteApiMerchantApplication(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = apiMerchantApplicationDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ApiMerchantApplicationDTO findApiMerchantApplicationById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ApiMerchantApplicationDTO apiMerchantApplicationDTO = apiMerchantApplicationDAO.selectOneDTO(params);
        return apiMerchantApplicationDTO;
    }

    @Override
    public ApiMerchantApplicationDTO findOneApiMerchantApplication(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ApiMerchantApplication apiMerchantApplication = apiMerchantApplicationDAO.selectOne(params);
        ApiMerchantApplicationDTO apiMerchantApplicationDTO = new ApiMerchantApplicationDTO();
        if (null != apiMerchantApplication) {
            BeanUtils.copyProperties(apiMerchantApplication, apiMerchantApplicationDTO);
        }
        return apiMerchantApplicationDTO;
    }

    @Override
    public List<ApiMerchantApplicationDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiMerchantApplicationDTO> resultList = apiMerchantApplicationDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return apiMerchantApplicationDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return apiMerchantApplicationDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = apiMerchantApplicationDAO.groupCount(conditions);
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
        return apiMerchantApplicationDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = apiMerchantApplicationDAO.groupSum(conditions);
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
    public void newMerchantSubmitAudit(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        if( !requestInfo.containsKey("id")){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        ApiMerchantApplicationDTO merchantApplicationDTO = this.findApiMerchantApplicationById(requestInfo.getLong("id"));
        if(merchantApplicationDTO == null || merchantApplicationDTO.getId() == null){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        ApiMerchantDTO merchantData = JSONObject.parseObject(JSONObject.parseObject(merchantApplicationDTO.getData()).toJSONString(), ApiMerchantDTO.class);
        if(merchantApplicationDTO.getMerchantId() == null ){
            merchantData.setId(apiMerchantService.saveApiMerchant(merchantData,request));
        }else{
            merchantData.setId(merchantApplicationDTO.getMerchantId());
        }

        // 录入企业合伙人信息
        apiMerchantService.replenishDirectorAndOwner(merchantData,request);

        // 录入联系人信息
        List<ContactPersonDTO> contactPersonDTOList = merchantData.getContactPersonDTOList();
        if (contactPersonDTOList != null && !contactPersonDTOList.isEmpty()) {
            List<ContactPerson> contactPersonList = new ArrayList<>(contactPersonDTOList.size());
            contactPersonService.deleteContactPersonByMerchantId(merchantData.getId());
            contactPersonDTOList.forEach(contactPersonDTO -> {
                ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
                contactPerson = (ContactPerson) this.packAddBaseProps(contactPerson, request);
                contactPerson.setMerchantId(merchantData.getId());
                contactPersonList.add(contactPerson);
            });
            contactPersonService.saveContactPersonList(contactPersonList, request);
        }

        // 商户状态为入网审核中
        ApiMerchantDTO updateMerchant = new ApiMerchantDTO();
        updateMerchant.setState(StaticDataEnum.MERCHANT_STATE_2.getCode());
        apiMerchantService.updateApiMerchant(merchantData.getId(),updateMerchant,request);

        // 更新审核表状态
        merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_2.getCode());
        merchantApplicationDTO.setMerchantId(merchantData.getId());
        this.updateApiMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
    }

    @Override
    public String saveH5MerchantMessage(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        JSONObject newData = requestInfo.getJSONObject("merchantDTO");
        if(newData == null || newData.size() == 0){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        boolean newFlag = false;
        ApiMerchantDTO newMerchant ;
        JSONObject merchantData;
        ApiMerchantApplicationDTO merchantApplicationDTO = new ApiMerchantApplicationDTO();
        if(requestInfo.containsKey("id") && StringUtils.isNotEmpty(requestInfo.getString("id") )){
            Long id = requestInfo.getLong("id");
            merchantApplicationDTO = this.findApiMerchantApplicationById(id);
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
            merchantData.put("marchantRate",0);
            merchantData.put("basePayRate",0);
            newFlag = true;
        }
//        merchantData.put("userId",merchantData.getString("userId"));
        newMerchant = JSONObject.parseObject(merchantData.toString(), ApiMerchantDTO.class);
        // 必要信息校验
        if(StringUtils.isEmpty(newMerchant.getAbn())){
            throw new BizException(I18nUtils.get("docusign.abn.isNull", getLang(request)));
        }
        if (newMerchant.getAbn().length() > Validator.ABN && newMerchant.getAbn().length() < Validator.ACN) {
            throw new BizException(I18nUtils.get("abn.error", getLang(request)));
        }
        if (newMerchant.getMerchantClass() == null) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        // 校验是否已有商户
        Map<String, Object > params = new HashMap<>(8);
        params.put("abn",newMerchant.getAbn());
        params.put("practicalName",newMerchant.getPracticalName());
        ApiMerchantDTO merchantDTO = apiMerchantService.findOneApiMerchant(params);
        if(merchantDTO != null && merchantDTO.getId() != null && merchantDTO.getState() != StaticDataEnum.APPROVE_STATE_.getCode() ){
            throw new BizException(I18nUtils.get("merchant.exist", getLang(request)));
        }
        // 查询是否有在申请的商户
        params.put("type", StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        params.put("haveMessage",1);
        ApiMerchantApplicationDTO checkData = this.findOneApiMerchantApplication(params);
        if(checkData != null && checkData.getId() != null && !checkData.getId().equals(requestInfo.getLong("id"))){
            throw new BizException(I18nUtils.get("have.no.submit.merchant", getLang(request)));
        }

        if(newMerchant.getMerchantClass() == StaticDataEnum.H5_MERCHANT_CLASS_0.getCode()){
            merchantData.put("superMerchantId",null);
//                this.updateApiMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
        }
        if(newFlag){
            merchantApplicationDTO.setMerchantClass(newMerchant.getMerchantClass());
            merchantApplicationDTO.setAbn(newMerchant.getAbn());
            merchantApplicationDTO.setPracticalName(newMerchant.getPracticalName());
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_0.getCode());
            merchantApplicationDTO.setData(merchantData.toJSONString());
            merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());

            merchantApplicationDTO.setId(this.saveApiMerchantApplication(merchantApplicationDTO,request));

        }else{
            merchantApplicationDTO.setMerchantClass(newMerchant.getMerchantClass());
            merchantApplicationDTO.setAbn(newMerchant.getAbn());
            merchantApplicationDTO.setPracticalName(newMerchant.getPracticalName());
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_0.getCode());
            merchantApplicationDTO.setData(merchantData.toJSONString());
//            if(newMerchant.getMerchantClass() == StaticDataEnum.H5_MERCHANT_CLASS_0.getCode()){
//                newMerchant.setSuperMerchantId(merchantApplicationDTO.getId());
//            }
            this.updateApiMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);

        }

        return merchantApplicationDTO.getId().toString();
    }

    @Override
    public JSONObject getMerchantMessage(Long id, HttpServletRequest request) {
        ApiMerchantApplicationDTO merchantApplicationDTO = this.findApiMerchantApplicationById(id);
        JSONObject result =  JSONObject.parseObject(merchantApplicationDTO.getData());
        return result;
    }


}
