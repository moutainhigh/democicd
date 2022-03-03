package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.DonationFlow;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 捐赠流水
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:37:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface DonationFlowService extends BaseService {

   /**
    * 保存一条 DonationFlow 数据
    *
    * @param donationFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveDonationFlow(DonationFlowDTO donationFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 DonationFlow 数据
     *
     * @param donationFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveDonationFlowList(List<DonationFlow> donationFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 DonationFlow 数据
     *
     * @param id 数据唯一id
     * @param donationFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateDonationFlow(Long id, DonationFlowDTO donationFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 donationFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateDonationFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 DonationFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteDonationFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 DonationFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteDonationFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 DonationFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 DonationFlow 数据
     */
    DonationFlowDTO findDonationFlowById(Long id);

    /**
     * 根据条件查询得到第一条 donationFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 donationFlow
     */
    DonationFlowDTO findOneDonationFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<DonationFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
    * 根据用户id查询用户订单
    * @param params
    * @param scs
    * @param pc
    * @return
    */
    List<JSONObject> findOrderByUserId(JSONObject params, Vector<SortingContext> scs, PagingContext pc);

   /**
    * 修改用户捐赠订单状态
    * @param param
    * @param request
    */
   void updateSettlementState(JSONObject param, HttpServletRequest request);

  /**
   * 批量导出明细excl
   * @param param
   * @param request
   * @return
   */
  Workbook exportUserOrder(DonationFlowClearDTO param, HttpServletRequest request) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;


    /**
     * 根据flow Id 更改状态值
     * @author zhangzeyuan
     * @date 2021/7/22 16:43
     * @param flowId
     * @param state
     * @param request
     */
    void updateDonationFlowStateByFlowId(Long flowId, Integer state, HttpServletRequest request);



    /**
     * 捐赠列表查询
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    Map<String,Object> getUserDonationList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 捐赠列表查询个数
     * @param params
     * @return
     */
    int getUserDonationListCount(Map<String, Object> params);

   /**
    * 清算
    * @param donationFlowClearDTO
    * @param request
    */
   void clear(DonationFlowClearDTO donationFlowClearDTO, HttpServletRequest request) throws  Exception;



   /**
    * 捐赠清算记录 结算失败
    * @author zhangzeyuan
    * @date 2021/7/28 11:25
    * @param clearDetailDTO
    * @param clearBatchDTO
    * @param request
    */
   void settleFailed(ClearDetailDTO clearDetailDTO, ClearBatchDTO clearBatchDTO,HttpServletRequest request) throws Exception;

    /**
     * @param params
     * 根据用户id 状态查询订单列表
     * @return
     */
    int countOrderByUserId(Map<String, Object> params);

    /**
     * 获取已结算批次捐赠订单明细
     * @author zhangzeyuan
     * @date 2021/7/27 9:24
     * @param batchId
     * @return java.util.List<com.uwallet.pay.main.model.dto.DonationUserListDTO>
     */
    List<QrPayFlowDTO> getClearBatchList(Long batchId);

}
