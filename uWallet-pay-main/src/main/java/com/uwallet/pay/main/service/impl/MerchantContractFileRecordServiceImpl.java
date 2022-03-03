package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.MerchantContractFileRecordDAO;
import com.uwallet.pay.main.model.dto.MerchantContractFileRecordDTO;
import com.uwallet.pay.main.model.entity.MerchantContractFileRecord;
import com.uwallet.pay.main.service.MerchantContractFileRecordService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 合同记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 合同记录表
 * @author: fenmi
 * @date: Created in 2021-04-29 10:11:38
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: fenmi
 */
@Service
@Slf4j
public class MerchantContractFileRecordServiceImpl extends BaseServiceImpl implements MerchantContractFileRecordService {

    @Autowired
    private MerchantContractFileRecordDAO merchantContractFileRecordDAO;

    @Override
    public void saveMerchantContractFileRecord(@NonNull MerchantContractFileRecordDTO merchantContractFileRecordDTO, HttpServletRequest request) throws BizException {
        String fileOldName = merchantContractFileRecordDTO.getFileOldName();
        if(StringUtils.isNotBlank(fileOldName)){
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(currentTime);

            merchantContractFileRecordDTO.setFileName("Merchant contract-" + fileOldName.substring(0,fileOldName.lastIndexOf(".")) + "-" + dateString + fileOldName.substring(fileOldName.lastIndexOf(".")));
            merchantContractFileRecordDTO.setFileType(fileOldName.substring(fileOldName.lastIndexOf(".")).toLowerCase());
            /* //设置文件名 文件类型
            String[] fileSplitArray = filePath.split("/");
            String fileName = fileSplitArray[1];
            merchantContractFileRecordDTO.setFileName(fileName);

            String[] fileNameSplitArray = fileName.split("\\.");
            merchantContractFileRecordDTO.setFileType(fileNameSplitArray[1]);*/
        }

        MerchantContractFileRecord merchantContractFileRecord = BeanUtil.copyProperties(merchantContractFileRecordDTO, new MerchantContractFileRecord());
        log.info("save MerchantContractFileRecord:{}", merchantContractFileRecord);

        MerchantContractFileRecord saveRecord = (MerchantContractFileRecord) this.packAddBaseProps(merchantContractFileRecord, request);
        //最后操作人操作时间
        saveRecord.setModifiedBy(getUserId(request));
        saveRecord.setModifiedDate(System.currentTimeMillis());
        saveRecord.setIp(getIp(request));

        if (merchantContractFileRecordDAO.insert(saveRecord) != 1) {
            log.error("insert error, data:{}", merchantContractFileRecord);
            throw new BizException("Insert merchantContractFileRecord Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchantContractFileRecordList(@NonNull List<MerchantContractFileRecord> merchantContractFileRecordList, HttpServletRequest request) throws BizException {
        if (merchantContractFileRecordList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = merchantContractFileRecordDAO.insertList(merchantContractFileRecordList);
        if (rows != merchantContractFileRecordList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, merchantContractFileRecordList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMerchantContractFileRecord(@NonNull Long id, @NonNull MerchantContractFileRecordDTO merchantContractFileRecordDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantContractFileRecordDTO:{}", merchantContractFileRecordDTO);
        MerchantContractFileRecord merchantContractFileRecord = BeanUtil.copyProperties(merchantContractFileRecordDTO, new MerchantContractFileRecord());
        merchantContractFileRecord.setId(id);
        int cnt = merchantContractFileRecordDAO.update((MerchantContractFileRecord) this.packModifyBaseProps(merchantContractFileRecord, request));
        if (cnt != 1) {
            log.error("update error, data:{}", merchantContractFileRecordDTO);
            throw new BizException("update merchantContractFileRecord Error!");
        }
    }



    @Override
    public void updateMerchantContractFileRecordSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        merchantContractFileRecordDAO.updatex(params);
    }

    @Override
    public void logicDeleteMerchantContractFileRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = merchantContractFileRecordDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMerchantContractFileRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = merchantContractFileRecordDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MerchantContractFileRecordDTO findMerchantContractFileRecordById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MerchantContractFileRecordDTO merchantContractFileRecordDTO = merchantContractFileRecordDAO.selectOneDTO(params);
        return merchantContractFileRecordDTO;
    }

    @Override
    public MerchantContractFileRecordDTO findOneMerchantContractFileRecord(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MerchantContractFileRecord merchantContractFileRecord = merchantContractFileRecordDAO.selectOne(params);
        MerchantContractFileRecordDTO merchantContractFileRecordDTO = new MerchantContractFileRecordDTO();
        if (null != merchantContractFileRecord) {
            BeanUtils.copyProperties(merchantContractFileRecord, merchantContractFileRecordDTO);
        }
        return merchantContractFileRecordDTO;
    }


    @Override
    public List<MerchantContractFileRecordDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantContractFileRecordDTO> resultList = merchantContractFileRecordDAO.selectDTO(params);
        return resultList;
    }


    @Override
    public List<MerchantContractFileRecordDTO> listContractFile(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantContractFileRecordDTO> resultList = merchantContractFileRecordDAO.listContractFile(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return merchantContractFileRecordDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return merchantContractFileRecordDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = merchantContractFileRecordDAO.groupCount(conditions);
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
        return merchantContractFileRecordDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = merchantContractFileRecordDAO.groupSum(conditions);
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

}
