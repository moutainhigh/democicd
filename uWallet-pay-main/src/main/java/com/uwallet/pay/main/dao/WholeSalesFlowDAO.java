package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.main.model.dto.WholeSalesFlowDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.WholeSalesFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 整体销售流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 整体销售流水表
 * @author: zhoutt
 * @date: Created in 2020-10-17 14:33:54
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface WholeSalesFlowDAO extends BaseDAO<WholeSalesFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<WholeSalesFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 WholeSalesFlowDTO
     * @param params
     * @return
     */
    WholeSalesFlowDTO selectOneDTO(Map<String, Object> params);

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
    List<WholeSalesFlowDTO> wholeSaleInterestOrderList(Map<String, Object> params);

    /**
     * 整体出售订单分页
     * @param params
     * @return
     */
    int wholeSaleOrderCount(Map<String, Object> params);

    /**
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<WholeSalesFlowDTO> wholeSaleOrderList(Map<String, Object> params);

    /**
     * 查询商户全部出售金额
     * @param merchantId
     * @return
     */
    BigDecimal merchantWholeSaleTotalAmount(@Param("merchantId") Long merchantId);

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
    List<String> appWholeSaleOrder(Map<String, Object> params);

    /**
     * 查询最新整体出售订单
     * @param params
     * @return
     */
    WholeSalesFlowDTO findLatestWholeSaleFlowDTO(Map<String, Object> params);

    /**
     * 查询已结算详情
     * @param id
     * @return
     */
    List<WholeSalesFlowDTO> clearedDetailTransFlowList(Map<String, Object> params);

    int clearedDetailTransFlowCount(Long id);

    /**
     * 查询清算数据条数
     * @param params
     * @return
     */
    int countMerchantClearList(Map<String, Object> params);

    /**
     * 记录清算流水批次号
     * @param params
     * @return
     */
    int addClearBatchId(Map<String, Object> params);

    /**
     * 获取清算批次信息
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDataByBatchId(Long id);

    /**
     * 处理整体出售的清算结果
     * @param id
     * @param time
     * @return
     */
    int dealWholeSaleClear(@Param("id") Long id ,@Param("time") Long time);

    /**
     * 获取商户结算信息
     * @param params
     * @return
     */
    List<WholeSalesFlowDTO> merchantClearMessageList(Map<String, Object> params);

    /**
     * 更新批次流水为失败
      * @param params
     * @return
     */
    int updateClearBatchToFail(Map<String, Object> params);

    /**
     * 获取清算条数
     * @param params
     * @return
     */
    int getMerchantClearMessageCount(Map<String, Object> params);
}
