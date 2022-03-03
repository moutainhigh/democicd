package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.InviteUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ApiMerchantDAO;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * api商户信息表j
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: api商户信息表j
 * @author: zhoutt
 * @date: Created in 2021-09-02 17:35:15
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ApiMerchantServiceImpl extends BaseServiceImpl implements ApiMerchantService {

    @Autowired
    private ApiMerchantDAO apiMerchantDAO;
    @Autowired
    private StaticDataService staticDataService;
    @Value("${google.mapGeocodingAPI}")
    private String googleMapsApi;

    @Value("${google.mapGeocodingAPIKey}")
    private String googleApiKey;

    @Autowired
    private DirectorService directorService;
    @Autowired
    private ShareholderService shareholderService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private TrusteeService trusteeService;
    @Autowired
    private ContactPersonService contactPersonService;
    @Autowired
    private ApiMerchantApplicationService apiMerchantApplicationService ;
    @Autowired
    private ApiApproveLogService apiApproveLogService;
    @Autowired
    private ServerService serverService;

    @Override
    public Long saveApiMerchant(@NonNull ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) throws BizException {
        ApiMerchant apiMerchant = BeanUtil.copyProperties(apiMerchantDTO, new ApiMerchant());
        log.info("save ApiMerchant:{}", apiMerchant);
        apiMerchant = (ApiMerchant) this.packAddBaseProps(apiMerchant, request);
        Long merchantId = this.getNextId();
        apiMerchant.setId(merchantId);
        if (apiMerchantDAO.insert(apiMerchant) != 1) {
            log.error("insert error, data:{}", apiMerchant);
            throw new BizException("Insert apiMerchant Error!");
        }
        return  apiMerchant.getId();
    }

    private Long getNextId() {
        Long id = 10000L;
        try {
           id =  apiMerchantDAO.getMaxId();
        }catch (Exception e){
            return  10000L;
        }
        if(id != null){
            return  id+1;
        }else{
            return  id;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiMerchantList(@NonNull List<ApiMerchant> apiMerchantList, HttpServletRequest request) throws BizException {
        if (apiMerchantList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = apiMerchantDAO.insertList(apiMerchantList);
        if (rows != apiMerchantList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, apiMerchantList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateApiMerchant(@NonNull Long id, @NonNull ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) throws BizException {
        log.info("full update apiMerchantDTO:{}", apiMerchantDTO);
        ApiMerchant apiMerchant = BeanUtil.copyProperties(apiMerchantDTO, new ApiMerchant());
        apiMerchant.setId(id);
        int cnt = apiMerchantDAO.update((ApiMerchant) this.packModifyBaseProps(apiMerchant, request));
        if (cnt != 1) {
            log.error("update error, data:{}", apiMerchantDTO);
            throw new BizException("update apiMerchant Error!");
        }
    }

    @Override
    public void updateApiMerchantSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        apiMerchantDAO.updatex(params);
    }

    @Override
    public void logicDeleteApiMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = apiMerchantDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteApiMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = apiMerchantDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ApiMerchantDTO findApiMerchantById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ApiMerchantDTO apiMerchantDTO = apiMerchantDAO.selectOneDTO(params);
        return apiMerchantDTO;
    }

    @Override
    public ApiMerchantDTO findOneApiMerchant(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ApiMerchant apiMerchant = apiMerchantDAO.selectOne(params);
        ApiMerchantDTO apiMerchantDTO = new ApiMerchantDTO();
        if (null != apiMerchant) {
            BeanUtils.copyProperties(apiMerchant, apiMerchantDTO);
        }
        return apiMerchantDTO;
    }

    @Override
    public List<ApiMerchantDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiMerchantDTO> resultList = apiMerchantDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return apiMerchantDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return apiMerchantDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = apiMerchantDAO.groupCount(conditions);
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
        return apiMerchantDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = apiMerchantDAO.groupSum(conditions);
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
    public List<ApiMerchantDTO> getApiClearMerchantList(Map<String, Object> params) {
        return apiMerchantDAO.getApiClearMerchantList(params);
    }

    @Override
    public List<Map<String, String>> findSuperMerchant() {
        Map<String, Object> params  = new HashMap<>();
        params.put("merchantClass", StaticDataEnum.H5_MERCHANT_CLASS_0.getCode());
        params.put("isAvailable",StaticDataEnum.MERCHANT_AVAILABLE_1.getCode());
        List<Map<String, String>> list = apiMerchantDAO.findSuperMerchantMap(params);

        return  list;


    }

    @Override
    public void replenishDirectorAndOwner(ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Long merchantId = merchantDTO.getId();
        // 董事、股东、受益人信息添加
        Integer merchantEntityType = merchantDTO.getEntityType();
        // 当为公司类型时，添加董事、股东信息
        if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {

            directorService.deleteDirectorByMerchantId(merchantId);
            shareholderService.deleteShareholderByMerchantId(merchantId);

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {

            List<PartnerDTO> partnerDTOList = merchantDTO.getPartnerDTOList();

            if (partnerDTOList != null && !partnerDTOList.isEmpty()) {
                partnerService.deletePartnerByMerchantId(merchantId);
                List<Partner> partnerList = new ArrayList<>(partnerDTOList.size());
                partnerDTOList.forEach(partnerDTO -> {
                    Partner partner = BeanUtil.copyProperties(partnerDTO, new Partner());
                    partner = (Partner) this.packAddBaseProps(partner, request);
                    partner.setMerchantId(merchantId);
                    partnerList.add(partner);
                });
                partnerService.savePartnerList(partnerList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<TrusteeDTO> trusteeDTOList = merchantDTO.getTrusteeDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (trusteeDTOList != null && !trusteeDTOList.isEmpty()) {
                trusteeService.deleteTrusteeByMerchantId(merchantId);
                List<Trustee> trusteeList = new ArrayList<>(trusteeDTOList.size());
                trusteeDTOList.forEach(trusteeDTO -> {
                    Trustee trustee = BeanUtil.copyProperties(trusteeDTO, new Trustee());
                    trustee = (Trustee) this.packAddBaseProps(trustee, request);
                    trustee.setMerchantId(merchantId);
                    trusteeList.add(trustee);
                });
                trusteeService.saveTrusteeList(trusteeList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        }
        // 二十个文件
        List<JSONObject> files = merchantDTO.getFiles();
        if (files != null && files.size() > 0) {
            JSONArray filesUrl = new JSONArray();
            files.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                filesUrl.add(url);
            });
            merchantDTO.setFileList(filesUrl.toJSONString());
        }
        // 信托文件
        List<JSONObject> trusteeFiles = merchantDTO.getTrusteeFiles();
        if (trusteeFiles != null && trusteeFiles.size() > 0) {
            JSONArray filesUrl = new JSONArray();
            trusteeFiles.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                filesUrl.add(url);
            });
            merchantDTO.setTrusteeFileList(filesUrl.toJSONString());
        }
        this.updateApiMerchant(merchantDTO.getId(), merchantDTO, request);
    }

    @Override
    public void refuseMerchant(Long id, String remark, HttpServletRequest request) throws BizException {
        if (remark.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        ApiMerchantDTO apiMerchantDTO = this.findApiMerchantById(id);
        apiMerchantDTO.setState(StaticDataEnum.MERCHANT_STATE_.getCode());
        apiMerchantDTO.setRemark(remark);
        this.updateApiMerchant(id,apiMerchantDTO,request);

        audit(id, apiMerchantDTO, request);

        // 查询录入的记录
        Map<String ,Object > params = new HashMap<>();
        params.put("merchantId" ,  id);
        params.put("type" ,StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        params.put("state" ,StaticDataEnum.APPROVE_STATE_2.getCode());
        ApiMerchantApplicationDTO merchantApplicationDTO = apiMerchantApplicationService.findOneApiMerchantApplication(params);
        if(merchantApplicationDTO != null && merchantApplicationDTO.getId() != null){
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_.getCode());
            apiMerchantApplicationService.updateApiMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passMerchant(Long id, MerchantDetailH5DTO merchantDetailDTO, HttpServletRequest request) throws Exception {
        ApiMerchantDTO merchantDTO = this.findApiMerchantById(merchantDetailDTO.getId());
        // 将审核状态改为审批中并增加审核记录
        merchantDTO.setIsAvailable(StaticDataEnum.AVAILABLE_1.getCode());
        merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_1.getCode());
        merchantDTO.setId(merchantDTO.getId());
        merchantDTO.setMerchantApprovePassTime(System.currentTimeMillis());

        // 生成账号和密码
        merchantDTO.setSecret(  InviteUtil.getBindNum(16).toLowerCase());
        merchantDTO.setKey( InviteUtil.getBindNum(32).toUpperCase());
        if(merchantDTO.getMerchantClass() == StaticDataEnum.H5_MERCHANT_CLASS_0.getCode()){
            merchantDTO.setSuperMerchantId( id );
        }

        ApiMerchantDTO merchantResult = null;
        try {
            merchantResult = audit(merchantDetailDTO.getId(), merchantDTO, request);
        } catch (Exception e) {
            log.info("pass merchant audit failed, data:{}, error message:{}, e:{}", merchantDTO, e.getMessage(), e);
            throw new BizException(I18nUtils.get("pass.merchant.failed", getLang(request)));
        }

        Map<String ,Object> params = new HashMap<>();
        params.put("merchantId",merchantDTO.getId());
        params.put("type",StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        ApiMerchantApplicationDTO merchantApplicationDTO = apiMerchantApplicationService.findOneApiMerchantApplication(params);
        if(merchantApplicationDTO != null && merchantApplicationDTO.getId() != null){
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_1.getCode());
            apiMerchantApplicationService.updateApiMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
        }

        // 向分期付系统新增商户
        CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
        creditMerchantDTO.setMerchantId(merchantDTO.getId());
        creditMerchantDTO.setUserId(merchantDTO.getId());
        creditMerchantDTO.setMerchantName(merchantResult.getPracticalName());
        creditMerchantDTO.setBusinessType(merchantResult.getEntityType());
//        creditMerchantDTO.setMainBusiness(merchantResult.getMainBusiness());
        creditMerchantDTO.setAbn(merchantResult.getAbn());
        creditMerchantDTO.setAccountNo(merchantResult.getAccountNo());
//        creditMerchantDTO.setPercentageToUser(merchantDTO.getBaseRate());
//        creditMerchantDTO.setPercentageToPlatform(merchantDTO.getAppChargeRate());
        creditMerchantDTO.setEmail(merchantResult.getEmail());
        creditMerchantDTO.setAddress(merchantResult.getAddress());
        creditMerchantDTO.setCity(merchantResult.getCity());
        creditMerchantDTO.setRegion(merchantResult.getMerchantState());
        creditMerchantDTO.setPostcode(merchantResult.getPostcode());
        creditMerchantDTO.setCountry(merchantResult.getCounty());
        creditMerchantDTO.setAcn(merchantResult.getAcn());
        creditMerchantDTO.setMerchantSource(StaticDataEnum.ORDER_SOURCE_1.getCode());
//        creditMerchantDTO.setExtraDiscount(merchantDTO.getExtraDiscount());
//        creditMerchantDTO.setExtraDiscountPeriod(merchantDTO.getExtraDiscountPeriod());

        JSONObject merchantInfo = serverService.getMerchantByMerchantId(merchantDTO.getId());
        if (merchantInfo.getJSONObject("data") != null) {
            serverService.updateMerchant(merchantDTO.getId(), JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
        } else {
            serverService.saveMerchant(JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)));
        }

        this.updateApiMerchant(id,merchantDTO,request);
    }

    /**
     * 商户审核并增加审核记录
     * @param id
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    private ApiMerchantDTO audit(Long id, ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        // 补充信息也要记录到申请表中
        merchantDTO  = this.getOtherMerchantMessage(merchantDTO);
        // 增加审核记录
        ApiApproveLogDTO approveLogDTO = new ApiApproveLogDTO();
        approveLogDTO.setMerchantId(id);
        approveLogDTO.setApproveType(StaticDataEnum.APPROVE_LOG_APPROVE_TYPE_0.getCode());
        approveLogDTO.setData(JSONObject.toJSONString(merchantDTO));
        approveLogDTO.setState(merchantDTO.getState());
        approveLogDTO.setApprovedBy(getUserId(request));
        approveLogDTO.setRemark(merchantDTO.getRemark());
        approveLogDTO.setMerchantCity(Integer.parseInt(merchantDTO.getMerchantState()));
        approveLogDTO.setMerchantClass(merchantDTO.getMerchantClass());
        apiApproveLogService.saveApiApproveLog(approveLogDTO, request);
        return  merchantDTO;
    }


    private ApiMerchantDTO getOtherMerchantMessage(ApiMerchantDTO merchantDTO) {
        Map<String ,Object> params = new HashMap<>(8);
        params.put("merchantId", merchantDTO.getId());
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
//            List<BeneficiaryDTO> beneficiaryDTOList = beneficiaryService.find(params, null, null);
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            merchantDTO.setDirectorDTOList(directorDTOList);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
//            merchantDTO.setBeneficiaryDTOList(beneficiaryDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
            List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
            merchantDTO.setPartnerDTOList(partnerDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            merchantDTO.setDirectorDTOList(directorDTOList);
            List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            merchantDTO.setTrusteeDTOList(trusteeDTOList);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
        }

        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);
        merchantDTO.setContactPersonDTOList(contactPersonDTOList);


        if (!StringUtils.isEmpty(merchantDTO.getFileList())) {
            List<String> urls = JSONArray.parseArray(merchantDTO.getFileList(), String.class);
            if (urls != null && urls.size() > 0) {
                List<JSONObject> urlFile = new ArrayList<>(urls.size());
                urls.stream().forEach(s -> {
                    JSONObject file = new JSONObject();
                    String a = SnowflakeUtil.getEncryptionId();
                    String[] url = s.split("/");
                    file.put("uid", a);
                    file.put("name", url[url.length - 1]);
                    file.put("url", s);
                    file.put("status", "done");
                    urlFile.add(file);
                });
                merchantDTO.setFiles(urlFile);
            }
        }
        // 信托文件
        if (!StringUtils.isEmpty(merchantDTO.getTrusteeFileList())) {
            List<String> trusteeUrls = JSONArray.parseArray(merchantDTO.getTrusteeFileList(), String.class);
            if (trusteeUrls != null && trusteeUrls.size() > 0) {
                List<JSONObject> trusteeUrlFile = new ArrayList<>(trusteeUrls.size());
                trusteeUrls.stream().forEach(s -> {
                    JSONObject file = new JSONObject();
                    String a = SnowflakeUtil.getEncryptionId();
                    String[] url = s.split("/");
                    file.put("uid", a);
                    file.put("name", url[url.length - 1]);
                    file.put("url", s);
                    file.put("status", "done");
                    trusteeUrlFile.add(file);
                });
                merchantDTO.setTrusteeFiles(trusteeUrlFile);
            }
        }
//        if (!StringUtils.isEmpty(merchantDTO.getDocusignFiles())) {
//            JSONObject docusignFiles = JSONObject.parseObject(merchantDTO.getDocusignFiles());
//            List<JSONObject> docusignFileList = new ArrayList<>(3);
//            docusignFiles.keySet().stream().forEach(key -> {
//                if (!key.startsWith("envelopId")) {
//                    JSONObject file = new JSONObject();
//                    file.put("name", key);
//                    file.put("url", docusignFiles.getString(key));
//                    docusignFileList.add(file);
//                }
//            });
//            merchantDTO.setDocusignFileList(docusignFileList);
//        }
        return merchantDTO;
    }



}
