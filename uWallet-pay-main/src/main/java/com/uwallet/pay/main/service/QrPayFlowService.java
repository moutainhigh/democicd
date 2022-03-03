package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.QrPayFlow;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 扫码支付交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 扫码支付交易流水表
 * @author: zhoutt
 * @date: Created in 2019-12-13 18:00:26
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
public interface QrPayFlowService extends BaseService {

    /**
     * 保存一条 QrPayFlow 数据
     *
     * @param qrPayFlowDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    Long saveQrPayFlow(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 QrPayFlow 数据
     *
     * @param qrPayFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveQrPayFlowList(List<QrPayFlow> qrPayFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 QrPayFlow 数据
     *
     * @param id           数据唯一id
     * @param qrPayFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateQrPayFlow(Long id, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 qrPayFlow
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateQrPayFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 QrPayFlow
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteQrPayFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 QrPayFlow
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteQrPayFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 QrPayFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 QrPayFlow 数据
     */
    QrPayFlowDTO findQrPayFlowById(Long id);

    /**
     * 根据条件查询得到第一条 qrPayFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 qrPayFlow
     */
    QrPayFlowDTO findOneQrPayFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表
     *
     * @param
     * @return 查询到的 QrPayFlow 数据
     */
    List<QrPayFlowDTO> qrPayFlowListDetails(Map<String, Object> map);

    /**
     * 根据查询条件得到数据列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    JSONObject appQrPayFlowListRecAmountTotal(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到数据列表
     *
     * @param id 数据唯一id
     * @return 查询到的 QrPayFlow 数据
     */
    QrPayFlowDTO appQrPayFlowUnRecAmountTotal(Long id);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<QrPayFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 商户资金清算，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<QrPayFlowDTO> qrPayFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
    int countQrPayFlowList(Map<String, Object> params);

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
     * 支付订单列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<PayBorrowDTO> selectPayBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 支付订单列表分页
     *
     * @param params
     * @return
     */
    int selectPayBorrowCount(Map<String, Object> params);

    /**
     * 转账订单列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<TransferBorrowDTO> selectTransferBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 交易明细
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    Map<String, Object> transactionDetails(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 转账订单列表分页
     *
     * @param params
     * @return
     */
    int selectTransferBorrowCount(Map<String, Object> params);

    /**
     * 查询支付订单接口
     *
     * @param params
     * @return
     */
    List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 查询支付订单列表分页
     *
     * @param params
     * @return
     */
    int selectBatchBorrowCount(Map<String, Object> params);


    /**
     * 查询账户交易可疑流水
     *
     * @return
     */
    List<QrPayFlowDTO> findAccountDoubleFlow();

    /**
     * 查询账户交易失败流水
     *
     * @return
     */
    List<QrPayFlowDTO> findAccountFailFlow();

    /**
     * 查询三方交易可疑流水
     *
     * @return
     */
    List<QrPayFlowDTO> findThirdDoubtFlow();



    /**
     * 批量修改清算流水信息
     *
     * @param map
     * @return
     */
    int updateClearBatch(Map<String, Object> map);

