package com.uwallet.pay.main.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.uwallet.pay.core.util.EasyPoiUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ClearReconciliationDAO;
import com.uwallet.pay.main.model.dto.ClearReconciliationDTO;
import com.uwallet.pay.main.model.entity.ClearReconciliation;
import com.uwallet.pay.main.model.excel.Clear;
import com.uwallet.pay.main.model.excel.Transaction;
import com.uwallet.pay.main.service.ClearReconciliationService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 清算对账表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 清算对账表
 * @author: baixinyue
 * @date: Created in 2020-03-06 09:00:14
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ClearReconciliationServiceImpl extends BaseServiceImpl implements ClearReconciliationService {

    @Autowired
    private ClearReconciliationDAO clearReconciliationDAO;

    @Override
    public void saveClearReconciliation(@NonNull ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) throws BizException {
        ClearReconciliation clearReconciliation = BeanUtil.copyProperties(clearReconciliationDTO, new ClearReconciliation());
        log.info("save ClearReconciliation:{}", clearReconciliation);
        if (clearReconciliationDAO.insert((ClearReconciliation) this.packAddBaseProps(clearReconciliation, request)) != 1) {
            log.error("insert error, data:{}", clearReconciliation);
            throw new BizException("Insert clearReconciliation Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClearReconciliationList(@NonNull List<ClearReconciliation> clearReconciliationList, HttpServletRequest request) throws BizException {
        if (clearReconciliationList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = clearReconciliationDAO.insertList(clearReconciliationList);
        if (rows != clearReconciliationList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, clearReconciliationList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateClearReconciliation(@NonNull Long id, @NonNull ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) throws BizException {
        log.info("full update clearReconciliationDTO:{}", clearReconciliationDTO);
        ClearReconciliation clearReconciliation = BeanUtil.copyProperties(clearReconciliationDTO, new ClearReconciliation());
        clearReconciliation.setId(id);
        int cnt = clearReconciliationDAO.update((ClearReconciliation) this.packModifyBaseProps(clearReconciliation, request));
        if (cnt != 1) {
            log.error("update error, data:{}", clearReconciliationDTO);
            throw new BizException("update clearReconciliation Error!");
        }
    }

    @Override
    public void updateClearReconciliationSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        clearReconciliationDAO.updatex(params);
    }

    @Override
    public void logicDeleteClearReconciliation(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = clearReconciliationDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteClearReconciliation(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = clearReconciliationDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ClearReconciliationDTO findClearReconciliationById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ClearReconciliationDTO clearReconciliationDTO = clearReconciliationDAO.selectOneDTO(params);
        return clearReconciliationDTO;
    }

    @Override
    public ClearReconciliationDTO findOneClearReconciliation(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ClearReconciliation clearReconciliation = clearReconciliationDAO.selectOne(params);
        ClearReconciliationDTO clearReconciliationDTO = new ClearReconciliationDTO();
        if (null != clearReconciliation) {
            BeanUtils.copyProperties(clearReconciliation, clearReconciliationDTO);
        }
        return clearReconciliationDTO;
    }

    @Override
    public List<ClearReconciliationDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ClearReconciliationDTO> resultList = clearReconciliationDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return clearReconciliationDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return clearReconciliationDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = clearReconciliationDAO.groupCount(conditions);
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
        return clearReconciliationDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = clearReconciliationDAO.groupSum(conditions);
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
    public void importClearFile(Integer type, MultipartFile file, HttpServletRequest request) throws Exception {
        if (type == StaticDataEnum.GATEWAY_TYPE_1.getCode() || type ==  StaticDataEnum.GATEWAY_TYPE_2.getCode()) {
            ImportParams params = new ImportParams();
            StringBuilder sb=new StringBuilder();
            // 验证数据
            params.setNeedVerfiy(true);
            List<Clear> clearList = null;
            ExcelImportResult<Clear> result = ExcelImportUtil.importExcelMore(file.getInputStream(), Clear.class, params);
            // 校验是否合格
            if(result.isVerfiyFail()){
                // 不合格的数据
                List<Clear> errorList = result.getList();
                // 拼凑错误信息,自定义
                for (int i = 0; i < errorList.size(); i++) {
                    EasyPoiUtils.getWrongInfo(sb, errorList, i, errorList.get(i), "name", "清算信息不合法");
                }
            }
            clearList = result.getList();
            saveClearRecord(type, clearList, request);
        } else {

        }
    }

    /**
     * 记录清算文件
     * @param clearList
     * @param request
     * @throws BizException
     */
    public void saveClearRecord(Integer gatewayId,List<Clear> clearList, HttpServletRequest request) throws BizException {
        List<ClearReconciliation> clearReconciliationList = new ArrayList<>(1);
        clearList.stream().forEach(clear -> {
            ClearReconciliation clearReconciliation = new ClearReconciliation();
            clearReconciliation.setFinanceDate(clear.getFinanceDate().getTime());
            clearReconciliation.setClearingAmount(clear.getClearingAmount());
            clearReconciliation.setClearingNumber(clear.getClearingNumber());
            clearReconciliation.setPayAmount(clear.getPaymentGrossAmount());
            clearReconciliation.setPayCount(clear.getPayCount());
            clearReconciliation.setRefundAmount(clear.getRefundAmount());
            clearReconciliation.setRefundCount(clear.getRefundCount());
            clearReconciliation.setGatewayId(gatewayId);
            clearReconciliation = (ClearReconciliation) packAddBaseProps(clearReconciliation, request);
            clearReconciliationList.add(clearReconciliation);
        });
        saveClearReconciliationList(clearReconciliationList, request);
    }

    /**
     * 清算对账
     * @param clearReconciliations
     * @param request
     * @throws BizException
     */
    @Async("taskExecutor")
    public void clearCheck(List<ClearReconciliation> clearReconciliations, HttpServletRequest request) throws BizException {

    }

}
