package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.CreateCreditOrderFlowDTO;
import com.uwallet.pay.main.model.entity.CreateCreditOrderFlow;
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
 * @author: zhangzeyuan
 * @date: Created in 2021-07-07 11:21:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface CreateCreditOrderFlowDAO extends BaseDAO<CreateCreditOrderFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<CreateCreditOrderFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 CreateCreditOrderFlowDTO
     * @param params
     * @return
     */
    CreateCreditOrderFlowDTO selectOneDTO(Map<String, Object> params);

}
