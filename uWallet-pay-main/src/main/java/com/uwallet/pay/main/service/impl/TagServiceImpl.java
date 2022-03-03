package com.uwallet.pay.main.service.impl;

import autovalue.shaded.com.squareup.javapoet$.$TypeVariableName;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.dao.TagDAO;
import com.uwallet.pay.main.model.dto.TagDTO;
import com.uwallet.pay.main.model.entity.Tag;
import com.uwallet.pay.main.service.TagService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Tag数据
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: Tag数据
 * @author: aaronS
 * @date: Created in 2021-01-07 11:19:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Service
@Slf4j
public class TagServiceImpl extends BaseServiceImpl implements TagService {

    @Autowired
    private TagDAO tagDAO;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void saveTag(@NonNull TagDTO tagDTO, HttpServletRequest request) throws BizException {
        Tag tag = BeanUtil.copyProperties(tagDTO, new Tag());
        Tag packAddBaseProps;
        if (request == null){
            packAddBaseProps = tag;
            packAddBaseProps.setId(SnowflakeUtil.generateId());
            packAddBaseProps.setIp("0.0.0.1");
            packAddBaseProps.setModifiedBy(1L);
            packAddBaseProps.setCreatedBy(1L);
            packAddBaseProps.setModifiedDate(System.currentTimeMillis());
            packAddBaseProps.setCreatedDate(System.currentTimeMillis());
        }else {
            packAddBaseProps = (Tag) this.packAddBaseProps(tag, request);
        }
        log.info("save Tag:{}", tag);
        if (tagDAO.insert(packAddBaseProps) != 1) {
            log.error("insert error, data:{}", tag);
            throw new BizException("Insert tag Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTagList(@NonNull List<Tag> tagList, HttpServletRequest request) throws BizException {
        if (tagList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = tagDAO.insertList(tagList);
        if (rows != tagList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, tagList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTag(@NonNull Long id, @NonNull TagDTO tagDTO, HttpServletRequest request) throws BizException {
        log.info("full update tagDTO:{}", tagDTO);
        Tag tag = BeanUtil.copyProperties(tagDTO, new Tag());
        tag.setId(id);
        int cnt = tagDAO.update((Tag) this.packModifyBaseProps(tag, request));
        if (cnt != 1) {
            log.error("update error, data:{}", tagDTO);
            throw new BizException("update tag Error!");
        }
    }

    @Override
    public void updateTagSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        tagDAO.updatex(params);
    }

    @Override
    public void logicDeleteTag(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = tagDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTag(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = tagDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TagDTO findTagById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TagDTO tagDTO = tagDAO.selectOneDTO(params);
        return tagDTO;
    }

    @Override
    public TagDTO findOneTag(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Tag tag = tagDAO.selectOne(params);
        TagDTO tagDTO = new TagDTO();
        if (null != tag) {
            BeanUtils.copyProperties(tag, tagDTO);
        }
        return tagDTO;
    }

    @Override
    public List<TagDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TagDTO> resultList = tagDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return tagDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return tagDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = tagDAO.groupCount(conditions);
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
        return tagDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = tagDAO.groupSum(conditions);
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
    public Object getTagInfo(HttpServletRequest request) {
        JSONObject result = new JSONObject(4);
        Object top10Tags = this.getTop10Tags();
        List<String> list = new ArrayList<>();
        if (top10Tags instanceof ArrayList<?>) {
            for (Object o : (List<?>) top10Tags) {
                list.add(String.class.cast(o));
            }
        }
        //返回8个tags 原来为10个
        result.put("topTen", list.stream().limit(8).collect(Collectors.toList()));
        result.put("tags",this.getTagAllInfo());
        return result;
    }

    /**
     * 获取全量tag信息,首先从redis中获取,如果没有从库取,更新到redis
     * @return
     */
    public Object getTagAllInfo() {
        if (redisUtils.hasKey(Constant.REDIS_FULL_TAG_DATA_KEY)){
            return redisUtils.get(Constant.REDIS_FULL_TAG_DATA_KEY);
        }else {
            List<String> tags = tagDAO.getTop10Tags(null);
            redisUtils.set(Constant.REDIS_FULL_TAG_DATA_KEY,tags);
            return tags;
        }
    }

    @Override
    public void updateTop10Tags() {
        redisUtils.set(Constant.REDIS_TOP10_TAG_KEY,tagDAO.getTop10Tags("true"));
    }

    @Async("taskExecutor")
    @Override
    public void updateTagPopular(String keyword, HttpServletRequest request) {
        JSONObject param = new JSONObject(6);
        param.put("keyword",keyword);
        param.put("time",System.currentTimeMillis());
        param.put("adminId",1L);
        param.put("ip",getIp(request));
        tagDAO.updateTagPopular(param);
    }

    @Override
    public List<String> matchTags(JSONObject data, HttpServletRequest request) {
        String value = data.getString("value");
        if (StringUtils.isNotBlank(value)){
            return tagDAO.matchTags(value);
        }
        return new ArrayList<>();
    }

    /**
     * 获取top10数据,首先从redis中获取,如果没有从库取,更新到redis
     * @return
     */
    private Object getTop10Tags() {
        if (redisUtils.hasKey(Constant.REDIS_TOP10_TAG_KEY)){
            return redisUtils.get(Constant.REDIS_TOP10_TAG_KEY);
        }else {
            List<String> top10Tags = tagDAO.getTop10Tags("true");
            redisUtils.set(Constant.REDIS_TOP10_TAG_KEY,top10Tags);
            return top10Tags;
        }
    }

}
