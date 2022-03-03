package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.WithholdFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 代收三方流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 代收三方流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:50:03
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface WithholdFlowDAO extends BaseDAO<WithholdFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<WithholdFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<WithholdFlowDTO> withholdFlowList(Map<String, Object> params);

    /**
     * 根据id查询一条 WithholdFlowDTO
     * @param params
     * @return
     */
    WithholdFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countWithholdFlowList(Map<String, Object> params);

    /**
     * 查询流水记录
     * @param flowIds
     * @return
     */
    List<Map> selectResults(Long[] flowIds);

    int updateForConcurrency(WithholdFlow withholdFlow);

    /**
     *  查询with_hold_flow表数据,transactionId = flowId  拿出orderNo就是 结算交易流水号
     * @param transactionId
     * @return
     */
    String getOrderNoByTransId(@Param("transactionId") Long transactionId);
}
