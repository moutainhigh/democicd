package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ReconciliationBatchDTO;
import com.uwallet.pay.main.model.entity.ReconciliationBatch;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 对账表
 * @author: aaronS
 * @date: Created in 2021-01-25 16:11:20
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Mapper
public interface ReconciliationBatchDAO extends BaseDAO<ReconciliationBatch> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ReconciliationBatchDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ReconciliationBatchDTO
     * @param params
     * @return
     */
    ReconciliationBatchDTO selectOneDTO(Map<String, Object> params);

}