    /**
     * 修改清算批次状态
     *
     * @param params
     * @param clearBatchDTO
     * @param request
     * @throws BizException
     */
    void updateQrPayClearBatch(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 添加清算批次号
     *
     * @param params
     * @param clearBatchDTO
     * @param request
     * @throws BizException
     */
    void addQrPayClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 查询清算交易信息
     * @param clearMap
     * @return
     */
    QrPayFlowDTO clearTotal(Map<String, Object> clearMap);

    /**
     * 查询用户当日收款明细
     *
     * @param userId
     * @return
     */
    QrPayFlowDTO gathering(Long userId);

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countList(Map<String, Object> params);

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countDetails(Map<String, Object> params);

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countLists(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    JSONObject appQrPayFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    void updateQrPayFlowForConcurrency(Long id, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException ;

    int countMerchantClearList(Map<String, Object> params);

    List<QrPayFlowDTO> merchantClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 创建api订单
     * @param requestInfo
     * @param request
     * @return
     */
    String createApiOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 支付api订单
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject paymentApiOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 订单token解密
     * @param token
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject orderTokenDecode(String token, HttpServletRequest request) throws Exception;

    /**
     * 折扣查询
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject discountRateSearch(HttpServletRequest request) throws Exception;

    /**
     * 订单计划展示
     * @param productId
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject apiOrderRepayment(Long productId, HttpServletRequest request) throws Exception;

    /**
     * api可疑订单跑批
     * @throws Exception
     */
    void paymentApiOrderDoubleHandle() throws Exception;

    /**
     * api订单查询
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    String paymentApiOrderSearchToken(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * api订单查询
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject paymentApiOrderSearch(HttpServletRequest request) throws Exception;

    /**
     * 三方结束平台服务费清算打批号
     * @param params
     * @param clearBatchDTO
     * @param request
     */
    void addApiPlatformClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request);

    /**
     * 查询待清算笔数
     * @param params
     * @return
     */
    int countApiPlatformClear(Map<String, Object> params);

    /**
     * 更新api商户服务费清算状态为成功
     * @param clearMap
     */
    void updateApiPlatformClearData(Map<String, Object> clearMap);

    /**
     * 待清算列表查询
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<ClearBatchDTO> apiPlatformClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 待清算列表详情
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getApiPlatformClearDetail(Map<String, Object> params);

    /**
     * 超时订单关闭
     * @param orderNo
     */
    void apiOrderClosed(String orderNo);

    /**
     * 分期付订单结果处理
     * @param result
     * @param qrPayFlowDTO
     * @param orderState
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject dealCreditOrderResultOld(JSONObject result, QrPayFlowDTO qrPayFlowDTO, Integer orderState, HttpServletRequest request) throws Exception;
    /**
     * 分期付订单结果处理
     * @param result
     * @param qrPayFlowDTO
     * @param orderState
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject dealCreditOrderResult(JSONObject result, QrPayFlowDTO qrPayFlowDTO, Integer orderState, HttpServletRequest request) throws  Exception;

    /**
     * @param list
     * 更新分期付交易清算状态
     * @param request
     */
    void updateClearState(List<OneMerchantClearDataDTO> list, HttpServletRequest request);

    /**
     * 回退清算状态
     *
     * @param clearBatchId
     * @param id
     * @return
     */
    int rollbackClearDetail(Long clearBatchId, Long id);
    /**
     *财富首页查询
     * @param request
     * @param merchantId
     * @return
     * @throws Exception
     */
    Map<String,Object> wealthPageQuery(HttpServletRequest request, Long merchantId) throws  Exception;

    /**
     * 订单管理头部查询
     * @param request
     * @param merchantId
     * @return
     * @throws Exception
     */
    Map<String, Object> orderPageHeadQuery(HttpServletRequest request, Long merchantId) throws  Exception;

    /**
     * 查询订单列表按月分组
     * @param params
     * @param scs
     * @param pc
     * @return
     * @throws Exception
     */
    List<Map<String,Object>> getOrderPageGroupByMonthList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) ;

    /**
     * 整体出售商户订单分页计数
     * @param params
     * @return
     */
    int wholeMerchantOrderSearchCount(Map<String, Object> params);

    /**
     * 整体出售商户订单查询
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<JSONObject> wholeMerchantOrderSearch(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);
    /**
     * 查询订单列表按日分组
     * @param params
     * @param scs
     * @param pc
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> orderPageGroupByDayList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 分期付清算
     * @param list
     * @param request
     */
    void creditClear(List<OneMerchantClearDataDTO> list, HttpServletRequest request) throws BizException;

    /**
     * 查询未清算订单列表
     * @param params
     * @return
     */
    List<Map<String, Object>> getUnClearedList(Map<String, Object> params);

    /**
     * 查询商户所有订单（交易和整体出售）
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowDTO> getAllTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 获取日起始时间
     * @param time
     * @return
     */
    Map<String, Long> getDayStartTime(Long time);

    /**
     * 运营数据查询
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    Map<String, Object> getOperationData(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 统计商户的数量（orderReport用）
     * @param params
     * @return
     */
    int countDistinctMerchant(Map<String, Object> params);

    /**
     * 查询订单报告
     * @param params
     * @return
     */
    List<OrderReportDTO> getOrderReport(Map<String, Object> params, PagingContext pc);

    /**
     * 查询订单报告（包含汇总数据）
     * @param params
     * @param pc
     * @return
     */
    JSONObject getOrderReportTotal(Map<String, Object> params, PagingContext pc);

    /**
     * 统计卡支付数量
     * @param params
     * @return
     */
    int countPayFlowByCard(Map<String, Object> params);

    /**
     * 查询卡支付列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowCardPayDTO> getPayFlowByCard(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 统计分期付支付数量
     * @param params
     * @return
     */
    int countPayFlowByInstalment(Map<String, Object> params);
    /**
     * h5统计分期付支付数量
     * @param params
     * @return
     */
    int countPayFlowByInstalmenth5(Map<String, Object> params);

    /**
     * 查询分期付支付列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalment(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, int isExcel);

    /**
     * h5查询分期付支付列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalmenth5(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, int isExcel);

    /**
     * 扫码支付红包、整体出售出账可疑流水查询
     * @return
     */
    List<QrPayFlowDTO> findBatchAmountOutDoubtFlow();

    /**
     * 清算状态互转
     * @param req
     * @param request
     * @throws Exception
     */
    void changeClearState(ChangeClearStateDTO req, HttpServletRequest request) throws  Exception;

    /**
     * 查询商户清算信息列表
     * @param params
     * @param scs
     * @param pc
     * @param request
     * @return
     */
    List<Map<String ,Object>> merchantClearMessageList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request);

