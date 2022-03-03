package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ContactPersonDAO;
import com.uwallet.pay.main.model.dto.ContactPersonDTO;
import com.uwallet.pay.main.model.entity.ContactPerson;
import com.uwallet.pay.main.service.ContactPersonService;
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
 * 联系人信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 联系人信息表
 * @author: baixinyue
 * @date: Created in 2020-08-06 11:45:37
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ContactPersonServiceImpl extends BaseServiceImpl implements ContactPersonService {

    @Autowired
    private ContactPersonDAO contactPersonDAO;

    @Override
    public void saveContactPerson(@NonNull ContactPersonDTO contactPersonDTO, HttpServletRequest request) throws BizException {
        ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
        log.info("save ContactPerson:{}", contactPerson);
        if (contactPersonDAO.insert((ContactPerson) this.packAddBaseProps(contactPerson, request)) != 1) {
            log.error("insert error, data:{}", contactPerson);
            throw new BizException("Insert contactPerson Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContactPersonList(@NonNull List<ContactPerson> contactPersonList, HttpServletRequest request) throws BizException {
        if (contactPersonList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = contactPersonDAO.insertList(contactPersonList);
        if (rows != contactPersonList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, contactPersonList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateContactPerson(@NonNull Long id, @NonNull ContactPersonDTO contactPersonDTO, HttpServletRequest request) throws BizException {
        log.info("full update contactPersonDTO:{}", contactPersonDTO);
        ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
        contactPerson.setId(id);
        int cnt = contactPersonDAO.update((ContactPerson) this.packModifyBaseProps(contactPerson, request));
        if (cnt != 1) {
            log.error("update error, data:{}", contactPersonDTO);
            throw new BizException("update contactPerson Error!");
        }
    }

    @Override
    public void updateContactPersonSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        contactPersonDAO.updatex(params);
    }

    @Override
    public void logicDeleteContactPerson(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = contactPersonDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteContactPerson(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = contactPersonDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ContactPersonDTO findContactPersonById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ContactPersonDTO contactPersonDTO = contactPersonDAO.selectOneDTO(params);
        return contactPersonDTO;
    }

    @Override
    public ContactPersonDTO findOneContactPerson(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ContactPerson contactPerson = contactPersonDAO.selectOne(params);
        ContactPersonDTO contactPersonDTO = new ContactPersonDTO();
        if (null != contactPerson) {
            BeanUtils.copyProperties(contactPerson, contactPersonDTO);
        }
        return contactPersonDTO;
    }

    @Override
    public List<ContactPersonDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ContactPersonDTO> resultList = contactPersonDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return contactPersonDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return contactPersonDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = contactPersonDAO.groupCount(conditions);
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
        return contactPersonDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = contactPersonDAO.groupSum(conditions);
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
    public void deleteContactPersonByMerchantId(Long merchantId) {
        log.info("delete contact person by merchantId, merchantId:{}", merchantId);
        int i = 0;
        try {
            i = contactPersonDAO.deleteContactPersonByMerchantId(merchantId);
        } catch (Exception e) {
            log.info("delete contact person by merchantId failed, merchantId:{}, error message:{}, e:{}", merchantId, e.getMessage(), e);
        }
        log.info("delete contact person by merchantId successful, merchantId:{}", merchantId);
    }
}
