package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.LoginMissDAO;
import com.uwallet.pay.main.model.dto.LoginMissDTO;
import com.uwallet.pay.main.model.entity.LoginMiss;
import com.uwallet.pay.main.service.LoginMissService;
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
 * 登陆错误次数记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 登陆错误次数记录表
 * @author: baixinyue
 * @date: Created in 2020-01-02 13:56:13
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class LoginMissServiceImpl extends BaseServiceImpl implements LoginMissService {

    @Autowired
    private LoginMissDAO loginMissDAO;

    @Override
    public Long saveLoginMiss(@NonNull LoginMissDTO loginMissDTO, HttpServletRequest request) throws BizException {
        LoginMiss loginMiss = BeanUtil.copyProperties(loginMissDTO, new LoginMiss());
        log.info("save LoginMiss:{}", loginMiss);
        loginMiss = (LoginMiss) this.packAddBaseProps(loginMiss, request);
        if (loginMissDAO.insert(loginMiss) != 1) {
            log.error("insert error, data:{}", loginMiss);
            throw new BizException("Insert loginMiss Error!");
        }
        return loginMiss.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLoginMissList(@NonNull List<LoginMiss> loginMissList, HttpServletRequest request) throws BizException {
        if (loginMissList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = loginMissDAO.insertList(loginMissList);
        if (rows != loginMissList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, loginMissList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateLoginMiss(@NonNull Long id, @NonNull LoginMissDTO loginMissDTO, HttpServletRequest request) throws BizException {
        log.info("full update loginMissDTO:{}", loginMissDTO);
        LoginMiss loginMiss = BeanUtil.copyProperties(loginMissDTO, new LoginMiss());
        loginMiss.setId(id);
        int cnt = loginMissDAO.update((LoginMiss) this.packModifyBaseProps(loginMiss, request));
        if (cnt != 1) {
            log.error("update error, data:{}", loginMissDTO);
            throw new BizException("update loginMiss Error!");
        }
    }

    @Override
    public void updateLoginMissSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        loginMissDAO.updatex(params);
    }

    @Override
    public void logicDeleteLoginMiss(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = loginMissDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteLoginMiss(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = loginMissDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public LoginMissDTO findLoginMissById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        LoginMissDTO loginMissDTO = loginMissDAO.selectOneDTO(params);
        return loginMissDTO;
    }

    @Override
    public LoginMissDTO findOneLoginMiss(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        LoginMiss loginMiss = loginMissDAO.selectOne(params);
        LoginMissDTO loginMissDTO = new LoginMissDTO();
        if (null != loginMiss) {
            BeanUtils.copyProperties(loginMiss, loginMissDTO);
        }
        return loginMissDTO;
    }

    @Override
    public List<LoginMissDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<LoginMissDTO> resultList = loginMissDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return loginMissDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return loginMissDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = loginMissDAO.groupCount(conditions);
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
        return loginMissDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = loginMissDAO.groupSum(conditions);
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
    public void loginMissRecord(LoginMissDTO loginMissDTO, HttpServletRequest request) throws BizException {
        LoginMiss loginMiss = BeanUtil.copyProperties(loginMissDTO, new LoginMiss());
        loginMiss = (LoginMiss) this.packModifyBaseProps(loginMiss, request);
        if (loginMissDAO.loginMissRecord(loginMiss) != 1) {
            log.error("update error, data:{}", loginMissDTO);
            throw new BizException("update loginMiss Error!");
        }
    }

    @Override
    public void updateLoginMissStatus(Long id, LoginMissDTO oneLoginMiss, HttpServletRequest request) throws BizException {
        log.info("full update loginMissDTO:{}", oneLoginMiss);
        LoginMiss loginMiss = BeanUtil.copyProperties(oneLoginMiss, new LoginMiss());
        loginMiss.setId(id);
        int cnt = loginMissDAO.updateLoginMissStatus((LoginMiss) this.packModifyBaseProps(loginMiss, request));
        if (cnt != 1) {
            log.error("update error, data:{}", oneLoginMiss);
            throw new BizException("update loginMiss Error!");
        }
    }
}
