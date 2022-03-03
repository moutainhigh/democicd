package com.uwallet.pay.main.service;


import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.RefundDetailListDTO;
import com.uwallet.pay.main.model.dto.RefundFlowDTO;
import com.uwallet.pay.main.model.dto.RefundListDTO;
import com.uwallet.pay.main.model.entity.RefundFlow;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 退款流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.service
 * @description: 退款流水表
 * @author: baixinyue
 * @date: Created in 2020-02-07 15:56:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
public interface RefundFlowService extends BaseService {

   /**
    * 保存一条 RefundFlow 数据
    *
    * @param refundFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveRefundFlow(RefundFlowDTO refundFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 RefundFlow 数据
     *
     * @param refundFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveRefundFlowList(List<RefundFlow> refundFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 RefundFlow 数据
     *
     * @param id 数据唯一id
     * @param refundFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateRefundFlow(Long id, RefundFlowDTO refundFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 refundFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateRefundFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 RefundFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteRefundFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 RefundFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteRefundFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 RefundFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 RefundFlow 数据
     */
    RefundFlowDTO findRefundFlowById(Long id);

    /**
     * 根据条件查询得到第一条 refundFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 refundFlow
     */
    RefundFlowDTO findOneRefundFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<RefundFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 查询正在退款和退款可疑的退款交易
     * @return
     */
    List<RefundFlowDTO> selectFlowDTO();

    int addQrPayClearBatchId(Map<String, Object> updateMap);

    int countClearList(Map<String, Object> params);

    int clearData(Map<String, Object> map);

    int updateAmountOut(Map<String, Object> map);

    /**
     *
     * @param flowId
     * @return
     */
    List<RefundFlowDTO> selectReason(Long flowId);

    int updateRefundFlowToCheckFail(Long id,  long updateTime);

    RefundFlowDTO getUnCleared(Long userId,int gatewayId);

    List<RefundFlowDTO> findAllUnClearedRefundFlow(Map<String, Object> map);

    /**
     * 查询退款列表页
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<RefundListDTO> selectRefund(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 退款列表页条数
     * @param params
     * @return
     */
    int selectRefundCount(Map<String, Object> params);

    /**
     * 退款明细列表页
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<RefundDetailListDTO> selectRefundDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 退款明细列表页总条数
     * @param params
     * @return
     */
    int selectRefundDetailCount(Map<String, Object> params);

    /**
     * 补款CSV文件
     */
    String exportMakeUpFile(HttpServletRequest request) throws Exception;

 /**
  * 修改清算批次状态,适用于退款失败补充清算部分
  * @param map
  * @return
  */
 int updateClearBatch(Map<String, Object> map);

 /**
  * 退款交易查询
  * @param refundNo
  * @param request
  * @return
  */

 JSONObject creditThirdRefundCheck(String refundNo, HttpServletRequest request);
}
