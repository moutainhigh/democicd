package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ShareholderDAO;
import com.uwallet.pay.main.model.dto.ShareholderDTO;
import com.uwallet.pay.main.model.entity.Shareholder;
import com.uwallet.pay.main.service.ShareholderService;
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
import java.util.*;

/**
 * <p>
 * 股东信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 股东信息表
 * @author: baixinyue
 * @date: Created in 2020-04-21 17:22:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ShareholderServiceImpl extends BaseServiceImpl implements ShareholderService {

    @Autowired
    private ShareholderDAO shareholderDAO;

    @Override
    public void saveShareholder(@NonNull ShareholderDTO shareholderDTO, HttpServletRequest request) throws BizException {
        Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
        log.info("save Shareholder:{}", shareholder);
        if (shareholderDAO.insert((Shareholder) this.packAddBaseProps(shareholder, request)) != 1) {
            log.error("insert error, data:{}", shareholder);
            throw new BizException("Insert shareholder Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShareholderList(@NonNull List<Shareholder> shareholderList, HttpServletRequest request) throws BizException {
        if (shareholderList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = shareholderDAO.insertList(shareholderList);
        if (rows != shareholderList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, shareholderList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateShareholder(@NonNull Long id, @NonNull ShareholderDTO shareholderDTO, HttpServletRequest request) throws BizException {
        log.info("full update shareholderDTO:{}", shareholderDTO);
        Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
        shareholder.setId(id);
        int cnt = shareholderDAO.update((Shareholder) this.packModifyBaseProps(shareholder, request));
        if (cnt != 1) {
            log.error("update error, data:{}", shareholderDTO);
            throw new BizException("update shareholder Error!");
        }
    }

    @Override
    public void updateShareholderSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        shareholderDAO.updatex(params);
    }

    @Override
    public void logicDeleteShareholder(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = shareholderDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteShareholder(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = shareholderDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ShareholderDTO findShareholderById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ShareholderDTO shareholderDTO = shareholderDAO.selectOneDTO(params);
        return shareholderDTO;
    }

    @Override
    public ShareholderDTO findOneShareholder(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Shareholder shareholder = shareholderDAO.selectOne(params);
        ShareholderDTO shareholderDTO = new ShareholderDTO();
        if (null != shareholder) {
            BeanUtils.copyProperties(shareholder, shareholderDTO);
        }
        return shareholderDTO;
    }

    @Override
    public List<ShareholderDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ShareholderDTO> resultList = shareholderDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return shareholderDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return shareholderDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = shareholderDAO.groupCount(conditions);
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
        return shareholderDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = shareholderDAO.groupSum(conditions);
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
    public void deleteShareholderByMerchantId(Long merchantId) {
        log.info("delete shareholder by merchantId, merchantId:{}", merchantId);
        int i = 0;
        try {
            i = shareholderDAO.deleteShareholderByMerchantId(merchantId);
        } catch (Exception e) {
            log.info("delete shareholder by merchantId failed, merchantId:{}, error message:{}, e:{}", merchantId, e.getMessage(), e);
        }
        log.info("delete shareholder by merchantId successful, merchantId:{}", merchantId);
    }
}
