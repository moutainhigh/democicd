package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.ClearFlowDetailDTO;
import com.uwallet.pay.main.model.dto.PayBorrowDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-13 12:00:23
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
public interface ClearFlowDetailService extends BaseService {

   /**
    * 保存一条 ClearFlowDetail 数据
    *
    * @param clearFlowDetailDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveClearFlowDetail(ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 ClearFlowDetail 数据
     *
     * @param clearFlowDetailList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveClearFlowDetailList(List<ClearFlowDetail> clearFlowDetailList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 ClearFlowDetail 数据
     *
     * @param id 数据唯一id
     * @param clearFlowDetailDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateClearFlowDetail(Long id, ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 clearFlowDetail
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateClearFlowDetailSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 ClearFlowDetail
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteClearFlowDetail(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 ClearFlowDetail
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteClearFlowDetail(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 ClearFlowDetail
     *
     * @param id 数据唯一id
     * @return 查询到的 ClearFlowDetail 数据
     */
    ClearFlowDetailDTO findClearFlowDetailById(Long id);

    /**
     * 根据条件查询得到第一条 clearFlowDetail
     *
     * @param params 查询条件
     * @return 符合条件的一个 clearFlowDetail
     */
    ClearFlowDetailDTO findOneClearFlowDetail(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ClearFlowDetailDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
    * 查询批次信息
    * @param id
    * @return
    */
   List<ClearFlowDetail> getDataByBatchId(Long id);

   /**
    * 查询正常清算金额
    * @param map
    * @return
    */
   BigDecimal findAmountInAmount(Map<String, Object> map);


   /**
    * 更新清算信息
    * @param map
    * @return
    */
   int clearData(Map<String, Object> map);
 /**
  * 查询回退清算金额
  * @param map
  * @return
  */
    List<ClearFlowDetailDTO> findAmountOutAmount(Map<String, Object> map);

   /**
    * 更新回退清算流水
    * @param map
    * @return
    */
   int updateAmountOut(Map<String, Object> map);

   /**
    * 查询清算记录
    * @param params
    * @param scs
    * @param pc
    * @return
    */
    List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


   /**
    * 清算
    * @param clearMap
    * @return
    */
   ClearFlowDetailDTO clearTotal(Map<String, Object> clearMap);

   /**
    * 查询清算条数
    * @param params
    * @return
    */
   int selectBatchBorrowCount(Map<String, Object> params);

   /**
    * 更新清算批次为失败
    * @param map
    * @return
    */
   int updateClearBatchToFail(Map<String, Object> map);

    /**
     * 查询三方平台服务费清算明细
     * @param id
     * @return
     */
    List<ClearFlowDetail> getApiPltClearDataByBatchId(Long id);

    /**
     * 查询清算明细
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDataByBatchIdNew(Long id);

    /**
     * 处理整体出售清算结果
     * @param id
     */
    int dealWholeSaleClear(Long id);

    /**
     * 查询捐赠清算明细
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDonationDataByBatchId(Long id);


    /**
     * 根据批次号更新状态
     * @author zhangzeyuan
     * @date 2021/8/12 11:10
     * @param params
     * @return int
     */
    int updateStateByBatchId(Map<String, Object> params);
}
