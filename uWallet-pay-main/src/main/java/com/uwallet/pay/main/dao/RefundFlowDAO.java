package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;
import com.uwallet.pay.main.model.dto.RefundDetailListDTO;
import com.uwallet.pay.main.model.dto.RefundFlowDTO;
import com.uwallet.pay.main.model.dto.RefundListDTO;
import com.uwallet.pay.main.model.entity.RefundFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 退款流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.mapper
 * @description: 退款流水表
 * @author: baixinyue
 * @date: Created in 2020-02-07 15:56:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface RefundFlowDAO extends BaseDAO<RefundFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RefundFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RefundFlowDTO
     * @param params
     * @return
     */
    RefundFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     *
     * @return
     */
    List<RefundFlowDTO> selectFlowDTO();

    int addQrPayClearBatchId(Map<String, Object> updateMap);

    int countClearList(Map<String, Object> params);

    int clearData(Map<String, Object> map);

    int updateAmountOut(Map<String, Object> map);

    List<RefundFlowDTO> selectReason(Long flowId);

    int updateRefundFlowToCheckFail(Long id, long updateTime);

    RefundFlowDTO getUnCleared(Long userId, int gateWayId);

    List<RefundFlowDTO> findAllUnClearedRefundFlow(Map<String, Object> map);

    /**
     * 退款管理列表页
     * @param params
     * @return
     */
    List<RefundListDTO> selectRefund(Map<String, Object> params);

    /**
     * 退款管理列表页总条数
     * @param params
     * @return
     */
    int selectRefundCount(Map<String, Object> params);

    /**
     * 退款明细列表页
     * @param params
     * @return
     */
    List<RefundDetailListDTO> selectRefundDetail(Map<String, Object> params);

    /**
     * 退款明细列表页总条数
     * @param params
     * @return
     */
    int selectRefundDetailCount(Map<String, Object> params);

    /**
     * 更新补款状态
     * @param ids
     * @param state
     * @return
     */
    int updateMakeUpState(@Param("state") Integer state);

    /**
     * 补款导出信息
     * @return
     */
    List<ClearBillCSVDTO> makeUpCSVData();

    int updateClearBatch(Map<String, Object> map);
}
