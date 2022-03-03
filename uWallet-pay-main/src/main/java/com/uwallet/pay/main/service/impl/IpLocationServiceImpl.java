package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.dao.IpLocationDAO;
import com.uwallet.pay.main.model.dto.IpLocationDTO;
import com.uwallet.pay.main.model.entity.IpLocation;
import com.uwallet.pay.main.service.IpLocationService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * ip定位
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: ip定位
 * @author: baixinyue
 * @date: Created in 2021-01-12 13:54:55
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class IpLocationServiceImpl extends BaseServiceImpl implements IpLocationService {

    @Autowired
    private IpLocationDAO ipLocationDAO;

    @Override
    public void saveIpLocation(@NonNull IpLocationDTO ipLocationDTO, HttpServletRequest request) throws BizException {
        IpLocation ipLocation = BeanUtil.copyProperties(ipLocationDTO, new IpLocation());
        log.info("save IpLocation:{}", ipLocation);
        if (ipLocationDAO.insert((IpLocation) this.packAddBaseProps(ipLocation, request)) != 1) {
            log.error("insert error, data:{}", ipLocation);
            throw new BizException("Insert ipLocation Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveIpLocationList(@NonNull List<IpLocation> ipLocationList) throws BizException {
        if (ipLocationList.size() == 0) {
            throw new BizException("参数长度不能为0");
        }
        int rows = ipLocationDAO.insertList(ipLocationList);
        if (rows != ipLocationList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, ipLocationList.size());
            throw new BizException("批量保存异常");
        }
    }

    @Override
    public void updateIpLocation(@NonNull Long id, @NonNull IpLocationDTO ipLocationDTO, HttpServletRequest request) throws BizException {
        log.info("full update ipLocationDTO:{}", ipLocationDTO);
        IpLocation ipLocation = BeanUtil.copyProperties(ipLocationDTO, new IpLocation());
        ipLocation.setId(id);
        int cnt = ipLocationDAO.update((IpLocation) this.packModifyBaseProps(ipLocation, request));
        if (cnt != 1) {
            log.error("update error, data:{}", ipLocationDTO);
            throw new BizException("update ipLocation Error!");
        }
    }

    @Override
    public void updateIpLocationSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        ipLocationDAO.updatex(params);
    }

    @Override
    public void logicDeleteIpLocation(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = ipLocationDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("删除失败");
        }
    }

    @Override
    public void deleteIpLocation(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = ipLocationDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("删除失败");
        }
    }

    @Override
    public IpLocationDTO findIpLocationById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        IpLocationDTO ipLocationDTO = ipLocationDAO.selectOneDTO(params);
        return ipLocationDTO;
    }

    @Override
    public IpLocationDTO findOneIpLocation(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        IpLocation ipLocation = ipLocationDAO.selectOne(params);
        IpLocationDTO ipLocationDTO = new IpLocationDTO();
        if (null != ipLocation) {
            BeanUtils.copyProperties(ipLocation, ipLocationDTO);
        }
        return ipLocationDTO;
    }

    @Override
    public List<IpLocationDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<IpLocationDTO> resultList = ipLocationDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return ipLocationDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return ipLocationDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = ipLocationDAO.groupCount(conditions);
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
        return ipLocationDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = ipLocationDAO.groupSum(conditions);
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
