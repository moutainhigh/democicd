package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.TipFlow;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 小费流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 小费流水表
 * @author: zhangzeyuan
 * @date: Created in 2021-08-10 16:01:03
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface TipFlowService extends BaseService {

   /**
    * 保存一条 TipFlow 数据
    *
    * @param tipFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveTipFlow(TipFlowDTO tipFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 TipFlow 数据
     *
     * @param tipFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveTipFlowList(List<TipFlow> tipFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 TipFlow 数据
     *
     * @param id 数据唯一id
     * @param tipFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateTipFlow(Long id, TipFlowDTO tipFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 tipFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateTipFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 TipFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteTipFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 TipFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteTipFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 TipFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 TipFlow 数据
     */
    TipFlowDTO findTipFlowById(Long id);

    /**
     * 根据条件查询得到第一条 tipFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 tipFlow
     */
    TipFlowDTO findOneTipFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<TipFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 根据flowId修改状态
     * @author zhangzeyuan
     * @date 2021/8/10 17:22
     * @param flowId
     * @param state
     * @param request
     * @return java.lang.Integer
     */
    Integer updateStateByFlowId(Long flowId, Integer state, HttpServletRequest request);


    /**
     * 小费结算
     * @author zhangzeyuan
     * @date 2021/8/11 9:36
     * @param merchantIds
     * @param request
     */
    ClearBatchDTO tipSettlement(String merchantIds, HttpServletRequest request) throws BizException;


    /**
     * 查询小费结算状态下的商户条数
     * @param params
     * @return
     */
    Integer countTipMerchant(Map<String, Object> params);

   /**
    * 查询小费结算状态下的商户数据
    * @param params
    * @return
    */
    List<TipMerchantsDTO> findTipMerchantData(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

   /**
    * 查询订单总条数
    * @param param
    * @return
    */
    int countOrderByMerchantId(JSONObject param);

   /**
    * 根据条件查询状态下的订单
    * @param param
    * @param scs
    * @param pc
    * @return
    */
   List<JSONObject> findOrderByUserId(JSONObject param, Vector<SortingContext> scs, PagingContext pc);

   /**
    * 转换某个订单状态
    * @param data
    * @param request
    */
   void updateSettlementState(JSONObject data, HttpServletRequest request);

   /**
    * 根据条件导出excl
    * @param param
    * @param request
    * @return
    */
   Workbook exportUserOrder(TipFlowClearDTO param, HttpServletRequest request) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;


   /**
    * 小费结算失败
    * @author zhangzeyuan
    * @date 2021/8/12 16:27
    * @param clearDetailDTO
    * @param clearBatchDTO
    * @param request
    */
    void settleFailed(ClearDetailDTO clearDetailDTO, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws Exception;



    /*
     * 获取清算记录详情
     * @author zhangzeyuan
     * @date 2021/8/13 10:36
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.QrPayFlowDTO>
     */
    List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 获取清算记录数量
     * @author zhangzeyuan
     * @date 2021/8/13 10:36
     * @param id
     * @return java.lang.Integer
     */
    Integer clearedDetailTransFlowCount(Long id);

}
