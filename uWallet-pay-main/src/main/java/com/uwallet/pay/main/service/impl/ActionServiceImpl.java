package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.ActionDAO;
import com.uwallet.pay.main.model.dto.ActionDTO;
import com.uwallet.pay.main.model.dto.ActionOnlyDTO;
import com.uwallet.pay.main.model.entity.Action;
import com.uwallet.pay.main.service.ActionService;
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
 * 权限表
 * </p>
 *
 * @package: com.loancloud.rloan.main.service.impl
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class ActionServiceImpl extends BaseServiceImpl implements ActionService {

    @Autowired
    private ActionDAO actionDAO;

    @Override
    public void saveAction(@NonNull ActionDTO actionDTO, HttpServletRequest request) throws BizException {
        Action action = BeanUtil.copyProperties(actionDTO, new Action());
        log.info("save Action:{}", action);
        if (actionDAO.insert((Action) this.packAddBaseProps(action, request)) != 1) {
            log.error("insert error, data:{}", action);
            throw new BizException("Insert action Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveActionList(@NonNull List<Action> actionList) throws BizException {
        if (actionList.size() == 0) {
            throw new BizException("Length of parameter can not be 0");
        }
        int rows = actionDAO.insertList(actionList);
        if (rows != actionList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, actionList.size());
            throw new BizException("Batch save exception");
        }
    }

    @Override
    public void updateAction(@NonNull Long id, @NonNull ActionDTO actionDTO, HttpServletRequest request) throws BizException {
        log.info("full update actionDTO:{}", actionDTO);
        Action action = BeanUtil.copyProperties(actionDTO, new Action());
        action.setId(id);
        int cnt = actionDAO.update((Action) this.packModifyBaseProps(action, request));
        if (cnt != 1) {
            log.error("update error, data:{}", actionDTO);
            throw new BizException("update action Error!");
        }
    }

    @Override
    public void updateActionSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        actionDAO.updatex(params);
    }

    @Override
    public void logicDeleteAction(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = actionDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public void deleteAction(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = actionDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public ActionDTO findActionById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ActionDTO actionDTO = actionDAO.selectOneDTO(params);
        return actionDTO;
    }

    @Override
    public ActionDTO findOneAction(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Action action = actionDAO.selectOne(params);
        ActionDTO actionDTO = new ActionDTO();
        if (null != action) {
            BeanUtils.copyProperties(action, actionDTO);
        }
        return actionDTO;
    }

    @Override
    public List<ActionDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ActionDTO> resultList = actionDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("Columns cannot be 0 in length");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return actionDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return actionDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = actionDAO.groupCount(conditions);
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
        return actionDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = actionDAO.groupSum(conditions);
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
    public List<ActionOnlyDTO> actionTree() {
        List<ActionOnlyDTO> actionList = actionDAO.actionTree();
        List<ActionOnlyDTO> tree = treeBuild(0L, actionList);
        return tree;
    }

    /**
     * 递归方法
     * @return
     */
    public List<ActionOnlyDTO> treeBuild(Long id, List<ActionOnlyDTO> actionList) {
        List<ActionOnlyDTO> trees = new ArrayList<>();
        for (ActionOnlyDTO actionDTO : actionList) {
            if (id.equals(actionDTO.getParent_id())) {
                actionDTO.setChildren(treeBuild(actionDTO.getMenu_id(), actionList));
                trees.add(actionDTO);
            }
        }
        return trees;
    }

}
