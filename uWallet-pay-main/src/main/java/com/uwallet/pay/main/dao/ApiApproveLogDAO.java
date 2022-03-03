package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ApiApproveLogDTO;
import com.uwallet.pay.main.model.entity.ApiApproveLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 审核日志
 * @author: zhoutt
 * @date: Created in 2021-09-23 15:39:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ApiApproveLogDAO extends BaseDAO<ApiApproveLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ApiApproveLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ApiApproveLogDTO
     * @param params
     * @return
     */
    ApiApproveLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 审核记录列表查询条数
     * @param params
     * @return
     */
    int countMerchantApprove(Map<String, Object> params);

    /**
     * 审核记录 列表
     * @param params
     * @return
     */
    List<ApiApproveLogDTO> findMerchantApprove(Map<String, Object> params);
}
