package com.uwallet.pay.main.service;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author: liming
 * @Date: 2020/10/19 16:02
 * @Description: 分期付Web服务
 */

public interface CreditWebService {

    /**
     * 统计查询
     * @param params 条件
     * @return 结果
     */
    int searchMerchantListCount(Map<String, Object> params);

    /**
     * 查询商户数据
     * @param params 条件
     * @param scs 排序
     * @param pc 分页
     * @return 结果集
     */
    List<DiscountPackageInfoDTO> searchMerchantList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 置结算失败
     * @param flowId 交易流水号
     * @param request 请求
     * @throws BizException 操作失败异常
     */
    void failedClearDetailInfo(Long flowId, HttpServletRequest request) throws BizException;

    /**
     * 整体结算流水明细
     * @param params 查询条件
     * @param scs 排序
     * @param pc 分页
     * @return 结果
     */
    List<MerchantWholeSalesFlowInfoDTO> searchMerchantWholeSalesFlowInfo(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 导出结算
     *
     * @param ids     商户id
     * @param request 请求
     * @return 结果集
     * @throws BizException 操作失败异常
     */
    ClearBatchDTO export(List<Long> ids, HttpServletRequest request) throws BizException;

    /**
     * 统计查询整体出售流水明细
     * @param params 查询条件
     * @return 结果
     */
    int searchMerchantWholeSalesFlowInfoCount(Map<String, Object> params);

    /**
     * 清算记录统计查询
     * @param params 查询条件
     * @return 结果
     */
    int searchSettlementInfoCount(Map<String, Object> params);

    /**
     * 清算记录
     * @param params 查询条件
     * @param scs 排序
     * @param pc 分页
     * @return 结果集
     */
    List<SettlementInfoDTO> searchSettlementInfoList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据 ids 批量修改延时结算状态
     * @param delay 延时结算状态: 0.不延时 1.延时
     * @param ids 待修改数据
     * @throws BizException 操作失败异常
     */
    void updateSettlementDelay(Integer delay, List<String> ids) throws BizException;
    /**
     * 导出结算(新)
     *
     * @param ids     商户id
     * @param request 请求
     * @return 结果集
     * @throws BizException 操作失败异常
     */
    ClearBatchDTO exportNew(Map<String, Object> ids, HttpServletRequest request)  throws Exception;

    /**
     * 查询整体出售清算列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<Map<String, Object>> merchantClearMessageList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 待结算详情查询
     * @param params
     * @return
     */
    List<WholeSalesFlowDTO> wholeSaleListDetails(Map<String, Object> params);
}
