package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.SignErrorCode;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.dao.AccessPlatformDAO;
import com.uwallet.pay.main.exception.SignException;
import com.uwallet.pay.main.model.dto.AccessMerchantDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformInfoDTO;
import com.uwallet.pay.main.model.dto.CreditMerchantDTO;
import com.uwallet.pay.main.model.entity.AccessPlatform;
import com.uwallet.pay.main.service.AccessMerchantService;
import com.uwallet.pay.main.service.AccessPlatformService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.util.HuffmanCode;
import com.uwallet.pay.main.util.I18nUtils;
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
 * 接入方平台表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 接入方平台表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class AccessPlatformServiceImpl extends BaseServiceImpl implements AccessPlatformService {

    @Autowired
    private AccessPlatformDAO accessPlatformDAO;
    @Autowired
    private AccessMerchantService accessMerchantService;
    @Autowired
    private ServerService serverService;


    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Long saveAccessPlatform(@NonNull AccessPlatformDTO accessPlatformDTO, HttpServletRequest request) throws BizException {
        AccessPlatform accessPlatform = BeanUtil.copyProperties(accessPlatformDTO, new AccessPlatform());
        log.info("save AccessPlatform:{}", accessPlatform);
        accessPlatform = (AccessPlatform) this.packAddBaseProps(accessPlatform, request);
        if (accessPlatformDAO.insert(accessPlatform) != 1) {
            log.error("insert error, data:{}", accessPlatform);
            throw new BizException("Insert accessPlatform Error!");
        }
        return  accessPlatform.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccessPlatformList(@NonNull List<AccessPlatform> accessPlatformList, HttpServletRequest request) throws BizException {
        if (accessPlatformList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = accessPlatformDAO.insertList(accessPlatformList);
        if (rows != accessPlatformList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, accessPlatformList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAccessPlatform(@NonNull Long id, @NonNull AccessPlatformDTO accessPlatformDTO, HttpServletRequest request) throws BizException {
        log.info("full update accessPlatformDTO:{}", accessPlatformDTO);
        AccessPlatform accessPlatform = BeanUtil.copyProperties(accessPlatformDTO, new AccessPlatform());
        accessPlatform.setId(id);
        int cnt = accessPlatformDAO.update((AccessPlatform) this.packModifyBaseProps(accessPlatform, request));
        if (cnt != 1) {
            log.error("update error, data:{}", accessPlatformDTO);
            throw new BizException("update accessPlatform Error!");
        }
    }

    @Override
    public void updateAccessPlatformSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        accessPlatformDAO.updatex(params);
    }

    @Override
    public void logicDeleteAccessPlatform(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = accessPlatformDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAccessPlatform(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = accessPlatformDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AccessPlatformDTO findAccessPlatformById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AccessPlatformDTO accessPlatformDTO = accessPlatformDAO.selectOneDTO(params);
        return accessPlatformDTO;
    }

    @Override
    public AccessPlatformDTO findOneAccessPlatform(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AccessPlatform accessPlatform = accessPlatformDAO.selectOne(params);
        AccessPlatformDTO accessPlatformDTO = new AccessPlatformDTO();
        if (null != accessPlatform) {
            BeanUtils.copyProperties(accessPlatform, accessPlatformDTO);
        }
        return accessPlatformDTO;
    }

    @Override
    public List<AccessPlatformDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AccessPlatformDTO> resultList = accessPlatformDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return accessPlatformDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return accessPlatformDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = accessPlatformDAO.groupCount(conditions);
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
        return accessPlatformDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = accessPlatformDAO.groupSum(conditions);
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
    public String apiEnter(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(16);
        params.put("uuid", requestInfo.getString("uuid"));

        AccessPlatformDTO accessPlatformDTO = findOneAccessPlatform(params);

        if (accessPlatformDTO.getId() == null || accessPlatformDTO.getState().intValue() == StaticDataEnum.STATUS_0.getCode()) {
            throw new Exception(I18nUtils.get("access.platform.error", getLang(request)));
        }

        HuffmanCode huffmanCode = new HuffmanCode();

        byte[] bytes = huffmanCode.huffmanBuild(accessPlatformDTO.getId().toString());

        String string = Arrays.toString(bytes);
        String encrypt;
        try {
            encrypt = EncryptUtil.encrypt(string + System.currentTimeMillis());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(e.getMessage());
        }

        Map<String, Object> map = new HashMap<>(6);
        map.put("bytes", string);
        map.put("length", huffmanCode.getLength());
        map.put("code", huffmanCode.getHuffmanCodeMap());

        // 暂定八小时有效期
        redisUtils.hmset(encrypt, map, 60 * 60 * 8);

        return encrypt;
    }

    @Override
    public Long apiVerify(String token, HttpServletRequest request) throws Exception {
        Map<Object, Object> map = redisUtils.hmget(token);

        if (null == map || map.isEmpty()) {
            throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }

        String decode = this.getHuffmanCode(map.get("bytes").toString(), (Map<String, Object>) map.get("code"), Long.parseLong(map.get("length").toString()));

        AccessPlatformDTO accessPlatformDTO = findAccessPlatformById(Long.parseLong(decode));

        if (accessPlatformDTO.getId() == null) {
            throw new Exception(I18nUtils.get("access.platform.error", getLang(request)));
        }

        return accessPlatformDTO.getId();
    }

    private String getHuffmanCode(String string, Map<String, Object> code, long length) {
        JSONArray jsonArray = JSONArray.parseArray(string);
        byte[] bytes = new byte[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            bytes[i] = jsonArray.getByte(i);
        }
        Map<Byte, String> huffmanCodeMap = new HashMap<>(JSONResultHandle.getContainerSize(code.size()));
        Set<Map.Entry<String, Object>> entries = code.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            huffmanCodeMap.put(Byte.parseByte(entry.getKey()), entry.getValue().toString());
        }

        byte[] decode = HuffmanCode.huffmanDecode(bytes, length, huffmanCodeMap);

        return new String(decode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccessPlatformInfo(AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) throws BizException {
        accessPlatformInfoSaveCheck(accessPlatformInfoDTO,request);
        //商户idNo与平台idNo相同
        accessPlatformInfoDTO.setMerchantIdNo(accessPlatformInfoDTO.getPlatformIdNo());
        //查询平台名称是否重复
        Map<String ,Object> params = new HashMap<>();
        params.put("platformIdNo",accessPlatformInfoDTO.getPlatformIdNo());
        //校验平台是否已经存在
        List<AccessPlatformDTO> checkResult = find(params,null,null);
        if(checkResult!=null && checkResult.size()>0){
            throw new BizException(I18nUtils.get("merchant.exist", getLang(request)));
        }
        //生成平台信息
        AccessPlatformDTO accessPlatformDTO = createAccessPlatformDTO(accessPlatformInfoDTO, true, request);
        //记录平台id
        accessPlatformInfoDTO.setPlatformId(this.saveAccessPlatform(accessPlatformDTO,request).toString());
        //生成商户信息
        AccessMerchantDTO accessMerchantDTO = createAccessMerchantDTO(accessPlatformInfoDTO,request);
        //记录商户Id
        accessPlatformInfoDTO.setMerchantId(accessMerchantService.saveAccessMerchant(accessMerchantDTO,request).toString());
        //同步分期付商户信息
        CreditMerchantDTO creditMerchantDTO = createCreditMerchantDTO(accessPlatformInfoDTO);
        serverService.saveMerchant(JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)));


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccessPlatformInfo(AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) throws BizException {
        //字段校验
        accessPlatformInfoUpdateCheck(accessPlatformInfoDTO,request);
        //商户idNo与平台idNo相同
        accessPlatformInfoDTO.setMerchantIdNo(accessPlatformInfoDTO.getPlatformIdNo());
        //查询商户信息
        log.info(accessPlatformInfoDTO.getMerchantId());
        Long merchantId = Long.parseLong(accessPlatformInfoDTO.getMerchantId());
        AccessMerchantDTO checkMerchantDTO = accessMerchantService.findAccessMerchantById(merchantId);
        //商户不存在
        if(checkMerchantDTO == null || checkMerchantDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        //查询平台信息
        AccessPlatformDTO checkPlatformDTO = this.findAccessPlatformById(Long.parseLong(checkMerchantDTO.getPlatformId()));
        //商户不存在
        if(checkPlatformDTO == null || checkPlatformDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        if (!accessPlatformInfoDTO.getPlatformIdNo().equals(checkPlatformDTO.getPlatformIdNo())&&StringUtils.isNotBlank(checkPlatformDTO.getPlatformIdNo())){
            Map<String,Object> params = new HashMap<>();
            params.put("platformIdNo",accessPlatformInfoDTO.getPlatformIdNo());
            //校验平台是否已经存在
            List<AccessPlatformDTO> checkResult = find(params,null,null);
            if(checkResult!=null && checkResult.size()>0){
                throw new BizException(I18nUtils.get("merchant.exist", getLang(request)));
            }
        }
        accessPlatformInfoDTO.setPlatformId(checkPlatformDTO.getId().toString());
        //生成平台信息
        AccessPlatformDTO accessPlatformDTO = createAccessPlatformDTO(accessPlatformInfoDTO,false,request);
        //更新平台信息
        updateAccessPlatform(checkPlatformDTO.getId(),accessPlatformDTO,request);
        //生成商户信息
        AccessMerchantDTO accessMerchantDTO = createAccessMerchantDTO(accessPlatformInfoDTO,request);
        //更新平台信息
        accessMerchantService.updateAccessMerchant(checkMerchantDTO.getId(),accessMerchantDTO,request);
        //同步分期付信息更新
        CreditMerchantDTO creditMerchantDTO = createCreditMerchantDTO(accessPlatformInfoDTO);
        serverService.updateMerchant(creditMerchantDTO.getMerchantId(), JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
    }

    @Override
    public void updateMerchantState(Long id, HttpServletRequest request) throws BizException {
        //查询商户信息
        AccessMerchantDTO checkMerchantDTO = accessMerchantService.findAccessMerchantById(id);
        //商户不存在
        if(checkMerchantDTO == null || checkMerchantDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        //查询平台信息
        AccessPlatformDTO checkPlatformDTO = this.findAccessPlatformById(Long.parseLong(checkMerchantDTO.getPlatformId()));
        //商户不存在
        if(checkPlatformDTO == null || checkPlatformDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        AccessMerchantDTO updateDTO = new AccessMerchantDTO();
        updateDTO.setState((checkMerchantDTO.getState()+1)%2);
        accessMerchantService.updateAccessMerchant(id,updateDTO,request);
    }

    @Override
    public List<AccessPlatform> getAllPlatform() {
        return accessPlatformDAO.getAllPlatform();
    }

    @Override
    public void updateUuid(Long id, HttpServletRequest request) throws BizException {
        //查询商户信息
        AccessPlatformDTO accessPlatformDTO = findAccessPlatformById(id);
        //商户不存在
        if(accessPlatformDTO == null || accessPlatformDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        accessPlatformDTO.setUuid(UUID.randomUUID().toString());
        updateAccessPlatform(id,accessPlatformDTO,request);
    }


    private void accessPlatformInfoUpdateCheck(AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) throws BizException {
        //包括新增中的字段校验
        accessPlatformInfoSaveCheck(accessPlatformInfoDTO,request);
        //商户号不能为空
        if(StringUtils.isBlank(accessPlatformInfoDTO.getMerchantId())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

    }

    private CreditMerchantDTO createCreditMerchantDTO(AccessPlatformInfoDTO accessPlatformInfoDTO) {
        CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
        creditMerchantDTO.setMerchantId(Long.parseLong(accessPlatformInfoDTO.getMerchantId()));
        creditMerchantDTO.setAccessSideId(Long.parseLong(accessPlatformInfoDTO.getPlatformId()));
        creditMerchantDTO.setBsb(accessPlatformInfoDTO.getBsb());
        creditMerchantDTO.setAccountNo(accessPlatformInfoDTO.getAccountNo());
        creditMerchantDTO.setAccountName(accessPlatformInfoDTO.getAccountName());
        creditMerchantDTO.setMerchantName(accessPlatformInfoDTO.getName());
        return creditMerchantDTO;
    }

    private AccessMerchantDTO createAccessMerchantDTO(AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) throws BizException {
        AccessMerchantDTO accessMerchantDTO = new AccessMerchantDTO();
        accessMerchantDTO.setAccountName(accessPlatformInfoDTO.getAccountName());
        accessMerchantDTO.setMerchantIdNo(accessPlatformInfoDTO.getMerchantIdNo());
        accessMerchantDTO.setBsb(accessPlatformInfoDTO.getBsb());
        accessMerchantDTO.setAccountNo(accessPlatformInfoDTO.getAccountNo());
        accessMerchantDTO.setPlatformOwn(StaticDataEnum.STATUS_1.getCode());
        accessMerchantDTO.setPlatformId(accessPlatformInfoDTO.getPlatformId());
        accessMerchantDTO.setName(accessPlatformInfoDTO.getName());
        return  accessMerchantDTO;
    }

    private AccessPlatformDTO createAccessPlatformDTO(AccessPlatformInfoDTO accessPlatformInfoDTO, boolean flag, HttpServletRequest request) throws BizException {
        AccessPlatformDTO accessPlatformDTO = new AccessPlatformDTO();
        accessPlatformDTO.setAccessSideInfo(accessPlatformInfoDTO.getAccessSideInfo());
        accessPlatformDTO.setPlatformIdNo(accessPlatformInfoDTO.getPlatformIdNo());
        accessPlatformDTO.setName(accessPlatformInfoDTO.getName());
        if(flag){
            accessPlatformDTO.setUuid(UUID.randomUUID().toString());
            accessPlatformDTO.setState(StaticDataEnum.STATUS_1.getCode());
        }
        accessPlatformDTO.setDiscountRate(accessPlatformInfoDTO.getDiscountRate()!=null?accessPlatformInfoDTO.getDiscountRate().divide(new BigDecimal("100")):BigDecimal.ZERO);
        accessPlatformDTO.setServerFeeRate(accessPlatformInfoDTO.getServerFeeRate()!=null?accessPlatformInfoDTO.getServerFeeRate().divide(new BigDecimal("100")):BigDecimal.ZERO);

        return accessPlatformDTO;
    }

    private void accessPlatformInfoSaveCheck(AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) throws BizException {
        if(StringUtils.isBlank(accessPlatformInfoDTO.getPlatformIdNo())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(StringUtils.isBlank(accessPlatformInfoDTO.getName())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(StringUtils.isBlank(accessPlatformInfoDTO.getBsb())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (accessPlatformInfoDTO.getBsb().length() > Validator.BSB_NO_LENGTH) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(StringUtils.isBlank(accessPlatformInfoDTO.getAccountName())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (accessPlatformInfoDTO.getAccountName().length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(StringUtils.isBlank(accessPlatformInfoDTO.getAccountNo())){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (accessPlatformInfoDTO.getAccountNo().length() < Validator.BANK_ACCOUNT_NAME_MIN_LENGTH ||
                accessPlatformInfoDTO.getAccountNo().length() > Validator.BANK_ACCOUNT_NAME_MAX_LENGTH) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        //费率字段，值校验
        if(accessPlatformInfoDTO.getDiscountRate()!=null){
            if(accessPlatformInfoDTO.getDiscountRate().compareTo(BigDecimal.ZERO)<0 || accessPlatformInfoDTO.getDiscountRate().compareTo(BigDecimal.ONE)>1){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }
        if(accessPlatformInfoDTO.getServerFeeRate()!=null){
            if(accessPlatformInfoDTO.getServerFeeRate().compareTo(BigDecimal.ZERO)<0 || accessPlatformInfoDTO.getServerFeeRate().compareTo(BigDecimal.ONE)>1){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }

    }

}
