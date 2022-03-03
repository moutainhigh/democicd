package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.QrPayFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 扫码支付交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 扫码支付交易流水表
 * @author: zhoutt
 * @date: Created in 2019-12-13 18:00:26
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface QrPayFlowDAO extends BaseDAO<QrPayFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<QrPayFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 商户资金清算，包含分页和排序信息
     * @param params
     * @return
     */
    List<QrPayFlowDTO> qrPayFlowList(Map<String, Object> params);

    /**
     * 根据id查询一条 QrPayFlowDTO
     * @param params
     * @return
     */
    QrPayFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询支付订单接口
     * @param params
     * @return
     */
    List<PayBorrowDTO> selectPayBorrow(Map<String, Object> params);

    /**
     * 查询支付订单列表分页
     * @param params
     * @return
     */
    int selectPayBorrowCount(Map<String, Object> params);

    /**
     * 查询转账订单接口
     * @param params
     * @return
     */
    List<TransferBorrowDTO> selectTransferBorrow(Map<String, Object> params);

    /**
     * 查询转账订单列表分页
     * @param params
     * @return
     */
    int selectTransferBorrowCount(Map<String, Object> params);

    /**
     * 查询支付订单接口
     * @param params
     * @return
     */
    List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params);

    /**
     * 查询支付订单列表分页
     * @param params
     * @return
     */
    int selectBatchBorrowCount(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countQrPayFlowList(Map<String, Object> params);

    /**
     * 根据id查询 List<QrPayFlowDTO>
     * @param params
     * @return
     */
    List<QrPayFlowDTO> qrPayFlowListDetails(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppTransactionDetailsDTO> transactionDetails(Map<String, Object> params);

    /**
     * 根据id查询 List<QrPayFlowDTO>
     * @param params
     * @return
     */
    List<QrPayFlowDTO> appQrPayFlowListRecAmountTotal(Map<String, Object> params);

    /**
     * 根据id查询 QrPayFlowDTO
     * @param params
     * @return
     */
    QrPayFlowDTO recAmountTotal(Map<String, Object> params);

    /**
     * 根据id查询 QrPayFlowDTO
     * @param params
     * @return
     */
    QrPayFlowDTO appQrPayFlowUnRecAmountTotal(Map<String, Object> params);

    /**
     * 查询账户入账可疑流水
     * @return
     */
    List<QrPayFlowDTO> findAccountDoubleFlow();

    /**
     * 查询账户入账失败流水
     * @return
     */
    List<QrPayFlowDTO> findAccountFailFlow();
    /**
     * 查询三方交易可疑流水
     * @return
     */
    List<QrPayFlowDTO> findThirdDoubtFlow();

    /**
     * 更新清算批次流水状态
     * @param updateMap
     * @return
     */
    int updateClearBatch(Map<String, Object> updateMap);

    QrPayFlowDTO clearTotal(Map<String, Object> clearMap);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countList(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countDetails(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countLists(Map<String, Object> params);

    /**
     * 根据id查询 List<QrPayFlowDTO>
     * @param params
     * @return
     */
    List<QrPayFlowDTO> appQrPayFlowList(Map<String, Object> params);

    /**
     * 更新交易状态，防止并发更新
     * @param qrPayFlow
     * @return
     */
    int updateForConcurrency(QrPayFlow qrPayFlow);

    /**
     * 查询订单已退款金额
     * @param flowId
     * @return
     */
    BigDecimal selectOrderRefund(Long flowId);

    /**
     * 根据交易渠道查询未清算订单金额
     * @param gatewayId
     * @return
     */
    BigDecimal selectUnClearByGateway(@Param("gatewayId") Long gatewayId, @Param("userId") Long userId);

    /**
     * 支付清算打批号
     * @param updateMap
     */
    void addQrPayClearBatchId(Map<String, Object> updateMap);

    /**
     * 查询结算条数
     * @param params
     * @return
     */
    Integer countMerchantClearList(Map<String, Object> params);

    /**
     * 查询商户结算列表
     * @param params
     * @return
     */
    List<QrPayFlowDTO> merchantClearList(Map<String, Object> params);

    /**
     * 三方平台服务费清算查询条数
     * @param params
     * @return
     */
    Integer countApiPlatformClear(Map<String, Object> params);

    /**
     * 插入三方平台服务费清算批号
     * @param params
     */
    void addApiPlatformClearBatchId(Map<String, Object> params);

    /**
     * 三方平台服务费待清算清算查询列表
     * @param params
     * @return
     */
    List<ClearBatchDTO> apiPlatformClearList(Map<String, Object> params);

    /**
     * 三方平台服务费待清算详情
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getApiPlatformClearDetail(Map<String, Object> params);

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
    List<JSONObject> wholeMerchantOrderSearch(Map<String, Object> params);


    /**
     * 回退清算状态
     * @param batchId
     * @param userId
     * @param time
     * @return
     */
    int rollbackClearDetail(@Param("batchId") Long batchId, @Param("userId") Long userId,  @Param("time") Long time);

    /**
     * 查询未清算列表
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getUnClearedList(Map<String, Object> params);

    /**
     * 运营管理交易统计
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getOperationTransData(Map<String, Object> params);
    /**
     * 运营管理清算统计
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getOperationClearData(Map<String, Object> params);

    /**
     * 计算该User 支付记录条数
     * @param param
     * @return
     */
    int countTransactionRecord(JSONObject param);

    /**
     * 获取交易明细列表
     * @param params
     * @return
     */
    List<AppTransactionDetailsDTO> getTransRecord(Map<String, Object> params);

    /**
     * 交易明细数据列表变为不可见
     * @param param
     */
    void updateRecordIsShow(JSONObject param);

    /**
     * 获取交易详情
     * @param id
     * @return
     */
    QrPayFlowDTO getRecordDetail(@Param("id") Long id, @Param("creditNo") String creditNo);

    /**
     * 扫码支付红包、整体出售出账可疑流水处理
     * @return
     */
    List<QrPayFlowDTO> findBatchAmountOutDoubtFlow();

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
    List<OrderReportDTO> getOrderReport(Map<String, Object> params);

    /**
     * 统计卡支付数量
     * @return
     */
    int countPayFlowByCard(Map<String, Object> params);

    /**
     * 查询卡支付列表
     * @param params
     * @return
     */
    List<QrPayFlowCardPayDTO> getPayFlowByCard(Map<String, Object> params);

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
     * @return
     */
    List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalment(Map<String, Object> params);
    /**
     * h5查询分期付支付列表
     * @param params
     * @return
     */
    List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalmenth5(Map<String, Object> params);


    /**
     * 更新清算状态
     * @param params
     */
    void changeClearState(Map<String, Object> params);

    /**
     * 商户清算信息列表
     * @param params
     * @return
     */
    List<QrPayFlowDTO> merchantClearMessageList(Map<String, Object> params);

    /**
     * 商户清算信息列表
     * @param params
     * @return
     */
    int merchantUnclearDetailCount(Map<String, Object> params);

    /**
     * 查询未清算明细
     * @param params
     * @return
     */
    List<QrPayFlowDTO> merchantUnclearDetailList(Map<String, Object> params);

    /**
     * 已清算明细
     * @param
     * @return
     */
    List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params);

    int clearedDetailTransFlowCount(@Param("id") Long id);

    /**
     * 查询清算条数
     * @param params
     * @return
     */
    int countMerchantClearListNew(Map<String, Object> params);

    /**
     * 更新流水清算批次好
     * @param updateMap
     */
    void addQrPayClearBatchIdNew(Map<String, Object> updateMap);

    /**
     * 获取清算商户条数
     * @param params
     * @return
     */
    int getClearMerchantListCount(Map<String, Object> params);

    /**
     *  上月用户统计
     * @param params
     * @return
     */
    List<QrPayFlowDTO> getMonthlyUserData(Map<String, Object> params);

    /**
     * 计算交易记录条数
     * @param data
     * @return
     */
    int countRecordNew(JSONObject data);

    /**
     * get trans record list by search conditions
     * @param params
     * @return
     */
    List<JSONObject> transactionDetailsNew(JSONObject params);


    /**
     * 获取一个月没有交易的用户数据
     * @author zhangzeyuan
     * @date 2021/5/14 15:31
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDTO>
     */
    List<UserDTO> getAMonthNoTransactionUserList(long startTime, long endTime);



    /**
     *  获取2周前注册的用户且 没交易记录
     * @author zhangzeyuan
     * @date 2021/5/13 15:37
     * @param timestamp
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDTO>
     */
    List<UserDTO> getNoTransactionUserListByTwoWeeksAgo(long timestamp);

    /**
     * 查询三方交易可疑流水
     *
     * @return
     */
    List<QrPayFlowDTO> getSuspiciousOrderFlowList();



    /**
     * 查询用户成功支付订单数量
     * @author zhangzeyuan
     * @date 2021/8/26 9:59
     * @param userId
     * @return int
     */
    int countPaidSuccessByUserId(@Param("userId")Long userId, @Param("transType")Integer transType);
    /**
     * 退款成功更新
     * @param params
     */
    int updateRefundData(Map<String, Object> params);

    /**
     * 查询H5商户清算数量
     * @param params
     * @return
     */
    Integer countH5MerchantClearList(Map<String, Object> params);

    /**
     * 查询清算商户条数
     * @param params
     * @return
     */
    Integer getApiMerchantClearListCount(Map<String, Object> params);

    /**
     * H5商户清算明细查询
     * @param params
     * @return
     */
    List<QrPayFlowDTO> h5ClearTransDetail(Map<String, Object> params);

    /**
     * 回退清算流水
     * @param batchId
     * @param merchantId
     * @param time
     */
    int rollbackClearDetail2(@Param("batchId") Long batchId, @Param("merchantId") Long merchantId,  @Param("time") Long time);



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
     * @return java.util.List<com.uwallet.pay.main.model.dto.QrPayFlowDTO>
     */
    List<UserDetailPayOrderDTO> listUserDetailOrder(Map<String, Object> params);


    /**
     * 查询用户最近一次消费记录
     * @param result
     * @return
     */
    QrPayFlowDTO findMaxUserUseById(JSONObject result);


    /**
     * api商户待清算数据查询
     * @param params
     * @return
     */
    List<QrPayFlowDTO> apiMerchantClearMessageList(Map<String, Object> params);

    int apiMerchantUnclearDetailCount(Map<String, Object> params);

    List<QrPayFlowDTO> apiMerchantUnclearDetailList(Map<String, Object> params);
}
