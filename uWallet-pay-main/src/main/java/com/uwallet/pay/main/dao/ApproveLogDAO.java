package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.ApproveLogDTO;
import com.uwallet.pay.main.model.entity.ApproveLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 审核日志表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 审核日志表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:34:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface ApproveLogDAO extends BaseDAO<ApproveLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ApproveLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ApproveLogDTO
     * @param params
     * @return
     */
    ApproveLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 统计符合条件的商户变更审核条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchantApprove(Map<String, Object> params);

    /**
     * 根据查询条件得到商户变更审核列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @return 查询结果的数据集合
     */
    List<ApproveLogDTO> findMerchantApprove(Map<String, Object> params);

}
