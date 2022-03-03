package com.uwallet.pay.main.service;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.ActionDTO;
import com.uwallet.pay.main.model.dto.ActionOnlyDTO;
import com.uwallet.pay.main.model.entity.Action;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.service
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
public interface ActionService extends BaseService {

   /**
    * 保存一条 Action 数据
    *
    * @param actionDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveAction(ActionDTO actionDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Action 数据
     *
     * @param actionList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveActionList(List<Action> actionList) throws BizException;

    /**
     * 修改一条 Action 数据
     *
     * @param id 数据唯一id
     * @param actionDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateAction(Long id, ActionDTO actionDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 action
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateActionSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Action
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteAction(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Action
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteAction(Long id) throws BizException;

    /**
     * 根据id查询一条 Action
     *
     * @param id 数据唯一id
     * @return 查询到的 Action 数据
     */
    ActionDTO findActionById(Long id);

    /**
     * 根据条件查询得到第一条 action
     *
     * @param params 查询条件
     * @return 符合条件的一个 action
     */
    ActionDTO findOneAction(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ActionDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params  查询条件
     * @param columns 需要查询的字段信息
     * @param scs     排序信息
     * @param pc      分页信息
     * @return 查询结果的数据集合
     * @throws BizException 查询异常
     */
    List<Map> findMap(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException;

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int count(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count
     *
     * @param group 分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group 分组的字段。
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 权限树
     *
     * @return
     */
    List<ActionOnlyDTO> actionTree();

}
