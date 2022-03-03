package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.RechargeBorrowDTO;
import com.uwallet.pay.main.model.dto.RechargeFlowDTO;
import com.uwallet.pay.main.model.entity.RechargeFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 充值交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 充值交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface RechargeFlowDAO extends BaseDAO<RechargeFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RechargeFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RechargeFlowDTO
     * @param params
     * @return
     */
    RechargeFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     *  充值订单列表页
     * @param params
     * @return
     */
    List<RechargeBorrowDTO> selectRechargeBorrow(Map<String, Object> params);

    /**
     *  充值订单列表页分页
     * @param params
     * @return
     */
    int selectRechargeBorrowCount(Map<String, Object> params);

}
