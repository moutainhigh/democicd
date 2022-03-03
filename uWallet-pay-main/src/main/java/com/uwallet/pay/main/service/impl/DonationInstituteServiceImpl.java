package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.DonationInstituteDAO;
import com.uwallet.pay.main.model.dto.DonationInstituteDTO;
import com.uwallet.pay.main.model.dto.DonationInstituteDataDTO;
import com.uwallet.pay.main.model.entity.DonationInstitute;
import com.uwallet.pay.main.service.DonationInstituteService;
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
 * 捐赠机构
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 捐赠机构
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:12
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class DonationInstituteServiceImpl extends BaseServiceImpl implements DonationInstituteService {

    @Autowired
    private DonationInstituteDAO donationInstituteDAO;

    @Override
    public void saveDonationInstitute(@NonNull DonationInstituteDTO donationInstituteDTO, HttpServletRequest request) throws BizException {
        DonationInstitute donationInstitute = BeanUtil.copyProperties(donationInstituteDTO, new DonationInstitute());
        log.info("save DonationInstitute:{}", donationInstitute);
        if (donationInstituteDAO.insert((DonationInstitute) this.packAddBaseProps(donationInstitute, request)) != 1) {
            log.error("insert error, data:{}", donationInstitute);
            throw new BizException("Insert donationInstitute Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDonationInstituteList(@NonNull List<DonationInstitute> donationInstituteList, HttpServletRequest request) throws BizException {
        if (donationInstituteList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = donationInstituteDAO.insertList(donationInstituteList);
        if (rows != donationInstituteList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, donationInstituteList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateDonationInstitute(@NonNull Long id, @NonNull DonationInstituteDTO donationInstituteDTO, HttpServletRequest request) throws BizException {
        log.info("full update donationInstituteDTO:{}", donationInstituteDTO);
        DonationInstitute donationInstitute = BeanUtil.copyProperties(donationInstituteDTO, new DonationInstitute());
        donationInstitute.setId(id);
        int cnt = donationInstituteDAO.update((DonationInstitute) this.packModifyBaseProps(donationInstitute, request));
        if (cnt != 1) {
            log.error("update error, data:{}", donationInstituteDTO);
            throw new BizException("update donationInstitute Error!");
        }
    }

    @Override
    public void updateDonationInstituteSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        donationInstituteDAO.updatex(params);
    }

    @Override
    public void logicDeleteDonationInstitute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = donationInstituteDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteDonationInstitute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = donationInstituteDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public DonationInstituteDTO findDonationInstituteById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        DonationInstituteDTO donationInstituteDTO = donationInstituteDAO.selectOneDTO(params);
        return donationInstituteDTO;
    }

    @Override
    public DonationInstituteDTO findOneDonationInstitute(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        DonationInstitute donationInstitute = donationInstituteDAO.selectOne(params);
        DonationInstituteDTO donationInstituteDTO = new DonationInstituteDTO();
        if (null != donationInstitute) {
            BeanUtils.copyProperties(donationInstitute, donationInstituteDTO);
        }
        return donationInstituteDTO;
    }

    @Override
    public List<DonationInstituteDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<DonationInstituteDTO> resultList = donationInstituteDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return donationInstituteDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return donationInstituteDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = donationInstituteDAO.groupCount(conditions);
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
        return donationInstituteDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = donationInstituteDAO.groupSum(conditions);
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

    /**
     * 根据id查询一条 DonationInstitute
     *
     * @param id 数据唯一id
     * @return 查询到的 DonationInstitute 数据
     */
    @Override
    public DonationInstituteDataDTO getDonationDataById(String id) {
        return donationInstituteDAO.getDonationDataById(id);
    }

}
