package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ClearReconciliationDTO;
import com.uwallet.pay.main.model.entity.ClearReconciliation;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 清算对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 清算对账表
 * @author: baixinyue
 * @date: Created in 2020-03-06 09:00:14
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ClearReconciliationDAO extends BaseDAO<ClearReconciliation> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ClearReconciliationDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ClearReconciliationDTO
     * @param params
     * @return
     */
    ClearReconciliationDTO selectOneDTO(Map<String, Object> params);

}
