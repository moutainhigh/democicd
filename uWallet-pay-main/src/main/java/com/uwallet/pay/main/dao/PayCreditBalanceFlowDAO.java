package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.PayCreditBalanceFlowDTO;
import com.uwallet.pay.main.model.entity.PayCreditBalanceFlow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 
 * @author: fenmi
 * @date: Created in 2021-07-07 10:38:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: fenmi
 */
@Mapper
public interface PayCreditBalanceFlowDAO extends BaseDAO<PayCreditBalanceFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<PayCreditBalanceFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PayCreditBalanceFlowDTO
     * @param params
     * @return
     */
    PayCreditBalanceFlowDTO selectOneDTO(Map<String, Object> params);

}
