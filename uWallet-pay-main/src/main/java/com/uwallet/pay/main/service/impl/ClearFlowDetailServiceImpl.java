package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ClearBatchDAO;
import com.uwallet.pay.main.dao.ClearFlowDetailDAO;
import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.ClearFlowDetailDTO;
import com.uwallet.pay.main.model.dto.PayBorrowDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.service.ClearFlowDetailService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-13 12:00:23
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ClearFlowDetailServiceImpl extends BaseServiceImpl implements ClearFlowDetailService {

    @Autowired
    private ClearFlowDetailDAO clearFlowDetailDAO;
    @Autowired
    private ClearBatchDAO clearBatchDAO;

    @Override
    public Long saveClearFlowDetail(@NonNull ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) throws BizException {
        ClearFlowDetail clearFlowDetail = BeanUtil.copyProperties(clearFlowDetailDTO, new ClearFlowDetail());
        clearFlowDetail = (ClearFlowDetail) this.packAddBaseProps(clearFlowDetail, request);
        log.info("save ClearFlowDetail:{}", clearFlowDetail);
        if (clearFlowDetailDAO.insert(clearFlowDetail) != 1) {
            log.error("insert error, data:{}", clearFlowDetail);
            throw new BizException("Insert clearFlowDetail Error!");
        }
        return clearFlowDetail.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClearFlowDetailList(@NonNull List<ClearFlowDetail> clearFlowDetailList, HttpServletRequest request) throws BizException {
        if (clearFlowDetailList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = clearFlowDetailDAO.insertList(clearFlowDetailList);
        if (rows != clearFlowDetailList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, clearFlowDetailList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateClearFlowDetail(@NonNull Long id, @NonNull ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) throws BizException {
        log.info("full update clearFlowDetailDTO:{}", clearFlowDetailDTO);
        ClearFlowDetail clearFlowDetail = BeanUtil.copyProperties(clearFlowDetailDTO, new ClearFlowDetail());
        clearFlowDetail.setId(id);
        int cnt = clearFlowDetailDAO.update((ClearFlowDetail) this.packModifyBaseProps(clearFlowDetail, request));
        if (cnt != 1) {
            log.error("update error, data:{}", clearFlowDetailDTO);
            throw new BizException("update clearFlowDetail Error!");
        }
    }

    @Override
    public void updateClearFlowDetailSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        clearFlowDetailDAO.updatex(params);
    }

    @Override
    public void logicDeleteClearFlowDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = clearFlowDetailDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteClearFlowDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = clearFlowDetailDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ClearFlowDetailDTO findClearFlowDetailById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailDAO.selectOneDTO(params);
        return clearFlowDetailDTO;
    }

    @Override
    public ClearFlowDetailDTO findOneClearFlowDetail(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ClearFlowDetail clearFlowDetail = clearFlowDetailDAO.selectOne(params);
        ClearFlowDetailDTO clearFlowDetailDTO = new ClearFlowDetailDTO();
        if (null != clearFlowDetail) {
            BeanUtils.copyProperties(clearFlowDetail, clearFlowDetailDTO);
        }
        return clearFlowDetailDTO;
    }

    @Override
    public List<ClearFlowDetailDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ClearFlowDetailDTO> resultList = clearFlowDetailDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return clearFlowDetailDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return clearFlowDetailDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = clearFlowDetailDAO.groupCount(conditions);
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
        return clearFlowDetailDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = clearFlowDetailDAO.groupSum(conditions);
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
    public List<ClearFlowDetail> getDataByBatchId(Long id) {
        return clearFlowDetailDAO.getDataByBatchId( id);
    }

    @Override
    public BigDecimal findAmountInAmount(Map<String, Object> map) {
        return clearFlowDetailDAO.findAmountInAmount(map);
    }

    @Override
    public int clearData(Map<String, Object> map) {
        return clearFlowDetailDAO.clearData(map);
    }

    @Override
    public List<ClearFlowDetailDTO> findAmountOutAmount(Map<String, Object> map) {
        return clearFlowDetailDAO.findAmountOutAmount(map);
    }

    @Override
    public int updateAmountOut(Map<String, Object> map) {
        return clearFlowDetailDAO.updateAmountOut(map);
    }

    @Override
    public List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        //查询batch表
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", params.get("clearBatchId"));
        ClearBatchDTO clearBatchDTO = clearBatchDAO.selectOneDTO(map);
        List<PayBorrowDTO> list = null;
        if(StaticDataEnum.CLEAR_TYPE_1.getCode() == clearBatchDTO.getClearType()){
            list = clearFlowDetailDAO.selectApiPltClearBatchBorrow(params);
        }else{
            list = clearFlowDetailDAO.selectBatchBorrow(params);
        }

        return list;
    }



    @Override
    public ClearFlowDetailDTO clearTotal(Map<String, Object> map) {
        return clearFlowDetailDAO.clearTotal(map);
    }

    @Override
    public int selectBatchBorrowCount(Map<String, Object> params) {
        int i = clearFlowDetailDAO.selectBatchBorrowCount(params) ;
        return  i;

    }

    @Override
    public int updateClearBatchToFail(Map<String, Object> map) {
        return clearFlowDetailDAO.updateClearBatchToFail(map);
    }

    @Override
    public List<ClearFlowDetail> getApiPltClearDataByBatchId(Long id) {
        return clearFlowDetailDAO.getApiPltClearDataByBatchId(id) ;
    }

    @Override
    public List<ClearFlowDetail> getDataByBatchIdNew(Long id) {
        return clearFlowDetailDAO.getDataByBatchIdNew(id);
    }

    @Override
    public int dealWholeSaleClear(Long id) {
        return clearFlowDetailDAO.dealWholeSaleClear(id);
    }

    @Override
    public List<ClearFlowDetail> getDonationDataByBatchId(Long id) {
        return clearFlowDetailDAO.getDonationDataByBatchId(id);
    }

    /**
     * 根据批次号更新状态
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/8/12 11:10
     */
    @Override
    public int updateStateByBatchId(Map<String, Object> params) {
        return clearFlowDetailDAO.updateStateByBatchId(params);
    }

}