    /**
     * 查询商户未清算信息明细条数
     * @param params
     * @return
     */
    int merchantUnclearDetailCount(Map<String, Object> params);

    /**
     * 查询商户未清算信息明细
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowDTO> merchantUnclearDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 清算记录明细
     * @param
     * @return
     */
    List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    Integer clearedDetailTransFlowCount(Long id);

    /**
     * 查询需要清算的交易条数
     * @param params
     * @return
     */
    int countMerchantClearListNew(Map<String, Object> params);

    /**
     * 导出卡付款excel重新封装
     * @param list
     * @param request
     * @return
     */
    List<ExcelCardPayDTO> repackExportPayFlowByCard(List<QrPayFlowCardPayDTO> list, HttpServletRequest request);

    /**
     * 导出分期付支付列表 重新封装参数
     * @param list
     * @param request
     * @return
     */
    List<ExcelInstPayDTO> repackExportInstPayFlow(List<QrPayFlowInstalmentPayDTO> list, HttpServletRequest request);

    /**
     * 导出订单报告 重新封装参数
     * @param list
     * @return
     */
    List<ExcelOrderDTO> repackExcelOrderDTO(List<OrderReportDTO> list);

    /**
     * 生成每日报告
     */
    void genDailyReport();

    /**
     * 查询清算条数
     * @param params
     * @return
     */
    int getClearMerchantListCount(Map<String, Object> params);

    /**
     * 每月1日做上月用户统计
     */
    void getMonthlyUserSavedTask() throws BizException;


    /**
     * 新用户2个周后还未消费  发送邮件 站内信
     * @author zhangzeyuan
     * @date 2021/5/13 13:41
     */
    void sendMsgUserTwoWeeksNoTransaction() throws Exception;


    /**
     * 有过交易但一一个月已上未有交易 发送邮件
     * @author zhangzeyuan
     * @date 2021/5/13 13:43
     */
    void sendMailOneMonthNoTransaction() throws BizException;


    /**
     * 获得用户已省金额
     * @param userId
     * @param request
     * @return
     */
    JSONObject getUserSavedAmount(Long userId, HttpServletRequest request) throws BizException;


    /**
     * 查询三方交易可疑流水
     *
     * @return
     */
    List<QrPayFlowDTO> getSuspiciousOrderFlowList();


    /**
     * 查询用户支付成功订单数
     * @author zhangzeyuan
     * @date 2021/8/26 10:01
     * @param userId
     * @return java.lang.Long
     */
    Integer countPaidSuccessByUserId(Long userId, Integer transType);


    /**
     * 更新退款数据
     * @param params
     * @param request
     */
    int updateRefundData(Map<String, Object> params, HttpServletRequest request);

    /**
     * 查询H5商户结算条数
     * @param params
     * @return
     */
    int countH5MerchantClearList(Map<String, Object> params);

    /**
     * 增加H5清算流水号
     * @param params
     * @param clearBatchDTO
     * @param request
     */
    void addH5ClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request);

    /**
     * 添加清算明细
     * @param params
     * @param clearBatchDTO
     * @param request
     */
    void addClearDetailFlow(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException;

    /**
     * 查询api商户清算列表
     * @param params
     * @return
     */
    int getApiMerchantClearListCount(Map<String, Object> params);

    /**
     * 查询api商户清算信息
     * @param params
     * @param scs
     * @param pc
     * @param request
     * @return
     */
    List<Map<String, Object>> getApiMerchantClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request);

    /**
     * 清算明细
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrPayFlowDTO> h5ClearTransDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * H5商户清算失败
     * @param clearBatchDTO
     * @param clearDetailDTO
     * @param request
     */
    void h5MerchantSettleFail(ClearBatchDTO clearBatchDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException;



    /**
     * 用户详情订单数量
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     * @param params
     * @return int
     */
    int countUserDetail(Map<String, Object> params);


    /**
     * 用户详情订单列表
     * @author zhangzeyuan
     * @date 2021/9/13 16:10
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.QrPayFlowDTO>
     */
    List<UserDetailPayOrderDTO> listUserDetailOrder(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 查询最近一次使用卡支付分期付使用时间
     * @param result
     * @param request
     * @return
     */
    QrPayFlowDTO findMaxUserUseById(JSONObject result, HttpServletRequest request);

    int apiMerchantUnclearDetailCount(Map<String, Object> params);

    List<QrPayFlowDTO> apiMerchantUnclearDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);
}
