package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.RefundOrderDTO;
import com.uwallet.pay.main.model.dto.RefundOrderListDTO;
import com.uwallet.pay.main.model.entity.RefundOrder;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.service.RefundOrderService;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 退款订单
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 退款订单
 * @author: zhoutt
 * @date: Created in 2021-08-18 09:01:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface RefundOrderDAO extends BaseDAO<RefundOrder> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RefundOrderDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RefundOrderDTO
     * @param params
     * @return
     */
    RefundOrderDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询分期付可以订单
     * @return
     *
     */
    List<RefundOrderDTO> findCreditRefundDoubt();

    /**
     * 查询时间点之前的退款未清算条数
     * @param params
     * @return
     */
    int getH5MerchantRefundUnclearedCount(Map<String, Object> params);

    /**
     * 增加批次号
     * @param params
     */
    void addClearBatchId(Map<String, Object> params);

    /**
     * 修改清算状态
     * @param map
     */
    void clearData(Map<String, Object> map);

    /**
     * 查询清算数据
     * @param params
     * @return
     */
    List<RefundOrderDTO> merchantClearMsgList(Map<String, Object> params);

    /**
     * 清算回退
     * @param params
     * @return
     */
    int rollbackSettlement(Map<String, Object> params);

    /**
     * 查询退款列表条数
     * @param params
     * @return
     */
    int getRefundsListCount(Map<String, Object> params);

    /**
     * 查询退款列表
     * @param params
     * @return
     */
    List<RefundOrderListDTO> getRefundsList(Map<String, Object> params);
}
