package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.entity.AccountFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:00
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface AccountFlowDAO extends BaseDAO<AccountFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AccountFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AccountFlowDTO
     * @param params
     * @return
     */
    AccountFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据flowId查询最新一条数据
     * @param flowId
     * @param transType
     * @return
     */
    AccountFlowDTO selectLatestByFlowId(Long flowId, Integer transType);

    /**
     * 重写更新方法，添加乐观锁
     * @param accountFlow
     * @return
     */
    int updateForConcurrency(AccountFlow accountFlow);

    /**
     * 查询退款入账回滚失败流水
     * @return
     */
    List<AccountFlowDTO> selectAccountRollbackDoubtFlow();

    /**
     * 查询退款出账可疑流水
     * @return
     */
    List<AccountFlowDTO> selectAccountOutDoubtFlow();

    /**
     * 查询钱包流水
     * @param params
     * @return
     */
    List<AccountFlowDTO> selectWalletBooked(Map<String, Object> params);

    /**
     * 查询钱包交易
     * @param params
     * @return
     */
    List<String> selectWalletTransaction(Map<String, Object> params);

    /**
     * 查询钱包交易分页
     * @param params
     * @return
     */
    int selectWalletTransactionCount(Map<String, Object> params);

    /**
     * 查询钱包交易明细
     * @param params
     * @return
     */
    List<AccountFlowDTO> selectWalletTransactionDetail(Map<String, Object> params);

    /**
     * 查询钱包支出、收入总和
     * @param transType
     * @return
     */
    BigDecimal selectWalletIncomeAndExpend(Map<String, Object> params);

    /**
     * 按orderNo更新流水
     * @param accountFlowDTO
     * @return
     */
    int updateAccountFlowByOrderNo(AccountFlowDTO accountFlowDTO);

    /**
     * 查询关联流水
     * @param params
     * @return
     */
    AccountFlowDTO selectByFlowId(Map<String, Object> params);

    /**
     * 查询批量出账回滚可疑流水
     * @return
     */
    List<AccountFlowDTO> getQrPayBatAmtOutRollbackDoubtFlow();

    /**
     * 查询批量出账回滚失败流水
     * @return
     */
    List<AccountFlowDTO> getQrPayBatAmtOutRollbackFailFlow();

    /**
     * 查询批量出账可疑流水
     * @return
     */
    List<AccountFlowDTO> getBatchAmountOutDoubtFlow();
}
