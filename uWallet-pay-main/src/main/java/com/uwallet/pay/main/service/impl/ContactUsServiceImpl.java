package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ContactUsDAO;
import com.uwallet.pay.main.model.dto.ContactUsDTO;
import com.uwallet.pay.main.model.entity.ContactUs;
import com.uwallet.pay.main.service.ContactUsService;
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
 * 联系我们
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 联系我们
 * @author: baixinyue
 * @date: Created in 2020-06-17 08:52:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ContactUsServiceImpl extends BaseServiceImpl implements ContactUsService {

    @Autowired
    private ContactUsDAO contactUsDAO;

    @Override
    public void saveContactUs(@NonNull ContactUsDTO contactUsDTO, HttpServletRequest request) throws BizException {
        ContactUs contactUs = BeanUtil.copyProperties(contactUsDTO, new ContactUs());
        log.info("save ContactUs:{}", contactUs);
        if (contactUsDAO.insert((ContactUs) this.packAddBaseProps(contactUs, request)) != 1) {
            log.error("insert error, data:{}", contactUs);
            throw new BizException("Insert contactUs Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContactUsList(@NonNull List<ContactUs> contactUsList, HttpServletRequest request) throws BizException {
        if (contactUsList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = contactUsDAO.insertList(contactUsList);
        if (rows != contactUsList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, contactUsList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateContactUs(@NonNull Long id, @NonNull ContactUsDTO contactUsDTO, HttpServletRequest request) throws BizException {
        log.info("full update contactUsDTO:{}", contactUsDTO);
        ContactUs contactUs = BeanUtil.copyProperties(contactUsDTO, new ContactUs());
        contactUs.setId(id);
        int cnt = contactUsDAO.update((ContactUs) this.packModifyBaseProps(contactUs, request));
        if (cnt != 1) {
            log.error("update error, data:{}", contactUsDTO);
            throw new BizException("update contactUs Error!");
        }
    }

    @Override
    public void updateContactUsSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        contactUsDAO.updatex(params);
    }

    @Override
    public void logicDeleteContactUs(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = contactUsDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteContactUs(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = contactUsDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ContactUsDTO findContactUsById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ContactUsDTO contactUsDTO = contactUsDAO.selectOneDTO(params);
        return contactUsDTO;
    }

    @Override
    public ContactUsDTO findOneContactUs(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ContactUs contactUs = contactUsDAO.selectOne(params);
        ContactUsDTO contactUsDTO = new ContactUsDTO();
        if (null != contactUs) {
            BeanUtils.copyProperties(contactUs, contactUsDTO);
        }
        return contactUsDTO;
    }

    @Override
    public List<ContactUsDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ContactUsDTO> resultList = contactUsDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return contactUsDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return contactUsDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = contactUsDAO.groupCount(conditions);
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
        return contactUsDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = contactUsDAO.groupSum(conditions);
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
