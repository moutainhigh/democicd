package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ReconciliationDTO;
import com.uwallet.pay.main.model.dto.ReconciliationDetailDTO;
import com.uwallet.pay.main.model.entity.Reconciliation;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 对账
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.mapper
 * @description: 对账
 * @author: baixinyue
 * @date: Created in 2020-02-17 09:59:08
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ReconciliationDAO extends BaseDAO<Reconciliation> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ReconciliationDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ReconciliationDTO
     * @param params
     * @return
     */
    ReconciliationDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询对账详情列表
     * @return
     */
    List<ReconciliationDetailDTO> findReconciliationDetail(Map<String, Object> params);

    /**
     * 统计对账详情列表
     * @param params
     * @return
     */
    int countReconciliationDetail(Map<String, Object> params);
}
