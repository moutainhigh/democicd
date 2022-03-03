package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:49:55
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
public interface ClearBatchService extends BaseService {

    /**
     * 保存一条 ClearBatch 数据
     *
     * @param clearBatchDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    Long saveClearBatch(ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 ClearBatch 数据
     *
     * @param clearBatchList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveClearBatchList(List<ClearBatch> clearBatchList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 ClearBatch 数据
     *
     * @param id            数据唯一id
     * @param clearBatchDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateClearBatch(Long id, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 clearBatch
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateClearBatchSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 ClearBatch
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteClearBatch(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 ClearBatch
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteClearBatch(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 ClearBatch
     *
     * @param id 数据唯一id
     * @return 查询到的 ClearBatch 数据
     */
    ClearBatchDTO findClearBatchById(Long id);

    /**
     * 根据条件查询得到第一条 clearBatch
     *
     * @param params 查询条件
     * @return 符合条件的一个 clearBatch
     */
    ClearBatchDTO findOneClearBatch(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ClearBatchDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 清算执行组件
     *
     * @param id
     * @param request
     * @param type 0 :老清算，按通道清算，处理退款 1：新清算，无退款处理
     * @param response
     * @throws Exception
     * @return
     */
    ClearBatchDTO clear(Long id, HttpServletRequest request, int type, HttpServletResponse response) throws Exception;

    /**
     * 出账回滚组件
     *
     * @param id
     * @return
     * @throws Exception
     */
    int amountOutRollback(Long id,HttpServletRequest request) throws Exception;

    /**
     * 批次清算
     *
     * @param params
     * @param request
     * @throws BizException
     */
    void clearBatchAction(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws BizException;

    /**
     * 记录清算明细流水
     * @param list
     * @param clearBatchDTO
     * @param request
     * @throws BizException
     */
    void addClearFlowDetail(List<ClearFlowDetail> list, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 清算回滚组件
     * @param accountFlowDTO
     * @param clearDetailDTO
     * @param request
     * @throws Exception
     */
    int doClearRollback(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws Exception;

    /**
     * 失败处理
     * @param accountFlowDTO
     * @param clearDetailDTO
     * @param request
     * @throws BizException
     */
    void doClearFailResult(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws  BizException;

    /**
     * 生成清算账单文件
     * @param billList
     * @param fileName
     * @param filePath
     * @param response
     */
    void createClearBillFile(List<ClearBillCSVDTO> billList, String fileName, String filePath);

    void clearDoubtHandle();

    void clearRollbackDoubtHandle();

    void clearRollbackFailHandle();

    /**
     * 三方清算
     * @param params
     * @param request
     * @param response
     * @throws BizException
     */
    void apiPlatformClearAction(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws  BizException;

    /**
     * 三方清算
     * @param id
     * @param request
     * @param response
     */
    void apiPlatformClear(Long id, HttpServletRequest request, HttpServletResponse response) throws BizException;

    /**
     * 分期付清算处理
     * @param list
     * @param clearBatchDTO
     * @param request
     */

    void doCreditClear(List<OneMerchantClearDataDTO> list, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 查询清算列表条数
     * @param params
     * @param merchantDTO
     * @return
     * @throws BizException
     */
    int getClearCount(Map<String, Object> params, MerchantDTO merchantDTO) throws  BizException;

    /**
     * 查询清算列表
     * @param params
     * @param merchantDTO
     * @param scs
     * @param pc
     * @return
     */
    List<Map<String, Object>> getClearList(Map<String, Object> params, MerchantDTO merchantDTO, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 查询清算明细
     * @param merchantId
     * @param clearId
     * @param scs
     * @param request
     * @return
     * @throws BizException
     */
    Map<String, Object> getClearFlowDetailById(Long merchantId, Long clearId, Vector<SortingContext> scs, HttpServletRequest request) throws  BizException;

    /**
     * 清算失败处理
     * @param id
     * @param request
     * @throws Exception
     */
    void clearFail(Long id, HttpServletRequest request) throws  Exception;

    /**
     * 清算并导出文件
     * @param data
     * @param request
     * @param response
     * @throws Exception
     * @return
     */
    ClearBatchDTO settleFileExport(Map<String, Object> data, HttpServletRequest request, HttpServletResponse response) throws  Exception;

    /**
     * 生成空的清算文件
     * @return
     */
    ClearBatchDTO createNullClearBillFile();

    /**
     * H5商户正常交易结算
     * @param params
     * @param request
     * @param response
     * @return
     */
    ClearBatchDTO H5MerchantSettleFileExport(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response)  throws  Exception;

    void h5MerchantSettleCheck(Map<String, Object> params, HttpServletRequest request) throws  Exception;
}
