package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ApiQrPayFlowDTO;
import com.uwallet.pay.main.model.entity.ApiQrPayFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import lombok.NonNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * api交易订单流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: api交易订单流水表
 * @author: caishaojun
 * @date: Created in 2021-08-17 15:50:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: caishaojun
 */
@Mapper
public interface ApiQrPayFlowDAO extends BaseDAO<ApiQrPayFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ApiQrPayFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ApiQrPayFlowDTO
     * @param params
     * @return
     */
    ApiQrPayFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 修改订单流水表状态
     * @param id
     * @return
     */
//    int updateApiQrPayFlow(@Param("id") String id,
//                           @Param("donateAmount") String donateAmount,
//                           @Param("orderStatus") String orderStatus,
//                           @Param("userId") Long userId,
//                           @Param("cardId") Long cardId);
    /**
     *  根据apiTransNo 查询一条数据
     * @param params
     * @return
     */
    ApiQrPayFlowDTO selectOneDTOV2(Map<String, Object> params);

    List<ApiQrPayFlowDTO> selectMapPayments(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int counts(Map<String, Object> params);

    ApiQrPayFlowDTO paymentsId(@Param("id") Long id);

    int redisUpdateDoubtHandle();
}
