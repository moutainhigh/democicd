package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.dao.AppAboutUsDAO;
import com.uwallet.pay.main.model.dto.AppAboutUsDTO;
import com.uwallet.pay.main.model.entity.AppAboutUs;
import com.uwallet.pay.main.service.AppAboutUsService;
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
 * app 关于我们
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: app 关于我们
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class AppAboutUsServiceImpl extends BaseServiceImpl implements AppAboutUsService {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    private AppAboutUsDAO appAboutUsDAO;

    @Override
    public void saveAppAboutUs(@NonNull AppAboutUsDTO appAboutUsDTO, HttpServletRequest request) throws BizException {
        AppAboutUs appAboutUs = BeanUtil.copyProperties(appAboutUsDTO, new AppAboutUs());
        log.info("save AppAboutUs:{}", appAboutUs);
        if (appAboutUsDAO.insert((AppAboutUs) this.packAddBaseProps(appAboutUs, request)) != 1) {
            log.error("insert error, data:{}", appAboutUs);
            throw new BizException("Insert appAboutUs Error!");
        }
        redisUtils.set("aboutUs", appAboutUsDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppAboutUsList(@NonNull List<AppAboutUs> appAboutUsList, HttpServletRequest request) throws BizException {
        if (appAboutUsList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appAboutUsDAO.insertList(appAboutUsList);
        if (rows != appAboutUsList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appAboutUsList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppAboutUs(@NonNull Long id, @NonNull AppAboutUsDTO appAboutUsDTO, HttpServletRequest request) throws BizException {
        log.info("full update appAboutUsDTO:{}", appAboutUsDTO);
        AppAboutUs appAboutUs = BeanUtil.copyProperties(appAboutUsDTO, new AppAboutUs());
        appAboutUs.setId(id);
        int cnt = appAboutUsDAO.update((AppAboutUs) this.packModifyBaseProps(appAboutUs, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appAboutUsDTO);
            throw new BizException("update appAboutUs Error!");
        }
        redisUtils.del(new String[]{"aboutUs"});
    }

    @Override
    public void updateAppAboutUsSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appAboutUsDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppAboutUs(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appAboutUsDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppAboutUs(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appAboutUsDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppAboutUsDTO findAppAboutUsById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppAboutUsDTO appAboutUsDTO = appAboutUsDAO.selectOneDTO(params);
        return appAboutUsDTO;
    }

    @Override
    public AppAboutUsDTO findOneAppAboutUs(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppAboutUs appAboutUs = appAboutUsDAO.selectOne(params);
        AppAboutUsDTO appAboutUsDTO = new AppAboutUsDTO();
        if (null != appAboutUs) {
            BeanUtils.copyProperties(appAboutUs, appAboutUsDTO);
        }
        return appAboutUsDTO;
    }

    @Override
    public List<AppAboutUsDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        List<AppAboutUsDTO> resultList = appAboutUsDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appAboutUsDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appAboutUsDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appAboutUsDAO.groupCount(conditions);
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
        return appAboutUsDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appAboutUsDAO.groupSum(conditions);
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
