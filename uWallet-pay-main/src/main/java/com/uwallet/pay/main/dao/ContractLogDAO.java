package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ContractLogDTO;
import com.uwallet.pay.main.model.entity.ContractLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 合同记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 合同记录表
 * @author: xucl
 * @date: Created in 2021-04-27 10:13:42
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface ContractLogDAO extends BaseDAO<ContractLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ContractLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ContractLogDTO
     * @param params
     * @return
     */
    ContractLogDTO selectOneDTO(Map<String, Object> params);

}
