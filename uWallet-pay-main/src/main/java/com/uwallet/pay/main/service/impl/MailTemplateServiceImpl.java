package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.MailTemplateDAO;
import com.uwallet.pay.main.model.dto.MailTemplateDTO;
import com.uwallet.pay.main.model.entity.MailTemplate;
import com.uwallet.pay.main.service.MailTemplateService;
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
 * 模板
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:56:55
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class MailTemplateServiceImpl extends BaseServiceImpl implements MailTemplateService {

    @Autowired
    private MailTemplateDAO mailTemplateDAO;

    @Override
    public void saveMailTemplate(@NonNull MailTemplateDTO mailTemplateDTO, HttpServletRequest request) throws BizException {
        MailTemplate mailTemplate = BeanUtil.copyProperties(mailTemplateDTO, new MailTemplate());
        log.info("save MailTemplate:{}", mailTemplate);
        if (mailTemplateDAO.insert((MailTemplate) this.packAddBaseProps(mailTemplate, request)) != 1) {
            log.error("insert error, data:{}", mailTemplate);
            throw new BizException("Insert mailTemplate Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMailTemplateList(@NonNull List<MailTemplate> mailTemplateList, HttpServletRequest request) throws BizException {
        if (mailTemplateList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = mailTemplateDAO.insertList(mailTemplateList);
        if (rows != mailTemplateList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, mailTemplateList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMailTemplate(@NonNull Long id, @NonNull MailTemplateDTO mailTemplateDTO, HttpServletRequest request) throws BizException {
        log.info("full update mailTemplateDTO:{}", mailTemplateDTO);
        MailTemplate mailTemplate = BeanUtil.copyProperties(mailTemplateDTO, new MailTemplate());
        mailTemplate.setId(id);
        int cnt = mailTemplateDAO.update((MailTemplate) this.packModifyBaseProps(mailTemplate, request));
        if (cnt != 1) {
            log.error("update error, data:{}", mailTemplateDTO);
            throw new BizException("update mailTemplate Error!");
        }
    }

    @Override
    public void updateMailTemplateSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        mailTemplateDAO.updatex(params);
    }

    @Override
    public void logicDeleteMailTemplate(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = mailTemplateDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMailTemplate(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = mailTemplateDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MailTemplateDTO findMailTemplateById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MailTemplateDTO mailTemplateDTO = mailTemplateDAO.selectOneDTO(params);
        return mailTemplateDTO;
    }

    @Override
    public MailTemplateDTO findOneMailTemplate(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MailTemplate mailTemplate = mailTemplateDAO.selectOne(params);
        MailTemplateDTO mailTemplateDTO = new MailTemplateDTO();
        if (null != mailTemplate) {
            BeanUtils.copyProperties(mailTemplate, mailTemplateDTO);
        }
        return mailTemplateDTO;
    }

    @Override
    public List<MailTemplateDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MailTemplateDTO> resultList = mailTemplateDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return mailTemplateDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return mailTemplateDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = mailTemplateDAO.groupCount(conditions);
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
        return mailTemplateDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = mailTemplateDAO.groupSum(conditions);
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
    public MailTemplateDTO findMailTemplateBySendNode(String node) {
        return mailTemplateDAO.findMailTemplateBySendNode(node);
    }

}
