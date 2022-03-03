package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.KycSubmitLogDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.KycSubmitLog;
import com.uwallet.pay.main.service.KycSubmitLogService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-08 13:24:29
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class KycSubmitLogServiceImpl extends BaseServiceImpl implements KycSubmitLogService {

    @Autowired
    private KycSubmitLogDAO kycSubmitLogDAO;

    @Resource
    @Lazy
    private UserService userService;

    @Autowired
    private StaticDataService staticDataService;


    @Override
    public void saveKycSubmitLog(@NonNull KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) throws BizException {
        KycSubmitLog kycSubmitLog = BeanUtil.copyProperties(kycSubmitLogDTO, new KycSubmitLog());
        log.info("save KycSubmitLog:{}", kycSubmitLog);
        if (kycSubmitLogDAO.insert((KycSubmitLog) this.packAddBaseProps(kycSubmitLog, request)) != 1) {
            log.error("insert error, data:{}", kycSubmitLog);
            throw new BizException("Insert kycSubmitLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveKycSubmitLogList(@NonNull List<KycSubmitLog> kycSubmitLogList, HttpServletRequest request) throws BizException {
        if (kycSubmitLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = kycSubmitLogDAO.insertList(kycSubmitLogList);
        if (rows != kycSubmitLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, kycSubmitLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateKycSubmitLog(@NonNull Long id, @NonNull KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update kycSubmitLogDTO:{}", kycSubmitLogDTO);
        KycSubmitLog kycSubmitLog = BeanUtil.copyProperties(kycSubmitLogDTO, new KycSubmitLog());
        kycSubmitLog.setId(id);
        int cnt = kycSubmitLogDAO.updateNewTow((KycSubmitLog) this.packModifyBaseProps(kycSubmitLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", kycSubmitLogDTO);
            throw new BizException("update kycSubmitLog Error!");
        }
    }

    @Override
    public void updateKycSubmitLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        kycSubmitLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteKycSubmitLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = kycSubmitLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteKycSubmitLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = kycSubmitLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public KycSubmitLogDTO findKycSubmitLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        KycSubmitLogDTO kycSubmitLogDTO = kycSubmitLogDAO.selectOneDTO(params);
        return kycSubmitLogDTO;
    }

    @Override
    public KycSubmitLogDTO findOneKycSubmitLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        KycSubmitLog kycSubmitLog = kycSubmitLogDAO.selectOne(params);
        KycSubmitLogDTO kycSubmitLogDTO = new KycSubmitLogDTO();
        if (null != kycSubmitLog) {
            BeanUtils.copyProperties(kycSubmitLog, kycSubmitLogDTO);
        }
        return kycSubmitLogDTO;
    }

    @Override
    public List<KycSubmitLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<KycSubmitLogDTO> resultList = kycSubmitLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return kycSubmitLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return kycSubmitLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = kycSubmitLogDAO.groupCount(conditions);
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
        return kycSubmitLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = kycSubmitLogDAO.groupSum(conditions);
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void insertKycSubmitLog(@NonNull KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) throws BizException {
        KycSubmitLog kycSubmitLog = BeanUtil.copyProperties(kycSubmitLogDTO, new KycSubmitLog());
        log.info("save KycSubmitLog:{}", kycSubmitLog);
        long now = System.currentTimeMillis();
        Long currentLoginId = getUserId(request);
        if (kycSubmitLogDTO.getUserId()==null){
            kycSubmitLog.setUserId(currentLoginId);
        }
        kycSubmitLog.setCreatedBy(currentLoginId);
        kycSubmitLog.setCreatedDate(now);
        kycSubmitLog.setModifiedBy(currentLoginId);
        kycSubmitLog.setModifiedDate(now);
        kycSubmitLog.setIp(getIp(request));
        kycSubmitLog.setStatus(1);
        if (kycSubmitLogDAO.insertNew( kycSubmitLog) != 1) {
            log.error("insert error, data:{}", kycSubmitLog);
            throw new BizException("Insert kycSubmitLog Error!");
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateKycSubmitLogTwo(@NonNull Long id, @NonNull KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update kycSubmitLogDTO:{}", kycSubmitLogDTO);
        KycSubmitLog kycSubmitLog = BeanUtil.copyProperties(kycSubmitLogDTO, new KycSubmitLog());
        kycSubmitLog.setId(id);
        int cnt = kycSubmitLogDAO.update((KycSubmitLog) this.packModifyBaseProps(kycSubmitLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", kycSubmitLogDTO);
            throw new BizException("update kycSubmitLog Error!");
        }
    }

    @Override
    public List<JSONObject> findLogList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws BizException {
        List<JSONObject> results=new ArrayList<>();
        params = getUnionParams(params, scs, pc);
        List<KycSubmitLogDTO> resultList = kycSubmitLogDAO.selectDTO(params);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        x:for (KycSubmitLogDTO kycSubmitLogDTO : resultList) {
            Long userId = kycSubmitLogDTO.getUserId();
            JSONObject param=new JSONObject();
            param.put("id",userId);
            UserDTO userById = userService.findOneUser(param);
            if (userById==null||userById.getId()==null){
                log.error("查询用户信息不存在 用户id:{}",userById);
                continue ;
//                throw new BizException("user is not exist");
            }
            JSONObject result=new JSONObject();
            result.put("name",userById.getUserFirstName()+userById.getUserLastName());
            result.put("customerAccount",kycSubmitLogDTO.getPhone());
            result.put("accountSubmittedTimes",kycSubmitLogDTO.getAccountSubmittedTimes());
            result.put("referralCode",kycSubmitLogDTO.getId().toString());
            Integer kycStatus = kycSubmitLogDTO.getKycStatus();
            if (kycStatus!=null){
                if (kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_1.getCode())==0){
                    result.put("kycStatus",StaticDataEnum.KYC_CHECK_STATE_1.getMessage());
                }else if (kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_2.getCode())==0){
                    result.put("kycStatus",StaticDataEnum.KYC_CHECK_STATE_2.getMessage());
                }else if (kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_3.getCode())==0){
                    result.put("kycStatus",StaticDataEnum.KYC_CHECK_STATE_3.getMessage());
                }else if (kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_0.getCode())==0){
                    result.put("kycStatus",StaticDataEnum.KYC_CHECK_STATE_0.getMessage());
                }
                result.put("kycState",kycStatus);
            }
            Long date = kycSubmitLogDTO.getDate();
            if (date!=null){
                result.put("date",simpleDateFormat.format(new Date(date)));
            }
            results.add(result);
        }
        return results;
    }
    @Override
    public void updateKycSubmitLogNew(JSONObject param, HttpServletRequest request) throws BizException {
        log.info("full update param:{}", param);
        int cnt = kycSubmitLogDAO.updateNew(param);
        if (cnt != 1) {
            log.error("update error, data:{}", param);
            throw new BizException("update kycSubmitLog Error!");
        }
    }

    @Override
    public List<KycSubmitLogDTO> notInFind(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<KycSubmitLogDTO> resultList = kycSubmitLogDAO.selectNoInDTO(params);
        return resultList;
    }

    @Override
    public KycSubmitLogDTO findLatelyLog(JSONObject findParam) {
        return kycSubmitLogDAO.findLatelyLog(findParam);
    }

}
