package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;
import com.uwallet.pay.main.model.dto.ClearDetailDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.entity.ClearDetail;
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
 * 清算批次明细表
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 清算批次明细表
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:58:28
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
public interface ClearDetailService extends BaseService {

    /**
     * 保存一条 ClearDetail 数据
     *
     * @param clearDetailDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveClearDetail(ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 ClearDetail 数据
     *
     * @param clearDetailList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveClearDetailList(List<ClearDetail> clearDetailList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 ClearDetail 数据
     *
     * @param id             数据唯一id
     * @param clearDetailDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateClearDetail(Long id, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 clearDetail
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateClearDetailSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 ClearDetail
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteClearDetail(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 ClearDetail
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteClearDetail(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 ClearDetail
     *
     * @param id 数据唯一id
     * @return 查询到的 ClearDetail 数据
     */
    ClearDetailDTO findClearDetailById(Long id);

    /**
     * 根据条件查询得到第一条 clearDetail
     *
     * @param params 查询条件
     * @return 符合条件的一个 clearDetail
     */
    ClearDetailDTO findOneClearDetail(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ClearDetailDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * @param group      分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group      分组的字段。
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 查询清算明细信息
     *
     * @param updateMap
     * @return
     */
    List<ClearDetailDTO> getClearBatch(Map<String, Object> updateMap);

    /**
     * 清算可疑查证
     */
    void clearDoubtHandle();

    /**
     * 清算回滚失败处理
     */
    void clearRollbackDoubtHandle();

    /**
     * 清算回滚可疑处理
     */
    void clearRollbackFailHandle();

    List<ClearBillCSVDTO> getClearBillList(Long id);

    List<ClearDetailDTO> getClearABAList(Long id);

    List<ClearDetailDTO> getClearBatchNew(Map<String, Object> updateMap);

    ClearDetailDTO clearTotal(Map<String, Object> clearMap);

    List<ClearDetailDTO> getClearDoubt(int code);

    void createApiPlatformClearData(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    void clearData(Map<String, Object> clearMap);

    List<ClearBillCSVDTO> getApiPltClearBillList(Long id);

    List<Map<String, Object>> clearFlowListGroupByDate(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 已结算列表查询个数
     * @param params
     * @return
     */
    int getClearedDetailListCount(Map<String, Object> params);

    /**
     * 已结算列表信息
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<ClearDetailDTO> getClearedDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 已结算列表查询明细
     * @param id
     * @return
     */
//    Object clearedDetailTransFlowList(Long id ,HttpServletRequest request) throws BizException;

    /**
     * 整体出售结算结果处理
     * @param id
     */
    void dealWholeSaleClear(Long id);

    /**
     * 捐赠清算数据查询
     * @param params
     * @return
     */
    List<ClearDetail> getDonationClearBatch(Map<String, Object> params);


    /**
     * 根据批次号更新状态
     * @author zhangzeyuan
     * @date 2021/8/12 11:10
     * @param params
     * @return int
     */
    int updateStateByBatchId(Map<String, Object> params);

    /**
     * 得到清算数据
     * @param params
     * @return
     */
    List<ClearDetailDTO> getH5MerchantClearDetail(Map<String, Object> params);

    /**
     * 生成H5商户清算文件
     * @param id
     * @return
     */
    List<ClearBillCSVDTO> getH5ClearBillList(Long id);

    List<QrPayFlowDTO> h5ClearCount(Long id);
}
