package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.dao.NoticeDAO;
import com.uwallet.pay.main.model.dto.NoticeDTO;
import com.uwallet.pay.main.model.entity.Notice;
import com.uwallet.pay.main.service.NoticeService;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-11 16:54:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class NoticeServiceImpl extends BaseServiceImpl implements NoticeService {

    @Autowired
    private NoticeDAO noticeDAO;

    @Override
    public void saveNotice(@NonNull NoticeDTO noticeDTO, HttpServletRequest request) throws BizException {
        Notice notice = BeanUtil.copyProperties(noticeDTO, new Notice());
        if (request != null) {
            notice = (Notice) this.packAddBaseProps(notice, request);
        } else {
            long now = System.currentTimeMillis();
            notice.setId(SnowflakeUtil.generateId());
            notice.setCreatedDate(now);
            notice.setModifiedDate(now);
        }
        log.info("save Notice:{}", notice);
        if (noticeDAO.insert(notice) != 1) {
            log.error("insert error, data:{}", notice);
            throw new BizException("Insert notice Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNoticeList(@NonNull List<Notice> noticeList, HttpServletRequest request) throws BizException {
        if (noticeList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = noticeDAO.insertList(noticeList);
        if (rows != noticeList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, noticeList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateNotice(@NonNull Long id, @NonNull NoticeDTO noticeDTO, HttpServletRequest request) throws BizException {
        log.info("full update noticeDTO:{}", noticeDTO);
        Notice notice = BeanUtil.copyProperties(noticeDTO, new Notice());
        notice.setId(id);
        int cnt = noticeDAO.update((Notice) this.packModifyBaseProps(notice, request));
        if (cnt != 1) {
            log.error("update error, data:{}", noticeDTO);
            throw new BizException("update notice Error!");
        }
    }

    @Override
    public void updateNoticeSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        noticeDAO.updatex(params);
    }

    @Override
    public void logicDeleteNotice(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = noticeDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteNotice(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = noticeDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public NoticeDTO findNoticeById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        NoticeDTO noticeDTO = noticeDAO.selectOneDTO(params);
        return noticeDTO;
    }

    @Override
    public NoticeDTO findOneNotice(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Notice notice = noticeDAO.selectOne(params);
        NoticeDTO noticeDTO = new NoticeDTO();
        if (null != notice) {
            BeanUtils.copyProperties(notice, noticeDTO);
        }
        return noticeDTO;
    }

    @Override
    public List<NoticeDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<NoticeDTO> resultList = noticeDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return noticeDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return noticeDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = noticeDAO.groupCount(conditions);
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
        return noticeDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = noticeDAO.groupSum(conditions);
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
    public boolean allNoticeHasRead(Long userId) {
        boolean result = false;
        Map<String, Object> params = new HashMap<>(16);
        params.put("userId", userId);
        List<NoticeDTO> noticeDTOList = find(params, null, null);
        result = noticeDTOList.stream().anyMatch(noticeDTO -> noticeDTO.getIsRead() == 0);
        return result;
    }

    @Override
    public void allNoticeRead(Long userId, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>(16);
        params.put("userId", userId);
        List<NoticeDTO> noticeDTOList = find(params, null, null);
        noticeDTOList.stream().forEach(noticeDTO -> {
            noticeDTO.setIsRead(1);
            try {
                updateNotice(noticeDTO.getId(), noticeDTO, request);
            } catch (BizException e) {
                log.info("notice all has read failed, data:{}, error message:{}. e:{}", noticeDTO, e.getMessage(), e);
            }
        });
    }

    @Override
    public JSONObject getAllNoticeHasRead(Long userId) {

        JSONObject result = new JSONObject();
        Map<String, Object> params = new HashMap<>(16);
        params.put("userId", userId);
        List<NoticeDTO> noticeDTOList = find(params, null, null);
        int notReadNum = 0;
        boolean isNotRead = false;
        if(noticeDTOList != null || noticeDTOList.size() > 0){
            List<NoticeDTO> isNotReadList = noticeDTOList.stream().filter(dto->dto.getIsRead() == 0) .collect(Collectors.toList());
            notReadNum = isNotReadList == null ? 0 : isNotReadList.size();
            if(notReadNum > 0){
                isNotRead = true;
            }
        }

        result.put("notReadNum",notReadNum);
        result.put("isNeedRead",isNotRead);
//        result = noticeDTOList.stream().anyMatch(noticeDTO -> noticeDTO.getIsRead() == 0);
        return result;
    }

    @Override
    public void noticeClearAll(Long userId, HttpServletRequest request) {
        noticeDAO.noticeClearAll(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNoticeListNew(List<Notice> noticeList) throws BizException {
        if (noticeList.size() == 0) {
            throw new BizException("站内信长度错误");
        }
        int rows = noticeDAO.insertList(noticeList);
        if (rows != noticeList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, noticeList.size());
            throw new BizException("数据库实际插入成功数与给定的不一致");
        }
    }

}
