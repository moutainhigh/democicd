package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.RefundOrder;
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
 * 退款订单
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 退款订单
 * @author: zhoutt
 * @date: Created in 2021-08-18 09:01:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
public interface RefundOrderService extends BaseService {

   /**
    * 保存一条 RefundOrder 数据
    *
    * @param refundOrderDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveRefundOrder(RefundOrderDTO refundOrderDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 RefundOrder 数据
     *
     * @param refundOrderList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveRefundOrderList(List<RefundOrder> refundOrderList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 RefundOrder 数据
     *
     * @param id 数据唯一id
     * @param refundOrderDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateRefundOrder(Long id, RefundOrderDTO refundOrderDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 refundOrder
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateRefundOrderSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 RefundOrder
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteRefundOrder(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 RefundOrder
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteRefundOrder(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 RefundOrder
     *
     * @param id 数据唯一id
     * @return 查询到的 RefundOrder 数据
     */
    RefundOrderDTO findRefundOrderById(Long id);

    /**
     * 根据条件查询得到第一条 refundOrder
     *
     * @param params 查询条件
     * @return 符合条件的一个 refundOrder
     */
    RefundOrderDTO findOneRefundOrder(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<RefundOrderDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * H5商户退款
     * @param requestInfo
     * @param apiQrPayFlowDTO
     * @param qrPayFlowDTO
     * @param request
     * @return
     * @throws Exception
     */
    RefundOrderDTO createH5RefundOrderDTO(H5RefundsRequestDTO requestInfo, ApiQrPayFlowDTO apiQrPayFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * 查询分期付可疑订单
     * @return
     *
     */
    List<RefundOrderDTO> findCreditRefundDoubt();

    /**
     * 查询时间点之前的退款条数
     * @param params1
     * @return
     */
    int getH5MerchantRefundUnclearedCount(Map<String, Object> params1);

    /**
     * 退款流水更新批次号
     * @param params
     * @param request
     */
    void addClearBatchId(Map<String, Object> params, HttpServletRequest request);

    /**
     * 修改清算状态
     * @param map
     */
    void clearData(Map<String, Object> map);

    /**
     * 查询清算数据
     * @param params
     * @return
     */
    List<RefundOrderDTO> merchantClearMessageList(Map<String, Object> params);

    /**
     * 清算回退
     * @param batchId
     * @param merchantId
     * @param request
     * @return
     */
    int rollbackSettlement(Long batchId, Long merchantId, HttpServletRequest request);

    /**
     * 查询H5商户退款订单列表
     * @param params
     * @return
     */
    int getH5RefundsListCount(Map<String, Object> params);

    /**
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<RefundOrderListDTO> getH5RefundsList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

}
