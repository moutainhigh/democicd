package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.ContentDAO;
import com.uwallet.pay.main.model.dto.ContentDTO;
import com.uwallet.pay.main.model.entity.Content;
import com.uwallet.pay.main.service.ContentService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 内容管理
 * @author: Strong
 * @date: Created in 2020-01-14 11:10:06
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class ContentServiceImpl extends BaseServiceImpl implements ContentService {

    @Autowired
    private ContentDAO contentDAO;

    @Value("${spring.adsContentUrl}")
    private String adsContentUrl;

    @Autowired
    private ContentService contentService;

    @Override
    public void saveContent(@NonNull ContentDTO contentDTO, HttpServletRequest request) throws BizException {
        Content content = BeanUtil.copyProperties(contentDTO, new Content());
        log.info("save Content:{}", content);
        if (contentDAO.insert((Content) this.packAddBaseProps(content, request)) != 1) {
            log.error("insert error, data:{}", content);
            throw new BizException("Insert content Error!");
        }
        contentDTO.setPath(adsContentUrl+content.getId());
        contentService.updateContent(content.getId(), contentDTO, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContentList(@NonNull List<Content> contentList, HttpServletRequest request) throws BizException {
        if (contentList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = contentDAO.insertList(contentList);
        if (rows != contentList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, contentList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateContent(@NonNull Long id, @NonNull ContentDTO contentDTO, HttpServletRequest request) throws BizException {
        log.info("full update contentDTO:{}", contentDTO);
        Content content = BeanUtil.copyProperties(contentDTO, new Content());
        content.setId(id);
        int cnt = contentDAO.update((Content) this.packModifyBaseProps(content, request));
        if (cnt != 1) {
            log.error("update error, data:{}", contentDTO);
            throw new BizException("update content Error!");
        }
    }

    @Override
    public void updateContentSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        contentDAO.updatex(params);
    }

    @Override
    public void logicDeleteContent(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = contentDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteContent(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = contentDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ContentDTO findContentById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ContentDTO contentDTO = contentDAO.selectOneDTO(params);
        return contentDTO;
    }

    @Override
    public ContentDTO findOneContent(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Content content = contentDAO.selectOne(params);
        ContentDTO contentDTO = new ContentDTO();
        if (null != content) {
            BeanUtils.copyProperties(content, contentDTO);
        }
        return contentDTO;
    }

    @Override
    public List<ContentDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ContentDTO> resultList = contentDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return contentDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return contentDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = contentDAO.groupCount(conditions);
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
        return contentDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = contentDAO.groupSum(conditions);
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
