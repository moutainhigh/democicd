package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.MailLogDAO;
import com.uwallet.pay.main.model.dto.MailLogDTO;
import com.uwallet.pay.main.model.entity.MailLog;
import com.uwallet.pay.main.service.MailLogService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 邮件发送记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 邮件发送记录表
 * @author: zhoutt
 * @date: Created in 2020-01-07 15:46:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class MailLogServiceImpl extends BaseServiceImpl implements MailLogService {

    @Autowired
    private MailLogDAO mailLogDAO;

    @Override
    public void saveMailLog(@NonNull MailLogDTO mailLogDTO, HttpServletRequest request) throws BizException {
        MailLog mailLog = BeanUtil.copyProperties(mailLogDTO, new MailLog());
        log.info("save MailLog:{}", mailLog);
        if (mailLogDAO.insert((MailLog) this.packAddBaseProps(mailLog, request)) != 1) {
            log.error("insert error, data:{}", mailLog);
            throw new BizException("Insert mailLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMailLogList(@NonNull List<MailLog> mailLogList, HttpServletRequest request) throws BizException {
        if (mailLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = mailLogDAO.insertList(mailLogList);
        if (rows != mailLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, mailLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMailLog(@NonNull Long id, @NonNull MailLogDTO mailLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update mailLogDTO:{}", mailLogDTO);
        MailLog mailLog = BeanUtil.copyProperties(mailLogDTO, new MailLog());
        mailLog.setId(id);
        int cnt = mailLogDAO.update((MailLog) this.packModifyBaseProps(mailLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", mailLogDTO);
            throw new BizException("update mailLog Error!");
        }
    }

    @Override
    public void updateMailLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        mailLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteMailLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = mailLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMailLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = mailLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MailLogDTO findMailLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MailLogDTO mailLogDTO = mailLogDAO.selectOneDTO(params);
        return mailLogDTO;
    }

    @Override
    public MailLogDTO findOneMailLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MailLog mailLog = mailLogDAO.selectOne(params);
        MailLogDTO mailLogDTO = new MailLogDTO();
        if (null != mailLog) {
            BeanUtils.copyProperties(mailLog, mailLogDTO);
        }
        return mailLogDTO;
    }

    @Override
    public List<MailLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MailLogDTO> resultList = mailLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return mailLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return mailLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = mailLogDAO.groupCount(conditions);
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
        return mailLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = mailLogDAO.groupSum(conditions);
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
