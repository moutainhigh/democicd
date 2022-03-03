package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.dto.WholeSalesFlowDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.WholeSalesFlow;
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
 * 整体销售流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 整体销售流水表
 * @author: zhoutt
 * @date: Created in 2020-10-17 14:33:57
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
public interface WholeSalesFlowService extends BaseService {

   /**
    * 保存一条 WholeSalesFlow 数据
    *
    * @param wholeSalesFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveWholeSalesFlow(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 WholeSalesFlow 数据
     *
     * @param wholeSalesFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveWholeSalesFlowList(List<WholeSalesFlow> wholeSalesFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 WholeSalesFlow 数据
     *
     * @param id 数据唯一id
     * @param wholeSalesFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateWholeSalesFlow(Long id, WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 wholeSalesFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateWholeSalesFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 WholeSalesFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteWholeSalesFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 WholeSalesFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteWholeSalesFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 WholeSalesFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 WholeSalesFlow 数据
     */
    WholeSalesFlowDTO findWholeSalesFlowById(Long id, HttpServletRequest request) throws Exception;

    /**
     * 根据条件查询得到第一条 wholeSalesFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 wholeSalesFlow
     */
    WholeSalesFlowDTO findOneWholeSalesFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<WholeSalesFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 整体出售意向订单分页计数
     *
     */
    int wholeSalesInterestOrderCount(Map<String, Object> params);

    /**
     *  整体出售意向订单分页
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<WholeSalesFlowDTO> wholeSaleInterestOrderList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 整体出售订单分页
     * @param params
     * @return
     */
    int wholeSaleOrderCount(Map<String, Object> params);

    /**
     * 整体出售订单分页
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<WholeSalesFlowDTO> wholeSaleOrderList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 意向订单审核
     * @param wholeSalesFlowDTO
     * @param request
     */
    void interestOrderAudit(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * 正式订单审核
     * @param wholeSalesFlowDTO
     * @param request
     * @throws Exception
     */
    void wholeSaleOrderAudit(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * 整体出售入账失败处理
     * @throws Exception
     */
    void wholeSaleAmountInFailedHandle() throws Exception;

    /**
     * 整体出售入账可疑处理
     * @throws Exception
     */
    void wholeSaleAmountInDoubfulHandle() throws Exception;

    /**
     * 费率修改
     * @param wholeSalesFlowDTO
     * @param request
     * @throws Exception
     */
    void rateModify(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * 查询商户整体出售订单
     * @param merchantId
     * @param request
     * @return
     */
    JSONObject merchantOrderDetails(Long merchantId, HttpServletRequest request) throws Exception;

    /**
     * 整体出售申请
     * @param wholeSalesFlowDTO
     * @param request
     * @throws Exception
     */
    Long wholeSaleApply(WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * app整体出售订单
     * @param params
     * @return
     */
    int appWholeSaleOrderCount(Map<String, Object> params);

    /**
     * app整体出售订单
     * @param params
     * @return
     */
    List<JSONObject> appWholeSaleOrder(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 查询最新整体出售订单
     * @param params
     * @return
     */
    WholeSalesFlowDTO findLatestWholeSaleFlowDTO(Map<String, Object> params);

    /**
     * 查询意向订单拒绝原因
     * @param id
     * @return
     */
    JSONObject findInterestRejectInfo(Long id, HttpServletRequest request) throws Exception;

    /**
     * 查询应算列表详情
     * @param id
     * @return
     */
    List<WholeSalesFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    Integer clearedDetailTransFlowCount(Long id);

    /**
     * 查询清算数据的条数
     * @param params
     * @return
     */
    int countMerchantClearList(Map<String, Object> params);

    /**
     * 插入清算流水批号
     * @param params
     * @param clearBatchDTO
     * @param request
     */
    void addClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request);

    /**
     * 查询清算流水信息
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDataByBatchId(Long id);

    /**
     * 处理整体出售清算结果
     * @param id
     */
    int dealWholeSaleClear(Long id);

    /**
     * 查询商户整体出售订单
     * @param params
     * @return
     */
    List<WholeSalesFlowDTO> merchantClearMessageList(Map<String, Object> params);

   /**
    * 更新批次流水为失败
    * @param params
    */
   int updateClearBatchToFail(Map<String, Object> params);

 /**
  * 查询整体出售结算条数
  * @param params
  * @return
  */
 int getMerchantClearMessageCount(Map<String, Object> params);


}
