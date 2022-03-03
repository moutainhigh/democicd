package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.WithholdFlow;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 代收三方流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 代收三方流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:50:03
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
public interface WithholdFlowService extends BaseService {

   /**
    * 保存一条 WithholdFlow 数据
    *
    * @param withholdFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveWithholdFlow(WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 WithholdFlow 数据
     *
     * @param withholdFlowList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveWithholdFlowList(List<WithholdFlow> withholdFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 WithholdFlow 数据
     *
     * @param id 数据唯一id
     * @param withholdFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateWithholdFlow(Long id, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 withholdFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateWithholdFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 WithholdFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteWithholdFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 WithholdFlow
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteWithholdFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 WithholdFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 WithholdFlow 数据
     */
    WithholdFlowDTO findWithholdFlowById(Long id);

    /**
     * 根据条件查询得到第一条 withholdFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 withholdFlow
     */
    WithholdFlowDTO findOneWithholdFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<WithholdFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<WithholdFlowDTO> withholdFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countWithholdFlowList(Map<String, Object> params);

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
     * 查询流水记录
     * @param flowIds
     * @return
     */
    List<Map> selectResults(Long[] flowIds, HttpServletRequest request) throws BizException;

    /**
     * 保存omipay返回订单号
     * @param requestData
     * @param request
     * @throws BizException
     */
    void saveOmiPayOrderNo(JSONObject requestData, HttpServletRequest request) throws BizException;

    void updateWithholdFlowForConcurrency(Long id, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

}
